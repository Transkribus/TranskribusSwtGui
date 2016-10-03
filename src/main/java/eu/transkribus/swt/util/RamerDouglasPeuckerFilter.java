package eu.transkribus.swt.util;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polygon simplification using Ramer-Douglas-Peucker algorithm with specified
 * tolerance
 * 
 * @see <a
 *      href="http://en.wikipedia.org/wiki/Ramer-Douglas-Peucker_algorithm">Ramer-Douglas-Peucker
 *      algorithm</a>
 */
public class RamerDouglasPeuckerFilter {

	private static Logger logger = LoggerFactory.getLogger(RamerDouglasPeuckerFilter.class);

	private double epsilon;

	/**
	 * @param epsilon Maximum distance of a point in data between original curve and simplified curve
	 * @throws IllegalArgumentException when {@code epsilon <= 0}
	 */
	public RamerDouglasPeuckerFilter(double epsilon) {
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon must be > 0");
		}
		this.epsilon = epsilon;
	}

	/**
	 * Filters a list of polygon points
	 */
	public List<Point> filter(List<Point> pts) {
		return ramerDouglasPeuckerFunction(pts, 0, pts.size() - 1);
	}

	/**
	 * 
	 * @return {@code epsilon}
	 */
	public double getEpsilon() {
		return epsilon;
	}

	protected List<Point> ramerDouglasPeuckerFunction(List<Point> pts, int startIndex, int endIndex) {

		double dmax = 0;
		int index = 0;
		Line2D.Double line = new Line2D.Double(pts.get(startIndex), pts.get(endIndex));

		for (int i = startIndex + 1; i < endIndex; ++i) {
			double dist = line.ptSegDist(pts.get(i));
			logger.debug("dist = " + dist);
			if (dist > dmax) {
				index = i;
				dmax = dist;
			}
		}

		List<Point> simpl = new ArrayList<Point>();

		if (dmax >= epsilon) {
			List<Point> sub1Simpl = ramerDouglasPeuckerFunction(pts, startIndex, index);
			List<Point> sub2Simpl = ramerDouglasPeuckerFunction(pts, index, endIndex);
			simpl.addAll(sub1Simpl);
			simpl.addAll(sub2Simpl.subList(1, sub2Simpl.size())); // do not add
																	// first
																	// point
																	// since it
																	// was added
																	// by
																	// sub1Simpl
																	// already!
		} else {
			simpl.add(pts.get(startIndex));
			simpl.add(pts.get(endIndex));
		}

		return simpl;
	}

	/**
	 * 
	 * @param epsilon
	 *            maximum distance of a point in data between original curve and
	 *            simplified curve
	 */
	public void setEpsilon(double epsilon) {
		if (epsilon <= 0) {
			throw new IllegalArgumentException("Epsilon nust be > 0");
		}
		this.epsilon = epsilon;
	}

}
