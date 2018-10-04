package eu.transkribus.swt_gui.transcription;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
//import org.apache.commons.math3.util.Pair;
//import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.core.model.beans.pagecontent.WordType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.util.Utils;

import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolItem;

public class WordTranscriptionWidget extends ATranscriptionWidget {
	private final static Logger logger = LoggerFactory.getLogger(WordTranscriptionWidget.class);
	
	ToolItem applyTextFromWords;
	boolean isEditOfEmptyWord=false;
	
	public WordTranscriptionWidget(final Composite parent, int style, TrpSettings settings, TrpMainWidgetView view) {
		super(parent, style, settings, view);
		
		deleteWordTextItem.setEnabled(true);
		
		applyTextFromWords = new ToolItem(regionsPagingToolBar.getToolBar(), SWT.CHECK);
		applyTextFromWords.setImage(Images.getOrLoad("/icons/arrow_merge.png"));
		applyTextFromWords.setText("Sync with lines");
		applyTextFromWords.setToolTipText("Enable to sync the text in the words with the text in the corresponding lines - warning: overwrites the text content in the lines!");
		
		applyTextFromWords.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (currentRegionObject != null) {
					logger.debug("applying text from words to lines and region!");
					if (applyTextFromWords.getSelection()) {
						if (DialogUtil.showYesNoDialog(parent.getShell(), 
								"Synchronize word text to lines", "Do you really want to overwrite the text in the lines with text from the words?") == SWT.YES) {						
							applyTextFromWords();
						}
						else {
							applyTextFromWords.setSelection(false);
						}
					}
				}
			}
		});
		
//		applyTextFromWords.setEnabled(!isLinesInSyncWithWordsText());
	}
	
	@Override public TranscriptionLevel getType() { return TranscriptionLevel.WORD_BASED; }
	
	protected void applyTextFromWords() {
		if (currentRegionObject!=null) {
			currentRegionObject.applyTextFromWords();
		}
	}
	
	@Override 
	protected void onDataUpdated() {
//		applyTextFromWords.setEnabled(!isLinesInSyncWithWordsText());
	}
	
	public boolean isLinesInSyncWithWordsText() {
		return currentRegionObject == null || currentRegionObject.isLinesInSyncWithWordsText();		
	}
		
	
//	@Override protected void initCustomTagPaintListener() {
//	}
		
	@Override protected void onPaintTags(PaintEvent e) {
		if (false) return;
		
		if (currentRegionObject == null || currentRegionObject.getTextLine().isEmpty())
			return;

		// FIXME??
		Font oldFont = e.gc.getFont();
		// for all visible lines:
		for (int i = JFaceTextUtil.getPartialTopIndex(text); i <= JFaceTextUtil.getPartialBottomIndex(text); ++i) {
			TrpTextLineType line = getLineObject(i);
			if (line==null) {
				logger.error("Could not paint line tags - should not happen here!");
				return;
			}			
			
			int lo = text.getOffsetAtLine(i);
			
			TreeMap<TrpWordType, Pair<Integer, Integer>> wordRanges = getWordRanges(line);
			for (TrpWordType word : wordRanges.keySet()) {
				int wo = wordRanges.get(word).getLeft();
				logger.trace("i = " + i + " lo: " + lo + " word: "+word.getId()+" wo: "+wo);
				CustomTagList ctl = word.getCustomTagList();
				paintTagsFromCustomTagList(e, ctl, lo+wo);
			}
		}
		e.gc.setFont(oldFont); // needed ?? (most probably not)
	}
	
//	@Override
//	protected void initLineStyleListener() {
//		
//		// highlight selected line and draw line bullets:
//		addUserLineStyleListener(new LineStyleListener() {
//			@Override
//			public void lineGetStyle(LineStyleEvent event) {
//				if (text.getText().isEmpty())
//					return;
//								
//				int line = text.getLineAtOffset(event.lineOffset);				
//				
//				
//				// line bullets:
//				StyleRange style = new StyleRange();
//				style.metrics = new GlyphMetrics(0, 0, Integer.toString(text.getLineCount() + 1).length() * 12);
//				style.background = Colors.getSystemColor(SWT.COLOR_GRAY);
//				Bullet bullet = new Bullet(ST.BULLET_NUMBER, style);
//				event.bullet = bullet;
//				event.bulletIndex = line;
//				event.indent = 25;
//				event.alignment = textAlignment;
//				
//				List<StyleRange> styleList = new ArrayList<>();
//				highlightLines(event, styleList);		
////				logger.debug("nr of styles: "+styleList.size());
//				
//				event.styles = (StyleRange[]) ArrayUtils.addAll(event.styles, (StyleRange[]) styleList.toArray(new StyleRange[0]));
//			}
//		});
//		
//		// this one highlights words that are tagged:
////		text.addLineStyleListener(new LineStyleListener() {
////			@Override
////			public void lineGetStyle(LineStyleEvent event) {		
////				if (text.getText().isEmpty())
////					return;		
////				int lineIndex = text.getLineAtOffset(event.lineOffset);
////				TrpTextLineType lineObject = getLineObject(lineIndex);
////				if (lineObject==null)
////					return;
////				
////				List<StyleRange> styleList = TranscriptionUtil.getTagStylesForLine(lineObject, event.lineOffset);
////				
////				event.styles = (StyleRange[]) ArrayUtils.addAll(event.styles, (StyleRange[]) styleList.toArray(new StyleRange[0]));
////			}
////		});
//	}
	
//	@Override
//	protected void initModifyListener() { // no modifylistener needed in this widget
//		
//	}
		
//	@Override
//	protected void initCaretListener() {
//		CaretListener caretListener = new CaretListener() {
//			@Override
//			public void caretMoved(CaretEvent event) {
////				String word = Utils.parseWord(text.getText(), event.caretOffset);
////				logger.debug("caret moved word = "+word);
//				
//				updateObjects();
//			}
//		};
//		addUserCaretListener(caretListener);
//	}
	
	private void preventSomeStuff(VerifyEvent e) {
//		Pair<Integer, Integer> wiC = getCurrentWordIndex();
		Pair<Integer, Integer> wiLeft = getWordIndexAndRelativePosition(e.start);
		Pair<Integer, Integer> wiRight = getWordIndexAndRelativePosition(e.end);
		logger.debug("wiLeft = "+wiLeft+", wiRight = "+wiRight+" e = "+e);
		
		if (wiLeft.getLeft() == -1) { // // word was not found; should not happen but you know - bad things happen!
			logger.warn("word index not found (index = -1) - should not happen here -> won't do it!");
			e.doit = false;
			return;
		}
		
		boolean isMultipleWord = (wiLeft.getLeft() != wiRight.getLeft());
		boolean isSingleSelect = e.start == e.end;
				
//		logger.debug("word length: "+currentWordObject.getUnicodeText().length());
		// backspace and on left border of word
		if ( (lastKeyCode == SWT.BS && wiRight.getRight()==0) && isMultipleWord ) { 
			logger.debug("backspace on left boundary of word -> reinterpret as arrow left");
			sendTextKeyDownEvent(SWT.ARROW_LEFT);
			e.doit = false;
			return;
		}
		// prevent del on right border of word
		if (lastKeyCode == SWT.DEL && wiLeft.getRight()==currentWordObject.getUnicodeText().length() && isMultipleWord ) { 
			logger.debug("del on right boundary of word -> won't do it!");
			e.doit = false;
			return;
		}
		// reinterpret space at end of word as jump to next word
		if (lastKeyCode == SWT.SPACE && wiLeft.getRight()==currentWordObject.getUnicodeText().length() && isSingleSelect ) { 
			logger.debug("space on right boundary of word -> reinterpret as arrow right!");
			sendTextKeyDownEvent(SWT.ARROW_RIGHT);
			e.doit = false;
			return;
		}		
		// prevent deleting of an empty word:
		if (currentWordObject.getUnicodeText().isEmpty() && (lastKeyCode == SWT.DEL || lastKeyCode == SWT.BS)) {
			logger.debug("trying to delete an empty word -> won't do it!");
			e.doit = false;
			return;
		}
		// prevent changes over multiple words (if not the cases above):
		if (isMultipleWord) {
			logger.debug("trying to edit over multiple words -> won't do it!");
			e.doit = false;
			return;
		}
	}
	
	@Override
//	protected void onTextChangedFromUser(VerifyEvent e) {
	// FIXME: MACHE DAS A BISSL SCHÖNER!!!! (EXCEPTION SOLLTE AUCH KEINE GESCHMISSEN WERDEN!!) (oder doch?)
	// FIXME: derzeit wird der Text immer für das ganze Wort ersetzt und nicht indexbasiert so wie beim line-based editor!
	protected void onTextChangedFromUser(int start, int end, String replacementText) {
		if (currentRegionObject == null || currentLineObject==null || currentWordObject==null) {
			return;
		}
		int currentLineIndex = getCurrentLineIndex();
		if (currentLineIndex == -1) {
			return;
		}
		
		int wi = currentLineObject.getWordIndex(currentWordObject);
		if (wi == -1)
			throw new RuntimeException("Fatal exception: could not find word in text change from user!");
		
		// construct new word from replacement:
		String newWordText = "";
		Pair<Integer, Integer> wRange = new ArrayList<>(getWordRanges(currentLineObject).values()).get(wi);
//		int lineOffset = text.getOffsetAtLine(currentLineObject.getIndex());
		int lineOffset = text.getOffsetAtLine(currentLineIndex);

		if (!currentWordObject.getUnicodeText().isEmpty()) {
			int bi = start - lineOffset - wRange.getLeft();
			int ei = end - lineOffset - wRange.getLeft();
			logger.trace("bi = "+bi+" ei = "+ei);			
			StringBuilder sb = new StringBuilder(currentWordObject.getUnicodeText());
			newWordText = sb.replace(bi, ei, replacementText).toString();
		} else { // empty word, but in the editor the word is displayed with EMPTY_WORD_FILL!
			logger.trace("the edited word was empty");
			newWordText = replacementText;
			// set range to range of EMPTY_WORD_FILL: (???, not needed anymore...)
			start = lineOffset+wRange.getLeft();
			end = start+TrpWordType.EMPTY_WORD_FILL.length();
		}
		
		logger.trace("wRange = "+wRange+" lineOffset = "+lineOffset+" newWordText='"+newWordText+"'");
			
//		if (newWordText.isEmpty()) {
//			e.text = TrpWordType.EMPTY_WORD_FILL;
//		}
		
		// compute and set the new selection index - for an empty word just put it to the start
//		modifiedSelection = newWordText.isEmpty() ? start : start+replacementText.length();

		// send a text modified signal, s.t. the text change is actually applied to current word:
//		sendTextModifiedSignal(currentWordObject, newWordText, modifiedSelection, start, end, text); // TODO: only replace text!
		sendTextModifiedSignal(currentWordObject, /*newWordText, modifiedSelection,*/ 0, currentWordObject.getUnicodeText().length(), newWordText);
		
//		e.doit = false; // text gets replaced from TranscriptObserver
//		text.redraw();
	}
	
	@Override
	protected void initVerifyListener() {
		VerifyListener verifyListener = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				if (currentWordObject == null) {
					e.doit = false;
					return;
				}
				// prevent changes over multiple lines:
				preventChangeOverMultipleLines(e);
				if (e.doit == false)
					return;
				
				preventInsertionOfMultipleLines(e);
				if (e.doit == false)
					return;
				
				preventSomeStuff(e);
				if (e.doit == false)
					return;
				
				preventAndReinterpretDeletionOfWholeWord(e);
				if (e.doit == false)
					return;
				
				detectEditOfEmptyWord(e);
				if (e.doit == false)
					return;				
			}
		};
		addUserVerifyListener(verifyListener);
	}
	
	private void detectEditOfEmptyWord(VerifyEvent e) {
		if (!e.text.isEmpty() && currentWordObject.getUnicodeText().isEmpty()) {
			logger.debug("current word is empty and will be edited!");
			isEditOfEmptyWord = true;
//			int si = getWordStartIndex(currentWordObject);
//			e.doit = false;
//			text.replaceTextRange(si, TrpWordType.EMPTY_WORD_FILL.length(), e.text);
		}
	}
	
	/** If deletion is done, checks if whole word will be deleted and reinterprets to insert EMPTY_WORD_FILL instead */
	private void preventAndReinterpretDeletionOfWholeWord(VerifyEvent e) {
		if (e.text.isEmpty()) {
//			java.awt.Point wb = Utils.wordBoundary(text.getText(), e.start);
			java.awt.Point wb = Utils.wordBoundary(text.getText(), e.start);
			if (e.start == wb.x && e.end == wb.y) {
				logger.debug("This operation deletes the whole word -> reinterpret to fill up with: '"+TrpWordType.EMPTY_WORD_FILL+"'");
				logger.debug("wb = "+wb+" e = "+e);
				e.doit = false;
				text.replaceTextRange(e.start, e.end-e.start, TrpWordType.EMPTY_WORD_FILL);
			}
		}
	}
	
	private void preventInsertionOfMultipleLines(VerifyEvent e) {
		List<String> lines = Utils.getLines(e.text);
		if (lines.size() > 1)
			e.doit = false;
	}
	
	@Override
	protected void initVerifyKeyListener() {
		VerifyKeyListener verifyKeyListener = new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent e) {			
//				if (currentWordObject == null) {
//					updateWordObject();
//				}
				
				Pair<Integer, Integer> wi = getCurrentWordIndex();
				logger.debug("key/code = "+e.character+"/"+e.keyCode+", wi/wii = "+wi.getKey()+" / "+wi.getValue()+ ", currentLineIndex = "+getCurrentLineIndex());
				
				if (wi.getLeft() == -1) { // // word was not found; should not happen but you know - bad things happen!
					logger.warn("word index not found (index = -1) - should not happen here -> won't do it!");
					e.doit = false;
					return;
				}
				
				if (currentLineObject==null || currentWordObject==null) {
					logger.warn("line object or word object null - should not happen here -> won't do it!");
					e.doit = false;
					return;
				}
				
				// Jump to next word on tab or ctrl+arrow_right and to previous word on ctrl+arrow_left
				if ( (e.keyCode == SWT.TAB || (e.keyCode==SWT.ARROW_LEFT && CanvasKeys.isCtrlKeyDown(e)) || (e.keyCode==SWT.ARROW_RIGHT && CanvasKeys.isCtrlKeyDown(e)) ) 
						&& currentLineObject!=null && currentWordObject != null) {
					boolean prev = e.keyCode == SWT.ARROW_LEFT;
					TrpWordType nextWord = currentWordObject.getNeighborWord(prev, true, false);
					if (nextWord != currentWordObject) {
						updateData(nextWord.getLine().getRegion(), nextWord.getLine(), nextWord);
						sendSelectionChangedSignal();
						e.doit = false;
						return;
					}
				}

			}
		};
		addUserVerifyKeyListener(verifyKeyListener);
	}
	
	@Override
	protected List<StyleRange> getLineStyleRanges(int lineOffset) {		
		List<StyleRange> styleList = new ArrayList<>();
		
		int lineToHighlight = text.getLineAtOffset(lineOffset);
		TrpTextLineType line = getLineObject(lineToHighlight);
		int currentLineIndex = getCurrentLineIndex();
		TreeMap<TrpWordType, Pair<Integer, Integer>> wordRanges = getWordRanges(line);
		logger.trace("wordRanges.size = "+wordRanges.size());
		logger.trace("text: "+text.getText());
		
		for (TrpWordType word : wordRanges.keySet()) {
//			if (word!=null && word.getTextStyle()!=null) {
				Pair<Integer, Integer> r = wordRanges.get(word);
				TextStyle ts = GuiUtil.getSWTTextStyle(word.getTextStyle(), text.getFont().getFontData()[0], settings);
				StyleRange sr = new StyleRange(ts);
	
				sr.start = lineOffset + r.getLeft();
				sr.length = r.getRight() - r.getLeft();
				sr.foreground = Colors.getSystemColor(SWT.COLOR_BLACK);
								
				logger.trace("word = '"+word.getUnicodeText()+"'");
				logger.trace("stylerange = "+sr);
				
				// style for selected line:
				if (lineToHighlight == currentLineIndex) {
					sr.background = Colors.getSystemColor(SWT.COLOR_GRAY);
					// highlight word under cursor:
					if (currentWordObject == word) {									
						sr.foreground = Colors.getSystemColor(SWT.COLOR_BLUE);
					}
				}		
				
				styleList.add(sr);
//			}
		}		

//		TrpTextLineType tl = getLineObject(currentLineIndex);
//		logger.debug("highlightLines: lineToHighlight = " + lineToHighlight + " currentLineIndex = " + currentLineIndex);
			
		return styleList;
	}
		
	/** Returns the same as getWordIndex(int xIndex) but for the current x index. */
	private Pair<Integer, Integer> getCurrentWordIndex() {		
		return getWordIndexAndRelativePosition(text.getCaretOffset());
	}
	
	private int getWordStartIndex(TrpWordType word) {
		TrpTextLineType tl = word.getLine();
		Pair<Integer, Integer> wi = getWordRanges(tl).get(word);
		if (wi == null) {
			return -1;
		} else {
			logger.trace("tl index: "+tl.getIndex());
//			int lo = text.getOffsetAtLine(tl.getIndex());
			
			int lineIndex = currentRegionObject.getTrpTextLine().indexOf(tl);
			if (lineIndex == -1) {
				return -1;
			}
			
			int lo = text.getOffsetAtLine(lineIndex);
			return lo+wi.getLeft();
		}		
	}
	
	/** Returns a pair of indices specifying the word index for the given caretOffset and the 
	 * index of caretOffset relative to this word. (-1, -1) is returned if no word is found for xIndex. */
	private Pair<Integer, Integer> getWordIndexAndRelativePosition(int caretOffset) {
		TrpTextLineType tl = getLineObject(text.getLineAtOffset(caretOffset));
//		logger.debug("tl = "+tl);
		
		int xIndex = getXIndex(caretOffset);
				
//		String lineText = text.getLine(currentLineIndex);
				
		int i=0; // the current word index
		int l=0; // the length of the text already parsed through
		for (Pair<Integer, Integer> p : getWordRanges(tl).values()) {
			if (xIndex >= p.getKey() && xIndex <= p.getValue()) {
				return Pair.of(i, xIndex-l);
			}
			l += (p.getValue()-p.getKey())+1;
			++i;
		}
		
		return Pair.of(-1, -1);
	}
	
	/** Returns a map of words keying a pair of indices specifying [start, end) ranges of line x-indices that mark the start and end of a word. 
	 * The map is sorted according to the index of the word in its corresponding line i.e. in the order the text is constructed for this widget.
	 * For an empty word, TrpWordType.EMPTY_WORD_FILL is assumed!
	 * */
	private TreeMap<TrpWordType, Pair<Integer, Integer>> getWordRanges(TrpTextLineType tl) {		
		TreeMap<TrpWordType, Pair<Integer, Integer>> hm = new TreeMap<>(new Comparator<TrpWordType>() {
			@Override
			public int compare(TrpWordType o1, TrpWordType o2) {
				return new Integer(o1.getIndex()).compareTo(o2.getIndex());
			}
		});
		
		if (tl != null) {
			int i=0;
			for (WordType w : tl.getWord()) {
				TrpWordType tw = (TrpWordType)w;
				String wt = tw.getUnicodeText(); 
				
				if (wt.isEmpty()) { // an empty word is filled with EMPTY_WORD_FILL
					hm.put(tw, Pair.of(i, i+TrpWordType.EMPTY_WORD_FILL.length()));
					i += TrpWordType.EMPTY_WORD_FILL.length();
				} else {
					hm.put(tw, Pair.of(i, i+wt.length()));
					i += wt.length();
				}
				
				++i; // count whitespace character
			}
		}
		
		return hm;
	}
	
	@Override
	protected void updateSelection(boolean textChanged, boolean lineChanged, boolean wordChanged) {
		if (currentWordObject == null)
			text.setSelection(0);
		else if (textChanged && isEditOfEmptyWord) { // FIXME: solve this problem another way??
			int wi = getWordStartIndex(currentWordObject);
			text.setSelection(wi+currentWordObject.getUnicodeText().length());
			isEditOfEmptyWord = false;
		}
		else if (textChanged || wordChanged) { // text or word change from outside of transcription widget
			int wi = getWordStartIndex(currentWordObject);
//			logger.debug("wi = "+wi);
			if (wi != -1) {
				text.setSelection(wi);
			}
		}
	}
	
	/** Updates the current word object from the current caret offset. */
	@Override
	protected void updateWordObject() {
		Pair<Integer, Integer> wi = getWordIndexAndRelativePosition(text.getCaretOffset());
		TrpWordType newWord = getWordObject(wi.getLeft());
		if (newWord==null)
			return;
		
		logger.debug("updating word object, caret offset: "+text.getCaretOffset()+", word index and relative positon: "+wi+" newWord="+newWord.getId()+" changed="+(newWord != currentWordObject));
		
		if (newWord != currentWordObject) {
			currentWordObject = newWord;
			if (getType() == TranscriptionLevel.WORD_BASED) { // only send signal if in in word based editor -> should be true here always
				sendSelectionChangedSignal();
				text.redraw();
			}
		}
	}
	
	@Override public Pair<ITrpShapeType, Integer> getTranscriptionUnitAndRelativePositionFromOffset(int offset) {
		TrpTextLineType tl = getLineObject(text.getLineAtOffset(offset));
		if (tl==null)
			return null;
		
		Pair<Integer, Integer> wi = getWordIndexAndRelativePosition(offset);
		if (wi.getLeft()==-1)
			return null;
			
		return Pair.of((ITrpShapeType) tl.getWord(wi.getLeft()), wi.getRight());
	}
	
//	protected TrpWordType getWordFromOffset(int offset) {
//		TrpTextLineType tl = getLineObject(text.getLineAtOffset(offset));
//		if (tl==null)
//			return null;
//		
//		Pair<Integer, Integer> wi = getWordIndexAndRelativePosition(offset);
//		if (wi.getLeft()==-1)
//			return null;
//			
//		return tl.getWord(wi.getLeft());
//	}
	
	protected TrpWordType getWordObject(int wordIndex) {
		if (currentLineObject==null || wordIndex < 0 || wordIndex >= currentLineObject.getWord().size())
			return null;
		
		return (TrpWordType) currentLineObject.getWord().get(wordIndex);
	}
	
	@Override
	protected String getTextFromRegion() {
		if (currentRegionObject == null)
			return "";
		else
			return currentRegionObject.getTextFromWords(true);
	}
	
	public ToolItem getApplyTextFromWords() {
		return applyTextFromWords;
	}
	
	@Override public ITrpShapeType getTranscriptionUnit() { return currentWordObject; }
	@Override public Class<TrpWordType> getTranscriptionUnitClass() { return TrpWordType.class; }
	
	@Override public List<Pair<ITrpShapeType, IntRange>> getSelectedShapesAndRanges() {
		List<Pair<ITrpShapeType, IntRange>> selection = new ArrayList<>();
		if (currentRegionObject==null || currentLineObject==null || currentWordObject==null)
			return selection;
		
		Point s = text.getSelection();
		Pair<Integer, Integer> wi1 = getWordIndexAndRelativePosition(s.x);
		Pair<Integer, Integer> wi2 = getWordIndexAndRelativePosition(s.y);
		if (wi1.getLeft()==-1 || wi2.getLeft()==-1) {
			logger.warn("Warning: one of the words of the current selection was not found -> returning empty selection!");
			return selection;
		}
		int li1=text.getLineAtOffset(s.x);
		int li2=text.getLineAtOffset(s.y);
		
		for (int i=li1; i<=li2; ++i) {
			TrpTextLineType line = getLineObject(i);
			int startWord = (i==li1) ? wi1.getLeft() : 0;
			int endWord = (i==li2) ? wi2.getLeft() : line.getWordCount()-1;
			for (int j=startWord; j<=endWord; ++j) {
				TrpWordType word = line.getWord(j);
				int wordLength = word.getUnicodeText().length();
				int start=0;
				int length=wordLength;
				
				if (wordLength==0) { // special case for empty words, as those words are filled with EMPTY_WORD_FILL in the editor!!
					start = 0; length = 0;
				} else {
					if (i==li1 && j==wi1.getLeft()) { // this is the first word of the selection range
						start = wi1.getRight();
						length = wordLength - start;
					} 
					if (i==li2 && j==wi2.getLeft()) { // this is the last word of the selection range					
						length = wi2.getRight()-start;
						logger.trace("last word! wi2.getRight = "+wi2.getRight()+" start = "+start+" length = "+length);
					}
				}

				selection.add(Pair.of((ITrpShapeType) word, new IntRange(start, length)));
			}
		}
		
		return selection;
	}
	
	@Override public boolean selectCustomTag(CustomTag t) {
		// TODO
		return false;
	}
	
	@Override
	public TranscriptionLevel getTranscriptionLevel() {
		return TranscriptionLevel.WORD_BASED;
	}	
	
	@Override
	protected void initModifyListener() {
		ExtendedModifyListener extendedModifyListener = new ExtendedModifyListener() {
			@Override public void modifyText(ExtendedModifyEvent event) {
				
				logger.debug("modified event: "+event.start+"/"+event.length+"/"+event.replacedText);
				// now 'invert' the modification, 
				// i.e. compute start / end indices of the modification and the text that was replaced
				int start = event.start;
				int end = event.start + event.replacedText.length();
				String replacementText = "";
				if (event.length > 0)
					replacementText = text.getText(event.start, event.start+event.length-1);
				
				logger.debug("modified as such: "+start+"/"+end+"/"+replacementText);
				
				// send this information to this bloody method which causes the modification to be done in the underlying page element:
				onTextChangedFromUser(start, end, replacementText);
				
				redrawText(true);
//				text.redraw();
//				text.redrawRange(0, text.getCharCount(), true);
			}
		};
		addUserExtendedModifyListener(extendedModifyListener);
		
//		ModifyListener modifyListener = new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				logger.debug("modified: "+e);
//				
////				sendTextModifiedSignal(currentLineObject, text.getLine(getCurrentLineIndex()), text.getSelection().x);
//			}
//		};
//		addUserModifyListener(modifyListener);
	}
	
//	@Override public Point getSelectionRangeRelativeToTranscriptionUnit() {
//		Point s = text.getSelection();
//					
//		logger.debug("getSelectionRangeRelativeToTranscriptionUnit - selection = "+s);
////			if (text.getSelectionCount()==0) {
////				logger.debug("getSelectionRangeRelativeToTranscriptionUnit - no selection -> setting to whole word!");
////				return new Point(0, currentWordObject.getUnicodeText().length());
////			} 
//		
//		Pair<Integer, Integer> wi1 = getWordIndexAndRelativePosition(s.x);
//		TrpTextLineType tl1 = getLineObject(text.getLineAtOffset(s.x));
//		Pair<Integer, Integer> wi2 = getWordIndexAndRelativePosition(s.y);
//		
//		if (wi1.getLeft() == -1 || (wi1.getLeft() != wi2.getLeft()) ) { // not the same words or one word not found
//			return null;
//		}
//		else {
//			TrpWordType word = tl1.getWord(wi1.getLeft());
//			boolean isEmptyWord = word.getUnicodeText().isEmpty();
//			int start, length;
//			if (isEmptyWord) { // special case for empty words, as those words are filled with EMPTY_WORD_FILL in the editor!!
//				start = 0; length = 0;
//			} else {
//				start = wi1.getRight();
//				length = s.y-s.x;	
//			}
//
//			Point r = new Point(start, length);
//			logger.debug("getSelectionRangeRelativeToTranscriptionUnit - returning range = "+r);
//			return r;
//		}
//	}
}
