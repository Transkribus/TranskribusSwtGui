package eu.transkribus.swt_canvas.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.DropDownToolItem;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.canvas.TrpCanvasAddMode;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class CanvasToolBar extends ToolBar {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBar.class);

	// private ToolBar topToolBar;
	protected ToolItem zoomIn;
	protected ToolItem zoomOut;
	protected ToolItem zoomSelection;
	protected ToolItem loupe;
	
	protected DropDownToolItem rotateItem;
	
//	protected ToolItem rotateRight;
//	protected ToolItem rotateLeft;
	
	protected DropDownToolItem fitItem;
//	protected ToolItem fitToPage;
//	protected ToolItem fitWidth;
//	protected ToolItem fitHeight;
	
	protected ToolItem originalSize;
	
	protected ToolItem selectionMode;
	// ToolItem toolItem;
	
	protected DropDownToolItem translateItem;
//	protected ToolItem translateLeft;
//	protected ToolItem translateDown;
//	protected ToolItem translateUp;
//	protected ToolItem translateRight;
	
	
	protected ToolItem focus;
	// EDIT ITEMS:
	protected ToolItem addPoint;	
	protected ToolItem removePoint;
	protected ToolItem addShape;
	protected ToolItem removeShape;
	protected ToolItem mergeShapes;
	protected ToolItem splitShapeLine;
	private static final boolean ENABLE_SPLIT_SHAPE_LINE=true;
	protected ToolItem splitShapeWithVerticalLine;
	protected ToolItem splitShapeWithHorizontalLine;
	
//	protected DropDownToolItem splitTypeItem;
//	protected ToolItem simplifyShape;
	protected DropDownToolItem simplifyEpsItem;
	protected ToolItem undo;
	protected ToolItem editingEnabledToolItem;	
	
	protected ToolItem viewSettingsMenuItem;
	
//	protected HashMap<ToolItem, CanvasMode> modeMap = new HashMap<>();
	protected HashMap<Item, CanvasMode> modeMap = new HashMap<>();
	
	
	
	protected SelectionAdapter radioGroupSelectionAdapter;

//	protected ToolItem addTextRegion;
//
//	protected ToolItem addLine;
//
//	protected ToolItem addBaseLine;
//
//	protected ToolItem addWord;
//
//	protected ToolItem addPrintspace;

	protected DropDownToolItem addSpecialRegion;

	protected DropDownToolItem optionsItem;

	protected ToolItem imgEnhanceItem;

	MenuItem rectangleModeItem;

	MenuItem autoCreateParentItem;

	MenuItem addLineToOverlappingRegionItem;

	MenuItem addBaselineToOverlappingLineItem;

	MenuItem addWordsToOverlappingLineItem;

	MenuItem lockZoomOnFocusItem;

	MenuItem deleteLineIfBaselineDeletedItem;

	MenuItem selectNewlyCreatedShapeItem;

	List<ToolItem> addItems;

	protected CanvasWidget canvasWidget;

	DropDownToolItem imageVersionItem;

	DropDownToolItem tableItem;

	MenuItem deleteRowItem;

	MenuItem deleteColumnItem;

	MenuItem splitMergedCell;

	MenuItem removeIntermediatePtsItem;

	ToolItem renderBlackeningsToggle;

//	TrpMainWidget mainWidget;

	/**
	 * Wraps the DeaSWTCanvas widget into a widget containing a toolbar for the
	 * most common operations such as scaling, rotation, translation etc.
	 */
	public CanvasToolBar(CanvasWidget parent, int style) {
		super(parent, style);
		this.canvasWidget = parent;
//		this.canvas = canvasWidget.getCanvas();
//		this.mainWidget = mainWidget

		init();
	}

	private void init() {
		setLayout(new GridLayout(1, false));

		// topToolBar = new ToolBar(this, SWT.FLAT | SWT.RIGHT);
		// topToolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
		// false, 1, 1));

		selectionMode = new ToolItem(this, SWT.RADIO);
		selectionMode.setToolTipText("Selection mode");
		selectionMode.setSelection(true);
		selectionMode.setImage(Images.getOrLoad("/icons/cursor.png"));
		modeMap.put(selectionMode, CanvasMode.SELECTION);

		// translationMode = new ToolItem(this, SWT.RADIO);
		// translationMode.setToolTipText("Translation mode");
		// translationMode.setSelection(false);
		// translationMode.setImage(Images.getOrLoad(CanvasWidget.class,
		// "/icons/cursor_hand.png"));

		zoomSelection = new ToolItem(this, SWT.RADIO);
		zoomSelection.setToolTipText("Zoom selection mode");
		zoomSelection.setImage(Images.getOrLoad("/icons/zoom_rect.png"));
		modeMap.put(zoomSelection, CanvasMode.ZOOM);
		
		loupe = new ToolItem(this, SWT.RADIO);
		loupe.setToolTipText("Loupe mode");
		loupe.setImage(Images.getOrLoad("/icons/zoom.png"));
		modeMap.put(loupe, CanvasMode.LOUPE);		
		
		// toolItem = new ToolItem(this, SWT.SEPARATOR);

		zoomIn = new ToolItem(this, SWT.NONE);

		zoomIn.setToolTipText("Zoom in");
		zoomIn.setImage(Images.getOrLoad("/icons/zoom_in.png"));

		zoomOut = new ToolItem(this, SWT.NONE);

		zoomOut.setToolTipText("Zoom out");
		zoomOut.setImage(Images.getOrLoad("/icons/zoom_out.png"));

		ToolItem sep1 = new ToolItem(this, SWT.SEPARATOR);
		
		fitItem = new DropDownToolItem(this, false, true, SWT.NONE);
		fitItem.addItem("Fit to page", Images.getOrLoad("/icons/arrow_in.png"), "Fit to page");
		fitItem.addItem("Original size", Images.getOrLoad( "/icons/arrow_out.png"), "Original size");
		fitItem.addItem("Fit to width", Images.getOrLoad("/icons/arrow_left_right.png"), "Fit to width");
		fitItem.addItem("Fit to height", Images.getOrLoad("/icons/arrow_up_down.png"), "Fit to height");

//		fitToPage = new ToolItem(this, SWT.PUSH);
//		fitToPage.setToolTipText("Fit to page");
//		fitToPage.setImage(Images.getOrLoad("/icons/arrow_in.png"));
//		
//		fitWidth = new ToolItem(this, SWT.NONE);
//		fitWidth.setToolTipText("Fit to width");
//		fitWidth.setImage(Images.getOrLoad("/icons/arrow_left_right.png"));
//		
//		fitHeight = new ToolItem(this, SWT.NONE);
//		fitHeight.setToolTipText("Fit to height");
//		fitHeight.setImage(Images.getOrLoad("/icons/arrow_up_down.png"));
		
//		originalSize = new ToolItem(this, SWT.PUSH);
//		originalSize.setToolTipText("Original size");
//		originalSize.setImage(Images.getOrLoad("/icons/arrow_out.png"));		

//		rotateLeft = new ToolItem(this, SWT.PUSH);
//		rotateLeft.setToolTipText("Rotate left");
//		rotateLeft.setImage(Images.getOrLoad("/icons/arrow_turn_left.png"));
//
//		rotateRight = new ToolItem(this, SWT.PUSH);
//		rotateRight.setToolTipText("Rotate right");
//		rotateRight.setImage(Images.getOrLoad("/icons/arrow_turn_right.png"));
		
		rotateItem = new DropDownToolItem(this, false, true, SWT.NONE);
		rotateItem.addItem("Rotate left", Images.getOrLoad("/icons/arrow_turn_left.png"), "Rotate left");
		rotateItem.addItem("Rotate right", Images.getOrLoad("/icons/arrow_turn_right.png"), "Rotate right");
		rotateItem.addItem("Rotate left 90 degress", Images.getOrLoad("/icons/arrow_turn_left_90.png"), "Rotate left 90 degress");
		rotateItem.addItem("Rotate right 90 degrees", Images.getOrLoad("/icons/arrow_turn_right_90.png"), "Rotate right 90 degrees");
		rotateItem.addItem("Translate left", Images.getOrLoad("/icons/arrow_left.png"), "Translate left");
		rotateItem.addItem("Translate right", Images.getOrLoad("/icons/arrow_right.png"), "Translate right");
		rotateItem.addItem("Translate up", Images.getOrLoad("/icons/arrow_up.png"), "Translate up");
		rotateItem.addItem("Translate down", Images.getOrLoad("/icons/arrow_down.png"), "Translate down");		
		
		if (false) {
		translateItem = new DropDownToolItem(this, false, true, SWT.NONE);
		translateItem.addItem("Translate left", Images.getOrLoad("/icons/arrow_left.png"), "Translate left");
		translateItem.addItem("Translate right", Images.getOrLoad("/icons/arrow_right.png"), "Translate right");
		translateItem.addItem("Translate up", Images.getOrLoad("/icons/arrow_up.png"), "Translate up");
		translateItem.addItem("Translate down", Images.getOrLoad("/icons/arrow_down.png"), "Translate down");
		}

//		translateLeft = new ToolItem(this, SWT.PUSH);
//		translateLeft.setToolTipText("Translate left");
//		translateLeft.setImage(Images.getOrLoad("/icons/arrow_left.png"));
//
//		translateRight = new ToolItem(this, SWT.PUSH);
//		translateRight.setToolTipText("Translate right");
//		translateRight.setImage(Images.getOrLoad("/icons/arrow_right.png"));
//
//		translateUp = new ToolItem(this, SWT.PUSH);
//		translateUp.setToolTipText("Translate right");
//		translateUp.setImage(Images.getOrLoad("/icons/arrow_up.png"));
//
//		translateDown = new ToolItem(this, SWT.PUSH);
//		translateDown.setToolTipText("Translate right");
//		translateDown.setImage(Images.getOrLoad("/icons/arrow_down.png"));

		if (false) {
		focus = new ToolItem(this, SWT.PUSH);
		focus.setToolTipText("Focus selected object");
		focus.setImage(Images.getOrLoad("/icons/focus16.png"));
		}
		
		// EDIT BUTTONS:
		ToolItem sep2 = new ToolItem(this, SWT.SEPARATOR);
		
		editingEnabledToolItem = new ToolItem(this, SWT.CHECK);
		editingEnabledToolItem.setToolTipText("Enable shape editing");
		editingEnabledToolItem.setImage(Images.getOrLoad("/icons/shape_square_edit.png"));
		editingEnabledToolItem.setSelection(canvasWidget.getCanvas().getSettings().isEditingEnabled());
		
		addShape = new ToolItem(this, SWT.RADIO);
		addShape.setToolTipText("Add a shape");
		addShape.setImage(Images.getOrLoad("/icons/add.png"));
		modeMap.put(addShape, CanvasMode.ADD_SHAPE);
		
		removeShape = new ToolItem(this, SWT.PUSH);
		removeShape.setToolTipText("Remove a shape");
		removeShape.setImage(Images.getOrLoad("/icons/delete.png"));
		
		addPoint = new ToolItem(this, SWT.RADIO);
		addPoint.setToolTipText("Add point to selected shape");
		addPoint.setImage(Images.getOrLoad("/icons/vector_add.png"));
		modeMap.put(addPoint, CanvasMode.ADD_POINT);
		
		removePoint = new ToolItem(this, SWT.RADIO);
		removePoint.setToolTipText("Remove point from selected shape");
		removePoint.setImage(Images.getOrLoad("/icons/vector_delete.png"));
		modeMap.put(removePoint, CanvasMode.REMOVE_POINT);
						
		splitShapeWithHorizontalLine = new ToolItem(this, SWT.RADIO);
		splitShapeWithHorizontalLine.setToolTipText("Splits a shape with a horizontal line");
//		splitShapeVertical.setImage(Images.getOrLoad("/icons/scissor_v.png"));
		splitShapeWithHorizontalLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeWithHorizontalLine.setText("H");
		modeMap.put(splitShapeWithHorizontalLine, CanvasMode.SPLIT_SHAPE_BY_HORIZONTAL_LINE);
		
		splitShapeWithVerticalLine = new ToolItem(this, SWT.RADIO);
		splitShapeWithVerticalLine.setToolTipText("Splits a shape with a vertical line");
//		splitShapeHorizontal.setImage(Images.getOrLoad("/icons/scissor_h.png"));
		splitShapeWithVerticalLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeWithVerticalLine.setText("V");
		modeMap.put(splitShapeWithVerticalLine, CanvasMode.SPLIT_SHAPE_BY_VERTICAL_LINE);		
		
		if (ENABLE_SPLIT_SHAPE_LINE) {
		splitShapeLine = new ToolItem(this, SWT.RADIO);
		splitShapeLine.setToolTipText("Splits a shape with a custom polyline");
//		splitShapeLine.setImage(Images.getOrLoad("/icons/scissor_l.png"));
		splitShapeLine.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeLine.setText("L");
		modeMap.put(splitShapeLine, CanvasMode.SPLIT_SHAPE_LINE);
		}
		
		mergeShapes = new ToolItem(this, SWT.PUSH);
		mergeShapes.setToolTipText("Merges the selected shapes");
		mergeShapes.setImage(Images.getOrLoad("/icons/merge.png"));
		
//		simplifyShape = new ToolItem(this, SWT.PUSH | SWT.RIGHT);
//		simplifyShape.setToolTipText("Simplify the selected polygon using the Ramer-Douglas-Peucker algorithm");
//		simplifyShape.setImage(Images.getOrLoad("/icons/vector.png"));
		
		if (false) {
		simplifyEpsItem = new DropDownToolItem(this, false, true, SWT.RADIO);
		for (int i=5; i<=100; i+=5)
			simplifyEpsItem.addItem(""+i, Images.getOrLoad("/icons/vector.png"), "");
		simplifyEpsItem.selectItem(14, false);
		simplifyEpsItem.ti.setToolTipText(
				"Simplify the selected shape using the Ramer-Douglas-Peucker algorithm\n"
				+ "The value determines the strength of polygon simplification - the higher the value, the more points are removed. "
				+ "\n (i.e. Epsilon is set as the given percentage of the diameter of the bounding box of the shape)");
		}
		
//		simplifyEpsItem.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if (e.detail == SWT.ARROW) {
//				} else{
//					logger.debug("selected item from dropdown: "+simplifyEpsItem.getSelectedItem());
//				}
//			}
//		});
		
//		DropDownToolItem test = new DropDownToolItem(this);
//		for (int i=5; i<=100; i+=5)
//			test.addItem(""+i, null);
			
		
//		new ToolItem(this, SWT.SEPARATOR);
		undo = new ToolItem(this, SWT.PUSH);
		undo.setToolTipText("Undo last edit step");
		undo.setImage(Images.ARROW_UNDO);
		
		
//		ToolItem item = new ToolItem(this, SWT.DROP_DOWN);
//	    item.setText("One");
//
//	    DropdownSelectionListener listenerOne = new DropdownSelectionListener(item);
//	    listenerOne.add("Option One for One");
//	    listenerOne.add("Option Two for One");
//	    listenerOne.add("Option Three for One");
//	    item.addSelectionListener(listenerOne);
		
		// TRP TOOLBAR STUFF:
		
		modeMap.remove(addShape);
		addShape.dispose();
		
		imageVersionItem = new DropDownToolItem(this, true, false, SWT.RADIO, 0);
		
		String versText = "Image file type displayed\n\torig: original image\n\tview: compressed viewing file\n\tbin: binarized image";
		imageVersionItem.addItem("orig", null, versText, false);
		imageVersionItem.addItem("view", null, versText, true);
		imageVersionItem.addItem("bin", null, versText, false);
		imageVersionItem.selectItem(1, false);
		
		imgEnhanceItem = new ToolItem(this, SWT.PUSH, 0);
		imgEnhanceItem.setImage(Images.CONTRAST);
		
		int i = indexOf(editingEnabledToolItem);
		logger.debug("index = "+i);
		
//		if (false) {
//		addPrintspace = new ToolItem(this, SWT.RADIO, ++i);
//		addPrintspace.setText("PS");
//		addPrintspace.setToolTipText("Add a printspace");
//		addPrintspace.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		modeMap.put(addPrintspace, TrpCanvasAddMode.ADD_PRINTSPACE);
//		addToRadioGroup(addPrintspace);
//		}
//		
//		if (false) {
//		addTextRegion = new ToolItem(this, SWT.RADIO, ++i);
//		addTextRegion.setText("TR");
//		addTextRegion.setToolTipText("Add a text region");
//		addTextRegion.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		modeMap.put(addTextRegion, TrpCanvasAddMode.ADD_TEXTREGION);
//		addToRadioGroup(addTextRegion);
//		
//		addLine = new ToolItem(this, SWT.RADIO, ++i);
//		addLine.setText("L");
//		addLine.setToolTipText("Add a line");
//		addLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		modeMap.put(addLine, TrpCanvasAddMode.ADD_LINE);
//		addToRadioGroup(addLine);
//		
//		addBaseLine = new ToolItem(this, SWT.RADIO, ++i);
//		addBaseLine.setText("BL");
//		addBaseLine.setToolTipText("Add a baseline");
//		addBaseLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		modeMap.put(addBaseLine, TrpCanvasAddMode.ADD_BASELINE);
//		addToRadioGroup(addBaseLine);
//
//		addWord = new ToolItem(this, SWT.RADIO, ++i);
//		addWord.setText("W");
//		addWord.setToolTipText("Add a word");
//		addWord.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		modeMap.put(addWord, TrpCanvasAddMode.ADD_WORD);
//		addToRadioGroup(addWord);
//		}
				
		if (true) {
			addSpecialRegion = new DropDownToolItem(this, true, true, SWT.PUSH, ++i);
			
			MenuItem mi = null;
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.TEXT_REGION, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TEXT_REGION));
			modeMap.put(mi, TrpCanvasAddMode.ADD_TEXTREGION);
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.LINE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.LINE));
			modeMap.put(mi, TrpCanvasAddMode.ADD_LINE);
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.BASELINE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.BASELINE));
			modeMap.put(mi, TrpCanvasAddMode.ADD_BASELINE);
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.WORD, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.WORD));
			modeMap.put(mi, TrpCanvasAddMode.ADD_WORD);		
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.TABLE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.TABLE));
			modeMap.put(mi, TrpCanvasAddMode.ADD_TABLEREGION);
			
			mi = addSpecialRegion.addItem(RegionTypeUtil.PRINTSPACE, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(RegionTypeUtil.PRINTSPACE));
			modeMap.put(mi, TrpCanvasAddMode.ADD_PRINTSPACE);
			
			for (String name : RegionTypeUtil.SPECIAL_REGIONS) {
//				mode.data = c;
				mi = addSpecialRegion.addItem(name, Images.getOrLoad("/icons/shape_square_add.png"), "", false, RegionTypeUtil.getRegionClass(name));
				modeMap.put(mi, TrpCanvasAddMode.ADD_OTHERREGION);	
			}
			
//			CanvasMode mode = TrpCanvasAddMode.ADD_OTHERREGION;
//			modeMap.put(addSpecialRegion.ti, TrpCanvasAddMode.ADD_OTHERREGION);
			
//			for (RegionType rt : specialRegions) {
//				addSpecialRegion.addItem(rt.getClass().getSimpleName(), Images.getOrLoad("/icons/shape_square_add.png"), "");	
//			}
		}
		
		
		optionsItem = new DropDownToolItem(this, false, true, SWT.CHECK, ++i);
		optionsItem.ti.setImage(Images.getOrLoad("/icons/wrench.png"));
		rectangleModeItem = optionsItem.addItem("Rectangle mode - add all shapes as rectangles initially", Images.getOrLoad("/icons/wrench.png"), "");
		autoCreateParentItem = optionsItem.addItem("Create missing parent shapes (regions or lines) automatically", Images.getOrLoad("/icons/wrench.png"), "");
		addLineToOverlappingRegionItem = optionsItem.addItem("Add lines to overlapping parent regions (else: use the selected region as parent)", Images.getOrLoad("/icons/wrench.png"), "");
		addBaselineToOverlappingLineItem = optionsItem.addItem("Add baselines to overlapping parent lines (else: use the selected line as parent)", Images.getOrLoad("/icons/wrench.png"), "");
		addWordsToOverlappingLineItem = optionsItem.addItem("Add words to overlapping parent lines (else: use the selected line as parent)", Images.getOrLoad("/icons/wrench.png"), "");
		selectNewlyCreatedShapeItem = optionsItem.addItem("Select a new shape after it was created", Images.getOrLoad("/icons/wrench.png"), "");
		lockZoomOnFocusItem = optionsItem.addItem("Lock zoom on focus", Images.getOrLoad("/icons/wrench.png"), "");
		deleteLineIfBaselineDeletedItem = optionsItem.addItem("Delete line if baseline is deleted", Images.getOrLoad("/icons/wrench.png"), "");

		if (false) {
		tableItem = new DropDownToolItem(this, false, true, SWT.PUSH, ++i);
		tableItem.ti.setImage(Images.getOrLoad("/icons/table_edit.png"));
		deleteRowItem = tableItem.addItem("Delete row of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		deleteColumnItem = tableItem.addItem("Delete column of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		splitMergedCell = tableItem.addItem("Split up formerly merged cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		removeIntermediatePtsItem = tableItem.addItem("Remove intermediate points of cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		}
		
//		new ToolItem(this, SWT.SEPARATOR, ++i);
		
//		shapeAddRectMode = new ToolItem(this, SWT.CHECK, ++i);
//		shapeAddRectMode.setImage(Images.getOrLoad("/icons/shape_square.png"));
//		shapeAddRectMode.setToolTipText("Toggles rectangle mode: if enabled, all shapes (except baselines) are added as rectangles instead of polygons - additional points can be added later however!");
		
//		autoCreateParent = new ToolItem(this, SWT.CHECK, ++i);
//		autoCreateParent.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
//		autoCreateParent.setToolTipText("If enabled, missing parent shapes (i.e. text regions or lines) are automatically created if they are not found!");
		
//		new ToolItem(this, SWT.SEPARATOR, ++i);
		
		
//		linkShapes = new ToolItem(this, SWT.PUSH, ++i);
//		linkShapes.setImage(Images.getOrLoad("/icons/link.png"));
//		linkShapes.setToolTipText("Links two shapes, e.g. a footnote and a link to it");
		
//		linkBreakShapes = new ToolItem(this, SWT.PUSH, ++i);
//		linkBreakShapes.setImage(Images.getOrLoad("/icons/link_break.png"));
//		linkBreakShapes.setToolTipText("Removes an existing link");
		
//		addItems = new ArrayList<>();
//		addItems.add(addPrintspace);
//		addItems.add(addTextRegion);
//		addItems.add(addLine);
//		addItems.add(addBaseLine);
//		addItems.add(addWord);

//		addItems.add(shapeAddRectMode);

		i = indexOf(mergeShapes);
		i += 2;
		
//		new ToolItem(this, SWT.SEPARATOR, i);
		
		if (false) {
		renderBlackeningsToggle = new ToolItem(this, SWT.CHECK, ++i);
		renderBlackeningsToggle.setToolTipText("If toggled, blackening regions are rendered with opaque background");
		//renderBlackeningsToggle.setText("Render blackenings");
		renderBlackeningsToggle.setImage(Images.getOrLoad("/icons/rabbit-silhouette.png"));
				
		new ToolItem(this, SWT.SEPARATOR, ++i);
		}
		
		i = indexOf(undo);
		
		viewSettingsMenuItem = new ToolItem(this, SWT.PUSH, ++i);
		viewSettingsMenuItem.setToolTipText("Change &viewing settings...");
		viewSettingsMenuItem.setImage(Images.getOrLoad("/icons/palette.png"));
		

		this.pack();
		
		addListeners();
		updateButtonVisibility();
	}
	
	protected void addToRadioGroup(Item item) {
		addItemSelectionListener(item, radioGroupSelectionAdapter);
//		item.addSelectionListener(radioGroupSelectionAdapter);
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
		radioGroupSelectionAdapter = new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("modeMap size: "+modeMap.keySet().size());
				for (Item i : modeMap.keySet()) {
					logger.debug("toolitem, mode: "+modeMap.get(i));
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
		
		DataBinder.get().bindBoolBeanValueToToolItemSelection("editingEnabled", canvasWidget.getCanvas().getSettings(), editingEnabledToolItem);
		
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

	public void addSelectionListener(SelectionListener listener) {
		SWTUtil.addSelectionListener(selectionMode, listener);
		SWTUtil.addSelectionListener(zoomSelection, listener);
		SWTUtil.addSelectionListener(zoomIn, listener);
		SWTUtil.addSelectionListener(zoomOut, listener);
		SWTUtil.addSelectionListener(loupe, listener);
//		SWTUtil.addToolItemSelectionListener(rotateLeft, listener);
//		SWTUtil.addToolItemSelectionListener(rotateRight, listener);
		SWTUtil.addSelectionListener(fitItem.ti, listener);
		SWTUtil.addSelectionListener(rotateItem.ti, listener);
//		SWTUtil.addToolItemSelectionListener(translateItem.ti, listener);
		
		SWTUtil.addSelectionListener(focus, listener);
		SWTUtil.addSelectionListener(addPoint, listener);
		SWTUtil.addSelectionListener(removePoint, listener);
		SWTUtil.addSelectionListener(addShape, listener);
		SWTUtil.addSelectionListener(removeShape, listener);
		SWTUtil.addSelectionListener(simplifyEpsItem, listener);
		SWTUtil.addSelectionListener(undo, listener);
		SWTUtil.addSelectionListener(splitShapeLine, listener);
		SWTUtil.addSelectionListener(splitShapeWithVerticalLine, listener);
		SWTUtil.addSelectionListener(splitShapeWithHorizontalLine, listener);
		SWTUtil.addSelectionListener(mergeShapes, listener);
	
		SWTUtil.addSelectionListener(imageVersionItem.ti, listener);
		
//		SWTUtil.addSelectionListener(addPrintspace, listener);
//		SWTUtil.addSelectionListener(addTextRegion, listener);
//		SWTUtil.addSelectionListener(addLine, listener);
//		SWTUtil.addSelectionListener(addBaseLine, listener);
//		SWTUtil.addSelectionListener(addWord, listener);
		
		SWTUtil.addSelectionListener(addSpecialRegion.ti, listener);

		SWTUtil.addSelectionListener(viewSettingsMenuItem, listener);
		
		SWTUtil.addSelectionListener(imgEnhanceItem, listener);
		
		// table stuff
		SWTUtil.addSelectionListener(deleteRowItem, listener);
		SWTUtil.addSelectionListener(deleteColumnItem, listener);
		SWTUtil.addSelectionListener(splitMergedCell, listener);
		SWTUtil.addSelectionListener(removeIntermediatePtsItem, listener);
	}

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
	
	public DropDownToolItem getTranslateItem() {
		return translateItem;
	}
	
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

	public ToolItem getAddShape() {
		return addShape;
	}

	public ToolItem getRemoveShape() {
		return removeShape;
	}

	public ToolItem getMergeShapes() {
		return mergeShapes;
	}

//	public ToolItem getSplitShape() {
//		return splitShapeLine;
//	}
	
//	public ToolItem getSimplifyShape() {
//		return simplifyShape;
//	}
	
	public ToolItem getEditingEnabledToolItem() { 
		return editingEnabledToolItem;
	}
	
	public HashMap<Item, CanvasMode> getModeMap() {
		return modeMap;
	}

	public ToolItem getUndo() { return undo; }
	
	public ToolItem getViewSettingsMenuItem() {
		return viewSettingsMenuItem;
	}	
	
	public DropDownToolItem getSimplifyEpsItem() { return simplifyEpsItem; }
	
//	public DropDownToolItem getSplitTypeItem() { return splitTypeItem; }
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
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
		SWTUtil.setEnabled(addShape, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(removeShape, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeWithVerticalLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeWithHorizontalLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(mergeShapes, isEditingEnabled && notNullAndEditable && canvasWidget.getCanvas().getScene().getNSelected() >= 2);
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

	public void addBindings(TrpSettings trpSets) {
		DataBinder.get().bindBoolBeanValueToToolItemSelection(TrpSettings.RENDER_BLACKENINGS_PROPERTY, trpSets, renderBlackeningsToggle);
	}

	public String getSelectedSpecialRegionType() {
		MenuItem si = addSpecialRegion.getSelected();
		return (si != null) ? si.getText() : "";
	}

	public DropDownToolItem getImageVersionItem() {
		return imageVersionItem;
	}

	public ToolItem getImgEnhanceItem() { 
		return imgEnhanceItem;
	}

	public MenuItem getRectangleModeItem() {
		return rectangleModeItem;
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

	public DropDownToolItem getAddSpecialRegion() {
		return addSpecialRegion;
	}

	public MenuItem getDeleteRowItem() {
		return deleteRowItem;
	}

	public MenuItem getDeleteColumnItem() {
		return deleteColumnItem;
	}

	public MenuItem getSplitMergedCell() {
		return splitMergedCell;
	}

	public MenuItem getRemoveIntermediatePtsItem() {
		return removeIntermediatePtsItem;
	}

}
