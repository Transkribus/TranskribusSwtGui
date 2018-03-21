package eu.transkribus.swt.pagination_table;

import java.util.List;

/**
 * Use {@link IPageLoadMethod} instead on new endpoints that provide JaxbPaginatedList
 *
 * @param <T>
 */
public interface IPageLoadMethods<T> {
	int loadTotalSize();
	List<T> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection);	
//	List<T> loadPage(String propertyName, Object value);
}
