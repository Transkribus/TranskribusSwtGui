package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;

@Deprecated
public class ToolbarToolItem extends ACustomToolItem {
	ToolBar toolbar;
	
	public ToolbarToolItem(ToolBar parent, int style, ToolBar toolbar) {
		super(parent, style);
		this.toolbar = toolbar;
	}

	@Override
	protected void initControl() {	
		this.setControl(toolbar);
	}
		
	@Override
	void resizeControl () {
		super.resizeControl();
		if (toolbar != null)
			setWidth(toolbar.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
	}
	
	

}
