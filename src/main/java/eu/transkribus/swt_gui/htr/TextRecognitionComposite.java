package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TextRecognitionComposite extends Composite {

	private LabeledCombo methodCombo;
	
	public static final String METHOD_OCR = "OCR (Abbyy FineReader)";
	public static final String METHOD_HTR = "HTR (CITlab)";
	
	public static final String[] METHODS = { METHOD_HTR, METHOD_OCR };
	
	Button runBtn;
	Button trainBtn;
	
	public TextRecognitionComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);

		methodCombo = new LabeledCombo(this, "Method:");
		methodCombo.combo.setItems(METHODS);
		methodCombo.combo.select(0);
		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		methodCombo.combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGui(Storage.getInstance().isAdminLoggedIn());
			}
		});
		
		trainBtn = new Button(SWTUtil.dummyShell, 0);
		trainBtn.setText("Train...");
		trainBtn.setImage(Images.getOrLoad("/icons/muscle_16.png"));
		trainBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		runBtn = new Button(this, 0);
		runBtn.setText("Run...");
		runBtn.setImage(Images.ARROW_RIGHT);
		runBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Storage.getInstance().addListener(new IStorageListener() {
			public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
				updateGui(Storage.getInstance().isAdminLoggedIn());
			}
		});
	}
	
	public Button getRunBtn() {
		return runBtn;
	}
	
	public Button getTrainBtn() {
		return trainBtn;
	}
	
	public String getSelectedMethod() {
		return methodCombo.txt();
	}
	
	public boolean isOcr() {
		return methodCombo.txt().equals(METHOD_OCR);
	}
	
	public boolean isHtr() {
		return methodCombo.txt().equals(METHOD_HTR);
	}

//	private void createTrainBtn() {
//		trainBtn = new Button(this, 0);
//		trainBtn.moveAbove(runBtn);
//		trainBtn.setText("Train...");
//		trainBtn.setImage(Images.getOrLoad("/icons/muscle_16.png"));
//		trainBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//	}
	
	public void updateGui(boolean withTrainBtn) {
		boolean showTrainBtn = withTrainBtn && isHtr();
		
		if (showTrainBtn) {
			trainBtn.setParent(this);
//			runBtn.moveBelow(trainBtn);
			trainBtn.moveAbove(runBtn);
		} else {
			trainBtn.setParent(SWTUtil.dummyShell);
		}
		
		this.layout();
	}

}
