package eu.transkribus.swt.pagination_table;

import java.util.ArrayList;

import org.dea.fimagestore.core.util.SebisStopWatch.SSW;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.rest.JaxbPaginatedList;

/**
 * Can be used by PageableTable class to simplify remote page loading
 * @param <R>
 */
public class RemotePageLoaderSingleRequest<T extends JaxbPaginatedList<R>, R> implements IPageLoader<PageResult<R>> {
	private final static Logger logger = LoggerFactory.getLogger(RemotePageLoaderSingleRequest.class);
	
	PageableController controller;
	IPageLoadMethod<T, R> method;
	T currentData;
	
	public RemotePageLoaderSingleRequest(PageableController controller, IPageLoadMethod<T, R> method) {
		this.controller = controller;
		this.method = method;
		this.currentData = null;
	}
	
	@Override 
	public PageResult<R> loadPage(PageableController controller) {
		//this code previously was located in PagingUtils but in order to cache the previously loaded raw data in the currentData field, it was moved here.
		logger.trace("loading page, pageIndex = "+controller.getPageOffset()+" pageSize = "+controller.getPageSize());
		String sortDirection = null;
		if (controller.getSortDirection()==SWT.UP)
			sortDirection = "desc";
		else if (controller.getSortDirection()==SWT.DOWN)
			sortDirection = "asc";
		
		int pageSize = controller.getPageSize();
		int pageIndex = controller.getPageOffset();
		int fromIndex = pageIndex;
		int toIndex = pageIndex + pageSize;
		try {
			SSW sw = new SSW();
			sw.start();
			currentData = loadPage(fromIndex, toIndex, controller.getSortPropertyName(), sortDirection);		
			logger.debug("Loaded page in {} ms. Type = {}", sw.stop(false), currentData.getClass());
			logger.debug("Total count = {}", currentData.getTotal());
			logger.debug("List size = {}", currentData.getList().size());
			if(logger.isTraceEnabled()) {
				currentData.getList().stream().map(e -> "" + e).forEach(logger::trace);
			}
			return new PageResult<R>(currentData.getList(), currentData.getTotal());
		} catch (Exception e) {
			logger.debug("error loading page: "+e.getMessage(), e);
			return new PageResult<R>(new ArrayList<R>(), 0);
		}
	}
	
	/**
	 * Load the page data. by default it uses the IPageLoadMethod passed in constructor.
	 *  
	 * @param fromIndex
	 * @param toIndex
	 * @param sortPropertyName
	 * @param sortDirection
	 * @return
	 */
	/*
	 * Do we need the IPageLoadMethod at all? the anonymous implementation could be directly of this class for simplicity!?
	 */
	protected T loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
		return method.loadPage(fromIndex, toIndex, sortPropertyName, sortDirection);
	}

	/**
	 * @return the most recently loaded page data object or null if data was not loaded yet.
	 */
	public T getCurrentData() {
		return currentData;
	}
}
