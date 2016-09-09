package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CanvasShapeUtil {
	public final static Logger logger = LoggerFactory.getLogger(CanvasShapeUtil.class);
	
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
