package eu.transkribus.swt_gui.tables;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_gui.canvas.shapes.CanvasRect;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.table_editor.BorderFlags;

public class TableBorderTest {


	public static void main(String[] args) {
		
		// needed for method testing...
		
		CanvasShapeEditor cse = new CanvasShapeEditor(new SWTCanvas(SWTUtil.dummyShell, SWT.NONE));
		
		// create some canvas shapes
		List<ICanvasShape> shapes = new ArrayList<ICanvasShape>();
		List<ICanvasShape> single = new ArrayList<ICanvasShape>();
		List<ICanvasShape> doubleMiddle = new ArrayList<ICanvasShape>();
		
		// create (blank) table cells
		List<TrpTableCellType> cells = new ArrayList<TrpTableCellType>();
		
		int numRows = 5; 
		int numCols = 3;
		
		for (int r = 0; r < numRows; r++) {
			for (int c=0; c < numCols; c++) {
				TrpTableCellType t = new TrpTableCellType();
				t.setCol(c);
				t.setRow(r);
				cells.add(t);
				
				t.setLeftBorderVisible(c != 0);
				t.setRightBorderVisible(c < numCols -1);
				t.setTopBorderVisible(r != 0);
				t.setBottomBorderVisible(r < numRows -1);
				
				CanvasRect canvasRect = new CanvasRect(r, c, 10, 10);
				canvasRect.setData(t);
				shapes.add(canvasRect);
				
				if (r == 0 && c == 0)
					single.add(canvasRect);
				
				if (c == 1 && r > 0 && r < numRows -1)
					doubleMiddle.add(canvasRect);
			}
		}
		
		BorderFlags bf;
		bf = cse.retrieveExistingBordersForTableCells(single);
		System.out.println("Test single: "+bf.toString());
		
		bf = cse.retrieveExistingBordersForTableCells(doubleMiddle);
		System.out.println("Test multi: "+bf.toString());
		
		
		bf = cse.retrieveExistingBordersForTableCells(shapes);
		System.out.println("Test full: "+bf.toString());
		
	}

}
