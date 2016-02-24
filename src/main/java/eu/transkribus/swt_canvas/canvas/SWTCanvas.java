package eu.transkribus.swt_canvas.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.editing.UndoStack;
import eu.transkribus.swt_canvas.canvas.listener.CanvasGlobalEventsFilter;
import eu.transkribus.swt_canvas.canvas.listener.CanvasKeyListener;
import eu.transkribus.swt_canvas.canvas.listener.CanvasMouseListener;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasShapeType;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.canvas.shapes.RectDirection;
import eu.transkribus.swt_canvas.util.CanvasTransform;
import eu.transkribus.swt_canvas.util.CanvasTransformTransition;
import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.GeomUtils;
import eu.transkribus.swt_canvas.util.Resources;
import eu.transkribus.swt_canvas.util.SWTUtil;

public class SWTCanvas extends Canvas {
	private final static Logger logger = LoggerFactory.getLogger(SWTCanvas.class);
	
	// --------------------- PUBLIC STATIC FINAL CONSTANTS: ---------------------
	public final static int STYLE_BITS = 
			SWT.BORDER 
//			| SWT.NO_BACKGROUND 
			| SWT.DOUBLE_BUFFERED
			;	
	// --------------------- PROTECTED MEMBERS: ---------------------
	protected CanvasSettings settings=new CanvasSettings();
	/** A Scene object containing all objects to be drawn, including the main image **/
	protected CanvasScene scene;
	/** This is the transformation matrix representing the affine transformation that is applied before each painting! **/
	
	protected CanvasTransform transformCopy = new CanvasTransform(getDisplay());
	protected CanvasTransform transform = new CanvasTransform(getDisplay());
	protected final CanvasTransform IDENTITY_TRANSFORM = new CanvasTransform(getDisplay());
	/** The ShapeDrawer is responsible for drawing a new shape according to interactively added points **/
	protected CanvasShapeEditor shapeEditor;
	protected UndoStack undoStack;
//	protected CanvasSettingsPropertyChangeListener settingsPropertyChangeListener;
	
	// --------------------- PRIVATE MEMBERS: -----------------------
	// listener:
	protected CanvasMouseListener mouseListener;
	protected CanvasKeyListener keyListener;
	protected CanvasGlobalEventsFilter globalEventsListener;
	
//	private boolean scrollBarsVisible = false;
		
	public SWTCanvas(final Composite parent, int style) {
		super(parent, style | STYLE_BITS);
//		setLayout(new GridLayout(1, false));
		setLayout(null); // absolute layout
				
//		parent.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLUE));
		
		init();
	}

//	public DeaSWTCanvas(final Composite parent, int style) {
//		super( parent, style | SWT.BORDER | SWT.V_SCROLL| SWT.H_SCROLL | SWT.NO_BACKGROUND);
//		init();
//	}
	
	protected void init() {
		initSettings();
		
		initCanvasScene();
		initShapeEditor();
		initUndoStack();
		
		initListener();
		setFocus();
	}
	
	protected void initSettings() {
		settings=new CanvasSettings();
	}
	
	protected void initCanvasScene() {
		scene = new CanvasScene(this);
	}
	
	protected void initShapeEditor() {
		shapeEditor = new CanvasShapeEditor(this);
	}
	
	protected void initUndoStack() {
		undoStack = new UndoStack(this);
	}
	
	protected void initKeyListener() {
		keyListener = new CanvasKeyListener(this);
		
	}
		
	protected void initListener() {
//		removeKeyListener(); // remove key listener from canvas to
		// prevent duplicate signals from
		// global event listener!
				
		addControlListener(new ControlAdapter() { /* resize listener. */
			@Override
			public void controlResized(ControlEvent event) {
				redraw();
			}
		});
		
		addPaintListener(new PaintListener() { /* paint listener. */
			@Override
			public void paintControl(final PaintEvent event) {
				paint(event);
			}
		});
			
		mouseListener = new CanvasMouseListener(this);
		addMouseListener();

		initKeyListener();
//		this.addKeyListener(keyListener);
		
		// mouse events and key events are filtered by this listener and redirected to the listener:
		globalEventsListener = new CanvasGlobalEventsFilter(this, keyListener, mouseListener);
				
		// observer settings change:
		settings.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(CanvasSettings.EDITING_ENABLED_PROPERTY)) {
					onEditEnabledStatusChanged();
				}
				redraw();
			}
		});
		
//		settingsPropertyChangeListener = new CanvasSettingsPropertyChangeListener(this);
//		
//		settings.addPropertyChangeListener(settingsPropertyChangeListener);
	}
	
	public void addMouseListener() {
		addMouseWheelListener(mouseListener);
		addMouseListener(mouseListener);
		addMouseMoveListener(mouseListener);
		addMouseTrackListener(mouseListener);
	}
	
	public void removeMouseListener() {
		removeMouseWheelListener(mouseListener);
		removeMouseListener(mouseListener);
		removeMouseMoveListener(mouseListener);
		removeMouseTrackListener(mouseListener);
	}
	
//	protected void replacePropertyChangeListener(CanvasSettingsPropertyChangeListener l) {
//		settings.removePropertyChangeListener(settingsPropertyChangeListener);
//		settingsPropertyChangeListener = l;
//		settings.addPropertyChangeListener(settingsPropertyChangeListener);
//	}
	
	protected void onEditEnabledStatusChanged() {
//		logger.debug("onEditEnabledStatusChanged");
		getScene().setAllEditable(getSettings().isEditingEnabled());
		if (!settings.isEditingEnabled()) {
			scene.clearMultiSelection();
		}
		redraw();
	}
			
	public void resetTransformation() {
//		startTransformTransition(new MyTransform(getDisplay()));
		
		transform.identity();
		onTransformChanged(transform);
		onScaleChanged(transform.getScaleX(), transform.getScaleY());
		redraw();
	}
	
//	public Point getTransformedSceneCenter() {
//		Point center = scene.getCenter();
//		return affineTransform.transform(new Point2D.Double(), ptDst)
//		
//	}

	public void zoomIn() {
		zoomIn(0, 0);
	}
	
	public void zoomIn(int cx, int cy) {
		scale(1+settings.getScalingFactor(), 1+settings.getScalingFactor(), cx, cy);
	}

	public void zoomOut() {
		zoomOut(0, 0);
	}
	
	public void zoomOut(int cx, int cy) {
		scale(1-settings.getScalingFactor(), 1-settings.getScalingFactor(), cx, cy);
	}
	
	public void rotateLeft() {
		rotate(-settings.getRotationFactor());
	}
	public void rotateRight() {
		rotate(settings.getRotationFactor());
	}
	public void rotate90Left() {
		rotate(-90);
	}
	public void rotate90Right() {
		rotate(90);
	}
//	public void scaleCenter(float dx, float dy, float sx, float sy) { 
//		transform.scaleCenter(dx, dy, sx, sy);
//		redraw();
//	}
	
	/**
	 * Scaling of the scene with different parameters
	 * @param sx Scale factor in x-direction
	 * @param sy Scale factor in y-direction
	 * @param aroundCenter Scale around the center of the main image?
	 */
	public void scale(float sx, float sy, float cx, float cy) {
		if (settings.isScaleAroundCenter()) {
//			Point center = transform.inverseTransform(getClientAreaCenter());
			transform.scaleCenter(cx, cy, sx, sy);
			logger.trace("scaling ="+transform.getScaleX() + " x "+transform.getScaleY());
		}
		else {
			transform.scale(sx, sy);
		}
		onTransformChanged(transform);
		onScaleChanged(transform.getScaleX(), transform.getScaleY());
		redraw();
	}
	
	/**
	 * Rotation of the scene with different parameters
	 * @param angle Angle of rotation in degrees
	 * @param aroundCenter Rotate around the center of the main image?
	 */
	public void rotate(float angle) {
		if (settings.isRotateAroundCenter()) {
			Point center = scene.getCenter();
			logger.debug("center = "+center);
			transform.rotateCenter(center.x, center.y, angle);
			
			logger.debug("rotation (rad/deg) = "+transform.getAngleRad()+"/"+transform.getAngleDeg());
		}
		else {
			transform.rotate(angle);
		}
		onTransformChanged(transform);
		redraw();
	}
	
	public void fitToPage() {
		transform.identity();
		zoomToBounds(scene.getBounds(), false, false, 0.0f);
		// after zoomToBounds, image will be centered, but we want it at the top-left corner:
//		transform.setTranslation(0, 0);
		redraw();
	}
	
	public void fitWidth() {
//		transform.identity();
		zoomToBounds(new Rectangle(0, 0, scene.getBounds().width, 1), false, false, 0.0f);
		// after zoomToBounds, image will be centered, but we want it at the top-left corner:
//		transform.setTranslation(0, 0);
		redraw();
	}
	
	public void fitHeight() {
//		transform.identity();
		zoomToBounds(new Rectangle(0, 0, 1, scene.getBounds().height), false, false, 0.0f);
		// after zoomToBounds, image will be centered, but we want it at the top-left corner:
//		transform.setTranslation(0, 0);
		redraw();
	}
	
	public void zoomToBounds(Rectangle bounds) {
		zoomToBounds(bounds, true, settings.isDoTransition(), 0.0f);
	}
	
	/**
	 * Returns scaling info for a scaling of rectangles rectToScale to scaleToRect. 
	 * The returned pair contains first a boolean flag
	 * indicating if the scaleToRect shall be scaled according to its width (or height if false), and the corresponding scale factor as second argument.
	 */
	public static Pair<Boolean, Float> getScaleToInfo(Rectangle scaleToRect, Rectangle rectToScale) {
//		Rectangle clientRect = getClientArea();
		
		float relWDiff = (float)scaleToRect.width / (float)rectToScale.width;
		float relHDiff = (float)scaleToRect.height / (float)rectToScale.height;
		logger.debug("relWDiff = "+relWDiff+" relHDiff = "+relHDiff);
		
		// decide if to scale to width or height of the bounds:
		boolean scaleToWidth;
		if (( relWDiff < 1 && relHDiff < 1) || (relWDiff > 1 && relHDiff > 1) ) {
			scaleToWidth = relWDiff < relHDiff;
		}
		else if (relWDiff < 1) {
			scaleToWidth = true;
		}
		else if (relHDiff < 1) {
			scaleToWidth = false;
		}
		else {
			scaleToWidth =  relWDiff < relHDiff;
		}
		
		// determine scaling factor and scale:
		float sf = (scaleToWidth) ? relWDiff : relHDiff;
		
		return Pair.of(scaleToWidth, sf);
	}
	
	public void zoomToBounds(Rectangle bounds, boolean doCentering, boolean doTransition, float angle) {
		if (bounds.width == 0.0f || bounds.height == 0.0f) {
			return;
		}		
		
//		transformCopy.copyElements(transform);
		transformCopy.identity();
		Rectangle clientRect = getClientArea();
		
		Pair<Boolean, Float> scaleInfo = getScaleToInfo(clientRect, bounds);
		float sf = scaleInfo.getRight();
		boolean scaleToWidth = scaleInfo.getLeft();

//		float relWDiff = (float)clientRect.width / (float)bounds.width;
//		float relHDiff = (float)clientRect.height / (float)bounds.height;
//		logger.debug("relWDiff = "+relWDiff+" relHDiff = "+relHDiff);
		
//		// decide if to scale to width or height of the bounds:
//		boolean scaleToWidth;
//		if (( relWDiff < 1 && relHDiff < 1) || (relWDiff > 1 && relHDiff > 1) ) {
//			scaleToWidth = relWDiff < relHDiff;
//		}
//		else if (relWDiff < 1) {
//			scaleToWidth = true;
//		}
//		else if (relHDiff < 1) {
//			scaleToWidth = false;
//		}
//		else {
//			scaleToWidth =  relWDiff < relHDiff;
//		}
//		
//		// determine scaling factor and scale:
//		float sf = (scaleToWidth) ? relWDiff : relHDiff;
				
		logger.debug("zoomToBounds, bounds = "+bounds+" clientRect = "+clientRect+" sf = "+sf+" scaleToWidth = "+scaleToWidth);
		transformCopy.scale(sf, sf);
		
		// do centering of bounds if desired:
		if (doCentering) {
			if (scaleToWidth) { 		// center image at height
				transformCopy.translate(-bounds.x, clientRect.height/sf/(2.0f)-(bounds.y+bounds.height/2.0f));
			} else { 		// center image at width
				transformCopy.translate(clientRect.width/sf/(2.0f)-(bounds.x+bounds.width/2.0f), -bounds.y);
			}
		}
		
		// correction for rotation - rotate around center of bounds s.t. bounds are still centered afterwards:
		if (angle != 0.0f) {
			transformCopy.rotateCenter(bounds.x+bounds.width/2, bounds.y+bounds.height/2, angle);
		}

		// Either perform a smooth transition to the new transformation or just set the new transformation matrix:
		if (doTransition) {
			startTransformTransition(transformCopy);
		}
		else {
			transform.copyElements(transformCopy);
		}

		onScaleChanged(transform.getScaleX(), transform.getScaleY());
		onTransformChanged(transform);
		redraw();
	}
	
	/** Starts a smooth transition from the current transformation matrix to the given one. */
	public void startTransformTransition(CanvasTransform newTransform) {
		CanvasTransformTransition ttrans = new CanvasTransformTransition(this, transform, newTransform);
		ttrans.startTransition();
	}
	
	public void translateLeft() {
		translate(-settings.getTranslationFactor(), 0);
	}
	public void translateRight() {
		translate(settings.getTranslationFactor(), 0);
	}
	public void translateUp() {
		translate(0, -settings.getTranslationFactor());
	}
	public void translateDown() {
		translate(0, settings.getTranslationFactor());
	}
	
	public void translate(float tx, float ty) {
		if (settings.isTranslateWoScalingAndRotation()) {
			// construct transformation that inverts current rotation and scaling:
			
			CanvasTransform tr = new CanvasTransform(getDisplay());
			tr.rotate(transform.getAngleDeg());
			tr.scale(transform.getScaleX(), transform.getScaleY());
			tr.invert();
			Point ptWoRot = tr.transform(new Point((int)tx, (int)ty));
			tr.dispose();
			transform.translate(ptWoRot.x, ptWoRot.y);
		}
		else {
			transform.translate(tx, ty);	
		}
		logger.trace("translate = "+transform.getTranslateX()+" x "+transform.getTranslateY());
		onTransformChanged(transform);

		redraw();
	}
	
//	public void loadImage(URL url) {
//		try {
//			scene.loadMainImage(url);
//		} catch (Exception e) {
//			logger.debug("shell = "+getShell()+", message = "+e.getMessage());
//			DialogUtil.showErrorMessageBox(getShell(), "Error opening file", e.getMessage());
//		}
//	}	
//	
//	public void loadImage(String url) {
//		try {
//			scene.loadMainImage(url);
//		} catch (Exception e) {
//			logger.debug("shell = "+getShell()+", message = "+e.getMessage());
//			DialogUtil.showErrorMessageBox(getShell(), "Error opening file", e.getMessage());
//		}
//	}
	
	/** Draws the rectangle drawn by the user for either zooming, adding a new rectangle etc. **/
	private void drawDrawnShape(GC gc) {
//		CanvasSettings settings = canvas.getSettings();
		
		List<java.awt.Point> drawnPoints = shapeEditor.getDrawnPoints();
		CanvasShapeType shapeToDraw = shapeEditor.getShapeToDraw();
		
//		logger.debug("drawDrawnShape, shapeToDraw = "+shapeToDraw);
		
		CanvasMode m = settings.getMode();
		if ( m.isAddOperation() || m.isSplitOperation() || m.equals(CanvasMode.MOVE) ) {
			gc.setLineStyle(settings.getNewLineStyle());
			gc.setForeground(settings.getNewDrawColor());
			gc.setLineWidth(settings.getNewDrawLineWidth());
			gc.setBackground(settings.getSelectedPointColor());
			
			Point mP = getMouseListener().getMousePtWoTr();
			
			// draw shape:
//			logger.debug("mousept = "+mP+" mode = "+m);
			if (m==CanvasMode.SPLIT_SHAPE_LINE && drawnPoints.size() >= 1) {
//				logger.debug("mousept = "+mP);
				if (mP!=null)
					SWTUtil.drawLineExtended(gc, drawnPoints.get(0).x, drawnPoints.get(0).y, mP.x, mP.y);
			} else if (m==CanvasMode.SPLIT_SHAPE_HORIZONTAL) {
				if (mP!=null)
					SWTUtil.drawLineExtended(gc, mP.x, -1, mP.x, 1);
			} else if (m==CanvasMode.SPLIT_SHAPE_VERTICAL) {
				if (mP!=null)
					SWTUtil.drawLineExtended(gc, -1, mP.y, 1, mP.y);
			}
			else if (drawnPoints.size() >= 1 && shapeToDraw == CanvasShapeType.POLYGON) { // draw new polygon
					CanvasPolygon poly = new CanvasPolygon(drawnPoints);
					if (mP != null)
						poly.addPoint(mP.x, mP.y);
					
					gc.drawPolygon(poly.getPointArray());
			}
			else if (drawnPoints.size() >= 1 && shapeToDraw == CanvasShapeType.POLYLINE) {
				CanvasPolyline poly = new CanvasPolyline(drawnPoints);
				if (mP != null)
					poly.addPoint(mP.x, mP.y);

				gc.drawPolyline(poly.getPointArray());
//				if (mP!=null) {
//					java.awt.Point pt1 = drawnPoints.get(0);
//					gc.drawLine(pt1.x, pt1.y, mP.x-pt1.x, mP.y-pt1.y);
//				}
			}
			else if (shapeToDraw == CanvasShapeType.RECTANGLE && drawnPoints.size()==1) {
//					if (drawnPoints.size()!=1)
//						return;
					
					java.awt.Point pt1 = drawnPoints.get(0);
					if (mP!=null)
						gc.drawRectangle(pt1.x, pt1.y, mP.x-pt1.x, mP.y-pt1.y);
			}
			// draw points:
			for (int i=0; i<drawnPoints.size(); ++i) {
				java.awt.Point pt = drawnPoints.get(i);
				if (i<drawnPoints.size()-1 && i>0)
					gc.drawOval(pt.x-settings.getSelectedPointRadius(), pt.y-settings.getSelectedPointRadius(), settings.getSelectedPointRadius()*2, settings.getSelectedPointRadius()*2);
				else if (i==0) // draw square for first point
					gc.drawRectangle(pt.x-settings.getSelectedPointRadius(), pt.y-settings.getSelectedPointRadius(), settings.getSelectedPointRadius()*2, settings.getSelectedPointRadius()*2);
				else // fill last point
					gc.fillOval(pt.x-settings.getSelectedPointRadius(), pt.y-settings.getSelectedPointRadius(), settings.getSelectedPointRadius()*2, settings.getSelectedPointRadius()*2);					
			}
		}
	}
	
	protected void onAfterPaintScene(final GC gc) {
		// can be overridden by subclasses to draw additional application specific stuff, e.g. TrpSWTCanvas!
	}
	
	/* Paint function */
	protected void paint(final PaintEvent event) {
//		logger.debug("PAINTING");
		
		GC gc = event.gc;
		restoreDefaultStyle(gc);
				
//		if (getMode()==CanvasMode.MOVE) {
//			gc.setAntialias(SWT.OFF);
//			gc.setInterpolation(SWT.NONE);
//		}
//		else {
//			gc.setAntialias(SWT.ON);
//			gc.setInterpolation(SWT.DEFAULT);	
//		}
		
		// change cursor depending on mode:
		ICanvasShape selected = getFirstSelected();
		setCursor(selected);
		
		long st = System.currentTimeMillis();
		Rectangle clientRect = getClientArea(); /* Canvas' painting area */
		
		if (scene.hasDataToPaint()) {
//			logger.debug("has data to paint");
//			CanvasTransform test = new CanvasTransform(getDisplay());
//			test.setTranslation(10,  20);
			
			gc.setClipping(clientRect);
			
			gc.setTransform(transform);
			scene.paint(gc);
			onAfterPaintScene(gc);
			
			drawMouseOverPoint(gc, selected);
			drawMouseOverBoundaryPoint(gc, selected);
			
			drawAddPointsLine(gc, selected);
			drawRemoveLines(gc, selected);
			drawDrawnShape(gc);
			drawLoupe(gc);
			
			// set identity transform:
			gc.setTransform(IDENTITY_TRANSFORM);
			// draw rectangle:
			drawInteractiveRectangle(gc);
		}
		else {
//			logger.debug("has NO data to paint");
			gc.setClipping(clientRect);
			gc.fillRectangle(clientRect);
//			updateScrollBars();
		}
		
		long time = System.currentTimeMillis()-st;
//		logger.trace("painted, time = "+time+" ms");
	}
	
	/** Draws the rectangle drawn by the user for either zooming, adding a new rectangle etc. **/
	private void drawInteractiveRectangle(GC gc) {
//		if (settings.getMode() == CanvasMode.ZOOM || settings.getMode() == CanvasMode.SELECTION) {
			Rectangle rect = mouseListener.getSelectionRectangle();
			if (rect != null) {
				logger.trace("interactive recangle: "+rect);
				gc.setLineStyle(settings.getNewLineStyle());
				gc.setForeground(settings.getNewDrawColor());
				gc.drawRectangle(rect);
			}
//		}
	}
	
	/**
	 * Highlights the polygon point of the selected shape under the cursor
	 */
	private void drawMouseOverPoint(GC gc, ICanvasShape selected) {
		int ptIndex = mouseListener.getMouseOverPoint() != -1 ? mouseListener.getMouseOverPoint() : mouseListener.getSelectedPoint();
		
		if (selected!=null && selected.isEditable() && ptIndex!=-1 && settings.getMode().isHighlightPointsRequired()
				&& !isMovingBoundingBoxPossible()) {
			selected.drawSelectedPoint(this, gc, ptIndex, true);
			
//			gc.setBackground(settings.getSelectedPointColor());
//			int radius = getSettings().getSelectedPointRadius();
//			java.awt.Point p = selected.getPoint(ptIndex);
//			if (p==null) return;
//			
//			gc.fillOval(p.x-radius, p.y-radius, radius*2, radius*2);
//			
//			// draw surrounding circle of size 4 times the given radius:
//			int rr = radius+3;
//			gc.setForeground(settings.getSelectedPointColor());
//			gc.drawOval(p.x - rr, p.y - rr, rr*2, rr*2);
////			gc.drawRectangle(p.x - rr, p.y - rr, rr*2, rr*2);
		}
	}
	
	private void drawMouseOverBoundaryPoint(GC gc, ICanvasShape selected) {
		Point pt = mouseListener.getShapeBoundaryPt();
		
		if (selected!=null && mouseListener.getMouseOverPoint() == -1 && selected.isEditable() && pt!=null && settings.getMode().isHighlightPointsRequired()
				&& !isMovingBoundingBoxPossible()) {
			gc.setBackground(settings.getSelectedPointColor());
			int radius = getSettings().getSelectedPointRadius();
			
			gc.fillOval(pt.x-radius, pt.y-radius, radius*2, radius*2);
			
			// draw surrounding circle of size 4 times the given radius:
			if (false) {
			int rr = radius+3;
			gc.setForeground(settings.getSelectedPointColor());
			gc.drawOval(pt.x - rr, pt.y - rr, rr*2, rr*2);
//			gc.drawRectangle(p.x - rr, p.y - rr, rr*2, rr*2);
			}
		}
	}
	
	/**
	 * Draws a line between the two points that will be connected if the highlighted point will be removed
	 */
	private void drawRemoveLines(GC gc, ICanvasShape selected) {
		int mop = mouseListener.getMouseOverPoint();
		if (selected==null || !selected.isEditable() || !(settings.getMode() == CanvasMode.REMOVE_POINT) ||
				!selected.isPointRemovePossible(mop)) {
			return;
		}
		// do not draw lines if polyline and first or last point selected:
		if (selected instanceof CanvasPolyline && (mop==0 || mop==selected.getNPoints()-1))
			return;
		
		logger.debug("mop = "+mop);
		List<java.awt.Point> pts = selected.getPoints();
		
		int prevPt = mop-1 < 0 ? pts.size()-1 : mop-1;
		int nextPt = (mop+1) % pts.size();
	 
		java.awt.Point p1 = pts.get(prevPt);
		java.awt.Point p2 = pts.get(nextPt);
			
		gc.setLineStyle(settings.getNewLineStyle());
		gc.setForeground(settings.getNewDrawColor());
		gc.drawLine(p1.x, p1.y, p2.x, p2.y);
			
		// OLD VERSION:
//		if (selected!=null && selected.isEditable() && settings.getMode() == CanvasMode.REMOVE_POINT 
//				&& mop>0 && mop<selected.getPoints().size()-1 // lines are only drawn when inner points are removed
//				&& selected.isPointRemovePossible(mop)) {
//			logger.debug("mop = "+mop);
//			List<java.awt.Point> pts = selected.getPoints();
//			java.awt.Point p1 = (java.awt.Point) pts.get(mop-1);
//			java.awt.Point p2 = (java.awt.Point) pts.get(mop+1);
//			
//			gc.setLineStyle(settings.getNewLineStyle());
//			gc.setForeground(settings.getNewDrawColor());
//			gc.drawLine(p1.x, p1.y, p2.x, p2.y);
//		}
	}
	
	private void drawLoupe(GC gc) {
		if (getMode() != CanvasMode.LOUPE)
			return;
		
		Point pt = mouseListener.getMousePtWoTr();
		
		if (pt == null || !scene.hasDataToPaint())
			return;
		
		gc.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
		gc.setAlpha(255);
		gc.setLineWidth(1);
		
		Rectangle clipRect = getClientArea();
//		logger.debug("clipRect = "+clipRect+ " imgRect = "+scene.getMainImage().getBounds());
		
		Image img = scene.getMainImage().img;
		
		int lw = 240, lh = 120;
		int f = 3;

		int srcX = GeomUtils.bound(pt.x - lw, 0, scene.getMainImage().width);
		int srcY = GeomUtils.bound(pt.y - lh, 0, scene.getMainImage().height);
		int srcWidth = lw;
		int srcHeight = lh;
		
		Rectangle srcRect = new Rectangle(srcX, srcY, srcWidth, srcHeight);
//		srcRect = srcRect.intersection(scene.getMainImage().getBounds());
		
		int destX = srcX - f*lw;
		int destY = srcY - f*lh;
		int destWidth = f*lw;
		int destHeight = f*lh;

		Rectangle destRect = new Rectangle(destX, destY, destWidth, destHeight);
//		destRect = destRect.intersection(scene.getMainImage().getBounds());
		
		Rectangle destRectT = transform.transform(destRect);
//		logger.debug("destRectT = " + destRectT);
		
		// change direction of loupe window if extending clipRect:
		if (destRectT.x < clipRect.x)
			destRect.x += lw+destWidth;
	
		if (destRectT.y < clipRect.y)
			destRect.y += lh+destHeight;		
		
		// draw loupe rectangles:
		gc.drawRectangle(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
		gc.drawRectangle(destRect.x, destRect.y, destRect.width, destRect.height);
		
		// draw zoomed image:
		if (scene.getMainImage().internalScalingFactor != null) { // apply internal image scaling factor
			logger.trace("applying internal image scaling factor of: "+scene.getMainImage().internalScalingFactor);
			srcRect.x *= scene.getMainImage().internalScalingFactor;
			srcRect.y *= scene.getMainImage().internalScalingFactor;
			srcRect.width *= scene.getMainImage().internalScalingFactor;
			srcRect.height *= scene.getMainImage().internalScalingFactor;
		}		
		
		Rectangle srcRect1 = srcRect.intersection(img.getBounds());
		
		gc.drawImage(img, srcRect1.x, srcRect1.y, srcRect1.width, srcRect1.height, 
				destRect.x, destRect.y, destRect.width, destRect.height);
	}
	
	/** Draws the point under the cursor and a line to each of the two (or only one!) points that the eventual new point will be connected to */
	private void drawAddPointsLine(GC gc, ICanvasShape selected) {
		if (selected!=null && selected.isEditable() && settings.getMode() == CanvasMode.ADD_POINT) {
			Point pt = mouseListener.getMousePtWoTr();
			if (pt==null)
				return;			
			
			int ii = selected.getInsertIndex(pt.x, pt.y);
			selected.getPoint(ii);
			
			java.awt.Point p1 = null;
			java.awt.Point p2 = null;			
			if (!selected.isClosedShape()) {
				if (ii==0) // insert before first pt
					p1 = selected.getPoint(ii);
				else if (ii==selected.getNPoints())
					p1 = selected.getPoint(ii-1);
				else {
					int i1 = ii-1 >= 0 ? ii-1 : selected.getNPoints()-1; // ii-1 < 0 should never happen in this case but to be sure...
					p1 = selected.getPoint(i1);
					p2 = selected.getPoint(ii);
				}
			} else {
				int i1 = ii-1 >= 0 ? ii-1 : selected.getNPoints()-1;
				p1 = selected.getPoint(i1);
				p2 = selected.getPoint(ii);
			}
			
			gc.setLineStyle(settings.getLineStyle());
			gc.setForeground(settings.getDrawColor());
			gc.setBackground(settings.getSelectedPointColor());
			int radius = getSettings().getSelectedPointRadius();
			
			if (p1!=null) {
				gc.drawLine(p1.x, p1.y, pt.x, pt.y);
				gc.fillOval(p1.x-radius, p1.y-radius, radius*2, radius*2);
			}
			if (p2!=null) {
				gc.drawLine(p2.x, p2.y, pt.x, pt.y);
				gc.fillOval(p2.x-radius, p2.y-radius, radius*2, radius*2);
			}				
		}
	}
		
	private void restoreDefaultStyle(GC gc) {
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setForeground(settings.getDrawColor());		
		gc.setInterpolation(SWT.DEFAULT);
	}
	
	public CanvasSettings getSettings() { return settings; }
//	public void setSettings(CanvasSettings settings) { this.settings = settings; }
	
	public boolean isMouseInCanvas(int x, int y) {
		logger.debug("isMouseInCanvas: x/y = "+x+"/"+y);
		return getClientArea().contains(x, y);
		
//		return mouseListener.getMousePt() != null;
	}
	
	public boolean isMouseInCanvas() {
		return mouseListener.getMousePt() != null;
	}
	
	public CanvasMouseListener getMouseListener() { return mouseListener; }
	public CanvasKeyListener getKeyListener() { return keyListener; }
	public void removeKeyListener() { this.removeKeyListener(keyListener); }
	
	/**
	 * Returns the current transformation matrix as a new instance.
	 * Warning: you *have* to call the dispose method on the transformation to free the resources!
	 */
	public CanvasTransform getTransformCopy() {
		return new CanvasTransform(transform);
	}
	

//	public MyTransform getInternalTransformCopy() {
//		transformCopy.setValues(transform.handle);
//		
//		return transformCopy;
//	}
	
//	public CanvasTransform getTransform() {
////		return new CanvasTransform(transform);
//		return getTransformCopy();
//		
////		return transform;
//	}

	/**
	 * Returns the internal instance of the current transformation matrix.
	 * You do *not* have to dispose this object after usage but pay attention that you don't overwrite the internal copy
	 * when it is not desired!
	 */
	public CanvasTransform getPersistentTransform() {
		return transform;
	}	
	
//	public void setTransform(CanvasTransform transform) {
//		this.transform = transform;
//	}
	
	public boolean isMovingBoundingBoxPossible() {
		ICanvasShape selected = getFirstSelected();
		
		RectDirection dir = mouseListener.getMouseOverDirection() != RectDirection.NONE ? mouseListener.getMouseOverDirection() : mouseListener.getSelectedDirection();
//		logger.debug("dir = "+dir);
		org.eclipse.swt.graphics.Point p = getMouseListener().getMousePtWoTr();
		return (p!=null && selected!=null && dir != RectDirection.NONE && getSettings().isEditingEnabled() && 
				CanvasKeys.isKeyDown(keyListener.getCurrentStateMask(), CanvasKeys.MOVE_SUBSHAPES_REQUIRED_KEY));
	}
		
	public void setCursor(ICanvasShape selected) {
//		logger.debug("setCursor, isMovingBoundingBoxPossible: "+isMovingBoundingBoxPossible());
		// if a shape is resized along its bounding box --> draw corresponding cursor
		if (isMovingBoundingBoxPossible()) {
			RectDirection dir = mouseListener.getMouseOverDirection() != RectDirection.NONE ? mouseListener.getMouseOverDirection() : mouseListener.getSelectedDirection();
			setCursor(getDisplay().getSystemCursor(dir.getCursorType()));
		}
		else {
			// else: set cursor depending on mode:
			setCursorForMode(settings.getMode());
		}
	}
	
	public void setCursorForMode(CanvasMode mode) {		
		if (mode.isEditOperation()) {
			setCursor(getDisplay().getSystemCursor(SWT.CURSOR_CROSS));
			if (settings.getMode() == CanvasMode.MOVE_SHAPE) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
			}
		}
		else if (mode == CanvasMode.ZOOM || mode == CanvasMode.LOUPE) {
			setCursor(Resources.CURSOR_ZOOM);
//			setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
		else if (mode == CanvasMode.MOVE) {
			setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
//			setCursor(Cursors.cursorHandDrag);
		}
		else {
//			if (settings.getMode() == CanvasMode.SELECTION) {
				setCursor(getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
//			}
		}
	}
	
	public boolean isMovingShapePossible() {
		int selectedPoint = mouseListener.getSelectedPoint();
		Point mPt = mouseListener.getMousePtWoTr();
		ICanvasShape selected = getFirstSelected();
		
		return (selectedPoint==-1 && selected != null && selected.isEditable() 
				&& mPt!=null && selected.contains(mPt)
				&& getMode() == CanvasMode.SELECTION
				);
	}

	public CanvasScene getScene() { return scene; }
	
//	public void removeSelected() {
//		getScene().removeSelected();
//	}
	
	public void onSelectionRectangleDrawn(Rectangle r, boolean isMultiselect) {
		logger.debug("selection rectangle: "+r);
		// act upon drawn rectangle depending on mode:
		if (settings.getMode() == CanvasMode.ZOOM) {
			Rectangle invR = inverseTransform(r);
			zoomToBounds(invR);
		} else if (settings.getMode() == CanvasMode.SELECTION) {
			Rectangle invR = inverseTransform(r);
			// if only one shape selected and intersection area overlaps this shape -> select points from shape
			if (scene.getNSelected() == 1 && scene.getFirstSelected().intersects(invR.x, invR.y, invR.width, invR.height)) {
				logger.debug("selecting points!");
				scene.getFirstSelected().selectPoints(invR.x, invR.y, invR.width, invR.height, true, isMultiselect);
			} else { // else: select object in intersection area
				scene.selectObjects(invR, true, isMultiselect);
			}
		}
	}
	
//	/** Zooms to the specified rectangle as good as possible **/
//	public void zoom(Rectangle r) {
//		Point caCtr = getClientAreaCenter();
//		
//		
//		
//		
//		loggerger.debug("Zooming on rectangle: "+r);
//		Point ctr = new Point(r.x+r.width/2, r.y+r.height/2);
//		
////		this.translate(-ctr.x, -ctr.y);
//		
//		// scale:
//		float wr = (float)getClientArea().width / (float) r.width;
//		float hr = (float)getClientArea().height / (float) r.height;
//		
//		loggerger.debug("wr = "+wr+", hr = "+hr);
//		
//		float sf=0.0f;
//		if (Math.abs(1.0f - wr) > Math.abs(1.0f - hr)) { // scale to height
//			
//			sf = hr;
//			loggerger.debug("scaling to height: "+sf);
//		}
//		else { // scale to width
//			sf = wr;
//			loggerger.debug("scaling to width: "+sf);
//		}
//		
//		this.scale(sf, sf);
//		
//		
//		// translate the center of the drawn rectangle to the center of the client rectangle:
//		this.translate(caCtr.x-sf*ctr.x, caCtr.y-sf*ctr.y);
////		this.translate(caCtr.x, caCtr.y-ctr.y);
//		
//		
//	}
	
	public Point getClientAreaCenter() {
		Rectangle ca = getClientArea();
		return new Point(ca.x+ca.width/2, ca.y+ca.height/2);
	}
	
	public void selectObject(Point mousePt, boolean sendSignal, boolean isMultiSelect) {
//		if (settings.getMode() != CanvasMode.SELECTION)
//			return;
		
		Point invPt = transform.inverseTransform(new Point(mousePt.x, mousePt.y));
		scene.selectObject(invPt.x, invPt.y, sendSignal, isMultiSelect);
	}
//	public List<ICanvasShape> getSelected() {
//		return scene.getSelected();
//	}
	public ICanvasShape getFirstSelected() {
		return scene.getFirstSelected();
	}

	public int getNSelected() {
		return scene.getNSelected();
	}
	
//	public ICanvasShape getLastSelected() {
//		return scene.getLastSelected();
//	}
	
	public void focusFirstSelected() {
		focusShape(scene.getFirstSelected());
	}	
	
//	public void focusLastSelected() {
//		focus(getLastSelected());
//	}
	
	public void focusShape(ICanvasShape sel) {
		focusShape(sel, false);
	}

	public void focusShape(ICanvasShape sel, boolean force) {
		if (sel==null || (!force && !sel.isVisible())) {
			return;
		}
		java.awt.Rectangle awtR = sel.getBounds();
		int offsetX = scene.getBounds().width / 15;
		int offsetY = scene.getBounds().height / 15;
				
//		Rectangle ca = this.getClientArea();
		
		zoomToBounds(new Rectangle(awtR.x-offsetX, awtR.y-offsetY, awtR.width+2*offsetX, awtR.height+2*offsetY));
	}
	
	public void setMode(CanvasMode mode) {
		settings.setMode(mode);
	}
	public CanvasMode getMode() { return settings.getMode(); }

	public CanvasShapeEditor getShapeEditor() {
		return shapeEditor;
	}
	
	public UndoStack getUndoStack() {
		return undoStack;
	}
	
	public void setUndoStack(UndoStack undoStack) { this.undoStack = undoStack; }
		
	public Point inverseTransform(int x, int y) {
		return transform.inverseTransform(new Point(x, y));
	}
	
	public Point inverseTransform(Point pt) {
		return transform.inverseTransform(pt);
	}
	
	public java.awt.Rectangle inverseTransform(java.awt.Rectangle r) {
		return transform.inverseTransform(r);
	}
	public Rectangle inverseTransform(Rectangle r) {
		return transform.inverseTransform(r);
	}	
	
	// 
	public Point transform(int x, int y) {
		return transform.transform(new Point(x, y));
	}
	
	public Point transform(Point pt) {
		return transform.transform(pt);
	}
	
	public java.awt.Rectangle transform(java.awt.Rectangle r) {
		return transform.transform(r);
	}
	public Rectangle transform(Rectangle r) {
		return transform.transform(r);
	}	
	
	public void onSelectionChanged(ICanvasShape selected) {
		
		
	}
	
	public void onTransformChanged(CanvasTransform transform) {
	}
	
	public void onScaleChanged(double scaleX, double scaleY) {
	}
	
	public Point invertTranslationTransform(int tx, int ty) {
		CanvasTransform tr = getTransformCopy();
		tr.setTranslation(0, 0);
		tr.invert();
		Point transWoTr = tr.transform(new Point(tx, ty));
		tr.dispose();
		
		return transWoTr;
	}

	
}
