package eu.transkribus.swt_gui.table_editor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType.GetCellsType;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.canvas.shapes.TableDimension;

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
	

	
	public static TrpTableRegionType getTable(ICanvasShape shape, boolean includeCellShapes) {
		
		if (includeCellShapes) {
			TrpTableCellType tc = getTableCell(shape);
			if (tc != null) {
				return tc.getTable();
			}
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
	
	public static class SplittableCellsStruct {
		public TableDimension dir=null;
		public int index=-1;
		public List<TrpTableCellType> cells=null;
	}
	
	/**
	 * Tries to split the given table using the given polyline in either row or column direction.<br>
	 * Returns a structure containing the splittable cells according to a certain direction (row or column)
	 * and the row / column index of the cells that are splitted.<br>
	 * Returns null if no splitting can be achieved in row or column direction using the polyline.
	 */
	public static SplittableCellsStruct getSplittableCells(CanvasPolyline pl, TrpTableRegionType table) {
		if (table == null)
			return null;
		
		SplittableCellsStruct res = new SplittableCellsStruct();
		
		Pair<Integer, Integer> dims = table.getDimensions();
		
		int nRows = dims.getLeft();
		int nCols = dims.getRight();
		
//		List<TrpTableCellType> splittableCells=null;
		TableDimension dir = null;		
		for (int j=0; j<2 && res.index==-1; ++j) {
			int N = j==0 ? nRows : nCols;
			
			for (int i=0; i<N; ++i) {
				List<TrpTableCellType> cells = table.getCells(j==0, GetCellsType.OVERLAP, i);
				dir = j==0 ? TableDimension.ROW : TableDimension.COLUMN;
				
				logger.debug("cells i = "+i+" first cell: "+cells.get(0));
				
				for (TrpTableCellType c : cells) {
					CanvasQuadPolygon qp = (CanvasQuadPolygon) c.getData();
					
					if (qp.computeSplitPoints(pl, true, dir) == null) {
						logger.debug("cells not splittable in dir = "+dir);
						cells = null;
						break;
					}
				}
				
				if (cells != null) {				
					res.index = i;
					res.cells = cells;
					res.dir = dir;
					
					break;	
				}
			}
		}
		
		if (res.index == -1)
			return null;
		else {
			return res;
//			return Pair.of(dir, splittableCells);
		}
			
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
	
	public static TrpTableCellType getCell(TrpTableRegionType table, int row, int col) {
		if (table==null)
			return null;
		
		return table.getCell(row, col);
	}
	
	public static void selectCells(SWTCanvas canvas, TrpTableCellType cell, TableDimension dim, boolean multiSelect) {
		int index = -1;
		if (dim == TableDimension.ROW) {
			index = cell.getRow();
		}
		else if (dim == TableDimension.COLUMN) {
			index = cell.getCol();
		}
		
		selectCells(canvas, cell.getTable(), index, dim, multiSelect);
	}
	
	public static void selectCells(SWTCanvas canvas, TrpTableRegionType table, int index, TableDimension dim, boolean multiSelect) {
		List<TrpTableCellType> cells = null;
		if (dim == null) {
			cells = table.getTrpTableCell();
		} else {
			cells = table.getCells(dim==TableDimension.ROW, GetCellsType.OVERLAP, index);	
		}

		if (!multiSelect)
			canvas.getScene().clearSelected();
		for (int i=0; i<cells.size(); ++i) {
			TrpTableCellType c = cells.get(i);
			selectTableCell(canvas, c, i==cells.size()-1, true);
		}
		
		canvas.redraw();
	}
	
	public static void selectTableCell(SWTCanvas canvas, TrpTableCellType cell, boolean sendSignal, boolean multiselect) {
		if (cell!=null && cell.getData() instanceof ICanvasShape) {
			ICanvasShape s = (ICanvasShape) cell.getData();
			s.setSelected(false); // for multiselection to work
			
			canvas.getScene().selectObject((ICanvasShape) cell.getData(), sendSignal, multiselect);
		}
	}
	
	public static int parsePositionFromArrowKeyCode(int keyCode) {
		if (keyCode == SWT.ARROW_LEFT)
			return 0;
		if (keyCode == SWT.ARROW_DOWN)
			return 1;
		if (keyCode == SWT.ARROW_RIGHT)
			return 2;
		if (keyCode == SWT.ARROW_UP)
			return 3;
		
		return -1;
	}
	
	/**
	 * select neighbor according to position
	 * 0 -> left
	 * 1 -> bottom
	 * 2 -> right
	 * 3 -> top
	 */
	public static void selectNeighborCell(SWTCanvas canvas, TrpTableCellType tc, int position) {
		logger.debug("selectNeighborCell, tc = "+tc+", position = "+position);
		
		if (tc == null || tc.getTable()==null)
			return;
		
		TrpTableRegionType table = tc.getTable();
				
		int r=-1, c=-1;
		if (position == 0) { // left
			r = tc.getRow();
			c = tc.getCol()-1;
		}
		else if (position == 1) { // bottom
			r = tc.getRowEnd();
			c = tc.getCol();
		}
		else if (position == 2) { // right
			r = tc.getRow();
			c = tc.getColEnd();
		}
		else if (position == 3) { // top
			r = tc.getRow()-1;
			c = tc.getCol();
		}
		else
			return;
		
		logger.debug("r = "+r+" c = "+c);
		TrpTableCellType neighborCell = table.getCell(r, c);
		logger.debug("neighborCell = "+neighborCell);
		
		selectTableCell(canvas, neighborCell, true, false);
		
		// OLD code
//		List<TrpTableCellType> n = tc.getNeighborCells(position);
//		if (n.isEmpty()) {
//			// TODO: what to do here? --> jump to first / last cell in next row / column
//			
//		} else {
//			TrpTableCellType c = n.get(0);
//			selectTableCell(canvas, c, true, false);			
//		}
	}
	
	public static boolean hasLeftNeighbor(TrpTableCellType c, List<ICanvasShape> tableCellShapes) {
		return tableCellShapes.stream().anyMatch(s -> {
			if (s.getData() instanceof TrpTableCellType) {
				TrpTableCellType n = (TrpTableCellType) s.getData();
				if (n.getColEnd()==c.getCol())
					return true;
			}
			return false;
		});
	}
	
	public static boolean hasRightNeighbor(TrpTableCellType c, List<ICanvasShape> tableCellShapes) {
		return tableCellShapes.stream().anyMatch(s -> {
			if (s.getData() instanceof TrpTableCellType) {
				TrpTableCellType n = (TrpTableCellType) s.getData();
				if (c.getColEnd()==n.getCol())
					return true;
			}
			return false;
		});
	}

	public static boolean hasBottomNeighbor(TrpTableCellType c, List<ICanvasShape> tableCellShapes) {
		return tableCellShapes.stream().anyMatch(s -> {
			if (s.getData() instanceof TrpTableCellType) {
				TrpTableCellType n = (TrpTableCellType) s.getData();
				if (c.getRowEnd()==n.getRow())
					return true;
			}
			return false;
		});
	}
	
	public static boolean hasTopNeighbor(TrpTableCellType c, List<ICanvasShape> tableCellShapes) {
		return tableCellShapes.stream().anyMatch(s -> {
			if (s.getData() instanceof TrpTableCellType) {
				TrpTableCellType n = (TrpTableCellType) s.getData();
				if (n.getRowEnd()==c.getRow())
					return true;
			}
			return false;
		});
	}	
	
}
