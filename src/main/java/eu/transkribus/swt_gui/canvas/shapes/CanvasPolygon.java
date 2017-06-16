package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

import eu.transkribus.core.util.GeomUtils;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.core.util.PointStrUtils.PointParseException;
import math.geom2d.Point2D;
import math.geom2d.polygon.SimplePolygon2D;

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
	
	public CanvasPolygon(java.awt.Rectangle rect) {
		this();
		
		List<Point> pts = new ArrayList<>();
		pts.add(new Point(rect.x, rect.y));
		pts.add(new Point(rect.x+rect.width, rect.y));
		pts.add(new Point(rect.x+rect.width, rect.y+rect.height));
		pts.add(new Point(rect.x, rect.y+rect.height));
		
		setPoints(pts);
	}
	
	public CanvasPolygon(Rectangle r) {
		this(new java.awt.Rectangle(r.x, r.y, r.width, r.height));
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
		java.awt.Polygon poly = GeomUtils.createPolygon(pts, true);
		
//		java.awt.Polygon poly = new java.awt.Polygon();
//		
//		Point lastPt=null;
//		for (Point p : pts) {
//			if (lastPt != null && p.equals(lastPt)) {
//				continue;
//			}
//			
//			poly.addPoint(p.x, p.y);
//			lastPt = p;
//		}
		
		setAwtShape(poly);
		
		setChanged();
		notifyObservers();
		
		return true;
	}
	
	public List<java.awt.Point> getPoints(boolean removeSucceedingEqualPts) {
		return GeomUtils.getPoints(awtShape, removeSucceedingEqualPts);
	}
			
	@Override public List<java.awt.Point> getPoints() {
		return getPoints(false);
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
