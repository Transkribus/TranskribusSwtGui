package eu.transkribus.swt_gui.transcription.autocomplete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.util.Utils;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.IControlContentAdapter2;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * An {@link IControlContentAdapter} for SWT Text controls. This is a
 * convenience class for easily creating a {@link ContentProposalAdapter} for
 * text fields.
 * 
 * @since 3.2
 */
public class StyledTextContentAdapter implements IControlContentAdapter,
		IControlContentAdapter2 {
	private final static Logger logger = LoggerFactory.getLogger(StyledTextContentAdapter.class);
	
	StyledText textField;
	boolean INSERT_IS_REPLACE = false;
	
	public StyledTextContentAdapter(StyledText text) {
		this.textField = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#getControlContents(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public String getControlContents(Control control) {
		return Utils.parseWord(textField.getText(), textField.getCaretOffset());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String, int)
	 */
	@Override
	public void setControlContents(Control control, String text,
			int cursorPosition) {
		int index = Utils.wordStartIndex(textField.getText(), textField.getCaretOffset());
		String word = Utils.parseWord(textField.getText(), textField.getCaretOffset());
		logger.debug("setControlContents, wordIndex = "+index
//				+" char at: "+textField.getText().charAt(index)
				+ " word = "+word+" insert text = "+text+" total text length = "+textField.getText().length());
		
		textField.replaceTextRange(index, word.length(), text);
		textField.setCaretOffset(index+text.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String, int)
	 */
	@Override
	public void insertControlContents(Control control, String text,
			int cursorPosition) {
		if (INSERT_IS_REPLACE) { // replace instead of insert when flag is set
			setControlContents(control, text, cursorPosition);
		}
		else {
			Point selection = textField.getSelection();
			textField.insert(text);
			// Insert will leave the cursor at the end of the inserted text. If this
			// is not what we wanted, reset the selection.
			if (cursorPosition < text.length()) {
				textField.setSelection(selection.x + cursorPosition,
						selection.x + cursorPosition);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public int getCursorPosition(Control control) {
		return textField.getCaretOffset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse.swt.widgets.Control)
	 */
	@Override
	public Rectangle getInsertionBounds(Control control) {
//		logger.debug("getting insertion bounds!!!!");
		
		Point caretOrigin = textField.getCaret().getLocation();
		// We fudge the y pixels due to problems with getCaretLocation
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=52520
		return new Rectangle(caretOrigin.x + textField.getClientArea().x,
				caretOrigin.y + textField.getClientArea().y + 3, 1, textField.getLineHeight());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse.swt.widgets.Control,
	 *      int)
	 */
	@Override
	public void setCursorPosition(Control control, int position) {
		textField.setSelection(new Point(position, position));
	}

	/**
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#getSelection(org.eclipse.swt.widgets.Control)
	 * 
	 * @since 3.4
	 */
	@Override
	public Point getSelection(Control control) {
		return textField.getSelection();
	}

	/**
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#setSelection(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.swt.graphics.Point)
	 * 
	 * @since 3.4
	 */
	@Override
	public void setSelection(Control control, Point range) {
		textField.setSelection(range);
	}
}

