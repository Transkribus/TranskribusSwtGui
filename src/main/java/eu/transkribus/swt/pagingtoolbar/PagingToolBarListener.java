package eu.transkribus.swt.pagingtoolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Wrapper interface for a listener for the PagingToolBar class. SelectionListener and KeyListener are inherited.
 */
public class PagingToolBarListener implements SelectionListener, KeyListener {
	protected PagingToolBar toolbar;
	
	public PagingToolBarListener(PagingToolBar toolbar) {
		this.toolbar = toolbar;
		
		attach();
	}
	
	public void attach() {
		addSelectionListener(toolbar.getPageFirstBtn());
		addSelectionListener(toolbar.getPagePrevDoubleBtn());
		addSelectionListener(toolbar.getPageNextDoubleBtn());
		addSelectionListener(toolbar.getPagePrevBtn());
		
		if (toolbar.getCurrentPageText()!=null && !toolbar.getCurrentPageText().isDisposed()) {
			toolbar.getCurrentPageText().addKeyListener(this);
		}
		
		addSelectionListener(toolbar.getPageNextBtn());
		addSelectionListener(toolbar.getPageLastBtn());
		addSelectionListener(toolbar.getReloadBtn());	
	}
	
	public void detach() {
		removeSelectionListener(toolbar.getPageFirstBtn());
		removeSelectionListener(toolbar.getPagePrevDoubleBtn());
		removeSelectionListener(toolbar.getPageNextDoubleBtn());
		removeSelectionListener(toolbar.getPagePrevBtn());
		
		if (toolbar.getCurrentPageText()!=null && !toolbar.getCurrentPageText().isDisposed()) {
			toolbar.getCurrentPageText().removeKeyListener(this);
		}
		
		removeSelectionListener(toolbar.getPageNextBtn());
		removeSelectionListener(toolbar.getPageLastBtn());
		removeSelectionListener(toolbar.getReloadBtn());	
	}
	
	private void addSelectionListener(ToolItem el) {
		if (el!=null && !el.isDisposed()) {
			el.addSelectionListener(this);
		}
	}
	
	private void removeSelectionListener(ToolItem el) {
		if (el!=null && !el.isDisposed()) {
			el.removeSelectionListener(this);
		}
	}	
	
	public void onFirstPressed() {
		
	}
	public void onPrevDoublePressed() {
		
	}
	public void onPrevPressed() {
		
	}
	public void onNextPressed() {
		
	}
	public void onNextDoublePressed() {
		
	}
	public void onLastPressed() {
		
	}
	public void onReloadPressed() {
		
	}
	public void onEnterInPageFieldPressed() {
		
	}
	
	
	@Override
	public void widgetSelected(SelectionEvent e) {
//		logger.debug("source = "+e.getSource());
		
		if (e.getSource() == toolbar.getPageFirstBtn()) {
			onFirstPressed();
		}
		else if (e.getSource() == toolbar.getPagePrevDoubleBtn()) {
			onPrevDoublePressed();
		}
		else if (e.getSource() == toolbar.getPagePrevBtn()) {
			onPrevPressed();
		}
		else if (e.getSource() == toolbar.getPageNextBtn()) {
			onNextPressed();
		}		
		else if (e.getSource() == toolbar.getPageNextDoubleBtn()) {
			onNextDoublePressed();
		}
		else if (e.getSource() == toolbar.getPageLastBtn()) {
			onLastPressed();
		}
		else if (e.getSource() == toolbar.getReloadBtn()) {
			onReloadPressed();
		}
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}	

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) { // return key pressed
			onEnterInPageFieldPressed();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}	
}
