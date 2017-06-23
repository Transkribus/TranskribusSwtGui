package eu.transkribus.swt_gui.collection_comboviewer;

import java.util.function.Predicate;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.pagination_tables.CollectionsTableWidgetPagination;

public class CollectionSelectorDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(CollectionSelectorDialog.class);

	TrpCollection selectedCollection;
//	List<TrpCollection> collections;
	CollectionsTableWidgetPagination collectionTable;
	Button createBtn, deleteBtn, modifyBtn, addUsersBtn;
	TrpCollection initColl;
	
	Predicate<TrpCollection> collectionPredicate;

	public CollectionSelectorDialog(Shell parentShell, Predicate<TrpCollection> collectionPredicate, TrpCollection initColl) {
		super(parentShell);
		
		this.collectionPredicate = collectionPredicate;
		this.initColl = initColl;
	}
	
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Choose a collection via double click");
		shell.setSize(800, 650);
		SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		collectionTable = new CollectionsTableWidgetPagination(container, SWT.SINGLE | SWT.FULL_SELECTION, 50, collectionPredicate, null, initColl);
		collectionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		IDoubleClickListener openSelectedColListener = new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		};
		collectionTable.getTableViewer().addDoubleClickListener(openSelectedColListener);
//		collectionTable.refreshList(collections);
		collectionTable.getFilter().setFocus();
		collectionTable.getFilter().setSelection(0);
		
		Composite btns = new Composite(container, 0);
		btns.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		btns.setLayout(new GridLayout(4, true));
				
		createBtn = new Button(btns, SWT.PUSH);
		createBtn.setText("Create");
		createBtn.setImage(Images.ADD);
		createBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		deleteBtn = new Button(btns, SWT.PUSH);
		deleteBtn.setText("Delete");
		deleteBtn.setImage(Images.DELETE);
		deleteBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		modifyBtn = new Button(btns, SWT.PUSH);
		modifyBtn.setText("Modify");
		modifyBtn.setImage(Images.PENCIL);
		modifyBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		addUsersBtn = new Button(btns, SWT.PUSH);
		addUsersBtn.setText("Manage users");
		addUsersBtn.setImage(Images.USER_EDIT);
		addUsersBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		SelectionAdapter btnsListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrpCollection c = collectionTable.getFirstSelected();
				logger.trace("c = "+c+" source = "+e.getSource());
				
				if (e.getSource() == createBtn) {
					int collId = TrpMainWidget.getInstance().createCollection();
					if (collId > 0) {
						collectionTable.loadPage("colId", collId, false);
					}
				}
				else if (e.getSource() == deleteBtn && c!=null) {
					TrpMainWidget.getInstance().deleteCollection(c);
				}
				else if (e.getSource() == modifyBtn && c!=null) {
					TrpMainWidget.getInstance().modifyCollection(c);
				}
				else if (e.getSource() == addUsersBtn && c!=null) {
					TrpMainWidget.getInstance().openCollectionUsersDialog(c);
				}
			}
		};
		createBtn.addSelectionListener(btnsListener);
		deleteBtn.addSelectionListener(btnsListener);
		modifyBtn.addSelectionListener(btnsListener);
		addUsersBtn.addSelectionListener(btnsListener);

		return container;
	}
	
	@Override protected void okPressed() {
		this.selectedCollection = collectionTable.getFirstSelected();
		
		super.okPressed();
	}

	public TrpCollection getSelectedCollection() {
		return this.selectedCollection;
	}
	
}
