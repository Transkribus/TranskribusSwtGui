package eu.transkribus.swt_gui.dialogs;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpP2PaLA;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.rest.P2PaLATrainJobPars;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.P2PaLATrainDialog.P2PaLATrainUiConf;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrDocPagesOrCollectionSelector;

public class P2PaLAConfDialog extends Dialog {
	public static class P2PaLARecogUiConf {
		public boolean currentTranscript=true;
		public String pagesStr=null;
		public TrpP2PaLA model;
	}
	
	public static class P2PaLAModelFilterComposite extends Composite {
		public static interface P2PaLAModelFilterChangedListener {
			public void onFilterChanged();
		}
		
		public Label label;
		public Button collBasedRadio, userBasedRadio, showPublicRadio, showAllRadio, reloadModelsBtn;
		
		Storage store = Storage.getInstance();
		List<P2PaLAModelFilterChangedListener> listener = new ArrayList<>(); 

		public P2PaLAModelFilterComposite(Composite parent) {
			super(parent, 0);
			
			this.setLayout(new GridLayout(6, false));
			this.setLayout(SWTUtil.createGridLayout(6, false, 0, 0));
			
			label = new Label(this, 0);
			label.setText("Model filter: ");
			Fonts.setBoldFont(label);
			
			collBasedRadio = new Button(this, SWT.RADIO);
			collBasedRadio.setText("Collection");
			collBasedRadio.setToolTipText("Show only models of the current colllection");
			collBasedRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			collBasedRadio.setSelection(true);
			
			userBasedRadio = new Button(this, SWT.RADIO);
			userBasedRadio.setText("User");
			userBasedRadio.setToolTipText("Show only models that were trained by you");
			userBasedRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			
			showPublicRadio = new Button(this, SWT.RADIO);
			showPublicRadio.setText("Public models");
			showPublicRadio.setToolTipText("Show only models that are publicly available");
			showPublicRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			
			showAllRadio = new Button(this, SWT.RADIO);
			showAllRadio.setText("All");
			showPublicRadio.setToolTipText("Show all models (only for admins)");
			showAllRadio.setVisible(store.isAdminLoggedIn());
			showAllRadio.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
			if (store.isAdminLoggedIn()) {
				collBasedRadio.setSelection(false);
				showAllRadio.setSelection(true);
			}			
			
			reloadModelsBtn = new Button(this, SWT.PUSH);
			reloadModelsBtn.setImage(Images.REFRESH);
			reloadModelsBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			reloadModelsBtn.setToolTipText("Reload models according to filter");
			
			SWTUtil.onSelectionEvent(reloadModelsBtn, e -> onFilterChanged());
			SWTUtil.onSelectionEvent(collBasedRadio, e -> onFilterChanged());
			SWTUtil.onSelectionEvent(userBasedRadio, e -> onFilterChanged());
			SWTUtil.onSelectionEvent(showPublicRadio, e -> onFilterChanged());
			SWTUtil.onSelectionEvent(showAllRadio, e -> onFilterChanged());
		}
		
		public void addListener(P2PaLAModelFilterChangedListener l) {
			this.listener.add(l);
		}
		
		public void onFilterChanged() {
			for (P2PaLAModelFilterChangedListener l : listener) {
				l.onFilterChanged();
			}
		}
		
		public List<TrpP2PaLA> loadModelsForCurrentFilter() {
				boolean showAll = showAllRadio.getSelection();
				Integer colId = collBasedRadio.getSelection() ? store.getCollId() : null;
				Integer userId = userBasedRadio.getSelection() ? store.getUserId() : null;
				Integer releaseLevel = showPublicRadio.getSelection() ? 1 : null;
				try {
					List<TrpP2PaLA> models = store.getConnection().getModelCalls().getP2PaLAModels(true, showAll, colId, userId, releaseLevel);
					logger.debug("loaded "+models.size()+" models");
					return models;
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
					DialogUtil.showErrorMessageBox(getShell(), "Error loading models", e1.getMessage());
					return new ArrayList<>();
				}
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(P2PaLAConfDialog.class);
//	MyTableViewer modelsTable;
	CurrentTranscriptOrDocPagesOrCollectionSelector pagesSelector;
	private boolean docsSelected = false;
	public boolean isDocsSelected() {
		return docsSelected;
	}

	public List<DocumentSelectionDescriptor> getSelectedDocDescriptors() {
		return selectedDocDescriptors;
	}

	private List<DocumentSelectionDescriptor> selectedDocDescriptors;
//	Combo modelCombo;
	ComboViewer modelComboViewer;
	AutoCompleteField modelsAutocomplete;
	Label selectedModelLbl;
	
	Button modelDetailsBtn;
	P2PaLAModelFilterComposite modelFilterComp;
	
	P2PaLARecogUiConf conf = null;
	
	public static String NAME_COL = "Name";
	public static String DESC_COL = "Description";
	public static String BASELINES_COL = "Baselines";
	public static String STRUCT_TYPES_COL = "Structure Types";
	
	public static String TRAIN_SET_SIZE_COL = "N-Train";
	public static String VAL_SET_SIZE_COL = "N-Validation";
	public static String TEST_SET_SIZE_COL = "N-Test";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(NAME_COL, 120, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESC_COL, 250, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(BASELINES_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "Does this model detect Baselines?"),
			new ColumnConfig(STRUCT_TYPES_COL, 750, false, DefaultTableColumnViewerSorter.ASC, "The region structure types this model detects"),
			new ColumnConfig(TRAIN_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the training set"),
			new ColumnConfig(VAL_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the validation set (which is used after every epoch during training to evaluate the model)"),
			new ColumnConfig(TEST_SET_SIZE_COL, 75, false, DefaultTableColumnViewerSorter.ASC, "The size of the test set (which is used once after training to evaluate the model)"),
		};	
	
//	List<TrpP2PaLAModel> models;
	List<TrpP2PaLA> models = new ArrayList<>();
	Storage store;
	
	public P2PaLAConfDialog(Shell parentShell) {
		super(parentShell);
		store = Storage.getInstance();
//		this(parentShell, null);
	}
	
	@Override
	protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE);
	    setBlockOnOpen(false);
	}

//	public P2PaLAConfDialog(Shell parentShell /*, List<TrpP2PaLA> models*/) {
//		super(parentShell);
//		this.models = models;
//	}
	
	@Override
	protected Point getInitialSize() {
//		return new Point(600, 250);
//		return new Point(500, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		return new Point(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).x, getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
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
			org.eclipse.swt.program.Program.launch("https://transkribus.eu/wiki/index.php/P2PaLA");
		});
		
		boolean isUserAllowedForP2PaLATraining = false;
		try {
			isUserAllowedForP2PaLATraining = store.getConnection().isUserAllowedForJob(JobImpl.P2PaLATrainJob.toString());
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
			isUserAllowedForP2PaLATraining=false;
			logger.error("Error determining if user is allowed for "+JobImpl.P2PaLATrainJob.toString()+": "+e.getMessage(), e);
		}
		
		if (store.isAdminLoggedIn() || isUserAllowedForP2PaLATraining) {
			Button trainBtn = createButton(parent, IDialogConstants.CLIENT_ID, "Train", false);
			trainBtn.setImage(Images.TRAIN);
			SWTUtil.onSelectionEvent(trainBtn, e -> {
				TrpMainWidget mw = TrpMainWidget.i();
				
				String jobId;
				try {
					jobId = trainP2PaLAModel();
					if (jobId != null && mw!=null) {
						mw.registerJobStatusUpdateAndShowSuccessMessage(jobId);
					}					
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
					if (mw != null) {
						mw.onError("Error training P2PaLA model", e1.getMessage(), e1);	
					} else {
						logger.error(e1.getMessage(), e1);
					}
				}
			});
		}
		
		Button modelDetailsBtn = createButton(parent, IDialogConstants.DETAILS_ID, "Model info", false);
		modelDetailsBtn.setImage(Images.INFO);
		SWTUtil.onSelectionEvent(modelDetailsBtn, e -> {
			P2PaLAModelDetailsDialog d = new P2PaLAModelDetailsDialog(getShell(), models);
			d.open();
		});		
		
	    Button runBtn = createButton(parent, IDialogConstants.OK_ID, "Run", false);
	    runBtn.setImage(Images.ARROW_RIGHT);
	    
	    onSelectedModelChanged();
	}
	
	public String trainP2PaLAModel() throws SessionExpiredException, ServerErrorException, ClientErrorException {
//		try {
			logger.debug("p2palaTrainBtn pressed...");
			if (!store.getConnection().isUserAllowedForJob(JobImpl.P2PaLATrainJob.toString())) {
				DialogUtil.showErrorMessageBox(getShell(), "Not allowed!", "You are not allowed to start a P2PaLA training.\n If you are interested, please apply at email@transkribus.eu");
				return null;
			}
			P2PaLATrainDialog d = new P2PaLATrainDialog(getShell());
			if (d.open() == IDialogConstants.OK_ID) {
				P2PaLATrainUiConf conf = d.getConf();
				if (conf==null) {
					return null;
				}
				P2PaLATrainJobPars jobPars = conf.toP2PaLATrainJobPars();
				String jobId = store.getConnection().trainP2PaLAModel(store.getCollId(), jobPars);
				logger.info("Started P2PaLA training job "+jobId);
				return jobId;
//				mw.registerJobStatusUpdateAndShowSuccessMessage(jobId);
				
			}
			return null;
//		} catch (Exception e) {
//			mw.onError("Error starting P2PaLA training", e.getMessage(), e);
//		}
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("P2PaLA structure analysis tool");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		cont.setLayout(new GridLayout(2, false));
		
//		Link infoText = new Link(cont, 0);
//		infoText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
//		String githubLink="https://github.com/lquirosd/P2PaLA";
//		infoText.setText("This tool detects regions including its structure types and (optionally) baselines, see <a href=\""+githubLink+"\">"+githubLink+"</a>");
//		SWTUtil.onSelectionEvent(infoText, e -> {
//			try {
//				org.eclipse.swt.program.Program.launch(e.text);
//			} catch (Exception ex) {
//				logger.error(ex.getMessage(), ex);
//			}
//		});
//		Fonts.setBoldFont(infoText);
		
		pagesSelector = new CurrentTranscriptOrDocPagesOrCollectionSelector(cont, SWT.NONE, false,true);
		pagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label recogModelLabel = new Label(cont, 0);
		recogModelLabel.setText("Select a model for recognition: ");
		Fonts.setBoldFont(recogModelLabel);
		
//		modelCombo = new Combo(cont, SWT.READ_ONLY | SWT.DROP_DOWN);
//		modelCombo = new Combo(cont, SWT.DROP_DOWN);
//		modelCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		modelCombo.setToolTipText("The model used for the P2PaLA Layout Analysis");

//		Text textField = new Text(cont, SWT.BORDER);
//		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		modelsAutocomplete = new AutoCompleteField(textField, new TextContentAdapter(), new String[] {});
		
		modelComboViewer = new ComboViewer(cont, SWT.DROP_DOWN);
		modelComboViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		modelComboViewer.getCombo().setToolTipText("The model used for the P2PaLA Layout Analysis");
		modelComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		modelComboViewer.setLabelProvider(new LabelProvider() {
	        @Override
	        public String getText(Object element) {
	            if (element instanceof TrpP2PaLA) {
	            	TrpP2PaLA model = (TrpP2PaLA) element;
	            	String lbl = model.getName();
	            	if (model.getCreated()!=null) {
	            		lbl += " - "+model.getCreated();
	            	}
	            	
	            	return lbl;
	            }
	            return "<i am error>";
	        }
	    });
		modelsAutocomplete = new AutoCompleteField(modelComboViewer.getCombo(), new ComboContentAdapter(), new String[] {});
		
		selectedModelLbl = new Label(cont, SWT.NONE);
		selectedModelLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Fonts.setItalicFont(selectedModelLbl);
		
		modelComboViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				onSelectedModelChanged();
			}
		});
		
		SWTUtil.onSelectionEvent(modelComboViewer.getCombo(), e -> {
			onSelectedModelChanged();
		});
		modelComboViewer.getCombo().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onSelectedModelChanged();
			}
		});
		modelComboViewer.getCombo().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				logger.trace("key released, text = "+modelComboViewer.getCombo().getText());
				onSelectedModelChanged();
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		
		initModelFacetsCombo(cont);
		
		reloadModels();
		
//		setModels(models);
		onSelectedModelChanged();
		
		return cont;
	}
	
	private void initModelFacetsCombo(Composite parent) {
		modelFilterComp = new P2PaLAModelFilterComposite(parent);
		modelFilterComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		modelFilterComp.addListener(() -> reloadModels());
	}
	
	private void reloadModels() {
		this.models = modelFilterComp.loadModelsForCurrentFilter();
		setModels(models);
		
//		boolean showAll = moderFilterComp.showAllRadio.getSelection();
//		Integer colId = moderFilterComp.collBasedRadio.getSelection() ? store.getCollId() : null;
//		Integer userId = moderFilterComp.userBasedRadio.getSelection() ? store.getUserId() : null;
//		Integer releaseLevel = moderFilterComp.showPublicRadio.getSelection() ? 1 : null;
//		try {
//			this.models = store.getConnection().getModelCalls().getP2PaLAModels(true, showAll, colId, userId, releaseLevel);
//			logger.debug("loaded "+models.size()+" models");
//		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
//			DialogUtil.showErrorMessageBox(getShell(), "Error loading models", e1.getMessage());
//			this.models = new ArrayList<>();
//		}	
//		
//		setModels(models);
	}
	
	private void onSelectedModelChanged() {
		TrpP2PaLA m = getSelectedP2PaLAModel();
		
		if (m == null) { // try to select model from text in combo
			m = models.stream().filter(m1 -> m1.getName().equals(modelComboViewer.getCombo().getText())).findFirst().orElse(null);
			if (m!=null) {
				modelComboViewer.setSelection(new StructuredSelection(m));
				m = getSelectedP2PaLAModel();
			}
		}
		
		if (selectedModelLbl!=null) {
			selectedModelLbl.setText(m==null ? "Selected: <none>" : "Selected: "+m.getName());
		}
		
		Button b = getButton(IDialogConstants.OK_ID);
		if (b!=null) {
			b.setEnabled(m!=null);	
		}
	}
		
//	public void setModels(List<TrpP2PaLAModel> models) {
//		logger.debug("setting input models, N = "+CoreUtils.size(models));
//		
//		if (models != null && !models.isEmpty()) { // null check needed???
//			modelsTable.setInput(models);
//			
//			List<String> items = new ArrayList<>();
//			int i=0;
//			for (TrpP2PaLAModel m : models) {
//				items.add(m.getName());
//				modelCombo.setData(""+i, m);
//				++i;
//			}
//			modelCombo.setItems(items.toArray(new String[0]));
//			modelCombo.select(0);			
//		}
//		else {
//			modelsTable.setInput(new ArrayList<>());
//			modelCombo.setItems(new String[] {});
//		}
//	}
	
	public void setModels(List<TrpP2PaLA> models) {
		if (models==null) {
			models = new ArrayList<>();
		}
		
		models = new ArrayList<>(models);
		Collections.sort(models, new Comparator<TrpP2PaLA>() {
			@Override
			public int compare(TrpP2PaLA o1, TrpP2PaLA o2) {
				return CoreUtils.compareTo(o1.getCreated(), o2.getCreated());
				
//				String n1 = o1.getName()==null ? "" : o1.getName();
//				String n2 = o2.getName()==null ? "" : o2.getName();
//				return n1.compareTo(n2);
			}
		});
		
		logger.debug("setting input models, N = "+CoreUtils.size(models));
		
		if (models != null && !models.isEmpty()) { // null check needed???
//			modelsTable.setInput(models);
			modelComboViewer.setInput(models);
			
			List<String> items = new ArrayList<>();
			for (TrpP2PaLA m : models) {
				items.add(m.getName());
			}
			modelsAutocomplete.setProposals(items.toArray(new String[0]));
			modelComboViewer.getCombo().select(0);
		}
		else {
//			modelsTable.setInput(new ArrayList<>());
			modelComboViewer.setInput(new ArrayList<>());
		}
	}	
	
//	public TrpP2PaLAModel getSelectedP2PaLAModel() {
//		int i = modelCombo.getSelectionIndex();
//		if (i>=0 && i<modelCombo.getItemCount()) {
//			try {
//				return (TrpP2PaLAModel) modelCombo.getData(""+i);
//			} catch (Exception e) {
//				logger.error("Error casting selected P2PaLAModel: "+e.getMessage(), e);
//			}
//		}
//		return null;
//	}	
	
	public TrpP2PaLA getSelectedP2PaLAModel() {
		return (TrpP2PaLA) modelComboViewer.getStructuredSelection().getFirstElement();
//		IStructuredSelection sel = modelComboViewer.getStructuredSelection();
//		return sel.isEmpty() ? null : (TrpP2PaLAModel) sel.getFirstElement();
	}	
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	private void storeConf() {
		TrpP2PaLA model = getSelectedP2PaLAModel();
		if (model != null) {
			conf = new P2PaLARecogUiConf();
			conf.currentTranscript = pagesSelector.isCurrentTranscript();
			conf.pagesStr = pagesSelector.getPagesStr();
			conf.model = model;
		}
		else {
			conf = null;
		}
	}
	
	public P2PaLARecogUiConf getConf() {
		return conf;
	}
	
	public List<DocumentSelectionDescriptor> getDocs(){
		return selectedDocDescriptors;
	}
	
	@Override
	protected void okPressed() {
		storeConf();
		if(pagesSelector.isDocsSelection()){

			docsSelected = pagesSelector.isDocsSelection();
			selectedDocDescriptors = pagesSelector.getDocumentsSelected();
			
		}
		super.okPressed();
	}

}
