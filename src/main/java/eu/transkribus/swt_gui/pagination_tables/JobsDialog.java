package eu.transkribus.swt_gui.pagination_tables;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class JobsDialog extends Dialog {
	
	public JobTableWidgetPagination jw;
	JobTableWidgetListener jwl;

	public JobsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		jw = new JobTableWidgetPagination(container, 0, 50);
		jw.setLayoutData(new GridData(GridData.FILL_BOTH));
		jwl = new JobTableWidgetListener(jw);
		
		container.pack();

		return container;
	}
	
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Jobs on server");
	}

	@Override protected Point getInitialSize() { return new Point(1000, 800); }
	@Override protected boolean isResizable() { return true; }
	@Override protected void createButtonsForButtonBar(Composite parent) {}

	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}

}
