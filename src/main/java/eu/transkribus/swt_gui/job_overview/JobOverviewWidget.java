package eu.transkribus.swt_gui.job_overview;

import java.util.ArrayList;
import java.util.List;

import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.mytableviewer.MyTableViewer;
import org.dea.swt.util.DefaultTableColumnViewerSorter;
import org.dea.swt.util.Images;
import org.dea.swt.util.TableViewerSorter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;

public class JobOverviewWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(JobOverviewWidget.class);
	
	public class DateViewerSorter extends TableViewerSorter {
		public DateViewerSorter(TableViewer viewer) {
			super(viewer);
		}

		@Override protected int doCompare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof TrpTranscriptMetadata && e2 instanceof TrpTranscriptMetadata) {
				return ((TrpTranscriptMetadata)e1).getTime().compareTo(((TrpTranscriptMetadata)e2).getTime());
			}
			
			return 0;
		}
	}
	
//	TableViewer jobTableViewer;
	MyTableViewer jobTableViewer;
	Table jobTable;
	JobOverviewLabelProvider labelProvider;
		
	public static final String ID_COL = "ID";
	public static final String TYPE_COL = "Type";
	public static final String STATE_COL = "State";
	public static final String CREATION_COL = "Created";
	public static final String DOC_ID_COL = "Doc-Id";
	public static final String PAGE_COL = "Page";
	public static final String DESCRIPTION_COL = "Description";
	public static final String USER_NAME_COL = "Username";
	
	// This are the columns, sorted in their order of appearence in the table:
	public final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(TYPE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(STATE_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CREATION_COL, 120, true, TableViewerSorter.DESC, SWT.LEFT, new TableViewerSorter(jobTableViewer) {
			@Override protected int doCompare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof TrpJobStatus && e2 instanceof TrpJobStatus) {
					return Long.compare(((TrpJobStatus)e1).getCreateTime(), ((TrpJobStatus)e2).getCreateTime());
				}
				
				return 0;
			}
		}),
		new ColumnConfig(DOC_ID_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(USER_NAME_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(PAGE_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(DESCRIPTION_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	
//	HashMap<String, TableColumn> cols=new HashMap<>();
	
	Button reloadBtn, showAllJobsBtn, cancelBtn;
	
	public JobOverviewWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(3, false));
				
		reloadBtn = new Button(this, SWT.NONE);
		reloadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		reloadBtn.setToolTipText("Reload job list");
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		
		showAllJobsBtn = new Button(this, SWT.CHECK);
		showAllJobsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		showAllJobsBtn.setText("Show all jobs");
		
		cancelBtn = new Button(this, SWT.NONE);
		cancelBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		cancelBtn.setText("Cancel job");
//		cancelBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		
		jobTableViewer = new MyTableViewer(this, SWT.SINGLE | SWT.FULL_SELECTION);
		jobTableViewer.setContentProvider(new ArrayContentProvider());
		labelProvider = new JobOverviewLabelProvider(this);
		jobTableViewer.setLabelProvider(labelProvider);
		
		jobTable = jobTableViewer.getTable();
		jobTable.setHeaderVisible(true);
		jobTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		jobTableViewer.addColumns(COLS);
	}
				
	public Button getReloadBtn() { return reloadBtn; }
	public Button getShowAllJobsBtn() { return showAllJobsBtn; }
	public Button getCancelBtn() { return cancelBtn; }
	
	@Override public void setEnabled(boolean enabled) {
		reloadBtn.setEnabled(enabled);
		showAllJobsBtn.setEnabled(enabled);
		jobTable.setEnabled(enabled);
	}
	
	public void update(boolean isLoggedIn, boolean isAdmin) {
		setEnabled(isLoggedIn);
		if (!isLoggedIn)
			return;
		
		showAllJobsBtn.setEnabled(isAdmin);
	}
	
	public TrpJobStatus getJob(String jobId) {
		for (TrpJobStatus job : getJobs()) {
			if (job.getJobId().equals(jobId))
				return job;
		}
		return null;
	}
	
	public List<TrpJobStatus> getJobs() { 
		return (List<TrpJobStatus>) jobTableViewer.getInput();
	}
	
	public void setInput(List<TrpJobStatus> jobs) {
//		IStructuredSelection oldSel = (IStructuredSelection) jobTableViewer.getSelection();
//		TrpJobStatus stat = null;
//		if (!sel.isEmpty()) {
//			stat = (TrpJobStatus) sel.getFirstElement();
//		}
		
		jobTableViewer.setInput(jobs);
//		jobTableViewer.setSelection(oldSel);
	}
	
	public int getRowIndex(Object data) {
		if (!(data instanceof TrpJobStatus))
			return -1;
		
		TrpJobStatus job = (TrpJobStatus) data;
		int i=0;
		for (TrpJobStatus j : getJobs()) {
			if (j.equals(job)) {
				return i;
			}
			++i;
		}
		return -1;
	}
		
	public void clearInput() {
		jobTableViewer.setInput(new ArrayList<TrpJobStatus>());
	}
	
	public TableViewer getJobTableViewer() { return jobTableViewer; }

}
