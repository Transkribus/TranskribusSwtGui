package eu.transkribus.swt_gui.mainwidget.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt_canvas.canvas.CanvasKeys;
import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.TrpCanvasAddMode;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.table_editor.TableUtils.TrpTableCellsMissingException;

public class TrpMainWidgetKeyListener implements Listener {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetKeyListener.class);
	
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	Storage storage = Storage.getInstance();
	
	long lastTime=0;
	int count=0;
	int lastKc=-1;
	
	public TrpMainWidgetKeyListener(TrpMainWidget mw) {
		this.mw = mw;
		this.ui = mw.getUi();
	}
	
	@Override public void handleEvent(Event event) {
		if (event.type != SWT.KeyDown)
			return;
		
		// check if mainwidget shell is active, return elsewise
		if (!mw.getShell().equals(Display.getCurrent().getActiveShell()))
			return;
			
		int kc = event.keyCode;
		long time = System.currentTimeMillis();
		long diff = time-lastTime;
		boolean timeThresholdExceeded = diff > 1000;
		if (timeThresholdExceeded || lastKc!=kc) {
			count = 0;
		}
		count++;
		
		logger.trace("kc = "+kc+", lastKc = "+lastKc+", count = "+count+", timeThresholdExceeded="+timeThresholdExceeded);
		
		boolean isCtrlOrCommand = CanvasKeys.isCtrlOrCommandKeyDown(event.stateMask);
		boolean isAlt = CanvasKeys.isAltKeyDown(event.stateMask);
		boolean isCtrlAlt = isCtrlOrCommand && isAlt;
		
		if (CanvasKeys.containsKey(CanvasKeys.SET_SELECTION_MODE, kc)) {
			mw.getCanvas().setMode(CanvasMode.SELECTION);
		}
		
		// enable debug mode on ctrl-d-d-d:
		if (isCtrlOrCommand && kc == 'd' && count == 3) {
			mw.showDebugDialog();
		}
		
		// load local testset on crtl-t-t-t:
		else if (isCtrlOrCommand && kc == 't' && count == 3) {
			logger.debug("loading local testset!");
			mw.loadLocalTestset();
		}
						
		// save transcript:
		else if (!storage.isPageLocked() && isCtrlOrCommand && kc == 's') {
			mw.saveTranscription(false);
		}
		else if (isCtrlOrCommand && kc == 'f') {
			mw.openSearchDialog();
		}
		else if (isCtrlOrCommand && kc == 'r') {
			logger.debug("reloading current transcript!");
			mw.reloadCurrentTranscript(false, false);
		}
		else if (isCtrlOrCommand && kc == 'o') {
			logger.debug("open local folder!");
			mw.loadLocalFolder();
		}
		
		// SOME TEST HOOKS FOR TABLES:
		if (isCtrlOrCommand && kc == 'c') { // create table cell
			mw.getCanvas().setMode(TrpCanvasAddMode.ADD_TABLECELL);
		} else if (isCtrlOrCommand && kc == 'x') { // check table consistency
			TrpTableRegionType t = TableUtils.getTable(mw.getCanvas().getFirstSelected());			
			if (t != null) {
				try {
					TableUtils.checkTableConsistency(t);
					DialogUtil.showInfoMessageBox(mw.getShell(), "Success", "Everything ok with table cells!");
				} catch (Exception e) {
					logger.debug(e.getMessage(), e);
					DialogUtil.showErrorMessageBox(mw.getShell(), "Something's wrong with the table", e.getMessage());
				}
			}
		} 
		else if (isCtrlOrCommand && kc == 'y') { // split merged table cells
			try {
				mw.getCanvas().getShapeEditor().splitMergedTableCell(mw.getCanvas().getFirstSelected(), true);
			} catch (Throwable e) {
				mw.onError("Error", e.getMessage(), e);
			}
		} 
		else if (isCtrlOrCommand && kc == 'b') {
			try {
				mw.getCanvas().getShapeEditor().removeIntermediatePointsOfTableCell(mw.getCanvas().getFirstSelected(), true);
			} catch (Throwable e) {
				mw.onError("Error", e.getMessage(), e);
			}
			
			
		}
		
		lastTime = time;
		lastKc = kc;
	}

}
