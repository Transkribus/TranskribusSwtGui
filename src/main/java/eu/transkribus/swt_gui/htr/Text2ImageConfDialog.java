package eu.transkribus.swt_gui.htr;

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.swt.util.DialogUtil;

public class Text2ImageConfDialog extends Dialog {
	
	Text2ImageConfComposite text2ImageComp;
	CitLabSemiSupervisedHtrTrainConfig config;

	public Text2ImageConfDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
	
		text2ImageComp = new Text2ImageConfComposite(cont, 0);
		text2ImageComp.setLayoutData(new GridData(GridData.FILL_BOTH));
				
		return cont;
	}
	
	@Override
	protected void okPressed() {
		try {
			config = text2ImageComp.getConfig();
			super.okPressed();
		} catch (IOException e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error retrieving configuration", e.getMessage());
			return;
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text to Image Configuration");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
	
	public CitLabSemiSupervisedHtrTrainConfig getConfig() {
		return config;
	}
}
