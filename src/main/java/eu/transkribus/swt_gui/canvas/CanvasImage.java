package eu.transkribus.swt_gui.canvas;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.CanvasTransform;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.util.MemoryUsage;

final public class CanvasImage {
	private final static Logger logger = LoggerFactory.getLogger(CanvasImage.class);
	
	public final float DEFAULT_INTERNAL_SCALING=0.5f;
	public final int N_PIXELS_THRESHOLD_FOR_SCALING = (int) (15 * 1e6);
	
	public URL url;
	public Image img;
	Image imgOrig;
	Image imgBackup;

	Image imgRot;
	
	public int width;
	public int height;
	public long nPixels;
	
	public Float internalScalingFactor=null;
	
	public double gamma=1.0f;
	public double thresh=0.5f;
	
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
		
		setOrig(img);
		backup(img);
	}
	
	private void setOrig(Image image) {
		if (image != null && !image.isDisposed()) {
			SWTUtil.dispose(imgOrig);
			this.imgOrig = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
		}
	}
	
	private void backup(Image image) {
		if (image != null && !image.isDisposed()) {
			SWTUtil.dispose(imgBackup);
			this.imgBackup = new Image(image.getDevice(), image, SWT.IMAGE_COPY);
		}
	}
	
	public void revert() {
		if (imgBackup != null && !imgBackup.isDisposed()) {
			SWTUtil.dispose(img);
			this.img = new Image(imgBackup.getDevice(), imgBackup, SWT.IMAGE_COPY);
		}
	}
	
	public void applyThreshold(double factor) {
		if (SWTUtil.isDisposed(img)) {
			return;
		}
		logger.debug("this.thresh = "+this.thresh);	
		ImageData d = SWTUtil.thresholdImage(imgOrig.getImageData(), factor, false);
		
		logger.debug("disposing old image and creating new one with scaled image data...");
		img.dispose();
		img = new Image(Display.getDefault(), d);
		
	}
	
	public void applyGamma(double gamma) {
		if (SWTUtil.isDisposed(img) /*|| SWTUtil.isDisposed(imgBackup)*/) {
			return;
		}
		
		logger.debug("this.gamma = " + this.gamma);
		double scaledGamma = gamma / this.gamma;
		logger.debug("scaledGamma = " + scaledGamma);
		ImageData d = SWTUtil.multScalar(imgBackup.getImageData(), scaledGamma, false);

		logger.debug("disposing old image and creating new one with scaled image data...");
		img.dispose();
		img = new Image(Display.getDefault(), d);
		
		backup(img);
		
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
	
	/**
	 * @deprecated does not work yet...
	 */
	public void rotateImage(SWTCanvas canvas, float angleDeg) {
		logger.debug("rotating main image: "+angleDeg);
		
		if (CoreUtils.equalsEps(0.0f, angleDeg, 0.1f)) {
			SWTUtil.dispose(imgRot);
			imgRot = null;
		}
		else {
			SWTUtil.dispose(imgRot);
//			imgRot = new Image(canvas.getDisplay(), canvas.getClientArea().width, canvas.getClientArea().height);
			imgRot = new Image(canvas.getDisplay(), img.getBounds().width*2, img.getBounds().height*2);
//			imgRot = new Image(canvas.getDisplay(), img, SWT.IMAGE_COPY);
			GC gc = new GC(imgRot);
//			gc.setClipping(canvas.getClientArea());
			CanvasTransform transform = new CanvasTransform(canvas.getDisplay());
			Point center = canvas.getScene().getCenter();
			transform.rotateCenter(center.x, center.y, angleDeg);
			transform.rotateCenter(0, 0, angleDeg);
//			gc.drawLine(0, 0, canvas.getClientArea().width, canvas.getClientArea().height);
			gc.setTransform(transform);
	        gc.drawImage(img, 0, 0);
	        gc.dispose();
	        transform.dispose();
		}
	}
	
	private void drawImageFromSeparateGC(GC gc, SWTCanvas canvas) {
		// TODO: apply rotation directly on image also - for now, if rotated, draw image the usual way (which is slow on windows!)
		if (!CoreUtils.equalsEps(0.0f, canvas.getPersistentTransform().getAngleDeg(), 1e-4)) {
			gc.drawImage(img, 0, 0); // VERY SLOW ON WINDOWS MACHINES!
			return;
		}
		
		Image img=this.img;
		if (!SWTUtil.isDisposed(imgRot)) {
			logger.debug("drawing rotated image!");
			img = this.imgRot;
		}
		
//		CanvasTransform transform = canvas.getPersistentTransform();
		CanvasTransform transform = canvas.getTransformCopy();
		if (internalScalingFactor != null) { // revert internal scaling!
			transform.scale(1.0f/internalScalingFactor, 1.0f/internalScalingFactor);
		}
		
		Rectangle clientRect= canvas.getClientArea();
		Rectangle imageRect=transform.inverseTransform(canvas.getClientArea());
		
		// find a better start point to render:
		/*
		int gap = 2;
		imageRect.x -= gap;
		imageRect.y -= gap;
		imageRect.width += 2 * gap;
		imageRect.height += 2 * gap;
		*/
		
		Rectangle imageBound = img.getBounds();
		imageRect = imageRect.intersection(imageBound);
		Rectangle destRect = transform.transform(imageRect);
		
		Image screenImage = new Image(canvas.getDisplay(),clientRect.width, clientRect.height);
		GC newGC = new GC(screenImage);
		
		newGC.setClipping(clientRect);
		
//		if (!CoreUtils.equalsEps(0.0f, canvas.getPersistentTransform().getAngleDeg(), 0.1)) {
//			CanvasTransform rot = new CanvasTransform(canvas.getDisplay());
//			Point center = canvas.getScene().getCenter();
//			rot.rotateCenter(center.x, center.y, transform.getAngleDeg());
//			newGC.setTransform(rot);
//		}
		
		newGC.setBackground(canvas.getBackground());
		newGC.fillRectangle(clientRect); // draw background color
		newGC.drawImage(img, imageRect.x, imageRect.y, imageRect.width, imageRect.height, destRect.x,
				destRect.y, destRect.width, destRect.height);

		gc.setAdvanced(false);
		gc.drawImage(screenImage, 0, 0);
		gc.setAdvanced(true);
		
		screenImage.dispose();
		newGC.dispose();
		transform.dispose();
		
//		gc.setTransform(canvas.getPersistentTransform()); // needed? (...don't think so)
	}
	
	/**
	 * FIXME old method to paint image. Delete when {@link #paint(GC, SWTCanvas)} works.
	 * #189
	 * 
	 * @deprecated Slow on Windows - advanced graphics system uses GDI+ on Windows which is not hardware accelerated
	 */
	public void paintOld(GC gc, SWTCanvas canvas) {
		try {
			if (internalScalingFactor!=null) {
				CanvasTransform myT = canvas.getTransformCopy();
				myT.scale(1.0f/internalScalingFactor, 1.0f/internalScalingFactor); // revert internal scaling!
				gc.setTransform(myT);

				gc.drawImage(img, 0, 0); // VERY SLOW ON WINDOWS MACHINES!
				gc.setTransform(canvas.getPersistentTransform());
				myT.dispose();
			}
			else if (img != null && !img.isDisposed()) {
				gc.drawImage(img, 0, 0); // VERY SLOW ON WINDOWS MACHINES!
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void paint(GC gc, SWTCanvas canvas) {
		try {
			if (internalScalingFactor!=null) {
				logger.trace("internalScalingFactor = "+internalScalingFactor);
				CanvasTransform myT = canvas.getTransformCopy();
				myT.scale(1.0f/internalScalingFactor, 1.0f/internalScalingFactor); // revert internal scaling!
				gc.setTransform(myT);

				drawImageFromSeparateGC(gc, canvas);
//				gc.drawImage(img, 0, 0); // VERY SLOW ON WINDOWS (uses GDI+ which is not hardware accelerated)
				gc.setTransform(canvas.getPersistentTransform());
				myT.dispose();
			}
			else if (img != null && !img.isDisposed()) {
				drawImageFromSeparateGC(gc, canvas);
//				gc.drawImage(img, 0, 0); // VERY SLOW ON WINDOWS (uses GDI+ which is not hardware accelerated)
			}
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
