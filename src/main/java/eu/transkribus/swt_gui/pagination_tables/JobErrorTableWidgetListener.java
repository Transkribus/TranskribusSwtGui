package eu.transkribus.swt_gui.pagination_tables;

import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.job.JobError;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class JobErrorTableWidgetListener extends SelectionAdapter implements IStorageListener, IDoubleClickListener {
	private final static Logger logger = LoggerFactory.getLogger(JobErrorTableWidgetListener.class);

	JobErrorTableWidgetPagination jw;
	Storage storage = Storage.getInstance();
	
	TableViewer tv;

	public JobErrorTableWidgetListener(JobErrorTableWidgetPagination jw) {
		Assert.assertNotNull("JobTablWidgetPagination cannot be null!", jw);
		
		this.jw = jw;		
		this.tv = jw.getPageableTable().getViewer();
		
		jw.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
		
		attach();
	}
	
	void attach() {
		tv.addDoubleClickListener(this);
		Storage.getInstance().addListener(this);
	}
	
	void detach() {
		tv.removeDoubleClickListener(this);
		Storage.getInstance().removeListener(this);		
	}
	
	@Override public void doubleClick(DoubleClickEvent event) {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		
		JobError error = jw.getFirstSelected();
		logger.debug("double click on error: "+ error);
		
		if (error != null) {
			logger.debug("Loading doc: " + error.getDocId());
			int col = 0;
			try {
				List<TrpDocMetadata> docList = 
						storage.getConnection().findDocuments(0, error.getDocId(), "", "", "", "", true, false, 0, 0, null, null);
				if (docList != null && docList.size() > 0){
					col = docList.get(0).getColList().get(0).getColId();
				}
						
			} catch (SessionExpiredException | ServerErrorException
					| ClientErrorException | IllegalArgumentException e) {
				logger.error("Could not find document: " + e.getMessage(), e);
			}
			if(col > 0) {
				TrpLocation l = new TrpLocation();
				l.collId = col;
				l.docId = error.getDocId();
				l.pageNr = error.getPageNr();
				l.shapeId = error.getLineId();
				mw.showLocation(l);
			}
		}		
	}	
}
