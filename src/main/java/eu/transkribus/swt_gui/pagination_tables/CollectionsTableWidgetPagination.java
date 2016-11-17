package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CollectionsTableWidgetPagination extends ATableWidgetPagination<TrpCollection> {
	private final static Logger logger = LoggerFactory.getLogger(CollectionsTableWidgetPagination.class);
	
	static String ID_COL = "ID";
	static String NAME_COL = "Name";
	static String ROLE_COL = "Role";
	static String DESC_COL = "Description";
	static String LABEL_COL = "Label";
	
	static final boolean USE_LIST_LOADER = true;
	
	List<TrpCollection> collections = new ArrayList<>();
	PageResultLoaderList<TrpCollection> listLoader;
	
	public CollectionsTableWidgetPagination(Composite parent, int style, int initialPageSize, IPageLoadMethods<TrpCollection> methods, boolean singleSelection) {
		super(parent, style, initialPageSize, methods, singleSelection);
	}
	
	public CollectionsTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
	}
	
	public synchronized void refreshList(List<TrpCollection> collections) {
//		this.collections = collections;
		
		this.collections = new ArrayList<>();
		this.collections.addAll(collections);
		
		if (USE_LIST_LOADER && listLoader!=null) {
			listLoader.setItems(this.collections);
		}
		
		refreshPage(true);
	}

	@Override protected void setPageLoader() {
		IPageLoader<PageResult<TrpCollection>> loader;
		
		if (USE_LIST_LOADER) {
			List<TrpCollection> collections = Storage.getInstance().getCollections();
			listLoader = new PageResultLoaderList<TrpCollection>(collections);
			loader = listLoader;
		} else {
			if (methods == null) {
				methods = new IPageLoadMethods<TrpCollection>() {
					Storage store = Storage.getInstance();
					
					@Override public int loadTotalSize() {
						List<TrpCollection> collections = Storage.getInstance().getCollections();
						return collections.size();
						
	//					int N = 0;
	//					if (store.isLoggedIn()) {
	//						try {
	//							return store.getConnection().countAllCollections();
	//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
	//							TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
	//						}
	//					}
	//					return N;
					}
		
					@Override public List<TrpCollection> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
						List<TrpCollection> collections = Storage.getInstance().getCollections();
						logger.trace("colls loading page, fromIndex = "+fromIndex+", toIndex = "+toIndex);
						return collections.subList(fromIndex, toIndex);
						
	//					List<TrpCollection> list = new ArrayList<>();
	//					if (store.isLoggedIn()) {
	//						try {
	//							list = store.getConnection().getAllCollections(fromIndex, toIndex-fromIndex);
	//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
	//							TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
	//						}
	//					}
	//					return list;
					}
				};
			}
			
			RemotePageLoader<TrpCollection> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
			loader = pl;
		}

		pageableTable.setPageLoader(loader);
	}

	@Override protected void createColumns() {
		class CollectionsTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			Font boldFont = Fonts.createBoldFont(tv.getControl().getFont());
			
			public CollectionsTableColumnLabelProvider(String colName) {
				super(colName);
			}
            
        	@Override public Font getFont(Object element) {
        		if (element instanceof TrpCollection) {
        			TrpCollection c = (TrpCollection) element;
        			
        			if (c.getColId() == TrpMainWidget.getInstance().getUi().getServerWidget().getSelectedCollectionId())
        				return boldFont;
        		}
        		
        		return null;
        	}
        	
//        	@Override public String getText(Object element) {
//        		if (!colName.equals(ROLE_COL))
//        			super.getText(element);
//        		
//        		if (element instanceof TrpCollection) {
//        			TrpCollection c = (TrpCollection) element;
//        			
//        			return c.getRole() == null ? "" : c.getRole().toString();
//        			
//        		}
//        		return null;
//        		
//        	}
		}
		
		createColumn(ID_COL, 50, "colId", new CollectionsTableColumnLabelProvider("colId"));
		createColumn(NAME_COL, 200, "colName", new CollectionsTableColumnLabelProvider("colName"));
		createColumn(ROLE_COL, 80, "colName", new CollectionsTableColumnLabelProvider("role"));
		createColumn(DESC_COL, 100, "description", new CollectionsTableColumnLabelProvider("description"));
		createColumn(LABEL_COL, 100, "label", new CollectionsTableColumnLabelProvider("label"));
	}

}
