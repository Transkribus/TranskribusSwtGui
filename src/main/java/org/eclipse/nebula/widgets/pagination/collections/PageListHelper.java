package org.eclipse.nebula.widgets.pagination.collections;
import java.util.List;

import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper to create implementation of {@link PageResult} from a Java
 * {@link List}.
 * 
 */
public class PageListHelper {
	
	private final static Logger logger = LoggerFactory.getLogger(PageListHelper.class);

	public static <T> PageResult<T> createPage(List<T> list,
			PageableController controller) {
		return createPage(list, controller, DefaultSortProcessor.getInstance());
	}

	public static <T> PageResult<T> createPage(List<T> list,
			PageableController controller, SortProcessor processor) {
		int sortDirection = controller.getSortDirection();
		if (sortDirection != SWT.NONE) {
			// Sort the list
			processor.sort(list, controller.getSortPropertyName(),
					sortDirection);
		}
		int totalSize = list.size();
		int pageSize = controller.getPageSize();
		int pageIndex = controller.getPageOffset();

		int fromIndex = pageIndex;
		int toIndex = pageIndex + pageSize;
		if (toIndex > totalSize) {
			toIndex = totalSize;
		}
		// FIXED BY S.C.
		if (fromIndex > toIndex) {
			logger.debug("fromIndex = "+fromIndex+" toIndex = "+toIndex);
			fromIndex = toIndex;
		}
		
		List<?> content = list.subList(fromIndex, toIndex);
		return new PageResult(content, totalSize);
	}

}
