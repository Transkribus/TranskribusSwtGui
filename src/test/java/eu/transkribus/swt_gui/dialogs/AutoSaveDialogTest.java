package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt_gui.TrpConfig;

public class AutoSaveDialogTest {
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				AutoSaveDialog dialog = new AutoSaveDialog(getShell(), TrpConfig.getTrpSettings());
				dialog.open();
				return parent;
			}
		};
		aw.setBlockOnOpen(false);
		aw.open();

		Display.getCurrent().dispose();
	}
}

