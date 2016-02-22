package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;

public class ActivityDialog extends Dialog {
	
	Group pageLocksGroup;
	PageLockTablePagination pageLockTable;

	public ActivityDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("User Avtivity");
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(700, 700);
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	    setBlockOnOpen(false);
	}	
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		pageLocksGroup = new Group(container, 0);
		pageLocksGroup.setLayout(new FillLayout());
		pageLocksGroup.setText("Opened pages per collection");
		pageLocksGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		pageLockTable = new PageLockTablePagination(pageLocksGroup, 0, 25);
//		pageLockTable.setLayoutData(new GridData(GridData.FILL_BOTH));

		return container;
	}

}
