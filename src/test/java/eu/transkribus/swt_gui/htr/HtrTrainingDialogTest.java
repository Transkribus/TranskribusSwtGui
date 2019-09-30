package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.swt_gui.TestApplicationWindow;

public class HtrTrainingDialogTest {
	private static final Logger logger = LoggerFactory.getLogger(HtrTrainingDialogTest.class);
	
	public static void main(String[] args) {
		if(args.length == 0) {
			throw new IllegalArgumentException("No arguments.");
		}
		
		boolean showGtData = true;
		
		boolean displayJsonDescriptorOnChange = true;
		
		new TestApplicationWindow(TrpServerConn.TEST_SERVER_URI, args[0], args[1], 575) {
			@Override
			protected void createTestContents(Composite parent) throws Exception {
				getShell().setSize(500, 700);
//				getStorage().reloadHtrs();
				parent.pack();
				HtrTrainingDialog htd = new HtrTrainingDialog(
						parent.getShell(), 
						showGtData ? getStorage().getHtrs(null) : null, 
						getStorage().getHtrTrainingJobImpls()
						);
				
				//show/update debug dialog with JSON descriptor on each change in data set
				htd.enableDebugDialog(displayJsonDescriptorOnChange);
				
				if (htd.open() == IDialogConstants.OK_ID) {
					// new: check here if user wants to store or not
					// if (!mw.saveTranscriptDialogOrAutosave()) {
					// //if user canceled this
					// return;
					// }
					String jobId = null;
					if (htd.getCitlabTrainConfig() != null) {
						CitLabHtrTrainConfig config = htd.getCitlabTrainConfig();
						logger.info("conig = " + config);
					} else if (htd.getCitlabT2IConfig() != null) {
						CitLabSemiSupervisedHtrTrainConfig config = htd.getCitlabT2IConfig();
						logger.info("conig = " + config);
					}
				}
			}
		}.show();
	}
}
