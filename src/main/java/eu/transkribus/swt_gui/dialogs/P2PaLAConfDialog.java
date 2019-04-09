package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class P2PaLAConfDialog extends Dialog {

	public P2PaLAConfDialog(Shell parentShell) {
		super(parentShell);
		
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		Label l = new Label(cont, 0);
		l.setText("P2PaLA conf dialog!");
		
		return cont;
	}
	
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected void okPressed() {
//		storeSelectionInParameterMap();
		super.okPressed();
	}	

}
