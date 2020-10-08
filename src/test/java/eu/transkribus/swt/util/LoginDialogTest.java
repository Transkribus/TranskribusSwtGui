package eu.transkribus.swt.util;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class LoginDialogTest {
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				LoginDialog dialog = new LoginDialog(getShell(), "creds!", new String[0], new String[]{"test"},
						0);
				dialog.open();
				return parent;
			}
		};
		aw.setBlockOnOpen(false);
		aw.open();

		Display.getCurrent().dispose();
	}
}


