package eu.transkribus.swt_gui.collection_manager;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;

public class CollectionUsersDialog extends Dialog {
	
	CollectionUsersWidget cuw;
	TrpCollection collection;

	public CollectionUsersDialog(Shell parentShell, TrpCollection collection) {
		super(parentShell);
		
		this.collection = collection;
	}
	
	public void setCollection(TrpCollection collection) {
		this.collection = collection;
		
		cuw.setCollection(collection);
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Users in Collection");
	      SWTUtil.centerShell(shell);
	}
	

	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(600, 800);
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.APPLICATION_MODAL /*| SWT.MODELESS*/| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
//	    setBlockOnOpen(false);
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FillLayout());

		cuw = new CollectionUsersWidget(container, 0);
		cuw.setCollection(collection);
		
		return container;
	}

}
