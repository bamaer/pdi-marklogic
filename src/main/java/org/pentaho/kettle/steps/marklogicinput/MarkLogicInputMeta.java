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

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.marklogic.MarkLogicDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;

import org.w3c.dom.Node;

@Step(id = "MarkLogicInput", image = "marklogic_output.svg", i18nPackageName = "org.pentaho.kettle.steps.MarkLogicInput", name = "MarkLogicInput.Name", description = "MarkLogicInput.Description", categoryDescription = "BaseStep.Category.BigData")
/**
 * Metadata (configuration) holding class for the MarkLogic input step
 * 
 * @author Adam Fowler {@literal <adam.fowler@marklogic.com>}
 * @since 1.0 11-01-2018
 */
public class MarkLogicInputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = MarkLogicInputMeta.class; // for i18n purposes, needed by Translator2!!

  public final String FORMAT_JSON = "json";
  public final String FORMAT_XML = "xml";
  public final String FORMAT_TEXT = "text";
  public final String FORMAT_BINARY = "binary";

  /**
   * The connection to the database
   */
  private DatabaseMeta databaseMeta;

  private String host = "localhost";
  private int port = 8000;
  private String username = "admin";
  private String password = "password";
  private String collection = "";
  private String formatField = null;
  private String mimeTypeField = null;
  private String documentUriField = null;
  private String documentContentField = null;

  @Override
  /**
   * Loads step configuration from PDI ktr file XML
   */
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
    readData(stepnode, databases);
  }

  @Override
  /**
   * Clones this meta class instance in PDI
   */
  public Object clone() {
    MarkLogicInputMeta retval = (MarkLogicInputMeta) super.clone();
    return retval;
  }

  /**
   * Sets default metadata configuration
   */
  public void setDefault() {
    databaseMeta = null;
  }

  @Override
  /**
   * Adds any additional fields to the stream
   */
  public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space) {
    // we don't add any, so leave blank
    // TODO add options for http status (success/failure) fields per document / row
  }

  /**
   * @return Returns the database.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param database
   *          The database to set.
   */
  public void setDatabaseMeta(DatabaseMeta database) {
    this.databaseMeta = database;
  }

  /**
   * Actually read the XML stream (used by loadXML())
   */
  private void readData(Node entrynode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
    try {
      //host = XMLHandler.getTagValue(entrynode, "host");
      //port = Integer.parseInt(XMLHandler.getTagValue(entrynode, "port"));
      //username = XMLHandler.getTagValue(entrynode, "username");
      //password = XMLHandler.getTagValue(entrynode, "password");
      databaseMeta = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(entrynode, "connection"));
      collection = XMLHandler.getTagValue(entrynode, "collection");
      formatField = XMLHandler.getTagValue(entrynode, "formatField");
      mimeTypeField = XMLHandler.getTagValue(entrynode, "mimeTypeField");
      documentUriField = XMLHandler.getTagValue(entrynode, "documentUriField");
      documentContentField = XMLHandler.getTagValue(entrynode, "documentContentField");
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG, "MarkLogicInputMeta.Exception.UnableToLoadStepInfo"),
          e);
    }
  }

  @Override
  /**
   * Returns the XML configuration of this step for saving in a ktr file
   */
  public String getXML() {
    StringBuffer retval = new StringBuffer(300);
    retval.append("    " + XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName()));

    //retval.append("      ").append(XMLHandler.addTagValue("host", host));
    //retval.append("      ").append(XMLHandler.addTagValue("port", port));
    //retval.append("      ").append(XMLHandler.addTagValue("username", username));
    //retval.append("      ").append(XMLHandler.addTagValue("password", password));
    retval.append("      ").append(XMLHandler.addTagValue("collection", collection));
    retval.append("      ").append(XMLHandler.addTagValue("formatField", formatField));
    retval.append("      ").append(XMLHandler.addTagValue("mimeTypeField", mimeTypeField));
    retval.append("      ").append(XMLHandler.addTagValue("documentUriField", documentUriField));
    retval.append("      ").append(XMLHandler.addTagValue("documentContentField", documentContentField));

    return retval.toString();
  }

  @Override
  /**
   * Reads the configuration of this step from a repository
   */
  public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
      throws KettleException {

    try {
      //host = rep.getJobEntryAttributeString(id_step, "host");
      //port = (int)rep.getJobEntryAttributeInteger(id_step, "port");
      //username = rep.getJobEntryAttributeString(id_step, "username");
      //password = rep.getJobEntryAttributeString(id_step, "password");
      databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
      collection = rep.getJobEntryAttributeString(id_step, "collection");
      formatField = rep.getJobEntryAttributeString(id_step, "formatField");
      mimeTypeField = rep.getJobEntryAttributeString(id_step, "mimeTypeField");
      documentUriField = rep.getJobEntryAttributeString(id_step, "documentUriField");
      documentContentField = rep.getJobEntryAttributeString(id_step, "documentContentField");
    } catch (Exception e) {
      throw new KettleException(
          BaseMessages.getString(PKG, "MarkLogicInputMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e);
    }

  }

  @Override
  /**
   * Saves the configuration of this step to a repository
   */
  public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
      throws KettleException {
    try {
      rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
      //rep.saveJobEntryAttribute(id_transformation, getObjectId(), "host", host);
      //rep.saveJobEntryAttribute(id_transformation, getObjectId(), "port", port);
      //rep.saveJobEntryAttribute(id_transformation, getObjectId(), "username", username);
      //rep.saveJobEntryAttribute(id_transformation, getObjectId(), "password", password);
      rep.saveJobEntryAttribute(id_transformation, getObjectId(), "collection", collection);
      rep.saveJobEntryAttribute(id_transformation, getObjectId(), "formatField", formatField);
      rep.saveJobEntryAttribute(id_transformation, getObjectId(), "mimeTypeField", mimeTypeField);
      rep.saveJobEntryAttribute(id_transformation, getObjectId(), "documentUriField", documentUriField);
      rep.saveJobEntryAttribute(id_transformation, getObjectId(), "documentContentField", documentContentField);
    } catch (KettleException e) {
      throw new KettleException(
          BaseMessages.getString(PKG, "MarkLogicInputMeta.Exception.UnableToSaveStepInfo") + id_step, e);
    }
  }

  @Override
  /**
   * Validates this step's configuration
   */
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore) {

    CheckResult cr;
    if (databaseMeta != null) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Connection exists", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use",
          stepMeta);
      remarks.add(cr);
    }

    if (input.length > 0) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          BaseMessages.getString(PKG, "MarkLogicInputMeta.CheckResult.StepReceiveInfo.DialogMessage"), stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
          BaseMessages.getString(PKG, "MarkLogicInputMeta.CheckResult.NoInputReceived.DialogMessage"), stepMeta);
      remarks.add(cr);
    }

  }

  /**
   * Returns a new instance of this step
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
      Trans trans) {
    return new MarkLogicInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
  }

  /**
   * Returns a new instance of step data
   */
  public StepDataInterface getStepData() {
    return new MarkLogicInputData();
  }

  public DatabaseMeta[] getUsedDatabaseConnections() {
    if (databaseMeta != null) {
      return new DatabaseMeta[] { databaseMeta };
    } else {
      return super.getUsedDatabaseConnections();
    }
  }

  /*
  public void setHost(String h) {
    host = h;
  }
  
  public String getHost() {
    return host;
  }
  
  public void setPort(int p) {
    port = p;
  }
  
  public int getPort() {
    return port;
  }
  
  public void setUsername(String user) {
    username = user;
  }
  
  public String getUsername() {
    return username;
  }
  
  public void setPassword(String pass) {
    password = pass;
  }
  
  public String getPassword() {
    return password;
  }
  */
  public void setCollection(String coll) {
    collection = coll;
  }

  public String getCollection() {
    return collection;
  }

  public void setFormatField(String ff) {
    formatField = ff;
  }

  public String getFormatField() {
    return formatField;
  }

  public void setMimeTypeField(String mime) {
    mimeTypeField = mime;
  }

  public String getMimeTypeField() {
    return mimeTypeField;
  }

  public void setDocumentUriField(String duri) {
    documentUriField = duri;
  }

  public String getDocumentUriField() {
    return documentUriField;
  }

  public void setDocumentContentField(String dcfield) {
    documentContentField = dcfield;
  }

  public String getDocumentContentField() {
    return documentContentField;
  }

}