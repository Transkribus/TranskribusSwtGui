package eu.transkribus.swt_gui.mainwidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.table_editor.TableUtils;

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
		
		// TEST
		if (isCtrlOrCommand && kc == 'c' && count == 3) {
			mw.showTrayNotificationOnChangelog(true);
		} 
		
		if (isCtrlOrCommand && kc == 'd' && count == 3) {
			mw.openDebugDialog();
		}
		
		else if (isCtrlOrCommand && kc == 'm' && count == 3) {
			mw.openSleak();
		}
		
		else if (isCtrlOrCommand && kc == 't' && count == 3) {
			mw.loadTestDocSpecifiedInLocalFile();
		}
						
		else if (!storage.isPageLocked() && isCtrlOrCommand && kc == 's') {
			mw.saveTranscription(false);
		}
		else if (isCtrlOrCommand && kc == 'f') {
			mw.openSearchDialog();
		}
		else if (isCtrlOrCommand && kc == 'r') {
			mw.reloadCurrentTranscript(false, false, null, null);
		}
		else if (isCtrlOrCommand && kc == 'o') {
			mw.loadLocalFolder();
		}
		
		// SOME TEST HOOKS FOR TABLES:
		if (false) {
		if (isCtrlOrCommand && kc == 'n') { // create table cell
			mw.getCanvas().setMode(CanvasMode.ADD_TABLECELL);
		} 
		else if (isCtrlOrCommand && kc == 'x') { // check table consistency
			TrpTableRegionType t = TableUtils.getTable(mw.getCanvas().getFirstSelected(), true);			
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
		}
		
		// TEST
//		else if (isCtrlOrCommand && kc == '1') {
//			mw.getCanvasWidget().toggleToolbarVisiblity(mw.getCanvasWidget().bar1); 
//			
//		}
//		else if (isCtrlOrCommand && kc == '2') {
//			mw.getCanvasWidget().toggleToolbarVisiblity(mw.getCanvasWidget().bar2);
//			
//		}		
		
		lastTime = time;
		lastKc = kc;
	}
	
}
