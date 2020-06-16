package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
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
import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.rest.TrpCreditPackageList;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Page loader will retrieve credit packages
 *
 */
public class CreditPackagesUserPagedTableWidget extends ATableWidgetPagination<TrpCreditPackage> {
	private static final Logger logger = LoggerFactory.getLogger(CreditPackagesUserPagedTableWidget.class);

	// TODO show username additionally?
	public static final String PACKAGE_USER_ID_COL = "Owner ID";
	public static final String PACKAGE_NAME_COL = "Name";
	public static final String PACKAGE_BALANCE_COL = "Balance";
	public static final String PACKAGE_TYPE_COL = "Type";
	public static final String PACKAGE_DATE_COL = "Created";
	public static final String PACKAGE_ID_COL = "ID";

	// filter:
	Composite filterAndReloadComp;

	public CreditPackagesUserPagedTableWidget(Composite parent, int style) {
		super(parent, style, 25);
		this.setLayout(new GridLayout(1, false));
		addFilter();
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

	public TrpCreditPackage getSelectedPackage() {
		return getFirstSelected();
	}

	public void setSelection(int packageId) {
		// TODO
	}

	
	private int packageId;

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

	@Override
	protected void setPageLoader() {
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
				return new TrpCreditPackageList(new ArrayList<>(), 0, 0, 0, null, null);
			}
		};
		final IPageLoader<PageResult<TrpCreditPackage>> pl = new RemotePageLoaderSingleRequest<>(
				pageableTable.getController(), plm);
		pageableTable.setPageLoader(pl);
	}
}