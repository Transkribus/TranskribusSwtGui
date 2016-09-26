package eu.transkribus.swt_gui.canvas;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.canvas.CanvasToolBar;
import eu.transkribus.swt_canvas.canvas.CanvasWidget;
import eu.transkribus.swt_canvas.canvas.SWTCanvas;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TrpCanvasWidget extends CanvasWidget {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasWidget.class);

	TrpMainWidget mainWidget;
	TrpCanvasToolBarSelectionListener trpCanvasToolBarSelectionListener;
	
	public TrpCanvasWidget(Composite parent, int style, TrpMainWidget mainWidget) {
		super(parent, style);
		this.mainWidget = mainWidget;
		((TrpSWTCanvas)canvas).setMainWidget(mainWidget);
	}
	
	@Override
	protected void init(SWTCanvas canvas, CanvasToolBar toolBar) {

		setLayout(new GridLayout(1, false));
		
		if (this.canvas != null && !this.canvas.isDisposed())
			this.canvas.dispose();
		
		if (canvas != null) {
			this.canvas = (TrpSWTCanvas) canvas;
		}
		else {
			this.canvas = new TrpSWTCanvas(SWTUtil.dummyShell, SWT.NONE, mainWidget);
		}
		this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if (this.toolBar != null && !this.toolBar.isDisposed())
			this.toolBar.dispose();
		
		if (toolBar!=null) {
			this.toolBar = (TrpCanvasToolBar) toolBar;
			this.toolBar.setParent(this);
		} else {
			this.toolBar = new TrpCanvasToolBar(this, mainWidget, SWT.FLAT | SWT.RIGHT | SWT.WRAP);
		}
		this.toolBar.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		
		this.canvas.setParent(this);
		
		addListener();
//		addCustomListener();
	}
	
	protected void addListener() {
		// selection listener for toolbar:
		trpCanvasToolBarSelectionListener = new TrpCanvasToolBarSelectionListener((TrpCanvasToolBar)toolBar, (TrpSWTCanvas) canvas);
		super.addListener();
		toolBar.addSelectionListener(trpCanvasToolBarSelectionListener);
		// selection listener on canvas:
//		canvas.getScene().addCanvasSceneListener(new CanvasSceneListener() {
//			@Override
//			public void onSelectionChanged(SceneEvent e) {
//				toolBar.updateButtonVisibility();
//			}
//		});
//		// update buttons on changes in canvas settings:
//		canvas.getSettings().addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				toolBar.updateButtonVisibility();
//				canvas.redraw();
//			}
//		});
//		// update undo button on changes in undo stack:
//		canvas.getUndoStack().addObserver(new Observer() {
//			@Override
//			public void update(Observable o, Object arg) {
//				if (arg == UndoStack.AFTER_UNDO || arg == UndoStack.AFTER_ADD_OP) {
//					logger.trace("updating undo button after undo or add op!");
//					updateUndoButton();
//				}
//			}
//		});
	}
	
	@Override public TrpSWTCanvas getCanvas() {
		return (TrpSWTCanvas) canvas;
	}

	@Override public TrpCanvasToolBar getToolBar() {
		return (TrpCanvasToolBar) toolBar;
	}
	
	

}
