package eu.transkribus.swt.pagination_table;

import java.util.ArrayList;

import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
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
	
	public RemotePageLoaderSingleRequest(PageableController controller, IPageLoadMethod<T, R> method) {
		this.controller = controller;
		this.method = method;
	}
	
	@Override public PageResult<R> loadPage(PageableController controller) {
		logger.trace("loading page, pageIndex = "+controller.getPageOffset()+" pageSize = "+controller.getPageSize());
		
		try {
			return PagingUtils.loadPage(method, controller);
		} catch (Exception e) {
			logger.debug("error loading page: "+e.getMessage(), e);
			return new PageResult<R>(new ArrayList<R>(), 0);
		}
	}
	
//	public PageResult<T> loadPageAsync(PageableController controller) {
//		logger.debug("loading page, pageIndex = "+controller.getPageOffset()+" pageSize = "+controller.getPageSize());
//		
//		try {
//			return PagingUtils.loadPage(methods, controller);
//		} catch (Exception e) {
//			logger.debug("error loading page: "+e.getMessage(), e);
//			return new PageResult<T>(new ArrayList<T>(), 0);
//		}
//	}	
	
	
}
