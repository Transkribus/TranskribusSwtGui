/**
 * 
 */
package eu.transkribus.swt_gui.upload;

import java.io.IOException;

import javax.security.auth.login.LoginException;
import javax.ws.rs.ClientErrorException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.ATrpServerConn;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * @author lange
 *
 */
public class UploadDialogUltimateTest {

	public static void main(String[] args) throws IOException, LoginException, ClientErrorException {
		// set storage info in order to have the collection list work properly
		Storage store = Storage.getInstance();
		store.updateProxySettings();
		store.login(ATrpServerConn.PROD_SERVER_URI, args[0], args[1]);
		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setSize(600, 600);
				SWTUtil.centerShell(getShell());
				
				TrpCollection col = store.getCollection(Integer.parseInt(args[2]));
				
				UploadDialogUltimate upload = new UploadDialogUltimate(getShell(), col);
				upload.open();

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}
}
