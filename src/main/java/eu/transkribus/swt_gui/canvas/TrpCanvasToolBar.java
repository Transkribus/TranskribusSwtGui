package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.CanvasWidget;
import eu.transkribus.swt_canvas.canvas.listener.CanvasToolBarSelectionListener;
import eu.transkribus.swt_canvas.util.DropDownToolItem;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TrpCanvasToolBar extends CanvasToolBar {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasToolBar.class);
	
	protected ToolItem addTextRegion;
	protected ToolItem addLine;
	protected ToolItem addBaseLine;
	protected ToolItem addWord;
			
	protected ToolItem addPrintspace;
	protected DropDownToolItem addSpecialRegion;
	protected DropDownToolItem optionsItem;
	
	protected ToolItem imgEnhanceItem;
	
	//
	MenuItem rectangleModeItem;
	MenuItem autoCreateParentItem;
	MenuItem addLineToOverlappingRegionItem, addBaselineToOverlappingLineItem, 
				addWordsToOverlappingLineItem, lockZoomOnFocusItem, deleteLineIfBaselineDeletedItem;
	MenuItem selectNewlyCreatedShapeItem;
	
//	protected ToolItem shapeAddRectMode;
//	protected ToolItem autoCreateParent;
	
	List<ToolItem> addItems;
//	protected ToolItem linkShapes;
//	protected ToolItem linkBreakShapes;
	
	CanvasWidget canvasWidget;
	
	DropDownToolItem imageVersionItem;
	
	DropDownToolItem tableItem;
	MenuItem deleteRowItem, deleteColumnItem, splitMergedCell, removeIntermediatePtsItem; 

	private TrpMainWidget mainWidget;

	public TrpCanvasToolBar(CanvasWidget parent, TrpMainWidget mainWidget, int style) {
		super(parent, style);
		
		this.canvasWidget = parent;
		this.mainWidget = mainWidget;
		
		initTrpCanvasToolBar();
	}
	
	private void initTrpCanvasToolBar() {
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
		
		if (false) {
		addPrintspace = new ToolItem(this, SWT.RADIO, ++i);
		addPrintspace.setText("PS");
		addPrintspace.setToolTipText("Add a printspace");
		addPrintspace.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
		modeMap.put(addPrintspace, TrpCanvasAddMode.ADD_PRINTSPACE);
		addToRadioGroup(addPrintspace);
		}
		
		if (false) {
		addTextRegion = new ToolItem(this, SWT.RADIO, ++i);
		addTextRegion.setText("TR");
		addTextRegion.setToolTipText("Add a text region");
		addTextRegion.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
		modeMap.put(addTextRegion, TrpCanvasAddMode.ADD_TEXTREGION);
		addToRadioGroup(addTextRegion);
		
		addLine = new ToolItem(this, SWT.RADIO, ++i);
		addLine.setText("L");
		addLine.setToolTipText("Add a line");
		addLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
		modeMap.put(addLine, TrpCanvasAddMode.ADD_LINE);
		addToRadioGroup(addLine);
		
		addBaseLine = new ToolItem(this, SWT.RADIO, ++i);
		addBaseLine.setText("BL");
		addBaseLine.setToolTipText("Add a baseline");
		addBaseLine.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
		modeMap.put(addBaseLine, TrpCanvasAddMode.ADD_BASELINE);
		addToRadioGroup(addBaseLine);

		addWord = new ToolItem(this, SWT.RADIO, ++i);
		addWord.setText("W");
		addWord.setToolTipText("Add a word");
		addWord.setImage(Images.getOrLoad("/icons/shape_square_add.png"));
		modeMap.put(addWord, TrpCanvasAddMode.ADD_WORD);
		addToRadioGroup(addWord);
		}
				
		if (true) {
			addSpecialRegion = new DropDownToolItem(this, true, true, SWT.RADIO, ++i);
			
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
		
		tableItem = new DropDownToolItem(this, false, true, SWT.PUSH, ++i);
		tableItem.ti.setImage(Images.getOrLoad("/icons/table_edit.png"));
		deleteRowItem = tableItem.addItem("Delete row of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		deleteColumnItem = tableItem.addItem("Delete column of selected cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		splitMergedCell = tableItem.addItem("Split up formerly merged cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		removeIntermediatePtsItem = tableItem.addItem("Remove intermediate points of cell", Images.getOrLoad("/icons/table_edit.png"), "Table tools");
		
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
		
		addItems = new ArrayList<>();
		addItems.add(addPrintspace);
		addItems.add(addTextRegion);
		addItems.add(addLine);
		addItems.add(addBaseLine);
		addItems.add(addWord);

//		addItems.add(shapeAddRectMode);

		i = indexOf(mergeShapes);
		i += 2;
		
		new ToolItem(this, SWT.SEPARATOR, i);
		
		viewSettingsMenuItem = new ToolItem(this, SWT.PUSH, ++i);
		viewSettingsMenuItem.setToolTipText("Change &viewing settings...");
		viewSettingsMenuItem.setImage(Images.getOrLoad("/icons/palette.png"));
		
		new ToolItem(this, SWT.SEPARATOR);
		
		this.pack();
		
	}
	
	public String getSelectedSpecialRegionType() {
		MenuItem si = addSpecialRegion.getSelected();
		return (si != null) ? si.getText() : "";
	}
	
	@Override public void addSelectionListener(SelectionListener listener) {
		super.addSelectionListener((CanvasToolBarSelectionListener)listener);

		SWTUtil.addToolItemSelectionListener(imageVersionItem.ti, listener);
		
		SWTUtil.addToolItemSelectionListener(addPrintspace, listener);
		SWTUtil.addToolItemSelectionListener(addTextRegion, listener);
		SWTUtil.addToolItemSelectionListener(addLine, listener);
		SWTUtil.addToolItemSelectionListener(addBaseLine, listener);
		SWTUtil.addToolItemSelectionListener(addWord, listener);
		
		SWTUtil.addToolItemSelectionListener(addSpecialRegion.ti, listener);

		SWTUtil.addToolItemSelectionListener(viewSettingsMenuItem, listener);
		
		SWTUtil.addToolItemSelectionListener(imgEnhanceItem, listener);
		
		// table stuff
		SWTUtil.addMenuItemSelectionListener(deleteRowItem, listener);
		SWTUtil.addMenuItemSelectionListener(deleteColumnItem, listener);
		SWTUtil.addMenuItemSelectionListener(splitMergedCell, listener);
		SWTUtil.addMenuItemSelectionListener(removeIntermediatePtsItem, listener);
		
//		for (Class c : REGION_TYPES) {
//			CanvasMode m = TrpCanvasAddMode.ADD_OTHERREGION;
//			m.data = c;
//			
//			MenuItem mi = addSpecialRegion.getItemWithData(c);
//			
//			
//			mi.addSelectionListener(listener);			
//		}
	}
	
	@Override
	public void updateButtonVisibility() {
		super.updateButtonVisibility();
		
		if (canvasWidget==null)
			return;
		
		TrpSWTCanvas canvas = (TrpSWTCanvas) canvasWidget.getCanvas();
		boolean isEditingEnabled = canvas.getSettings().isEditingEnabled();
		for (ToolItem ai : addItems)
			SWTUtil.setEnabled(ai, isEditingEnabled);
		
		SWTUtil.setEnabled(optionsItem.ti, isEditingEnabled);
		
//		if (linkShapes!=null)
//			SWTUtil.setToolItemVisibility(linkShapes, canvasWidget.getCanvas().getNSelected()==2 && 
//				canvasWidget.getCanvas().getSettings().isEditingEnabled());
	}

//	public ToolItem getAddPrintspace() {
//		return addPrintspace;
//	}

//	public ToolItem getAddTextRegion() {
//		return addTextRegion;
//	}
//
//	public ToolItem getAddLine() {
//		return addLine;
//	}
//
//	public ToolItem getAddBaseLine() {
//		return addBaseLine;
//	}
//
//	public ToolItem getAddWord() {
//		return addWord;
//	}
	
//	public ToolItem getShapeAddRectMode() {
//		return shapeAddRectMode;
//	}
//	
//	public ToolItem getAutoCreateParent() {
//		return autoCreateParent;
//	}
	
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

//	public ToolItem getLinkShapes() {
//		return linkShapes;
//	}

//	public ToolItem getLinkBreakShapes() {
//		return linkBreakShapes;
//	}	
	
	
	
	

}
