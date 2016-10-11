package eu.transkribus.swt_gui.dialogs;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.HtrModel;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.doc_overview.DocMetadataEditor;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.tools.HtrModelTableLabelProvider;
import eu.transkribus.swt_gui.upload.UploadFromFtpDialog;
import eu.transkribus.swt_gui.util.DocPagesSelector;

public class TextRecognitionDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(TextRecognitionDialog.class);

	private Table htrModelTable;
	private MyTableViewer htrModelTv;
	private Storage store = Storage.getInstance();
	private TrpMainWidget mw = TrpMainWidget.getInstance();
	private DocPagesSelector dps;
	private DocMetadataEditor dme;
	private Composite ocrContainer;
	
	private CTabFolder tabFolder;
	private CTabItem ocrItem, htrItem;
	private CTabFolder htrTabFolder;
	private CTabItem hmmItem, rnnItem;
	
	Combo netCombo, dictCombo;
	List<String> htrNets, htrDicts;
	
	private Button currPageBtn;
	
	private static List<HtrModel> htrModels;
	private static HtrModel selHtrModel = null;
	private static RecMode selRecMode = null;
	private static String selPagesStr = null;

	private static HtrRecMode selHtrMode = null;
	private static String selRnn = null;
	private static String selDict = null;
	
	public static final String ID_COL = "ID";
	public static final String MODEL_NAME_COL = "Model Name";
	public static final String LABEL_COL = "Label";
	public static final String LANG_COL = "Language";
	public static final String NR_OF_TOKENS_COL = "Nr. of tokens";
	public static final String NR_OF_LINES_COL = "Nr. of lines";
	public static final String NR_OF_DICT_TOKENS_COL = "Nr. of tokens in dict.";
		
	public static final ColumnConfig[] MODEL_COLS = new ColumnConfig[] {
		new ColumnConfig(ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(MODEL_NAME_COL, 180, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(LABEL_COL, 180, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(LANG_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(NR_OF_TOKENS_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(NR_OF_LINES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(NR_OF_DICT_TOKENS_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public TextRecognitionDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite mainContainer = (Composite) super.createDialogArea(parent);
		GridLayout g = (GridLayout)mainContainer.getLayout();
		g.numColumns = 2;
		
		dme = new DocMetadataEditor(mainContainer, SWT.NONE);
		dme.setMetadataToGui(store.getDoc().getMd());
		
		Composite container = (Composite) super.createDialogArea(mainContainer);
		GridLayout gridLayout = (GridLayout)container.getLayout();
		gridLayout.numColumns = 3;
		
		tabFolder = new CTabFolder(container, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		htrTabFolder = new CTabFolder(tabFolder, SWT.BORDER | SWT.FLAT);
		htrTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		Composite hmmContainer = new Composite(htrTabFolder, SWT.NONE);
		hmmContainer.setLayout(new GridLayout(4, false));
		
		htrModelTv = new MyTableViewer(hmmContainer, SWT.FULL_SELECTION);
		htrModelTv.setContentProvider(new ArrayContentProvider());
		htrModelTv.setLabelProvider(new HtrModelTableLabelProvider(htrModelTv));
		
		htrModelTable = htrModelTv.getTable();
		htrModelTable.setHeaderVisible(true);
		htrModelTable.setLinesVisible(true);
		htrModelTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		htrModelTv.addColumns(MODEL_COLS);
		
		hmmItem = createCTabItem(htrTabFolder, hmmContainer, "Hidden Markov Models");
		
		Composite rnnContainer = new Composite(htrTabFolder, SWT.NONE);
		rnnContainer.setLayout(new GridLayout(2, false));
		
		Label netLbl = new Label(rnnContainer, SWT.NONE);
		netLbl.setText("Network:");
		netCombo = new Combo(rnnContainer, SWT.READ_ONLY);
		netCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		netCombo.setItems(new String[]{""});
		
		Label dictLbl = new Label(rnnContainer, SWT.NONE);
		dictLbl.setText("Dictionary:");
		dictCombo = new Combo(rnnContainer, SWT.READ_ONLY);
		dictCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		dictCombo.setItems(new String[]{""});
		
		if(Storage.getInstance().isAdminLoggedIn() || true) {
			rnnItem = createCTabItem(htrTabFolder, rnnContainer, "Recurrent Neural Networks (Beta)");
		}
		
		htrTabFolder.setSelection(hmmItem);
		htrItem = createCTabItem(tabFolder, htrTabFolder, "HTR");
		
		ocrContainer = new Composite(tabFolder, SWT.NONE);
		ocrContainer.setLayout(new GridLayout(1, false));
		updateOcrInfo(ocrContainer);

		ocrItem = createCTabItem(tabFolder, ocrContainer, "OCR");
		
		dps = new DocPagesSelector(container, SWT.NONE, store.getDoc().getPages());
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		currPageBtn = new Button(container, SWT.PUSH);
		currPageBtn.setText("Current Page");
		
		setPageSelectionToCurrentPage();
		updateHtrModels();
//		if(Storage.getInstance().getServerUri().contains("Testing")) {
		updateHtrNetsAndDicts();
//		}
		updateTabSelection();
		addListener();
		return mainContainer;
	}
		
	private void updateOcrInfo(Composite ocrContainer) {
		for(Control c : ocrContainer.getChildren()){
			c.dispose();
		}
		
		final String langStr = getLangStr();
		final String scriptStr = getScriptStr();
		final Color red = ocrContainer.getDisplay().getSystemColor(SWT.COLOR_RED);
		
		Label ocrEngine = new Label(ocrContainer, SWT.NONE);
		ocrEngine.setText("Abbyy Finereader 11");
		Font fat = Fonts.createFont(Fonts.getSystemFontName(false, true, false), 12, SWT.BOLD);
		ocrEngine.setFont(fat);
		
		Label ocrLang = new Label(ocrContainer, SWT.NONE);
		ocrLang.setText("Language: " + langStr);
		if(langStr == null || langStr.isEmpty()){
			ocrLang.setForeground(red);
		}
		
		Label ocrScript = new Label(ocrContainer, SWT.NONE);
		ocrScript.setText("Selected script type: " + scriptStr);
		if(scriptStr == null || scriptStr.isEmpty()){
			ocrScript.setForeground(red);
		}
		
		ocrContainer.pack();
		ocrContainer.layout();
	}

	private String getLangStr() {
		return dme.getLangTable().getSelectedLanguagesString();
	}

	private String getScriptStr() {
		return dme.getScriptTypeCombo2().getText();
	}
	
	private boolean isOcrMdComplete(){
		return getLangStr() != null && !getLangStr().isEmpty() 
				&& getScriptStr() != null && ! getScriptStr().isEmpty();
	}
	
	private void addListener() {
		
		dme.getScriptTypeCombo().addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateTabSelection();
				updateOcrInfo(ocrContainer);
			}
		});
		
		htrModelTv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				selHtrModel = getSelectedHtrModelFromTable();
				updateBtnVisibility();
			}
		});
		
		dme.getScriptTypeCombo2().addSelectionListener(new SelectionAdapter(){
			@Override public void widgetSelected(SelectionEvent e){
				updateOcrInfo(ocrContainer);
				updateBtnVisibility();
			}
		});
		
		dme.getLangTable().addCheckStateListener(new ICheckStateListener(){
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateOcrInfo(ocrContainer);
				updateBtnVisibility();
			}
		});
		
		currPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e){
				setPageSelectionToCurrentPage();
			}
		});
		
		tabFolder.addSelectionListener(new SelectionAdapter(){
			@Override public void widgetSelected(SelectionEvent e){
				updateBtnVisibility();
			}
		});
		htrTabFolder.addSelectionListener(new SelectionAdapter(){
			@Override public void widgetSelected(SelectionEvent e){
				updateBtnVisibility();
			}
		});
	}
	
	private void setPageSelectionToCurrentPage(){
		dps.getPagesText().setText(""+store.getPage().getPageNr());
	}
	
	@Override protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		
		updateBtnVisibility();
		return ctrl;
	}
	
	private void updateBtnVisibility() {
		if (getButton(IDialogConstants.OK_ID) != null){
			if(tabFolder.getSelection().equals(ocrItem)){
				getButton(IDialogConstants.OK_ID).setEnabled(isOcrMdComplete());
			} else if (tabFolder.getSelection().equals(htrItem)){
				if(htrTabFolder.getSelection().equals(hmmItem)) {
					getButton(IDialogConstants.OK_ID).setEnabled(getSelectedHtrModelFromTable() != null);
				} else if(htrTabFolder.getSelection().equals(rnnItem)) {
					getButton(IDialogConstants.OK_ID).setEnabled(getSelectedRnn() != null && getSelectedDict() != null);
				}
			}
		}
	}
	
	private void updateHtrModels(){
		try {
			htrModels = store.getHtrModels();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			mw.onError("Error", "Could not load HTR model list!", e);
		}
		htrModelTv.setInput(htrModels);
	}
	
	private void updateHtrNetsAndDicts(){
		try {
			this.htrNets = store.getHtrNets();
			this.htrDicts = store.getHtrDicts();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			mw.onError("Error", "Could not load HTR model list!", e);
		}
		netCombo.setItems(this.htrNets.toArray(new String[this.htrNets.size()]));
		netCombo.select(0);
		dictCombo.setItems(this.htrDicts.toArray(new String[this.htrDicts.size()]));
		dictCombo.select(0);
	}

	private String getSelectedRnn() {
		String rnn = null;
		if(netCombo.getSelectionIndex() >= 0){
			rnn = netCombo.getItem(netCombo.getSelectionIndex());
		}
		return rnn;
	}

	private String getSelectedDict() {
		String dict = null;
		if(dictCombo.getSelectionIndex() >= 0){
			dict = dictCombo.getItem(dictCombo.getSelectionIndex());
		}
		return dict;
	}
	
	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text Recognition");
	}

	@Override protected Point getInitialSize() {
		return new Point(1400, 750);
	}

	// override method to use "Login" as label for the OK button
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Run", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		selHtrModel = getSelectedHtrModelFromTable();
		selRecMode = tabFolder.getSelection().equals(htrItem) ? RecMode.HTR : RecMode.OCR;
		selPagesStr = dps.getPagesText().getText();
		
		selHtrMode = htrTabFolder.getSelection().equals(rnnItem) ? HtrRecMode.RNN : HtrRecMode.HMM;
		selRnn = getSelectedRnn();
		selDict = getSelectedDict();
		
		dme.updateMetadataObjectFromGui(store.getDoc().getMd());
		mw.saveDocMetadata();
		if(selPagesStr == null || selPagesStr.isEmpty()) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select pages to be recognized.");
		} else if(tabFolder.getSelection().equals(htrItem) && selHtrMode.equals(HtrRecMode.HMM) 
				&& selHtrModel == null) {
				DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select an HTR model.");
		} else if(tabFolder.getSelection().equals(htrItem) && selHtrMode.equals(HtrRecMode.RNN) 
				&& (selRnn == null || selDict == null)){
				DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select a neural network and a dictionary.");
		} else if(tabFolder.getSelection().equals(ocrItem) && !isOcrMdComplete()){
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "Please select a script type and language.");
		} else {
			super.okPressed();
		}
	}
			
	private HtrModel getSelectedHtrModelFromTable(){
		IStructuredSelection sel = (IStructuredSelection) htrModelTv.getSelection();
		HtrModel model = null;
		Iterator<Object> it = sel.iterator();
		while(it.hasNext()){
			model = (HtrModel)it.next();
			logger.debug("Selected model: " + model.getModelName());
			break;
		}
		return model;	
	}
	
	public HtrModel getSelectedHtrModel(){
		return selHtrModel;
	}
	
	public RecMode getRecMode(){
		return selRecMode;
	}
	
	public String getSelectedPages() {
		return selPagesStr;
	}
	
	public HtrRecMode getHtrRecMode(){
		return selHtrMode;
	}
	
	public String getRnnName(){
		return selRnn;
	}

	public String getDictName(){
		return selDict;
	}
	
	private void updateTabSelection(){
		if(DocMetadataEditor.PRINT_META_SCRIPTTYPE.equals(dme.getScriptTypeCombo().getText())){
			tabFolder.setSelection(ocrItem);
		} else {
			tabFolder.setSelection(htrItem);
		}
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(300, 200);

				Button btn = new Button(parent, SWT.PUSH);
				btn.setText("Open HTR dialog");
				btn.addSelectionListener(new SelectionListener() {

					@Override public void widgetSelected(SelectionEvent e) {
						(new UploadFromFtpDialog(getShell(), null)).open();
					}

					@Override public void widgetDefaultSelected(SelectionEvent e) {
					}
				});

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}
	
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
		return ti;
	}
	
	public enum RecMode {
		OCR, HTR;
	}
	
	public enum HtrRecMode {
		RNN, HMM;
	}
}
