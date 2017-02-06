package eu.transkribus.swt_gui.pagination_tables;

import static org.junit.Assert.*;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.swt_gui.mainwidget.menubar.TrpMenuBar;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class JobsDialogTest {

	public static void main(final String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
//				getShell().setSize(100, 100);
				
				try {
					Storage s = Storage.getInstance();
					s.login(TrpServerConn.PROD_SERVER_URI, args[0], args[1]);
				
					JobsDialog d = new JobsDialog(getShell());
					d.open();
					
				
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
