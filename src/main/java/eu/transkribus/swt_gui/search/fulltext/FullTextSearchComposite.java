package eu.transkribus.swt_gui.search.fulltext;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.enums.SearchType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.searchresult.Facet;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.beans.searchresult.PageHit;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class FullTextSearchComposite extends Composite{
	private final static Logger logger = LoggerFactory.getLogger(FullTextSearchComposite.class);
	Group facetsGroup;
	LabeledText inputText;
	Button wholeWordCheck, caseSensitiveCheck, previewCheck;
	Button searchBtn, searchPrevBtn, searchNextBtn;
	Composite parameters;
	Composite facetComp;
	Button[] textTypeBtn;
	FulltextSearchResult fullTextSearchResult;
	Tree facetTree;
	Table resultsTable;
	TableViewer viewer;
	TreeViewer facetViewer;
	SashForm resultsSf;
	Label resultsLabel;
	String lastHoverCoords;
	Shell shell;
	
	Label facetLabel;
	ToolBar facetToolBar;
	ToolItem colItem;
	ToolItem docItem;
	ToolItem authorItem;
	ToolItem uploaderItem;
	MultiSelectionCombo collCombo;
	MultiSelectionCombo docCombo;
	MultiSelectionCombo authCombo;
	MultiSelectionCombo uplCombo;
	
	List<String> filters;
	Storage storage;
	
	volatile ArrayList<Hit> hits;
	
	Thread imgLoaderThread;
	volatile HashMap<String,Image> prevImages;
	
	FimgStoreGetClient imgStoreClient;
	
	boolean enableHover;
	
	private int rows = 10;
	private int start = 0;
	String searchText;
	private String lastSearchText;
	private int numPageHits;
	private static final String BAD_SYMBOLS = "(,[,+,-,:,=,],)";
	private SearchType type;

	public FullTextSearchComposite(Composite parent, int style){
		super(parent, style);
		shell = parent.getShell();	
		try {
			imgStoreClient = new FimgStoreGetClient(new URL("https://dbis-thure.uibk.ac.at/f/"));
		} catch (Exception e) {
			logger.error("Could not create connection to FimgStore" + e);
			e.printStackTrace();
		}

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
		facetsGroup.setText("Solr search currently supports standard word search, exact phrasing (\"...\") and wildcards (* or ?) ");
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					findText();
				}
			}
		};
		
		inputText = new LabeledText(facetsGroup, "Search for:");
		inputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		inputText.text.addTraverseListener(findTagsOnEnterListener);
		
		parameters = new Composite(facetsGroup, 0);
		parameters.setLayout(new GridLayout(4, false));
		parameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2));
		
		caseSensitiveCheck = new Button(parameters, SWT.CHECK);
		caseSensitiveCheck.setText("Case sensitive");
		
		previewCheck = new Button(parameters, SWT.CHECK);
		previewCheck.setText("Show word preview");
		previewCheck.setSelection(true);
		previewCheck.setToolTipText("Automatic loading of preview word image. Works better with word-based text. Guesses word coordinates for line-based text.");
		
		textTypeBtn = new Button[2];
		textTypeBtn[0] = new Button(parameters, SWT.RADIO);
		textTypeBtn[0].setSelection(false);
		textTypeBtn[0].setText("Word-based text");
		textTypeBtn[0].setToolTipText("Search documents transcribed word by word");
		textTypeBtn[1] = new Button(parameters, SWT.RADIO);
		textTypeBtn[1].setSelection(true);
		textTypeBtn[1].setText("Line-based text");	
		textTypeBtn[1].setToolTipText("Search documents transcribed line by line");
						
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
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

//		Composite filters;
//		filters = new Composite(facetsGroup, 0);
//		filters.setLayout(new GridLayout(3, false));
//		filters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));		

		initResultsTable(sf);
		sf.setWeights(new int[] { 20, 80 } );		
	}	
	
	void initResultsTable(Composite container){
		Group resultsGroup = new Group(container, SWT.NONE);
		resultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsGroup.setText("Search results");
		resultsGroup.setLayout(new GridLayout(1, false));
		
		resultsLabel = new Label(resultsGroup, 0);
		resultsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));		

		initFacetSf(resultsGroup);
        
		resultsSf = new SashForm(resultsGroup, SWT.HORIZONTAL);
		resultsSf.setLayout(new GridLayout(1, false));
		resultsSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
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
//						logger.debug("User collection: " + userCol.getColId());
					}
//					logger.debug("Hit collections: " + clHit.getCollectionIds());
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

		resultsTable = viewer.getTable();
		final HoverShell hShell = new HoverShell(shell);
		enableHover = true;
		resultsTable.addListener(SWT.MouseEnter, new Listener(){
			public void handleEvent(Event e){
				//enableHover = true;
//				hShell.hoverShell.setVisible(true);
//				logger.debug("Mouse inside table");
			}		
		});
		
		resultsTable.addListener(SWT.MouseExit, new Listener(){
			public void handleEvent(Event e){
				//enableHover = false;
				hShell.hoverShell.setVisible(false);
//				logger.debug("Mouse outside table");
			}
		});			
		
		resultsTable.addListener(SWT.MouseMove, new Listener(){
			public void handleEvent(Event e){
				if(enableHover == true && previewCheck.getSelection()){

					Point p = new Point(e.x,e.y);
					
					TableItem hoverItem = resultsTable.getItem(p);
					if(hoverItem != null){						
						
						String coords = ((Hit)hoverItem.getData()).getPixelCoords();
						Point mousePos = Display.getCurrent().getCursorLocation();							
						if(coords != lastHoverCoords){
						
							//hShell.imgLabel.setImage(Images.LOADING_IMG);
							String imgKey = ((Hit)hoverItem.getData()).getImgUrl().replace("https://dbis-thure.uibk.ac.at/f/Get?id=", "");
							imgKey = imgKey.replace("&fileType=view", "");
//							logger.debug(imgKey);							
							
							try {						
								
								
								Image img;							
								int[] cropValues = getCropValues(coords);
								
								if(prevImages.keySet().contains(coords)){
									img = prevImages.get(coords);
								}else{
									URL url = imgStoreClient.getUriBuilder().getImgCroppedUri(imgKey, cropValues[0], cropValues[1], cropValues[2], cropValues[3]).toURL();
									img = ImageDescriptor.createFromURL(url).createImage();	
//									logger.debug("Forced loading of img coords:"+coords);
									prevImages.put(coords, img);
									
								}

								hShell.imgLabel.setImage(img);
								hShell.hoverShell.setVisible(true);
								
							} catch (Exception ex) {								
								ex.printStackTrace();
								hShell.imgLabel.setText("Could not load preview image");
							}						
							
						}						
							hShell.hoverShell.setLocation(mousePos.x + 20 , mousePos.y - 20);
							hShell.hoverShell.pack();
							hShell.hoverShell.redraw();
//							logger.debug("Obj:"+((Hit)hoverItem.getData()).getPixelCoords());
							lastHoverCoords = coords;		

					}else{
						hShell.hoverShell.setVisible(false);
					}
				}
			}
		});
		
	}
	
	private void initFacetSf(Group resultsGroup) {
		int noOfFacets = 4;
		SashForm facetSf = new SashForm(resultsGroup, SWT.HORIZONTAL);
		facetSf.setLayout(new GridLayout(1, false));
		facetSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, noOfFacets, 1));
		
//		SashForm facetLabelSf = new SashForm(facetSf, SWT.HORIZONTAL);
//		facetLabelSf.setLayout(new GridLayout(1, false));
//		facetLabelSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, noOfFacets, 1));
//		
//		SashForm facetSelectSf = new SashForm(facetSf, SWT.HORIZONTAL);
//		facetSf.setLayout(new GridLayout(1, false));
//		facetSf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, noOfFacets, 1));		
		
		
		//initFacetToolBar(resultsGroup);  
		
        int[] selection = new int[] { 0 };


        
        
        String[] startItems = new String[] { "All collections" };
        collCombo = new MultiSelectionCombo(facetSf, startItems, selection, SWT.NONE, this);
        startItems = new String[] { "All documents" };
        docCombo = new MultiSelectionCombo(facetSf, startItems, selection, SWT.NONE, this);
        startItems = new String[] { "All authors" };
        authCombo = new MultiSelectionCombo(facetSf, startItems, selection, SWT.NONE, this);
        startItems = new String[] { "All uploaders" };
        uplCombo = new MultiSelectionCombo(facetSf, startItems, selection, SWT.NONE, this);
		
	}
	
	private void updateFacets(){
		
		List<String> colNames = new ArrayList<String>();
		for(TrpCollection coll : storage.getCollections()){
			colNames.add(coll.getColName());
		}
		

		collCombo.currentSelection = new int[] { 0 };
		docCombo.currentSelection = new int[] { 0 };
		authCombo.currentSelection = new int[] { 0 };
		uplCombo.currentSelection = new int[] { 0 };
		
		int i;
		for(Facet facet : fullTextSearchResult.getFacets()){
			//logger.debug(""+facet.getFacetMap().keySet().contains(colNames.get(0)));
						
			if(facet.getName().equals("f_collectionName")){

//				String[] facetItems = new String[facet.getFacetMap().keySet().size()+1];
				String[] facetItems = new String[colNames.size()+1];
				facetItems[0] = "All collections";
				i = 1;

				for(String key : facet.getFacetMap().keySet()){						
					if(colNames.contains(key)){
						facetItems[i] = key + " ("+facet.getFacetMap().get(key)+")";
						i++;
					}

				}
				if(facetItems != null){
					collCombo.textItems = facetItems;
				}
			}			
			else if(facet.getName().equals("f_author")){

				String[] facetItems = new String[facet.getFacetMap().keySet().size()+1];
				facetItems[0] = "All authors";
				i = 1;

				for(String key : facet.getFacetMap().keySet()){
					
					facetItems[i] = key + " ("+facet.getFacetMap().get(key)+")";
					logger.debug("author facet: "+ facetItems[i]);					
					i++;					
				}
//				logger.debug("author keyset: " + facet.getFacetMap().keySet());
				if(facetItems != null){
					authCombo.textItems = facetItems;
				}
			}
			else if(facet.getName().equals("f_uploader")){

				String[] facetItems = new String[facet.getFacetMap().keySet().size()+1];
				facetItems[0] = "All uploaders";
				i = 1;

				for(String key : facet.getFacetMap().keySet()){
					facetItems[i] = key + " ("+facet.getFacetMap().get(key)+")";
//					logger.debug("uploader facet: "+ facetItems[i]);
					i++;					
				}
				logger.debug("uploader keyset: " + facet.getFacetMap().keySet());
				if(facetItems != null){
					uplCombo.textItems = facetItems;
				}
			}
			else if(facet.getName().equals("f_title")){

				String[] facetItems = new String[facet.getFacetMap().keySet().size()+1];
				facetItems[0] = "All documents";
				i = 1;

				for(String key : facet.getFacetMap().keySet()){
					facetItems[i] = key + " ("+facet.getFacetMap().get(key)+")";
					i++;					
				}
				if(facetItems != null){
					docCombo.textItems = facetItems;
				}
			}


		}
		
		

		
	}

	private void initFacetToolBar(Composite parent) {
	    facetToolBar = new ToolBar(parent, SWT.HORIZONTAL);
	    facetToolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
	    
	    colItem = new ToolItem(facetToolBar, SWT.DROP_DOWN);
	    colItem.setText("Collections");
	    
	    docItem = new ToolItem(facetToolBar, SWT.DROP_DOWN);
	    docItem.setText("Documents");
	    
	    authorItem = new ToolItem(facetToolBar, SWT.DROP_DOWN);
	    authorItem.setText("Authors");
	    
	    uploaderItem = new ToolItem(facetToolBar, SWT.DROP_DOWN);
	    uploaderItem.setText("Uploaders");	    
	    
	}

	int[] getCropValues(String coords){
		int[] values = new int[4];
		
		String[] singleCoords = coords.split(" ");
		
		ArrayList<Integer> xPts = new ArrayList<Integer>();
		ArrayList<Integer> yPts = new ArrayList<Integer>();	
		
		for(String s: singleCoords){
			String[] PtCoords = s.split(",");
			xPts.add(Integer.parseInt(PtCoords[0]));					//All X coords
			yPts.add(Integer.parseInt(PtCoords[1]));					//All Y coords
		}
		
		int CoordX1 = xPts.get(0);
		int CoordX2 = xPts.get(0);
		int CoordY1 = yPts.get(0);
		int CoordY2 = yPts.get(0);
		
		//find outlining points
		for(int x : xPts){
			if(CoordX1>x){
				CoordX1 = x;
			}
			if(CoordX2<x){
				CoordX2 = x;
			}
		}
		for(int y : yPts){
			if(CoordY1>y){
				CoordY1 = y;
			}
			if(CoordY2<y){
				CoordY2 = y;
			}
		}			
	
		int width = Math.abs(CoordX2-CoordX1);
		int height = Math.abs(CoordY2-CoordY1);
		
		width = (int)(width*2f);
		height = (int)(height*2f);
		CoordX1-=(int)(width*0.25f);
		CoordY1-=(int)(height*0.25f);
		
		values[0]=CoordX1;
		values[1]=CoordY1;
		values[2]=width;
		values[3]=height;		
		
		return values;
	}
	
	void findText(){	
		
		prevImages = new HashMap<String,Image>();
		
		searchText = inputText.getText().toString();
		
		for(String c : BAD_SYMBOLS.split(",")){
			searchText = searchText.replaceAll("\\"+c, "");
		}
		
		searchText = searchText.trim();
		
		if(searchText.isEmpty()) {
			return;
		}
		
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
		
		storage = Storage.getInstance();
		
		generateFilters();
		

		
		try {	
			if(!filters.isEmpty()){
				fullTextSearchResult = storage.searchFulltext(searchText, type, start, rows, filters);
			}else{
				fullTextSearchResult = storage.searchFulltext(searchText, type, start, rows, null);
			}
			
			
			logger.debug("Searching for: " + searchText + ", Start: "+start+" rows: "+rows);
			
			logger.debug("Query: " + fullTextSearchResult.getParams());
			logger.debug("Num. Results: " + fullTextSearchResult.getNumResults());	


			
			if(fullTextSearchResult != null){				
				updateResultsTable();
//				updateFacets();
				if(!searchText.equals(lastSearchText))	{
					updateFacets();
				}

			}
			
		} catch (SessionExpiredException e) {
			logger.error("Error when trying to search: Session expired!"+e);
			e.printStackTrace();
		} catch (ServerErrorException e) {
			logger.error("Error when trying to search: ServerError!"+e);
			e.printStackTrace();
		} catch (ClientErrorException e) {
			logger.error("Error when trying to search: ClientError!"+e);
			e.printStackTrace();
		} catch (NoConnectionException e) {
			logger.error("Error when trying to search: No connection!"+e);
			e.printStackTrace();
		}		
		
		lastSearchText = searchText;
		
	}


	//Clean up some day
	private void generateFilters() {
		filters = new ArrayList<String>();
		String subFilters = "";
		
		for(int i : collCombo.getSelections()){
			
			if(i>=collCombo.textItems.length) {
				return;
			}

			//Remove number of hits and only use facet name
			String filterString = collCombo.textItems[i].replaceAll("\\(.*\\)", "").trim();
			if(!filterString.equals("All collections") && subFilters.isEmpty()){
				subFilters+="(f_collectionName:\""+filterString+"\""; 
			}else if(!filterString.equals("All collections") && !subFilters.isEmpty()){
				subFilters+=" OR f_collectionName:\""+filterString+"\""; 
			}
		}
		
		if(!subFilters.isEmpty()){
			subFilters+=")";
			filters.add(subFilters);
			subFilters="";
		}

		
		for(int i : authCombo.getSelections()){
			
			if(i>=authCombo.textItems.length) {
				return;
			}

			
			String filterString = authCombo.textItems[i].replaceAll("\\(.*\\)", "").trim();
			if(!filterString.equals("All authors") && subFilters.isEmpty()){
				subFilters+="(f_author:\""+filterString+"\""; 
			}else if(!filterString.equals("All collections") && !subFilters.isEmpty()){
				subFilters+=" OR f_author:\""+filterString+"\""; 
			}				
		}
		
		if(!subFilters.isEmpty()){
			subFilters+=")";
			filters.add(subFilters);
			subFilters="";
		}
		
		for(int i : uplCombo.getSelections()){
			
			if(i>=uplCombo.textItems.length) {
				return;
			}

			String filterString = uplCombo.textItems[i].replaceAll("\\(.*\\)", "").trim();
			if(!filterString.equals("All uploaders") && subFilters.isEmpty()){
				subFilters+="(f_uploader:\""+filterString+"\""; 
			}else if(!filterString.equals("All uploaders") && !subFilters.isEmpty()){			
				subFilters+=" OR f_uploader:\""+filterString+"\"";
			}
		}
		
		if(!subFilters.isEmpty()){
			subFilters+=")";
			filters.add(subFilters);
			subFilters="";
		}
		
		for(int i : docCombo.getSelections()){
			
			if(i>=docCombo.textItems.length) {
				return;
			}

			String filterString = docCombo.textItems[i].replaceAll("\\(.*\\)", "").trim();
			if(!filterString.equals("All documents") && subFilters.isEmpty()){
				subFilters+="(f_title:\""+filterString+"\""; 
			}else if(!filterString.equals("All documents") && !subFilters.isEmpty()){			
				subFilters+=" OR f_title:\""+filterString+"\"";
			}				
		}

		if(!subFilters.isEmpty()){
			subFilters+=")";
			filters.add(subFilters);
			subFilters="";
		}

		
		logger.debug("filters:"+filters);
		
	}

	private void updateResultsTable() {
     
		numPageHits = (int) fullTextSearchResult.getNumResults();
		int max = (start+rows) > numPageHits ? numPageHits : (start+rows);
		resultsLabel.setText("Showing Pagehits "+(start)+" to "+(max)+" of "+(numPageHits));
		
        hits = new ArrayList<Hit>();
        
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
        		
        		String pUrl = pHit.getPageUrl();
        		Hit hit = new Hit(hlString, (int)pHit.getDocId(), (int)pHit.getPageNr(), regId, linId, worId, pxCoords, pUrl);
        		hit.setCollectionIds(colIds);
        		hits.add(hit);
        		numTags += tags.size();
        	}
        	
        }

        viewer.setInput(hits);
        
        Runnable loadPrevImg = new Runnable(){
        	
        	public boolean stopFlag = false;
        	
        	public void run() {
        		try{
	        		for(Hit hit : hits){
	        			
	        			if(Thread.currentThread().isInterrupted()) stopFlag = true;
	        			if (stopFlag) return;
	        			String coords = hit.getPixelCoords();
						String imgKey = hit.getImgUrl().replace("https://dbis-thure.uibk.ac.at/f/Get?id=", "");
						imgKey = imgKey.replace("&fileType=view", "");
						try {						
							Image img;							
							int[] cropValues = getCropValues(coords);
							
							if(prevImages.keySet().contains(coords)){
								//Do Nothing
							}else{
								URL url = imgStoreClient.getUriBuilder().getImgCroppedUri(imgKey, cropValues[0], cropValues[1], cropValues[2], cropValues[3]).toURL();
								img = ImageDescriptor.createFromURL(url).createImage();		
								prevImages.put(coords, img);
//								logger.debug("Background | Loaded img coords: "+coords);
							}
							
						} catch (Exception ex) {								
							ex.printStackTrace();
						}	
		        			
        			}
	        		logger.debug("Background word thumbnail loading complete");
	        		}catch(Exception iEx){
	        			logger.debug("Exception"+iEx);
	        		}   	
        	}
        };
        
        if(imgLoaderThread != null){
        	imgLoaderThread.interrupt();
        	logger.debug("Thread interrupted");
        }
    	imgLoaderThread = new Thread(loadPrevImg,"WordThmbLoaderThread");
    	imgLoaderThread.start();
        imgLoaderThread.setPriority(Thread.MIN_PRIORITY);        
        logger.debug("Image loading thread started. Nr of imgages: "+hits.size());   
//        else{
//        	try{
//	        	imgLoaderThread.interrupt(); 
//	        	logger.debug("Thread interrupted");
//	        	imgLoaderThread = new Thread(loadPrevImg);
//        	}catch(Exception e){
//        		
//        	}
//        }
    
               
    
        
        //loadPrevImg.run();
		
	}
	
	  private static class Hit
	  {
	    String highlightText;
	    String imgUrl;

		String regionId, lineId, wordId;
	    private String pixelCoords;
	    private ArrayList<Integer> collectionIds;

		int docId, pageNr;
		      
	    Hit(String hl, int doc, int page, String region, String line, String word, String coords, String url){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;
	    	regionId = region;
	    	lineId = line;
	    	wordId = word;	  
	    	pixelCoords = coords;
	    	imgUrl = url;
	    }
	    
	    Hit(String hl, int doc, int page){
	    	highlightText = hl;
	    	docId = doc;
	    	pageNr = page;	    	
	    }
	    
	    public String getImgUrl() {
			return imgUrl;
		}

		public void setImgUrl(String imgUrl) {
			this.imgUrl = imgUrl;
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
	  
	  private class HoverShell{		  
		  Shell hoverShell;
		  Label imgLabel;
		  Image wordPrev;
		  public HoverShell(Shell shell){
		  hoverShell = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
		  hoverShell.setLayout(new FillLayout());
		  imgLabel = new Label(hoverShell, SWT.NONE);	
		  imgLabel.setImage(wordPrev);
		  }

		 } 

}