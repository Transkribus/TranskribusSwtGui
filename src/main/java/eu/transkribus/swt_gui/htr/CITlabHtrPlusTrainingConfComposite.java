package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.HtrCITlabUtils;

public class CITlabHtrPlusTrainingConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(CITlabHtrPlusTrainingConfComposite.class);
		
	private Text numEpochsTxt;
	private HtrModelChooserButton baseModelBtn;
	
	private final static boolean BASE_MODEL_SELECTION_ENABLED = false;
	
	// FIXME as soon as update to CITlabModule 2.0.2 is done, this can be removed. 2.0.1 sets "-1" which would use the whole set in each epoch.
	public final static int DEFAULT_TRAIN_SIZE_PER_EPOCH = 8192;
	public final static int DEFAULT_NUM_EPOCHS = 200;	

//	private CitlabNoiseParamCombo noiseCmb;
//	private Text trainSizeTxt, learningRateTxt;

	public CITlabHtrPlusTrainingConfComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label numEpochsLbl = new Label(this, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		numEpochsTxt = new Text(this, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

//		Label learningRateLbl = new Label(this, SWT.NONE);
//		learningRateLbl.setText("Learning Rate:");
//		learningRateTxt = new Text(this, SWT.BORDER);
//		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		Label trainSizeLbl = new Label(this, SWT.NONE);
//		trainSizeLbl.setText("Train Size per Epoch:");
//		trainSizeTxt = new Text(this, SWT.BORDER);
//		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		//Base models are not supported for CITlabPlus yet
		if(BASE_MODEL_SELECTION_ENABLED) {
			Label baseModelLbl = new Label(this, SWT.NONE);
			baseModelLbl.setText("Base Model:");		
			baseModelBtn = new HtrModelChooserButton(this, true, getProvider());
			baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		} else {
			baseModelBtn = null;
		}

		setCitlabTrainingDefaults();

		new Label(this, SWT.NONE);
		Button resetUroDefaultsBtn = new Button(this, SWT.PUSH);
		resetUroDefaultsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		resetUroDefaultsBtn.setText("Reset to defaults");
		resetUroDefaultsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				setCitlabTrainingDefaults();
			}
		});
		
		//TODO advanced parameters
		
//		Group customGrp = new Group(this, SWT.BORDER);
//		customGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true, 4, 2));
//		customGrp.setLayout(new GridLayout(2, true));
//		
//		Label noiseLbl = new Label(customGrp, SWT.NONE);
//		noiseLbl.setText("Noise:");
//		noiseCmb = new CitlabNoiseParamCombo(customGrp, 0);
//		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		
//		new Label(customGrp, SWT.NONE);
//		new Label(customGrp, SWT.NONE);
//		
//		final CustomParameter[] params = {
//				new CustomParameter(CitLabHtrTrainConfig.LEARNING_RATE_KEY),
//				new CustomParameter(CitLabHtrTrainConfig.TRAIN_SIZE_KEY)		
//		};
//		
//		AdvancedParametersComposite advComp = new AdvancedParametersComposite(customGrp, params);
//		advComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}
	
	public void setCitlabTrainingDefaults() {
		numEpochsTxt.setText("" + DEFAULT_NUM_EPOCHS);
//		learningRateTxt.setText(CitLabHtrTrainConfig.DEFAULT_LEARNING_RATE);
//		noiseCmb.setDefault();
//		trainSizeTxt.setText("" + CitLabHtrTrainConfig.DEFAULT_TRAIN_SIZE_PER_EPOCH);
		if(BASE_MODEL_SELECTION_ENABLED) {
			baseModelBtn.setModel(null);
		}
	}
	
	public List<String> validateParameters(List<String> errorList) {
		if(errorList == null) {
			errorList = new ArrayList<>();
		}
		if (!StringUtils.isNumeric(numEpochsTxt.getText())) {
			errorList.add("Number of Epochs must contain a number!");
		}
//		if (StringUtils.isEmpty(learningRateTxt.getText())) {
//			errorList.add("Learning rate must not be empty!");
//		}
//		if (!StringUtils.isNumeric(trainSizeTxt.getText())) {
//			errorList.add("Train size per epoch must contain a number!");
//		}
		return errorList;
	}

	public CitLabHtrTrainConfig addParameters(CitLabHtrTrainConfig citlabTrainConf) {
		citlabTrainConf.setProvider(this.getProvider());
		citlabTrainConf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
//		citlabTrainConf.setNoise(noiseCmb.getNoise());
//		citlabTrainConf.setLearningRate(learningRateTxt.getText());
//		citlabTrainConf.setTrainSizePerEpoch(Integer.parseInt(trainSizeTxt.getText()));
		
		if(BASE_MODEL_SELECTION_ENABLED) {
			TrpHtr htr = baseModelBtn.getModel();
			if (htr != null) {
				citlabTrainConf.setBaseModelId(htr.getHtrId());
			} else {
				logger.debug("No base HTR selected.");
			}
		}
		return citlabTrainConf;
	}

	public String getProvider() {
		return HtrCITlabUtils.PROVIDER_CITLAB_PLUS;
	}
}
