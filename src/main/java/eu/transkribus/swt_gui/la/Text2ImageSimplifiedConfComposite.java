package eu.transkribus.swt_gui.la;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.htr.HtrModelChooserButton;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class Text2ImageSimplifiedConfComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(Text2ImageSimplifiedConfComposite.class);
	
	Button removeLineBreaksBtn;
	Button performLaBtn;
	HtrModelChooserButton baseModelBtn;
//	LabeledText thresholdText;
	Label thresholdLabel;
	Combo thresholdCombo;
	Combo editStatusCombo;
//	Combo thresholdText;
	CurrentTranscriptOrCurrentDocPagesSelector pagesSelector;
	
	public static class Text2ImageConf {
		public TrpHtr model=null;
		public boolean performLa=true;
		public boolean removeLineBreaks=false;
//		public String versionsStatus=null;
		public EditStatus editStatus=null;
		public double threshold=0.0d;
		
		public boolean currentTranscript=true;
		public String pagesStr=null;
		
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
					+ removeLineBreaks + ", editStatus=" + editStatus + ", threshold=" + threshold
					+ ", currentTranscript=" + currentTranscript + ", pagesStr=" + pagesStr + "]";
		}
	}
	
	public Text2ImageSimplifiedConfComposite(Composite parent, int flags, Text2ImageConf conf) {
		super(parent, flags);
		int nCols = 2;
		this.setLayout(new GridLayout(nCols, false));
		
		
//		Label modelLabel = new Label(this, 0);
//		modelLabel.setText("Base model");
//		modelLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		pagesSelector = new CurrentTranscriptOrCurrentDocPagesSelector(this, SWT.NONE, true,true);
		pagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));	
		
		baseModelBtn = new HtrModelChooserButton(this, null, "Base model: ");
		baseModelBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));
		
		performLaBtn = new Button(this, SWT.CHECK);
		performLaBtn.setText("Perform Layout Analysis");
		performLaBtn.setToolTipText("Perform a new layout analysis for text alignment - uncheck to use the existing layout");
		performLaBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		removeLineBreaksBtn = new Button(this, SWT.CHECK);
		removeLineBreaksBtn.setText("Remove Line Breaks");
		removeLineBreaksBtn.setToolTipText("Check to disrespect linebreaks of the input text during text alignment");
		removeLineBreaksBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite editStatusComp = new Composite(this, 0);
		editStatusComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));
		editStatusComp.setLayout(SWTUtil.createGridLayout(nCols, false, 0, 0));
		
		Label editStatusLbl = new Label(editStatusComp, 0);
		editStatusLbl.setText("Use versions with edit status: ");
		
		editStatusCombo = new Combo(editStatusComp, SWT.DROP_DOWN);
		
		List<String> stati = EnumUtils.stringsList(EditStatus.class);
		stati.add(0, "");
		editStatusCombo.setItems(stati.toArray(new String[0]));
		editStatusCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		editStatusCombo.setToolTipText("Use versions with this edit status for matching.\nIf empty, current version is used");
		editStatusCombo.setText("");
		
//		useNewVersionsBtn = new Button(this, SWT.CHECK);
//		useNewVersionsBtn.setText("Use 'New' versions");
//		useNewVersionsBtn.setToolTipText("Use versions with status 'New' instead of the current version for matching");
//		useNewVersionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));		

		Composite thresholdComp = new Composite(this, 0);
		thresholdComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, nCols, 1));
		thresholdComp.setLayout(SWTUtil.createGridLayout(nCols, false, 0, 0));
		
		thresholdLabel = new Label(thresholdComp, 0);
		thresholdLabel.setText("Threshold: ");
		
		String thresholdToolTip = "Threshold for text alignment. If the confidence of a text-to-image\n" + 
				"alignment is above this threshold, an alignment is done (default = 0.0). A\n" + 
				"good value is between 0.01 and 0.05. Note that the confidence is stored\n" + 
				"in the pageXML anyway, so deleting text alignments with low confidence\n" + 
				"can also be made later.";
		
		thresholdCombo = new Combo(thresholdComp, SWT.DROP_DOWN);
		thresholdCombo.setItems(new String[] {"0.0", "0.01", "0.05"} );
		thresholdCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		thresholdCombo.setToolTipText(thresholdToolTip);
		
//		thresholdText = new LabeledText(this, "Threshold:");
//		thresholdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		thresholdText.setToolTipText(thresholdToolTip);
		
		setUiFromGivenConf(conf);
	}
	
	private void setUiFromGivenConf(Text2ImageConf conf) {
		if (conf == null) {
			conf = new Text2ImageConf();
		}
		
		// do not update page selection as a different doc with different nr of pages could be loaded since last call...
//		pagesSelector.getCurrentTranscriptButton().setSelection(conf.currentTranscript);
//		pagesSelector.setPagesStr(conf.pagesStr);
		
		if (conf.editStatus != null) {
			editStatusCombo.setText(conf.editStatus.getStr());	
		}
		
		baseModelBtn.setModel(conf.model);
		performLaBtn.setSelection(conf.performLa);
		removeLineBreaksBtn.setSelection(conf.removeLineBreaks);
//		thresholdText.setText(""+conf.threshold);
		thresholdCombo.setText(""+conf.threshold);
	}
	
	public Text2ImageConf getConfigFromUi() {
		Text2ImageConf conf = new Text2ImageConf();
		
		conf.currentTranscript = pagesSelector.isCurrentTranscript();
		conf.pagesStr = pagesSelector.getPagesStr();
		
		conf.model = baseModelBtn.getModel();
		conf.performLa = performLaBtn.getSelection();
		conf.removeLineBreaks = removeLineBreaksBtn.getSelection();
		
		conf.editStatus = null;
		if (!StringUtils.isEmpty(editStatusCombo.getText())) {
			try {
				conf.editStatus = EditStatus.fromString(editStatusCombo.getText());
			} catch (Exception e) {
				DialogUtil.showErrorMessageBox(getShell(), "Invalid Edit Status", "Invalid Edit Status: "+editStatusCombo.getText()+" - skipping!");
				conf.editStatus = null;
			}			
		}

		
		try {
			conf.threshold = Double.parseDouble(thresholdCombo.getText());
		} catch (Exception e) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid threshold", "Invalid threshold value: "+thresholdCombo.getText()+" - setting to 0.0");
			thresholdCombo.setText("0.0");
			conf.threshold = 0.0d;
		}
		
		return conf;
	}
		

}
