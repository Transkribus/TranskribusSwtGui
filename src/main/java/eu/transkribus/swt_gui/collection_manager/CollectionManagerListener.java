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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.UserInputChecker;
import eu.transkribus.swt.util.ComboInputDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.doc_overview.ServerWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

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
		
		cmw.getShell().addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
		
		attach();
	}
	
	public void attach() {
		cmw.addCollectionBtn.addSelectionListener(this);
		cmw.deleteCollectionBtn.addSelectionListener(this);
		cmw.addDocumentToCollBtn.addSelectionListener(this);
		cmw.removeDocumentFromCollBtn.addSelectionListener(this);
		cmw.modifyCollectionBtn.addSelectionListener(this);
		cmw.deleteDocumentBtn.addSelectionListener(this);
		cmw.duplicatedDocumentBtn.addSelectionListener(this);
		store.addListener(this);
	}
	
	public void detach() {
		cmw.addCollectionBtn.removeSelectionListener(this);
		cmw.deleteCollectionBtn.removeSelectionListener(this);
		cmw.addDocumentToCollBtn.removeSelectionListener(this);
		cmw.removeDocumentFromCollBtn.removeSelectionListener(this);
		cmw.modifyCollectionBtn.removeSelectionListener(this);
		cmw.deleteDocumentBtn.removeSelectionListener(this);
		cmw.duplicatedDocumentBtn.removeSelectionListener(this);
		store.removeListener(this);
	}

	@Override public void widgetSelected(SelectionEvent e) {
		try {
			Object s = e.getSource();

			if (s == cmw.addCollectionBtn) {
				mw.createCollection();
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
		if (SWTUtil.isDisposed(shell))
			return;
		
		cmw.collectionsTv.refreshList(cle.collections);
	}
	
	@Override public void handleDocListLoadEvent(DocListLoadEvent dle) {
		if (SWTUtil.isDisposed(shell))
			return;		
		
		cmw.updateCollections();
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
		
		mw.addDocumentsToCollection(cmw.getSelectedCollection().getColId(), selected);
		
		cmw.docsTableWidget.refreshPage(false);
	}
	
	private void removeDocumentFromCollection() {
		List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
		TrpCollection c = cmw.getSelectedCollection();
		if (selected.isEmpty() || c==null)
			return;
		
		mw.removeDocumentsFromCollection(c.getColId(), selected);
		
		cmw.updateAll();
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	private void deleteCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		
		mw.deleteCollection(c);
	}
	
	public static boolean isUploader(TrpUserLogin user, TrpDocMetadata md) {
		return md.getUploaderId() == user.getUserId();
	}
	
	public static boolean isAdminOrUploader(TrpUserLogin user, TrpDocMetadata md) {
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
			if (!CoreUtils.isEmpty(selected)) {
				mw.deleteDocuments(selected);
				mw.reloadDocList(mw.getSelectedCollectionId());
			}
		}
	}

	private void duplicateDocument() {
		List<TrpDocMetadata> selected = cmw.getSelectedDocuments();
		if (!CoreUtils.isEmpty(selected)) {
			mw.duplicateDocuments(cmw.getSelectedCollectionId(), selected);
			mw.reloadDocList(mw.getSelectedCollectionId());
		}
	}
	
	private void modifySelectedCollection() {
		TrpCollection c = cmw.getSelectedCollection();
		
		mw.modifyCollection(c);
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
