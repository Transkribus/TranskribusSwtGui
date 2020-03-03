package eu.transkribus.swt_gui.htr;

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
//	int batchSize = PyLaiaTrainCtcPars.DEFAULT_BATCH_SIZE;
	TextFeatsCfg textFeatsCfg = new TextFeatsCfg();
	PyLaiaCreateModelPars modelPars;
	PyLaiaTrainCtcPars trainPars;
	
//	LabeledText batchSizeText;
	
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
	LabeledText maxwidthText;
	
	Group modelParsGroup;
	Text modelParsText;
	
	Group trainParsGroup;
	Text trainParsText;
	
	public PyLaiaAdvancedConfComposite(Composite parent, /*int batchSize,*/ TextFeatsCfg textFeatsCfg, PyLaiaCreateModelPars modelPars, PyLaiaTrainCtcPars trainPars) {
		super(parent, 0);

//		this.batchSize = batchSize;
		this.textFeatsCfg = textFeatsCfg == null ? new TextFeatsCfg() : textFeatsCfg;
		this.modelPars = modelPars == null ? PyLaiaCreateModelPars.getDefault() : modelPars;
		this.trainPars = trainPars == null ? PyLaiaTrainCtcPars.getDefault() : trainPars;
		
		// remove pars that are explicitly set via custom UI fields
//		this.trainPars.remove("--batch_size"); // set via custom field
		this.trainPars.remove("--max_nondecreasing_epochs"); // set via main UI
		this.trainPars.remove("--max_epochs"); // set via main UI
		this.trainPars.remove("--learning_rate"); // set via main UI

		// those are the fixed parameters that cannot be changed by the user (will be set to default value at server):
		this.modelPars.remove("--fixed_input_height"); // determined via fixed height par in textFeatsCfg
		this.modelPars.remove("--logging_level");
		this.modelPars.remove("--logging_also_to_stderr");
		this.modelPars.remove("--logging_file");
		this.modelPars.remove("--logging_config");
		this.modelPars.remove("--logging_overwrite");
		this.modelPars.remove("--train_path");
		this.modelPars.remove("--model_filename");
		this.modelPars.remove("--print_args");
		
		this.trainPars.remove("--logging_level");
		this.trainPars.remove("--logging_also_to_stderr");
		this.trainPars.remove("--logging_file");
		this.trainPars.remove("--logging_config");
		this.trainPars.remove("--logging_overwrite");
		this.trainPars.remove("--train_path");
		this.trainPars.remove("--show_progress_bar");
		this.trainPars.remove("--delimiters");
		this.trainPars.remove("--print_args");
		
		createContent();
	}
	
//	public int getCurrentBatchSize() throws IOException {
//		try {
//			batchSize = Integer.parseInt(batchSizeText.getText());
//			if (batchSize <= 0 || batchSize>50) {
//				throw new IOException("Batch size not in range 1-50");
//			}
//			return batchSize;
//		} catch (NumberFormatException e) {
//			throw new IOException("Not a valid batch size: "+batchSizeText.getText());
//		}
//	}
	
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
		textFeatsCfg.setMaxwidth(maxwidthText.toIntVal(textFeatsCfg.getMaxwidth()));
		
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
//		batchSizeText.setText(""+batchSize);
		
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
		maxwidthText.setText(""+textFeatsCfg.getMaxwidth());
		
		// model pars:
		modelParsText.setText(modelPars.toSimpleStringLineByLine());
		
		// train pars:
		trainParsText.setText(trainPars.toSimpleStringLineByLine());
	}
	
	private void createContent() {
		this.setLayout(new GridLayout(1, false));
//		batchSizeText = new LabeledText(this, "Batch size: ");
//		batchSizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		Fonts.setBoldFont(batchSizeText.getLabel());
		
		SashForm subC = new SashForm(this, 0);
		subC.setLayout(SWTUtil.createGridLayout(3, false, 0, 0));
		subC.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		createPreprocessUi(subC);
		createModelParsUi(subC);
		createTrainParsUi(subC);
		
		subC.setWeights(new int[] {3, 3, 2});
		
		updateUi();
	}
	
	private void createModelParsUi(Composite parent) {
		modelParsGroup = new Group(parent, 0);
		Fonts.setBoldFont(modelParsGroup);
		modelParsGroup.setText("Model");
		modelParsGroup.setLayout(new GridLayout(1, false));
		modelParsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		modelParsText = new Text(modelParsGroup, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		modelParsText.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
	
	private void createTrainParsUi(Composite parent) {
		trainParsGroup = new Group(parent, 0);
		Fonts.setBoldFont(trainParsGroup);
		trainParsGroup.setText("Training");
		trainParsGroup.setLayout(new GridLayout(1, false));
		trainParsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		trainParsText = new Text(trainParsGroup, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
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
		
		enhWinText = new LabeledText(preprocGroup, "Enhance window size: ");
		enhWinText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhPrmText = new LabeledText(preprocGroup, "Sauvola enhancement parameter: ");
		enhPrmText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normHeightText = new LabeledText(preprocGroup, "Line height: ");
		normHeightText.setToolTipText("Normalized height of extracted lines. Set to 0 for no normalization.");
		normHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normxHeightText = new LabeledText(preprocGroup, "Line x-height: ");
		normxHeightText.setToolTipText("Normalized x-height (= height - descender and cap height) of extracted lines. Set to 0 for no normalization.");
		normxHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		momentnormCheck = new Button(preprocGroup, SWT.CHECK);
		momentnormCheck.setText("Moment normalization");
		momentnormCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fpgramCheck = new Button(preprocGroup, SWT.CHECK);
		fpgramCheck.setText("Features parallelogram");
		fpgramCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontourCheck = new Button(preprocGroup, SWT.CHECK);
		fcontourCheck.setText("Features surrounding polygon");
		fcontourCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontour_dilateText = new LabeledText(preprocGroup, "Features surrounding polygon dilate: ");
		fcontour_dilateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		paddingText = new LabeledText(preprocGroup, "Left/right padding: ");
		paddingText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		maxwidthText = new LabeledText(preprocGroup, "Max width: ");
		maxwidthText.setToolTipText("Maximum width of the output line - warning: exceeding pixels are cut off on the right side!");
		maxwidthText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
	}

}
