package eu.transkribus.swt_gui.transcription.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_extension.TrpTextLineType;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.transcription.LineEditor;

import org.dea.swt.canvas.CanvasKeys;
import org.dea.swt.canvas.shapes.ICanvasShape;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

public class LineEditorListener implements ModifyListener, KeyListener {
	private final static Logger logger = LoggerFactory.getLogger(LineEditorListener.class);
	
	TrpMainWidget mainWidget;
	LineEditor lineEditor;
	
	public LineEditorListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.lineEditor = mainWidget.getCanvas().getLineEditor();
		
		lineEditor.getTextField().addModifyListener(this);
		lineEditor.getTextField().addKeyListener(this);
	}
	
	private void jumpToNeighborLine(boolean previous) {
		TrpTextLineType tl = lineEditor.getShape();
		if (tl==null) return;
				
		TrpTextLineType nextLine = tl.getNeighborLine(previous, true);
		ICanvasShape shape = mainWidget.selectObjectWithData(nextLine, true, false);
		mainWidget.getScene().makeShapeVisible(shape);
		
		lineEditor.setFocus();
		lineEditor.getTextField().setSelection(lineEditor.getText().length());	
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!e.doit)
			return;
		
//		if (!autocomplete.getAdapter().isProposalPopupOpen()) {
//			Event newEvent = new Event();
//			newEvent.keyCode = SWT.ARROW_DOWN;
//			text.notifyListeners(SWT.KeyDown, newEvent);
//		}
		
		if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) { // enter pressed
			jumpToNeighborLine(CanvasKeys.isCtrlKeyDown(e));
		}
		else if (e.keyCode == SWT.ARROW_DOWN) {
			jumpToNeighborLine(false);
		}
		else if (e.keyCode == SWT.ARROW_UP) {
			jumpToNeighborLine(true);
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		
	}

	@Override
	public void modifyText(ModifyEvent e) {
		TrpTextLineType tl = lineEditor.getShape();
		if (tl == null)
			return;
				
//		if (tl.getTextEquiv()==null)
//			tl.setTextEquiv(new TextEquivType());
		
		logger.debug("updating text of line to: "+lineEditor.getText());
		tl.setUnicodeText(lineEditor.getText(), lineEditor);
	}

}
