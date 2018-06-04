package eu.transkribus.swt_gui.search.kws_solr;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.InvocationCallback;

import org.apache.commons.lang3.StringUtils;
import org.dea.fimgstoreclient.FimgStoreGetClient;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.TrpFimgStoreConf;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.kws.TrpKwsHit;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.searchresult.FulltextSearchResult;
import eu.transkribus.core.model.beans.searchresult.KeywordHit;
import eu.transkribus.core.model.beans.searchresult.KeywordSearchResult;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.Msgs;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.fulltext.FullTextSearchComposite;
import eu.transkribus.swt_gui.search.kws.KwsHitTableWidget;

public class KWSearchComposite extends Composite{
	
	private final static boolean TEST_ONLY = true;
	
	private final static Logger logger = LoggerFactory.getLogger(FullTextSearchComposite.class);
	FimgStoreGetClient imgStoreClient;
	Shell shell;
	Storage storage;
	
	private final static double MIN_CONF = 1.0;
	private final static double MAX_CONF = 100.0;
	private final static double DEFAULT_CONF = 25;
	private final static int THUMB_SIZE = 1; // size of slider thumb
	private static final DecimalFormat CONF_FORMAT = new DecimalFormat("0");	
	
	Group facetsGroup;
	Combo scopeCombo;
	Combo docCombo;	
	LabeledText inputText;
	Slider confSlider;
	Button searchBtn, searchPrevBtn, searchNextBtn;
	
	String searchWord;
	private int start = 0;
	private final int rows = 100;	
	
	private String sorting = "childfield(probability) desc";
	boolean prob_desc = true;
	boolean word_desc = false;
	boolean page_desc = false;
	
	Group resultsGroup;
	KeywordSearchResult kwSearchResult;
	ArrayList<KeywordHit> keywordHits;
	TableViewer tv;
	
	volatile Map<String,Image> imageMap;
	Thread imgLoaderThread;
	Image currentImgOrig;
	Image currentImgScaled;
	Canvas canvas;
	KeywordHit lastHoverHit = null;
		
	public KWSearchComposite(Composite parent, int style){
		super(parent, style);
		shell = parent.getShell();	
		try {
			imgStoreClient = new FimgStoreGetClient(new URL(TrpFimgStoreConf.getFimgStoreUrl()+"/"));
		} catch (Exception e) {
			logger.error("Could not create connection to FimgStore" + e);
			e.printStackTrace();
		}

		createContents();
		
	}
	
	ArrayList<String> DOCSCOPES = new ArrayList<String>();
	private void createContents(){
		
		storage = Storage.getInstance();
		this.setLayout(new FillLayout());
		Composite c = new Composite(this, 0);
		c.setLayout(new FillLayout());
				
		SashForm sf = new SashForm(c, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));			
		
		facetsGroup = new Group(sf, SWT.NONE);
		facetsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));	
		facetsGroup.setLayout(new GridLayout(2, false));
		facetsGroup.setText("Search HTR text for single words");
		
		Composite scopeComp = new Composite(facetsGroup, 0);
		scopeComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		scopeComp.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 3, 1));
		
		Label scopeLbl = new Label(scopeComp, SWT.NONE);
		scopeLbl.setText("Search in:");
		scopeCombo = new Combo(scopeComp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] SCOPES = new String[] { "All Collections", "1555"};
		scopeCombo.setItems(SCOPES);
		//FIXME Java Heap space error when to many confmats are loaded. Thus for now only scope "document"
		scopeCombo.select(1);
		scopeCombo.setEnabled(false);
		scopeCombo.addSelectionListener(new SelectionAdapter(){
			@Override public void widgetSelected(SelectionEvent e) {
				List<TrpDocMetadata> mdList = new ArrayList<TrpDocMetadata>();
				if(scopeCombo.getSelectionIndex() == 0) return;
				try {
					int collId = Integer.parseInt(scopeCombo.getItem(scopeCombo.getSelectionIndex()));
					mdList = storage.getConnection().getAllDocs(collId);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(mdList.size() != 0){
					DOCSCOPES = new ArrayList<String>();
					DOCSCOPES.add("All Documents");
					for(TrpDocMetadata md : mdList){
						DOCSCOPES.add(md.getTitle());
						logger.debug(""+md.getTitle());
					}
					String[] docArray = new String[DOCSCOPES.size()];
					docCombo.setItems(DOCSCOPES.toArray(docArray));
					docCombo.select(0);
				}
			}
		});
		
		List<TrpDocMetadata> docsInCollection;
		
		DOCSCOPES.add("All Documents");
		try {
			int collId = Integer.parseInt(scopeCombo.getItem(scopeCombo.getSelectionIndex()));
			docsInCollection = storage.getConnection().getAllDocs(collId);
			for(TrpDocMetadata md : docsInCollection){				
				DOCSCOPES.add(md.getTitle());
				logger.debug(""+md.getDocId());
			}
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e1) {
			e1.printStackTrace();
		}
		
		docCombo =  new Combo(scopeComp, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] docArray = new String[DOCSCOPES.size()];
		docArray = DOCSCOPES.toArray(docArray);
		docCombo.setItems(docArray);
		docCombo.select(0);
		
		
		TraverseListener findTagsOnEnterListener = new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					start = 0;
					sorting = "childfield(probability) desc";
					findKW();
				}
			}
		};
		
		inputText = new LabeledText(facetsGroup, "Search for keyword:");
		inputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		inputText.text.addTraverseListener(findTagsOnEnterListener);
		
		Composite sliderComp = new Composite(facetsGroup, SWT.NONE);
		sliderComp.setLayout(new GridLayout(3, false));
		sliderComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));
		
		Label sliderLabel = new Label(sliderComp, SWT.NONE);
		sliderLabel.setText("Confidence Threshold:");
		Text confValueTxt = new Text(sliderComp, SWT.BORDER);
		confValueTxt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		confSlider = new Slider(sliderComp, SWT.HORIZONTAL);
		confSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		confSlider.setMaximum(convertConfidenceToSliderValue(MAX_CONF) + THUMB_SIZE);
		confSlider.setThumb(THUMB_SIZE);
		confSlider.setMinimum(convertConfidenceToSliderValue(MIN_CONF));
		confSlider.setSelection(convertConfidenceToSliderValue(DEFAULT_CONF));
		
		confValueTxt.setText(CONF_FORMAT.format(getConfidenceSliderValue()));
		confValueTxt.setTextLimit(4);
		
		Composite btnsComp = new Composite(facetsGroup, 0);
		btnsComp.setLayout(new FillLayout(SWT.HORIZONTAL));
		btnsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		searchBtn = new Button(btnsComp, SWT.PUSH);
		searchBtn.setImage(Images.FIND);
		searchBtn.setText("Search!");
		searchBtn.setToolTipText("Search for keyword");
		searchBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				start = 0;
				sorting = "childfield(probability) desc";
				findKW();
			}
		});
		
		searchPrevBtn = new Button(btnsComp, SWT.PUSH);
		searchPrevBtn.setImage(Images.PAGE_PREV);
		searchPrevBtn.setText("Previous page");
		searchPrevBtn.setEnabled(false);
		searchPrevBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
					if(start > 0){
						start -= rows;
						findKW();
				}				
			}
		});
		
		searchNextBtn = new Button(btnsComp, SWT.PUSH);
		searchNextBtn.setImage(Images.PAGE_NEXT);
		searchNextBtn.setText("Next page");
		searchNextBtn.setEnabled(false);
		searchNextBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if(kwSearchResult != null){
					if((start+rows) < kwSearchResult.getNumResults()) {
						start += rows;
						findKW();
					}
				}				
			}
		});	
		
		confValueTxt.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				//DO nothing
			}

			@Override
			public void keyReleased(KeyEvent e) {
				final String text = confValueTxt.getText();
				Double value = getConfidenceSliderValue();
				if(!StringUtils.isEmpty(text)) {
					try {
						value = Double.parseDouble(text);
						confValueTxt.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
						if(value < MIN_CONF) {
							value = MIN_CONF;
						}
						if(value > MAX_CONF) {
							value = MAX_CONF;
						}
						setConfidenceSliderValue(value);
					} catch(NumberFormatException nfe) {
						confValueTxt.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					}
				}
			}
		});
		
		confSlider.addListener(SWT.Selection, new Listener(){
			@Override
			public void handleEvent(Event event){
				if(event.detail == SWT.DRAG){
					confValueTxt.setText(CONF_FORMAT.format(getConfidenceSliderValue()));								
				}
			}
		});
		
		confSlider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.NONE){
					confValueTxt.setText(CONF_FORMAT.format(getConfidenceSliderValue()));// + "%");
				}
			}
		});
		
		initResultsTable(sf);	
		initPreviewArea(sf);
		sf.setWeights(new int[] { 22, 48, 30 } );

		
	}
	
	private double prob2sigma(double x){
		
		if (x == 1.0){
			return x*100;
		}
		double z = 0;
		double lim = 4.53988e-05;
		double a = 0.2;
		z = (float) (x < lim ?  lim : (x > 1.0 ? 1.0 : x)); 
		double zz = 50.0 - (1.0 / a) * Math.log(1.0 / z - 1.0);
		if (zz < 0.000102575) zz = 0;
		if (zz > 100.0) zz = 100.0;
		return zz;
		
	}
	
	private double sigma2prob(double zz){
		double a = 0.2;
		double z = 1.0/(Math.exp( (50.0-zz)*a ) + 1);
		
		return z;
	}
		
	private void initPreviewArea(Composite cont){
		Group previewGrp = new Group(cont, SWT.NONE);
		previewGrp.setText(Msgs.get("search.kws.preview"));
		previewGrp.setLayout(new FillLayout());
		canvas = new Canvas(previewGrp, SWT.NONE);
		canvas.setBackground(Colors.getSystemColor(SWT.COLOR_GRAY));
		
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(currentImgOrig != null) {
					Rectangle client = canvas.getClientArea();
					if(currentImgScaled != null) {
						currentImgScaled.dispose();
					}
					currentImgScaled = Images.resize(currentImgOrig, client.width, client.height,
							Colors.getSystemColor(SWT.COLOR_GRAY));
					Rectangle imgBounds = currentImgScaled.getBounds();
					final int xOffset = (client.width - imgBounds.width) / 2; 
					e.gc.drawImage(currentImgScaled, xOffset, 0);
				}
			}
		});
		
		tv.getTable().addListener(SWT.MouseMove, new MouseMoveListener(tv.getTable()));		
		
	}
			
	private void initResultsTable(SashForm sf){
		resultsGroup = new Group(sf, SWT.NONE);
		resultsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		resultsGroup.setText("Search results");
		resultsGroup.setLayout(new GridLayout(1, false));	
		
		tv = new TableViewer(resultsGroup, SWT.FULL_SELECTION);
		Table table = tv.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
        tv.getTable().setHeaderVisible(true);
        tv.getTable().setLinesVisible(true);
        tv.setContentProvider(new ArrayContentProvider());
        
		tv.addDoubleClickListener(new IDoubleClickListener() {	
			@Override public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					logger.debug("Clicked! Doc: "+ ((KeywordHit)sel.getFirstElement()).getId()+ " Page: "+((KeywordHit)sel.getFirstElement()).getPageNr());
					KeywordHit clHit = (KeywordHit)sel.getFirstElement();
					Storage s = Storage.getInstance();		
					
					ArrayList<Integer> userCols = new ArrayList<>();
					for(TrpCollection userCol : s.getCollections()){
						userCols.add(userCol.getColId());
//						logger.debug("User collection: " + userCol.getColId());
					}
//					find collections in searchresult that user has access to
					int col = -1;
					for(Integer userColId : userCols){
						for(Integer hitColId : clHit.getColIds()){							
							if(userColId.equals(hitColId)){
								col = userColId;
							}
						}
					}
					logger.debug("Col: " + col);
					if(col != -1){
						int docId = Integer.parseInt(clHit.getId().split("_")[0]);
						int pageNr = clHit.getPageNr();
						TrpLocation l = new TrpLocation();
						
						l.collId = col;
						l.docId = docId;
						l.pageNr = pageNr;		
						l.shapeId=clHit.getLineId();
	
						TrpMainWidget.getInstance().showLocation(l);
					}
				}
				
			}
		});
		
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Probability");
		tc.setWidth(90);
		
		TableViewerColumn probCol = new TableViewerColumn(tv, tc);
		
		Listener sortListenerProb = new Listener(){
			public void handleEvent(Event e){
				TableColumn col = (TableColumn) e.widget;
				prob_desc = !prob_desc;
				if(prob_desc){
					sorting = "childfield(probability) desc";
				}else{
					sorting = "childfield(probability) asc";
				}
				start = 0;
				findKW();
			}
		};	
        probCol.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element) {
                KeywordHit hit = (KeywordHit)element;  
                double sigma = prob2sigma((double)hit.getProbability());
                return "" + CONF_FORMAT.format(sigma);
            }
        });		
        tc.addListener(SWT.Selection, sortListenerProb);
        
        tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Word");
		tc.setWidth(100);		
		TableViewerColumn wordCol = new TableViewerColumn(tv, tc);		
        wordCol.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element) {
                KeywordHit hit = (KeywordHit)element;  
                return ""+hit.getWord();
            }
        });	
        
		Listener sortListenerWord = new Listener(){
			public void handleEvent(Event e){
				TableColumn col = (TableColumn) e.widget;
				word_desc = !word_desc;
				if(word_desc){
					sorting = "childfield(word) desc";
				}else{
					sorting = "childfield(word) asc";
				}
				start = 0;
				findKW();
			}
		};	
		tc.addListener(SWT.Selection, sortListenerWord);
        
        tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Document");
		tc.setWidth(300);		
		TableViewerColumn docCol = new TableViewerColumn(tv, tc);		
        docCol.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element) {
                KeywordHit hit = (KeywordHit)element;  
                return ""+hit.getDocTitle();
            }
        });
        
        tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Page");
		tc.setWidth(80);		
		TableViewerColumn pageCol = new TableViewerColumn(tv, tc);		
        pageCol.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element) {
                KeywordHit hit = (KeywordHit)element;  
                return ""+hit.getPageNr();
            }
        });
		Listener sortListenerPage = new Listener(){
			public void handleEvent(Event e){
				TableColumn col = (TableColumn) e.widget;
				page_desc = !page_desc;
				if(page_desc){
					sorting = "pageNr desc";
				}else{
					sorting = "pageNr asc";
				}
				start = 0;
				findKW();
			}
		};
		tc.addListener(SWT.Selection, sortListenerPage);
        
        tc = new TableColumn(table, SWT.LEFT);
		tc.setText("Line ID");
		tc.setWidth(150);		
		TableViewerColumn lineCol = new TableViewerColumn(tv, tc);		
        lineCol.setLabelProvider(new ColumnLabelProvider(){
            @Override
            public String getText(Object element) {
                KeywordHit hit = (KeywordHit)element;  
                return ""+hit.getLineId();
            }
        });		
	}
	
	private double getConfidenceSliderValue() {
		return confSlider.getSelection();
	}
	
	private void setConfidenceSliderValue(Double value) {
		confSlider.setSelection(convertConfidenceToSliderValue(value));
	}
	
	private int convertConfidenceToSliderValue(Double value) {
		if(value == null) {
			throw new IllegalArgumentException("Value must not be null");
		}
		final Double sliderVal = value;
		return sliderVal.intValue();
	}
	
	private void findKW(){
		storage = Storage.getInstance();
		
		canvas.setBackground(Colors.getSystemColor(SWT.COLOR_GRAY));
		canvas.redraw();;
			
		if(TEST_ONLY){
			final String testServer = "https://transkribus.eu/TrpServerTesting";
			
			if(!storage.getCurrentServer().equals(testServer)){
				TrpMainWidget.getInstance().onError(
						"Error searching keyword", "Keyword search (solr) is currently only supportet on Trp Testserver",
						null, false, false);
				return;
			}
		}		
		
		searchWord = inputText.getText().trim();
		if(searchWord.isEmpty()) {
			return;
		}		
		
		//Async search
		InvocationCallback<KeywordSearchResult> callback = new InvocationCallback<KeywordSearchResult>() {

			@Override
			public void completed(KeywordSearchResult response) {
				kwSearchResult = response;
				if(kwSearchResult != null){	
					Display.getDefault().asyncExec(()->{
						logger.debug("searched word: "+searchWord);
						logger.debug("num hits: "+kwSearchResult.getNumResults());
						updateResultsTable();
					}); 					
				}else{				
//					tv.getTable().clearAll();
//					tv.refresh();
//					shell.redraw();
				}
			}

			@Override
			public void failed(Throwable throwable) {
				logger.error("Fulltext search failed."+ throwable);
				Display.getDefault().asyncExec(() -> {
					TrpMainWidget.getInstance().onError("Error searching keyword", throwable.getMessage(), throwable);
				});
			}			
		};
		
		ArrayList<String> filters = new ArrayList<String>();		
		if(docCombo.getSelectionIndex() != 0){
			String filterTitle = docCombo.getItem(docCombo.getSelectionIndex());
//			Solr requires this format for spaces in title -> title:"xxx xxx"
			filterTitle = filterTitle.replaceAll("'", "");
			filterTitle = '"'+filterTitle+'"';
			filters.add("title:"+filterTitle);
		}	
		if (scopeCombo.getSelectionIndex() != 0){
			String filterCollection = scopeCombo.getItem(scopeCombo.getSelectionIndex());
			filters.add("collectionId:"+filterCollection);
		}
		
		logger.debug("filters:" + filters);
		
		float probLow = (float)sigma2prob(getConfidenceSliderValue());
		System.out.println(probLow);
		
		try{
			storage.getConnection().searchKWAsync(searchWord, start, rows, probLow, 1.0f, filters, sorting, 0, callback);
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
		
	private void updateResultsTable(){
		
		if(kwSearchResult == null) return;
		
		if(start > 0){
			searchPrevBtn.setEnabled(true);
		}else{
			searchPrevBtn.setEnabled(false);
		}
		
		if(kwSearchResult.getNumResults()>start+rows){
			searchNextBtn.setEnabled(true);
		}else{
			searchNextBtn.setEnabled(false);
		}
		
		long numResults = kwSearchResult.getNumResults();
		int pages = (int) Math.ceil((double) numResults / (double) rows);
		int currentPage = Math.floorDiv(start, rows)+1;
		
		String searchOutput;
		if(numResults>0){
			searchOutput = String.format("Search results (%d hits, page %d of %d):",numResults, currentPage, pages);
		}else{
			searchOutput = "Search results (no matches):";
		}		
		
		resultsGroup.setText(searchOutput);
		
		keywordHits = (ArrayList<KeywordHit>) kwSearchResult.getKeywordHits();	
		
		Runnable loadPreviewImages = new Runnable(){
			
		public void run(){
				if(imageMap!= null){
					imageMap.clear();
				}
				imageMap = new HashMap<String,Image>();
				for(KeywordHit kwHit : keywordHits){
					putInImageMap(kwHit);		
				}				
			}
		};

		if(imgLoaderThread != null){
			imgLoaderThread.interrupt();
			logger.debug("Image loading thread interrupted");
        }
		imgLoaderThread = new Thread(loadPreviewImages,"WordThmbLoaderThread");
		imgLoaderThread.start();
		imgLoaderThread.setPriority(Thread.MIN_PRIORITY);
		logger.debug("Image loading thread started. Nr of imgages: "+keywordHits.size());  
		
		tv.setInput(kwSearchResult.getKeywordHits());		
		tv.refresh();		
		shell.redraw();		
	}
	
	public void putInImageMap(KeywordHit kwHit){
//		String imgKey = kwHit.getPageUrl().replace("https://dbis-thure.uibk.ac.at/f/Get?id=", "");
//		imgKey = imgKey.replace("&fileType=view", "");
		
		//Extract key from URL
		String imgKey = StringUtils.substringBetween(kwHit.getPageUrl(), "Get?id=", "&fileType=view");	

		String coords = kwHit.getTextCoords();
		String imgId = kwHit.getId();
		
		if(imageMap.containsKey(imgId)) return;
		
		int[] cropValues = FullTextSearchComposite.getCropValues(coords);
		URL url;
		Image img = null;			
		try {
			url = imgStoreClient.getUriBuilder().getImgCroppedUri(imgKey, cropValues[0], cropValues[1], cropValues[2], cropValues[3]).toURL();
			img = ImageDescriptor.createFromURL(url).createImage();

		} catch (MalformedURLException | IllegalArgumentException e1) {
			e1.printStackTrace();
		}
		imageMap.put(imgId, img);
	}
	
	private class MouseMoveListener implements Listener {
		
		final Table table;
		KeywordHit currentHit;
		Thread singleImageLoaderThread;
		
		public MouseMoveListener(Table resultTable) {
			this.table = resultTable;
		}
		
		Runnable loadPreviewImage = new Runnable(){

			@Override
			public void run() {
				if(currentHit != null){
					putInImageMap(currentHit);
					currentImgOrig = imageMap.get(currentHit.getId());
					Display.getDefault().asyncExec(()->{
						canvas.redraw();
					});
				}
			}
			
		};
		
		public void handleEvent(Event e) {
			Point p = new Point(e.x, e.y);

			TableItem hoverItem = table.getItem(p);
			

			if (hoverItem != null 
					&& (currentHit = ((KeywordHit) hoverItem.getData())) != null
					&& !currentHit.equals(lastHoverHit)) {				

				currentHit = (KeywordHit) hoverItem.getData();
//				logger.debug(currentHit.getId());
				
				if(imageMap.get(currentHit.getId()) != null){
					currentImgOrig = imageMap.get(currentHit.getId());

				}else{
					currentImgOrig = Images.LOADING_IMG;
					if(singleImageLoaderThread != null){
						singleImageLoaderThread.interrupt();
					}
					singleImageLoaderThread = new Thread(loadPreviewImage,"WordThmbLoaderThread");
					singleImageLoaderThread.start();
					singleImageLoaderThread.setPriority(Thread.MAX_PRIORITY);
				}
				canvas.redraw();
				lastHoverHit = currentHit;
			}
		}
	}
}


