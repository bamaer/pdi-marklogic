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

import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.DatabaseClient;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseFactoryInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.i18n.BaseMessages;

/**
 * A ConnectionFactory for MarkLogic Server
 * 
 * @author Adam Fowler <adam.fowler@hitachivantara.com>
 * @since 22-12-2017
 */
public class MarkLogicConnectionFactory implements DatabaseFactoryInterface {
  // TODO add Kerberos auth context support
  public enum AuthScheme { BASIC, DIGEST };

  public static DatabaseClient create( String host, int port, AuthScheme authScheme, String username, String password, String databaseName) throws Exception {
    if ( authScheme == AuthScheme.BASIC ) {
      DatabaseClient client = DatabaseClientFactory.newClient( host, port, databaseName,
          new DatabaseClientFactory.BasicAuthContext( username, password ) );
      return client;
    } else if ( authScheme == AuthScheme.DIGEST ) {
      DatabaseClient client = DatabaseClientFactory.newClient( host, port, databaseName,
          new DatabaseClientFactory.DigestAuthContext( username, password ) );
      return client;
    } else {
      throw new Exception( "Auth scheme unsupported" );
    }
  }

  /**
   * The MarkLogic connection to test
   */
  public String getConnectionTestReport(DatabaseMeta databaseMeta) throws KettleDatabaseException {

    StringBuilder report = new StringBuilder();

    DatabaseClient client = null;

    try {
      //MarkLogicDatabaseMeta meta = (MarkLogicDatabaseMeta)databaseMeta;
      
      String asString = (String)databaseMeta.getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_AUTHSCHEME);
      AuthScheme as = AuthScheme.DIGEST;
      if ("basic".equals(asString)) {
        as = AuthScheme.BASIC;
      }

      client = create(
          //(String)databaseMeta.getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_HOST),
          databaseMeta.getHostname(),
          Integer.parseInt(databaseMeta.getDatabasePortNumberString()),
          //Integer.parseInt((String)databaseMeta.getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PORT)),
          as,
          databaseMeta.getUsername(),
          databaseMeta.getPassword(),
          databaseMeta.getDatabaseName()
      );
          //(String)databaseMeta.getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_USERNAME),
          //(String)databaseMeta.getAttributes().get(MarkLogicDatabaseMeta.ATTRIBUTE_PASSWORD));

      client.getSecurityContext(); // should be valid only if connected???

      // If the connection was successful
      //
      report.append("Connection to MarkLogic Server [").append(databaseMeta.getName())
          .append("] succeeded without a problem.").append(Const.CR);

    } catch (Throwable e) {
      report.append("Unable to connect to MarkLogic Server: ").append(e.getMessage()).append(Const.CR);
      report.append(Const.getStackTracker(e));
    } finally {
      if (client != null) {
        client.release();
      }
    }

    return report.toString();
  }
}
