package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import eu.transkribus.swt_gui.canvas.SWTCanvas;

public interface ICanvasShape extends Comparable<ICanvasShape>, Shape, ITreeNode<ICanvasShape> {	
	static final int MOVE_LINE_THRESH = 5;
	
	ICanvasShape copy();
	
	String getType();
	
	void draw(SWTCanvas canvas, GC gc);
	
	int getX();
	int getY();

	/** Find the index at which to insert the given point. **/
	int getInsertIndex(int x, int y);
	/** Returns the indices of the nPts number of closest points to (x,y) **/
	int[] getClosestPtsIndices(int x, int y, int nPts);
	
	/** Returns the point indices of the line of this shape thats is closes to the given point (x,y) **/
	int[] getClosestLineIndices(int x, int y);
	/** Returns the line of this shape thats is closes to the given point (x,y) **/
	Line2D getClosestLine(int x, int y);
	/** Returns the index of the given point (x,y), -1 if this is not a point of this shape. **/
	int getPointIndex(int x, int y);
	/** Returns the index of the given point with some threshold, -1 if there is no such point. **/
	int getPointIndex(int x, int y, int threshold);
	
	default int[] whichLine(int x, int y) {
		Pair<int[], Double> p = CanvasShapeUtil.getClosestLineIndices(x, y, getPoints(), isClosed());
		if (p.getRight() <= MOVE_LINE_THRESH) {
			return p.getLeft();
		}
		return null;
	}

	/** Determines which corner of the bounding box this point belongs to. Returns RectCorner.NONE if none. **/
	RectDirection whichDirection(int x, int y, int threshold);
	
	Point getCornerPoint(RectDirection rc);
	
	List<java.awt.Point> getPoints();
	java.awt.Point getPoint(int i);
	int [] getPointArray();
	int getNPoints();
	boolean hasPoint(int i);
	
//	public void setX(int x);
//	public void setY(int y);
	
	boolean isVisible();
	void setVisible(boolean visible);
	
	boolean isBaselineVisible();
	void setBaselineVisibiliy(boolean showReadingOrder);
	
	boolean isSelected();
	void setSelected(boolean selected);
	
	boolean isEditable();
	void setEditable(boolean editable);
	
	boolean isSelectable();
	void setSelectable(boolean selectable);
	
	Object getData();
	void setData(Object data);
	
	double distanceToBoundingBox(double x, double y);
	double distance(double x, double y, boolean signed);
	double distanceToCenter(ICanvasShape s);
	public Pair<Double, Point> distanceAndClosestPoint(double x, double y, boolean signedDist);
		
	boolean hasShapeType(Class<Shape> clazz);
	boolean hasDataType(Class<?> clazz);
	
	boolean isClosed();
	long getSelectedTime();
	
	boolean isClosedShape();
	
	// edit operations:
	boolean setPoints(List<Point> pts);
	default boolean setPoints2D(Collection<math.geom2d.Point2D> ptsIn) {
		List<java.awt.Point> pts = new ArrayList<>();
		for (math.geom2d.Point2D p : ptsIn)
			pts.add(new Point((int)p.x(), (int)p.y()));
		
		return setPoints(pts);
	}
	
	/** Moves the shape by the given translation **/
	boolean translate(int tx, int ty);
	
	Point movePoint(int index, int x, int y);
	
	default boolean translatePoint(int index, int tx, int ty) {
		Point p = getPoint(index);
		if (p == null)
			return false;
		
		movePoint(index, p.x+tx, p.y+ty);
		return true;
	}
	
	void movePoints(int x, int y, Integer... pts);
	List<Integer> movePointAndSelected(int grabbedPtIndex, int x, int y);
	
	void moveBoundingBox(RectDirection direction, int newX, int newY);
	int insertPoint(int x, int y);
	boolean canInsert();
	boolean isPointRemovePossible(int i);
	boolean removePoint(int i);
	boolean canRemove();
	
	Color getColor();
	void setColor(Color color);
	
	/** True if some point of the given shape o is inside lies inside this shape. */
	boolean isSomePointInside(ICanvasShape o);
	
	/** Returns the bounding box as a CanvasPolygon object. */
	default CanvasPolygon getBoundsPolygon() {
		return new CanvasPolygon(getBoundsPoints());
	}
	
	/** Returns the points of the boundary rectangle, starting from upper left corner in counter clockwise orientation */
	default List<Point> getBoundsPoints() {
		Rectangle r = getBounds();
		List<Point> pts = new ArrayList<>();
		pts.add(new Point(r.x, r.y));
		pts.add(new Point(r.x, r.y+r.height));
		pts.add(new Point(r.x+r.width, r.y+r.height));
		pts.add(new Point(r.x+r.width, r.y));
		return pts;
	}
	
	default void simplifyToBounds() {
		List<Point> pts = getBoundsPoints();
		setPoints(pts);
	}
	
//	Rectangle getBounds();
	
	/** Simplifies this shape with the given epsilon parameter using the Ramer-Douglas-Peucker algorithm */
	void simplify(double eps);
//	List<ICanvasShape> splitShape(CanvasPolyline line);
	
	boolean contains(java.awt.Point p);
	boolean contains(org.eclipse.swt.graphics.Point p);
	
	java.awt.Point computeCenter();

//	default java.awt.Point computeCenter() {
//		Point ct = new Point(0, 0);
//		for (Point pt : getPoints()) {
//			ct.x += pt.x;
//			ct.y += pt.y;
//		}
//		ct.x /= getNPoints();
//		ct.y /= getNPoints();
//		
//		return ct;
//	}
	
	/** Returns the nesting level of this shape. Is also used for comparing shapes. */
	int getLevel();
	void setLevel(int level);
	
	double area();
	List<math.geom2d.Point2D> getPoints2D();
	double intersectionArea(ICanvasShape shape);
	
	default List<ShapePoint> intersectionPoints(Point p1, Point p2, boolean extendLine) {
		return intersectionPoints(p1.x, p1.y, p2.x, p2.y, extendLine);
	}
	
	default List<ShapePoint> intersectionPoints(int x1, int y1, int x2, int y2, boolean extendLine) {
		List<Point> pts = getPoints();
		
		math.geom2d.line.LinearElement2D lGiven = null;
		if (!extendLine)
			lGiven = new math.geom2d.line.LineSegment2D(x1, y1, x2, y2);
		else
			lGiven = new math.geom2d.line.StraightLine2D(x1, y1, x2-x1, y2-y1);
		
		List<ShapePoint> ipts = new ArrayList<>();
		
		int N = isClosedShape() ? pts.size() : pts.size()-1;
		for (int i=0; i<N; ++i) {
			int iNext = (i+1) % pts.size();
			math.geom2d.line.Line2D l = new math.geom2d.line.Line2D((int)pts.get(i).getX(), (int)pts.get(i).getY(),
					(int)pts.get(iNext).getX(), (int)pts.get(iNext).getY());
			
			math.geom2d.Point2D pt = lGiven.intersection(l);
			if (pt!=null) {
				ipts.add(new ShapePoint(pt.getAsInt(), i));
			}
		}
		
		return ipts;
	}
	
	default List<ShapePoint> intersectionPoints(CanvasPolyline pl, boolean extendLine) {
		CanvasPolyline ipl = pl;
		if (extendLine) {
			final int extDist = (int) 1e6;
			ipl = pl.extendAtEnds(extDist);
		}
		
		List<ShapePoint> ipts = new ArrayList<>();
		for (int i=0; i<ipl.getNPoints()-1; ++i) {
			Point p1 = ipl.getPoint(i);
			Point p2 = ipl.getPoint(i+1);
			
			ipts.addAll(intersectionPoints(p1.x, p1.y, p2.x, p2.y, false));
		}
		
		return ipts;
	}
	
	Pair<ICanvasShape, ICanvasShape> splitByPolyline(CanvasPolyline pl);
	
	default Pair<ICanvasShape, ICanvasShape> splitByVerticalLine(int x) {
		return splitByLine(x, -1, x, 1); // split along vertical line (i.e. horizontal splitting)
	}
	
	default Pair<ICanvasShape, ICanvasShape> splitByHorizontalLine(int y) {
		return splitByLine(-1, y, 1, y); // split along horizontal line (i.e. vertical splitting)
	}
	
	default Pair<ICanvasShape, ICanvasShape> splitByLine(int x1, int y1, int x2, int y2) {
		return splitByPolyline(new CanvasPolyline(new Point(x1, y1), new Point(x2, y2)));
	}
	
	ICanvasShape merge(ICanvasShape shape);

	int compareByLevelAndYXCoordinates(ICanvasShape arg0);
	
	void selectPoint(int ptIndex, boolean sendSignal, boolean multiselect);
	Set<Integer> getSelectedPoints();
	void drawSelectedPoint(SWTCanvas canvas, GC gc, int ptIndex, boolean isMouseOver);
	void clearSelectedPoints();
	void selectPoints(Rectangle rect, boolean sendSignal, boolean multiselect);
	void selectPoints(int x, int y, int w, int h, boolean sendSignal, boolean multiselect);
	boolean isPointSelected(int ptIndex);
	void deselectPoint(int ptIndex, boolean sendSignal);	
	
	/** Adds a point to the end of the list */
	void addPoint(int x, int y);

	void createReadingOrderShape(SWTCanvas canvas, boolean r, boolean l, boolean w, boolean hasBaseline);
	void updateReadingOrderShapeWidth(int newWidth);
	
	Ellipse2D getReadingOrderCircle();
	void showReadingOrder(boolean contains);
	boolean isReadingOrderVisible();

}
