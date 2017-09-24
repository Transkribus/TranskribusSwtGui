package eu.transkribus.swt_gui.htr;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TextRecognitionComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionComposite.class);
	
	private LabeledCombo methodCombo;
	
	public static final String METHOD_OCR = "OCR (Abbyy FineReader)";
	public static final String METHOD_HTR = "HTR (CITlab)";
	
	public static final String[] METHODS = { METHOD_HTR, METHOD_OCR };
	
	Button runBtn;
	
	HtrModelChooserButton modelsBtn;
	Button trainBtn;
//	Button text2ImageBtn;
	
	public TextRecognitionComposite(Composite parent, int style) {
		super(parent, style);
		
		int nCols = 2;
		GridLayout gl = new GridLayout(nCols, false);
		gl.marginHeight = gl.marginWidth = 0;
		this.setLayout(gl);

		methodCombo = new LabeledCombo(this, "Method:");
		methodCombo.combo.setItems(METHODS);
		methodCombo.combo.select(0);
		methodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));
		methodCombo.combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGui();
			}
		});
		
		modelsBtn = new HtrModelChooserButton(this);
		modelsBtn.setText("Models...");
		modelsBtn.setImage(Images.getOrLoad("/icons/model2_16.png"));
		modelsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		trainBtn = new Button(this, 0);
		trainBtn.setText("Train...");
		trainBtn.setImage(Images.getOrLoad("/icons/muscle_16.png"));
//		trainBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		trainBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		text2ImageBtn = new Button(this, SWT.PUSH);
//		text2ImageBtn.setText("Text2Image (experimental)...");
//		text2ImageBtn.setImage(Images.getOrLoad("/icons/image_link.png"));
//		text2ImageBtn.setToolTipText("Tries to align the text in this document to a layout analysis\nWarning: does take some time...");
////		text2ImageBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		text2ImageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		runBtn = new Button(this, 0);
		runBtn.setText("Run...");
		runBtn.setImage(Images.ARROW_RIGHT);
		runBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		runBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));
		
		Storage.getInstance().addListener(new IStorageListener() {
			public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
				updateGui();
			}
		});
		
		updateGui();
	}
	
	public Button getRunBtn() {
		return runBtn;
	}
	
	public Button getTrainBtn() {
		return trainBtn;
	}
	
//	public Button getText2ImageBtn() {
//		return text2ImageBtn;
//	}
	
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
	
	public void updateGui() {
		boolean withTrainBtn = false;
		
		if(Storage.getInstance() != null && Storage.getInstance().isLoggedIn()) {
			try {
				withTrainBtn = Storage.getInstance().getConnection().isUserAllowedForJob(JobImpl.CITlabHtrTrainingJob.toString());
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
				withTrainBtn = false;
			}
		}
		
		setBtnVisibility(withTrainBtn);
	}
	
	private void setBtnVisibility(boolean withTrainBtn) {
		boolean showTrainBtn = withTrainBtn && isHtr();

		if (showTrainBtn) {
			trainBtn.setParent(this);
//			runBtn.moveBelow(trainBtn);
			trainBtn.moveAbove(runBtn);
		} else {
			trainBtn.setParent(SWTUtil.dummyShell);
		}
				
		this.layout();
		logger.info("parent: "+getParent());
		getParent().layout();
	}

}
