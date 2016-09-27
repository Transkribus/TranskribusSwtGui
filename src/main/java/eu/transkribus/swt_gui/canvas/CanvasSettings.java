package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import eu.transkribus.swt.util.MouseButtons;
import eu.transkribus.util.APropertyChangeSupport;

public class CanvasSettings extends APropertyChangeSupport  {	
//		public final static MouseButtons DEFAULT_EDIT_MOUSE_BUTTON = MouseButtons.BUTTON_LEFT;
//		private final static int DEFAULT_STYLE_BITS = SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.NO_BACKGROUND;
	
		public final static CanvasSettings DEFAULT = new CanvasSettings();
		
		/** Mode of operation, e.g. selection mode, drawing a rectangle etc. **/
		private CanvasMode mode = CanvasMode.SELECTION;
		public final static String MODE_PROPERTY = "mode";
		
		private float translationFactor = 10.0f;
		
		private float scalingFactor = 0.1f;
		
		private float rotationFactor = 2.0f;
		
		private MouseButtons translateMouseButton = MouseButtons.BUTTON_RIGHT;
		
		private MouseButtons selectMouseButton = MouseButtons.BUTTON_LEFT;
		
		private MouseButtons editMouseButton = MouseButtons.BUTTON_LEFT;
		
//		public MouseButtons editMouseButton = DEFAULT_EDIT_MOUSE_BUTTON;
		
		private Color drawColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		public static final String DRAW_COLOR_PROPERTY = "drawColor";
		
		private Color newDrawColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		public static final String NEW_DRAW_COLOR_PROPERTY = "newDrawColor";
		
		private int newDrawLineWidth = 3;
		public static final String NEW_DRAW_LINE_WIDTH_PROPERTY = "newDrawLineWidth";
		
		private Color readingOrderBackgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
		
		private Color boundingBoxColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN);
		
		private Color fillColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
		
		private Color selectedColor = Display.getDefault().getSystemColor(SWT.COLOR_RED);
		
		private Color selectedPointColor = Display.getDefault().getSystemColor(SWT.COLOR_CYAN);
		
		private Color mouseOverPointColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN);
		
		private LocalResourceManager resManager = new LocalResourceManager(JFaceResources.getResources());
		private Font fontTahoma16 = resManager.createFont(FontDescriptor.createFrom(new FontData("Tahoma", (int) (16), SWT.ITALIC)));
		private Font fontTahoma22 = resManager.createFont(FontDescriptor.createFrom(new FontData("Tahoma", (int) (22), SWT.BOLD)));
		private Font fontTahoma30 = resManager.createFont(FontDescriptor.createFrom(new FontData("Tahoma", (int) (30), SWT.BOLD)));
		private Font fontTahoma50 = resManager.createFont(FontDescriptor.createFrom(new FontData("Tahoma", (int) (50), SWT.BOLD)));
		private Font fontArial10 = resManager.createFont(FontDescriptor.createFrom(new FontData("Arial", 10, SWT.NONE)));
		
		private int drawLineWidth = 1;
		public static final String DRAW_LINE_WIDTH_PROPERTY="drawLineWidth";
		
		private int selectedLineWidth = 1;
		public static final String SELECTED_LINE_WIDTH_PROPERTY="selectedLineWidth";
		
		private int selectedPointRadius = 4;
		public static final String SELECTED_POINT_RADIUS_PROPERTY="selectedPointRadius";
		
		private boolean drawSelectedCornerNumbers = false;
		public static final String DRAW_SELECTED_CORNER_NUMBERS_PROPERTY="drawSelectedCornerNumbers";
		
		private boolean drawPolylineArcs = true;
		public static final String DRAW_POLYLINE_ARCS_PROPERTY="drawPolylineArcs";
		
		private int backgroundAlpha = 50;
		public final static String BACKGROUND_ALPHA_PROPERTY = "backgroundAlpha";
		
		private int foregroundAlpha = 255;
		public final static String FOREGROUND_ALPHA_PROPERY = "foregroundAlpha";
		
		private int readingOrderCircleWidth = 100;
		public final static String READING_ORDER_PROPERTY = "readingOrderCircleWidth";
		
		private int lineStyle = SWT.LINE_SOLID;
		
		private int newLineStyle = SWT.LINE_DASH;
		
		private int boundingBoxLineStyle = SWT.LINE_DASHDOT;
		
		private boolean scaleAroundCenter= true;
		
		private boolean rotateAroundCenter= true;
		
		private boolean translateWoScalingAndRotation = true;
		
//		private boolean multiselect= false;
//		public static final String MULTISELECT_PROPERTY="multiselect";
		
		private boolean editingEnabled= true;
		public static final String EDITING_ENABLED_PROPERTY="editingEnabled";

		/** Determines whether the first or last selected element is the one with the focus in a multiselect scenario */
		private boolean focusFirstSelected = true;
		
		/** Determines whether a smooth transition is performed when focusing a shape */
		private boolean doTransition = false;
		public static final String DO_TRANSITION_PROPERTY = "doTransition";

		private boolean lockZoomOnFocus=false;
		public static final String LOCK_ZOOM_ON_FOCUS_PROPERTY = "lockZoomOnFocus";
		
		static final List<String> DO_NOT_SAVE_THOSE_PROPERTIES = new ArrayList<String>() {{
		    	add(MODE_PROPERTY);
		}};

//		private void setChangedAndNotifyObservers() {
//			this.setChanged();
//			this.notifyObservers(this);
//		}
		
		public CanvasSettings() {
			super();			
		}
		
//		public CanvasSettings(Properties properties) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//			super(properties);
//		}
		
		
		
		public boolean isFocusFirstSelected() {
			return focusFirstSelected;
		}

		public void setFocusFirstSelected(boolean focusFirstSelected) {
			this.focusFirstSelected = focusFirstSelected;
		}

		public CanvasMode getMode() {
			return mode;
		}
		public void setMode(CanvasMode mode) {
			CanvasMode old = this.mode;
			this.mode = mode;
			
			if (old != this.mode) {
				firePropertyChange( MODE_PROPERTY, old, mode );
//				setChangedAndNotifyObservers();
			}
		}
		public float getTranslationFactor() {
			return translationFactor;
		}
		public void setTranslationFactor(float translationFactor) {
			this.translationFactor = translationFactor;
		}
		public float getScalingFactor() {
			return scalingFactor;
		}
		public void setScalingFactor(float scalingFactor) {
			this.scalingFactor = scalingFactor;
		}
		public float getRotationFactor() {
			return rotationFactor;
		}
		public void setRotationFactor(float rotationFactor) {
			this.rotationFactor = rotationFactor;
		}
		public MouseButtons getTranslateMouseButton() {
			return translateMouseButton;
		}
		public void setTranslateMouseButton(MouseButtons translateMouseButton) {
			this.translateMouseButton = translateMouseButton;
		}
		public MouseButtons getSelectMouseButton() {
			return selectMouseButton;
		}
		public MouseButtons getEditMouseButton() {
			return editMouseButton;
		}		
		public void setSelectMouseButton(MouseButtons selectMouseButton) {
			this.selectMouseButton = selectMouseButton;
		}
		public Color getDrawColor() {
			return drawColor;
		}
		public void setDrawColor(Color drawColor) {
			this.drawColor = drawColor;
		}
		public Color getNewDrawColor() {
			return newDrawColor;
		}
		public void setNewDrawColor(Color newDrawColor) {
			Color old = this.newDrawColor;
			this.newDrawColor = newDrawColor;
			firePropertyChange( NEW_DRAW_COLOR_PROPERTY, old, this.newDrawColor );
		}
		
		public int getNewDrawLineWidth() {
			return newDrawLineWidth;
		}
		public void setNewDrawLineWidth(int newDrawLineWidth) {
			int old = this.newDrawLineWidth;
			this.newDrawLineWidth = newDrawLineWidth;
			firePropertyChange( NEW_DRAW_LINE_WIDTH_PROPERTY, old, this.newDrawLineWidth );
		}

		public Color getFillColor() {
			return fillColor;
		}
		public void setFillColor(Color fillColor) {
			this.fillColor = fillColor;
		}
		
		public Color getSelectedColor() {
			return selectedColor;
		}
		public void setSelectedColor(Color selectedColor) {
			this.selectedColor = selectedColor;
		}
		
		public Color getSelectedPointColor() {
			return selectedPointColor;
		}
		public void setSelectedPointColor(Color selectedPointColor) {
			this.selectedPointColor = selectedPointColor;
		}
		
		public Color getMouseOverPointColor() {
			return mouseOverPointColor;
		}
		public void setMouseOverPointColor(Color mouseOverPointColor) {
			this.mouseOverPointColor = mouseOverPointColor;
		}

		public int getDrawLineWidth() {
			return drawLineWidth;
		}
		public void setDrawLineWidth(int drawLineWidth) {
			int old = this.drawLineWidth;
			this.drawLineWidth = drawLineWidth;
			firePropertyChange( DRAW_LINE_WIDTH_PROPERTY, old, this.drawLineWidth );
		}
		public int getSelectedLineWidth() {
			return selectedLineWidth;
		}
		public void setSelectedLineWidth(int selectedLineWidth) {
			int old = this.selectedLineWidth;
			this.selectedLineWidth = selectedLineWidth;
			firePropertyChange( SELECTED_LINE_WIDTH_PROPERTY, old, this.selectedLineWidth );
		}
		public int getSelectedPointRadius() {
			return selectedPointRadius;
		}
		public void setSelectedPointRadius(int selectedPointRadius) {
			int old = this.selectedPointRadius;
			this.selectedPointRadius = selectedPointRadius;
			firePropertyChange( SELECTED_POINT_RADIUS_PROPERTY, old, this.selectedPointRadius );
		}
		public boolean isDrawSelectedCornerNumbers() {
			return drawSelectedCornerNumbers;
		}
		public void setDrawSelectedCornerNumbers(boolean drawSelectedCornerNumbers) {
			this.drawSelectedCornerNumbers = drawSelectedCornerNumbers;
			firePropertyChange( DRAW_SELECTED_CORNER_NUMBERS_PROPERTY, !this.drawSelectedCornerNumbers, this.drawSelectedCornerNumbers );
		}

		public boolean isDrawPolylineArcs() {
			return drawPolylineArcs;
		}

		public void setDrawPolylineArcs(boolean drawPolylineArcs) {
			this.drawPolylineArcs = drawPolylineArcs;
			firePropertyChange( DRAW_POLYLINE_ARCS_PROPERTY, !this.drawPolylineArcs, this.drawPolylineArcs );
		}

		public int getLineStyle() {
			return lineStyle;
		}
		public void setLineStyle(int lineStyle) {
			this.lineStyle = lineStyle;
		}
		public int getNewLineStyle() {
			return newLineStyle;
		}
		public void setNewLineStyle(int newLineStyle) {
			this.newLineStyle = newLineStyle;
		}
		public boolean isScaleAroundCenter() {
			return scaleAroundCenter;
		}
		public void setScaleAroundCenter(boolean scaleAroundCenter) {
			this.scaleAroundCenter = scaleAroundCenter;
		}
		public boolean isRotateAroundCenter() {
			return rotateAroundCenter;
		}
		public void setRotateAroundCenter(boolean rotateAroundCenter) {
			this.rotateAroundCenter = rotateAroundCenter;
		}
		public boolean isTranslateWoScalingAndRotation() {
			return translateWoScalingAndRotation;
		}
		public void setTranslateWoScalingAndRotation(boolean translateWoScalingAndRotation) {
			this.translateWoScalingAndRotation = translateWoScalingAndRotation;
		}
		
//		public boolean isMultiselect() {
//			return multiselect;
//		}
//		public void setMultiselect(boolean multiselect) {
//			this.multiselect = multiselect;
//			firePropertyChange( MULTISELECT_PROPERTY, !this.multiselect, this.multiselect );
//		}

		public Color getBoundingBoxColor() {
			return boundingBoxColor;
		}

		public void setBoundingBoxColor(Color boundingBoxColor) {
			this.boundingBoxColor = boundingBoxColor;
		}

		public int getBoundingBoxLineStyle() {
			return boundingBoxLineStyle;
		}

		public void setBoundingBoxLineStyle(int boundingBoxLineStyle) {
			this.boundingBoxLineStyle = boundingBoxLineStyle;
		}
		
		public void setEditingEnabled(boolean value) {
			this.editingEnabled = value;
			firePropertyChange( EDITING_ENABLED_PROPERTY, !editingEnabled, editingEnabled );
		}
		
		public boolean isEditingEnabled() { return editingEnabled; }

		public int getBackgroundAlpha() {
			return backgroundAlpha;
		}

		public void setBackgroundAlpha(int backgroundAlpha) {
			int old = this.backgroundAlpha;
			this.backgroundAlpha = backgroundAlpha;
			firePropertyChange( DO_TRANSITION_PROPERTY, old, this.backgroundAlpha );
		}

		public int getForegroundAlpha() {
			return foregroundAlpha;
		}

		public void setForegroundAlpha(int foregroundAlpha) {
			int old = this.foregroundAlpha;
			this.foregroundAlpha = foregroundAlpha;
			firePropertyChange( DO_TRANSITION_PROPERTY, old, this.foregroundAlpha );
		}

		public boolean isDoTransition() {
			return doTransition;
		}

		public void setDoTransition(boolean doTransition) {
			this.doTransition = doTransition;
			firePropertyChange( DO_TRANSITION_PROPERTY, !doTransition, doTransition );
		}

		public Font getFontTahoma16() {
			return fontTahoma16;
		}

		public Font getFontTahoma50() {
			return fontTahoma50;
		}
		
		public Font getFontTahoma30() {
			return fontTahoma30;
		}

		public Font getFontTahoma22() {
			return fontTahoma22;
		}

		public Color getReadingOrderBackgroundColor() {
			return readingOrderBackgroundColor;
		}

		public int getReadingOrderCircleWidth() {
			return readingOrderCircleWidth;
		}

		public void setReadingOrderCircleWidth(int readingOrderCircleWidth) {
			int old = this.readingOrderCircleWidth;
			this.readingOrderCircleWidth = readingOrderCircleWidth;
			firePropertyChange( READING_ORDER_PROPERTY, old, this.readingOrderCircleWidth );
		}

		public boolean isLockZoomOnFocus() {
			return lockZoomOnFocus;
		}

		public void setLockZoomOnFocus(boolean lockZoomOnFocus) {
			this.lockZoomOnFocus = lockZoomOnFocus;
			firePropertyChange( LOCK_ZOOM_ON_FOCUS_PROPERTY, !this.lockZoomOnFocus, this.lockZoomOnFocus );
		}

		@Override public List<String> getPropertiesToNotSave() {
			return DO_NOT_SAVE_THOSE_PROPERTIES;
		}

		@Override public String toString() {
			return "CanvasSettings [mode=" + mode + ", translationFactor=" + translationFactor + ", scalingFactor=" + scalingFactor + ", rotationFactor="
					+ rotationFactor + ", translateMouseButton=" + translateMouseButton + ", selectMouseButton=" + selectMouseButton + ", editMouseButton="
					+ editMouseButton + ", drawColor=" + drawColor + ", newDrawColor=" + newDrawColor + ", newDrawLineWidth=" + newDrawLineWidth
					+ ", readingOrderBackgroundColor=" + readingOrderBackgroundColor + ", boundingBoxColor=" + boundingBoxColor + ", fillColor=" + fillColor
					+ ", selectedColor=" + selectedColor + ", selectedPointColor=" + selectedPointColor + ", mouseOverPointColor=" + mouseOverPointColor
					+ ", resManager=" + resManager + ", fontTahoma16=" + fontTahoma16 + ", fontTahoma22=" + fontTahoma22 + ", fontTahoma30=" + fontTahoma30
					+ ", fontTahoma50=" + fontTahoma50 + ", fontArial10=" + fontArial10 + ", drawLineWidth=" + drawLineWidth + ", selectedLineWidth="
					+ selectedLineWidth + ", selectedPointRadius=" + selectedPointRadius + ", drawSelectedCornerNumbers=" + drawSelectedCornerNumbers
					+ ", drawPolylineArcs=" + drawPolylineArcs + ", backgroundAlpha=" + backgroundAlpha + ", foregroundAlpha=" + foregroundAlpha
					+ ", readingOrderCircleWidth=" + readingOrderCircleWidth + ", lineStyle=" + lineStyle + ", newLineStyle=" + newLineStyle
					+ ", boundingBoxLineStyle=" + boundingBoxLineStyle + ", scaleAroundCenter=" + scaleAroundCenter + ", rotateAroundCenter="
					+ rotateAroundCenter + ", translateWoScalingAndRotation=" + translateWoScalingAndRotation + ", editingEnabled=" + editingEnabled
					+ ", focusFirstSelected=" + focusFirstSelected + ", doTransition=" + doTransition + ", lockZoomOnFocus=" + lockZoomOnFocus + "]";
		}
		
		

	}