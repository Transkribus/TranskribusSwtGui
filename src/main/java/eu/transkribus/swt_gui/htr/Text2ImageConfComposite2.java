package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.mihalis.opal.propertyTable.PTProperty;
import org.mihalis.opal.propertyTable.PropertyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.GsonUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;

public class Text2ImageConfComposite2 extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(Text2ImageConfComposite2.class);
	
	LabeledText epochsTxt;
	LabeledText subsetsTxt;
	Button respectLineBreaksCheck;
	
	LabeledText trainSizePerEpochTxt;
	CitlabNoiseParamCombo noiseCmb;
	LearningRateCombo learningRateCombo;
	
	HtrModelChooserButton baseModelBtn;
	
	Text advancedParameters;
	PropertyTable advancedPropertiesTable;
	
//	CurrentDocPagesSelector currentDocPagesSelector;
	
	public Text2ImageConfComposite2(Composite parent, int flags) {
		super(parent, flags);
		this.setLayout(new GridLayout(2, false));
		
//		currentDocPagesSelector = new CurrentDocPagesSelector(this, 0, true, true, true);
//		currentDocPagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Composite baseModelCont = SWTUtil.createContainerComposite(this, 2, true);
		baseModelCont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label labelBaseModel = new Label(baseModelCont, 0);
		labelBaseModel.setText("Base model:");
		baseModelBtn = new HtrModelChooserButton(baseModelCont, true, null);
		baseModelBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		epochsTxt = new LabeledText(this, "Epochs per iteration: ", true);
		epochsTxt.setText(CitLabSemiSupervisedHtrTrainConfig.DEFAULT_TRAINING_EPOCHS);
		epochsTxt.setToolTipText("A series of training epochs per iteration divided by semicolons - enter an empty string for no training at all");
		epochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		subsetsTxt = new LabeledText(this, "Subsets: ", true);
		subsetsTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_SUBSETS);
		subsetsTxt.setToolTipText("The number of subsets the document is divided into - max is the number of pages");
		subsetsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite removeLbCont = SWTUtil.createContainerComposite(this, 2, true);
		removeLbCont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(removeLbCont, 0);
		respectLineBreaksCheck = new Button(removeLbCont, SWT.CHECK);
		respectLineBreaksCheck.setSelection(true);
		respectLineBreaksCheck.setText("Respect line breaks of input text");
		respectLineBreaksCheck.setToolTipText("Check to respect line breaks in the input text");
		respectLineBreaksCheck.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite noiseCont = SWTUtil.createContainerComposite(this, 2, true);
		noiseCont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label noiseLbl = new Label(noiseCont, SWT.NONE);
		noiseLbl.setText("Noise:");
		noiseCmb = new CitlabNoiseParamCombo(noiseCont, 0);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		trainSizePerEpochTxt = new LabeledText(this, "Train size per epoch:", true);
		trainSizePerEpochTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_TRAIN_SIZE_PER_EPOCH);
		trainSizePerEpochTxt.setToolTipText("The number of lines per epoch that is used for training");
		trainSizePerEpochTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite lrCont = SWTUtil.createContainerComposite(this, 2, true);
		lrCont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label lrLbl = new Label(lrCont, SWT.NONE);
		lrLbl.setText("Learning rate:");
		learningRateCombo = new LearningRateCombo(lrCont, 0);
		learningRateCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		new Label(this, 0);
		/*
		numberOfThreadsTxt = new LabeledText(this, "Number of threads:", true);
		numberOfThreadsTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_NUMBER_OF_THREADS);
		numberOfThreadsTxt.setToolTipText("The number of threads that is used on the server to process this job");
		numberOfThreadsTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		*/
//		thresholdTxt = new LabeledText(this, "Threshold:", true);
//		thresholdTxt.setText(""+CitLabSemiSupervisedHtrTrainConfig.DEFAULT_NUMBER_OF_THREADS);
//		thresholdTxt.setToolTipText("Threshold for alignment. Typically between 0.01 and 0.05 - see https://read02.uibk.ac.at/wiki/index.php/Text2ImageParameters");
//		thresholdTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		initAdditionalParametersUi();
	}
		
	private void initAdditionalParametersUi() {
		Composite container = new Composite(this, 0);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		Link help = new Link(container, 0);
		String t2iParsLink="https://transkribus.eu/wiki/index.php/Text2ImageParameters";
		help.setText("Advanced Parameters, see <a href=\""+t2iParsLink+"\">"+t2iParsLink+"</a>");
		help.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				try {
					org.eclipse.swt.program.Program.launch(e.text);
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});

		advancedParameters = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	    advancedParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		advancedParameters.setToolTipText("Advanced parameters for Text2Image - use key=value format in each line!");
		advancedParameters.setText(	"thresh=0.01\n"+
										"hyphen=null\n"+
										"hyphen_lang=null\n"+
										"skip_word=null\n"+
										"skip_bl=null\n"+
										"jump_bl=null\n"+
										"best_pathes=Infinity\n"
				);
	}
	
	@Deprecated
    private static PropertyTable buildPropertyTable(Composite parent, /*boolean showButton, boolean showAsCategory,*/ boolean showDescription) {
        PropertyTable table = new PropertyTable(parent, SWT.CHECK);

//        if (showButton) {
//                table.showButtons();
//        } else {
//                table.hideButtons();
//        }
//
//        if (showAsCategory) {
//                table.viewAsCategories();
//        } else {
//                table.viewAsFlatList();
//        }
        
        
        table.hideButtons();
        table.viewAsFlatList();

        if (showDescription) {
                table.showDescription();
        } else {
                table.hideDescription();
        }
        
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_HYPHEN, null);
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_HYPHEN_LANG, null);
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_JUMP_BASELINE, null);
//        
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_SKIP_WORD, "3.0");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_SKIP_BASELINE, "0.3");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_BEST_PATHES, "200.0");
//        propsT2I = addT2IPropertyFromJobProps(propsT2I, Key.T2I_THRESH, "0.01");
        
        PTProperty hyphenProperty = new PTProperty("hyphen", "hyphen", "Description for identifier", "value");
        
        table.addProperty(new PTProperty("hyphen", "hyphen", "Description for identifier", "value"));

//        table.addProperty(new PTProperty("id", "Identifier", "Description for identifier", "My id")).setCategory("General");
//        table.addProperty(new PTProperty("text", "Description", "Description for the description field", "blahblah...")).setCategory("General");
//        table.addProperty(new PTProperty("url", "URL:", "This is a nice <b>URL</b>", "http://www.google.com").setCategory("General")).setEditor(new PTURLEditor());
//        table.addProperty(new PTProperty("password", "Password", "Enter your <i>password</i> and keep it secret...", "password")).setCategory("General").setEditor(new PTPasswordEditor());
//
//        table.addProperty(new PTProperty("int", "An integer", "Type any integer", "123")).setCategory("Number").setEditor(new PTIntegerEditor());
//        table.addProperty(new PTProperty("float", "A float", "Type any float", "123.45")).setCategory("Number").setEditor(new PTFloatEditor());
//        table.addProperty(new PTProperty("spinner", "Another integer", "Use a spinner to enter an integer")).setCategory("Number").setEditor(new PTSpinnerEditor(0, 100));
//
//        table.addProperty(new PTProperty("directory", "Directory", "Select a directory")).setCategory("Directory/File").setEditor(new PTDirectoryEditor());
//        table.addProperty(new PTProperty("file", "File", "Select a file")).setCategory("Directory/File").setEditor(new PTFileEditor());
//
//        table.addProperty(new PTProperty("comboReadOnly", "Combo (read-only)", "A simple combo with seasons")).setCategory("Combo").setEditor(new PTComboEditor(true, new Object[] {"Spring", "Summer", "Autumn", "Winter"} ) );
//        table.addProperty(new PTProperty("combo", "Combo", "A combo that is not read-only")).setCategory("Combo").setEditor(new PTComboEditor("Value 1", "Value 2", "Value 3"));
//
//        table.addProperty(new PTProperty("cb", "Checkbox", "A checkbox")).setCategory("Checkbox").setEditor(new PTCheckboxEditor()).setCategory("Checkbox");
////        table.addProperty(new PTProperty("cb", "Checkbox", "A checkboxxx")).setCategory("Checkbox").setEditor(new PTCheckboxEditor()).setCategory("Checkbox");
//        table.addProperty(new PTProperty("cb2", "Checkbox (disabled)", "A disabled checkbox...")).setEditor(new PTCheckboxEditor()).setCategory("Checkbox").setEnabled(false);
//
//        table.addProperty(new PTProperty("color", "Color", "Pick it !")).setCategory("Misc").setEditor(new PTColorEditor());
//        table.addProperty(new PTProperty("font", "Font", "Pick again my friend")).setEditor(new PTFontEditor()).setCategory("Misc");
//        table.addProperty(new PTProperty("dimension", "Dimension", "A dimension is composed of a width and a height")).setCategory("Misc").setEditor(new PTDimensionEditor());
//        table.addProperty(new PTProperty("rectangle", "Rectangle", "A rectangle is composed of a position (x,y) and a dimension(width,height)")).setCategory("Misc").setEditor(new PTRectangleEditor());
//        table.addProperty(new PTProperty("inset", "Inset", "An inset is composed of the following fields:top,left,bottom,right)")).setCategory("Misc").setEditor(new PTInsetsEditor());
//        table.addProperty(new PTProperty("date", "Date", "Well, is there something more to say ?")).setCategory("Misc").setEditor(new PTDateEditor());
        
//        table.getProperties().put("cb", "true");
        
//        for (PTProperty p : table.getPropertiesAsList()) {
//        	if (p.getName().equals("cb2")) {
//        		p.setValue(true);
//        	}
//        }
        

        return table;
    }
		
	public CitLabSemiSupervisedHtrTrainConfig getConfig() throws IOException {
		CitLabSemiSupervisedHtrTrainConfig config = new CitLabSemiSupervisedHtrTrainConfig();
		
		// add current document as train document:
//		Storage store = Storage.getInstance();
//		if (store.isRemoteDoc()) {
//			logger.debug("pages str: "+currentDocPagesSelector.getPagesStr());
//			config.getTrain().add(DocumentSelectionDescriptor.fromDocAndPagesStr(store.getDoc(), currentDocPagesSelector.getPagesStr()));
//			config.setColId(store.getCurrentDocumentCollectionId());
//		} else {
//			throw new IOException("No remote document loaded!");
//		}
		
		if (baseModelBtn.getModel() != null) {
			config.setBaseModelId(baseModelBtn.getModel().getHtrId());
		} else {
			throw new IOException("No base model chosen!");
		}
		
		if (CitLabSemiSupervisedHtrTrainConfig.isValidTrainingEpochsString(epochsTxt.getText())) {
			config.setTrainEpochs(epochsTxt.getText());
		} else {
			throw new IOException("Cannot parse epochs string: "+epochsTxt.getText());
		}
		
		if (subsetsTxt.toIntVal()!=null) {
			config.setSubSets(subsetsTxt.toIntVal());
		} else {
			throw new IOException("Cannot parse subsampling parameter: "+epochsTxt.getText());
		}
		
		boolean removeLineBreaks = !respectLineBreaksCheck.getSelection();
		config.setRemoveLineBreaks(removeLineBreaks);
		
		int trainSizePerEpoch = CitLabSemiSupervisedHtrTrainConfig.DEFAULT_TRAIN_SIZE_PER_EPOCH;
		if(trainSizePerEpochTxt.getText() != null) {
			try {
				trainSizePerEpoch = Integer.parseInt(trainSizePerEpochTxt.getText());
			} catch (NumberFormatException nfe) {
				throw new IOException("Invalid value for train size: " + trainSizePerEpochTxt.getText(), nfe);
			}
		}
		config.setTrainSizePerEpoch(trainSizePerEpoch);
		
		config.setNoise(noiseCmb.getNoise());
		
		try {
			logger.debug("lr = "+learningRateCombo.getLearningRate());
			Double.valueOf(learningRateCombo.getLearningRate());
			config.setLearningRate(learningRateCombo.getLearningRate());
		} catch (NumberFormatException e) {
			throw new IOException("Cannot parse learning rate: "+learningRateCombo.getLearningRate());
		}
		
		try {
			Properties props = CoreUtils.readPropertiesFromString(advancedParameters.getText());
			config.setJsonProps(GsonUtil.toJson(GsonUtil.toJson(props)));
		} catch (IOException e) {
			throw new IOException("Cannot parse advanced properties - use key=value format for each line!");
		}

		return config;
	}
	
	public static void main(String[] args) throws IOException {
		Properties props = CoreUtils.readPropertiesFromString("key1=value1\nkey2=value2");
		System.out.println(GsonUtil.toJson(props));
	}

}
