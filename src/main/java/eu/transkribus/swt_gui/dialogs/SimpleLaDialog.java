package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DocPagesSelector;

public class SimpleLaDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(SimpleLaDialog.class);
	private Storage store = Storage.getInstance();
	private DocPagesSelector dps;
	private Button doBlockSegBtn, doLineSegBtn, currPageBtn;
	
	private boolean doLineSeg, doBlockSeg;
	private String pages;
	
	public SimpleLaDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite mainContainer = (Composite) super.createDialogArea(parent);
		GridLayout g = (GridLayout)mainContainer.getLayout();
		g.numColumns = 2;
		
		Group checkGrp = new Group(mainContainer,SWT.NONE);
		checkGrp.setLayout(new GridLayout(2, false));
		checkGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 2));
		doBlockSegBtn = new Button(checkGrp, SWT.CHECK);
		Label dbsLbl = new Label(checkGrp, SWT.FLAT);
		dbsLbl.setText("Find Text Regions");
		doLineSegBtn = new Button(checkGrp, SWT.CHECK);
		Label dlsLbl = new Label(checkGrp, SWT.FLAT);
		dlsLbl.setText("Find Lines in Text Regions");
		
		dps = new DocPagesSelector(mainContainer, SWT.NONE, store.getDoc().getPages());
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		currPageBtn = new Button(mainContainer, SWT.PUSH);
		currPageBtn.setText("Current Page");
		
		setPageSelectionToCurrentPage();
		
		addListener();
		return mainContainer;
	}
		
	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout Analysis");
	}

	@Override protected Point getInitialSize() {
		return new Point(350, 250);
	}
	
	
	
	private void addListener() {	
		currPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e){
				setPageSelectionToCurrentPage();
			}
		});
	}
	
	private void setPageSelectionToCurrentPage(){
		dps.getPagesText().setText(""+store.getPage().getPageNr());
	}
	
	public void setPageSelectionToSelectedPages(String pages){
		dps.getPagesText().setText(pages);
	}
	
	public Button getDoBlockSegBtn() {
		return doBlockSegBtn;
	}

	public Button getDoLineSegBtn() {
		return doLineSegBtn;
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
		
		doLineSeg = doLineSegBtn.getSelection();
		doBlockSeg = doBlockSegBtn.getSelection();
		pages = getSelectedPages(); 
		
		boolean userMessedUpOnPageSelection;
		try{
			userMessedUpOnPageSelection = dps.getSelectedPageIndices().isEmpty();
		} catch(IOException e){
			userMessedUpOnPageSelection = true;
		}
		
		if(userMessedUpOnPageSelection) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select pages to be analyzed.");
		} else if (!doLineSeg && !doBlockSeg) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select at least one analysis step.");
		} else {
//			int goOn = SWT.YES;
//		
//			boolean dontShowWarning = false;
//			if(doLineSeg && !doBlockSeg && !dontShowWarning){
//		//				goOn = DialogUtil.showYesNoDialog(super.getShell(), "Info", "Lines can only be found if text blocks already are defined! Are you sure you want to proceed?");
//				String[] btnArr = new String[]{"Yes", "No"};
//				Pair<Integer, Boolean> result = DialogUtil.showMessageDialogWithToggle(super.getShell(), 
//						"Info", 
//						"Lines can only be found if text blocks already are defined! Are you sure you want to proceed?",
//						"Do not show this message again", 
//						false, 
//						SWT.CHECK, 
//						btnArr);
//				if(result.getLeft() == 1) {
//					logger.debug("User clicked NO");
//					goOn = SWT.NO;
//				}
//				if(result.getRight() == true) {
//					//store stuff
//				}
//				
//			}
//			if(goOn == SWT.YES) {		
				super.okPressed();
//			}
		}
	}

	private String getSelectedPages() {
		return dps.getPagesText().getText();
	}
	
	public boolean isDoLineSeg(){
		return doLineSeg;
	}
	
	public boolean isDoBlockSeg(){
		return doBlockSeg;
	}
	
	public String getPages(){
		return pages;
	}
}
