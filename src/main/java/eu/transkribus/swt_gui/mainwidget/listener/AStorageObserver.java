package eu.transkribus.swt_gui.mainwidget.listener;

import java.util.Observable;
import java.util.Observer;

import eu.transkribus.swt_gui.mainwidget.Storage.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.DocLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.DocMetadataUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.JobUpdateEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.LoginOrLogoutEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.MainImageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.PageLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.TranscriptListLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.TranscriptLoadEvent;
import eu.transkribus.swt_gui.mainwidget.Storage.TranscriptSaveEvent;

public abstract class AStorageObserver implements Observer {
	
	public AStorageObserver() {
	}
	
	protected void handleMainImageLoadEvent(MainImageLoadEvent mile) {
	}
	
	protected void handleTranscriptSaveEvent(TranscriptSaveEvent tse) {
	}
	
	protected void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
	}
	
	protected void handleJobUpdate(JobUpdateEvent jue) {
	}
	
	protected void handleDocLoadEvent(DocLoadEvent dle) {
	}
	
	protected void handleTranscriptListLoadEvent(TranscriptListLoadEvent arg) {
	}

	protected void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {		
	}
	
	protected void handleLoginOrLogout(LoginOrLogoutEvent arg) {
	}

	protected void handlePageLoadEvent(PageLoadEvent arg) {
	}
	
	protected void handleDocMetadataUpdateEvent(DocMetadataUpdateEvent e) {
	}
	
	@Override public void update(Observable o, Object arg) {
		if (arg instanceof JobUpdateEvent) {
			handleJobUpdate((JobUpdateEvent) arg);
		}
		else if (arg instanceof DocLoadEvent) {
			handleDocLoadEvent((DocLoadEvent) arg);
		}
		else if (arg instanceof PageLoadEvent) {
			handlePageLoadEvent((PageLoadEvent) arg);
		}
		else if (arg instanceof TranscriptLoadEvent) {
			handleTranscriptLoadEvent((TranscriptLoadEvent) arg);
		}
		else if (arg instanceof TranscriptListLoadEvent) {
			handleTranscriptListLoadEvent((TranscriptListLoadEvent) arg);
		}
		else if (arg instanceof LoginOrLogoutEvent) {
			handleLoginOrLogout((LoginOrLogoutEvent) arg);
		}
		else if (arg instanceof CollectionsLoadEvent) {
			handleCollectionsLoadEvent((CollectionsLoadEvent) arg);
		}
		else if (arg instanceof DocMetadataUpdateEvent) {
			handleDocMetadataUpdateEvent((DocMetadataUpdateEvent) arg);
		}
		else if (arg instanceof TranscriptSaveEvent) {
			handleTranscriptSaveEvent((TranscriptSaveEvent) arg);
		}
		else if (arg instanceof MainImageLoadEvent) {
			handleMainImageLoadEvent((MainImageLoadEvent) arg);
		}
	}


}
