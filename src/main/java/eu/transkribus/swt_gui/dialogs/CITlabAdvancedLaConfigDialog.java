package eu.transkribus.swt_gui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.LaCITlabUtils;
import eu.transkribus.core.util.LaCITlabUtils.RotScheme;
import eu.transkribus.core.util.LaCITlabUtils.SepScheme;

public class CITlabAdvancedLaConfigDialog extends ALaConfigDialog {

	private final static String PRESET_NET_NAME = "Preset";
	
	private Combo neuralNetCombo;
	private Button rotSchemeHom, rotSchemeHet, sepSchemeAlways, sepSchemeNever;
	private Group settingsGroup, rotGroup, sepGroup;
	
	//TODO add real data from server in loadData()
	private final String konzilsProtNetLabel = "Konzilsprotokolle Greifswald";
	private final String konzilsProtNetName = "LA_alvermann1.pb";
	
	public CITlabAdvancedLaConfigDialog(Shell parent, ParameterMap parameters) {
		super(parent, parameters);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		GridLayout gl = new GridLayout(1, true);
		cont.setLayout(gl);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		
		Group netGroup = new Group(cont, SWT.NONE);
		netGroup.setText("Neural Net:");
		netGroup.setLayout(new GridLayout(1, false));
		netGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		
		//TODO recreate that as table when more items have to be shown
		neuralNetCombo = new Combo(netGroup, SWT.READ_ONLY);
		neuralNetCombo.setLayoutData(gd);
		
		settingsGroup = new Group(cont, SWT.NONE);
		settingsGroup.setText("Settings");
		settingsGroup.setLayout(new GridLayout(1, false));
		settingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		rotGroup = new Group(settingsGroup, SWT.NONE);
		rotGroup.setLayout(new GridLayout(2, false));
		rotGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		rotGroup.setText("Text orientation");
		rotSchemeHet = new Button(rotGroup, SWT.RADIO);
		rotSchemeHet.setText("Heterogenuous");
		
		rotSchemeHom = new Button(rotGroup, SWT.RADIO);
		rotSchemeHom.setText("Homogenuous");
		
		sepGroup = new Group(settingsGroup, SWT.NONE);
		sepGroup.setLayout(new GridLayout(2, false));
		sepGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sepGroup.setText("Use separators");
		sepSchemeNever = new Button(sepGroup, SWT.RADIO);
		sepSchemeNever.setText("Never");
		sepSchemeAlways = new Button(sepGroup, SWT.RADIO);
		sepSchemeAlways.setText("Always");
		
		neuralNetCombo.add(PRESET_NET_NAME);
		neuralNetCombo.select(0);
		
		loadData();
		
		applyParameterMapToDialog();
		
		addListeners();
		
		cont.pack();
		
		return cont;
	}

	private void addListeners() {
		neuralNetCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateGui();				
			}});
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout Analysis Configuration");
//		newShell.setMinimumSize(480, 480);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(480, 300);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.TITLE); //SWT.MAX | SWT.RESIZE
	}

	@Override
	protected void storeSelectionInParameterMap() {
		parameters = new ParameterMap();
		if(PRESET_NET_NAME.equals(getSelectedNet())) {
			parameters.addParameter(LaCITlabUtils.ROT_SCHEME_KEY, rotSchemeHet.getSelection() ? RotScheme.hetero : RotScheme.hom);
			parameters.addParameter(LaCITlabUtils.SEP_SCHEME_KEY, sepSchemeNever.getSelection() ? SepScheme.never : SepScheme.always);
		} else {
			//just set netName
			final String netName = konzilsProtNetLabel.equals(getSelectedNet()) ? konzilsProtNetName : getSelectedNet();
			parameters.addParameter(JobConst.PROP_MODELNAME, netName);
		}
	}
	
	private void loadData() {
		// TODO load available nets from server
		neuralNetCombo.add(konzilsProtNetLabel);		
	}

	@Override
	protected void applyParameterMapToDialog() {
		String netName = parameters.getParameterValue(JobConst.PROP_MODELNAME);
		if(netName == null || netName.equals(PRESET_NET_NAME)) {
			setSelectedNet(PRESET_NET_NAME);
		} else {
			
			//FIXME map internal name to label here for now
			if(netName.equals(konzilsProtNetName)) {
				netName = konzilsProtNetLabel;
			}
			
			setSelectedNet(netName);
		}
		
		final String rotScheme = parameters.getParameterValue(LaCITlabUtils.ROT_SCHEME_KEY);
		if(RotScheme.hetero.toString().equals(rotScheme)) {
			rotSchemeHet.setSelection(true);
		} else {
			//default
			rotSchemeHom.setSelection(true);
		}
		final String sepScheme = parameters.getParameterValue(LaCITlabUtils.SEP_SCHEME_KEY);
		if(SepScheme.always.toString().equals(sepScheme)) {
			sepSchemeAlways.setSelection(true);
		} else {
			//default
			sepSchemeNever.setSelection(true);
		}
		
		updateGui();
	}

	private void updateGui() {
		final String net = getSelectedNet();
		boolean isPreset = PRESET_NET_NAME.equals(net);
		setSettingsGroupEnabled(isPreset);
	}

	private void setSettingsGroupEnabled(boolean enabled) {
		for(Group g : new Group[]{settingsGroup, sepGroup, rotGroup}) {
			g.setEnabled(enabled);
		}
		for(Button b : new Button[]{rotSchemeHom, rotSchemeHet, sepSchemeAlways, sepSchemeNever}) {
			b.setEnabled(enabled);
		}
	}

	private void setSelectedNet(String netName) {
		neuralNetCombo.select(0);
		if(StringUtils.isEmpty(netName)) {
			return;
		}
		for(int i = 0; i < neuralNetCombo.getItems().length; i++) {
			if(neuralNetCombo.getItems()[i].equals(netName)) {
				neuralNetCombo.select(i);
				return;
			}
		}
	}
	private String getSelectedNet() {
		return neuralNetCombo.getText();		
	}
}
