package eu.transkribus.swt_gui.la;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.la.Text2ImageSimplifiedConfComposite.Text2ImageConf;

public class Text2ImageSimplifiedDialog extends Dialog {
	Text2ImageConf conf;
	Text2ImageSimplifiedConfComposite confComp;

	public Text2ImageSimplifiedDialog(Shell parentShell, Text2ImageConf conf) {
		super(parentShell);
		this.conf = conf;
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(400, 300);
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		
		Button helpBtn = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
		helpBtn.setImage(Images.HELP);
		SWTUtil.onSelectionEvent(helpBtn, e -> {
			org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/Text2Image");
		});
		
	    Button runBtn = createButton(parent, IDialogConstants.OK_ID, "Run", false);
	    runBtn.setImage(Images.ARROW_RIGHT);
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text2Image configuration");
		newShell.setMinimumSize(400, 250);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		
		confComp = new Text2ImageSimplifiedConfComposite(cont, 0, conf);
		confComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		parent.getShell().setMinimumSize(confComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		return cont;
	}
	
	private void storeConf() {
		this.conf = confComp.getConfigFromUi();
	}
	
	public Text2ImageConf getConfig() {
		return conf;
	}
	
	@Override
	protected void okPressed() {
		storeConf();
		super.okPressed();
	}

}
