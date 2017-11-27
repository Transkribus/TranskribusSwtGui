package eu.transkribus.swt_gui;

import java.util.concurrent.Future;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.ATrpServerConn;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.htr.HtrModelsDialog;
import eu.transkribus.swt_gui.htr.Text2ImageConfDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.TagConfDialog;
import eu.transkribus.swt_gui.metadata.TaggingWidgetDialog;

public class GenericDialogTest {

	public static void main(String[] args) throws Exception {
		Storage store=null;
		try {
			
			if (false) { // load Storage?
			store = Storage.getInstance();
			store.login(ATrpServerConn.TEST_SERVER_URI, args[0], args[1]);
			Future<?> fut = store.reloadDocList(1); // reload doclist of a collection just that the collection id gets set!
//			store.loadRemoteDoc(1, 455); // bentham doc on testserver
			
			fut.get();
			}
			
			ApplicationWindow aw = new ApplicationWindow(null) {
				@Override
				protected Control createContents(Composite parent) {
					getShell().setSize(300, 200);
					SWTUtil.centerShell(getShell());
					
//					System.out.println(Storage.getInstance().loadTextRecognitionConfig());
	//				HtrTextRecognitionConfigDialog diag = new HtrTextRecognitionConfigDialog(getShell(), null);
					
					if (true) {
						TaggingWidgetDialog diag = new TaggingWidgetDialog(getShell());
						if (diag.open() == Dialog.OK) {
							
						}
					}
					
					if (false) {
						TagConfDialog diag = new TagConfDialog(getShell());
						if (diag.open() == Dialog.OK) {
							
						}
					}
					
					if (false) {
					HtrModelsDialog diag = new HtrModelsDialog(getShell());
					if (diag.open() == Dialog.OK) {
						System.out.println("selected model: "+diag.getSelectedHtr());
					}
					}
					
					if (false) {
					Text2ImageConfDialog diag = new Text2ImageConfDialog(getShell());
					if (diag.open() == Dialog.OK) {
						System.out.println("conf: "+diag.getConfig());
					}
					}
					
//					if (true) {
//						HtrTrainingDialog diag = new HtrTrainingDialog(getShell());
//						diag.open();
//					}
					
//					if (true) {
//						MyMessageDialog diag = new MyMessageDialog(parentShell, dialogTitle, dialogTitleImage, dialogMessage, image, dialogButtonLabels, defaultIndex);
//						
//						
//					}
	
					return parent;
				}
			};
			aw.setBlockOnOpen(true);
			aw.open();
	
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (store!=null) {
				store.logout();
			}
		}
	}

}
