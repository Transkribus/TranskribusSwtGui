package eu.transkribus.swt_gui.canvas;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.canvas.editing.UndoStack;
import eu.transkribus.swt_gui.canvas.listener.CanvasToolBarSelectionListener;
import eu.transkribus.swt_gui.canvas.listener.ICanvasSceneListener;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class CanvasWidget extends Composite {
	static Logger logger = LoggerFactory.getLogger(CanvasWidget.class);
	
	protected SWTCanvas canvas;
//	protected CanvasToolBar toolbar;
	protected CanvasToolBarNew toolbar;
	protected CanvasToolBarSelectionListener canvasToolBarSelectionListener;
	
	TrpMainWidget mainWidget;

	public ToolBar bar1, bar2; // TEST
	
	public CanvasWidget(Composite parent, TrpMainWidget mainWidget, int style) {
		this(parent, mainWidget, style, null);
	}
	
	public CanvasWidget(Composite parent, TrpMainWidget mainWidget, int style, ToolBar tb) {
		super(parent, style);
		this.mainWidget = mainWidget;
										
		GridLayout l = new GridLayout(3, false);
		l.marginTop = 0;
		l.marginBottom = 0;
		l.marginHeight = 0;
		l.marginWidth = 0;
		
		setLayout(l);
		
		int barStyle = /*SWT.FLAT |*/ SWT.RIGHT;
		
		bar1 = new ToolBar(this, SWT.VERTICAL | barStyle );
//		bar1.setData(0);
//		for (int i=0; i<3; ++i) {
//			ToolItem i1 = new ToolItem(bar1, 0);
//			i1.setImage(Images.APPLICATION);
//		}
		bar1.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));
		
		bar2 = new ToolBar(this, SWT.VERTICAL | barStyle  );
//		bar2.setData(1);
//		for (int i=0; i<100; ++i) {
//			ToolItem i1 = new ToolItem(bar2, 0);
//			i1.setImage(Images.REFRESH);
//		}
		bar2.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, true, 1, 1));
		

		this.canvas = new SWTCanvas(this, SWT.NONE, mainWidget);
		this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
				
//		this.toolbar = new CanvasToolBar(this, bar, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
//		this.toolbar = new CanvasToolBar(this, tb, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		this.toolbar = new CanvasToolBarNew(this, tb, bar1, bar2, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		tb.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
				
		addListener();
	}
	
	// TEST	
	public void toggleToolbarVisiblity(ToolBar tb, boolean show) {
		if (tb != bar1 && tb != bar2)
			return;
		
		if (!show) {
			tb.setParent(SWTUtil.dummyShell);
		} else {
			tb.setParent(this);
		}
		
		if (tb == bar1) {
			tb.moveAbove(null);
		} else {
			tb.moveAbove(canvas);
		}
		
		pack();
	}
	// END TEST
	
	protected void addListener() {
		// selection listener for toolbar:
		//canvasToolBarSelectionListener = new CanvasToolBarSelectionListener(toolBar, canvas);
		//toolBar.addAddButtonsSelectionListener(canvasToolBarSelectionListener);
		
		// selection listener on canvas:
		canvas.getScene().addCanvasSceneListener(new ICanvasSceneListener() {
			@Override
			public void onSelectionChanged(SceneEvent e) {
				toolbar.updateButtonVisibility();
			}
		});
		
		// update buttons on changes in canvas settings:
		canvas.getSettings().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				toolbar.updateButtonVisibility();
				canvas.redraw();
			}
		});
		// update undo button on changes in undo stack:
		canvas.getUndoStack().addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if (arg == UndoStack.AFTER_UNDO || arg == UndoStack.AFTER_ADD_OP) {
					logger.trace("updating undo button after undo or add op!");
					updateUndoButton();
				}
			}
		});
		
		toolbar.addSelectionListener(new CanvasToolBarSelectionListener(this));
	}

	public SWTCanvas getCanvas() {
		return canvas;
	}

	public CanvasToolBarNew getToolbar() {
		return toolbar;
	}

	public ToolItem getZoomIn() {
		return toolbar.getZoomIn();
	}

	public ToolItem getZoomOut() {
		return toolbar.getZoomOut();
	}

	public ToolItem getZoomSelection() {
		return toolbar.getZoomSelection();
	}

//	public ToolItem getRotateRight() {
//		return toolBar.getRotateRight();
//	}
//
//	public ToolItem getRotateLeft() {
//		return toolBar.getRotateLeft();
//	}
//
//	public ToolItem getTranslateLeft() {
//		return toolBar.getTranslateLeft();
//	}
//
//	public ToolItem getFitToPage() {
//		return toolBar.getFitToPage();
//	}
//
//	public ToolItem getTranslateRight() {
//		return toolBar.getTranslateRight();
//	}

	public ToolItem getSelectionMode() {
		return toolbar.getSelectionMode();
	}

//	public ToolItem getTranslateDown() {
//		return toolBar.getTranslateDown();
//	}
//
//	public ToolItem getTranslateUp() {
//		return toolBar.getTranslateUp();
//	}

	public ToolItem getOriginalSize() {
		return toolbar.getOriginalSize();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void updateUiStuff() {
		updateUndoButton();
		toolbar.updateButtonVisibility();
	}
	
	public void updateUndoButton() {
		if (canvas.getUndoStack().getSize()!=0) {
			toolbar.getUndo().setEnabled(true);
			toolbar.getUndo().setToolTipText("Undo ("+canvas.getUndoStack().getSize()+"): "+canvas.getUndoStack().getLastOperationDescription());
		}
		else {
			toolbar.getUndo().setEnabled(false);
			toolbar.getUndo().setToolTipText("Undo: Nothing do undo...");
		}
	}
	
	public ToolItem getShowRegionsToolItem() {
		return toolbar.showRegionsToolItem;
	}
	
	public ToolItem getShowLinesToolItem() {
		return toolbar.showLinesToolItem;
	}
	
	public ToolItem getShowBaselinesToolItem() {
		return toolbar.showBaselinesToolItem;
	}
	
	public ToolItem getShowWordsToolItem() {
		return toolbar.showWordsToolItem;
	}
		
	public MenuItem getShowRegionsItem() {
		return toolbar.showRegionsItem;
	}	
	
	public MenuItem getShowLinesItem() {
		return toolbar.showLinesItem;
	}
	
	public MenuItem getShowBaselinesItem() {
		return toolbar.showBaselinesItem;
	}
	
	public MenuItem getShowWordsItem() {
		return toolbar.showWordsItem;
	}
	
	public MenuItem getShowPrintspaceItem() {
		return toolbar.showPrintspaceItem;
	}
	
	public MenuItem getShowReadingOrderRegionsMenuItem() {
		return toolbar.showReadingOrderRegionsMenuItem;
	}
	
	public MenuItem getShowReadingOrderLinesMenuItem() {
		return toolbar.showReadingOrderLinesMenuItem;
	}
	
	public MenuItem getShowReadingOrderWordsMenuItem() {
		return toolbar.showReadingOrderWordsMenuItem;
	}
	
	public MenuItem getRenderBlackeningsItem() {
		return toolbar.renderBlackeningsItem;
	}

	public ToolItem getEditingEnabledToolItem() {
		return toolbar.editingEnabledToolItem;
	}
}
