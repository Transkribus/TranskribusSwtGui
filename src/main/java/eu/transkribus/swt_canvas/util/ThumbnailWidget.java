package eu.transkribus.swt_canvas.util;

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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class ThumbnailWidget extends Composite {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailWidget.class);
	
	static final int TEXT_TO_THUMB_OFFSET = 5;
	
	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;
	
	protected List<ThmbImg> thumbs = new ArrayList<>();
	
	protected ThmbImgLoadThread loadThread;
	
//	TableViewer tv;
	protected Method setItemHeightMethod;
	protected Gallery gallery;
	
	protected Composite groupComposite;
	protected Composite labelComposite;
	
	protected Label statisticLabel;
	protected Label pageNrLabel;
	
	protected GalleryItem group;
	

	protected ToolItem reload, showOrigFn;
//	protected TextToolItem infoTi;

	protected List<URL> urls;
	protected List<String> names=null;
	
	protected List<TrpTranscriptMetadata> transcripts;
	
//	protected NoGroupRenderer groupRenderer;
	protected AbstractGridGroupRenderer groupRenderer;
	
	static int thread_counter=0;
	
	public static class ThmbImg {		
		Image image = null;
		URL url;
		boolean isError=false;
		int index;
		
		public ThmbImg(int index, URL url) {
			this.index = index;
			this.url = url;
			load();
		}
		
		public static Image scaleImageToHeight(Image im, int newHeight) {
			Rectangle b = im.getBounds();
			
			double sf = (double)newHeight/(double)b.height;
			int newWidth = (int)(b.width * sf);
			logger.debug("scaling to :"+newWidth + " x "+newHeight);
			
			Image scaled =
				new Image(Display.getCurrent(), im.getImageData().scaledTo(newWidth, newHeight));
			return scaled;
		}
		
		public static Image scaleImageToWidth(Image im, int newWidth) {
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
				image = ImgLoader.load(url);
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
			}
		}
		
		protected void onSuccess() {
		}
		
		protected void onError() {	
		}
		
		public void dispose() {
			if (image != null && !image.isDisposed()) {
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
			
			for (int i=0; i<urls.size() && !cancel; ++i) {
				if (cancel)
					break;
				
				thumbs.add(new ThmbImg(i, urls.get(i)) {
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
						//just a try
						groupComposite.redraw();
						gallery.redraw();
					}
					@Override
					protected void onError() {
						if (cancel || index >= group.getItemCount() || index <0)
							return;						
						
						group.getItem(index).setImage(Images.ERROR_IMG);
						group.getItem(index).setData("doNotScaleImage", new Object());
						gallery.redraw();
					}
				});	
			}
			
			logger.debug("thumbnail thread finished!");
		}
	} // end ThmbImgLoadThread
	
	public ThumbnailWidget(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());
		
		final ToolBar tb = new ToolBar(this, SWT.FLAT);
		tb.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		reload = new ToolItem(tb, SWT.PUSH);
		reload.setToolTipText("Reload");
		reload.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reload.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});
		
//		infoTi = new TextToolItem(tb, SWT.FILL | SWT.READ_ONLY);
		
		
//		showOrigFn = new ToolItem(tb, SWT.CHECK);
//		showOrigFn.setToolTipText("Shows the original filenames when checked");
//		showOrigFn.setText("Original filenames");
//		showOrigFn.setImage(Images.getOrLoad(TrpMainWidgetView.class, "/icons/refresh.gif"));
//		
//		showOrigFn.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				updateGalleryItemNames();
//			}
//		});
		
		groupComposite = new Composite(this, SWT.FILL);
		GridLayout gl = new GridLayout(1, false);
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupComposite.setLayoutData(gridData);
		
		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		statisticLabel = new Label(labelComposite, SWT.TOP);
		statisticLabel.setText("Statistic: ");
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					
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
						tb.pack();
						
						notifyListeners(SWT.Selection, e);
					}
				
			}
		});
		
		this.pack();
	}
	
	private void updateGalleryItemNames() {
		for (int i=0; i<group.getItemCount(); ++i) {
			setItemText(group.getItems()[i], i);
		}
	}

	private void setUrls(List<URL> urls, List<String> names) {
		this.urls = urls;
		this.names = names;
		
//		if (urls != null)
//			reload();
	}
	
	private void createGalleryItems() {
		//add text
		
		for (int i=0; i<urls.size(); ++i) {
			final GalleryItem item = new GalleryItem(group, SWT.MULTI);
//			item.setText(0, "String 0\nString2");
//			item.setText(1, "String 1");
			item.setExpanded(true);
			setItemText(item, i);
			item.setImage(Images.LOADING_IMG);
			item.setData("doNotScaleImage", new Object());
		}
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
				logger.debug("thumbnail thread stopped!");
			}
		}
	}
		
	public void reload() {
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
		
//		logger.debug("reloading thumbs, is doc loaded: "+storage.isDocLoaded());
		if (!storage.isDocLoaded())
			return;
		
		if (storage.getDoc() != null){
			if(pageNrLabel != null && !pageNrLabel.isDisposed()){
				pageNrLabel.dispose();
			}
			pageNrLabel = new Label(labelComposite, SWT.NONE);
			pageNrLabel.setText("Nr of pages: " + storage.getDoc().getNPages());
			groupComposite.layout(true, true);
		}

		// set url and page data:
		setUrls(storage.getDoc().getThumbUrls(), storage.getDoc().getPageImgNames());
		
		setTranscripts(storage.getDoc().getTranscripts());
		
		// create new gallery items:
		createGalleryItems();
		
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
	
	private void setTranscripts(List<TrpTranscriptMetadata> transcripts2) {
		this.transcripts = transcripts2;
	}

	private void setItemText(GalleryItem item, int i) {
		String text=""+(i+1);
		
		int maxWidth = 0;
		GC gc = new GC(item.getParent());
		
		if (/*showOrigFn.getSelection() && */names!=null && i>=0 && i<names.size() && !names.get(i).isEmpty()) {
			text+=": "+names.get(i);
			int tmp = gc.textExtent(text).x + 10;
			maxWidth = Math.max(maxWidth, tmp);
//			logger.debug("/////user id" + transcripts.get(i).getUserName());
//			logger.debug("/////status" + transcripts.get(i).getStatus());
			text+= (transcripts.get(i)!= null ? "\n"+transcripts.get(i).getUserName() : "");
			if (transcripts.get(i)!= null){
				tmp = gc.textExtent(transcripts.get(i).getUserName()).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
			}
				
			text+= (transcripts.get(i)!= null ? "\n"+transcripts.get(i).getStatus() : "");
			if (transcripts.get(i)!= null){
				tmp = gc.textExtent(transcripts.get(i).getStatus().getStr()).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
				//logger.debug("curr maxWidth " + maxWidth);
			}
				
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
	
	

}
