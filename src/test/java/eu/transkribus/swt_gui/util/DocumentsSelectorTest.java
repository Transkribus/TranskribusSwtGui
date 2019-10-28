package eu.transkribus.swt_gui.util;

import java.util.List;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.client.connection.ATrpServerConn.TrpServer;
import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.DocumentsSelectorDialog;

public class DocumentsSelectorTest {
	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				TrpProperties creds = new TrpProperties("testCreds.properties");
				try (TrpServerConn conn = new TrpServerConn(TrpServer.Test, creds.getString("username"), creds.getString("password"))) {
					List<TrpDocMetadata> docs = conn.getAllDocs(1);
					System.out.println("nr of docs = "+docs.size());

//					DocumentsSelector ds = new DocumentsSelector(parent, 0, false, true);
//					ds.setDataList(docs);
					
					getShell().setSize(500, 800);
					SWTUtil.centerShell(getShell());
					
					DocumentsSelectorDialog dsd = new DocumentsSelectorDialog(getShell(), "Select documents", docs, true);
					if (dsd.open() == IDialogConstants.OK_ID) {
//						System.out.println("n selected documents: "+dsd.getCheckedDocs().size());
//						System.out.println("selected documents: "+CoreUtils.toListString(dsd.getCheckedDocs()));
						System.out.println("docselections: "+CoreUtils.join(dsd.getCheckedDocSelections()));
						
						for (TrpDocMetadata d : docs) {
							System.out.println(d);
						}
						
						DocumentsSelectorDialog dsd2 = new DocumentsSelectorDialog(getShell(), "Select documents2", docs, true, dsd.getCheckedDocSelections());
						if (dsd2.open() == IDialogConstants.OK_ID) {
							System.out.println("n selected documents: "+dsd2.getCheckedDocs().size());
							System.out.println("selected documents: "+CoreUtils.toListString(dsd2.getCheckedDocs()));
							System.out.println(CoreUtils.join(dsd.getCheckedDocSelections()));
						}						
					}
					
//					if (true)
//						break;
					
			
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
	
				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();
	
		Display.getCurrent().dispose();

	}
}
