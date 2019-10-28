package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabeledCombo extends Composite {

	public Label label;
	public Combo combo;

	public ComboViewer comboV;

	public LabeledCombo(Composite parent, String labelText) {
		this(parent, labelText, 2, false);
	}

	protected LabeledCombo(Composite parent, String labelText, int numColumns, boolean makeColumnsEqualWidth) {
		super(parent, 0);

//		this.setLayout(new GridLayout(numColumns, false));
		this.setLayout(SWTUtil.createGridLayout(numColumns, makeColumnsEqualWidth, 0, 0));

		label = new Label(this, 0);
		label.setText(labelText);

		combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	public String txt() {
		return combo.getText();
	}

	public void setItems(String[] items) {
		combo.setItems(items);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		SWTUtil.setEnabled(label, enabled);
		SWTUtil.setEnabled(combo, enabled);
	}
	
	@Override public void setToolTipText(String tooltip) {
		label.setToolTipText(tooltip);
		combo.setToolTipText(tooltip);
	}

	public Combo getCombo() {
		return combo;
	}
	
	public Label getLabel() {
		return label;
	}
}
