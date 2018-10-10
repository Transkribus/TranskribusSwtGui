package eu.transkribus.swt_gui.transcription;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.catti.TrpCattiClientEndpoint;
import eu.transkribus.client.catti.TrpCattiClientEndpoint.CattiMessageHandler;
import eu.transkribus.core.catti.CattiMethod;
import eu.transkribus.core.catti.CattiRequest;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.enums.TranscriptionLevel;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.util.Utils;
import junit.framework.Assert;

public class LineTranscriptionWidget extends ATranscriptionWidget {
	private final static Logger logger = LoggerFactory.getLogger(LineTranscriptionWidget.class);
	
	List<Pair<TrpTextLineType, IntRange>> lastReplacements;
	
	TrpCattiClientEndpoint ce;
	long lastTextChange=0;
	boolean cattiEditFlag=false;
		
	public LineTranscriptionWidget(Composite parent, int style, TrpSettings settings, final TrpMainWidgetView view) {
		super(parent, style, settings, view);
		
		deleteWordTextItem.setEnabled(false);
		
		logger.debug("spacing before = "+text.getLineSpacing());
	}
	
	@Override public TranscriptionLevel getType() { return TranscriptionLevel.LINE_BASED; }
			
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
				
//				redrawText(true); // OLD --> ineffiecient!!
				updateLineStylesForCharacterOffsets(start, end);
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
	
	private void preventDelAndBackspace(VerifyEvent e) {
		// one char max between start and end
		boolean isSingleSelect = (e.end - e.start < 2); // (e.start == e.end);
		
		// prevent backspace on start of line
		int xIndex = getCurrentXIndex();
		if (lastKeyCode == SWT.BS && xIndex == 0 && isSingleSelect && e.text.equals("")) { 
			logger.debug("preventing bs");
			e.doit = false;
			return;
		}
		
		// prevent del on end of line
		int lineLength = text.getLine(text.getLineAtOffset(text.getCaretOffset())).length();
		if (lastKeyCode == SWT.DEL && xIndex == lineLength && isSingleSelect && e.text.equals("")) { 
			logger.debug("preventing del");
			e.doit = false;
			return;
		}
	}
	
	@Override public Pair<ITrpShapeType, Integer> getTranscriptionUnitAndRelativePositionFromOffset(int offset) {
		if (offset < 0 || offset > text.getCharCount())
			return null;
		
		int li = text.getLineAtOffset(offset);
		ITrpShapeType line = getLineObject(li);
		if (line==null)
			return null;
		
		return Pair.of(line, offset-text.getOffsetAtLine(li));
	}
	
//	protected void onTextChangedFromUser(VerifyEvent e) {
	protected void onTextChangedFromUser(final int start, final int end, final String replacementText) {
		lastTextChange = System.currentTimeMillis();
		
		logger.debug("text changed from user: "+start+"/"+end+"/"+replacementText);
		List<String> linesToInsert = Utils.getLines(replacementText);
		Assert.assertTrue("Nr of affected lines must be equal to inserted lines", 
				linesToInsert.size()==lastReplacements.size());
						
		logger.debug("last replacements: "+lastReplacements.size());
		for (int i=0; i<lastReplacements.size(); ++i) {
			logger.debug("replacement: ");
			String lineReplacement = linesToInsert.get(i);
			Pair<TrpTextLineType, IntRange> p = lastReplacements.get(i);
			TrpTextLineType line = p.getLeft();
			IntRange r = p.getRight();
			sendTextModifiedSignal(line, r.offset, r.offset+r.length, lineReplacement);
		}
		
		reloadWordGraphMatrix(true);
		if (wordGraphEditor.isEditing()) {
			int endOfEdit = start+replacementText.length();
			logger.debug("setting caret offset to: "+endOfEdit);
			text.setFocus();
			text.setCaretOffset(endOfEdit);
		}
		
		if (false) {
			// actions performed after DIFF_T ms of no text change:
			final long DIFF_T = 500;
			new Timer().schedule(new TimerTask() {
				@Override public void run() {
	    			long diff = System.currentTimeMillis() - lastTextChange;
	    			logger.debug("diff = "+diff);
	    			if (diff >= DIFF_T) {
	    				Display.getDefault().asyncExec(new Runnable() {
							@Override public void run() {
								if (isCattiMode()) {
									logger.debug("starting cattiOnEdit!");
									callCattiMethod(CattiMethod.SET_PREFIX, start, end, replacementText);
								}
							}
						});
	    			}
				}
			}, DIFF_T);
		}
	}
	
	private boolean isCattiMode() {
		return enableCattiItem!=null && enableCattiItem.getSelection();
	}
	
	private boolean isCattiEndpointOpen() {
		return ce != null && ce.isOpen();
	}
	
	private void replaceTextFromCattiResponose(CattiRequest message) {
		int co = text.getCaretOffset();
		int li = text.getLineAtOffset(co);
		String lineTxt = text.getLine(li);
		int lo = text.getOffsetAtLine(li);
		int e = co - lo;
		int nwbi = e;
//		if (message.getMethod() == CattiMethod.SET_PREFIX)
//			nwbi = CoreUtils.findNextWordBoundaryIndex(lineTxt, e);
		
		String prefix = lineTxt.substring(0, nwbi);
		prefix = CoreUtils.tokenizeForCATTI(prefix); // have to tokenize to ensure that prefix from text field and catti response are equal!
		
		String suffix = lineTxt.substring(nwbi, lineTxt.length());
//		String suffixDetokenized = CoreUtils.detokenizeForCATTI(suffix);
		
		logger.trace("1prefix = '"+prefix+"'");
		logger.trace("1suffix = '"+suffix+"'");
		if (!prefix.equals(message.getPrefix())) {
			logger.error("prefix do not match: " + prefix + " / "+message.getPrefix());
			return;
		}
		logger.trace("replacing text!");
		cattiEditFlag = true; // prevents an endless loop
		
		int cutIndex = prefix.length() > message.getCorrected_translation_out().length() ? message.getCorrected_translation_out().length() : prefix.length();
//		text.replaceTextRange(nwbi+lo, suffix.length(), message.getCorrected_translation_out().substring(cutIndex));

		// with detokinization:
		String suffixFromTranlationOut = message.getCorrected_translation_out().substring(cutIndex);
		int nWhitespaces = CoreUtils.getNOfRepeatingChars(suffixFromTranlationOut, 0, ' ');
		logger.trace("suffixFromTranlationOut: '"+suffixFromTranlationOut+"'"+" nWhitespaces = "+nWhitespaces);
		String replaceText = CoreUtils.detokenizeForCATTI("    "+suffixFromTranlationOut);
		logger.trace("replaceText: '"+replaceText+"'");
		
		// add whitespaces from beginning removed by detokenization: 
		if (nWhitespaces>0)
			replaceText = StringUtils.repeat(' ', nWhitespaces) + replaceText;
		
		// add extra space if suffix started with that. Elsewise, it would have been deleted by the tokenization!
//		if (false) {
//		if (suffixFromTranlationOut.startsWith(" ") && !replaceText.startsWith(" "))
////		if (suffix.startsWith(" "))
//			replaceText = " "+replaceText;
//		}
		
		logger.trace("replaceText1: "+replaceText);
		
		text.replaceTextRange(nwbi+lo, suffix.length(), replaceText);
	}
		
	private void callCattiMethod(CattiMethod method, final int start, final int end, final String replacementText) {
		logger.debug("callCattiMethod: "+method);
		
		if (cattiEditFlag) {
			cattiEditFlag = false;
			logger.debug("preventing double call...");
			return;
		}
		
		final TrpMainWidget mw = TrpMainWidget.getInstance();
		final Storage store = Storage.getInstance();
		if (store.isRemoteDoc() && store.isPageLoaded() && currentLineObject != null) {
			int docid = store.getDoc().getId();
			int pid = store.getPage().getPageNr();
			String lid = currentLineObject.getId();
			try {
				if (!isCattiEndpointOpen()) {
					logger.debug("creating new catti endpoint on url: "+mw.getTrpSets().getCattiServerUrl());
					
					ce = new TrpCattiClientEndpoint(mw.getTrpSets().getCattiServerUrl(), store.getUserId(), docid, pid, lid);
					ce.addMessageHandler(new CattiMessageHandler() {
						@Override public void handleMessage(final CattiRequest request) {
							String msg = "";
							if (request.hasError()) {
								msg = "CATTI error: "+request.getError() + "(message: "+request.toString()+")";
							} else {
								msg = "CATTI out: "+request.getCorrected_translation_out() + "(message: "+request.toString()+")";
								Display.getDefault().asyncExec(new Runnable() {
									@Override public void run() {
										replaceTextFromCattiResponose(request);
									}
								});
							}
							logger.debug("message = "+msg);
//							mw.appendDebugLog(msg);
							
							for (ITranscriptionWidgetListener l : listener)
								l.onCattiMessage(request, msg);
							
//							setChangedAndNotifyObservers(new CattiMessageEvent(request));
						}
					});
				} else {
					logger.debug("catti endpoint already exists!");
				}
				
				int co = text.getCaretOffset();
				int li = text.getLineAtOffset(co);
				logger.debug("li = "+li);
				String lineTxt = text.getLine(li);
				logger.debug("lineTxt = "+lineTxt);
				int lo = text.getOffsetAtLine(li);
				logger.debug("lo = "+lo+" co: "+co);
				int e = co - lo;
				
				int nwbi = e;
//				if (method == CattiMethod.SET_PREFIX)
//					nwbi = CoreUtils.findNextWordBoundaryIndex(lineTxt, e);
//				int nwbi = e;
				logger.debug("nwbi = "+nwbi);
				
				String prefix = lineTxt.substring(0, nwbi);
//				prefix = prefix.trim();
				logger.debug("prefix = "+prefix);
				
				String suffix = lineTxt.substring(nwbi, lineTxt.length());
//				if (method == CattiMethod.SET_PREFIX)
//					suffix = "";
				
//				suffix = suffix.trim();
				logger.debug("suffix = "+suffix);
								
				// last token is *not* partial iff it ends with an empty space and is not empty
				boolean last_token_is_partial = !prefix.isEmpty() && !prefix.endsWith(" ");
				
//				boolean last_token_is_partial = !prefix.isEmpty() && !suffix.isEmpty() 
//				&& !suffix.startsWith(" ") /*&& !prefix.endsWith(" ")*/;				
				
//				boolean last_token_is_partial = true;
//				if (replacementText!=null && replacementText.isEmpty())
//					last_token_is_partial = false;
				
				String corrected_out = "";
				
				// TOKENIZE STUFF:
				prefix = CoreUtils.tokenizeForCATTI(prefix);
				suffix = CoreUtils.tokenizeForCATTI(suffix);
				logger.debug("prefix tokenized: '"+prefix+"'");
				logger.debug("suffix tokenized: '"+suffix+"'");
				
				CattiRequest r = new CattiRequest(store.getUserId(), docid, pid, lid, method, prefix, suffix, last_token_is_partial, corrected_out);
				ce.sendObjectBasicRemote(r);
			} catch (Exception e) {
				logger.debug(e.getMessage(), e);
			}
		}
		logger.debug("END OF callCattiMethod: "+method);
	}
	
	@Override protected void onPaintTags(PaintEvent e) {
		if (currentRegionObject == null || currentRegionObject.getTextLine().isEmpty())
			return;
		
		logger.trace("onPaintTags!");

//		Font oldFont = e.gc.getFont();
		// for all visible lines:
		int firstLine = JFaceTextUtil.getPartialTopIndex(text);
		int lastLine = JFaceTextUtil.getPartialBottomIndex(text);
		logger.trace("firstline = "+firstLine+" lastLine = "+lastLine);
		
		for (int i = firstLine; i <= lastLine; ++i) {
			TrpTextLineType line = getLineObject(i);
			if (line==null) {
				logger.error("Could not paint line tags for line "+i+" - should not happen here!");
				return;
			}
			paintTagsForShape(e, line);
			
//			CustomTagList ctl = line.getCustomTagList();
//			int lo = text.getOffsetAtLine(i);
//			logger.trace("i = " + i + " lo = " + lo);
//			paintTagsFromCustomTagList(e, ctl, lo);
		}
//		e.gc.setFont(oldFont); // needed ?? (most probably not)
	}
	
	private List<Pair<TrpTextLineType, IntRange>> getReplaceRanges(int start, int end) {
		List<Pair<TrpTextLineType, IntRange>> rrs = new ArrayList<>();
		int sl = text.getLineAtOffset(start);
		int el = text.getLineAtOffset(end);
		
		for (int i=sl; i<=el; ++i) {
			TrpTextLineType line = getLineObject(i);
			IntRange r = new IntRange(0, line.getUnicodeText().length());
			if (i==sl) {
				int lo = text.getOffsetAtLine(i);
				r.offset = start - lo;
				r.length -= r.offset;
			}
			if (i==el) {
				r.length = end - text.getOffsetAtLine(i) - r.offset;
			}
			rrs.add(Pair.of(line, r));
		}
		
		return rrs;
	}
		
	@Override
	protected void initVerifyListener() {
		VerifyListener verifyListener = new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {

				//TODO:FIXME take out!
				logger.trace("verifyText() "+e.keyCode + ": "+e.character + " - "+ e.text);
				
				// prevent del and backspace on begin and end of line:
				preventDelAndBackspace(e);
				if (e.doit == false)
					return;
				
//				logger.debug("range changed = "+e.start+" / "+e.end+ " e = "+e.toString());
				
				// TEST: print ranges and text of affected lines of replacement:
//				List<Pair<TrpTextLineType, IntRange>> rrs = getReplaceRanges(e);
//				logger.debug("replace ranges for e = "+e);
//				for (Pair<TrpTextLineType, IntRange> pr : rrs) {
//					TrpTextLineType line = pr.getLeft();
//					IntRange range = pr.getRight();
//					String text = line.getUnicodeText().substring(range.offset, range.offset+range.length);
//					logger.debug("range: "+range+" text: "+text);
//				}
				
//				preventChangeOverMultipleLines(e);
//				if (e.doit == false)
//					return;
				
				checkAndReinterpretMultilineInsertions(e);
				if (e.doit == false)
					return;
								
				lastReplacements = getReplaceRanges(e.start, e.end);
			}
		};
		addUserVerifyListener(verifyListener);
	}
		
	private void checkAndReinterpretMultilineInsertions(VerifyEvent e) {
		// TODO here: change event s.t. insertion does never change the nr of lines!!!
		List<Pair<TrpTextLineType, IntRange>> rrs = getReplaceRanges(e.start, e.end);
		logger.debug("e.text = "+e.text);
		
		List<String> linesToInsert = Utils.getLines(e.text);
		
		logger.debug("nr of affected lines / nr of inserted lines "+rrs.size()+" / "+linesToInsert.size());
		
		final boolean ALLOW_ONLY_CHANGES_ON_ONE_LINE=false;
		if (rrs.size() == linesToInsert.size()) { // one line selected and one line inserted -> nothing special
			return;
		}
		else if (ALLOW_ONLY_CHANGES_ON_ONE_LINE) {
			e.doit = false;
			return;
		}
		
		e.doit = false;
		int diff = rrs.size() - linesToInsert.size();
		String txt = e.text;
		if (diff>0) { // there are fewer lines to insert than affected lines --> fill up with empty lines!
			logger.debug("there are "+diff+" fewer lines to insert than affected lines -> filling up with empty lines!");
			for (int i=0; i<diff; ++i)
				txt += "\n";
		} else { // there are more lines to insert than affected lines --> remove some newlines and replace them with spaces!
			// first: check if there are some empty lines after this one and adjust end index accordingly:
			int elo = text.getLineAtOffset(e.end);
			for (int i=elo+1; i<text.getLineCount(); ++i) {
				if (text.getLine(i).isEmpty()) {
					e.end = text.getOffsetAtLine(i);
					diff++;
					if (diff == 0)
						break;
				}
			}
			// now: replace diff nr of newlines with whitespaces!
			logger.debug("there are "+(-diff)+" more lines to insert than affected lines -> replacing new line chars with spaces!");
			txt = "";
			for (int i=0; i<linesToInsert.size()+diff-1; ++i) {
				txt += linesToInsert.get(i)+"\n";
			}
			for (int i=linesToInsert.size()+diff-1; i<linesToInsert.size(); ++i) {
				txt += linesToInsert.get(i)+" ";
			}
			txt = StringUtils.chop(txt);
		}
		logger.debug("changed modify event to: "+e.start+" / "+e.end+" text = "+txt);
		text.replaceTextRange(e.start, e.end-e.start, txt);
	}
		
	@Override
	protected void initVerifyKeyListener() {
		VerifyKeyListener verifyKeyListener = new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent e) {
				boolean isAlt = CanvasKeys.isAltKeyDown(e);
				boolean isCtrl = CanvasKeys.isCtrlKeyDown(e);
				boolean isCmd = CanvasKeys.isCommandKeyDown(e);
				
				// test hooks:
				if (false) {
				if (isCtrl && isAlt && e.keyCode == 'p') {
					callCattiMethod(CattiMethod.SET_PREFIX, 0, 0, null);
				}
				if (isCtrl && isAlt && e.keyCode == 'u') {
					callCattiMethod(CattiMethod.REJECT_SUFFIX, 0, 0, null);
				}
				}
				
				// on ctlr-arrowdown, call reject suffix method
				if (isCattiMode()) {
					if (isCtrl && e.keyCode == SWT.ARROW_DOWN) {
						callCattiMethod(CattiMethod.REJECT_SUFFIX, 0, 0, null);
					} else if (isCmd && e.keyCode =='n') {
						callCattiMethod(CattiMethod.REJECT_SUFFIX, 0, 0, null);
					}
				}
				
				//TODO:FIXME take out!
				logger.debug("verifyKey() "+e.keyCode + ": "+e.character + " - "+ e.text);
				
				// VERY OLD SHIT:
//				boolean isSingleSelect = (e.start == e.end);
//				
//				// prevent backspace on start of line
//				int xIndex = getCurrentXIndex();
//				if (e.keyCode == SWT.BS && xIndex == 0 && isSingleSelect) { 
//					logger.debug("preventing bs");
//					e.doit = false;
//					return;
//				}
//				
//				// prevent del on end of line
//				int lineLength = text.getLine(text.getLineAtOffset(text.getCaretOffset())).length();
//				if (e.keyCode == SWT.DEL && xIndex == lineLength && isSingleSelect) { 
//					logger.debug("preventing del");
//					e.doit = false;
//					return;
//				}				
//				else if (e.keyCode == TAG_CHAR) {
//				}
			}
		};
		addUserVerifyKeyListener(verifyKeyListener);
	}
	
//	/** Returns the index of the current cursor position in the current line */
//	public int getCurrentXIndex() {
//		return getXIndex(text.getCaretOffset());
//	}
//	
//	public int getXIndex(int caretOffset) {
//		int lineOffset = text.getOffsetAtLine(text.getLineAtOffset(caretOffset));
//		int xIndex = caretOffset - lineOffset;
//		return xIndex;
//	}
				
//	private void highlightWords(LineStyleEvent event, List<StyleRange> styleList) {
//		int lineToHighlight = text.getLineAtOffset(event.lineOffset);
//		
//		if (lineToHighlight == 0 || currentRegionObject==null)
//			return;
//		
////		String lineText = text.getLine(lineToHighlight);
//		List<TaggedWord> taggedWords = LineTags.getTaggedWords(currentRegionObject.getPage(), event.lineText);
//		for (TaggedWord tw : taggedWords) {
//			logger.debug(tw);
//			
//			StyleRange twSr = new StyleRange();
//			twSr.start = event.lineOffset+tw.getStart();
//			twSr.length = tw.getWord().length();
//			twSr.fontStyle = SWT.BOLD;
//			if (tw.getWordRegion()!=null)
//				twSr.foreground = Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
//			else
//				twSr.foreground = Colors.getSystemColor(SWT.COLOR_DARK_RED);
//			
//			styleList.add(twSr);
//		}
//	}
	
//	@Override
//	protected List<StyleRange> getLineStyleRanges(int lineOffset) {

//	}	
	
	private List<TextStyleTag> fillTextStyleTags(List<TextStyleTag> textStyleTags, int lineSize) {
		List<TextStyleTag> filledTextStyleTags = new ArrayList<>();
		if (textStyleTags.isEmpty()) {
			return filledTextStyleTags;
		}
			
		
		if (textStyleTags.get(0).getOffset() > 0) {
			filledTextStyleTags.add(new TextStyleTag(0, textStyleTags.get(0).getOffset()));
		}
		for (int i=0; i<textStyleTags.size(); ++i) {
			filledTextStyleTags.add(textStyleTags.get(i));
			
			// fill up to the next element or end of line:
			int nextBegin = (i+1)==textStyleTags.size() ? lineSize : textStyleTags.get(i+1).getOffset();
			if (textStyleTags.get(i).getEnd() < nextBegin) { // FIXME?
				filledTextStyleTags.add(new TextStyleTag(textStyleTags.get(i).getEnd(), nextBegin-textStyleTags.get(i).getEnd()));
			}
		}
		
		return filledTextStyleTags;
	}
	
	@Override
	protected List<StyleRange> getLineStyleRanges(int lineOffset) {
		List<StyleRange> styleList = new ArrayList<>();
		
		int lineToHighlight = text.getLineAtOffset(lineOffset);
		TrpTextLineType line = getLineObject(lineToHighlight);
		int currentLineIndex = getCurrentLineIndex();
		boolean isCurrentLine = lineToHighlight == currentLineIndex;
		String lineText = text.getLine(lineToHighlight);
		logger.trace("lineText size = "+lineText.length()+" line = "+line);
		
		if (TrpConfig.getTrpSettings().isEnableIndexedStyles()) { // THIS IS THE NEW SHIT
			if (line==null)
				return styleList;
			
			logger.trace("line id = "+line.getId());
			List<TextStyleTag> textStyleTags = line.getTextStyleTags();

			if (textStyleTags.isEmpty()) {
				TextStyle ts = GuiUtil.getDefaultSWTTextStyle(text.getFont().getFontData()[0], settings);
				StyleRange sr = new StyleRange(ts);
				sr.start = lineOffset;
				sr.length = lineText.length();				

				// style for selected line:
				if (isCurrentLine) sr.foreground = Colors.getSystemColor(SWT.COLOR_BLUE);
				
				logger.trace("adding sr: "+sr);
				styleList.add(sr);
			} else {
				List<TextStyleTag> fillTextStyleTags = fillTextStyleTags(textStyleTags, lineText.length());
				for (int i=0; i<fillTextStyleTags.size(); ++i) {
					TextStyleTag tst = fillTextStyleTags.get(i);
					logger.trace("tst: "+tst);
					
					TextStyle ts = GuiUtil.getSWTTextStyle(tst.getTextStyle(), text.getFont().getFontData()[0], settings);			
					StyleRange sr = new StyleRange(ts);
					
					if (true) { // do left/right cutoff of style if not in range to prevent exception!
						sr.start = Math.max(lineOffset, tst.getOffset() + lineOffset);
						int maxL = lineText.length() - (lineOffset-sr.start);
						sr.length = Math.min(maxL, tst.getLength());
					} else {
						sr.start = lineOffset;
						sr.length = lineText.length();					
					}					
					
					// style for selected line:
					if (isCurrentLine) sr.foreground = Colors.getSystemColor(SWT.COLOR_BLUE);
					
					logger.trace("adding sr: "+sr);
					styleList.add(sr);
				}
			}
									
		//	highlighWords(event, styleList);
		}
		else { // OLD VERSION: only global TextStyleType is considered!			
			if (line!=null && line.getTextStyle()!=null) {
				TextStyle ts = GuiUtil.getSWTTextStyle(line.getTextStyle(), text.getFont().getFontData()[0], settings);
				StyleRange sr = new StyleRange(ts);
							
				sr.start = lineOffset;
				sr.length = text.getLine(lineToHighlight).length();
			
				// style for selected line:
				if (isCurrentLine) {
					sr.foreground = Colors.getSystemColor(SWT.COLOR_BLUE);
				}
				styleList.add(sr);
			}
			
	//		highlighWords(event, styleList);
		}
		return styleList;
	}
		
	@Override
	protected void updateSelection(boolean textChanged, boolean lineChanged, boolean wordChanged) {
		if (currentLineObject == null) {
			text.setSelection(0);
		}
		else if (textChanged || lineChanged) {// text or line change from outside of transcription widget
//			int li = currentLineObject.getIndex();
			int li = getIndexOfLineInCurrentRegion(currentLineObject);
			logger.debug("li123 = "+li);
			if (li!=-1) {
				int selectionOffset = text.getOffsetAtLine(li);
				text.setSelection(selectionOffset);
			}
		}
	}

	@Override
	protected void updateWordObject() {
	}
	
	@Override
	protected String getTextFromRegion() {
		return (currentRegionObject == null) ? "" : currentRegionObject.getRegionTextFromLines();
	}
	
	@Override public ITrpShapeType getTranscriptionUnit() { return currentLineObject; }
	@Override public Class<? extends ITrpShapeType> getTranscriptionUnitClass() { return TrpTextLineType.class; }
	
	@Override public List<Pair<ITrpShapeType, IntRange>> getSelectedShapesAndRanges() {
		List<Pair<ITrpShapeType, IntRange>> selection = new ArrayList<>();
		if (currentRegionObject==null || currentLineObject==null)
			return selection;
		
		Point s = text.getSelection();
		
		int li1=text.getLineAtOffset(s.x);
		int offsetL1 = text.getOffsetAtLine(li1);
		
		int li2=text.getLineAtOffset(s.y);
		int offsetL2 = text.getOffsetAtLine(li2);

		for (int i=li1; i<=li2; ++i) {
			ITrpShapeType line = getLineObject(i);
			if (line == null)
				continue;
			
			int lineLength = line.getUnicodeText().length();
			IntRange range = new IntRange(0, lineLength);
			if (i==li1) { // cut off for first selected line
				range.offset = s.x - offsetL1;
				range.length = lineLength - range.offset;
			}
			if (i==li2) { // cut off for last selected line
				range.length = s.y - offsetL2 - range.offset;
			}
			selection.add(Pair.of(line, range));
		}
		
		return selection;
	}

	@Override public boolean selectCustomTag(CustomTag t) {
		if (t==null || t.getCustomTagList()==null || t.getCustomTagList().getShape()==null)
			return false;
		
		if (!(t.getCustomTagList().getShape() instanceof TrpTextLineType))
			return false;
		
		if (currentRegionObject == null || currentLineObject==null)
			return false;
		
		if (!t.getCustomTagList().getShape().getId().equals(currentLineObject.getId()))
			return false;
		
		
		
		TrpTextLineType l = (TrpTextLineType) t.getCustomTagList().getShape();
		int lineIndex = currentRegionObject.getTrpTextLine().indexOf(l);
		if (lineIndex==-1) {
			return false;
		}

		int lo = text.getOffsetAtLine(lineIndex);
		
		int s = lo+t.getOffset();
		int e = s+t.getLength();
		logger.debug("nr of continuations: "+t.continuations.size());
		for (CustomTag ct : t.continuations) {
			e += ct.getLength()+1;
		}
		
		logger.debug("setting selection to: ["+s+"/"+e+"]");
		text.setSelection(s, e);

		return true;
	}

	@Override
	public TranscriptionLevel getTranscriptionLevel() {
		return TranscriptionLevel.LINE_BASED;
	}
	
//	@Override public Point getSelectionRangeRelativeToTranscriptionUnit() {
//		if (currentLineObject==null)
//			return null;
//		
//		Point s = text.getSelection();
//		logger.debug("getSelectionRangeRelativeToTranscriptionUnit - selection = "+s);
////			if (text.getSelectionCount()==0) {
////				logger.debug("getSelectionRangeRelativeToTranscriptionUnit - no selection -> setting to whole line!");
////				return new Point(0, currentLineObject.getUnicodeText().length());
////			} 
//		
//		int line1=text.getLineAtOffset(s.x);
//		int line2=text.getLineAtOffset(s.y);
//		
//		if (line1 != line2) { // selection over multiple lines --> return null
//			return null;
//		}
//		else {
//			int offset = text.getOffsetAtLine(line1);
//			int start = s.x - offset;
//			int length = s.y-s.x;
//			Point r = new Point(start, length);
//			logger.debug("getSelectionRangeRelativeToTranscriptionUnit - returning range = "+r);
//			return r;				
//		}
//	}
	
	protected List<Pair<Integer, ITrpShapeType>> getShapesWithOffsets(int lineIndex) {
		List<Pair<Integer, ITrpShapeType>> shapes = new ArrayList<>();
		TrpTextLineType line = getLineObject(lineIndex);
		if (line == null) {
			return shapes;
		}
		
		int lo = text.getOffsetAtLine(lineIndex);
		shapes.add(Pair.of(lo, line));
		return shapes;
	}
	
}
