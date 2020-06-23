package eu.transkribus.swt_gui.credits;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.TrpCreditProduct;
import eu.transkribus.core.model.beans.auth.TrpUser;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.credits.CreditPackageManagerDialog.TrpSliderComposite;
import eu.transkribus.swt_gui.dialogs.FindUserDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.CreditProductsPagedTableWidget;

public class CreateCreditPackageDialog extends Dialog {
	protected Composite dialogArea;
	
	protected CTabFolder tabFolder;
	protected CTabItem productsTableTabItem;
	protected CTabItem newProductFormTabItem;
	
	protected CreditProductsPagedTableWidget productsTable;
	
	protected Composite newProductFormWidget;
	protected Text labelTxt;
	protected TrpSliderComposite nrOfCreditsSldr;
	protected Button shareableChk;
	
	protected Text ownerTxt;
	protected Button ownerBtn;
	protected TrpUser selectedOwner;
	
	//store final outcome here
	private TrpCreditPackage packageToCreate;
	
	public CreateCreditPackageDialog(Shell parent) {
		super(parent);
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayout(new GridLayout(1, false));
		
		tabFolder = new CTabFolder(dialogArea, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		productsTableTabItem = new CTabItem(tabFolder, SWT.NONE);
		
		productsTable = new CreditProductsPagedTableWidget(tabFolder, SWT.NONE);
		productsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		productsTableTabItem.setText("Existing Products");
		productsTableTabItem.setControl(productsTable);
		
		newProductFormTabItem = new CTabItem(tabFolder, SWT.NONE);
		newProductFormWidget = createNewProductFormWidget(tabFolder, SWT.NONE);
		newProductFormTabItem.setText("Create Product");
		newProductFormTabItem.setControl(newProductFormWidget);

		Group pkgPropsGrp = new Group(dialogArea, SWT.BORDER);
		pkgPropsGrp.setText("Credit Package Properties");
		pkgPropsGrp.setLayoutData(new GridData(GridData.FILL_BOTH));
		pkgPropsGrp.setLayout(new GridLayout(3, false));
		
		Label ownerLbl = new Label(pkgPropsGrp, SWT.NONE);
		ownerLbl.setText("Owner");
		ownerTxt = new Text(pkgPropsGrp, SWT.BORDER | SWT.READ_ONLY);
		ownerTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		ownerBtn = new Button(pkgPropsGrp, SWT.PUSH);
		ownerBtn.setImage(Images.FIND);
		SWTUtil.onSelectionEvent(ownerBtn, (e) -> {
			FindUserDialog fud = new FindUserDialog(CreateCreditPackageDialog.this.getShell());
			if(fud.open() == IDialogConstants.OK_ID) {
				List<TrpUser> selection = fud.getSelectedUsers();
				if(!CollectionUtils.isEmpty(selection)) {
					setOwner(selection.get(0));
				}
			}
		});
		
		
		tabFolder.setSelection(productsTableTabItem);
		//init owner field with logged-in user
		setOwner(Storage.getInstance().getUser());
		
		dialogArea.pack();
		//init both tabs and not only the visible one. 
		//not resetting the tables to first page initially will lead to messed up pagination display.
		updateProductsTable(true);
		
		return dialogArea;
	}
	
	private void setOwner(TrpUser trpUser) {
		selectedOwner = trpUser;
		ownerTxt.setText(trpUser.getUserName());		
	}

	private Composite createNewProductFormWidget(Composite parent, int style) {
		Composite c = new Composite(parent, style);
		c.setLayoutData(new GridData(GridData.FILL_BOTH));
		c.setLayout(new GridLayout(2, false));
		Label labelLbl = new Label(c, SWT.NONE);
		labelLbl.setText("Label:");
		labelTxt = new Text(c, SWT.BORDER);
		labelTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label nrOfCreditsLbl = new Label(c, SWT.NONE);
		nrOfCreditsLbl.setText("Nr. of Credits:");
		nrOfCreditsSldr = new TrpSliderComposite(c, SWT.NONE);
		nrOfCreditsSldr.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nrOfCreditsSldr.setValue(100.0);
		nrOfCreditsSldr.setMaximum(10000.0);
		
		shareableChk = new Button(c, SWT.CHECK);
		shareableChk.setText("Shareable");
		
		return c;
	}
	
	@Override
	protected void okPressed() {
		TrpCreditProduct product = null;
		if(tabFolder.getSelection().equals(productsTableTabItem)) {
			product = productsTable.getFirstSelected();
			if(product == null) {
				DialogUtil.showErrorMessageBox(this.getShell(), "Incomplete data", "No product is selected.");
				return;
			}			
		} else {
			String errorMsg = "";
			String label = labelTxt.getText();
			if(label == null || label.length() < 5) {
				errorMsg += "Label is too short\n";
			}
			double value = nrOfCreditsSldr.getValue();
			if(value <= 0.0) {
				errorMsg += "Nr of credits must be positive\n";
			}
			boolean shareable = shareableChk.getSelection();
						
			if(!StringUtils.isEmpty(errorMsg)) {
				DialogUtil.showErrorMessageBox(this.getShell(), "Incomplete data", errorMsg);
				return;
			}
			
			product = new TrpCreditProduct();
			product.setLabel(label);
			//FIXME value must be an int!
			product.setNrOfCredits(new Double(value).intValue());
			product.setShareable(shareable);
		}
		packageToCreate = new TrpCreditPackage();
		packageToCreate.setUserId(selectedOwner.getUserId());
		packageToCreate.setUserName(selectedOwner.getUserName());
		packageToCreate.setProduct(product);
		super.okPressed();
	}
	
	protected TrpCreditPackage getPackageToCreate() {
		return packageToCreate;
	}
	
	/**
	 * Refreshes the tables in the visible tab.
	 * 
	 * @param resetTablesToFirstPage
	 */
	protected void updateUI(boolean resetTablesToFirstPage) {
		CTabItem selection = tabFolder.getSelection();
		if(selection.equals(productsTableTabItem)) {
			updateProductsTable(resetTablesToFirstPage);
		} else {
			//clear form fields?
		}
	}
	
	protected void updateProductsTable(boolean resetTablesToFirstPage) {
		productsTable.refreshPage(resetTablesToFirstPage);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Credit Manager");
		newShell.setMinimumSize(640, 768);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(640, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
}
