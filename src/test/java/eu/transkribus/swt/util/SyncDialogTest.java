package eu.transkribus.swt.util;

import java.io.IOException;

import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.swt_gui.dialogs.DocSyncDialog;


public class SyncDialogTest {

	public static void main(String[] args) {
		TrpDoc doc1, doc2;
		try {
			doc1 = LocalDocReader.load("C:\\Users\\lange\\Desktop\\Data_Transkribus_tmp\\ABP_Brenner_Georg_test\\ABP_Brenner_Georg_test");
			doc2 = LocalDocReader.load("C:\\Users\\lange\\Desktop\\Data_Transkribus_tmp\\ABP_Brenner_Georg_test\\ABP_Brenner_Georg_test-Kopie");
			DocSyncDialog syncDialog = new DocSyncDialog(null, doc1, doc2);
			syncDialog.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
