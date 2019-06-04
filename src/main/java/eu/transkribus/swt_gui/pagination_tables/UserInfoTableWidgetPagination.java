package eu.transkribus.swt_gui.pagination_tables;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	private List<TrpUserInfo> userInfo = new ArrayList<>();
	
	public UserInfoTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
		
	}

	@Override
	protected void setPageLoader() {
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
		
					try {
						userInfo = store.getConnection().getUserInfoForCollection(collectionId, null, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading documents", e.getMessage(), e);
					}
					TrpUserInfo total = new TrpUserInfo();
					total.setUserName("TOTAL");
					Integer totalUploads = 0 ,totalHtr = 0, totalOcr = 0 , totalLa = 0, totalTraining = 0 ;
					String totalTrainingTime = "00:00:00", totalHtrTime = "00:00:00", totalOcrTime = "00:00:00", totalLaTime = "00:00:00";
					double totalCreate = 0, totalDelete = 0, totalHosting = 0;
					
					for(TrpUserInfo user : userInfo) {
						totalUploads += user.getUploads();
						totalHtr += user.getHtr();
						totalOcr += user.getOcr();
						totalLa += user.getLa();
						totalTraining += user.getTraining();
						totalCreate += user.getCreateDoc().doubleValue();
						totalDelete += user.getDeleteDoc().doubleValue();
						totalHosting += user.getHosting().doubleValue();
						totalTrainingTime = addStringTime(totalTrainingTime, user.getTrainingTime());
						totalHtrTime = addStringTime(totalHtrTime, user.getHtrTime());
						totalOcrTime = addStringTime(totalOcrTime, user.getOcrTime());
						totalLaTime = addStringTime(totalLaTime, user.getLaTime());
						
					}
					total.setUploads(totalUploads);
					total.setTraining(totalTraining);
					total.setHtr(totalHtr);
					total.setOcr(totalOcr);
					total.setLa(totalLa);
					total.setCreateDoc(new BigDecimal(totalCreate));
					total.setDeleteDoc(new BigDecimal(totalDelete));
					total.setHosting(new BigDecimal(totalHosting));
					total.setTrainingTime(totalTrainingTime);
					total.setHtrTime(totalHtrTime);
					total.setOcrTime(totalOcrTime);
					total.setLaTime(totalLaTime);
					userInfo.add(total);
					
					return userInfo;
				}
			};
		}
			
		RemotePageLoader<TrpUserInfo> pl = new RemotePageLoader<TrpUserInfo>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
		
	}
	
	public void refreshList(int collectionId) {
		
		this.collectionId  = collectionId;	
		refreshPage(true);
	}
	
	public List<TrpUserInfo> getUserInfo() {
		return userInfo;
	}
	
	public String addStringTime(String time1 , String time2) {
		
		String[] firstTimeParts = time1.split(":");
		int hours1 = Integer.parseInt(firstTimeParts[0]);
		int minutes1 = Integer.parseInt(firstTimeParts[1]);
		int seconds1 = Integer.parseInt(firstTimeParts[2]);
		
		if(time2 == null) {
			time2 = "00:00:00";
		}

		String[] secondTimeParts = time2.split(":");

		int hours2 = Integer.parseInt(secondTimeParts[0]);
		int minutes2 = Integer.parseInt(secondTimeParts[1]);
		int seconds2 = Integer.parseInt(secondTimeParts[2]);

		int hours = hours1 + hours2;
		int minutes = minutes1 + minutes2;
		int seconds = seconds1 + seconds2;
		int days = 0;

		if (seconds > 59) {
			seconds = seconds - 60;
			minutes = minutes + 1;
			if (minutes > 59) {
				minutes = minutes - 60;
				hours = hours + 1;
				if (hours > 23) {
					hours = hours - 24;
					days = days + 1;
				}
			} else {

				if (hours > 23) {
					hours = hours - 24;
					days = days + 1;
				}
			}
		} else {
			if (minutes > 59) {
				minutes = minutes - 60;
				hours = hours + 1;
				if (hours > 23) {
					hours = hours - 24;
					days = days + 1;
				}
			} 
		}
		return String.valueOf(hours) + ":" + String.valueOf(minutes) + ":" + String.valueOf(seconds);
		
	}

	@Override
	protected void createColumns() {
			
			createDefaultColumn(USER_USERNAME_COL, 100, "userName", true);
			createDefaultColumn(USER_UPLOAD_COL, 100, "uploads",true);
			createDefaultColumn(USER_TRAINING_COL, 100, "training",true);
			createDefaultColumn(USER_TRAINING_TIME_COL, 100, "trainingTime",true);
			createDefaultColumn(USER_HTR_COL, 100, "htr", true);
			createDefaultColumn(USER_HTR_TIME_COL, 100, "htrTime", true);
			createDefaultColumn(USER_OCR_COL, 100, "ocr", true);
			createDefaultColumn(USER_OCR_TIME_COL, 100, "ocrTime", true);
			createDefaultColumn(USER_LA_COL, 100, "la", true);
			createDefaultColumn(USER_LA_TIME_COL, 100, "laTime", true);
			createDefaultColumn(USER_CREATE_COL, 100, "createDoc",true);
			createDefaultColumn(USER_DELETE_COL, 100, "deleteDoc",true);
			createDefaultColumn(USER_HOSTING_COL, 100, "hosting", true);


	}
	
}
