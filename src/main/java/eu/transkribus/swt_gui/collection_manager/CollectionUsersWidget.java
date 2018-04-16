package eu.transkribus.swt_gui.collection_manager;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpUserCollection;
import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;
import eu.transkribus.swt_gui.pagination_tables.UserTableWidgetPagination;

/**
 * Combines a table for the users in a collection with a widget to find and add new users to the collection
 */
public class CollectionUsersWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(CollectionUsersWidget.class);
	
	static Storage store = Storage.getInstance(); 
	
	UserTableWidgetPagination collectionUsersTv;
	FindUsersWidget findUsersWidget;
	
	Button addUserToColBtn, removeUserFromColBtn, showUserCollections/*, editUserFromColBtn*/;
	Combo role;

	Group group;
	TrpCollection collection;
	
	CollectionUsersWidgetListener listener;

	public CollectionUsersWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		Composite sf = new SashForm(this, SWT.VERTICAL);
//		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sf.setLayout(new GridLayout(1, false));
		
		createCollectionUsersTable(sf);
		createFindUsersWidget(sf);
		
		addListener();
	}
	
	private void createCollectionUsersTable(Composite container) {
		group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Users in collection ");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new GridLayout(1, false));
		group.setFont(Fonts.createBoldFont(group.getFont()));
		
		collectionUsersTv = new UserTableWidgetPagination(group, 0, 25);
		collectionUsersTv.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// add buttons:
		Composite btns = new Composite(group, 0);
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		btns.setLayout(new GridLayout(5, false));
		
		btns.setLayoutData(new GridData(SWT.TOP, SWT.FILL, true, false, 1, 1));
		addUserToColBtn = new Button(btns, SWT.PUSH);
		addUserToColBtn.setText("Add user");
		addUserToColBtn.setImage(Images.getOrLoad("/icons/user_add.png"));
		addUserToColBtn.setToolTipText("Add selected users from search window on the right to collection");
		addUserToColBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				
		removeUserFromColBtn = new Button(btns, SWT.PUSH);
		removeUserFromColBtn.setText("Remove user");
		removeUserFromColBtn.setToolTipText("Remove selected users from collection");
		removeUserFromColBtn.setImage(Images.getOrLoad("/icons/user_delete.png"));
		removeUserFromColBtn.setLayoutData(new GridData(GridData.FILL_VERTICAL));
				
		Label l = new Label(btns, 0);
		l.setText("Change Role:");
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalAlignment = SWT.CENTER;
		removeUserFromColBtn.setLayoutData(gd);
		
		role = new Combo(btns, SWT.READ_ONLY);
		role.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		for (TrpRole r : TrpRole.values()) {
			if (!r.isVirtual()) {
				role.add(r.toString());
			}
		}
		
		showUserCollections = new Button(btns, SWT.PUSH);
		showUserCollections.setText("Collections");
		showUserCollections.setToolTipText("Show all collections of this user");
		showUserCollections.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		showUserCollections.setVisible(false);
		
		selectRole(TrpRole.Transcriber);
				
		group.pack();
	}
	
	private void createFindUsersWidget(Composite container) {
		Group group = new Group(container, SWT.SHADOW_ETCHED_IN);
		group.setText("Find users");
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group.setLayout(new FillLayout());
		group.setFont(Fonts.createBoldFont(group.getFont()));
		
		findUsersWidget = new FindUsersWidget(group, 0);
		findUsersWidget.getUsersTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				//new user selected in 'Find users' ==> deselect user in 'Users in collection' table and vice versa
				IStructuredSelection users = findUsersWidget.getSelectedUsersAsStructuredSelection();
				if (!getSelectedUsersInCollection().isEmpty()){
					collectionUsersTv.getTableViewer().setSelection(null);
					if(users != null)
						findUsersWidget.setSelectedUsers(users);
				}
				
				updateBtnVisibility();
			}
		});
	}
	
	private void addListener() {
		listener = new CollectionUsersWidgetListener(this);
		
		collectionUsersTv.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				//new user selected in 'Users in collection' ==> deselect user in 'Find users' table and vice versa
				ISelection users = collectionUsersTv.getSelectedAsIStructuredSelection();
				if (!findUsersWidget.getSelectedUsers().isEmpty()){
					findUsersWidget.getUsersTableViewer().setSelection(null);
					if(users != null)
						collectionUsersTv.getTableViewer().setSelection(users);
				}
				updateBtnVisibility();
			}
		});
	}
	
	public List<TrpUser> getSelectedUsersInCollection() {
		return ((IStructuredSelection) collectionUsersTv.getTableViewer().getSelection()).toList();
	}
	
	public TrpUser getFirstSelectedUser(){
		if (!findUsersWidget.getSelectedUsers().isEmpty()){
			return findUsersWidget.getSelectedUsers().get(0);
		}
		return null;
	}
	
	public TrpRole getSelectedRole() {
		return TrpRole.fromStringNonVirtual(role.getItem(role.getSelectionIndex()));
	}
		
	void selectRole(TrpRole r) {
		if (r == null)
			return;
		
		for (int i=0; i<role.getItemCount(); ++i) {
			if (role.getItem(i).equals(r.toString())) {
				role.select(i);
				break;
			}
		}		
	}
	
	void updateBtnVisibility() {
		boolean isAdmin = store.getUser() != null ? store.getUser().isAdmin() : false;
		
		boolean hasRole = collection!=null && collection.getRole()!=null;
		boolean canManage = hasRole && collection.getRole().canManage() || isAdmin;
//		boolean isOwner = hasRole && collection.getRole().getValue()>=TrpRole.Owner.getValue() || isAdmin;
		
		boolean hasFindUsersSelected = !findUsersWidget.getSelectedUsers().isEmpty();
		boolean hasCollectionUsersSelected = !getSelectedUsersInCollection().isEmpty();
					
		addUserToColBtn.setEnabled(canManage && hasFindUsersSelected);
		removeUserFromColBtn.setEnabled(canManage && hasCollectionUsersSelected);
		 
//		editUserFromColBtn.setEnabled(isOwner && hasCollectionUsersSelected);
				
		if (canManage && hasCollectionUsersSelected) {
			role.setEnabled(true);
			// update role combo:
			List<TrpUser> us = getSelectedUsersInCollection();
			if (us.size() > 0) {
				TrpUserCollection uc = us.get(0).getUserCollection();
				TrpRole r = uc == null ? null : uc.getRole(); 
				selectRole(r);
			}
		}
		else{
			role.setEnabled(false);
		}
		
		if (isAdmin && getFirstSelectedUser() != null){
			showUserCollections.setVisible(true);
		}
		else{
			showUserCollections.setVisible(false);
		}
	}
	
	public void setCollection(TrpCollection collection) {
		this.collection = collection;
		
		updateUsersForSelectedCollection();
		updateGroupText();
	}
	
	private void updateGroupText() {
		if (collection != null){
			group.setText("Users in collection " + collection.getColName());
		}
		
	}

	public TrpCollection getCollection() {
		return collection;
	}
	
	public void updateUsersForSelectedCollection() {
		logger.debug("updating users for selected collection: "+collection);
//		TrpCollection c = getSelectedCollection();
		updateBtnVisibility();
		
		if (collection!=null && store.isLoggedIn()) {
			try {
				collectionUsersTv.refreshList(collection.getColId());
			} catch (ServerErrorException | IllegalArgumentException e) {
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, e.getMessage(), "Error loading users", -1, -1, true);
			}
		}
	}

}
