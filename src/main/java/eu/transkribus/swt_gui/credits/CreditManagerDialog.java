package eu.transkribus.swt_gui.credits;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.pagination_tables.CreditPackagesCollectionPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.CreditPackagesUserPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.CreditTransactionsPagedTableWidget;
import eu.transkribus.swt_gui.pagination_tables.JobTableWidgetPagination;

public class CreditManagerDialog extends Dialog implements IStorageListener {
	private static final Logger logger = LoggerFactory.getLogger(CreditManagerDialog.class);

	private CTabFolder tabFolder;
	private CTabItem collectionTabItem;
	private CTabItem jobTabItem;
	
	private Composite collectionCreditWidget, jobTransactionWidget;
	
	private CreditPackagesUserPagedTableWidget userCreditsTable;
	private CreditPackagesCollectionPagedTableWidget collectionCreditsTable;
	private JobTableWidgetPagination jobsTable;
	private CreditTransactionsPagedTableWidget transactionsTable;

	public CreditManagerDialog(Shell parent) {
		super(parent);
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		tabFolder = new CTabFolder(cont, SWT.BORDER | SWT.FLAT);
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
		cont.pack();
		SWTUtil.onSelectionEvent(tabFolder, (e) -> { updateUI(); } );
		SWTUtil.setTabFolderBoldOnItemSelection(tabFolder);
		updateUI();
		
		Storage.getInstance().addListener(this);
		cont.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				Storage.getInstance().removeListener(CreditManagerDialog.this);
			}
		});
		
		return cont;
	}

	private Composite createCollectionCreditWidget(Composite parent, int style) {
		SashForm sf = new SashForm(parent, SWT.HORIZONTAL | style);
		sf.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		sf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group userCreditGroup = new Group(sf, SWT.BORDER);
		userCreditGroup.setLayout(new GridLayout(1, true));
		userCreditGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		userCreditGroup.setText("My Credit Packages");
		userCreditsTable = new CreditPackagesUserPagedTableWidget(userCreditGroup, SWT.NONE);
		userCreditsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Group collectionCreditGroup = new Group(sf, SWT.BORDER);
		collectionCreditGroup.setLayout(new GridLayout(1, true));
		collectionCreditGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		collectionCreditGroup.setText("Credit Packages in Collection");
		collectionCreditsTable = new CreditPackagesCollectionPagedTableWidget(collectionCreditGroup, SWT.NONE);
		collectionCreditsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		sf.setWeights(new int[] { 50, 50 });
		return sf;
	}

	private Composite createJobTransactionWidget(Composite parent, int style) {
		SashForm sf = new SashForm(parent, SWT.HORIZONTAL | style);
		sf.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
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
		
		jobsTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
					@Override
					public void run() {
						List<TrpJobStatus> jobs = jobsTable.getSelected();
						if(CollectionUtils.isEmpty(jobs)) {
							logger.debug("No job selected");
							return;
						}
						transactionsTable.setJobId(jobs.get(0).getJobIdAsInt());
					}
				});
			}
		});
		
		return sf;
	}
	
	private void updateUI() {
		//refresh tables in selected tab
		CTabItem selection = tabFolder.getSelection();
		if(selection.equals(collectionTabItem)) {
			userCreditsTable.refreshPage(false);
			collectionCreditsTable.refreshPage(false);
		} else {
			//TODO refresh transaction view
		}
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
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	@Override
	public void handleDocListLoadEvent(DocListLoadEvent e) {
		if(e.isCollectionChange) {
			updateUI();
		}
	}
}
