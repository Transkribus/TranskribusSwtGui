package eu.transkribus.swt_gui.dialogs.la;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DocPagesSelector;

public class LayoutAnalysisDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(LayoutAnalysisDialog.class);
	
	public static boolean TEST = false;
	
	private Storage store = TEST ? null : Storage.getInstance();
	private DocPagesSelector dps;
	private Button doBlockSegBtn, doLineSegBtn, doWordSegBtn, currPageBtn;
	private LabeledCombo methodCombo;
	private LabeledText customJobImplText;
	
	private boolean doLineSeg, doBlockSeg, doWordSeg;
	private String jobImpl="";
	private String pages;
	
	public static final String METHOD_NCSR_OLD = "NCSR (Old)";
	public static final String METHOD_NCSR = "NCSR (New)";
	public static final String METHOD_CVL = "CVL";
	public static final String METHOD_CITLAB = "CITlab";
	public static final String METHOD_CUSTOM = "Custom";

	public LayoutAnalysisDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	private void updateGui() {
		String method = getSelectedMethod();
				
		doBlockSegBtn.setEnabled(true);
		doLineSegBtn.setEnabled(true);
		doWordSegBtn.setEnabled(true);
				
		if (method.equals(METHOD_NCSR)) {
			doBlockSegBtn.setEnabled(false);
		} else if (method.equals(METHOD_CITLAB) || method.equals(METHOD_NCSR_OLD)) {
			doWordSegBtn.setEnabled(false);		
		}
		
		customJobImplText.setVisible(method.equals(METHOD_CUSTOM));
		
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite mainContainer = (Composite) super.createDialogArea(parent);
		GridLayout g = (GridLayout)mainContainer.getLayout();
		g.numColumns = 1;
		g.makeColumnsEqualWidth = false;
				
		methodCombo = new LabeledCombo(mainContainer, "Method: ");
		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		methodCombo.combo.set
		methodCombo.combo.setItems(getMethods().toArray(new String[0]));
		methodCombo.combo.select(0);

		Group checkGrp = new Group(mainContainer,SWT.NONE);
		checkGrp.setLayout(new GridLayout(1, false));
		checkGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 2));
		
		doBlockSegBtn = new Button(checkGrp, SWT.CHECK);
		doBlockSegBtn.setText("Find Text Regions");
//		Label dbsLbl = new Label(checkGrp, SWT.FLAT);
//		dbsLbl.setText("Find Text Regions");
		
		doLineSegBtn = new Button(checkGrp, SWT.CHECK);
		doLineSegBtn.setText("Find Lines in Text Regions");
		
//		Label dlsLbl = new Label(checkGrp, SWT.FLAT);
//		dlsLbl.setText("Find Lines in Text Regions");
		
		doWordSegBtn = new Button(checkGrp, SWT.CHECK);
		doWordSegBtn.setText("Find Words in Lines (experimental!)");
		
//		Label dlsWbl = new Label(checkGrp, SWT.FLAT);
//		dlsWbl.setText("Find Words in Lines (experimental!)");

		if (TEST) {
			List<TrpPage> pages = new ArrayList<>();
			dps = new DocPagesSelector(mainContainer, SWT.NONE, pages);
		} else {
			dps = new DocPagesSelector(mainContainer, SWT.NONE, store.getDoc().getPages());
		}
		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		currPageBtn = new Button(mainContainer, SWT.PUSH);
		currPageBtn.setText("Current Page");
		
		customJobImplText = new LabeledText(mainContainer, "Custom jobImpl: ");
		customJobImplText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		setPageSelectionToCurrentPage();
		
		addListener();
		
		updateGui();
		
		return mainContainer;
	}
	
	private List<String> getMethods() {
		List<String> methods = new ArrayList<>();
		
		methods.add(METHOD_NCSR_OLD);
		methods.add(METHOD_NCSR);
		methods.add(METHOD_CVL);
		methods.add(METHOD_CITLAB);
		methods.add(METHOD_CUSTOM);
		
		if (!TEST && !store.getUser().isAdmin()) {
			methods.remove(METHOD_CITLAB);
			methods.remove(METHOD_CUSTOM);
		}
		
		return methods;
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
	
	
	
	private void addListener() {	
		currPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e){
				setPageSelectionToCurrentPage();
			}
		});
		
		methodCombo.combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				logger.trace("method changed: "+getSelectedMethod());
				updateGui();
			}
		});
	}
	
	private void setPageSelectionToCurrentPage(){
		if (TEST) {
			dps.getPagesText().setText("NA");
		} else {
			dps.getPagesText().setText(""+store.getPage().getPageNr());	
		}
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
	
	private String getSelectedMethod() {
		return methodCombo.combo.getItems()[methodCombo.combo.getSelectionIndex()];
	}
	
	private void setJobImpl() {
		String selectedMethod = getSelectedMethod();
		if (selectedMethod.equals(METHOD_NCSR_OLD)) {
			jobImpl = JobImpl.NcsrOldLaJob.toString();
		}
		if (selectedMethod.equals(METHOD_NCSR)) {
			jobImpl = JobImpl.NcsrLaJob.toString();
		}
		else if (selectedMethod.equals(METHOD_CVL)) {
			jobImpl = JobImpl.CvlLaJob.toString();
		}
		else if (selectedMethod.equals(METHOD_CITLAB)) {
			jobImpl = JobImpl.CITlabLaJob.toString();
		}
		else if (selectedMethod.equals(METHOD_CUSTOM)) {
			jobImpl = customJobImplText.getText();
			
		}
		
	}

	@Override protected void okPressed() {
		
		setJobImpl();
		doLineSeg = doLineSegBtn.isEnabled() ? doLineSegBtn.getSelection() : false;
		doBlockSeg = doBlockSegBtn.isEnabled() ? doBlockSegBtn.getSelection() : false;
		doWordSeg = doWordSegBtn.isEnabled() ? doWordSegBtn.getSelection() : false;
		
		pages = getSelectedPages();
		
		boolean userMessedUpOnPageSelection;
		try{
			userMessedUpOnPageSelection = dps.getSelectedPageIndices().isEmpty();
		} catch(IOException e){
			userMessedUpOnPageSelection = true;
		}
		
		if(userMessedUpOnPageSelection) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select pages to be analyzed.");
		} else if (!doLineSeg && !doBlockSeg && !doWordSeg) {
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
	
	public boolean isDoWordSeg() {
		return doWordSeg;
	}
	
	public String getPages(){
		return pages;
	}
	
	public String getJobImpl() {
		return jobImpl;
	}
}
