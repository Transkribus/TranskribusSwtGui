package eu.transkribus.swt_gui.metadata;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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

import eu.transkribus.core.util.RegexPattern;
import eu.transkribus.swt.util.Colors;

public class CreateTagNameDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(CreateTagNameDialog.class);

	String title;
	boolean addIsEmptyTagCheckBox;
	
	Text nameTxt;
	Button isEmptyTagBtn;

	String name = null;
	boolean isEmptyTag=false;
	
	Text status;
	
	public CreateTagNameDialog(Shell parentShell, String title, boolean addIsEmptyTagCheckBox) {
		super(parentShell);
		this.title = title;
		this.addIsEmptyTagCheckBox = addIsEmptyTagCheckBox;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(title);
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label l0 = new Label(container, 0);
		l0.setText("Name: ");
		nameTxt = new Text(container, SWT.BORDER);
//		nameTxt.setToolTipText("The short name of the tag / attribute as it will be encoded in the export format - must be unique!");
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameTxt.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		if (addIsEmptyTagCheckBox) {
			isEmptyTagBtn = new Button(container, SWT.CHECK);
			isEmptyTagBtn.setText("Empty tag");
			isEmptyTagBtn.setToolTipText("Creates a tag that contains no corresponding text, only a position inside the text (as e.g. the <gap/> tag)");
			isEmptyTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		}
		
		status = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		status.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
		status.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		return container;
	}
	
	@Override protected Control createContents(Composite parent) {
		Control ctrl = super.createContents(parent);
		postInit();
		
		return ctrl;
	}
	
	private void postInit() {
		validateInput();
		
	}
	
	private void validateInput() {		
		boolean matches = RegexPattern.TAG_NAME_PATTERN.matches(nameTxt.getText());
		getButton(IDialogConstants.OK_ID).setEnabled(matches);
		
		status.setText(!matches ? RegexPattern.TAG_NAME_PATTERN.getDescription() : "");
	}
	
	public void applyData() {
		name = nameTxt.getText();
		isEmptyTag = isEmptyTagBtn!=null ? isEmptyTagBtn.getSelection() : false;
	}
	
	@Override protected void okPressed() {
		applyData();
		super.okPressed();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isEmptyTag() {
		return isEmptyTag;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(400, 200);
	}
}
