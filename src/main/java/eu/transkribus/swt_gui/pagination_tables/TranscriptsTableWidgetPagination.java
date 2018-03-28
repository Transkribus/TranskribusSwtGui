package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TranscriptsTableWidgetPagination extends ATableWidgetPagination<TrpTranscriptMetadata> {
	
	public static final String DATE_COL = "Date";
	public static final String STATUS_COL = "Status";
	public static final String USER_NAME_COL = "Username";
	public static final String TOOLNAME_COL = "Toolname";
	public static final String ID_COL = "ID";
	public static final String PARENT_ID_COL = "Parent-ID";
	public static final String MESSAGE_COL = "Message";
	
	Button deleteBtn;
	Menu contextMenu;
	MenuItem newItem, inProgressItem, doneItem, finalItem, gtItem;

	public TranscriptsTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);

		pageableTable.getController().setSort("time", SWT.UP);
		
	    contextMenu = new Menu(tv.getTable());
	    tv.getTable().setMenu(contextMenu);
	    
	    addMenuItems(contextMenu, EditStatus.getStatusListWithoutNew());
	    		
//		if (withDeleteBtn) {
			deleteBtn = new Button(this, SWT.NONE);
			deleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
			deleteBtn.setToolTipText("Delete version");
			deleteBtn.setImage(Images.getOrLoad("/icons/delete.png"));
			
			deleteBtn.moveAbove(pageableTable);
//		}
	}
	
	private void addMenuItems(Menu contextMenu, String[] editStatusArray) {
		MenuItem tmp;
		for (String editStatus : editStatusArray){
			tmp = new MenuItem(contextMenu, SWT.None);
			tmp.setText(editStatus);
			tmp.addSelectionListener(new MenuItemListener());
			
		}
	}

	/*
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 */
	class MenuItemListener extends SelectionAdapter {
	    public void widgetSelected(SelectionEvent event) {
//	    	System.out.println("You selected " + ((MenuItem) event.widget).getText());
//	    	System.out.println("You selected cont.1 " + EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
//	    	System.out.println("You selected cont.2 " + EnumUtils.indexOf(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText())));
	    	TrpMainWidget.getInstance().changeVersionStatus(((MenuItem) event.widget).getText(),Storage.getInstance().getPage());
//	    	Storage.getInstance().getTranscriptMetadata().setStatus(EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));
	    	try {
//				Storage.getInstance().saveTranscript(Storage.getInstance().getCurrentDocumentCollectionId(), null);
//				Storage.getInstance().setLatestTranscriptAsCurrent();
				tv.refresh();
				tv.getTable().deselectAll();
				tv.getTable().select(0);
			} catch (ServerErrorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
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

	public Menu getContextMenu() {
		return contextMenu;
	}
	
	public void disableContextMenu(){
		contextMenu.dispose();
	}
	
	public void enableContextMenu(){
		contextMenu = new Menu(tv.getTable());
		tv.getTable().setMenu(contextMenu); 
		addMenuItems(contextMenu, EditStatus.getStatusListWithoutNew());
	}

//	public void setContextMenuVisible(boolean value) {
//		tv.getTable().getMenu().setVisible(value); 	
//	}

}
