package eu.transkribus.swt.pagination_table;

import java.util.List;

import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;

public class PagingUtils {

	public static <T> PageResult<T> loadPage(IPageLoadMethods<T> methods, PageableController c) {
		int pageSize = c.getPageSize();
		int pageIndex = c.getPageOffset();
		int fromIndex = pageIndex;
		int toIndex = pageIndex + pageSize;
		int totalSize = methods.loadTotalSize();
		if (toIndex > totalSize) {
			toIndex = totalSize;
		}	
		String sortDirection = null;
		if (c.getSortDirection()==SWT.UP)
			sortDirection = "desc";
		else if (c.getSortDirection()==SWT.DOWN)
			sortDirection = "asc";
		
		List<T> items = methods.loadPage(fromIndex, toIndex, c.getSortPropertyName(), sortDirection);
				
		return new PageResult<T>(items, totalSize);
	}
	
}
