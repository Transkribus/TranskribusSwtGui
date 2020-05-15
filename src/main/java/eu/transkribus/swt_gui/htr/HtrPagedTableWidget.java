package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.job.JobError;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrPagedTableWidget extends ATableWidgetPagination<TrpHtr> {
	private static final Logger logger = LoggerFactory.getLogger(HtrTableWidget.class);
	
	public final static String[] providerValues = { HtrCITlabUtils.PROVIDER_CITLAB, HtrCITlabUtils.PROVIDER_CITLAB_PLUS, HtrPyLaiaUtils.PROVIDER_PYLAIA };	
	
	public static final String HTR_NAME_COL = "Name";
	public static final String HTR_LANG_COL = "Language";
	public static final String HTR_CREATOR_COL = "Curator";
	public static final String HTR_TECH_COL = "Technology";
	public static final String HTR_DATE_COL = "Created";
	public static final String HTR_ID_COL = "ID";
	
	// filter:
	Composite filterAndReloadComp;
	HtrFilterWithProviderWidget filterComposite;
	Button reloadBtn;
	private final String providerFilter;
	
	public HtrPagedTableWidget(Composite parent, int style, String providerFilter) {
		super(parent, style, 30);
		
		if(providerFilter != null && !Arrays.stream(providerValues).anyMatch(s -> s.equals(providerFilter))) {
			throw new IllegalArgumentException("Invalid providerFilter value");
		}
		
		this.providerFilter = providerFilter;
		this.setLayout(new GridLayout(1, false));
		
		addFilter();
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
		
		filterComposite = new HtrFilterWithProviderWidget(filterAndReloadComp, getTableViewer(), providerFilter, SWT.NONE) {
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
		reloadBtn.setToolTipText("Reload current page");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true));
		filterAndReloadComp.moveAbove(getTableViewer().getTable());
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
				if (store.isLoggedIn()) {
					try {
						l = store.getConnection().getHtrsSync(store.getCollId(), providerFilter, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
					}
				}
				else {
					l = new TrpHtrList(new ArrayList<>(), 0, 0, 0, null, null);
				}
			}			
			
			private void applyFilter() {
				logger.debug("in filter function");
				if (filterComposite!=null && l!=null && l.getList()!=null) {
					logger.debug("filtering htrs..., N-before = "+l.getList().size());
					l.getList().removeIf(htr -> !filterComposite.getViewerFilter().select(getTableViewer(), null, htr));
					l.setTotal(l.getList().size());
					logger.debug("filtering htrs..., N-after = "+l.getList().size());
				}
			}
			
			@Override
			public TrpHtrList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				load(fromIndex, toIndex, sortPropertyName, sortDirection);
				applyFilter();
				return l;
			}
		};
		final IPageLoader<PageResult<TrpHtr>> pl = new RemotePageLoaderSingleRequest<>(pageableTable.getController(), plm);
		pageableTable.setPageLoader(pl);		
	}

	@Override
	protected void createColumns() {
		HtrTableLabelProvider lp = new HtrTableLabelProvider(tv);
		createDefaultColumn(HTR_NAME_COL, 220, "name", true);
		createDefaultColumn(HTR_LANG_COL, 100, "language", true);
		
		createColumn(HTR_CREATOR_COL, 120, "userName", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_CREATOR_COL));	
				}
			}
		});
		createColumn(HTR_TECH_COL, 100, "provider", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_TECH_COL));	
				}
			}
		});
		createColumn(HTR_DATE_COL, 70, "created", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpHtr) {
					cell.setText(lp.getColumnText((TrpHtr)cell.getElement(), HTR_DATE_COL));
				}
			}
		});			
		createDefaultColumn(HTR_ID_COL, 50, "htrId", true);
	}	
}