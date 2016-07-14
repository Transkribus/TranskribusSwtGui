package eu.transkribus.swt_gui.table_editor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType.GetCellsType;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.SplitDirection;

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
				if (p.getLeft().getId().equals(p.getRight().getId())) {
					s += "not enough neigbhor pts: "+p.getLeft().print()+"\n";
				} else {
					s += "pts not matching: "+p.getLeft().print() + " - " + p.getRight().print() + "\n";	
				}
			}
			return s;
		}

	}
	
	public static boolean isTableCells(List<ICanvasShape> shapes) {
		for (ICanvasShape s : shapes) {
			if (getTableCell(s) == null)
				return false;
		}
		return true;
	}
	
	
	public static TrpTableCellType getTableCell(ICanvasShape shape) {
		if (shape!=null && shape instanceof CanvasQuadPolygon && shape.getData() instanceof TrpTableCellType) {
			return (TrpTableCellType) shape.getData();
		}
		
		return null;
	}
	
	public static TrpTableRegionType getTable(ICanvasShape shape) {
		TrpTableCellType tc = getTableCell(shape);
		if (tc != null) {
			return tc.getTable();
		}
		
		if (shape!=null && shape.getData() instanceof TrpTableRegionType) {
			return (TrpTableRegionType) shape.getData();
		}
		
		return null;
	}	
	
	public static void checkTableConsistency(TrpTableRegionType table) throws TrpTableCellsMissingException,  TrpTablePointsInconsistentException {
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
	
	public static Pair<SplitDirection, List<TrpTableCellType>> getSplittableCells(int x1, int y1, int x2, int y2, TrpTableRegionType table) {
		if (table == null)
			return null;
		
		Pair<Integer, Integer> dims = table.getDimensions();
		
		int nRows = dims.getLeft();
		int nCols = dims.getRight();
		
		List<TrpTableCellType> splittableCells=null;
		SplitDirection dir = null;
		for (int j=0; j<2; ++j) {
			int N = j==0 ? nRows : nCols;
			
			for (int i=0; i<N; ++i) {
				List<TrpTableCellType> cells = table.getCells(j==0, GetCellsType.START_INDEX, i);
				dir = j==0 ? SplitDirection.VERTICAL : SplitDirection.HORIZONAL;
				
				logger.debug("cells i = "+i+" first cell: "+cells.get(0));
				
				for (TrpTableCellType c : cells) {
					CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
					
					if (qp.computeSplitPoints(x1, y1, x2, y2, true, dir) == null) {
						logger.debug("cells not splittable in dir = "+dir);
						cells = null;
						break;
					}
				}
				
				if (cells != null) {
					splittableCells = cells;
					break;				
				}
			}
			
			if (splittableCells != null) {
				break;				
			}
		}
		
		if (splittableCells == null)
			return null;
		else
			return Pair.of(dir, splittableCells);
	}

	
	public static void checkForPointConsistency(TrpTableRegionType table) throws TrpTablePointsInconsistentException {
		logger.debug("checking table for point consistency");
		List<String> checked = new ArrayList<>();

		List<Pair<TrpTableCellType, TrpTableCellType>> invalid = new ArrayList<>();

		// for each cell: check if points from neighbor cell match
		for (TrpTableCellType c : table.getTrpTableCell()) {
			for (int s = 0; s < 4; ++s) {
				List<TrpTableCellType> ns = c.getNeighborCells(s);
				logger.debug("s = "+s+" ns.size() = "+ns.size());
				
				if (ns.isEmpty())
					continue;
				
				CanvasQuadPolygon qp1 = (CanvasQuadPolygon) c.getData();
				
				List<Point> segPts = qp1.getPointsOfSegment(s, true);
				logger.debug("s = "+s+" segPts.size() = "+segPts.size());
				
				for (Point p : segPts) {
					boolean found = false;
					for (TrpTableCellType nc : ns) {
						CanvasQuadPolygon qpn = (CanvasQuadPolygon) nc.getData();						
						if (qpn.getPoints().contains(p)) {
							logger.debug("found: "+p+ " s = "+s+" id = "+c.getId());
							found = true;
							break;
						}
					}
					if (!found) {
						logger.debug("not found: "+p+" s = "+s+" id = "+c.getId());
						invalid.add(Pair.of(c, c));
					}
				}
				
				
				
				/*
				List<Point> pt1 = qp1.getPointsOfSegment(s, true);
				List<Pair<Point, TrpTableCellType>> pt2 = new ArrayList<>();
				
				boolean rot = s>1;
				
				int so = (s + 2) % 4; // the opposite side of the neighbor cell
				
				// accumulate points of all neighbors on this side, in right order!
				for ( int i=(rot?ns.size()-1:0); i!=(rot?-1:ns.size()); i+=(rot?-1:1) ) {
					TrpTableCellType nc = ns.get(i);
					
					String combinedId = c.getId() + "-" + nc.getId();
					// if already checked this combination of cells -> continue with next
					if (checked.contains(combinedId))
						continue;									
					
					CanvasQuadPolygon qpn = (CanvasQuadPolygon) nc.getData();
					List<Point> ptn = qpn.getPointsOfSegment(so, true);
					
					Collections.reverse(ptn);
					
					// remove last point if this is not the last of the neighbor cells
					if ( (!rot && i!=ns.size()-1) || (rot && i!=0) ) {
						ptn.remove(ptn.size()-1);
					}
					
					for (Point p : ptn) {
						pt2.add(Pair.of(p, nc));
					}

					checked.add(combinedId);
				}

//				CanvasQuadPolygon qp1 = (CanvasQuadPolygon) c.getData();
//				CanvasQuadPolygon qp2 = (CanvasQuadPolygon) nc.getData();

//				List<Point> pt1 = qp1.getPointsOfSegment(s, true);
//				List<Point> pt2 = qp2.getPointsOfSegment(so, true);

				logger.debug("pt1.size = "+pt1.size()+" pt2.size = "+pt2.size());

				// pts must have equals size
//				if (pt1.size() != pt2.size()) {
//					invalid.add(Pair.of(c, c));
//					continue;
//				}
				
				// pts must be equal
				int N = pt1.size();
				
				for (int j = 0; j < N; ++j) {
					Point p1 = pt1.get(j);
					Point p2 = pt2.get(j).getLeft();
//					logger.debug("p1 = "+p1+" p2 = "+p2);
					
					if (!p1.equals(p2)) {
						invalid.add(Pair.of(c, pt2.get(pt1.size() - j - 1).getRight()));
						continue;
					}
				}
				*/
			}
		}

		if (!invalid.isEmpty())
			throw new TrpTablePointsInconsistentException("Table has inconsistent points!", invalid);
	}

}
