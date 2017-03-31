package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.canvas.CanvasImage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class ImageEnhanceDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ImageEnhanceDialog.class);
	TrpMainWidget mw = TrpMainWidget.getInstance();
	
	public class ImageEnhanceSelectionListner implements SelectionListener {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		CanvasImage img;

		@Override public void widgetSelected(SelectionEvent e) {
			Object s = e.getSource();
			
			if (s == applyGammaBtn) {
				applyGamma();
			} else if (s == defaultGammaBtn) {
				gammaSlider.setSelection(50);
				updateGammaValue();
			}
			
		}

		@Override public void widgetDefaultSelected(SelectionEvent e) {
			
		}
		
		void applyGamma() {			
			double sg = gammaSlider.getSelection() / 50.0d;
			
			logger.debug("applying gamma: "+sg);
			if (mw.getScene().getMainImage()!=null) {
				mw.getScene().getMainImage().applyGamma(sg);
				mw.getCanvas().redraw();
			}

		}
	}
	
	
	Slider gammaSlider;
	Button applyGammaBtn, defaultGammaBtn;
	Label gammaValueLabel;

	public ImageEnhanceDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Image modification");
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(700, 150);
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	    setBlockOnOpen(false);
	}	
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(5, false));
		
		Label gl = new Label(container, 0);
		gl.setText("Gamma correction: ");
		
	    gammaSlider = new Slider(container, SWT.HORIZONTAL);
	    gammaSlider.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    gammaSlider.setIncrement(1);
	    gammaSlider.setValues(1, 1, 100, 1, 1, 1);
	    gammaSlider.setSelection(50);
	    gammaSlider.addSelectionListener(new SelectionAdapter() {			
			@Override public void widgetSelected(SelectionEvent e) {
				updateGammaValue();
			}
		});
	    
	    gammaValueLabel = new Label(container, 0);
	    
	    applyGammaBtn = new Button(container, SWT.PUSH);
	    applyGammaBtn.setText("Apply");
	    
	    defaultGammaBtn = new Button(container, SWT.PUSH);
	    defaultGammaBtn.setText("Default");
//	    defaultGammaBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
	    
	    updateSliderValueFromGamma();
	    
	    addListener();
		
		return container;
	}
	
	private void updateSliderValueFromGamma() {
		CanvasImage ci = mw.getScene().getMainImage();
		if (ci != null) {
			int sel = (int) (ci.gamma*50.0d);
			logger.debug("gamma from img = "+ci.gamma+" sel = "+sel);
			gammaSlider.setSelection(sel);
		}
		updateGammaValue();
	}
	
	public void updateGammaValue() {
		gammaValueLabel.setText(""+gammaSlider.getSelection() / 50.0d);
	}
	
	void addListener() {
		ImageEnhanceSelectionListner l = new ImageEnhanceSelectionListner();
		
		applyGammaBtn.addSelectionListener(l);
		defaultGammaBtn.addSelectionListener(l);
	}

	public Slider getGammaSlider() {
		return gammaSlider;
	}

	public Button getApplyGammaBtn() {
		return applyGammaBtn;
	}

	public Button getDefaultGammaBtn() {
		return defaultGammaBtn;
	}
	
	public void setActive() {

		Shell shell = this.getShell();
		if(shell == null || shell.isDisposed()){
			open();
		}else{
			shell.setActive();	
			shell.setFocus();
		}
	}
	

}

