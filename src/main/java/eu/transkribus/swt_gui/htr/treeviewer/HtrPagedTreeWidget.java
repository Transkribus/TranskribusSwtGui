package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.Columns;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.nebula.widgets.pagination.table.SortTableColumnSelectionListener;
import org.eclipse.nebula.widgets.pagination.tree.SortTreeColumnSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.GroundTruthSelectionDescriptor;
import eu.transkribus.core.model.beans.ReleaseLevel;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.job.JobError;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.ATreeWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.htr.HtrFilterWithProviderWidget;
import eu.transkribus.swt_gui.htr.HtrPagedTableWidget;
import eu.transkribus.swt_gui.htr.HtrTableLabelProvider;
import eu.transkribus.swt_gui.htr.HtrTreeLabelProvider;
import eu.transkribus.swt_gui.htr.ShareHtrDialog;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.mainwidget.storage.StorageUtil;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

public class HtrPagedTreeWidget extends ATreeWidgetPagination<TrpHtr> {
	private static final Logger logger = LoggerFactory.getLogger(HtrPagedTreeWidget.class);
	
	public final static String[] providerValues = { HtrCITlabUtils.PROVIDER_CITLAB, HtrCITlabUtils.PROVIDER_CITLAB_PLUS, HtrPyLaiaUtils.PROVIDER_PYLAIA };	
	
	public final static ColConfig NAME_COL = new ColConfig("Name", 210, "name");
	public final static ColConfig SIZE_COL = new ColConfig("Size", 100, "size");
	public final static ColConfig CURATOR_COL = new ColConfig("Curator", 120, "userName");
	public final static ColConfig DATE_COL = new ColConfig("Date", 80, "created");
	public final static ColConfig ID_COL = new ColConfig("HTR ID",50, "htrId");
	public final static ColConfig WORD_COL = new ColConfig("nrOfWords", 80, "NR_OF_WORDS");

	public final static ColConfig[] COLUMNS = new ColConfig[] { NAME_COL, SIZE_COL, CURATOR_COL, ID_COL, WORD_COL, DATE_COL };
	
	// filter:
	Composite filterAndReloadComp;
	HtrFilterWithProviderWidget filterComposite;
	Button reloadBtn;
	private final String providerFilter;
	
	Menu contextMenu;
	
	public HtrPagedTreeWidget(Composite parent, int style, String providerFilter, ITreeContentProvider contentProvider, CellLabelProvider labelProvider) {
		super(parent, style, 40, contentProvider, labelProvider);
		
		if(providerFilter != null && !Arrays.stream(providerValues).anyMatch(s -> s.equals(providerFilter))) {
			throw new IllegalArgumentException("Invalid providerFilter value");
		}
		
		createColumns();
		
		this.providerFilter = providerFilter;
		this.setLayout(new GridLayout(1, false));

		contextMenu = new Menu(tv.getTree());
		tv.getTree().setMenu(contextMenu);
		
		addFilter();
		
		Listener filterModifyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				loadFirstPage();
			}
		};
		this.addListener(SWT.Modify, filterModifyListener);

		initListener();
	}
	
	
	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		filterComposite.addListener(eventType, listener);
	}
	
	private void addFilter() {
		filterAndReloadComp = new Composite(this, SWT.NONE);
		filterAndReloadComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterAndReloadComp.setLayout(new GridLayout(2, false));
		filterAndReloadComp.moveAbove(null);
		
		filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTreeViewer(), providerFilter, SWT.NONE) {
			@Override
			protected void refreshViewer() {
				logger.debug("refreshing viewer...");
				refreshPage(true);
			}
			@Override
			protected void attachFilter() {
			}
		};	
		
		this.reloadBtn = new Button(filterAndReloadComp, SWT.PUSH);
		reloadBtn.setToolTipText("Reload current tree page");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		filterAndReloadComp.moveAbove(getTreeViewer().getTree());
	}
	
	void resetProviderFilter() {
		filterComposite.resetProviderFilter();
	}

	public Button getReloadButton() {
		return reloadBtn;
	}
	
	public String getProviderComboValue() {
		Combo providerCombo = filterComposite.getProviderCombo();
		return (String) providerCombo.getData(providerCombo.getText());
	}
	
	public TrpHtr getSelectedHtr() {
		return getFirstSelected();
//		IStructuredSelection sel = (IStructuredSelection) htrTv.getSelection();
//		if (sel.getFirstElement() != null && sel.getFirstElement() instanceof TrpHtr) {
//			return (TrpHtr) sel.getFirstElement();
//		} else
//			return null;

	}

	public void refreshList(List<TrpHtr> htrs) {
		// TODO: htrs are reloaded using the IPageLoadMethod created in setPageLoader method
		// --> no need to set them here
		
		logger.debug("refreshList");
		refreshPage(true);
	}
	
	public void loadFirstPage() {
		logger.debug("load first page of htr paged tree");
		refreshPage(true);
	}

	public void setSelection(int htrId) {
		// TODO
		
//		List<TrpHtr> htrs = (List<TrpHtr>)htrTv.getInput();
//		TrpHtr htr = null;
//		for(int i = 0; i < htrs.size(); i++){
//			final TrpHtr curr = htrs.get(i);
//			if(curr.getHtrId() == htrId){
//				logger.trace("Found htrId {}", htrId);
//				htr = curr;
//				break;
//			}
//		}
//		logger.trace("Selecting HTR in table viewer: {}", htr);
//		if(htr != null) { //if model has been removed from this collection it is not in the list.
//			htrTv.setSelection(new StructuredSelection(htr), true);
//		} else {
//			htrTv.setSelection(null);
//		}
	}

	@Override
	protected void setPageLoader() {
		IPageLoadMethod<TrpHtrList, TrpHtr> plm = new IPageLoadMethod<TrpHtrList, TrpHtr>() {
			Storage store = Storage.getInstance();
			TrpHtrList l;
			
			private void load(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {
				if (store != null && store.isLoggedIn()) {
					try {
						Integer collId = store.getCollId();
						String htrRelease = filterComposite.getLinkageFilterComboText();
						Integer releaseLevel = htrRelease.contains("All")? null : htrRelease.contains("Public")? -1 : 0;
						if (store.isAdminLoggedIn() && releaseLevel == null){
							collId = null;
						}
					
						logger.debug("load HTRs from DB with filter: " + filterComposite.getFilterText().getText());
						logger.debug("providerFilter: " + getProviderComboValue());
						logger.debug("linkage filter: " + filterComposite.getLinkageFilterComboText());
						logger.debug("htr release is : " + releaseLevel);
						
						l = store.getConnection().getHtrsSync(collId, getProviderComboValue(), filterComposite.getFilterText().getText(), releaseLevel, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						if (l.getList()== null){
							logger.debug("the result list is null - no htr match the search string");
							//if we set not this the old entries persist in the table!!
							l = new TrpHtrList(new ArrayList<>(), 0, 0, 0, null, null);
						}
						
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
					}
				}
				else {
					l = new TrpHtrList(new ArrayList<>(), 0, 0, 0, null, null);
				}
			}		
			
			@Override
			public TrpHtrList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				//pageableTable.refreshPage();
				load(fromIndex, toIndex, sortPropertyName, sortDirection);
				//applyFilter();
				return l;
			}
		};
		final IPageLoader<PageResult<TrpHtr>> pl = new RemotePageLoaderSingleRequest<>(pageableTree.getController(), plm);
		pageableTree.setPageLoader(pl);	
		

	}

	@Override
	protected void createColumns() {
		
		for (ColConfig cf : COLUMNS) {
			TreeViewerColumn column = new TreeViewerColumn(tv, SWT.MULTI);
			column.getColumn().setText(cf.name);
			column.getColumn().setWidth(cf.colSize);
			column.getColumn().addSelectionListener(new SortTreeColumnSelectionListener(cf.dbName));
			column.setLabelProvider(labelProvider);
		}
		
	}	
	
	void initListener() {
		tv.getTree().addMenuDetectListener(new MenuDetectListener() {
			
			@Override
			public void menuDetected(MenuDetectEvent event) {
				if (tv.getTree().getSelectionCount() != 1) {
					event.doit = false;
					return;
				}
				//clear all options
				for(MenuItem item : contextMenu.getItems()) {
					item.dispose();
				}
				
				TreeItem selection = tv.getTree().getSelection()[0];
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
		TreeItem selection = tv.getTree().getSelection()[0];
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
	
	public void expandTreeItem(Object o) {
		final ITreeContentProvider provider = (ITreeContentProvider) tv.getContentProvider();
		if(!provider.hasChildren(o)) {
			return;
		}
		if (tv.getExpandedState(o)) {
			tv.collapseToLevel(o, AbstractTreeViewer.ALL_LEVELS);
		} else {
			tv.expandToLevel(o, 1);
		}
	}

}