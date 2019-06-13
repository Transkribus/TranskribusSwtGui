package eu.transkribus.swt_gui.util;

import java.util.ArrayList;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * A document pages selector that always adjusts to the currently loaded document in Storage
 * @author sebastian
 */
public class CurrentDocPagesSelector extends DocPagesSelector {
	IStorageListener storageListener;

	public CurrentDocPagesSelector(Composite parent, int style, boolean showLabel, boolean showCurrentPageBtn, boolean showAllPagesBtn) {
		super(parent, style, showLabel, showCurrentPageBtn, showAllPagesBtn, new ArrayList<>());

		storageListener = new IStorageListener() {
			// on doc load, set pages of selector
			public void handleDocLoadEvent(DocLoadEvent dle) { 
				updatePagesFromStorage();
			}
			
			// on page load, set pages text field to current page
			public void handlePageLoadEvent(PageLoadEvent arg) { 
				updatePageStrFromStorage();
			}
		};
		Storage.getInstance().addListener(storageListener);
		
		this.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				if (Storage.getInstance()!=null) {
					Storage.getInstance().removeListener(storageListener);	
				}
			}
		});
		
		updatePagesFromStorage();
		updatePageStrFromStorage();
	}
	
	private void updatePagesFromStorage() {
		if (Storage.getInstance().isPageLoaded()) {
			if (!SWTUtil.isDisposed(CurrentDocPagesSelector.this) && !SWTUtil.isDisposed(CurrentDocPagesSelector.this.getPagesText())) {
				CurrentDocPagesSelector.this.getPagesText().setText(""+(Storage.getInstance().getPageIndex()+1));	
			}
		}
	}
	
	private void updatePageStrFromStorage() {
		if (Storage.getInstance().isDocLoaded()) {
			if (!SWTUtil.isDisposed(CurrentDocPagesSelector.this) && !SWTUtil.isDisposed(CurrentDocPagesSelector.this.getPagesText())) {
				CurrentDocPagesSelector.this.setPages(Storage.getInstance().getDoc().getPages());	
			}
		}
	}
	
	@Override protected void openDocPageViewer() {
		/*
		 * once a selection has been made, it should be kept
		 * especially when you have to select intervals of pages in a huge document, you forget one and have to go back to the selector
		 * 
		 * you go crazy if always the loaded page is selected
		 */
		//updatePagesFromStorage(
		super.openDocPageViewer();
	}
	
}
