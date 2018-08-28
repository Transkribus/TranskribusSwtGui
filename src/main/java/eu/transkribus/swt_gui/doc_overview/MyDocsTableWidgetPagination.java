package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * This widget only display documents the current user has uploaded
 */
public class MyDocsTableWidgetPagination extends DocTableWidgetPagination {
	IStorageListener storageListener;
	
	public MyDocsTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize, false);
		
		addListener();
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				removeListener();
			}
		});
	}

	@Override protected void setPageLoader() {
		PageResultLoaderList<TrpDocMetadata> listLoader = new PageResultLoaderList<>(Storage.getInstance().getUserDocList());
		pageableTable.setPageLoader(listLoader);
	}
	
	void addListener() {
		storageListener = new IStorageListener() {
			@Override public void handleDocListLoadEvent(DocListLoadEvent e) {
				if (!e.isDocsByUser)
					return;
				
				Display.getDefault().asyncExec(() -> {
					if (SWTUtil.isDisposed(MyDocsTableWidgetPagination.this))
						return;
					
					PageResultLoaderList<TrpDocMetadata> pll = (PageResultLoaderList<TrpDocMetadata>) getPageableTable().getPageLoader();
					pll.setItems(Storage.getInstance().getUserDocList());
					refreshPage(true);
				});
			}
		};
		
		Storage.getInstance().addListener(storageListener);
	}
	
	void removeListener() {
		Storage.getInstance().removeListener(storageListener);
	}
	
}
