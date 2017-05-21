package eu.transkribus.swt_gui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.pagination_tables.CollectionsTableWidgetPagination;

public class CollectionSelectorDialog extends Dialog {

	private TrpCollection selectedCollection;
	
	private List<TrpCollection> collections;
	private CollectionsTableWidgetPagination collectionTable;

	public CollectionSelectorDialog(Shell parentShell, List<TrpCollection> collections) {
		super(parentShell);
		this.collections = collections;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Choose a collection via double click");
		shell.setSize(600, 600);
		SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		collectionTable = new CollectionsTableWidgetPagination(container, SWT.SINGLE | SWT.FULL_SELECTION, 50, null, true);
		collectionTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		IDoubleClickListener openSelectedColListener = new IDoubleClickListener() {
			@Override public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		};		
		collectionTable.getTableViewer().addDoubleClickListener(openSelectedColListener);
		
		collectionTable.refreshList(collections);
		collectionTable.getFilter().setFocus();
		collectionTable.getFilter().setSelection(0);

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
