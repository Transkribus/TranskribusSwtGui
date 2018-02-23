package eu.transkribus.swt_gui.la;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.swt.util.LabeledComboWithButton;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.dialogs.ALaConfigDialog;
import eu.transkribus.swt_gui.dialogs.CITlabAdvancedLaConfigDialog;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class LayoutAnalysisComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LayoutAnalysisComposite.class);
	
	public static boolean TEST = false;
	
	static private Storage store = TEST ? null : Storage.getInstance();
//	private DocPagesSelector dps;
	private CurrentTranscriptOrCurrentDocPagesSelector dps;
	private Button doBlockSegBtn, doLineSegBtn, doWordSegBtn;
	private LabeledComboWithButton methodCombo;
//	private LabeledText customJobImplText;
		
	public static final String METHOD_NCSR_OLD = "NCSR";
	public static final String METHOD_NCSR = "NCSR New (experimental)";
	public static final String METHOD_CVL = "CVL (experimental)";
	public static final String METHOD_CITLAB = "CITlab";
	public static final String METHOD_CITLAB_ADVANCED = "CITlab Advanced";
	
//	public static final String METHOD_CUSTOM = "Custom";
	
	public static final String[] LA_CITLAB_ALLOWED_USERS = { };
	
	private ParameterMap paramMap;
	private ALaConfigDialog configDialog = null;

	public LayoutAnalysisComposite(Composite parent, int style) {
		super(parent, style);
		
		Composite mainContainer = (Composite) this;
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);
		
//		GridLayout g = (GridLayout)mainContainer.getLayout();
//		g.numColumns = 1;
//		g.makeColumnsEqualWidth = false;
				
		methodCombo = new LabeledComboWithButton(mainContainer, "Method: ", "Configure...");
		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		updateMethods();
//		methodCombo.combo.setItems(getMethods(true).toArray(new String[0]));
//		methodCombo.combo.select(0);
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(mainContainer, SWT.NONE, true);		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

//		Group checkGrp = new Group(mainContainer,SWT.NONE);
//		checkGrp.setLayout(new GridLayout(1, false));
//		checkGrp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 2));
		
		doBlockSegBtn = new Button(mainContainer, SWT.CHECK);
		doBlockSegBtn.setText("Find Text Regions");
		doBlockSegBtn.setSelection(true);

		doLineSegBtn = new Button(mainContainer, SWT.CHECK);
		doLineSegBtn.setText("Find Lines in Text Regions");
		doLineSegBtn.setSelection(true);

		doWordSegBtn = new Button(mainContainer, SWT.CHECK);
		doWordSegBtn.setText("Find Words in Lines (experimental!)");

//		customJobImplText = new LabeledText(mainContainer, "Custom jobImpl: ");
//		customJobImplText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
		addListener();
		
		updateGui();
	}
	
	private void updateMethods() {
		methodCombo.combo.setItems(getMethods(true).toArray(new String[0]));
		methodCombo.combo.select(0);
	}
	
	private void updateGui() {
		String method = getSelectedMethod();
				
		doBlockSegBtn.setEnabled(true);
		doLineSegBtn.setEnabled(true);
		doWordSegBtn.setEnabled(true);
		
		boolean isMethodConfigurable = false;
		switch(method) {
		case METHOD_NCSR:
			doBlockSegBtn.setEnabled(false);
			doBlockSegBtn.setSelection(false);
			break;
		case METHOD_NCSR_OLD:
			doWordSegBtn.setSelection(false);
			doWordSegBtn.setEnabled(false);
			break;
		case METHOD_CITLAB:
//			doBlockSegBtn.setSelection(true);
//			doBlockSegBtn.setEnabled(false);
			doWordSegBtn.setSelection(false);
			doWordSegBtn.setEnabled(false);
			break;
		case METHOD_CVL:
			doBlockSegBtn.setSelection(true);
			doBlockSegBtn.setEnabled(false);
			doWordSegBtn.setSelection(false);
			doWordSegBtn.setEnabled(false);
			break;
		case METHOD_CITLAB_ADVANCED:
			doWordSegBtn.setSelection(false);
			doWordSegBtn.setEnabled(false);
			isMethodConfigurable = true;
			break;
		default:
			return;
		}
		
		//enable config button only if method is configurable (only CITlabAdvanced for now)
		methodCombo.getButton().setEnabled(isMethodConfigurable);
		
//		customJobImplText.setVisible(method.equals(METHOD_CUSTOM));
	}

	public static boolean isUserAllowedCitlab() {
		if (TEST || store.isAdminLoggedIn())
			return true;
		
		try {
			return store.getConnection().isUserAllowedForJob(JobImpl.CITlabLaJob.toString());
		} catch (Exception e) {
			logger.error("Could not determine if user is allowed for CITlabLaJob: "+e.getMessage());
			return false;
		}
	}

	public static List<String> getMethods(boolean withCustom) {
		List<String> methods = new ArrayList<>();
		
		methods.add(METHOD_CITLAB_ADVANCED);
		methods.add(METHOD_NCSR_OLD);
//		methods.add(METHOD_NCSR);
		methods.add(METHOD_CVL);
		methods.add(METHOD_CITLAB);
		
//		if (withCustom) {
//			methods.add(METHOD_CUSTOM);
//		}
		
		if (!isUserAllowedCitlab()) {
			methods.remove(METHOD_CITLAB);
//			methods.remove(METHOD_CUSTOM);
		}
		
		return methods;
	}
	
	private void addListener() {			
		methodCombo.combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				logger.trace("method changed: " + getJobImpl());
				paramMap = TrpGuiPrefs.getLaParameters(getJobImpl());
				updateGui();
				if(configDialog != null) {
					configDialog.close();
					configDialog = null;
				}
			}
		});
		
		methodCombo.getButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				if(configDialog != null) {
					configDialog.setVisible();
				} else {
					JobImpl impl = getJobImpl();
					switch(impl) {
					case CITlabAdvancedLaJob:
						configDialog = new CITlabAdvancedLaConfigDialog(getShell(), paramMap);
						break;
					default:
						return;	
					}
					final int ret = configDialog.open();
					logger.debug("Dialog ret = " + ret);
					if(ret == IDialogConstants.OK_ID && configDialog != null) { //may be null if close() is called programmatically (happens when switching methods)
						paramMap = configDialog.getParameters();
						TrpGuiPrefs.storeLaParameters(impl, paramMap);
					}
					configDialog = null;
				}
			}
		});
		
		if (store != null) {
			store.addListener(new IStorageListener() {
				public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
					updateMethods();
				}
			});
		}
	}
	
	private void setPageSelectionToCurrentPage(){
		if (TEST) {
			dps.getPagesSelector().getPagesText().setText("NA");
		} else {
			dps.getPagesSelector().getPagesText().setText(""+store.getPage().getPageNr());	
		}
	}
	
	public void setPageSelectionToSelectedPages(String pages){
		dps.getPagesSelector().getPagesText().setText(pages);
	}
	
	public Button getDoBlockSegBtn() {
		return doBlockSegBtn;
	}

	public Button getDoLineSegBtn() {
		return doLineSegBtn;
	}
	
	public String getSelectedMethod() {
		if (methodCombo.combo.getSelectionIndex()>=0 && methodCombo.combo.getSelectionIndex()<methodCombo.combo.getItemCount()) {
			return methodCombo.combo.getItems()[methodCombo.combo.getSelectionIndex()];	
		} else {
			return "";
		}
	}
	
	public static JobImpl getJobImplForMethod(String selectedMethod) {
		switch(selectedMethod) {
		case METHOD_NCSR_OLD:
			return JobImpl.NcsrOldLaJob;
		case METHOD_NCSR:
			return JobImpl.NcsrLaJob;
		case METHOD_CVL:
			return JobImpl.CvlLaJob;
		case METHOD_CITLAB:
			return JobImpl.CITlabLaJob;
		case METHOD_CITLAB_ADVANCED:
			return JobImpl.CITlabAdvancedLaJob;
		default:
			return null;
		}
	}
	
	public boolean isDoLineSeg(){
		return doLineSegBtn.getSelection();
	}
	
	public boolean isDoBlockSeg(){
		return doBlockSegBtn.getSelection();
	}
	
	public boolean isDoWordSeg() {
		return doWordSegBtn.getSelection();
	}
	
	public boolean isCurrentTranscript() {
		return dps.isCurrentTranscript();
	}
	
	public String getPages(){
		return dps.getPagesStr();
	}
	
	public ParameterMap getParameters() {
		return paramMap;
	}
	
	public JobImpl getJobImpl() {
		String selectedMethod = getSelectedMethod();
		JobImpl jobImpl = getJobImplForMethod(selectedMethod);
		return jobImpl;
	}

}
