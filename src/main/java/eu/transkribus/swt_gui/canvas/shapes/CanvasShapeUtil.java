package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.util.CoreUtils;

public class CanvasShapeUtil {
	public final static Logger logger = LoggerFactory.getLogger(CanvasShapeUtil.class);
	
	public static <T> boolean sortCanvasShapesByReadingOrder(List<ICanvasShape> canvasShapes) {
		try {
			Collections.sort(canvasShapes, new CanvasShapeReadingOrderComparator());
			return true;
		}
		catch (Exception e) {
			logger.error("Could not sort canvas shapes by reading order, exception = "+e.getMessage() +" - not sorting!");
			return false;
		}
	}
	
	public static <T> boolean sortCanvasShapesByXY(List<ICanvasShape> canvasShapes) {
		try {
			logger.debug("sorting canvas shapes by XY!");
			Collections.sort(canvasShapes, new CanvasShapeXYComparator());
			return true;
		}
		catch (Exception e) {
			logger.error("Could not sort canvas shapes by XY, exception = "+e.getMessage() +" - not sorting!");
			return false;
		}
	}	
	
	public static <T> boolean sortCanvasShapesByCoordinates(List<ICanvasShape> canvasShapes, boolean sortYX) {
		try {
			logger.debug("sorting canvas shapes by coordinates!");
			TrpShapeTypeUtils.sortShapesByCoordinates(canvasShapes, true);
			//Collections.sort(canvasShapes, new CanvasShapeXYComparator());
			return true;
		}
		catch (Exception e) {
			logger.error("Could not sort canvas shapes by XY, exception = "+e.getMessage() +" - not sorting!");
			return false;
		}
	}
	
	public static ICanvasShape copyShape(ITrpShapeType st) {
		ICanvasShape shape = CanvasShapeUtil.getCanvasShape(st);
		if (shape == null) {
			return null;
		}
		return new CanvasPolyline(shape.getPoints());
	}
	
	public static ICanvasShape getCanvasShape(ITrpShapeType st) {
		return (st==null || !(st.getData() instanceof ICanvasShape)) ? null : (ICanvasShape) st.getData();
	}
	
	public static ITrpShapeType getTrpShapeType(ICanvasShape cs) {
		return (cs == null || !(cs.getData() instanceof ITrpShapeType)) ? null : (ITrpShapeType) cs.getData();
	}
	
	public static int assignToParentIfOverlapping(ITrpShapeType parentShape, List<? extends ITrpShapeType> childShapes, double minOverlapRatio) {
		if (parentShape == null || CoreUtils.isEmpty(childShapes))
			return 0;
		
		int count=0;
		for (ITrpShapeType cs : childShapes) {
			if (cs.getParent() == parentShape)
				continue;
			
			ICanvasShape ccs = getCanvasShape(cs);
			if (ccs == null)
				continue;

			ICanvasShape pcs = getCanvasShape(parentShape);
			if (pcs == null)
				continue;
			
			double overlap = ccs.intersectionArea(pcs);
			
			double area = ccs.area();
			if (area < 0) // area can be < 0 for some strange reason (must have to do with orientation of polygon...)
				area *= -1;
			
			double ratio = overlap / area;
			logger.debug("overlap="+overlap+", area="+ccs.area()+", ratio = "+ratio);
			if (ratio > minOverlapRatio) {
				cs.removeFromParent();
				cs.setParent(parentShape);
				cs.reInsertIntoParent();
				++count;
			}
		}
		
		return count;
	}
	
	/**
	 * Assigns all shapes from childShapes to shapes from parentShapes according to maximum geometric overlap.
	 * If not shape from parentShapes is overlapping, the closest shape is chosen.
	 */
	public static int assignToShapesGeometrically(List<? extends ITrpShapeType> parentShapes, List<? extends ITrpShapeType> childShapes) {
		if (CoreUtils.isEmpty(parentShapes) || CoreUtils.isEmpty(childShapes))
			return 0;
		
		int count=0;
		for (ITrpShapeType cs : childShapes) {
			ICanvasShape ccs = getCanvasShape(cs);
			if (ccs == null)
				continue;
			
			ITrpShapeType shapeToAssignTo = null;
			
			double maxOverlap = 0;
			for (ITrpShapeType ps : parentShapes) {
				ICanvasShape pcs = getCanvasShape(ps);
				if (pcs == null)
					continue;
				
				double overlap = ccs.intersectionArea(pcs);
				if (overlap > maxOverlap) {
					shapeToAssignTo = ps;
					maxOverlap = overlap;
				}
			}
			
			if (shapeToAssignTo == null) { // no overlapping shape found -> find closes shape
				double minDist=Double.MAX_VALUE;
				for (ITrpShapeType ps : parentShapes) {
					ICanvasShape pcs = getCanvasShape(ps);
					if (pcs == null)
						continue;
					
					double dist = ccs.distanceToCenter(pcs);
					if (dist < minDist) {
						shapeToAssignTo = ps;
						minDist = dist;
					}
				}
			}
			
			if (shapeToAssignTo != null) {
				cs.removeFromParent();
				cs.setParent(shapeToAssignTo);
				cs.reInsertIntoParent();
				++count;
			}
		}
		
		return count;
	}
	
	public static TrpTextRegionType getFirstTextRegionWithSize(TrpPageType page, int x, int y, int width, int height, boolean recursive) {
		Rectangle check = new Rectangle(x, y, width, height);
		for (TrpTextRegionType tr : page.getTextRegions(recursive)) {
			ICanvasShape s = (ICanvasShape) tr.getData();

			if (s != null && s.getNPoints() == 4 && check.equals(s.getBounds())) {
				return tr;
			}
		}
		
		return null;
	}
	
	public static ICanvasShape getLineShape(ICanvasShape s) {
		if (s==null) {
			return null;
		}
		TrpTextLineType l = TrpShapeTypeUtils.getLine((ITrpShapeType) s.getData());
		return l==null ? null : (CanvasPolygon) l.getData();
	}
	
	public static ICanvasShape getBaselineShape(ICanvasShape s) {
		if (s == null)
			return null;
		
		TrpBaselineType bl = TrpShapeTypeUtils.getBaseline((ITrpShapeType) s.getData());
		if (bl == null)
			return null;
		
		return (CanvasPolyline) bl.getData();
	}
	
	public static Pair<int[], Double> getClosestLineIndices(int x, int y, List<Point> pts, boolean wrap) {		
		logger.trace("getClosestLineIndices: " + x + ", " + y + " wrap = " + wrap + ", pts.size() = " + pts.size());

		double minDist = Double.MAX_VALUE;
		int[] iz = new int[2];
		iz[0] = -1;
		iz[1] = -1;

		final int N = wrap ? pts.size() : pts.size() - 1;
		for (int i = 0; i < N; ++i) {
			int index1 = i;
			int index2 = (i + 1) % pts.size();

			Line2D line = new Line2D.Double(pts.get(index1), pts.get(index2));
			double d = line.ptSegDist(x, y);
			//			logger.debug("d = "+d+" minDist = "+minDist);
			if (d < minDist) {
				minDist = d;
				iz[0] = index1;
				iz[1] = index2;
			}
		}
		
		return Pair.of(iz, minDist);
	}

}
