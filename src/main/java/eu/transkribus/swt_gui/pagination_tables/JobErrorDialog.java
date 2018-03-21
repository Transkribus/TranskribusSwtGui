package eu.transkribus.swt_gui.pagination_tables;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.SWTUtil;

public class JobErrorDialog extends Dialog {
	
	public JobErrorTableWidgetPagination jw;
	protected JobErrorTableWidgetListener jwl;
	protected int jobId;

	public JobErrorDialog(Shell parentShell, final int jobId) {
		super(parentShell);
		this.jobId = jobId;
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		jw = new JobErrorTableWidgetPagination(container, 0, 50, this.jobId);
		jw.setLayoutData(new GridData(GridData.FILL_BOTH));
		jwl = new JobErrorTableWidgetListener(jw);
		
		container.pack();

		return container;
	}
	
	@Override protected void configureShell(Shell shell) {
		super.configureShell(shell);
		SWTUtil.centerShell(shell);
		shell.setText("Errors in job " + jobId);
	}

	@Override protected Point getInitialSize() { return new Point(1000, 800); }
	@Override protected boolean isResizable() { return true; }
	@Override protected void createButtonsForButtonBar(Composite parent) {}

	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}

	/**
	 * Set new JobID and update the table
	 * 
	 * @param jobId
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
		jw.reloadJobErrorList();
	}

}
