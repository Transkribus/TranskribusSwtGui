package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
<<<<<<< HEAD
=======
import org.eclipse.jface.viewers.SelectionChangedEvent;
>>>>>>> refs/remotes/origin/master
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.KwsDocHit;
import eu.transkribus.core.model.beans.KwsHit;
import eu.transkribus.core.model.beans.KwsPageHit;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt_canvas.pagination_table.IPageLoadMethods;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.ComboInputDialog;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.LabeledCombo;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.pagination_tables.DocTableWidgetPagination;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class DocSearchDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(DocSearchDialog.class);
	
	DocTableWidget docWidget;
	DocTableWidgetPagination docWidgetPaged;
	
	LabeledText documentId, title, description, author, writer;
	LabeledCombo collection;
	Button exactMatch, caseSensitive;
	
	Button findBtn;
	Label infoLabel;

	LabeledText kwsDocId;
	LabeledCombo kwsCollection;
	
	private CTabFolder tabFolder;
	private CTabItem searchItem, kwsItem;
	private TreeViewer treeViewer;
	private Tree tree;
	private Button kwsBtn;
	private LabeledText term;
	private Slider confSlider;

	private Label kwsInfoLabel;

	private Text confValueTxt;
	
	public final static ColConfig TYPE_COL = new ColConfig("Type", 100);
	public final static ColConfig DOC_ID_COL = new ColConfig("Doc ID", 60);
	public final static ColConfig TITLE_COL = new ColConfig("Title", 200);
	public final static ColConfig PAGE_NR_COL = new ColConfig("Page Nr.", 100);
	public final static ColConfig HITS_COL = new ColConfig("Hits", 60);
	public final static ColConfig SCORE_COL = new ColConfig("Score", 100);
	public final static ColConfig LINE_ID_COL = new ColConfig("Line ID", 60);

	public final static ColConfig[] COLUMNS = new ColConfig[] { TYPE_COL, DOC_ID_COL, TITLE_COL, PAGE_NR_COL, HITS_COL, SCORE_COL, LINE_ID_COL };
	
	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public DocSearchDialog(Shell parentShell) {
		super(parentShell);
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	@Override protected boolean isResizable() {
		return true;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
//		c.setLayout(new FillLayout());
		c.setLayout(new GridLayout());
		
		tabFolder = new CTabFolder(c, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		SashForm sf = new SashForm(tabFolder, SWT.VERTICAL);
		sf.setLayout(new GridLayout());
		
		Composite facetsC = new Composite(sf, 0);
		facetsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		facetsC.setLayout(new GridLayout());
		
		TraverseListener tl = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					searchThemFuckingDocuments();
				}
			}
		};
		
		collection = new LabeledCombo(facetsC, "Restrict search to collection: ");
		collection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		documentId = new LabeledText(facetsC, "Doc-ID: ");
		documentId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		documentId.text.addTraverseListener(tl);
		
		title = new LabeledText(facetsC, "Title: ");
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		title.text.addTraverseListener(tl);
		
		description = new LabeledText(facetsC, "Description");
		description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		description.text.addTraverseListener(tl);
		
		author = new LabeledText(facetsC, "Author: ");
		author.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		author.text.addTraverseListener(tl);
		
		writer = new LabeledText(facetsC, "Writer: ");
		writer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		writer.text.addTraverseListener(tl);
		
		exactMatch = new Button(facetsC, SWT.CHECK);
		exactMatch.setText("Exact match of keywords ");
		
		caseSensitive = new Button(facetsC, SWT.CHECK);
		caseSensitive.setText("Case sensitve search ");		
		
		findBtn = new Button(facetsC, SWT.PUSH);
		findBtn.setText("Find Documents");
		findBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		findBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				searchThemFuckingDocuments();
				

			}
		});
		
		infoLabel = new Label(facetsC, 0);
		infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		docWidgetPaged = new DocTableWidgetPagination(sf, 0, 50, new IPageLoadMethods<TrpDocMetadata>() {
			Storage store = Storage.getInstance();
			
			@Override public int loadTotalSize() {
				int N = 0;
				
				int colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return 0;
				}
								
				if (store.isLoggedIn()) {
					try {
						N = store.getConnection().countFindDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection());
						logger.debug("N search docs = "+N);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return N;
			}
			
			@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				List<TrpDocMetadata> docs = new ArrayList<>();
				
				int colId = getColId();				
				Integer docid = getDocId();
				if (!documentId.txt().isEmpty() && docid == null) {
					return docs;
				}
				
				if (store.isLoggedIn()) {
					try {
						docs = store.getConnection().findDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						logger.debug("search docs pagesize = "+docs.size());
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
				}
				
				return docs;
			}
		});
				
		docWidgetPaged.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		IDoubleClickListener openSelectedDocListener = new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.isEmpty())
					return;
				
				openDocument((TrpDocMetadata) sel.getFirstElement());
			}
		};		
		docWidgetPaged.getTableViewer().addDoubleClickListener(openSelectedDocListener);
		
		if (false) {
		docWidget = new DocTableWidget(c, 0);
		docWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docWidget.getTableViewer().addDoubleClickListener(openSelectedDocListener);
		}
		
		sf.setWeights(new int[]{55, 45});
//		sf.setWeights(new int[]{facetsC.computeSize(SWT.DEFAULT, SWT.DEFAULT).y, docWidgetPaged.computeSize(SWT.DEFAULT, SWT.DEFAULT).y});

		searchItem = createCTabItem(tabFolder, sf, "Search");
		
		SashForm kwsSf = new SashForm(tabFolder, SWT.VERTICAL);
		kwsSf.setLayout(new GridLayout());
		
		Composite kwsC = new Composite(kwsSf, 0);
		kwsC.setLayoutData(new GridData(GridData.FILL_BOTH));
		kwsC.setLayout(new GridLayout());
		
		TraverseListener tl2 = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					spotThemFuckingKeyWords();
				}
			}
		};
		
		kwsCollection = new LabeledCombo(kwsC, "Restrict search to collection: ");
		kwsCollection.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		kwsDocId = new LabeledText(kwsC, "Doc-ID: ");
		kwsDocId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		kwsDocId.text.addTraverseListener(tl2);
		
		term = new LabeledText(kwsC, "Search term: ");
		term.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		term.text.addTraverseListener(tl2);
		
		Composite sliderComp = new Composite(kwsC, SWT.NONE);
		sliderComp.setLayout(new GridLayout(3, false));
		sliderComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label sliderLabel = new Label(sliderComp, SWT.NONE);
		sliderLabel.setText("Confidence:");
		confValueTxt = new Text(sliderComp, SWT.NONE);
		confValueTxt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		confValueTxt.setEnabled(false);
		confSlider = new Slider(sliderComp, SWT.HORIZONTAL);
		confSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		confSlider.setMaximum(109);
		confSlider.setThumb(10);
		confSlider.setMinimum(0);
		confSlider.setSelection(50);

		confValueTxt.setText(""+confSlider.getSelection());
		
		confSlider.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.NONE){
					confValueTxt.setText(""+confSlider.getSelection());
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				return;				
			}
		});
		
		treeViewer = new TreeViewer(kwsC, SWT.FULL_SELECTION | SWT.MULTI);
		treeViewer.setContentProvider(new KwsTreeContentProvider());
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tree = treeViewer.getTree();
		
		treeViewer.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object el = selection.getFirstElement();
				logger.debug("double click on element: "+el);
				TrpLocation loc;
				if (el instanceof KwsDocHit) {
					loc = new TrpLocation();
					KwsDocHit h = ((KwsDocHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
				} else if (el instanceof KwsPageHit) {
					loc = new TrpLocation();
					KwsPageHit h = ((KwsPageHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
					loc.pageNr = h.getPageNr();					
				} else if (el instanceof KwsHit){
					loc = new TrpLocation();
					KwsHit h = ((KwsHit)el);
					loc.collectionId = h.getColId();
					loc.docId = h.getDocId();
					loc.pageNr = h.getPageNr();	
					loc.shapeId = h.getLineId();
				} else {
					loc = null;
				}
				TrpMainWidget.getInstance().showLocation(loc);
			}
		});
		
		kwsItem = createCTabItem(tabFolder, kwsSf, "KWS");
		
		kwsBtn = new Button(kwsC, SWT.PUSH);
		kwsBtn.setText("Find Documents");
		kwsBtn.setImage(Images.getOrLoad("/icons/find.png"));
		
		kwsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				spotThemFuckingKeyWords();
			}
		});
		
		kwsInfoLabel = new Label(kwsC, 0);
		kwsInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		updateCollections();
		initCols();
		
		return c;
	}
	
	void openDocument(TrpDocMetadata md) {
		
//		TrpDocMetadata md = docWidgetPaged.getFirstSelected();
		
//		TrpDocMetadata md = docWidget.getSelectedDocument();
		
		if (md!=null) {
			logger.debug("md = "+md);
			
			int docId = md.getDocId();
			logger.debug("Loading doc with id: "+docId);
			
			int colId = 0;
			if (md.getColList().isEmpty()) {
				DialogUtil.showMessageBox(getShell(), "Error loading document", 
						"Collection list is empty - should not happen here!", SWT.ICON_ERROR);
				
				logger.error("Collection list is empty - should not happen here!");
				return;
			}
			if (md.getColList().size() == 1)
				colId = md.getColList().get(0).getColId();
			else {
				List<String> items = new ArrayList<>();
				for (TrpCollection c : md.getColList()) {
					items.add(c.getColName());
				}
				
				ComboInputDialog cd = 
						new ComboInputDialog(getShell(), "Select collection to load document from: ", items.toArray(new String[0]));
				
				if (cd.open() != IDialogConstants.OK_ID) {
					return;
				}

				logger.debug("selected index: "+cd.getSelectedIndex());
				
				TrpCollection coll = md.getColList().get(cd.getSelectedIndex());
				colId = coll.getColId();
			}
			
			logger.debug("loading from collection id: "+colId);
			
			TrpMainWidget mw = TrpMainWidget.getInstance(); 
			// select collection in DocOverviewWidget
//			mw.getUi().getDocOverviewWidget().
			
			mw.getUi().getDocOverviewWidget().clearCollectionFilter();
			mw.getUi().getDocOverviewWidget().setSelectedCollection(colId, true);
			
			// select page of document in doc-table:
			mw.getUi().getDocOverviewWidget().getDocTableWidget().loadPage("docId", docId, false);

			TrpMainWidget.getInstance().loadRemoteDoc(docId, colId);
		}
		
	}
	
	void updateCollections() {
		Storage s = Storage.getInstance();
		
		List<String> items = new ArrayList<>();
		
		for (TrpCollection c : s.getCollections()) {
			String key = c.getColId()+" - "+c.getColName();
			collection.combo.setData(key, c);
			kwsCollection.combo.setData(key, c);
			items.add(key);
		}
		kwsCollection.combo.setItems(items.toArray(new String[0]));
		kwsCollection.combo.select(0);
		
		items.add(0, "");
		collection.combo.setData("", null);
		
		collection.combo.setItems(items.toArray(new String[0]));
		collection.combo.select(0);		
	}
	
	Integer getDocId() {
		try {
			return Integer.parseInt(documentId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getColId() {
		String key = collection.combo.getText();
		Object d = collection.combo.getData(key);
		int collId = d==null ? 0 : ((TrpCollection) d).getColId();
		
		return collId;
	}
	
	Integer getKwsDocId() {
		try {
			return Integer.parseInt(kwsDocId.txt().trim());
//			logger.debug("parsed docid = "+docid);
		} catch (Exception e) {
			return null;
		}
	}
	
	int getKwsColId() {
		String key = kwsCollection.combo.getText();
		Object d = kwsCollection.combo.getData(key);
		int collId = d==null ? 0 : ((TrpCollection) d).getColId();
		
		return collId;
	}
	
	void searchThemFuckingDocuments() {
		Storage s = Storage.getInstance();
		if (s.isLoggedIn()) {
			try {				
				int colId = getColId();
				logger.debug("searching for docs, collId = "+colId);
				
				Integer docid = getDocId();
				
				if (!documentId.txt().isEmpty() && docid == null) {
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					infoLabel.setText("Invalid document id!");
					return;
				}
				
				docWidgetPaged.refreshPage(true);
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
				infoLabel.setText("Found "+docWidgetPaged.getPageableTable().getController().getTotalElements()+" documents!");
				
				if (docWidget != null) {
					List<TrpDocMetadata> docList = 
							s.getConnection().findDocuments(colId, docid, title.txt(), description.txt(), author.txt(), writer.txt(), exactMatch.getSelection(), caseSensitive.getSelection(), 0, 0, null, null);
					logger.debug("found docs: "+docList.size());
					
					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
					infoLabel.setText("Found "+docList.size()+" documents!");
	//				for (TrpDocMetadata doc : docList)
	//					logger.debug(doc.toString());
					
					
					docWidget.refreshList(docList);
				}
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
				
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				infoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	void spotThemFuckingKeyWords() {
		Storage s = Storage.getInstance();
		if (s.isLoggedIn()) {
			try {				
				int colId = getKwsColId();
				logger.debug("searching for docs, collId = "+colId);
				
				Integer docid = getKwsDocId();
				
				if (!kwsDocId.txt().isEmpty() && docid == null) {
					kwsInfoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					kwsInfoLabel.setText("Invalid document id!");
					return;
				}
				
//				docWidgetPaged.refreshPage(true);
//				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//				infoLabel.setText("Found "+docWidgetPaged.getPageableTable().getController().getTotalElements()+" documents!");
				
//				if (docWidget != null) {
				List<KwsDocHit> docList = s.getConnection().doKwsSearch(colId, docid, term.txt(), confSlider.getSelection());
					
					logger.debug("found docs: "+docList.size());
					
//					infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_GREEN));
//					infoLabel.setText("Found "+docList.size()+" documents!");
	//				for (TrpDocMetadata doc : docList)
	//					logger.debug(doc.toString());
					treeViewer.setInput(docList);
					
//					docWidget.refreshList(docList);
//				}
					
			} catch(ClientErrorException cee){
				logger.error(cee.getMessage(), cee);
				treeViewer.setInput(new ArrayList<KwsDocHit>(0));
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				logger.error(e1.getMessage(), e1);
				
				infoLabel.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				infoLabel.setText("Error: "+e1.getMessage());
			}
		}
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(800, 800);
	}
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
		return ti;
	}
	
	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.SINGLE);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(new KwsTreeLabelProvider());
		}
	}
}
