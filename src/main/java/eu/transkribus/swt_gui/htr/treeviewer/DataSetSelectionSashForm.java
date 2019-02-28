package eu.transkribus.swt_gui.htr.treeviewer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.htr.DataSetTableWidget;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.GroundTruthSet;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DataSetSelectionSashForm extends SashForm {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionSashForm.class);
	
	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
	private static final RGB GREEN_RGB = new RGB(0, 140, 0);
	private static final RGB LIGHT_GREEN_RGB = new RGB(0, 255, 0);
	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
//	private static final RGB BLUE_RGB = new RGB(0, 83, 138);
//	private static final RGB LIGHT_BLUE_RGB = new RGB(115, 161, 191);
//	private static final RGB GREEN_RGB = new RGB(0, 105, 66);
//	private static final RGB LIGHT_GREEN_RGB = new RGB(69, 145, 117);
//	private static final RGB CYAN_RGB = new RGB(0, 95, 99);
	
	private static final Color BLUE = Colors.createColor(BLUE_RGB);
	private static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	private static final Color GREEN = Colors.createColor(GREEN_RGB);
	private static final Color LIGHT_GREEN = Colors.createColor(LIGHT_GREEN_RGB);
	private static final Color CYAN = Colors.createColor(CYAN_RGB);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private TreeViewer docTv, groundTruthTv;
	private Button useGtVersionChk, useNewVersionChk;
	
	private Composite buttonComp;
	private Label previewLbl;
	
	private CTabFolder dataTabFolder;
	private CTabItem documentsTabItem;
	private CTabItem gtTabItem;
	
	private DataSetTableWidget<IDataSetEntry<Object, Object>> testSetOverviewTable, trainSetOverviewTable;
	private DataSetSelectionController dataHandler;
	
	private Button addToTrainSetBtn, addToTestSetBtn, removeFromTrainSetBtn, removeFromTestSetBtn;
	
	private int colId;
	//the input to select data from
	private List<TrpDocMetadata> docList;
	private List<TrpHtr> htrList;

	public DataSetSelectionSashForm(Composite parent, int style, final int colId, List<TrpHtr> htrList, List<TrpDocMetadata> docList) {
		super(parent, style);
		
		this.colId = colId;
		this.docList = docList;
		this.htrList = htrList;
		
		dataHandler = new DataSetSelectionController(this);
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.setLayout(new GridLayout(1, false));
		
		dataTabFolder = new CTabFolder(this, SWT.BORDER | SWT.FLAT);
		dataTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		documentsTabItem = new CTabItem(dataTabFolder, SWT.NONE);
		documentsTabItem.setText("Documents");
		docTv = createDocumentTreeViewer(dataTabFolder);
		documentsTabItem.setControl(docTv.getControl());

		if(!htrList.isEmpty()) {
			gtTabItem = new CTabItem(dataTabFolder, SWT.NONE);
			gtTabItem.setText("HTR Model Data");
			groundTruthTv = createGroundTruthTreeViewer(dataTabFolder);
			gtTabItem.setControl(groundTruthTv.getControl());
		} else {
			groundTruthTv = null;
		}
		
		buttonComp = new Composite(this, SWT.NONE);
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

		Group trainOverviewCont = new Group(this, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));

		useGtVersionChk = new Button(trainOverviewCont, SWT.CHECK);
		useGtVersionChk.setText("Use Groundtruth versions");
		
		useNewVersionChk = new Button(trainOverviewCont, SWT.CHECK);
		useNewVersionChk.setText("Use initial('New') versions");
		useNewVersionChk.setSelection(true);

		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);

		Group trainSetGrp = new Group(trainOverviewCont, SWT.NONE);
		trainSetGrp.setText("Training Set");
		trainSetGrp.setLayoutData(tableGd);
		trainSetGrp.setLayout(tableGl);

		trainSetOverviewTable = new DataSetTableWidget<IDataSetEntry<Object, Object>>(trainSetGrp, SWT.BORDER) {};
		trainSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromTrainSetBtn = new Button(trainSetGrp, SWT.PUSH);
		removeFromTrainSetBtn.setLayoutData(buttonGd);
		removeFromTrainSetBtn.setImage(Images.CROSS);
		removeFromTrainSetBtn.setText("Remove selected entries from train set");

		Group testSetGrp = new Group(trainOverviewCont, SWT.NONE);
		testSetGrp.setText("Test Set");
		testSetGrp.setLayoutData(tableGd);
		testSetGrp.setLayout(tableGl);

		testSetOverviewTable = new DataSetTableWidget<IDataSetEntry<Object, Object>>(testSetGrp, SWT.BORDER) {};
		testSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		removeFromTestSetBtn = new Button(testSetGrp, SWT.PUSH);
		removeFromTestSetBtn.setLayoutData(buttonGd);
		removeFromTestSetBtn.setImage(Images.CROSS);
		removeFromTestSetBtn.setText("Remove selected entries from test set");

		this.setWeights(new int[] { 45, 10, 45 });
		dataTabFolder.setSelection(documentsTabItem);
		
		docTv.getTree().pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		trainSetGrp.pack();
		testSetGrp.pack();
		
		addListeners();
	}

	private TreeViewer createDocumentTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		tv.setContentProvider(dataHandler.getDocContentProvider());
		tv.setLabelProvider(dataHandler.getDocLabelProvider());
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		return tv;
	}
	
	private TreeViewer createGroundTruthTreeViewer(Composite parent) {
		TreeViewer tv = new TreeViewer(parent, SWT.BORDER | SWT.MULTI);
		tv.setContentProvider(dataHandler.getHtrGtContentProvider());
		tv.setLabelProvider(dataHandler.getHtrGtLabelProvider());
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.htrList);
		return tv;
	}

	private boolean isGroundTruthSelectionEnabled() {
		return groundTruthTv != null;
	}
	
	private void addListeners() {
		IDoubleClickListener treeViewerDoubleClickListener = new TreeViewerDoubleClickListener();
		ISelectionChangedListener treeViewerSelectionChangedListener = new TreeViewerSelectionChangedListener();
		
		docTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
		docTv.addDoubleClickListener(treeViewerDoubleClickListener);
		
		docTv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateDocTvColors(dataHandler.getTrainDocMap(), dataHandler.getTestDocMap());
			}
		});
		
		if(isGroundTruthSelectionEnabled()) {
			groundTruthTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
			groundTruthTv.addDoubleClickListener(treeViewerDoubleClickListener);
			groundTruthTv.getTree().addListener(SWT.Expand, new Listener() {
				public void handleEvent(Event e) {
					updateGtTvColors(dataHandler.getTrainGtMap(), dataHandler.getTestGtMap());
				}
			});
		}

		addToTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(documentsTabItem.equals(dataTabFolder.getSelection())) {
					dataHandler.addDocumentSelectionToTrainSet((IStructuredSelection) docTv.getSelection());
				} else if (isGroundTruthSelectionEnabled() 
						&& gtTabItem.equals(dataTabFolder.getSelection())) {
					// TODO
				}
			}
		});

		addToTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(documentsTabItem.equals(dataTabFolder.getSelection())) {
					dataHandler.addDocumentSelectionToValidationSet((IStructuredSelection) docTv.getSelection());
				} else if (isGroundTruthSelectionEnabled() 
						&& gtTabItem.equals(dataTabFolder.getSelection())) {
					// TODO
				}
			}
		});

		removeFromTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<IDataSetEntry<Object, Object>> entries = trainSetOverviewTable.getSelectedDataSets();
				dataHandler.removeFromTrainSetSelection(entries);
			}
		});

		removeFromTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<IDataSetEntry<Object, Object>> entries = testSetOverviewTable.getSelectedDataSets();
				dataHandler.removeFromTestSetSelection(entries);
			}
		});
	}
	
	private void updateThumbnail(IStructuredSelection selection) {
		Object o = selection.getFirstElement();
		if (o instanceof TrpPage) {
			TrpPage p = (TrpPage) o;
			updateThumbnail(p.getThumbUrl());
		} else if (o instanceof TrpGroundTruthPage) {
			TrpGroundTruthPage g = (TrpGroundTruthPage) o;
			updateThumbnail(g.getImage().getThumbUrl());		
		} else {
			if (previewLbl.getImage() != null) {
				previewLbl.getImage().dispose();
			}
			previewLbl.setImage(null);
		}
	}
	
	private void updateThumbnail(URL thumbUrl) {
		try {
			Image image = ImgLoader.load(thumbUrl);
			if (previewLbl.getImage() != null) {
				previewLbl.getImage().dispose();
			}
			previewLbl.setImage(image);
		} catch (IOException e) {
			logger.error("Could not load image", e);
		}
	}
	
	void updateTrainSetTable(Map<TrpDocMetadata, List<TrpPage>> trainDocMap) {
		updateTable(trainSetOverviewTable, trainDocMap);
	}
	
	void updateValidationSetTable(Map<TrpDocMetadata, List<TrpPage>> validationDocMap) {
		updateTable(testSetOverviewTable, validationDocMap);		
	}
	
	private void updateTable(DataSetTableWidget<IDataSetEntry<Object, Object>> t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<IDataSetEntry<?, ?>> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DocumentDataSetEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}
	
	void updateGtTvColors(Map<TrpHtr, List<TrpGroundTruthPage>> trainGtMap, Map<TrpHtr, List<TrpGroundTruthPage>> testGtMap) {
		// TODO Auto-generated method stub
		
	}
	
	void updateDocTvColors(Map<TrpDocMetadata, List<TrpPage>> trainDocMap, Map<TrpDocMetadata, List<TrpPage>> testDocMap) {
		List<TrpPage> trainPages, testPages;
		for (TreeItem i : docTv.getTree().getItems()) {
			TrpDocMetadata doc = (TrpDocMetadata) i.getData();

			// default color set
			Color fgColor = BLACK;
			Color bgColor = WHITE;

			if (trainDocMap.containsKey(doc) && testDocMap.containsKey(doc)) {
				fgColor = WHITE;
				bgColor = CYAN;
			} else if (trainDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if (doc.getNrOfPages() == trainDocMap.get(doc).size()) {
					bgColor = BLUE;
				} else {
					bgColor = LIGHT_BLUE;
				}
			} else if (testDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if (doc.getNrOfPages() == testDocMap.get(doc).size()) {
					bgColor = GREEN;
				} else {
					bgColor = LIGHT_GREEN;
				}
			}
			i.setBackground(bgColor);
			i.setForeground(fgColor);

			trainPages = trainDocMap.containsKey(doc) ? trainDocMap.get(doc) : new ArrayList<>(0);
			testPages = testDocMap.containsKey(doc) ? testDocMap.get(doc) : new ArrayList<>(0);

			for (TreeItem child : i.getItems()) {
				TrpPage page = (TrpPage) child.getData();
				if (trainPages.contains(page)) {
					child.setBackground(BLUE);
					child.setForeground(WHITE);
				} else if (testPages.contains(page)) {
					child.setBackground(GREEN);
					child.setForeground(WHITE);
				} else {
					child.setBackground(WHITE);
					child.setForeground(BLACK);
				}
			}
		}
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return dataHandler.getTrainDocMap();
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTestDocMap() {
		return dataHandler.getTestDocMap();
	}
	
	public Map<TrpHtr, List<TrpGroundTruthPage>> getTrainGtMap() {
		return dataHandler.getTrainGtMap();
	}
	
	public Map<TrpHtr, List<TrpGroundTruthPage>> getTestGtMap() {
		return dataHandler.getTestGtMap();
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return dataHandler.getTrainSetMetadata();
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return dataHandler.getTestSetMetadata();
	}
	
	public Button getUseGtVersionChk() {
		return useGtVersionChk;
	}
	
	public Button getUseNewVersionChk() {
		return useNewVersionChk;
	}
		
	/**
	 * Updates thumbnail image on selection change
	 *
	 */
	private class TreeViewerSelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			updateThumbnail(selection);
		}
	};
	
	/**
	 * Expands items that have children on double click. Leaf elements are displayed.
	 * 
	 * @author philip
	 *
	 */
	private class TreeViewerDoubleClickListener implements IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (o instanceof TrpDocMetadata) {
				expandTreeItem(o, docTv);
				updateDocTvColors(dataHandler.getTrainDocMap(), dataHandler.getTestDocMap());
			} else if (o instanceof TrpPage) {
				TrpPage p = (TrpPage)o;
				TrpLocation loc = new TrpLocation();
				loc.collId = colId;
				loc.docId = p.getDocId();
				loc.pageNr = p.getPageNr();
				TrpMainWidget.getInstance().showLocation(loc);
			} else if (o instanceof TrpHtr || o instanceof GroundTruthSet) {
				expandTreeItem(o, groundTruthTv);
				updateGtTvColors(dataHandler.getTrainGtMap(), dataHandler.getTestGtMap());
			}
		}
		private void expandTreeItem(Object o, TreeViewer tv) {
			for (TreeItem i : tv.getTree().getItems()) {
				if (i.getData().equals(o)) {
					tv.setExpandedState(o, !i.getExpanded());
					return;
				}
			}
		}
	}
}
