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

import eu.transkribus.core.model.beans.PyLaiaHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.HtrCITlabUtils;

public class PyLaiaTrainingConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(PyLaiaTrainingConfComposite.class);
	
	private Text numEpochsTxt;
//	private Text earlyStoppingTxt;
	private HtrModelChooserButton baseModelBtn;
	
	public PyLaiaTrainingConfComposite(Composite parent, boolean enableBaseModelSelection, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label numEpochsLbl = new Label(this, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		numEpochsTxt = new Text(this, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
//		if (false) {
//		Label earlyStoppingLbl = new Label(this, SWT.NONE);
//		earlyStoppingLbl.setText("Early Stopping: ");
//		earlyStoppingTxt = new Text(this, SWT.BORDER);
//		earlyStoppingTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		earlyStoppingTxt.setToolTipText("Stop training early, if model does not improve for this number of epochs. (Optional)");
//		}

//		Label learningRateLbl = new Label(this, SWT.NONE);
//		learningRateLbl.setText("Learning Rate:");
//		learningRateTxt = new Text(this, SWT.BORDER);
//		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		Label trainSizeLbl = new Label(this, SWT.NONE);
//		trainSizeLbl.setText("Train Size per Epoch:");
//		trainSizeTxt = new Text(this, SWT.BORDER);
//		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		//Base models are not supported for CITlabPlus yet
		if(enableBaseModelSelection) {
			Label baseModelLbl = new Label(this, SWT.NONE);
			baseModelLbl.setText("Base Model:");		
			baseModelBtn = new HtrModelChooserButton(this, true, getProvider());
			baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		} else {
			baseModelBtn = null;
		}
		
		setDefaults();

		new Label(this, SWT.NONE);
		Button resetUroDefaultsBtn = new Button(this, SWT.PUSH);
		resetUroDefaultsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		resetUroDefaultsBtn.setText("Reset to defaults");
		resetUroDefaultsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				setDefaults();
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
	
	public void setDefaults() {
		numEpochsTxt.setText("" + PyLaiaHtrTrainConfig.DEFAULT_NUM_EPOCHS);
//		if (earlyStoppingTxt!=null) {
//			earlyStoppingTxt.setText(""+ CitLabHtrTrainConfig.DEFAULT_EARLY_STOPPING);
//		}
		if(baseModelBtn != null) {
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
//		if (earlyStoppingTxt!=null) {
//			if (!StringUtils.isEmpty(earlyStoppingTxt.getText()) && !StringUtils.isNumeric(earlyStoppingTxt.getText())) {
//				errorList.add("Early stopping must be empty or a number!");
//			}			
//		}

		return errorList;
	}

	public PyLaiaHtrTrainConfig addParameters(PyLaiaHtrTrainConfig conf) {
		conf.setProvider(this.getProvider());
		conf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
//		if (earlyStoppingTxt != null) {
//			citlabTrainConf.setEarlyStopping(CoreUtils.parseInteger(earlyStoppingTxt.getText(), null));	
//		}
		
		if(baseModelBtn != null) {
			TrpHtr htr = baseModelBtn.getModel();
			if (htr != null) {
				conf.setBaseModelId(htr.getHtrId());
			} else {
				logger.debug("No base HTR selected.");
			}
		}
		return conf;
	}

	public String getProvider() {
		return HtrCITlabUtils.PROVIDER_PYLAIA;
	}
}
