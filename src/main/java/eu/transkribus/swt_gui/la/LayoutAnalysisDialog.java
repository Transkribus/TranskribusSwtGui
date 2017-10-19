package eu.transkribus.swt_gui.la;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;

public class LayoutAnalysisDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(LayoutAnalysisDialog.class);
	
	LayoutAnalysisComposite laComp;
	
	private boolean doLineSeg, doBlockSeg, doWordSeg, currentTranscript;
	private String jobImpl="";
	private String pages;

	public LayoutAnalysisDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	

	@Override protected Control createDialogArea(Composite parent) {
		Composite mainContainer = (Composite) super.createDialogArea(parent);
		GridLayout g = (GridLayout)mainContainer.getLayout();
		g.numColumns = 1;
		g.makeColumnsEqualWidth = false;
//		g.marginHeight = g.marginWidth = 0;
		
		laComp = new LayoutAnalysisComposite(mainContainer, 0);
		laComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return mainContainer;
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout Analysis");
	}

	@Override protected Point getInitialSize() {
		return new Point(400, 400);
	}

	@Override protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		return ctrl;
	}
	
	// override method to use "Login" as label for the OK button
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Run", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		jobImpl = laComp.getJobImpl();
		
		doLineSeg = laComp.isDoLineSeg();
		doBlockSeg = laComp.isDoBlockSeg();
		doWordSeg = laComp.isDoWordSeg();
		
		currentTranscript = laComp.isCurrentTranscript();
		
		pages = getSelectedPages();
				
		if (StringUtils.isEmpty(pages)) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select pages to be analyzed.");
		} else if (!doLineSeg && !doBlockSeg && !doWordSeg) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select at least one analysis step.");
		} else {
			super.okPressed();
		}
	}

	private String getSelectedPages() {
		return pages;
	}
	
	public boolean isDoLineSeg(){
		return doLineSeg;
	}
	
	public boolean isDoBlockSeg(){
		return doBlockSeg;
	}
	
	public boolean isDoWordSeg() {
		return doWordSeg;
	}
	
	public boolean isCurrentTranscript() {
		return currentTranscript;
	}
	
	public String getPages(){
		return pages;
	}
	
	public String getJobImpl() {
		return jobImpl;
	}

	public void setPageSelectionToSelectedPages(String pages2) {
		pages = pages2;
		laComp.setPageSelectionToSelectedPages(pages2);
	}
}
