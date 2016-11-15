package eu.transkribus.swt.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.MyDefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt.util.ThumbnailWidget.ThmbImgLoadThread;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ThumbnailWidgetVirtualMinimal extends Composite {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailWidgetVirtualMinimal.class);

	static final int TEXT_TO_THUMB_OFFSET = 5;

	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;

//	protected List<ThmbImg> thumbs = new ArrayList<>();

	protected ThmbImgLoadThread loadThread;

	protected Gallery gallery;

	protected Composite groupComposite;

//	protected Label statisticLabel;
//	protected Label pageNrLabel;
//	protected Label totalTranscriptsLabel;

	protected GalleryItem group;

	protected TrpDoc doc;

	protected AbstractGridGroupRenderer groupRenderer;
	protected MyDefaultGalleryItemRenderer ir;

	static int thread_counter = 0;

	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	private int totalLinesTranscribed = 0;

	private int maxWidth = 0;

	private static final boolean DISABLE_TRANSCRIBED_LINES = false;

	public ThumbnailWidgetVirtualMinimal(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout());

		groupComposite = new Composite(this, SWT.NONE);
		GridLayout gl = new GridLayout(1, false);
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupComposite.setLayoutData(gridData);

		gallery = new Gallery(groupComposite, SWT.V_SCROLL | SWT.MULTI | SWT.VIRTUAL);

		gallery.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		group = new GalleryItem(gallery, SWT.VIRTUAL);

		groupRenderer = new NoGroupRenderer();

		groupRenderer.setMinMargin(2);
		groupRenderer.setItemHeight(THUMB_HEIGHT);
		groupRenderer.setItemWidth(THUMB_WIDTH);
		groupRenderer.setAutoMargin(true);
		groupRenderer.setAlwaysExpanded(true);

		gallery.setGroupRenderer(groupRenderer);

		ir = new MyDefaultGalleryItemRenderer();
		ir.setShowLabels(true);
		gallery.setItemRenderer(ir);

		gallery.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (gallery.getSelectionCount() >= 1) {
					Event e = new Event();
					e.index = group.indexOf(gallery.getSelection()[0]);

					// if (names != null && names.size()>e.index) {
					// infoTi.setText(names.get(e.index));
					// } else
					// infoTi.setText(""+e.index);
					// infoTi.setWidth(100);
					// btns.pack();
					//
					notifyListeners(SWT.Selection, e);
				}

			}
		});

		// virtual table stuff:
		gallery.setVirtualGroups(true);
		gallery.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
				System.out.println("setting data: " + event);
				System.out.println("item: " + event.item);

				GalleryItem item = (GalleryItem) event.item;
				int index;
				if (item.getParentItem() != null) { // if this is a leaft item
													// -> set nr of items to 0!
					index = item.getParentItem().indexOf(item);
					item.setItemCount(0);
				} else {
					index = gallery.indexOf(item);
					item.setItemCount(doc.getThumbUrls().size());
				}

				System.out.println("setData index " + index); //$NON-NLS-1$
				
				item.setExpanded(true);

				try {
					item.setImage(ImgLoader.load(doc.getPages().get(index).getThumbUrl()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
//				item.setImage(Images.LOADING_IMG);
				
				item.setData("doNotScaleImage", new Object());

				setItemTextAndBackground(item, index);
			}
		});
		gallery.setItemCount(1);
		// END virtual table stuff

		this.pack();
	}

	public void setDoc(TrpDoc doc) {
		this.doc = doc;
		reload();
	}

	private void disposeOldData() {
		// dispose images:
//		for (ThmbImg th : thumbs) {
//			th.dispose();
//		}
//		thumbs.clear();
		// dispose gallery items:
		for (GalleryItem item : group.getItems()) {
			if (item != null) {
				item.clear();
				item.dispose();
			}
		}
	}

//	private void stopActiveThread() {
//		if (loadThread != null && loadThread.isAlive()) {
//			logger.debug("stopping thumbnail thread from thread: " + Thread.currentThread().getName());
//			loadThread.cancel = true;
//			try {
//				// loadThread.join(); // leads to deadlock... don't know why
//				loadThread.interrupt(); // works!
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			} finally {
//				logger.debug("thumbnail thread stopped: " + !loadThread.isAlive());
//			}
//		}
//	}

	private void reload() {
		if(doc == null) {
			return;
		}
		
		disposeOldData();
		group.clearAll();
		gallery.clearAll();
		
		group.setItemCount(doc.getNPages());
		
		GC gc = new GC(this);
		groupRenderer.layout(gc, group);
		gc.dispose();
		
		
		
		// remember index of selected item:
		int selectedIndex = -1;
		if (gallery.getSelectionCount() > 0) {
			GalleryItem si = gallery.getSelection()[0];
			// logger.debug("si = "+si);
			selectedIndex = gallery.indexOf(si);
		}

		// first: stop old thread
//		stopActiveThread();

		// dispose old images:

		

		// create a new thread and start it:
//		loadThread = new ThmbImgLoadThread();
//		boolean DO_THREADING = true;
//		if (DO_THREADING) {
//			logger.debug("starting thumbnail thread!");
//			loadThread.start();
//		}
//		else {
//			logger.debug("running thumbnail reload method");
//			loadThread.run(); // sequential version -> just call run() method
//		}
				


		// select item previously selected:
		logger.debug("previously selected index = " + selectedIndex + " n-items = " + group.getItemCount());
		if (selectedIndex >= 0 && selectedIndex < group.getItemCount()) {
			GalleryItem it = group.getItem(selectedIndex);
			logger.trace("it = " + it);
			if (it != null) {
				it.setExpanded(true);
				gallery.setSelection(new GalleryItem[] { it });
			}
		}
		
		
	}

	private void setItemTextAndBackground(GalleryItem item, int index) {
		if (SWTUtil.isDisposed(item)) {
			System.out.println("Item is disposed");
			return;
		}
			
		TrpTranscriptMetadata tmd = doc.getTranscripts().get(index);

		String transcribedLinesText = "";

		int transcribedLines = tmd.getNrOfTranscribedLines();
		int segmentedLines = tmd.getNrOfLines();

		// logger.debug("segmentedLines: " + segmentedLines);
		// logger.debug("transcribedLines: " + transcribedLines);

		if (segmentedLines == 0) {
			transcribedLinesText = "\nNo lines segmented";
		} else {
			transcribedLinesText = (transcribedLines > 0 ? "\nTranscribed lines: " + transcribedLines
					: "\nTranscribed lines: 0");
		}

		if (transcribedLines > 0) {
			totalLinesTranscribed += transcribedLines;

			item.setBackground(lightGreen);
		} else if (transcribedLines == 0 && segmentedLines > 0) {
			item.setBackground(lightYellow);
		} else {
			item.setBackground(lightRed);
		}

		// set item text
		String text = "" + (index + 1);

		GC gc = new GC(item.getParent());

		final List<TrpTranscriptMetadata> transcripts = doc.getTranscripts();
		final List<String> names = doc.getPageImgNames();
		if (/* showOrigFn.getSelection() && */names != null && index >= 0 && index < names.size()
				&& !names.get(index).isEmpty()) {
			// this shows the filename but is not really necessary in the
			// thumbnail view
			// text+=": "+names.get(i);
			text += ": ";
			int tmp = gc.textExtent(text).x + 10;
			maxWidth = Math.max(maxWidth, tmp);
			// logger.debug("/////user id" + transcripts.get(i).getUserName());
			// logger.debug("/////status" + transcripts.get(i).getStatus());
			text += (transcripts.get(index) != null ? transcripts.get(index).getStatus().getStr() : "");
			if (transcripts.get(index) != null) {
				// tmp =
				// gc.textExtent(transcripts.get(i).getStatus().getStr()).x +
				// 10;
				tmp = gc.textExtent(text).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
				// logger.debug("curr maxWidth " + maxWidth);
			}

			if (!DISABLE_TRANSCRIBED_LINES && !transcribedLinesText.equals("")) {
				text += transcribedLinesText;
				tmp = gc.textExtent(transcribedLinesText).x + 10;
				maxWidth = Math.max(maxWidth, tmp);
			}
		}

		int te = gc.textExtent(text).x + 10;
		int ty = gc.textExtent(text).y + 10;
		// groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, te));

		groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, maxWidth));
		groupRenderer.setItemHeight(THUMB_HEIGHT + ty);
		// logger.debug("thumbText " + text);
		item.setText(text);

		gc.dispose();

	}

//	public static class ThmbImg {
//		Image image = Images.LOADING_IMG;
//		URL url;
//		TrpTranscriptMetadata transcript;
//		boolean isError = false;
//		int index;
//
//		public ThmbImg(int index, URL url, TrpTranscriptMetadata transcript) {
//			this.index = index;
//			this.url = url;
//			this.transcript = transcript;
//			load();
//		}
//
//		public static Image scaleImageToHeight(Image im, int newHeight) {
//			Rectangle b = im.getBounds();
//
//			double sf = (double) newHeight / (double) b.height;
//			int newWidth = (int) (b.width * sf);
//			logger.debug("scaling to :" + newWidth + " x " + newHeight);
//
//			Image scaled = new Image(Display.getCurrent(), im.getImageData().scaledTo(newWidth, newHeight));
//			return scaled;
//		}
//
//		public static Image scaleImageToWidth(Image im, int newWidth) {
//			Rectangle b = im.getBounds();
//
//			double sf = (double) newWidth / (double) b.width;
//			int newHeight = (int) (b.height * sf);
//			logger.debug("scaling to :" + newWidth + " x " + newHeight);
//
//			Image scaled = new Image(Display.getCurrent(), im.getImageData().scaledTo(newWidth, newHeight));
//			return scaled;
//		}
//
//		private void load() {
//			try {
//				isError = false;
//				image = ImgLoader.load(url);
//			} catch (Exception e) {
//				// logger.error(e.getMessage(), e);
//				logger.debug(e.getMessage());
//				isError = true;
//				Display.getDefault().asyncExec(new Runnable() {
//					@Override
//					public void run() {
//						onError();
//					}
//				});
//			} finally {
//				Display.getDefault().asyncExec(new Runnable() {
//					@Override
//					public void run() {
//						onDone();
//					}
//				});
//			}
//		}
//
//		protected void onSuccess() {
//		}
//
//		protected void onError() {
//		}
//
//		protected void onDone() {
//		}
//
//		public void dispose() {
//			if (image != null && !image.isDisposed()) {
//				image.dispose();
//				image = null;
//			}
//		}
//
//		public Image getDisplayImage() {
//			if (isError)
//				return Images.ERROR_IMG;
//			else if (image == null)
//				return Images.LOADING_IMG;
//
//			else
//				return image;
//		}
//	}
//
//	class ThmbImgLoadThread extends Thread {
//		public boolean cancel = false;
//
//		public ThmbImgLoadThread() {
//			this.setName("ThmbImgLoadThread_" + (++thread_counter));
//		}
//
//		@Override
//		public void run() {
//			for(ThmbImg t : thumbs) {
//				t.load();
//			}
//			logger.debug("thumbnail thread finished!");
//		}
//	} // end ThmbImgLoadThread
}
