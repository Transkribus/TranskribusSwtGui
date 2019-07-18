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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
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
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.treeviewer.DocumentDataSelectionEntry;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

/**
 * Widget for data set selection in {@link HtrTrainingDialogLegacy}
 * 
 * @deprecated new training dialog uses classes in htr.treeviewer package
 */
public class TreeViewerDataSetSelectionSashForm extends SashForm {
	private static final Logger logger = LoggerFactory.getLogger(TreeViewerDataSetSelectionSashForm.class);
	
	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
	private static final RGB GREEN_RGB = new RGB(0, 140, 0);
	private static final RGB LIGHT_GREEN_RGB = new RGB(0, 255, 0);
	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
	private static final Color BLUE = Colors.createColor(BLUE_RGB);
	private static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	private static final Color GREEN = Colors.createColor(GREEN_RGB);
	private static final Color LIGHT_GREEN = Colors.createColor(LIGHT_GREEN_RGB);
//	private static final Color CYAN = Colors.getSystemColor(SWT.COLOR_CYAN);
	private static final Color CYAN = Colors.createColor(CYAN_RGB);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProvider labelProv;
	private Button useGtVersionChk, useNewVersionChk;
	
	private Composite buttonComp;
	private Label previewLbl;
	
	private DataSetTableWidget testSetOverviewTable, trainSetOverviewTable;

	private Button addToTrainSetBtn, addToTestSetBtn, removeFromTrainSetBtn, removeFromTestSetBtn;
	
	private int colId;
	private List<TrpDocMetadata> docList;
	private Map<TrpDocMetadata, List<TrpPage>> trainDocMap, testDocMap;
	
	public TreeViewerDataSetSelectionSashForm(Composite parent, int style, final int colId, List<TrpDocMetadata> docList) {
		super(parent, style);
		
		this.colId = colId;
		this.docList = docList;
		trainDocMap = new TreeMap<>();
		testDocMap = new TreeMap<>();	
		
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.setLayout(new GridLayout(1, false));

		Group treeViewerCont = new Group(this, SWT.NONE);
		treeViewerCont.setText("Training Set");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));

		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);

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

		trainSetOverviewTable = new DocumentDataSetTableWidget(trainSetGrp, SWT.BORDER);
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

		testSetOverviewTable = new DocumentDataSetTableWidget(testSetGrp, SWT.BORDER);
		testSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		removeFromTestSetBtn = new Button(testSetGrp, SWT.PUSH);
		removeFromTestSetBtn.setLayoutData(buttonGd);
		removeFromTestSetBtn.setImage(Images.CROSS);
		removeFromTestSetBtn.setText("Remove selected entries from test set");

		this.setWeights(new int[] { 45, 10, 45 });

		treeViewerCont.pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		trainSetGrp.pack();
		testSetGrp.pack();
		
		addListeners();
	}

	private void addListeners() {

		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object o = selection.getFirstElement();
				if (o instanceof TrpPage) {
					TrpPage p = (TrpPage) o;
					try {
						Image image = ImgLoader.load(p.getThumbUrl());
						if (previewLbl.getImage() != null) {
							previewLbl.getImage().dispose();
						}
						previewLbl.setImage(image);
					} catch (IOException e) {
						logger.error("Could not load image", e);
					}
				} else if (o instanceof TrpDocMetadata) {
					if (previewLbl.getImage() != null) {
						previewLbl.getImage().dispose();
					}
					previewLbl.setImage(null);
				}

			}
		});

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (o instanceof TrpDocMetadata) {
					for (TreeItem i : tv.getTree().getItems()) {
						if (i.getData().equals(o)) {
							tv.setExpandedState(o, !i.getExpanded());
							break;
						}
					}
					updateColors();
				} else if (o instanceof TrpPage) {
					TrpPage p = (TrpPage)o;
					TrpLocation loc = new TrpLocation();
					loc.collId = colId;
					loc.docId = p.getDocId();
					loc.pageNr = p.getPageNr();
					TrpMainWidget.getInstance().showLocation(loc);
				}
			}
		});

		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateColors();
			}
		});

		addToTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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

						trainDocMap.put(docMd, pageList);

						if (testDocMap.containsKey(docMd)) {
							testDocMap.remove(docMd);
						}
					} else if (o instanceof TrpPage) {
						TrpPage p = (TrpPage) o;
						TrpDocMetadata parent = (TrpDocMetadata) contentProv.getParent(p);
						if (trainDocMap.containsKey(parent) && !trainDocMap.get(parent).contains(p)) {
							trainDocMap.get(parent).add(p);
						} else if (!trainDocMap.containsKey(parent)) {
							List<TrpPage> pageList = new LinkedList<>();
							pageList.add(p);
							trainDocMap.put(parent, pageList);
						}

						if (testDocMap.containsKey(parent) && testDocMap.get(parent).contains(p)) {
							if (testDocMap.get(parent).size() == 1) {
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

		addToTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
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
						testDocMap.put(docMd, pageList);

						if (trainDocMap.containsKey(docMd)) {
							trainDocMap.remove(docMd);
						}
					} else if (o instanceof TrpPage) {
						TrpPage p = (TrpPage) o;
						TrpDocMetadata parent = (TrpDocMetadata) contentProv.getParent(p);
						if (testDocMap.containsKey(parent) && !testDocMap.get(parent).contains(p)) {
							testDocMap.get(parent).add(p);
						} else if (!testDocMap.containsKey(parent)) {
							List<TrpPage> pageList = new LinkedList<>();
							pageList.add(p);
							testDocMap.put(parent, pageList);
						}

						if (trainDocMap.containsKey(parent) && trainDocMap.get(parent).contains(p)) {
							if (trainDocMap.get(parent).size() == 1) {
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
				List<DocumentDataSelectionEntry> entries = trainSetOverviewTable.getSelectedDataSets();
				if (!entries.isEmpty()) {
					for (DocumentDataSelectionEntry entry : entries) {
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
				List<DocumentDataSelectionEntry> entries = testSetOverviewTable.getSelectedDataSets();
				if (!entries.isEmpty()) {
					for (DocumentDataSelectionEntry entry : entries) {
						testDocMap.remove(entry.getDoc());
					}
					updateTable(testSetOverviewTable, testDocMap);
					updateColors();
				}
			}
		});
	}
	
	private void updateTable(DataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DocumentDataSelectionEntry> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DocumentDataSelectionEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}
	
	private void updateColors() {
		List<TrpPage> trainPages, testPages;
		for (TreeItem i : tv.getTree().getItems()) {
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
	
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		final boolean useGt = useGtVersionChk.isEnabled() && useGtVersionChk.getSelection();
		final boolean useInitial = useNewVersionChk.isEnabled() && useNewVersionChk.getSelection();
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
				if (useGt || useInitial) {
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
						if (useGt && t.getStatus().equals(EditStatus.GT)) {
							tmd = t;
							break;
						}
						if (useInitial && t.getStatus().equals(EditStatus.NEW)){
							tmd=t;
							break;
						}
					}
				}
				pages++;
				lines += tmd.getNrOfTranscribedLines();
				words += tmd.getNrOfWordsInLines();
			}
		}
		return new DataSetMetadata(DataSetMetadataTableLabelProvider.TOTAL_ROW_LABEL, pages, lines, words);
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return trainDocMap;
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTestDocMap() {
		return testDocMap;
	}
	
	public DataSetMetadata getTrainSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public DataSetMetadata getTestSetMetadata() {
		return computeDataSetSize(getTestDocMap());
	}
	
	public Button getUseGtVersionChk() {
		return useGtVersionChk;
	}
	
	public Button getUseNewVersionChk() {
		return useNewVersionChk;
	}
}