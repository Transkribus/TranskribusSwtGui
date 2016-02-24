package eu.transkribus.swt_canvas.pagination_table;

import java.util.List;

import org.eclipse.swt.SWT;

public interface IPageLoadMethods<T> {
	int loadTotalSize();
	List<T> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection);	
//	List<T> loadPage(String propertyName, Object value);
}
