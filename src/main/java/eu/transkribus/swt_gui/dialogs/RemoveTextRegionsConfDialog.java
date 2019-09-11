package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class RemoveTextRegionsConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(RemoveTextRegionsConfDialog.class);
	
//	CurrentDocPagesSelector pagesSelector;
	CurrentTranscriptOrCurrentDocPagesSelector pagesSelector;
//	LabeledText threshPercText;
	Button dryRunBtn;
	
//	ComboViewer threshPercComboViewer;
	Combo threshPercCombo;
	
	Set<Integer> pageIndices=null;
	double threshPerc=0.05;
	boolean dryRun=true;

	public RemoveTextRegionsConfDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(400, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}
	
	@Override
	protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE);
	    setBlockOnOpen(false);
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
		
		pagesSelector = new CurrentTranscriptOrCurrentDocPagesSelector(cont, SWT.NONE, true,true);
		pagesSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite threshContainer = new Composite(cont, 0);
		threshContainer.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		threshContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label threshLabel = new Label(threshContainer, 0);
		threshLabel.setText("Threshold percentage of image size: ");
		threshPercCombo = new Combo(threshContainer, SWT.DROP_DOWN);
		threshPercCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		threshPercCombo.setToolTipText("The model used for the P2PaLA Layout Analysis");
		AutoCompleteField threshAutocomplete = new AutoCompleteField(threshPercCombo, new ComboContentAdapter(), new String[] {});
		String[] items = { "5", "1", "0.5", "0.1", "0.05", "0.01", "0.005", "0.001" };
		threshPercCombo.setItems(items);
		threshAutocomplete.setProposals(items);
		threshPercCombo.select(4);
		threshPercCombo.setToolTipText("All text-regions smaller than this percent of the image size are getting removed");
		
//		threshPercText = new LabeledText(cont, "Percent of image area: ");
//		threshPercText.getTextField().setText(""+threshPerc);
//		threshPercText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		threshPercText.setToolTipText("All text-regions smaller than this percent of the image size are getting removed");
		
		dryRunBtn = new Button(cont, SWT.CHECK);
		dryRunBtn.setText("Dry run");
		dryRunBtn.setToolTipText("Do not save pages. Use to determine how many regions would be removed with a certain threshold.");
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
//			threshPerc = Double.valueOf(threshPercText.getText());
			threshPerc = Double.valueOf(threshPercCombo.getText());
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid value", "Could not parse threshold (must be a valid floating point number)");
			logger.error(e.getMessage());
			return;
		}
		
		dryRun = dryRunBtn.getSelection();
		
		logger.debug("pageIndices = "+pageIndices);
		logger.debug("threshPerc = "+threshPerc);
		logger.debug("dryRun = "+dryRun);
		
		super.okPressed();
	}

	public Set<Integer> getPageIndices() {
		return pageIndices;
	}

	public double getThreshPerc() {
		return threshPerc;
	}

	public boolean isDryRun() {
		return dryRun;
	}

}
