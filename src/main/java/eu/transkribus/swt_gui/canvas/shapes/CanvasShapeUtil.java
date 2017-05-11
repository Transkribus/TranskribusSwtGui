package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;

public class CanvasShapeUtil {
	public final static Logger logger = LoggerFactory.getLogger(CanvasShapeUtil.class);
	
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
