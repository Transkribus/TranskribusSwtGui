package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.eclipse.nebula.widgets.pagination.IPageLoader;
import org.eclipse.nebula.widgets.pagination.collections.PageResult;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.job.JobError;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.pagination_table.ATableWidgetPagination;
import eu.transkribus.swt.pagination_table.IPageLoadMethod;
import eu.transkribus.swt.pagination_table.RemotePageLoaderSingleRequest;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class JobErrorTableWidgetPagination extends ATableWidgetPagination<JobError> {
	private final static Logger logger = LoggerFactory.getLogger(JobErrorTableWidgetPagination.class);

	protected final int jobId;
	
	public static final String PAGE_NR_COL = "Page Nr.";
	public static final String MSG_COL = "Error";
	public static final String EX_COL = "Exception";
	public static final String DOC_ID_COL = "Doc-ID";
	
//	public static final String ID_COL = "ID";

	public final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(PAGE_NR_COL, 50, true, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(MSG_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DOC_ID_COL, 80, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(EX_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
//			new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC)
	};


	// DocJobUpdater docUpdater;
	static Storage store = Storage.getInstance();

	public JobErrorTableWidgetPagination(Composite parent, int style, int initialPageSize, final int jobId) {
		super(parent, style, initialPageSize);
		this.jobId = jobId;

		pageableTable.getController().setSort("createTime", SWT.UP);

		Composite btns = new Composite(this, 0);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.fill = true;
		// btns.setLayout(rl);
		btns.setLayout(new GridLayout(2, false));

		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.moveAbove(pageableTable);

		reloadJobErrorList();
	}

	
	@Override
	protected void setPageLoader() {
		IPageLoadMethod<JobErrorList, JobError> plm = new IPageLoadMethod<JobErrorList, JobError>() {
			@Override
			public JobErrorList loadPage(int fromIndex, int toIndex, String sortPropertyName,
					String sortDirection) {
				JobErrorList errors = new JobErrorList(new ArrayList<>(0), 0, 0, -1, null, null);
				if (store != null && store.isLoggedIn()) {
					try {
						// sw.start();
						logger.debug("loading job errors from server...");
						errors = store.getConnection().getJobErrors(""+getJobId(), fromIndex, toIndex - fromIndex, sortPropertyName, sortDirection);
					} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException | IllegalArgumentException e) {
						TrpMainWidget.getInstance().onError("Error loading job errors", e.getMessage(), e);
					}
				}
				try {
					logger.debug(JaxbUtils.marshalToString(errors, JobError.class));
					for(JobError e : errors.getList()) {
						logger.debug(e.getClass().getCanonicalName() + " -> "+e);
					}
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.debug("returning error list object of size = " + errors.getList().size());
				return errors;
			}
		};
		final IPageLoader<PageResult<JobError>> pl = new RemotePageLoaderSingleRequest<>(pageableTable.getController(), plm);
		pageableTable.setPageLoader(pl);
	}

	@Override
	protected void createColumns() {
		createDefaultColumn(PAGE_NR_COL, 50, "pageNr", true);
		createDefaultColumn(MSG_COL, 600, "message", true);
		createDefaultColumn(DOC_ID_COL, 80, "docId", true);
		createDefaultColumn(EX_COL, 200, "exceptionClass", true);		
	}

	public void reloadJobErrorList() {
		try {
			logger.debug("reloading job error list!");
			refreshPage(true);
			// startOrResumeJobThread();
		} catch (Exception ex) {
			TrpMainWidget.getInstance().onError("Error", "Error during update of jobs", ex);
		}
	}
	
	public int getJobId() {
		return this.jobId;
	}
}
