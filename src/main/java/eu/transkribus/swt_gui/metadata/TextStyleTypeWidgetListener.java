//package eu.transkribus.swt_gui.metadata;
//
//import java.util.List;
//
//import org.apache.commons.lang3.tuple.Pair;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.events.ModifyEvent;
//import org.eclipse.swt.events.ModifyListener;
//import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.SelectionListener;
//import org.eclipse.swt.widgets.Event;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Widget;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import eu.transkribus.core.model.beans.customtags.CustomTag;
//import eu.transkribus.core.model.beans.customtags.TextStyleTag;
//import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
//import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
//import eu.transkribus.swt.util.databinding.DataBinder;
//import eu.transkribus.swt_gui.TrpConfig;
//import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
//import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
//
///**
// * @deprecated outdated
// *
// */
//public class TextStyleTypeWidgetListener implements SelectionListener, ModifyListener, Listener {
//	private static final Logger logger = LoggerFactory.getLogger(TextStyleTypeWidgetListener.class);
//	
//	TextStyleTypeWidget tw;
//	
//	public TextStyleTypeWidgetListener(TextStyleTypeWidget tw) {
//		this.tw = tw;
//		
//		addListener();
//	}
//
//	private void addListener() {
//		tw.addTextStyleListener((Listener) this);
//		
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.UNDERLINE_TEXT_STYLES_PROPERTY, TrpConfig.getTrpSettings(), tw.underlineTextStylesBtn);
//	}
//
//	@Override
//	public void modifyText(ModifyEvent e) {
//		if (e.getSource()==tw.fontFamilyText /*&& getNSelected() == 1*/) {
//			logger.debug("font family modified: "+tw.fontFamilyText);
//			applyTextStyleToAllSelected("fontFamily", false);
//		}		
//	}
//
//	@Override
//	public void widgetSelected(SelectionEvent e) {
//		Object s = e.getSource();
//		
//		// update text style:
//		if (tw.getTextStyleSources().contains(s) /*&& getNSelected() == 1*/) {
//			String propertyName = (String) ((Widget)s).getData("propertyName");
//			logger.debug("property name: "+propertyName);
//			
//			applyTextStyleToAllSelected(propertyName, false);
//			tw.savePage();
//		}
//		
////		else if (s == tw.getApplyBtn()) {
////			applyTextStyleToAllSelected(null, false);
////			tw.savePage();
////		}
////		else if (s == tw.getApplyRecursiveBtn()) {
////			applyTextStyleToAllSelected(null, true);
////			tw.savePage();
////		}		
//	}
//
//	@Override
//	public void widgetDefaultSelected(SelectionEvent e) {		
//	}
//	
//	private void applyTextStyleToAllSelected(String updateOnlyThisProperty, boolean recursive) {
//		TrpMainWidget mw = TrpMainWidget.getInstance();
//		
//		boolean isTextSelectedInTranscriptionWidget = mw.isTextSelectedInTranscriptionWidget();
//		logger.debug("isTextSelectedInTranscriptionWidget = "+isTextSelectedInTranscriptionWidget);
//		
//		TextStyleType ts = tw.getTextStyleTypeFromUi();
//		if (updateOnlyThisProperty!=null && updateOnlyThisProperty.equals("fontFamily")) { // FIXME?? this is a hack -> if only fontFamily is updated, use also empty font family field!!
//			logger.debug("setting font family: "+tw.fontFamilyText.getText());
//			ts.setFontFamily(tw.fontFamilyText.getText());
//		}
//		
//		if (!isTextSelectedInTranscriptionWidget) {
//			logger.debug("applying this text style to all selected in canvas: "+ts);
//			List<? extends ITrpShapeType> selData = mw.getCanvas().getScene().getSelectedData(ITrpShapeType.class);
//			logger.debug("nr selected: "+selData.size());
//			for (ITrpShapeType sel : selData) {
//				sel.setTextStyle(ts, recursive, tw);
//			}
//		} else { // for a selection in the transcription widget
//			logger.debug("applying this text style to all selected in transcription widget: "+ts);
//			List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = TaggingWidgetUtils.constructTagsFromSelectionInTranscriptionWidget(mw.getUi(), TextStyleTag.TAG_NAME, null);
//			for (Pair<ITrpShapeType, CustomTag> p : tags4Shapes) {
//				TextStyleTag tag = (TextStyleTag) p.getRight();
//				
//				if (mw.getTrpSets().isEnableIndexedStyles() /*&& isLineEditor*/ && !recursive) {
//					if (tag != null) {
//						tag.setTextStyle(ts);
//						p.getLeft().addTextStyleTag(tag, updateOnlyThisProperty, /*false,*/ tw);
//					}
//				} else {
//					p.getLeft().setTextStyle(ts, recursive, tw);
//				}	
//			}
//			
//			// OLD:
////			ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
////			if (aw==null) {
////				logger.debug("no transcription widget selected - doin nothing!");
////				return;
////			}
////			boolean isLineEditor = aw.getType() == ATranscriptionWidget.Type.LINE_BASED;
////			List<Pair<ITrpShapeType, IntRange>> selRanges = aw.getSelectedShapesAndRanges();
////			for (Pair<ITrpShapeType, IntRange> r : selRanges) {
////				boolean isSingleSelection = selRanges.size()==1 && r.getRight().length==0;
////				if (settings.isEnableIndexedStyles() && isLineEditor && !recursive) {
////					TextStyleTag tst=null;
////					
////					// create textstyle - for the word editor or a single selection, set whole range
////					if ( (isSingleSelection && APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION) || !isLineEditor) {
////						tst = new TextStyleTag(ts, 0, r.getLeft().getUnicodeText().length());
////					} else if (r.getRight().length>0) {
////						tst = new TextStyleTag(ts, r.getRight().offset, r.getRight().length);
////					}
////					
////					if (tst!=null)
////						r.getLeft().addTextStyleTag(tst, updateOnlyThisProperty, recursive, mw);
////				} else {
////					r.getLeft().setTextStyle(ts, recursive, mw);
////				}
////			}
//		}
//		
//		tw.updateStyleSheetAccordingToCurrentSelection();
//		mw.getUi().getLineTranscriptionWidget().redrawText(true, false, false);
//		mw.getUi().getWordTranscriptionWidget().redrawText(true, false, false);
//		mw.refreshStructureView();
//	}
//
//	@Override
//	public void handleEvent(Event event) {
//		if (event.type == SWT.Selection) {
//			widgetSelected(new SelectionEvent(event));
//		}		
//	}
//	
//	
//
//}
