package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
//import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasRect extends ACanvasShape<java.awt.Rectangle> {
	private final static Logger logger = LoggerFactory.getLogger(CanvasRect.class);
	
	private CanvasRect() {
		setAwtShape(new java.awt.Rectangle());
	}
	
	public CanvasRect(int x, int y, int width, int height) {
		this();
		setRectValues(x, y, width, height);
	}
	
	public CanvasRect(List<Point> pts) throws CanvasShapeException {
		this();
		if (!setPoints(pts)) {
			throw new CanvasShapeException("Could not create rectangle from set of points - length of point vector must be two; given: "+pts.size());
		}
	}
	
	public CanvasRect(java.awt.Rectangle rect) {
		this();
		setRectValues(rect.x, rect.y, rect.width, rect.height);
	}
	
	public CanvasRect(org.eclipse.swt.graphics.Rectangle rect) {
		this();
		setRectValues(rect.x, rect.y, rect.width, rect.height);
	}
	
	public CanvasRect(CanvasRect rect) {
		super(rect);
	}
	
	public CanvasRect copy() {
		return new CanvasRect(this);
	}
	
	@Override
	public String getType() {
		return "RECTANGLE";
	}
	
	public void setRectValues(int x, int y, int width, int height) {
		awtShape.x = x;
		awtShape.y = y;
		awtShape.width = width;
		awtShape.height = height;
		
		setChanged();
		notifyObservers();
	}
	
//	public DrawableRect(Rectangle rect, D data) {
//		this(rect);
//		this.data = data;
//	}
	
	@Override
	public boolean isClosed() {
		return true;
	}	

//	@Override
//	public void draw(GC gc) {
//		setColor(gc);
//		gc.drawRectangle(awtShape.x, awtShape.y, awtShape.width, awtShape.height);
//	}

	public int getWidth() {
		return awtShape.width;
	}

	public void setWidth(int width) {
		awtShape.width = width;
	}

	public int getHeight() {
		return awtShape.height;
	}

	public void setHeight(int height) {
		awtShape.height = height;
	}
	
	@Override
	public List<Point> getPoints() {
		List<Point> pts = new ArrayList<Point>();
		for (int i=0; i<4; ++i) {
			pts.add(getPoint(i));	
		}
		
		return pts;
	}
	
	@Override
	public List<math.geom2d.Point2D> getPoints2D() {
		List<math.geom2d.Point2D> pts = new ArrayList<>();
		for (int i=0; i<4; ++i) {
			pts.add(new math.geom2d.Point2D(getPoint(i).x, getPoint(i).y));	
		}
		
		return pts;
	}
	
	@Override public double area() { return awtShape.width * awtShape.height; }
	
	@Override
	public int getNPoints() {
		return 4;
	}
	
	@Override
	public java.awt.Point getPoint(int i) {		
		switch (i) {
		case 0:
			return new Point(awtShape.x, awtShape.y);
		case 1:
			return new Point(awtShape.x+awtShape.width, awtShape.y);
		case 2:
			return new Point(awtShape.x+awtShape.width, awtShape.y+awtShape.height);
		case 3:
			return new Point(awtShape.x, awtShape.y+awtShape.height);
		}
		
		return null;
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
	    retValue = "RectShape ( "
	        + super.toString() + TAB
	        + "width = " + awtShape.width + TAB
	        + "height = " + awtShape.height + TAB
	        + " )";
	
	    return retValue;
	}
	
	@Override
	public boolean setPoints(List<Point> pts) {
		if (pts.size() != 2)
			return false;
		
		Point p1 = pts.get(0);
		Point p2 = pts.get(1);
		int x = p1.x < p2.x ? p1.x : p2.x;
		int y = p1.y < p2.y ? p1.y : p2.y;
		int w = Math.abs(p1.x - p2.x);
		int h = Math.abs(p1.y - p2.y);
		setRectValues(x,y,w,h);
		return true;
	}
	
	@Override
	public boolean translate(int tx, int ty) {
		setRectValues(getX()+tx, getY()+ty, getWidth(), getHeight());
		return true;
	}

	@Override
	public Point movePoint(int index, int x, int y) {
		if (!hasPoint(index)) {
			return null;
		}
		// compute new left upper and right lower point:
		Point lu = new Point(awtShape.x, awtShape.y);
		Point rl = new Point(awtShape.x+awtShape.width, awtShape.y+awtShape.height);
		
		// compute translation of this point:
		Point pt = getPoint(index);
		Point trans = new Point(x - pt.x, pt.y -y);
		
		if (index == 0) {
			lu.x = x;
			lu.y = y;
		}
		if (index == 1) {
			lu.y = y;
			rl.x = x;
		}
		if (index == 2) {
			rl.x = x;
			rl.y = y;
		}
		if (index == 3) {
			lu.x = x;
			rl.y = y;
		}
		
		List<Point> pts = new ArrayList<Point>();
		pts.add(lu);
		pts.add(rl);
		setPoints(pts);
		
		return trans;
	}

	@Override
	public int insertPoint(int x, int y) {
		return -1; // inserting into a rectangle is not permitted
	}

	@Override
	public boolean isPointRemovePossible(int i) {
		return false;
	}
	
	@Override
	public boolean removePoint(int i) {
		return false;
	}
	
	@Override
	public boolean canRemove() {
		return false;
	}
	
	@Override
	public boolean canInsert() {
		return false;
	}
	
	@Override
	public double distance(double x, double y, boolean signedDist) {
		double d = distanceToBoundingBox(x, y);
		if (!signedDist && d < 0)
			d *= -1;
		
		return d;
	}
	
	@Override
	public void simplify(double eps) {
		return;
	}
	
	@Override
	public boolean isClosedShape() { return true; }
	
	@Override public void addPoint(int x, int y) {}
	
	public static void main(String [] args) {
		CanvasRect r = new CanvasRect(new java.awt.Rectangle(2, 3, 10, 30));
		CanvasRect r1 = new CanvasRect(new java.awt.Rectangle(0, 3, 10, 30));
		CanvasRect r2 = new CanvasRect(new java.awt.Rectangle(0, 3, 15, 30));
		CanvasRect r3 = new CanvasRect(new java.awt.Rectangle(0, 3, 10, 10));
		
		
		
//		System.out.println(r.distanceToBoundingBox(4, 5));
//		System.out.println(r.distanceToBoundingBox(1, 1));
//		System.out.println(r.distanceToBoundingBox(2.0, 1));
		
//		java.awt.Polygon polygon = new java.awt.Polygon();
//		polygon.addPoint(1, 2);
//		polygon.addPoint(2, 3);
//		polygon.addPoint(5, 6);
		
		List<Point> pts = r.getPoints();		
		for (Point p : pts) {
			System.out.println("p: "+p.toString());
		}
		
		
//		System.out.println("compare = "+r.compareTo(r1));
//		System.out.println("compare = "+r.compareTo(r2));
//		System.out.println("compare = "+r.compareTo(r3));
//		System.out.println("compare = "+r.compareTo(r3));
	}









}
