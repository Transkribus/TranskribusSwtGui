package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.dea.swt.pagination_table.ATableWidgetPagination;
import org.dea.swt.pagination_table.IPageLoadMethods;
import org.dea.swt.pagination_table.RemotePageLoader;
import org.dea.swt.pagination_table.TableColumnBeanLabelProvider;
import org.dea.swt.util.Fonts;
import org.dea.swt.util.Images;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TranscriptsTableWidgetPagination extends ATableWidgetPagination<TrpTranscriptMetadata> {
	
	public static final String DATE_COL = "Date";
	public static final String STATUS_COL = "Status";
	public static final String USER_NAME_COL = "Username";
	public static final String TOOLNAME_COL = "Toolname";
	public static final String ID_COL = "ID";
	public static final String PARENT_ID_COL = "Parent-ID";
	public static final String MESSAGE_COL = "Message";
	
	Button deleteBtn;

	public TranscriptsTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
		
		pageableTable.getController().setSort("time", SWT.UP);
		
//		if (withDeleteBtn) {
			deleteBtn = new Button(this, SWT.NONE);
			deleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
			deleteBtn.setToolTipText("Delete version");
			deleteBtn.setImage(Images.getOrLoad("/icons/delete.png"));
			
			deleteBtn.moveAbove(pageableTable);
//		}
	}
	
	public Button getDeleteBtn() { 
		return deleteBtn;
	}
	
	protected void createColumns() {
		class TranscriptsColumnLabelProvider extends TableColumnBeanLabelProvider {
			Font boldFont = Fonts.createBoldFont(tv.getControl().getFont());
			
			public TranscriptsColumnLabelProvider(String colName) {
				super(colName);
			}
            
        	@Override public Font getFont(Object element) {
        		if (element instanceof TrpTranscriptMetadata) {
        			TrpTranscriptMetadata e = (TrpTranscriptMetadata) element;
        			if (e.equals(Storage.getInstance().getTranscript().getMd()))
        				return boldFont;
        		}
        		
        		return null;
        	}
		}		
		
		createColumn(STATUS_COL, 100, "status", new TranscriptsColumnLabelProvider("status"));
		createColumn(USER_NAME_COL, 100, "userName", new TranscriptsColumnLabelProvider("userName"));
		createColumn(DATE_COL, 225, "timestamp", new TranscriptsColumnLabelProvider("timeFormatted")); // TODO: time-str!
		createColumn(TOOLNAME_COL, 100, "toolName", new TranscriptsColumnLabelProvider("toolName"));
		
		createColumn(ID_COL, 100, "tsId", new TranscriptsColumnLabelProvider("tsId"));
		createColumn(PARENT_ID_COL, 100, "parentTsId", new TranscriptsColumnLabelProvider("parentTsId"));
		createColumn(MESSAGE_COL, 200, "note", new TranscriptsColumnLabelProvider("note"));
	}

	@Override protected void setPageLoader() {
		if (methods == null) {
			methods = new IPageLoadMethods<TrpTranscriptMetadata>() {
				Storage store = Storage.getInstance();
				
				@Override public int loadTotalSize() {					
					int N = 0;
					if (store.isLoggedIn() && store.isPageLoaded()) {
						try {
							N = store.getConnection().countTranscriptMdList(store.getCurrentDocumentCollectionId(), store.getDocId(), store.getPage().getPageNr());
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
						}
					}
					return N;
				}
	
				@Override public List<TrpTranscriptMetadata> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {			
					List<TrpTranscriptMetadata> list = new ArrayList<>();
					if (store.isLoggedIn() && store.isPageLoaded()) {
						try {
							list = store.getConnection().getTranscriptMdList(store.getCurrentDocumentCollectionId(), store.getDocId(), store.getPage().getPageNr(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
						}
					}
					return list;
				}
			};
		}
			
		RemotePageLoader<TrpTranscriptMetadata> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);		
	}

}
