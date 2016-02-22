package org.dea.swt.canvas;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dea.swt.util.ImgLoader;
import org.dea.swt.util.CanvasTransform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

final public class CanvasImage {
	private final static Logger logger = LoggerFactory.getLogger(CanvasImage.class);
	
	public final float DEFAULT_INTERNAL_SCALING=0.5f;
	public final int N_PIXELS_THRESHOLD_FOR_SCALING = (int) (15 * 1e6);
	
	public Image img;
	public int width;
	public int height;
	public long nPixels;
	
	public Float internalScalingFactor=null;
	
	public CanvasImage(URL url) throws Exception {
		Image imgIn = ImgLoader.load(url);
		if (imgIn==null)
			throw new Exception("Could not load image: "+url);
		
		this.width = imgIn.getBounds().width;
		this.height = imgIn.getBounds().height;
		this.nPixels = this.width * this.height;
		
		this.internalScalingFactor = null;
		if (nPixels > N_PIXELS_THRESHOLD_FOR_SCALING) {
			
			internalScalingFactor = DEFAULT_INTERNAL_SCALING;
			logger.debug("internalScalingFactor = "+internalScalingFactor);
			
			Image imgScaled = new Image(imgIn.getDevice(), 
										Math.round(width*internalScalingFactor),
										Math.round(height*internalScalingFactor));
			
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
			this.img = imgScaled;
		}
		else {
			this.img = imgIn;
		}
	}
	
	public void dispose() {
		if (img!=null && !img.isDisposed())
			img.dispose();
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
				gc.drawImage(img, 0, 0);
				gc.setTransform(canvas.getPersistentTransform());
				myT.dispose();
			}
			else if (img != null && !img.isDisposed())
				gc.drawImage(img, 0, 0);
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
