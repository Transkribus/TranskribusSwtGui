package eu.transkribus.swt_canvas.util;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;

public class CreateThumbsThread extends Thread {
	private final static Logger logger = LoggerFactory.getLogger(CreateThumbsThread.class);
	
	TrpDoc doc;
	
	public CreateThumbsThread(TrpDoc doc) {
		Assert.assertNotNull("document is null", doc);
		
		this.doc = doc;
	}
	
	
	@Override
	public void run() {
		logger.debug("starting to create thumbnail for local doc: "+doc.getMd().getLocalFolder());
		try {
//			LocalDocWriter.createThumbsForDoc(storage.getDoc(), false);
			SWTUtil.createThumbsForDoc(doc, false);
			
			onFinished();
		} catch (Exception e) {
			logger.error("Error creating thumbnails for local doc: " + e.getMessage(), e);
		}
	}
	
	protected void onFinished() {
		
	}

}
