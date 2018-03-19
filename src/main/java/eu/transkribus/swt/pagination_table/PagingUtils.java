package eu.transkribus.swt.pagination_table;

import java.util.List;

import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;

import eu.transkribus.core.model.beans.rest.JaxbPaginatedList;

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
	
	public static <T extends JaxbPaginatedList<R>, R> PageResult<R> loadPage(IPageLoadMethod<T, R> method, PageableController c) {
		String sortDirection = null;
		if (c.getSortDirection()==SWT.UP)
			sortDirection = "desc";
		else if (c.getSortDirection()==SWT.DOWN)
			sortDirection = "asc";
		
		int pageSize = c.getPageSize();
		int pageIndex = c.getPageOffset();
		int fromIndex = pageIndex;
		int toIndex = pageIndex + pageSize;
				
		T items = method.loadPage(fromIndex, toIndex, c.getSortPropertyName(), sortDirection);
				
		return new PageResult<R>(items.getList(), items.getTotal());
	}

	
}
