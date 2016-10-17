package eu.transkribus.swt_gui.canvas.listener;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.MouseButtons;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.canvas.shapes.RectDirection;

public class CanvasMouseListener implements MouseListener, MouseMoveListener, MouseWheelListener, MouseTrackListener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasMouseListener.class);
	
	public final static int MOVE_LAG = 200; // nr of ms between mouse moves to actually move a shape
	final boolean ADD_POINT_ON_BOUNDARY_CLICK = false;
	
	HashMap<MouseButtons, Point> mouseDownMap = new HashMap<MouseButtons, Point>();
	HashMap<MouseButtons, Point> mouseMoveMap = new HashMap<MouseButtons, Point>();
	HashMap<MouseButtons, Point> mouseUpMap = new HashMap<MouseButtons, Point>();
	HashMap<MouseButtons, Rectangle> drawnRects = new HashMap<MouseButtons, Rectangle>();
	
	MouseButtons lastDownBtn=null;
	int selectedPoint = -1;
	int mouseOverPoint = -1;
	Point mousePt = null;
	Point mousePtWoTr = null;
	Point translation = null;
	Point shapeBoundaryPt = null;
	boolean hasMouseMoved=false;
	
	RectDirection mouseOverDirection=RectDirection.NONE;
	RectDirection selectedDirection=RectDirection.NONE;
	
	int[] mouseOverLine=null;
	int[] selectedLine=null;
	
	boolean wasPointSelected=false;
	boolean firstMove=false;
	ShapeEditOperation currentMoveOp = null;
	CanvasMode modeBackup=CanvasMode.SELECTION;
	int currentMoveStateMask=0; // mouse move mask also masks mouse down positions!!
	long timeDown=0;
	
	protected SWTCanvas canvas;
	protected CanvasSettings settings;

	public CanvasMouseListener(SWTCanvas canvas) {
		this.canvas = canvas;
		this.settings = canvas.getSettings();
		modeBackup=canvas.getMode();
	}
	
	public void reset() {
		logger.debug("mouse listener reset");
		mouseDownMap = new HashMap<MouseButtons, Point>();
		mouseMoveMap = new HashMap<MouseButtons, Point>();
		mouseUpMap = new HashMap<MouseButtons, Point>();
		drawnRects = new HashMap<MouseButtons, Rectangle>();		
		
		selectedPoint = -1;
		mouseOverPoint = -1;
		mousePt = null;
		mousePtWoTr = null;
		shapeBoundaryPt = null;
		hasMouseMoved=false;
		
		mouseOverDirection=RectDirection.NONE;
		selectedDirection=RectDirection.NONE;
		
		mouseOverLine = null;
		selectedLine = null;
		
		wasPointSelected=false;
		firstMove=false;
		currentMoveOp = null;
		modeBackup=CanvasMode.SELECTION;
		currentMoveStateMask=0;
		timeDown=0;
	}
	
	@Override
	public void mouseDoubleClick(MouseEvent e) {
		logger.debug("mouse double click: "+e);
		MouseButtons button = MouseButtons.fromInt(e.button);
		
		// focus selected shape on double-click 
		if (button == settings.getSelectMouseButton() && canvas.getMode() == CanvasMode.SELECTION && e.stateMask==0) {
			canvas.focusShape(canvas.getScene().getLastSelected());
		}
		// finish current shape on double-click
		else if (button == settings.getEditMouseButton() && canvas.getMode().isAddOperation()) { 
			canvas.getShapeEditor().finishCurrentShape(true);
		}
		// split shape:
		else if (button == settings.getEditMouseButton() && canvas.getMode().equals(CanvasMode.SPLIT_SHAPE_LINE)) {			
			canvas.getShapeEditor().finishSplitByLine();
		}
	}
	
	private void doMouseDownShapeEditOperations(MouseButtons button, Point moustPt, int stateMask) {
		// add point:
		if (button == settings.getEditMouseButton()
				&& isAddingPointOnBoundaryClickPossible()
//				&& ADD_POINT_ON_BOUNDARY_CLICK
//				&& CanvasKeys.isCtrlOrCommandKeyDown(stateMask)
//				&& shapeBoundaryPt != null
//				&& mouseOverPoint == -1
//				&& ( mouseOverDirection == RectDirection.NONE
//				|| !CanvasKeys.isKeyDown(stateMask, CanvasKeys.RESIZE_BOUNDING_BOX_REQUIRED_KEY) )
//				&& canvas.getMode() == CanvasMode.SELECTION
				) {
			Point shapePtWTr = canvas.transform(shapeBoundaryPt); // have to transform, since shape point is without transformation!
			canvas.getShapeEditor().addPointToShape(canvas.getFirstSelected(), shapePtWTr.x, shapePtWTr.y, true);
		}
	}
	
	public boolean isAddingPointOnBoundaryClickPossible() {
		return (ADD_POINT_ON_BOUNDARY_CLICK
				&& CanvasKeys.isCtrlOrCommandKeyDown(currentMoveStateMask)
				&& shapeBoundaryPt != null
				&& mouseOverPoint == -1
				&& ( mouseOverDirection == RectDirection.NONE
				|| !CanvasKeys.isKeyDown(currentMoveStateMask, CanvasKeys.RESIZE_BOUNDING_BOX_REQUIRED_KEY) )
				&& canvas.getMode() == CanvasMode.SELECTION
				&& mouseOverLine == null
				);
	}
	
	private void doMouseUpShapeEditOperations(MouseButtons button, Point mousePt) {
		// add point:
		if (button == settings.getEditMouseButton() && canvas.getMode() == CanvasMode.ADD_POINT) {
			canvas.getShapeEditor().addPointToShape(canvas.getFirstSelected(), mousePt.x, mousePt.y, true);
		}
		// remove point:
		else if (button == settings.getEditMouseButton() && canvas.getMode() == CanvasMode.REMOVE_POINT) {
			canvas.getShapeEditor().removePointFromSelected(canvas.getFirstSelected(), mouseOverPoint, true);
		}
		// add shape:
		else if (button == settings.getEditMouseButton() && canvas.getMode().isAddOperation()) {
			canvas.getShapeEditor().addPointForNewShape(mousePt.x, mousePt.y, false);
		}
		// split shape:
		else if (button == settings.getEditMouseButton() && canvas.getMode().isSplitOperation()) {			
			canvas.getShapeEditor().addPointForSplitting(mousePt.x, mousePt.y);
		}
		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		timeDown = System.currentTimeMillis();
		
		boolean isMultiselect = CanvasKeys.isKeyDown(e.stateMask, CanvasKeys.MULTISELECTION_REQUIRED_KEY);
		logger.debug("mouse down: "+e+" mode = "+canvas.getMode().toString()+" isMultiselect: "+isMultiselect);
		canvas.setFocus();
		
		if  (e.count == 2) // doubleclick --> handled in mouseDoubleClick method
			return;
		
		// update data:
		wasPointSelected=false;
		hasMouseMoved=false;
		firstMove = true;
		mousePt = new Point(e.x, e.y);
		mousePtWoTr = canvas.inverseTransform(mousePt.x, mousePt.y);
		MouseButtons button = MouseButtons.fromInt(e.button);
		lastDownBtn = button;
		mouseDownMap.put(button, new Point(e.x, e.y));
		mouseMoveMap.put(button, new Point(e.x, e.y));
		mouseUpMap.put(button, null);
				
		boolean isMovingShapePossible = canvas.isMovingShapePossible();
		
		// set move image mode:
		final boolean isMoveImgOnLeftBtnPossible = 
				(canvas.getMode() == CanvasMode.SELECTION || canvas.getMode() == CanvasMode.LOUPE) && 
				button == settings.getSelectMouseButton() 
				&& !isMovingShapePossible && shapeBoundaryPt == null 
				&& mouseOverPoint == -1 && e.stateMask==0 && e.stateMask != CanvasKeys.SELECTION_RECTANGLE_REQUIRED_KEYS;
		
		if (button == settings.getTranslateMouseButton() 
				/*|| e.stateMask == CanvasKeys.MOVE_SCENE_REQUIRED_KEYS*/
				|| (isMoveImgOnLeftBtnPossible)) {
			modeBackup = settings.getMode();
			canvas.setMode(CanvasMode.MOVE);
		}
		
		// do edit operations for mouse down:
		doMouseDownShapeEditOperations(button, mousePt, e.stateMask);
		
		// select object and track selected points or resize direction:
		if (canvas.getMode() == CanvasMode.SELECTION && button == settings.getSelectMouseButton()) {
			selectedPoint = -1;
			ICanvasShape selected = canvas.getFirstSelected();
			logger.debug("first selected: "+canvas.getFirstSelected());
			if (selected != null && selected.isEditable()) {
				selectedPoint = selected.getPointIndex(mousePtWoTr.x, mousePtWoTr.y, settings.getSelectedPointRadius());
				wasPointSelected = selected.isPointSelected(selectedPoint);
				if (selectedPoint != -1) {
					selected.selectPoint(selectedPoint, true, isMultiselect /*canvas.getSettings().isMultiselect()*/);
				}
				
				if (selectedPoint == -1) {
					selectedLine = selected.whichLine(mousePtWoTr.x, mousePtWoTr.y);
				}
				
				if (CanvasKeys.isKeyDown(e.stateMask, CanvasKeys.RESIZE_BOUNDING_BOX_REQUIRED_KEY)) {
					selectedDirection = selected.whichDirection(mousePtWoTr.x, mousePtWoTr.y, settings.getSelectedPointRadius());
					selectedPoint = -1;
					selectedLine = null;
				}
								
				logger.debug("selected point: "+selectedPoint + " selected direction: "+selectedDirection+" sel-line: "+selectedLine);
			}
//			if (selectedPoint == -1 && selectedDirection == RectDirection.NONE) {
//				logger.debug("selecting object on: "+e.x+"x"+e.y);
//				canvas.selectObject(new Point(e.x, e.y), true);
//			}
		}
		
		// set move shape mode:
		if (canvas.getMode() == CanvasMode.SELECTION && button == settings.getSelectMouseButton() 
				&& !canvas.isMovingBoundingBoxPossible() && canvas.isMovingShapePossible() 
//				&& e.stateMask != CanvasKeys.SELECTION_RECTANGLE_REQUIRED_KEYS
				) {
			modeBackup = settings.getMode();
			canvas.setMode(CanvasMode.MOVE_SHAPE);
		}

		canvas.redraw();
	}
		
	@Override
	public void mouseMove(MouseEvent e) {
		try {
//			Point mouseDownPt = getMouseDown(MouseButtons.BUTTON_LEFT);
//			if (mouseDownPt != null) {
//				double d = java.awt.Point.distance(mouseDownPt.x, mouseDownPt.y, e.x, e.y);
//				logger.debug("d = "+d);
//				if (d > 5)
//					hasMouseMoved=true;	
//			}
			hasMouseMoved=true;	
			
			
//			logger.debug("mouse move: "+e);
			
			// update some data:
			long tdiff = (System.currentTimeMillis() - timeDown);
			currentMoveStateMask = e.stateMask;
			
			updateTranslation(e.x, e.y);
			mousePt = new Point(e.x, e.y);
			mousePtWoTr = canvas.inverseTransform(mousePt.x, mousePt.y);
//			Point oldMovePt = getMouseMove(settings.getTranslateMouseButton());
			Point oldMovePt = getMouseMove(lastDownBtn);
			
			ICanvasShape selected = canvas.getFirstSelected();
			
			// calculate mouse over point of selected shape:
			mouseOverPoint = -1;
			mouseOverLine = null;
			if (selected != null) {
				mouseOverPoint = selected.getPointIndex(mousePtWoTr.x, mousePtWoTr.y, settings.getSelectedPointRadius());
				mouseOverDirection = selected.whichDirection(mousePtWoTr.x, mousePtWoTr.y, settings.getSelectedPointRadius());
				mouseOverLine = selected.whichLine(mousePtWoTr.x, mousePtWoTr.y);
			}
			
			// calc point on boundary of selected shape:
			shapeBoundaryPt = null;
			if (selected != null) {
				Pair<Double, java.awt.Point> dap = selected.distanceAndClosestPoint(mousePtWoTr.x, mousePtWoTr.y, false);
				if (dap.getLeft() < settings.getSelectedPointRadius()) {
					shapeBoundaryPt = new Point(dap.getRight().x, dap.getRight().y);
				}
			}
			
			// update move points and drawn rects:
			for (MouseButtons b : MouseButtons.getValidButtons()) {
				if (isMouseMove(b)) {
					mouseMoveMap.put(b, new Point(e.x, e.y));
					Point ptDown = getMouseDown(b);
					logger.trace("moving mouse, mouse down is "+ptDown);
					if (ptDown!=null) {
						Rectangle r = new Rectangle(ptDown.x, ptDown.y, e.x-ptDown.x, e.y-ptDown.y);
						r = SWTUtil.normalizeRect(r);
						drawnRects.put(b, r);
					}
				}
			}	
			
			boolean isLagThresh = tdiff > MOVE_LAG;
			
			// Perform translation of whole scene on move of translation button:
			if (canvas.getMode()==CanvasMode.MOVE/*&& isMouseMove(settings.getTranslateMouseButton())*/) {
//				logger.debug("moving!!");
//				canvas.setMode(CanvasMode.MOVE);
//				Point oldMovePt = getMouseMove(settings.getTranslateMouseButton());
				if (oldMovePt != null) {
					canvas.translate(mousePt.x-oldMovePt.x, mousePt.y-oldMovePt.y);
					hasMouseMoved=true;
				}
			}
			// move shape:
			else if (canvas.getMode()==CanvasMode.MOVE_SHAPE && isLagThresh) {
				Point trans = getTotalTranslation(mousePt, settings.getSelectMouseButton());
				if (trans != null) {
					currentMoveOp = canvas.getShapeEditor().moveShape(canvas.getFirstSelected(), trans.x, trans.y, currentMoveOp, true);
					if (firstMove)
						firstMove = false;
					hasMouseMoved=true;
				}
			}
			// move a point or the bounding box if selected:
			else if (canvas.getMode()==CanvasMode.SELECTION && isMouseMove(settings.getSelectMouseButton()) && isLagThresh) {
//				logger.debug("heeeeeeeeeeeeeeere, point: "+selectedPoint+" direction: "+selectedDirection);
				
				if (selectedPoint!=-1) 	{ // perform moving a point if point selected
					canvas.getShapeEditor().movePointAndSelected(canvas.getFirstSelected(), selectedPoint, mousePt.x, mousePt.y, firstMove);
					
					if (firstMove) firstMove = false;
					hasMouseMoved=true;
				} 
				else if (selectedLine != null) {
//					Point trans = getTotalTranslation(mousePt, settings.getSelectMouseButton());
					java.awt.Point tWTr = canvas.inverseTransformWithoutTranslation(translation.x, translation.y);
					if (tWTr != null) {
						logger.debug("moving pts of line "+selectedLine+" by "+tWTr+" translation = "+translation);
						int tx = tWTr.x;
						int ty = tWTr.y;
						canvas.getShapeEditor().translatePoints(canvas.getFirstSelected(), tx, ty, firstMove, selectedLine[0], selectedLine[1]);
						
						if (firstMove) firstMove = false;
						hasMouseMoved=true;
//						canvas.getFirstSelected().movePoints(x, y, pts);						
					}
				}
				else if (selectedDirection!=RectDirection.NONE) {
					Point trans = getTotalTranslation(mousePt, settings.getSelectMouseButton());
					if (trans != null) {
						canvas.getShapeEditor().resizeBoundingBoxFromSelected(selectedDirection, trans.x, trans.y, firstMove);
						if (firstMove) firstMove = false;
						hasMouseMoved=true;
					}
				}
			}
			
			canvas.redraw();
		}
		catch (Exception ex) {
			logger.error("Fatal error in mouse move: "+ex.getMessage(), ex);
		}
	}
	
	private void updateTranslation(int x, int y) {
		if (mousePt == null)
			translation = new Point(0, 0);
		else
			translation = new Point(x-mousePt.x, y-mousePt.y);
	}
	
	private Point getTotalTranslation(Point mousePt, MouseButtons mb) {
//		Point oldMovePt = getMouseMove(mb);
//		if (oldMovePt==null)
//			return null;
		
		Point mouseDownPt = getMouseDown(mb);
		if (mouseDownPt==null)
			return null;
		
		Point trans = new Point(mousePt.x-mouseDownPt.x, mousePt.y-mouseDownPt.y);
		return trans;
		
//		CanvasTransform tr = canvas.getTransformCopy();
////		tr.identity();
////		tr.rotate(canvas.getTransform().getAngleDeg());
//		
//		tr.setTranslation(0, 0);
////		tr.scale(1.0d/tr.getScaleX(), 1.0d/tr.scaleY);
//		tr.invert();
//		Point transWoTr = tr.transform(new Point(trans.x, trans.y));
//		tr.dispose();	
//		
////		logger.debug("trans / transwo: "+trans+"/"+transWoTr);
//		
//		return transWoTr;
	}
	
	@Override
	public void mouseUp(MouseEvent e) {
		boolean isMultiselect = CanvasKeys.isKeyDown(e.stateMask, CanvasKeys.MULTISELECTION_REQUIRED_KEY);
		logger.debug("mouse up: "+e+" hasMouseMoved: "+hasMouseMoved+" isMultiselect: "+isMultiselect);
		
		if  (e.count == 2) // doubleclick --> handled in mouseDoubleClick method
			return;

		MouseButtons button = MouseButtons.fromInt(e.button);
		
		/*
		 * equals middle button
		 */
		if (e.button == 2){
			canvas.fitToPage();
			return;
		}
		
		// deselect point if mouse has not moved:
		CanvasMode mode = canvas.getMode();
		boolean triedToMoveShapeOrImage = (mode == CanvasMode.MOVE || mode == CanvasMode.MOVE_SHAPE) && modeBackup == CanvasMode.SELECTION;
		logger.debug("H0 "+triedToMoveShapeOrImage+" button: "+button+" selectedPoint: "+selectedPoint+" mode: "+canvas.getMode());
		if (button == settings.getSelectMouseButton()
				&& !hasMouseMoved
				&& (canvas.getMode() == CanvasMode.SELECTION || triedToMoveShapeOrImage)
				) { // perform de-selection of point or selection of shape on mouse up
//			logger.debug("H1");
			ICanvasShape selected = canvas.getFirstSelected();			
			if (selected != null && selected.isEditable() && wasPointSelected) {
				selected.deselectPoint(selectedPoint, true);
				wasPointSelected = false;
			}
//			logger.debug("H2");
			// TEST:
			if (selectedPoint == -1 && selectedDirection == RectDirection.NONE && selectedLine == null) {
				logger.debug("selecting object on: "+e.x+"x"+e.y);
				canvas.selectObject(new Point(e.x, e.y), true, isMultiselect);
			}
		}
		// open up context menu
		else if (button == MouseButtons.BUTTON_RIGHT && !hasMouseMoved) {
			Point p = canvas.toDisplay(e.x, e.y);
			canvas.getContextMenu().show(canvas.getFirstSelected(), p.x, p.y);
		}
		
		hasMouseMoved = false;
		firstMove = false;
		currentMoveOp = null;
		mousePt = new Point(e.x, e.y);
		logger.trace("mouse pt = "+mousePt+ "button = "+e.button);
		mousePtWoTr = canvas.inverseTransform(mousePt.x, mousePt.y);
		Rectangle selRect = getSelectionRectangle();
		logger.debug("selection rectangle drawn: "+selRect);
		selectedPoint = -1;
		selectedDirection = RectDirection.NONE;
		mouseOverLine = null;
		selectedLine = null;
		
		doMouseUpShapeEditOperations(button, mousePt);
		
		// reset mouse-down, -move, -up and drawn-rects maps
		if (button != MouseButtons.BUTTON_UNKNOWN) {
			mouseDownMap.put(button, null);
			mouseMoveMap.put(button, null);
			mouseUpMap.put(button, new Point(e.x, e.y));
			drawnRects.put(button, null);
		}
		
		// reset cursor if translateMouseButton has gone up:
		if (canvas.getMode().isEndsWithMouseUp()) {
			canvas.setMode(modeBackup);
		}		
				
		// react upon a drawn rectangle:
		if (button == settings.getSelectMouseButton() && selRect!=null) {
			canvas.onSelectionRectangleDrawn(selRect, isMultiselect);
		}
						
		canvas.redraw();
		logger.trace("mouse pt2 = "+mousePt);
	}	
	
	@Override
	public void mouseScrolled(MouseEvent e) {
		logger.trace("wheel, e = "+e);
		logger.trace("mousePt = "+mousePt);
		if (mousePt==null) // do not scroll when mouse not inside
			return;

		logger.trace("mouselistener wheel, e1 = "+e);
		float sign = Math.signum(e.count); // > 0 --> wheel up, < 0 --> wheel down
						
		Point mpt = canvas.inverseTransform(new Point(e.x, e.y));
		if (sign > 0) {
			if ((e.stateMask & SWT.CTRL) == SWT.CTRL)
//				canvas.rotateLeft();
				;
			else
				canvas.zoomIn(mpt.x, mpt.y);
		}
		else {
			if ((e.stateMask & SWT.CTRL) == SWT.CTRL)
//				canvas.rotateRight();
				;
			else
				canvas.zoomOut(mpt.x, mpt.y);
		}
		
//		canvas.scaleCenter(1+sign*settings.scalingFactor, 1+sign*settings.scalingFactor, false);
	}
	
	@Override
	public void mouseEnter(MouseEvent e) {
		logger.trace("mouse entering canvas!");
//		canvas.setFocus();
	}

	@Override
	public void mouseExit(MouseEvent e) {
		logger.trace("mouse exiting canvas: "+e);
		// bug in windows: when user scrolls on touchpad, 
		// the exit event is thrown even though the mouse is still inside the canvas
		// jump out of the function in this case
		Rectangle b = new Rectangle(0, 0, canvas.getBounds().width, canvas.getBounds().height);
		if (b.contains(e.x, e.y)) {
			logger.trace("exit pt is inside bounds - doin nothin!");
			return;
		}
		
		mousePt = null;
		mousePtWoTr = null;
	}

	@Override
	public void mouseHover(MouseEvent e) {

	}
		
	public Point getMouseMove(MouseButtons button) {
		return mouseMoveMap.get(button);
	}
	public boolean isMouseMove(MouseButtons button) {
		return getMouseMove(button) != null;
	}
	
	public Point getMouseDown(MouseButtons button) {
		return mouseDownMap.get(button);
	}
	public boolean isMouseDown(MouseButtons button) {
		return getMouseDown(button) != null;
	}
	
	public Point getMouseUp(MouseButtons button) {
		return mouseUpMap.get(button);
	}
	public boolean isMouseUp(MouseButtons button) {
		return getMouseUp(button) != null;
	}
	
	public Rectangle getDrawnRect(MouseButtons button) {
		return drawnRects.get(button);
	}
	
	public Rectangle getSelectionRectangle() {
		if (	settings.getMode() == CanvasMode.ZOOM || settings.getMode() == CanvasMode.SELECTION 
				&& selectedPoint == -1
				&& selectedDirection == RectDirection.NONE
				&& selectedLine == null
				&& (currentMoveStateMask & CanvasKeys.SELECTION_RECTANGLE_REQUIRED_KEYS) == CanvasKeys.SELECTION_RECTANGLE_REQUIRED_KEYS
				) {
			return drawnRects.get(settings.getSelectMouseButton());
		} else
			return null;
	}
	
//	public boolean 
	
	
	public boolean hasDrawnRect(MouseButtons button) {
		return getDrawnRect(button) != null;
	}
	
	public int getSelectedPoint() { return selectedPoint; }
	public int getMouseOverPoint() { return mouseOverPoint; }
	public Point getMousePt() { return mousePt; }
	public Point getMousePtWoTr() { return mousePtWoTr; }
	public Point getShapeBoundaryPt() { return shapeBoundaryPt; }
	public int getCurrentMoveStateMask() { return currentMoveStateMask; }
	
	
	public RectDirection getMouseOverDirection() { return mouseOverDirection; }
	public RectDirection getSelectedDirection() { return selectedDirection; }
	public int[] getMouseOverLine() { return mouseOverLine; }
	public int[] getSelectedLine() { return selectedLine; }

	public boolean isKeyDown(int key) {
		return CanvasKeys.isKeyDown(getCurrentMoveStateMask(), SWT.CTRL);		
	}
	
	

}
