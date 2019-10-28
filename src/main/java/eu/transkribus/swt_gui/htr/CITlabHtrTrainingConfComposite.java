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

public class CITlabHtrTrainingConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(CITlabHtrTrainingConfComposite.class);
		
	public final static int DEFAULT_TRAIN_SIZE_PER_EPOCH = 10000;
	public final static String DEFAULT_NOISE = "both";
	public final static String DEFAULT_LEARNING_RATE = "1e-3";
	public final static int DEFAULT_NUM_EPOCHS = 20;	
	
	private Text trainSizeTxt;

	private CitlabNoiseParamCombo noiseCmb;
	private HtrModelChooserButton baseModelBtn;

	private Text numEpochsTxt, learningRateTxt;
//	private Text langTxt;
//	private MultiCheckSelectionCombo langSelection;
//	private Combo scriptType;

	public CITlabHtrTrainingConfComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(4, false));

		Label numEpochsLbl = new Label(this, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		numEpochsTxt = new Text(this, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label learningRateLbl = new Label(this, SWT.NONE);
		learningRateLbl.setText("Learning Rate:");
		learningRateTxt = new Text(this, SWT.BORDER);
		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label noiseLbl = new Label(this, SWT.NONE);
		noiseLbl.setText("Noise:");
		noiseCmb = new CitlabNoiseParamCombo(this, 0);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label trainSizeLbl = new Label(this, SWT.NONE);
		trainSizeLbl.setText("Train Size per Epoch:");
		trainSizeTxt = new Text(this, SWT.BORDER);
		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label baseModelLbl = new Label(this, SWT.NONE);
		baseModelLbl.setText("Base Model:");		
		baseModelBtn = new HtrModelChooserButton(this, true, getProvider());
		baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
//		Label scriptLbl = new Label(this, SWT.NONE);
//		scriptLbl.setText("Script Type");
//		scriptType = new Combo(this, SWT.FLAT | SWT.READ_ONLY);
//		scriptType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		scriptType.setItems(new String[] {"Handwritten" , "Printed", "Mixed"});
//		
//		langSelection = new MultiCheckSelectionCombo(this, SWT.FLAT,"Languages ISO Codes", 3, 250, 400);
//		langSelection.setLayoutData(new GridData(SWT.FLAT, SWT.FLAT, false, false));
//		langSelection.setItems(Locale.getISOLanguages());
//
//		Label langLbl = new Label(this, SWT.NONE);
//		langLbl.setText("Language (Detailed):");
//		langTxt = new Text(this, SWT.BORDER);
//		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
//		gd.horizontalSpan = 2;
//		langTxt.setLayoutData(gd);
				

		setCitlabTrainingDefaults();

//		Label emptyLbl = new Label(this, SWT.NONE);
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
	}
	
	public void setCitlabTrainingDefaults() {
		numEpochsTxt.setText("" + DEFAULT_NUM_EPOCHS);
		learningRateTxt.setText(DEFAULT_LEARNING_RATE);
		noiseCmb.setDefault();
		trainSizeTxt.setText("" + DEFAULT_TRAIN_SIZE_PER_EPOCH);
		baseModelBtn.setModel(null);
	}
	
	public List<String> validateParameters(List<String> errorList) {
		if(errorList == null) {
			errorList = new ArrayList<>();
		}
		
		if (!StringUtils.isNumeric(numEpochsTxt.getText())) {
			errorList.add("Number of Epochs must contain a number!");
		}
		if (StringUtils.isEmpty(learningRateTxt.getText())) {
			errorList.add("Learning rate must not be empty!");
		}
		if (!StringUtils.isNumeric(trainSizeTxt.getText())) {
			errorList.add("Train size per epoch must contain a number!");
		}
		
		return errorList;
	}

	public CitLabHtrTrainConfig addParameters(CitLabHtrTrainConfig citlabTrainConf) {
		citlabTrainConf.setProvider(this.getProvider());
		citlabTrainConf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
		citlabTrainConf.setNoise(noiseCmb.getNoise());
		citlabTrainConf.setLearningRate(learningRateTxt.getText());
		citlabTrainConf.setTrainSizePerEpoch(Integer.parseInt(trainSizeTxt.getText()));
		
		TrpHtr htr = baseModelBtn.getModel();
		if (htr != null) {
			citlabTrainConf.setBaseModelId(htr.getHtrId());
		}
		else {
			logger.debug("No base HTR selected.");
		}
		return citlabTrainConf;
	}
	
	public String getProvider() {
		return HtrCITlabUtils.PROVIDER_CITLAB;
	}

}
