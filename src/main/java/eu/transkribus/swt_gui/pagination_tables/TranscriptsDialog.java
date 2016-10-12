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

public class TranscriptsDialog extends Dialog {
	
	TranscriptsTableWidgetPagination transcriptsWidget;
	TranscriptsTableWidgetListener transcriptsWidgetListener;
	
	public TranscriptsDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setSize(1000, 800);
	      SWTUtil.centerShell(shell);
	      shell.setText("Versions");
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
		transcriptsWidget = new TranscriptsTableWidgetPagination(container, 0, 50);
		transcriptsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		transcriptsWidgetListener = new TranscriptsTableWidgetListener(transcriptsWidget);
			
		container.pack();

		return container;
	}

	@Override protected Point getInitialSize() { return new Point(1000, 800); }
	@Override protected boolean isResizable() { return true; }
	@Override protected void createButtonsForButtonBar(Composite parent) {}

	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
	}

}
