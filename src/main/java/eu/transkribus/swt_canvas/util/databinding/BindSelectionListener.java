package eu.transkribus.swt_canvas.util.databinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Binds the selection event of the two given widgets. Currently supported: ToolItem, MenuItem, Button
 */
class BindSelectionListener implements SelectionListener {
	private final static Logger logger = LoggerFactory.getLogger(BindSelectionListener.class);
	
	Widget source, target;
	
	Button b1, b2;
	ToolItem t1, t2;
	MenuItem m1, m2;
	
	public BindSelectionListener(Widget source, Widget target) throws UnsupportedOperationException {
		this.source = source; this.target = target;
		
		if (source instanceof ToolItem) {
			t1 = (ToolItem)source;
		}
		else if (source instanceof Button) {
			b1 = (Button)source;			
		}
		else if (source instanceof MenuItem) {
			m1 = (MenuItem)source;			
		}
		else
			throw new UnsupportedOperationException(source.getClass().getSimpleName()+ " not supported!");
		
		if (target instanceof ToolItem)
			t2 = (ToolItem)target;
		if (target instanceof Button)
			b2 = (Button)target;
		if (target instanceof MenuItem)
			m2 = (MenuItem)target;			
		else
			throw new UnsupportedOperationException(target.getClass().getSimpleName()+ " not supported!");
		
		
		setSelection(source, getSelection(target), true, false);
		
		addSelectionListener(source);
		addSelectionListener(target);
	}
	
	public void detachListener() {
		removeSelectionListener(source);
		removeSelectionListener(target);
	}
	
	private void addSelectionListener(Widget w) {
		if (w instanceof ToolItem)
			((ToolItem)w).addSelectionListener(this);		
		else if (w instanceof Button)
			((Button)w).addSelectionListener(this);	
		else if (w instanceof MenuItem)
			((MenuItem)w).addSelectionListener(this);
	}
	
	private void removeSelectionListener(Widget w) {
		if (w instanceof ToolItem)
			((ToolItem)w).removeSelectionListener(this);		
		else if (w instanceof Button)
			((Button)w).removeSelectionListener(this);	
		else if (w instanceof MenuItem)
			((MenuItem)w).removeSelectionListener(this);
	}	
	
	
	private boolean getSelection(Object w) {
		if (w instanceof ToolItem)
			return ((ToolItem)w).getSelection();
		else if (w instanceof Button)
			return ((Button)w).getSelection();
		else if (w instanceof MenuItem)
			return ((MenuItem)w).getSelection();
		
		return false;
	}

	private void setSelection(Object w, boolean selection, boolean onlyIfChanged, boolean fireSignal) {
		boolean selOther = getSelection(w);
		boolean changed = selOther != selection;
		logger.debug("setSelection: "+w+" changed = "+changed);
		
		if (w instanceof ToolItem) {
			if (changed || !onlyIfChanged) {
				((ToolItem)w).setSelection(selection);
				((ToolItem)w).notifyListeners(SWT.Selection, new Event());
			}
		}
		else if (w instanceof Button) {
			if (changed || !onlyIfChanged) {
				((Button)w).setSelection(selection);
				((Button)w).notifyListeners(SWT.Selection, new Event());
			}
		}
		else if (w instanceof MenuItem) {
			if (changed || !onlyIfChanged) {
				((MenuItem)w).setSelection(selection);
				((MenuItem)w).notifyListeners(SWT.Selection, new Event());
			}
		}
	}	

	@Override
	public void widgetSelected(SelectionEvent e) {
		boolean sel = getSelection(e.getSource());
		if (e.getSource() == source) {
			setSelection(target, sel, true, true);
		}
		else if (e.getSource() == target) {			
			setSelection(source, sel, true, true);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}
}
