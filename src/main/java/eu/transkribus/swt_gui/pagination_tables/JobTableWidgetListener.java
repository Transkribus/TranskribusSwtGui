package eu.transkribus.swt_gui.pagination_tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.itextpdf.text.DocListener;

public class JobTableWidgetListener extends SelectionAdapter implements Observer, IDoubleClickListener {
	private final static Logger logger = LoggerFactory.getLogger(JobTableWidgetListener.class);
	
	TrpMainWidget mainWidget;
	JobTableWidgetPagination jobOverviewWidget;
	Storage store = Storage.getInstance();
	
	TableViewer tv;

	public JobTableWidgetListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.jobOverviewWidget = mainWidget.getUi().getJobOverviewWidget();
		
//		jobOverviewWidget.reloadBtn.addSelectionListener(this);
		jobOverviewWidget.getShowAllJobsBtn().addSelectionListener(this);
		jobOverviewWidget.getCancelBtn().addSelectionListener(this);
		
		this.tv = jobOverviewWidget.getPageableTable().getViewer();
		
		tv.addDoubleClickListener(this);
		
		Storage.getInstance().addObserver(this);
	}
	
	@Override public void doubleClick(DoubleClickEvent event) {
		TrpJobStatus jobStatus = jobOverviewWidget.getFirstSelected();
		logger.debug("double click on transcript: "+jobStatus);
		
		if (jobStatus!=null) {
			logger.debug("Loading doc: " + jobStatus.getDocId());
			int col = 0;
			try {
				List<TrpDocMetadata> docList = 
						store.getConnection().findDocuments(0, jobStatus.getDocId(), "", "", "", "", true, false, 0, 0, null, null);
				if (docList != null && docList.size() > 0){
					col = docList.get(0).getColList().get(0).getColId();
				}
						
			} catch (SessionExpiredException | ServerErrorException
					| ClientErrorException | IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mainWidget.loadRemoteDoc(jobStatus.getDocId(), col);
		}		
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
