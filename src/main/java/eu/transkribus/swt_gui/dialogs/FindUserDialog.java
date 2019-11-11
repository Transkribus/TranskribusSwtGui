package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt_gui.collection_manager.FindUsersWidget;

public class FindUserDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(FindUserDialog.class);
	
	FindUsersWidget fuw;
	
	List<TrpUser> selectedUsers=new ArrayList<>();
	
	public FindUserDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		return new Point(p.x, Math.max(600, p.y+50));
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    Button okBtn = createButton(parent, IDialogConstants.OK_ID, "OK", false);
	    okBtn.setToolTipText("Selected users will be returned");
	}	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		
		fuw = new FindUsersWidget(cont, 0);
		fuw.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return cont;
	}
	
	@Override
	protected void okPressed() {
		selectedUsers = fuw.getSelectedUsers();
		super.okPressed();
	}
			
	public List<TrpUser> getSelectedUsers() {
		return selectedUsers;
	}
			
		

}
