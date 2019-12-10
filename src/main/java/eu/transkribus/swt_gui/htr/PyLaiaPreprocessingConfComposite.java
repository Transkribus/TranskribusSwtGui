package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.TextFeatsCfg;
import eu.transkribus.swt.util.LabeledText;

public class PyLaiaPreprocessingConfComposite extends Composite {
	TextFeatsCfg cfg = new TextFeatsCfg();
	
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
	
	public PyLaiaPreprocessingConfComposite(Composite parent, TextFeatsCfg cfg) {
		super(parent, 0);
		
		this.cfg = cfg;
		if (this.cfg == null) {
			this.cfg = new TextFeatsCfg();	
		}
		
		createContent();
	}
	
	public TextFeatsCfg getCurrentConfig() {
		if (this.cfg == null) {
			this.cfg = new TextFeatsCfg();
		}
		
		cfg.setDeslope(deslopeCheck.getSelection());
		cfg.setDeslant(deslantCheck.getSelection());
		cfg.setStretch(stretchCheck.getSelection());
		cfg.setEnh(enhanceCheck.getSelection());
		
		cfg.setEnh_win(enhWinText.toIntVal(cfg.getEnh_win()));
		cfg.setEnh_prm(enhPrmText.toDoubleVal(cfg.getEnh_prm()));
		cfg.setNormheight(normHeightText.toIntVal(cfg.getNormheight()));
		cfg.setNormxheight(normxHeightText.toIntVal(cfg.getNormxheight()));
		
		cfg.setMomentnorm(momentnormCheck.getSelection());
		cfg.setFpgram(fpgramCheck.getSelection());
		cfg.setFcontour(fcontourCheck.getSelection());
		
		cfg.setFcontour_dilate(fcontour_dilateText.toIntVal(cfg.getFcontour_dilate()));
		cfg.setPadding(paddingText.toIntVal(cfg.getPadding()));
		
		return cfg;
	}
	
	private void updateUi() {
		deslopeCheck.setSelection(cfg.isDeslope());
		deslantCheck.setSelection(cfg.isDeslant());
		stretchCheck.setSelection(cfg.isStretch());
		enhanceCheck.setSelection(cfg.isEnh());
		enhWinText.setText(""+cfg.getEnh_win());
		enhPrmText.setText(""+cfg.getEnh_prm());
		normHeightText.setText(""+cfg.getNormheight());
		normxHeightText.setText(""+cfg.getNormxheight());
		momentnormCheck.setSelection(cfg.isMomentnorm());
		fpgramCheck.setSelection(cfg.isFpgram());
		fcontourCheck.setSelection(cfg.isFcontour());
		fcontour_dilateText.setText(""+cfg.getFcontour_dilate());
		paddingText.setText(""+cfg.getPadding());
	}
	
	private void createContent() {
		this.setLayout(new GridLayout(1, false));
		
		deslopeCheck = new Button(this, SWT.CHECK);
		deslopeCheck.setText("Deslope");
		deslopeCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		deslantCheck = new Button(this, SWT.CHECK);
		deslantCheck.setText("Deslant");
		deslantCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stretchCheck = new Button(this, SWT.CHECK);
		stretchCheck.setText("Stretch");
		stretchCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhanceCheck = new Button(this, SWT.CHECK);
		enhanceCheck.setText("Enhance");
		enhanceCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhWinText = new LabeledText(this, "Enhance window: ");
		enhWinText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		enhPrmText = new LabeledText(this, "Sauvola enhancement parameter: ");
		enhPrmText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normHeightText = new LabeledText(this, "Norm-height: ");
		normHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		normxHeightText = new LabeledText(this, "Norm-xheight: ");
		normxHeightText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		momentnormCheck = new Button(this, SWT.CHECK);
		momentnormCheck.setText("Use moment normalization");
		momentnormCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fpgramCheck = new Button(this, SWT.CHECK);
		fpgramCheck.setText("Use feature parallelograms");
		fpgramCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontourCheck = new Button(this, SWT.CHECK);
		fcontourCheck.setText("Use features surrounding polygon");
		fcontourCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fcontour_dilateText = new LabeledText(this, "Dilate for features surrounding polygon: ");
		fcontour_dilateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		paddingText = new LabeledText(this, "Left/right padding: ");
		paddingText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		updateUi();
	}

}
