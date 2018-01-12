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

import org.pentaho.di.core.database.DatabaseMeta;

import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.List;
import java.util.ArrayList;

/**
 * Dialog box for the MarkLogic input step
 * 
 * @author Adam Fowler {@literal <adam.fowler@marklogic.com>}
 * @since 1.0 11-01-2018
 */
public class MarkLogicInputDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = MarkLogicInputMeta.class; // for i18n purposes, needed by Translator2!!

  private MarkLogicInputMeta input;

  private Label wlName;
  private Text wName;

  private FormData fdlName, fdName;

  private CCombo wConnection;
  /*
  private Label cplName;
  private Text cpName;
  private FormData fcplName, fcpName;
  
  private Label plName;
  private Text pName;
  private FormData fplName, fpName;
  
  private Label dslName;
  private Text dsName;
  private FormData fdslName, fdsName;
  
  private Label tlName;
  private Text tName;
  private FormData ftlName, ftName;
  */

  private Label lblCollection;
  private Text collection;
  private FormData flblCollection, fcollection;

  private Label lblFormat;
  private Text format;
  private FormData flblFormat, fformat;

  private Label lblMime;
  private Text mime;
  private FormData flblMime, fmime;

  private Label lblDocUri;
  private Text docUri;
  private FormData flblDocUri, fdocUri;

  private Label lblDocContent;
  private Text docContent;
  private FormData flblDocContent, fdocContent;

  /*
  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields,fdFields;
  */

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private SelectionAdapter lsDef;

  private boolean changed = false;

  /**
   * Standard PDI dialog constructor
   */
  public MarkLogicInputDialog(Shell parent, Object in, TransMeta tr, String sname) {
    super(parent, (BaseStepMeta) in, tr, sname);
    input = (MarkLogicInputMeta) in;
  }

  /**
   * Initialises and displays the dialog box
   */
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    setShellImage(shell, input);

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    ModifyListener lsConnectionMod = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        input.setChanged();
      }
    };

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "MarkLogicInput.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Step Name
    wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "MarkLogicInput.Name.Label"));
    props.setLook(wlName);
    fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, -margin);
    fdlName.top = new FormAttachment(0, margin);
    wlName.setLayoutData(fdlName);
    wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wName.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Name.Tooltip"));
    props.setLook(wName);
    wName.addModifyListener(lsMod);
    fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    // Database Connection

    wConnection = addConnectionLine(shell, wName, middle, margin);
    List<String> items = new ArrayList<String>();
    for (DatabaseMeta dbMeta : transMeta.getDatabases()) {
      if (dbMeta.getDatabaseInterface() instanceof MarkLogicDatabaseMeta) {
        items.add(dbMeta.getName());
      }
    }
    wConnection.setItems(items.toArray(new String[items.size()]));
    if (input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1) {
      wConnection.select(0);
    }
    wConnection.addModifyListener(lsConnectionMod);

    /*
    // Hostname
    cplName = new Label(shell, SWT.RIGHT);
    cplName.setText(BaseMessages.getString(PKG, "MarkLogicInput.Hostname.Label"));
    props.setLook(cplName);
    fcplName = new FormData();
    fcplName.left = new FormAttachment(0, 0);
    fcplName.right = new FormAttachment(middle, -margin);
    fcplName.top = new FormAttachment(wName, margin);
    cplName.setLayoutData(fcplName);
    cpName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    cpName.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Hostname.Tooltip"));
    props.setLook(cpName);
    cpName.addModifyListener(lsMod);
    fcpName = new FormData();
    fcpName.left = new FormAttachment(middle, 0);
    fcpName.top = new FormAttachment(wName, margin);
    fcpName.right = new FormAttachment(100, 0);
    cpName.setLayoutData(fcpName);
    
    // port
    plName = new Label(shell, SWT.RIGHT);
    plName.setText(BaseMessages.getString(PKG, "MarkLogicInput.Port.Label"));
    props.setLook(plName);
    fplName = new FormData();
    fplName.left = new FormAttachment(0, 0);
    fplName.right = new FormAttachment(middle, -margin);
    fplName.top = new FormAttachment(cpName, margin);
    plName.setLayoutData(fplName);
    pName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    pName.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Port.Tooltip"));
    props.setLook(pName);
    pName.addModifyListener(lsMod);
    fpName = new FormData();
    fpName.left = new FormAttachment(middle, 0);
    fpName.top = new FormAttachment(cpName, margin);
    fpName.right = new FormAttachment(100, 0);
    pName.setLayoutData(fpName);
    
    // Username
    dslName = new Label(shell, SWT.RIGHT);
    dslName.setText(BaseMessages.getString(PKG, "MarkLogicInput.Username.Label"));
    props.setLook(dslName);
    fdslName = new FormData();
    fdslName.left = new FormAttachment(0, 0);
    fdslName.right = new FormAttachment(middle, -margin);
    fdslName.top = new FormAttachment(pName, margin);
    dslName.setLayoutData(fdslName);
    dsName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    dsName.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Username.Tooltip"));
    props.setLook(dsName);
    dsName.addModifyListener(lsMod);
    fdsName = new FormData();
    fdsName.left = new FormAttachment(middle, 0);
    fdsName.top = new FormAttachment(pName, margin);
    fdsName.right = new FormAttachment(100, 0);
    dsName.setLayoutData(fdsName);
    
    // Password
    tlName = new Label(shell, SWT.RIGHT);
    tlName.setText(BaseMessages.getString(PKG, "MarkLogicInput.Password.Label"));
    props.setLook(tlName);
    ftlName = new FormData();
    ftlName.left = new FormAttachment(0, 0);
    ftlName.right = new FormAttachment(middle, -margin);
    ftlName.top = new FormAttachment(dsName, margin);
    tlName.setLayoutData(ftlName);
    tName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    tName.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Password.Tooltip"));
    props.setLook(tName);
    tName.addModifyListener(lsMod);
    ftName = new FormData();
    ftName.left = new FormAttachment(middle, 0);
    ftName.top = new FormAttachment(dsName, margin);
    ftName.right = new FormAttachment(100, 0);
    tName.setLayoutData(ftName);
    */
    // collection field
    lblCollection = new Label(shell, SWT.RIGHT);
    lblCollection.setText(BaseMessages.getString(PKG, "MarkLogicInput.Collection.Label"));
    props.setLook(lblCollection);
    flblCollection = new FormData();
    flblCollection.left = new FormAttachment(0, 0);
    flblCollection.right = new FormAttachment(middle, -margin);
    flblCollection.top = new FormAttachment(wConnection, 2 * margin);
    lblCollection.setLayoutData(flblCollection);
    collection = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    collection.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.Collection.Tooltip"));
    props.setLook(collection);
    collection.addModifyListener(lsMod);
    fcollection = new FormData();
    fcollection.left = new FormAttachment(middle, 0);
    fcollection.top = new FormAttachment(wConnection, 2 * margin);
    fcollection.right = new FormAttachment(100, 0);
    collection.setLayoutData(fcollection);

    // formatField
    lblFormat = new Label(shell, SWT.RIGHT);
    lblFormat.setText(BaseMessages.getString(PKG, "MarkLogicInput.FormatField.Label"));
    props.setLook(lblFormat);
    flblFormat = new FormData();
    flblFormat.left = new FormAttachment(0, 0);
    flblFormat.right = new FormAttachment(middle, -margin);
    flblFormat.top = new FormAttachment(collection, margin);
    lblFormat.setLayoutData(flblFormat);
    format = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    format.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.FormatField.Tooltip"));
    props.setLook(format);
    format.addModifyListener(lsMod);
    fformat = new FormData();
    fformat.left = new FormAttachment(middle, 0);
    fformat.top = new FormAttachment(collection, margin);
    fformat.right = new FormAttachment(100, 0);
    format.setLayoutData(fformat);

    // mimeType field
    lblMime = new Label(shell, SWT.RIGHT);
    lblMime.setText(BaseMessages.getString(PKG, "MarkLogicInput.MimeField.Label"));
    props.setLook(lblMime);
    flblMime = new FormData();
    flblMime.left = new FormAttachment(0, 0);
    flblMime.right = new FormAttachment(middle, -margin);
    flblMime.top = new FormAttachment(format, margin);
    lblMime.setLayoutData(flblMime);
    mime = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    mime.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.MimeField.Tooltip"));
    props.setLook(mime);
    mime.addModifyListener(lsMod);
    fmime = new FormData();
    fmime.left = new FormAttachment(middle, 0);
    fmime.top = new FormAttachment(format, margin);
    fmime.right = new FormAttachment(100, 0);
    mime.setLayoutData(fmime);

    // documentUri Field
    lblDocUri = new Label(shell, SWT.RIGHT);
    lblDocUri.setText(BaseMessages.getString(PKG, "MarkLogicInput.DocUriField.Label"));
    props.setLook(lblDocUri);
    flblDocUri = new FormData();
    flblDocUri.left = new FormAttachment(0, 0);
    flblDocUri.right = new FormAttachment(middle, -margin);
    flblDocUri.top = new FormAttachment(mime, margin);
    lblDocUri.setLayoutData(flblDocUri);
    docUri = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    docUri.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.DocUriField.Tooltip"));
    props.setLook(docUri);
    docUri.addModifyListener(lsMod);
    fdocUri = new FormData();
    fdocUri.left = new FormAttachment(middle, 0);
    fdocUri.top = new FormAttachment(mime, margin);
    fdocUri.right = new FormAttachment(100, 0);
    docUri.setLayoutData(fdocUri);

    // document Content field
    lblDocContent = new Label(shell, SWT.RIGHT);
    lblDocContent.setText(BaseMessages.getString(PKG, "MarkLogicInput.DocContentField.Label"));
    props.setLook(lblDocContent);
    flblDocContent = new FormData();
    flblDocContent.left = new FormAttachment(0, 0);
    flblDocContent.right = new FormAttachment(middle, -margin);
    flblDocContent.top = new FormAttachment(docUri, margin);
    lblDocContent.setLayoutData(flblDocContent);
    docContent = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    docContent.setToolTipText(BaseMessages.getString(PKG, "MarkLogicInput.DocContentField.Tooltip"));
    props.setLook(docContent);
    docContent.addModifyListener(lsMod);
    fdocContent = new FormData();
    fdocContent.left = new FormAttachment(middle, 0);
    fdocContent.top = new FormAttachment(docUri, margin);
    fdocContent.right = new FormAttachment(100, 0);
    docContent.setLayoutData(fdocContent);

    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, margin, docContent);

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    getData();
    //activeCopyFromPrevious();
    //activeUseKey();

    BaseStepDialog.setSize(shell);

    shell.open();
    props.setDialogSize(shell, "MarkLogicInputDialogSize");
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return stepname;

  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wName.setText(Const.nullToEmpty(stepname));
    if (input.getDatabaseMeta() != null) {
      wConnection.setText(input.getDatabaseMeta().getName());
    } else if (transMeta.nrDatabases() == 1) {
      wConnection.setText(transMeta.getDatabase(0).getName());
    }
    collection.setText(Const.nullToEmpty(input.getCollection()));
    format.setText(Const.nullToEmpty(input.getFormatField()));
    mime.setText(Const.nullToEmpty(input.getMimeTypeField()));
    docUri.setText(Const.nullToEmpty(input.getDocumentUriField()));
    docContent.setText(Const.nullToEmpty(input.getDocumentContentField()));

    wName.selectAll();
    wName.setFocus();
  }

  /**
   * Handles clicking cancel
   */
  private void cancel() {
    stepname = null;
    input.setChanged(changed);
    dispose();
  }

  private int showDatabaseWarning(boolean includeCancel) {
    MessageBox mb = new MessageBox(shell, SWT.OK | (includeCancel ? SWT.CANCEL : SWT.NONE) | SWT.ICON_ERROR);
    mb.setMessage(BaseMessages.getString(PKG, "MarkLogicInputDialog.InvalidConnection.DialogMessage"));
    mb.setText(BaseMessages.getString(PKG, "MarkLogicInputDialog.InvalidConnection.DialogTitle"));
    return mb.open();
  }

  /**
   * Saves data to the meta class instance
   */
  private void ok() {
    if (null == wName.getText() || "".equals(wName.getText().trim())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title"));
      mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg"));
      mb.open();
      return;
    }
    stepname = wName.getText();
    //input.setName( wName.getText() );

    if (transMeta.findDatabase(wConnection.getText()) == null) {
      int answer = showDatabaseWarning(true);
      if (answer == SWT.CANCEL) {
        return;
      }
    } else {
      input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()));
    }
    //input.setHost(cpName.getText());
    //input.setPort(Integer.parseInt(pName.getText()));
    //input.setUsername(dsName.getText());
    //input.setPassword(tName.getText());
    String collectionField = collection.getText();
    if (null == collectionField || "".equals(collectionField.trim())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setText(BaseMessages.getString(PKG, "MarkLogicInputDialog.CollectionMissing.Title"));
      mb.setMessage(BaseMessages.getString(PKG, "MarkLogicInputDialog.CollectionMissing.Msg"));
      mb.open();
      return;
    }
    input.setCollection(collectionField);
    input.setFormatField(format.getText());
    input.setMimeTypeField(mime.getText());
    input.setDocumentUriField(docUri.getText());
    input.setDocumentContentField(docContent.getText());

    dispose();
  }

}