/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.kettle.steps.marklogicoutput;

import org.pentaho.di.core.database.marklogic.MarkLogicDatabaseMeta;
import org.pentaho.di.core.database.marklogic.MarkLogicConnectionFactory.AuthScheme;
import org.pentaho.di.core.database.marklogic.MarkLogicConnectionFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.util.RequestLogger;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClientFactory.DigestAuthContext;

import java.io.ByteArrayOutputStream;

/**
 * MarkLogic Document Output Step
 *
 * @author afowler
 * @since 01-12-2017
 */
public class MarkLogicOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MarkLogicOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private MarkLogicOutputMeta meta;
  private MarkLogicOutputData data;

  private boolean first = true;

  /**
   * Standard constructor
   */
  public MarkLogicOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Processes a single row in the PDI stream
   */
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MarkLogicOutputMeta) smi;
    data = (MarkLogicOutputData) sdi;

    Object[] r = getRow(); // get row, set busy!

    if ( null == r ) {
      logRowlevel("Processing last MarkLogic row");
      // no more input to be expected...

      data.batcher.flushAndWait();
      data.dmm.stopJob( data.batcher );
      logRowlevel("Processing last MarkLogic row completed");
      return false;
    }

    if ( first ) {
      first = false;
      logRowlevel("Processing first MarkLogic row");
      logRowlevel("Collection Field: " + meta.getCollectionField());
      logRowlevel("Doc URI Field: " + meta.getDocumentUriField());
      logRowlevel("Format Field: " + meta.getFormatField());
      logRowlevel("Doc Content Field: " + meta.getDocumentContentField());
      logRowlevel("Mime Type Field: " + meta.getMimeTypeField());

      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      String collectionField = meta.getCollectionField();
      if (null != collectionField) {
        data.collectionFieldId = data.inputRowMeta.indexOfValue(collectionField);
      }
      String docUriField = meta.getDocumentUriField();
      if (null != docUriField) {
        data.docUriFieldId = data.inputRowMeta.indexOfValue(docUriField);
      }
      String docContentField = meta.getDocumentContentField();
      if (null != docContentField) {
        data.docContentFieldId = data.inputRowMeta.indexOfValue(docContentField);
      }
      String mimeType = meta.getMimeTypeField();
      if (null != mimeType) {
        data.mimeTypeFieldId = data.inputRowMeta.indexOfValue(mimeType);
      }
      String format = meta.getFormatField();
      if (null != format) {
        data.formatFieldId = data.inputRowMeta.indexOfValue(format);
      }

      // create connection
      /*
      data.client = DatabaseClientFactory.newClient( 
        (String)meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_HOST), 
          Integer.parseInt((String)meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PORT)),
        new DatabaseClientFactory.DigestAuthContext(
          (String)meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_USERNAME) , 
          (String)meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PASSWORD) 
        )
      );
      */
      //String asString = (String) meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_AUTHSCHEME);
      //AuthScheme as = AuthScheme.DIGEST;
      //if ("basic".equals(asString)) {
      //  as = AuthScheme.BASIC;
      //}
      try {

        //data.client = MarkLogicConnectionFactory.create(
        //  (String) meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_HOST),
        //  Integer.parseInt((String) meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PORT)), 
        //  as,
        //  (String) meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_USERNAME),
        //  (String) meta.getDatabaseMeta().getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PASSWORD)
        //);
        data.client = ((MarkLogicDatabaseMeta)meta.getDatabaseMeta().getDatabaseInterface()).getConnection();
      } catch (Exception e) {
        logError(BaseMessages.getString(PKG, "MarkLogicOutput.Log.CannotConnect"),e);
      }

      //data.client = ((MarkLogicDatabaseMeta)meta.getDatabaseMeta()).getConnection();

      // TODO redirect MarkLogic logger to our row level logger (somehow...)
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      RequestLogger rl = data.client.newLogger( baos );
      rl.setEnabled( false );

      data.dmm = data.client.newDataMovementManager( );

      data.batcher = data.dmm.newWriteBatcher( );

      data.batcher.withBatchSize( 100 ).withThreadCount( 3 ); // no lambdas today

      // start the job and feed input to the batcher
      data.dmm.startJob( data.batcher );
      logRowlevel("Processing first MarkLogic row completed");

    } // end if for first row (initialisation based on row data)

    // Do something to this row's data (create row for BigQuery, and append to current stream)
    data.inputRowMeta = getInputRowMeta();

    // TODO replace the below with fetching the four fields we care about
    String uri = null;
    if ( -1 != data.docUriFieldId ) {
      // got URI
      uri = (String) r[data.docUriFieldId];
    }
    //logRowlevel("DocUri: " + uri);
    String docFormat = (String) r[data.formatFieldId ];
    //logRowlevel("DocFormat: " + docFormat);
    String docContent = (String) r[data.docContentFieldId ];
    //logRowlevel("DocContent: " + docContent );
    String docMime = (String) r[data.mimeTypeFieldId];
    //logRowlevel("DocMime: " + docMime );

    // now load JSON / XML / text
    StringHandle handle = new StringHandle( docContent );
    if (null != uri) {
      data.batcher.add( uri, new DocumentMetadataHandle().withCollections((String)r[data.collectionFieldId]),handle );
    ////} else {
    ////  data.batcher.add(handle);
    }
    // TODO handle binary


    // Also copy rows to output
    putRow( data.outputRowMeta, r );

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "MarkLogicOutput.Log.LineNumber" ) + getLinesRead( ) );
      }
    }

    return true;
  }


  /**
   * Initialises the data for the step (meta data and runtime data)
   */
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    first = true;

    meta = (MarkLogicOutputMeta) smi;
    data = (MarkLogicOutputData) sdi;

    if ( super.init( smi, sdi ) ) {


      return true;
    }
    return false;
  }
}
