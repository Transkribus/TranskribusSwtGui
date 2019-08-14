package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.util.CurrentDocPagesSelector;

public class RemoveTextRegionsConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(RemoveTextRegionsConfDialog.class);
	
	CurrentDocPagesSelector pagesSelector;
	LabeledText fractionText;
	Button dryRunBtn;
	
	Set<Integer> pageIndices=null;
	double fraction=0.0005;
	boolean dryRun=true;

	public RemoveTextRegionsConfDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(400, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Filter small text regions");
	}
	
	@Override
	protected boolean isResizable() {
	    return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(1, false));
		
		pagesSelector = new CurrentDocPagesSelector(cont, 0, true, true, true);
		pagesSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fractionText = new LabeledText(cont, "Fraction of image area: ");
		fractionText.getTextField().setText(""+fraction);
		fractionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		fractionText.getTextField().addModifyListener(e-> {
//		});
		fractionText.setToolTipText("All text-regions with an area smaller than this fraction of the image size are getting removed");
		
		dryRunBtn = new Button(cont, SWT.CHECK);
		dryRunBtn.setText("Dry run");
		dryRunBtn.setToolTipText("Do not save changed pages, just print info on how many text-regions would be removed on which pages");
		dryRunBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dryRunBtn.setSelection(dryRun);
		
		return cont;
	}
	
	@Override
	protected void okPressed() {
		try {
			pageIndices = pagesSelector.getSelectedPageIndices();
		} catch (IOException e) {
			pageIndices = null;
			DialogUtil.showErrorMessageBox(getShell(), "Invalid value", "Could not parse selected pages");
			logger.error("Could not parse page indices: "+e.getMessage(), e);
			return;
		}
		
		try {
			fraction = Double.valueOf(fractionText.getText());
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid value", "Could not parse threshold (must be a valid floating point number)");
			logger.error(e.getMessage());
			return;
		}
		
		dryRun = dryRunBtn.getSelection();
		
		logger.debug("pageIndices = "+pageIndices);
		logger.debug("threshold = "+fraction);
		logger.debug("dryRun = "+dryRun);
		
		super.okPressed();
	}

	public Set<Integer> getPageIndices() {
		return pageIndices;
	}

	public double getFraction() {
		return fraction;
	}

	public boolean isDryRun() {
		return dryRun;
	}

}
