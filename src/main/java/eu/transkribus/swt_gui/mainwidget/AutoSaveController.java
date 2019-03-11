package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.io.FilenameFilter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.batik.dom.GenericEntityReference;
import org.apache.commons.io.FileUtils;
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
	
	
	Thread autoSaveThread=null;
	
	private final boolean localAutosaveEnabled = true;
	private long lastAutoSaveTime = 0;
	private int autoSaveErrorCounter=0;
	private int outerAutoSaveErrorCounter=0;
	static final int MAX_NR_OF_AUTO_SAVE_ERRORS = 5;
	
	Runnable saveTask = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					logger.trace("in autosave thread: autosaveenabled = "+trpSets.getAutoSaveEnabled());
					if (outerAutoSaveErrorCounter >= MAX_NR_OF_AUTO_SAVE_ERRORS) {
						logger.warn("autosave feature broken --> skipping out of thread");
						break;
					}
					
					Thread.sleep(mw.getTrpSets().getAutoSaveInterval() * 1000);
					if (trpSets.getAutoSaveEnabled()) {
						Display.getDefault().asyncExec(() -> {
							localAutoSave(mw.getTrpSets().getAutoSaveFolder());
							outerAutoSaveErrorCounter=0;
						});
					}
				} catch (Exception e) {
					logger.error("Exception " + e, e);
					outerAutoSaveErrorCounter++;
				}
			}
		}
	};
	
	public AutoSaveController(TrpMainWidget mw) {
		this.mw = mw;
		this.trpSets = mw.getTrpSets();
		beginAutoSaveThread();
	}
	
	private void localAutoSave(String path) {
		if (autoSaveErrorCounter >= MAX_NR_OF_AUTO_SAVE_ERRORS) {
			logger.warn("reached max nr of subsequent autosave errors - skipping autosave from now on!");
			return;
		}
		
		if (!localAutosaveEnabled) {
			return;
		}
		if (!storage.isPageLoaded()) {
			return;
		}
		if (!storage.hasTranscript() || !storage.hasTranscriptMetadata()) {
			return;
		}
		
		logger.trace("performing local autosave, interval: "+mw.getTrpSets().getAutoSaveInterval());
		
		JAXBPageTranscript tr = storage.getTranscript();
		PcGtsType currentPage = tr.getPageData();
		
		if (!tr.getPage().isEdited()) {
			logger.trace("transcript not edited... skipping autosave!");
			return;
		}

		if (!tr.getPage().isEditedSince(lastAutoSaveTime)) {
			logger.trace("transcript not edited since last autosave ("+CoreUtils.toTimeString(lastAutoSaveTime)+") - skipping autosave!");
			return;
		}

		File f = null;
		try {
			if (currentPage == null) {
				return;
			}
			
			// FIXME: do not write setLastChange date into current transcript but directly into the autosave file xml
			// (should not touch currently set transcript here!!)
			Date datenow = new Date();
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(datenow);
			XMLGregorianCalendar xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
			currentPage.getMetadata().setLastChange(xc);
			
			String tempDir = path;
			tempDir += File.separator + storage.getTranscript().getMd().getPageId() + ".xml";
			// tempDir += File.separator + "p" +
			// storage.getTranscript().getMd().getPageId()+"_autoSave.xml";
			f = new File(tempDir);

			byte[] bytes = PageXmlUtils.marshalToBytes(currentPage);
			// PageXmlUtils.marshalToFile(storage.getTranscript().getPageData(), f);
			FileUtils.writeByteArrayToFile(f, bytes);
			logger.debug("Auto-saved current transcript to " + f.getAbsolutePath());
			lastAutoSaveTime = System.currentTimeMillis();
			autoSaveErrorCounter = 0;
		} catch (Exception e1) {
			// onError("Saving Error", "Error while saving transcription to " +
			// f.getAbsolutePath(), e1);
			String fn = f == null ? "NA" : f.getAbsolutePath();
			++autoSaveErrorCounter;
			logger.error("Error while autosaving transcription to " + fn+" (autoSaveErrorCounter = "+autoSaveErrorCounter+")", e1);
		}
	}
	
	private void beginAutoSaveThread() {
		if (autoSaveThread==null) {
			autoSaveThread = new Thread(saveTask, "AutoSaveThread");
			autoSaveThread.start();
			logger.debug("AutoSave Thread started");
		}
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
	    List<File> fileList = new ArrayList<>();
	    if (files != null) {
	    	fileList = Arrays.asList(files);	
	    }
	    
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
	
	public boolean deleteAutoSavedFilesForThisPage(TrpPage page){
		try {
			List<File> files = getAutoSavesFiles(page);
			for (File file : files){
				file.delete();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("The locally stored 'auto save' file could not be deleted!");
			e.printStackTrace();
		}
		return true;
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
//		mw.ui.taggingWidget.updateAvailableTags();
		mw.updateSelectedTranscriptionWidgetData();
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
