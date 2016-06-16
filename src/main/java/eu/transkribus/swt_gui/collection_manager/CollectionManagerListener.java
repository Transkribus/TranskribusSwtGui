package eu.transkribus.swt_gui.collection_manager;

import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.core.model.beans.auth.TrpUserLogin;
import eu.transkribus.swt_canvas.util.ComboInputDialog;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.TextAndComboInputDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.Storage.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.AStorageObserver;

@Deprecated
public class CollectionManagerListener extends AStorageObserver implements SelectionListener, DragSourceListener  {
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerListener.class);
	
	CollectionManagerDialog cmw;
	TrpMainWidget mw;
	Storage store = Storage.getInstance();
	
	public CollectionManagerListener(CollectionManagerDialog cmw) {
		this.cmw = cmw;
		this.mw = TrpMainWidget.getInstance();
		
		attach();
	}
	
	public void attach() {
		cmw.addUserToColBtn.addSelectionListener(this);
		cmw.removeUserFromColBtn.addSelectionListener(this);
		cmw.editUserFromColBtn.addSelectionListener(this);
		cmw.addCollectionBtn.addSelectionListener(this);
		cmw.deleteCollectionBtn.addSelectionListener(this);
		cmw.addDocumentToCollBtn.addSelectionListener(this);
		cmw.removeDocumentFromCollBtn.addSelectionListener(this);
		cmw.reloadMyDocsBtn.addSelectionListener(this);
		cmw.reloadCollectionsBtn.addSelectionListener(this);
		cmw.modifyCollectionBtn.addSelectionListener(this);
		cmw.delDocumentBtn.addSelectionListener(this);
		cmw.duplicateBtn.addSelectionListener(this);
		
		store.addObserver(this);
	}
	
	public void detach() {
		cmw.addUserToColBtn.removeSelectionListener(this);
		cmw.removeUserFromColBtn.removeSelectionListener(this);
		cmw.editUserFromColBtn.removeSelectionListener(this);
		cmw.addCollectionBtn.removeSelectionListener(this);
		cmw.deleteCollectionBtn.removeSelectionListener(this);
		cmw.addDocumentToCollBtn.removeSelectionListener(this);
		cmw.removeDocumentFromCollBtn.removeSelectionListener(this);
		cmw.reloadMyDocsBtn.removeSelectionListener(this);
		cmw.reloadCollectionsBtn.removeSelectionListener(this);
		cmw.modifyCollectionBtn.removeSelectionListener(this);
		cmw.delDocumentBtn.removeSelectionListener(this);
		cmw.duplicateBtn.removeSelectionListener(this);
		
		store.deleteObserver(this);
	}

	@Override public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		if (s == cmw.addUserToColBtn) {
			addSelectedUsersToCollection();
		} else if (s == cmw.removeUserFromColBtn) {
			removeSelectedUsersFromCollection();
		} else if (s == cmw.editUserFromColBtn) {
			editSelectedUsersFromCollection();
		} else if (s == cmw.addCollectionBtn) {
			createCollection();
		} else if (s == cmw.deleteCollectionBtn) {
			deleteCollection();
		} else if (s == cmw.addDocumentToCollBtn) {
			addDocumentToCollection();
		} else if (s == cmw.removeDocumentFromCollBtn) {
			removeDocumentFromCollection();
		} else if (s == cmw.reloadMyDocsBtn) {
			cmw.updateMyDocuments();
		} else if (s == cmw.reloadCollectionsBtn) {
			cmw.updateCollections();
		} else if (s == cmw.modifyCollectionBtn) {
			cmw.modifySelectedCollection();
		} else if (s == cmw.delDocumentBtn){
			deleteDocument();
		} else if (s == cmw.duplicateBtn) {
			duplicateDocument();
		}
	}
	
	@Override protected void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
		cmw.collectionsTv.refreshList(cle.collections);
	}	

	private void addDocumentToCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		
		if (c != null && store.isLoggedIn()) {
			TrpServerConn conn = store.getConnection();
			
			List<TrpDocMetadata> selected = cmw.getSelectedMyDocuments();
			if (selected.isEmpty())
				return;
			
			List<TrpDocMetadata> error = new ArrayList<>();
			for (TrpDocMetadata d : selected) {
				logger.debug("adding document: "+d+" to collection: "+c.getColId());				
				try {
					conn.addDocToCollection(c.getColId(), d.getDocId());
					logger.info("added document: "+d);
				} catch (Throwable e) {
					logger.warn("Could not add document: "+d);
					error.add(d);
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not add the following documents:\n";
				for (TrpDocMetadata u : error) {
					msg += u.getTitle() + "\n";
				}
				
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error(s) adding documents", -1, -1, true);
			} else {
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully added "+selected.size()+" documents", "Success", -1, -1, true);
			}
			
			cmw.docsForCollectionTableWidget.refreshPage(false);
//			cmw.updateAll();
		}
	}
	
	private void removeDocumentFromCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		
		if (c != null && store.isLoggedIn()) {
			TrpServerConn conn = store.getConnection();
			
			List<TrpDocMetadata> selected = cmw.getSelectedCollectionDocuments();
			if (selected.isEmpty())
				return;
			
			List<TrpDocMetadata> error = new ArrayList<>();
			for (TrpDocMetadata d : selected) {
				logger.debug("removing document: "+d+" to collection: "+c.getColId());				
				try {
					conn.removeDocFromCollection(c.getColId(), d.getDocId());
					logger.info("removed document: "+d);
				} catch (Throwable e) {
					logger.warn("Could not remove document: "+d);
					error.add(d);
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not remove the following documents:\n";
				for (TrpDocMetadata u : error) {
					msg += u.getTitle() + "\n";
				}
				
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error(s) remvoving documents", -1, -1, true);
			} else {
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully removed "+selected.size()+" documents", "Success", -1, -1, true);
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
			
			try {
				conn.deleteEmptyCollection(c.getColId());
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
		TrpCollection collection = cmw.getSelectedCollection();
		if (store.isLoggedIn() && collection!=null) {
			TrpServerConn conn = store.getConnection();
			TrpRole r = TrpRole.fromStringNonVirtual(cmw.role.getItem(cmw.role.getSelectionIndex()));
			List<TrpUser> selected = cmw.getSelectedUsersInCollection();
			
			List<TrpUser> error = new ArrayList<>();
			for (TrpUser u : selected) {
				logger.debug("edit user: "+u+ " new role: "+r.toString());				
				try {
					conn.addOrModifyUserInCollection(collection.getColId(), u.getUserId(), r);
					logger.info("edited user: "+u+ " new role: "+r.toString());		
				} catch (Throwable e) {
					logger.warn("Could not edit user: "+u+ " new role: "+r.toString());		
					error.add(u);
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not edit the following user:\n";
				for (TrpUser u : error) {
					msg += u.getInfo(true) + "\n";
				}
				
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error(s) editing user", -1, -1, true);
			} else {
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully edited "+selected.size()+" user", "Success", -1, -1, true);
			}
			
			cmw.updateUsersForSelectedCollection();
		}
	}
	
	void removeSelectedUsersFromCollection() {
		TrpCollection collection = cmw.getSelectedCollection();
		if (store.isLoggedIn() && collection!=null) {
			TrpServerConn conn = store.getConnection();
			List<TrpUser> selected = cmw.getSelectedUsersInCollection();
			
			List<TrpUser> error = new ArrayList<>();
			for (TrpUser u : selected) {
				logger.debug("removing user: "+u);				
				try {
					conn.removeUserFromCollection(collection.getColId(), u.getUserId());
					logger.info("removed user: "+u);
				} catch (Throwable e) {
					logger.warn("Could not remove user: "+u);
					error.add(u);
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not remove the following user:\n";
				for (TrpUser u : error) {
					msg += u.getInfo(true) + "\n";
				}
				
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error(s) removing user", -1, -1, true);
			} else {
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully removed "+selected.size()+" user", "Success", -1, -1, true);
			}
			
			cmw.updateUsersForSelectedCollection();
		}
	}
	
	void addSelectedUsersToCollection() {
		TrpCollection collection = cmw.getSelectedCollection();
		if (store.isLoggedIn() && collection!=null) {
			TrpServerConn conn = store.getConnection();
			TrpRole r = TrpRole.fromStringNonVirtual(cmw.role.getItem(cmw.role.getSelectionIndex()));
			List<TrpUser> selected = cmw.findUsersWidget.getSelectedUsers();
//			if (selected.isEmpty()) {
//				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_INFORMATION, "No users selected", "No ", -1, -1, true);
//			}

			List<TrpUser> error = new ArrayList<>();
			for (TrpUser u : selected) {
				logger.debug("adding user: "+u);
				
				try {
					conn.addOrModifyUserInCollection(collection.getColId(), u.getUserId(), r);
					logger.info("added user: "+u);
				} catch (Throwable e) {
					logger.warn("Could not add user: "+u);
					error.add(u);
				}
			}
			
			if (!error.isEmpty()) {
				String msg = "Could not add the following users:\n";
				for (TrpUser u : error) {
					msg += u.getInfo(true) + "\n";
				}
				
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error(s) adding user", -1, -1, true);
			} else {
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully added "+selected.size()+" user", "Success", -1, -1, true);
			}
			
			cmw.updateUsersForSelectedCollection();
		}
	}
	
	private void deleteDocument() {
		if (store.isLoggedIn()) {
			List<TrpDocMetadata> selected = cmw.getSelectedMyDocuments();
			if (selected.isEmpty() || selected.size() > 1)
				return;
			
			TrpDocMetadata md = selected.get(0);
			
			TrpUserLogin user = store.getUser();
			
			if(/*!user.isAdmin() || */md.getUploaderId() != user.getUserId()){
				//show error dialog
				MessageBox messageBox = new MessageBox(cmw.getShell(), SWT.ICON_INFORMATION
			            | SWT.OK);
			        messageBox.setMessage("You are not the owner of this document.");
			        messageBox.setText("Unauthorized");
			        messageBox.open();
				return;
			}
			
			//open yes/no dialog
			MessageBox messageBox = new MessageBox(cmw.getShell(), SWT.ICON_QUESTION
		            | SWT.YES | SWT.NO);
	        messageBox.setMessage("Do you really want to delete this document?");
	        messageBox.setText("Delete Document");
	        int response = messageBox.open();
	        if (response == SWT.YES){
	        	try {
					store.deleteDocument(md.getColList().get(0).getColId(), md.getDocId());
				} catch (SessionExpiredException | ServerErrorException
						| IllegalArgumentException | NoConnectionException e) {
					final String msg = e.getMessage();
					DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error deleting document", -1, -1, true);
				}
	        } else {
	        	return;
	        }
	          
			DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully deleted document", "Success", -1, -1, true);

			
			cmw.updateAll();
		}
	}

	private void duplicateDocument() {
		if (store.isLoggedIn()) {
			List<TrpDocMetadata> selected = cmw.getSelectedMyDocuments();
			if (selected.isEmpty() || selected.size() > 1)
				return;
			
			TrpDocMetadata md = selected.get(0);
			
			TrpUserLogin user = store.getUser();
			
			if(!user.isAdmin() && md.getUploaderId() != user.getUserId()){
				//show error dialog
				MessageBox messageBox = new MessageBox(cmw.getShell(), SWT.ICON_INFORMATION
			            | SWT.OK);
			        messageBox.setMessage("You are not the owner of this document.");
			        messageBox.setText("Unauthorized");
			        messageBox.open();
				return;
			}
			
			
			List<String> items = new ArrayList<>();
			for (TrpCollection c : md.getColList()) {
				items.add(c.getColName());
			}
			
			TextAndComboInputDialog cd = 
					new TextAndComboInputDialog(cmw.getShell(), "New Document Name: ", "Copy of " + md.getTitle(), 
							"Select target collection: ", items.toArray(new String[0]));
			
			if (cd.open() != IDialogConstants.OK_ID) {
				return;
			}

			logger.debug("selected index: "+cd.selectedIndex);
			logger.debug("selected name: " +cd.inputText);
			TrpCollection coll = md.getColList().get(cd.selectedIndex);
			int colId = coll.getColId();
			String newName = cd.inputText;
	        
			try {
				store.duplicateDocument(colId, md.getDocId(), newName, null);
			} catch (SessionExpiredException | ServerErrorException
					| IllegalArgumentException | NoConnectionException e) {
				final String msg = e.getMessage();
				DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error while duplicating document", -1, -1, true);
			}
			
			//open yes/no dialog
//			MessageBox messageBox = new MessageBox(cmw.getShell(), SWT.ICON_QUESTION
//		            | SWT.YES | SWT.NO);
//	        messageBox.setMessage("Do you really want to delete this document?");
//	        messageBox.setText("Delete Document");
//	        int response = messageBox.open();
	        
	        
//	        if (response == SWT.YES){
//	        	try {
//					store.duplicateDocument(colId, md.getDocId(), newName);
//				} catch (SessionExpiredException | ServerErrorException
//						| IllegalArgumentException | NoConnectionException e) {
//					final String msg = e.getMessage();
//					DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_ERROR, msg, "Error deleting document", -1, -1, true);
//				}
//	        } else {
//	        	return;
//	        }
//	          
//			DialogUtil.createAndShowBalloonToolTip(cmw.getShell(), SWT.ICON_INFORMATION, "Successfully deleted document", "Success", -1, -1, true);
//
//			
//			cmw.updateAll();
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
