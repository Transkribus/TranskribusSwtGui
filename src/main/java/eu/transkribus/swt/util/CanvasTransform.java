package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.util.MathUtil;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

public class CanvasTransform extends Transform {
	private final static Logger logger = LoggerFactory.getLogger(CanvasTransform.class);
	
	public static final boolean DEFAULT_ADJUST_ANGLE = false;
	
	private float[] els = new float[6];
	
	public CanvasTransform(CanvasTransform transform) {
		super(transform.getDevice());
//		logger.debug("CanvasTransform copy constructor called");
		copyElements(transform);
	}
	
	/**
	 * Copies elements from the given transform to this transform
	 */
	public void copyElements(CanvasTransform newTr) {
		newTr.getElements(els);
		setElements(els);
	}

	public CanvasTransform(Device device) {
		super(device);
//		logger.debug("CanvasTransform device constructor called");
	}
	public CanvasTransform(Device device, float[] elements) {
		super(device, elements);
//		logger.debug("CanvasTransform device constructor with elements called");
	}
	public CanvasTransform(Device device, float m11, float m12, float m21, float m22, float dx, float dy) {
		super(device, m11, m12, m21, m22, dx, dy);
//		logger.debug("CanvasTransform device constructor with single elements called");
	}
	
	public void setElements(float[] values) {
		setElements(values[0], values[1], values[2], values[3], values[4], values[5]);
	}
		
	public void scaleCenter(float dx, float dy, float sx, float sy) {
		translate(dx, dy);
		scale(sx, sy);
		translate(-dx, -dy);
	}
	
	public void rotateCenter(float dx, float dy, float angleDegrees) {
		translate(dx, dy);
		rotate(angleDegrees);
		translate(-dx, -dy);
	}
	
	public float determinant() {
		getElements(els);
		return els[0]*els[3] - els[1]*els[2];
	}
	
	public float getScaleX() {
		getElements(els);		
		return (float) Math.sqrt(els[0]*els[0] + els[1]*els[1]);
	}
	
	public float getScaleY() {
		getElements(els);		
		return (float) Math.sqrt(els[2]*els[2] + els[3]*els[3]);
	}
	
	public float getTranslateX() {
		getElements(els);
		return els[4];
	}
	
	public float getTranslateY() {
		getElements(els);		
		return els[5];
	}
	/**
	 * Sets the translation in x-direction to the given value
	 */
	public void setTranslationX(float dx) {
		getElements(els);
		els[4] = dx;
		setElements(els);
	}
	/**
	 * Sets the translation in y-direction to the given value
	 */	
	public void setTranslationY(float dy) {
		getElements(els);
		els[5] = dy;
		setElements(els);
	}
	/**
	 * Sets the translation to the given values
	 */
	public void setTranslation(float dx, float dy) {
		setTranslationX(dx);
		setTranslationY(dy);
	}
	
	/** 
	 * Returns the current rotation angle in radiants.
	 * @param adjustAngle If true, the angle is returned in the range [0, 2*pi] or in [-pi, pi] elsewise
	 * @return The current rotation angle in radiants.
	 */
	public float getAngleRad(boolean adjustAngle) {
		getElements(els);
		double atan2 = Math.atan2(els[1], els[0]); // returns angle in range -PI, PI
//		logger.debug("atan2 = "+atan2);

		final double eps=1e-5;
		if (adjustAngle && MathUtil.signumEps(atan2, eps)==-1) {
//			logger.debug("signum = -1");
			return (float) (atan2 + 2*Math.PI);
		}
		else
			return (float)atan2;
	}
		
	public float getAngleDeg(boolean adjustAngle) {
		return (float) MathUtil.radToDeg(getAngleRad(adjustAngle));
	}
	
	public float getAngleRad() {
		return getAngleRad(DEFAULT_ADJUST_ANGLE);
	}
	
	public float getAngleDeg() {
		return getAngleDeg(DEFAULT_ADJUST_ANGLE);
	}	
	
	public Point transform(Point pt) {
		
		float [] pts = new float[] {pt.x, pt.y};
		transform(pts);
		return new Point(Math.round(pts[0]), Math.round(pts[1]));
//		return new Point((int)Math.ceil(pts[0]), (int)Math.ceil(pts[1]));
//		return new Point((int)pts[0], (int)pts[1]);
	}
	
	public CanvasPolygon transform(CanvasPolygon p) {
		List<java.awt.Point> newPts = new ArrayList<java.awt.Point>();
		for (java.awt.Point pt : p.getPoints()) {
			newPts.add(transform(pt));
		}
		return new CanvasPolygon(newPts);
	}
	
	public java.awt.Point invertRotation(java.awt.Point p) {
//		logger.debug("rot: "+getAngleDeg());
		CanvasTransform tr = new CanvasTransform(getDevice());
//		tr.setTranslation(-getTranslateX(), -getTranslateY());
		tr.rotate(-getAngleDeg());
//		tr.scale(getScaleX(), getScaleY());
		
		Point tp = tr.transform(new Point(p.x, p.y));
		tr.dispose();
		
		return new java.awt.Point(tp.x, tp.y);
	}
	
	public java.awt.Point inverseTransformWithoutTranslation(java.awt.Point p) {
		return inverseTransformWithoutTranslation(p.x, p.y);
	}
	
	public java.awt.Point inverseTransformWithoutTranslation(int x, int y) {
		CanvasTransform tr = new CanvasTransform(this);
		tr.setTranslation(0, 0);
		tr.invert();
		Point tp = tr.transform(new Point(x, y));
		tr.dispose();
		
		return new java.awt.Point(tp.x, tp.y);
	}

	public java.awt.Point transform(java.awt.Point p) {
		Point swtPt = transform(new Point(p.x, p.y));
		return new java.awt.Point(swtPt.x, swtPt.y);
	}	
	
	public Rectangle transform(Rectangle r) {
		float [] pts = new float[] {r.x, r.y};
		transform(pts);
		
		return new Rectangle((int)pts[0], (int)pts[1], (int) (r.width*getScaleX()), (int) (r.height*getScaleY()));
	}

	public java.awt.Rectangle transform(java.awt.Rectangle r) {
		 Rectangle swtRect = transform(new Rectangle(r.x, r.y, r.width, r.height));
		 return new java.awt.Rectangle(swtRect.x, swtRect.y, swtRect.width, swtRect.height);
	}
	
	/**
	 * Performs and in-place inverse-transformation of the given array of points in x-y space
	 */
	public void inverseTransform(float[] pts) {
		CanvasTransform i = getInvertedCopy();
		i.transform(pts);
		i.dispose();
	}
	
	public float[] inverseTransform(float x, float y) {
		float [] pts = new float[] {x, y};
		inverseTransform(pts);
		return pts;
	}
	
	public Point inverseTransform(Point pt) {
		float [] pts = new float[] {pt.x, pt.y};
		inverseTransform(pts);
		return new Point((int)pts[0], (int)pts[1]);	
	}
	
	public java.awt.Point inverseTransform(java.awt.Point pt) {
		Point swtPt = inverseTransform(new Point(pt.x, pt.y));
		return new java.awt.Point(swtPt.x, swtPt.y);
	}
		
	public Rectangle inverseTransform(Rectangle r) {
		float [] pts = new float[] {r.x, r.y};
		inverseTransform(pts);
	
		return new Rectangle((int)pts[0], (int)pts[1], (int) (r.width/getScaleX()), (int) (r.height/getScaleY()));
	}
	
	public java.awt.Rectangle inverseTransform(java.awt.Rectangle r) {
		 Rectangle swtRect = inverseTransform(new Rectangle(r.x, r.y, r.width, r.height));
		 return new java.awt.Rectangle(swtRect.x, swtRect.y, swtRect.width, swtRect.height);
	}	
	
	/**
	 * Returns the inverted matrix as a new instance. <br>
	 * <i>NOTE: you must dispose the transform after use!!!</i>
	 */
	private CanvasTransform getInvertedCopy() {
		CanvasTransform inv = new CanvasTransform(this);
		inv.invert();
		return inv;
	}
	
	/**
	 * Copies elements from the new transform to the old transform
	 */
	public static void copyElements(CanvasTransform oldTr, CanvasTransform newTr) {
		float[] els = new float[6];
		newTr.getElements(els);
		oldTr.setElements(els);
	}
	
	public boolean hasRotation() {
		return hasRotation(0.1); // eps = 0.1 -> angle has to be at least > 0.1 degress to be regarded as valid rotation
	}
	
	public boolean hasRotation(double eps) {
		return !CoreUtils.equalsEps(getAngleDeg(), 0.0d, eps);
	}

	@Override
	public String toString() {
		String str = "CanvasTransform: [tx/ty ="+getTranslateX()+"/"+getTranslateY();
		str += ", sx/sy = "+getScaleX()+"/"+getScaleY();
		str += ", angle = "+getAngleDeg()+"]";
		return str;
	}
	


}
