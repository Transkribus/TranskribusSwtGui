package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class TableMarkupBoxTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {

				getShell().setSize(600, 600);
				TableMarkupBox tmb = new TableMarkupBox(getShell(), "Test cell markup");
				tmb.show();
				return parent;
			}
		};
		aw.setBlockOnOpen(false);
		aw.open();
	
		while (!aw.getShell().isDisposed()) {
			try {
				if (!Display.getCurrent().readAndDispatch()) {
					Display.getCurrent().sleep();
				}
			} catch (Throwable th) {
				System.err.println("Unexpected error occured: "+th.getMessage());
			}
		}
		
	}
}
