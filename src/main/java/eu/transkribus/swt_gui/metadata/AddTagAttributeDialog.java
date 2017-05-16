package eu.transkribus.swt_gui.metadata;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.core.util.RegexPattern;
import eu.transkribus.swt.util.Colors;

@Deprecated
public class AddTagAttributeDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(AddTagAttributeDialog.class);

	Text nameTxt, displayNameTxt, descriptionTxt;
	Combo typeCombo;

	CustomTagAttribute result = null;
//	Label status;
	Text status;

	boolean simple = true;
	String tagName;
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 * @param tn 
	 */
	public AddTagAttributeDialog(Shell parentShell, String tn, boolean simple) {
		super(parentShell);
		this.tagName = tn;
		this.simple = simple;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Add attribute to '"+tagName+"' tag");
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		Label l0 = new Label(container, 0);
		l0.setText("Name: ");
		nameTxt = new Text(container, SWT.BORDER);
		nameTxt.setToolTipText("The short name of the tag as it is encoded in the export format - must be unique!");
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameTxt.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		if (!simple) {
			Label l1 = new Label(container, 0);
			l1.setText("Display name: ");
			displayNameTxt = new Text(container, SWT.BORDER);
			displayNameTxt.setToolTipText("The display name of the tag - can be empty!");
			displayNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
		
		if (!simple) {
			Label l2 = new Label(container, 0);
			l2.setText("Description: ");
			descriptionTxt = new Text(container, SWT.BORDER);
			descriptionTxt.setToolTipText("A descritpion for the tag - can be empty!");
			descriptionTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
			Label l3 = new Label(container, 0);
			l3.setText("Data type: ");
			typeCombo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
			typeCombo.add("String");
			typeCombo.add("Floating point number");
			typeCombo.add("Integer (number without decimal)");
			typeCombo.add("Boolean (yes / no value)");
			typeCombo.select(0);
			typeCombo.setToolTipText("The datatype of the attribute - if uncertain leave set to 'String'");
			typeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
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
		logger.trace("validating input: "+nameTxt.getText());
		
		boolean matches = RegexPattern.TAG_NAME_PATTERN.matches(nameTxt.getText());
		getButton(IDialogConstants.OK_ID).setEnabled(matches);
		
		status.setText(!matches ? "Please specify a tag name. Syntax:\n"+RegexPattern.TAG_NAME_PATTERN.getDescription() : "");
	}
	
	public void applyData() {
		if (simple)
			result = new CustomTagAttribute(nameTxt.getText());
		else {
			result = new CustomTagAttribute(nameTxt.getText(), true, 
					displayNameTxt!=null ? displayNameTxt.getText() : null, 
					descriptionTxt!=null ? descriptionTxt.getText() : null);
		
			if (typeCombo != null) {
				int ti = typeCombo.getSelectionIndex();
				Class<?> type = Object.class;
				if (ti == 1)
					type = Float.class;
				else if (ti == 2)
					type = Integer.class;
				else if (ti == 3)
					type = Boolean.class;
				
				result.setType(type);
			}
		}
	}
	
	@Override protected void okPressed() {
		applyData();
		super.okPressed();
	}
	
	public CustomTagAttribute getResult() {
		return result;
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
		return new Point(450, 300);
	}

}
