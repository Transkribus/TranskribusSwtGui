package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.GroundTruthSelectionDescriptor;
import eu.transkribus.core.model.beans.ReleaseLevel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.htr.HtrFilterWidget;
import eu.transkribus.swt_gui.htr.ShareHtrDialog;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.mainwidget.storage.StorageUtil;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class GroundTruthTreeWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(GroundTruthTreeWidget.class);

	private TreeViewer treeViewer;
	private final CellLabelProvider labelProvider;
	
	private final ITreeContentProvider treeContentProvider;
	
	private Composite filterWidget;
	private Button reloadBtn;
	
	Menu contextMenu;
	
	//TODO paging
	//ToolItem clearPageItem, deleteSelectedBtn;
	//List<ToolItem> editToolItems;
	
	public final static ColConfig NAME_COL = new ColConfig("Name", 240, "name");
	public final static ColConfig SIZE_COL = new ColConfig("Size", 150, "size");
	public final static ColConfig CURATOR_COL = new ColConfig("Curator", 240, "userName");
	public final static ColConfig ID_COL = new ColConfig("HTR ID", 100, "htrId");

	public final static ColConfig[] COLUMNS = new ColConfig[] { NAME_COL, SIZE_COL, CURATOR_COL, ID_COL };
	
	public GroundTruthTreeWidget(Composite parent, ITreeContentProvider contentProvider, CellLabelProvider labelProvider) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, true));		
		
		this.treeViewer = new TreeViewer(this, SWT.BORDER | SWT.MULTI);
		this.treeViewer.getTree().setHeaderVisible(true);
		
		//providers may be passed as arguments to make this more flexible
		if(contentProvider != null) {
			this.treeContentProvider = contentProvider;
		} else {
			//default contentProvider shows HTR GT for now
			this.treeContentProvider = new HtrGroundTruthContentProvider(null);
		}
		
		if(labelProvider != null) {
			this.labelProvider = labelProvider;
		} else {
			this.labelProvider = new HtrGroundTruthTableLabelAndFontProvider(treeViewer.getControl().getFont());
		}
		
		this.treeViewer.setContentProvider(this.treeContentProvider);
		this.treeViewer.setLabelProvider(this.labelProvider);
		this.treeViewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite filterAndReloadComp = new Composite(this, SWT.NONE);
		filterAndReloadComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterAndReloadComp.setLayout(new GridLayout(2, false));
		
		this.filterWidget = new HtrFilterWidget(filterAndReloadComp, treeViewer, SWT.None);
		this.reloadBtn = new Button(filterAndReloadComp, SWT.PUSH);
		reloadBtn.setToolTipText("Reload current page");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));

		contextMenu = new Menu(treeViewer.getTree());
		treeViewer.getTree().setMenu(contextMenu);
		
		initCols();
		
		initListener();
	}
	
	public GroundTruthTreeWidget(Composite parent) {
		this(parent, null, null);
	}
	
	void initToolBar() {
//		ToolBar toolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
//		toolBar.setBounds(0, 0, 93, 25);
//		editToolItems = new ArrayList<>(2);
//		
//		clearPageItem = new ToolItem(toolBar, 0);
//		clearPageItem.setToolTipText("Clear page content");
//		clearPageItem.setImage(Images.CROSS);
//		editToolItems.add(clearPageItem);
//		
//		deleteSelectedBtn = new ToolItem(toolBar, 0);
//		deleteSelectedBtn.setToolTipText("Delete selected shapes");
//		deleteSelectedBtn.setImage(Images.DELETE);
//		editToolItems.add(deleteSelectedBtn);
	}
	
	void initListener() {
		treeViewer.getTree().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent event) {
				if (treeViewer.getTree().getSelectionCount() != 1) {
					event.doit = false;
					return;
				}
				//clear all options
				for(MenuItem item : contextMenu.getItems()) {
					item.dispose();
				}
				
				TreeItem selection = treeViewer.getTree().getSelection()[0];
				Object selectionData = selection.getData();
				
				logger.debug("Menu detected on tree item of type: {}", selectionData.getClass());
				
				if(selectionData instanceof HtrGtDataSet) {
					HtrGtDataSet gtSet = (HtrGtDataSet) selectionData;
					TrpHtr htr = gtSet.getModel();
					boolean isDataSetAccessible = htr.getCollectionIdLink() != null 
							|| htr.getReleaseLevelValue() >= ReleaseLevel.DisclosedDataSet.getValue();
					
					if(!Storage.getInstance().isAdminLoggedIn() && !isDataSetAccessible) {
						logger.debug("Data set not accessible for this user.");
						event.doit = false;
						return;
					}
					
					if (!StorageUtil.canDuplicate(Storage.getInstance().getCollId())) {
						logger.debug("User not privileged to manage collection.");
						event.doit = false;
						return;
					}
					MenuItem copyGtSetToDocItem = new MenuItem(contextMenu, SWT.NONE);
					copyGtSetToDocItem.setText("Copy data set to new document...");
					copyGtSetToDocItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							startCopyGtSetToDocumentAction();
							super.widgetSelected(e);
						}
					});
				} else if (selectionData instanceof TrpHtr) {
					TrpHtr htr = (TrpHtr) selectionData;
					//open menu and show HTR-related items
					MenuItem showDetailsItem = new MenuItem(contextMenu, SWT.NONE);
					showDetailsItem.setText("Show details...");
					showDetailsItem.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							TrpMainWidget.getInstance().getUi().getServerWidget().showHtrDetailsDialog(htr);
						}
					});
					
					if(Storage.getInstance().isAdminLoggedIn() 
							|| (htr.getCollectionIdLink() != null && htr.getCollectionIdLink() == Storage.getInstance().getCollId())) {
						MenuItem shareModelItem = new MenuItem(contextMenu, SWT.NONE);
						shareModelItem.setText("Share model...");
						shareModelItem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								ShareHtrDialog diag = new ShareHtrDialog(getShell(), htr);
								diag.open();
							}
						});
					}
				} else {
					event.doit = false;
				}
			}
		});
	}
	
	private void startCopyGtSetToDocumentAction() {
		ChooseCollectionDialog ccd = new ChooseCollectionDialog(getShell());
		
		@SuppressWarnings("unused")
		int ret = ccd.open();
		TrpCollection col = ccd.getSelectedCollection();
		
		if(col == null) {
			logger.debug("No collection was selected.");
			return;
		}
		
		//MenuDetectListener determined that this action is fine for the selection. Only HtrGtDataSet is allowed now
		TreeItem selection = treeViewer.getTree().getSelection()[0];
		Object selectionData = selection.getData();
		
		if(selectionData == null) {
			logger.debug("Menu aborted without selection.");
		}
		
		HtrGtDataSet gtSet = (HtrGtDataSet) selectionData;

		final String title = "Copy of HTR " + gtSet.getDataSetType().getLabel() + " '" + gtSet.getModel().getName() + "'";
		
		GroundTruthSelectionDescriptor desc = new GroundTruthSelectionDescriptor(gtSet.getId(), gtSet.getDataSetType().toString());
		
		try {
			TrpMainWidget.getInstance().duplicateGtToDocument(col, desc, title);
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException e1) {
			logger.debug("Could copy dataset to collection!", e1);
			String errorMsg = "The data set could not be copied to this collection.";
			if(!StringUtils.isEmpty(e1.getMessage())) {
				errorMsg += "\n" + e1.getMessage();
			}
			DialogUtil.showErrorMessageBox(getShell(), "Error while copying data set",
					errorMsg);
		}
	}
	
	private void initCols() {
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(treeViewer, SWT.MULTI);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.setLabelProvider(labelProvider);
		}
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	public Button getReloadButton() {
		return reloadBtn;
	}

	public void refreshLabels(Object source) {
		if(source == null) {
			treeViewer.refresh(true);
		} else {
			treeViewer.refresh(source, true);
		}
	}

	public void expandTreeItem(Object o) {
		final ITreeContentProvider provider = (ITreeContentProvider) treeViewer.getContentProvider();
		if(!provider.hasChildren(o)) {
			return;
		}
		if (treeViewer.getExpandedState(o)) {
			treeViewer.collapseToLevel(o, AbstractTreeViewer.ALL_LEVELS);
		} else {
			treeViewer.expandToLevel(o, 1);
		}
	}

	public void setInput(List<TrpHtr> treeViewerInput) {
		treeViewer.setInput(treeViewerInput);
	}
}
