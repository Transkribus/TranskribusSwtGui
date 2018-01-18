package eu.transkribus.swt_gui.search;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class SearchDialogTest {

	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				try {
					Storage s = Storage.getInstance();
					s.updateProxySettings();
					s.login(TrpServerConn.PROD_SERVER_URI, args[0], args[1]);
					SearchDialog search = new SearchDialog(getShell());
					search.open();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}
}
