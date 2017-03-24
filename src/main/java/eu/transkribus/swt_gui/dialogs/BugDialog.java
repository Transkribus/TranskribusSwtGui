package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.ProgramInfo;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;

public class BugDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(BugDialog.class);

	protected Object result;
	protected Shell shell;
	private Text messageText;
	private Text subjectText;
	private Text emailText;
	private Button isBugRadio;
	private Button sendCopyBtn;
	
	private Button isFeatureRequestRadio;
	private Button sendButton;
	private Button cancelButton;
	
	static Storage store = Storage.getInstance();
	
	public final static int MAX_KB_OF_LOG_TAIL = 1024; // in KB

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public BugDialog(Shell parent, int style) {
		super(parent, style|= (SWT.DIALOG_TRIM | SWT.RESIZE) );
		setText("Send a bug report / feature request");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {		

		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {

		shell = new Shell(getParent(), getStyle());
		shell.setSize(547, 333);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setText("E-Mail");
		
		emailText = new Text(shell, SWT.BORDER);
		GridData gd_emailText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_emailText.widthHint = 413;
		emailText.setLayoutData(gd_emailText);
		
		if (store.isLoggedIn()) {
			emailText.setText(store.getUser().getEmail());
		}
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setText("Subject");
		
		subjectText = new Text(shell, SWT.BORDER);
		GridData gd_subjectText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_subjectText.widthHint = 408;
		subjectText.setLayoutData(gd_subjectText);
		
		Label lblNewLabel_2 = new Label(shell, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_2.setText("Message - please be as specific as possible");
		
		messageText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_messageText = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_messageText.widthHint = 472;
		gd_messageText.heightHint = 134;
		messageText.setLayoutData(gd_messageText);
		
		isBugRadio = new Button(shell, SWT.RADIO);
		isBugRadio.setToolTipText("This a bug report. In this case the latest local log file (stored in the \"logs\" subfolder) is attached to the report.");
		isBugRadio.setSelection(true);
		isBugRadio.setText("Bug");
		
		isFeatureRequestRadio = new Button(shell, SWT.RADIO);
		isFeatureRequestRadio.setToolTipText("This a feature request. No log files are attached in this case.");
		isFeatureRequestRadio.setText("Feature request");
		
		sendCopyBtn = new Button(shell, SWT.CHECK);
		sendCopyBtn.setToolTipText("Check to send a copy of the bug report / feature request to your email adress");
		sendCopyBtn.setText("Send me a copy");
		sendCopyBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		
		sendButton = new Button(shell, SWT.NONE);
		sendButton.setToolTipText("Send this bug report / feature request to the developer team");
		sendButton.setText("Send");
		sendButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				sendBugRequest();
			}
		});
		
		cancelButton = new Button(shell, SWT.NONE);
		cancelButton.setToolTipText("Cancel and close");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		cancelButton.setText("Cancel");
	}
	
	private void sendBugRequest() {
		try {
			final File[] atts;
			
			ProgramInfo info = new ProgramInfo();
			String subjectPrefix = info.getVersion()+" ("+info.getTimestampString()+") ";
			
			final boolean isBug = isBugRadio.getSelection();
			final boolean sendCopy = sendCopyBtn.getSelection(); 
			final File tailOfLogFile;
			if (isBug) {
				tailOfLogFile = GuiUtil.getTailOfLogFile(MAX_KB_OF_LOG_TAIL * CoreUtils.SIZE_OF_LOG_FILE_TAIL);
				atts = new File[] { tailOfLogFile };
//				subjectPrefix += "[BUG] ";
			} else {
				tailOfLogFile = null;
				atts = null;
//				subjectPrefix += "[FEATURE] ";	
			}
			
			if (subjectText.getText().isEmpty() /*|| messageText.getText().isEmpty()*/ || emailText.getText().isEmpty()) {
				throw new Exception("Subject or email cannot be empty");
			}
			
			try {
				InternetAddress.parse(emailText.getText());
			} catch (AddressException ae) {
				throw new AddressException("Invalid email address: "+emailText.getText()+", error message: "+ae.getMessage());
			}
			
			final String subject = subjectPrefix + subjectText.getText();
			final String message = messageText.getText();
			final String replyTo = emailText.getText();
			
			ProgressBarDialog.open(getParent(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Sending bug report / feature request", IProgressMonitor.UNKNOWN);
						logger.debug("sending report...");						
						
						// get existing server connection (if logged in) or create new anonymous one:
						TrpServerConn conn = Storage.getInstance().getConnection();
						if (conn == null) {
							logger.debug("creating connection to test server without credentials as no connection exists!");
							conn = new TrpServerConn(TrpServerConn.SERVER_URIS[0]);
						}

						if (isBug) {
							conn.sendBugReport(replyTo, subject, message, isBug, sendCopy, tailOfLogFile);
							// TEST:
//							CoreUtils.fileBugzillaBugReport(tailOfLogFile);
						}
						else {
							conn.sendBugReport(replyTo, subject, message, isBug, sendCopy, tailOfLogFile);
						}
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Sending report", false);			

			DialogUtil.showInfoMessageBox(shell, "Success", "Successfully sent report!");
			shell.close();
		} catch (Throwable e) {
			TrpMainWidget.getInstance().onError("Error", "Error during bug report / feature request", e);
		}
	}

	public void setActive() {
		shell.setActive();	
		shell.setFocus();
	}	
}
