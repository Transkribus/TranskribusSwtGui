package eu.transkribus.swt_gui.credits;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CreditPackageManagerDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(CreditPackageManagerDialog.class);
	
	protected Composite dialogArea;
	
	final TrpCreditPackage creditPackage;
	
	TrpSliderComposite sliderComp;
	Text splitNumPackagesTxt;
	Label errorLbl;
	CreditCostsTable costsTbl;
	
	//fields for storing input on okPressed()
	int numPackages;
	double creditValue;
	
	public CreditPackageManagerDialog(Shell parent, TrpCreditPackage creditPackage) {
		super(parent);
		this.creditPackage = creditPackage;
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		
		Group splitPackageGrp = new Group(dialogArea, SWT.BORDER);
		splitPackageGrp.setText("Split Volume to New Packages");
		splitPackageGrp.setLayoutData(new GridData(GridData.FILL_BOTH));
		splitPackageGrp.setLayout(new GridLayout(2, false));
		
		Label packageLbl = new Label(splitPackageGrp, SWT.NONE);
		packageLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		packageLbl.setText("Source Package '" + creditPackage.getProduct().getLabel() + "', " + creditPackage.getBalance() + " credits");
		
		Label splitVolLbl = new Label(splitPackageGrp, SWT.NONE);
		splitVolLbl.setText("Credits per Package:");
//		splitVolLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		sliderComp = new TrpSliderComposite(splitPackageGrp, SWT.NONE);
		sliderComp.setMaximum(creditPackage.getBalance());
		
		Label splitNumPackagesLbl = new Label(splitPackageGrp, SWT.NONE);
		splitNumPackagesLbl.setText("Nr. of Packages:");
		
		splitNumPackagesTxt = new Text(splitPackageGrp, SWT.BORDER);
		splitNumPackagesTxt.setText("" + 1);
		
		errorLbl = new Label(splitPackageGrp, SWT.NONE);
		errorLbl.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
		errorLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label tableLbl = new Label(splitPackageGrp, SWT.NONE);
		tableLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		tableLbl.setText("Page Volume per Package:");
		
		costsTbl = new CreditCostsTable(splitPackageGrp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL,  0.0, Storage.getInstance().getCreditCosts(null, false));
		costsTbl.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		addListeners();
		
		updateCostsTable();
				
		return dialogArea;
	}
	
	private void addListeners() {
		sliderComp.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event arg0) {
				updateCostsTable();
			}
		});
		
		splitNumPackagesTxt.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				errorLbl.setText("");
				//validate input
				int numPackages;
				try {
					numPackages = parseNumPackagesTxt();
				} catch (NumberFormatException e) {
					errorLbl.setText(e.getMessage());
					return;
				}
				sliderComp.setMaximum(creditPackage.getBalance() / numPackages);
			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				// Do nothing
				
			}
		});
	}

	@Override
	protected void okPressed() {
		int tmp;
		try {
			tmp = parseNumPackagesTxt();
		} catch (NumberFormatException e) {
			errorLbl.setText(e.getMessage());
			return;
		}
		numPackages = tmp;
		creditValue = sliderComp.getValue();
		if(creditValue <= 0.0) {
			String amountError = "Nr. of Credits is too low";
			errorLbl.setText(errorLbl.getText() + "," + amountError);
		}
		logger.debug("okPressed: numPackages = {}, creditValue = {}", numPackages, creditValue);
		// save input
		super.okPressed();
	}
	
	public int getNumPackages() {
		return numPackages;
	}
	
	public double getCreditValue() {
		return creditValue;
	}
	
	public TrpCreditPackage getCreditPackage() {
		return creditPackage;
	}
	
	private int parseNumPackagesTxt() throws NumberFormatException {
		String numPackagesStr = splitNumPackagesTxt.getText();
		try {
			return Integer.parseInt(numPackagesStr);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Nr. of packages is not a number: " + numPackagesStr);
		}
	}
	
	private void updateCostsTable() {
		costsTbl.setNrOfCredits(sliderComp.getValue());
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Manage Credit Package");
		newShell.setMinimumSize(480, 480);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 480);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}

	public static class TrpSliderComposite extends Composite {
		private final static double MIN_CREDITS = 1.00;
		private final static int THUMB_SIZE = 5; // size of slider thumb
		private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("0.00");
		
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
			
			//TODO this stuff is copied from KeywordSpottingComposite's confidence slider. Pack this into a separate composite for easy reuse?
			
			txt = new Text(this, SWT.BORDER);
			txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			
			sldr = new Slider(this, SWT.HORIZONTAL);
			sldr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			sldr.setThumb(THUMB_SIZE);
			sldr.setMinimum(convertToSliderValue(MIN_CREDITS));
			
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
							if(value < MIN_CREDITS) {
								value = MIN_CREDITS;
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
			setValue(MIN_CREDITS);
		}
		
		public void setValue(double value) {
			this.value = value;
			updateSlider();
			updateTxt();
			this.notifyListeners(SWT.Modify, new Event());
		}
		
		public double getValue() {
			return value;
		}

		private void updateTxt() {
			String txtValue = VALUE_FORMAT.format(value);
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
	}
}
