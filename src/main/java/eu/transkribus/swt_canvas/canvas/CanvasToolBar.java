package eu.transkribus.swt_canvas.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.DropDownToolItem;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_canvas.util.databinding.DataBinder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

public class CanvasToolBar extends ToolBar {
	private final static Logger logger = LoggerFactory.getLogger(CanvasToolBar.class);

	// private ToolBar topToolBar;
	protected ToolItem zoomIn;
	protected ToolItem zoomOut;
	protected ToolItem zoomSelection;
	protected ToolItem loupe;
	protected ToolItem rotateRight;
	protected ToolItem rotateLeft;
	
	protected DropDownToolItem fitItem;
	protected ToolItem fitToPage;
	protected ToolItem fitWidth;
	protected ToolItem fitHeight;
	
	protected ToolItem originalSize;
	
	protected ToolItem selectionMode;
	// ToolItem toolItem;
	
	protected DropDownToolItem translateItem;
	protected ToolItem translateLeft;
	protected ToolItem translateDown;
	protected ToolItem translateUp;
	protected ToolItem translateRight;
	
	
	protected ToolItem focus;
	// EDIT ITEMS:
	protected ToolItem addPoint;	
	protected ToolItem removePoint;
	protected ToolItem addShape;
	protected ToolItem removeShape;
	protected ToolItem mergeShapes;
	protected ToolItem splitShapeLine;
	private static final boolean ENABLE_SPLIT_SHAPE_LINE=true;
	protected ToolItem splitShapeHorizontal;
	protected ToolItem splitShapeVertical;
	
//	protected DropDownToolItem splitTypeItem;
//	protected ToolItem simplifyShape;
	protected DropDownToolItem simplifyEpsItem;
	protected ToolItem undo;
	protected ToolItem editingEnabledToolItem;	
	
	protected ToolItem viewSettingsMenuItem;
	
//	protected HashMap<ToolItem, CanvasMode> modeMap = new HashMap<>();
	protected HashMap<Item, CanvasMode> modeMap = new HashMap<>();
	
	
	
	protected CanvasWidget canvasWidget;
	
	protected SelectionAdapter radioGroupSelectionAdapter;

	/**
	 * Wraps the DeaSWTCanvas widget into a widget containing a toolbar for the
	 * most common operations such as scaling, rotation, translation etc.
	 */
	public CanvasToolBar(CanvasWidget parent, int style) {
		super(parent, style);
		this.canvasWidget = parent;
//		this.canvas = canvasWidget.getCanvas();

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

		rotateLeft = new ToolItem(this, SWT.PUSH);
		rotateLeft.setToolTipText("Rotate left");
		rotateLeft.setImage(Images.getOrLoad("/icons/arrow_turn_left.png"));

		rotateRight = new ToolItem(this, SWT.PUSH);
		rotateRight.setToolTipText("Rotate right");
		rotateRight.setImage(Images.getOrLoad("/icons/arrow_turn_right.png"));
		
		translateItem = new DropDownToolItem(this, false, true, SWT.NONE);
		translateItem.addItem("Translate left", Images.getOrLoad("/icons/arrow_left.png"), "Translate left");
		translateItem.addItem("Translate right", Images.getOrLoad("/icons/arrow_right.png"), "Translate right");
		translateItem.addItem("Translate up", Images.getOrLoad("/icons/arrow_up.png"), "Translate up");
		translateItem.addItem("Translate down", Images.getOrLoad("/icons/arrow_down.png"), "Translate down");

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

		focus = new ToolItem(this, SWT.PUSH);
		focus.setToolTipText("Focus selected object");
		focus.setImage(Images.getOrLoad("/icons/focus16.png"));
		
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
		addPoint.setToolTipText("Add point to selected polygon");
		addPoint.setImage(Images.getOrLoad("/icons/vector_add.png"));
		modeMap.put(addPoint, CanvasMode.ADD_POINT);
		
		removePoint = new ToolItem(this, SWT.RADIO);
		removePoint.setToolTipText("Remove point from selected polygon");
		removePoint.setImage(Images.getOrLoad("/icons/vector_delete.png"));
		modeMap.put(removePoint, CanvasMode.REMOVE_POINT);
				
		splitShapeHorizontal = new ToolItem(this, SWT.RADIO);
		splitShapeHorizontal.setToolTipText("Splits a shape into subshapes horizontally");
//		splitShapeHorizontal.setImage(Images.getOrLoad("/icons/scissor_h.png"));
		splitShapeHorizontal.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeHorizontal.setText("H");
		modeMap.put(splitShapeHorizontal, CanvasMode.SPLIT_SHAPE_HORIZONTAL);
		
		splitShapeVertical = new ToolItem(this, SWT.RADIO);
		splitShapeVertical.setToolTipText("Splits a shape into subshapes vertically");
//		splitShapeVertical.setImage(Images.getOrLoad("/icons/scissor_v.png"));
		splitShapeVertical.setImage(Images.getOrLoad("/icons/scissor.png"));
		splitShapeVertical.setText("V");
		modeMap.put(splitShapeVertical, CanvasMode.SPLIT_SHAPE_VERTICAL);
		
		if (ENABLE_SPLIT_SHAPE_LINE) {
		splitShapeLine = new ToolItem(this, SWT.RADIO);
		splitShapeLine.setToolTipText("Splits a shape into subshapes by a user defined line");
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
		
		simplifyEpsItem = new DropDownToolItem(this, false, true, SWT.RADIO);
		for (int i=5; i<=100; i+=5)
			simplifyEpsItem.addItem(""+i, Images.getOrLoad("/icons/vector.png"), "");
		simplifyEpsItem.selectItem(14, false);
		simplifyEpsItem.ti.setToolTipText(
				"Simplify the selected polygon using the Ramer-Douglas-Peucker algorithm\n"
				+ "The value determines the strength of polygon simplification - the higher the value, the more points are removed. "
				+ "\n (i.e. Epsilon is set as the given percentage of the diameter of the bounding box of the shape)");
		
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
		
		
		new ToolItem(this, SWT.SEPARATOR);
		undo = new ToolItem(this, SWT.PUSH);
		undo.setToolTipText("Undo last edit step");
		undo.setImage(Images.getOrLoad("/icons/arrow_undo.png"));	
		
		
//		ToolItem item = new ToolItem(this, SWT.DROP_DOWN);
//	    item.setText("One");
//
//	    DropdownSelectionListener listenerOne = new DropdownSelectionListener(item);
//	    listenerOne.add("Option One for One");
//	    listenerOne.add("Option Two for One");
//	    listenerOne.add("Option Three for One");
//	    item.addSelectionListener(listenerOne);
		
		
		

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
		
//		// if not in selection mode, disable add shape action combo box:
//		Observer settingsChangedObserver = new Observer() {
//			@Override
//			public void update(Observable o, Object arg) {
//				logger.debug("setttings changed - updating edit status!");
//				addShapeActionCombo.setEnabled(canvasWidget.getCanvas().getSettings().getMode() == CanvasMode.SELECTION);
//			}
//		};
//		canvasWidget.getCanvas().getSettings().addObserver(settingsChangedObserver);
	}
	
//	private void updateModeButtonSelection() {
//		CanvasMode canvasMode = canvasWidget.getCanvas().getSettings().getMode();
//		System.out.println("canvasMode = "+canvasMode);
//		
////		if (true)
////			return;
//		
//		for (ToolItem i : modeMap.keySet()) {
//			CanvasMode mode = modeMap.get(i);
//			i.setSelection(mode == canvasMode);
//		}
//		return;
//	}
	
	public void addAddButtonsSelectionListener(SelectionListener listener) {
		SWTUtil.addToolItemSelectionListener(selectionMode, listener);
		SWTUtil.addToolItemSelectionListener(zoomSelection, listener);
		SWTUtil.addToolItemSelectionListener(zoomIn, listener);
		SWTUtil.addToolItemSelectionListener(zoomOut, listener);
		SWTUtil.addToolItemSelectionListener(loupe, listener);
		SWTUtil.addToolItemSelectionListener(rotateLeft, listener);
		SWTUtil.addToolItemSelectionListener(rotateRight, listener);
		SWTUtil.addToolItemSelectionListener(fitItem.ti, listener);
		SWTUtil.addToolItemSelectionListener(translateItem.ti, listener);
		
		SWTUtil.addToolItemSelectionListener(focus, listener);
		SWTUtil.addToolItemSelectionListener(addPoint, listener);
		SWTUtil.addToolItemSelectionListener(removePoint, listener);
		SWTUtil.addToolItemSelectionListener(addShape, listener);
		SWTUtil.addToolItemSelectionListener(removeShape, listener);
		SWTUtil.addToolItemSelectionListener(simplifyEpsItem.ti, listener);
		SWTUtil.addToolItemSelectionListener(undo, listener);
		if (splitShapeLine!=null)
			SWTUtil.addToolItemSelectionListener(splitShapeLine, listener);
		SWTUtil.addToolItemSelectionListener(splitShapeHorizontal, listener);
		SWTUtil.addToolItemSelectionListener(splitShapeVertical, listener);
		SWTUtil.addToolItemSelectionListener(mergeShapes, listener);
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

	public ToolItem getRotateRight() {
		return rotateRight;
	}

	public ToolItem getRotateLeft() {
		return rotateLeft;
	}

	public ToolItem getTranslateLeft() {
		return translateLeft;
	}

	public ToolItem getFitToPage() {
		return fitToPage;
	}
	
	public ToolItem getFitWidth() {
		return fitWidth;
	}
	
	public ToolItem getFitHeight() {
		return fitHeight;
	}

	public ToolItem getTranslateRight() {
		return translateRight;
	}

	public ToolItem getSelectionMode() {
		return selectionMode;
	}

	public ToolItem getTranslateDown() {
		return translateDown;
	}

	public ToolItem getTranslateUp() {
		return translateUp;
	}

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
		ICanvasShape selected = canvasWidget.getCanvas().getFirstSelected();
		boolean notNullAndEditable = selected!=null && selected.isEditable();
		logger.trace("shape notNullAndEditable: "+notNullAndEditable);
		
		boolean isEditingEnabled = canvasWidget.getCanvas().getSettings().isEditingEnabled();

		SWTUtil.setEnabled(addPoint, isEditingEnabled && notNullAndEditable && selected.canInsert());
		SWTUtil.setEnabled(addPoint, isEditingEnabled && notNullAndEditable && selected.canInsert());
		SWTUtil.setEnabled(removePoint, isEditingEnabled && notNullAndEditable && selected.canRemove());
		SWTUtil.setEnabled(addShape, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(removeShape, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeLine, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeHorizontal, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(splitShapeVertical, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(mergeShapes, isEditingEnabled && notNullAndEditable && canvasWidget.getCanvas().getScene().getNSelected()>=2);
		SWTUtil.setEnabled(simplifyEpsItem.ti, isEditingEnabled && notNullAndEditable);
		SWTUtil.setEnabled(undo, isEditingEnabled);
	}

	public DropDownToolItem getTranlateItem() {
		return translateItem;
	}
	
	public DropDownToolItem getFitItem() {
		return fitItem;
	}


}
