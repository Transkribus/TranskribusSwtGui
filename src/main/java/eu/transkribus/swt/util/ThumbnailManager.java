package eu.transkribus.swt.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.nebula.widgets.gallery.MyDefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt_gui.dialogs.SimpleLaDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class ThumbnailManager extends Dialog {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailManager.class);
	
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
	protected Label totalTranscriptsLabel;
	
	protected GalleryItem group;
	

	protected ToolItem reload, showOrigFn, createThumbs, showPageManager;
//	protected TextToolItem infoTi;

	protected List<URL> urls;
	protected List<String> names=null;
	
	protected List<TrpTranscriptMetadata> transcripts;
	
	protected List<Integer> nrTranscribedLines;
	
//	protected NoGroupRenderer groupRenderer;
	protected AbstractGridGroupRenderer groupRenderer;
	
	static int thread_counter=0;
	
	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	private int totalLinesTranscribed = 0;
	
	private int maxWidth = 0;
	
	private static final boolean DISABLE_TRANSCRIBED_LINES=false;
	
	Shell shell;
	TrpDocMetadata docMd;

	private boolean GalleryItem;
	
	public static class ThmbImg {		
		Image image = null;
		URL url;
		TrpTranscriptMetadata transcript;
		boolean isError=false;
		int index;
		int segmentedLines;                        
		int transcribedLines;
		
		public ThmbImg(int index, URL url, TrpTranscriptMetadata transcript) {
			this.index = index;
			this.url = url;
			this.transcript = transcript;
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
				
				SebisStopWatch sw = new SebisStopWatch();
				
//				sw.start();
				image = ImgLoader.load(url);
//				sw.stop(true, "loading img time: ");
				
				if (!DISABLE_TRANSCRIBED_LINES) {
					//sw.start();
					//transcribedLines = countTranscribedLines(transcript.unmarshallTranscript());
					transcribedLines = transcript.getNrOfTranscribedLines();
					segmentedLines = transcript.getNrOfLines();
					//sw.stop(true, "loading lines time: ");
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
			totalLinesTranscribed = 0;
			
			for (int i=0; i<urls.size() && !cancel; ++i) {
				if (cancel)
					break;
				
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
						
						//text and background according to the nr of transcribed lines
						//setItemTextAndBackgroung(group.getItem(index), index, transcribedLines);

						//totalTranscriptsLabel.setText("Nr. of lines trancribed: " + totalLinesTranscribed);
						/*
						groupComposite.layout(true, true);
						labelComposite.redraw();
						groupComposite.redraw();
						*/
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
					
					@Override protected void onDone() {
						setItemTextAndBackground(group.getItem(index), index, transcribedLines, segmentedLines);
						//groupComposite.layout(true, true);
						//labelComposite.redraw();
						//groupComposite.redraw();
						gallery.redraw();
					}
				});	
			}
			
			logger.debug("thumbnail thread finished!");
		}
	} // end ThmbImgLoadThread
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {

		shell.setSize(1100, 800);
		SWTUtil.centerShell(shell);
				
		shell.open();
		shell.layout();
		
		reload();
		
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}
	
	public ThumbnailManager(Composite parent, int style) {
		super(parent.getShell(), style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));

		shell = new Shell(getParent(), style);
		shell.setText("Document Manager");
		
		docMd = Storage.getInstance().getDoc().getMd();
		
		FillLayout l = new FillLayout();
		l.marginHeight = 5;
		l.marginWidth = 5;
		shell.setLayout(l);
		
		Composite container = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		
		final ToolBar tb = new ToolBar(container, SWT.FLAT);
		tb.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));
		
		reload = new ToolItem(tb, SWT.PUSH);
		reload.setToolTipText("Reload");
		reload.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reload.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});
		
		createThumbs = new ToolItem(tb, SWT.PUSH);
		createThumbs.setToolTipText("Create thumbnails for this local document");
		createThumbs.setText("Create thumbs");
				
		groupComposite = new Composite(container, SWT.FILL);
		GridLayout gl = new GridLayout(1, false);
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		groupComposite.setLayoutData(gridData);
		
		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		statisticLabel = new Label(labelComposite, SWT.TOP);
		statisticLabel.setText("Document Overview of: " + docMd.getTitle());
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
					
		gallery = new Gallery(groupComposite, SWT.V_SCROLL | SWT.MULTI);
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
						
						//notifyListeners(SWT.Selection, e);
					}
				
			}
		});
				
	    final Menu contextMenu = new Menu(gallery);
	    gallery.setMenu(contextMenu);
	    
	    addMenuItems(contextMenu, EnumUtils.stringsArray(EditStatus.class));
	    
//	    contextMenu.addMenuListener(new MenuAdapter()
//	    {
//	        public void menuShown(MenuEvent e)
//	        {
//	            MenuItem[] items = contextMenu.getItems();
//	            for (int i = 0; i < items.length; i++)
//	            {
//	                items[i].dispose();
//	            }
//	            MenuItem newItem = new MenuItem(contextMenu, SWT.NONE);
//	            newItem.setText("Menu for " + gallery.getSelection()[0].getText());
//	        }
//	    });
		
		shell.pack();
		
		open();
	}
	
	protected void setup_layout_recognition(String pages) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		SimpleLaDialog laD = new SimpleLaDialog(shell);
		
		laD.create();
		//all selected pages are shown as default and are taken for segmentation
		laD.setPageSelectionToSelectedPages(pages);
		
		int ret = laD.open();

		if (ret == IDialogConstants.OK_ID) {
			final String pageStr = laD.getPages();
			final boolean doBlockSeg = laD.isDoBlockSeg();
			final boolean doLineSeg = laD.isDoLineSeg();
			logger.debug("collID " + Storage.getInstance().getCurrentDocumentCollectionId());
			logger.debug("docMd.getDocId() " + docMd.getDocId());
			logger.debug("pageStr " + pageStr);
			String jobId = Storage.getInstance().analyzeLayout(Storage.getInstance().getCurrentDocumentCollectionId(), docMd.getDocId(), pageStr, doBlockSeg, doLineSeg);
		}
	}

	private void addMenuItems(Menu contextMenu, String[] editStatusArray) {
		MenuItem tmp;
		
		Menu statusMenu = new Menu(contextMenu);
		MenuItem statusMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		statusMenuItem.setText("Edit Status");
		statusMenuItem.setMenu(statusMenu);
		
		Menu labelMenu = new Menu(contextMenu);
		MenuItem labelMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		labelMenuItem.setText("Edit Label");
		labelMenuItem.setMenu(labelMenu);
		
		Menu layoutMenu = new Menu(contextMenu);
		MenuItem layoutMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		layoutMenuItem.setText("Layout Analysis");
		layoutMenuItem.setMenu(layoutMenu);
		
		for (String editStatus : editStatusArray){
			tmp = new MenuItem(statusMenu, SWT.None);
			tmp.setText(editStatus);
			tmp.addSelectionListener(new EditStatusMenuItemListener());
		}
		
		//just dummy labels for testing
		tmp = new MenuItem(labelMenu, SWT.None);
		tmp.setText("GT");
		tmp.addSelectionListener(new EditLabelMenuItemListener());
		
		tmp = new MenuItem(labelMenu, SWT.None);
		tmp.setText("eLearning");
		tmp.addSelectionListener(new EditLabelMenuItemListener());
		
		tmp = new MenuItem(layoutMenu, SWT.None);
		tmp.setText("Setting Up");
		
		tmp.addListener(SWT.Selection, event-> {
			String pages = getPagesString();
            try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
		
		
		
	}
	
	private String getPagesString() {
		String pages = "";
		if (gallery.getSelectionCount() > 0) {
			for(GalleryItem si : gallery.getSelection()){
				int selectedPageNr = gallery.indexOf(si) + 1;
				String tmp = Integer.toString(selectedPageNr);
				pages += (pages.equals("")? tmp : ",".concat(tmp));
			}			
		}
		logger.debug("pages String " + pages);
		return pages;
	}

	/*
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 */
	class EditStatusMenuItemListener extends SelectionAdapter {
	    public void widgetSelected(SelectionEvent event) {
//	    	System.out.println("You selected " + ((MenuItem) event.widget).getText());
//	    	System.out.println("You selected cont.1 " + EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
//	    	System.out.println("You selected cont.2 " + EnumUtils.indexOf(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText())));

	    	Storage.getInstance().getTranscriptMetadata().setStatus(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
	    	try {
//				Storage.getInstance().saveTranscript(Storage.getInstance().getCurrentDocumentCollectionId(), null);
//				Storage.getInstance().setLatestTranscriptAsCurrent();
				gallery.redraw();
				gallery.deselectAll();
//			} catch (SessionExpiredException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
			} catch (ServerErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
	}
	
	/*
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 */
	class EditLabelMenuItemListener extends SelectionAdapter {
	    public void widgetSelected(SelectionEvent event) {
//	    	System.out.println("You selected " + ((MenuItem) event.widget).getText());
//	    	System.out.println("You selected cont.1 " + EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
//	    	System.out.println("You selected cont.2 " + EnumUtils.indexOf(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText())));

//	    	Storage.getInstance().getTranscriptMetadata().setStatus(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
//	    	try {
////				Storage.getInstance().saveTranscript(Storage.getInstance().getCurrentDocumentCollectionId(), null);
////				Storage.getInstance().setLatestTranscriptAsCurrent();
//				gallery.redraw();
//				gallery.deselectAll();
////			} catch (SessionExpiredException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
//			} catch (ServerErrorException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	    	
	    }
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

		for (int i=0; i<urls.size(); ++i) {
			final GalleryItem item = new GalleryItem(group, SWT.MULTI);
//			item.setText(0, "String 0\nString2");
//			item.setText(1, "String 1");
			item.setExpanded(true);
			
			item.setImage(Images.LOADING_IMG);
			item.setData("doNotScaleImage", new Object());
			
			String transcribedLinesText = "";
			//here we could set background color regarding to if transcribed text exists
			
			/*
			 * get number or transcribed lines: 0: no lines available, -1: lines but no text, n lines with text
			 */

			int transcribedLines = transcripts.get(i).getNrOfTranscribedLines();
			int segmentedLines = transcripts.get(i).getNrOfLines();
			
			if (segmentedLines == 0){
				transcribedLinesText = "\nNo lines segmented";
			}
			else{
				transcribedLinesText = (transcribedLines > 0 ? "\nTranscribed lines: "+transcribedLines : "\nTranscribed lines: 0");
			}
			
			if (transcribedLines > 0){
				totalLinesTranscribed += transcribedLines;
//					String text = item.getText();
//					String tlText = "\nTranscribed lines: "+transcribedLines;
//									
//					
//					text += "\nTranscribed lines: "+transcribedLines;

				item.setBackground(lightGreen);
			}
			else if(transcribedLines == 0){
				item.setBackground(lightYellow);
			}
			else{
				item.setBackground(lightRed);
			}
//				if (checkIfTranscribed(transcripts.get(i).unmarshallTranscript())){
//					//item.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
//					item.setBackground(lightGreen);
//				}
//				else{
//					item.setBackground(lightRed);
//				}

			setItemText(item, i, transcribedLinesText);
			
		}
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
		
		// set url and page data:
		setUrls(storage.getDoc().getThumbUrls(), storage.getDoc().getPageImgNames());
		
		setTranscripts(storage.getDoc().getTranscripts());
		
		//setImageTexts();
		
		// create new gallery items:
		createGalleryItems();
		
		if (storage.getDoc() != null){
			if(pageNrLabel != null && !pageNrLabel.isDisposed()){
				pageNrLabel.dispose();
			}
			if(totalTranscriptsLabel != null && !totalTranscriptsLabel.isDisposed()){
				totalTranscriptsLabel.dispose();
			}
			pageNrLabel = new Label(labelComposite, SWT.NONE);
			pageNrLabel.setText("Nr of pages: " + storage.getDoc().getNPages());
			
			totalTranscriptsLabel = new Label(labelComposite, SWT.None);
			totalTranscriptsLabel.setText("Nr. of lines trancribed: " + totalLinesTranscribed);

			groupComposite.layout(true, true);
		}


		
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
	
	private void setItemTextAndBackground(GalleryItem galleryItem, int index, int transcribedLines, int segmentedLines) {
		if (SWTUtil.isDisposed(galleryItem))
			return;
		
		String transcribedLinesText = "";
		
		//int transcribedLines;

				//transcribedLines = countTranscribedLines(transcripts.get(index).unmarshallTranscript());
	
			//here we could set background color regarding to if transcribed text exists
	
			/*
			 * get number or transcribed lines: 0: no lines available, -1: lines but no text, n lines with text
			 */
			//int transcribedLines = nrTranscribedLines.get(index);
					
			if (segmentedLines == 0){
				transcribedLinesText = "\nNo lines segmented";
			}
			else{
				transcribedLinesText = (transcribedLines > 0 ? "\nTranscribed lines: "+transcribedLines : "\nTranscribed lines: 0");
			}
			
			if (transcribedLines > 0){
				totalLinesTranscribed += transcribedLines;
	//				String text = item.getText();
	//				String tlText = "\nTranscribed lines: "+transcribedLines;
	//								
	//				
	//				text += "\nTranscribed lines: "+transcribedLines;
	
				galleryItem.setBackground(lightGreen);
			}
			else if(transcribedLines == 0){
				galleryItem.setBackground(lightYellow);
			}
			else{
				galleryItem.setBackground(lightRed);
			}
			
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
			
			text+= (transcripts.get(i)!= null ? "\n"+transcripts.get(i).getUserName() : "");
			if (transcripts.get(i)!= null){
				tmp = gc.textExtent(transcripts.get(i).getUserName()).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
			}
			
			if (!DISABLE_TRANSCRIBED_LINES && !transcribedLinesText.equals("")){
				text+=transcribedLinesText;
				tmp = gc.textExtent(transcribedLinesText).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
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
	
	public ToolItem getCreateThumbs() {
		return createThumbs;
	}

}
