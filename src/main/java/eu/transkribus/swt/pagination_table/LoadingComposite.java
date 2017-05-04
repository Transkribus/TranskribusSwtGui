package eu.transkribus.swt.pagination_table;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.dnd.SwtUtil;

import eu.transkribus.swt.util.Images;

public class LoadingComposite extends Composite {
	public Label label;
	public Button reload;

	public LoadingComposite(Composite parent, boolean withTimingLabel) {
		super(parent, SWT.NONE);
		super.setLayout(new GridLayout(withTimingLabel ? 2 : 1, false));

		reload = new Button(this, SWT.PUSH);
		reload.setToolTipText("Reload current page");
		reload.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reload.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (withTimingLabel) {
			label = new Label(this, SWT.NONE);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
	}

	public void setText(String text) {
		if (isDisposed() || SwtUtil.isDisposed(label))
			return;
		
		label.setText(text);
		this.redraw();
	}
}