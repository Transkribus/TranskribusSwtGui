package eu.transkribus.swt_gui.collection_comboviewer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.pagination_tables.CollectionsOfUserTableWidgetPagination;

public class CollectionOverviewDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(CollectionOverviewDialog.class);

	TrpCollection selectedCollection;
//	List<TrpCollection> collections;
	CollectionsOfUserTableWidgetPagination collectionTable;
	TrpUser user;

	public CollectionOverviewDialog(Shell parentShell, TrpUser trpUser) {
		super(parentShell);
		this.user = trpUser;
	}
	
	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.TITLE);
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Collections of user " + user.getUserName());
		shell.setSize(800, 650);
		SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		collectionTable = new CollectionsOfUserTableWidgetPagination(container, SWT.SINGLE | SWT.FULL_SELECTION, 50, null, null, user.getUserId());
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
