package eu.transkribus.swt.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.MyDefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;

public class ThumbnailWidgetVirtualMinimal extends Composite {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailWidgetVirtualMinimal.class);

	static final int TEXT_TO_THUMB_OFFSET = 5;

	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;

	protected Gallery gallery;

	protected Composite groupComposite;

	protected GalleryItem group;

	public GalleryItem getGroup() {
		return group;
	}

	protected TrpDoc doc;

	protected AbstractGridGroupRenderer groupRenderer;
	protected MyDefaultGalleryItemRenderer ir;

	static int thread_counter = 0;

	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	private int totalLinesTranscribed = 0;
	private int totalWordsTranscribed = 0;

	private int maxWidth = 0;
	private int maxHeight = 0;

	private final boolean ENABLE_TRANSCRIBED_LINES;
	
	private boolean useGtVersions = false;
	private boolean showFilenames = false;

	public ThumbnailWidgetVirtualMinimal(Composite parent, final boolean displayTranscribedLines, int style) {
		super(parent, style);
		ENABLE_TRANSCRIBED_LINES = displayTranscribedLines;
		
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
		// virtual table stuff:
		gallery.setVirtualGroups(true);
		gallery.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) {
//				logger.debug("setting data: " + event);
//				logger.debug("item: " + event.item);

				final GalleryItem item = (GalleryItem) event.item;
				int index;
				if (item.getParentItem() != null) { // if this is a leaft item
													// -> set nr of items to 0!
					index = item.getParentItem().indexOf(item);
					item.setItemCount(0);
				} else {
					index = gallery.indexOf(item);
					item.setItemCount(doc.getThumbUrls().size());
				}

				//logger.debug("setData index " + index); //$NON-NLS-1$
				
				item.setExpanded(true);

				try {
					item.setImage(ImgLoader.load(doc.getPages().get(index).getThumbUrl()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				item.setData("doNotScaleImage", new Object());

				setItemTextAndBackground(item, index);
			}
		});
		
		gallery.addListener(SWT.MouseHover, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				GalleryItem item = gallery.getItem (new Point(event.x, event.y));
				if(item == null) {
					return;
				}
				int index;
				if (item.getParentItem() != null) { // if this is a leaft item
													// -> set nr of items to 0!
					index = item.getParentItem().indexOf(item);
				} else {
					index = gallery.indexOf(item);
				}
				gallery.setToolTipText(doc.getPages().get(index).getImgFileName());
			}
		});
		
		if (doc != null)
			gallery.setItemCount(doc.getNPages());
		else{
			gallery.setItemCount(1);
		}
		
		//should bring some improvement during image loading
		gallery.setAntialias(SWT.OFF);
		gallery.setInterpolation(SWT.LOW);
		// END virtual table stuff

		this.pack();
	}

	public void setDoc(TrpDoc doc, boolean useGtVersions) {
		this.doc = doc;
		this.useGtVersions = useGtVersions;
//		reload(doc.getId() == this.doc.getId());
		reload();
	}
	
	public TrpDoc getDoc() {
		return this.doc;
	}
	
	public Gallery getGallery() {
		return this.gallery;
	}
		
	public void setUseGtVersions(boolean useGtVersions) {
		this.useGtVersions = useGtVersions;
		reload();
	}
	
	public List<TrpPage> getSelection() {
		List<TrpPage> sList = new LinkedList<>();
		if (gallery.getSelectionCount() < 1) {
			return sList;
		}
		
		GalleryItem[] selection = gallery.getSelection();
		for(GalleryItem item : selection) {
			final int index = gallery.indexOf(item);
			
			TrpPage p = doc.getPages().get(index);
			sList.add(p);
		}
		
		return sList;
	}
	
	public DocumentSelectionDescriptor getSelectionDescriptor() {
		if (gallery.getSelectionCount() < 1) {
			return null;
		}
		DocumentSelectionDescriptor dsd = new DocumentSelectionDescriptor();
		dsd.setDocId(doc.getId());
		GalleryItem[] selection = gallery.getSelection();
		for(GalleryItem item : selection) {
			final int index = gallery.indexOf(item);
			
			TrpPage p = doc.getPages().get(index);
			
			PageDescriptor pd = new PageDescriptor();
			pd.setPageId(p.getPageId());
			//TODO determine which transcript should be chosen
			if(useGtVersions) {
				int tsId = p.getCurrentTranscript().getTsId();
				for(TrpTranscriptMetadata tmd : p.getTranscripts()) {
					if(tmd.getStatus().equals(EditStatus.GT)) {
						tsId = tmd.getTsId();
						break;
					}
				}
				pd.setTsId(tsId);
			} else {
				pd.setTsId(p.getCurrentTranscript().getTsId());
			}
			dsd.getPages().add(pd);
		}
		return dsd;
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


	void reload() {
		if(doc == null) {
			return;
		}
		
		
		
		//FIXME restoring previously selected items leads to ArithmeticException with SWT.Virtual
//		List<Integer> selection = new LinkedList<>();
//		if(rememberSelection) {
//			// remember index of selected item:
//			if (gallery.getSelectionCount() > 0) {
//				for(GalleryItem si : gallery.getSelection()) {
//					logger.debug("si = "+si);
//					selection.add(gallery.indexOf(si));
//				}
//			}	
			logger.debug("reload called ");
//		}
		disposeOldData();
		group.clearAll();
		gallery.clearAll();
		
		group.setItemCount(doc.getNPages());
		
		int index = 0;
		for (GalleryItem item : gallery.getItems()) {
			if (item != null) {
				setItemTextAndBackground(item, index++);
			}
		}
		
		//hack for reloading the display
//		GC gc = new GC(this);
//		groupRenderer.layout(gc, group);
//		gc.dispose();

		// select item previously selected:
//		if (!selection.isEmpty()) { //&& selectedIndex < group.getItemCount()) {
//			ArrayList<GalleryItem> giList = new ArrayList<>(selection.size());
//			for(Integer si : selection) {
//				if(si < group.getItemCount()) {
//					GalleryItem it = group.getItem(si);
//					logger.trace("it = " + it);
//					if (it != null) {
//						it.setExpanded(true);
//						giList.add(it);
//					}
//				}
//			}
//			logger.debug("Restoring selection: " + giList.size());
//			gallery.setSelection(giList.toArray(new GalleryItem[giList.size()]));
//		}
	}

	private void setItemTextAndBackground(GalleryItem item, int index) {
		
		if (SWTUtil.isDisposed(item)) {
			System.out.println("Item is disposed");
			return;
		}
		
		maxWidth = 0;
		maxHeight = 0;
			
		TrpTranscriptMetadata tmd;
		tmd = doc.getTranscripts().get(index);
		
		if(useGtVersions) {
			List<TrpTranscriptMetadata> tList = doc.getPages().get(index).getTranscripts();
			for(TrpTranscriptMetadata t : tList) {
				if(t.getStatus().equals(EditStatus.GT)) {
					tmd = t;
					break;
				}
			}
		}
		
		String transcribedLinesText = "";
		String transcribedWordsText = "";

		int transcribedLines = tmd.getNrOfTranscribedLines();
		int transcribedWords = tmd.getNrOfWordsInLines();
		int segmentedLines = tmd.getNrOfLines();

		// logger.debug("segmentedLines: " + segmentedLines);
		// logger.debug("transcribedLines: " + transcribedLines);

		if (segmentedLines == 0) {
			transcribedLinesText = "\nNo lines segmented";
		} else {
			transcribedLinesText = (transcribedLines > 0 ? "\nTranscribed lines: " + transcribedLines
					: "\nTranscribed lines: 0");
		}
		
		if(transcribedWords > 0){
			transcribedWordsText = (transcribedWords > 0 ? "\nTranscribed words: " + transcribedWords
					: "\nTranscribed words: 0");
		}

		if (transcribedLines > 0) {
			totalLinesTranscribed += transcribedLines;

			item.setBackground(lightGreen);
		} else if (transcribedLines == 0 && segmentedLines > 0) {
			item.setBackground(lightYellow);
		} else {
			item.setBackground(lightRed);
		}
		
		if (transcribedWords > 0){
			totalWordsTranscribed += transcribedWords;
		}

		/*
		 * set either index or the filename as thumb title
		 */
		String text = "" + (index + 1);
		if (showFilenames){
			text = doc.getPages().get(index).getImgFileName();
		}

		GC gc = new GC(item.getParent());
		
		final List<TrpTranscriptMetadata> transcripts = doc.getTranscripts();
		final List<String> names = doc.getPageImgNames();
		if (/* showOrigFn.getSelection() && */names != null && index >= 0 && index < names.size()
				&& !names.get(index).isEmpty()) {
			// this shows the filename but is not really necessary in the
			// thumbnail view
			// text+=": "+names.get(i);
			text += ": ";
			int tmpWidth = gc.textExtent(text).x + 20;
			int tmpHeight = gc.textExtent(text).y + 20;
			
			maxWidth = Math.max(maxWidth, tmpWidth);
			maxHeight = Math.max(maxHeight, tmpHeight);
			// logger.debug("/////user id" + transcripts.get(i).getUserName());
			// logger.debug("/////status" + transcripts.get(i).getStatus());
			text += (transcripts.get(index) != null ? transcripts.get(index).getStatus().getStr() : "");
			if (transcripts.get(index) != null) {
				// tmp =
				// gc.textExtent(transcripts.get(i).getStatus().getStr()).x +
				// 10;
				tmpWidth = gc.textExtent(text).x + 20;
				tmpHeight = gc.textExtent(text).y + 20;
				
				maxWidth = Math.max(maxWidth, tmpWidth);
				
				// logger.debug("curr maxWidth " + maxWidth);
			}

			if (ENABLE_TRANSCRIBED_LINES && !transcribedLinesText.equals("")) {
				text += transcribedLinesText;
				tmpWidth = gc.textExtent(transcribedLinesText).x + 20;
				tmpHeight = gc.textExtent(text).y + 20;
				
				maxWidth = Math.max(maxWidth, tmpWidth);
			}
			
			if (ENABLE_TRANSCRIBED_LINES && !transcribedWordsText.equals("")) {
				text += transcribedWordsText;
				tmpWidth = gc.textExtent(transcribedWordsText).x + 20;
				maxWidth = Math.max(maxWidth, tmpWidth);
			}
		}

		int te = gc.textExtent(text).x + 10;
		int ty = gc.textExtent(text).y + 10;
		
		int tmpHeight = gc.textExtent(text).y + 20;
		maxHeight = Math.max(maxHeight, tmpHeight);
		// groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, te));

		groupRenderer.setItemWidth(Math.max(THUMB_WIDTH, maxWidth));
		groupRenderer.setItemHeight(THUMB_HEIGHT + maxHeight);
		// logger.debug("thumbText " + text);
		item.setText(text);

		gc.dispose();

	}

	public int getTotalLinesTranscribed() {
		return totalLinesTranscribed;
	}

	public int getTotalWordsTranscribed() {
		return totalWordsTranscribed;
	}

	public void setShowFilenames(boolean showFilenames) {
		this.showFilenames = showFilenames;
	}

	public int getMaxWidth() {
		return maxWidth;
	}	


}
