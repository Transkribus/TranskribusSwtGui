package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.swt.util.SWTUtil;

public class LearningRateCombo extends Composite {
	
	Combo combo;
	
	static String[] learningRates = new String[] { "0.001", "0.0015", "0.002", "0.0025", "0.003", "0.0035", "0.004", "0.0045", "0.005", "0.0055", "0.006", "0.0065", "0.007", "0.0075", "0.008", "0.0085", "0.009", "0.0095", "0.01" }; 

	public LearningRateCombo(Composite parent, int style) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		combo = new Combo(this, SWT.DROP_DOWN);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		combo.setToolTipText("The step size during each epoch of the training");
		
		for (int j = 2; j <= 4; ++j) {
			for (int i = 1; i <= 9; ++i) {
				combo.add(i + "e-" + j);
			}
		}
		combo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
					System.out.println(getLearningRate());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
//		combo.setItems(NOISE_OPTIONS);
		
		setDefault();
	}
	
	public void setDefault() {
		for (int i=0; i<combo.getItems().length; ++i) {
			if (combo.getItem(i).equals(CitLabHtrTrainConfig.DEFAULT_LEARNING_RATE)) {
				combo.select(i);
			}
		}
		
//		combo.setText(""+CitLabHtrTrainConfig.DEFAULT_LEARNING_RATE);		
	}
	
	public String getLearningRate() throws NumberFormatException {
		return combo.getText();
	}
	
	public double getLearningRateAsDouble() throws NumberFormatException {
		return Double.valueOf(combo.getText());
	}

}
