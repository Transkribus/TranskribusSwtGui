package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.PointStrUtils;

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

	public CanvasQuadPolygon(String points) throws Exception {
		setPoints(PointStrUtils.parsePoints(points));
	}

	public CanvasQuadPolygon(CanvasPolygon src) {
		super(src);
	}

	@Override public String getType() {
		return "QUAD_POLYGON";
	}

	@Override public boolean setPoints(List<Point> pts) {
		// check min-number of points:
		if (pts == null || pts.size()<4) {
			throw new RuntimeException("Less than 4 pts given in setPoints method of CanvasQuadPolygon!");
		}
		// check if new pts comply with corner indices
		for (int ptIndex : corners) {
			if (ptIndex >= pts.size())
				throw new RuntimeException("Existing corner point index "+ptIndex+" not inside new points range, N = "+pts.size());	
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
	public void setDefaultCornerPts() {
		for (int i=0; i<4; ++i) {
			corners[i] = i;
		}
	}
	
	public void setCornerPts(int[] corners) throws IOException {
		if (corners == null || corners.length!=4)
			throw new IOException("Invalid corner pts given - must be array of length 4!");
		
		for (int posIndex=0; posIndex<4; ++posIndex) {
			setCornerPt(posIndex, corners[posIndex]);
		}
	}
	
	public void setCornerPt(int posIndex, int ptIndex) throws IOException {
		if (posIndex<0 || posIndex > 3) {
			throw new IOException("Invalid posIndex in setCornerPt: "+posIndex);
		}
		if (ptIndex < 0 || ptIndex >= getNPoints()) {
			throw new IOException("Invalid ptIndex in setCornerPt: "+ptIndex+" - nPts = "+getNPoints());
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
	
	public int getCornerPtIndex(int x, int y) {
		int ptIndex = getPointIndex(x, y);
		if (ptIndex == -1)
			return -1;
		
		return getCornerPtIndex(ptIndex);
	}
	
	public Point getCornerPt(int posIndex) throws CanvasShapeException {
		if (posIndex < 0 || posIndex >= 4)
			throw new CanvasShapeException("Invalid position index: "+posIndex);
		
		return getPoint(getCornerPtIndex(posIndex));
	}
	
	// end of corner pts stuff ----------------------

	@Override public boolean isPointRemovePossible(int i) {
		return (getNPoints() > 4 && i>=0 && i<getNPoints());
	}
	
	@Override
	public boolean removePoint(int i) {
//		throw new CanvasShapeException("removePoint operation not implemented yet for quad polygons!");
		
		// TODO: 
		if (!isPointRemovePossible(i))
			return false;
		
		try {
			List<Point> newPts = getPoints();
			newPts.remove(i);
			setPoints(newPts);
			
			// adapt corner points:
			for (int j=1; j<4; ++j) {
				if (corners[j] >= i)
					--corners[j];
			}
			
			return true;
		}
		catch (Exception e) {
			logger.error("Error while removing point "+i+" from shape "+this+": "+e.getMessage());
			return false;
		}
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

}
