package eu.transkribus.swt.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NullValueException;
import eu.transkribus.core.io.UnsupportedFormatException;
import eu.transkribus.core.model.beans.TrpAction;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTotalTranscriptStatistics;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProviderExtended;
import eu.transkribus.swt_gui.htr.DataSetMetadata;
import eu.transkribus.swt_gui.htr.DocumentDataSetTableWidget;
import eu.transkribus.swt_gui.htr.treeviewer.DocumentDataSelectionEntry;
import eu.transkribus.swt_gui.la.LayoutAnalysisDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocumentManager extends Dialog {
	protected final static Logger logger = LoggerFactory.getLogger(DocumentManager.class);

	protected Composite groupComposite;
	protected Composite labelComposite;

	Combo labelCombo, statusCombo, movePage, optionCombo;

	Composite editCombos;

	protected Label statisticLabel;
	protected Label pageNrLabel;
	protected Label totalTranscriptsLabel;
	protected Label totalWordTranscriptsLabel;
	protected Label lastSaveAction;
	
	protected Label docLabel, collLabel;
	
	LabeledText nrOfPagesTxt, documentNameLbl;

	protected GalleryItem group;

	protected Button reload, showOrigFn, createThumbs, startLA, statisticButton, addPage, addTrans,
						revert, deletePage , addToSampleSetBtn, removeFromSampleSetBtn, createSampleButton;
	protected Button sort;
	//protected Button collectionImageBtn, documentImageBtn;
	protected Button showCollectionImageBtn, showDocumentImageBtn;

	protected List<URL> urls;
	protected List<String> names = null;

	protected List<TrpTranscriptMetadata> transcripts;

	protected List<Integer> nrTranscribedLines;

	protected AbstractGridGroupRenderer groupRenderer;

	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProviderExtended labelProv;
	private Composite buttonComp, buttonComp2, imageComp;
	private Canvas previewLbl;

	private int colId;
	private List<TrpDocMetadata> docList;
	private boolean canManage = true;
	
	private HashMap<Integer,String> latestSavesMap = new HashMap<Integer,String>();
	private Map<TrpDocMetadata, List<TrpPage>> sampleDocMap;
	private DocumentDataSetTableWidget sampleSetOverviewTable;
	private Storage store = Storage.getInstance();


	static int thread_counter = 0;

	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	static final Color lightBlue = new Color(Display.getCurrent(), 0, 140, 255);
	
	Image image;

	static GalleryItem[] draggedItem;
	static int[] originalItemIndex;

	Shell shell;
	TrpDocMetadata docMd;
	TrpMainWidget mw;
	Menu contextMenu;
	
	public DocumentManager(Shell parent) {
		this(parent, 0, TrpMainWidget.getInstance(), Storage.getInstance().getCollId());
	}

	private DocumentManager(Shell parent, int style, TrpMainWidget mw, int colId) {
		super(parent.getShell(), style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));

		this.colId = colId;

		this.mw = mw;

		docList = Storage.getInstance().getDocList();
		
		sampleDocMap = new TreeMap<>();

		if (Storage.getInstance().getDoc() != null) {
			docMd = Storage.getInstance().getDoc().getMd();
		}

		shell = new Shell((Shell)parent, style);
		shell.setText("Document Manager");

		FillLayout l = new FillLayout();
		l.marginHeight = 5;
		l.marginWidth = 5;
		shell.setLayout(l);

		Composite container = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		

		SashForm sash = new SashForm(container, SWT.VERTICAL);
		sash.setLayout(new GridLayout(2, false));
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		groupComposite = new Composite(sash, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.makeColumnsEqualWidth = true;
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, true, true);
		groupComposite.setLayoutData(gridData);
		
		Composite btns = new Composite(container, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));

		reload = new Button(btns, SWT.PUSH);
		reload.setToolTipText("Reload thumbs");
		reload.setImage(Images.REFRESH);
		
		statisticButton = new Button(btns, SWT.None);
		statisticButton.setText("Load statistics");

		createTreeViewerTab(sash);

		contextMenu = new Menu(tv.getTree());
		tv.getTree().setMenu(contextMenu);

		sash.setWeights(new int[] { 0, 100 });

		addListeners();

		shell.pack();
	}	

	public Object open() {
		if (shell != null) {
			shell.setMinimumSize(800, 800);
			shell.setSize(1000, 800);
			SWTUtil.centerShell(shell);

			shell.open();
			shell.layout();

//			addStatisticalNumbers();

			expandCurrentDocument();

			Display display = shell.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} else {
			logger.debug("Shell is null????????");
		}

		return null;
	}

	private void expandCurrentDocument() {
		for (TreeItem ti : tv.getTree().getItems()) {
			TrpDocMetadata md = (TrpDocMetadata) ti.getData();
			if (store.getDoc() != null && md.compareTo(store.getDoc().getMd()) == 0) {
				tv.expandToLevel(md, 1);
				tv.getTree().setTopItem(ti);
				
				updateColors();
//				if (ti.getItems().length > 0) {
//					TreeItem[] childs = ti.getItems();
//					for (TreeItem child : ti.getItems()) {
//						TrpPage p = (TrpPage) child.getData();
//						tv.getTree().setSelection(child);
//						try {
//							image = ImgLoader.load(p.getUrl());
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
////						previewLbl.redraw();
////						previewLbl.update();
//						break;
//					}
//				}
//				
//				updateSymbolicImgLabels();
			}
		}

	}

	/*
	 * TODO: add functionality when the underlying data model is clear and
	 * implemented
	 */
	private void addChooseImageMenuItems(Menu menu) {
		MenuItem chooseImg = new MenuItem(menu, SWT.NONE);
		chooseImg.setText("Take as document image");
		chooseImg.setToolTipText("Take this image as the symbolic image for this document");

		logger.debug("listener for setting symbolic doc image called");

		chooseImg.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				addSymbolicDocImage();
			}
		});

		MenuItem chooseCollectionImg = new MenuItem(menu, SWT.NONE);
		chooseCollectionImg.setText("Take as collection image");
		chooseCollectionImg.setToolTipText("Take this image as the symbolic image for the overall collection");
		chooseCollectionImg.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				addSymbolicCollectionImage();
			}
		});

	}
	
	private void addSymbolicDocImage() {
		try {
			for (TreeItem ti : tv.getTree().getSelection()) {

				if (ti.getData() instanceof TrpPage){
					TrpPage p = (TrpPage) ti.getData();
					if(p.getDocId() == docMd.getDocId()){
						docMd.setPageId(p.getPageId());
						
						ti.getParentItem().setData(docMd);
						store.getConnection().updateDocMd(colId, docMd.getDocId(), docMd);
						reloadRemoteDoc();
					}
					break;
				}	
			}
		}
		catch (SessionExpiredException | IllegalArgumentException e) {
			logger.error(e.getMessage(), e);		
		} 
		catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		updateColors();
		updateSymbolicImgLabels();
		tv.getTree().redraw();

	}
	
	private void addSymbolicCollectionImage() {
		try {
			for (TreeItem ti : tv.getTree().getSelection()) {
				if (ti.getData() instanceof TrpPage){
					TrpPage p = (TrpPage) ti.getData();
					TrpCollection colMd = Storage.getInstance().getDoc().getCollection();
					colMd.setPageId(new Integer(p.getPageId()));
					Storage.getInstance().getConnection().updateCollectionMd(colMd);
					Storage.getInstance().reloadCollections();
					break;
				}
			}
		} catch (SessionExpiredException | IllegalArgumentException | ServerErrorException | NoConnectionException e) {
			logger.error(e.getMessage(), e);
		}

		updateColors();
		updateSymbolicImgLabels();
		tv.getTree().redraw();
		
	}

	private static int getLevelOfItem(TreeItem item) {
		int counter = 0;

		while (item.getParentItem() != null) {
			item = item.getParentItem();
			counter++;
		}

		return counter;
	}

	private void createTreeViewerTab(Composite parent) {
		SashForm docSash2;

		docSash2 = new SashForm(parent, SWT.HORIZONTAL);
		docSash2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash2.setLayout(new GridLayout(2, false));
		

		Group treeViewerCont = new Group(docSash2, SWT.NONE);
		treeViewerCont.setText("Documents in current Collection");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));

		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProviderExtended();
		
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(docList);
			
		SashForm docSashOptionImage = new SashForm(docSash2, SWT.VERTICAL);
		docSashOptionImage.setLayout(new GridLayout(2, false));
		docSashOptionImage.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		editCombos = new Composite(docSashOptionImage, SWT.NONE);
		editCombos.setLayout(new GridLayout(2, true));
		editCombos.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		statusCombo = initComboWithLabel(editCombos, "Change edit status: ", SWT.DROP_DOWN | SWT.READ_ONLY);
		statusCombo.setItems(EditStatus.getStatusListWithoutNew());
		statusCombo.setEnabled(false);
		
		Label addLable = new Label(editCombos, SWT.CENTER);
		addLable.setText("Add new page(s)");
		addPage = new Button(editCombos, SWT.PUSH);
		addPage.setImage(Images.ADD);
		addPage.setEnabled(false);
		
		Label transLabel = new Label(editCombos, SWT.CENTER);
		transLabel.setText("Add local transcription (PAGEXML)");
		addTrans = new Button(editCombos, SWT.PUSH);
		addTrans.setImage(Images.ADD);
		addTrans.setEnabled(false);
		
		Label revertLabel = new Label(editCombos, SWT.CENTER);
		revertLabel.setText("Revert to previous version(s) of last job");
		revert = new Button(editCombos, SWT.PUSH);
		revert.setImage(Images.ADD);
		revert.setEnabled(false);
		
		/*
		 * no listener and method implemented right now
		 */
//		Label sortLabel = new Label(editCombos, SWT.CENTER);
//		sortLabel.setText("Sort pages by filename list..");
//		sort = new Button(editCombos, SWT.CENTER);
//		sort.setImage(Images.ADD);
//		sort.setEnabled(false);
		
		// Page specific buttons
		movePage = initComboWithLabel(editCombos, "Move page(s) to", SWT.DROP_DOWN | SWT.READ_ONLY);
		movePage.setItems("Before first page","After last page","Select position");
		movePage.setEnabled(false);
			
		Label deleteLabel = new Label(editCombos, SWT.CENTER);
		deleteLabel.setText("Delete page(s)");
		deletePage = new Button(editCombos, SWT.CENTER);
		deletePage.setText("Delete");
		deletePage.setEnabled(false);
		
		
		Group imageGroup = new Group(docSashOptionImage, SWT.SHADOW_IN);
		imageGroup.setText("Create basic set");
		imageGroup.setLayout(new GridLayout(2, true));
		imageGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2,2));
		
		imageComp = new Composite(imageGroup, SWT.NONE);
		imageComp.setLayout(new GridLayout(1, true));
		
		addToSampleSetBtn = new Button(imageComp, SWT.PUSH);
		addToSampleSetBtn.setImage(Images.ADD);
		addToSampleSetBtn.setText("Add to Basic Set");
		addToSampleSetBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);
		
		Group sampleSetGrp = new Group(imageGroup, SWT.NONE);
		sampleSetGrp.setText("Documents added to Basic Set");
		sampleSetGrp.setLayoutData(tableGd);
		sampleSetGrp.setLayout(tableGl);

		sampleSetOverviewTable = new DocumentDataSetTableWidget(sampleSetGrp, SWT.BORDER);
		sampleSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromSampleSetBtn = new Button(sampleSetGrp, SWT.PUSH);
		removeFromSampleSetBtn.setLayoutData(buttonGd);
		removeFromSampleSetBtn.setImage(Images.CROSS);
		removeFromSampleSetBtn.setText("Remove");
		
		createSampleButton = new Button(sampleSetGrp, SWT.PUSH);
		createSampleButton.setLayoutData(buttonGd);
		createSampleButton.setImage(Images.DISK);
		createSampleButton.setText("Create Sample");
		
		// Uncomment following part to enable preview of page thumbnail
		
//		previewLbl = new Canvas(docSashOptionImage, SWT.NONE);
//
//		GridData gd2 = new GridData(GridData.CENTER, GridData.CENTER, true, true);
//
//		previewLbl.setLayoutData(gd2);
//
//		previewLbl.addPaintListener(new PaintListener() {
//			public void paintControl(final PaintEvent event) {
//				if (image != null) {
//					float ratio = (float) image.getBounds().height / image.getBounds().width;
//					ImageData data = image.getImageData();
//					int h = (int) (250 * ratio);
//					event.gc.drawImage(image, docSashOptionImage.getSashWidth()/2, 0, image.getBounds().width, image.getBounds().height, 0, 0, 250, h);
//				}
//			}
//		});

		updateColors();
		docSashOptionImage.setWeights(new int[] { 45, 50});
		docSash2.setWeights(new int[] { 35, 65 });


	}

	private void updateSymbolicImgLabels() {

		
		if (Storage.getInstance().getCollection(colId) != null){
			
			TrpCollection currCol = Storage.getInstance().getCollection(colId);
			if (currCol.getPageId() != null){
				
				//tv.expandToLevel(2);

				for (TreeItem i : tv.getTree().getItems()) {

					for (TreeItem child : i.getItems()) {
						TrpPage page = (TrpPage) child.getData();
						if (page == null) {
							continue;
						}
							
					}
							
				}
			}	
		}
//		buttonComp2.layout();
	}

	private void addListeners() {
		
		tv.getTree().addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent e) {
				TreeItem treeItem = tv.getTree().getSelection()[0];
				e.doit = getLevelOfItem(treeItem) < 2;
			}
		});
		contextMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				TreeItem item = tv.getTree().getSelection()[0];
//				setButtonLevelEnable(item);
			}
		});
		
		statisticButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("loading statistics...");
				addStatisticalNumbers();
			}
		});
		
		reload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});
		
		statusCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug(statusCombo.getText());
				mw.changeVersionStatus(statusCombo.getText(), getPageList());
				
				totalReload(colId);

//				tv.getTree().update();
//				try {
//					store.reloadDocWithAllTranscripts();
//				} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				contentProv.inputChanged(null, null, store.getDocList());
//				expandCurrentDocument();
//
//				tv.getTree().redraw();
			}
		});
		
		addPage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mw.addSeveralPages2Doc();
				try {
//					Storage.getInstance().reloadCurrentDocument(colId);
					totalReload(colId);
				} catch (IllegalArgumentException e1) {
					logger.error(e1.getMessage(), e1);
				}
				
			}
		});
		
		
		addTrans.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				mw.getDocSyncController().syncPAGEFilesWithLoadedDoc();
			}
		});
		
		revert.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (DialogUtil.showYesNoDialog(mw.getShell(), "Revert to previous version(s)", "Do you really want to revert to the version(s) before the last job was completed")!=SWT.YES) {
					return;
				}
				Thread thread = new Thread(){
				    public void run(){
				    	mw.revertVersions();
				    }
				};
			
				thread.start();
			}
		});
		
		
		movePage.addSelectionListener(new SelectionAdapter() {
			 public void widgetSelected(SelectionEvent e) {
				 try {
					 if (movePage.getText().equals("Before first page")) {
						 
						 logger.debug("Moved to 1st place");
						 movePages(getPageList(), 1);
						 totalReload(colId);
					 } else if(movePage.getText().equals("After last page")) {
						 logger.debug("Moved to last place");
						 int NPages = Storage.getInstance().getDoc().getNPages();
							
						 movePages(getPageList(), NPages);
						 totalReload(colId);
					 } else if(movePage.getText().equals("Select position")) {
						logger.debug("Moved selection place");
						
						/*
						 * using this is much faster then using the extra created shell4Text
						 */
						Double pos = DialogUtil.showDoubleInputDialog(getShell(), "Select position", "To which position do you want to move the page(s)", -1);
						if (pos != null) {
							movePages(getPageList(), pos.intValue());
							totalReload(colId);
						}
					 }
				 } catch (Exception e1) {
					 mw.onError("An error occurred moving pages", e1.getMessage(), e1);
					 totalReload(colId); // for safety reasons...
//					 logger.error(e1.getMessage(), e1);
				 }finally{
//					 totalReload(colId);
				 }
				 
					//mw.getUi().getThumbnailWidget().reload();
					//tv.getTree().redraw();
			 }
		});
		
		deletePage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int response = DialogUtil.showYesNoCancelDialog(mw.getShell(), "Delete page from server",
						"Are you sure you want to delete the selected page(s)? \nThis action cannot be undone.");
				if (response == SWT.YES) {
					tv.getSelection();
					List<TrpPage> selection = new ArrayList<TrpPage>();// =
																		// (List<TrpPage>)
																		// tv.getSelection();
					IStructuredSelection treeSelection = (IStructuredSelection) tv.getSelection();
					Iterator it = treeSelection.iterator();
					/*
					 * delete Pages: now only pages from one document can be
					 * deleted at the same time. No document must be chosen
					 */
					while (it.hasNext()) {
						Object o = it.next();
						if (o instanceof TrpPage) {
							TrpPage currPage = (TrpPage) o;
							//logger.debug("selected page " + currPage.getImgFileName());
							selection.add(currPage);
							tv.remove(currPage);
						} else if (o instanceof TrpDoc) {
							break;
						}

					}
					deletePages(selection);
					try {
						totalReload(colId);
						//mw.getUi().getThumbnailWidget().reload();
					} catch (IllegalArgumentException e1) {
						logger.error(e1.getMessage(), e1);
					}

				}
			}
		});

		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//logger.debug("selection changed");
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object o = selection.getFirstElement();
				int currDocId = 0;
				if (o instanceof TrpPage) {
					TrpPage p = (TrpPage) o;	
					currDocId = p.getDocId();
					logger.info("Change to enable Page - selectionListener");
					enableEditsPage(currDocId == Storage.getInstance().getDocId() && canManage);

				} else if (o instanceof TrpDocMetadata) {
					logger.info("Change to enable Doc - selectionListener");
					currDocId = ((TrpDocMetadata) o).getDocId();
					enableEditsDoc(currDocId == Storage.getInstance().getDocId() && canManage);
				}
				updateColors();	

			}
		});

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				try {
					Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
					int currDocId = 0;
					if (o instanceof TrpDocMetadata) {
						setDefaultStatistics();
						for (TreeItem i : tv.getTree().getItems()) {
							if (i.getData().equals(o)) {
								tv.setExpandedState(o, !i.getExpanded());
								break;
							}
						}
						TrpLocation loc = new TrpLocation();
						loc.collId = colId;
						loc.docId = ((TrpDocMetadata) o).getDocId();
						loc.pageNr = 1;
						mw.showLocation(loc);
						currDocId = loc.docId;
						expandCurrentDocument();
						enableEditsDoc(currDocId == Storage.getInstance().getDocId() && canManage);
					} else if (o instanceof TrpPage) {
						TrpPage p = (TrpPage) o;
						TrpLocation loc = new TrpLocation();
						loc.collId = colId;
						loc.docId = p.getDocId();
						loc.pageNr = p.getPageNr();
						mw.showLocation(loc);
						currDocId = loc.docId;
						enableEditsPage(currDocId == Storage.getInstance().getDocId() && canManage);
					}
					docMd = Storage.getInstance().getDoc().getMd();
					tv.refresh(true);
					updateColors();
					updateSymbolicImgLabels();
				} catch (SWTException e) {
					if (!e.getMessage().equals("Widget is disposed")) {
						throw e;
					} else {
						logger.warn("Ignoring widget disposed exception caused by invalidated session");
					}
				}
			}
		});

		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				try {
					updateColors();
				} catch (SWTException ex) {
					if (!ex.getMessage().equals("Widget is disposed")) {
						throw ex;
					} else {
						logger.warn("Ignoring widget disposed exception caused by invalidated session");
					}
				}
			}
		});
		
		addToSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
					Iterator<?> it = sel.iterator();
					while (it.hasNext()) {
						Object o = it.next();
						if (o instanceof TrpDocMetadata) {
							TrpDocMetadata docMd = (TrpDocMetadata) o;
							Object[] pageObjArr = contentProv.getChildren(docMd);
							List<TrpPage> pageList = new LinkedList<>();
							for (Object page : pageObjArr) {
								pageList.add((TrpPage) page);
							}

							sampleDocMap.put(docMd, pageList);

						} else if (o instanceof TrpPage) {
							TrpPage p = (TrpPage) o;
							TrpDocMetadata parent = (TrpDocMetadata) contentProv.getParent(p);
							if (sampleDocMap.containsKey(parent) && !sampleDocMap.get(parent).contains(p)) {
								sampleDocMap.get(parent).add(p);
							} else if (!sampleDocMap.containsKey(parent)) {
								List<TrpPage> pageList = new LinkedList<>();
								pageList.add(p);
								sampleDocMap.put(parent, pageList);
							}

						}
					}
					updateTable(sampleSetOverviewTable, sampleDocMap);
					updateColors();
				} catch (SWTException ex) {
					if (!ex.getMessage().equals("Widget is disposed")) {
						throw ex;
					} else {
						logger.warn("Ignoring widget disposed exception caused by invalidated session");
					}
				}
			}
		});

		removeFromSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					List<DocumentDataSelectionEntry> entries = sampleSetOverviewTable.getSelectedDataSets();
					if (!entries.isEmpty()) {
						for (DocumentDataSelectionEntry entry : entries) {
							sampleDocMap.remove(entry.getDoc());
						}
						updateTable(sampleSetOverviewTable, sampleDocMap);
						updateColors();
//						nrOfPagesTxt.setText(""+getSampleSetMetadata().getPages());
					}
				} catch (SWTException ex) {
					if (!ex.getMessage().equals("Widget is disposed")) {
						throw ex;
					} else {
						logger.warn("Ignoring widget disposed exception caused by invalidated session");
					}
				}
			}
		});
		
		
		
		
		createSampleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					String msg = "";
					DataSetMetadata sampleSetMd = getSampleSetMetadata();
					msg += "Sample set size:\n \t\t\t\t" + sampleSetMd.getPages() + " pages\n";
					msg += "\t\t\t\t" + sampleSetMd.getLines() + " lines\n";
					msg += "\t\t\t\t" + sampleSetMd.getWords() + " words\n";
//					msg += "Samples Options:\n ";
//					msg += "\t\t\t\t" + nrOfPagesTxt.getText()  + " pages\n";

					Composite optionComp = new Composite(getParent(), SWT.NONE);
					optionComp.setLayout(new GridLayout(2, false));

					optionCombo = initComboWithLabel(optionComp, "Option : ", SWT.DROP_DOWN | SWT.READ_ONLY);
					optionCombo.setItems("Random", "Systematic", "For each document x pages");
					optionCombo.setEnabled(true);

					nrOfPagesTxt = new LabeledText(optionComp, "Nr of pages for each document");
					nrOfPagesTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2));
					nrOfPagesTxt.setText("" + getSampleSetMetadata().getPages());

					documentNameLbl = new LabeledText(optionComp, "Document name");
					documentNameLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2));

					optionCombo.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							logger.debug(optionCombo.getText());
							if (optionCombo.getText().equals("Systematic")) {
								nrOfPagesTxt.label.setText("X-th page of each document");
							} else if (optionCombo.getText().equals("Random")) {
								nrOfPagesTxt.label.setText("Nr. of pages for sample");
							} else {
								nrOfPagesTxt.label.setText("Nr of pages for each document");
							}
						}
					});

					Button start = new Button(optionComp, SWT.NONE);
					start.setText("Create");
					start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 2));

					DialogUtil.openShellWithComposite(getShell(), optionComp, 350, 200, "Sample options");

					start.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {

							if (sampleSetMd.getPages() < Integer.parseInt(nrOfPagesTxt.getText())) {
								DialogUtil.showErrorMessageBox(getShell(), "Error number of lines",
										"Choose at most " + sampleSetMd.getPages() + " pages for your sample");
							} else if (documentNameLbl.getText().isEmpty()) {
								DialogUtil.showErrorMessageBox(getShell(), "Error! Document name is missing",
										"Please insert a document name");
							} else if (optionCombo.getText().isEmpty()) {
								DialogUtil.showErrorMessageBox(getShell(), "Error! Option is missing",
										"Please insert an option");
							} else {

								try {

									store.createSamplePages(sampleDocMap, Integer.parseInt(nrOfPagesTxt.getText()),
											"Sample_" + documentNameLbl.getText(), "Description",
											optionCombo.getText());

									DialogUtil.showInfoMessageBox(getShell(), "Sample Job started",
											"Started sample job ");

								} catch (ServerErrorException | ClientErrorException | IllegalArgumentException
										| SessionExpiredException ex) {
									logger.error(ex.getMessage(), ex);
								}
							}
						}
					});
				} catch (SWTException ex) {
					if (!ex.getMessage().equals("Widget is disposed")) {
						throw ex;
					} else {
						logger.warn("Ignoring widget disposed exception caused by invalidated session");
					}
				}
			}
		});

	}

	private void updateColors() {

		for (TreeItem i : tv.getTree().getItems()) {
			// current doc is expanded and should be marked with blue color
			if (Storage.getInstance().getDoc() != null
					&& ((TrpDocMetadata) i.getData()).compareTo(Storage.getInstance().getDoc().getMd()) == 0) {
				i.setForeground(lightBlue);
			} else {
				i.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
			}
			TrpDocMetadata doc = (TrpDocMetadata) i.getData();
			if (doc == null) {
				logger.debug("null??");
				continue;
			}

			// TODO dependent on status different colors get set
			// if (doc.getStatus().equals())

			for (TreeItem child : i.getItems()) {

				TrpPage page = (TrpPage) child.getData();
				if (page == null) {
					continue;
				}
				
				Fonts.setNormalFont(child);
				child.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
				
				if (page.getCurrentTranscript().getNrOfTranscribedLines() != null){
					if (page.getCurrentTranscript().getNrOfTranscribedLines() > 0) {
						child.setBackground(lightGreen);
					} else if (page.getCurrentTranscript().getNrOfTranscribedLines() == 0
							&& page.getCurrentTranscript().getNrOfLines() > 0) {
						child.setBackground(lightYellow);
					} else {
						child.setBackground(lightRed);
					}
				}
				
				TrpCollection colMd = null;
				if (Storage.getInstance().getDoc() != null){
					colMd = Storage.getInstance().getDoc().getCollection();
				}
				
				// highlight page set as symbolic image for collection
				if (colMd != null && colMd.getPageId() != null && Integer.valueOf(page.getPageId()).equals(colMd.getPageId())){
					//logger.debug("symbolic image found for collection with ID: " + colMd.getColId());
					child.setFont(Fonts.addStyleBit(child.getFont(), SWT.BOLD));
					child.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_CYAN));
				}
				// highlight page set as symbolic image for document
				else if (doc != null && doc.getPageId() != null && Integer.valueOf(page.getPageId()).equals(doc.getPageId())) {
					//logger.debug("symbolic image found for document with ID: " + doc.getDocId());
					logger.debug("here: "+page.getPageId()+" - "+doc.getPageId());
					child.setFont(Fonts.addStyleBit(child.getFont(), SWT.BOLD));
					child.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
				}
				// highlight currently loaded page
				if (store.getPage()!=null && page.getPageId()==store.getPage().getPageId()) {
					child.setFont(Fonts.addStyleBit(child.getFont(), SWT.ITALIC));
				}
			}
		}
	}

	private void movePage(int fromPageNr, int toPageNr) {
		try {
			Storage.getInstance().movePage(colId, Storage.getInstance().getDocId(), fromPageNr, toPageNr);
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e) {
			logger.error(e.toString());
		}

	}

	private void deletePage(TrpPage page) {
		try {
			Storage.getInstance().deletePage(colId, page.getDocId(), page.getPageNr());
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e) {
			logger.error(e.toString());
		}
	}

	private void deletePages(List<TrpPage> selection) {
		Collections.sort(selection, new Comparator<TrpPage>() {
			@Override
			public int compare(TrpPage o1, TrpPage o2) {
				return o1.getPageNr() - o2.getPageNr();
			}
		});
		
		Collections.reverse(selection);
//		for (TrpPage page : selection) {
//			deletePage(page);
//		}
		
		ProgressBarDialog pbd = new ProgressBarDialog(getShell());
		IRunnableWithProgress r = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Delete page(s): ", selection.size());
				int a = 0;
				for (TrpPage page : selection) {

					monitor.setTaskName("Delete page: " + ++a);
					deletePage(page);
					monitor.worked(a);
				}

				logger.debug("Finished deleting page(s)");
			}
		};
		
		try {
			pbd.open(r, "Deleting page(s) from this document", true);
		} catch (Throwable e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", e.getMessage());
			logger.error("Error in ProgressMonitorDialog", e);
		}

	}

	private void movePages(List<TrpPage> selection, int toPageNr)
			throws SessionExpiredException, ServerErrorException, ClientErrorException, NoConnectionException,
			IllegalArgumentException, UnsupportedFormatException, IOException, NullValueException {

		Collections.sort(selection, new Comparator<TrpPage>() {
			@Override
			public int compare(TrpPage o1, TrpPage o2) {
				return o1.getPageNr() - o2.getPageNr();
			}
		});

		if (selection == null || selection.size() == 0) {
			logger.debug("move pages called with null argument");
			return;
		}
		
		ProgressBarDialog pbd = new ProgressBarDialog(getShell());
		IRunnableWithProgress r = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Move page(s): ", selection.size());
				int a = 0;
				if (selection.get(0).getPageNr() > toPageNr) {
					int j = 0;
					for (int i = selection.size() - 1; i >= 0; i--) {
						monitor.setTaskName("Move page: " + ++a);
						movePage(selection.get(i).getPageNr() + j, toPageNr);
						j++;
						monitor.worked(a);
					}

				} else if (selection.get(0).getPageNr() < toPageNr) {
					int i = 0;
					for (TrpPage page : selection) {
						monitor.setTaskName("Move page: " + ++a);
						movePage(page.getPageNr() - i, toPageNr);
						i++;
						monitor.worked(a);
					}
				}

				logger.debug("Finished moving page(s)");
			}
		};
		
		
		try {
			pbd.open(r, "Checking remote directories...", false);
		} catch (Throwable e) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", e.getMessage());
			logger.error("Error in ProgressMonitorDialog", e);
		}

	}

	private void changeVersionStatus(String text, List<TrpPage> pageList) {
		Storage storage = Storage.getInstance();

		try {
			
			if (!pageList.isEmpty()) {
	
				for (TrpPage page : pageList) {
					int pageNr = page.getPageNr();
					int docId = page.getDocId();
					int transcriptId = 0;
					if ((pageNr - 1) >= 0) {
						transcriptId = page.getCurrentTranscript().getTsId();
					}
					
						storage.getConnection().updatePageStatus(colId, docId, pageNr, transcriptId,
								EditStatus.fromString(text), "");
	
						// tw.setDoc(Storage.getInstance().getDoc(), false);
						//enableEdits(false);
						
						/*
						 * TODO: we break after first change because otherwise too slow for a batch
						 * Try to fasten this on the server side
						 */
						break;
						// logger.debug("status is changed to : " +
						// storage.getDoc().getPages().get(pageNr-1).getCurrentTranscript().getStatus());
				}
				storage.reloadCollections();
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage(), e);
		} catch (NoConnectionException e) {
			logger.error(e.getMessage(), e);
		}

	}

	private static Combo initComboWithLabel(Composite parent, String label, int comboStyle) {

		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText(label);

		Combo combo = new Combo(parent, comboStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		return combo;
	}

	protected void setup_layout_recognition(String pages) throws SessionExpiredException, ServerErrorException,
			ClientErrorException, IllegalArgumentException, NoConnectionException {
		LayoutAnalysisDialog laD = new LayoutAnalysisDialog(shell);

		laD.create();
		// all selected pages are shown as default and are taken for
		// segmentation
		laD.setPageSelectionToSelectedPages(pages);

		int ret = laD.open();

		if (ret == IDialogConstants.OK_ID) {
			try {
				List<String> jobIds = Storage.getInstance().analyzeLayoutOnLatestTranscriptOfPages(laD.getPages(),
						laD.isDoBlockSeg(), laD.isDoLineSeg(), laD.isDoWordSeg(), false, false, laD.getJobImpl(), null);

				if (jobIds != null && mw != null) {
					logger.debug("started jobs: " + jobIds.size());
					String jobIdsStr = mw.registerJobsToUpdate(jobIds);
					Storage.getInstance().sendJobListUpdateEvent();
					mw.updatePageLock();

					DialogUtil.showInfoMessageBox(getShell(), jobIds.size() + " jobs started",
							jobIds.size() + " jobs started\nIDs:\n " + jobIdsStr);
				}
			} catch (Exception e) {
				mw.onError("Error", e.getMessage(), e);
			}
		}
	}

	private void addMenuItems4DocLevel(Menu contextMenu) {
		MenuItem addPage = new MenuItem(contextMenu, SWT.NONE);
		addPage.setText("Add new page(s)");
		addPage.setToolTipText("For importing several pages press 'ctrl' or 'shift' button");
		addPage.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				// mw.addPage();
				mw.addSeveralPages2Doc();
				reloadRemoteDoc();
				tv.getTree().redraw();
				reload();
				mw.getUi().getThumbnailWidget().reload();
				
			}

		});

		MenuItem syncDoc = new MenuItem(contextMenu, SWT.NONE);
		syncDoc.setText("Add local transcriptions");
		syncDoc.setToolTipText("Add local transcriptions for this document");
		syncDoc.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				mw.getDocSyncController().syncPAGEFilesWithLoadedDoc();
			}
		});
		
		MenuItem moveBack = new MenuItem(contextMenu, SWT.NONE);
		moveBack.setText("Revert to version(s) prior to your last (batch) job");
		moveBack.setToolTipText("Latest versions - derived from (batch) job - of the document swap places with their parents - use case is e.g. if layout recognition should be undone");
		moveBack.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (DialogUtil.showYesNoDialog(mw.getShell(), "Revert to previous version(s)", "Do you really want to revert to the version(s) before the last job was completed")!=SWT.YES) {
					return;
				}
				Thread thread = new Thread(){
				    public void run(){
				    	mw.revertVersions();
				    }
				};
			
				thread.start();
				
			}
		});
		
		MenuItem movePagesByFilelist = new MenuItem(contextMenu, 0);
		movePagesByFilelist.setText("Sort pages by filename list...");
		SWTUtil.onSelectionEvent(movePagesByFilelist, e -> mw.getDocSyncController().movePagesByFilelist());
	}

	private void addMenuItems4PageLevel(Menu contextMenu, String[] editStatusArray) {
		MenuItem tmp;
		
		/*
		 * now also allowed for current doc -> addMenuItems4BothLevels
		 */
//		Menu statusMenu = new Menu(contextMenu);
//		MenuItem statusMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
//		statusMenuItem.setText("Edit Status");
//		statusMenuItem.setMenu(statusMenu);
//		// statusMenuItem.setEnabled(false);
//
//		for (String editStatus : editStatusArray) {
//			tmp = new MenuItem(statusMenu, SWT.PUSH);
//			tmp.setText(editStatus);
//			tmp.addSelectionListener(new EditStatusMenuItemListener());
//			// tmp.setEnabled(true);
//		}
		
		MenuItem movePage = new MenuItem(contextMenu, SWT.CASCADE);
		movePage.setText("Move page(s) to");

		Menu subMoveMenu = new Menu(contextMenu);
		movePage.setMenu(subMoveMenu);

		MenuItem moveFront = new MenuItem(subMoveMenu, SWT.NONE);
		moveFront.setText("Beginning");

		MenuItem moveBack = new MenuItem(subMoveMenu, SWT.NONE);
		moveBack.setText("End");

		MenuItem moveSpecific = new MenuItem(subMoveMenu, SWT.NONE);
		moveSpecific.setText("Select position");

		MenuItem deletePage = new MenuItem(contextMenu, SWT.NONE);
		deletePage.setText("Delete page(s)");

		deletePage.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				int response = DialogUtil.showYesNoCancelDialog(mw.getShell(), "Delete page from server",
						"Are you sure you want to delete the selected page(s)? \nThis action cannot be undone.");
				if (response == SWT.YES) {
					tv.getSelection();
					List<TrpPage> selection = new ArrayList<TrpPage>();// =
																		// (List<TrpPage>)
																		// tv.getSelection();
					IStructuredSelection treeSelection = (IStructuredSelection) tv.getSelection();
					Iterator it = treeSelection.iterator();
					/*
					 * delete Pages: now only pages from one document can be
					 * deleted at the same time. No document must be chosen
					 */
					while (it.hasNext()) {
						Object o = it.next();
						if (o instanceof TrpPage) {
							TrpPage currPage = (TrpPage) o;
							//logger.debug("selected page " + currPage.getImgFileName());
							selection.add(currPage);
							tv.remove(currPage);
						} else if (o instanceof TrpDoc) {
							break;
						}

					}
					deletePages(selection);
					try {
						reloadRemoteDoc();
						// TODO: next method must be adapted to tree
						// tv.setDoc(Storage.getInstance().getDoc(), false);
						reload();
						mw.getUi().getThumbnailWidget().reload();
					} catch (IllegalArgumentException e) {
						logger.error(e.getMessage(), e);
					}

				}

			}

		});

		moveFront.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					movePages(getPageList(), 1);
					reloadRemoteDoc();
					reload();
					mw.getUi().getThumbnailWidget().reload();
					tv.getTree().redraw();

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});

		moveBack.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				int NPages = Storage.getInstance().getDoc().getNPages();

				// TODO for moving take care that only for one document at a
				// time moving is allowed
				try {
					movePages(getPageList(), NPages);
					reloadRemoteDoc();
					reload();
					mw.getUi().getThumbnailWidget().reload();
					tv.getTree().redraw();

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}

		});

		moveSpecific.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				Shell shell = new Shell(Display.getCurrent());
				shell.setLayout(new GridLayout(2, false));
				Text inputText = new Text(shell, SWT.BORDER);
				inputText.setText("");
				Button apply = new Button(shell, SWT.PUSH);
				apply.setText("Apply");
				shell.setLocation(Display.getCurrent().getCursorLocation());

				shell.pack();
				shell.open();

				apply.addListener(SWT.Selection, new Listener() {

					@Override
					public void handleEvent(Event event) {
						if (inputText.getText().isEmpty())
							return;

						int targetPage;
						targetPage = Integer.parseInt(inputText.getText());
						if (targetPage > Storage.getInstance().getDoc().getNPages() || targetPage < 1) {
							DialogUtil.showErrorMessageBox(getShell(), "Error", "Invalid position");
						}
						try {
							movePages(getPageList(), targetPage);
							reloadRemoteDoc();
							reload();
							mw.getUi().getThumbnailWidget().reload();
							tv.getTree().redraw();
						} catch (SessionExpiredException | ServerErrorException | ClientErrorException
								| IllegalArgumentException | NoConnectionException | IOException e) {
							logger.error(e.getMessage(), e);
						}

					}

				});

			}

		});

	}

	private void addMenuItems4BothLevels(Menu contextMenu) {
		MenuItem tmp;

		// Menu labelMenu = new Menu(contextMenu);
		// MenuItem labelMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		// labelMenuItem.setText("Edit Label");
		// labelMenuItem.setMenu(labelMenu);

		Menu layoutMenu = new Menu(contextMenu);
		MenuItem layoutMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		layoutMenuItem.setText("Layout Analysis");
		layoutMenuItem.setMenu(layoutMenu);

		// just dummy labels for testing
		// tmp = new MenuItem(labelMenu, SWT.None);
		// tmp.setText("Upcoming feature - cannot be set at at the moment");
		// tmp.addSelectionListener(new EditLabelMenuItemListener());
		// tmp.setEnabled(false);
		//
		// tmp = new MenuItem(labelMenu, SWT.None);
		// tmp.setText("GT");
		// tmp.addSelectionListener(new EditLabelMenuItemListener());
		// tmp.setEnabled(false);
		//
		// tmp = new MenuItem(labelMenu, SWT.None);
		// tmp.setText("eLearning");
		// tmp.addSelectionListener(new EditLabelMenuItemListener());
		// tmp.setEnabled(false);

		tmp = new MenuItem(layoutMenu, SWT.None);
		tmp.setText("Configure");
		// tmp.setEnabled(false);

		tmp.addListener(SWT.Selection, event -> {
			String pages = getPagesString();

			try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
		
		Menu statusMenu = new Menu(contextMenu);
		MenuItem statusMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		statusMenuItem.setText("Edit Status");
		statusMenuItem.setMenu(statusMenu);
		// statusMenuItem.setEnabled(false);
		statusCombo.setEnabled(true);

		for (String editStatus : EditStatus.getStatusListWithoutNew()) {
			tmp = new MenuItem(statusMenu, SWT.PUSH);
			tmp.setText(editStatus);
			tmp.addSelectionListener(new EditStatusMenuItemListener());
			// tmp.setEnabled(true);
		}

	}

	private String getPagesString() {
		String pages = "";
	
		IStructuredSelection treeSelection = (IStructuredSelection) tv.getSelection();
		Iterator it = treeSelection.iterator();
		/*
		 * delete Pages: now only pages from one document can be deleted at the
		 * same time. No document must be chosen
		 */
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpPage) {
				TrpPage currPage = (TrpPage) o;
				//logger.debug("selected page " + currPage.getPageNr());
				int selectedPageNr = currPage.getPageNr();
				String tmp = Integer.toString(selectedPageNr);
				pages += (pages.equals("") ? tmp : ",".concat(tmp));
			} else if (o instanceof TrpDocMetadata) {
				TrpDocMetadata currDoc = (TrpDocMetadata) o;
				pages = "1-" + currDoc.getNrOfPages();
				// for (int i = 0; i<currDoc.getNPages(); i++){
				// String tmp = Integer.toString(i+1);
				// pages += (pages.equals("")? tmp : ",".concat(tmp));
				// }
			}

		}
		return pages;
	}

	private List<TrpPage> getPageList() {
		List<TrpPage> pages = new ArrayList<TrpPage>();

		IStructuredSelection treeSelection = (IStructuredSelection) tv.getSelection();
		Iterator it = treeSelection.iterator();
		/*
		 * delete Pages: now only pages from one document can be deleted at the
		 * same time. No document must be chosen
		 */
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof TrpPage) {
				TrpPage currPage = (TrpPage) o;
				//logger.debug("selected page " + currPage.getPageNr());
				pages.add(currPage);
			} else if (o instanceof TrpDocMetadata) {
				//logger.debug("doc type page ");
				pages.addAll(Storage.getInstance().getDoc().getPages());
				
				// break;
			}

		}
		return pages;
	}

	/*
	 * right click listener for the transcript table for the latest transcript
	 * the new status can be set with the right click button and by choosing the
	 * new status
	 */
	class EditStatusMenuItemListener extends SelectionAdapter {

		public void widgetSelected(SelectionEvent event) {
			//logger.debug("You selected " + ((MenuItem) event.widget).getText());
			// System.out.println("You selected cont.1 " +
			// EnumUtils.fromString(EditStatus.class, ((MenuItem)
			// event.widget).getText()));

			String tmp = ((MenuItem) event.widget).getText();
			mw.changeVersionStatus(tmp, getPageList());
			//enableEdits(false);

			reload();
			mw.getUi().getThumbnailWidget().reload();

			tv.getTree().redraw();
			tv.getTree().deselectAll();
		}
	}

	/*
	 * right click listener for the transcript table for the latest transcript
	 * the new status can be set with the right click button and by choosing the
	 * new status
	 */
	// class EditLabelMenuItemListener extends SelectionAdapter {
	// public void widgetSelected(SelectionEvent event) {
	//
	// }
	// }

	public void setUrls(List<URL> urls, List<String> names) {
		this.urls = urls;
		this.names = names;
	}

	public void setTranscripts(List<TrpTranscriptMetadata> transcripts2) {
		this.transcripts = transcripts2;
	}

	public void reload() {
		tv.refresh(true);

//		addStatisticalNumbers();
	}
	
	public void totalReload(int colId) {
		if (!store.isLoggedIn()) {
			return;
		}
		
		this.colId = colId;

		if(store != null && store.getUser() != null && store.getUser().getRoleInCollection() != null){
			canManage = (store.getRoleOfUserInCurrentCollection().canManage() || store.isAdminLoggedIn()) ? true : false;
		}
		
		reloadRemoteDoc();
		
		docList = store.getDocList();
		tv.setInput(docList);
	
		expandCurrentDocument();
		
		updateSymbolicImgLabels();
		updateColors();

		//documentImageBtn.setEnabled(canManage);
		//showDocumentImageBtn.setEnabled(canManage);
		
		enableEditsDoc(false);
		enableEditsPage(false);
		
		tv.refresh(true);

	}
	
	private void reloadRemoteDoc() {
		if (store.isRemoteDoc()) {
			int pageIndex = store.getPageIndex();
			if (pageIndex < 0 || pageIndex >= store.getDoc().getNPages()) {
				pageIndex = 0;
			}
			logger.debug("reloading remote doc, pageIndex = "+pageIndex);
			mw.loadRemoteDoc(store.getDoc().getId(), colId, pageIndex);	
		}
		else {
			logger.debug("no remote doc loaded -> skipping doc reload");
		}
	}

	private void addStatisticalNumbers() {
		
		Storage storage = Storage.getInstance();
		TrpDoc doc = storage.getDoc();
		String msg = null;
		
		
		if (doc != null) {

			msg = "Loaded Document is : " + doc.getMd().getTitle() + " with ID " + doc.getMd().getDocId()+"\n";
			
			/*
			 * get all save actions for the loaded doc - the first on is the latest and is shown in the doc statistics
			 * all other have to be parsed and we try to get the latest save for each page if any
			 */
			try {
				List<TrpAction> actions = Storage.getInstance().getConnection().listActions(1, Storage.getInstance().getCollId(), Storage.getInstance().getDocId(), 1);
				for (TrpAction action : actions){
					msg += "<Last save: " + action.getTime() + " # page: " + action.getPageNr() + " # user: " + action.getUserName()+">"+"\n";
					break;
				}
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
			}
			
			int totalCollectionPages = 0;
			int totalCollectionLines = 0;
			int totalCollectionWords = 0;
			
			try {
				TrpTotalTranscriptStatistics collectionStats = storage.getConnection().getCollectionStats(colId);
				if (collectionStats.getNrOfTranscribedLines() != null){
					totalCollectionLines = collectionStats.getNrOfTranscribedLines();
				}
				if (collectionStats.getNrOfWordsInLines() != null){
					totalCollectionWords = collectionStats.getNrOfWordsInLines();
				}
				else if (collectionStats.getNrOfTranscribedWords() != null){
					totalCollectionWords = collectionStats.getNrOfTranscribedWords();
				}
				
			} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e) {
				logger.error(e.getMessage(), e);
			}
			for (TrpDocMetadata currDoc : storage.getDocList()){
				totalCollectionPages += currDoc.getNrOfPages();
			}

			msg += "Nr of pages : " + doc.getNPages() + " in doc / " + totalCollectionPages + " in collection"+"\n";

			int totalLinesTranscribed = 0;
			int totalWordsTranscribed = 0;
			try {
				TrpTotalTranscriptStatistics docStats = storage.getConnection().getDocStats(colId, doc.getMd().getDocId());
				if (docStats.getNrOfTranscribedLines() != null){
					totalLinesTranscribed = docStats.getNrOfTranscribedLines();
				}
				if (docStats.getNrOfWordsInLines() != null){
					totalWordsTranscribed = docStats.getNrOfWordsInLines();
				}
				else if (docStats.getNrOfTranscribedWords() != null){
					totalWordsTranscribed = docStats.getNrOfTranscribedWords();
				}
				
			} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e) {
				logger.error(e.getMessage(), e);
			}
	
			msg += "Nr. of lines transcribed: " + totalLinesTranscribed + " in doc / " + totalCollectionLines + " in collection"+"\n";
			msg += "Nr. of words transcribed: " + totalWordsTranscribed + " in doc / " + totalCollectionWords + " in collection"+"\n";
			
			DialogUtil.showInfoMessageBox(getShell(), "Statistics",  msg);


			// gallery.redraw();
		}

	}

	private void enableEditsDoc(boolean enable) {
		statusCombo.setEnabled(enable);
		addPage.setEnabled(enable);
		addTrans.setEnabled(enable);
		//sort.setEnabled(enable);
		revert.setEnabled(enable);
		movePage.setEnabled(false);
		deletePage.setEnabled(false);

	}
	
	private void enableEditsPage(boolean enable) {
		statusCombo.setEnabled(enable);
		movePage.setEnabled(enable);
		deletePage.setEnabled(enable);
		addPage.setEnabled(false);
		addTrans.setEnabled(false);
		//sort.setEnabled(false);
		revert.setEnabled(false);

	}
	
	private void updateTable(DocumentDataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DocumentDataSelectionEntry> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DocumentDataSelectionEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}

	public Button getCreateThumbs() {
		return createThumbs;
	}
	
	public DataSetMetadata getSampleSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return sampleDocMap;
	}
	
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
							tmd = t;
							break;
					}
					pages++;
					lines += tmd.getNrOfLines();
					words += tmd.getNrOfWordsInLines();
				}
				
			
		}
		return new DataSetMetadata(pages, lines, words);
	}
	
	public void setDefaultStatistics() {
		
		if (Storage.getInstance().getDoc() != null) {
			Storage store = Storage.getInstance();
			if(store != null && store.getUser() != null && store.getUser().getRoleInCollection() != null){
				canManage = (store.getRoleOfUserInCurrentCollection().canManage() || store.isAdminLoggedIn()) ? true : false;
			}
		} 
	}

	public void addListener(int selection, Listener listener) {
		//logger.debug("add double click listener");
		shell.addListener(selection, listener);
	}
	
	public Shell getShell() {
		return shell;
	}

}
