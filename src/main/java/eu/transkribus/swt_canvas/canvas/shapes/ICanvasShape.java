package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import math.geom2d.Point2D;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import eu.transkribus.swt_canvas.canvas.SWTCanvas;

public interface ICanvasShape extends Comparable<ICanvasShape>, Shape, ITreeNode<ICanvasShape> {
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
	
//	boolean isShowReadingOrder();
//	void setShowReadingOrder(boolean showReadingOrder);
	
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
	boolean setPoints2D(Collection<Point2D> ptsIn);
	/** Moves the shape by the given translation **/
	boolean translate(int tx, int ty);
	
	Point movePoint(int index, int x, int y);
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
	CanvasPolygon getBoundsPolygon();
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
	List<java.awt.Point> intersectionPoints(int x1, int y1, int x2, int y2, boolean extendLine);
	List<java.awt.Point> intersectionPoints(CanvasPolyline pl, boolean extendLine);
	
	Pair<ICanvasShape, ICanvasShape> splitShape(CanvasPolyline pl);
	Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2);
	Pair<ICanvasShape, ICanvasShape> splitShapeHorizontal(int x);
	Pair<ICanvasShape, ICanvasShape> splitShapeVertical(int y);
	ICanvasShape mergeShapes(ICanvasShape shape);

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
