package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.rest.TrpCreditPackageList;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Page loader will retrieve credit packages
 *
 */
public class CreditPackagesUserPagedTableWidget extends ATableWidgetPagination<TrpCreditPackage> {
	
	// TODO show username additionally?
	public static final String PACKAGE_USER_ID_COL = "Owner ID";
	public static final String PACKAGE_NAME_COL = "Name";
	public static final String PACKAGE_BALANCE_COL = "Balance";
	public static final String PACKAGE_TYPE_COL = "Type";
	public static final String PACKAGE_DATE_COL = "Created";
	public static final String PACKAGE_ID_COL = "ID";
	
	RemotePageLoaderSingleRequest<TrpCreditPackageList, TrpCreditPackage> pageLoader;
	
	OverallBalanceComposite overallBalanceComp;

	public CreditPackagesUserPagedTableWidget(Composite parent, int style) {
		super(parent, style, 25);
		this.setLayout(new GridLayout(1, false));
		
		createOverallBalanceComposite(pageableTable);
	}

	private void createOverallBalanceComposite(PageableTable pageableTable) {
		// Create the composite in the bottom right of the table widget
		Composite parent = pageableTable.getCompositeBottom();
		
		//create Label to occupy space in the middle and push other stuff to the right
		Label space = new Label(parent, SWT.NONE);
		space.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
		
		overallBalanceComp = new OverallBalanceComposite(parent, SWT.NONE);
		overallBalanceComp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
		
		//adjust layout of bottom
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns += 2;
		parent.pack();
	}

	public TrpCreditPackage getSelectedPackage() {
		return getFirstSelected();
	}

	public void setSelection(int packageId) {
		// TODO
	}
	
	@Override
	public void refreshPage(boolean resetToFirstPage) {
		//do the refresh
		super.refreshPage(resetToFirstPage);
		//retrieve previous page data from loader for further UI update
		TrpCreditPackageList currentData = pageLoader.getCurrentData();
		Double balance = null;
		if(currentData != null && currentData.getOverallBalance() != null) {
			balance = currentData.getOverallBalance();
		}
		overallBalanceComp.updateBalanceValue(balance);
	}

	@Column(name = "PRODUCT_ID")
	private Integer productId;

	@Column(name = "PURCHASE_DATE")
	private Date purchaseDate;

	@Column(name = "EXPIRATION_DATE")
	private Date expirationDate;

	@Column(name = "PAYMENT_RECEIVED")
	private Date paymentReceived;
	
	@Column(name = "IS_ACTIVE")
	private boolean active;
	
	@Column(name = "USER_ID")
	private int userId;
	
	@Override
	protected void createColumns() {
		createColumn(PACKAGE_NAME_COL, 220, "label", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpCreditPackage) {
					cell.setText(((TrpCreditPackage)cell.getElement()).getProduct().getLabel());	
				}
			}
		});
		createColumn(PACKAGE_TYPE_COL, 100, "creditType", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpCreditPackage) {
					cell.setText(((TrpCreditPackage)cell.getElement()).getProduct().getCreditType());	
				}
			}
		});
		createDefaultColumn(PACKAGE_USER_ID_COL, 50, "userId", true);
		createDefaultColumn(PACKAGE_DATE_COL, 70, "purchaseDate", true);
		createDefaultColumn(PACKAGE_BALANCE_COL, 120, "balance", true);
		createDefaultColumn(PACKAGE_ID_COL, 50, "packageId", true);
	}

	protected RemotePageLoaderSingleRequest<TrpCreditPackageList, TrpCreditPackage> createPageLoader() {
		IPageLoadMethod<TrpCreditPackageList, TrpCreditPackage> plm = new IPageLoadMethod<TrpCreditPackageList, TrpCreditPackage>() {

			@Override
			public TrpCreditPackageList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				Storage store = Storage.getInstance();
				if (store.isLoggedIn()) {
					try {
						return store.getConnection().getCreditCalls().getCreditPackagesByUser(fromIndex, toIndex - fromIndex,
								sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
					}
				}
				return new TrpCreditPackageList(new ArrayList<>(), 0.0d, 0, 0, 0, null, null);
			}
		};
		return new RemotePageLoaderSingleRequest<>(pageableTable.getController(), plm);
	}
	
	@Override
	protected void setPageLoader() {
		//hold reference to page loader for later access
		pageLoader = createPageLoader();
		pageableTable.setPageLoader(pageLoader);
	}
	
	protected static class OverallBalanceComposite extends Composite {
		final Label overallBalanceLbl;
		final Text overallBalanceValueTxt;
		
		public OverallBalanceComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new GridLayout(2, false));
			
			overallBalanceLbl = new Label(this, SWT.NONE);
			overallBalanceLbl.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
			overallBalanceLbl.setText("Overall Credits:");
			Fonts.setBoldFont(overallBalanceLbl);
			
			overallBalanceValueTxt = new Text(this, SWT.BORDER | SWT.READ_ONLY);
			overallBalanceValueTxt.setLayoutData(new GridData(GridData.END, SWT.CENTER, true, false));
			
			updateBalanceValue(null);
		}
		
		public void updateBalanceValue(Double balance) {
			String txt = "N/A";
			if(balance != null) {
				txt = "" + balance;
			}

			overallBalanceValueTxt.setText(txt);
			overallBalanceValueTxt.pack();
			this.pack();
			this.getParent().pack();
		}
	}
}