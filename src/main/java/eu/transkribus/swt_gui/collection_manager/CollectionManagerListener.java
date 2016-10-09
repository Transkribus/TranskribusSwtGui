package eu.transkribus.swt_gui.collection_manager;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.InvalidUserInputException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.core.util.AuthUtils;
import eu.transkribus.core.util.UserInputChecker;
import eu.transkribus.swt.util.ComboInputDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener.CollectionsLoadEvent;

public class CollectionManagerListener implements IStorageListener, SelectionListener, DragSourceListener  {
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerListener.class);
	
	CollectionManagerDialog cmw;
	TrpMainWidget mw;
	Storage store = Storage.getInstance();
	Shell shell;
	
	public CollectionManagerListener(CollectionManagerDialog cmw) {
		this.cmw = cmw;
		shell = cmw.getShell();
		this.mw = TrpMainWidget.getInstance();
		
		attach();
	}
	
	public void attach() {
		cmw.addUserToColBtn.addSelectionListener(this);
		cmw.removeUserFromColBtn.addSelectionListener(this);
//		cmw.editUserFromColBtn.addSelectionListener(this);
		cmw.role.addSelectionListener(this);
		cmw.addCollectionBtn.addSelectionListener(this);
		cmw.deleteCollectionBtn.addSelectionListener(this);
		cmw.addDocumentToCollBtn.addSelectionListener(this);
		cmw.removeDocumentFromCollBtn.addSelectionListener(this);
//		cmw.reloadCollectionsBtn.addSelectionListener(this);
		cmw.modifyCollectionBtn.addSelectionListener(this);
		cmw.deleteDocumentBtn.addSelectionListener(this);
		cmw.duplicatedDocumentBtn.addSelectionListener(this);
		store.addListener(this);
	}
	
	public void detach() {
		cmw.addUserToColBtn.removeSelectionListener(this);
		cmw.removeUserFromColBtn.removeSelectionListener(this);
//		cmw.editUserFromColBtn.removeSelectionListener(this);
		cmw.role.removeSelectionListener(this);
		cmw.addCollectionBtn.removeSelectionListener(this);
		cmw.deleteCollectionBtn.removeSelectionListener(this);
		cmw.addDocumentToCollBtn.removeSelectionListener(this);
		cmw.removeDocumentFromCollBtn.removeSelectionListener(this);
//		cmw.reloadCollectionsBtn.removeSelectionListener(this);
		cmw.modifyCollectionBtn.removeSelectionListener(this);
		cmw.deleteDocumentBtn.removeSelectionListener(this);
		cmw.duplicatedDocumentBtn.removeSelectionListener(this);
		store.removeListener(this);
	}

	@Override public void widgetSelected(SelectionEvent e) {
		try {
			Object s = e.getSource();
			if (s == cmw.addUserToColBtn) {
				addSelectedUsersToCollection();
			} else if (s == cmw.removeUserFromColBtn) {
				removeSelectedUsersFromCollection();
			} 
			else if (s == cmw.role) {
				editSelectedUsersFromCollection();
			} 
			else if (s == cmw.addCollectionBtn) {
				createCollection();
			} else if (s == cmw.deleteCollectionBtn) {
				deleteCollection();
			} else if (s == cmw.addDocumentToCollBtn) {
				addDocumentsToCollection();
			} else if (s == cmw.removeDocumentFromCollBtn) {
				removeDocumentFromCollection();
			} 
//			else if (s == cmw.reloadCollectionsBtn) {
//				cmw.updateCollections();
//			}
			else if (s == cmw.modifyCollectionBtn) {
				modifySelectedCollection();
			} else if (s == cmw.deleteDocumentBtn){
				deleteDocument();
			} else if (s == cmw.duplicatedDocumentBtn) {
				duplicateDocument();
			}
		} catch (Throwable th) {
			mw.onError("Unexpected error", "An unexpected error occured: "+th.getMessage(), th);
		}
	}
	
	@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
		cmw.collectionsTv.refreshList(cle.collections);
	}
	
	boolean checkUploaderOrCollectionOwnerRights(TrpUserLogin user, TrpDocMetadata ...docs) {
		for (TrpDocMetadata d : docs) {
			if(!user.isAdmin() && !isUploader(user, d) && !isOwnerOfCurrentCollection()) {
				DialogUtil.showErrorMessageBox(shell, "Unauthorized", "You are not the uploader of this document or owner of the selected collection!\n\nDocument: "+d.getTitle()+" ID = "+d.getDocId());
				return false;
			}
		}
		return true;
	}

	private void addDocumentsToCollection() {
		
		// check rights first:
		List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
		if (selected.isEmpty())
			return;
		
		if (!checkUploaderOrCollectionOwnerRights(store.getUser(), selected.toArray(new TrpDocMetadata[0])))
			return;
		
		ChooseCollectionDialog diag = new ChooseCollectionDialog(cmw.shell);
		if (diag.open() != Dialog.OK)
			return;
		TrpCollection c = diag.getSelectedCollection();
		if (c==null) {
			DialogUtil.showErrorMessageBox(cmw.getShell(), "No collection selected", "Please select a collection to add the document to!");
			return;
		}
		logger.debug("selected collection is: "+c);		
		
		if (c != null && store.isLoggedIn()) {
			TrpServerConn conn = store.getConnection();
					
			List<String> error = new ArrayList<>();
			
			for (TrpDocMetadata d : selected) {
				logger.debug("adding document: "+d+" to collection: "+c.getColId());				
				try {						
					conn.addDocToCollection(c.getColId(), d.getDocId());
					logger.info("added document: "+d);
				} catch (Throwable e) {
					logger.warn("Could not add document: "+d);
					error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not add the following documents:\n";
				for (String u : error) {
					msg += u + "\n";
				}
				
				mw.onError("Error adding documents", msg, null);
//				DialogUtil.showErrorMessageBox(shell, "Error adding documents", msg);
			} else {
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully added "+selected.size()+" documents");
			}
			
			cmw.docsTableWidget.refreshPage(false);
//			cmw.updateAll();
		}
	}
	
	private void removeDocumentFromCollection() {
		List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
		if (selected.isEmpty())
			return;
		
		// check rights first:
		if (!checkUploaderOrCollectionOwnerRights(store.getUser(), selected.toArray(new TrpDocMetadata[0])))
			return;			
		
		TrpCollection c = cmw.getSelectedCollection();
		if (c==null) {
			DialogUtil.showErrorMessageBox(cmw.getShell(), "No collection selected", "Please select a collection to remove the documents from!");
			return;
		}
		logger.debug("selected collection is: "+c);		
		
		if (c != null && store.isLoggedIn()) {
			TrpServerConn conn = store.getConnection();

			List<String> error = new ArrayList<>();
			for (TrpDocMetadata d : selected) {
				logger.debug("removing document: "+d+" to collection: "+c.getColId());				
				try {
					conn.removeDocFromCollection(c.getColId(), d.getDocId());
					logger.info("removed document: "+d);
				} catch (Throwable e) {
					logger.warn("Could not remove document: "+d);
					error.add(d.getTitle()+", ID = "+d.getDocId()+", Reason = "+e.getMessage());
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not remove the following documents:\n";
				for (String u : error) {
					msg += u + "\n";
				}
				
				mw.onError("Error removing documents", msg, null);
//				DialogUtil.showErrorMessageBox(shell, "Error removing documents", msg);
			} else {
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully removed "+selected.size()+" documents");
			}
			
			cmw.updateAll();
		}
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	private void deleteCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		if (c != null && store.isLoggedIn()) {
			TrpServerConn conn = store.getConnection();
			logger.debug("deleting collection: "+c.getColId()+" name: "+c.getColName());
			
			if(!store.getUser().isAdmin() && !isOwnerOfCurrentCollection()) {
				DialogUtil.showErrorMessageBox(shell, "Unauthorized", "You are not the owner of this collection.");
				return;
			}
			
			if (DialogUtil.showYesNoDialog(shell, "Are you sure?", "Do you really want to delete this collection?\n\n"
					+ "Note: documents are not deleted, only their reference to the collection is removed - "
					+ "use the delete document button to completely remove documents from the server!")!=SWT.YES) {
				return;
			}
			
			try {
				conn.deleteCollection(c.getColId());
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully deleted collection!");
				
				logger.info("deleted collection "+c.getColId()+" name: "+c.getColName());
				store.reloadCollections();
			} catch (Throwable th) {
				mw.onError("Error", "Error deleting collection '"+c.getColName()+"': "+th.getMessage(), th);
			}
		}
	}
	
	private void createCollection() {
		logger.debug("creating collection...");
		
		InputDialog dlg = new InputDialog(cmw.getShell(),
	            "Create collection", "Enter the name of the new collection (min. 3 characters)", "", new IInputValidator() {
					@Override public String isValid(String newText) {
						if (StringUtils.length(newText) >= 3)
							return null;
						else
							return "Too short";
					}
				});
		if (dlg.open() == Window.OK) {
			String collName = dlg.getValue();
			try {
				store.addCollection(dlg.getValue());
				logger.debug("created new collection '"+collName+"' - now reloading available collections!");
				store.reloadCollections();
			} catch (Throwable th) {
				mw.onError("Error", "Error creating collection '"+collName+"': "+th.getMessage(), th);	
			}
	    }	
	}
	
	void editSelectedUsersFromCollection() {
		//add dialog to check with the user
		int a = DialogUtil.showYesNoDialog(cmw.getShell(), "Change user role", "Really change the role of the selected user?");
		if (a == SWT.YES){
	
			TrpCollection collection = cmw.getSelectedCollection();
			if (store.isLoggedIn() && collection!=null) {
				TrpServerConn conn = store.getConnection();
				TrpRole r = cmw.getSelectedRole();
				List<TrpUser> selected = cmw.getSelectedUsersInCollection();
				
				List<String> error = new ArrayList<>();
				for (TrpUser u : selected) {
					logger.debug("edit user: "+u+ " new role: "+r.toString());				
					try {
						conn.addOrModifyUserInCollection(collection.getColId(), u.getUserId(), r);
						logger.info("edited user: "+u+ " new role: "+r.toString());		
					} catch (Throwable e) {
						logger.warn("Could not edit user: "+u+ " new role: "+r.toString());		
						error.add(u.getUserName()+" - reason: "+e.getMessage());
					}
				}
				
				if (!error.isEmpty()) {
					String msg = "Could not edit the following user:\n";
					for (String u : error) {
						msg += u + "\n";
					}
								
					mw.onError("Error editing user", msg, null);
				} else {
					DialogUtil.showInfoMessageBox(shell, "Success", "Successfully edited user ("+selected.size()+")");
				}
				
				cmw.updateUsersForSelectedCollection();
			}
		}
	}
	
	void removeSelectedUsersFromCollection() {

		
		TrpCollection collection = cmw.getSelectedCollection();
		if (store.isLoggedIn() && collection!=null) {
			if (cmw.isUploadedDocTabOpen()) {
				DialogUtil.showErrorMessageBox(shell, "Error", "Cannot determine the collection for an uploaded document");
				
				return;
			}			
			
			
			TrpServerConn conn = store.getConnection();
			List<TrpUser> selected = cmw.getSelectedUsersInCollection();
			
			List<String> error = new ArrayList<>();
			boolean currentUserAffected=false;
			for (TrpUser u : selected) {
				if (u.getUserId() == store.getUser().getUserId()) {
					currentUserAffected=true;
				}
				
				logger.debug("removing user: "+u);				
				try {
					conn.removeUserFromCollection(collection.getColId(), u.getUserId());
					logger.info("removed user: "+u);
				} catch (Throwable e) {
					logger.warn("Could not remove user: "+u);
					error.add(u.getUserName()+" - reason: "+e.getMessage());
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not remove the following user:\n";
				for (String u : error) {
					msg += u + "\n";
				}
				
				mw.onError("Error removing user", msg, null);
			} else {
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully removed user ("+selected.size()+")");
			}
			
			if (currentUserAffected) {
				try {
					store.reloadCollections();
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			cmw.updateUsersForSelectedCollection();
		}
	}
	
	TrpRole getRoleFromUser(String title) {
		List<String> roleStrs = new ArrayList<>();
		for (TrpRole r : TrpRole.values()) {
			if (!r.isVirtual()) {
				roleStrs.add(r.toString());
			}
		}
		
		ComboInputDialog d = new ComboInputDialog(shell, title==null?"Choose a role: ":title, roleStrs.toArray(new String[0]));
		
		if (d.open() != Window.OK)
			return null;
		
		return TrpRole.fromString(d.getSelectedText());
	}
	
	void addSelectedUsersToCollection() {
		TrpCollection collection = cmw.getSelectedCollection();
		if (store.isLoggedIn() && collection!=null) {
			TrpServerConn conn = store.getConnection();
			
			TrpRole r = getRoleFromUser(null);
			if (r==null)
				return;
			
			List<TrpUser> selected = cmw.findUsersWidget.getSelectedUsers();
			if (selected.isEmpty())
				return;

			List<String> error = new ArrayList<>();
			for (TrpUser u : selected) {
				logger.debug("adding user: "+u);
				
				try {
					conn.addOrModifyUserInCollection(collection.getColId(), u.getUserId(), r);
					logger.info("added user: "+u);
				} catch (Throwable e) {
					logger.warn("Could not add user: "+u);
					error.add(u.getUserName()+" - reason: "+e.getMessage());
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not add the following users:\n";
				for (String u : error) {
					msg += u + "\n";
				}
				
				mw.onError("Error adding user", msg, null);
			} else {
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully adding user ("+selected.size()+")");
			}
			
			cmw.updateUsersForSelectedCollection();
		}
	}
	
	boolean isUploader(TrpUserLogin user, TrpDocMetadata md) {
		return md.getUploaderId() == user.getUserId();
	}
	
	boolean isAdminOrUploader(TrpUserLogin user, TrpDocMetadata md) {
		return user.isAdmin() || isUploader(user, md);
	}
	
	boolean isAdminOrUploaderOrOwnerOfCurrentCollection(TrpUserLogin user, TrpDocMetadata md) {
		return user.isAdmin() || isUploader(user, md) || isOwnerOfCurrentCollection();
	}	
	
	boolean isOwnerOfCurrentCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		return c!=null && AuthUtils.isOwner(c.getRole());
	}
	
	boolean canManageCurrentCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		return c!=null && AuthUtils.canManage(c.getRole());
	}
	
	private void deleteDocument() {
		if (store.isLoggedIn()) {
			List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
//			if (selected.isEmpty() || selected.size() > 1) {
//				DialogUtil.showErrorMessageBox(shell, "Select a single document", "Please select a single document!");
//				return;
//			}
			
			if (selected.isEmpty()) {
				DialogUtil.showErrorMessageBox(shell, "Select a document", "Please select a document you wish to delete!");
				return;
			}
			else if (selected.size() > 1){
				if (DialogUtil.showYesNoDialog(shell, "Delete Documents", "Do you really want to delete " + selected.size() + " selected document ")!=SWT.YES) {
					return;
				}
			}
			else{
				if (DialogUtil.showYesNoDialog(shell, "Delete Document", "Do you really want to delete document "+selected.get(0).getTitle())!=SWT.YES) {
					return;
				}
			}
			
			TrpUserLogin user = store.getUser();
			
			//TrpDocMetadata md = selected.get(0);
			
			int count = 0;
			for (TrpDocMetadata md : selected){
				count++;
				if(!isAdminOrUploader(user, md)) {
					DialogUtil.showErrorMessageBox(shell, "Unauthorized", "You are not the owner of this document. " + md.getTitle());
					return;
				}

				try {
					store.deleteDocument(md.getColList().get(0).getColId(), md.getDocId());
				} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException | NoConnectionException e) {
					mw.onError("Error deleting document", e.getMessage(), e);
				}
				
				if (selected.size() == 1){
					DialogUtil.showInfoMessageBox(shell, "Success", "Successfully deleted document "+md.getTitle());
				}
				else if(count == selected.size()){
					DialogUtil.showInfoMessageBox(shell, "Success", "Successfully deleted "+selected.size()+" documents");
				}

				cmw.docsTableWidget.refreshList(cmw.getSelectedCollectionId(), false);
				cmw.updateDocumentsTable(md, false);
				cmw.getCurrentDocTableWidgetPagination().getPageableTable().refreshPage();
			}

			
		}
	}

	private void duplicateDocument() {
		if (store.isLoggedIn()) {
			List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
			if (selected.isEmpty() || selected.size() > 1) {
				DialogUtil.showErrorMessageBox(cmw.getShell(), "Select a single document", "Please select a single document");
				return;
			}
			
			TrpDocMetadata md = selected.get(0);
			
			if (!checkUploaderOrCollectionOwnerRights(store.getUser(), md))
				return;
			
			ChooseCollectionDialog diag = new ChooseCollectionDialog(cmw.shell, "Choose a collection to duplicate to");
			if (diag.open() != Dialog.OK)
				return;
			
			TrpCollection c = diag.getSelectedCollection();
			if (c==null) {
				DialogUtil.showErrorMessageBox(cmw.getShell(), "No collection selected", "Please select a collection to duplicate the document to!");
				return;
			}
			int toColId = c.getColId();
			
			InputDialog dlg = new InputDialog(shell, "New name", "Enter the new name of the document", null, null);
			if (dlg.open() != Window.OK)
				return;
			
			String newName = dlg.getValue();
			
			try {
				store.duplicateDocument(cmw.getSelectedCollection().getColId(), md.getDocId(), newName, toColId <= 0 ? null : toColId);
			} catch (SessionExpiredException | ServerErrorException
					| IllegalArgumentException | NoConnectionException e) {
				mw.onError("Error duplicating document", e.getMessage(), e);
			}
			
			DialogUtil.showInfoMessageBox(shell, "Success duplicating", "Go to the jobs view to check the status of duplication!");
		}
	}
	
	private void modifySelectedCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		if (c!=null && store.isLoggedIn()) {
			if (!canManageCurrentCollection()) {
				DialogUtil.showErrorMessageBox(shell, "Unauthorized", "You are not allowed to modify this collection!");
				return;
			}
			
			InputDialog id = new InputDialog(shell, "Modify collection", "Enter the new collection name: ", c.getColName(), new IInputValidator() {
				@Override public String isValid(String newText) {
					try {
						UserInputChecker.checkCollectionName(newText);
					} catch (InvalidUserInputException e) {
						return e.getMessage();
					}
					return null;
				}
			});
			if (id.open() != Window.OK)
				return;
			
			String newName = id.getValue();
			if (StringUtils.isEmpty(newName))
				return;
			
			try {
				store.getConnection().modifyCollection(c.getColId(), newName);
				store.reloadCollections();
				
				DialogUtil.showInfoMessageBox(shell, "Success", "Successfully modified the colleciton!");
			} catch (Exception e) {
				mw.onError("Error modifying collection", e.getMessage(), e);
			}
		}
	}	
	
	@Override public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	@Override public void dragExit(DragSourceEvent dse) {
	}

	@Override public void dragDropEnd(DragSourceDropEvent dsde) {
	}

}
