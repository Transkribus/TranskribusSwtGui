package eu.transkribus.swt_gui.util;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * A document pages selector that always adjusts to the currently loaded document in Storage
 * @author sebastian
 */
public class CurrentDocPagesSelector extends DocPagesSelector {

	public CurrentDocPagesSelector(Composite parent, int style) {
		super(parent, style, new ArrayList<>());

		Storage.getInstance().addListener(new IStorageListener() {
			
			// on doc load, set pages of selector
			public void handleDocLoadEvent(DocLoadEvent dle) { 
				if (Storage.getInstance().isDocLoaded()) {
					CurrentDocPagesSelector.this.setPages(Storage.getInstance().getDoc().getPages());
				}
			}
			
			// on page load, set pages text field to current page
			public void handlePageLoadEvent(PageLoadEvent arg) { 
				if (Storage.getInstance().isPageLoaded()) {
					CurrentDocPagesSelector.this.getPagesText().setText(""+(Storage.getInstance().getPageIndex()+1));
				}
			}
			
		});
		
	}

	

}
