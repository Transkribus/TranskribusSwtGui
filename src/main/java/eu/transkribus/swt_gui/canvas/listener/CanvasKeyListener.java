package eu.transkribus.swt_gui.canvas.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.canvas.CanvasKeys.KeyAction;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.util.GuiUtil;


public class CanvasKeyListener implements KeyListener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasKeyListener.class);
	
	SWTCanvas canvas;
	protected CanvasMode modeBackup=CanvasMode.SELECTION;
	int currentStateMask;


	public CanvasKeyListener(SWTCanvas canvas) {
		this.canvas = canvas;
		modeBackup = canvas.getSettings().getMode();
	}
	
	private void jumpToNextElement(int keyCode) {
		if (keyCode != SWT.ARROW_LEFT && keyCode != SWT.ARROW_RIGHT && keyCode != SWT.ARROW_UP && keyCode != SWT.ARROW_DOWN)
			return;
		
		boolean previous = keyCode==SWT.ARROW_UP || keyCode==SWT.ARROW_LEFT;
		ICanvasShape selected = canvas.getFirstSelected();
		if (selected == null)
			return;

		ITrpShapeType st = GuiUtil.getTrpShape(selected);
		if (st == null)
			return;
		
		if (st instanceof TrpTableCellType) {
			TrpTableCellType cell = (TrpTableCellType) st;
			
			int position = TableUtils.parsePositionFromArrowKeyCode(keyCode); // = left
			if (position == -1)
				return;

			TableUtils.selectNeighborCell(canvas, cell, position);			
		}
		else {
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
		try {
		currentStateMask = e.stateMask | e.keyCode;
		
//		logger.debug("ismouseincanvas = "+canvas.isMouseInCanvas()+ "canvas focus = "+canvas.isFocusControl());
//		logger.debug("isFocusControl = "+canvas.isFocusControl());
		if (/*!canvas.isMouseInCanvas() ||*/ !canvas.isFocusControl()) {
//			logger.debug("mouse not in canvas or canvas not focused - doing nothing!");
			return;
		}
		
		logger.debug("key pressed: "+e.keyCode+ " mask: "+e.stateMask);
		
		TrpSettings sets = TrpMainWidget.getInstance().getTrpSets();
		switch (e.keyCode) {
			case SWT.ARROW_DOWN:
			case SWT.ARROW_UP:
			case SWT.ARROW_LEFT:
			case SWT.ARROW_RIGHT:
				if (canvas.isFocusControl()) {
					jumpToNextElement(e.keyCode);
				}
				break;
				
//			case SWT.F1:
//				sets.setShowPrintSpace(!sets.isShowPrintSpace());
//				break;
//			case SWT.F2:
//				sets.setShowTextRegions(!sets.isShowTextRegions());
//				break;			
//			case SWT.F3:
//				sets.setShowLines(!sets.isShowLines());
//				break;
//			case SWT.F4:
//				sets.setShowBaselines(!sets.isShowBaselines());
//				break;
//			case SWT.F5:
//				sets.setShowWords(!sets.isShowWords());
//				break;
		}
		
		// set modes depending on key press:		
		CanvasMode mode = canvas.getMode();
		
		KeyAction ka = CanvasKeys.getKeyAction(e);
		if (ka == null) {
			logger.debug("cannot find key action for key event: "+e);
			return;
		}
				
		final boolean hasKeysDown = ka.hasRequiredKeysDown(e.stateMask);
		if (!hasKeysDown)
			return;
		
		if (ka.isEditOperation() && !canvas.getSettings().isEditingEnabled()) {
			logger.debug("Preventing edit operation for key="+e.keyCode);
			return;
		}
		
		if (CanvasKeys.containsKey(CanvasKeys.FIT_TO_WIDTH, e.keyCode)) {
			canvas.fitWidth();
		}
		else if (CanvasKeys.containsKey(CanvasKeys.FIT_TO_HEIGHT, e.keyCode)) {
			canvas.fitHeight();
		}
//		else if (ka.equals(CanvasKeys.FIT_TO_PAGE)) {
//			canvas.fitToPage();
//		}
		else if (CanvasKeys.containsKey(CanvasKeys.SET_SELECTION_MODE, e.keyCode)) {
			canvas.setMode(CanvasMode.SELECTION);
		}
//		else if (CanvasKeys.containsKey(CanvasKeys.ROTATE_LEFT, e.keyCode)) {
//			canvas.rotateLeft();
//		}
//		else if (CanvasKeys.containsKey(CanvasKeys.ROTATE_RIGHT, e.keyCode)) {
//			canvas.rotateRight();
//		}
		else if (CanvasKeys.containsKey(CanvasKeys.ZOOM_IN, e.keyCode)) {
			canvas.zoomIn();
		}
		else if (CanvasKeys.containsKey(CanvasKeys.ZOOM_OUT, e.keyCode)) {
			canvas.zoomOut();
		}
		
		// EDIT OPERATIONS:
		else if (CanvasKeys.containsKey(CanvasKeys.FINISH_SHAPE, e.keyCode) && mode.isAddOperation()) {
			canvas.getShapeEditor().finishCurrentShape(true);
		}
		else if (CanvasKeys.containsKey(CanvasKeys.FINISH_SHAPE, e.keyCode) && mode.equals(CanvasMode.SPLIT_SHAPE_LINE)) {
			canvas.getShapeEditor().finishSplitByLine();
		}
		else if (CanvasKeys.containsKey(CanvasKeys.DELETE_SHAPE, e.keyCode)) {
//			logger.debug("delete button pressed - removing selected!");
			canvas.getShapeEditor().removeSelected();
		}
		else if (CanvasKeys.containsKey(CanvasKeys.UNDO, e.keyCode)) {
//			logger.debug("delete button pressed - removing selected!");
			canvas.getUndoStack().undo();
		}
		else if (ka == CanvasKeys.ADD_POINT) {
			canvas.setMode(CanvasMode.ADD_POINT);
		}
//		else if (CanvasKeys.containsKey(CanvasKeys.ADD_SHAPE, e.keyCode)) {
//			if (canvas.getSettings().isEditingEnabled())
//				canvas.setMode(CanvasMode.ADD_SHAPE);
//		}		
//		else if (CanvasKeys.containsKey(CanvasKeys.TRANSLATE_LEFT, e.keyCode)) {
//			logger.debug("translating left");
//			canvas.translateLeft();
//		}
//		else if (CanvasKeys.containsKey(CanvasKeys.TRANSLATE_RIGHT, e.keyCode)) {
//			logger.debug("translating right");
//			canvas.translateRight();
//		}
//		else if (CanvasKeys.containsKey(CanvasKeys.TRANSLATE_UP, e.keyCode)) {
//			logger.debug("translating up");
//			canvas.translateUp();
//		}
//		else if (CanvasKeys.containsKey(CanvasKeys.TRANSLATE_DOWN, e.keyCode)) {
//			logger.debug("translating down");
//			canvas.translateDown();
//		}
		
		canvas.redraw();
		}
		catch(Throwable th) {
			logger.error(th.getMessage(), th);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		currentStateMask = e.stateMask & ~e.keyCode;
//		if (e.keyCode == CanvasKeys.MULTISELECTION_REQUIRED_KEY) {
//			logger.debug("ctrl released - deactivating multiselect");
//			canvas.getSettings().setMultiselect(false);
//		}
		
//		logger.debug("keyreleased: "+e.stateMask);
//		if (e.keyCode == CanvasKeys.MOVE_SHAPE_REQUIRED_KEY) {
//			canvas.setMode(modeBackup);
//		}
		
		canvas.redraw();
	}
	
	public int getCurrentStateMask() { return currentStateMask; }
}
