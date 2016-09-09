package eu.transkribus.swt_gui.canvas;

import java.util.List;

import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.TextStyleTypeUtils;
import eu.transkribus.swt_canvas.canvas.CanvasScene;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.util.GuiUtil;

public class TrpCanvasScene extends CanvasScene {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasScene.class);

	public TrpCanvasScene(TrpSWTCanvas canvas) {
		super(canvas);
	}
	
	@Override public void paint(org.eclipse.swt.graphics.GC gc) {
		super.paint(gc);
		
		// TEST - draw border types of table cells
		for (ICanvasShape s : shapes) {
			TrpTableCellType c = TableUtils.getTableCell(s);
			if (c != null) {
				CanvasQuadPolygon qp = (CanvasQuadPolygon) s;
				CanvasSettings sets = canvas.getSettings();
				
				for (int i=0; i<4; ++i) {
					List<java.awt.Point> pts = qp.getPointsOfSegment(i, true);
					int[] ptArr = CoreUtils.getPointArray(pts);

					if (i == 0 && c.isLeftBorderVisible() || i == 1 && c.isBottomBorderVisible() || i == 2 && c.isRightBorderVisible()
							|| i == 3 && c.isTopBorderVisible()) {
						gc.setAlpha(sets.getForegroundAlpha());
						if (s.isSelected()) { // if selected:
							gc.setLineWidth(sets.getSelectedLineWidth() + 4); // set selected line with
							gc.setBackground(s.getColor()); // set background color
						} else {
							gc.setLineWidth(sets.getDrawLineWidth() + 4);
							gc.setBackground(s.getColor());
						}
						gc.setForeground(s.getColor());
						
						// TEST
						gc.setBackground(Colors.getSystemColor(SWT.COLOR_BLACK));
						gc.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
						
						gc.setLineStyle(canvas.getSettings().getLineStyle());
	
						gc.setAlpha(sets.getForegroundAlpha());
						gc.drawPolyline(ptArr);
	
					}
				}
			}
		}
		// END OF TEST
	}
	
	public void updateSegmentationViewSettings() {
		TrpSettings sets = TrpMainWidget.getInstance().getTrpSets();
		logger.trace("trpsets: " + sets.toString());

		for (ICanvasShape s : getShapes()) {
			if (s.hasDataType(TrpPrintSpaceType.class)) {
				s.setVisible(sets.isShowPrintSpace());
			}
			if (s.hasDataType(TrpTextRegionType.class)) {
				s.setVisible(sets.isShowTextRegions());
			}
			if (s.hasDataType(TrpTextLineType.class)) {
				s.setVisible(sets.isShowLines());
			}
			if (s.hasDataType(TrpBaselineType.class)) {
				s.setVisible(sets.isShowBaselines());
				s.setBaselineVisibiliy(!sets.isShowOnlySelectedBaseline());
			}
			if (s.hasDataType(TrpWordType.class)) {
				s.setVisible(sets.isShowWords());
			}
		}
		
		canvas.redraw();
	}
			
	public ICanvasShape selectObjectWithId(String id, boolean sendSignal, boolean multiselect) {
		for (ICanvasShape s : getShapes()) {
			if (s.getData() instanceof ITrpShapeType) {
				ITrpShapeType st = (ITrpShapeType) s.getData();
				if (st.getId().equals(id)) {
					selectObject(s, sendSignal, multiselect);
					return s;
				}
			}
		}
		return null;
	}
	
	public TextStyleType getCommonTextStyleOfSelected() {
		TextStyleType textStyle = null;
		for (int i=0; i<selected.size(); ++i) {
			ITrpShapeType st = GuiUtil.getTrpShape(selected.get(i));
			if (st == null)
				continue;
			
			if (textStyle == null) {
				textStyle = st.getTextStyle();
			}
			else {
				textStyle = TextStyleTypeUtils.mergeEqualTextStyleTypeFields(textStyle, st.getTextStyle());
			}
		}
		
		return textStyle;
	}
	
	public static void updateParentShape(ITrpShapeType st) {
		ICanvasShape shape = GuiUtil.getCanvasShape(st);
		if (shape == null)
			return;
				
		if (st.getParentShape() != null) {
			ICanvasShape pShape = (ICanvasShape) st.getParentShape().getData();
//			logger.debug("parent shape: "+pShape);
			shape.setParentAndAddAsChild(pShape);
		} else {
			shape.setParent(null);
		}
	}
	
	public static void updateParentInfo(ICanvasShape shape, boolean recursive) {
		ITrpShapeType st = GuiUtil.getTrpShape(shape);
		if (st == null)
			return;
		
		updateParentShape(st);
		if (recursive) {
			for (ITrpShapeType childSt : st.getChildren(recursive)) {
				updateParentShape(childSt);
			}
		}
	}
		
	public void updateAllShapesParentInfo() {
		Storage store = Storage.getInstance();
		
		if (!store.hasTranscript() || store.getTranscript().getPage()==null)
			return;
		
		for (ICanvasShape s : shapes) {
			s.setParent(null);
			s.removeChildren();
		}
		
		for (ITrpShapeType st : store.getTranscript().getPage().getAllShapes(true)) {
			updateParentShape(st);
		}
	}
	
	public List<TrpTableCellType> getSelectedTableCells() {
		return getSelectedData(TrpTableCellType.class);
	}
	
	public List<ICanvasShape> getSelectedTableCellShapes() {
		return getSelectedShapesWithData(TrpTableCellType.class);
	}
	
}
