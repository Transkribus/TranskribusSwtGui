package eu.transkribus.swt_canvas.util;

import java.awt.Point;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Rectangle;

import math.geom2d.line.Line2D;

public class GeomUtils {
	
	public static int bound(int v, int min, int max) {
		if (v < min)
			return min;
		else if (v > max)
			return max;
		else
			return v;	
	}
		
	/**
	   * Returns closest point on segment to point
	   * 
	   * @param ss
	   *            segment start point
	   * @param se
	   *            segment end point
	   * @param p
	   *            point to found closest point on segment
	   * @return closest point on segment to p
	   */
	  public static Point getClosestPointOnSegment(Point ss, Point se, Point p)
	  {
	    return getClosestPointOnSegment(ss.x, ss.y, se.x, se.y, p.x, p.y);
	  }

	  /**
	   * Returns closest point on segment to point
	   * 
	   * @param sx1
	   *            segment x coord 1
	   * @param sy1
	   *            segment y coord 1
	   * @param sx2
	   *            segment x coord 2
	   * @param sy2
	   *            segment y coord 2
	   * @param px
	   *            point x coord
	   * @param py
	   *            point y coord
	   * @return closets point on segment to point
	   */
	  public static Point getClosestPointOnSegment(int sx1, int sy1, int sx2, int sy2, int px, int py)
	  {
	    double xDelta = sx2 - sx1;
	    double yDelta = sy2 - sy1;

	    if ((xDelta == 0) && (yDelta == 0)) { // segment is a point -> return this point as it must be the closest one!
	    	return new Point(sx1, sy1);
//	      throw new IllegalArgumentException("Segment start equals segment end");
	    }

	    double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

	    final Point closestPoint;
	    if (u < 0)
	    {
	      closestPoint = new Point(sx1, sy1);
	    }
	    else if (u > 1)
	    {
	      closestPoint = new Point(sx2, sy2);
	    }
	    else
	    {
	      closestPoint = new Point((int) Math.round(sx1 + u * xDelta), (int) Math.round(sy1 + u * yDelta));
	    }

	    return closestPoint;
	  }
	
	/**
	 * Returns the distance and the closest segment of a point (x,y) to a polygon given as a series of points
	 * @param isClosedShape True if this is a closes polygon or false if its a polyline
	 */
	public static Pair<Double, java.awt.geom.Line2D.Double> 
		getDistToPolygonAndClosestSegment(List<Point> pts, double x, double y, boolean isClosedShape) {
		double minDist = Integer.MAX_VALUE;
		java.awt.geom.Line2D.Double minLine = new java.awt.geom.Line2D.Double(0, 0, 0, 0);
		
		int N = isClosedShape ? pts.size() : pts.size()-1;
		
		for (int i=0; i<N; ++i) {
			java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double(pts.get(i), pts.get( (i+1) % pts.size() ));
			double d = line.ptSegDistSq(x, y);
//			logger.debug("d = "+d);
			if (d < minDist) {
				minDist = d;
				minLine = line;
			}
		}

		return Pair.of(minDist, minLine);
	}
	
	public static Point intersection(Line2D l1, Line2D l2) {
		return GeomUtils.intersection((int)l1.getX1(), (int)l1.getY1(), (int)l1.getX2(), (int)l1.getY2(),
				(int)l2.getX1(), (int)l2.getY1(), (int)l2.getX2(), (int)l2.getY2());
	}

	/**
	 * Computes the intersection between two lines. The calculated point is
	 * approximate, since integers are used. If you need a more precise result,
	 * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
	 * license). http://www.ahristov.com
	 * 
	 * @param x1
	 *            Point 1 of Line 1
	 * @param y1
	 *            Point 1 of Line 1
	 * @param x2
	 *            Point 2 of Line 1
	 * @param y2
	 *            Point 2 of Line 1
	 * @param x3
	 *            Point 1 of Line 2
	 * @param y3
	 *            Point 1 of Line 2
	 * @param x4
	 *            Point 2 of Line 2
	 * @param y4
	 *            Point 2 of Line 2
	 * @return Point where the segments intersect, or null if they don't
	 */
	public static Point intersection(int x1, int y1, int x2, int y2, int x3,
			int y3, int x4, int y4) {
		int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d == 0)
			return null;

		int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2)
				* (x3 * y4 - y3 * x4))
				/ d;
		int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2)
				* (x3 * y4 - y3 * x4))
				/ d;

		return new Point(xi, yi);
	}

}
