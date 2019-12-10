package eu.transkribus.swt_gui.htr;

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

import eu.transkribus.core.model.beans.TextFeatsCfg;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaPreprocessingConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(PyLaiaPreprocessingConfDialog.class);
	
	PyLaiaPreprocessingConfComposite cfgComp;
	TextFeatsCfg cfg;

	public PyLaiaPreprocessingConfDialog(Shell parentShell, TextFeatsCfg cfg) {
		super(parentShell);
		
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
		
		cfgComp = new PyLaiaPreprocessingConfComposite(cont, cfg);
		cfgComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return cont;
	}
	
	private void storeConf() {
		this.cfg = cfgComp.getCurrentConfig();
	}
	
	public TextFeatsCfg getConfig() {
		return cfg;
	}
	
	@Override
	protected void okPressed() {
		storeConf();
		logger.debug("ok pressed, cfg = "+cfg);
		
		super.okPressed();
	}

}
