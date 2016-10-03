package eu.transkribus.swt.pagination_table;

import java.util.ArrayList;

import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.PageableController;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be used by PageableTable class to simplify remote page loading
 */
public class RemotePageLoader<T> implements IPageLoader<PageResult<T>> {
	private final static Logger logger = LoggerFactory.getLogger(RemotePageLoader.class);
	
	PageableController controller;
	IPageLoadMethods<T> methods;
	
	public RemotePageLoader(PageableController controller, IPageLoadMethods<T> methods) {
		this.controller = controller;
		this.methods = methods;
	}
	
	@Override public PageResult<T> loadPage(PageableController controller) {
		logger.debug("loading page, pageIndex = "+controller.getPageOffset()+" pageSize = "+controller.getPageSize());
		
		try {
			return PagingUtils.loadPage(methods, controller);
		} catch (Exception e) {
			logger.debug("error loading page: "+e.getMessage(), e);
			return new PageResult<T>(new ArrayList<T>(), 0);
		}
	}
}
