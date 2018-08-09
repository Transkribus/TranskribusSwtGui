package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.ImageEnhanceDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class CanvasToolBarSelectionListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBarSelectionListener.class);
	
	CanvasWidget canvasWidget;
	ImageEnhanceDialog imgEnhanceDialog;
	CanvasToolBarNew tb;

	public CanvasToolBarSelectionListener(CanvasWidget canvasWidget) {
		this.canvasWidget = canvasWidget;
		tb = this.canvasWidget.getToolbar();
		
		attach();
	}
	
	private void attach() {
		SWTUtil.addSelectionListener(tb.selectionMode, this);
		SWTUtil.addSelectionListener(tb.zoomSelection, this);
		SWTUtil.addSelectionListener(tb.zoomIn, this);
		SWTUtil.addSelectionListener(tb.zoomOut, this);
		SWTUtil.addSelectionListener(tb.loupe, this);
//		SWTUtil.addToolItemSelectionListener(tb.rotateLeft, this);
//		SWTUtil.addToolItemSelectionListener(tb.rotateRight, this);
		SWTUtil.addSelectionListener(tb.fitItem, this);
		SWTUtil.addSelectionListener(tb.rotateItem, this);
//		SWTUtil.addToolItemSelectionListener(tb.translateItem.ti, this);
		
		SWTUtil.addSelectionListener(tb.focus, this);
		SWTUtil.addSelectionListener(tb.addPoint, this);
		SWTUtil.addSelectionListener(tb.removePoint, this);
//		SWTUtil.addSelectionListener(tb.addShape, this);
		SWTUtil.addSelectionListener(tb.removeShape, this);
		SWTUtil.addSelectionListener(tb.simplifyEpsItem, this);
		SWTUtil.addSelectionListener(tb.undo, this);
		
		SWTUtil.addSelectionListener(tb.splitHorizontalItem, this);
		SWTUtil.addSelectionListener(tb.splitVerticalItem, this);
		SWTUtil.addSelectionListener(tb.splitLineItem, this);
		
		SWTUtil.addSelectionListener(tb.splitShapeLine, this);
		SWTUtil.addSelectionListener(tb.splitShapeWithVerticalLine, this);
		SWTUtil.addSelectionListener(tb.splitShapeWithHorizontalLine, this);
		
		SWTUtil.addSelectionListener(tb.mergeShapes, this);
	
		SWTUtil.addSelectionListener(tb.imageVersionDropdown, this);
		
//		SWTUtil.addSelectionListener(tb.addPrintspace, this);
//		SWTUtil.addSelectionListener(tb.addTextRegion, this);
//		SWTUtil.addSelectionListener(tb.addLine, this);
//		SWTUtil.addSelectionListener(tb.addBaseLine, this);
//		SWTUtil.addSelectionListener(tb.addWord, this);
		
		SWTUtil.addSelectionListener(tb.addTextRegionItem, this);
		SWTUtil.addSelectionListener(tb.addLineItem, this);
		SWTUtil.addSelectionListener(tb.addBaselineItem, this);
		SWTUtil.addSelectionListener(tb.addWordItem, this);
		
		SWTUtil.addSelectionListener(tb.addElementDropdown, this);
		SWTUtil.addSelectionListener(tb.splitDropdown, this);

		SWTUtil.addSelectionListener(tb.viewSettingsMenuItem, this);
		
		SWTUtil.addSelectionListener(tb.imgEnhanceItem, this);
		
		SWTUtil.addSelectionListener(tb.markupItem, this);
		
		SWTUtil.addSelectionListener(tb.helpItem, this);
		SWTUtil.addSelectionListener(tb.canvasHelpItem, this);
		
		SWTUtil.addSelectionListener(tb.createDefaultLineItem, this);
		SWTUtil.addSelectionListener(tb.createImageSizeTextRegionItem, this);
		
		// table stuff
//		SWTUtil.addSelectionListener(tb.deleteRowItem, this);
//		SWTUtil.addSelectionListener(tb.deleteColumnItem, this);
//		SWTUtil.addSelectionListener(tb.splitMergedCell, this);
//		SWTUtil.addSelectionListener(tb.removeIntermediatePtsItem, this);
		
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		TrpMainWidget mw = TrpMainWidget.getInstance();
		SWTCanvas canvas = canvasWidget.getCanvas();
		CanvasToolBarNew toolbar = canvasWidget.getToolbar();
		
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
		else if (toolbar.getRotateItem()!=null && s == toolbar.getRotateItem().ti && e.detail != SWT.ARROW) {
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
		else if (toolbar.getFitItem()!=null && s == toolbar.getFitItem().ti && e.detail != SWT.ARROW) {
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
		else if (s == toolbar.getBorderMarkupDialog()) {
//			canvas.getTableMarkup().set(canvas.getShapeEditor().retrieveExistingBordersForTableCells(canvas.getScene().getSelectedTableCellShapes()));
			canvas.getTableMarkup().show();
		}


		if (s == toolbar.getViewSettingsMenuItem()) {
			mw.openViewSetsDialog();
		}		
		else if (canvas.getMode() == CanvasMode.ADD_OTHERREGION) {
			CanvasMode.ADD_OTHERREGION.data = toolbar.getSelectedAddElementType(); 
		}
		
		if (s == toolbar.getViewSettingsMenuItem()) {
			mw.getUi().getTabWidget().selectServerTab();
		}
		else if (s == toolbar.getImageVersionDropdown().ti && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
			TrpMainWidget.getInstance().reloadCurrentImage();
		}
		else if (s == toolbar.getImgEnhanceItem()) {
			// TODO: open enhance dialog			
			
			if(imgEnhanceDialog == null){
				imgEnhanceDialog = new ImageEnhanceDialog(canvas.getShell());
				imgEnhanceDialog.open();
			}else{
				imgEnhanceDialog.setActive();
			}
		}
		else if (s == toolbar.getHelpItem()) {
			mw.openHowToGuides();
		}
		else if (s == toolbar.getCanvasHelpItem()) {
			mw.openCanvasHelpDialog();
		}
		else if (s == toolbar.createDefaultLineItem) {
			mw.createDefaultLineForSelectedShape();
		}
		else if (s == toolbar.createImageSizeTextRegionItem) {
			mw.createImageSizeTextRegion();
		}

	}
	
	protected CanvasMode getModeForSelectionEvent(SelectionEvent e) {
		CanvasToolBarNew toolbar = canvasWidget.getToolbar();
		Object s = e.getSource();
		
		logger.debug("source = "+e.getSource());
		if (false) { return CanvasMode.SELECTION; }
		else if (toolbar.getAddElementDropDown()!=null && s.equals(toolbar.getAddElementDropDown().ti)) {
			logger.debug("getting mode for adding element...");
			if (e.detail != SWT.ARROW) {
				CanvasMode mode = toolbar.getModeMap().get(toolbar.getAddElementDropDown().getSelected());
				return mode!=null ? mode : CanvasMode.SELECTION;
			} else
				return CanvasMode.SELECTION;
		}
//		else if (s.equals(toolbar.getSplitDropdown().ti)) {
//			if (e.detail != SWT.ARROW) {
//				CanvasMode mode = toolbar.getModeMap().get(toolbar.getSplitDropdown().getSelected());
//				return mode!=null ? mode : CanvasMode.SELECTION;
//			} else
//				return CanvasMode.SELECTION;
//		}
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
