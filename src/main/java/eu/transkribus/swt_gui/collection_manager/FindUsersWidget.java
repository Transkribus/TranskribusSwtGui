package eu.transkribus.swt_gui.collection_manager;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.mytableviewer.MyTableViewer;
import org.dea.swt.util.DefaultTableColumnViewerSorter;
import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.Images;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class FindUsersWidget extends Composite {
	
	private final static Logger logger = LoggerFactory.getLogger(CollectionManagerDialog.class);
	MyTableViewer usersTv;
	Text userNameText;
	Text firstNameText;
	Text lastNameText;
	Button find;
	
//	CollectionManagerDialog cmw;
	
	static final Storage store = Storage.getInstance();
	
	
	public static final String USER_USERNAME_COL = "Username";
	public static final String USER_FULLNAME_COL = "Name";
	
	public static final ColumnConfig[] USER_COLS = new ColumnConfig[] {
		new ColumnConfig(USER_USERNAME_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(USER_FULLNAME_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
	};

	public FindUsersWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));

//		Assert.assertNotNull(cmw);
//		this.cmw = cmw;
		
		Label l0 = new Label(this, 0);
		l0.setText("Users");
		
		createUsersTable();
		
		Label l1 = new Label(this, 0);
		l1.setText("Username / E-Mail: ");
		userNameText = new Text(this, SWT.SINGLE | SWT.BORDER);
		userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l2 = new Label(this, 0);
		l2.setText("First name: ");
		firstNameText = new Text(this, SWT.SINGLE | SWT.BORDER);
		firstNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l3 = new Label(this, 0);
		l3.setText("Last name: ");
		lastNameText = new Text(this, SWT.SINGLE | SWT.BORDER);
		lastNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		find = new Button(this, SWT.PUSH);
		find.setText("Find");
		find.setImage(Images.getOrLoad("/icons/find.png"));
		find.setText("Find users");
		find.setToolTipText("Finds users with the current filter values");
		find.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
//		add = new Button(this, SWT.PUSH);
//		add.setText("Add");
//		add.setImage(Images.getOrLoad("/icons/user_add.png"));
//		add.setToolTipText("Add selected users to collection");
				
		addListener();
	}
	
	private void addListener() {
		SelectionAdapter enterPressedInSearchTextFieldListener = new SelectionAdapter() {
			@Override public void widgetDefaultSelected(SelectionEvent e) {
				searchUsers();
			}
		};
		userNameText.addSelectionListener(enterPressedInSearchTextFieldListener);
		firstNameText.addSelectionListener(enterPressedInSearchTextFieldListener);
		lastNameText.addSelectionListener(enterPressedInSearchTextFieldListener);
		
		find.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				searchUsers();
			}
		});
	
	}
		
	private void searchUsers() {
		if (store.isLoggedIn()) {
			boolean exactMatch = false;
			boolean caseSensitive = false;
			try {
				List<TrpUser> users = store.getConnection().findUsers(userNameText.getText(), firstNameText.getText(), lastNameText.getText(), exactMatch, caseSensitive);
				setUsers(users);
			} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
				DialogUtil.createAndShowBalloonToolTip(getShell(), SWT.ICON_ERROR, e1.getMessage(), "Error searching users", -1, -1, true);
				setUsers(null);
			}
		}
	}
	
	public List<TrpUser> getSelectedUsers() {
		IStructuredSelection sel = (IStructuredSelection) usersTv.getSelection();
		return sel.toList();	
	}
		
	public MyTableViewer getUsersTableViewer() {
		return usersTv;
	}

	private void createUsersTable() {
		
		usersTv = new MyTableViewer(this, SWT.MULTI | SWT.FULL_SELECTION);
		usersTv.setContentProvider(new ArrayContentProvider());
		usersTv.setLabelProvider(new UsersTableLabelProvider(usersTv));
		
		Table table = usersTv.getTable();
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		usersTv.addColumns(USER_COLS);
	}
			
	public void setUsers(List<TrpUser> users) {
		usersTv.setInput(users);
	}
	
}
