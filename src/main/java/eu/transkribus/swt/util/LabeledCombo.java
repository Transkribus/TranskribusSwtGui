package eu.transkribus.swt.util;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabeledCombo extends Composite {
	
	public Label label; 
	public Combo combo;
	
	public ComboViewer comboV;
	
	public LabeledCombo(Composite parent, String labelText) {
		super(parent, 0);
		
		this.setLayout(new GridLayout(2, false));
		
		label = new Label(this, 0);
		label.setText(labelText);
		
		combo = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}
	
	public String txt() { return combo.getText(); }
	
}
