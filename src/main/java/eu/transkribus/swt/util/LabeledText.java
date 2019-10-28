package eu.transkribus.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LabeledText extends Composite {
	
	public Label label; 
	public Text text;
	
	boolean validateToInt=false;
	
	public LabeledText(Composite parent, String labelText) {
		this(parent, labelText, false);
	}
	
	public LabeledText(Composite parent, String labelText, boolean makeColumnsEqualWidth) {
		super(parent, 0);
		
		this.setLayout(SWTUtil.createGridLayout(2, makeColumnsEqualWidth, 0, 0));
		
		label = new Label(this, 0);
		label.setText(labelText);
		
		text = new Text(this, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text.addVerifyListener( new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				if (validateToInt) {
					String oldS = text.getText();
				    String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
					try {
						Integer.parseInt(newS);
						e.doit = true;
					} catch (Exception ex) {
						e.doit = false;
					}
				}
			}
		});
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		SWTUtil.setEnabled(label, enabled);
		SWTUtil.setEnabled(text, enabled);
	}
	
	@Override public void setToolTipText(String tooltip) {
		label.setToolTipText(tooltip);
		text.setToolTipText(tooltip);
	}
	
	public void setValidateToInt(boolean validateToInt) {
		this.validateToInt = validateToInt;
	}
	
	public String txt() { return text.getText(); }
	
	public String getText() { return txt(); }
	
	public void setText(String txt) {text.setText(txt);}
	
	public Text getTextField() { return text; }
	public Label getLabel() { return label; }
	
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
