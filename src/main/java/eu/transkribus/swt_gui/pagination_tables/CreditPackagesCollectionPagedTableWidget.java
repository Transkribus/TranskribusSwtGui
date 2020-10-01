package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.pagination.table.PageableTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.rest.TrpCreditPackageList;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.CreditPackagesUserPagedTableWidget.OverallBalanceComposite;

public class CreditPackagesCollectionPagedTableWidget extends CreditPackagesUserPagedTableWidget {
	private static final Logger logger = LoggerFactory.getLogger(CreditPackagesCollectionPagedTableWidget.class);
	
	public CreditPackagesCollectionPagedTableWidget(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected RemotePageLoaderSingleRequest<TrpCreditPackageList, TrpCreditPackage> createPageLoader() {
		IPageLoadMethod<TrpCreditPackageList, TrpCreditPackage> plm = new IPageLoadMethod<TrpCreditPackageList, TrpCreditPackage>() {

			@Override
			public TrpCreditPackageList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				Storage store = Storage.getInstance();
				if (store.isLoggedIn()) {
					try {
						return store.getConnection().getCreditCalls().getCreditPackagesByCollection(store.getCollId(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
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
	protected void createColumns() {
		createColumn(PACKAGE_NAME_COL, 220, "label", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpCreditPackage) {
					cell.setText(((TrpCreditPackage)cell.getElement()).getProduct().getLabel());	
				}
			}
		});
		createDefaultColumn(PACKAGE_BALANCE_COL, 80, "balance", true);
		createDefaultColumn(PACKAGE_USER_NAME_COL, 120, "userName", true);
		//for now we don't need the userid
//		createDefaultColumn(PACKAGE_USER_ID_COL, 50, "userId", true);
		createColumn(PACKAGE_SHAREABLE_COL, 70, "shareable", new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				if (cell.getElement() instanceof TrpCreditPackage) {
					cell.setText(((TrpCreditPackage)cell.getElement()).getProduct().getShareable() + "");	
				}
			}
		});
//		createDefaultColumn(PACKAGE_DATE_COL, 120, "purchaseDate", true);
		//hide credit type as the value is currently not used anyway
//		createColumn(PACKAGE_TYPE_COL, 100, "creditType", new CellLabelProvider() {
//			@Override
//			public void update(ViewerCell cell) {
//				if (cell.getElement() instanceof TrpCreditPackage) {
//					cell.setText(((TrpCreditPackage)cell.getElement()).getProduct().getCreditType());	
//				}
//			}
//		});
		createDefaultColumn(PACKAGE_ID_COL, 50, "packageId", true);
	}
	
	protected void createOverallBalanceComposite(PageableTable pageableTable) {
		// Create the composite in the bottom right of the table widget
		Composite parent = pageableTable.getCompositeBottom();
		int layoutColsIncrement = 2;
		
		//create Label to occupy space in the middle and push other stuff to the right
		Label space = new Label(parent, SWT.NONE);
		space.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true));
		
		overallBalanceComp = new OverallBalanceComposite(parent, SWT.NONE);
		overallBalanceComp.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
		
		//adjust layout of bottom
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.numColumns += layoutColsIncrement;
		parent.pack();
	}
}