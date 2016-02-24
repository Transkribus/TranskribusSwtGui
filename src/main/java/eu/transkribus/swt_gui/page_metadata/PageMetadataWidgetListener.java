package eu.transkribus.swt_gui.page_metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.dea.swt.canvas.CanvasKeys;
import org.dea.swt.canvas.CanvasMode;
import org.dea.swt.canvas.editing.ShapeEditOperation;
import org.dea.swt.canvas.editing.ShapeEditOperation.ShapeEditType;
import org.dea.swt.canvas.shapes.CanvasPolygon;
import org.dea.swt.canvas.shapes.CanvasPolyline;
import org.dea.swt.canvas.shapes.ICanvasShape;
import org.dea.swt.util.DialogUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.JAXBPageTranscript;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent.PageTypeSimpleType;
import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent.RelationType;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.builder.TrpPageTranscriptBuilder;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt_gui.canvas.TrpCanvasAddMode;
import eu.transkribus.swt_gui.canvas.TrpSWTCanvas;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.util.GuiUtil;

public class PageMetadataWidgetListener implements SelectionListener, ModifyListener, Listener {
	private final static Logger logger = LoggerFactory.getLogger(PageMetadataWidgetListener.class);
	
	TrpMainWidget mainWidget;
	TrpMainWidgetView ui;
	PageMetadataWidget mw;
	TextStyleTypeWidget tw;
	TrpSWTCanvas canvas;
	TrpSettings settings;
		
	public PageMetadataWidgetListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.ui = mainWidget.getUi();
		this.canvas = mainWidget.getCanvas();
		this.mw = mainWidget.getUi().getMetadataWidget();
		this.tw = mw.getTextStyleWidget();
		this.settings = mainWidget.getTrpSets();
		
		addListener();
	}
	
	private void addListener() {
		mw.addMetadataListener(this);
	}
	
	public boolean hasPage() {
		return mainWidget.getStorage().hasTranscript() 
				&& mainWidget.getStorage().getTranscript().getPage()!=null;
	}
	
	public int getNSelected() {
		return canvas.getScene().getNSelected();
	}
	
	public TrpPageType getPage() {
		return mainWidget.getStorage().getTranscript().getPage();
	}
	
	public JAXBPageTranscript getTranscript() {
		return mainWidget.getStorage().getTranscript();
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (!hasPage())
			return;
		TrpPageType page = getPage();
		TrpTranscriptMetadata md = getTranscript().getMd();
		
		Object s = e.getSource();
		Widget w = null;
		if (s instanceof Widget)
			w = (Widget) s;
		
		// update page style:
		if (s == mw.pageStyleCombo) {
			logger.debug("pagestyle changed: "+mw.getPageStyleCombo().getText()+" value = "+EnumUtils.fromValue(PageTypeSimpleType.class, mw.getPageStyleCombo().getText()));
			page.setType(EnumUtils.fromValue(PageTypeSimpleType.class, mw.pageStyleCombo.getText()));
		}
		else if (s == mw.statusCombo) {
			logger.debug("setting new status: "+mw.statusCombo.getText());
			md.setStatus(EnumUtils.fromString(EditStatus.class, mw.statusCombo.getText()));
		}
		// update structure:
//		else if (s == mw.getRegionTypeCombo() && getNSelected() == 1) {
//			applyStructureTypeToAllSelected(false);
//		}
		else if (mw.getStructureRadios().contains(s) /*&& getNSelected() == 1*/) {
			applyStructureTypeToAllSelected(((Button) s).getText(), false);
		}
		else if (s == mw.getApplyStructBtn()) {
			applyStructureTypeToAllSelected(mw.structureText.getText(), false);
//			applyTextStyleToAllSelected(false);
		}
		else if (s == mw.getApplyStructRecBtn()) {
			applyStructureTypeToAllSelected(mw.structureText.getText(), true);
//			applyTextStyleToAllSelected(true);
		}
		
		// update text style:
		else if (tw.getTextStyleSources().contains(s) /*&& getNSelected() == 1*/) {
			String propertyName = (String) ((Widget)s).getData("propertyName");
			logger.debug("property name: "+propertyName);
			
			applyTextStyleToAllSelected(propertyName, false);
		}
		else if (s == tw.getApplyBtn()) {
//			applyStructureTypeToAllSelected(false);
			applyTextStyleToAllSelected(null, false);
		}
		else if (s == tw.getApplyRecursiveBtn()) {
//			applyStructureTypeToAllSelected(true);
			applyTextStyleToAllSelected(null, true);
		}
		// update tags:
//		else if (s == mw.getTaggingWidget()) {
//			logger.debug("setting/removing tag: "+e.text+", "+e.detail);
//			
//			TaggingWidget.TaggingActionType type = (TaggingActionType) e.data;
//			if (type == TaggingWidget.TaggingActionType.ADD_TAG) {
//				applyTagToSelection(e.text);
//			} else if (type == TaggingWidget.TaggingActionType.DELETE_TAG) {
//				removeTagFromSelection(e.text);
//			} else if (type == TaggingWidget.TaggingActionType.CLEAR_TAGS) {
//				clearTagsFromSelection();
//			} else if (type == TaggingWidget.TaggingActionType.TAGS_UPDATED) {
//				mainWidget.getUi().getLineTranscriptionWidget().redrawText(true);
//				mainWidget.getUi().getWordTranscriptionWidget().redrawText(true);
//			}
//		}

		// linking stuff:
		else if (s == mw.getLinkList() || s == mw.getDeleteLinkMenuItem() || s == mw.getBreakLinkBtn()) {
			org.eclipse.swt.widgets.List linkList = mw.getLinkList();
			if (linkList.getSelectionCount()==1) {
				String[] splits = linkList.getSelection()[0].split(PageMetadataWidget.LINK_DELIMITER);
				String id1 = splits[0].trim();
				String id2 = splits[1].trim();
				
				if (s == mw.getDeleteLinkMenuItem() || s == mw.getBreakLinkBtn()) {
					logger.debug("deleting link "+id1+PageMetadataWidget.LINK_DELIMITER+id2+", hasLink = "+ page.hasLink(id1, id2));
					
					if (page.removeLink(id1, id2)) {
						mainWidget.updatePageRelatedMetadata();
					}
				} else if (s == mw.getLinkList()) {
					logger.debug("selecting link "+id1+PageMetadataWidget.LINK_DELIMITER+id2+", hasLink = "+ page.hasLink(id1, id2));
					
					RelationType r = page.getLink(id1, id2);
					if (r!=null) {
						ITrpShapeType s1 = (ITrpShapeType) r.getRegionRef().get(0).getRegionRef();
						ITrpShapeType s2 = (ITrpShapeType) r.getRegionRef().get(1).getRegionRef();
						
						if (s1 != null && s2 != null) {
							canvas.getScene().selectObjectWithData(s1, true, false);
							canvas.getScene().selectObjectWithData(s2, true, true);
						}
					}
				}
			}
		} else if (s ==  mw.linkBtn) {
			if (canvas.getScene().getNSelected()==2) {
				logger.debug("linking shapes!");
				List<Object> selData = canvas.getScene().getSelectedData();
				ITrpShapeType st1 = (ITrpShapeType) selData.get(0);
				ITrpShapeType st2 = (ITrpShapeType) selData.get(1);
				if (Storage.getInstance().hasTranscript()) {
					Storage.getInstance().getTranscript().getPage().addLink(st1, st2);
					mainWidget.updatePageRelatedMetadata();
				}
			}
		}
		else if (s == mw.shapeTypeCombo) {
			try {
				convertSelectedShape(mw.shapeTypeCombo.getText());
			} catch (IOException e1) {
				DialogUtil.showErrorMessageBox(canvas.getShell(), "Error while converting shape", e1.getMessage());
				mainWidget.updatePageRelatedMetadata();
			}
		}
		
		return;
	}
	
	
	////////////////// CODE FOR CONVERTING SHAPES:
	public static CanvasMode getMode(Class<? extends ITrpShapeType> convertClazz) {
		if (convertClazz.equals(TrpTextRegionType.class))
			return TrpCanvasAddMode.ADD_TEXTREGION;
		else if (convertClazz.equals(TrpTextLineType.class))
			return TrpCanvasAddMode.ADD_LINE;
		else if (convertClazz.equals(TrpBaselineType.class))
			return TrpCanvasAddMode.ADD_BASELINE;
		else if (convertClazz.equals(TrpWordType.class))
			return TrpCanvasAddMode.ADD_WORD;
		else if (convertClazz.equals(TrpPrintSpaceType.class))
			return TrpCanvasAddMode.ADD_PRINTSPACE;
		
		else if (RegionType.class.isAssignableFrom(convertClazz))
			return TrpCanvasAddMode.ADD_OTHERREGION;
		
		return null;
//		return null;
	}
	
	public void convertSelectedShape(String newShapeType) throws IOException {
		logger.debug("converting selected shape to type: "+newShapeType);
		ITrpShapeType sel = canvas.getFirstSelectedSt();
		ICanvasShape selShape = canvas.getFirstSelected();

		if (sel == null) {
			throw new IOException("No shape selected!");
		}
		if (newShapeType == null) {
			throw new IOException("No shape type specified!");
		}
		
		String oldShapeType = RegionTypeUtil.getRegionType(sel);
		if (newShapeType.equals(oldShapeType)) {
			logger.debug("same shape types... doin' nothing!");
			return;
		}
		
		if (newShapeType.equals(RegionTypeUtil.BASELINE_TYPE) || oldShapeType.equals(RegionTypeUtil.BASELINE_TYPE)) {
			throw new IOException("Cannot convert to or from baselines!");
		}
		
		Class<? extends ITrpShapeType> convertClazz = RegionTypeUtil.getRegionClass(newShapeType);
		if (convertClazz==null) {
			logger.error("Could not find corresponding convert class for type: "+newShapeType);
			return;
		}
		
		if (convertClazz.equals(TrpTextLineType.class) || convertClazz.equals(TrpWordType.class)) {
			throw new IOException("Cannot convert to line or word - parent information would be messed up!");
		}
		
		// determine parent shape:
		ICanvasShape copyShape = null;
		if (sel instanceof TrpBaselineType) {
			CanvasPolyline baselineShape = (CanvasPolyline) selShape;
			copyShape = new CanvasPolyline(selShape.getPoints());
		} else  {
			copyShape = new CanvasPolygon(selShape.getPoints());
		}
		
		// backup and set correct mode:
		CanvasMode modeBackup = canvas.getMode();
		
		CanvasMode newMode = getMode(convertClazz);
		if (newMode == null) {
			logger.error("Could not find mode!");
			return;
		}
		canvas.setMode(newMode);
		if (newMode == TrpCanvasAddMode.ADD_OTHERREGION) {
			newMode.data = newShapeType;
		}
		
		// try to add new shape:
		
		List<ShapeEditOperation> ops = new ArrayList<>();
		
		ShapeEditOperation opAdd = canvas.getScene().addShape(copyShape, null, true);
		opAdd.setDescription("Converted shape type to "+newShapeType);
		if (opAdd != null)
			ops.add(opAdd);
		
		if (canvas.getScene().removeShape(selShape, true, true)) {
			ShapeEditOperation opDel 
				= new ShapeEditOperation(canvas, ShapeEditType.DELETE, "", selShape);
			ops.add(opDel);
		}
		canvas.getShapeEditor().addToUndoStack(ops);

		canvas.setMode(modeBackup);

		canvas.getScene().selectObject(copyShape, true, false);
	}
	
////////////////
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if (e.getSource()==tw.fontFamilyText /*&& getNSelected() == 1*/) {
			logger.debug("font family modified: "+tw.fontFamilyText);
			applyTextStyleToAllSelected("fontFamily", false);
		} else if (e.getSource() == mw.structureText) {
			logger.debug("structure type text changed - applying to selected: "+mw.structureText.getText());
			applyStructureTypeToAllSelected(mw.structureText.getText(), false);
		}
	}
	
	private void applyStructureTypeToAllSelected(String structType, boolean recursive) {
		List<ICanvasShape> selected = mainWidget.getCanvas().getScene().getSelectedAsNewArray();
		logger.debug("applying structure type to selected, n = "+selected.size()+" structType: "+structType);
//		TextTypeSimpleType struct = EnumUtils.fromValue(TextTypeSimpleType.class, mw.getRegionTypeCombo().getText());
//		String struct = mw.getStructureType();		
		for (ICanvasShape sel : selected) {
			logger.debug("updating struct type for " + sel+" type = "+structType);
			ITrpShapeType st = GuiUtil.getTrpShape(sel);
			
			st.setStructure(structType, recursive, mw);
		}
		
		mainWidget.refreshStructureView();
	}

//	private IntRange getTagRange(int nRanges, Pair<ITrpShapeType, IntRange> r) {
//		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
//		if (aw==null) {
//			logger.debug("no transcription widget selected - returning null range!");
//			return null;
//		}
//		boolean isLineEditor = aw.getType() == ATranscriptionWidget.Type.LINE_BASED;
//		boolean isSingleSelection = nRanges==1 && r.getRight().length==0;
//			
//		// create range:
//		if ( (isSingleSelection && APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION) || !isLineEditor) {
//			return new IntRange(0, r.getLeft().getUnicodeText().length());
//		} else if (r.getRight().length>0) {
//			return r.getRight();
//		}
//		
//		return null;
//	}
	
//	private List<Pair<ITrpShapeType, CustomTag>> getTagsFromSelectionInTranscriptionWidget(String tagName) {
//		List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = new ArrayList<>();
//		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
//		if (aw==null) {
//			logger.debug("no transcription widget selected - doin nothing!");
//			return tags4Shapes;
//		}
//		List<Pair<ITrpShapeType, IntRange>> selRanges = aw.getSelectedShapesAndRanges();
//		logger.debug("selRanges: "+selRanges.size());
//		
//		for (Pair<ITrpShapeType, IntRange> r : selRanges) {
////			if (settings.isEnableIndexedStyles() && isLineEditor && !recursive) {
//				CustomTag t = null;	
//				IntRange tagRange =  getTagRange(selRanges.size(), r);
//				logger.debug("range is: "+tagRange);
//				if (tagRange != null) {
//					try {
//						t = CustomTagFactory.create(tagName, tagRange.offset, tagRange.length);
//						logger.debug("created tag: "+t);
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//					}
//				}
//				tags4Shapes.add(Pair.of(r.getLeft(), t));
//		}
//		return tags4Shapes;
//		
//	}
			
	private void applyTextStyleToAllSelected(String updateOnlyThisProperty, boolean recursive) {
		boolean isTextSelectedInTranscriptionWidget = mainWidget.isTextSelectedInTranscriptionWidget();
		logger.debug("isTextSelectedInTranscriptionWidget = "+isTextSelectedInTranscriptionWidget);
		
		TextStyleType ts = tw.getTextStyleTypeFromUi();
		if (updateOnlyThisProperty!=null && updateOnlyThisProperty.equals("fontFamily")) { // FIXME?? this is a hack -> if only fontFamily is updated, use also empty font family field!!
			logger.debug("setting font family: "+tw.fontFamilyText.getText());
			ts.setFontFamily(tw.fontFamilyText.getText());
		}
		
		if (!isTextSelectedInTranscriptionWidget) {
			logger.debug("applying this text style to all selected in canvas: "+ts);
			List<? extends ITrpShapeType> selData = canvas.getScene().getSelectedWithData(ITrpShapeType.class);
			logger.debug("nr selected: "+selData.size());
			for (ITrpShapeType sel : selData) {
				sel.setTextStyle(ts, recursive, mw);
			}
		} else { // for a selection in the transcription widget
			logger.debug("applying this text style to all selected in transcription widget: "+ts);
			List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = TaggingWidgetUtils.constructTagsFromSelectionInTranscriptionWidget(ui, TextStyleTag.TAG_NAME, null);
			for (Pair<ITrpShapeType, CustomTag> p : tags4Shapes) {
				TextStyleTag tag = (TextStyleTag) p.getRight();
				
				if (settings.isEnableIndexedStyles() /*&& isLineEditor*/ && !recursive) {
					if (tag != null) {
						tag.setTextStyle(ts);
						p.getLeft().addTextStyleTag(tag, updateOnlyThisProperty, /*false,*/ mw);
					}
				} else {
					p.getLeft().setTextStyle(ts, recursive, mw);
				}	
			}
			
			// OLD:
//			ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
//			if (aw==null) {
//				logger.debug("no transcription widget selected - doin nothing!");
//				return;
//			}
//			boolean isLineEditor = aw.getType() == ATranscriptionWidget.Type.LINE_BASED;
//			List<Pair<ITrpShapeType, IntRange>> selRanges = aw.getSelectedShapesAndRanges();
//			for (Pair<ITrpShapeType, IntRange> r : selRanges) {
//				boolean isSingleSelection = selRanges.size()==1 && r.getRight().length==0;
//				if (settings.isEnableIndexedStyles() && isLineEditor && !recursive) {
//					TextStyleTag tst=null;
//					
//					// create textstyle - for the word editor or a single selection, set whole range
//					if ( (isSingleSelection && APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION) || !isLineEditor) {
//						tst = new TextStyleTag(ts, 0, r.getLeft().getUnicodeText().length());
//					} else if (r.getRight().length>0) {
//						tst = new TextStyleTag(ts, r.getRight().offset, r.getRight().length);
//					}
//					
//					if (tst!=null)
//						r.getLeft().addTextStyleTag(tst, updateOnlyThisProperty, recursive, mw);
//				} else {
//					r.getLeft().setTextStyle(ts, recursive, mw);
//				}
//			}
		}
		
		tw.updateStyleSheetAccordingToCurrentSelection();
		mainWidget.getUi().getLineTranscriptionWidget().redrawText(true);
		mainWidget.getUi().getWordTranscriptionWidget().redrawText(true);
		mainWidget.refreshStructureView();
	}
			
//	private void applyTagToSelection(String tagName) {
//		boolean isSelectedInTranscriptionWidget = mainWidget.isSelectedInTranscriptionWidget();
//		logger.debug("isSelectedInTranscriptionWidget = "+isSelectedInTranscriptionWidget);		
//		
//		if (!isSelectedInTranscriptionWidget) {
//			logger.debug("applying tag to all selected in canvas: "+tagName);
//			List<? extends ITrpShapeType> selData = canvas.getScene().getSelectedWithData(ITrpShapeType.class);
//			logger.debug("selData = "+selData.size());
//			for (ITrpShapeType sel : selData) {
//				if (sel instanceof TrpTextLineType || sel instanceof TrpWordType) { // tags only for words and lines!
//					try {
//						CustomTag t = CustomTagFactory.create(tagName, 0, sel.getUnicodeText().length());
//						sel.getCustomTagList().addOrMergeTag(t, null);
//						logger.debug("created tag: "+t);
//					} catch (Exception e) {
//						logger.error("Error creating tag: "+e.getMessage(), e);
//					}
//				}
//			}
//		} else {
//			logger.debug("applying tag to all selected in transcription widget: "+tagName);
//			List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = TaggingWidgetUtils.getTagsFromSelectionInTranscriptionWidget(ui, tagName);
//			for (Pair<ITrpShapeType, CustomTag> p : tags4Shapes) {
//				CustomTag tag = p.getRight();
//				if (tag != null) {
//					tag.setContinued(tags4Shapes.size()>1);
//					p.getLeft().getCustomTagList().addOrMergeTag(tag, null);
//				}
//			}		
//			
//			// OLD:
////			ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
////			boolean isLineEditor = aw.getType() == ATranscriptionWidget.Type.LINE_BASED;
////			List<Pair<ITrpShapeType, IntRange>> selRanges = aw.getSelectedShapesAndRanges();
////			for (Pair<ITrpShapeType, IntRange> r : selRanges) {
////				boolean isSingleSelection = selRanges.size()==1 && r.getRight().length==0;
////				CustomTag t = null;
////				// create tag - for the word editor or a single selection, set whole range!
////				if ( (isSingleSelection && APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION) || !isLineEditor) {
////					t = new CustomTag(tagName, 0, r.getLeft().getUnicodeText().length());
////				} else if (r.getRight().length>0) {
////					t = new CustomTag(tagName, r.getRight().offset, r.getRight().length);
////				}
////				
////				if (t!=null) {
////					t.setContinued(selRanges.size()>1);
////					r.getLeft().getCustomTagList().addOrMergeTag(t, null);
////				}
////			}
//		}
//		
//		mainWidget.getUi().getLineTranscriptionWidget().redrawText(true);
//		mainWidget.getUi().getWordTranscriptionWidget().redrawText(true);
//		mainWidget.refreshStructureView();
//	}
	
//	private void applyCheckedTagsToSelection(List<String> checkedTags) {
////		List<String> tags = mw.getTaggingWidget().getCheckedTags();
//		for (String tagName : checkedTags) {
//			applyTagToSelection(tagName);
//		}
//	}
	
//	private void clearTagsFromSelection() {
//		logger.debug("clearing tags from selection!");
//		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
//		if (aw==null) {
//			logger.debug("no transcription widget selected - doin nothing!");
//			return;
//		}
//		
//		List<Pair<ITrpShapeType, IntRange>> ranges = aw.getSelectedShapesAndRanges();
//		for (Pair<ITrpShapeType, IntRange> p : ranges) {
//			ITrpShapeType s = p.getLeft();
//			IntRange r = p.getRight();
//			s.getCustomTagList().deleteTagsInRange(r.getOffset(), r.getLength());
//		}
//		
//		mainWidget.updatePageRelatedMetadata();
//		mainWidget.getUi().getLineTranscriptionWidget().redrawText(true);
//		mainWidget.getUi().getWordTranscriptionWidget().redrawText(true);
//		mainWidget.refreshStructureView();
//	}
	
//	private void removeTagFromSelection(String tagName) {
//		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
//		if (aw==null) {
//			logger.debug("no transcription widget selected - doin nothing!");
//			return;
//		}
//		
//		final Pair<ITrpShapeType, Integer> shapeAndRelativePositionAtOffset = aw.getTranscriptionUnitAndRelativePositionFromCurrentOffset();
//		if (shapeAndRelativePositionAtOffset==null)
//			return;
//		
//		final ITrpShapeType shape = shapeAndRelativePositionAtOffset.getLeft();
//		final CustomTagList ctl = shape.getCustomTagList();		
//		
//		CustomTag tag = null;
//		for (CustomTag t : aw.getCustomTagsForCurrentOffset()) {
//			if (t.getTagName().equals(tagName)) {
//				tag = t;
//				break;
//			}
//		}
//		if (tag==null) {
//			logger.warn("Could not find tag with name '"+tagName+"' for current offset - should not happen here!");
//			return;
//		}
//		
//		List<Pair<CustomTagList, CustomTag>> tags = ctl.getCustomTagAndContinuations(tag);
//
//		logger.debug(tag + " tags and continuations: ");
//		for (Pair<CustomTagList, CustomTag> t : tags) {
//			logger.debug("1shape: "
//					+ t.getLeft().getShape().getId() + " tag: "
//					+ t.getRight());
//			
//			t.getLeft().removeTag(t.getRight());
//		}
//		
//		mainWidget.updatePageRelatedMetadata();
//		mainWidget.getUi().getLineTranscriptionWidget().redrawText(true);
//		mainWidget.getUi().getWordTranscriptionWidget().redrawText(true);
//		mainWidget.refreshStructureView();
//	}
	
//	private void handleKeyDown(Event event) {
//		if (CanvasKeys.isKeyDown(event.stateMask, SWT.ALT) && event.keyCode == 'c') {
//			applyCheckedTagsToSelection(mw.getTaggingWidget().getCheckedTags());
//		}
//	}

	@Override public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			widgetSelected(new SelectionEvent(event));
		} 
//		else if (event.type == SWT.KeyDown) {
//			handleKeyDown(event);
//		}
	}
}
