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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;

/**
 * Runtime transient data container for the PDI BigQuery stream step
 * 
 * @author Adam Fowler {@literal <adam.fowler@marklogic.com>}
 * @since 1.0 11-01-2018
 */
public class MarkLogicInputData extends BaseStepData implements StepDataInterface {
  public RowMetaInterface outputRowMeta;
  public RowMetaInterface inputRowMeta;

  // in flight configuration objects here (E.g. batch handler
  public DatabaseClient client = null;
  public DataMovementManager dmm = null;
  public QueryBatcher batcher = null;

  public int docUriFieldId = -1;
  public int docContentFieldId = -1;
  public int mimeTypeFieldId = -1;
  public int formatFieldId = -1;

  /**
   * Default constructor
   */
  public MarkLogicInputData() {
    super();
  }

}