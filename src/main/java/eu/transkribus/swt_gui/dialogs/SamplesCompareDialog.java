package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionLabelProvider;
import eu.transkribus.swt_gui.htr.DataSetTableWidget;
import eu.transkribus.swt_gui.htr.TreeViewerDataSetSelectionSashForm.DataSetEntry;
import eu.transkribus.swt_gui.htr.TreeViewerDataSetSelectionSashForm.DataSetMetadata;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;


public class SamplesCompareDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(SamplesCompareDialog.class);
	
	private final int colId;

	private CTabFolder paramTabFolder;
	private CTabItem samplesTabItem;
	
	LabeledText nrOfLinesTxt, alphaValueTxt;
	
	private Text modelNameTxt, descTxt;

	private Storage store = Storage.getInstance();

	private List<TrpDocMetadata> docList;
	
	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
	private static final Color BLUE = Colors.createColor(BLUE_RGB);
	private static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	private static final Color CYAN = Colors.createColor(CYAN_RGB);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProvider labelProv;
	private Composite buttonComp;
	private Button addToSampleSetBtn, removeFromSampleSetBtn;
	private Label previewLbl;
	private DataSetTableWidget sampleSetOverviewTable;
	private Map<TrpDocMetadata, List<TrpPage>> sampleDocMap;
	

	public SamplesCompareDialog(Shell parentShell) {
		super(parentShell);
		docList = store.getDocList();
		colId = store.getCollId();	
		
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(1, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(1, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Sample Name:");
		modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		descTxt = new Text(paramCont, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 3;
		descTxt.setLayoutData(gd);

		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		SamplesMethodUITab tab = createSampelsTab(0);
		CTabItem selection = tab.getTabItem();
		
		paramTabFolder.setSelection(selection);		
		paramCont.pack();
		
		createSampleTreeViewer(sash, SWT.HORIZONTAL);
		
		//treeViewerSelector = new TreeViewerDataSetSelectionSashForm(sash, SWT.HORIZONTAL, colId, docList);
		
		sash.setWeights(new int[] { 45, 55 });
		
		
		return cont;
	}
	
	private void createSampleTreeViewer(Composite parent, int style) {
		
		SashForm sampleTreeViewer = new SashForm(parent,style);
		sampleDocMap = new TreeMap<>();
		sampleTreeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sampleTreeViewer.setLayout(new GridLayout(1, false));
		
		Group treeViewerCont = new Group(sampleTreeViewer, SWT.NONE);
		treeViewerCont.setText("Sample Set");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));
		
		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		
		buttonComp = new Composite(sampleTreeViewer, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		
		previewLbl = new Label(buttonComp, SWT.NONE);
		GridData gd2 = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		previewLbl.setLayoutData(gd2);
		
		addToSampleSetBtn = new Button(buttonComp, SWT.PUSH);
		addToSampleSetBtn.setImage(Images.ADD);
		addToSampleSetBtn.setText("Sample Set");
		addToSampleSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		Group trainOverviewCont = new Group(sampleTreeViewer, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));
		
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);
		
		Group sampleSetGrp = new Group(trainOverviewCont, SWT.NONE);
		sampleSetGrp.setText("Sample Set");
		sampleSetGrp.setLayoutData(tableGd);
		sampleSetGrp.setLayout(tableGl);

		sampleSetOverviewTable = new DataSetTableWidget(sampleSetGrp, SWT.BORDER);
		sampleSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromSampleSetBtn = new Button(sampleSetGrp, SWT.PUSH);
		removeFromSampleSetBtn.setLayoutData(buttonGd);
		removeFromSampleSetBtn.setImage(Images.CROSS);
		removeFromSampleSetBtn.setText("Remove selected entries from train set");
		
		sampleTreeViewer.setWeights(new int[] {45,15,45});
		
		treeViewerCont.pack();
		buttonComp.pack();
		trainOverviewCont.pack();
		sampleSetGrp.pack();
		addListeners();
		
	}

	private SamplesMethodUITab createSampelsTab(final int tabIndex) {
		samplesTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		samplesTabItem.setText("Compare Options");
		
		Composite samplesConfComposite = new Composite(paramTabFolder,0);
		samplesConfComposite.setLayout(new GridLayout(1,false));
		
		nrOfLinesTxt = new LabeledText(samplesConfComposite, "Nr. of lines", true);
		nrOfLinesTxt.setText("100");
		nrOfLinesTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true , true , 1, 1));
		
		alphaValueTxt = new LabeledText(samplesConfComposite, "Alpha value", true);
		alphaValueTxt.setText("0.05");
		alphaValueTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true , true , 1, 1));
		
		samplesTabItem.setControl(samplesConfComposite);
		return new SamplesMethodUITab(tabIndex, samplesTabItem, samplesConfComposite);
	}
	
	@Override
	protected void okPressed() {
		String msg = "";
		DataSetMetadata sampleSetMd = getSampleSetMetadata();
		msg += "Sample set size:\n \t\t\t\t" + sampleSetMd.getPages() + " pages\n";
		msg += "\t\t\t\t" + sampleSetMd.getLines() + " lines\n";
		msg += "\t\t\t\t" + sampleSetMd.getWords() + " words\n";
		msg += "Samples Options:\n ";
		msg += "\t\t\t\t" + nrOfLinesTxt.getText()  + " lines\n";
		msg += "\t\t\t\t" + alphaValueTxt.getText() + " alpha value\n";
		
		int result = DialogUtil.showYesNoDialog(this.getShell(), "Start?", msg);
		
		if (result == SWT.YES) {
			super.okPressed();
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Samples Compare");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
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

		addToSampleSetBtn.addSelectionListener(new SelectionAdapter() {
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
			}
		});

		removeFromSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<DataSetEntry> entries = sampleSetOverviewTable.getSelectedDataSets();
				if (!entries.isEmpty()) {
					for (DataSetEntry entry : entries) {
						sampleDocMap.remove(entry.getDoc());
					}
					updateTable(sampleSetOverviewTable, sampleDocMap);
					updateColors();
				}
			}
		});

	}
	
	private void updateColors() {
		List<TrpPage> trainPages;
		for (TreeItem i : tv.getTree().getItems()) {
			TrpDocMetadata doc = (TrpDocMetadata) i.getData();

			// default color set
			Color fgColor = BLACK;
			Color bgColor = WHITE;

			if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				bgColor = CYAN;
			} else if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if (doc.getNrOfPages() == sampleDocMap.get(doc).size()) {
					bgColor = BLUE;
				} else {
					bgColor = LIGHT_BLUE;
				}
			} 
			i.setBackground(bgColor);
			i.setForeground(fgColor);

			trainPages = sampleDocMap.containsKey(doc) ? sampleDocMap.get(doc) : new ArrayList<>(0);

			for (TreeItem child : i.getItems()) {
				TrpPage page = (TrpPage) child.getData();
				if (trainPages.contains(page)) {
					child.setBackground(BLUE);
					child.setForeground(WHITE);
				}else {
					child.setBackground(WHITE);
					child.setForeground(BLACK);
				}
			}
		}
	}
	
	private void updateTable(DataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DataSetEntry> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DataSetEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
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
					lines += tmd.getNrOfTranscribedLines();
					words += tmd.getNrOfWordsInLines();
				}
				
			
		}
		return new DataSetMetadata(pages, lines, words);
	}
	
	private class SamplesMethodUITab {
		final int tabIndex;
		final CTabItem tabItem;
		final Composite configComposite;
		private SamplesMethodUITab(int tabIndex, CTabItem tabItem, Composite configComposite) {
			this.tabIndex = tabIndex;
			this.tabItem = tabItem;
			this.configComposite = configComposite;
		}
		public int getTabIndex() {
			return tabIndex;
		}
		public CTabItem getTabItem() {
			return tabItem;
		}
		public Composite getConfigComposite() {
			return configComposite;
		}
	}

}
