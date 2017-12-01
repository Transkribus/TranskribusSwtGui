package eu.transkribus.swt.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;
import java.util.Iterator;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.batik.dom.GenericEntityReference;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
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
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTotalTranscriptStatistics;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProviderExtended;
import eu.transkribus.swt_gui.la.LayoutAnalysisDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocumentManager extends Dialog {
	protected final static Logger logger = LoggerFactory.getLogger(DocumentManager.class);

	protected Composite groupComposite;
	protected Composite labelComposite;

	Combo labelCombo, statusCombo;

	Composite editCombos;

	protected Label statisticLabel;
	protected Label pageNrLabel;
	protected Label totalTranscriptsLabel;
	protected Label totalWordTranscriptsLabel;
	
	protected Label docLabel, collLabel;

	protected GalleryItem group;

	protected Button reload, showOrigFn, createThumbs, startLA;
	protected Button collectionImageBtn, documentImageBtn;
	protected Button showCollectionImageBtn, showDocumentImageBtn;

	protected List<URL> urls;
	protected List<String> names = null;

	protected List<TrpTranscriptMetadata> transcripts;

	protected List<Integer> nrTranscribedLines;

	protected AbstractGridGroupRenderer groupRenderer;

	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProviderExtended labelProv;
	private Composite buttonComp, buttonComp2;
	private Canvas previewLbl;

	private int colId;
	private List<TrpDocMetadata> docList;

	static int thread_counter = 0;

	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	static final Color lightBlue = new Color(Display.getCurrent(), 0, 140, 255);
	
	FontDescriptor boldDescriptor = FontDescriptor.createFrom(Display.getCurrent().getSystemFont()).setStyle(SWT.BOLD);
	Font boldFont = boldDescriptor.createFont(Display.getCurrent());
	
	FontDescriptor normalDescriptor = FontDescriptor.createFrom(Display.getCurrent().getSystemFont()).setStyle(SWT.NORMAL);
	Font normalFont = normalDescriptor.createFont(Display.getCurrent());

	Image image;

	static GalleryItem[] draggedItem;
	static int[] originalItemIndex;

	Shell shell;

	public Shell getShell() {
		return shell;
	}

	TrpDocMetadata docMd;
	TrpMainWidget mw;

	Menu contextMenu;

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {

		if (shell != null) {
			shell.setMinimumSize(600, 600);
			shell.setSize(1000, 600);
			SWTUtil.centerShell(shell);

			shell.open();
			shell.layout();

			addStatisticalNumbers();

			expandCurrentDocument();

			Display display = shell.getDisplay();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} else {
			logger.debug("Sehll is null????????");
		}

		return null;
	}

	private void expandCurrentDocument() {
		for (TreeItem ti : tv.getTree().getItems()) {
			TrpDocMetadata md = (TrpDocMetadata) ti.getData();
			// logger.debug("md " + md);
			// logger.debug("current Doc " +
			// Storage.getInstance().getDoc().getMd());
			if (Storage.getInstance().getDoc() != null && md.compareTo(Storage.getInstance().getDoc().getMd()) == 0) {
				tv.expandToLevel(md, 1);
				if (ti.getItems().length > 0) {
					TreeItem[] childs = ti.getItems();
					for (TreeItem child : ti.getItems()) {
						TrpPage p = (TrpPage) child.getData();
						tv.getTree().setSelection(child);
						try {
							image = ImgLoader.load(p.getUrl());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						previewLbl.redraw();
						previewLbl.update();
						break;
					}
				}
				updateColors();
				updateSymbolicImgLabels();
			}
		}

	}

	public DocumentManager(Shell parent, int style, TrpMainWidget mw, int colId) {
		super(parent.getShell(), style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));

		this.colId = colId;

		this.mw = mw;

		// if (Storage.getInstance().getDoc() == null){
		// return;
		// }

		docList = Storage.getInstance().getDocList();

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
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));

		groupComposite = new Composite(sash, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.makeColumnsEqualWidth = true;
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		groupComposite.setLayoutData(gridData);

		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		statisticLabel = new Label(labelComposite, SWT.TOP);
		if (Storage.getInstance().getDoc() != null) {
			statisticLabel.setText("Loaded Document is " + docMd.getTitle() + " with ID " + docMd.getDocId());
		} else {
			statisticLabel.setText("Currently no document loaded in Transkribus");
		}
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		editCombos = new Composite(groupComposite, SWT.NONE);
		editCombos.setLayout(new GridLayout(2, true));
		editCombos.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		statusCombo = initComboWithLabel(editCombos, "Edit status: ", SWT.DROP_DOWN | SWT.READ_ONLY);
		statusCombo.setItems(EditStatus.getStatusListWithoutNew());
		statusCombo.setEnabled(false);
		statusCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug(statusCombo.getText());
				mw.changeVersionStatus(statusCombo.getText(), getPageList());
				reload();

				tv.getTree().redraw();
			}
		});

		Label la = new Label(editCombos, SWT.CENTER);
		la.setText("Layout Analysis");
		startLA = new Button(editCombos, SWT.PUSH);
		startLA.setText("Setting up");
		startLA.setEnabled(false);
		startLA.addListener(SWT.Selection, event -> {
			String pages = getPagesString();
			try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// Button showFn = new Button(editCombos, SWT.CHECK);
		// showFn.setText("Show filename in label");
		// showFn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		// showFn.setSelection(false);
		// showFn.addSelectionListener(new SelectionAdapter() {
		// @Override public void widgetSelected(SelectionEvent e) {
		// //TODO: show filenames in tree - could be the default
		// tv.refresh();
		// //tw.setTHUMB_WIDTH(Math.max(tw.getMaxWidth(), shell.getSize().x/3));
		// }
		// });

		Composite btns = new Composite(container, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));

		reload = new Button(btns, SWT.PUSH);
		reload.setToolTipText("Reload thumbs");
		reload.setImage(Images.REFRESH);
		reload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});

		createTreeViewerTab(sash);

		contextMenu = new Menu(tv.getTree());
		tv.getTree().setMenu(contextMenu);
		// at the moment not enabled because not the total functionality to edit
		// status, label is available
		// contextMenu.setEnabled(false);

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

				setMenu(contextMenu, item);
			}
		});

		sash.setWeights(new int[] { 20, 80 });

		addListeners();

		shell.pack();

	}

	private void setMenu(Menu menu, TreeItem item) {
		if (Storage.getInstance().getDoc() == null) {
			return;
		}

		int level = getLevelOfItem(item);

		MenuItem[] items = menu.getItems();
		for (MenuItem i : items) {
			i.dispose();
		}

		switch (level) {
		// document level
		case 0:
			// allow only for loaded document
			if (((TrpDocMetadata) item.getData()).compareTo(Storage.getInstance().getDoc().getMd()) == 0) {
				addMenuItems4BothLevels(menu);
				addMenuItems4DocLevel(menu);

			}
			break;
		case 1:
			// allow only for loaded pages
			if (((TrpPage) item.getData()).getDocId() == Storage.getInstance().getDoc().getId()) {
				addMenuItems4BothLevels(menu);
				addMenuItems4PageLevel(menu, EditStatus.getStatusListWithoutNew());
			}
			addChooseImageMenuItems(menu);
			break;
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

				TrpPage p = (TrpPage) ti.getData();
				if(p.getDocId() == docMd.getDocId()){
					docMd.setPageId(p.getPageId());
					
					ti.getParentItem().setData(docMd);
					Storage.getInstance().getConnection().updateDocMd(colId, docMd.getDocId(), docMd);
					Storage.getInstance().reloadCurrentDocument(colId);
				}
				break;
			}
		} catch (SessionExpiredException | IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updateColors();
		updateSymbolicImgLabels();
		tv.getTree().redraw();

	}
	
	private void addSymbolicCollectionImage() {
		try {
			for (TreeItem ti : tv.getTree().getSelection()) {

				TrpPage p = (TrpPage) ti.getData();
				TrpCollection colMd = Storage.getInstance().getDoc().getCollection();
				colMd.setPageId(new Integer(p.getPageId()));
				Storage.getInstance().getConnection().updateCollectionMd(colMd);
				Storage.getInstance().reloadCollections();
				break;
			}
		} catch (SessionExpiredException | IllegalArgumentException | ServerErrorException | NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		updateColors();
		updateSymbolicImgLabels();
		tv.getTree().redraw();
		
		// Storage.getInstance().getDoc().getCollection().setColImgUrl();
		// mw.addSeveralPages2Doc();
		// try {
		// Storage.getInstance().reloadCurrentDocument(colId);
		// } catch (SessionExpiredException | IllegalArgumentException |
		// NoConnectionException | IOException
		// | NullValueException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// reload();
		// mw.getUi().getThumbnailWidget().reload();
		// tv.getTree().redraw();

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
		docSash2.setLayout(new GridLayout(3, false));

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
		// for (TreeItem ti : tv.getTree().getItems()){
		// TrpDocMetadata md = (TrpDocMetadata) ti.getData();
		// //logger.debug("md " + md);
		// //logger.debug("current Doc " +
		// Storage.getInstance().getDoc().getMd());
		// if (Storage.getInstance().getDoc() != null &&
		// md.compareTo(Storage.getInstance().getDoc().getMd()) == 0){
		// tv.expandToLevel(md, 1);
		// if (ti.getItems().length > 0){
		// TreeItem[] childs = ti.getItems();
		// for (TreeItem child : ti.getItems()){
		// tv.getTree().select(child);
		// break;
		// }
		// }
		//
		// }
		// }
		// tv.expandToLevel(Storage.getInstance().getDoc().getMd(), 2);

		// tv.expandToLevel(tv.getTree().getItem(0),
		// AbstractTreeViewer.ALL_LEVELS);

		buttonComp2 = new Composite(docSash2, SWT.NONE);
		buttonComp2.setLayout(new GridLayout(1, true));
		// buttonComp2.setLayoutData(new GridData(GridData.CENTER,
		// GridData.CENTER, false, false));

		documentImageBtn = new Button(buttonComp2, SWT.PUSH);
		documentImageBtn.setImage(Images.ADD);
		documentImageBtn.setText("Choose Symbolic Image for Document");
		documentImageBtn.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		documentImageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				addSymbolicDocImage();

			}

		});
		
	
		showDocumentImageBtn = new Button(buttonComp2, SWT.PUSH);
		showDocumentImageBtn.setImage(Images.IMAGE);
		showDocumentImageBtn.setText("Show Symbolic Image for Document");
		showDocumentImageBtn.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		showDocumentImageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if (image != null) {
					image.dispose();
					image = null;
				}
				//show the symbolic image of the loaded doc
				try {
					if (Storage.getInstance().getDoc() != null && Storage.getInstance().getDoc().getMd().getUrl() != null){
						image = ImgLoader.load(Storage.getInstance().getDoc().getMd().getUrl());	
					}
					previewLbl.redraw();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}

		});
		
		docLabel = new Label(buttonComp2, SWT.NONE);
		docLabel.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		docLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		
		updateSymbolicImgLabels();
		
		collLabel = new Label(buttonComp2, SWT.NONE);
		collLabel.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		collLabel.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN));

		updateSymbolicImgLabels();
			

		collectionImageBtn = new Button(buttonComp2, SWT.PUSH);
		collectionImageBtn.setImage(Images.ADD);
		collectionImageBtn.setText("Choose Symbolic Image for Collection");
		collectionImageBtn.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		collectionImageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				addSymbolicCollectionImage();

			}

		});

		showCollectionImageBtn = new Button(buttonComp2, SWT.PUSH);
		showCollectionImageBtn.setImage(Images.IMAGE);
		showCollectionImageBtn.setText("Show Symbolic Image for Collection");
		showCollectionImageBtn.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER, true, false));
		showCollectionImageBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				if (image != null) {
					image.dispose();
					image = null;
				}
				//show the symbolic image of the loaded collection
				try {
					if (Storage.getInstance().getCollection(colId) != null && Storage.getInstance().getCollection(colId).getUrl() != null){
						image = ImgLoader.load(Storage.getInstance().getCollection(colId).getUrl());
					}
					previewLbl.redraw();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

			}

		});

		// buttonComp = new Composite(docSash2, SWT.NONE);
		// buttonComp.setLayout(new GridLayout(1, false));

		previewLbl = new Canvas(docSash2, SWT.NONE);
		// previewLbl.setSize(300, 440);
		GridData gd2 = new GridData(GridData.FILL, GridData.FILL, true, true);
		// gd2.heightHint = 400;
		// gd2.widthHint = 300;
		// gd2.minimumHeight = 440;
		// gd2.minimumWidth = 400;
		previewLbl.setLayoutData(gd2);

		previewLbl.addPaintListener(new PaintListener() {
			public void paintControl(final PaintEvent event) {
				if (image != null) {
					float ratio = (float) image.getBounds().height / image.getBounds().width;
					ImageData data = image.getImageData();
					// logger.debug("image width " + image.getBounds().width);
					// logger.debug("image height " + image.getBounds().height);
					// logger.debug("image ratio " + ratio);

					int h = (int) (250 * ratio);
					// logger.debug("new height " + h);
					// data = data.scaledTo(300,h);
					//
					// Image anotherImage = new Image(shell.getDisplay(), data);
					// event.gc.drawImage(anotherImage, 10, 10);
					// image.dispose();
					event.gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, 250, h);
				}
			}
		});

		updateColors();
		updateSymbolicImgLabels();

	}

	private void updateSymbolicImgLabels() {

		if (Storage.getInstance().getDoc() != null){
			TrpDoc currDoc = Storage.getInstance().getDoc();
			if (currDoc.getMd().getPageId() != null){
				for (TrpPage p : currDoc.getPages()){
					if (currDoc.getMd().getPageId().equals(p.getPageId())){
						docLabel.setText("Loaded doc: " + p.getDocId() + "\nwith symbolic image: \n" + p.getImgFileName() );
					}
				}
			}	
		}
		else{
			docLabel.setText("Currently no document loaded.");
		}
		
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
						if (Integer.valueOf(page.getPageId()).equals(currCol.getPageId())){
							//logger.debug("collection page for symbolic image found");
							collLabel.setText("Collection " + colId + "\n with symbolic image: \n" + page.getImgFileName() );
							return;
						}
							
					}
							
				}
			}	
		}
		buttonComp2.layout();
	}

	private void addListeners() {

		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				//logger.debug("selection changed");
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object o = selection.getFirstElement();
				int currDocId = 0;
				if (o instanceof TrpPage) {
					TrpPage p = (TrpPage) o;
					try {
						if (image != null) {
							image.dispose();
							image = null;
						}
						// image = ImgLoader.load(p.getThumbUrl());
						image = ImgLoader.load(p.getUrl());
						previewLbl.redraw();
						currDocId = p.getDocId();

						// if (previewLbl.getImage() != null) {
						// previewLbl.getImage().dispose();
						// }
						// previewLbl.setImage(image);
					} catch (IOException e) {
						logger.error("Could not load image", e);
					}
				} else if (o instanceof TrpDocMetadata) {
					if (image != null) {
						image.dispose();
						image = null;
					}
					previewLbl.redraw();
					currDocId = ((TrpDocMetadata) o).getDocId();
					// if (previewLbl.getImage() != null) {
					// previewLbl.getImage().dispose();
					// }
					// previewLbl.setImage(null);
				}
				updateColors();
				updateSymbolicImgLabels();
				enableEdits(currDocId == Storage.getInstance().getDocId());

			}
		});

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				int currDocId = 0;
				if (o instanceof TrpDocMetadata) {
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
					TrpMainWidget.getInstance().showLocation(loc);
					currDocId = loc.docId;
					expandCurrentDocument();

				} else if (o instanceof TrpPage) {
					TrpPage p = (TrpPage) o;
					TrpLocation loc = new TrpLocation();
					loc.collId = colId;
					loc.docId = p.getDocId();
					loc.pageNr = p.getPageNr();
					TrpMainWidget.getInstance().showLocation(loc);
					currDocId = loc.docId;

				}
				docMd = Storage.getInstance().getDoc().getMd();
				// enableEdits(currDocId == Storage.getInstance().getDocId());
				updateColors();
				updateSymbolicImgLabels();
				addStatisticalNumbers();
			}

		});

		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateColors();
				enableEdits(true);
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
				if (colMd != null && colMd.getPageId() != null && Integer.valueOf(page.getPageId()).equals(colMd.getPageId())){
					//logger.debug("symbolic image found for collection with ID: " + colMd.getColId());
					child.setFont( boldFont );
					child.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_CYAN));
				}
				
				else if (doc != null && doc.getPageId() != null && Integer.valueOf(page.getPageId()).equals(doc.getPageId())) {
					//logger.debug("symbolic image found for document with ID: " + doc.getDocId());
//					GC gc = new GC(cR_Dhild.getDisplay().getActiveShell());
//					gc.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//					gc.setBackground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//					gc.setLineWidth(10);
//					child.drawLine(child.getBounds().x, child.getBounds().y, child.getBounds().width, child.getBounds().y);
//					gc.drawRectangle(child.getBounds());
//					gc.dispose();
					child.setFont( boldFont );
					child.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
				}
				else {
					child.setFont(normalFont);
					child.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
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
		for (TrpPage page : selection) {
			deletePage(page);
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

		if (selection.get(0).getPageNr() > toPageNr) {

			int j = 0;
			for (int i = selection.size() - 1; i >= 0; i--) {
				movePage(selection.get(i).getPageNr() + j, toPageNr);
				j++;

			}

		} else if (selection.get(0).getPageNr() < toPageNr) {
			int i = 0;
			for (TrpPage page : selection) {
				movePage(page.getPageNr() - i, toPageNr);
				i++;
			}
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
						// .reloadCurrentDocument(colId);
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				try {
					Storage.getInstance().reloadCurrentDocument(colId);
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException | IOException
						| NullValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				reload();
				mw.getUi().getThumbnailWidget().reload();
				tv.getTree().redraw();
			}

		});

		MenuItem syncDoc = new MenuItem(contextMenu, SWT.NONE);
		syncDoc.setText("Add local transcriptions");
		syncDoc.setToolTipText("Add several transcriptions for this document, names must correspond to the filenames!");
		syncDoc.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				mw.syncWithLocalDoc();
			}

		});
	}

	private void addMenuItems4PageLevel(Menu contextMenu, String[] editStatusArray) {
		MenuItem tmp;
		
		Menu statusMenu = new Menu(contextMenu);
		MenuItem statusMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		statusMenuItem.setText("Edit Status");
		statusMenuItem.setMenu(statusMenu);
		// statusMenuItem.setEnabled(false);

		for (String editStatus : editStatusArray) {
			tmp = new MenuItem(statusMenu, SWT.PUSH);
			tmp.setText(editStatus);
			tmp.addSelectionListener(new EditStatusMenuItemListener());
			// tmp.setEnabled(true);
		}
		
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
						Storage.getInstance().reloadCurrentDocument(colId);
						// TODO: next method must be adapted to tree
						// tv.setDoc(Storage.getInstance().getDoc(), false);
						reload();
						mw.getUi().getThumbnailWidget().reload();
					} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException | IOException
							| NullValueException e) {
						e.printStackTrace();
					}

				}

			}

		});

		moveFront.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {

				try {

					movePages(getPageList(), 1);
					Storage.getInstance().reloadCurrentDocument(colId);
					reload();
					mw.getUi().getThumbnailWidget().reload();
					tv.getTree().redraw();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
					Storage.getInstance().reloadCurrentDocument(colId);
					reload();
					mw.getUi().getThumbnailWidget().reload();
					tv.getTree().redraw();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
							Storage.getInstance().reloadCurrentDocument(colId);
							reload();
							mw.getUi().getThumbnailWidget().reload();
							tv.getTree().redraw();
						} catch (SessionExpiredException | ServerErrorException | ClientErrorException
								| IllegalArgumentException | NoConnectionException | IOException
								| NullValueException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
		tmp.setText("Setting Up");
		// tmp.setEnabled(false);

		tmp.addListener(SWT.Selection, event -> {
			String pages = getPagesString();

			try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

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
				// logger.debug("selected page " + currPage.getPageNr());
				pages.add(currPage);
			} else if (o instanceof TrpDocMetadata) {
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

		// addStatisticalNumbers();
	}
	
	public void totalReload(int colId) {
		this.colId = colId;

		// if (Storage.getInstance().getDoc() == null){
		// return;
		// }

		docList = Storage.getInstance().getDocList();
		tv.setInput(docList);
		
		addStatisticalNumbers();
		
		expandCurrentDocument();
		
		updateSymbolicImgLabels();
		updateColors();
		
		tv.refresh(true);

		// addStatisticalNumbers();
	}

	private void addStatisticalNumbers() {

		Storage storage = Storage.getInstance();
		TrpDoc doc = storage.getDoc();

		if (doc != null) {
			if (statisticLabel != null && !statisticLabel.isDisposed()) {
				statisticLabel.dispose();
			}
			if (pageNrLabel != null && !pageNrLabel.isDisposed()) {
				pageNrLabel.dispose();
			}
			if (totalTranscriptsLabel != null && !totalTranscriptsLabel.isDisposed()) {
				totalTranscriptsLabel.dispose();
			}
			if (totalWordTranscriptsLabel != null && !totalWordTranscriptsLabel.isDisposed()) {
				totalWordTranscriptsLabel.dispose();
			}

			statisticLabel = new Label(labelComposite, SWT.TOP);
			statisticLabel
					.setText("Loaded Document is " + doc.getMd().getTitle() + " with ID " + doc.getMd().getDocId());
			statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			int totalCollectionPages = 0;
			int totalCollectionLines = 0;
			int totalCollectionWords = 0;
			
			try {
				TrpTotalTranscriptStatistics collectionStats = storage.getConnection().getCollectionStats(colId);
//				logger.debug("coll stats " + collectionStats.getNrOfTranscribedLines());
//				logger.debug("coll stats " + collectionStats.getNrOfWordsInLines());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (TrpDocMetadata currDoc : storage.getDocList()){
				totalCollectionPages += currDoc.getNrOfPages();
			}

			pageNrLabel = new Label(labelComposite, SWT.NONE);
			pageNrLabel.setText("Nr of pages : " + doc.getNPages() + " in doc / " + totalCollectionPages + " in collection");

			int totalLinesTranscribed = 0;
			int totalWordsTranscribed = 0;
			if (doc.getMd().getNrOfTranscribedLines() != null){
				totalLinesTranscribed = doc.getMd().getNrOfTranscribedLines();
			}
			if (doc.getMd().getNrOfWordsInLines() != null){
				totalWordsTranscribed = doc.getMd().getNrOfWordsInLines();
			}
			else if (doc.getMd().getNrOfTranscribedWords() != null){
				totalWordsTranscribed = doc.getMd().getNrOfTranscribedWords();
			}

//			for (int i = 0; i < doc.getTranscripts().size(); i++) {
//				TrpTranscriptMetadata tmd;
//				tmd = doc.getTranscripts().get(i);
//
//				totalLinesTranscribed += tmd.getNrOfTranscribedLines();
//				totalWordsTranscribed += tmd.getNrOfWordsInLines();
//			}

			totalTranscriptsLabel = new Label(labelComposite, SWT.None);
			totalTranscriptsLabel.setText("Nr. of lines trancribed: " + totalLinesTranscribed + " in doc / " + totalCollectionLines + " in collection");
						
			totalWordTranscriptsLabel = new Label(labelComposite, SWT.None);
			totalWordTranscriptsLabel.setText("Nr. of words trancribed: " + totalWordsTranscribed + " in doc / " + totalCollectionWords + " in collection");

			groupComposite.layout(true, true);

			// gallery.redraw();
		}

	}

	private void enableEdits(boolean enable) {
		statusCombo.setEnabled(enable);
		// labelCombo.setEnabled(enable);
		startLA.setEnabled(enable);

		// contextMenu.setEnabled(enable);

	}

	public Button getCreateThumbs() {
		return createThumbs;
	}

	public void addListener(int selection, Listener listener) {
		//logger.debug("add double click listener");
		shell.addListener(selection, listener);
	}

}
