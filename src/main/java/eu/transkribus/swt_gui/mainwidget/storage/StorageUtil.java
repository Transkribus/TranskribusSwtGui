package eu.transkribus.swt_gui.mainwidget.storage;

import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.util.AuthUtils;

public class StorageUtil {
	private static final Logger logger = LoggerFactory.getLogger(StorageUtil.class);
	
	public static Storage storage = Storage.getInstance();
		
	public static TrpRole getRoleOfUserInCurrentCollection() {
		return getRoleOfUserInCollection(storage.getCurrentDocumentCollectionId());
	}
	
	public static TrpRole getRoleOfUserInCollection(int colId) {
		if (!storage.isLoggedIn() || colId <= 0) {
			return TrpRole.Admin;
		}
		
		TrpUserLogin userLogin = storage.getUser();
		if (userLogin.isAdmin()) {
			return TrpRole.Admin;
		}
		
		TrpRole role = null;
		TrpCollection c = storage.getCollection(colId);
		if (c != null) {
			role = c.getRole();
		}

		if(role == null) {
			role = TrpRole.None;
		}
		return role;
	}
	
	public static boolean canDuplicate(int srcColId, TrpDocMetadata d) {
		if (storage.getUser() == null) {
			logger.error("No user - not logged in?");
			return false;
		}
		
		TrpUserLogin u = storage.getUser();
		TrpRole role = getRoleOfUserInCollection(srcColId);
		
		return AuthUtils.canManage(role);
	}
	
	public static boolean isOwnerOfCollection(TrpCollection coll) {
		return coll!=null && AuthUtils.isOwner(coll.getRole());		
	}
	
	public static boolean isUploader(TrpUserLogin user, TrpDocMetadata... docs) {
		return isUploader(user, Arrays.asList(docs));
	}
	
	public static boolean isUploader(TrpUserLogin user, Collection<TrpDocMetadata> docs) {
		return docs.stream().allMatch((d) -> {
			return d.getUploaderId() == user.getUserId();
		});
	}
	
//	public static boolean canAddDocumentToDifferentCollection(TrpUserLogin user, TrpDocMetadata d, TrpCollection c) {
//		!user.isAdmin() && !isUploader(user, d) && !isOwnerOfCollection(c);
//	}
	
	

}
