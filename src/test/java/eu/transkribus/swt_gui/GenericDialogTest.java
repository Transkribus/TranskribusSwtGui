package eu.transkribus.swt_gui;

import java.util.concurrent.Future;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.ATrpServerConn;
import eu.transkribus.core.util.UnicodeList;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.htr.HtrModelsDialog;
import eu.transkribus.swt_gui.htr.Text2ImageConfDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsWidget;
import eu.transkribus.swt_gui.vkeyboards.VirtualKeyboardEditor;

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
					getShell().setSize(500, 500);
					SWTUtil.centerShell(getShell());
					
//					System.out.println(Storage.getInstance().loadTextRecognitionConfig());
	//				HtrTextRecognitionConfigDialog diag = new HtrTextRecognitionConfigDialog(getShell(), null);
					
					if (true) {
						final TrpVirtualKeyboardsWidget vk = new TrpVirtualKeyboardsWidget(parent, 0);
						
//						UnicodeList ul = new UnicodeList("Hebrew", "U+0590-U+05ff U+fb1d-U+fb4f");
//						VirtualKeyboard vk = new VirtualKeyboard(parent, 0, ul);
						getShell().setSize(1000, 700);
						SWTUtil.centerShell(getShell());						
					}
					
					if (false) {
						VirtualKeyboardEditor vk = new VirtualKeyboardEditor(parent, 0);
						UnicodeList ul = new UnicodeList("Hebrew", "U+0590-U+05ff U+fb1d-U+fb4f");
						vk.setUnicodeList(ul.getUnicodes());
						
						MessageDialog md = DialogUtil.createCustomMessageDialog(getShell(), "vkeyboardeditor", null, null, SWT.RESIZE, new String[]{"OK", "Cancel"}, 0, vk, new Point(1000, 750));
//						md.getShell().setSize(1000, 1000);
						if (md.open()==0) {
							System.out.println("OK PRESSED");
						}
						
//						if (DialogUtil.showCustomMessageDialog(getShell(), "vkeyboardeditor", null, null, SWT.RESIZE, new String[]{"OK", "Cancel"}, 0, vk)==0) {
//							System.out.println("OK PRESSED");
//						}
						
//						getShell().setSize(1000, 700);
//						SWTUtil.centerShell(getShell());
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
