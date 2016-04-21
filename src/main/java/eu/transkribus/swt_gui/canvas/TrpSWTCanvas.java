package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_canvas.canvas.CanvasSettings;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.editing.CanvasShapeEditor;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.CanvasTransform;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.transcription.LineEditor;
import eu.transkribus.swt_gui.transcription.WordTagEditor;
import eu.transkribus.swt_gui.util.GuiUtil;
import eu.transkribus.util.MathUtil;

/**
 * Overrides the default canvas implementation to add TRP specific functionality
 */
public class TrpSWTCanvas extends SWTCanvas {
	private final static Logger logger = LoggerFactory.getLogger(TrpSWTCanvas.class);
	
	public static final double FOCUS_ANGLE_THRESHOLD = Math.PI / 16.0d;
	public static final boolean DO_FIX_WRONG_BASELINES_DIRECTIONS = false;
	
	StyledText readingOrderText;
	
	TrpMainWidget mainWidget;
	LineEditor lineEditor;
	
	public TrpSWTCanvas(Composite parent, int style, TrpMainWidget mainWidget) {
		super(parent, style);
		
		this.mainWidget = mainWidget;
//		dialog = new Shell(SWTUtil.dummyShell, SWT.DIALOG_TRIM);
		lineEditor = new LineEditor(this, SWT.BORDER);
		
		// set custom undo stack:
//		setUndoStack(new TrpUndoStack(this));
		initTrpCanvasListener();
	}
	
	@Override protected void initSettings() {
		settings = new CanvasSettings();
		TrpConfig.registerBean(settings, true);
	}
	
	private void initTrpCanvasListener() {
		this.addKeyListener(new TrpCanvasKeyListener(this));
	}
	
	@Override protected void initShapeEditor() {
		shapeEditor = new TrpCanvasShapeEditor(this);
	}
	
	@Override protected void initCanvasScene() {
		scene = new TrpCanvasScene(this);
	}
	
	@Override public TrpCanvasScene getScene() { return (TrpCanvasScene) scene; }
//	@Override public TrpUndoStack getUndoStack() { return (TrpUndoStack) undoStack; }
	
	public void updateShapeColors() {
		for (ICanvasShape s : scene.getShapes()) {
			s.setColor(TrpSettings.determineColor(mainWidget.getTrpSets(), s.getData()));
		}
		
		redraw();
	}
	
	public TrpMainWidget getMainWidget() { return mainWidget; }
	public void setMainWidget(TrpMainWidget mainWidget) { 
		this.mainWidget = mainWidget;
		
	}
	
	@Override
	public void onSelectionChanged(ICanvasShape selected) {
		logger.trace("onSelectionChanged");
	}
	
	private void drawBlackening(GC gc, ICanvasShape s) {
		CanvasSettings sets = getSettings();
		
		
		
		ITrpShapeType trpShape = (ITrpShapeType) s.getData();
		if (trpShape == null) // should not happen...
			return;
		
		boolean isBlackening = RegionTypeUtil.isBlackening(trpShape);
		if (isBlackening) {
			int [] pointArray = s.getPointArray();
				
			gc.setBackground(s.getColor());
			gc.setAlpha(255);
			gc.fillPolygon(pointArray);
		}
	}
	
	private void drawReadingOrderForShape(GC gc, final ICanvasShape s) {
				
		CanvasSettings sets = getSettings();
		//draw reading order section
		final ITrpShapeType trpShape = (ITrpShapeType) s.getData();
		if (trpShape == null) // should not happen...
			return;
		
		CanvasTransform tmpTransform = new CanvasTransform(getDisplay());
		gc.getTransform(tmpTransform);
		
		//Graphics2D g2D = (Graphics2D) gc;
		
		boolean isRegion = trpShape instanceof TrpTextRegionType;
		boolean isLine = trpShape instanceof TrpTextLineType;
		boolean isWord = trpShape instanceof TrpWordType;
		
		TrpSettings trpSets = mainWidget.getTrpSets();
		
		boolean showRo = (isRegion && trpSets.isShowReadingOrderRegions()) || (isLine && trpSets.isShowReadingOrderLines())
				|| (isWord && trpSets.isShowReadingOrderWords());
		
		boolean isSel = s.isSelected();
		
		if (showRo) {
			
			s.updateReadingOrderShapeWidth(sets.getReadingOrderCircleWidth());

			gc.setAlpha(CanvasSettings.DEFAULT.getForegroundAlpha());
			
			int arcWidth;
			if (s.getReadingOrderCircle() == null){
				boolean hasBaseline = false;
				if (trpShape.getChildren(false).size() > 0 && trpShape.getChildren(false).get(0) instanceof TrpBaselineType){
					hasBaseline = true;
				}
				s.createReadingOrderShape(this, isRegion, isLine, isWord, hasBaseline);
			}
			
			arcWidth = (int) s.getReadingOrderCircle().getWidth();
			
			gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
			gc.setLineStyle(CanvasSettings.DEFAULT.getLineStyle());	
			gc.setFont(sets.getFontTahoma16());
			
			String roString2Show = "";
			
			if (trpShape.getReadingOrder() != null){
				
				int readingOrder = trpShape.getReadingOrder();
				
				//add one to start ro from 1 to n
				int readingOrder2Show = readingOrder+1;	

				int xOffset = 0;
				int yOffset = 0;
								
				if (isRegion){
					gc.setFont(sets.getFontTahoma50());
					xOffset = 10;
					yOffset = 0;
					if (readingOrder2Show>=0 && readingOrder2Show<10)
					{
						xOffset = 20;
					}

				}
				else if (isLine){
					gc.setFont(sets.getFontTahoma30());
					yOffset = 5;
					if (readingOrder2Show>=0 && readingOrder2Show<10)
					{
						xOffset = 15;
					}
				}
				else if (isWord){
					gc.setFont(sets.getFontTahoma22());
					yOffset = 5;
					xOffset = 5;
					if (readingOrder2Show>=0 && readingOrder2Show<10)
					{
						xOffset = 15;
					}
				}
				
				roString2Show = Integer.toString(readingOrder2Show);

				if (true){										
					
					if (arcWidth < gc.getFontMetrics().getHeight()){
						arcWidth = gc.getFontMetrics().getHeight();
					}
					
					if (isSel){
						gc.setLineWidth(CanvasSettings.DEFAULT.getSelectedLineWidth());
						//arcWidth = (int) (1.5*arcWidth);
						gc.setAlpha(255);
						gc.setBackground(CanvasSettings.DEFAULT.getReadingOrderBackgroundColor());
						gc.setForeground(CanvasSettings.DEFAULT.getDrawColor());
					}
					else{
						gc.setLineWidth(CanvasSettings.DEFAULT.getDrawLineWidth());
						gc.setAlpha(155);
						gc.setBackground(s.getColor());
					}
					
					//gc.setBackground(CanvasSettings.DEFAULT.getReadingOrderBackgroundColor());
					

					gc.fillArc((int) s.getReadingOrderCircle().getX(), (int) s.getReadingOrderCircle().getY(), arcWidth, arcWidth, 0, 360);
					//gc.setForeground(CanvasSettings.DEFAULT.getDrawColor());
					//gc.drawArc(xLocation, yLocation, textSize.x+5, textSize.x+5, 0, 360);
					gc.drawArc((int) s.getReadingOrderCircle().getX(), (int) s.getReadingOrderCircle().getY(), arcWidth, arcWidth, 0, 360);

	//				else if (readingOrder>=10 && readingOrder<1000 )
	//				{
	//					gc.setFont(sets.getFontTahoma22());
	//					xOffset = 0;
	//					yOffset = 0;
	//					yLineOffset = 22/2;
	//				}
	
					//gc.drawRectangle(newRec);
					//gc.drawLine(rec.x-xLineOffset, rec.y+yLineOffset, rec.x, rec.y+yLineOffset);
					//gc.drawString(Integer.toString(trpShape.getReadingOrder()), getX(), getY());
					gc.setAlpha(255);
					
					gc.drawString(roString2Show, (int) s.getReadingOrderCircle().getX()+xOffset, (int) s.getReadingOrderCircle().getY()+yOffset, true);
									
				}

			}

		}
		
	}
	
	@Override protected void onBeforePaintScene(final GC gc) {
		
		// set reading order visibility:
		boolean isShowR = mainWidget.getTrpSets().isShowReadingOrderRegions();
		boolean isShowL = mainWidget.getTrpSets().isShowReadingOrderLines();
		boolean isShowW = mainWidget.getTrpSets().isShowReadingOrderWords();
		for (ICanvasShape s : getScene().getShapes()) {
			if (s.hasDataType(TrpTextRegionType.class)) {
				s.showReadingOrder(isShowR);
			}
			if (s.hasDataType(TrpTextLineType.class)) {
				s.showReadingOrder(isShowL);
			}
			if (s.hasDataType(TrpWordType.class)) {
				s.showReadingOrder(isShowW);
			}
		}
		
		
		
	}
	
	@Override protected void onAfterPaintScene(final GC gc) {
		boolean renderBlackenings = mainWidget.getTrpSets().isRenderBlackenings();
		
		ICanvasShape selected = null;
		
		for (ICanvasShape s : getScene().getShapes()) {
			if(!s.isSelected()){
				drawReadingOrderForShape(gc, s);
			}
			else{
				selected = s;
			}

			if (renderBlackenings)
				drawBlackening(gc, s);
		}
		
		//to draw selected upon all other shapes
		if (selected != null){
			drawReadingOrderForShape(gc, selected);
		}
	}
	
	@Override
	public void focusShape(ICanvasShape sel, boolean force) {
		if (sel==null || (!force && !sel.isVisible())) {
			return;
		}
		java.awt.Rectangle awtR = sel.getBounds();
		int offsetX = scene.getBounds().width / 15;
		int offsetY = scene.getBounds().height / 15;
		
		// set some offset depending on focused shape:
		if (sel.getData() instanceof RegionType) {
			logger.debug("focus on region");
			offsetX = 10;
			offsetY = 10;
		} else if (sel.getData() instanceof TrpTextLineType || sel.getData() instanceof TrpBaselineType) {
			logger.debug("focus on line/baseline");
//			offsetX = 40;
			offsetY = scene.getBounds().height / 15;
		} else if (sel.getData() instanceof TrpWordType) {
			logger.debug("focus on word");
			if (sel.getParent()!=null) { // focus on parent (= line) if its there (which it should be)
				awtR = sel.getParent().getBounds();
				offsetX = 10;
				offsetY = scene.getBounds().height / 15;				
			} else {
				offsetX = scene.getBounds().width / 10;
				offsetY = scene.getBounds().height / 15;
			}
		}
		
		// correct angle:	
		float angle = computeAngleOfLine(sel); // compute correction angle
		logger.debug("focus angle is: "+angle+" threshold = "+FOCUS_ANGLE_THRESHOLD);
		if (Math.abs(angle) < FOCUS_ANGLE_THRESHOLD) { // if angle is below a threshold, do no correct!
			angle = 0.0f;
		}
			
		Rectangle br = new Rectangle(awtR.x-offsetX, awtR.y-offsetY, awtR.width+2*offsetX, awtR.height+2*offsetY);	
		focusBounds(br, true, settings.isDoTransition(), -(float)MathUtil.radToDeg(angle), settings.isLockZoomOnFocus());
	}
	
	/**
	 * Computes the angle (in radiants) of the selected line shape and a horizontal line.
	 * In more detail: If a text line or baseline is selected, 
	 * the angle (in radiants) between the line drawn between the first and last point 
	 * of the corresponding baseline and the horizontal line is returned.
	 * If some other element is selected or no baseline is there, 0 is returned.  
	 */
	private float computeAngleOfLine(ICanvasShape sel) {
		if (sel==null)
			return 0.0f;
		
		ICanvasShape baseline = null;
		float angle = 0.0f;
		if (sel.getData() instanceof TrpBaselineType) {
			baseline = sel;
		} else if (sel.getData() instanceof TrpTextLineType) {
			TrpTextLineType tl = (TrpTextLineType)sel.getData();
			baseline = GuiUtil.getCanvasShape((TrpBaselineType)tl.getBaseline());
		}
		
		if (baseline != null && baseline.getPoints().size() >= 2) {
			java.awt.Point p1 = baseline.getPoint(0);
			java.awt.Point p2 = baseline.getPoint(baseline.getPoints().size()-1);
			angle = (float) Math.atan2(-p1.y+p2.y, -p1.x+p2.x);
			
			// FIX: ensure that it is always the smallest angle to the x-axis that is returned:
			// this is only necessary when some baselines are defined incorrectly from right to left!
			if (DO_FIX_WRONG_BASELINES_DIRECTIONS) {
				logger.debug("angle before fix: "+angle);
				if (angle > Math.PI/2)
					angle -= (float)Math.PI;
				else if (angle < -Math.PI/2)
					angle += (float)Math.PI;
				logger.debug("angle after fix: "+angle);
			}
		}
//		if (angle < 0) // FIXME: what about Baseline where points are defined in "wrong" direction!?
//			angle *= -1;
		
//		if (sel != null && sel.getData() instanceof TrpBaselineType) {
//			TrpBaselineType bl = (TrpBaselineType)sel.getData();
//			java.awt.Point p1 = sel.getPoint(0);
//			java.awt.Point p2 = sel.getPoint(sel.getNPoints()-1);
//			angle = (float) Math.atan2(-p1.y+p2.y, -p1.x+p2.x);
//		}
//		else if (sel != null && sel.getData() instanceof TrpTextLineType) {
//			TrpTextLineType tl = (TrpTextLineType)sel.getData();
//			if (tl.getBaseline() != null) {
////				ICanvasShape blShape = getScene().findShapeWithData(tl.getBaseline());
//				ICanvasShape blShape = TrpUtil.getCanvasShape((TrpBaselineType)tl.getBaseline());
//				
//				if (blShape != null && blShape.getPoints().size() >= 2) {
//					java.awt.Point p1 = blShape.getPoint(0);
//					java.awt.Point p2 = blShape.getPoint(blShape.getPoints().size()-1);
//					angle = (float) Math.atan2(-p1.y+p2.y, -p1.x+p2.x);
//				}
//			}
//		}
		return angle;
	}
	
	public ITrpShapeType getFirstSelectedSt() {
		ICanvasShape s = getFirstSelected();
		if (s != null) {
			return (ITrpShapeType) s.getData();
		}
		return null;
	}
	
	
	
	public void updateEditors() {
		lineEditor.updateEditor();
	}
	
	@Override
	public void onTransformChanged(CanvasTransform transform) {
		super.onTransformChanged(transform);
		
		lineEditor.updatePosition();
	}
	
	public LineEditor getLineEditor() { return lineEditor; }

}
