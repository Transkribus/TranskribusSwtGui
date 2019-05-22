package eu.transkribus.swt_gui.la;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.htr.HtrModelChooserButton;

public class Text2ImageSimplifiedConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(Text2ImageSimplifiedConfComposite.class);
	
	Button removeLineBreaksBtn;
	Button performLaBtn;
	HtrModelChooserButton baseModelBtn;
	LabeledText thresholdText;
	
	public static class Text2ImageConf {
		public TrpHtr model=null;
		public boolean performLa=true;
		public boolean removeLineBreaks=false;
		public double threshold=0.0d;
		
		public Text2ImageConf() {}
		public Text2ImageConf(TrpHtr model, boolean performLa, boolean removeLineBreaks, double threshold) {
			super();
			this.model = model;
			this.performLa = performLa;
			this.removeLineBreaks = removeLineBreaks;
			this.threshold = threshold;
		}
		@Override
		public String toString() {
			return "Text2ImageConf [model=" + model + ", performLa=" + performLa + ", removeLineBreaks="
					+ removeLineBreaks + ", threshold=" + threshold + "]";
		}
	}
	
	public Text2ImageSimplifiedConfComposite(Composite parent, int flags, Text2ImageConf conf) {
		super(parent, flags);
		this.setLayout(new GridLayout(2, false));
		
//		Label modelLabel = new Label(this, 0);
//		modelLabel.setText("Base model");
//		modelLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		baseModelBtn = new HtrModelChooserButton(this, null, "Base model");
		baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		performLaBtn = new Button(this, SWT.CHECK);
		performLaBtn.setText("Perform Layout Analysis");
		performLaBtn.setToolTipText("Perform a new layout analysis for text alignment - uncheck to use the existing layout");
		performLaBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		removeLineBreaksBtn = new Button(this, SWT.CHECK);
		removeLineBreaksBtn.setText("Remove Line Breaks");
		removeLineBreaksBtn.setToolTipText("Check to disrespect linebreaks of the input text during text alignment");
		removeLineBreaksBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		thresholdText = new LabeledText(this, "Threshold:");
		thresholdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		thresholdText.setToolTipText("Threshold for text alignment. If the confidence of a text-to-image\n" + 
				"alignment is above this threshold, an alignment is done (default = 0.0). A\n" + 
				"good value is between 0.01 and 0.05. Note that the confidence is stored\n" + 
				"in the pageXML anyway, so deleting text alignments with low confidence\n" + 
				"can also be made later.");
		
		setUiFromGivenConf(conf);
	}
	
	private void setUiFromGivenConf(Text2ImageConf conf) {
		if (conf == null) {
			conf = new Text2ImageConf();
		}
		
		baseModelBtn.setModel(conf.model);
		performLaBtn.setSelection(conf.performLa);
		removeLineBreaksBtn.setSelection(conf.removeLineBreaks);
		thresholdText.setText(""+conf.threshold);
	}
	
	public Text2ImageConf getConfigFromUi() {
		Text2ImageConf conf = new Text2ImageConf();
		conf.model = baseModelBtn.getModel();
		conf.performLa = performLaBtn.getSelection();
		conf.removeLineBreaks = removeLineBreaksBtn.getSelection();
		
		try {
			conf.threshold = Double.parseDouble(thresholdText.getText());
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid threshold", "Invalid threshold value: "+thresholdText.getText()+" - setting to 0.0");
			thresholdText.setText("0.0");
			conf.threshold = 0.0d;
		}
		
		return conf;
	}
		

}
