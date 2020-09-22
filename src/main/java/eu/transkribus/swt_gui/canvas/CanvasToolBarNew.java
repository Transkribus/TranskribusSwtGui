package eu.transkribus.swt_gui.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;

//public class CanvasToolBar extends ToolBar {
public class CanvasToolBarNew {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBarNew.class);

//	public ToolBar tb;
	
	public ToolBar mainTb, viewTb, editTb;
	
//	ToolItem showRegionsToolItem;
//	ToolItem showLinesToolItem;
//	ToolItem showBaselinesToolItem;
//	ToolItem showWordsToolItem;
		
//	DropDownToolItem visibilityItem;
//	MenuItem showRegionsItem;
//	MenuItem showLinesItem;
//	MenuItem showBaselinesItem;
//	MenuItem showWordsItem;
////	MenuItem showPrintspaceItem;
//	MenuItem renderBlackeningsItem;
	
	public Button showRegionsButton, showLinesButton, showBaselinesButton, showWordsButton, showPrintspaceButton, renderBlackeningsButton;
	public Button showReadingOrderRegionsButton, showReadingOrderLinesButton, showReadingOrderWordsButton;
	public Button rotateLeftBtn, rotateRightBtn, rotateLeft90Btn, rotateRight90Btn, translateLeftBtn, translateRightBtn, translateUpBtn, translateDownBtn;
	public ToolItem fitPageItem, fitWidthItem, origSizeItem;
	
//	DropDownToolItem readingOrderVisibilityItem;
//	MenuItem showReadingOrderRegionsMenuItem;
//	MenuItem showReadingOrderLinesMenuItem;
//	MenuItem showReadingOrderWordsMenuItem;	
	
	ToolItem zoomIn;
	ToolItem zoomOut;
	ToolItem zoomSelection;
	ToolItem loupe;

	DropDownToolItem rotateItem;
	DropDownToolItem fitItem;

	ToolItem originalSize;

	ToolItem selectionMode;

	ToolItem focus;

	// EDIT ITEMS:
	ToolItem addPoint;
	ToolItem removePoint;
	ToolItem removeShape;
	ToolItem mergeShapes;

	DropDownToolItem splitDropdown;
	MenuItem splitHorizontalItem, splitVerticalItem, splitLineItem;
		
	ToolItem splitShapeLine;
	ToolItem splitShapeWithVerticalLine;
	ToolItem splitShapeWithHorizontalLine;

	ToolItem markupItem;
//	TableToolBox markupBox;	
	
	DropDownToolItem simplifyEpsItem;
	ToolItem undo;
	
	ToolItem viewTbEnabledToolItem;
	ToolItem editingEnabledToolItem;

	ToolItem viewSettingsMenuItem;
	HashMap<Item, CanvasMode> modeMap = new HashMap<>();
	SelectionAdapter radioGroupSelectionAdapter;
	
	
	ToolItem addTextRegionItem;
	ToolItem addLineItem;
	ToolItem addBaselineItem;
	ToolItem addWordItem;
	
	DropDownToolItem otherSegmentationToolsDropDown;
	
	DropDownToolItem addElementDropdown;
	DropDownToolItem optionsItem;
	ToolItem imgEnhanceItem;
	MenuItem rectangleModeItem;
	MenuItem useScrollBarsItem;
	MenuItem autoCreateParentItem;
	MenuItem addLineToOverlappingRegionItem;
	MenuItem addBaselineToOverlappingLineItem;
	MenuItem addWordsToOverlappingLineItem;
	MenuItem lockZoomOnFocusItem;
	MenuItem deleteLineIfBaselineDeletedItem;
	MenuItem selectNewlyCreatedShapeItem;
	List<ToolItem> addItems;
	CanvasWidget canvasWidget;
	DropDownToolItem imageVersionDropdown;
//	DropDownToolItem tableItem;
	ToolItem helpItem, canvasHelpItem;
	
	MenuItem createDefaultLineItem;
	MenuItem createImageSizeTextRegionItem;
	MenuItem simplifySelectedLinesItem;
	MenuItem simplifyAllLinesItem;
	MenuItem removeSmallTextRegionsItem;
	MenuItem rectifyAllRegionsItem;
	
//	MenuItem deleteRowItem;
//	MenuItem deleteColumnItem;
//	MenuItem splitMergedCell;
//	MenuItem removeIntermediatePtsItem;
//	
//	ToolItem renderBlackeningsToggle;

	public CanvasToolBarNew(CanvasWidget parent, ToolBar mainTb, ToolBar viewTb, ToolBar editTb, int style) {
		Assert.assertNotNull("mainTb must be given!", mainTb);
//		Assert.assertNotNull("viewTb must be given!", viewTb);
		Assert.assertNotNull("editTb must be given!", editTb);
		
		this.mainTb = mainTb;
		this.viewTb = viewTb;
		this.editTb = editTb;
		
		this.canvasWidget = parent;
		
//		initMainTb(mainTb);
		createViewItems(mainTb);
		createEditItems(editTb);
		
		addListeners();
		updateButtonVisibility();
	}
	
	void initMainTb(ToolBar tb) {
		if (tb == null) { // never happens
			tb = new ToolBar(canvasWidget, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		}
		tb.setLayout(new GridLayout(1, false));
	
//		viewTbEnabledToolItem = new ToolItem(tb, SWT.CHECK);
//		viewTbEnabledToolItem.setToolTipText("Show view canvas toolbar");
//		viewTbEnabledToolItem.setImage(Images.getOrLoad("/icons/shape_square_edit.png"));
//		viewTbEnabledToolItem.setSelection(canvasWidget.getCanvas().getSettings().isEditingEnabled());
		
		if (false) {
		editingEnabledToolItem = new ToolItem(tb, SWT.CHECK);
		editingEnabledToolItem.setToolTipText("Show edit canvas toolbar");
		editingEnabledToolItem.setImage(Images.getOrLoad("/icons/shape_square_edit.png"));
		editingEnabledToolItem.setSelection(canvasWidget.getCanvas().getSettings().isEditingEnabled());
		}
	}
	
	void intiViewTb(ToolBar tb) {
		
		
	}
	
	public void createViewItems(ToolBar tb) {
		selectionMode = new ToolItem(tb, SWT.RADIO);
		selectionMode.setToolTipText("Selection mode");
		selectionMode.setSelection(true);
		selectionMode.setImage(Images.getOrLoad("/icons/cursor.png"));
		modeMap.put(selectionMode, CanvasMode.SELECTION);
		
		ToolItem visItem = new ToolItem(tb, SWT.CHECK);
		visItem.setToolTipText("Shape visibility...");
		visItem.setImage(Images.EYE);
			
		ToolBox visBox = new ToolBox(canvasWidget.mainWidgetUi.getShell(), true, "Item visibility");
		visBox.addTriggerWidget(visItem);
		showRegionsButton = visBox.addButton("Show regions", Images.getOrLoad("/icons/show_regions_shape.png"), SWT.CHECK);
		showLinesButton = visBox.addButton("Show lines", Images.getOrLoad("/icons/show_lines_shape.png"), SWT.CHECK);
		showBaselinesButton = visBox.addButton("Show baselines", Images.getOrLoad("/icons/show_baselines_shape.png"), SWT.CHECK);
		showWordsButton = visBox.addButton("Show words", Images.getOrLoad("/icons/show_word_shape.png"), SWT.CHECK);
//		showPrintspaceButton = visBox.addButton("Show printspace", Images.getOrLoad("/icons/show_ps_shape.png"), SWT.CHECK);
		renderBlackeningsButton = visBox.addButton("Render blackenings", Images.getOrLoad("/icons/rabbit-silhouette.png"), SWT.CHECK);
		showReadingOrderRegionsButton = visBox.addButton("Show regions reading order", Images.READING_ORDER_REGIONS, SWT.CHECK);
		showReadingOrderLinesButton = visBox.addButton("Show lines reading order", Images.READING_ORDER_LINES, SWT.CHECK);
		showReadingOrderWordsButton = visBox.addButton("Show words reading order", Images.READING_ORDER_WORDS, SWT.CHECK);

		zoomSelection = new ToolItem(tb, SWT.RADIO);
		zoomSelection.setToolTipText("Zoom selection mode");
		zoomSelection.setImage(Images.getOrLoad("/icons/zoom_rect.png"));
		modeMap.put(zoomSelection, CanvasMode.ZOOM);
		
		loupe = new ToolItem(tb, SWT.RADIO);
		loupe.setToolTipText("Loupe mode");
		loupe.setImage(Images.getOrLoad("/icons/zoom.png"));
		modeMap.put(loupe, CanvasMode.LOUPE);		
		
		// toolItem = new ToolItem(this, SWT.SEPARATOR);

		zoomIn = new ToolItem(tb, SWT.NONE);

		zoomIn.setToolTipText("Zoom in");
		zoomIn.setImage(Images.getOrLoad("/icons/zoom_in.png"));

		zoomOut = new ToolItem(tb, SWT.NONE);

		zoomOut.setToolTipText("Zoom out");
		zoomOut.setImage(Images.getOrLoad("/icons/zoom_out.png"));
				
		if (false) {
		fitItem = new DropDownToolItem(tb, false, true, false, SWT.NONE);
		fitItem.addItem("Fit to page", Images.getOrLoad("/icons/arrow_in.png"), "Fit to page");
		fitItem.addItem("Original size", Images.getOrLoad( "/icons/arrow_out.png"), "Original size");
		fitItem.addItem("Fit to width", Images.getOrLoad("/icons/arrow_left_right.png"), "Fit to width");
		fitItem.addItem("Fit to height", Images.getOrLoad("/icons/arrow_up_down.png"), "Fit to height");
		
		rotateItem = new DropDownToolItem(tb, false, true, false, SWT.NONE);
		rotateItem.addItem("Rotate left", Images.getOrLoad("/icons/arrow_turn_left.png"), "Rotate left");
		rotateItem.addItem("Rotate right", Images.getOrLoad("/icons/arrow_turn_right.png"), "Rotate right");
		rotateItem.addItem("Rotate left 90 degress", Images.getOrLoad("/icons/arrow_turn_left_90.png"), "Rotate left 90 degress");
		rotateItem.addItem("Rotate right 90 degrees", Images.getOrLoad("/icons/arrow_turn_right_90.png"), "Rotate right 90 degrees");
		rotateItem.addItem("Translate left", Images.getOrLoad("/icons/arrow_left.png"), "Translate left");
		rotateItem.addItem("Translate right", Images.getOrLoad("/icons/arrow_right.png"), "Translate right");
		rotateItem.addItem("Translate up", Images.getOrLoad("/icons/arrow_up.png"), "Translate up");
		rotateItem.addItem("Translate down", Images.getOrLoad("/icons/arrow_down.png"), "Translate down");
		}
		else {
			new ToolItem(tb, SWT.SEPARATOR);
		fitPageItem = new ToolItem(tb, SWT.PUSH);
		fitPageItem.setImage(Images.getOrLoad("/icons/arrow_in.png"));
		fitPageItem.setToolTipText("Fit to page");
		
		fitWidthItem = new ToolItem(tb, SWT.PUSH);
		fitWidthItem.setImage(Images.getOrLoad("/icons/arrow_left_right.png"));
		fitWidthItem.setToolTipText("Fit to width");
		
		origSizeItem = new ToolItem(tb, SWT.PUSH);
		origSizeItem.setImage(Images.getOrLoad("/icons/arrow_out.png"));
		origSizeItem.setToolTipText("Original size");			
			
		ToolItem moveItem = new ToolItem(tb, SWT.CHECK);
//		moveItem.setImage(Images.getOrLoad("/icons/arrow_inout.png"));
		moveItem.setText("...");
		moveItem.setToolTipText("Other image transformations...");
		
		ToolBox imgTransformsToolBox = new ToolBox(canvasWidget.mainWidgetUi.getShell(), true, "Image transformations");
		imgTransformsToolBox.addTriggerWidget(moveItem);
		rotateLeftBtn = imgTransformsToolBox.addButton("Rotate left", Images.getOrLoad("/icons/arrow_turn_left.png"), SWT.PUSH);
		rotateRightBtn = imgTransformsToolBox.addButton("Rotate right", Images.getOrLoad("/icons/arrow_turn_right.png"), SWT.PUSH);
		rotateLeft90Btn = imgTransformsToolBox.addButton("Rotate left 90 degrees", Images.getOrLoad("/icons/arrow_turn_left_90.png"), SWT.PUSH);
		rotateRight90Btn = imgTransformsToolBox.addButton("Rotate right 90 degrees", Images.getOrLoad("/icons/arrow_turn_right_90.png"), SWT.PUSH);
		
		translateLeftBtn = imgTransformsToolBox.addButton("Translate left", Images.getOrLoad("/icons/arrow_left.png"), SWT.PUSH);
		translateRightBtn = imgTransformsToolBox.addButton("Translate right", Images.getOrLoad("/icons/arrow_right.png"), SWT.PUSH);
		translateUpBtn = imgTransformsToolBox.addButton("Translate up", Images.getOrLoad("/icons/arrow_up.png"), SWT.PUSH);
		translateDownBtn = imgTransformsToolBox.addButton("Translate down", Images.getOrLoad("/icons/arrow_down.png"), SWT.PUSH);
			new ToolItem(tb, SWT.SEPARATOR);
		}
		
		
//		imageVersionDropdown = new DropDownToolItem(tb, true, false, true, SWT.RADIO);
		imageVersionDropdown = new DropDownToolItem(tb, false, false, true, SWT.RADIO);
		imageVersionDropdown.ti.setImage(Images.IMAGES);
		
		String versText = "Image file type displayed\n\torig: original image\n\tview: modified viewing file";
		imageVersionDropdown.addItem("original image", Images.IMAGE, versText, false, "orig");
		imageVersionDropdown.addItem("modified image", Images.IMAGE_EDIT, versText, true, "view");
		imageVersionDropdown.selectItem(1, false);
		
		
		imgEnhanceItem = new ToolItem(tb, SWT.PUSH);
		imgEnhanceItem.setToolTipText("Change image contrast settings");
		imgEnhanceItem.setImage(Images.CONTRAST);
		
		viewSettingsMenuItem = new ToolItem(tb, SWT.PUSH);
		viewSettingsMenuItem.setToolTipText("Change &viewing settings...");
		viewSettingsMenuItem.setImage(Images.getOrLoad("/icons/palette.png"));

		if (false) {
		helpItem = new ToolItem(tb, SWT.PUSH);
		helpItem.setToolTipText("How To Guides...");
		helpItem.setImage(Images.HELP);
		}
		
		if (true) {
		new ToolItem(tb, SWT.SEPARATOR);
		editingEnabledToolItem = new ToolItem(tb, SWT.CHECK);
		editingEnabledToolItem.setToolTipText("Show edit toolbar");
		editingEnabledToolItem.setImage(Images.getOrLoad("/icons/shape_square_edit.png"));
		editingEnabledToolItem.setSelection(canvasWidget.getCanvas().getSettings().isEditingEnabled());
		editingEnabledToolItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				canvasWidget.toggleToolbarVisiblity(editTb, editingEnabledToolItem.getSelection());
			}
		});
		}		
				
		tb.pack();
	}
	
	public void createEditItems(ToolBar tb) {
		
		if (false) {
		editingEnabledToolItem = new ToolItem(tb, SWT.CHECK);
		editingEnabledToolItem.setToolTipText("Enable shape editing");
		editingEnabledToolItem.setImage(Images.getOrLoad("/icons/shape_square_edit.png"));
		editingEnabledToolItem.setSelection(canvasWidget.getCanvas().getSettings().isEditingEnabled());
		}
		
		if (true) {
		addTextRegionItem = new ToolItem(tb, SWT.PUSH);
		addTextRegionItem.setToolTipText("Add a text region");
		addTextRegionItem.setImage(Images.ADD);
		addTextRegionItem.setText("TR");
		modeMap.put(addTextRegionItem, CanvasMode.ADD_TEXTREGION);
		
		addLineItem = new ToolItem(tb, SWT.PUSH);
		addLineItem.setToolTipText("Add a line");
		addLineItem.setImage(Images.ADD);
		addLineItem.setText("L");
		modeMap.put(addLineItem, CanvasMode.ADD_LINE);
		
		addBaselineItem = new ToolItem(tb, SWT.PUSH);
		addBaselineItem.setToolTipText("Add a baseline");
		addBaselineItem.setImage(Images.ADD);
		addBaselineItem.setText("BL");
		modeMap.put(addBaselineItem, CanvasMode.ADD_BASELINE);
		
		addWordItem = new ToolItem(tb, SWT.PUSH);
		addWordItem.setToolTipText("Add a word");
		addWordItem.setImage(Images.ADD);
		addWordItem.setText("W");
		modeMap.put(addWordItem, CanvasMode.ADD_WORD);
		}
		
//		addElementDropdown = new DropDownToolItem(tb, true, true, false, SWT.PUSH); // ORIG
//		addElementDropdown = new DropDownToolItem(tb, false, true, true, SWT.PUSH); // TEST
		addElementDropdown = new DropDownToolItem(tb, false, false, true, SWT.PUSH); // TEST
		addElementDropdown.ti.setImage(Images.ADD);
		addElementDropdown.ti.setText("...");
		addElementDropdown.ti.setToolTipText("Add other item...");
		
		String tt = "Add other item...";
				
		MenuItem mi = null;
		
		if (false) {
		mi = addElementDropdown.addItem(RegionTypeUtil.TEXT_REGION, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TEXT_REGION));
		modeMap.put(mi, CanvasMode.ADD_TEXTREGION);
		
		mi = addElementDropdown.addItem(RegionTypeUtil.LINE, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.LINE));
		modeMap.put(mi, CanvasMode.ADD_LINE);
		
		mi = addElementDropdown.addItem(RegionTypeUtil.BASELINE, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.BASELINE));
		modeMap.put(mi, CanvasMode.ADD_BASELINE);
		
		mi = addElementDropdown.addItem(RegionTypeUtil.WORD, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.WORD));
		modeMap.put(mi, CanvasMode.ADD_WORD);
		}
		
		mi = addElementDropdown.addItem(RegionTypeUtil.TABLE, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TABLE));
		modeMap.put(mi, CanvasMode.ADD_TABLEREGION);
		
		mi = addElementDropdown.addItem(RegionTypeUtil.PRINTSPACE, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(RegionTypeUtil.PRINTSPACE));
		modeMap.put(mi, CanvasMode.ADD_PRINTSPACE);
		
		for (String name : RegionTypeUtil.SPECIAL_REGIONS) {
//				mode.data = c;
			mi = addElementDropdown.addItem(name, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, RegionTypeUtil.getRegionClass(name));
			modeMap.put(mi, CanvasMode.ADD_OTHERREGION);	
		}	
		
		//is not a classical region but all lines belonging to an article will be marked 
		mi = addElementDropdown.addItem(RegionTypeUtil.ARTICLE, Images.getOrLoad("/icons/shape_square_add.png"), tt, false, null);
		modeMap.put(mi, CanvasMode.ADD_ARTICLE);
				
		removeShape = new ToolItem(tb, SWT.PUSH);
		removeShape.setToolTipText("Remove a shape");
//		removeShape.setImage(Images.getOrLoad("/icons/delete.png"));
		removeShape.setImage(Images.DELETE);
		
		addPoint = new ToolItem(tb, SWT.RADIO);
//		addPoint = new ToolItem(tb, SWT.PUSH);
		addPoint.setToolTipText("Add point to selected shape");
		addPoint.setImage(Images.getOrLoad("/icons/vector_add.png"));
		modeMap.put(addPoint, CanvasMode.ADD_POINT);
		
		removePoint = new ToolItem(tb, SWT.RADIO);
//		removePoint = new ToolItem(tb, SWT.PUSH);
		removePoint.setToolTipText("Remove point from selected shape");
		removePoint.setImage(Images.getOrLoad("/icons/vector_delete.png"));
		modeMap.put(removePoint, CanvasMode.REMOVE_POINT);
		
		if (false) {
		splitDropdown = new DropDownToolItem(tb, true, false, false, SWT.PUSH);
		
//		splitHorizontalItem = splitDropdown.addItem("Split by horizontal line", Images.getOrLoad("/icons/scissor_h.png"), "Split a shape", true);
		String stt = "Split a shape";
		splitHorizontalItem = splitDropdown.addItem("Split by horizontal line", Images.SCISSOR, stt, true);
		splitHorizontalItem.setData(DropDownToolItem.ALT_TXT_KEY, "H");
		modeMap.put(splitHorizontalItem, CanvasMode.SPLIT_SHAPE_BY_HORIZONTAL_LINE);
		
//		splitVerticalItem = splitDropdown.addItem("Split by vertical line", Images.getOrLoad("/icons/scissor_v.png"), "Split a shape", false);
		splitVerticalItem = splitDropdown.addItem("Split by vertical line", Images.SCISSOR, stt, false);
		splitVerticalItem.setData(DropDownToolItem.ALT_TXT_KEY, "V");
		modeMap.put(splitVerticalItem, CanvasMode.SPLIT_SHAPE_BY_VERTICAL_LINE);
		
//		splitLineItem = splitDropdown.addItem("Split by custom line", Images.getOrLoad("/icons/scissor_l.png"), "Split a shape", false);
		splitLineItem = splitDropdown.addItem("Split by custom line", Images.SCISSOR, stt, false);
		splitLineItem.setData(DropDownToolItem.ALT_TXT_KEY, "L");
		modeMap.put(splitLineItem, CanvasMode.SPLIT_SHAPE_LINE);
		
		splitDropdown.ti.setImage(Images.SCISSOR);
		splitDropdown.ti.setText("H");
		splitDropdown.selectItem(0, false);
		}
		
		if (true) {
		splitShapeWithHorizontalLine = new ToolItem(tb, SWT.RADIO);
		splitShapeWithHorizontalLine.setToolTipText("Splits a shape with a horizontal line");
		splitShapeWithHorizontalLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeWithHorizontalLine.setText("H");
		modeMap.put(splitShapeWithHorizontalLine, CanvasMode.SPLIT_SHAPE_BY_HORIZONTAL_LINE);
		
		splitShapeWithVerticalLine = new ToolItem(tb, SWT.RADIO);
		splitShapeWithVerticalLine.setToolTipText("Splits a shape with a vertical line");
		splitShapeWithVerticalLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeWithVerticalLine.setText("V");
		modeMap.put(splitShapeWithVerticalLine, CanvasMode.SPLIT_SHAPE_BY_VERTICAL_LINE);	
		
		splitShapeLine = new ToolItem(tb, SWT.RADIO);
		splitShapeLine.setToolTipText("Splits a shape with a custom polyline");
		splitShapeLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeLine.setText("L");
		modeMap.put(splitShapeLine, CanvasMode.SPLIT_SHAPE_LINE);
		}
		
		mergeShapes = new ToolItem(tb, SWT.PUSH);
		mergeShapes.setToolTipText("Merges the selected shapes");
		mergeShapes.setImage(Images.getOrLoad("/icons/merge.png"));
		
		// table cell markup
		markupItem = new ToolItem(tb, SWT.CHECK);
		markupItem.setToolTipText("Table Cell markup");
		markupItem.setImage(Images.BORDER_MENU);
		
//		markupBox = new TableToolBox(canvasWidget.mainWidgetUi.getShell(), true, "Cell borders");
		
		if (true) {
		otherSegmentationToolsDropDown = new DropDownToolItem(tb, false, false, true, SWT.PUSH); // TEST
		otherSegmentationToolsDropDown.ti.setImage(Images.SHAPE_SQUARE_EDIT);
		otherSegmentationToolsDropDown.ti.setText("...");
		otherSegmentationToolsDropDown.ti.setToolTipText("Other segmentation tools...");

		createImageSizeTextRegionItem = otherSegmentationToolsDropDown.addItem("Create top level text region with size of image", null, null);
		createDefaultLineItem = otherSegmentationToolsDropDown.addItem("Create default line for selected line / baseline", null, null);
		simplifySelectedLinesItem = otherSegmentationToolsDropDown.addItem("Simplify shape of selected lines", Images.getOrLoad("/icons/vector.png"), null);
		simplifyAllLinesItem = otherSegmentationToolsDropDown.addItem("Simplify shape of all lines on this page", Images.getOrLoad("/icons/vector.png"), null);
		removeSmallTextRegionsItem = otherSegmentationToolsDropDown.addItem("Remove small text regions...", null, null);
		rectifyAllRegionsItem = otherSegmentationToolsDropDown.addItem("Convert all region shapes to rectangles", null, null);
		}
		
		if (false) {
		simplifyEpsItem = new DropDownToolItem(tb, false, true, false, SWT.RADIO);
		for (int i=5; i<=100; i+=5)
			simplifyEpsItem.addItem(""+i, Images.getOrLoad("/icons/vector.png"), "");
		simplifyEpsItem.selectItem(14, false);
		simplifyEpsItem.ti.setToolTipText(
				"Simplify the selected shape using the Ramer-Douglas-Peucker algorithm\n"
				+ "The value determines the strength of polygon simplification - the higher the value, the more points are removed. "
				+ "\n (i.e. Epsilon is set as the given percentage of the diameter of the bounding box of the shape)");
		}		
		
		optionsItem = new DropDownToolItem(tb, false, true, true, SWT.CHECK);
		optionsItem.ti.setImage(Images.WRENCH);
//		optionsItem.setKeepMenuOpenOnClick(true);
		
		String ott = "Canvas options...";
		
		rectangleModeItem = optionsItem.addItem("Rectangle mode - add all shapes as rectangles initially", Images.WRENCH, ott);
		useScrollBarsItem = optionsItem.addItem("Use scrollbars instead of freely movable image (experimental for rotated images)", Images.WRENCH, ott);
		autoCreateParentItem = optionsItem.addItem("Create missing parent shapes (regions or lines) automatically", Images.WRENCH, ott);
		addLineToOverlappingRegionItem = optionsItem.addItem("Add lines to overlapping parent regions (else: use the selected region as parent)", Images.WRENCH, ott);
		addBaselineToOverlappingLineItem = optionsItem.addItem("Add baselines to overlapping parent lines (else: use the selected line as parent)", Images.WRENCH, ott);
		addWordsToOverlappingLineItem = optionsItem.addItem("Add words to overlapping parent lines (else: use the selected line as parent)", Images.WRENCH, ott);
		selectNewlyCreatedShapeItem = optionsItem.addItem("Select a new shape after it was created", Images.WRENCH, ott);
		lockZoomOnFocusItem = optionsItem.addItem("Lock zoom on focus", Images.WRENCH, "");
		deleteLineIfBaselineDeletedItem = optionsItem.addItem("Delete line if baseline is deleted", Images.WRENCH, ott);

//		new ToolItem(this, SWT.SEPARATOR);
		undo = new ToolItem(tb, SWT.PUSH);
		undo.setToolTipText("Undo last edit step");
		undo.setImage(Images.ARROW_UNDO);
		
		canvasHelpItem = new ToolItem(tb, SWT.PUSH);
		canvasHelpItem.setToolTipText("Canvas shortcuts...");
		canvasHelpItem.setImage(Images.HELP);
		
		tb.layout();
	}
	
	protected void addToRadioGroup(Item item) {
		addItemSelectionListener(item, radioGroupSelectionAdapter);
	}
	
	private void selectItem(Item i, boolean selected) {
		if (i instanceof ToolItem)
			((ToolItem) i).setSelection(selected);
		else if (i instanceof MenuItem)
			((MenuItem) i).setSelection(selected);
	}
	
	private void addItemSelectionListener(Item i, SelectionListener l) {
		if (i instanceof ToolItem) {
			((ToolItem) i).addSelectionListener(l);
		}
		else if (i instanceof MenuItem) {
			((MenuItem) i).addSelectionListener(l);
		}
	}
	
	private void addListeners() {
		// enforces that only one of the modeGroup elements is enabled at once!
		radioGroupSelectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.trace("modeMap size: "+modeMap.keySet().size());
				for (Item i : modeMap.keySet()) {
					logger.trace("toolitem, mode: "+modeMap.get(i));
					selectItem(i, i == e.getSource());
				}
			}
		};
		
		// update mode buttons on mode property change:
		canvasWidget.getCanvas().getSettings().addPropertyChangeListener(new PropertyChangeListener(){
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(CanvasSettings.MODE_PROPERTY)) {
					CanvasMode mode = canvasWidget.getCanvas().getSettings().getMode();
					for (Item i : modeMap.keySet()) {
//						i.setSelection(modeMap.get(i) == mode);
						selectItem(i, modeMap.get(i) == mode);
					}
				}
			}
		});
		
//		if (editingEnabledToolItem != null)
//			DataBinder.get().bindBoolBeanValueToToolItemSelection("editingEnabled", canvasWidget.getCanvas().getSettings(), editingEnabledToolItem);
		
//		editingEnabledToolItem.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				canvasWidget.getCanvas().getSettings().setEditingEnabled(editingEnabledToolItem.getSelection());
//			}			
//		});
		
		for (Item i : modeMap.keySet()) {
			addToRadioGroup(i);
		}

	}

//	public void addSelectionListener(SelectionListener listener) {
//		SWTUtil.addSelectionListener(selectionMode, listener);
//		SWTUtil.addSelectionListener(zoomSelection, listener);
//		SWTUtil.addSelectionListener(zoomIn, listener);
//		SWTUtil.addSelectionListener(zoomOut, listener);
//		SWTUtil.addSelectionListener(loupe, listener);
////		SWTUtil.addToolItemSelectionListener(rotateLeft, listener);
////		SWTUtil.addToolItemSelectionListener(rotateRight, listener);
//		SWTUtil.addSelectionListener(fitItem, listener);
//		SWTUtil.addSelectionListener(rotateItem, listener);
////		SWTUtil.addToolItemSelectionListener(translateItem.ti, listener);
//		
//		SWTUtil.addSelectionListener(focus, listener);
//		SWTUtil.addSelectionListener(addPoint, listener);
//		SWTUtil.addSelectionListener(removePoint, listener);
////		SWTUtil.addSelectionListener(addShape, listener);
//		SWTUtil.addSelectionListener(removeShape, listener);
//		SWTUtil.addSelectionListener(simplifyEpsItem, listener);
//		SWTUtil.addSelectionListener(undo, listener);
//		
//		SWTUtil.addSelectionListener(splitHorizontalItem, listener);
//		SWTUtil.addSelectionListener(splitVerticalItem, listener);
//		SWTUtil.addSelectionListener(splitLineItem, listener);
//		
//		SWTUtil.addSelectionListener(splitShapeLine, listener);
//		SWTUtil.addSelectionListener(splitShapeWithVerticalLine, listener);
//		SWTUtil.addSelectionListener(splitShapeWithHorizontalLine, listener);
//		
//		SWTUtil.addSelectionListener(mergeShapes, listener);
//	
//		SWTUtil.addSelectionListener(imageVersionDropdown, listener);
//		
////		SWTUtil.addSelectionListener(addPrintspace, listener);
////		SWTUtil.addSelectionListener(addTextRegion, listener);
////		SWTUtil.addSelectionListener(addLine, listener);
////		SWTUtil.addSelectionListener(addBaseLine, listener);
////		SWTUtil.addSelectionListener(addWord, listener);
//		
//		SWTUtil.addSelectionListener(addTextRegionItem, listener);
//		SWTUtil.addSelectionListener(addLineItem, listener);
//		SWTUtil.addSelectionListener(addBaselineItem, listener);
//		SWTUtil.addSelectionListener(addWordItem, listener);
//		
//		SWTUtil.addSelectionListener(addElementDropdown, listener);
//		SWTUtil.addSelectionListener(splitDropdown, listener);
//
//		SWTUtil.addSelectionListener(viewSettingsMenuItem, listener);
//		
//		SWTUtil.addSelectionListener(imgEnhanceItem, listener);
//		
//		SWTUtil.addSelectionListener(helpItem, listener);
//		
//		// table stuff
////		SWTUtil.addSelectionListener(deleteRowItem, listener);
////		SWTUtil.addSelectionListener(deleteColumnItem, listener);
////		SWTUtil.addSelectionListener(splitMergedCell, listener);
////		SWTUtil.addSelectionListener(removeIntermediatePtsItem, listener);
//	}

	public ToolItem getZoomIn() {
		return zoomIn;
	}

	public ToolItem getZoomOut() {
		return zoomOut;
	}

	public ToolItem getZoomSelection() {
		return zoomSelection;
	}
	
	public DropDownToolItem getRotateItem() {
		return rotateItem;
	}
	
//	public DropDownToolItem getTranslateItem() {
//		return translateItem;
//	}
	
	public DropDownToolItem getFitItem() {
		return fitItem;
	}

//	public ToolItem getRotateRight() {
//		return rotateRight;
//	}
//
//	public ToolItem getRotateLeft() {
//		return rotateLeft;
//	}

//	public ToolItem getTranslateLeft() {
//		return translateLeft;
//	}

//	public ToolItem getFitToPage() {
//		return fitToPage;
//	}
//	
//	public ToolItem getFitWidth() {
//		return fitWidth;
//	}
//	
//	public ToolItem getFitHeight() {
//		return fitHeight;
//	}

//	public ToolItem getTranslateRight() {
//		return translateRight;
//	}

	public ToolItem getSelectionMode() {
		return selectionMode;
	}

//	public ToolItem getTranslateDown() {
//		return translateDown;
//	}
//
//	public ToolItem getTranslateUp() {
//		return translateUp;
//	}

	public ToolItem getOriginalSize() {
		return originalSize;
	}

	public ToolItem getFocus() {
		return focus;
	}

	public ToolItem getAddPoint() {
		return addPoint;
	}

	public ToolItem getRemovePoint() {
		return removePoint;
	}

//	public ToolItem getAddShape() {
//		return addShape;
//	}

	public ToolItem getRemoveShape() {
		return removeShape;
	}

	public ToolItem getMergeShapes() {
		return mergeShapes;
	}
	
	public ToolItem getBorderMarkupDialog() {
		return markupItem;
	}

//	public ToolItem getSplitShape() {
//		return splitShapeLine;
//	}
	
//	public ToolItem getSimplifyShape() {
//		return simplifyShape;
//	}
	
//	public ToolItem getEditingEnabledToolItem() { 
//		return editingEnabledToolItem;
//	}
	
	public HashMap<Item, CanvasMode> getModeMap() {
		return modeMap;
	}

	public ToolItem getUndo() { return undo; }
	
	public ToolItem getViewSettingsMenuItem() {
		return viewSettingsMenuItem;
	}	
	
	public DropDownToolItem getSimplifyEpsItem() { return simplifyEpsItem; }
		
//	public DropDownToolItem getSplitTypeItem() { return splitTypeItem; }
	
//	@Override
//	protected void checkSubclass() {
//		// Disable the check that prevents subclassing of SWT components
//	}
	
	public void updateButtonVisibility() {
		if (canvasWidget == null)
			return;

		ICanvasShape selected = canvasWidget.getCanvas().getFirstSelected();
		boolean notNullAndEditable = selected != null && selected.isEditable();
		logger.trace("shape notNullAndEditable: " + notNullAndEditable);

		boolean isEditingEnabled = canvasWidget.getCanvas().getSettings().isEditingEnabled();

		SWTUtil.setEnabled(addPoint, isEditingEnabled && notNullAndEditable && selected.canInsert());
		SWTUtil.setEnabled(addPoint, isEditingEnabled && notNullAndEditable && selected.canInsert());
		SWTUtil.setEnabled(removePoint, isEditingEnabled && notNullAndEditable && selected.canRemove());
		SWTUtil.setEnabled(removeShape, isEditingEnabled && notNullAndEditable);
		
		//enable the 'add shapes' btns
		SWTUtil.setEnabled(addTextRegionItem, isEditingEnabled);
		SWTUtil.setEnabled(addLineItem, isEditingEnabled);
		SWTUtil.setEnabled(addBaselineItem, isEditingEnabled);
		SWTUtil.setEnabled(addWordItem, isEditingEnabled);	
		SWTUtil.setEnabled(addElementDropdown, isEditingEnabled);
		
		//options to set
		SWTUtil.setEnabled(otherSegmentationToolsDropDown, isEditingEnabled);
		SWTUtil.setEnabled(optionsItem, isEditingEnabled);
		
		SWTUtil.setEnabled(splitDropdown, isEditingEnabled && notNullAndEditable);
		
		SWTUtil.setEnabled(splitShapeLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeWithVerticalLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeWithHorizontalLine, isEditingEnabled && notNullAndEditable);
		
		SWTUtil.setEnabled(markupItem, isEditingEnabled && notNullAndEditable && canvasWidget.getCanvas().getScene().getSelectedTableCellShapes().size() > 0);
		
		SWTUtil.setEnabled(mergeShapes, isEditingEnabled && notNullAndEditable && canvasWidget.getCanvas().getScene().getNSelected() > 1);
		SWTUtil.setEnabled(simplifyEpsItem, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(undo, isEditingEnabled);

//		for (ToolItem ai : addItems) {
//			SWTUtil.setEnabled(ai, isEditingEnabled);
//		}

		SWTUtil.setEnabled(optionsItem, isEditingEnabled);
	}

//	protected void initTrpCanvasToolBar() {
//			modeMap.remove(addShape);
//			addShape.dispose();
//			
//			imageVersionItem = new DropDownToolItem(this, true, false, SWT.RADIO, 0);
//			
//			String versText = "Image file type displayed\n\torig: original image\n\tview: compressed viewing file\n\tbin: binarized image";
//			imageVersionItem.addItem("orig", null, versText, false);
//			imageVersionItem.addItem("view", null, versText, true);
//			imageVersionItem.addItem("bin", null, versText, false);
//			imageVersionItem.selectItem(1, false);
//			
//			imgEnhanceItem = new ToolItem(this, SWT.PUSH, 0);
//			imgEnhanceItem.setImage(Images.CONTRAST);
//			
//			int i = indexOf(editingEnabledToolItem);
//			logger.debug("index = "+i);
//			
////			if (false) {
////			addPrintspace = new ToolItem(this, SWT.RADIO, ++i);
////			addPrintspace.setText("PS");
////			addPrintspace.setToolTipText("Add a printspace");
////			addPrintspace.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
////			modeMap.put(addPrintspace, TrpCanvasAddMode.ADD_PRINTSPACE);
////			addToRadioGroup(addPrintspace);
////			}
////			
////			if (false) {
////			addTextRegion = new ToolItem(this, SWT.RADIO, ++i);
////			addTextRegion.setText("TR");
////			addTextRegion.setToolTipText("Add a text region");
////			addTextRegion.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
////			modeMap.put(addTextRegion, TrpCanvasAddMode.ADD_TEXTREGION);
////			addToRadioGroup(addTextRegion);
////			
////			addLine = new ToolItem(this, SWT.RADIO, ++i);
////			addLine.setText("L");
////			addLine.setToolTipText("Add a line");
////			addLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
////			modeMap.put(addLine, TrpCanvasAddMode.ADD_LINE);
////			addToRadioGroup(addLine);
////			
////			addBaseLine = new ToolItem(this, SWT.RADIO, ++i);
////			addBaseLine.setText("BL");
////			addBaseLine.setToolTipText("Add a baseline");
////			addBaseLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
////			modeMap.put(addBaseLine, TrpCanvasAddMode.ADD_BASELINE);
////			addToRadioGroup(addBaseLine);
////	
////			addWord = new ToolItem(this, SWT.RADIO, ++i);
////			addWord.setText("W");
////			addWord.setToolTipText("Add a word");
////			addWord.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
////			modeMap.put(addWord, TrpCanvasAddMode.ADD_WORD);
////			addToRadioGroup(addWord);
////			}
//					
//			if (true) {
//				addSpecialRegion = new DropDownToolItem(this, true, true, SWT.PUSH, ++i);
//				
//				MenuItem mi = null;
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.TEXT_REGION, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TEXT_REGION));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_TEXTREGION);
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.LINE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.LINE));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_LINE);
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.BASELINE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.BASELINE));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_BASELINE);
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.WORD, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.WORD));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_WORD);		
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.TABLE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TABLE));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_TABLEREGION);
//				
//				mi = addSpecialRegion.addItem(RegionTypeUtil.PRINTSPACE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.PRINTSPACE));
//				modeMap.put(mi, TrpCanvasAddMode.ADD_PRINTSPACE);
//				
//				for (String name : RegionTypeUtil.SPECIAL_REGIONS) {
//	//				mode.data = c;
//					mi = addSpecialRegion.addItem(name, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(name));
//					modeMap.put(mi, TrpCanvasAddMode.ADD_OTHERREGION);	
//				}
//				
//	//			CanvasMode mode = TrpCanvasAddMode.ADD_OTHERREGION;
//	//			modeMap.put(addSpecialRegion.ti, TrpCanvasAddMode.ADD_OTHERREGION);
//				
//	//			for (RegionType rt : specialRegions) {
//	//				addSpecialRegion.addItem(rt.getClass().getSimpleName(), Images.getOrLoad("/icons/shape_square_add.png"), "");	
//	//			}
//			}
//			
//			
//			optionsItem = new DropDownToolItem(this, false, true, SWT.CHECK, ++i);
//			optionsItem.ti.setImage(Images.getOrLoad("/icons/wrench.png"));
//			rectangleModeItem = optionsItem.addItem("Rectangle mode - add all shapes as rectangles initially", Images.getOrLoad("/icons/wrench.png"), "");
//			autoCreateParentItem = optionsItem.addItem("Create missing parent shapes (regions or lines) automatically", Images.getOrLoad("/icons/wrench.png"), "");
//			addLineToOverlappingRegionItem = optionsItem.addItem("Add lines to overlapping parent regions (else: use the selected region as parent)", Images.getOrLoad("/icons/wrench.png"), "");
//			addBaselineToOverlappingLineItem = optionsItem.addItem("Add baselines to overlapping parent lines (else: use the selected line as parent)", Images.getOrLoad("/icons/wrench.png"), "");
//			addWordsToOverlappingLineItem = optionsItem.addItem("Add words to overlapping parent lines (else: use the selected line as parent)", Images.getOrLoad("/icons/wrench.png"), "");
//			selectNewlyCreatedShapeItem = optionsItem.addItem("Select a new shape after it was created", Images.getOrLoad("/icons/wrench.png"), "");
//			lockZoomOnFocusItem = optionsItem.addItem("Lock zoom on focus", Images.getOrLoad("/icons/wrench.png"), "");
//			deleteLineIfBaselineDeletedItem = optionsItem.addItem("Delete line if baseline is deleted", Images.getOrLoad("/icons/wrench.png"), "");
//	
//			if (false) {
//			tableItem = new DropDownToolItem(this, false, true, SWT.PUSH, ++i);
//			tableItem.ti.setImage(Images.getOrLoad("/icons/table_edit.png"));
//			deleteRowItem = tableItem.addItem("Delete row of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
//			deleteColumnItem = tableItem.addItem("Delete column of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
//			splitMergedCell = tableItem.addItem("Split up formerly merged cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
//			removeIntermediatePtsItem = tableItem.addItem("Remove intermediate points of cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
//			}
//			
//	//		new ToolItem(this, SWT.SEPARATOR, ++i);
//			
//	//		shapeAddRectMode = new ToolItem(this, SWT.CHECK, ++i);
//	//		shapeAddRectMode.setImage(Images.getOrLoad("/icons/shape_square.png"));
//	//		shapeAddRectMode.setToolTipText("Toggles rectangle mode: if enabled, all shapes (except baselines) are added as rectangles instead of polygons - additional points can be added later however!");
//			
//	//		autoCreateParent = new ToolItem(this, SWT.CHECK, ++i);
//	//		autoCreateParent.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//	//		autoCreateParent.setToolTipText("If enabled, missing parent shapes (i.e. text regions or lines) are automatically created if they are not found!");
//			
//	//		new ToolItem(this, SWT.SEPARATOR, ++i);
//			
//			
//	//		linkShapes = new ToolItem(this, SWT.PUSH, ++i);
//	//		linkShapes.setImage(Images.getOrLoad("/icons/link.png"));
//	//		linkShapes.setToolTipText("Links two shapes, e.g. a footnote and a link to it");
//			
//	//		linkBreakShapes = new ToolItem(this, SWT.PUSH, ++i);
//	//		linkBreakShapes.setImage(Images.getOrLoad("/icons/link_break.png"));
//	//		linkBreakShapes.setToolTipText("Removes an existing link");
//			
////			addItems = new ArrayList<>();
////			addItems.add(addPrintspace);
////			addItems.add(addTextRegion);
////			addItems.add(addLine);
////			addItems.add(addBaseLine);
////			addItems.add(addWord);
//	
//	//		addItems.add(shapeAddRectMode);
//	
//			i = indexOf(mergeShapes);
//			i += 2;
//			
//	//		new ToolItem(this, SWT.SEPARATOR, i);
//			
//			if (false) {
//			renderBlackeningsToggle = new ToolItem(this, SWT.CHECK, ++i);
//			renderBlackeningsToggle.setToolTipText("If toggled, blackening regions are rendered with opaque background");
//			//renderBlackeningsToggle.setText("Render blackenings");
//			renderBlackeningsToggle.setImage(Images.getOrLoad("/icons/rabbit-silhouette.png"));
//					
//			new ToolItem(this, SWT.SEPARATOR, ++i);
//			}
//			
//			i = indexOf(undo);
//			
//			viewSettingsMenuItem = new ToolItem(this, SWT.PUSH, ++i);
//			viewSettingsMenuItem.setToolTipText("Change &viewing settings...");
//			viewSettingsMenuItem.setImage(Images.getOrLoad("/icons/palette.png"));
//			
//	//		new ToolItem(this, SWT.SEPARATOR);
//	
//			this.pack();
//			
//			
//		}

//	public void addBindings(TrpSettings trpSets) {
//		DataBinder.get().bindBoolBeanValueToToolItemSelection(TrpSettings.RENDER_BLACKENINGS_PROPERTY, trpSets, renderBlackeningsToggle);
//	}

	public String getSelectedAddElementType() {
		MenuItem si = addElementDropdown.getSelected();
		return (si != null) ? si.getText() : "";
	}

	public DropDownToolItem getImageVersionDropdown() {
		return imageVersionDropdown;
	}

	public ToolItem getImgEnhanceItem() { 
		return imgEnhanceItem;
	}

	public MenuItem getRectangleModeItem() {
		return rectangleModeItem;
	}
	
	public MenuItem getUseScrollBarsItem() {
		return useScrollBarsItem;
	}

	public MenuItem getAutoCreateParentItem() {
		return autoCreateParentItem;
	}

	public MenuItem getAddLineToOverlappingRegionItem() {
		return addLineToOverlappingRegionItem;
	}

	public MenuItem getAddBaselineToOverlappingLineItem() {
		return addBaselineToOverlappingLineItem;
	}

	public MenuItem getAddWordsToOverlappingLineItem() {
		return addWordsToOverlappingLineItem;
	}

	public MenuItem getLockZoomOnFocusItem() {
		return lockZoomOnFocusItem;
	}

	public MenuItem getDeleteLineIfBaselineDeletedItem() {
		return deleteLineIfBaselineDeletedItem;
	}

	public MenuItem getSelectNewlyCreatedShapeItem() {
		return selectNewlyCreatedShapeItem;
	}

	public DropDownToolItem getAddElementDropDown() {
		return addElementDropdown;
	}

//	public MenuItem getDeleteRowItem() {
//		return deleteRowItem;
//	}
//
//	public MenuItem getDeleteColumnItem() {
//		return deleteColumnItem;
//	}

//	public MenuItem getSplitMergedCell() {
//		return splitMergedCell;
//	}
//
//	public MenuItem getRemoveIntermediatePtsItem() {
//		return removeIntermediatePtsItem;
//	}
	
//	public ToolBar getTb() {
//		return tb;
//	}
//	
//	public ToolBar tb() {
//		return tb;
//	}
//	
//	public ToolBar getToolbar() {
//		return tb;
//	}

	public DropDownToolItem getSplitDropdown() {
		return splitDropdown;
	}
	
	public ToolItem getHelpItem() {
		return helpItem;
	}

	public Object getCanvasHelpItem() {
		return canvasHelpItem;
	}
	
//	public TableToolBox getBorderMarkBox() {
//		return markupBox;
//	}

}
