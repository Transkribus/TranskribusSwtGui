package eu.transkribus.swt_gui.pagination_tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class JobTableWidgetListener extends SelectionAdapter implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(JobTableWidgetListener.class);
	
	TrpMainWidget mainWidget;
	JobTableWidgetPagination jobOverviewWidget;
	Storage store = Storage.getInstance();

	public JobTableWidgetListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.jobOverviewWidget = mainWidget.getUi().getJobOverviewWidget();
		
//		jobOverviewWidget.reloadBtn.addSelectionListener(this);
		jobOverviewWidget.getShowAllJobsBtn().addSelectionListener(this);
		jobOverviewWidget.getCancelBtn().addSelectionListener(this);
		
		Storage.getInstance().addObserver(this);
	}
	
	@Override public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		if (s.equals(jobOverviewWidget.getShowAllJobsBtn())) {
			mainWidget.reloadJobList();
		} else if(s.equals(jobOverviewWidget.getCancelBtn())) {
			TrpJobStatus job = jobOverviewWidget.getFirstSelected();
			if(job != null && !job.getState().equals(TrpJobStatus.FINISHED) &&
					!job.getState().equals(TrpJobStatus.CANCELED) &&
					!job.getState().equals(TrpJobStatus.FAILED)){
				logger.debug("Canceling job with id = " + job.getJobId());
				mainWidget.cancelJob(job.getJobId());
			}
		}
	}

	@Override public void update(Observable o, Object arg) {
		if (arg instanceof LoginOrLogoutEvent) {
			boolean visible = Storage.getInstance().isLoggedIn() && Storage.getInstance().getUser().isAdmin();
			jobOverviewWidget.getShowAllJobsBtn().setVisible(visible);
		}
	}
	
	

}
