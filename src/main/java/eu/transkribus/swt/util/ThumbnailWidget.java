package eu.transkribus.swt.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.nebula.widgets.gallery.MyDefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ThumbnailWidget extends Composite {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailWidget.class);
	
	static final int TEXT_TO_THUMB_OFFSET = 5;
	
	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;
	
	protected List<ThmbImg> thumbs = new ArrayList<>();
	protected boolean thumbsLoaded;
	
	protected ThmbImgLoadThread loadThread;
	
//	TableViewer tv;
	protected Method setItemHeightMethod;
	protected Gallery gallery;
	
	protected Composite groupComposite;
	protected Composite labelComposite;
	
	protected Label statisticLabel;
	protected Label pageNrLabel;
	protected Label totalTranscriptsLabel;
	
	protected GalleryItem group;
	
	protected Button reload, showOrigFn, createThumbs, loadThumbs, showPageManager;
		
//	protected TextToolItem infoTi;

	protected List<URL> urls;
	protected List<String> names=null;
	
	protected List<TrpTranscriptMetadata> transcripts;
		
//	protected NoGroupRenderer groupRenderer;
	protected AbstractGridGroupRenderer groupRenderer;
	
	//static ThumbnailManager tm;
//	static DocumentManager ac;
	
	static int thread_counter=0;
	
	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	private int totalLinesTranscribed = 0;
	
	private int maxWidth = 0;
	
	private static final boolean DISABLE_TRANSCRIBED_LINES=false;
	
	public /*static*/ class ThmbImg {		
		Image image = null;
		URL url;
		TrpTranscriptMetadata transcript;
		boolean isError=false;
		int index;
		
		public ThmbImg(int index, URL url, TrpTranscriptMetadata transcript) {
			this.index = index;
			this.url = url;
			this.transcript = transcript;
			load();
		}
		
		public /*static*/ Image scaleImageToHeight(Image im, int newHeight) {
			Rectangle b = im.getBounds();
			
			double sf = (double)newHeight/(double)b.height;
			int newWidth = (int)(b.width * sf);
			logger.debug("scaling to :"+newWidth + " x "+newHeight);
			
			Image scaled =
				new Image(Display.getCurrent(), im.getImageData().scaledTo(newWidth, newHeight));
			return scaled;
		}
		
		public /*static*/ Image scaleImageToWidth(Image im, int newWidth) {
			Rectangle b = im.getBounds();
			
			double sf = (double)newWidth/(double)b.width;
			int newHeight = (int)(b.height * sf);
			logger.debug("scaling to :"+newWidth + " x "+newHeight);
			
			Image scaled =
				new Image(Display.getCurrent(), im.getImageData().scaledTo(newWidth, newHeight));
			return scaled;
		}		
		
		private void load() {
			try {
				isError = false;
				
				TrpSettings set = TrpMainWidget.getTrpSettings();
				if (set!=null && !set.isLoadThumbs()) {
					logger.trace("disabled thumbnail loading");
					image = Images.LOADING_IMG;
				} else {
					logger.trace("loading thumbnail: "+url);
					image = ImgLoader.load(url, null);
				}
								
//				if (image.getBounds().height > THUMB_HEIGHT) {
//					Image scaled = scaleImageToHeight(image, THUMB_HEIGHT);
//					image.dispose();
//					image = scaled;
//				}
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						onSuccess();
					}
				});
				
			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
				logger.debug(e.getMessage());
				isError = true;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						onError();
					}
				});
			} finally {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						onDone();
					}
				});
			}
		}
		
		protected void onSuccess() {
		}
		
		protected void onError() {	
		}
		
		protected void onDone() {			
		}
		
		public void dispose() {
			if (image != null && !image.isDisposed() && image!=Images.LOADING_IMG && image!=Images.ERROR_IMG) {
				image.dispose();
				image = null;
			}
		}
		
		public Image getDisplayImage() {
			if (isError)
				return Images.ERROR_IMG;
			else if (image == null)
				return Images.LOADING_IMG;
			
			else return image;
		}
	}
	
	public interface ThreadCompleteListener {
		void notifyOfThreadComplete(final Thread thread);
		void notifyImageLoaded(final int i);
	}
	
	class ThmbImgLoadThread extends Thread {		
		public boolean cancel=false;

		public ThmbImgLoadThread() {
			this.setName("ThmbImgLoadThread_"+(++thread_counter));
		}
		
		@Override
		public void run() {
			cancel = false;
			totalLinesTranscribed = 0;
			
			for (int i=0; i<urls.size() && !cancel; ++i) {
				if (cancel)
					break;
				
				if(thumbs.size() == i){
						thumbs.add(new ThmbImg(i, urls.get(i), transcripts.get(i)) {
						@Override
						protected void onSuccess() {
							if (cancel || index >= group.getItemCount() || index <0)
								return;
							
	//						logger.trace("thread: "+ThmbImgLoadThread.this.getName());
	//						logger.trace("group size: "+group.getItemCount());
	//						logger.trace("index: "+index);
	//						logger.trace("item: "+group.getItem(index));
							group.getItem(index).setImage(image);
							group.getItem(index).setData("doNotScaleImage", null);
							
							
							/*
							groupComposite.layout(true, true);
							labelComposite.redraw();
							groupComposite.redraw();
							*/
							gallery.redraw();
							
//							if (isManagerOpen(tm)){
//								tm.group.getItem(index).setImage(image);
//								tm.group.getItem(index).setData("doNotScaleImage", null);
//								tm.gallery.redraw();
//							}
						}
						@Override
						protected void onError() {
							if (cancel || index >= group.getItemCount() || index <0)
								return;						
							
							group.getItem(index).setImage(Images.ERROR_IMG);
							group.getItem(index).setData("doNotScaleImage", new Object());
							gallery.redraw();
							
//							if(isManagerOpen(tm)){
//								tm.group.getItem(index).setImage(Images.ERROR_IMG);
//								tm.group.getItem(index).setData("doNotScaleImage", new Object());
//								tm.gallery.redraw();
//							}
						}
						
						@Override protected void onDone() {
							setItemTextAndBackground(group.getItem(index), index);
							//groupComposite.layout(true, true);
							//labelComposite.redraw();
							//groupComposite.redraw();
							gallery.redraw();
//							if(isManagerOpen(tm)){
//								tm.setItemTextAndBackground(group.getItem(index), index);
//								tm.gallery.redraw();
//							}
						}
					});	
				}
			}
			thumbsLoaded = !cancel;
			logger.debug("thumbnail thread finished!");
		}
	} // end ThmbImgLoadThread
	
	public ThumbnailWidget(Composite parent, int style, TrpMainWidget mw) {
		super(parent, style);

		setLayout(new GridLayout());
				
		groupComposite = new Composite(this, SWT.FILL);
		GridLayout gl = new GridLayout(1, false);
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupComposite.setLayoutData(gridData);
		
		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		statisticLabel = new Label(labelComposite, SWT.TOP);
		statisticLabel.setText("Thumbnail Overview: ");
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		showPageManager = new Button(labelComposite, 0);
		showPageManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showPageManager.setText("Show Document Manager");
		
		showPageManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showPageManager(mw);
			}
		});
		
		Composite btns = new Composite(this, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.setLayout(SWTUtil.createGridLayout(3, false, 0, 0));
		reload = new Button(btns, SWT.PUSH);
		reload.setToolTipText("Reload thumbs");
		reload.setImage(Images.REFRESH);
		reload.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});
		
		createThumbs = new Button(btns, SWT.PUSH);
		createThumbs.setImage(Images.IMAGES);
//		createThumbs.setToolTipText("Create thumbnails for this local document");
		createThumbs.setText("Create thumbs for local doc");	
		
		loadThumbs = new Button(btns, SWT.CHECK);
		loadThumbs.setText("Load thumbs");
		loadThumbs.setToolTipText("Uncheck to *not* load thumbnail images in this widget\nWas introduced to prevent the nasty 'no more handles' issue");
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.LOAD_THUMBS_PROPERTY, TrpMainWidget.getTrpSettings(), loadThumbs);
		SWTUtil.onSelectionEvent(loadThumbs, e -> {
			reload();
		});
		
//		showPageManager = new Button(btns, SWT.PUSH);
//		showPageManager.setText("Document Overview");
//		
//		showPageManager.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				showPageManager();
//			}
//		});
					
		gallery = new Gallery(groupComposite, SWT.V_SCROLL | SWT.SINGLE);
		gallery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		group = new GalleryItem(gallery, SWT.NONE);
		
		groupRenderer = new NoGroupRenderer();
		//groupRenderer = new DefaultGalleryGroupRenderer();
		
		groupRenderer.setMinMargin(2);
		groupRenderer.setItemHeight(THUMB_HEIGHT);
		groupRenderer.setItemWidth(THUMB_WIDTH);
		groupRenderer.setAutoMargin(true);
		groupRenderer.setAlwaysExpanded(true);

		gallery.setGroupRenderer(groupRenderer);
		//gallery.setVirtualGroups(true);

		if (true) {
			MyDefaultGalleryItemRenderer ir = new MyDefaultGalleryItemRenderer();
//			DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
			ir.setShowLabels(true);
			gallery.setItemRenderer(ir);

		} else {
			ListItemRenderer ir = new ListItemRenderer();
			ir.setShowLabels(true);
			gallery.setItemRenderer(ir);
		}
		
		gallery.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {					
					if (gallery.getSelectionCount()>=1) {
						Event e = new Event();
						e.index = group.indexOf(gallery.getSelection()[0]);
						
//						if (names != null && names.size()>e.index) {
//							infoTi.setText(names.get(e.index));
//						} else 
//							infoTi.setText(""+e.index);
//						infoTi.setWidth(100);
						btns.pack();
						
						notifyListeners(SWT.Selection, e);
					}
				
			}
		});
		thumbsLoaded = false;
		this.pack();
	}
	
	protected void showPageManager(TrpMainWidget mw) {
					
		if (Storage.getInstance().getDoc() == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded");
			return;
		}
		
		mw.openDocumentManager();
//		if (isAdministrativeCenterIsOpen(ac)){
//			ac.getShell().setVisible(true);
//		} else {
//			ac = new DocumentManager(getShell(), SWT.NONE, mw, Storage.getInstance().getCollId());
//			ac.open();
//		}
	}
	
	private boolean isAdministrativeCenterIsOpen(DocumentManager ac){
		if(ac != null && ac.getShell() != null && !ac.getShell().isDisposed()){
			return true;
		}
		return false;
	}
	
	private boolean isManagerOpen(ThumbnailManager tm){
		if(tm != null && tm.getShell() != null && !tm.getShell().isDisposed()){
			return true;
		}
		return false;
	}
	
	private void updateGalleryItemNames() {
		for (int i=0; i<group.getItemCount(); ++i) {
			setItemText(group.getItems()[i], i, "");
		}
	}

	private void setUrls(List<URL> urls, List<String> names) {
		this.urls = urls;
		this.names = names;
		
//		if (urls != null)
//			reload();
	}
	
	private void createGalleryItems(){
		//add text
		
		if(group.getItemCount() > 0){
			return;
		}

		for (int i=0; i<urls.size(); ++i) {
			final GalleryItem item = new GalleryItem(group, SWT.MULTI);
//			item.setText(0, "String 0\nString2");
//			item.setText(1, "String 1");
			item.setExpanded(true);
			
			item.setImage(Images.LOADING_IMG);
			item.setData("doNotScaleImage", new Object());
			
			String transcribedLinesText = determineItemColor(item, transcripts.get(i));

			setItemText(item, i, transcribedLinesText);
			
		}
	}
	
	
	public String determineItemColor(GalleryItem item, TrpTranscriptMetadata trpTranscriptMetadata) {
		
		String transcribedLinesText = "";

		int transcribedLines = trpTranscriptMetadata != null ? trpTranscriptMetadata.getNrOfTranscribedLines() : 0;
		int segmentedLines = trpTranscriptMetadata != null ? trpTranscriptMetadata.getNrOfLines() : 0;
		
//		logger.debug("segmentedLines: " + segmentedLines);
//		logger.debug("transcribedLines: " + transcribedLines);
		
		if (segmentedLines == 0){
			transcribedLinesText = "\nNo lines segmented";
		}
		else{
			transcribedLinesText = (transcribedLines > 0 ? "\nTranscribed lines: "+transcribedLines : "\nTranscribed lines: 0");
		}
		
		if (transcribedLines > 0){
			totalLinesTranscribed += transcribedLines;

			item.setBackground(lightGreen);
		}
		else if(transcribedLines == 0 && segmentedLines > 0){
			item.setBackground(lightYellow);
		}
		else{
			item.setBackground(lightRed);
		}

		return transcribedLinesText;
	}

	/***
	 * 
	 * @param pc
	 * @return number or transcribed lines: if 0: no lines available, -1: lines but no text, >0 lines with text
	 */
	private static int countTranscribedLines(PcGtsType pc){
		int counter = 0;
		for( TextRegionType tr : PageXmlUtils.getTextRegions(pc)){			//Check all textregions of page
			for(TextLineType tl : tr.getTextLine()){						//Check all textlines of textregion
					TrpTextLineType ttl = (TrpTextLineType) tl;
					if(!ttl.getUnicodeText().isEmpty()){					//Check if textline is empty
						counter++;						
					}
			}
		}
		
		//no line contains text - check if there are lines
		if(counter == 0){
			for( TextRegionType tr : PageXmlUtils.getTextRegions(pc)){
				if(tr.getTextLine().size() > 0){
					counter = -1;
				}
			}
		}
		
		return counter;
	}
	
	private void disposeOldData() {
		// dispose images:
		for (ThmbImg th : thumbs) {
			th.dispose();
		}
		thumbs.clear();
		// dispose galler items:
		for (GalleryItem item : group.getItems() ) {
			item.clear();
			item.dispose();
		}
	}
	
	private void stopActiveThread() {		
		if (loadThread!=null && loadThread.isAlive()) {			
			logger.debug("stopping thumbnail thread from thread: "+Thread.currentThread().getName());
			loadThread.cancel = true;
			try {
//				loadThread.join(); // leads to deadlock... don't know why
				loadThread.interrupt(); // works!
			}
			catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			finally {
				logger.debug("thumbnail thread stopped: "+!loadThread.isAlive());
			}
		}
	}
	
	public void loadThumbsIntoManager() {
//		tm.setUrls(urls, names);
//		tm.setTranscripts(transcripts);
//		tm.createGalleryItems();
//		for (ThmbImg thumb : thumbs){
//			if (tm != null){
//				tm.group.getItem(thumb.index).setImage(thumb.image);
//				tm.group.getItem(thumb.index).setData("doNotScaleImage", null);
//				tm.gallery.redraw();
//			}
//		}
	}
		
	public void reload() {
//		TrpSettings sets = TrpMainWidget.getTrpSettings();
//		if (sets != null && !sets.isLoadThumbs()) {
//			logger.debug("disabled thumbnail loading!");
//			return;
//		}
		
		if ((loadThread != null && loadThread.isAlive()) || thumbsLoaded) {
			logger.debug("Thumbs already loaded or still loading. Skipping reload.");
			return;
		}
		
		Storage storage = Storage.getInstance();
		
		int N = !storage.isDocLoaded() ? 0 : storage.getDoc().getThumbUrls().size();
		
		/*
		 * TODO: 
		 * load transcripts
		 * show info later on in thumbnail widget
		 * 
		 */
		
		logger.debug("reloading thumbs, nr of thumbs = "+ N);
		
		// remember index of selected item:
		int selectedIndex=-1;
		if (gallery.getSelectionCount() > 0) {
			GalleryItem si = gallery.getSelection()[0];
//			logger.debug("si = "+si);
			selectedIndex = gallery.indexOf(si);
		}

		// first: stop old thread
		stopActiveThread();
		
		// dispose old images:

		disposeOldData();
//		if(isManagerOpen(tm)){
//			tm.disposeOldData();
//		}
		
		
//		logger.debug("reloading thumbs, is doc loaded: "+storage.isDocLoaded());
		if (!storage.isDocLoaded())
			return;
		
		// set url and page data:
		setUrls(storage.getDoc().getThumbUrls(), storage.getDoc().getPageImgNames());
		
		setTranscripts(storage.getDoc().getTranscripts());
		
		//setImageTexts();
		
		// create new gallery items:
		createGalleryItems();
//		if(isManagerOpen(tm)){
//			tm.setUrls(urls, names);
//			tm.setTranscripts(transcripts);
//			tm.createGalleryItems();
//		}
		
//		if (storage.getDoc() != null){
//			tm.createGalleryItems();
//			if(pageNrLabel != null && !pageNrLabel.isDisposed()){
//				pageNrLabel.dispose();
//			}
//			if(totalTranscriptsLabel != null && !totalTranscriptsLabel.isDisposed()){
//				totalTranscriptsLabel.dispose();
//			}
//			pageNrLabel = new Label(labelComposite, SWT.NONE);
//			pageNrLabel.setText("Nr of pages: " + storage.getDoc().getNPages());
//			
//			totalTranscriptsLabel = new Label(labelComposite, SWT.None);
//			totalTranscriptsLabel.setText("Nr. of lines trancribed: " + totalLinesTranscribed);
//
//			groupComposite.layout(true, true);
//		}


		
		// create a new thread and start it:
		loadThread = new ThmbImgLoadThread();
		boolean DO_THREADING = true;
		if (DO_THREADING) {
			logger.debug("starting thumbnail thread!");
			loadThread.start();
		}
		else {
			logger.debug("running thumbnail reload method");
			loadThread.run(); // sequential version -> just call run() method
		}
		

		
		// select item previously selected:
		logger.debug("previously selected index = "+selectedIndex+ " n-items = "+group.getItemCount());
		if (selectedIndex >= 0 && selectedIndex<group.getItemCount()) {
			GalleryItem it = group.getItem(selectedIndex);
			logger.trace("it = "+it);
			if (it != null) {
				it.setExpanded(true);
				gallery.setSelection( new GalleryItem[]{it} );
			}
		}	
	}
	
	private void setItemTextAndBackground(GalleryItem galleryItem, int index) {
		if (SWTUtil.isDisposed(galleryItem))
			return;
		
		String transcribedLinesText = determineItemColor(galleryItem, transcripts.get(index));
		
		setItemText(galleryItem, index, transcribedLinesText);
		
	}
	
	private void setTranscripts(List<TrpTranscriptMetadata> transcripts2) {
		this.transcripts = transcripts2;
	}

	private void setItemText(GalleryItem item, int i, String transcribedLinesText) {
		String text=""+(i+1);
		
		
		GC gc = new GC(item.getParent());
		
		if (/*showOrigFn.getSelection() && */names!=null && i>=0 && i<names.size() && !names.get(i).isEmpty()) {
			//this shows the filename but is not really necessary in the thumbnail view
			//text+=": "+names.get(i);
			text+=": ";
			int tmp = gc.textExtent(text).x + 10;
			maxWidth = Math.max(maxWidth, tmp);
//			logger.debug("/////user id" + transcripts.get(i).getUserName());
//			logger.debug("/////status" + transcripts.get(i).getStatus());
			text+= (transcripts.get(i)!= null ? transcripts.get(i).getStatus().getStr() : "");
			if (transcripts.get(i)!= null){
				//tmp = gc.textExtent(transcripts.get(i).getStatus().getStr()).x + 10;
				tmp = gc.textExtent(text).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
				//logger.debug("curr maxWidth " + maxWidth);
			}
			
//			text+= (transcripts.get(i)!= null ? "\n"+transcripts.get(i).getUserName() : "");
//			if (transcripts.get(i)!= null){
//				tmp = gc.textExtent(transcripts.get(i).getUserName()).x + 10;
//				maxWidth = Math.max(maxWidth, tmp);
//			}
//			
//			if (!DISABLE_TRANSCRIBED_LINES && !transcribedLinesText.equals("")){
//				text+=transcribedLinesText;
//				tmp = gc.textExtent(transcribedLinesText).x + 10;
//				maxWidth = Math.max(maxWidth, tmp);
//			}
				
		}
//		else
//			text="Page "+(i+1);
		
		
		
		if (false) { // FIXME: try to wrap text...
		int s=0, e=2;
		String wrapped="";
		do {
			if (e>=text.length()) {
				wrapped+=text.substring(s, text.length());
				break;
			}
			
			int extent = gc.textExtent(text.substring(s, e)).x;
			if (extent>THUMB_WIDTH) {
				wrapped+=text.substring(s, e-1)+"\n";
				s = e;
				e++;
			}
			e++;
		} while(true);
		
		logger.debug("wrapped:\n"+wrapped);
		item.setText(wrapped);
		} else {
			int te = gc.textExtent(text).x + 10;
			int ty = gc.textExtent(text).y + 10;
			//groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, te));
			
			groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, maxWidth));		
			groupRenderer.setItemHeight(THUMB_HEIGHT + ty);
			//logger.debug("thumbText " + text);
			item.setText(text);
		}
		
		gc.dispose();
	}

//	public void setImages(List<Image> thumbImages) {
//		tv.setInput(thumbImages);
//	}
//	public ThumbnailManager getThumbnailManager() {
//		return tm;
//	}
//	
//	public void setThumbnailManager(ThumbnailManager tm) {
//		this.tm = tm;
//	}
	
	public Button getCreateThumbs() {
		return createThumbs;
	}
	
	public Button getLoadThumbs() {
		return loadThumbs;
	}
	
	public boolean isLoadThumbs() {
		return loadThumbs.getSelection();
	}

	public void clear() {
		stopActiveThread();
		disposeOldData();
		thumbsLoaded = false;
	}

}
