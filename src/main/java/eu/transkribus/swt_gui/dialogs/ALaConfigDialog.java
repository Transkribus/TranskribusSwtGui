package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.rest.ParameterMap;

public abstract class ALaConfigDialog extends Dialog {

	protected ParameterMap parameters;

	public ALaConfigDialog(Shell parent, ParameterMap parameters) {
		super(parent);
		if(parameters == null) {
			this.parameters = new ParameterMap();
		} else {
			this.parameters = parameters;
		}
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected void okPressed() {
		storeSelectionInParameterMap();
		super.okPressed();
	}
	
	/**
	 * Read out dialog-specific settings and store them in the parameter map using the respective parameter names used in the backend
	 */
	protected abstract void storeSelectionInParameterMap();
	/**
	 * Set dialog-specific fields according to the values given in the parameter map 
	 */
	protected abstract void applyParameterMapToDialog();
	
	public ParameterMap getParameters() {
		return parameters;
	}
}
