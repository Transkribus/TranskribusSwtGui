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

public class MergeTextLinesConfDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(MergeTextLinesConfDialog.class);
	
	CurrentTranscriptOrCurrentDocPagesSelector pagesSelector;
//	LabeledText threshPercText;
	Button dryRunBtn;
	Button forSelectedBtn;
	
	Combo threshPercCombo;
	
	Set<Integer> pageIndices=null;
	double threshPixel=10;
	boolean dryRun=true;
	boolean applySelected=true;
	boolean doCurrentPage=true;

	public MergeTextLinesConfDialog(Shell parentShell) {
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
		newShell.setText("Merge small text lines");
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
		threshLabel.setText("Horizontal threshold in Pixels: ");
		threshPercCombo = new Combo(threshContainer, SWT.DROP_DOWN);
		threshPercCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		AutoCompleteField threshAutocomplete = new AutoCompleteField(threshPercCombo, new ComboContentAdapter(), new String[] {});
		String[] items = { "5", "10", "15", "20", "30", "50" };
		threshPercCombo.setItems(items);
		threshAutocomplete.setProposals(items);
		threshPercCombo.select(2);
		threshPercCombo.setToolTipText("All text-lines of the same region in a row (+/- the threshold) will be merged");
		
		dryRunBtn = new Button(cont, SWT.CHECK);
		dryRunBtn.setText("Dry run");
		dryRunBtn.setToolTipText("Do not save pages. Use to determine how many lines would be merged.");
		dryRunBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		dryRunBtn.setSelection(dryRun);
		
		forSelectedBtn = new Button(cont, SWT.CHECK);
		forSelectedBtn.setText("Apply to selected regions (only for current page)");
		forSelectedBtn.setToolTipText("First select some regions you want to handle!");
		forSelectedBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		forSelectedBtn.setSelection(applySelected);
		
		return cont;
	}
	
	@Override
	protected void okPressed() {
		try {
			pageIndices = pagesSelector.getSelectedPageIndices();
			setDoCurrentPage(pagesSelector.isCurrentTranscript());
			
			applySelected = forSelectedBtn.getSelection();
		} catch (IOException e) {
			pageIndices = null;
			DialogUtil.showErrorMessageBox(getShell(), "Invalid value", "Could not parse selected pages");
			logger.error("Could not parse page indices: "+e.getMessage(), e);
			return;
		}
		
		try {
//			threshPerc = Double.valueOf(threshPercText.getText());
			threshPixel = Double.valueOf(threshPercCombo.getText());
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid value", "Could not parse threshold (must be a valid floating point number)");
			logger.error(e.getMessage());
			return;
		}
		
		dryRun = dryRunBtn.getSelection();
		applySelected = forSelectedBtn.getSelection();
		
		logger.debug("pageIndices = "+pageIndices);
		logger.debug("threshPerc = "+threshPixel);
		logger.debug("dryRun = "+dryRun);
		
		super.okPressed();
	}

	public boolean isDoCurrentPage() {
		return doCurrentPage;
	}

	public void setDoCurrentPage(boolean doCurrentPage) {
		this.doCurrentPage = doCurrentPage;
	}

	public Set<Integer> getPageIndices() {
		return pageIndices;
	}

	public double getThreshPixel() {
		return threshPixel;
	}

	public boolean isDryRun() {
		return dryRun;
	}
	
	public boolean isApplySelected() {
		return applySelected;
	}

	public void setSelected(boolean selected) {
		this.applySelected = selected;
	}

}
