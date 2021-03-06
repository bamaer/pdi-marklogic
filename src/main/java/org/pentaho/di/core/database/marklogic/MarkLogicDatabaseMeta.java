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

package org.pentaho.di.core.database.marklogic;

import com.marklogic.client.DatabaseClient;

import org.eclipse.jface.wizard.WizardPage;
import org.pentaho.di.core.database.BaseDatabaseMeta;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.marklogic.MarkLogicConnectionFactory.AuthScheme;
import org.pentaho.di.core.plugins.DatabaseMetaPlugin;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.wizard.WizardPageFactory;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizardPageMarkLogic;

/**
 * Contains MarkLogic Server specific connection configuration.
 * 
 * Note: SQL syntax and drivers not supported by this class.
 *
 * @author Adam Fowler <adam.fowler@hitachivantara.com>
 * @since 22-12-2017
 */

@DatabaseMetaPlugin(type = "MarkLogicServer", typeDescription = "MarkLogic Server")
public class MarkLogicDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface, WizardPageFactory {
  //public static final String ATTRIBUTE_HOST = "MarkLogicHost";
  //public static final String ATTRIBUTE_PORT = "MarkLogicPort";
  public static final String ATTRIBUTE_AUTHSCHEME = "MarkLogicAuthScheme";
  //public static final String ATTRIBUTE_USERNAME = "MarkLogicUsername";
  //public static final String ATTRIBUTE_PASSWORD = "MarkLogicPassword";

  public static final String MARKLOGIC = "MarkLogicServer";

  @Override
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN };
  }

  @Override
  public int getDefaultDatabasePort() {
    return 8000;
  }
/*
// TODO figure out how to do this for the default UI...
  @Override
  public String getDefaultDatabaseName() {
    return "Documents";
  }
*/

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  public String getDriverClass() {
    return null;
  }

  public String getURL(String hostname, String port, String databaseName) {
    return null;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return false;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk,
      boolean semicolon) {
    return null;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon) {
    return null;
  }

  public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
      boolean add_fieldname, boolean add_cr) {
    return null;
  }

  @Override
  public String[] getReservedWords() {
    return null;
  }

  public String[] getUsedLibraries() {
    return new String[] {};
  }

  @Override
  public String getDatabaseFactoryName() {
    return org.pentaho.di.core.database.marklogic.MarkLogicConnectionFactory.class.getName();
  }

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  @Override
  public boolean isExplorable() {
    return false;
  }

  @Override
  public boolean canTest() {
    return false;
  }

  @Override
  public boolean requiresName() {
    return false;
  }

  public WizardPage createWizardPage(PropsUI props, DatabaseMeta info) {
    return new CreateDatabaseWizardPageMarkLogic(MARKLOGIC, props, info);
  }

  //public String getHost() {
  //  return (String)getAttributes().get(ATTRIBUTE_HOST);
  //}

  //public String getPort() {
  //  return (String) getAttributes().get(ATTRIBUTE_PORT);
  //}

  public AuthScheme getAuthScheme() {
    String scheme = (String) getAttributes().get(ATTRIBUTE_AUTHSCHEME);
    if ("basic".equals(scheme)) {
      return AuthScheme.BASIC;
    } else {
      // digest
      return AuthScheme.DIGEST;
    }
  }
/*
  public String getUsername() {
    return (String) getAttributes().get(ATTRIBUTE_USERNAME);
  }

  public String getPassword() {
    return (String) getAttributes().get(ATTRIBUTE_PASSWORD);
  }*/

  public DatabaseClient getConnection() throws Exception {
    return MarkLogicConnectionFactory.create( getHostname(),
      Integer.parseInt( getDatabasePortNumberString() ), getAuthScheme(), getUsername(), getPassword(), getDatabaseName() );
  }
}
