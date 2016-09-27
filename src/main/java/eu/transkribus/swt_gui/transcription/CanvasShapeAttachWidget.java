package eu.transkribus.swt_gui.transcription;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;

/**
 * A widget that attaches to the bounding box of a canvas shape or can be moved around freely. 
 */
public abstract class CanvasShapeAttachWidget<S extends ITrpShapeType> extends Composite {
	public static final int INSERT_INDEX = 1;
	
	public static final int DEFAULT_MIN_WIDTH = 400;
	public static final int DEFAULT_HEIGHT = 35;
	
	int minWidth = DEFAULT_MIN_WIDTH;
	int height = DEFAULT_HEIGHT;
	
	Label label;
//	StyledText textField;
//	Text textField;
	boolean isUp=false;
	Point location=null; // alternative location if not docked to upper or lower bound; null if docked!
	Point startPt=null;
	
	Button upDownBtn;
	SWTCanvas canvas;
	private Button moveButton;
	S shape;
	Class<S> clazz;
	
	public CanvasShapeAttachWidget(final SWTCanvas canvas, int style, Class<S> clazz) {
		this(canvas, style, clazz, DEFAULT_MIN_WIDTH, DEFAULT_HEIGHT);
	}
	
	public CanvasShapeAttachWidget(final SWTCanvas canvas, int style, Class<S> clazz, int minWidth, int height) {
		super(SWTUtil.dummyShell, style);
		this.canvas = canvas;
		this.clazz = clazz;
		setLayout(new GridLayout(4, false));
		
		this.minWidth = minWidth;
		this.height = height;
		
		label = new Label(this, SWT.NONE);
		label.setText("nada");
		
		upDownBtn = new Button(this, SWT.NONE);
		upDownBtn.setToolTipText("Dock the editor to the upper / lower bound of the bounding box");
		upDownBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setUpOrDownOfLine(!isUp);
			}
		});
		upDownBtn.setImage(Images.getOrLoad("/icons/arrow_up.png"));
		
		moveButton = new Button(this, SWT.NONE);
		moveButton.setToolTipText("Moves the editor around. Doubleclick to dock again");
		moveButton.setImage(Images.getOrLoad("/icons/arrow_out.png"));
		
		addListener();
	}
	
	protected void addWidget(Composite c) {
		c.setParent(this);
		c.moveBelow(label);
	}
	
	private void addListener() {
		this.addMouseTrackListener(new MouseTrackAdapter() {			
			@Override
			public void mouseEnter(MouseEvent e) {
				setFocus();
			}
		});
		
//		this.addMouseWheelListener(new MouseWheelListener() {
//			@Override
//			public void mouseScrolled(MouseEvent e) {
//				logger.debug("mouse scrolled!");
//				canvas.getMouseListener().mouseScrolled(e);
//			}
//		});
		
		// For moving the widget:
		moveButton.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				location = null;
				updatePosition();
			}

			@Override
			public void mouseDown(MouseEvent e) {
				startPt = CanvasShapeAttachWidget.this.toDisplay(e.x, e.y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				startPt = null;
			}
		});
		
		moveButton.addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (startPt != null) {
					Point dPt = CanvasShapeAttachWidget.this.toDisplay(e.x, e.y);
					Point trans = new Point(dPt.x - startPt.x, dPt.y - startPt.y);
					startPt = dPt;
					
//					logger.debug("display coordinates: "+dPt);
//					logger.debug("trans: "+trans);
					location = new Point(getLocation().x + trans.x, getLocation().y + trans.y);
				
					updatePosition();
				}
			}
			
		});
		
		// for resizing the widget along the right:
		Listener resizeListener = new Listener() {
			Point p = null;
			
			private boolean isRight(int x, int y) {
				Rectangle b = getBounds();
				Rectangle ra = new Rectangle(0, 0, 0, 0);
				int o=8;
				ra.x = b.width - o;
				ra.width = o;
				ra.y = 0;
				ra.height = b.height;
				
				return ra.contains(x, y);
			}

			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					if (isRight(event.x, event.y)) {
						p = new Point(event.x, event.y);
					}
					break;
				case SWT.MouseUp:
					p = null;
					break;
				case SWT.MouseMove:
					if (isRight(event.x, event.y) || p != null) {
						setCursor(getDisplay().getSystemCursor(SWT.CURSOR_SIZEE));
						if (p!=null) {
							setBounds(getBounds().x, getBounds().y, getBounds().width+(event.x-p.x), getBounds().height);
							p.x = event.x;
						}
					}
					else {
						setCursor(null);
					}
					break;
				case SWT.MouseExit:
					setCursor(null);
					p = null;
					break;
				}
			}
		};
		
		addListener (SWT.MouseDown, resizeListener);
		addListener (SWT.MouseMove, resizeListener);
		addListener (SWT.MouseUp, resizeListener);
		addListener (SWT.MouseExit, resizeListener);
	}
		
	public void setUpOrDownOfLine(boolean up) {
		this.location = null;
		if (up) {
			upDownBtn.setImage(Images.getOrLoad("/icons/arrow_down.png"));
			isUp = true;
		}
		else {
			upDownBtn.setImage(Images.getOrLoad("/icons/arrow_up.png"));
			isUp = false;
		}
		updatePosition();
	}
	
	protected abstract boolean isDisabled();
	
	public void updateEditor() {
		if (isDisabled())
			return;
		
		ICanvasShape selected = canvas.getFirstSelected();
		
		if (selected==null || !(selected.getData().getClass().isAssignableFrom(clazz))) {
			setData(false, null);
		}
		else {
			setData(showThisEditor(), (S) selected.getData());
		}
	}
	
	private boolean setData(boolean visible, S shape) {
		boolean ret;
//		logger.debug("setting visibility: "+visible+", shape: "+shape);
		if (visible) {
			ret = setParent(canvas);
			this.shape = shape;
		}
		else {
			ret = setParent(SWTUtil.dummyShell);
			this.shape = null;
			setDefaultPosition();
		}
				
		updateData();

		updatePosition();
		return ret;
	}
	
	public abstract void updateData();
	public abstract boolean showThisEditor();
		
	public void setDefaultPosition() {
		this.location = null;
		this.isUp = false;
	}
	
	public S getShape() {
		return shape;
	}
	
	public void updatePosition() {
		if (isDisabled())
			return;		
		
		ICanvasShape selected = canvas.getFirstSelected();
//		logger.debug("selected = "+selected);
//		logger.debug("updating position: "+location+" isUp: "+isUp);

		if (selected!=null) {
			CanvasPolygon bp = selected.getBoundsPolygon();
			
			CanvasPolygon bpT = canvas.getPersistentTransform().transform(bp);
			java.awt.Rectangle bTr = bpT.getBounds();
//			logger.debug("bTr = "+bTr);
			
//			java.awt.Rectangle b = selected.getBounds();
//			java.awt.Rectangle bTr = canvas.getTransform().transform(b);
			if (location != null) {
				setBounds(location.x, location.y, Math.max(minWidth, bTr.width), height);
			}
			else if (isUp) {
				setBounds(bTr.x, bTr.y-height, Math.max(minWidth, bTr.width), height);
			}
			else {
				setBounds(bTr.x, bTr.y+bTr.height, Math.max(minWidth, bTr.width), height);
			}
						
//			logger.debug("bounds = "+getBounds()+ " ca = "+canvas.getClientArea());
//			logger.debug("location = "+getLocation());
		}

		onUpdatePosition();
		
		canvas.redraw();
	}
	
	protected void onUpdatePosition() {
		
	}
	
//	public void changeTextFieldSize() {
//		GC gc = new GC(textField);
//		int te = gc.textExtent(textField.getText()).x + 20;
//		
//		gc.dispose();
//		if (textField.getSize().x < te) {
//			int diff = te - textField.getSize().x;
//			textField.setSize(te+10, textField.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//			Rectangle r = getBounds();
//			r.width += diff;
//			setBounds(r);
//		}
//	}

}
