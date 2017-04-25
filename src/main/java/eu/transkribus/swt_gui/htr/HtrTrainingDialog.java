package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrTrainingDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTrainingDialog.class);
	
	private static final Color DARK_BLUE = Colors.getSystemColor(SWT.COLOR_DARK_BLUE);
	private static final Color BLUE = Colors.getSystemColor(SWT.COLOR_BLUE);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color DARK_GREEN = Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
	private static final Color GREEN = Colors.getSystemColor(SWT.COLOR_GREEN);
	private static final Color CYAN = Colors.getSystemColor(SWT.COLOR_DARK_CYAN);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private final int colId;
	
	private CTabFolder paramTabFolder;
	private CTabItem uroTabItem;

	private CTabFolder selectionMethodTabFolder;
	private CTabItem thumbNailTabItem, treeViewerTabItem;
	
	private Button addTrainDocBtn, addTestDocBtn;
	private CTabFolder docTabFolder, testDocTabFolder;
	
	private Button useTrainGtVersionChk, useTestGtVersionChk; 
	
	//keep references of all ThumbnailWidgets for gathering selection results
	private List<ThumbnailWidgetVirtualMinimal> trainTwList, testTwList;
	
	private Text modelNameTxt, descTxt, langTxt, trainSizeTxt;
	private Combo baseModelCmb, noiseCmb;
	private ComboViewer baseModelCmbViewer;
	
	private Text numEpochsTxt, learningRateTxt;
	
	private CitLabHtrTrainConfig conf;
	
	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProvider labelProv;
	private Button useGtVersionChk;
	
	private Composite buttonComp;
	private Label previewLbl;
	
	private Button addToTrainSetBtn, addToTestSetBtn, removeFromTrainSetBtn, removeFromTestSetBtn;
	
	private DataSetTableWidget testSetOverviewTable, trainSetOverviewTable;
	
	private Storage store = Storage.getInstance();
	
	private List<TrpDocMetadata> docList;
	
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;

	private final static String[] NOISE_OPTIONS = new String[] {"no", "preproc", "net", "both"};
	private final static int NOISE_DEFAULT_CHOICE = 3;
	
	private final static int NUM_EPOCHS_DEFAULT = 200;
	private final static String LEARNING_RATE_DEFAULT = "2e-3";
	private final static int TRAIN_SIZE_DEFAULT = 1000;
	
	private final static String TAB_NAME_PREFIX = "Document ";
	
	public HtrTrainingDialog(Shell parent) {
		super(parent);
		trainTwList = new LinkedList<>();
		testTwList = new LinkedList<>();
		docList = store.getDocList();
		colId = store.getCollId();
		trainDocMap = new TreeMap<>();
		testDocMap = new TreeMap<>();
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(4, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Model Name:");
		modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label langLbl = new Label(paramCont, SWT.FLAT);
		langLbl.setText("Language:");
		langTxt = new Text(paramCont, SWT.BORDER);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		descTxt = new Text(paramCont, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 3;
//		gd.horizontalSpan = 3;
		descTxt.setLayoutData(gd);
		
		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		uroTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		uroTabItem.setText("CITlab RNN");

		Composite uroParamCont = new Composite(paramTabFolder, SWT.NONE);
		uroParamCont.setLayout(new GridLayout(4, false));

		paramTabFolder.setSelection(uroTabItem);

		Label numEpochsLbl = new Label(uroParamCont, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		numEpochsTxt = new Text(uroParamCont, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label learningRateLbl = new Label(uroParamCont, SWT.NONE);
		learningRateLbl.setText("Learning Rate:");
		learningRateTxt = new Text(uroParamCont, SWT.BORDER);
		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label noiseLbl = new Label(uroParamCont, SWT.NONE);
		noiseLbl.setText("Noise:");
		noiseCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		noiseCmb.setItems(NOISE_OPTIONS);
		
		Label trainSizeLbl = new Label(uroParamCont, SWT.NONE);
		trainSizeLbl.setText("Train Size per Epoch:");
		trainSizeTxt = new Text(uroParamCont, SWT.BORDER);
		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label baseModelLbl = new Label(uroParamCont, SWT.NONE);
		baseModelLbl.setText("Base Model:");
		baseModelCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		baseModelCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		baseModelCmb.setItems(new String[] {""});
		
		baseModelCmbViewer = new ComboViewer(baseModelCmb);
		baseModelCmbViewer.setLabelProvider(new LabelProvider() {
			@Override public String getText(Object element) {
				if (element instanceof TrpHtr) {
					return ((TrpHtr) element).getName();
				}
				else return "i am error";
			}
		});
		baseModelCmbViewer.setContentProvider(new ArrayContentProvider());
		
		updateHtrs();
		
		setUroDefaults();
		
		Label emptyLbl = new Label(uroParamCont, SWT.NONE);
		Button resetUroDefaultsBtn = new Button(uroParamCont, SWT.PUSH);
		resetUroDefaultsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		resetUroDefaultsBtn.setText("Reset to defaults");
		resetUroDefaultsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				setUroDefaults();
			}
		});
		
		uroTabItem.setControl(uroParamCont);

		paramCont.pack();

		// doc selection ===========================================================================================

		selectionMethodTabFolder = new CTabFolder(sash, SWT.BORDER | SWT.FLAT);
		selectionMethodTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		// create thumbnail view ================================================================================0
		
		thumbNailTabItem = new CTabItem(selectionMethodTabFolder, SWT.NONE);
		thumbNailTabItem.setText("Thumbnail View");
		
		Composite docCont = new Composite(selectionMethodTabFolder, SWT.BORDER);
		docCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docCont.setLayout(new GridLayout(1, false));

		SashForm docSash = new SashForm(docCont, SWT.HORIZONTAL);
		docSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash.setLayout(new GridLayout(2, false));		
		
		
		Composite trainDocCont = new Composite(docSash, SWT.NONE);
		trainDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainDocCont.setLayout(new GridLayout(2, false));
		
		addTrainDocBtn = new Button(trainDocCont, SWT.PUSH);
		addTrainDocBtn.setText("Add Train Document");
		addTrainDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(docTabFolder, SWT.CLOSE);
				Composite docOverviewCont = createDocOverviewCont(trainTwList, 
						useTrainGtVersionChk.getSelection(), docTabFolder, store.getDoc());
				item.setControl(docOverviewCont);
				
				renameTabs(null, docTabFolder);
			}
		});
		
		useTrainGtVersionChk = new Button(trainDocCont, SWT.CHECK);
		useTrainGtVersionChk.setText("Use Groundtruth versions");
		useTrainGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(ThumbnailWidgetVirtualMinimal tw : trainTwList) {
					tw.setUseGtVersions(useTrainGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});

		docTabFolder = new CTabFolder(trainDocCont, SWT.BORDER | SWT.FLAT);
		docTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem item = new CTabItem(docTabFolder, SWT.NONE);
		item.setText(TAB_NAME_PREFIX + 1);
		
		Composite docOverviewCont = createDocOverviewCont(trainTwList, useTrainGtVersionChk.getSelection(), 
				docTabFolder, store.getDoc());
		item.setControl(docOverviewCont); 

		docTabFolder.setSelection(0);
		
		docTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				renameTabs((CTabItem)event.item, docTabFolder);
			}
		});

		Composite testDocCont = new Composite(docSash, SWT.NONE);
		testDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		testDocCont.setLayout(new GridLayout(2, false));
		
		addTestDocBtn = new Button(testDocCont, SWT.PUSH);
		addTestDocBtn.setText("Add Test Document");
		addTestDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(testDocTabFolder, SWT.CLOSE);
				Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
						testDocTabFolder, store.getDoc());
				item.setControl(testDocOverviewCont);
				renameTabs(null, testDocTabFolder);
			}
		});
		
		useTestGtVersionChk = new Button(testDocCont, SWT.CHECK);
		useTestGtVersionChk.setText("Use Groundtruth versions");
		useTestGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(ThumbnailWidgetVirtualMinimal tw : testTwList) {
					tw.setUseGtVersions(useTestGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});
		
		testDocTabFolder = new CTabFolder(testDocCont, SWT.BORDER | SWT.FLAT);
		testDocTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem testItem = new CTabItem(testDocTabFolder, SWT.NONE);
		testItem.setText(TAB_NAME_PREFIX + 1);
		
		Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
				testDocTabFolder, store.getDoc());
		testItem.setControl(testDocOverviewCont); 
		
		testDocTabFolder.setSelection(0);
		
		testDocTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				renameTabs((CTabItem)event.item, testDocTabFolder);
			}
		});
		
		docSash.setWeights(new int[] {50, 50});
		testDocCont.pack();
		trainDocCont.pack();
		
		docCont.pack();
		
		sash.setWeights(new int[] { 34, 66 });
		
		thumbNailTabItem.setControl(docCont);
				
		// create TreeViewer view =============================================================================
		treeViewerTabItem = new CTabItem(selectionMethodTabFolder, SWT.NONE);
		treeViewerTabItem.setText("Tree View");
	
		SashForm docSash2 = new SashForm(selectionMethodTabFolder, SWT.HORIZONTAL);
		docSash2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash2.setLayout(new GridLayout(1, false));		
		
		
		Group treeViewerCont = new Group(docSash2, SWT.NONE);
		treeViewerCont.setText("Training Set");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));
		
		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(docList);
		
		buttonComp = new Composite(docSash2, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		
		previewLbl = new Label(buttonComp, SWT.NONE);
		GridData gd2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		gd2.heightHint = 120;
		gd2.widthHint = 100;
		previewLbl.setLayoutData(gd2);
		
		addToTrainSetBtn = new Button(buttonComp, SWT.PUSH);
		addToTrainSetBtn.setImage(Images.ADD);
		addToTrainSetBtn.setText("Training");
		addToTrainSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		addToTestSetBtn = new Button(buttonComp, SWT.PUSH);
		addToTestSetBtn.setImage(Images.ADD);
		addToTestSetBtn.setText("Testing");
		addToTestSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Group trainOverviewCont = new Group(docSash2, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));
		
		useGtVersionChk = new Button(trainOverviewCont, SWT.CHECK);
		useGtVersionChk.setText("Use Groundtruth versions");
		
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);
		
		Group trainSetGrp = new Group(trainOverviewCont, SWT.NONE);
		trainSetGrp.setText("Training Set");
		trainSetGrp.setLayoutData(tableGd);
		trainSetGrp.setLayout(tableGl);
		
		trainSetOverviewTable = new DataSetTableWidget(trainSetGrp, SWT.BORDER);
		trainSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromTrainSetBtn = new Button(trainSetGrp, SWT.PUSH);
		removeFromTrainSetBtn.setLayoutData(buttonGd);
		removeFromTrainSetBtn.setImage(Images.CROSS);
		removeFromTrainSetBtn.setText("Remove entries from train set");
		
		Group testSetGrp = new Group(trainOverviewCont, SWT.NONE);
		testSetGrp.setText("Test Set");
		testSetGrp.setLayoutData(tableGd);
		testSetGrp.setLayout(tableGl);
		
		testSetOverviewTable = new DataSetTableWidget(testSetGrp, SWT.BORDER);
		testSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		removeFromTestSetBtn = new Button(testSetGrp, SWT.PUSH);
		removeFromTestSetBtn.setLayoutData(buttonGd);
		removeFromTestSetBtn.setImage(Images.CROSS);
		removeFromTestSetBtn.setText("Remove entries from test set");
		
		docSash2.setWeights(new int[] {45, 10, 45});
		
		treeViewerCont.pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		trainSetGrp.pack();
		testSetGrp.pack();
		
		
		treeViewerTabItem.setControl(docSash2);
		
		addListeners();
		
		selectionMethodTabFolder.setSelection(1);
		
		return cont;
	}
	
	private void addListeners() {
		
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object o = selection.getFirstElement();
				if(o instanceof TrpPage) {
					TrpPage p = (TrpPage)o;
					try {
						Image image = ImgLoader.load(p.getThumbUrl());
						if(previewLbl.getImage() != null) {
							previewLbl.getImage().dispose();
						}
						previewLbl.setImage(image);
					} catch (IOException e) {
						logger.error("Could not load image", e);
					}
				} else if(o instanceof TrpDocMetadata) {
					if(previewLbl.getImage() != null) {
						previewLbl.getImage().dispose();
					}
					previewLbl.setImage(null);
				}
				
			}
		});
		
		tv.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(o instanceof TrpDocMetadata) {
					for(TreeItem i : tv.getTree().getItems()) {
						if(i.getData().equals(o)) {
							tv.setExpandedState(o, !i.getExpanded());
						}
					}
					updateColors();
				}
			}
		});
		
		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateColors();
			}
		});
		
		addToTrainSetBtn.addSelectionListener(
				new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						IStructuredSelection sel = (IStructuredSelection)tv.getSelection();
						Iterator<?> it = sel.iterator();
						while(it.hasNext()) {
							Object o = it.next();
							if(o instanceof TrpDocMetadata) {
								TrpDocMetadata docMd = (TrpDocMetadata)o;
								Object[] pageObjArr = contentProv.getChildren(docMd);
								List<TrpPage> pageList = new LinkedList<>();
								for(Object page : pageObjArr) {
									pageList.add((TrpPage)page);
								}
								
								trainDocMap.put(docMd, pageList);
								
								if(testDocMap.containsKey(docMd)) {
									testDocMap.remove(docMd);
								}
							} else if (o instanceof TrpPage) {
								TrpPage p = (TrpPage)o;
								TrpDocMetadata parent = (TrpDocMetadata)contentProv.getParent(p);
								if(trainDocMap.containsKey(parent) && !trainDocMap.get(parent).contains(p)) {
									trainDocMap.get(parent).add(p);
								} else if(!trainDocMap.containsKey(parent)) {
									List<TrpPage> pageList = new LinkedList<>();
									pageList.add(p);
									trainDocMap.put(parent, pageList);
								}
								
								if(testDocMap.containsKey(parent) && testDocMap.get(parent).contains(p)) {
									if(testDocMap.get(parent).size() == 1) {
										testDocMap.remove(parent);
									} else {
										testDocMap.get(parent).remove(p);
									}
								}
							}
						}
						updateTable(trainSetOverviewTable, trainDocMap);
						updateTable(testSetOverviewTable, testDocMap);
						updateColors();
					}
				});
		
		addToTestSetBtn.addSelectionListener(
				new SelectionAdapter(){
					@Override
					public void widgetSelected(SelectionEvent e) {
						IStructuredSelection sel = (IStructuredSelection)tv.getSelection();
						Iterator<?> it = sel.iterator();
						while(it.hasNext()) {
							Object o = it.next();
							if(o instanceof TrpDocMetadata) {
								TrpDocMetadata docMd = (TrpDocMetadata)o;
								Object[] pageObjArr = contentProv.getChildren(docMd);
								List<TrpPage> pageList = new LinkedList<>();
								for(Object page : pageObjArr) {
									pageList.add((TrpPage)page);
								}
								testDocMap.put(docMd, pageList);
								
								if(trainDocMap.containsKey(docMd)) {
									trainDocMap.remove(docMd);
								}
							} else if (o instanceof TrpPage) {
								TrpPage p = (TrpPage)o;
								TrpDocMetadata parent = (TrpDocMetadata)contentProv.getParent(p);
								if(testDocMap.containsKey(parent) && !testDocMap.get(parent).contains(p)) {
									testDocMap.get(parent).add(p);
								} else if(!testDocMap.containsKey(parent)) {
									List<TrpPage> pageList = new LinkedList<>();
									pageList.add(p);
									testDocMap.put(parent, pageList);
								}
								
								if(trainDocMap.containsKey(parent) && trainDocMap.get(parent).contains(p)) {
									if(trainDocMap.get(parent).size() == 1) {
										trainDocMap.remove(parent);
									} else {
										trainDocMap.get(parent).remove(p);
									}
								}
							}
						}
						updateTable(trainSetOverviewTable, trainDocMap);
						updateTable(testSetOverviewTable, testDocMap);
						updateColors();
					}
				});
		
		removeFromTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<DataSetEntry> entries = trainSetOverviewTable.getSelectedDataSets();
				if(!entries.isEmpty()) {
					for(DataSetEntry entry : entries) {
						trainDocMap.remove(entry.getDoc());
					}
					updateTable(trainSetOverviewTable, trainDocMap);
					updateColors();
				}
			}
		});
		
		removeFromTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<DataSetEntry> entries = testSetOverviewTable.getSelectedDataSets();
				if(!entries.isEmpty()) {
					for(DataSetEntry entry : entries) {
						testDocMap.remove(entry.getDoc());
					}
					updateTable(testSetOverviewTable, testDocMap);
					updateColors();
				}
			}
		});
	}
	
	private void updateColors() {
		List<TrpPage> trainPages, testPages;
		for(TreeItem i : tv.getTree().getItems()) {
			TrpDocMetadata doc = (TrpDocMetadata)i.getData();
			
			//default color set
			Color fgColor = BLACK;
			Color bgColor = WHITE;
			
			if(trainDocMap.containsKey(doc) && testDocMap.containsKey(doc)) {
				fgColor = WHITE;
				bgColor = CYAN;
			} else if(trainDocMap.containsKey(doc)){
				fgColor = WHITE;
				if(doc.getNrOfPages() == trainDocMap.get(doc).size()) {
					bgColor = DARK_BLUE;
				} else {
					bgColor = BLUE;
				}
			} else if(testDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if(doc.getNrOfPages() == testDocMap.get(doc).size()) {
					bgColor = DARK_GREEN;
				} else {
					bgColor = GREEN;
				}
			}
			i.setBackground(bgColor);
			i.setForeground(fgColor);
			
			trainPages = trainDocMap.containsKey(doc) ? trainDocMap.get(doc) : new ArrayList<>(0);
			testPages = testDocMap.containsKey(doc) ? testDocMap.get(doc) : new ArrayList<>(0);

			for(TreeItem child : i.getItems()) {
				TrpPage page = (TrpPage)child.getData();
				if(trainPages.contains(page)) {
					child.setBackground(DARK_BLUE);
					child.setForeground(WHITE);
				} else if (testPages.contains(page)) {
					child.setBackground(DARK_GREEN);
					child.setForeground(WHITE);
				} else {
					child.setBackground(WHITE);
					child.setForeground(BLACK);
				}
			}
		}
	}
	
	private void renameTabs(CTabItem closedItem, CTabFolder folder) {
		CTabItem[] items = folder.getItems();
		int count = 1;
		for(CTabItem item : items) {
			if(closedItem != null && item.equals(closedItem)) {
				continue;
			}
			logger.debug("Setting text: " + TAB_NAME_PREFIX + count);
			item.setText(TAB_NAME_PREFIX + count);
			count++;
		}
	}
	
	private void updateHtrs() {
		List<TrpHtr> uroHtrs = new ArrayList<>(0);
		try {
			uroHtrs = store.listHtrs("CITlab");
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e1) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Could not load HTR model list!");
		}
		
		baseModelCmbViewer.setInput(uroHtrs);
	}

	private void setUroDefaults() {
		numEpochsTxt.setText("" + NUM_EPOCHS_DEFAULT);
		learningRateTxt.setText(LEARNING_RATE_DEFAULT);
		noiseCmb.select(NOISE_DEFAULT_CHOICE);
		trainSizeTxt.setText(""+ TRAIN_SIZE_DEFAULT);
//		baseModelCmb.select(0);
	}

	private Composite createDocOverviewCont(List<ThumbnailWidgetVirtualMinimal> twList, boolean useGtVersions, CTabFolder parent, TrpDoc doc) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));
		
		Combo docCombo = new Combo(c, SWT.READ_ONLY);
		docCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		String[] items = new String[docList.size()];
		int selIndex = 0;
		for(int i = 0; i < docList.size(); i++) {
			TrpDocMetadata d = docList.get(i);
			items[i] = d.getDocId() + " - " + d.getTitle();
			if(doc != null && doc.getId() == d.getDocId()) {
				selIndex = i;
			}
		}
		docCombo.setItems(items);
		docCombo.select(selIndex);
		
		final ThumbnailWidgetVirtualMinimal tw = new ThumbnailWidgetVirtualMinimal(c, true, SWT.NONE);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if(doc != null) {
			tw.setDoc(doc, useGtVersions);
		}
		twList.add(tw);
		
		docCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final int index = docCombo.getSelectionIndex();
				TrpDocMetadata d = store.getDocList().get(index);
				try {
					tw.setDoc(store.getRemoteDoc(store.getCollId(), d.getDocId(), -1), useGtVersions);
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e1) {
					logger.error("Could not load remote doc!", e1);
				}
			}
		});
		
		c.pack();
		return c;
	}
	
	@Override
	protected void okPressed() {
		if(!isConfigValid()) {
			return;
		}
		if(paramTabFolder.getSelection().equals(uroTabItem)) {
			conf = new CitLabHtrTrainConfig();
			conf.setDescription(descTxt.getText());
			conf.setModelName(modelNameTxt.getText());
			conf.setLanguage(langTxt.getText());
			
			conf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
			conf.setNoise(noiseCmb.getText());
			conf.setLearningRate(learningRateTxt.getText());
			conf.setTrainSizePerEpoch(Integer.parseInt(trainSizeTxt.getText()));
		} else {
			throw new IllegalArgumentException();
		}
		
		conf.setColId(colId);
		
		if(selectionMethodTabFolder.getSelection().equals(thumbNailTabItem)) {
			conf.setTrain(getSelectionFromThumbnailWidgetList(trainTwList));
			conf.setTest(getSelectionFromThumbnailWidgetList(testTwList));		
		} else {
			conf.setTrain(buildSelectionDescriptorList(trainDocMap));
			conf.setTest(buildSelectionDescriptorList(testDocMap));
		}
		
		if(conf.getTrain().isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad configuration", 
					"Train set must not be empty!");
			return;
		}
		
		if(conf.isTestAndTrainOverlapping()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad configuration", 
					"Train and Test sets must not overlap!");
			return;
		}
		
		String msg = "You are about to start an HTR Training using ";
		if(paramTabFolder.getSelection().equals(uroTabItem)) {
			msg += uroTabItem.getText();
		} else {
			//TODO
			return;
		}
		msg += " HTR.\n\n";
		
		if(selectionMethodTabFolder.getSelection().equals(treeViewerTabItem)) {
			DataSetMetadata trainSetMd = computeDataSetSize(trainDocMap);
			DataSetMetadata testSetMd = computeDataSetSize(testDocMap);
			msg += "Training set size:\t" + trainSetMd.getPages() + " pages\n";
			msg += "\t\t\t\t" + trainSetMd.getLines() + " lines\n";
			msg += "\t\t\t\t" + trainSetMd.getWords() + " words\n";
			msg += "Test set size:\t\t" + testSetMd.getPages() + " pages\n";
			msg += "\t\t\t\t" + + testSetMd.getLines() + " lines\n";
			msg += "\t\t\t\t" + testSetMd.getWords() + " words\n";
		}
		
		msg += "\nStart training?";
		
		int result = DialogUtil.showYesNoDialog(this.getShell(), "Start Training?", msg);
		
		if(result == SWT.YES) {
			super.okPressed();
		}
	}

	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		final boolean useGt = useGtVersionChk.getSelection();
		int pages = 0;
		int lines = 0;
		int words = 0;
		for(Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for(TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				if(useGt) {
					for(TrpTranscriptMetadata t : p.getTranscripts()) {
						if(t.getStatus().equals(EditStatus.GT)) {
							tmd = t;
							break;
						}
					}
				}
				pages++;
				lines += tmd.getNrOfTranscribedLines();
				words += tmd.getNrOfWordsInLines();
			}
		}
		return new DataSetMetadata(pages, lines, words);
	}

	private List<DocumentSelectionDescriptor> buildSelectionDescriptorList(
			Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DocumentSelectionDescriptor> list = new LinkedList<>();
		final boolean useGt = useGtVersionChk.getSelection();
		
		for(Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			DocumentSelectionDescriptor dsd = new DocumentSelectionDescriptor();
			dsd.setDocId(e.getKey().getDocId());
			for(TrpPage p : e.getValue()) {
				PageDescriptor pd = new PageDescriptor();
				pd.setPageId(p.getPageId());
				pd.setTsId(p.getCurrentTranscript().getTsId());
				if(useGt) {
					for(TrpTranscriptMetadata t : p.getTranscripts()) {
						if(t.getStatus().equals(EditStatus.GT)) {
							pd.setTsId(t.getTsId());
							break;
						}
					}
				}
				dsd.addPage(pd);
			}
			list.add(dsd);
		}
		
		return list;
	}

	private List<DocumentSelectionDescriptor> getSelectionFromThumbnailWidgetList(
			List<ThumbnailWidgetVirtualMinimal> twList) {
		
		List<DocumentSelectionDescriptor> list = new LinkedList<>();
		for(ThumbnailWidgetVirtualMinimal tw : twList) {
			DocumentSelectionDescriptor dsd = tw.getSelectionDescriptor();
			
			if(dsd != null) {
				list.add(dsd);
			}
		}
		return list;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("HTR Training");
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

	private boolean isConfigValid() {
		String error = "";
		if(!isString(modelNameTxt)) {
			error += "Model Name must not be empty!\n";
		}
		if(!isString(descTxt)) {
			error += "Description must not be empty!\n";
		}
		if(!isString(langTxt)) {
			error += "Language must not be empty!\n";
		}
		if(paramTabFolder.getSelection().equals(uroTabItem)) {
			if(!isNumber(numEpochsTxt)) {
				error += "Number of Epochs must contain a number!\n";
			}
			if(!isString(learningRateTxt)) {
				error += "Learning rate must not be empty!\n";
			}
			if(!isNumber(trainSizeTxt)) {
				error += "Train size per epoch must contain a number!\n";
			}
		}
		if(!error.isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad Configuration", error);
		}
		return error.isEmpty();
	}
	
	private boolean isString(Text text) {
		return !text.getText().isEmpty();
	}
	
	private boolean isNumber(Text text) {
		if(text.getText().isEmpty()) {
			return false;
		}
		try {
			Integer.parseInt(text.getText());
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public CitLabHtrTrainConfig getConfig() {
		return conf;
	}
	
	private void updateTable(DataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DataSetEntry> list = new ArrayList<>(map.entrySet().size());
		for(Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();
			
			List<TrpPage> pageList = entry.getValue();
			
			list.add(new DataSetEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}
	
	public class DataSetEntry implements Comparable<DataSetEntry> {
		private String pageString;
		private TrpDocMetadata doc;
		private List<TrpPage> pages;
		public DataSetEntry (TrpDocMetadata doc, List<TrpPage> pages) {
			Collections.sort(pages);
			final int nrOfPages = doc.getNrOfPages();
			List<Boolean> boolList = new ArrayList<>(nrOfPages);
			for(int i = 0; i < nrOfPages; i++) {
				boolList.add(i, Boolean.FALSE);
			}
			
			for(TrpPage p : pages) {
				boolList.set(p.getPageNr()-1, Boolean.TRUE);
			}			
			this.pageString = CoreUtils.getRangeListStr(boolList);
			this.pages = pages;
			this.doc = doc;
		}
		public int getId() {
			return doc.getDocId();
		}
		public String getTitle() {
			return doc.getTitle();
		}
		public String getPageString() {
			return pageString;
		}
		public void setPageString(String pageString) {
			this.pageString = pageString;
		}
		public TrpDocMetadata getDoc() {
			return doc;
		}
		public void setDoc(TrpDocMetadata doc) {
			this.doc = doc;
		}
		public List<TrpPage> getPages() {
			return pages;
		}
		public void setPages(List<TrpPage> pages) {
			this.pages = pages;
		}
		
		@Override
		public int compareTo(DataSetEntry o) {
			if (this.doc.getDocId() > o.getId()) {
				return 1;
			}
			if (this.doc.getDocId() < o.getId()) {
				return -1;
			}
			return 0;
		}
	}
	
	private class DataSetMetadata {
		private int pages;
		private int lines;
		private int words;
		public DataSetMetadata(int pages, int lines, int words) {
			this.pages = pages;
			this.lines = lines;
			this.words = words;
		}
		public int getPages() {
			return pages;
		}
		public int getLines() {
			return lines;
		}
		public int getWords() {
			return words;
		}
	}
}
