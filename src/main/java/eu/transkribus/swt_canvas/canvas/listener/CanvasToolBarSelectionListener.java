package eu.transkribus.swt_canvas.canvas.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.CanvasWidget;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.TableDimension;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.TrpCanvasAddMode;
import eu.transkribus.swt_gui.dialogs.ImageEnhanceDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class CanvasToolBarSelectionListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBarSelectionListener.class);
	
	CanvasWidget canvasWidget;

	public CanvasToolBarSelectionListener(CanvasWidget canvasWidget) {
		this.canvasWidget = canvasWidget;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		TrpMainWidget mw = TrpMainWidget.getInstance();
		SWTCanvas canvas = canvasWidget.getCanvas();
		CanvasToolBar toolbar = canvasWidget.getToolbar();
		
		canvas.setMode(getModeForSelectionEvent(e));
		logger.debug("mode = "+canvas.getMode());
		
		if (s == toolbar.getZoomIn()) {
			canvas.zoomIn();
		}
		else if (s == toolbar.getZoomOut()) {
			canvas.zoomOut();
		}		
		else if (s == toolbar.getOriginalSize()) {
			canvas.resetTransformation();
		}
		
//		else if (toolbar.getTranslateItem()!= null && s == toolbar.getTranslateItem().ti && e.detail != SWT.ARROW) {
//			switch (toolbar.getTranslateItem().getLastSelectedIndex()) {
//			case 0:
//				canvas.translateLeft();
//				break;
//			case 1:
//				canvas.translateRight();
//				break;
//			case 2:
//				canvas.translateUp();
//				break;
//			case 3:
//				canvas.translateDown();
//				break;
//			}
//		}
		
		else if (s == toolbar.getRotateItem().ti && e.detail != SWT.ARROW) {
			switch (toolbar.getRotateItem().getLastSelectedIndex()) {
			case 0:
				canvas.rotateLeft();
				break;
			case 1:
				canvas.rotateRight();
				break;
			case 2:
				canvas.rotate90Left();
				break;
			case 3:
				canvas.rotate90Right();
				break;
				
			case 4:
				canvas.translateLeft();
				break;
			case 5:
				canvas.translateRight();
				break;
			case 6:
				canvas.translateUp();
				break;
			case 7:
				canvas.translateDown();
				break;
			}
		}		
		
		else if (s == toolbar.getFitItem().ti && e.detail != SWT.ARROW) {
			switch (toolbar.getFitItem().getLastSelectedIndex()) {
			case 0:
				canvas.fitToPage();
				break;
			case 1:
				canvas.resetTransformation();
				break;
			case 2:
				canvas.fitWidth();
				break;
			case 3:
				canvas.fitHeight();
				break;				
			}
		}		

		else if (s == toolbar.getFocus()) {
			canvas.focusFirstSelected();
		}
		else if (s == toolbar.getRemoveShape()) {
			canvas.getShapeEditor().removeSelected();
		}
		else if (SWTUtil.isSubItemSelected(s, toolbar.getSimplifyEpsItem(), e.detail)) {
//		else if (s == toolbar.getSimplifyEpsItem().ti && e.detail != SWT.ARROW) {
			canvas.getShapeEditor().simplifySelected(Double.valueOf(toolbar.getSimplifyEpsItem().getSelected().getText()));
		}
		else if (s == toolbar.getUndo()) {
			canvas.getUndoStack().undo();
		}
		else if (s == toolbar.getMergeShapes()) {
			canvas.getShapeEditor().mergeSelected();
		}
		
		
		if (s == toolbar.getViewSettingsMenuItem()) {
//			SettingsDialog sd = new SettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getCanvas().getSettings(), getTrpSets());		
//			sd.open();
		}		
		else if (canvas.getMode() == TrpCanvasAddMode.ADD_OTHERREGION) {
			TrpCanvasAddMode.ADD_OTHERREGION.data = toolbar.getSelectedAddElementType(); 
		}

		
		if (s == toolbar.getViewSettingsMenuItem()) {
			mw.getUi().openViewSetsDialog();
		}
		else if (s == toolbar.getImageVersionDropdown().ti && e.detail != SWT.ARROW) {
			TrpMainWidget.getInstance().reloadCurrentImage();
		}
		else if (s == toolbar.getImgEnhanceItem()) {
			// TODO: open enhance dialog
			ImageEnhanceDialog d = new ImageEnhanceDialog(canvas.getShell());
			d.open();
		}
		
		// TABLE STUFF:
//		else if (s == toolbar.getDeleteRowItem()) {
//			mw.getCanvas().getShapeEditor().deleteTableRowOrColumn(mw.getCanvas().getFirstSelected(), TableDimension.ROW, true);
//		}
//		else if (s == toolbar.getDeleteColumnItem()) {
//			mw.getCanvas().getShapeEditor().deleteTableRowOrColumn(mw.getCanvas().getFirstSelected(), TableDimension.COLUMN, true);
//		}
//		else if (s == toolbar.getSplitMergedCell()) {
//			mw.getCanvas().getShapeEditor().splitMergedTableCell(mw.getCanvas().getFirstSelected(), true);
//		}
//		else if (s == toolbar.getRemoveIntermediatePtsItem()) {
//			mw.getCanvas().getShapeEditor().removeIntermediatePointsOfTableCell(mw.getCanvas().getFirstSelected(), true);
//		}

	}
	
	protected CanvasMode getModeForSelectionEvent(SelectionEvent e) {
		CanvasToolBar toolbar = canvasWidget.getToolbar();
		Object s = e.getSource();
		
		logger.debug("source = "+e.getSource());
		
		if (s.equals(toolbar.getAddElementDropDown().ti)) {
			logger.debug("getting mode for adding element...");
			if (e.detail != SWT.ARROW) {
				CanvasMode mode = toolbar.getModeMap().get(toolbar.getAddElementDropDown().getSelected());
				return mode!=null ? mode : CanvasMode.SELECTION;
			} else
				return CanvasMode.SELECTION;
		}
		else if (s.equals(toolbar.getSplitDropdown().ti)) {
			if (e.detail != SWT.ARROW) {
				CanvasMode mode = toolbar.getModeMap().get(toolbar.getSplitDropdown().getSelected());
				return mode!=null ? mode : CanvasMode.SELECTION;
			} else
				return CanvasMode.SELECTION;
		}
		else {
			CanvasMode mode = toolbar.getModeMap().get(e.getSource());
			return mode!=null ? mode : CanvasMode.SELECTION;
		}
	}

//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		try {
//			TrpMainWidget mw = TrpMainWidget.getInstance();
//			
//			logger.debug("toolbar item selected: "+e);
//			
//			super.widgetSelected(e);
//			
//			if (canvas.getMode() == TrpCanvasAddMode.ADD_OTHERREGION) {
//				TrpCanvasAddMode.ADD_OTHERREGION.data = canvas.getMainWidget().getCanvasWidget().getToolBar().getSelectedSpecialRegionType(); 
//			}
//				
//			Object s = e.getSource();
//			
//			if (s == toolbar.getViewSettingsMenuItem()) {
//				canvas.getMainWidget().getUi().openViewSetsDialog();
//			}
//			else if (s == toolbar.getImageVersionItem().ti && e.detail != SWT.ARROW) {
//				TrpMainWidget.getInstance().reloadCurrentImage();
//			}
//			else if (s == toolbar.getImgEnhanceItem()) {
//				// TODO: open enhance dialog
//				ImageEnhanceDialog d = new ImageEnhanceDialog(canvas.getShell());
//				d.open();
//			}
//			
//			// TABLE STUFF:
//			else if (s == toolbar.getDeleteRowItem()) {
//				mw.getCanvas().getShapeEditor().deleteTableRowOrColumn(mw.getCanvas().getFirstSelected(), TableDimension.ROW, true);
//			}
//			else if (s == toolbar.getDeleteColumnItem()) {
//				mw.getCanvas().getShapeEditor().deleteTableRowOrColumn(mw.getCanvas().getFirstSelected(), TableDimension.COLUMN, true);
//			}
//			else if (s == toolbar.getSplitMergedCell()) {
//				mw.getCanvas().getShapeEditor().splitMergedTableCell(mw.getCanvas().getFirstSelected(), true);
//			}
//			else if (s == toolbar.getRemoveIntermediatePtsItem()) {
//				mw.getCanvas().getShapeEditor().removeIntermediatePointsOfTableCell(mw.getCanvas().getFirstSelected(), true);
//			}
//		} catch (Throwable ex) {
//			canvas.getMainWidget().onError("Error", ex.getMessage(), ex);
//		}
//	}	

}
