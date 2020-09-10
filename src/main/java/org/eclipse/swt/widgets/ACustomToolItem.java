package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.SysUtils;
import eu.transkribus.swt.util.SWTUtil;

public abstract class ACustomToolItem extends ToolItem {
	
	private final static Logger logger = LoggerFactory.getLogger(ACustomToolItem.class);
	
	public final static int DEFAULT_FONT_SIZE = 10;
	
	int minWidth = -1;
	int controlStyle;
	
	public ACustomToolItem (ToolBar parent, int style) {
		super (parent, SWT.SEPARATOR);
		this.controlStyle = style;
		initControl();
		addPL();
	}

	public ACustomToolItem (ToolBar parent, int style, int index) {
		super(parent, SWT.SEPARATOR, index);
		this.controlStyle = style;
		initControl();
		addPL();
	}
	
	void addPL() {
		if (SysUtils.isOsx()) {
			parent.addPaintListener(new PaintListener() {
				@Override
				public void paintControl(PaintEvent e) {
					resizeControl();
				}
			});
		}
	}
	
	public void setMinWidth(int minWidth) { this.minWidth = minWidth; }
	public int getControlStyle() { return controlStyle; }
	
	
	protected abstract void initControl();
	
	@Override
	protected void checkSubclass () {
	}
	
	protected void updateSize() {		
		int nW = 50;
//		if (control instanceof Label) {
//
//			logger.debug("text = "+((Label)control).text);
//			
////			nW = ((Label)control).getSize().x;
//			
////			GC gc = new GC(control.getParent());			
////			nW = gc.textExtent(((Label)control).text).x;
//		} else {
//			nW = control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;			
//		}
		
		nW = control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		
		if (minWidth >= 0) {
			setWidth(minWidth > nW ? minWidth : nW);
		}
		else
			setWidth(nW);
		
		center();
	}
	
	protected void center() {
		if (control == null || control.isDisposed()) {
			return;
		}
		
		/*
		 * Set the size and location of the control separately to minimize
		 * flashing in the case where the control does not resize to the
		 * size that was requested. This case can occur when the control is
		 * a combo box.
		 */
		Rectangle itemRect = getBounds();
		control.setSize(itemRect.width, itemRect.height);
		Rectangle rect = control.getBounds();
		rect.x = itemRect.x + (itemRect.width - rect.width) / 2;
		rect.y = itemRect.y + (itemRect.height - rect.height) / 2;
		control.setLocation(rect.x, rect.y);

		// center control if its a text or label (which it should be...):
		if (control instanceof Label || control instanceof Text) {
			Point ctrlPrefSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			control.setSize(itemRect.width, ctrlPrefSize.y);
			int newY = (rect.height - ctrlPrefSize.y) / 2;
			control.setLocation(rect.x, rect.y + newY);
		}
	}
		
	// FOR LINUX / WINDOWS:
//	@Override
	void resizeControl() {
		center();
	}
	
	@Override public void dispose() {
		if (!SWTUtil.isDisposed(control)) {
			control.dispose();
		}
		
		super.dispose();
	}

}
