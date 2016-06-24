package eu.transkribus.swt_canvas.canvas.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_gui.dialogs.SettingsDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CanvasToolBarSelectionListener extends SelectionAdapter {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBarSelectionListener.class);
	
	CanvasToolBar toolbar;
	SWTCanvas canvas;

	public CanvasToolBarSelectionListener(CanvasToolBar toolbar, SWTCanvas canvas) {
		this.toolbar = toolbar;
		this.canvas = canvas;
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		
		canvas.setMode(getModeForSelectionEvent(e));
		logger.debug("mode = "+canvas.getMode());
		
//		logger.debug("source = "+s);
//		if (s == toolbar.getSelectionMode()) {
//			canvas.getSettings().mode = CanvasMode.SELECTION;
//		}
//		else if (s == toolbar.getZoomSelection()) {
//			canvas.getSettings().mode = CanvasMode.ZOOM;
//		}
		
		if (s == toolbar.getZoomIn()) {
			canvas.zoomIn();
		}
		else if (s == toolbar.getZoomOut()) {
			canvas.zoomOut();
		}		
//		else if (s == toolbar.getRotateLeft()) {
//			canvas.rotateLeft();
//		}
//		else if (s == toolbar.getRotateRight()) {
//			canvas.rotateRight();
//		}
//		else if (s == toolbar.getFitToPage()) {
//			canvas.fitToPage();
//		}
//		else if (s == toolbar.getFitWidth()) {
//			canvas.fitWidth();
//		}
//		else if (s == toolbar.getFitHeight()) {
//			canvas.fitHeight();
//		}
		else if (s == toolbar.getOriginalSize()) {
			canvas.resetTransformation();
		}
		else if (s == toolbar.getTranslateItem().ti && e.detail != SWT.ARROW) {
			switch (toolbar.getTranslateItem().getLastSelectedIndex()) {
			case 0:
				canvas.translateLeft();
				break;
			case 1:
				canvas.translateRight();
				break;
			case 2:
				canvas.translateUp();
				break;
			case 3:
				canvas.translateDown();
				break;
			}
		}
		
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
		
//		else if (s == toolbar.getTranslateLeft()) {
//			canvas.translateLeft();
//		}
//		else if (s == toolbar.getTranslateRight()) {
//			canvas.translateRight();
//		}
//		else if (s == toolbar.getTranslateUp()) {
//			canvas.translateUp();
//		}
//		else if (s == toolbar.getTranslateDown()) {
//			canvas.translateDown();
//		}
		else if (s == toolbar.getFocus()) {
			canvas.focusFirstSelected();
		}
		else if (s == toolbar.getRemoveShape()) {
			canvas.getShapeEditor().removeSelected();
		}
		else if (s == toolbar.getSimplifyEpsItem().ti && e.detail != SWT.ARROW) {
			canvas.getShapeEditor().simplifySelected(Double.valueOf(toolbar.getSimplifyEpsItem().getSelected().getText()));
		}
		else if (s == toolbar.getUndo()) {
			canvas.getUndoStack().undo();
		}
		else if (s == toolbar.getMergeShapes()) {
			canvas.getShapeEditor().mergeSelected();
		}
		else if (s == toolbar.getViewSettingsMenuItem()) {
//			SettingsDialog sd = new SettingsDialog(getShell(), /*SWT.PRIMARY_MODAL|*/ SWT.DIALOG_TRIM, getCanvas().getSettings(), getTrpSets());		
//			sd.open();
		}
	}
	
	protected CanvasMode getModeForSelectionEvent(SelectionEvent e) {
		CanvasMode mode = toolbar.getModeMap().get(e.getSource());
		return mode!=null ? mode : CanvasMode.SELECTION;
	}

//public class DeaSWTCanvasSelectionAdapter extends SelectionAdapter {
//	DeaSWTCanvasWidget view;
//	DeaSWTCanvas canvas;
//
//	public DeaSWTCanvasSelectionAdapter(DeaSWTCanvasWidget view) {
//		this.view = view;
//		this.canvas = view.getCanvas();
//	}
//	
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		if (e.getSource() == view.getSelectionMode()) {
//			canvas.getSettings().mode = DeaSWTCanvasMode.SELECTION;
//		}
//		else if (e.getSource() == view.getZoomSelection()) {
//			canvas.getSettings().mode = DeaSWTCanvasMode.ZOOM;
//		}
//		else if (e.getSource() == view.getZoomIn()) {
//			canvas.zoomIn();
//		}
//		else if (e.getSource() == view.getZoomOut()) {
//			canvas.zoomOut();
//		}		
//		else if (e.getSource() == view.getRotateLeft()) {
//			canvas.rotateLeft();
//		}
//		else if (e.getSource() == view.getRotateRight()) {
//			canvas.rotateRight();
//		}
//		else if (e.getSource() == view.getFitToPage()) {
//			canvas.fitToPage();
//		}
//		else if (e.getSource() == view.getOriginalSize()) {
//			canvas.resetTransformation();
//		}
//		else if (e.getSource() == view.getTranslateLeft()) {
//			canvas.translateLeft();
//		}
//		else if (e.getSource() == view.getTranslateRight()) {
//			canvas.translateRight();
//		}
//		else if (e.getSource() == view.getTranslateUp()) {
//			canvas.translateUp();
//		}
//		else if (e.getSource() == view.getTranslateDown()) {
//			canvas.translateDown();
//		}		
//		
//	}
}
