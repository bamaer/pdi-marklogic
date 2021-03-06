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

package org.pentaho.kettle.steps.marklogicinput;

import org.pentaho.di.core.database.marklogic.MarkLogicDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;

import com.marklogic.client.datamovement.ExportListener;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.util.RequestLogger;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * MarkLogic Document Input Step
 *
 * @author Adam Fowler {@literal <adam.fowler@hitachivantara.com>}
 * @since 1.0 11-01-2018
 */
public class MarkLogicInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MarkLogicInputMeta.class; // for i18n purposes, needed by Translator2!!

  private MarkLogicInputMeta meta;
  private MarkLogicInputData data;

  //private boolean first = true;

  /**
   * Standard constructor
   */
  public MarkLogicInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

    meta = (MarkLogicInputMeta) getStepMeta().getStepMetaInterface();
    data = (MarkLogicInputData) stepDataInterface;
  }

  /**
   * Processes a single row in the PDI stream
   */
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    meta = (MarkLogicInputMeta) smi;
    data = (MarkLogicInputData) sdi;

    Object[] r = getRow(); // get row, set busy!

    if (null == r) {
      logRowlevel("Processing last MarkLogic row");

      // no more input to be expected...
      data.batcher.awaitCompletion();

      logRowlevel("Processing last MarkLogic row completed");
      setOutputDone();
      return false;
    }

    if (first) {
      first = false;
      logRowlevel("Processing first MarkLogic row");
      logRowlevel("Collection: " + meta.getCollection());
      logRowlevel("Doc URI Field: " + meta.getDocumentUriField());
      logRowlevel("Format Field: " + meta.getFormatField());
      logRowlevel("Doc Content Field: " + meta.getDocumentContentField());
      logRowlevel("Mime Type Field: " + meta.getMimeTypeField());

      data.inputRowMeta = getInputRowMeta();//.clone();
      //data.outputRowMeta = getInputRowMeta().clone();
      //data.outputRowMeta = new RowMeta();
      //Class c = data.inputRowMeta.getClass();
      //logDebug("input row meta class: " + c.getName());
      //c = data.outputRowMeta.getClass();
      //logDebug("output row meta clone class: " + c.getName());
      meta.getFields( getInputRowMeta(), getStepname(), null, null, this, repository, metaStore );
      data.outputRowMeta = getInputRowMeta().clone(); // modified by previous step (getFields), so MUST be called AFTER it

      // copy field definitions over from input to output (as per field analysis plugin)

      //data.outputRowMeta = new RowMeta();
      /*
      RowMetaInterface irm = getInputRowMeta();
      List<ValueMetaInterface> ml = irm.getValueMetaList();
      for (ValueMetaInterface vmi: ml) {
        logRowlevel("Value Meta: " + vmi.getName());
        data.outputRowMeta.addValueMeta(vmi);
      }
      */
      /*
      data.outputRowMeta.addValueMeta(new ValueMeta( "Collection", ValueMetaInterface.TYPE_STRING ));
      data.outputRowMeta.addValueMeta(new ValueMeta( "Content", ValueMetaInterface.TYPE_STRING ));
      data.outputRowMeta.addValueMeta(new ValueMeta( "MimeType", ValueMetaInterface.TYPE_STRING ));
      data.outputRowMeta.addValueMeta(new ValueMeta( "Format", ValueMetaInterface.TYPE_STRING ));
      data.outputRowMeta.addValueMeta(new ValueMeta( "Uri", ValueMetaInterface.TYPE_STRING ));
      data.outputRowMeta.addValueMeta(new ValueMeta( "OK", ValueMetaInterface.TYPE_STRING ));
      */
      

      // get IDs of fields we require
      String colField = meta.getCollection();
      if (null != colField) {
        data.collectionFieldId = data.inputRowMeta.indexOfValue(colField);
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
        data.client = ((MarkLogicDatabaseMeta) meta.getDatabaseMeta().getDatabaseInterface()).getConnection();
      } catch (Exception e) {
        logError(BaseMessages.getString(PKG, "MarkLogicInput.Log.CannotConnect"), e);
      }

      //data.client = ((MarkLogicDatabaseMeta)meta.getDatabaseMeta()).getConnection();

      // TODO redirect MarkLogic logger to our row level logger (somehow...)
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      RequestLogger rl = data.client.newLogger(baos);
      rl.setEnabled(false);

      data.dmm = data.client.newDataMovementManager();

      StructuredQueryBuilder qb = data.client.newQueryManager().newStructuredQueryBuilder();

      data.batcher = data.dmm.newQueryBatcher(
        qb.collection((String) r[data.inputRowMeta.indexOfValue(meta.getCollection())] ) 
      );

// TODO add batchcount and threadcount to ML database config
      data.batcher.withConsistentSnapshot().withBatchSize(100).withThreadCount(3).onUrisReady(
        new ExportListener().onDocumentReady(doc -> {
          //logRowlevel("Retrieving MarkLogic Document: " + doc.getUri());
          // 1. Create new results row for output
          Object[] outputRowData;
          /*
          Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

          // copy input fields to output fields
          for (int i = 0;i < outputRowData.length;i++) {
            outputRowData[i] = r[i];
          }*/

          outputRowData = RowDataUtil.createResizedCopy(r, data.outputRowMeta.size() ); //.outputRowMeta.size());
          //outputRowData = r;

          //logDebug("r row data num fields: " + r.length);
          //logDebug("output row data num fields: " + outputRowData.length);

          // 2. Fill data in new result row
          String uri = doc.getUri();
          if (-1 != data.docUriFieldId) {
            outputRowData[data.docUriFieldId] = uri;
          }
          //String collection = (String)r[data.collectionFieldId];
          //if (-1 != data.collectionFieldId) {
            //outputRowData[data.collectionFieldId] = collection;
          //}
          if (-1 != data.mimeTypeFieldId) {
            outputRowData[data.mimeTypeFieldId] = (String)doc.getMimetype();
          }
          if (-1 != data.formatFieldId) {
            outputRowData[data.formatFieldId] = (String)doc.getFormat().toString();
          }
          //String uriParts[] = doc.getUri().split("/");
          // check that content was requested (not just URIs or metadata)
          if (-1 != data.docContentFieldId) {
            try {
              StringHandle content = new StringHandle();
              doc.getContent(content);
              //Files.write(Paths.get(EX_DIR, "output", uriParts[uriParts.length - 1]),
              //  doc.getContent(new StringHandle()).toBuffer());
  
              // output content to content field
              outputRowData[data.docContentFieldId] = content.toString();

            } catch (Exception e) {
              e.printStackTrace();
            }
          } // end if content requested if

          //outputRowData[5] = Boolean.TRUE;

          // 3. putRow
          try {
            putRow(data.outputRowMeta, outputRowData);
          } catch (KettleStepException kse) {
            logError(BaseMessages.getString(PKG, "MarkLogicInput.Log.ExceptionPuttingRow"), kse);
          }
        })
      ).onQueryFailure(exception -> exception.printStackTrace());

      // start the job and feed input to the batcher
      data.dmm.startJob(data.batcher);
      logRowlevel("Processing first MarkLogic query row completed");

    } // end if for first row (initialisation based on row data)

    // Do something to this row's data (create row for BigQuery, and append to current stream)
    //data.inputRowMeta = getInputRowMeta();
/*
    // TODO replace the below with fetching the four fields we care about
    int docUriFieldId = data.inputRowMeta.indexOfValue(meta.getDocumentUriField());
    String uri = null;
    if (-1 != docUriFieldId) {
      // got URI
      uri = (String) r[docUriFieldId];
    }
    logRowlevel("DocUri: " + uri);
    String docFormat = (String) r[data.inputRowMeta.indexOfValue(meta.getFormatField())];
    logRowlevel("DocFormat: " + docFormat);
    String docContent = (String) r[data.inputRowMeta.indexOfValue(meta.getDocumentContentField())];
    logRowlevel("DocContent: " + docContent);
    String docMime = (String) r[data.inputRowMeta.indexOfValue(meta.getMimeTypeField())];
    logRowlevel("DocMime: " + docMime);

    // now load JSON / XML / text
    StringHandle handle = new StringHandle(docContent);
    if (null != uri) {
      data.batcher.add(uri, new DocumentMetadataHandle().withCollections(meta.getCollection()), handle);
      ////} else {
      ////  data.batcher.add(handle);
    }
    // TODO handle binary
    */


    // Execute request and then create a row for each document



    // Don't output incoming row - done for us in the first row invocation
    // putRow(data.outputRowMeta, r);

    if (checkFeedback(getLinesRead())) {
      if (log.isBasic()) {
        logBasic(BaseMessages.getString(PKG, "MarkLogicInput.Log.LineNumber") + getLinesRead());
      }
    }

    return true;
  }

  /**
   * Initialises the data for the step (meta data and runtime data)
   */
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (MarkLogicInputMeta) smi;
    data = (MarkLogicInputData) sdi;

    if (super.init(smi, sdi)) {

      return true;
    }
    return false;
  }
  
  @Override
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    //meta = (MarkLogicInputMeta) smi;
    //data = (MarkLogicInputData) sdi;

    //data.outputRowData = null;
    data.outputRowMeta = null;
    data.inputRowMeta = null;
    data.client = null;
    data.batcher = null;
    data.dmm = null;
    data.docUriFieldId = -1;
    data.docContentFieldId = -1;
    data.mimeTypeFieldId = -1;
    data.formatFieldId = -1;

    logRowlevel("dispose called on MarkLogicInput step");

    super.dispose(smi, sdi);
  }
  
}
