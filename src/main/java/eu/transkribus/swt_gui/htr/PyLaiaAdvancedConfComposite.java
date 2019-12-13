package eu.transkribus.swt_gui.htr;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.PyLaiaCreateModelPars;
import eu.transkribus.core.model.beans.PyLaiaTrainCtcPars;
import eu.transkribus.core.model.beans.TextFeatsCfg;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class PyLaiaAdvancedConfComposite extends Composite {
	int batchSize = PyLaiaTrainCtcPars.DEFAULT_BATCH_SIZE;
	TextFeatsCfg textFeatsCfg = new TextFeatsCfg();
	PyLaiaCreateModelPars modelPars;
	PyLaiaTrainCtcPars trainPars;
	
	LabeledText batchSizeText;
	
	Group preprocGroup;
	
	Button deslopeCheck;
	Button deslantCheck;
	Button stretchCheck;
	Button enhanceCheck;
	LabeledText enhWinText;
	LabeledText enhPrmText;
	LabeledText normHeightText;
	LabeledText normxHeightText;
	Button momentnormCheck;
	Button fpgramCheck;
	Button fcontourCheck;
	LabeledText fcontour_dilateText;
	LabeledText paddingText;
	
	Group modelParsGroup;
	Text modelParsText;
	
	Group trainParsGroup;
	Text trainParsText;
	
	public PyLaiaAdvancedConfComposite(Composite parent, int batchSize, TextFeatsCfg textFeatsCfg, PyLaiaCreateModelPars modelPars, PyLaiaTrainCtcPars trainPars) {
		super(parent, 0);

		this.batchSize = batchSize;
		this.textFeatsCfg = textFeatsCfg == null ? new TextFeatsCfg() : textFeatsCfg;
		this.modelPars = modelPars == null ? PyLaiaCreateModelPars.getDefault() : modelPars;
		this.trainPars = trainPars == null ? PyLaiaTrainCtcPars.getDefault() : trainPars;
		
		// remove irrelevant parameters
		this.modelPars.remove("--fixed_input_height"); // this is determined via fixed height par in textFeatsCfg already!
		this.trainPars.remove("--batch_size"); // set via custom field
		this.trainPars.remove("--max_nondecreasing_epochs"); // set via main UI
		this.trainPars.remove("--max_epochs"); // set via main UI
		this.trainPars.remove("--learning_rate"); // set via main UI
		
		createContent();
	}
	
	public int getCurrentBatchSize() throws IOException {
		try {
			batchSize = Integer.parseInt(batchSizeText.getText());
			if (batchSize <= 0 || batchSize>50) {
				throw new IOException("Batch size not in range 1-50");
			}
			return batchSize;
		} catch (NumberFormatException e) {
			throw new IOException("Not a valid batch size: "+batchSizeText.getText());
		}
	}
	
	public TextFeatsCfg getTextFeatsCfg() {
		textFeatsCfg.setDeslope(deslopeCheck.getSelection());
		textFeatsCfg.setDeslant(deslantCheck.getSelection());
		textFeatsCfg.setStretch(stretchCheck.getSelection());
		textFeatsCfg.setEnh(enhanceCheck.getSelection());
		
		textFeatsCfg.setEnh_win(enhWinText.toIntVal(textFeatsCfg.getEnh_win()));
		textFeatsCfg.setEnh_prm(enhPrmText.toDoubleVal(textFeatsCfg.getEnh_prm()));
		textFeatsCfg.setNormheight(normHeightText.toIntVal(textFeatsCfg.getNormheight()));
		textFeatsCfg.setNormxheight(normxHeightText.toIntVal(textFeatsCfg.getNormxheight()));
		
		textFeatsCfg.setMomentnorm(momentnormCheck.getSelection());
		textFeatsCfg.setFpgram(fpgramCheck.getSelection());
		textFeatsCfg.setFcontour(fcontourCheck.getSelection());
		
		textFeatsCfg.setFcontour_dilate(fcontour_dilateText.toIntVal(textFeatsCfg.getFcontour_dilate()));
		textFeatsCfg.setPadding(paddingText.toIntVal(textFeatsCfg.getPadding()));
		
		return textFeatsCfg;
	}
	
	public PyLaiaCreateModelPars getCreateModelPars() {
		PyLaiaCreateModelPars modelPars = new PyLaiaCreateModelPars();
		insertParametersFromText(modelParsText, modelPars);
		this.modelPars = modelPars;
		return this.modelPars;
	}
	
	public PyLaiaTrainCtcPars getTrainCtcPars() {
		PyLaiaTrainCtcPars trainPars = new PyLaiaTrainCtcPars();
		insertParametersFromText(trainParsText, trainPars);
		this.trainPars = trainPars;
		return this.trainPars;
	}
	
	private ParameterMap insertParametersFromText(Text text, ParameterMap parMap) {
		for (String line : text.getText().split("\n")) {
			parMap.addParameterFromSingleLine(line, " ");
		}
		return parMap;
	}
	
	private void updateUi() {
		batchSizeText.setText(""+batchSize);
		
		// preprocess pars:
		deslopeCheck.setSelection(textFeatsCfg.isDeslope());
		deslantCheck.setSelection(textFeatsCfg.isDeslant());
		stretchCheck.setSelection(textFeatsCfg.isStretch());
		enhanceCheck.setSelection(textFeatsCfg.isEnh());
		enhWinText.setText(""+textFeatsCfg.getEnh_win());
		enhPrmText.setText(""+textFeatsCfg.getEnh_prm());
		normHeightText.setText(""+textFeatsCfg.getNormheight());
		normxHeightText.setText(""+textFeatsCfg.getNormxheight());
		momentnormCheck.setSelection(textFeatsCfg.isMomentnorm());
		fpgramCheck.setSelection(textFeatsCfg.isFpgram());
		fcontourCheck.setSelection(textFeatsCfg.isFcontour());
		fcontour_dilateText.setText(""+textFeatsCfg.getFcontour_dilate());
		paddingText.setText(""+textFeatsCfg.getPadding());
		
		// model pars:
		modelParsText.setText(modelPars.toSimpleStringLineByLine());
		
		// train pars:
		trainParsText.setText(trainPars.toSimpleStringLineByLine());
	}
	
	private void createContent() {
		this.setLayout(new GridLayout(1, false));
		batchSizeText = new LabeledText(this, "Batch size: ");
		batchSizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Fonts.setBoldFont(batchSizeText.getLabel());
		
		SashForm subC = new SashForm(this, 0);
		subC.setLayout(SWTUtil.createGridLayout(3, false, 0, 0));
		subC.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createPreprocessUi(subC);
		createModelParsUi(subC);
		createTrainParsUi(subC);
		
		subC.setWeights(new int[] {1, 1, 1});
		
		updateUi();
	}
	
	private void createModelParsUi(Composite parent) {
		modelParsGroup = new Group(parent, 0);
		Fonts.setBoldFont(modelParsGroup);
		modelParsGroup.setText("Model");
		modelParsGroup.setLayout(new GridLayout(1, false));
		modelParsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		modelParsText = new Text(modelParsGroup, SWT.MULTI | SWT.V_SCROLL);
		modelParsText.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void createTrainParsUi(Composite parent) {
		trainParsGroup = new Group(parent, 0);
		Fonts.setBoldFont(trainParsGroup);
		trainParsGroup.setText("Training");
		trainParsGroup.setLayout(new GridLayout(1, false));
		trainParsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		trainParsText = new Text(trainParsGroup, SWT.MULTI | SWT.V_SCROLL);
		trainParsText.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void createPreprocessUi(Composite parent) {
		preprocGroup = new Group(parent, 0);
		Fonts.setBoldFont(preprocGroup);
		preprocGroup.setText("Preprocessing");
		preprocGroup.setLayout(new GridLayout(1, false));
		preprocGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		deslopeCheck = new Button(preprocGroup, SWT.CHECK);
		deslopeCheck.setText("Deslope");
		deslopeCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		deslantCheck = new Button(preprocGroup, SWT.CHECK);
		deslantCheck.setText("Deslant");
		deslantCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stretchCheck = new Button(preprocGroup, SWT.CHECK);
		stretchCheck.setText("Stretch");
		stretchCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhanceCheck = new Button(preprocGroup, SWT.CHECK);
		enhanceCheck.setText("Enhance");
		enhanceCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhWinText = new LabeledText(preprocGroup, "Enhance window: ");
		enhWinText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhPrmText = new LabeledText(preprocGroup, "Sauvola enhancement parameter: ");
		enhPrmText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normHeightText = new LabeledText(preprocGroup, "Norm-height: ");
		normHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normxHeightText = new LabeledText(preprocGroup, "Norm-xheight: ");
		normxHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		momentnormCheck = new Button(preprocGroup, SWT.CHECK);
		momentnormCheck.setText("Use moment normalization");
		momentnormCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fpgramCheck = new Button(preprocGroup, SWT.CHECK);
		fpgramCheck.setText("Use feature parallelograms");
		fpgramCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontourCheck = new Button(preprocGroup, SWT.CHECK);
		fcontourCheck.setText("Use features surrounding polygon");
		fcontourCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontour_dilateText = new LabeledText(preprocGroup, "Dilate for features surrounding polygon: ");
		fcontour_dilateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		paddingText = new LabeledText(preprocGroup, "Left/right padding: ");
		paddingText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

}
