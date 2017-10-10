package eu.transkribus.swt.util;

import java.lang.reflect.Method;

import java.net.URL;
import java.util.List;

import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;


public class ThumbnailWidgetVirtual extends Composite {
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailWidgetVirtual.class);
	
	static final int TEXT_TO_THUMB_OFFSET = 5;
	
	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;
	
//	protected List<ThmbImg> thumbs = new ArrayList<>();
//	
//	protected ThmbImgLoadThread loadThread;
	
//	TableViewer tv;
	protected Method setItemHeightMethod;
	protected Gallery gallery;
	
	protected Composite groupComposite;
	protected Composite labelComposite;
	
	protected Label statisticLabel;
	protected Label pageNrLabel;
	protected Label totalTranscriptsLabel;
		
	protected Button reload, showOrigFn, createThumbs, showPageManager;
//	protected TextToolItem infoTi;

	protected List<URL> urls;
	protected List<String> names=null;
	
	protected List<TrpTranscriptMetadata> transcripts;
		
//	protected NoGroupRenderer groupRenderer;
	protected AbstractGridGroupRenderer groupRenderer;
	
	static ThumbnailManagerVirtual tm;
	ThumbnailWidgetVirtualMinimal tw;
	
	static AdministrativeCenter ac;
	
	TrpMainWidget mw;
	
	static int thread_counter=0;
	
	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	
	private int maxWidth = 0;
		
	public ThumbnailWidgetVirtual(Composite parent, int style, TrpMainWidget mw) {
		super(parent, style);
		
		this.mw = mw;

		setLayout(new GridLayout());
				
		groupComposite = new Composite(this, SWT.FILL);
		GridLayout gl = new GridLayout(1, false);
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupComposite.setLayoutData(gridData);
		
		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		showPageManager = new Button(labelComposite, 0);
		showPageManager.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		showPageManager.setText("Open Administrative Center");
		
		showPageManager.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showPageManager();
			}
		});
		
		statisticLabel = new Label(labelComposite, SWT.TOP);
		statisticLabel.setText("Thumbnail Overview of Document: ");
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Composite btns = new Composite(this, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.wrap = true;
		rowLayout.pack = true;
		btns.setLayout(rowLayout);
		
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
				
		tw = new ThumbnailWidgetVirtualMinimal(groupComposite, false, SWT.BORDER);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//logger.debug("doc ID is " + Storage.getInstance().getDoc().getId());
		tw.setDoc(Storage.getInstance().getDoc(), false);
		
		tw.getGallery().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				List<TrpPage> selList = tw.getSelection();
				if(!selList.isEmpty()) {	
					Event e = new Event();
					logger.debug("group size : " + tw.getGroup().getItemCount());
					logger.debug("tw.getGallery().getSelection()[0] " + tw.getGallery().getSelection()[0]);
					//e.index = tw.getGroup().indexOf(tw.getGallery().getSelection()[0]);
					
					e.index = tw.getGallery().indexOf(tw.getGallery().getSelection()[0]);
					
					logger.debug("selection index " + e.index);
					
//					if (names != null && names.size()>e.index) {
//						infoTi.setText(names.get(e.index));
//					} else 
//						infoTi.setText(""+e.index);
//					infoTi.setWidth(100);
					btns.pack();
					
					notifyListeners(SWT.Selection, e);
					//loadImg(selList.get(0));
				}
			}
		});
							
		this.pack();
	}
	
	protected void showPageManager() {
		
		if (Storage.getInstance().getDoc() == null) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded");
			return;
		}
		
		if (isAdministrativeCenterIsOpen(ac)){
			ac.getShell().setVisible(true);
		} else {
			ac = new AdministrativeCenter(getShell(), SWT.NONE, mw, Storage.getInstance().getCollId());
			ac.open();
		}
		
		
		//if shell is open {
//		if(isManagerOpen(tm)){
//			tm.getShell().setVisible(true);
//		} else {
//			tm = new ThumbnailManagerVirtual(getShell(), SWT.NONE, mw);
//
//			tm.addListener(SWT.Selection, new Listener() {
//				@Override public void handleEvent(Event event) {
//					logger.debug("loading page " + event.index);
//					mw.jumpToPage(event.index);
//				}
//			});
//
//			tm.open();
//		}

	}
	
	private boolean isManagerOpen(ThumbnailManagerVirtual tm){
		if(tm != null && tm.getShell() != null && !tm.getShell().isDisposed()){
			return true;
		}
		return false;
	}
	
	private boolean isAdministrativeCenterIsOpen(AdministrativeCenter ac){
		if(ac != null && ac.getShell() != null && !ac.getShell().isDisposed()){
			return true;
		}
		return false;
	}
	
	
		
	public void reload() {
		
		tw.setDoc(Storage.getInstance().getDoc(), false);
		
	}
	
	public ThumbnailManagerVirtual getThumbnailManager() {
		return tm;
	}
	
	public void setThumbnailManager(ThumbnailManagerVirtual tm) {
		this.tm = tm;
	}
	
	public Button getCreateThumbs() {
		return createThumbs;
	}

}
