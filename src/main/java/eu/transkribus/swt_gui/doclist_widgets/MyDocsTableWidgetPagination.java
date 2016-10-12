package eu.transkribus.swt_gui.doclist_widgets;

import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;

public class MyDocsTableWidgetPagination extends DocTableWidgetPagination {
	public MyDocsTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
	}

	@Override protected void setPageLoader() {
		PageResultLoaderList<TrpDocMetadata> listLoader = new PageResultLoaderList<>(Storage.getInstance().getUserDocList());
		pageableTable.setPageLoader(listLoader);
	}
	
	void addListener() {
		Storage.getInstance().addListener(new IStorageListener() {
			@Override public void handleDocListLoadEvent(DocListLoadEvent e) {
				if (!e.isDocsByUser)
					return;
				
				Display.getDefault().asyncExec(() -> {
					PageResultLoaderList<TrpDocMetadata> pll = (PageResultLoaderList<TrpDocMetadata>) getPageableTable().getPageLoader();
					pll.setItems(Storage.getInstance().getUserDocList());
					refreshPage(true);
				});
			}
		});
	}
	
}
