package eu.transkribus.swt_gui.canvas;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.CanvasTransform;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.SWTUtil;
import examples.MemoryUsage;

final public class CanvasImage {
	private final static Logger logger = LoggerFactory.getLogger(CanvasImage.class);
	
	public final float DEFAULT_INTERNAL_SCALING=0.5f;
	public final int N_PIXELS_THRESHOLD_FOR_SCALING = (int) (15 * 1e6);
	
	public URL url;
	public Image img;
//	public Image imgBackup;
	
	public int width;
	public int height;
	public long nPixels;
	
	public Float internalScalingFactor=null;
	
	public double gamma=1.0f;
	
	public CanvasImage(URL url) throws Exception {
		this.url = url;
		
		logger.debug("--- memory before loading image ---");
		MemoryUsage.printMemoryUsage();
		
		Image imgIn = ImgLoader.load(url);
		
		logger.debug("--- memory after loading image ---");
		MemoryUsage.printMemoryUsage();
		
		logger.debug("loaded image from "+url.toString());
		
		if (imgIn==null)
			throw new Exception("Could not load image: "+url);
		
		this.width = imgIn.getBounds().width;
		this.height = imgIn.getBounds().height;
		this.nPixels = this.width * this.height;
		
		this.internalScalingFactor = null;
		if (true && nPixels > N_PIXELS_THRESHOLD_FOR_SCALING) {
			logger.debug("before loading scaled image");
			internalScalingFactor = DEFAULT_INTERNAL_SCALING;
			logger.debug("internalScalingFactor = "+internalScalingFactor);
			
			Image imgScaled = new Image(imgIn.getDevice(), 
										Math.round(width*internalScalingFactor),
										Math.round(height*internalScalingFactor));
			
			logger.debug("--- memory usage after loading scaled image ---");
			MemoryUsage.printMemoryUsage();
			
			CanvasTransform scaleTr = new CanvasTransform(imgIn.getDevice());
			scaleTr.scale(internalScalingFactor, internalScalingFactor);
			GC gc = new GC(imgScaled);
			gc.setAntialias(SWT.ON);
			gc.setInterpolation(SWT.HIGH);
			
			gc.setTransform(scaleTr);
			gc.drawImage(imgIn, 0, 0);
			
			gc.dispose();
			scaleTr.dispose();
			
			imgIn.dispose();
			
			logger.info("--- memory usage after disposing orignal image ---");
			MemoryUsage.printMemoryUsage();
			
			this.img = imgScaled;
		}
		else {
			this.img = imgIn;
		}
		
//		backup();
	}
	
//	private void backup() {
//		if (img != null && !img.isDisposed()) {
//			SWTUtil.dispose(imgBackup);
//			this.imgBackup = new Image(img.getDevice(), img, SWT.IMAGE_COPY);
//		}
//	}
	
//	public void revert() {
//		if (imgBackup != null && !imgBackup.isDisposed()) {
//			SWTUtil.dispose(img);
//			this.img = new Image(imgBackup.getDevice(), imgBackup, SWT.IMAGE_COPY);
//		}
//	}
	
	public void applyGamma(double gamma) {
		if (SWTUtil.isDisposed(img) /*|| SWTUtil.isDisposed(imgBackup)*/) {
			return;
		}
		
		if (true) { 
			logger.debug("this.gamma = "+this.gamma);	
			double scaledGamma = gamma / this.gamma;
			logger.debug("scaledGamma = "+scaledGamma);
			ImageData d = SWTUtil.multScalar(img.getImageData(), scaledGamma, true);
			
			logger.debug("disposing old image and creating new one with scaled image data...");
			img.dispose();
			img = new Image(Display.getDefault(), d);
		}
//		else {
//			logger.debug("gamma = "+gamma);
//			ImageData d = SWTUtil.multScalar(imgBackup.getImageData(), gamma, false);
//			SWTUtil.dispose(img);
//			img = new Image(Display.getDefault(), d);
//		}
		
		this.gamma = gamma;
	}
	
	public void dispose() {
		SWTUtil.dispose(img);
//		SWTUtil.dispose(imgBackup);
		
//		if (img!=null && !img.isDisposed())
//			img.dispose();
	}
	
	public boolean isDisposed() {
		return (img==null || img.isDisposed());
	}
	
	public void paint(GC gc, SWTCanvas canvas) {
		try {
			if (internalScalingFactor!=null) {
				CanvasTransform myT = canvas.getTransformCopy();
				myT.scale(1.0f/internalScalingFactor, 1.0f/internalScalingFactor); // revert internal scaling!
				gc.setTransform(myT);
				
//				gc.setF
				
				gc.drawImage(img, 0, 0);
				gc.setTransform(canvas.getPersistentTransform());
				myT.dispose();
			}
			else if (img != null && !img.isDisposed())
				gc.drawImage(img, 0, 0);
//			img.getImageData().depth
			
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the bounds of the <em>original</em> image, i.e. not the bounds of the probably scaled one 
	 * (which would be this.img.getBounds())
	 */
	public Rectangle getBounds() {
		return new Rectangle(0, 0, width, height);
	}
	
}
