package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
//import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.GeomUtils;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.core.util.PointStrUtils.PointParseException;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;

//public class CanvasPolyline extends ACanvasShape<java.awt.geom.GeneralPath> {
public class CanvasPolyline extends ACanvasShape<java.awt.Polygon> {
	static Logger logger = LoggerFactory.getLogger(CanvasPolyline.class);
	
	public final static int DIST_CONTAINS_THRESHOLD = 10;
	
//	public CanvasPolyline(java.awt.geom.GeneralPath path) {
//		super(path);
//	}
	
	public CanvasPolyline(List<Point> pts) {
//		Assert.assertTrue(pts.size() >= 2);
		
		setPoints(pts);
	}
	
	public CanvasPolyline(String points) throws PointParseException {
		setPoints(PointStrUtils.parsePoints(points));
	}
	
	public CanvasPolyline(CanvasPolyline src) {
		super(src);
	}
	
	public CanvasPolyline(Point p1, Point p2) {
		List<Point> pts = new ArrayList<>();
		pts.add(p1);
		pts.add(p2);
		setPoints(pts);
	}

	public CanvasPolyline copy() {
		return new CanvasPolyline(this);
	}

//	@Override
//	public Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2) {
//		logger.warn("Splitting not allowed for polylines - returning null!");
//		return null;
//	}
	
	@Override
//	public Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2) {
	public Pair<ICanvasShape, ICanvasShape> splitByPolyline(CanvasPolyline pl) {
		List<Point> pts = getPoints();
		
		List<Point> pts1 = new ArrayList<>();
		List<Point> pts2 = new ArrayList<>();
		boolean intersected=false;
		
		int N = pts.size()-1;
		for (int i=0; i<N; ++i) {
			// first add current point to left or right list:
			if (!intersected) {
				pts1.add(pts.get(i));
			}
			else {
				pts2.add(pts.get(i));
			}
			
			// if no intersection has been found yet, try to find one with the current line segment and add the intersection point if found:
			if (!intersected) {
				int iNext = (i+1) % pts.size();
				
				Point p1 = pts.get(i);
				Point p2 = pts.get(iNext);

				List<ShapePoint> ipts = pl.extendAtEnds(1e4).intersectionPoints(p1, p2, false);
				if (ipts.isEmpty())
					continue;
				else if (ipts.size() > 1) // more than one intersection point with a line segment -> no split possible!
					return null;				

				intersected=true;
				pts1.add(new Point(ipts.get(0).p));
				pts2.add(new Point(ipts.get(0).p));		
			}
		}
		pts2.add(pts.get(N));
		
		if (!intersected)
			return null;
		
		ICanvasShape s1 = this.copy();
		s1.setPoints(pts1);
		ICanvasShape s2 = this.copy();
		s2.setPoints(pts2);
						
		return Pair.of(s1, s2);
		
		///////////////
//		int nIntersections = intersectionPoints(x1, y1, x2, y2, true).size();
//		logger.debug("nr of intersections: "+nIntersections);
//		
//		// for a closed shape, the nr of intersections shall be 2, otherwise more than two split shapes will be created!
//		// for an open shape (e.g. a polyline) the nr of intersections must be 1
//		if ( (this.isClosed() && nIntersections!=2) || (!this.isClosed() && nIntersections !=1) ) 
//			return null;
//		
//		List<Point> pts = new ArrayList<Point>();
//		pts.add(new Point(x1, y1));
//		pts.add(new Point(x2, y2));
//		CanvasPolyline pl  = new CanvasPolyline(pts);
//		
//		final int extDist = (int)1e6;
//		pl.extendAtEnds(extDist);
//		
//		CanvasPolygon pUp = pl.getBufferPolygon(extDist, 1);
//		CanvasPolygon pDown = pl.getBufferPolygon(extDist, 2);
//			
//		Polygon2D pI1 = Polygons2D.intersection(SimplePolygon2D.create(pUp.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));
//		Polygon2D pI2 = Polygons2D.intersection(SimplePolygon2D.create(pDown.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));
//		
//		ICanvasShape s1 = CanvasShapeFactory.copyShape(this);		
//		s1.setPoints2D(pI1.vertices());
//		ICanvasShape s2 = CanvasShapeFactory.copyShape(this);
//		s2.setPoints2D(pI2.vertices());
//						
//		return Pair.of(s1, s2);
	}
	
	@Override
	public ICanvasShape merge(ICanvasShape shape) {
//		logger.warn("Merging not allowed for polylines - returning null!");
//		return null;

		List<Point2D> pts = new ArrayList<Point2D>();
		List<Point2D> pts1 = this.getPoints2D();
		List<Point2D> pts2 = shape.getPoints2D();
		
		if (true) { // "regular" merging of polylines -> takes points of this first, then the one of the given shape
			if (!pts1.isEmpty() && !pts2.isEmpty() && pts1.get(pts1.size()-1).distance(pts2.get(0)) < 2) { // remove first point of new line if they are too close together
				pts2.remove(0);
			}
			
			pts.addAll(pts1);
			pts.addAll(pts2);
		}
		// FIXME this code does not belong here, as it refers to the reading order which is a concept for trp-shapes not canvas-shapes...
		// reading order should be considered outside this function, i.e. sort the shapes by reading order before merging
		else {
			int ro1=0, ro2=1;
			if (this.getTrpShapeType()!=null && this.getTrpShapeType().getParentShape()!=null && shape.getTrpShapeType()!=null && shape.getTrpShapeType().getParentShape()!=null) {
				ro1 = this.getTrpShapeType().getParentShape().getReadingOrderAsInt();
				ro2 = shape.getTrpShapeType().getParentShape().getReadingOrderAsInt();			
			}
			if (ro1 < ro2) {
				pts.addAll(pts1);
				pts.addAll(pts2);
			}
			else{
				pts.addAll(pts2);
				pts.addAll(pts1);	
			}
		}
		
		CanvasPolyline pl = new CanvasPolyline(GeomUtils.toAwtPoints(pts));
		logger.trace("new polyline pts " + pl.getPoints());
		return pl;
	}
	
	public CanvasPolyline extendAtEnds(final double dist) {
		CanvasPolyline extended = new CanvasPolyline(this);
		if (extended.getNPoints() < 2)
			return extended;
		
		List<java.awt.Point> pts = getPoints();
		List<java.awt.Point> extendedPts = new ArrayList<>();
		
		// extend from beginning:		
		java.awt.Point p1 = pts.get(1);
		java.awt.Point p2 = pts.get(0);
		Vector2D v1 = new Vector2D(p2.x - p1.x, p2.y - p1.y);
		v1 = v1.normalize();
		Vector2D newFirstPt = new Vector2D(p2.x, p2.y).plus(v1.times(dist));
		extendedPts.add(new java.awt.Point((int)newFirstPt.x(), (int)newFirstPt.y()));
		
		for (java.awt.Point p : pts) {
			extendedPts.add(new java.awt.Point(p.x, p.y));
		}
		
		// extend at end:
		p1 = pts.get(pts.size()-2);
		p2 = pts.get(pts.size()-1);
		v1 = new Vector2D(p2.x - p1.x, p2.y - p1.y);
		v1 = v1.normalize();
		newFirstPt = new Vector2D(p2.x, p2.y).plus(v1.times(dist));
		extendedPts.add(new java.awt.Point((int)newFirstPt.x(), (int)newFirstPt.y()));

		extended.setPoints(extendedPts);
		return extended;	
	}
		
	@Override
	public String getType() {
		return "POLYLINE";
	}
	
	@Override
	public boolean isClosed() {
		return false;
	}
	
	public CanvasPolygon getDefaultPolyRectangle() {
		int distUp = 45;
		int distDown = 5;
		
		return getPolyRectangle(distUp, distDown, 0);
	}
	
	public CanvasPolygon getDefaultPolyRectangle4Baseline() {
		int distUp = 30;
		int distDown = 5;
		
		return getPolyRectangle(distUp, distDown, 0);
	}
	
	public CanvasPolygon getDistancePolyRectangle() {
		return getPolyRectangle(DIST_CONTAINS_THRESHOLD, DIST_CONTAINS_THRESHOLD, 0);
	}
	
	/** Returns a polyrectangle around this polyline with the distance DIST_CONTAINS_THRESHOLD to each line segment. <br>
	 * type == 0 -> up and down segments are included
	 * tpye == 1 / 2-> only up / down segments are included 
	 *  @fixme Remove type paramter as by specifying distUp or distDown as 0 
	 *  */
	public CanvasPolygon getPolyRectangle(int distUp, int distDown, int type) {
//		List<Point> pts = getPoints();
		List<Point> pts = getPoints(true);
		logger.trace("nr of pts in polyline = "+pts.size());
		
		if (pts.size()==0){
			return null;
		}

		List<Point> ptsUp = new ArrayList<>();
		List<Point> ptsDown = new ArrayList<>();
		
		Vector2D nBefore = new Vector2D(0, 0);
		Vector2D nCurrentLine = null;
		
		Point lastPt = null;
		for (int i=0; i<pts.size()-1; ++i) {
//			Point p1 = pts.get(i);
//			Point p2 = pts.get(i+1);
									
			Line2D line = new Line2D.Double(pts.get(i).x, pts.get(i).y, pts.get(i+1).x, pts.get(i+1).y);
			
			Vector2D pt = new Vector2D(pts.get(i).x, pts.get(i).y);
//			nCurrentLine = new Vector2D(line.getY1()-line.getY2(), line.getX2()-line.getX1());
			// NOTE: the upwards normal vector goes down, since the y-coordinate is inverted in the canvas --> thus we have to invert the normal vector here!
			nCurrentLine = new Vector2D(line.getY2()-line.getY1(), line.getX1()-line.getX2());
						
			nCurrentLine = nCurrentLine.normalize();
			
			Vector2D nBis = nCurrentLine.plus(nBefore);
			nBis = nBis.normalize();

			Vector2D ptUp = pt.plus(nBis.times(distUp));
			Vector2D ptDown = pt.plus(nBis.times(-distDown));
			
			ptsUp.add(new Point( Math.round((float)ptUp.x()), Math.round((float)ptUp.y())) );
			ptsDown.add(new Point( Math.round((float)ptDown.x()), Math.round((float)ptDown.y())) );
			
			nBefore = new Vector2D(nCurrentLine.x(), nCurrentLine.y());
		}
		Vector2D pt = new Vector2D(pts.get(pts.size()-1).x, pts.get(pts.size()-1).y);
		Vector2D ptUp = pt.plus(nBefore.times(distUp));
		Vector2D ptDown = pt.plus(nBefore.times(-distDown));
		ptsUp.add(new Point( Math.round((float)ptUp.x()), Math.round((float)ptUp.y())) );
		ptsDown.add(new Point( Math.round((float)ptDown.x()), Math.round((float)ptDown.y())) );	
		
		List<Point> newPts = new ArrayList<>();
		
		List<Point> firstPts = null, secondPts;

		if (type == 1) { // only up
			firstPts = pts;
			secondPts = ptsUp;
		} else if (type == 2) { // only down
			firstPts = pts;
			secondPts = ptsDown;
		} else { // up and down
			firstPts = ptsUp;
			secondPts = ptsDown;
		} 
		
		// construct buffer polygon:
		for (Point p : firstPts)
			newPts.add(p);
		for (int i=secondPts.size()-1; i>=0; --i)
			newPts.add(secondPts.get(i));
		
		return new CanvasPolygon(newPts);
		
		// does not work...:
//		Polygon2D p = Polyline2D.create(getPoints2D()).buffer(DIST_CONTAINS_THRESHOLD).asPolygon(0);
//		
//		List<Point> pts = new ArrayList<>();
//		for (math.geom2d.Point2D pt : p.vertices()) {
//			pts.add(pt.getAsInt());
//		}
//		
//		CanvasPolygon cp = new CanvasPolygon(pts);
//		
//		return cp;
	}
	
	@Override
	/**
	 * Contains for polylines returns true if min-distance from point to all lines is smaller than some threshold
	 */
	public boolean contains(double x, double y) {
		final boolean USE_DEFAULT_POLY_RECT = true;
		if (!USE_DEFAULT_POLY_RECT) { // the old version using some dist threshold
			List<Point> pts = getPoints();
			
			double minDist = Integer.MAX_VALUE;
			for (int i=0; i<pts.size()-1; ++i) {
				Line2D line = new Line2D.Double(pts.get(i).x, pts.get(i).y, pts.get(i+1).x, pts.get(i+1).y);
				double dist = line.ptSegDist(x, y);
	//			logger.debug("dist to baseline: "+dist);
				if (dist < minDist)
					minDist = dist;
			}
			
			boolean contains = minDist < DIST_CONTAINS_THRESHOLD;
	//		logger.debug("contains of polyline "+this+" contains: "+contains);
			
			return contains;
		} else { // the new version using the default poly rectangle around the polyline
			return getDefaultPolyRectangle4Baseline().contains(x, y);
		}
//		return awtShape.contains(arg0);
	}
	
	@Override
	public boolean setPoints(List<Point> pts) {
		java.awt.Polygon poly = GeomUtils.createPolygon(pts, true);
//		java.awt.Polygon poly = new java.awt.Polygon();
//		
//		for (Point p : pts) {
//			poly.addPoint(p.x, p.y);
//		}
//		
////		GeneralPath path = new GeneralPath(poly);
////		setAwtShape(path);
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
	
	@Override public double area() { return 0; }
	
	@Override
	public int getNPoints() {
		return awtShape.npoints;
	}
	
	@Override
	public java.awt.Point getPoint(int i) {
		if (!hasPoint(i))
			return null;
		
		return new Point(awtShape.xpoints[i], awtShape.ypoints[i]);
	}
	
	@Override
	public boolean translate(int tx, int ty) {
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
	public boolean isPointRemovePossible(int i) {
		if (getNPoints() < 3) return false;
		if (i < 0 || i >= getNPoints()) return false;
		
		return true;
	}
	
	@Override
	public boolean isClosedShape() { return false; }
	
	@Override public void addPoint(int x, int y) {
		awtShape.addPoint(x, y);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Sort points according to given direction vector (must *not* be normalized)
	 */
	public void sortPoints(int x, int y) {
		logger.debug("sorting points according to direction vector x="+x+" y="+y);
		Vector2D v = new Vector2D(x, y);
		v = v.normalize();
		
		SortedMap<Double, Point> pointsMap = new TreeMap<>();
		for (Point p : getPoints()) {			
			Vector2D a = new Vector2D(p.getX(), p.getY());
			double sp = a.dot(v);
			pointsMap.put(sp, p);
		}
		
		// reset points according to sorting in treemap
		List<Point> pts = new ArrayList<>();
		for (Point p : pointsMap.values()) {
			pts.add(p);
		}
		setPoints(pts);
	}


}
