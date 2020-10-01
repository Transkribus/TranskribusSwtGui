package org.eclipse.swt.widgets;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Colors;

/**
 * A Text field with a slider for value adjustment.
 * <br><br>
 * Currently uses double values internally!<br>
 */
public class TrpSliderComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TrpSliderComposite.class);
	
	private final static double MIN_VALUE = 1.00;
	private final static int THUMB_SIZE = 5; // size of slider thumb
	private DecimalFormat valueFormat;
	
	double value;
	double max;
	Slider sldr;
	Text txt;		

	public TrpSliderComposite (Composite parent, int style) {
		super(parent, style);
		GridLayout gl = new GridLayout(5, true);
		gl.marginWidth = 0;
		this.setLayout(gl);
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		//TODO this stuff is copied and adapted from KeywordSpottingComposite's confidence slider. It could be possible also used there to reduce duplicate code.
		
		txt = new Text(this, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		
		sldr = new Slider(this, SWT.HORIZONTAL);
		sldr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		sldr.setThumb(THUMB_SIZE);
		sldr.setMinimum(convertToSliderValue(MIN_VALUE));
		
		txt.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				//Do nothing
			}

			@Override
			public void keyReleased(KeyEvent e) {
				final String text = txt.getText();
				Double value = getValue();
				if(!StringUtils.isEmpty(text)) {
					try {
						value = Double.parseDouble(text);
						txt.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
						if(value < MIN_VALUE) {
							value = MIN_VALUE;
						}
						if(value > max) {
							value = max;
						}
						setValue(value);
					} catch(NumberFormatException nfe) {
						txt.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
					}
				}
			}
		});
		
		sldr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(e.detail == SWT.NONE){
					setValue(sldr.getSelection() / 100.0);
				}
			}
		});			
		valueFormat = new DecimalFormat("0.00");
		setValue(MIN_VALUE);
	}
	
	public void setValue(double value) {
		this.value = value;
		updateSlider();
		updateTxt();
		this.notifyListeners(SWT.Modify, new Event());
	}
	
	public Double getValue() {
		return value;
	}

	private void updateTxt() {
		String txtValue = valueFormat.format(value);
		logger.debug("Updating txt: {}", txtValue);
		txt.setText(txtValue);
	}
	
	private void updateSlider() {
		logger.debug("Updating slider: {}", value);
		sldr.setSelection(convertToSliderValue(value));
	}
	
	private int convertToSliderValue(Double value) {
		if (value == null) {
			throw new IllegalArgumentException("Value must not be null");
		}
		final Double sliderVal = value * 100;
		logger.debug("converted to slider value: {} -> {}", value, sliderVal);
		return sliderVal.intValue();
	}
	
	public void setMaximum(double max) {
		this.max = max;
		sldr.setMaximum(convertToSliderValue(max) + THUMB_SIZE);
		if(value > max) {
			setValue(max);
		}
	}

	/**
	 * Define String format for values shown in the text field. Default is "0.00".
	 * 
	 * @param format a DecimalFormat. Null value in argument is ignored.
	 */
	public void setNumberFormat(DecimalFormat format) {
		if(format == null) {
			return;
		}
		valueFormat = format;
	}
}