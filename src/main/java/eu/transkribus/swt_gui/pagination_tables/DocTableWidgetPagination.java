package eu.transkribus.swt_gui.pagination_tables;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt_canvas.pagination_table.IPageLoadMethods;
import eu.transkribus.swt_canvas.pagination_table.RemotePageLoader;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.swt_canvas.util.TableUtils;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocTableWidgetPagination extends ATableWidgetPagination<TrpDocMetadata> {
	private final static Logger logger = LoggerFactory.getLogger(DocTableWidgetPagination.class);
	
	public static final String DOC_NR_COL = "NR";
	public static final String DOC_ID_COL = "ID";
	public static final String DOCS_TITLE_COL = "Title";
	public static final String DOC_NPAGES_COL = "N-Pages";
	public static final String DOC_UPLOADER_COL = "Uploader";
	public static final String DOC_UPLOADED_COL = "Uploaded";
	public static final String DOC_COLLECTIONS_COL = "Collections";
	
//	public static final ColumnConfig[] DOCS_COLS = new ColumnConfig[] {
//		new ColumnConfig(DOC_NR_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_ID_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOCS_TITLE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_NPAGES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_OWNER_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
//		new ColumnConfig(DOC_COLLECTIONS_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
//	};

	protected int collectionId=0;
	
	public DocTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
	}	
	
	public DocTableWidgetPagination(Composite parent, int style, int initialPageSize, IPageLoadMethods<TrpDocMetadata> methods) {
		super(parent, style, initialPageSize, methods);
	}
	
	public void setCollectionId(int collectionId) {
		this.collectionId = collectionId;
	}
	
	public void refreshList(int collectionId, boolean resetPage) {
		logger.debug("refreshing doc table with collection "+collectionId+" reset page: "+resetPage);
		setCollectionId(collectionId);
		
		refreshPage(resetPage);
	}
	
	protected void createColumns() {
		// generic label provider constructed with the bean property used for this column
		class DocTableColumnLabelProvider extends ColumnLabelProvider {
			String colName;
			Font boldFont = Fonts.createBoldFont(tv.getControl().getFont());
			
			public DocTableColumnLabelProvider(String colName) {
				this.colName = colName;
			}
			
            @Override public String getText(Object element) {
            	try {
					return BeanUtils.getSimpleProperty(element, colName);
				} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					return "i am error";
				}
            }
            
        	@Override public Font getFont(Object element) {
        		if (element instanceof TrpDocMetadata) {
        			TrpDocMetadata md = (TrpDocMetadata) element;
        			if (md.getDocId() == Storage.getInstance().getDocId())
        				return boldFont;
        		}
        		
        		return null;
        	}
		}
		
		TableViewerColumn col = TableUtils.createTableViewerColumn(tv, 0, DOC_ID_COL, 50);
		col.setLabelProvider(new DocTableColumnLabelProvider("docId"));
		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("docId"));
		
		col = TableUtils.createTableViewerColumn(tv, 0, DOCS_TITLE_COL, 150);
		col.setLabelProvider(new DocTableColumnLabelProvider("title"));
		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("title"));
		
		col = TableUtils.createTableViewerColumn(tv, 0, DOC_NPAGES_COL, 100);
		col.setLabelProvider(new DocTableColumnLabelProvider("nrOfPages"));
//		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("nrOfPages"));

		col = TableUtils.createTableViewerColumn(tv, 0, DOC_UPLOADER_COL, 100);
		col.setLabelProvider(new DocTableColumnLabelProvider("uploader"));
		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("uploader"));
		
		col = TableUtils.createTableViewerColumn(tv, 0, DOC_UPLOADED_COL, 100);
		col.setLabelProvider(new DocTableColumnLabelProvider("uploadTime"));
		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("uploadTimestamp"));		
		
		col = TableUtils.createTableViewerColumn(tv, 0, DOC_COLLECTIONS_COL, 100);	
		col.setLabelProvider(new DocTableColumnLabelProvider("colString"));
//		col.getColumn().addSelectionListener(new SortTableColumnSelectionListener("colString"));

	}

	@Override protected void setPageLoader() {
		if (methods == null) { // if not set from outside -> set default one that loads docs from current collection!
			methods = new IPageLoadMethods<TrpDocMetadata>() {
				Storage store = Storage.getInstance();
	
				@Override public int loadTotalSize() {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return 0;
					
					int totalSize = 0;
					try {
						totalSize = store.getConnection().countDocs(collectionId);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return totalSize;
				}
	
				@Override public List<TrpDocMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
					if (!store.isLoggedIn() || collectionId <= 0)
						return new ArrayList<>();
					
					List<TrpDocMetadata> docs = new ArrayList<>();
					try {
						logger.debug("loading docs, sortDirection = "+sortDirection+" collectionId = "+collectionId+" fromIndex = "+fromIndex+" toIndex = "+toIndex);
						docs = store.getConnection().getAllDocs(collectionId, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return docs;
				}
			};
		}
		
		RemotePageLoader<TrpDocMetadata> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
	}

}
