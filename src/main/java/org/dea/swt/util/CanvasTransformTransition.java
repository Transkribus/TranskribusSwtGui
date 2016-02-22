package org.dea.swt.util;

import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dea.swt.canvas.SWTCanvas;

/**
 * This class performs a smooth transition between two given affine transformations
 */
public class CanvasTransformTransition {
	private final static Logger logger = LoggerFactory.getLogger(CanvasTransformTransition.class);
	
	SWTCanvas canvas;
	CanvasTransform start, end;
	
	Timer timer;
	float scaleXDelta, scaleYDelta;
	float transXDelta, transYDelta;
	float rotDelta;
	
	final int period = 10; // time period between two frames
	final int N = 25; // nr of iterations in the transition
	
	/**
	 * Constructs a new Transition object from one transformation into another
	 * @param canvas The corresponding canvas object
	 * @param start The start transformation. This transformation is changed according to the given end transformation.
	 * @param end The end transformation to which the start transformation is iteratively changed.
	 */
	public CanvasTransformTransition(SWTCanvas canvas, CanvasTransform start, CanvasTransform end) {
		this.canvas = canvas;
		this.start = start;
		this.end = end;
		
		logger.debug("startScaleX = "+start.getScaleX()+", endScaleX = "+end.getScaleX());
		logger.debug("startTransX = "+start.getTranslateX()+", endTransX = "+end.getTranslateX());
		logger.debug("startTransY = "+start.getTranslateY()+", endTransY = "+end.getTranslateY());
		logger.debug("startAngle = "+start.getAngleDeg()+", endAngle = "+end.getAngleDeg());
		
		computeDeltas(N);
		
		logger.debug("scaleXDelta = "+scaleXDelta+", scaleYDelta = "+scaleYDelta);
		logger.debug("transXDelta = "+transXDelta+", transYDelta = "+transYDelta);
		logger.debug("rotDelta = "+rotDelta);
	}
	
	/**
	 * Recomputes delta values for translation, rotation and scaling according to a given number of steps N
	 */
	private void computeDeltas(int N) {
		scaleXDelta = (float) Math.pow(end.getScaleX() / start.getScaleX(), 1.0d/N);
		scaleYDelta = (float) Math.pow(end.getScaleY() / start.getScaleY(), 1.0d/N);
		
		transXDelta = (end.getTranslateX() - start.getTranslateX()) / N;
		transYDelta = (end.getTranslateY() - start.getTranslateY()) / N;
		
		rotDelta = (end.getAngleDeg() - start.getAngleDeg()) / N;
	}
	
	public void startTransition() {
		canvas.getDisplay().timerExec(period, new Runnable() {
			int c=0; // iteration counter
			@Override
			public void run() {			
//				logger.debug("it "+(c+1)+": scale before: " +start.getScaleX()+ " x "+start.getScaleY());
				start.scale(scaleXDelta, scaleYDelta);
				
//				float tx = start.getTranslateX(), ty = start.getTranslateY();
//				float sx = start.getScaleX(), sy = start.getScaleY();
//				start.setTranslation(0, 0);
//				start.scale(1.0f/sx, 1.0f/sy);
//				logger.debug("it "+(c+1)+": trans before1: " +start.getTranslateX()+ " x "+start.getTranslateY());
				start.rotate(rotDelta);
//				start.rotateCenter(canvas.getScene().getCenter().x, canvas.getScene().getCenter().y, rotDelta);
//				start.setTranslation(tx, ty);
//				start.scale(sx, sy);
//				logger.debug("it "+(c+1)+": trans after1: " +start.getTranslateX()+ " x "+start.getTranslateY());
				
//				logger.debug("it "+(c+1)+": scale after: " +start.getScaleX()+ " x "+start.getScaleY());
//				float[] pts = start.inverseTransform(transXDelta, transYDelta);
				start.translate(transXDelta/start.getScaleX(), transYDelta/start.getScaleY());
//				start.translate(pts[0], pts[1]);
//				logger.debug("it "+(c+1)+": trans after: " +start.getTranslateX()+ " x "+start.getTranslateY());
				
//				start.rotateCenter(canvas.getScene().getCenter().x, canvas.getScene().getCenter().y, rotDelta);
//				logger.debug("it "+(c+1)+": rot after: "+start.getAngleDeg());
				canvas.redraw();
				c++;
				
				if (c<N) {
					computeDeltas(N-c);
					canvas.getDisplay().timerExec(period, this);
				}
				else {
					logger.debug("Start now: "+start.toString());
					logger.debug("End: "+end.toString());
					start.copyElements(end);
				}

				canvas.onTransformChanged(start);
			}
		});	
	}
}
