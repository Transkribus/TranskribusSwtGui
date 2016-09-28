package eu.transkribus.swt_canvas.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;

public class CreateThumbsService {
	private final static Logger logger = LoggerFactory.getLogger(CreateThumbsService.class);
	
	static final ExecutorService ex = Executors.newFixedThreadPool(5);
	
	public static class CreateThumbsRunnable implements Runnable {
		TrpDoc doc;
		TrpPage page;
		Image img;
		boolean overwrite=false;
		
		public CreateThumbsRunnable(TrpDoc doc, boolean overwrite) {
			Assert.assertNotNull("document is null", doc);
			
			this.doc = doc;
			this.overwrite = overwrite;
		}
		
		public CreateThumbsRunnable(TrpPage page, Image img, boolean overwrite) {
			this.page = page;
			this.img = img;
			this.overwrite = overwrite;
		}
		
		@Override
		public void run() {
			if (doc != null) {
				logger.debug("creating thumbnails for local doc: "+doc.getMd().getLocalFolder());
				
				try {
					SWTUtil.createThumbsForDoc(doc, overwrite, null);
				} catch (Exception e) {
					logger.error("Error creating thumbnails for local doc: " + e.getMessage(), e);
				}
			} else if (page != null) {
				logger.debug("creating thumbnail for local page: "+page);
				
				try {
					SWTUtil.createThumbForPage(page, img, overwrite);
				} catch (Exception e) {
					logger.error("Error creating thumbnails for page "+page.getPageNr()+": " + e.getMessage(), e);
				}
			} 
			onFinished();
		}
		
		protected void onFinished() {
			
		}
	}
		
	public static void createThumbForDoc(TrpDoc doc, boolean overwrite, final Runnable onFinished) {
		if (doc==null)
			return;
		
		logger.debug("creating thumbs for doc: "+doc.getMd());
		ex.execute(new CreateThumbsRunnable(doc, overwrite) {
			protected void onFinished() {
				if (onFinished != null) {
					Display.getDefault().asyncExec(onFinished);
				}
			}
		});
	}
	
	public static void createThumbForPage(TrpPage page, Image img, boolean overwrite, final Runnable onFinished) {
		if (page == null)
			return;
		
		logger.debug("creating thumbs for page: "+page.getImgFileName());
		ex.execute(new CreateThumbsRunnable(page, img, overwrite) {
			protected void onFinished() {
				if (onFinished != null) {
					Display.getDefault().asyncExec(onFinished);
				}
			}
		});
	}
	
	public static void stop(boolean now) {
		if (now) {
			ex.shutdownNow();
		} else
			ex.shutdown();
	}
	
	
	public static void main(String[] args) throws Exception {
		TrpDoc doc = LocalDocReader.load("/tmp/test_doc_tif_enrique_2");
		System.out.println("loaded doc");
		
//		ex.execute(new CreateThumbsThread(doc, false));
		
		ex.execute(new CreateThumbsRunnable(doc.getPages().get(25), null, true));
		
		System.in.read(); // wait for a user input
		
//		char c = '0';
//		while (c != 't') {
//			c = (char) System.in.read();
//			System.out.println("c = "+c);
//		}
		
		System.out.println("shutting down exectuor!");
		
		ex.shutdownNow();
	}
	
}


