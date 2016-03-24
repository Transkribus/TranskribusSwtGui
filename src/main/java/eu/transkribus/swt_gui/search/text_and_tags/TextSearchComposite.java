package eu.transkribus.swt_gui.search.text_and_tags;

import java.io.IOException;
import java.util.Comparator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.customtags.search.TextSearchFacets;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt_canvas.mytableviewer.MyTableViewer;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TextSearchComposite extends ATextAndSearchComposite {
	private final static Logger logger = LoggerFactory.getLogger(TextSearchComposite.class);
		
	Combo scopeCombo; 
	Button searchBtn, searchPrevBtn, searchNextBtn;
	
	LabeledText inputText;
	Label resultsLabel;
	Composite parameters;
	Button wholeWordCheck, caseSensitiveCheck;
	
	Group facetsGroup;
	
	MyTableViewer resultsTable;
		
	public static final String PROP_COL = "Property";
	public static final String VALUE_COL = "Value (Regex)";
	public static final ColumnConfig[] PROPS_COLS = new ColumnConfig[] {
		new ColumnConfig(PROP_COL, 100, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(VALUE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final String DOC_COL = "Doc";
	public static final String TITLE_COL = "Title";
	public static final String PAGE_COL = "Page";
	public static final String REGION_COL = "Region";
	public static final String LINE_COL = "Line";
	public static final String WORD_COL = "Word";
//	public static final String TAG_COL = "Tag";
	public static final String CONTEXT_COL = "Context";
	public static final String TAG_VALUE_COL = "Value";
	public static final ColumnConfig[] RESULT_COLS = new ColumnConfig[] {
		new ColumnConfig(TAG_VALUE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CONTEXT_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DOC_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(TITLE_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PAGE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(REGION_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(LINE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(WORD_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(TAG_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
	};
	

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public TextSearchComposite(Composite parent, int style) {
		super(parent, style);
		createContents();
	}
	
	protected void createContents() {
		this.setLayout(new FillLayout());
		Composite c = new Composite(this, 0);
		c.setLayout(new FillLayout());
		
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));
		
		facetsGroup = new Group(sf, SWT.NONE);
		facetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		facetsGroup.setLayout(new GridLayout(2, false));
		facetsGroup.setText("Search facets - use * and ? as wildcards for multiple or one unknown character");

		Label lS = new Label(facetsGroup, 0);
		lS.setText("Search scope: ");
		
		scopeCombo = new Combo(facetsGroup, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		scopeCombo.setItems(SCOPES);
		scopeCombo.select(2);
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					findTags();
				}
			}
		};
		
		inputText = new LabeledText(facetsGroup, "Query string:");
		inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		inputText.text.addTraverseListener(findTagsOnEnterListener);
				
		parameters = new Composite(facetsGroup, 0);
		parameters.setLayout(new GridLayout(2, false));
		parameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		wholeWordCheck = new Button(parameters, SWT.CHECK);
		wholeWordCheck.setText("Whole word");
		
		caseSensitiveCheck = new Button(parameters, SWT.CHECK);
		caseSensitiveCheck.setText("Case sensitive");		
		
//		Label lSpace = new Label(facetsGroup, 0);
		
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		searchBtn = new Button(btnsComp, SWT.PUSH);
		searchBtn.setImage(Images.FIND);
		searchBtn.setText("Search!");
		searchBtn.setToolTipText("Finds all matches in the selected scope using the specified facets and displays the results in the table below");
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				findTags();
			}
		});
		
		SelectionAdapter findNextPrevL = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				findNextTag(e.getSource() == searchPrevBtn);
			}
		};
		
		searchPrevBtn = new Button(btnsComp, SWT.PUSH);
		searchPrevBtn.setImage(Images.PAGE_PREV);
		searchPrevBtn.setText("Previous");
		searchPrevBtn.setToolTipText("Find and display the previous match in the current document, starting from the cursor position");
		searchPrevBtn.addSelectionListener(findNextPrevL);
		
		searchNextBtn = new Button(btnsComp, SWT.PUSH);
		searchNextBtn.setImage(Images.PAGE_NEXT);
		searchNextBtn.setText("Next");
		searchNextBtn.setToolTipText("Find and display the next match in the current document, starting from the cursor position");
		searchNextBtn.addSelectionListener(findNextPrevL);

		initResultsTable(sf);
		
		sf.setWeights(new int[] { 30, 70 } );
				
		c.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				logger.trace("disposed!");
			}
		});

	}
			
	void initResultsTable(Composite container) {
		Group resultsGroup = new Group(container, SWT.NONE);
		resultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsGroup.setText("Search results");
		resultsGroup.setLayout(new GridLayout(1, false));
		
		resultsLabel = new Label(resultsGroup, 0);
		resultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
//		resultsSf = new SashForm(resultsGroup, SWT.HORIZONTAL);
//		resultsSf.setLayout(new GridLayout(1, false));
//		resultsSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		resultsTable = new MyTableViewer(resultsGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		resultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsTable.getTable().setHeaderVisible(true);
		resultsTable.addColumns(RESULT_COLS);
		
		final MyTableLabelProvider mtlp = new MyTableLabelProvider() {
			@Override public String getColumnText(String cn, Object element, Object data) {
				CustomTag t = (CustomTag) element;
				CustomTagList ctl = t.getCustomTagList();
				TrpLocation l = new TrpLocation(t);				
				
				String txt = "";
				if (cn.equals(DOC_COL)) {
					if (l.md != null)
						txt = l.md.getDocId() == -1 ? l.md.getLocalFolder().getAbsolutePath() : ""+l.md.getDocId();
				}
//				else if (cn.equals(TITLE_COL)) {
//					if (md != null)
//						txt = md.getDocId() == -1 ? md.getLocalFolder().getAbsolutePath() : ""+md.getDocId();
//				}
				else if (cn.equals(PAGE_COL)) {
					if (l.md != null)
						txt = ""+l.md.getPageNr();
				}
				else if (cn.equals(REGION_COL)) {
					if (l.r != null)
						txt = l.r.getId();
				}
				else if (cn.equals(LINE_COL)) {
					if (l != null)
						txt = l.l.getId();
				}
				else if (cn.equals(WORD_COL)) {
					if (l.w != null)
						txt = l.w.getId();
				}
				else if (cn.equals(CONTEXT_COL)) {
					txt = t.getTextOfShape();
				}
				else if (cn.equals(TAG_VALUE_COL)) {
					txt = t.getContainedText();
				}				
				
				return txt;
			}
		};
		
		// set custom sorters (needed as this table is virtual!):
		for (final TableColumn tc : resultsTable.getTable().getColumns()) {
			resultsTable.setCustomListSorterForColumn(tc.getText(), new Comparator<CustomTag>() {
				@Override public int compare(CustomTag o1, CustomTag o2) {
					String t1 = mtlp.getColumnText(tc.getText(), o1, null);
					String t2 = mtlp.getColumnText(tc.getText(), o2, null);
					
					return t1.compareTo(t2);
				}
			});
		}
				
//		resultsTable.setContentProvider(new ArrayContentProvider());
		resultsTable.setContentProvider(new ILazyContentProvider() {
			@Override public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			@Override public void dispose() { }
			@Override public void updateElement(int index) {
				logger.trace("replacing element at index: "+index);
				if (index >= 0 && index < foundTags.size()) {
					resultsTable.replace(foundTags.get(index), index);
				}
			}
		});
		resultsTable.setUseHashlookup(true);
		resultsTable.setItemCount(100);
				
		resultsTable.setLabelProvider(new StyledCellLabelProvider() {
			public void update(ViewerCell cell) {
				int ci = cell.getColumnIndex();
				String cn = RESULT_COLS[ci].name;
				CustomTag t = (CustomTag) cell.getElement();
				
				String txt = mtlp.getColumnText(cn, t, null);
				if (cn.equals(CONTEXT_COL)) {
					StyleRange sr = new StyleRange(t.getOffset(), t.getLength(), cell.getForeground(), Colors.getSystemColor(SWT.COLOR_YELLOW));
					cell.setStyleRanges(new StyleRange[] { sr } );
				}			

				cell.setText(txt);
			}
		});

		resultsTable.setInput(foundTags);
		
		resultsTable.addDoubleClickListener(new IDoubleClickListener() {	
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					CustomTag t = (CustomTag) sel.getFirstElement();
					logger.debug("showing custom tag: "+t);
					TrpLocation l = new TrpLocation(t);
					TrpMainWidget.getInstance().showLocation(l);
				}
				
			}
		});	
	}
			
//	void findNextTag(final boolean previous) {
//		final CustomTagSearchFacets facets = 
//				new CustomTagSearchFacets(inputText.getText(), "", null, true, 
//											wholeWordCheck.getSelection(), caseSensitiveCheck.getSelection());
//		
//		logger.debug("searching for next tag, previous = "+previous);
//		
//		final Storage s = Storage.getInstance();
//		try {
//			if (!s.isDocLoaded()) {
//				DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", "No document loaded!");
//				return;
//			}
//			
//			final List<CustomTag> tag = new ArrayList<>();
//			// TODO: specify real current position here, and display a wait dialog
//			final int startPageIndex = s.getPageIndex();
//			final int startRegionIndex = s.getCurrentRegion()==-1 ? 0 : s.getCurrentRegion();
//			final int startLineIndex = s.getCurrentLineObject()==null ? 0 : s.getCurrentLineObject().getIndex();
//			int currentOffsetTmp = 0;
//			TrpMainWidget mw = TrpMainWidget.getInstance();
//			if (mw.getUi().getSelectedTranscriptionType() == Type.LINE_BASED) {
//				int o = mw.getUi().getSelectedTranscriptionWidget().getText().getCaretOffset();
//				int lo = mw.getUi().getSelectedTranscriptionWidget().getText().getOffsetAtLine(startLineIndex);
//				logger.debug("o = "+o+" lo = "+lo);
//				currentOffsetTmp = o-lo;
//			}
//			final int currentOffset = currentOffsetTmp;
//			logger.info("searching for next tag, startPageIndex= "+startPageIndex+", startRegionIndex= "+startRegionIndex+", startLindex= "+startLineIndex+" currentOffset= "+currentOffset+", previous= "+previous);			
//			
//			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
//				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					CustomTagSearcher.searchOnDoc_WithoutIndex(tag, s.getDoc(), facets, startPageIndex, startRegionIndex, startLineIndex, true, currentOffset, previous, monitor, false);
//				}
//			}, "Searching", true);		
//			
//			if (!tag.isEmpty()) {
//				logger.info("found a tag - displaying: "+tag.get(0));
//				TrpLocation l = new TrpLocation(tag.get(0));
//				TrpMainWidget.getInstance().showLocation(l);
//			} else {
//				DialogUtil.showInfoMessageBox(getShell(), "No match", "No match found!");
//			}
//		} catch (Throwable e) {
//			DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", e.getMessage());
//			logger.error(e.getMessage(), e);
//			return;
//		}
//	}
		
//	void findTags() {
//		final CustomTagSearchFacets facets = new CustomTagSearchFacets(inputText.getText(), "", null, true, 
//					wholeWordCheck.getSelection(), caseSensitiveCheck.getSelection());
//		
//		logger.debug("searching for text: "+facets.isSearchText());
//		
////		final String tn = tagNameInput.getText();
////		if (StringUtils.isEmpty(tn)) {
////			DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", "tag name cannot be empty - use * wildcard to search for any tag!");			
////			return;
////		}
//
//		final Storage s = Storage.getInstance();
//		final TrpMainWidget mw = TrpMainWidget.getInstance();
//		final int currentCollID = mw.getUi().getDocOverviewWidget().getSelectedCollectionId();
//		
//		String scope = scopeCombo.getText();
//		logger.debug("searching on scope: "+scope+ " currentCollID = "+currentCollID);
//		
//		foundTags.clear();
//		resultsLabel.setText("");
//
//		ProgressBarDialog pd = new ProgressBarDialog(getShell()) {
//			@Override public void subTask(final String name) {
//				super.subTask(name);
//				Display.getDefault().syncExec(new Runnable() {
//					@Override public void run() {
//						Shell s = TextSearchComposite.this.getShell();
//						if (!s.isDisposed()) {
//							updateResults();
//						}
//					}
//				});
//			}
//		};
//		
//		try {
//			if (scope.equals(SCOPE_COLL)) {
//				pd.open(new IRunnableWithProgress() {
//					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//						try {
//							CustomTagSearcher.searchOnCollection_WithoutIndex(currentCollID, foundTags, facets, monitor);
//							
//						} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
//							throw new InvocationTargetException(e);
//						}
//					}
//				}, "Searching", true);
//			}
//			else if (scope.equals(SCOPE_DOC)) {
//				if (!s.isDocLoaded()) {
//					DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", "No document loaded!");
//					return;
//				}
//				pd.open(new IRunnableWithProgress() {
//					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//						CustomTagSearcher.searchOnDoc_WithoutIndex(foundTags, s.getDoc(), facets, 0, 0, 0, false, 0, false, monitor, false);
//					}
//				}, "Searching", true);
//			}
//			else if (scope.equals(SCOPE_PAGE)) {
//	//			if (s.getTranscript()==null || s.getTranscript().getPage() == null) {
//				if (!s.isPageLoaded() || s.getTranscript().getPageData() == null) {
//					DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", "No page loaded!");
//					return;
//				}
//				TrpPageType p = s.getTranscript().getPage();
//				
//				CustomTagSearcher.searchOnPage(foundTags, p, facets, 0, 0, false, 0, false);
//			} else if (scope.equals(SCOPE_REGION)) {
//				TrpTextRegionType r = s.getCurrentRegionObject();
//				if (r==null) {
//					DialogUtil.showErrorMessageBox(getShell(), "Error in tag search", "No region selected!");
//					return;
//				}
//					
//				CustomTagSearcher.searchOnRegion(foundTags, r, facets, 0, false, 0, false);
//			}
//		}
//		catch (Throwable e) {
//			mw.onError("Error in tag search", e.getMessage(), e);
//			return;
//		}
//		
//		logger.debug("setting item count to "+foundTags.size());
//		
//		updateResults();
//	}
	
	@Override public void updateResults() {
		logger.debug("updating results table, N = "+foundTags.size());
		resultsLabel.setText(foundTags.size()+" matches");
		
		resultsTable.setItemCount(foundTags.size());
		resultsTable.refresh();
	}
	
	@Override public String getScope() {
		return scopeCombo.getText();
	}

	@Override public TextSearchFacets getFacets() throws IOException {
		String ss = inputText.getText();
		if (ss.length()==0)
			throw new IOException("Cannot search for an empty string");
		if (ss.length()==1 && (ss.equals("*") || ss.equals("?"))) {
			throw new IOException("At least one non-wildcard character (*, ?) is needed in the query string");
		}
		
		return new TextSearchFacets(inputText.getText(), wholeWordCheck.getSelection(), caseSensitiveCheck.getSelection());
	}
	
	public static void main(String [] args) {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		shell.setSize(800, 700);
		Display display = shell.getDisplay();
		
		TextSearchComposite c = new TextSearchComposite(shell, 0);

		shell.open();
		while (!shell.isDisposed())
		if (!display.readAndDispatch()) display.sleep();
	}



}

