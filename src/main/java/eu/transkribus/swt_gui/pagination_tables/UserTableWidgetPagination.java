package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt_canvas.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt_canvas.pagination_table.IPageLoadMethods;
import eu.transkribus.swt_canvas.pagination_table.RemotePageLoader;
import eu.transkribus.swt_canvas.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class UserTableWidgetPagination extends ATableWidgetPagination<TrpUser> {
	private final static Logger logger = LoggerFactory.getLogger(UserTableWidgetPagination.class);
	
	public static final String USER_USERNAME_COL = "Username";
//	public static final String USER_FULLNAME_COL = "Name";
	public static final String USER_FIRSTNAME_COL = "Firstname";
	public static final String USER_LASTNAME_COL = "Lastname";
	public static final String USER_ROLE_COL = "Role";
	
	private int collectionId = 0;
	
	public UserTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
	}
	
	protected void setPageLoader() {
		if (methods == null) {
			methods = new IPageLoadMethods<TrpUser>() {
				Storage store = Storage.getInstance();
				
				@Override public int loadTotalSize() {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return 0;
					
					int totalSize = 0;
					try {
						totalSize = store.getConnection().countUsersForCollection(collectionId, null);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return totalSize;
				}
	
				@Override public List<TrpUser> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return new ArrayList<>();
					
					List<TrpUser> docs = new ArrayList<>();
					try {
						docs = store.getConnection().getUsersForCollection(collectionId, null, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return docs;
				}
			};
		}
			
		RemotePageLoader<TrpUser> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
	}
	
	public void refreshList(int collectionId) {
		this.collectionId  = collectionId;
		
		refreshPage(true);
	}

	protected void createColumns() {		
		createDefaultColumn(USER_USERNAME_COL, 100, "userName", true);
//		createDefaultColumn(USER_FULLNAME_COL, 100, "fullname");
		createDefaultColumn(USER_FIRSTNAME_COL, 100, "firstname", true);
		createDefaultColumn(USER_LASTNAME_COL, 100, "lastname", true);
		createDefaultColumn(USER_ROLE_COL, 50, "roleInCollection", false);
	}

}
