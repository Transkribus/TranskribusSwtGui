package eu.transkribus.swt_gui.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.TrpCrowdProjectMilestone;
import eu.transkribus.swt.util.SWTUtil;

/**
 * @author schorsch
 *
 */
public class CrowdSourcingMessageDialog extends Dialog {
	
	public static final String NO_MILESTONE = "No milestone assigned";

	protected Object result;
	protected Shell shell;
		
	private Text subjectTxt;
	private Text messageTxt;
	private Text dateTxt;
	private Combo mstCombo;
	
	private String subject = "";
	private String message = "";
	private String date = "";
	private ArrayList<TrpCrowdProjectMilestone> mstList = new ArrayList<TrpCrowdProjectMilestone>();
	private ArrayList<String> milestonesAsString = new ArrayList<String>();
	private TrpCrowdProjectMilestone selectedMst = null;
	private int selectedMstId;
	private String selectedtMstAsString;

	private Button saveButton;
	
	DateFormat timeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CrowdSourcingMessageDialog(Shell parent, int style) {
		super(parent, style);
		setText("New crowdsourcing message");
	}
	
	public CrowdSourcingMessageDialog(Shell parent, int style, ArrayList<TrpCrowdProjectMilestone> milestones) {
		super(parent, style);
		setText("New crowdsourcing message");
		this.mstList = milestones;
		milestonesAsString.add("No milestone assigned");
		for (TrpCrowdProjectMilestone mst : milestones){
			milestonesAsString.add(mst.toShortString());
		}		
	}
	
	public CrowdSourcingMessageDialog(Shell parent, int style, String subject, String message, TrpCrowdProjectMilestone milestone, ArrayList<TrpCrowdProjectMilestone> milestones) {
		super(parent, style);
		setText("New crowdsourcing message");
		this.subject = subject;
		this.message = message;
		this.selectedMst = milestone;
		this.mstList = milestones;
		milestonesAsString.add(NO_MILESTONE);
		for (TrpCrowdProjectMilestone mst : milestones){
			milestonesAsString.add(mst.toShortString());
		}		
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
		lblSubjectLabel.setText("Subject:");
				
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
		lblMessageLabel.setText("Message:");
				
		messageTxt = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_messageText = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_messageText.widthHint = 472;
		gd_messageText.heightHint = 134;
		messageTxt.setLayoutData(gd_messageText);
		messageTxt.setText(message);
		
		Label mstLbl = new Label(shell, SWT.NONE);
		mstLbl.setText("Belongs to Milestone:");
		
		mstCombo = new Combo(shell, SWT.DROP_DOWN);
		mstCombo.setLayoutData(gd);
		String[] tmpArray = new String[milestonesAsString.size()];
		//mstCombo.setItem(0, "No milestone");
		mstCombo.setItems(milestonesAsString.toArray(tmpArray));
		if (selectedMst != null){
			mstCombo.select(milestonesAsString.indexOf(selectedMst.toShortString()));
		}
		else{
			mstCombo.select(0);
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
				selectedtMstAsString = milestonesAsString.get(mstCombo.getSelectionIndex());
				if (!selectedtMstAsString.equals(NO_MILESTONE)){
					selectedMstId = getMstId(selectedtMstAsString);
				}
				System.out.println("subject " + subject);
				System.out.println("message " + message);
				System.out.println("selectedtMstAsString " + selectedtMstAsString);
				if (subject != "" && message != ""){
					result = subject;
				}
				shell.dispose();
				
			}

			private int getMstId(String selectedtMstAsString) {
				String tmp = selectedtMstAsString.substring(selectedtMstAsString.indexOf("MilestoneId=")+"MilestoneId=".length(), selectedtMstAsString.indexOf(","));	
				System.out.println("id of milestone: " + tmp);
				return Integer.parseInt(tmp);
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

	public int getSelectedMstId() {
		return selectedMstId;
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

