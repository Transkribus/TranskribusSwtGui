package eu.transkribus.swt_gui.search.text_and_tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.collections.PageResultLoaderList;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.DelayedTask;


public class TagTableWidgetPagination extends ATableWidgetPagination<TrpDbTag> {
	private final static Logger logger = LoggerFactory.getLogger(TagTableWidgetPagination.class);
	
	static String ID_TAG = "Tag";
	static String TEXT_TAG = "Text";
	static String DOC_TAG = "Doc";
	static String PAGE_TAG = "Page";
	static String REGION_TAG = "Region";
	static String LABEL_TAG = "Label";
	
	
	List<TrpDbTag> tags = new ArrayList<>();
	PageResultLoaderList<TrpDbTag> listLoader;
	public static final boolean USE_LIST_LOADER = true;
	
	ViewerFilter viewerFilter;
	protected ModifyListener filterModifyListener;
	static String[] filterProperties = { "tagId", "tagText", "docId", "pageNr", "regionId" };
	
	public TagTableWidgetPagination(Composite parent, int style, int initialPageSize, IPageLoadMethods<TrpDbTag> methods) {
		super(parent, style, initialPageSize, methods, true);
		
		initFilter();
	}
	
//	void initListener() {
//			Storage.getInstance().addListener(new IStorageListener() {
//				@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
//					if (SWTUtil.isDisposed(CollectionsTableWidgetPagination.this) || SWTUtil.isDisposed(getShell()))
//							return;
//					
//					refreshList(Storage.getInstance().getCollections());
//				}
//			});
//			
//			refreshList(Storage.getInstance().getCollections());
//		}
	
	void initFilter() {
		viewerFilter = new ViewerFilter() {
			@Override public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (SWTUtil.isDisposed(filter)) {
					return true;
				}
				
				logger.debug("filter, select: "+element);

				String ft = filter.getText();
				logger.debug("ft = "+ft);
				if (StringUtils.isEmpty(ft))
					return true;
				
				ft = Pattern.quote(ft);
				
				String reg = "(?i)(.*"+ft+".*)";
				logger.debug("reg = "+reg);
				
				for (String property : filterProperties) {
					try {
						String propValue = BeanUtils.getSimpleProperty(element, property);
						logger.debug("property: "+property+" value: "+propValue);
						
						if (propValue.matches(reg)) {
							return true;
						}
					} catch (Exception e) {
						logger.error("Error getting filter property '"+property+"': "+e.getMessage());
					}
				}

				return false;
				
//				boolean matches = element.toString().matches(reg);
//				logger.debug("matches = "+matches);
//				return matches;
			}
		};
		
		filterModifyListener = new ModifyListener() {
			DelayedTask dt = new DelayedTask(() -> { 
				refreshList(tags);
			}, true);
			@Override public void modifyText(ModifyEvent e) {
				dt.start();
			}
		};
		filter.addModifyListener(filterModifyListener);
		
	}
	

	public String getFilterText() {
		if (SWTUtil.isDisposed(filter))
			return "";
		else
			return filter.getText();
	}
	
	public List<TrpDbTag> getTags() {
		return tags;
	}
	
	public synchronized void refreshList(List<TrpDbTag> tags) {
		this.tags = new ArrayList<>();
		this.tags.addAll(tags);
		
		List<TrpDbTag> filtered = new ArrayList<>();
		for (TrpDbTag t : tags) {
			if (viewerFilter.select(null, null, t)) { 
				filtered.add(t);
			}
		}
		
		Display.getDefault().syncExec(() -> {
			if (listLoader!=null) {
				listLoader.setItems(filtered);
			}
			
			refreshPage(true);
		});
	}
	

	@Override protected void setPageLoader() {
		if (USE_LIST_LOADER && methods==null) {
			listLoader = new PageResultLoaderList<TrpDbTag>(tags);
			pageableTable.setPageLoader(listLoader);
		} else {
		
			if (methods == null) {
				methods = new IPageLoadMethods<TrpDbTag>() {
					Storage store = Storage.getInstance();
					
					@Override public int loadTotalSize() {
						if (!store.isLoggedIn())
							return 0;
						
						// FIXME: need method for counting in connection
						int totalSize = 100;
						
						return totalSize;
						
					}

					@Override
					public List<TrpDbTag> loadPage(int fromIndex, int toIndex, String sortPropertyName,
							String sortDirection) {
						// TODO: load tags here
						List<TrpDbTag> tags = new ArrayList<>();

						if (!store.isLoggedIn())
							return tags;

						Set<Integer> collIds = null;
						Set<Integer> docIds = null;

						collIds = CoreUtils.createSet(store.getCollId());
						docIds = CoreUtils.createSet(store.getDocId());

						//deactivate this change for now as client's interface does not fit to this
//						try {
//							tags = store.getConnection().searchTags(collIds, docIds, null, null, null, null, true,
//									false, null, sortPropertyName, sortDirection);
//						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
//							TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
//						}
						
						try {
							tags = store.getConnection().searchTags(collIds, docIds, null, null, null, null, true,
									false, null);
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
						}
						
						return tags;
					}
				};
			}

		RemotePageLoader<TrpDbTag> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
		}
	}

	@Override protected void createColumns() {
		class TagTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			Font boldFont = Fonts.createBoldFont(tv.getControl().getFont());
			
			public TagTableColumnLabelProvider(String colName) {
				super(colName);
			}
            
        	@Override public Font getFont(Object element) {
        		if (element instanceof TrpDbTag) {
        			TrpDbTag t = (TrpDbTag) element;
        			
       				return boldFont;
        		}
        		
        		return null;
        	}
        	
		}
		
		createColumn(ID_TAG, 50, "tagId", new TagTableColumnLabelProvider("id"));
		createColumn(TEXT_TAG, 250, "tagText", new TagTableColumnLabelProvider("value"));
		createColumn(DOC_TAG, 80, "docId", new TagTableColumnLabelProvider("docid"));
		createColumn(PAGE_TAG, 50, "pageNr", new TagTableColumnLabelProvider("pagenr"));
		createColumn(REGION_TAG, 150, "regionId", new TagTableColumnLabelProvider("regionid"));
	}


}
