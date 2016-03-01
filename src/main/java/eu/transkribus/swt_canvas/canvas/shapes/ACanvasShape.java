package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.plutext.jaxb.svg11.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.GeomUtils;
import eu.transkribus.swt_canvas.util.RamerDouglasPeuckerFilter;
import eu.transkribus.swt_canvas.util.SWTUtil;
import math.geom2d.Vector2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;
import math.geom2d.polygon.convhull.ConvexHull2D;
import math.geom2d.polygon.convhull.JarvisMarch2D;

/**
 * Abstract shape class
 * S is the type of the underlying awt shape
 * D is the type of the data that can be stored with each object
 */
public abstract class ACanvasShape<S extends Shape> extends Observable implements ICanvasShape {
	private static Logger logger = LoggerFactory.getLogger(ACanvasShape.class);
	
	protected long selectedTime=-1;
	protected S awtShape;
	protected boolean editable=false;
	protected boolean visible=true;
	protected boolean RoVisible=false;
	protected boolean RoSelected=false;
	protected boolean selected=false;
	protected boolean selectable=true;
//	protected boolean showReadingOrder=false;
	protected Color color = CanvasSettings.DEFAULT.getDrawColor();
	protected int level=0;
	
	protected Set<Integer> selectedPoints = new HashSet<Integer>();
	
	protected ICanvasShape parent=null;
	protected List<ICanvasShape> children=new ArrayList<ICanvasShape>();	
	
	public Ellipse2D readingOrderCircle = null;
	
		
	/** A generic data object can be associated with each shape **/ 
	protected Object data=null;
	
	public ACanvasShape() {
	}
	
	public ACanvasShape(ACanvasShape<S> src) {		
		this.selectedTime = src.selectedTime;
		this.editable = src.editable;
		this.visible = src.visible;
		this.selected = src.selected;
		this.selectable = src.selectable;
		this.color = src.color;
		this.level = src.level;
				
		this.setPoints(src.getPoints());
		this.setData(src.getData());
		
		this.parent = src.parent;
		this.children = new ArrayList<ICanvasShape>(src.children);		
	}	
		
//	public ACanvasShape(S awtShape) {
//		setAwtShape(awtShape);
//	}
	
	public void setAwtShape(S awtShape) { this.awtShape = awtShape; }
	public Shape getAwtShape() { return awtShape; }
		
	@Override
	public boolean setPoints2D(Collection<math.geom2d.Point2D> ptsIn) {
		List<java.awt.Point> pts = new ArrayList<>();
		for (math.geom2d.Point2D p : ptsIn)
			pts.add(new Point((int)p.x(), (int)p.y()));
		
		return setPoints(pts);
	}	

	@Override
	public int getPointIndex(int x, int y, int threshold) {
		int i=0;
		for (Point p : getPoints()) {
			if (p.distance(x, y) < threshold)
				return i;
			++i;
		}
		return -1;
	}
	
	@Override
	public java.awt.Point computeCenter() {
		Point ct = new Point(0, 0);
		for (Point pt : getPoints()) {
			ct.x += pt.x;
			ct.y += pt.y;
		}
		ct.x /= getNPoints();
		ct.y /= getNPoints();
		
		return ct;
	}
	
	@Override
	public RectDirection whichDirection(int x, int y, int threshold) {
		Rectangle b = getBounds();
		
		double distNW = Point2D.distance(x, y, b.x, b.y);
		if (distNW < threshold)
			return RectDirection.NW;
		
		double distNE = Point2D.distance(x, y, b.x+b.width, b.y);
		if (distNE < threshold)
			return RectDirection.NE;
		
		double distSE = Point2D.distance(x, y, b.x + b.width, b.y + b.height);
		if (distSE < threshold)
			return RectDirection.SE;
		
		double distSW = Point2D.distance(x, y, b.x, b.y + b.height);
		if (distSW < threshold)
			return RectDirection.SW;
		
		double distN = Line2D.ptSegDist(b.x, b.y, b.x+b.width, b.y, x, y);
		if (distN < threshold)
			return RectDirection.N;
		
		double distE = Line2D.ptSegDist(b.x+b.width, b.y, b.x + b.width, b.y + b.height, x, y);
		if (distE < threshold)
			return RectDirection.E;
		
		double distS = Line2D.ptSegDist(b.x + b.width, b.y + b.height, b.x, b.y + b.height, x, y);
		if (distS < threshold)
			return RectDirection.S;
		
		double distW = Line2D.ptSegDist(b.x, b.y + b.height, b.x, b.y, x, y);
		if (distW < threshold)
			return RectDirection.W;
		
		
		return RectDirection.NONE;
	}
	
	@Override
	public Point getCornerPoint(RectDirection rc) {
		Rectangle b = getBounds();
		if (rc == RectDirection.NW) {
			new Point(b.x, b.y);
		} else if (rc == RectDirection.NE) {
			new Point(b.x+b.width, b.y);
		} else if (rc == RectDirection.SE) {
			new Point(b.x + b.width, b.y + b.height);
		} else if (rc == RectDirection.SW) {
			new Point(b.x, b.y + b.height);
		}
		
		return null;
	}
	
	@Override
	public int getInsertIndex(int x, int y) {
		int[] iz = getClosestLineIndices(x, y);
		
		if (isClosedShape()) { // if this is a closed shape, always insert at index of second point of line
			return iz[1];
		}
		double d1 = getPoint(iz[0]).distance(x, y);
		double d2 = getPoint(iz[1]).distance(x, y);
		double d = getPoint(iz[1]).distance(getPoint(iz[0]));
		
		if (iz[1] == getNPoints()-1 && (d1 > 1.5*d || d1 > 2*d2)) { // this is the last line
			return getNPoints(); // insert after last point
		} else if (iz[0] == 0 && (d2 > 1.5*d || d2 > 2*d1) ) { // this is the first line
			return 0; // insert before first point
		}
		
		return iz[1]; // insert at index of second point of line
	}
		
	@Override
	public Line2D getClosestLine(int x, int y) {		
		int[] iz = getClosestLineIndices(x, y);
		return new Line2D.Double(getPoint(iz[0]), getPoint(iz[1]));
	}
	
	@Override
	public int[] getClosestLineIndices(int x, int y) {
		double minDist = Double.MAX_VALUE;
		int[] iz = new int[2];
		iz[0] = -1; iz[1] = -1;
		
		final int N = isClosedShape() ? getNPoints() : getNPoints()-1;
		for (int i=0; i<N; ++i) {
			int index1 = i;
			int index2 = (i+1) % getPoints().size();
			
			Line2D line = new Line2D.Double(getPoint(index1), getPoint(index2));
			double d = line.ptSegDist(x, y);
//			logger.debug("d = "+d+" minDist = "+minDist);
			if (d < minDist) {
				minDist = d;
				iz[0] = index1;
				iz[1] = index2;
//				minLine = line;
			}
		}
//		return minLine;
		return iz;
	}	
	
//	@Override
//	public Line2D findClosestLine(int x, int y) {
//		double minDist = Double.MAX_VALUE;
//		Line2D minLine = null;
//		
//		final int N = isClosedShape() ? getNPoints() : getNPoints()-1;
//		for (int i=0; i<N; ++i) {
//			Line2D line = new Line2D.Double(getPoints().get(i), getPoints().get((i+1) % getPoints().size()));
//			double d = line.ptSegDist(x, y);
////			logger.debug("d = "+d+" minDist = "+minDist);
//			if (d < minDist) {
//				minDist = d;
//				minLine = line;
//			}
//		}
//		return minLine;
//	}	
	
	@Override 
	public int getPointIndex(Point pt) {
		return getPointIndex(pt.x, pt.y);
	}
	
	@Override
	public int getPointIndex(int x, int y) {
		int i=0; 
		for (Point p : getPoints()) {
			if (p.x == x && p.y == y)
				return i;
			
			++i;
		}
		return -1;
	}
	
	@Override
	public int[] getClosestPtsIndices(final int x, final int y, int nPts) {
		TreeMap<Point, Integer> ptsSorted = new TreeMap<Point, Integer>(new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {
				double d1 = o1.distance(x, y);
				double d2 = o2.distance(x, y);
				return new Double(d1).compareTo(d2);
			}
		});
		
		int i=0;
		for (Point p : getPoints()) {
			ptsSorted.put(p, i);
			++i;
		}

		if (nPts > ptsSorted.size())
			nPts = ptsSorted.size();
		
		i=0;
		int[] ptsIdx = new int[nPts];
		for (Point p : ptsSorted.keySet()) {
			ptsIdx[i] = ptsSorted.get(p);
			if (i+1 == nPts)
				break;
			++i;
		}
		
		return ptsIdx;
	}
	
	@Override
	public int insertPoint(int x, int y) {
		int ii = getInsertIndex(x, y);
		
		List<Point> newPts = new ArrayList<Point>();
		
		int i=0;
		for (Point pt : getPoints()) {
			if (ii == i++) {
				newPts.add(new Point(x, y));
			}
			newPts.add(pt);
		}
		if (ii == getNPoints()) {
			newPts.add(new Point(x,y));
		}
				
		setPoints(newPts);
		return ii;		
		
//		Line2D line = this.getClosestLine(x, y);
//		Point pt1 = new Point((int)line.getP1().getX(), (int)line.getP1().getY());
//		List<Point> newPts = new ArrayList<Point>();
//		boolean found = false;
//		for (Point pt : getPoints()) {
//			newPts.add(pt);
//			if (pt.equals(pt1)) {
//				newPts.add(new Point(x, y));
//				found = true;
//			}
//		}
//		setPoints(newPts);
//		return found;
	}
	
//	@Override
//	public List<java.awt.Point> getPoints() {
//		ArrayList<java.awt.Point> pts = new ArrayList<java.awt.Point>();
//		
//		PathIterator pi = getPathIterator(null);
//		
//		while (!pi.isDone()) {
//			double [] coords = new double[2];
//			int type = pi.currentSegment(coords);
////			logger.debug("coords = "+coords[0]+", "+coords[1]);
////			logger.debug("type = "+type + ", "+type);
//			
//			if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
//				pts.add(new Point((int)coords[0], (int)coords[1]));
//			}
//			else {
//				break;
//			}
//
//			pi.next();
//		}
//		// remove last point if equal to first point:
//		if (pts.size() > 2) {
//			if (pts.get(0).equals(pts.get(pts.size()-1))) {
//				pts.remove(pts.size()-1);
//			}
//		}
//		
//		return pts;
//	}
	
//	@Override
//	public java.awt.Point getPoint(int i) {
//		return getPoints().get(i);
//	}
	
	@Override
	public boolean hasPoint(int i) {
		return i>=0 && i<getNPoints();
	}
	
//	protected void setDrawingColor(SWTCanvas canvas, GC gc) {
//		CanvasSettings sets = canvas.getSettings();
//		if (selected) {
//			gc.setForeground(sets.getSelectedColor());
//			gc.setBackground(sets.getFillColor());
//		}
//		else {
//			gc.setForeground(sets.getDrawColor());
//			gc.setBackground(sets.getFillColor());
//		}
//		
//	}
	
	@Override
	public int [] getPointArray() {
		List<Point> pts = getPoints();
		int [] pointArray = new int[pts.size()*2];
		for (int i=0; i<pts.size(); ++i) {
			pointArray[i*2] = pts.get(i).x;
			pointArray[i*2+1] = pts.get(i).y;
		}
		return pointArray;
	}

	@Override
	public void draw(SWTCanvas canvas, GC gc) {
		CanvasSettings sets = canvas.getSettings();
//		setDrawingColor(canvas, gc);
		final boolean isSel = isSelected();
				
		// TEST: draw normals
		if (false) {
			List<Point> pts = getPoints();
			for (int i=0; i<getPoints().size()-1; ++i) {
				Line2D line = new Line2D.Double(pts.get(i).x, pts.get(i).y, pts.get(i+1).x, pts.get(i+1).y);
				
				Vector2D pt = new Vector2D(pts.get(i).x, pts.get(i).y);
				// NOTE: the upwards normal vector goes down, since the y-coordinate is inverted!!
				Vector2D nCurrentLine = new Vector2D(line.getY2()-line.getY1(), line.getX1()-line.getX2());
				nCurrentLine = nCurrentLine.normalize();
				
				Vector2D ptUp = pt.plus(nCurrentLine.times(20));
				Vector2D ptDown = pt.plus(nCurrentLine.times(-25));
			
				gc.setForeground(Colors.getSystemColor(SWT.COLOR_MAGENTA));
				gc.drawLine((int) pt.x(), (int) pt.y(), (int) ptUp.x(), (int) ptUp.y());
				gc.setForeground(Colors.getSystemColor(SWT.COLOR_DARK_YELLOW));
				gc.drawLine((int) pt.x(), (int) pt.y(), (int) ptDown.x(), (int) ptDown.y());
			}
		}
		// END TEST: draw normals
		
		int [] pointArray = getPointArray();
		gc.setAlpha(sets.getForegroundAlpha());
		if (isSel) { // if selected:
			if (canvas.getScene().isEditFocused(this))
				drawBoundingBox(canvas, gc); // draw bounding box
			gc.setLineWidth(sets.getSelectedLineWidth()); // set selected line with
			gc.setBackground(color); // set background color
		}
		else {
			gc.setLineWidth(sets.getDrawLineWidth());
			gc.setBackground(color);
		}
		gc.setForeground(color);
		gc.setLineStyle(canvas.getSettings().getLineStyle());		
		
		if (isClosed()) {
			if (isSel) { // fill polygon if selected
				gc.setAlpha(sets.getBackgroundAlpha());
				gc.fillPolygon(pointArray);
			}
			
			gc.setAlpha(sets.getForegroundAlpha());
			gc.drawPolygon(pointArray);
		}
		else {
			gc.setAlpha(sets.getForegroundAlpha());
			gc.drawPolyline(pointArray);
		}
		
		if (canvas.getScene().isEditFocused(this)) { // draw corner points if focused
//		if (isSel) {
			gc.setAlpha(sets.getForegroundAlpha());
			if (isEditable()) { 
				drawCornerPoints(canvas, gc);
			}
			if (sets.isDrawSelectedCornerNumbers()) {
				drawCornerPointNumbers(canvas, gc);
			}
			if (!isClosedShape() && sets.isDrawPolylineArcs()) {
				drawDirectionArrows(canvas, gc);
			}
		}
		drawSelectedPoints(canvas, gc);

		// TEST: draw "tube" around polylines
		if (this instanceof CanvasPolyline) {
			CanvasPolygon bp = ((CanvasPolyline) this).getDefaultPolyRectangle4Baseline();
			
			gc.setAlpha(isSelected() ? 75 : 50);
			gc.fillPolygon(bp.getPointArray());
//			bp.draw(canvas, gc);
		}
	}
	
	//Test
//	@Override
//	public void drawReadingOrderShapes(SWTCanvas canvas, GC gc) {
//		CanvasSettings sets = canvas.getSettings();
////		setDrawingColor(canvas, gc);
//		final boolean isSel = isSelected();
//				
//		int [] pointArray = getPointArray();
//		gc.setAlpha(sets.getForegroundAlpha());
//
//		gc.setLineWidth(sets.getDrawLineWidth());
//		gc.setBackground(color);
//		
//		gc.setForeground(color);
//		gc.setLineStyle(canvas.getSettings().getLineStyle());		
//		
//		gc.drawPolyline(pointArray);
//		gc.drawString((String) getData(), getX(), getY());
//	}
	
	public void drawSelectedPoints(SWTCanvas canvas, GC gc) {
		for (Integer i : selectedPoints) {
			drawSelectedPoint(canvas, gc, i, false);
		}
	}
	
	@Override
	public void drawSelectedPoint(SWTCanvas canvas, GC gc, int ptIndex, boolean isMouseOver) {
		CanvasSettings settings = canvas.getSettings();
		gc.setBackground(settings.getSelectedPointColor());
		int radius = settings.getSelectedPointRadius();
		java.awt.Point p = getPoint(ptIndex);
		if (p==null)
			return;
		
		gc.fillOval(p.x-radius, p.y-radius, radius*2, radius*2);
		
		// draw surrounding circle of size 4 times the given radius:
		int rr = radius+3;
		if (isMouseOver)
			gc.setForeground(settings.getMouseOverPointColor());
		else
			gc.setForeground(settings.getSelectedPointColor());
		
		gc.drawOval(p.x - rr, p.y - rr, rr*2, rr*2);
//		gc.drawRectangle(p.x - rr, p.y - rr, rr*2, rr*2);
	}
	
	public void drawBoundingBox(SWTCanvas canvas, GC gc) {
		gc.setForeground(canvas.getSettings().getBoundingBoxColor());
		gc.setLineStyle(canvas.getSettings().getBoundingBoxLineStyle());
		java.awt.Rectangle bounds = getBounds();
		gc.drawRectangle(bounds.x, bounds.y, bounds.width, bounds.height);
	}
	
	public void drawCornerPoints(SWTCanvas canvas, GC gc) {
		
//		float sx = canvas.getPersistentTransform().getScaleX();
//		float sy = canvas.getPersistentTransform().getScaleY();
		
		gc.setAlpha(canvas.getSettings().getForegroundAlpha());
		gc.setBackground(getColor());
		int radius = canvas.getSettings().getSelectedPointRadius();
//		radius = (int) ((float)radius/sx); // FIXME: not working
				
//		radius = 6;
//		canvas.scale(1.0f/sx, 1.0f/sy, 0, 0);
//		logger.debug("scale before: "+canvas.getPersistentTransform().getScaleX());
		for (Point pt : getPoints()) {	
			gc.fillOval(pt.x-radius, pt.y-radius, radius*2, radius*2);
//			drawCornerRect(gc, pt, radius, sx, sy);
		}
//		canvas.scale(sx, sy, 0, 0);
//		logger.debug("scale after: "+canvas.getPersistentTransform().getScaleX());
	}
	
//	private void drawCornerRect(GC gc, Point pt, int radius, float sx, float sy) {
//		radius = 10;
//		radius /= sx;
//		
//		int r2 = radius / 2;
//		gc.drawLine(pt.x-r2, pt.y-r2, pt.x+r2, pt.y-r2);
//		gc.drawLine(pt.x+r2, pt.y-r2, pt.x+r2, pt.y+r2);
//		gc.drawLine(pt.x+r2, pt.y+r2, pt.x-r2, pt.y+r2);
//		gc.drawLine(pt.x-r2, pt.y+r2, pt.x-r2, pt.y-r2);
//		
//		gc.drawLine(pt.x-r2, pt.y-r2, pt.x+r2, pt.y+r2);
//		gc.drawLine(pt.x+r2, pt.y-r2, pt.x-r2, pt.y+r2);
//	}
	
	public void drawCornerPointNumbers(SWTCanvas canvas, GC gc) {
		int i=0;
		gc.setAlpha(canvas.getSettings().getForegroundAlpha());
		gc.setBackground(canvas.getSettings().getFillColor());
		for (Point pt : getPoints()) {
			int radius = canvas.getSettings().getSelectedPointRadius();
			int off = 2;
			gc.drawString(""+(i+1), pt.x+radius+off, pt.y+radius+off);
			++i;
		}
	}
	
	public void drawDirectionArrows(SWTCanvas canvas, GC gc) {
		gc.setAlpha(canvas.getSettings().getForegroundAlpha());
		gc.setBackground(getColor());
		
		List<Point> pts = getPoints();
		
		int N = isClosedShape() ? pts.size() : pts.size()-1;
		for (int i=0; i<N; ++i) {
			Point pt0 = pts.get(i);
			Point pt1 = pts.get( (i+1) % pts.size() );
			
			SWTUtil.drawTriangleArc(gc, pt0.x, pt0.y, pt1.x, pt1.y, 20, 10, true);
		}
	}
	
//	public abstract void draw(GC gc);
	
	@Override
	public int getX() { return awtShape.getBounds().x; };
//	public void setX(int x) { this.x = x; }
//	
	@Override
	public int getY() { return awtShape.getBounds().y; };
//	public void setY(int y) { this.y = y; }
	
	@Override
	public boolean isVisible() { return visible; }
	@Override
	public void setVisible(boolean visible) { 
		this.visible = visible;
		if (!visible) {
			setSelected(false);
		}
	}
	
	@Override
	public boolean isSelected() { return selected; }
	@Override
	public void setSelected(boolean selected) { 
		this.selected = selected; 
		if (selected)
			selectedTime = System.currentTimeMillis();
		else
			selectedTime = -1;
		
		clearSelectedPoints();
	}
	@Override
	public long getSelectedTime() { return selectedTime; }
	
	@Override
	public boolean isEditable() { return editable; }
	@Override
	public void setEditable(boolean editable) { this.editable = editable; }
	
	@Override
	public boolean isSelectable() { return selectable; }
	@Override
	public void setSelectable(boolean selectable) { this.selectable = selectable; }
	
	@Override
	public Object getData() { return data; }
	@Override
	public void setData(Object data) { this.data = data; }
	
	@Override
	public boolean hasShapeType(Class<Shape> clazz) {
		return (awtShape != null) && awtShape.getClass()==clazz;
	}
	
	@Override
	public boolean hasDataType(Class<?> clazz) {
		return (data != null) && (clazz!=null) && clazz.isAssignableFrom(data.getClass());
	}
	
	@Override
	public int compareTo(ICanvasShape arg0) {
		return compareByLevelAndYXCoordinates(arg0);
	}
	
	@Override 
	public int compareByLevelAndYXCoordinates(ICanvasShape arg0) {
		// compare by level:
		int levelCompare = Integer.compare(getLevel(), arg0.getLevel());
		if (levelCompare != 0)
			return levelCompare;
		
		// then by y-coordinate:
		int yCompare = Integer.compare(getY(), arg0.getY());
		if (yCompare != 0)
			return yCompare;
		
		// then by x-coordinate:
		return Integer.compare(getX(), arg0.getX());
	}
		
	/**
	 * Returns the minimal distance to the bounding box of the shape.
	 * If the point is inside the rectangle, a negative distance is returned!
	 */
	@Override
	public double distanceToBoundingBox(double x, double y) {
		double x1 = getBounds().x;
		double x2 = getBounds().x + getBounds().width;
		double y1 = getBounds().y;
		double y2 = getBounds().y + getBounds().height;

		double dx1 = x - x1;
		double dx2 = x - x2;
		double dy1 = y - y1;
		double dy2 = y - y2;

		// test if point is inside or inside the x- or y-ranges:
		if (dx1 * dx2 < 0) { // x is between x1 and x2
			if (dy1 * dy2 < 0) { // (x,y) is inside the rectangle --> return a negative distance!
				return -Math.min(Math.min(Math.abs(dx1), Math.abs(dx2)), Math.min(Math.abs(dy1), Math.abs(dy2)));
			}
			return Math.min(Math.abs(dy1), Math.abs(dy2));
		}
		if (dy1 * dy2 < 0) { // y is between y1 and y2
			// we don't have to test for being inside the rectangle, it's
			// already tested.
			return Math.min(Math.abs(dx1), Math.abs(dx2));
		}
		// point is outside and in any of the four corners, compute the euclidiean distance to the corners and take the min of them:

		return Math.min(Math.min(Point2D.distance(x, y, x1, y1), Point2D.distance(x, y, x2, y2)), Math.min(Point2D.distance(x, y, x1, y2), Point2D.distance(x, y, x2, y1)));

		// double dx = Math.max(Math.abs(getX() - x) - getBounds().getWidth() /
		// 2, 0);
		// double dy = Math.max(Math.abs(getY() - y) - getBounds().getHeight() /
		// 2, 0);
		// return dx * dx + dy * dy;
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	@Override
	public String toString() {
	    final String TAB = "    ";
	    String retValue = "";
	    retValue = "AbstractShape ( "
	        + super.toString() + TAB
	        + "visible = " + this.visible + TAB
	        + "selected = " + this.selected + TAB
	        + "data = " + this.data + TAB
	        + " )";
	
	    return retValue;
	}
	
	@Override
	public boolean contains(Point2D arg0) { return contains(arg0.getX(), arg0.getY()); }
	@Override
	public boolean contains(java.awt.Point p) { return contains(p.x, p.y); }
	@Override
	public boolean contains(org.eclipse.swt.graphics.Point p) { return contains(p.x, p.y); }
	@Override
	public boolean contains(Rectangle2D rect) {
		return this.contains(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		return contains(x, y) && contains(x+w, y+h);
	}
	
	@Override
	public boolean contains(double x, double y) {
		return awtShape.contains(x, y);
	}

	@Override
	public Rectangle getBounds() {
		return awtShape.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		return awtShape.getBounds2D();
	}
	
	@Override
	public CanvasPolygon getBoundsPolygon() {
		Rectangle r = getBounds();
		List<Point> pts = new ArrayList<>();
		pts.add(new Point(r.x, r.y));
		pts.add(new Point(r.x+r.width, r.y));
		pts.add(new Point(r.x+r.width, r.y+r.height));
		pts.add(new Point(r.x, r.y+r.height));
		
		return new CanvasPolygon(pts);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform arg0) {
		return awtShape.getPathIterator(arg0);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform arg0, double arg1) {
		return awtShape.getPathIterator(arg0, arg1);
	}

	@Override
	public boolean intersects(Rectangle2D arg0) {
		return awtShape.intersects(arg0);
	}

	@Override
	public boolean intersects(double arg0, double arg1, double arg2, double arg3) {
		return awtShape.intersects(arg0, arg1, arg2, arg3);
	}
	
	@Override
	public void moveBoundingBox(RectDirection direction, int tx, int ty) {
		List<Point> newPts = getPoints();
		Point ct = computeCenter();
		
		for (Point p: newPts) {
			direction.movePointIfAffected(p, tx, ty, ct.x, ct.y);
		}
		setPoints(newPts);
	}
	
	@Override
	public void movePointAndSelected(int grabbedPtIndex, int x, int y) {
		Point trans = movePoint(grabbedPtIndex, x, y);
		
		if (trans != null) {
			for (Integer i : selectedPoints) {
				if (i!=null && hasPoint(i) && i!=grabbedPtIndex) {
					Point pt = getPoint(i);
					movePoint(i, pt.x+trans.x, pt.y+trans.y);
				}
			}
		}
	}
	
	@Override
	public Point movePoint(int index, int x, int y) {
		if (!hasPoint(index)) {
			return null;
		}
		List<Point> newPts = getPoints();
		Point pt = newPts.get(index);
		Point trans = new Point(x - pt.x, y - pt.y);
		pt.setLocation(x, y);
		setPoints(newPts);
		return trans;
	}
	
	@Override
	public void movePoints(int x, int y, Integer... pts) {
		for (Integer i : pts) {
			if (i!=null && i!=-1)
				movePoint(i, x, y);
		}
	}
	
	@Override
	public boolean removePoint(int i) {
		if (!isPointRemovePossible(i))
			return false;
		
		try {
			List<Point> newPts = getPoints();
			newPts.remove(i);
			setPoints(newPts);
			return true;
		}
		catch (Exception e) {
			logger.error("Error while removing point "+i+" from shape "+this+": "+e.getMessage());
			return false;
		}
	}
	
	@Override
	public boolean canRemove() {
		return true;
	}
	
	@Override
	public boolean canInsert() {
		return true;
	}	
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean isSomePointInside(ICanvasShape o) {
		List<java.awt.Point> pts = o.getPoints();
		for (java.awt.Point p : pts) {
			if (contains(p.x, p.y))
				return true;
		}
		return false;
	}
	
	@Override
	public Pair<Double, Point> distanceAndClosestPoint(double x, double y, boolean signedDist) {
		Pair<Double, java.awt.geom.Line2D.Double> distAndSeg 
			= GeomUtils.getDistToPolygonAndClosestSegment(getPoints(), x, y, isClosedShape());
		double minDist = distAndSeg.getLeft();
		java.awt.geom.Line2D.Double minL = distAndSeg.getRight();
		Point p = GeomUtils.getClosestPointOnSegment((int) minL.x1, (int) minL.y1, (int) minL.x2, (int) minL.y2, (int) x, (int) y); 
				
		return (signedDist && contains(x, y)) ? Pair.of(-1*minDist, p) : Pair.of(minDist, p);
	}
		
//	@Override
	@Override
	public double distance(double x, double y, boolean signedDist) {
		double minDist = GeomUtils.getDistToPolygonAndClosestSegment(getPoints(), x, y, isClosedShape()).getLeft();
		
//		double minDist = Integer.MAX_VALUE;
//		List<Point> pts = getPoints();
//		
//		int N = isClosedShape() ? pts.size() : pts.size()-1;
//		
//		for (int i=0; i<N; ++i) {
//			java.awt.geom.Line2D.Double line = new java.awt.geom.Line2D.Double(pts.get(i), pts.get( (i+1) % pts.size() ));
//			double d = line.ptSegDistSq(x, y);
////			logger.debug("d = "+d);
//			if (d < minDist)
//				minDist = d;
//		}
		
		
		return (signedDist && contains(x, y)) ? -1*minDist : minDist;
	}
	
	@Override
	public void simplify(double eps) {
		List<Point> pts = getPoints();
		
		RamerDouglasPeuckerFilter f = new RamerDouglasPeuckerFilter(eps);
//		RamerDouglasPeuckerFilter f = new RamerDouglasPeuckerFilter(0.0000001);
		List<Point> simpl = f.filter(pts);
		logger.debug("Simplified polygon from "+pts.size()+" to "+simpl.size()+" nr of points!");
		
		this.setPoints(simpl);
	}
	
	@Override
	public int getLevel() { return level; }
	
	@Override
	public void setLevel(int layer) { this.level = layer; }	
	
	@Override
	public double intersectionArea(ICanvasShape shape) {
		ICanvasShape p1 = this instanceof CanvasPolyline ? ((CanvasPolyline)this).getDistancePolyRectangle() : this;
		ICanvasShape p2 = shape instanceof CanvasPolyline ? ((CanvasPolyline)shape).getDistancePolyRectangle() : shape;
		
		Polygon2D i = Polygons2D.intersection(SimplePolygon2D.create(p1.getPoints2D()), SimplePolygon2D.create(p2.getPoints2D()));
		if (i==null)
			return 0;
		else
			return i.area();		
	}
	
	@Override
	public double distanceToCenter(ICanvasShape s) {
		Point c1 = new Point((int)s.getBounds().getCenterX(), (int)s.getBounds().getCenterY());
		Point c2 = new Point((int)getBounds().getCenterX(), (int)getBounds().getCenterY());
		return c1.distance(c2);
	}
	
	@Override
	public Pair<ICanvasShape, ICanvasShape> splitShapeHorizontal(int x) {
		return splitShape(x, -1, x, 1); // split along vertical line (ie horizontal splitting)
	}
	
	@Override
	public Pair<ICanvasShape, ICanvasShape> splitShapeVertical(int y) {
		return splitShape(-1, y, 1, y); // split along horizontal line (ie vertical splitting)
	}
	
	@Override
	public Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2) {
		int nIntersections = intersectionPoints(x1, y1, x2, y2, true).size();
		logger.debug("nr of intersections: "+nIntersections);
		
		// for a closed shape, the nr of intersections shall be 2, otherwise more than two split shapes will be created!
		// for an open shape (e.g. a polyline) the nr of intersections must be 1
		if ( (this.isClosed() && nIntersections!=2) || (!this.isClosed() && nIntersections !=1) ) 
			return null;
		
		List<Point> pts = new ArrayList<Point>();
		pts.add(new Point(x1, y1));
		pts.add(new Point(x2, y2));
		CanvasPolyline pl  = new CanvasPolyline(pts);
		
		final int extDist = (int)1e6;
		pl.extendAtEnds(extDist);
		
		CanvasPolygon pUp = pl.getPolyRectangle(extDist, extDist, 1);
		CanvasPolygon pDown = pl.getPolyRectangle(extDist, extDist, 2);
		
//		Polygon2D pI1 = Polygons2D.intersection(SimplePolygon2D.create(pUp.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));
//		Polygon2D pI2 = Polygons2D.intersection(SimplePolygon2D.create(pDown.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));

		Polygon2D pI1 = Polygons2D.intersection(SimplePolygon2D.create(pDown.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));
		Polygon2D pI2 = Polygons2D.intersection(SimplePolygon2D.create(pUp.getPoints2D()), SimplePolygon2D.create(this.getPoints2D()));
		
		ICanvasShape s1 = CanvasShapeFactory.copyShape(this);		
		s1.setPoints2D(pI1.vertices());
		
		ICanvasShape s2 = CanvasShapeFactory.copyShape(this);
		s2.setPoints2D(pI2.vertices());
						
		return Pair.of(s1, s2);
	}
	
	@Override
	public ICanvasShape mergeShapes(ICanvasShape shape) {
		ConvexHull2D ch = new JarvisMarch2D();
		
		List<math.geom2d.Point2D> pts = new ArrayList<>();
		pts.addAll(shape.getPoints2D());
		pts.addAll(this.getPoints2D());
		
		Polygon2D mergedPoly2D =ch.convexHull(pts);
		
		ICanvasShape merged = CanvasShapeFactory.copyShape(this);
		merged.setPoints2D(mergedPoly2D.vertices());
		
		return merged;
	}
	
	@Override
	public List<java.awt.Point> intersectionPoints(int x1, int y1, int x2, int y2, boolean extendLine) {
		List<Point> pts = getPoints();
		
		math.geom2d.line.LinearElement2D lGiven = null;
		if (!extendLine)
			lGiven = new math.geom2d.line.LineSegment2D(x1, y1, x2, y2);
		else
			lGiven = new math.geom2d.line.StraightLine2D(x1, y1, x2-x1, y2-y1);
		
		List<Point> ipts = new ArrayList<>();
		
		int N = isClosedShape() ? pts.size() : pts.size()-1;
		for (int i=0; i<N; ++i) {
			int iNext = (i+1) % pts.size();
			math.geom2d.line.Line2D l = new math.geom2d.line.Line2D((int)pts.get(i).getX(), (int)pts.get(i).getY(),
					(int)pts.get(iNext).getX(), (int)pts.get(iNext).getY());
			
			math.geom2d.Point2D pt = lGiven.intersection(l);
			if (pt!=null) {
				ipts.add(pt.getAsInt());
			}
		}
		
		return ipts;
	}
	
	public void selectPoints(Rectangle rect, boolean sendSignal, boolean multiselect) {
		for (int i=0; i<getNPoints(); ++i) {
			if (rect.contains(getPoint(i))) {
				selectPoint(i, sendSignal, multiselect);
			}
		}
	}
	
	public void selectPoints(int x, int y, int w, int h, boolean sendSignal, boolean multiselect) {		
		selectPoints(new java.awt.Rectangle(x, y, w, h), sendSignal, multiselect);
	}
	
	@Override public void selectPoint(int ptIndex, boolean sendSignal, boolean multiselect) {
		if (!multiselect)
			clearSelectedPoints();
		
		int nPts = getNPoints();
		if (ptIndex >= 0 && ptIndex < nPts) {
			selectedPoints.add(ptIndex);
			logger.debug("selected pts: "+selectedPoints.size());
			if (sendSignal) {
				// TODO ?? send signal that new point was selected
			}
		} else {
			logger.warn("cannot select pt with invalid index: "+ptIndex+", nr of pts: "+nPts);
		}
	}
	
	@Override public boolean isPointSelected(int ptIndex) {
		return selectedPoints.contains(ptIndex);
	}
	
	@Override public void deselectPoint(int ptIndex, boolean sendSignal) {
		int nPts = getNPoints();
		if (ptIndex >= 0 && ptIndex < nPts) {
			selectedPoints.remove(ptIndex);
			logger.debug("selected pts: "+selectedPoints.size());
			if (sendSignal) {
				// TODO ?? send signal that new point was selected
			}
		} else {
			logger.warn("cannot select pt with invalid index: "+ptIndex+", nr of pts: "+nPts);
		}
	}
		
	@Override public Set<Integer> getSelectedPoints() { return selectedPoints; }
	@Override public void clearSelectedPoints() { selectedPoints.clear(); }
	
	////////////////////////////
	// Tree stuff:	
	@Override
	public boolean hasParent() { return parent!=null; }
	@Override
	public ICanvasShape getParent() { return parent; }
    @Override
	public void setParent(ICanvasShape parent) { 
    	this.parent = parent;
    }
    
    @Override
	public void setParentAndAddAsChild(ICanvasShape parent) {
    	setParent(parent);
    	if (parent != null)
    		parent.addChild(this);
    }
    
    @Override
	public void removeFromParent() {
    	if (parent!=null) {
    		parent.removeChild(this);
    	}
    }

    @Override
	public List<ICanvasShape> getChildren(boolean recursive) {
    	if (!recursive)
    		return children;
    	
    	List<ICanvasShape> childrenRec = new ArrayList<>();
    	for (ICanvasShape s : children) {
    		childrenRec.add(s);
    		childrenRec.addAll(s.getChildren(recursive));
    	}
    	
    	return childrenRec;
    }
    
    @Override
	public void setChildren(List<ICanvasShape> children) {
    	this.children.clear();
    	if (children != null)    		
    		for (ICanvasShape child : children) {
    			addChild(child);
    	}
    }
    
    @Override
	public void addChild(ICanvasShape child) {
    	if (child!=null && !children.contains(child)) {
    		children.add(child);
    	}
    }

    @Override
	public int getNChildren() { return children.size(); }
    
    @Override
	public ICanvasShape getChild(int i) {
    	return (i>=0 && i<children.size()) ? children.get(i) : null;
    }
    
    @Override
	public ICanvasShape getChild(ICanvasShape s) {
    	return getChild(children.indexOf(s));
    }
    @Override
	public boolean hasChild(ICanvasShape s) {
    	return getChild(children.indexOf(s)) != null;
    }    
    
    @Override
	public boolean removeChild(ICanvasShape s) {
    	if (children.remove(s)) {
    		return true;
    	}
    	return false;
    }
    
    @Override
	public void removeChildren() {
    	children.clear();
    }
    
    public void createReadingOrderShape(SWTCanvas canvas, boolean isRegion, boolean isLine, boolean isWord, boolean hasBaseline){
    	
    	CanvasSettings sets = canvas.getSettings();
    	
    	java.awt.Rectangle rec = this.getBounds();
    	
    	int arcWidth = sets.getReadingOrderCircleWidth();
	
		if (isRegion){
			arcWidth = (int) (arcWidth*.9);
		}
		else if (isLine){
			arcWidth = (int) (arcWidth*.6);

		}
		else if (isWord){
			arcWidth = (int) (arcWidth*.5);

		}
		else{
			return;
		}
		
    	
		double xLocation = (rec.x+rec.width/2) - arcWidth/2;
		if (isLine && hasBaseline){
				xLocation = this.getX();						
		}
		double yLocation = (rec.y+rec.height/2) - arcWidth/2;
		
		readingOrderCircle = new Ellipse2D.Double(xLocation, yLocation, arcWidth, arcWidth);
		
    }
    
    public void updateReadingOrderShapeWidth(int width){
    	if(readingOrderCircle != null){
    		readingOrderCircle.setFrame(readingOrderCircle.getX(), readingOrderCircle.getY(), width, width);
    	}
    	
    }
    
    
    
    
    public void showReadingOrder(boolean show){
    		RoVisible = show;    	
    }

	public Ellipse2D getReadingOrderCircle() {
		return readingOrderCircle;
	}

	public boolean isReadingOrderVisible() {
		return RoVisible;
	}

    
//	@Override
//	public boolean isShowReadingOrder() {
//		// TODO Auto-generated method stub
//		return showReadingOrder;
//	}
//
//	@Override
//	public void setShowReadingOrder(boolean showReadingOrder) {
//		this.showReadingOrder = showReadingOrder;
//	}
};
