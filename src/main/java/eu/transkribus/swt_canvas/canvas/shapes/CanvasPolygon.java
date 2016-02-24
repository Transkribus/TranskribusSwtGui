package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.eclipse.swt.graphics.GC;

import eu.transkribus.core.util.PrimaUtils;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;

/**
 * A drawable polygon class inherited from ADrawableShape. The underlying shape class is set to be java.awt.Polygon
 * @author sebastianc
 *
 * @param <D> The Type of the data object
 */
public class CanvasPolygon extends ACanvasShape<java.awt.Polygon> {

//	public CanvasPolygon(java.awt.Polygon polygon) {
//		super(polygon);
//	}
	
	public CanvasPolygon(List<Point> pts) {
		setPoints(pts);
	}	
	
	public CanvasPolygon(Collection<Point2D> ptsIn) {
		setPoints2D(ptsIn);
	}
	
	public CanvasPolygon(String points) throws Exception {
		setPoints(PrimaUtils.parsePoints(points));
	}
	
	public CanvasPolygon(CanvasPolygon src) {
		super(src);
	}
	
	@Override public String getType() {
		return "POLYGON";
	}
			
	@Override public boolean setPoints(List<Point> pts) {
		java.awt.Polygon poly = new java.awt.Polygon();
		
		for (Point p : pts) {
			poly.addPoint(p.x, p.y);
		}
		setAwtShape(poly);
		
		setChanged();
		notifyObservers();
		
		return true;
	}
		
	@Override public List<java.awt.Point> getPoints() {
		List<java.awt.Point> pts = new ArrayList<java.awt.Point>();
		for (int i=0; i<awtShape.npoints; ++i) {
			pts.add(new java.awt.Point(awtShape.xpoints[i], awtShape.ypoints[i]));
		}
		return pts;
	}
	
	@Override
	public List<math.geom2d.Point2D> getPoints2D() {
		List<math.geom2d.Point2D> pts = new ArrayList<>();
		for (int i=0; i<awtShape.npoints; ++i) {
			pts.add(new math.geom2d.Point2D(awtShape.xpoints[i], awtShape.ypoints[i]));
		}
		return pts;
	}	
	
	@Override
	public java.awt.Point getPoint(int i) {
		if (!hasPoint(i))
			return null;
		
		return new Point(awtShape.xpoints[i], awtShape.ypoints[i]);
	}
	
	@Override
	public int getNPoints() {
		return awtShape.npoints;
	}
	
	@Override
	public double area() {
		return SimplePolygon2D.create(getPoints2D()).area();
	}
		
	@Override
	public boolean move(int tx, int ty) {
		awtShape.translate(tx, ty);
		
//		for (int i=0; i<awtShape.npoints; ++i) {
//			awtShape.xpoints[i] += tx;
//			awtShape.ypoints[i] += ty;
//		}
				
		setChanged();
		notifyObservers();
		
//		List<Point> newPts = getPoints();
//		for (Point p : newPts) {
//			p.setLocation(p.x+tx, p.y+ty);
//		}
//		setPoints(newPts);
		return true;
	}
		
	@Override
	public boolean isClosed() {
		return true;
	}
	
	@Override
	public boolean isPointRemovePossible(int i) { 
		if (getNPoints() < 4) return false;
		if (i < 0 || i >= getNPoints()) return false;
		
		return true;
	}
		
	@Override
	public boolean isClosedShape() { return true; }
	
	@Override public void addPoint(int x, int y) {
		awtShape.addPoint(x, y);
		setChanged();
		notifyObservers();
	}


}
