package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;

import javax.ws.rs.ServerErrorException;

import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCreditTransaction;
import eu.transkribus.core.model.beans.rest.TrpCreditTransactionList;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CreditTransactionsPagedTableWidget extends ATableWidgetPagination<TrpCreditTransaction> {
	private static final Logger logger = LoggerFactory.getLogger(CreditTransactionsPagedTableWidget.class);
		
	//TODO show username additionally?
	public static final String TA_DESC_COL = "Description";
	public static final String TA_VALUE_COL = "Value";
	public static final String TA_COST_COL = "Cost Factor";
	public static final String TA_BALANCE_COL = "Balance";	
	public static final String TA_DATE_COL = "Time";
	public static final String TA_PACKAGE_ID_COL = "ID";

	private Integer jobId;
	// filter:
	Composite filterAndReloadComp;
	
	public CreditTransactionsPagedTableWidget(Composite parent, int style) {
		super(parent, style, 25);
		this.setLayout(new GridLayout(1, false));
		addFilter();
		jobId = null;
	}
	
	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
	}
	
	private void addFilter() {
		filterAndReloadComp = new Composite(this, SWT.NONE);
		filterAndReloadComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		filterAndReloadComp.setLayout(new GridLayout(2, false));
		filterAndReloadComp.moveAbove(null);
	}
	
	public TrpCreditTransaction getSelectedPackage() {
		return getFirstSelected();
	}
	
	public void setJobId(Integer jobId) {
		this.jobId = jobId;
		this.refreshPage(true);
	}

	public void setSelection(int packageId) {
		// TODO
	}

	@Override
	protected void setPageLoader() {
		IPageLoadMethod<TrpCreditTransactionList, TrpCreditTransaction> plm = new IPageLoadMethod<TrpCreditTransactionList, TrpCreditTransaction>() {

			@Override
			public TrpCreditTransactionList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				Storage store = Storage.getInstance();
				TrpCreditTransactionList l = new TrpCreditTransactionList(new ArrayList<>(), 0, 0, 0, null, null);
				if(jobId == null) {
					logger.debug("No jobId set => not loading transactions.");
					return l;
				}
				if(!store.isLoggedIn()) {
					logger.debug("Not logged in.");
					return l;
				}
				try {
					l = store.getConnection().credits().getTransactionsByJob(jobId, fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
				} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
					TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
				}
				return l;
			}
		};
		final IPageLoader<PageResult<TrpCreditTransaction>> pl = new RemotePageLoaderSingleRequest<>(pageableTable.getController(), plm);
		pageableTable.setPageLoader(pl);		
	}

	@Override
	protected void createColumns() {
		createDefaultColumn(TA_DESC_COL, 220, "description", true);
		createDefaultColumn(TA_VALUE_COL, 50, "creditValue", true);
		createDefaultColumn(TA_COST_COL, 50, "costFactor", true);
		createDefaultColumn(TA_DATE_COL, 70, "time", true);
		createDefaultColumn(TA_BALANCE_COL, 50, "creditBalance", true);
		createDefaultColumn(TA_PACKAGE_ID_COL, 50, "packageId", true);
	}	
}