package eu.transkribus.swt_gui.util;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;

public class TableUtils {
	private final static Logger logger = LoggerFactory.getLogger(TableUtils.class);

	public static class TrpTableCellsMissingException extends RuntimeException {
		private static final long serialVersionUID = 8451407831159497586L;

		List<Pair<Integer, Integer>> missing = new ArrayList<>();
		List<Pair<Integer, Integer>> overlapping = new ArrayList<>();

		public TrpTableCellsMissingException(String message, List<Pair<Integer, Integer>> missing, List<Pair<Integer, Integer>> overlapping) {
			super(message);

			this.missing = missing;
			this.overlapping = overlapping;
		}

		public String getMissingStr() {
			return StringUtils.join(missing, " ");
		}

		public String getOverlappingStr() {
			return StringUtils.join(overlapping, " ");
		}

		@Override public String getMessage() {
			return super.getMessage() + ", missing: " + getMissingStr() + ", overlapping: " + getOverlappingStr();
		}
	}

	public static class TrpTablePointsInconsistentException extends RuntimeException {
		private static final long serialVersionUID = 7319029830291915321L;

		List<Pair<TrpTableCellType, TrpTableCellType>> invalidPts = new ArrayList<>();

		public TrpTablePointsInconsistentException(String message, List<Pair<TrpTableCellType, TrpTableCellType>> invalidPts) {
			super(message);

			this.invalidPts = invalidPts;
		}

		@Override public String getMessage() {
			String s = super.getMessage() + " - invalid point combinations:\n";
			for (Pair<TrpTableCellType, TrpTableCellType> p : invalidPts) {
				s += p.getLeft().print() + " - " + p.getRight().print() + "\n";
			}
			return s;
		}

	}
	
	public static void checkTable(TrpTableRegionType table) throws TrpTableCellsMissingException,  TrpTablePointsInconsistentException {
		checkForMissingCells(table);
		checkForPointConsistency(table);
	}
	
	public static void checkForMissingCells(TrpTableRegionType table) throws TrpTableCellsMissingException {
		logger.debug("checking table for missing and overlapping cells");
//		table.sortChildren(false);
		
		Pair<Integer, Integer> dims = table.getDimensions();
		
		int[][] occupied = new int[dims.getLeft()][dims.getRight()];
		for (int i=0; i<dims.getLeft(); ++i) {
			for (int j=0; j<dims.getRight(); ++j) {
				occupied[i][j] = 0;
			}
		}
		
		for (TrpTableCellType c: table.getTrpTableCell()) {
			for (int i=c.getRow(); i<c.getRowEnd(); ++i) {
				for (int j=c.getCol(); j<c.getColEnd(); ++j) {
					++occupied[i][j];
				}
			}
		}
		
		List<Pair<Integer, Integer>> missing = new ArrayList<>();
		List<Pair<Integer, Integer>> overlapping = new ArrayList<>();
		
		for (int i=0; i<dims.getLeft(); ++i) {
			for (int j=0; j<dims.getRight(); ++j) {
				if (occupied[i][j] == 0) {
					missing.add(Pair.of(i, j));
				} else if (occupied[i][j] > 1) {
					overlapping.add(Pair.of(i, j));
				}
			}
		}
		
		if (!missing.isEmpty() || !overlapping.isEmpty()) {
			throw new TrpTableCellsMissingException("Table cells not consistent!", missing, overlapping);
		}
	}

	public static void checkForPointConsistency(TrpTableRegionType table) throws TrpTablePointsInconsistentException {
		logger.debug("checking table for point consistency");
		List<String> checked = new ArrayList<>();

		List<Pair<TrpTableCellType, TrpTableCellType>> invalid = new ArrayList<>();

		// for each cell: check if points from neighbor cell match
		for (TrpTableCellType c : table.getTrpTableCell()) {
			for (int i = 0; i < 4; ++i) {
				TrpTableCellType nc = c.getNeighborCell(i);
				if (nc == null)
					continue;

				String combinedId = c.getId() + "-" + nc.getId();

				// if already checked this combination of cells -> continue with next
				if (checked.contains(combinedId))
					continue;

				int ni = (i + 2) % 4; // the opposite side of the neighbor cell

				CanvasQuadPolygon qp1 = (CanvasQuadPolygon) c.getData();
				CanvasQuadPolygon qp2 = (CanvasQuadPolygon) nc.getData();

				List<Point> pt1 = qp1.getPointsOfSegment(i, true);
				List<Point> pt2 = qp2.getPointsOfSegment(ni, true);
				
				checked.add(combinedId);
				
				logger.trace("pt1.size = "+pt1.size()+" pt2.size = "+pt2.size());

				// pts must have equals size
				if (pt1.size() != pt2.size()) {
					invalid.add(Pair.of(c, nc));
					continue;
				}
				
				// pts must be equal
				for (int j = 0; j < pt1.size(); ++j) {
					if (!pt1.get(j).equals(pt2.get(pt1.size() - j - 1))) {
						invalid.add(Pair.of(c, nc));
						continue;
					}
				}
			}
		}

		if (!invalid.isEmpty())
			throw new TrpTablePointsInconsistentException("Table has inconsistent points!", invalid);
	}

}
