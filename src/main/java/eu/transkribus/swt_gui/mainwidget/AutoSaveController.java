package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class AutoSaveController {
	private static final Logger logger = LoggerFactory.getLogger(AutoSaveController.class);
	
	TrpMainWidget mw;
	TrpSettings trpSets;
	static Storage storage = Storage.getInstance();

	public AutoSaveController(TrpMainWidget mw) {
		this.mw = mw;
		this.trpSets = mw.getTrpSets();
	}
	
	public List<File> getAutoSavesFiles(TrpPage page) {
		if (page == null || page.getCurrentTranscript() == null)
			return new ArrayList<>();
		
		logger.debug("Checking for local autosave files...");
		int remotePageId = page.getPageId();
		String localPath = mw.getTrpSets().getAutoSaveFolder();
		File dir = new File(localPath);
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.replace(".xml", "").equals(Integer.toString(remotePageId));
			}
		};

	    File[] files = dir.listFiles(filter);
	    List<File> fileList = CoreUtils.asList(files);
	    
	    logger.debug("found autosave files: "+fileList.size());
	    return fileList;
	}
	
	public File getLatestAutoSaveFile(TrpPage page) {
		List<File> files = getAutoSavesFiles(page);
		if (files.isEmpty())
			return null;
		else
			return files.get(0);
	}
		
	public File getLatestAutoSaveFileNewerThanPage(TrpPage page) {
		try {
			File localTranscript = getLatestAutoSaveFile(page);
			if (localTranscript == null)
				return null;
			
			long lLocalTimestamp = localTranscript.lastModified();
		    GregorianCalendar gc = new GregorianCalendar();
		    gc.setTimeInMillis(lLocalTimestamp);
		    XMLGregorianCalendar localTimestamp;
			localTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
	    
			logger.debug("local timestamp: "
	    		+localTimestamp.getMonth()
			    + "/" + localTimestamp.getDay()
			    +"h" + localTimestamp.getHour() 
			    + "m" + localTimestamp.getMinute() 
			    + "s" + localTimestamp.getSecond());
	    
		    long lRemoteTimestamp = page.getCurrentTranscript().getTimestamp();
		    gc.setTimeInMillis(lRemoteTimestamp);
		    XMLGregorianCalendar remoteTimeStamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);	  
	//	    XMLGregorianCalendar remoteTimeStamp = storage.getTranscript().getPage().getPcGtsType().getMetadata().getLastChange();
	
		    logger.debug("remote timestamp: "
	    		+remoteTimeStamp.getMonth()
			    + "/" + remoteTimeStamp.getDay()
			    +"h" + remoteTimeStamp.getHour() 
			    + "m" + remoteTimeStamp.getMinute() 
			    + "s" + remoteTimeStamp.getSecond());
	    
		    //Return false if local autosave transcript is older
		    if(localTimestamp.compare(remoteTimeStamp)==DatatypeConstants.LESSER 
		    		||localTimestamp.compare(remoteTimeStamp)==DatatypeConstants.EQUAL ){
		    	logger.debug("No newer autosave transcript found.");
		    	return null;
		    }
		    
		    return localTranscript;
		}
	    catch (DatatypeConfigurationException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public void loadAutoSaveTranscriptFileIntoView(File localTranscript) {
		logger.debug("loading local transcript into view: "+localTranscript);
		if (localTranscript == null)
			return;
		
		try {
			PcGtsType pcLocal = PageXmlUtils.unmarshal(localTranscript);
    		JAXBPageTranscript jxtr = new JAXBPageTranscript();
    		jxtr.setPageData(pcLocal);
    		storage.getTranscript().setPageData(pcLocal);
    		storage.getTranscript().setMd(jxtr.getMd());
    		storage.setLatestTranscriptAsCurrent();
			mw.loadJAXBTranscriptIntoView(storage.getTranscript());
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Error when loading transcript into view.", e.getMessage(), e.getCause());
			e.printStackTrace();
		}
		mw.ui.taggingWidget.updateAvailableTags();
		mw.updateTranscriptionWidgetsData();
		mw.canvas.getScene().updateSegmentationViewSettings();
		mw.canvas.update();
		
//		reloadCurrentPage(true);
	}
	
	public boolean checkForNewerAutoSavedPage(TrpPage page) {
		try {
			File localTranscript = getLatestAutoSaveFileNewerThanPage(page);
			if (localTranscript == null) {
				logger.debug("No local autosave file newer than the given page found.");
		    	return false;
			}

		    logger.debug("Newer autosave transcript found.");
		    Display.getDefault().syncExec(new Runnable() {
		        public void run() {
		        	String diagText = "A newer transcript of this page exists on your computer. Do you want to load it?";
		        	if(DialogUtil.showYesNoCancelDialog(mw.getShell(),"Newer version found in autosaves",diagText) == SWT.YES){
		        		loadAutoSaveTranscriptFileIntoView(localTranscript);     		
		        	}
		        }
		    });
	    } catch (Exception e) {
	    	logger.error(e.getMessage(), e);
	    }
	    
		return true;
	}
}
