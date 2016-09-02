package eu.transkribus.swt_gui.search.fulltext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.beans.searchresult.PageHit;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.mytableviewer.MyTableLabelProvider;
import eu.transkribus.swt_canvas.mytableviewer.MyTableViewer;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.search.text_and_tags.TextSearchComposite;



public class FullTextSearchComposite extends Composite{
	private final static Logger logger = LoggerFactory.getLogger(FullTextSearchComposite.class);
	Group facetsGroup;
	LabeledText inputText;
	Button wholeWordCheck, caseSensitiveCheck;
	Button searchBtn, searchPrevBtn, searchNextBtn;
	Composite parameters;
	Button[] textTypeBtn;
	FulltextSearchResult fullTextSearchResult;
	
	Table resultsTable;
	TableViewer viewer;
	SashForm resultsSf;
	Label resultsLabel;
	
	private int rows = 10;
	private int start = 0;
	private String lastSearchText;
	private int numPageHits;
	private static final String BAD_SYMBOLS = "[+-:=]";
	private SearchType type;
	
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
		facetsGroup.setText("Solr search currently supports standard word search, exact phrasing (\"...\") and wildcards (*) ");
		
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
		
		textTypeBtn = new Button[2];
		textTypeBtn[0] = new Button(parameters, SWT.RADIO);
		textTypeBtn[0].setSelection(true);
		textTypeBtn[0].setText("Word-based text");
		textTypeBtn[1] = new Button(parameters, SWT.RADIO);
		textTypeBtn[1].setSelection(false);
		textTypeBtn[1].setText("Line-based text");
		
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		searchBtn = new Button(btnsComp, SWT.PUSH);
		searchBtn.setImage(Images.FIND);
		searchBtn.setText("Search!");
		searchBtn.setToolTipText("Search for text");
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				start = 0;
				findText();
			}
		});
		
		searchPrevBtn = new Button(btnsComp, SWT.PUSH);
		searchPrevBtn.setImage(Images.PAGE_PREV);
		searchPrevBtn.setText("Previous page");

		searchPrevBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
					if(start > 0){
						start -= 10;
						findText();
				}
				
			}
		});
		searchNextBtn = new Button(btnsComp, SWT.PUSH);
		searchNextBtn.setImage(Images.PAGE_NEXT);
		searchNextBtn.setText("Next page");

		searchNextBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if(fullTextSearchResult != null){
					if((start+rows) < fullTextSearchResult.getNumResults()){
						start += 10;
						findText();
					}
				}
				
			}
		});
		
		
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

//		resultsTable = new MyTableViewer(resultsSf, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.VIRTUAL);
//		resultsTable.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		resultsTable.getTable().setHeaderVisible(true);
		
//		resultsTable= new Table(resultsSf, 1);
		
        viewer = new TableViewer(resultsSf);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new ArrayContentProvider());
        
        TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Context");
        column.setWidth(600);
        TableViewerColumn contextCol = new TableViewerColumn(viewer, column);
        contextCol.setLabelProvider(new StyledCellLabelProvider(){

        	  @Override
        	  public void update(ViewerCell cell) {
        		
        		String hlText = ((Hit)cell.getElement()).getHighlightText();
        	    cell.setText( hlText.replaceAll("<em>", "").replaceAll("</em>", "") );
        	    
        	    int hlStart = hlText.indexOf("<em>");
        	    int hlEnd = hlText.indexOf("</em>");
        	    int hlLen = hlEnd-hlStart-4;
        	    
        	    StyleRange myStyledRange = 
        	        new StyleRange(hlStart, hlLen, null, 
        	            Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
        	    StyleRange[] range = { myStyledRange };
        	    cell.setStyleRanges(range);
        	    super.update(cell);
        	  }
        	
//            @Override
//            public String getText(Object element) {
//                Hit hit = (Hit)element;
//
//                return hit.getHighlightText();
//            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("DocId");
        column.setWidth(50);
        TableViewerColumn docCol = new TableViewerColumn(viewer, column);
        docCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return Integer.toString(hit.getDocId());
            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Page");
        column.setWidth(50);
        TableViewerColumn pageCol = new TableViewerColumn(viewer, column);
        pageCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return Integer.toString(hit.getPageNr());
            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Region");
        column.setWidth(50);
        TableViewerColumn regCol = new TableViewerColumn(viewer, column);
        regCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return hit.getRegionId();
            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Line");
        column.setWidth(50);
        TableViewerColumn lineCol = new TableViewerColumn(viewer, column);
        lineCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return hit.getLineId();
            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Word");
        column.setWidth(50);
        TableViewerColumn worCol = new TableViewerColumn(viewer, column);
        worCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return hit.getWordId();
            }

        });
        
        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Pixel Coords");
        column.setWidth(150);
        TableViewerColumn pixelCol = new TableViewerColumn(viewer, column);
        pixelCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Hit hit = (Hit)element;

                return hit.getPixelCoords();
            }

        });
        
        
		viewer.addDoubleClickListener(new IDoubleClickListener() {	
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					logger.debug("Clicked! Doc: "+ ((Hit)sel.getFirstElement()).getDocId()+ " Page: "+((Hit)sel.getFirstElement()).getPageNr());
					Hit clHit = (Hit)sel.getFirstElement();
					Storage s = Storage.getInstance();					
					
					
					
					ArrayList<Integer> userCols = new ArrayList<>();
					for(TrpCollection userCol : s.getCollections()){
						userCols.add(userCol.getColId());
						logger.debug("User collection: " + userCol.getColId());
					}
					logger.debug("Hit collections: " + clHit.getCollectionIds());
					int col = -1;
					for(Integer userColId : userCols){
						for(Integer hitColId : clHit.getCollectionIds()){
							
							if(userColId.equals(hitColId)){
								col = userColId;
							}
						}
					}
					logger.debug("Col: " + col);
					if(col != -1){
						int docId = clHit.getDocId();
						int pageNr = clHit.getPageNr();
						TrpLocation l = new TrpLocation();
						
						l.collectionId = col;
						l.docId = docId;
						l.pageNr = pageNr;		
						l.shapeId=clHit.getLineId();

						
	
						TrpMainWidget.getInstance().showLocation(l);
					}
				}
				
			}
		});

		
	}
	
	void findText(){		
		
		String searchText = inputText.getText().replaceAll(BAD_SYMBOLS, "");
		
		if(searchText.isEmpty()) return;
		
		if(!searchText.equals(lastSearchText)) start = 0;
		
		if(caseSensitiveCheck.getSelection() == true){
			if(textTypeBtn[0].getSelection() == true){
				type = SearchType.Words;
			}else if(textTypeBtn[1].getSelection() == true){
				type = SearchType.Lines;
			}
		}else if(caseSensitiveCheck.getSelection() == false){
			if(textTypeBtn[0].getSelection() == true){
				type = SearchType.WordsLc;
			}else if(textTypeBtn[1].getSelection() == true){
				type = SearchType.LinesLc;
			}
		}
		
		final Storage storage = Storage.getInstance();
		
		try {			
			fullTextSearchResult = storage.searchFulltext(searchText, type, start, rows, null);;
			
			logger.debug("Searching for: " + searchText + ", Start: "+start+" rows: "+rows);
			logger.debug("Num. Results: " + fullTextSearchResult.getNumResults());			
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
		
		lastSearchText = searchText;
	}


	private void updateResultsTable() {
		
//		numPageHits = (int) fullTextSearchResult.getNumResults();
//		int max = (start+rows) > numPageHits ? numPageHits : (start+rows);
//
//		resultsLabel.setText("Showing Pagehits "+(start+1)+" to "+(max)+" of "+(numPageHits));
//			
//		resultsTable.removeAll();
//		for(PageHit hit : fullTextSearchResult.getPageHits()){
//			TableItem pItem = new TableItem(resultsTable, 1);
//			pItem.setText("Doc: " + hit.getDocId() + " p: " + hit.getPageNr());
//			for(String hl : hit.getHighlights()){
//				TableItem hlItem = new TableItem(resultsTable, 1);
//				hlItem.setText("      " + hl.toString());
//				
//			}
//		}
//		resultsTable.redraw();		
     
		numPageHits = (int) fullTextSearchResult.getNumResults();
		int max = (start+rows) > numPageHits ? numPageHits : (start+rows);
		resultsLabel.setText("Showing Pagehits "+(start)+" to "+(max)+" of "+(numPageHits));
		
        ArrayList<Hit> hits = new ArrayList<Hit>();
        
        for (PageHit pHit : fullTextSearchResult.getPageHits()){
        	int numTags = 0;
        	Map<String,Integer> foundWords = new HashMap<String,Integer>();
        	
        	
        	
        	for(String hlString : pHit.getHighlights()){
        		
        		ArrayList<String> tags = getTagValues(hlString);
        		
        		for(String tag : tags){
        			boolean contained = false;
        			for(String word : foundWords.keySet()){
        				if(word.equals(tag)){
        					contained = true;
        					foundWords.replace(word, foundWords.get(word)+1);
        				}
        			}
        			if(contained == false){
        				foundWords.put(tag, 0);
        			}
        		}
        		
        		ArrayList<String> matchedCoords = new ArrayList<>();
        		for(String word : pHit.getWordCoords()){
        			if(word.split(":")[0].equals(tags.get(0))){
        				matchedCoords.add(word);
        			}
        		}
        		
        		
        		String wCoords;
        		if(matchedCoords.size() > foundWords.get(tags.get(0))){
        			wCoords = matchedCoords.get(foundWords.get(tags.get(0)));
        		}else if(!matchedCoords.isEmpty()){
        			wCoords = matchedCoords.get(0);
        		}else if(!pHit.getWordCoords().isEmpty()){
        			wCoords = pHit.getWordCoords().get(0);
        		}else{
        			break;
        		}

        		String regId = wCoords.split(":")[1].split("/")[0];
        		String linId = wCoords.split(":")[1].split("/")[1];
        		String worId = wCoords.split(":")[1].split("/")[2];
        		String pxCoords = wCoords.split(":")[2];
        		
        		ArrayList<Integer> colIds = pHit.getCollectionIds();        		
        		
        		
        		Hit hit = new Hit(hlString, (int)pHit.getDocId(), (int)pHit.getPageNr(), regId, linId, worId, pxCoords);
        		hit.setCollectionIds(colIds);
        		hits.add(hit);
        		numTags += tags.size();
        	}
        	
        }

        viewer.setInput(hits);

		
	}
	
	  private static class Hit
	  {
	    String highlightText;
	    String regionId, lineId, wordId;
	    private String pixelCoords;
	    private ArrayList<Integer> collectionIds;

		int docId, pageNr;
		      
	    Hit(String hl, int doc, int page, String region, String line, String word, String coords){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;
	    	regionId = region;
	    	lineId = line;
	    	wordId = word;	  
	    	pixelCoords = coords;
	    }
	    
	    Hit(String hl, int doc, int page){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;	    	
	    }
		      
	      public String getHighlightText() {
			return highlightText;
		}

		public void setHighlightText(String highlightText) {
			this.highlightText = highlightText;
		}

		public int getDocId() {
			return docId;
		}

		public void setDocId(int docId) {
			this.docId = docId;
		}

		public int getPageNr() {
			return pageNr;
		}

		public void setPageNr(int pageNr) {
			this.pageNr = pageNr;
		}

		public String getLineId() {
			return lineId;
		}

		public void setLineId(String lineId) {
			this.lineId = lineId;
		}

		public String getWordId() {
			return wordId;
		}

		public void setWordId(String wordId) {
			this.wordId = wordId;
		}	
		
		public String getRegionId() {
			return regionId;
		}

		public void setRegionId(String regionId) {
			this.regionId = regionId;
		}

		public String getPixelCoords() {
			return pixelCoords;
		}

		public void setPixelCoords(String pixelCoords) {
			this.pixelCoords = pixelCoords;
		}

		public ArrayList<Integer> getCollectionIds() {
			return collectionIds;
		}

		public void setCollectionIds(ArrayList<Integer> collectionIds) {
			this.collectionIds = collectionIds;
		}
		
	  }
	  
	  private static final Pattern TAG_REGEX = Pattern.compile("<em>(.+?)</em>");
	  public static ArrayList<String> getTagValues(final String str) {
			ArrayList<String> tagValues = new ArrayList<String>();
			Matcher matcher = TAG_REGEX.matcher(str);
			while (matcher.find()) {
				tagValues.add(matcher.group(1));
			}
			return tagValues;
		}

}
