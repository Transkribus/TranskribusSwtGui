package eu.transkribus.swt_gui.credits;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.pagination_tables.CreditPackagesCollectionPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.CreditPackagesUserPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.CreditTransactionsPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.JobTableWidgetPagination;

public class CreditManagerDialog extends Dialog {
	
	TrpCollection collection;
	
	protected Composite dialogArea;
	
	protected CTabFolder tabFolder;
	protected CTabItem collectionTabItem;
	protected CTabItem jobTabItem;
	
	private Composite collectionCreditWidget, jobTransactionWidget;
	
	protected CreditPackagesUserPagedTableWidget userCreditsTable;
	protected Group collectionCreditGroup;
	protected CreditPackagesCollectionPagedTableWidget collectionCreditsTable;
	protected JobTableWidgetPagination jobsTable;
	protected CreditTransactionsPagedTableWidget transactionsTable;
	
	protected MenuItem splitUserPackageItem, showUserPackageDetailsItem;
	
	protected Button addToCollectionBtn, removeFromCollectionBtn;

	public CreditManagerDialog(Shell parent, TrpCollection collection) {
		super(parent);
		this.collection = collection;
	}
	
	/**
	 * Dialog is now modal. Update on collection change to be tested yet.
	 */
	private void setCollection(TrpCollection collection) {
		this.collection = collection;
		updateCreditsTabUI(true);
	}

	public TrpCollection getCollection() {
		return collection;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);

		tabFolder = new CTabFolder(dialogArea, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		collectionTabItem = new CTabItem(tabFolder, SWT.NONE);
		collectionCreditWidget = createCollectionCreditWidget(tabFolder, SWT.NONE);
		collectionTabItem.setText("Credits");
		collectionTabItem.setControl(collectionCreditWidget);
		
		jobTabItem = new CTabItem(tabFolder, SWT.NONE);
		jobTransactionWidget = createJobTransactionWidget(tabFolder, SWT.NONE);
		jobTabItem.setText("Transactions");
		jobTabItem.setControl(jobTransactionWidget);

		tabFolder.setSelection(collectionTabItem);		
		dialogArea.pack();
		//init both tabs and not only the visible one. 
		//not resetting the tables to first page initially will lead to messed up pagination display.
		updateCreditsTabUI(true);
		updateJobsTabUI(true);
		new CreditManagerListener(this);
		
		return dialogArea;
	}

	private Composite createCollectionCreditWidget(Composite parent, int style) {
		SashForm sf = new SashForm(parent, SWT.HORIZONTAL | style);
		sf.setLayout(SWTUtil.createGridLayout(3, false, 0, 0));
		sf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group userCreditGroup = new Group(sf, SWT.BORDER);
		userCreditGroup.setLayout(new GridLayout(1, true));
		userCreditGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		userCreditGroup.setText("My Credit Packages");
		userCreditsTable = new CreditPackagesUserPagedTableWidget(userCreditGroup, SWT.NONE);
		userCreditsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite buttonComp = new Composite(sf, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		buttonComp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label space = new Label(buttonComp, SWT.NONE);
		space.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		
		addToCollectionBtn = new Button(buttonComp, SWT.PUSH);
		addToCollectionBtn.setImage(Images.ARROW_RIGHT);
//		addToCollectionBtn.setText("Assign");
		addToCollectionBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeFromCollectionBtn = new Button(buttonComp, SWT.PUSH);
		removeFromCollectionBtn.setImage(Images.ARROW_LEFT);
//		removeFromCollectionBtn.setText("Remove");
		removeFromCollectionBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label space2 = new Label(buttonComp, SWT.NONE);
		space2.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
		
		collectionCreditGroup = new Group(sf, SWT.BORDER);
		collectionCreditGroup.setLayout(new GridLayout(1, true));
		collectionCreditGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		//group's title text is updated when data is loaded
		collectionCreditsTable = new CreditPackagesCollectionPagedTableWidget(collectionCreditGroup, SWT.NONE);
		collectionCreditsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//TODO add menu to collectionCreditsTable too with same listeners
		Menu menu = new Menu(userCreditsTable.getTableViewer().getTable());
		userCreditsTable.getTableViewer().getTable().setMenu(menu);

		showUserPackageDetailsItem = new MenuItem(menu, SWT.NONE);
		showUserPackageDetailsItem.setText("Show details...");
		splitUserPackageItem = new MenuItem(menu, SWT.NONE);
		splitUserPackageItem.setText("Split package...");
		
		final int buttonWeight = 6;
		sf.setWeights(new int[] { 47, buttonWeight, 47 });

		return sf;
	}

	private void updateCollectionCreditGroupText(TrpCollection collection) {
		String text = "Credit Packages in Collection";
		if(collection != null) {
			text += " '" + collection.getColName() + "'";
		}
		collectionCreditGroup.setText(text);
	}

	private Composite createJobTransactionWidget(Composite parent, int style) {
		SashForm sf = new SashForm(parent, SWT.HORIZONTAL | style);
		sf.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		sf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group jobsGroup = new Group(sf, SWT.BORDER);
		jobsGroup.setLayout(new GridLayout(1, true));
		jobsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobsGroup.setText("Jobs");
		jobsTable = new JobTableWidgetPagination(jobsGroup, SWT.NONE, 20);
		jobsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group jobTransactionGroup = new Group(sf, SWT.BORDER);
		jobTransactionGroup.setLayout(new GridLayout(1, true));
		jobTransactionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		jobTransactionGroup.setText("Transactions of Job");
		transactionsTable = new CreditTransactionsPagedTableWidget(jobTransactionGroup, SWT.NONE);
		transactionsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		sf.setWeights(new int[] { 50, 50 });
		
		return sf;
	}
	
	/**
	 * Refreshes the tables in the visible tab.
	 * 
	 * @param resetTablesToFirstPage
	 */
	protected void updateUI(boolean resetTablesToFirstPage) {
		CTabItem selection = tabFolder.getSelection();
		if(selection.equals(collectionTabItem)) {
			updateCreditsTabUI(resetTablesToFirstPage);
		} else {
			updateJobsTabUI(resetTablesToFirstPage);
		}
	}
	
	protected void updateCreditsTabUI(boolean resetTablesToFirstPage) {
		updateCollectionCreditGroupText(this.getCollection());
		collectionCreditsTable.setCollection(this.getCollection());
		userCreditsTable.refreshPage(resetTablesToFirstPage);
		collectionCreditsTable.refreshPage(resetTablesToFirstPage);
	}

	protected void updateJobsTabUI(boolean resetTablesToFirstPage) {
		jobsTable.refreshPage(resetTablesToFirstPage);
		transactionsTable.refreshPage(resetTablesToFirstPage);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Credit Manager");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.APPLICATION_MODAL | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	}
}
