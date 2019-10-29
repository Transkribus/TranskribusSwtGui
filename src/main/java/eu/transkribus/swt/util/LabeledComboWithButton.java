package eu.transkribus.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class LabeledComboWithButton extends LabeledCombo {

	protected Button button;
	
	public LabeledComboWithButton(Composite parent, String labelText, String buttonText) {
		super(parent, labelText, 3, false);
		button = new Button(this, SWT.PUSH);
		button.setText(buttonText);
	}

	public Button getButton() {
		return button;
	}
}
