package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.doc_overview.DocTableWidgetPagination;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class RecycleBinDialog extends Dialog implements SelectionListener {
	private static final Logger logger = LoggerFactory.getLogger(RecycleBinDialog.class);
	
	CTabFolder mainTf;
	
	CTabItem documentItem;
	CTabItem transcriptItem;
	
	TranscriptsTableWidgetPagination transcriptsWidget;
	TranscriptsTableWidgetListener transcriptsWidgetListener;
	
	DocTableWidgetPagination docTableWidget;
	
	ToolItem deleteSelected;
	ToolItem deleteAll;
	ToolItem retrieveSelected;
	
	List<Control> userControls = new ArrayList<>();
	
	int collectionId;
	
	public RecycleBinDialog(Shell parentShell, int colId) {
		super(parentShell);
		collectionId = colId;
	}
	
	void updateLoggedIn() {
		boolean isLoggedIn = Storage.getInstance().isLoggedIn();
		
		for (Control c : userControls) {			
			c.setEnabled(isLoggedIn);
		}
		
		if (!isLoggedIn) {
			clearDocList();
		}
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setSize(1000, 800);
	      SWTUtil.centerShell(shell);
	      shell.setText("Deleted documents and transcripts");
	}

	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, true));
		
//		transcriptsWidget = new TranscriptsTableWidgetPagination(container, 0, 50);
//		transcriptsWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
//		transcriptsWidgetListener = new TranscriptsTableWidgetListener(transcriptsWidget);
		
		ToolBar tb = new ToolBar(container, SWT.NONE);
		
		deleteSelected = new ToolItem(tb, SWT.NONE);
		deleteSelected.setData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		deleteSelected.setToolTipText("Delete selected");
		deleteSelected.setImage(Images.getOrLoad("/icons/package_delete.png"));
		SWTUtil.addSelectionListener(deleteSelected, this);
		
		new ToolItem(tb, SWT.SEPARATOR);
		
		deleteAll = new ToolItem(tb, SWT.NONE);
		deleteAll.setData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
		deleteAll.setToolTipText("Empty recycle bin");
		deleteAll.setImage(Images.getOrLoad("/icons/bin_empty.png"));
		SWTUtil.addSelectionListener(deleteAll, this);
		
		new ToolItem(tb, SWT.SEPARATOR);
		
		retrieveSelected = new ToolItem(tb, SWT.NONE);
		retrieveSelected.setData(new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1));
		retrieveSelected.setToolTipText("Retrieve Selected");
		retrieveSelected.setImage(Images.getOrLoad("/icons/arrow_undo.png"));
		SWTUtil.addSelectionListener(retrieveSelected, this);
		
		new ToolItem(tb, SWT.SEPARATOR);
		
		docTableWidget = new DocTableWidgetPagination(container, 0, 50, true);
		docTableWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		docTableWidget.refreshList(collectionId);
		userControls.add(docTableWidget);
								
		container.pack();

		return container;
	}
	

	@Override protected Point getInitialSize() { return new Point(1000, 800); }
	@Override protected boolean isResizable() { return true; }
	@Override protected void createButtonsForButtonBar(Composite parent) {}

	@Override protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.BORDER | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		setBlockOnOpen(false);
	}
	
	
	public void updateHighlightedRow(int selectedId) {
		docTableWidget.getTableViewer().refresh();
	}
		
	public void clearDocList() {
		docTableWidget.refreshList(0, true, false);
	}
	
	public DocTableWidgetPagination getDocTableWidget() { return docTableWidget; }
	
	public TableViewer getTableViewer() { return docTableWidget.getTableViewer(); }
		
	public TrpDocMetadata getSelectedDocument() {
		return docTableWidget.getFirstSelected();
	}
	
	public List<TrpDocMetadata> getSelectedDocuments() {
		return docTableWidget.getSelected();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		TrpMainWidget mw = TrpMainWidget.getInstance();

		if (s == deleteAll){
			logger.debug("delete all - empty the recycle bin");
			for (TrpDocMetadata doc : mw.getStorage().getDeletedDocList()){
				logger.debug("Delete doc " + doc.getDocId());
			}
			//mw.deleteDocuments(mw.getStorage().getDeletedDocList(), true);
		}
		else if (s == deleteSelected) {
			logger.debug("delete selected - empty the recycle bin");
			for (TrpDocMetadata doc : getSelectedDocuments()){
				logger.debug("Delete doc " + doc.getDocId());
			}
			mw.deleteDocuments(getSelectedDocuments(), true);
		}
		else if (s == retrieveSelected) {
			logger.debug("retrieve selected docs/transcripts");
			for (TrpDocMetadata doc : getSelectedDocuments()){
				logger.debug("Retrieve doc " + doc.getDocId());
			}
			mw.saveDocMetadata(getSelectedDocuments());
			
			/*
			 * 
			 */
		}
		
	}


}
