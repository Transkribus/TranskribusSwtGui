package eu.transkribus.swt.pagination_table;

import eu.transkribus.core.model.beans.rest.JaxbPaginatedList;

public interface IPageLoadMethod<T extends JaxbPaginatedList<R>, R> {	
	T loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection);	
}
