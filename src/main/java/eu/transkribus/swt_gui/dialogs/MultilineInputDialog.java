package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class MultilineInputDialog extends InputDialog {

	public MultilineInputDialog(Shell parentShell, String dialogTitle, String dialogMessage, String initialValue, IInputValidator validator) {
		super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
	}
	
	protected int getInputTextStyle() {
		return SWT.MULTI | SWT.BORDER | SWT.V_SCROLL;
	}
	
	protected Control createDialogArea(Composite parent) {
		Control ctrl = super.createDialogArea(parent);
		((GridData) this.getText().getLayoutData()).heightHint = 100;
		return ctrl;
	}
	
}
