package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;

import org.eclipse.swt.graphics.GC;

import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.core.util.PointStrUtils.PointParseException;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;

/**
 * A drawable polygon class inherited from ADrawableShape. The underlying shape class is set to be java.awt.Polygon
 * @author sebastianc
 *
 * @param <D> The Type of the data object
 */
public class CanvasPolygon extends ACanvasShape<java.awt.Polygon> {
	
	protected CanvasPolygon() {}

//	public CanvasPolygon(java.awt.Polygon polygon) {
//		super(polygon);
//	}
	
	public CanvasPolygon(List<Point> pts) {
		setPoints(pts);
	}	
	
	public CanvasPolygon(Collection<Point2D> ptsIn) {
		setPoints2D(ptsIn);
	}
	
	public CanvasPolygon(String points) throws PointParseException {
		setPoints(PointStrUtils.parsePoints(points));
	}
	
	public CanvasPolygon(CanvasPolygon src) {
		super(src);
	}
	
	@Override public CanvasPolygon copy() {
		return new CanvasPolygon(this);
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
	public boolean translate(int tx, int ty) {
		awtShape.translate(tx, ty);
		
		setChanged();
		notifyObservers();

		return true;
	}
		
	@Override
	public boolean isClosed() {
		return true;
	}
	
	@Override
	public boolean isPointRemovePossible(int i) {
		return (getNPoints() > 3 && i>=0 && i<getNPoints());
	}
		
	@Override
	public boolean isClosedShape() { return true; }
	
	@Override public void addPoint(int x, int y) {
		awtShape.addPoint(x, y);
		setChanged();
		notifyObservers();
	}


}
