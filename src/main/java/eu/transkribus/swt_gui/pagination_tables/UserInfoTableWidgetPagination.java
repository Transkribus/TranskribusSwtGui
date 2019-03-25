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
import eu.transkribus.core.model.beans.auth.TrpUserInfo;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class UserInfoTableWidgetPagination extends ATableWidgetPagination<TrpUserInfo>{

private final static Logger logger = LoggerFactory.getLogger(UserInfoTableWidgetPagination.class);
	
	public static final String USER_USERNAME_COL = "Username";
	public static final String USER_UPLOAD_COL = "Uploads";
	public static final String USER_HTR_COL = "HTR Module";
	public static final String USER_OCR_COL = "OCR Module";
	public static final String USER_LA_COL = "LA Module";
	public static final String USER_HOSTING_COL = "Hosting";
	
	private int collectionId = 0;
	
	public UserInfoTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
	}

	@Override
	protected void setPageLoader() {
		// TODO Auto-generated method stub
		if (methods == null) {
			methods = new IPageLoadMethods<TrpUserInfo>() {
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
	
				@Override public List<TrpUserInfo> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return new ArrayList<>();
					
					List<TrpUserInfo> userInfo = new ArrayList<>();
					try {
						userInfo = store.getConnection().getUserInfoForCollection(collectionId, null, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return userInfo;
				}
			};
		}
			
		RemotePageLoader<TrpUserInfo> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
		
	}
	
	public void refreshList(int collectionId) {
		
		this.collectionId  = collectionId;	
		refreshPage(true);
	}

	@Override
	protected void createColumns() {
		
		createDefaultColumn(USER_USERNAME_COL, 100, "userName", true);
		createDefaultColumn(USER_UPLOAD_COL, 100, "uploads",true);
		createDefaultColumn(USER_HTR_COL, 100, "htr", true);
		createDefaultColumn(USER_OCR_COL, 100, "ocr", true);
		createDefaultColumn(USER_LA_COL, 100, "la", true);
		createDefaultColumn(USER_HOSTING_COL, 100, "hosting", true);
	}
	
}
