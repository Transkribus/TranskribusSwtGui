package eu.transkribus.swt_gui.dialogs;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.SWTUtil;

public class TrpErrorDialogTest {

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setSize(600, 600);
				
//				TrpErrorDialog d = new TrpErrorDialog(getShell(), "Error in...", "I am Error!", "detailed error message!", new Exception("asdadf", new Exception("ddd", new Exception())));
				TrpErrorDialog d = new TrpErrorDialog(getShell(), "Error in...", "I am Error!", null, new Exception("asdadf", new Exception("ddd", new Exception())));
//				TrpErrorDialog d = new TrpErrorDialog(getShell(), "Error in...", "I am Error!", "detailed error message!", null);
				d.open();
				
//				s = new Status(IStatus.ERROR, "ID0", 0, "", null);
//				ExceptionDetailsErrorDialog.openError(getShell(), "asdf", "massage", new Status(IStatus.ERROR, "ID0", 0, "", null));

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
