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
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.core.model.beans.auth.TrpUserInfo;
import eu.transkribus.core.model.beans.auth.TrpUserOverallInfo;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class UserInfoOverallTableWidgetPagination extends ATableWidgetPagination<TrpUserOverallInfo>{

private final static Logger logger = LoggerFactory.getLogger(UserInfoOverallTableWidgetPagination.class);
	
	public static final String USER_USERNAME_COL = "Username";
	public static final String USER_UPLOAD_COL = "Uploaded images";
	public static final String USER_CREATE_COL = "Create Docs(MB)";
	public static final String USER_DELETE_COL = "Delete Docs(MB)";
	public static final String USER_TRAINING_COL = "Training Runs";
	public static final String USER_TRAINING_TIME_COL = "Training Time";
	public static final String USER_HTR_COL = "HTR Runs";
	public static final String USER_HTR_TIME_COL = "HTR Time";
	public static final String USER_OCR_COL = "OCR Runs";
	public static final String USER_OCR_TIME_COL = "OCR Time";
	public static final String USER_LA_COL = "LA Runs";
	public static final String USER_LA_TIME_COL = "LA Time";
	public static final String USER_HOSTING_COL = "Hosting(MB)";
	
	private int collectionId = 0;
	private TrpUserOverallInfo overallInfo = new TrpUserOverallInfo();
	
	
	public UserInfoOverallTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
		
	}

	@Override
	protected void setPageLoader() {
		if (methods == null) {
			methods = new IPageLoadMethods<TrpUserOverallInfo>() {
				Storage store = Storage.getInstance();
				
				@Override public int loadTotalSize() {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return 0;
					
					int totalSize = 1;
					return totalSize;
				}
	
				@Override public List<TrpUserOverallInfo> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
					
					if (!store.isLoggedIn() || collectionId <= 0)
						return new ArrayList<>();
		
					List<TrpUserInfo> userInfo;
					List<TrpUserOverallInfo> overallList = new ArrayList<>();
					try {
						userInfo = store.getConnection().getUserInfoForCollection(collectionId, null, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						overallInfo = userInfo.get(0).getOverallInfo();
						overallList.add(overallInfo);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					return overallList;
				}
			};
		}
			
		RemotePageLoader<TrpUserOverallInfo> pl = new RemotePageLoader<TrpUserOverallInfo>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
		
	}
	
	public void refreshList(int collectionId) {
		
		this.collectionId  = collectionId;	
		refreshPage(true);
	}
	

	@Override
	protected void createColumns() {

			createDefaultColumn(USER_USERNAME_COL, 100, "overallUserName", true);
			createDefaultColumn(USER_UPLOAD_COL, 100, "overallUploads",true);
			createDefaultColumn(USER_TRAINING_COL, 100, "overallTraining",true);
			createDefaultColumn(USER_TRAINING_TIME_COL, 100, "overallTrainingTime",true);
			createDefaultColumn(USER_HTR_COL, 100, "overallHtr", true);
			createDefaultColumn(USER_HTR_TIME_COL, 100, "overallHtrTime", true);
			createDefaultColumn(USER_OCR_COL, 100, "overallOcr", true);
			createDefaultColumn(USER_OCR_TIME_COL, 100, "overallOcrTime", true);
			createDefaultColumn(USER_LA_COL, 100, "overallLa", true);
			createDefaultColumn(USER_LA_TIME_COL, 100, "overallLaTime", true);
			createDefaultColumn(USER_CREATE_COL, 100, "overallCreateDoc",true);
			createDefaultColumn(USER_DELETE_COL, 100, "overallDeleteDoc",true);
			createDefaultColumn(USER_HOSTING_COL, 100, "overallHosting", true);
	
	}
	
}
