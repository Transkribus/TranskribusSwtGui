package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.graphics.GC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.core.util.PointStrUtils.PointParseException;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;

public class CanvasQuadPolygon extends CanvasPolygon {
	
	private final static Logger logger = LoggerFactory.getLogger(CanvasQuadPolygon.class);
	
	/**
	 * This are the indices of the corner points of the quad.</br>
	 * The first value is the index of the upper left corner.</br>
	 * The second value is the index of the lower left corner.</br>
	 * The third value is the index of the lower right corner.</br>
	 * The fourth value is the index of the upper right corner.</br>
	 */
	private final int [] corners = { 0, 1, 2, 3 };

	public CanvasQuadPolygon(List<Point> pts) {
		setPoints(pts);
	}

//	public CanvasQuadPolygon(Collection<Point2D> ptsIn) {
//		setPoints2D(ptsIn);
//	}

	public CanvasQuadPolygon(String points) throws PointParseException {
		setPoints(PointStrUtils.parsePoints(points));
	}
	
	public CanvasQuadPolygon(String points, int[] corners) throws PointParseException, CanvasShapeException {
		setPoints(PointStrUtils.parsePoints(points));
		setCornerPts(corners);
	}

	public CanvasQuadPolygon(CanvasQuadPolygon src) {
		this.selectedTime = src.selectedTime;
		this.editable = src.editable;
		this.visible = src.visible;
		this.selected = src.selected;
		this.selectable = src.selectable;
		this.color = src.color;
		this.level = src.level;
		
		for (int i=0; i<4; ++i)
			this.corners[i] = src.corners[i];
						
		this.setPoints(src.getPoints());
		this.data = src.data;
		
		this.parent = src.parent;
		this.children = new ArrayList<ICanvasShape>(src.children);		
	}
	
	@Override public CanvasQuadPolygon copy() {
		return new CanvasQuadPolygon(this);
	}	

	@Override public String getType() {
		return "QUAD_POLYGON";
	}

	@Override public boolean setPoints(List<Point> pts) throws CanvasShapeException {
		// check min-number of points:
		if (pts == null || pts.size()<4) {
			throw new CanvasShapeException("Less than 4 pts given in setPoints method of CanvasQuadPolygon!");
		}
		// check if new pts comply with corner indices
		for (int ptIndex : corners) {
			if (ptIndex >= pts.size())
				throw new CanvasShapeException("Existing corner point index "+ptIndex+" not inside new points range, N = "+pts.size());	
		}
		
		java.awt.Polygon poly = new java.awt.Polygon();

		for (Point p : pts) {
			poly.addPoint(p.x, p.y);
		}
		setAwtShape(poly);

		setChanged();
		notifyObservers();

		return true;
	}
	
	// corner pts stuff ---------------
	public int[] getCorners() {
		return corners;
	}
	
	public void setDefaultCornerPts() {
		for (int i=0; i<4; ++i) {
			corners[i] = i;
		}
	}
	
	public void setCornerPts(int[] corners) throws CanvasShapeException {
		if (corners == null || corners.length!=4)
			throw new CanvasShapeException("Invalid corner pts given - must be array of length 4!");
		
		for (int posIndex=0; posIndex<4; ++posIndex) {
			setCornerPt(posIndex, corners[posIndex]);
		}
	}
	
	public void setCornerPt(int posIndex, int ptIndex) throws CanvasShapeException {
		if (posIndex<0 || posIndex > 3) {
			throw new CanvasShapeException("Invalid posIndex in setCornerPt: "+posIndex);
		}
		if (ptIndex < 0 || ptIndex >= getNPoints()) {
			throw new CanvasShapeException("Invalid ptIndex in setCornerPt: "+ptIndex+" - nPts = "+getNPoints());
		}
		
//		if (posIndex==0) {
//			throw new IOException("Cannot set corner point 0: "+ptIndex+" - nPts = "+getNPoints());
//		}
		
		corners[posIndex] = ptIndex;
	}

	/**
	 * Returns the index of the point 
	 * @param index
	 * @return
	 */
	public int getCornerPtIndex(int index) {
		if (index<0 || index > 3) {
			return -1;
		}
		
		return corners[index];
	}
	
	/**
	 * Returns the corner point *position* of the given point index,
	 * i.e. either 0,1,2 or 3 indicating left upper, left lower, right lower or right upper position.
	 * -1 is returned if the point with this index is not a corner point!
	 */
	public int getCornerPtPosition(int ptIndex) {
		for (int i=0; i<4; ++i) {
			if (corners[i] == ptIndex)
				return i;
		}
		return -1;
	}
	
	public int getCornerPtPosition(int x, int y) {
		int ptIndex = getPointIndex(x, y);
		if (ptIndex == -1)
			return -1;
		
		return getCornerPtPosition(ptIndex);
	}	
	
	public boolean isCornerPoint(int ptIndex) {
		return getCornerPtPosition(ptIndex) != -1;
	}
	
	public boolean isCornerPoint(int x, int y) {
		return getCornerPtPosition(x, y) != -1;
	}
	
	public Point getCornerPt(int posIndex) throws CanvasShapeException {
		if (posIndex < 0 || posIndex >= 4)
			throw new CanvasShapeException("Invalid position index: "+posIndex);
		
		return getPoint(getCornerPtIndex(posIndex));
	}
	
	// end of corner pts stuff ----------------------

	@Override public boolean isPointRemovePossible(int i) {
		return (getNPoints() > 4 && i>=0 && i<getNPoints() && !isCornerPoint(i));
	}
	
	@Override
	public boolean removePoint(int i) {
		logger.debug("removing point: "+i+" nPts = "+getNPoints());
//		throw new CanvasShapeException("removePoint operation not implemented yet for quad polygons!");
		
		// TODO: 
		if (!isPointRemovePossible(i))
			return false;
		
//		try {
			List<Point> newPts = getPoints();
			newPts.remove(i);			
			setPoints(newPts);
			
			// adapt corner points: FIXME - what if corner pt --> fix = corner pt removal not allowed!!
			for (int j=0; j<4; ++j) {
				if (corners[j] > i /*&& (j==0 || corners[j]-1>corners[j-1])*/)
					--corners[j];
			}
			printCorners();
			
			return true;
//		}
//		catch (Exception e) {
//			logger.error("Error while removing point "+i+" from shape "+this+": "+e.getMessage());
//			return false;
//		}
	}
	
	private void printCorners() {
		logger.debug("corners: "+ PointStrUtils.cornerPtsToString(corners));		
	}
	
	@Override
	public int insertPoint(int x, int y) {
//		throw new CanvasShapeException("insertPoint operation not implemented yet for quad polygons!");
		
		int ii = getInsertIndex(x, y);
		if (ii == 0)
			ii = getNPoints();
		
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
		
		// adapt corner points:
		for (int j=1; j<4; ++j) {
			if (corners[j] >= ii)
				++corners[j];
		}
		printCorners();
		
		return ii;
	}
	
	@Override
	public void simplify(double eps) {
		throw new CanvasShapeException("simplify operation not supported for quad polygons!");
	}
	
	@Override
	public Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2) {
		throw new CanvasShapeException("splitShape operation not implemented yet for quad polygons!");
	}
	
	@Override
	public ICanvasShape mergeShapes(ICanvasShape shape) {
		throw new CanvasShapeException("mergeShapes operation not implemented yet for quad polygons!");
	}
	
	@Override public void drawCornerPoints(SWTCanvas canvas, GC gc) {
		
//		float sx = canvas.getPersistentTransform().getScaleX();
//		float sy = canvas.getPersistentTransform().getScaleY();
		
		gc.setAlpha(canvas.getSettings().getForegroundAlpha());
		gc.setBackground(getColor());
		int radius = canvas.getSettings().getSelectedPointRadius();
//		radius = (int) ((float)radius/sx); // FIXME: not working
				
//		radius = 6;
//		canvas.scale(1.0f/sx, 1.0f/sy, 0, 0);
//		logger.debug("scale before: "+canvas.getPersistentTransform().getScaleX());
		int i=0;
		for (Point pt : getPoints()) {
			if (isCornerPoint(i)) {
				gc.fillRectangle(pt.x-radius-2, pt.y-radius-2, (radius+2)*2, (radius+2)*2);	
			} else {
				gc.fillOval(pt.x-radius, pt.y-radius, radius*2, radius*2);	
			}
		
//			drawCornerRect(gc, pt, radius, sx, sy);
			
			++i;
		}
//		canvas.scale(sx, sy, 0, 0);
//		logger.debug("scale after: "+canvas.getPersistentTransform().getScaleX());
	}

}
