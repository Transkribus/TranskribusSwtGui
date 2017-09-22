package eu.transkribus.swt_gui.htr;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.swt.util.SWTUtil;

public class CitlabNoiseParamCombo extends Composite {
	private final static String[] NOISE_OPTIONS = new String[] { "no", "preproc", "net", "both" };
	
	Combo noiseCmb;

	public CitlabNoiseParamCombo(Composite parent, int style) {
		super(parent, style);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));

		noiseCmb = new Combo(this, SWT.READ_ONLY);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		noiseCmb.setItems(NOISE_OPTIONS);
		
		setDefault();
	}
	
	public void setDefault() {
		for (int i=0; i<NOISE_OPTIONS.length; ++i) {
			if (NOISE_OPTIONS[i].equals(CitLabSemiSupervisedHtrTrainConfig.DEFAULT_NOISE)) {
				noiseCmb.select(i);
				return;
			}
		}
		noiseCmb.select(3); // set to 'both' element if not found ...
	}
	
	public String getNoise() {
		return noiseCmb.getText();
	}

}
