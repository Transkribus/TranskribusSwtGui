package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCreditProduct;
import eu.transkribus.core.model.beans.rest.TrpCreditProductList;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Page loader will retrieve credit packages
 *
 */
public class CreditProductsPagedTableWidget extends ATableWidgetPagination<TrpCreditProduct> {
	
	public static final String PRODUCT_LABEL_COL = "Label";
	public static final String PRODUCT_CREDITS_COL = "Nr. of Credits";
	public static final String PRODUCT_SHAREABLE_COL = "Shareable";
	public static final String PRODUCT_SUBSCRIPTION_COL = "Subscription";
	public static final String PRODUCT_STATUS_COL = "Source";
	public static final String PRODUCT_TYPE_COL = "Type";
	public static final String PRODUCT_ID_COL = "ID";
	
	RemotePageLoaderSingleRequest<TrpCreditProductList, TrpCreditProduct> pageLoader;


	public CreditProductsPagedTableWidget(Composite parent, int style) {
		super(parent, style | SWT.SINGLE, 25);
		this.setLayout(new GridLayout(1, false));
	}

	public void setSelection(int packageId) {
		// TODO
	}
	
	@Override
	protected void createColumns() {
		createDefaultColumn(PRODUCT_LABEL_COL, 220, "label", true);
		createDefaultColumn(PRODUCT_CREDITS_COL, 100, "nrOfCredits", true);
		createDefaultColumn(PRODUCT_SHAREABLE_COL, 50, "shareable", true);
		createDefaultColumn(PRODUCT_TYPE_COL, 75, "creditType", true);
		createDefaultColumn(PRODUCT_SUBSCRIPTION_COL, 70, "subscription", true);
//		createDefaultColumn(PRODUCT_STATUS_COL, 60, "status", true);
		createColumn(PRODUCT_STATUS_COL, 70, "status", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpCreditProduct) {
					final Integer value = ((TrpCreditProduct)cell.getElement()).getStatus();
					String text;
					if(value == null) {
						text = "No set";
					} else {
						//TODO values are now defined in persistence CreditManager. Move them to core?
						switch (value) {
						case 0:
							text = "Admin";
							break;
						case 1:
							text = "Webshop/Zapier";
							break;
						case 2:
							text = "User";
							break;
						default: text = "Unknown value: " + value;
						}
					}
					cell.setText(text);	
				}
			}
		});
		createDefaultColumn(PRODUCT_ID_COL, 50, "productId", true);
	}

	protected RemotePageLoaderSingleRequest<TrpCreditProductList, TrpCreditProduct> createPageLoader() {
		IPageLoadMethod<TrpCreditProductList, TrpCreditProduct> plm = new IPageLoadMethod<TrpCreditProductList, TrpCreditProduct>() {

			@Override
			public TrpCreditProductList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				Storage store = Storage.getInstance();
				if (store.isLoggedIn()) {
					try {
						return store.getConnection().getCreditCalls().getCreditProducts(fromIndex, toIndex - fromIndex,
								sortPropertyName, sortDirection);
					} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading HTRs", e.getMessage(), e);
					}
				}
				return new TrpCreditProductList(new ArrayList<>(), 0, 0, 0, null, null);
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
}