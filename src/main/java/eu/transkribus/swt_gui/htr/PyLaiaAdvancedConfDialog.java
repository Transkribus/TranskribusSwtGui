package eu.transkribus.swt_gui.htr;

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.PyLaiaTrainCtcPars;
import eu.transkribus.core.model.beans.TextFeatsCfg;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaAdvancedConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(PyLaiaAdvancedConfDialog.class);
	
	PyLaiaAdvancedConfComposite cfgComp;
	TextFeatsCfg cfg;
	int batchSize = PyLaiaTrainCtcPars.DEFAULT_BATCH_SIZE;

	public PyLaiaAdvancedConfDialog(Shell parentShell, int batchSize, TextFeatsCfg cfg) {
		super(parentShell);
		
		this.batchSize = batchSize;
		this.cfg = cfg;
	}
	
	@Override
	protected Point getInitialSize() {
		return SWTUtil.getPreferredOrMinSize(getShell(), 400, 250);
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		
		// TODO: create help page
//		Button helpBtn = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
//		helpBtn.setImage(Images.HELP);
//		SWTUtil.onSelectionEvent(helpBtn, e -> {
//			org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/Text2Image");
//		});
		
		createButton(parent, IDialogConstants.OK_ID, "OK", false);		
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("PyLaia preprocessing configuration");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		
		cfgComp = new PyLaiaAdvancedConfComposite(cont, batchSize, cfg);
		cfgComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return cont;
	}
	
	private void storeConf() throws IOException {
		this.batchSize = cfgComp.getCurrentBatchSize();
		this.cfg = cfgComp.getCurrentConfig();
	}
	
	public TextFeatsCfg getConfig() {
		return cfg;
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	
	@Override
	protected void okPressed() {
		try {
			storeConf();
			logger.debug("ok pressed, batchsize = "+batchSize+", cfg = "+cfg);
			super.okPressed();
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error storing parameter", e.getMessage());
		}
	}

}
