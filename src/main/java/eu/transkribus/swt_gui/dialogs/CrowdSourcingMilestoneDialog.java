package eu.transkribus.swt_gui.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import eu.transkribus.swt.util.SWTUtil;


/**
 * @author schorsch
 *
 */
public class CrowdSourcingMilestoneDialog extends Dialog {

	protected Object result;
	protected Shell shell;
		
	private Text subjectTxt;
	private Text messageTxt;
	private Text dateTxt;
	private Text dueDateTxt;
	
	private String subject = "";
	private String message = "";
	private String date = "";
	private String dueDate = "";

	private Button saveButton;
	
	DateFormat timeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CrowdSourcingMilestoneDialog(Shell parent, int style) {
		super(parent, style);
		setText("New crowdsourcing milestone");
	}
	
	public CrowdSourcingMilestoneDialog(Shell parent, int style, String subject, String message, String dueDate) {
		super(parent, style);
		setText("New crowdsourcing milestone");
		this.subject = subject;
		this.message = message;
		this.dueDate = dueDate;
		
//		System.out.println("init subject " + subject);
//		System.out.println("init message " + message);
		
	}
	
	public Shell getShell() {
		return shell;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		SWTUtil.centerShell(shell);
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
	
	public void setActive() {

		if(shell.isDisposed()){
			open();
		}else{
			shell.setActive();	
			shell.setFocus();
		}
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle()| SWT.RESIZE);
		shell.setSize(547, 333);
		//shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
		Label lblSubjectLabel = new Label(shell, SWT.NONE);
		lblSubjectLabel.setText("Title:");
				
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 413;
		subjectTxt = new Text(shell, SWT.BORDER);
		subjectTxt.setLayoutData(gd);
		subjectTxt.setText(subject);
		
		Label lblDateLabel = new Label(shell, SWT.NONE);
		lblDateLabel.setText("Date Created:");
		
		LocalDateTime now = LocalDateTime.now();
		dateTxt = new Text(shell, SWT.BORDER);
		dateTxt.setLayoutData(gd);
		dateTxt.setText(now.toString());
		dateTxt.setEditable(false);
		
		Label lblMessageLabel = new Label(shell, SWT.NONE);
		lblMessageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblMessageLabel.setText("Description:");
				
		messageTxt = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_messageText = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_messageText.widthHint = 472;
		gd_messageText.heightHint = 134;
		messageTxt.setLayoutData(gd_messageText);
		messageTxt.setText(message);
		
		//Textfeld (zeigt aktuelle due time)
		//button mit dem sich Kalender öffnet
		Button openDatePicker = new Button(shell, SWT.NONE);
		openDatePicker.setText("Choose Due Date");
		openDatePicker.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				DatePickerDialog datepicker = new DatePickerDialog(shell);
				datepicker.open();
				dueDateTxt.setText(datepicker.getDate().toString());
				shell.redraw();
			}
		});
		
		dueDateTxt = new Text(shell, SWT.BORDER);
		dueDateTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (dueDate != ""){
			dueDateTxt.setText(dueDate);
		}
		else{
			dueDateTxt.setText("No due date");
		}

		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());

		saveButton = new Button(buttonComposite, SWT.NONE);
		saveButton.setText("OK");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				
				subject = subjectTxt.getText();
				message = messageTxt.getText();
				date = dateTxt.getText();
				dueDate = dueDateTxt.getText();
//				System.out.println("subject " + subject);
//				System.out.println("message " + message);
				if (subject != "" && message != ""){
					result = subject;
				}
				shell.dispose();
				
			}
		});
				
		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.dispose();
			}	
		});
		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
	}
	

	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDate() {
		return date;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public void dispose() {

		if(!shell.isDisposed()){
			shell.dispose();
		}
	}
	
}

