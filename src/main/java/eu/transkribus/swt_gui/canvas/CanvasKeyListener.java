package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.util.GuiUtil;

public final class CanvasKeyListener extends KeyAdapter {
	private static final Logger logger = LoggerFactory.getLogger(CanvasKeyListener.class);
	
	private SWTCanvas canvas;
//	private TrpMainWidget mainWidget;

	public CanvasKeyListener(SWTCanvas canvas) {
		this.canvas = canvas;
//		this.mainWidget = canvas.getMainWidget();
	}
	
	public void jumpToNextElement(int keyCode) {
		boolean previous = keyCode==SWT.ARROW_UP || keyCode==SWT.ARROW_LEFT;
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected != null) {
			ITrpShapeType st = GuiUtil.getTrpShape(selected);
			ITrpShapeType nextOrPrev = null;
			if (st instanceof TrpTextRegionType) {
				nextOrPrev = ((TrpTextRegionType) st).getNeighborTextRegion(previous);
			} else if (st instanceof TrpTextLineType) {
				nextOrPrev = ((TrpTextLineType) st).getNeighborLine(previous, true);
			} else if (st instanceof TrpWordType) {
				nextOrPrev = ((TrpWordType) st).getNeighborWord(previous, true, true);
			} else if (st instanceof TrpBaselineType) {
				TrpTextLineType neighborLine = ((TrpBaselineType) st).getLine().getNeighborLine(previous, true);
				nextOrPrev = (neighborLine!=null && neighborLine.getBaseline()!=null) ? (TrpBaselineType) neighborLine.getBaseline() : neighborLine;
//				nextOrPrev = ((TrpBaselineType) st).getLine().getNeighborLine(previous, true).getBaseline();
			}
			
			ICanvasShape shape = canvas.getScene().selectObjectWithData(nextOrPrev, true, false);
			canvas.getScene().makeShapeVisible(shape);
		}		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		TrpMainWidget mainWidget = TrpMainWidget.getInstance();
//		logger.debug("trp canvas key listener, key pressed: "+e.keyCode);
		
//		if (!canvas.isFocusControl())
//			return;
		
		TrpSettings sets = mainWidget.getTrpSets();

		switch (e.keyCode) {
		case SWT.ARROW_DOWN:
		case SWT.ARROW_UP:
		case SWT.ARROW_LEFT:
		case SWT.ARROW_RIGHT:
			
			if (canvas.isFocusControl())
				jumpToNextElement(e.keyCode);
			break;
		case SWT.F1:
			sets.setShowPrintSpace(!sets.isShowPrintSpace());
			break;
		case SWT.F2:
			sets.setShowTextRegions(!sets.isShowTextRegions());
			break;			
		case SWT.F3:
			sets.setShowLines(!sets.isShowLines());
			break;
		case SWT.F4:
			sets.setShowBaselines(!sets.isShowBaselines());
			break;
		case SWT.F5:
			sets.setShowWords(!sets.isShowWords());
			break;
		}
		
//		if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.ARROW_UP 
//				|| e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT) {
//			
//			boolean previous = e.keyCode==SWT.ARROW_UP || e.keyCode==SWT.ARROW_LEFT;
//			ICanvasShape selected = canvas.getFirstSelected();
//			if (selected != null) {
//				ITrpShapeType st = TrpUtil.getTrpShape(selected);
//				ITrpShapeType nextOrPrev = null;
//				if (st instanceof TrpTextRegionType) {
//					nextOrPrev = ((TrpTextRegionType) st).getNeighborRegion(previous);
//				} else if (st instanceof TrpTextLineType) {
//					nextOrPrev = ((TrpTextLineType) st).getNeighborLine(previous, true);
//				} else if (st instanceof TrpWordType) {
//					nextOrPrev = ((TrpWordType) st).getNeighborWord(previous, true, true);
//				}
//				
//				ICanvasShape shape = canvas.getScene().selectObjectWithData(nextOrPrev, true, false);
//				canvas.getScene().makeShapeVisible(shape);
//			}
//			
//		} 
		
	}
}
