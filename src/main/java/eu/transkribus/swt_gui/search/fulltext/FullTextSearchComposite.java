package eu.transkribus.swt_gui.search.fulltext;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt_canvas.mytableviewer.MyTableViewer;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.search.text_and_tags.TextSearchComposite;

public class FullTextSearchComposite extends Composite{
	private final static Logger logger = LoggerFactory.getLogger(FullTextSearchComposite.class);
	Group facetsGroup;
	LabeledText inputText;
	Button wholeWordCheck, caseSensitiveCheck;
	Button searchBtn, searchPrevBtn, searchNextBtn;
	Composite parameters;
	
	FulltextSearchResult fullTextSearchResult;
	
	MyTableViewer resultsTable;
	SashForm resultsSf;
	Label resultsLabel;
	
	private static final String BAD_SYMBOLS = "[+-:=]";
	
	public static final String DOC_COL = "Doc";
	public static final String TITLE_COL = "Title";
	public static final String PAGE_COL = "Page";
	public static final String REGION_COL = "Region";
	public static final String LINE_COL = "Line";
	public static final String WORD_COL = "Word";
//	public static final String TAG_COL = "Tag";
	public static final String CONTEXT_COL = "Context";
	public static final ColumnConfig[] RESULT_COLS = new ColumnConfig[] {
			new ColumnConfig(CONTEXT_COL, 500, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DOC_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
//			new ColumnConfig(TITLE_COL, 60, true, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(PAGE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(REGION_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(LINE_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(WORD_COL, 60, false, DefaultTableColumnViewerSorter.ASC),
//			new ColumnConfig(TAG_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
		};
	
	
	public FullTextSearchComposite(Composite parent, int style){
		super(parent, style);
		createContents();
	}
	
	
	protected void createContents(){
		
		this.setLayout(new FillLayout());
		Composite c = new Composite(this, 0);
		c.setLayout(new FillLayout());
				
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));
		
		facetsGroup = new Group(sf, SWT.NONE);
		facetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		facetsGroup.setLayout(new GridLayout(2, false));
		facetsGroup.setText("Solr search currently supports specific text (\"... ...\") and wildcards (*) ");
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					findText();
				}
			}
		};
		
		inputText = new LabeledText(facetsGroup, "Search for:");
		inputText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		inputText.text.addTraverseListener(findTagsOnEnterListener);
		
		parameters = new Composite(facetsGroup, 0);
		parameters.setLayout(new GridLayout(3, false));
		parameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		caseSensitiveCheck = new Button(parameters, SWT.CHECK);
		caseSensitiveCheck.setText("Case sensitive");
		
		Button[] textType = new Button[2];
		textType[0] = new Button(parameters, SWT.RADIO);
		textType[0].setSelection(true);
		textType[0].setText("Word-based text");
		textType[1] = new Button(parameters, SWT.RADIO);
		textType[1].setSelection(false);
		textType[1].setText("Line-based text");
		
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		searchBtn = new Button(btnsComp, SWT.PUSH);
		searchBtn.setImage(Images.FIND);
		searchBtn.setText("Search!");
		searchBtn.setToolTipText("Search for text");
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				findText();
			}
		});
		
		searchPrevBtn = new Button(btnsComp, SWT.PUSH);
		searchPrevBtn.setImage(Images.PAGE_PREV);
		searchPrevBtn.setText("Previous page");

//		searchPrevBtn.addSelectionListener(findNextPrevL);
		
		searchNextBtn = new Button(btnsComp, SWT.PUSH);
		searchNextBtn.setImage(Images.PAGE_NEXT);
		searchNextBtn.setText("Next page");

//		searchNextBtn.addSelectionListener(findNextPrevL);
		
		
		initResultsTable(sf);
		
	}
	
	void initResultsTable(Composite container){
		Group resultsGroup = new Group(container, SWT.NONE);
		resultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsGroup.setText("Search results");
		resultsGroup.setLayout(new GridLayout(1, false));
		
		resultsLabel = new Label(resultsGroup, 0);
		resultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		resultsSf = new SashForm(resultsGroup, SWT.HORIZONTAL);
		resultsSf.setLayout(new GridLayout(1, false));
		resultsSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		resultsTable = new MyTableViewer(resultsSf, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
		resultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsTable.getTable().setHeaderVisible(true);
		resultsTable.addColumns(RESULT_COLS);	
		
		resultsTable.setLabelProvider(new StyledCellLabelProvider() {
			public void update(ViewerCell cell) {
				int ci = cell.getColumnIndex();
				String cn = RESULT_COLS[ci].name;
				CustomTag t = (CustomTag) cell.getElement();
				
				String txt = "TSTSTWST"; //mtlp.getColumnText(cn, t, null);
				if (cn.equals(CONTEXT_COL)) {
					StyleRange sr = new StyleRange(t.getOffset(), t.getLength(), cell.getForeground(), Colors.getSystemColor(SWT.COLOR_YELLOW));
					cell.setStyleRanges(new StyleRange[] { sr } );
				}			

				cell.setText(txt);
			}
		});
		
	}
	
	void findText(){
		
		
		String searchText = inputText.getText().replaceAll(BAD_SYMBOLS, "");
		
		if(searchText.isEmpty()) return;
		
		final Storage storage = Storage.getInstance();
		
		try {			
			fullTextSearchResult = storage.searchFulltext(searchText, SearchType.Words, 0, 10, null);;
			
			logger.debug("Searching for: " + searchText);
			logger.debug("Num. Results:" + fullTextSearchResult.getNumResults());			
			if(fullTextSearchResult != null){
				updateResultsTable();
			}
			
		} catch (SessionExpiredException e) {
			logger.error("Error when trying to search: Session expired!");
			e.printStackTrace();
		} catch (ServerErrorException e) {
			logger.error("Error when trying to search: ServerError!");
			e.printStackTrace();
		} catch (ClientErrorException e) {
			logger.error("Error when trying to search: ClientError!");
			e.printStackTrace();
		} catch (NoConnectionException e) {
			logger.error("Error when trying to search: No connection!");
			e.printStackTrace();
		}
		
		
	}


	private void updateResultsTable() {
		resultsLabel.setText("Pagehits: "+fullTextSearchResult.getNumResults());
		resultsTable.refresh();
		resultsTable.setContentProvider(new ILazyContentProvider() {
			@Override public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }
			@Override public void dispose() { }
			@Override public void updateElement(int index) {
				logger.trace("replacing element at index: "+index);
				if (index >= 0 && index < fullTextSearchResult.getNumResults()) {
					resultsTable.replace(fullTextSearchResult.getPageHits().get(index), index);
				}
			}
		});
		

		
	}

}
