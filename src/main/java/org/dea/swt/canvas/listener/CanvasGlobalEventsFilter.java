package org.dea.swt.canvas.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dea.swt.canvas.SWTCanvas;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Filters events on a global (Display) scope and transfers them to the canvas if necessary.
 * This is especially needed for the mouse scrolling on the canvas which is buggy in windows.
 */
public class CanvasGlobalEventsFilter implements Listener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasGlobalEventsFilter.class);
	
	SWTCanvas canvas;
	Display display;
	
	CanvasMouseListener mouseListener;
	CanvasKeyListener keyListener;
		
	int [] eventsToFilter = new int[] { SWT.KeyDown, SWT.KeyUp, SWT.MouseWheel, 
//			SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseUp, 
//			SWT.MouseEnter, SWT.MouseExit, , SWT.MouseHover, SWT.MouseMove 
			};

	public CanvasGlobalEventsFilter(final SWTCanvas canvas, CanvasKeyListener keyListener, CanvasMouseListener mouseListener) {
		Assert.isNotNull(canvas);
		Assert.isNotNull(mouseListener);
		Assert.isNotNull(keyListener);
		
		this.canvas = canvas;
		this.mouseListener = mouseListener;
		this.keyListener = keyListener;
		
		display = canvas.getDisplay();
		
		attach();
		
		// adding a dispose listener
		canvas.addDisposeListener(new DisposeListener() {
            @Override
			public void widgetDisposed(DisposeEvent e) {
            	logger.debug("Disposing GlobalEventsListener... removing filters!");
            	detach();
            }
        });
	}
	
	public void detach() {
		for (int e : eventsToFilter)
			display.removeFilter(e, this);	
	}
	
	public void attach() {
		for (int e : eventsToFilter)
			display.addFilter(e, this);		
	}
	
	/** Returns true if key event was delivered. */
	private boolean deliverKeyEvents(Event event) {
		switch (event.type) {
		case SWT.KeyDown:
			canvas.getKeyListener().keyPressed(new KeyEvent(event));
			return true;
		case SWT.KeyUp:
			canvas.getKeyListener().keyReleased(new KeyEvent(event));
			return true;
		}
		
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		
		if (deliverKeyEvents(event)) {
			return;
		}

		// set focus on canvas on mouse wheel - necessary due to bug in windows that does not detect mouse wheels when exiting canvas!
		if (event.type == SWT.MouseWheel) {
			if (canvas.isMouseInCanvas()) {
				canvas.setFocus();
			}

		}
	}
	
	

}
