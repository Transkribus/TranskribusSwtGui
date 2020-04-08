package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.LaCITlabUtils;
import eu.transkribus.core.util.LaCITlabUtils.RotScheme;
import eu.transkribus.core.util.LaCITlabUtils.SepScheme;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CITlabAdvancedLaConfigDialog extends ALaConfigDialog {

	final static String PRESET_NET_NAME = "Preset";
	final static String HELP_WIKI_PAGE = "https://transkribus.eu/wiki/index.php/Layout_Analysis_Help";
	
	final static String NEURAL_NET_LBL = "Neural Net:";
	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String DEFAULT_LBL = "Default";
	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String ROT_SCHEME_LBL = "Text orientation";	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String ROT_HET_LBL = "Heterogeneous";	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String ROT_HOM_LBL = "Homogeneous";	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	
	final static String SEP_SCHEME_LBL = "Use separators";	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String SEP_NEVER_LBL = "Never";	
	/**
	 * @deprecated moved to {@link LaCITlabUtils}
	 */
	final static String SEP_ALWAYS_LBL = "Always";	
	
	private Combo neuralNetCombo;
	private Button rotSchemeDef, rotSchemeHom, rotSchemeHet, sepSchemeDef, sepSchemeAlways, sepSchemeNever;
	private Group settingsGroup, rotGroup, sepGroup;
	private Button helpButton;
//	private Button deleteTextCheck;
	
	private List<LaModel> modelList;
	
	public CITlabAdvancedLaConfigDialog(Shell parent, ParameterMap parameters) {
		super(parent, parameters);
		modelList = new ArrayList<>(0);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		GridLayout gl = new GridLayout(1, true);
		cont.setLayout(gl);
		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		
		Group netGroup = new Group(cont, SWT.NONE);
		netGroup.setText(NEURAL_NET_LBL);
		netGroup.setLayout(new GridLayout(1, false));
		netGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
		
		//TODO recreate that as TableViewer when more items have to be shown
		neuralNetCombo = new Combo(netGroup, SWT.READ_ONLY);
		neuralNetCombo.setLayoutData(gd);
		
		settingsGroup = new Group(cont, SWT.NONE);
		settingsGroup.setText("Settings");
		settingsGroup.setLayout(new GridLayout(1, false));
		settingsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
//		deleteTextCheck = new Button(settingsGroup, SWT.CHECK);
//		deleteTextCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		deleteTextCheck.setText("Delete existing text");
//		deleteTextCheck.setToolTipText("If checked, existing text in the transcription will be deleted during layout analysis");
		
		rotGroup = new Group(settingsGroup, SWT.NONE);
		rotGroup.setLayout(new GridLayout(3, false));
		rotGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		rotGroup.setText(ROT_SCHEME_LBL);
		rotSchemeDef = new Button(rotGroup, SWT.RADIO);
		rotSchemeDef.setText(DEFAULT_LBL);
		rotSchemeDef.setToolTipText("Assume text is horizontal");
		rotSchemeHet = new Button(rotGroup, SWT.RADIO);
		rotSchemeHet.setText(ROT_HET_LBL);
		rotSchemeHet.setToolTipText("Mixture of text orientations");
		rotSchemeHom = new Button(rotGroup, SWT.RADIO);
		rotSchemeHom.setText(ROT_HOM_LBL);
		rotSchemeHom.setToolTipText("Entire text is equally oriented (0°, 90°, 180° or 270°)");
		
//		independentSettingsGrp = new Group(cont, SWT.NONE);
//		independentSettingsGrp.setText("Use Separators");
//		independentSettingsGrp.setLayout(new GridLayout(1, false));
//		independentSettingsGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
		
		sepGroup = new Group(settingsGroup, SWT.NONE);
		sepGroup.setLayout(new GridLayout(3, false));
		sepGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sepGroup.setText(SEP_SCHEME_LBL);
		sepSchemeDef = new Button(sepGroup, SWT.RADIO);
		sepSchemeDef.setText(DEFAULT_LBL);
		sepSchemeDef.setToolTipText("Only use separators if no regions are given");
		sepSchemeNever = new Button(sepGroup, SWT.RADIO);
		sepSchemeNever.setText(SEP_NEVER_LBL);
		sepSchemeNever.setToolTipText("Never use separators");
		sepSchemeAlways = new Button(sepGroup, SWT.RADIO);
		sepSchemeAlways.setText(SEP_ALWAYS_LBL);
		sepSchemeAlways.setToolTipText("Always use separators");
		
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
			}
		});
	}
	
	private void loadData() {
		//TODO get from Storage
		modelList = LaDataProvider.getModels();
		modelList.add(0, new LaModel(PRESET_NET_NAME, null));
		if(neuralNetCombo != null) {
			modelList.stream().forEach(m -> neuralNetCombo.add(m.getLabel()));
			neuralNetCombo.select(0);
		}
	}
	
	@Override
	protected void storeSelectionInParameterMap() {
		parameters = new ParameterMap();
		LaModel m = getSelectedNet();
		
//		if (deleteTextCheck.getSelection()) {
//			parameters.addParameter(LaCITlabUtils.LA_DELETE_SCHEME_KEY, "all");
//		}
		parameters.addParameter(LaCITlabUtils.LA_DELETE_SCHEME_KEY, null); // del-scheme gets set in job at server
		
		SepScheme sep = null;
		if(sepSchemeAlways.getSelection()) {
			sep = SepScheme.always;
		} else if (sepSchemeNever.getSelection()) {
			sep = SepScheme.never;
		}
		
		if(PRESET_NET_NAME.equals(m.getLabel())) {
			RotScheme rot = null;
			if(rotSchemeHet.getSelection()) {
				rot = RotScheme.het;
			} else if (rotSchemeHom.getSelection()) {
				rot = RotScheme.hom;
			}
			parameters.addParameter(LaCITlabUtils.ROT_SCHEME_KEY, rot);
			parameters.addParameter(LaCITlabUtils.SEP_SCHEME_KEY, sep);
		} else {
			parameters.addParameter(LaCITlabUtils.SEP_SCHEME_KEY, sep);
			//set net's filename to parameters
			parameters.addParameter(JobConst.PROP_MODELNAME, m.getFilename());
			
			if(LaDataProvider.postcardNetLabel.equals(m.getLabel())) {
				//FIXME quick fix to set the rotScheme parameter correctly for postcards net
				parameters.addParameter(LaCITlabUtils.ROT_SCHEME_KEY, RotScheme.het);
			}
		}
	}

	@Override
	protected void applyParameterMapToDialog() {
		LaModel model = resolveSelectedNetFromParameters();
		setSelectedNet(model);
		
//		String delScheme = parameters.getParameterValue(LaCITlabUtils.LA_DELETE_SCHEME_KEY);
//		if (StringUtils.equals(delScheme, "all")) {
//			deleteTextCheck.setSelection(true);
//		}
//		else {
//			deleteTextCheck.setSelection(false);
//		}
		
		final String rotScheme = parameters.getParameterValue(LaCITlabUtils.ROT_SCHEME_KEY);
		if(RotScheme.het.toString().equals(rotScheme)) {
			rotSchemeHet.setSelection(true);
		} else if (RotScheme.hom.toString().equals(rotScheme)){
			rotSchemeHom.setSelection(true);
		} else {
			//default
			rotSchemeDef.setSelection(true);
		}
		final String sepScheme = parameters.getParameterValue(LaCITlabUtils.SEP_SCHEME_KEY);
		if(SepScheme.always.toString().equals(sepScheme)) {
			sepSchemeAlways.setSelection(true);
		} else if (SepScheme.never.toString().equals(sepScheme)) {
			sepSchemeNever.setSelection(true);
		} else {
			//default
			sepSchemeDef.setSelection(true);
		}
		
		updateGui();
	}
	
	@Override
	public String getConfigInfoString() {
		if(CollectionUtils.isEmpty(modelList)) {
			loadData();
		}
		String infoStr = "";
		LaModel model = resolveSelectedNetFromParameters();
		infoStr += NEURAL_NET_LBL + " " + (model == null ? "Preset" : model.getLabel());
		
//		String delScheme = parameters.getParameterValue(LaCITlabUtils.LA_DELETE_SCHEME_KEY);
//		if (StringUtils.equals(delScheme, "all")) {
//			infoStr += "\nDeleting existing transcriptions";
//		}
//		else {
//			infoStr += "\nKeeping existing transcriptions";
//		}
		
		infoStr +=  "\n" + ROT_SCHEME_LBL + ": ";
		final String rotScheme = parameters.getParameterValue(LaCITlabUtils.ROT_SCHEME_KEY);
		if(RotScheme.het.toString().equals(rotScheme)) {
			infoStr += ROT_HET_LBL;
		} else if (RotScheme.hom.toString().equals(rotScheme)){
			infoStr += ROT_HOM_LBL;
		} else {
			//default
			infoStr += DEFAULT_LBL;
		}
		infoStr += "\n" + SEP_SCHEME_LBL + ": ";
		final String sepScheme = parameters.getParameterValue(LaCITlabUtils.SEP_SCHEME_KEY);
		if(SepScheme.always.toString().equals(sepScheme)) {
			infoStr += SEP_ALWAYS_LBL;
		} else if (SepScheme.never.toString().equals(sepScheme)) {
			infoStr += SEP_NEVER_LBL;
		} else {
			//default
			infoStr += DEFAULT_LBL;
		}
		
		return infoStr;
	}

	private void updateGui() {
		final LaModel m = getSelectedNet();
		boolean isPreset = PRESET_NET_NAME.equals(m.label);
		setSettingsGroupEnabled(isPreset);
	}

	private void setSettingsGroupEnabled(boolean enabled) {
		for(Group g : new Group[]{rotGroup}) {
			g.setEnabled(enabled);
		}
		for(Button b : new Button[]{rotSchemeDef, rotSchemeHom, rotSchemeHet}) {
			b.setEnabled(enabled);
		}
	}

	private void setSelectedNet(LaModel m) {
		neuralNetCombo.select(0);
		if(m == null) {
			return;
		}
		for(int i = 0; i < neuralNetCombo.getItems().length; i++) {
			if(neuralNetCombo.getItems()[i].equals(m.getLabel())) {
				neuralNetCombo.select(i);
				return;
			}
		}
	}
	/**
	 * Get selection from neuralNetCombo
	 * @return the selected LaModel
	 */
	private LaModel getSelectedNet() {
		final String label = neuralNetCombo.getText();
		for(LaModel m : modelList) {
			if(m.getLabel().equals(label)) {
				return m;
			}
		}
		return modelList.get(0);
	}
	
	/**
	 * Checks the current parameter map for the LA model's filename and finds it in the modelList
	 * @return the resolved LaModel
	 */
	private LaModel resolveSelectedNetFromParameters() {
		//FIXME map internal filename to label here for now. Should be solved with TableViewer
		String netFilename = parameters.getParameterValue(JobConst.PROP_MODELNAME);
		LaModel model = null;
		if(netFilename != null) {
			for(LaModel m : modelList) {
				if(netFilename.equals(m.getFilename())) {
					model = m;
					break;
				}
			}
		}
		return model;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Layout Analysis Configuration");
//		newShell.setMinimumSize(480, 480);
	}

	@Override
	protected Point getInitialSize() {
		return SWTUtil.getPreferredOrMinSize(getShell(), 480, 300);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.HELP); //SWT.MAX | SWT.RESIZE
	}

	
	/* 
	 * Add help button to buttonBar
	 * 
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	  // Change parent layout data to fill the whole bar
//	  parent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

	  helpButton = createButton(parent, IDialogConstants.HELP_ID, "Help", false);
	  helpButton.setImage(Images.HELP); //super.getImage(Dialog.DLG_IMG_HELP));
	  
	  // Update layout of the parent composite to count the spacer
//	  GridLayout layout = (GridLayout)parent.getLayout();
//	  layout.numColumns++;
//	  layout.makeColumnsEqualWidth = false;

	  createButton(parent, IDialogConstants.OK_ID, "OK", true);
	  createButton(parent, IDialogConstants.CANCEL_ID, "Cancel" , false);
	  GridData buttonLd = (GridData)getButton(IDialogConstants.CANCEL_ID).getLayoutData();
	  helpButton.setLayoutData(buttonLd);
	  helpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_PAGE, "You can find the relevant information on the Transkribus Wiki.", getParentShell());
			}
		});		
	}

	/**
	 * TODO Replace that with respective methods in Storage/TrpServerConn
	 * 
	 * @author philip
	 *
	 */
	private static class LaDataProvider {
		private final static String konzilsProtNetLabel = "Konzilsprotokolle Greifswald";
		private final static String konzilsProtNetName = "LA_alvermann1.pb";
		protected final static String postcardNetLabel = "Postcards";
		private final static String postcardNetName = "postcards_aru_c3.pb";
		//hebrew newspaper LA net
		private final static String newspaperV1NetLabel = "Newspapers (Hebrew)";
		private final static String newspaperV1NetName = "LA_ara_news_aru_mix_90_ema.pb";
		//2019-07 newseye newspaper LA net
		private final static String newspaperV2NetLabel = "Newspapers (NewsEye) July 2019";
		private final static String newspaperV2NetName = "LA_news_onb_att_newseye_2019-7.pb";
		//2019-08 newseye newspaper LA net
		private final static String newspaperV3NetLabel = "Newspapers (NewsEye)";
		private final static String newspaperV3NetName = "LA_news_onb_att_newseye.pb";
		private LaDataProvider() {}
		public static List<LaModel> getModels() {
			final List<LaModel> nets = new ArrayList<>(1);
			LaModel konzilsProt = new LaModel(konzilsProtNetLabel, konzilsProtNetName);
			LaModel postcards = new LaModel(postcardNetLabel, postcardNetName);
			LaModel newspaperV1 = new LaModel(newspaperV1NetLabel, newspaperV1NetName);
			//LaModel newspaperV2 = new LaModel(newspaperV2NetLabel, newspaperV2NetName);
			LaModel newspaperV3 = new LaModel(newspaperV3NetLabel, newspaperV3NetName);
			nets.add(konzilsProt);
			nets.add(postcards);
			nets.add(newspaperV1);
//			if(Storage.getInstance().isAdminLoggedIn()) {
//				//only admins may access outdated newseye net
//				nets.add(newspaperV2);
//			}
			nets.add(newspaperV3);
			return nets;
		}
	}
	private static class LaModel {
		private String filename;
		private String label;
		public LaModel(String label, String filename) {
			this.filename = filename;
			this.label = label;
		}
		public String getFilename() {
			return filename;
		}
		public String getLabel() {
			return label;
		}
	}
}
