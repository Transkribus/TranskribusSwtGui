package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dea.fimgstoreclient.beans.ImgType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.PyLaiaCreateModelPars;
import eu.transkribus.core.model.beans.PyLaiaHtrTrainConfig;
import eu.transkribus.core.model.beans.PyLaiaTrainCtcPars;
import eu.transkribus.core.model.beans.TextFeatsCfg;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaTrainingConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(PyLaiaTrainingConfComposite.class);
	
	private Text numEpochsTxt, earlyStoppingTxt, learningRateTxt;
	private Combo imgTypeCombo;
//	private Text trainSizeTxt;
	private HtrModelChooserButton baseModelBtn;
	private Button advancedParsBtn;
	
	TextFeatsCfg textFeatsCfg = new TextFeatsCfg();
	PyLaiaCreateModelPars createModelPars = PyLaiaCreateModelPars.getDefault();
	PyLaiaTrainCtcPars trainCtcPars = PyLaiaTrainCtcPars.getDefault();
//	int batchSize = PyLaiaTrainCtcPars.DEFAULT_BATCH_SIZE;
	
	public PyLaiaTrainingConfComposite(Composite parent, boolean enableBaseModelSelection, int style) {
		super(parent, style);
//		setLayout(new GridLayout(2, false));
		setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		ScrolledComposite sc = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		sc.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		Composite content = new Composite(sc, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		content.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label numEpochsLbl = new Label(content, SWT.NONE);
		numEpochsLbl.setText("Max-nr. of Epochs:");
		numEpochsTxt = new Text(content, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		numEpochsTxt.setToolTipText("The maximum number of epochs, if early stopping does not apply");
		
		Label earlyStoppingLbl = new Label(content, SWT.NONE);
		earlyStoppingLbl.setText("Early Stopping: ");
		earlyStoppingTxt = new Text(content, SWT.BORDER);
		earlyStoppingTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		earlyStoppingTxt.setToolTipText("Stop training early, if model does not improve for this number of epochs");
		
		//Base models are not supported for PyLaia yet
		if(enableBaseModelSelection) {
			Label baseModelLbl = new Label(content, SWT.NONE);
			baseModelLbl.setText("Base Model:");		
			baseModelBtn = new HtrModelChooserButton(content, true, getProvider());
			baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		} else {
			baseModelBtn = null;
		}		
		
//		Group advancedParsGroup = new Group(this, 0);
//		advancedParsGroup.setText("Advanced parameters");
//		advancedParsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
//		advancedParsGroup.setLayout(new GridLayout(2, false));

		Label learningRateLbl = new Label(content, SWT.NONE);
		learningRateLbl.setText("Learning Rate:");
		learningRateTxt = new Text(content, SWT.BORDER);
		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label imgTypeLbl = new Label(content, 0);
		imgTypeLbl.setText("Image type:");
		imgTypeCombo = new Combo(content, SWT.READ_ONLY | SWT.DROP_DOWN);
		imgTypeCombo.add("Originals");
		imgTypeCombo.add("Compressed");
		imgTypeCombo.select(0);
		imgTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		advancedParsBtn = new Button(content, SWT.PUSH);
		advancedParsBtn.setText("Advanced parameters...");
		advancedParsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		SWTUtil.onSelectionEvent(advancedParsBtn, e -> {
			PyLaiaAdvancedConfDialog d = new PyLaiaAdvancedConfDialog(getShell(), /*batchSize,*/ textFeatsCfg, createModelPars, trainCtcPars);
			if (d.open() == IDialogConstants.OK_ID) {
//				batchSize = d.getBatchSize();
				textFeatsCfg = d.getTextFeatsCfg();
				createModelPars = d.getModelPars();
				trainCtcPars = d.getTrainPars();
//				logger.info("batch size = "+batchSize);
				logger.info("preprocessing config = "+textFeatsCfg.toSingleLineConfigString());
				logger.info("modelPars = "+createModelPars.toSingleLineString());
				logger.info("trainPars = "+trainCtcPars.toSingleLineString());
			}
		});
		
//		if (false) {
//		Label trainSizeLbl = new Label(this, SWT.NONE);
//		trainSizeLbl.setText("Train Size per Epoch:");
//		trainSizeTxt = new Text(this, SWT.BORDER);
//		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//		}

		setDefaults();

//		new Label(this, SWT.NONE);
		Button resetBtn = new Button(content, SWT.PUSH);
		resetBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		resetBtn.setText("Reset to defaults");
		resetBtn.addSelectionListener(new SelectionAdapter() {
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
		
		sc.setContent(content);
	    sc.setExpandHorizontal(true);
	    sc.setExpandVertical(true);
		content.layout();
	    sc.setMinSize(0, content.computeSize(SWT.DEFAULT, SWT.DEFAULT).y-1);	    
	}
	
	public void setDefaults() {
		numEpochsTxt.setText("" + PyLaiaTrainCtcPars.DEFAULT_MAX_EPOCHS);
		earlyStoppingTxt.setText(""+ PyLaiaTrainCtcPars.DEFAULT_MAX_NONDECREASING_EPOCHS);
//		learningRateTxt.setText(""+PyLaiaTrainCtcPars.DEFAULT_LEARNING_RATE);
		learningRateTxt.setText(CoreUtils.formatDoubleNonScientific(PyLaiaTrainCtcPars.DEFAULT_LEARNING_RATE));
//		if (trainSizeTxt!=null) {
//			trainSizeTxt.setText(""+PyLaiaTrainCtcPars.DEFAULT_BATCH_SIZE);	
//		}
		if(baseModelBtn != null) {
			baseModelBtn.setModel(null);
		}
		imgTypeCombo.select(0);
	}
	
	public List<String> validateParameters(List<String> errorList) {
		if(errorList == null) {
			errorList = new ArrayList<>();
		}
		if (!StringUtils.isNumeric(numEpochsTxt.getText())) {
			errorList.add("Number of Epochs must be a number!");
		}
		if (!StringUtils.isNumeric(earlyStoppingTxt.getText())) {
			errorList.add("Early stopping must be a number!");
		}
//		if (trainSizeTxt!=null) {
//			if (!StringUtils.isNumeric(trainSizeTxt.getText())) {
//				errorList.add("Train size must be a number!");
//			}			
//		}
		if (!CoreUtils.isDouble(learningRateTxt.getText())) {
			errorList.add("Learning rate must be a floating point number!");
		}
		
		return errorList;
	}

	public PyLaiaHtrTrainConfig addParameters(PyLaiaHtrTrainConfig conf) {
		conf.setProvider(this.getProvider());
		
		// important: set advanced preprocessing, model and train pars here, s.t. the "main" pars such as learning rate etc. can override those maybe set also in the advanced dialog... 
		conf.setTextFeatsCfg(textFeatsCfg);
		conf.setCreateModelPars(createModelPars);
		conf.setTrainCtcPars(trainCtcPars);
		
		// those are the "main" parameters:
//		conf.setBatchSize(batchSize); // now set in advanced pars dialog
		conf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
		conf.setEarlyStopping(Integer.parseInt(earlyStoppingTxt.getText()));
//		if (trainSizeTxt!=null) {
//			conf.setBatchSize(Integer.parseInt(trainSizeTxt.getText()));	
//		}
		conf.setLearningRate(Double.parseDouble(learningRateTxt.getText()));
		
		// NOTE: not used by PyLaia currently, but maybe useful in the future... 
		if(baseModelBtn != null) {
			TrpHtr htr = baseModelBtn.getModel();
			if (htr != null) {
				conf.setBaseModelId(htr.getHtrId());
			} else {
				logger.debug("No base HTR selected.");
			}
		}

		ImgType imgType = imgTypeCombo.getSelectionIndex()==1 ? ImgType.view : ImgType.orig;
		logger.debug("imgType = "+imgType);
		conf.setImgType(imgType);
		
		return conf;
	}
	
//	public TextFeatsCfg getPreprocessingConfig() {
//		return textFeatsCfg;
//	}
	
//	public int getBatchSize() {
//		return batchSize;
//	}

	public String getProvider() {
		return HtrPyLaiaUtils.PROVIDER_PYLAIA;
	}
}
