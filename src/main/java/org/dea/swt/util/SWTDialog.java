package org.dea.swt.util;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public abstract class SWTDialog<T> extends Dialog {
	Shell shell;
	
	T result;

	public SWTDialog(Shell parent, int style) {
		super(parent, style);
	}
	
	abstract protected void createContents();
	
	public T getResult() { return result; }
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public T open() {
		createContents();
		SWTUtil.centerShell(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}
}
