package eu.transkribus.swt_gui.transcription;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.transcription.autocomplete.StyledTextContentAdapter;
import eu.transkribus.swt_gui.transcription.autocomplete.TrpAutoCompleteField;

/**
 * A line editor for transcription. Note that the editor is invisible at first (i.e. added to a dummy shell). <br>
 * Call method setVisible with true and a corresponding TextLineType object to make it visible. If setVisible is called
 * with false it is made invisible again and the internal TextLineType object is set to null, i.e. the second argument
 * has no effect then!
 * @deprecated very very old and untested. currently not activated
 */
public class LineEditor extends CanvasShapeAttachWidget<TrpTextLineType> {
	private final static Logger logger = LoggerFactory.getLogger(LineEditor.class);

	StyledText textField;
	TrpAutoCompleteField autocomplete;
	
	public LineEditor(final SWTCanvas canvas, int style) {
		super(canvas, style, TrpTextLineType.class);
		
		textField = new StyledText(SWTUtil.dummyShell, SWT.SINGLE | SWT.BORDER);
		textField.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// this one highlights words that are tagged:
		textField.addLineStyleListener(new LineStyleListener() {
			@Override
			public void lineGetStyle(LineStyleEvent event) {				
				if (shape==null)
					return;
				
				List<StyleRange> styleList = ATranscriptionWidget.getTagStylesForLine(shape, event.lineOffset);
				event.styles = (StyleRange[]) ArrayUtils.addAll(event.styles, styleList.toArray(new StyleRange[0]));
			}
		});
		addWidget(textField);

		// autocomplete field:
		autocomplete = new TrpAutoCompleteField(textField, 
				new StyledTextContentAdapter(textField), new String[]{}, 
				KeyStroke.getInstance(SWT.CTRL, SWT.SPACE), null
				);
		autocomplete.getAdapter().setEnabled(false);
	}
	
	@Override protected boolean isDisabled() { return true; }
	
	@Override
	public boolean showThisEditor() {
		return canvas.getMainWidget().getTrpSets().isShowLineEditor();
	}
	
	public TrpAutoCompleteField getAutoComplete() { return autocomplete; }
	
	@Override
	public void updateData() {
		if (isDisabled())
			return;		
		
		Listener[] listeners = detachModifyListener();
		
		String textOfLine = "";
		if (shape!=null)
			textOfLine = shape.getUnicodeText();
		logger.debug("updateText, textOfLine: "+textOfLine+", text in field: "+textField.getText());
		
		// update label
		if (shape != null) {
			label.setText("Line "+(shape.getRegion().getLineIndex(shape)+1));
			label.pack();
		}
		else
			label.setText("no line set");
		
		// update text if changed
		if (textField.getText() == null || !textField.getText().equals(textOfLine)) { // only set new text if it has changed!
			textField.setText(textOfLine);
			changeTextFieldSizeToFit();
		}
		layout(true);
		
		attachModifyListener(listeners);
		
		textField.redraw();
	}
	
	public String getText() { return textField.getText(); }
	public StyledText getTextField() { return textField; }
	
	private Listener[] detachModifyListener() {
		Listener[] removing = textField.getListeners(SWT.Modify);
		for (Listener l : removing) {
			textField.removeListener(SWT.Modify, l);
		}
		return removing;
	}
	
	private void attachModifyListener(Listener[] listener) {
		for (Listener l : listener) {
			textField.addListener(SWT.Modify, l);
		}
	}
//	public StyledText getTextField() { return textField; }
			
	public void changeTextFieldSizeToFit() {
//		GC gc = new GC(textField);
//		int te = gc.textExtent(textField.getText()).x + 20;
//		
//		gc.dispose();
//		if (textField.getSize().x < te) {
//			int diff = te - textField.getSize().x;
//			textField.setSize(te, height);
//			Rectangle r = getBounds();
//			r.width += diff;
//			setBounds(r);
//		}
		
//		textField.setSize(Math.max(textField.getSize().x, minWidth), height);
	}

}
