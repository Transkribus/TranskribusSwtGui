package eu.transkribus.swt_gui.vkeyboards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class TrpVirtualKeyboardsDialog extends Dialog {

	TrpVirtualKeyboardsWidget vk;

	public TrpVirtualKeyboardsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));

		vk = new TrpVirtualKeyboardsWidget(container, 0);
		vk.setLayoutData(new GridData(GridData.FILL_BOTH));

		return container;
	}

	public TrpVirtualKeyboardsTabWidget getVkTabWidget() {
		return vk.getVirtualKeyboardsTabWidget();
	}

	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Virtual keyboards");
	}

	@Override protected Point getInitialSize() {
		return new Point(800, 400);
	}

	@Override protected boolean isResizable() {
		return true;
	}

	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}
}
