package eu.transkribus.swt_gui.dialogs.la;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.SWTUtil;

public class LayoutAnalysisConfigDialogTest {

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setSize(300, 200);
				SWTUtil.centerShell(getShell());
				
				LayoutAnalysisConfigDialog diag = new LayoutAnalysisConfigDialog(getShell());
				diag.open();

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
