package eu.transkribus.swt_canvas.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LabeledText extends Composite {
	
	public Label label; 
	public Text text;
	
	public LabeledText(Composite parent, String labelText) {
		super(parent, 0);
		
		this.setLayout(new GridLayout(2, false));
		
		label = new Label(this, 0);
		label.setText(labelText);
		
		text = new Text(this, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}
	
	public String txt() { return text.getText(); }
	
	public String getText() { return txt(); }
	
	public Integer toIntVal() {
		try {
			return Integer.parseInt(text.getText());
		} catch (Exception e) {
			return null;
		}
	}
	
	public Double toDoubleVal() {
		try {
			return Double.parseDouble(text.getText());
		} catch (Exception e) {
			return null;
		}
	}
}
