package eu.transkribus.swt_gui.search;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class SearchDialogTest {

	private static Shell shell;
	
	public static void main(String[] args) {
		Display display = new Display();
		shell = new Shell(display);

		try {
			Storage s = Storage.getInstance();
			s.updateProxySettings();
			s.login(TrpServerConn.PROD_SERVER_URI, args[0], args[1]);
			SearchDialog search = new SearchDialog(shell);
			search.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}


}
