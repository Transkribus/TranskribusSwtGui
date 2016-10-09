package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.core.util.PointStrUtils.PointParseException;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.table_editor.TableUtils;

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
	
	public CanvasQuadPolygon(Rectangle r) {
		List<Point> pts = new ArrayList<>();
		pts.add(new Point(r.x, r.y));
		pts.add(new Point(r.x, r.y+r.height));
		pts.add(new Point(r.x+r.width, r.y+r.height));
		pts.add(new Point(r.x+r.width, r.y));
		
		setPoints(pts);
	}

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
		this(PointStrUtils.parsePoints(points), corners);
	}
	
	public CanvasQuadPolygon(List<Point> pts, int[] corners) throws PointParseException, CanvasShapeException {
		setPoints(pts);
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
//		// check if new pts comply with corner indices
//		for (int ptIndex : corners) {
//			if (ptIndex >= pts.size())
//				throw new CanvasShapeException("Existing corner point index "+ptIndex+" not inside new points range, N = "+pts.size());	
//		}
		
		java.awt.Polygon poly = new java.awt.Polygon();

		for (Point p : pts) {
			poly.addPoint(p.x, p.y);
		}
		setAwtShape(poly);

		setChangedAndNotifyObservers();

		return true;
	}
	
	// corner pts stuff ---------------
	public int[] getCorners() {
		return corners;
	}
	
	public void setDefaultCornerPts() {
		setCornerPts(new int[] {0, 1, 2, 3});
		
//		for (int i=0; i<4; ++i) {
//			corners[i] = i;
//		}
//		setChangedAndNotifyObservers();
	}
	
	public void setCornerPts(int[] corners) throws CanvasShapeException {
		if (corners == null || corners.length!=4)
			throw new CanvasShapeException("Invalid corner pts given - must be array of length 4!");
		
		for (int posIndex=0; posIndex<4; ++posIndex) {
			setCornerPt(posIndex, corners[posIndex], false);
		}
		
		setChangedAndNotifyObservers();
	}
	
	private void setCornerPt(int posIndex, int ptIndex, boolean notifyObservers) throws CanvasShapeException {
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
		
		if (notifyObservers)
			setChangedAndNotifyObservers();
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
	
	/**
	 * Returns the side that the point with this index is on.<br/>
	 * 0 -> left side
	 * 1 -> bottom side
	 * 2 -> right side
	 * 3 -> top side
	 * -1 -> no side
	 */
	public int getPointSide(int ptIndex) {
		for (int i=0; i<4; ++i) {
			int ci2 = i==3 ? getNPoints() : corners[i+1];
			
			if (corners[i] <= ptIndex && ptIndex < ci2) {
				return i;
			}
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
		
		// adapt corner points: FIXME - what if corner pt --> fix = corner pt removal not allowed!!
		for (int j=0; j<4; ++j) {
			if (corners[j] > i /*&& (j==0 || corners[j]-1>corners[j-1])*/)
				--corners[j];
		}
		printCorners();		
	
		List<Point> newPts = getPoints();
		newPts.remove(i);			
		setPoints(newPts);
		
		return true;
	}
	
	private void printCorners() {
		logger.debug("corners: "+ PointStrUtils.cornerPtsToString(corners));		
	}
	
	public int[] getClosestLineIndices(int x, int y, int side) {
		int sp, ep;
		if (side == 0) {
			sp = corners[0];
			ep = corners[1];
		} else if (side == 1) {
			sp = corners[1];
			ep = corners[2];			
		} else if (side == 2) {
			sp = corners[2];
			ep = corners[3];
		} else if (side == 3) {
			sp = corners[3];
			ep = getNPoints();
		} else
			throw new CanvasShapeException("getClosestLineIndices, invalid side specified: "+side);
		
		double minDist = Double.MAX_VALUE;
		int[] iz = new int[2];
		iz[0] = -1; iz[1] = -1;
		
//		final int N = isClosedShape() ? getNPoints() : getNPoints()-1;
		for (int i=sp; i<ep; ++i) {
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
	
	public int insertPointOnSide(int x, int y, int side) {
		int[] iz = getClosestLineIndices(x, y, side);
		int ii = iz[1];
		
		insertPointOnIndex(x, y, ii);
		
		return ii;
	}

	
	@Override
	public int insertPoint(int x, int y) {
//		throw new CanvasShapeException("insertPoint operation not implemented yet for quad polygons!");
		
		int ii = getInsertIndex(x, y);
//		if (ii == 0)
//			ii = getNPoints();
		
		insertPointOnIndex(x, y, ii);
		
//		List<Point> newPts = new ArrayList<Point>();
//		
//		int i=0;
//		for (Point pt : getPoints()) {
//			if (ii == i++) {
//				newPts.add(new Point(x, y));
//			}
//			newPts.add(pt);
//		}
//		if (ii == getNPoints()) {
//			newPts.add(new Point(x,y));
//		}
//				
//		setPoints(newPts);
//		
//		// adapt corner points:
//		for (int j=1; j<4; ++j) {
//			if (corners[j] >= ii)
//				++corners[j];
//		}
		
		printCorners();
		
		return ii;
	}
	
	public void insertPointOnIndex(int x, int y, int ii) {
//		int ii = getInsertIndex(x, y);
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

		// adapt corner points:
		for (int j=1; j<4; ++j) {
			if (corners[j] >= ii)
				++corners[j];
		}
//		printCorners();
		
		setPoints(newPts);
	}
	
	
	public List<Point> getPointsOfSegment(int cornerPtIndex, boolean includeFirstOfNext) {
		List<Point> pts = new ArrayList<>();
		if (cornerPtIndex<0 || cornerPtIndex>3)
			return pts;
					
		int firstCornerPt = corners[cornerPtIndex];
		int secondCornerPt = cornerPtIndex==3 ? getNPoints() : corners[cornerPtIndex+1];
		
		int i=firstCornerPt;
		while (i<secondCornerPt) {
			pts.add(getPoint(i++));
		}
		
		if (includeFirstOfNext) {
			pts.add(getPoint(i == getNPoints() ? 0 : i));
		}
		
		return pts;
	}
	
//	/**
//	 * Returns the intersection points of the given line with a specific side of the QuadPolygon specified by the cornerPtIndex
//	 */
//	public List<ShapePoint> intersectionPoints(int x1, int y1, int x2, int y2, boolean extendLine, int cornerPtIndex) {
//		List<ShapePoint> ipts = new ArrayList<>();
//		if (cornerPtIndex<0 || cornerPtIndex>3)
//			return ipts;
//					
//		math.geom2d.line.LinearElement2D lGiven = null;
//		if (!extendLine)
//			lGiven = new math.geom2d.line.LineSegment2D(x1, y1, x2, y2);
//		else
//			lGiven = new math.geom2d.line.StraightLine2D(x1, y1, x2-x1, y2-y1);
//		
//		int istart = corners[cornerPtIndex];
//		int iend = cornerPtIndex==3 ? getNPoints() : corners[cornerPtIndex+1];
//		
//		List<Point> pts = getPoints();
//		int N = pts.size();
//		
//		for (int i=istart; i<iend; ++i) {
//			int iNext = (i+1) % N;
//			
//			math.geom2d.line.Line2D l = new math.geom2d.line.Line2D((int)pts.get(i).getX(), (int)pts.get(i).getY(),
//					(int)pts.get(iNext).getX(), (int)pts.get(iNext).getY());
//		
//			math.geom2d.Point2D pt = lGiven.intersection(l);
//			if (pt!=null) {
//				ipts.add(new ShapePoint(pt.getAsInt(), i));
//			}
//		}
//		
//		return ipts;
//	}
	
	public List<ShapePoint> intersectionPoints(CanvasPolyline pl, boolean extendLine, int cornerPtIndex) {
		List<ShapePoint> ipts = new ArrayList<>();
		if (cornerPtIndex<0 || cornerPtIndex>3)
			return ipts;
					
//		math.geom2d.line.LinearElement2D lGiven = null;
//		if (!extendLine)
//			lGiven = new math.geom2d.line.LineSegment2D(x1, y1, x2, y2);
//		else
//			lGiven = new math.geom2d.line.StraightLine2D(x1, y1, x2-x1, y2-y1);
		
		CanvasPolyline ipl = pl;
		if (extendLine)
			ipl = pl.extendAtEnds(1e36);
		
		int istart = corners[cornerPtIndex];
		int iend = cornerPtIndex==3 ? getNPoints() : corners[cornerPtIndex+1];
		
		List<Point> pts = getPoints();
		int N = pts.size();
		
		for (int i=istart; i<iend; ++i) {
			int iNext = (i+1) % N;
			
			Point p1 = pts.get(i);
			Point p2 = pts.get(iNext);
			
//			math.geom2d.line.Line2D l = new math.geom2d.line.Line2D((int)pts.get(i).getX(), (int)pts.get(i).getY(),
//					(int)pts.get(iNext).getX(), (int)pts.get(iNext).getY());
		
			List<ShapePoint> seg = ipl.intersectionPoints(p1.x, p1.y, p2.x, p2.y, false);
//			ipts.addAll(seg);
						
//			math.geom2d.Point2D pt = lGiven.intersection(l);
			
			for (ShapePoint p : seg) {
				ipts.add(new ShapePoint(p.p, i));
			}
			
//			if (pt!=null) {
//				ipts.add(new ShapePoint(pt.getAsInt(), i));
//			}
		}
		
		return ipts;
	}
	
//	public Pair<ShapePoint, ShapePoint> computeSplitPoints(int x1, int y1, int x2, int y2, boolean extendLine, TableDimension dir) {
//		Point p1 = new Point(x1, y1);
//		Point p2 = new Point(x2, y2);
//		CanvasPolyline pl = new CanvasPolyline(p1, p2);
//		
//		return computeSplitPoints(pl, extendLine, dir);
////		List<ShapePoint> ipts1 = intersectionPoints(x1, y1, x2, y2, extendLine, cp1);//		if (ipts1.size() != 1)
////			return null;
////		
////		List<ShapePoint> ipts2 = intersectionPoints(x1, y1, x2, y2, extendLine, cp2);
////		if (ipts2.size() != 1)
////			return null;
////		
////		return Pair.of(ipts1.get(0), ipts2.get(0));
//	}
	
	public Pair<ShapePoint, ShapePoint> computeSplitPoints(CanvasPolyline pl, boolean extendLine, TableDimension dir) {		
		int cp1 = dir==TableDimension.COLUMN ? 1 : 0;
		int cp2 = dir==TableDimension.COLUMN ? 3 : 2;
		
		List<ShapePoint> ipts1 = intersectionPoints(pl, extendLine, cp1);
		if (ipts1.size() != 1)
			return null;
		
		List<ShapePoint> ipts2 = intersectionPoints(pl, extendLine, cp2);
		if (ipts2.size() != 1)
			return null;
		
		return Pair.of(ipts1.get(0), ipts2.get(0));
	}
	
	@Override
	public void simplify(double eps) {
		throw new CanvasShapeException("Simplify operation is not supported for this shape!");
	}
	
	public void translatePointsOfSide(int side, int tx, int ty) {
		for (java.awt.Point pt : getPointsOfSegment(side, true)) {		
			movePoint(getPointIndex(pt.x, pt.y), pt.x+tx, pt.y+ty);
		}
	}
	
	@Override public void simplifyToBounds() {
		// NOT YET IMPLEMENTED FOR CANVAS QUAD POLYS
	}
	
	private CanvasQuadPolygon computeSplitShape(TableDimension dir, boolean topOrLeft, Pair<ShapePoint, ShapePoint> sp) {
		logger.debug("computeSplitShape, topOrLeft = "+topOrLeft+" sp = "+sp+" dir = "+dir);
		
		int[] newCorners = { 0, 0, 0, 0 };
		List<Point> newPts = new ArrayList<>();
		
		int cc=0;
		if (!topOrLeft && dir==TableDimension.COLUMN) {
			cc = 1;
		}
		
		int pc=0;
		for (int i=0; i<getNPoints(); ++i) {
			if (topOrLeft) {
				if (i<=sp.getLeft().index || i>sp.getRight().index) {
					if (isCornerPoint(i)) {
						logger.debug("cc0 = "+cc+" i = "+i);
						newCorners[cc++] = pc;
					}
					newPts.add(getPoint(i));
					++pc;
				}

				if (i == sp.getLeft().index) {
					newPts.add(sp.getLeft().p);
					logger.debug("cc1 = "+cc+" i = "+i);
					newCorners[cc++] = pc;
					++pc;
				} else if (i == sp.getRight().index) {
					newPts.add(sp.getRight().p);
					logger.debug("cc2 = "+cc+" i = "+i);
					newCorners[cc++] = pc;
					++pc;
				}
			} else {
				if (i>sp.getLeft().index && i<=sp.getRight().index) {
					if (isCornerPoint(i)) {
						logger.debug("cc3 = "+cc+" i = "+i);
						newCorners[cc++] = pc;
					}
					newPts.add(getPoint(i));
					++pc;
				}

				if (i == sp.getLeft().index) {
					logger.debug("cc4 = "+cc+" i = "+i);
					newPts.add(sp.getLeft().p);
					newCorners[cc++] = pc;
					++pc;
				} else if (i == sp.getRight().index) {
					logger.debug("cc5 = "+cc+" i = "+i);
				
					if (dir == TableDimension.ROW) {
						newPts.add(sp.getRight().p);
						newCorners[cc++] = pc;
						++pc;
					} else {
						// add last pt of right-horizontal split to front
						newPts.add(0, sp.getRight().p);
						newCorners[0] = 0;
						for (int j=1; j<4; ++j) {
							++newCorners[j];
						}
					}
					
					++pc;
				}
			}
		}
		
		CanvasQuadPolygon qp = copy();
		for (int i=0; i<4; ++i) {
			qp.corners[i] = newCorners[i];
		}
		qp.setPoints(newPts);
		
		return qp;
	}

//	private CanvasQuadPolygon computeLeftSplitShape(SplitDirection dir, Pair<ShapePoint, ShapePoint> sp) {			
//		int[] splitCorners = { 0, 0, 0, 0 };
//		List<Point> pts = new ArrayList<>();
//		
//		int c=0;
//		
//		int is1 = corners[0];
//		int ie1 = sp.getLeft().index;
//		for (int i=is1; i<ie1; ++i) {
//			pts.add(getPoint(i));
//			++c;
//		}
//		pts.add(sp.getLeft().p);
//		splitCorners[1] = c++;
//		
//		pts.add(sp.getRight().p);
//		splitCorners[2] = c++;
//		
//		int is2 = sp.getRight().index+1;
//		int ie2 = corners[3];
//		for (int i=is2; i<ie2; ++i) {
//			pts.add(getPoint(i));
//			++c;
//		}
//		
////		pts.add(getPoint(corners[3]));
//		
//		splitCorners[3] = c;
//		int is3 = corners[3];
//		int ie3 = getNPoints();
//		for (int i=is3; i<ie3; ++i) {
//			pts.add(getPoint(i));
//		}
//		
//		return new CanvasQuadPolygon(pts, splitCorners);
//	}	
	
	@Override
	public Pair<ICanvasShape, ICanvasShape> splitByPolyline(CanvasPolyline pl) {
		// try to find horizontal or vertical split points:
		TableDimension dir = TableDimension.COLUMN;
		
		Pair<ShapePoint, ShapePoint> sp = this.computeSplitPoints(pl, true, dir);
		if (sp == null) {
			dir = TableDimension.ROW;
			sp = this.computeSplitPoints(pl, true, dir);
		}
		
		// no split points found -> no split possible -> return null
		if (sp == null)
			return null;
		
		ICanvasShape s1 = computeSplitShape(dir, true, sp);
		ICanvasShape s2 = computeSplitShape(dir, false, sp);
		
		return Pair.of(s1, s2);
	}
		
//	@Override
//	public Pair<ICanvasShape, ICanvasShape> splitShape(int x1, int y1, int x2, int y2) {
//		//TODO use intersectionPoints method in both directions to determine split points and their index, then construct the two
//		// splits using them -> Polygons2D.intersection not needed here
//		
//		// try to find horizontal or vertical split points:
//		SplitDirection dir = SplitDirection.HORIZONAL;
//		Pair<ShapePoint, ShapePoint> sp = this.computeSplitPoints(x1, y1, x2, y2, true, dir);
//		if (sp == null) {
//			dir = SplitDirection.VERTICAL;
//			sp = this.computeSplitPoints(x1, y1, x2, y2, true, dir);
//		}
//		// no split points found -> no split possible -> return null
//		if (sp == null)
//			return null;
//		
//		ICanvasShape s1 = computeSplitShape(dir, true, sp);
//		ICanvasShape s2 = computeSplitShape(dir, false, sp);
//		
//		return Pair.of(s1, s2);
//	}
	
	public int getMergeableSide(ICanvasShape shape) {
		if (!(shape instanceof CanvasQuadPolygon))
			return -1;
		
		CanvasQuadPolygon shapeQp = (CanvasQuadPolygon) shape;
		
		for (int i=0; i<4; ++i) {
			Point p1 = getCornerPt(i);
			Point p2 = getCornerPt((i+1) % 4);
			
			Point p1_ = shapeQp.getCornerPt((i+3) % 4);
			Point p2_ = shapeQp.getCornerPt((i+2) % 4);
			
			logger.trace("p1 = "+p1+" p1_ = "+p1_);
			logger.trace("p2 = "+p2+" p2_ = "+p2_);
			
			if (p1 == null || p2 == null || p1_ == null || p2_==null) // should not happen, but u never know...
				continue;
			
			if (p1.equals(p1_) && p2.equals(p2_))
				return i;
		}
		
		return -1;
	}
	
	public ICanvasShape mergeOnLeftSide(ICanvasShape shape) {
		CanvasQuadPolygon qp = (CanvasQuadPolygon) shape;
		
		List<Point> pts = new ArrayList<>();
		int corners[] = { 0, 0, 0, 0 };
		
		int c=0;
		for (Point p : qp.getPointsOfSegment(0, false)) {
			pts.add(p);
			++c;
		}
		
		corners[1] = c;
		for (Point p : qp.getPointsOfSegment(1, false)) {
			pts.add(p);
			++c;
		}
		
		for (Point p : this.getPointsOfSegment(1, false)) {
			pts.add(p);
			++c;			
		}
		corners[2] = c;
		

		for (Point p : this.getPointsOfSegment(2, false)) {
			pts.add(p);
			++c;			
		}
		corners[3] = c;
		
		for (Point p : this.getPointsOfSegment(3, false)) {
			pts.add(p);
			++c;			
		}
		
		for (Point p : qp.getPointsOfSegment(3, false)) {
			pts.add(p);
			++c;		
		}
		
		CanvasQuadPolygon merged = this.copy();
		
		merged.setPoints(pts);
		merged.setCornerPts(corners);
		
		return merged;
	}
	
	public ICanvasShape mergeOnBottomSide(ICanvasShape shape) {
		CanvasQuadPolygon qp = (CanvasQuadPolygon) shape;
		
		List<Point> pts = new ArrayList<>();
		int corners[] = { 0, 0, 0, 0 };
		
		int c=0;
		for (Point p : this.getPointsOfSegment(0, false)) {
			pts.add(p);
			++c;
		}
		
		for (Point p : qp.getPointsOfSegment(0, false)) {
			pts.add(p);
			++c;
		}
		corners[1] = c;
		
		for (Point p : qp.getPointsOfSegment(1, false)) {
			pts.add(p);
			++c;			
		}
		corners[2] = c;
		
		for (Point p : qp.getPointsOfSegment(2, false)) {
			pts.add(p);
			++c;			
		}
		
		for (Point p : this.getPointsOfSegment(2, false)) {
			pts.add(p);
			++c;			
		}
		corners[3] = c;
		
		for (Point p : this.getPointsOfSegment(3, false)) {
			pts.add(p);
			++c;		
		}
		
		CanvasQuadPolygon merged = this.copy();
		
		merged.setPoints(pts);
		merged.setCornerPts(corners);
		
		return merged;
	}
	
	public ICanvasShape mergeOnRightSide(ICanvasShape shape) {
		return ((CanvasQuadPolygon)shape).mergeOnLeftSide(this);
	}
	
	public ICanvasShape mergeOnTopSide(ICanvasShape shape) {
		return ((CanvasQuadPolygon)shape).mergeOnBottomSide(this);
	}
	
//	public ICanvasShape mergeOnSide(ICanvasShape shape, int side) {
//		if (side==0)
//			return mergeOnLeftSide(shape);
//		else if (side==1)
//			return mergeOnBottomSide(shape);
//		else if (side==2)
//			return mergeOnRightSide(shape);
//		else if (side==3)
//			return mergeOnTopSide(shape);
//		
//		return null;
//	}
		
	@Override
	public ICanvasShape merge(ICanvasShape shape) {
		logger.debug("merging quad polygon shapes!");
//		throw new CanvasShapeException("mergeShapes operation not implemented yet for quad polygons!");
		
		int side = getMergeableSide(shape);
		logger.debug("mergeable side: "+side);
		if (side == -1) {
			logger.debug("shaped not mergeable!");
			return null;
		}
		
		if (side==0)
			return mergeOnLeftSide(shape);
		else if (side==1)
			return mergeOnBottomSide(shape);
		else if (side==2)
			return mergeOnRightSide(shape);
		else if (side==3)
			return mergeOnTopSide(shape);
		
		return null;
	}
	
	@Override public void drawOutline(SWTCanvas canvas, GC gc) {
		CanvasSettings sets = canvas.getSettings();
		
		TrpTableCellType c = TableUtils.getTableCell(this);
		if (c != null) {
			CanvasQuadPolygon qp = (CanvasQuadPolygon) this;
			
			if (isSelected()) { // fill polygon if selected
				int[] ptArr = CoreUtils.getPointArray(getPoints());
				gc.setAlpha(sets.getBackgroundAlpha());
				gc.fillPolygon(ptArr);
			}
			
			for (int i=0; i<4; ++i) {
				List<java.awt.Point> pts = qp.getPointsOfSegment(i, true);
				int[] sidePtArr = CoreUtils.getPointArray(pts);
				
				boolean hasBorder = i == 0 && c.isLeftBorderVisible() || i == 1 && c.isBottomBorderVisible() || i == 2 && c.isRightBorderVisible()
						|| i == 3 && c.isTopBorderVisible();
				
				gc.setAlpha(sets.getForegroundAlpha());
				if (isSelected()) { // if selected:
					gc.setLineWidth(sets.getSelectedLineWidth()); // set selected line with
					gc.setBackground(getColor()); // set background color
				} else {
					gc.setLineWidth(sets.getDrawLineWidth());
					gc.setBackground(getColor());
				}
				gc.setForeground(getColor());
				gc.setLineStyle(canvas.getSettings().getLineStyle());
				
				if (hasBorder) {
					// TEST
					gc.setBackground(Colors.getSystemColor(SWT.COLOR_BLACK));
					gc.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));

					gc.drawPolyline(sidePtArr);
				} else {
//					if (isSelected()) { // fill polygon if selected
//						gc.setAlpha(sets.getBackgroundAlpha());
//						gc.fillPolygon(sidePtArr);
//					}
					
					gc.drawPolyline(sidePtArr);
				}
			}
		} else {
			super.drawOutline(canvas, gc);
		}
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
				
				gc.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
				
				gc.drawString(""+(getCornerPtPosition(i)+1), pt.x-radius-2, pt.y-radius-2, true);
				
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
