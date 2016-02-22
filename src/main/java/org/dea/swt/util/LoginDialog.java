package org.dea.swt.util;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.transcription.autocomplete.TrpAutoCompleteField;

public class LoginDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(LoginDialog.class);
	
  protected Text txtUser;
  protected Text txtPassword;
  protected CCombo serverCombo;
  protected Button rememberCredentials;
  protected Button clearStoredCredentials;
  
  protected String message;
  protected String[] userProposals;
  
  protected Label infoLabel;
  

  protected String[] serverProposals;
  protected  int defaultUriIndex;
  
  
  
//  private String user = "";
//  private char[] password = new char[]{};
  
  TrpAutoCompleteField autocomplete;
  
//  public LoginDialog(Shell parentShell, String message) {
//	  this(parentShell, message, new String[0], withServerCombo);
//  }
  
  public LoginDialog(Shell parentShell, String message, String[] userProposals, String[] serverProposals, int defaultUriIndex) {
	   super(parentShell);
	   this.message = message;
	   this.userProposals = userProposals;
	   this.serverProposals = serverProposals;
	   this.defaultUriIndex = defaultUriIndex;
  }
  
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout layout = new GridLayout(2, false);
    layout.marginRight = 5;
    layout.marginLeft = 10;
    container.setLayout(layout);

    Label lblUser = new Label(container, SWT.NONE);
    lblUser.setText("User:");

    txtUser = new Text(container, SWT.BORDER);
    txtUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
        1, 1));
    txtUser.setText("");

    Label lblPassword = new Label(container, SWT.NONE);
    GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false,
        false, 1, 1);
    gd_lblNewLabel.horizontalIndent = 1;
    lblPassword.setLayoutData(gd_lblNewLabel);
    lblPassword.setText("Password:");

    txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
    txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
        false, 1, 1));
    txtPassword.setText(String.valueOf(""));
    
    if (serverProposals!=null)
    	initServerCombo(container);
    
    rememberCredentials = new Button(container, SWT.CHECK);
    rememberCredentials.setText("Remember credentials");
    rememberCredentials.setToolTipText("If enabled and login is successful credentials will be stored and remembered at the next login");
//    rememberCredentials.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
//            true, 2, 2));
    
    clearStoredCredentials = new Button(container, SWT.PUSH);
    clearStoredCredentials.setText("Clear stored credentials");
    clearStoredCredentials.setToolTipText("Clears previously stored credentials from harddisk");
    
    infoLabel = new Label(container, SWT.FLAT);
    infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
            true, 2, 2));
    infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_RED));
    infoLabel.setText(message);
    
    //FIXME autocomplete disabled on Günters request
    if (true) {
		autocomplete = new TrpAutoCompleteField(txtUser, 
				new TextContentAdapter(), new String[]{}, 
				KeyStroke.getInstance(SWT.CTRL, SWT.SPACE), null
				);
		autocomplete.getAdapter().setEnabled(true);
		autocomplete.getAdapter().setPropagateKeys(true);
		autocomplete.setProposals(userProposals);
    }
    	
	postInit();
	
    return container;
  }
  
  protected void postInit() {
	  // Override this method to perform post init stuff outside the class definition!
  }
  
  private void initServerCombo(Composite container) {
	  Label serverLabel = new Label(container, SWT.FLAT);
	  serverLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
	  serverLabel.setText("Server: ");
	  
	  serverCombo = new CCombo(container, SWT.DROP_DOWN);
	  serverCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
	  for (String s : serverProposals)
		  serverCombo.add(s);
	  if (serverProposals.length>0)
		  serverCombo.select(0);
	  if (defaultUriIndex>=0 && defaultUriIndex<serverProposals.length)
		  serverCombo.select(defaultUriIndex);
	  
//	  serverCombo.pack();
  }
  
  public void setInfo(String info) {
	  infoLabel.setText(info);
  }
  
  public CCombo getServerCombo() { return serverCombo; }
  
  @Override protected boolean isResizable() {
	    return true;
	}
  
  @Override
protected void configureShell(Shell shell) {
      super.configureShell(shell);
      shell.setText("Login");
   }

  // override method to use "Login" as label for the OK button
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, "Login", true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  protected Point getInitialSize() {
    return new Point(450, 250);
  }

//  @Override
//  protected void okPressed() {
//    user = txtUser.getText();
//    password = txtPassword.getText().toCharArray();
//    setReturnCode(OK);
//    
////    super.okPressed();
//  }
  
  public void setMessage(String message) {
	  this.message = message;
	  infoLabel.setText(message);
  }

  public String getUser() {
    return txtUser.getText();
  }

//  public void setUser(String user) {
//    this.user = user;
//  }
  
  public void setPassword(String pw) {
	  txtPassword.setText(pw);
  }
  
  public void setUsername(String username) {
	  txtUser.setText(username);
  }

  public char[] getPassword() {
    return txtPassword.getText().toCharArray();
  }
  
  public boolean isRememberCredentials() {
	  return rememberCredentials.getSelection();
  }
  
  public boolean isDisposed() {
	  return getShell()==null || getShell().isDisposed();
  }

//  public void setPassword(char[] password) {
//    this.password = password;
//  }

} 