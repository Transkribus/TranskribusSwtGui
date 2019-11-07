package eu.transkribus.swt_gui.models;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.ATrpModel;
import eu.transkribus.core.util.ModelUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class EditModelDialog extends Dialog  {
	
	private ATrpModel model;
	private Storage store = Storage.i();
	private LabeledText nameTxt, descTxt;

	public EditModelDialog(Shell parentShell, ATrpModel model) {
		super(parentShell);
		this.model = model;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Share model '"+model.getName()+"'");
	      SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected Point getInitialSize() {
		Point s = SWTUtil.getPreferredSize(getShell());
		return new Point(s.x+200, s.y+50);
	}	
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
				
		nameTxt = new LabeledText(cont, "Name: ", true);
		nameTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameTxt.setText(model.getName());
		
		descTxt = new LabeledText(cont, "Description: ", true);
		descTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descTxt.setText(model.getDescription());
				
		return cont;
	}	
	
	private boolean updateModelOnServer() {
		if (StringUtils.isEmpty(nameTxt.getText())) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "The model name cannot be empty!");
			return false;
		}
		
		if (!store.isLoggedIn()) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "You are not logged in!");
			return false;
		}
		
		try {
			model.setName(nameTxt.getText());
			model.setDescription(descTxt.getText());
			
			Class clazz = ModelUtil.getModelClass(model.getType());
			store.getConnection().getModelCalls().updateModel(model, clazz);
			return true;
		}
		catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error updating model on server", e.getMessage());
			return false;
		}
	}
	
	public ATrpModel getModel() {
		return model;
	}
	
	@Override protected void okPressed() {
		if (updateModelOnServer()) {
			super.okPressed();	
		}
	}

}
