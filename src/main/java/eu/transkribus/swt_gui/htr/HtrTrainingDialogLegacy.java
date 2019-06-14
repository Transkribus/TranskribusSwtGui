package eu.transkribus.swt_gui.htr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.CitLabSemiSupervisedHtrTrainConfig;
import eu.transkribus.core.model.beans.HtrTrainConfig;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.util.DescriptorUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.htr.treeviewer.DataSetSelectionSashForm;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

/**
 * Configuration dialog for HTR training.<br/>
 * @author philip
 *
 */
public class HtrTrainingDialogLegacy extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTrainingDialog.class);

	private final int colId;

	private CTabFolder paramTabFolder;
	private CTabItem citlabHtrTrainingTabItem;
	private CTabItem citlabHtrPlusTrainingTabItem;
	private CTabItem citlabT2ITabItem;
	
	private Text2ImageConfComposite2 t2iConfComp;
	private CITlabHtrTrainingConfComposite citlabHtrParamCont;
	private CITlabHtrPlusTrainingConfComposite citlabHtrPlusParamCont;
	
	private TreeViewerDataSetSelectionSashForm treeViewerSelector;

	private Text modelNameTxt, descTxt, langTxt;

	private CitLabHtrTrainConfig citlabTrainConfig;
	private CitLabSemiSupervisedHtrTrainConfig citlabT2IConf;

	private Storage store = Storage.getInstance();

	private List<TrpDocMetadata> docList;
	
	private final List<JobImpl> trainJobImpls;
	
	private List<TrainMethodUITab> tabList; 
	
	public static final boolean ENABLE_T2I = false;

	public HtrTrainingDialogLegacy(Shell parent, JobImpl[] impls) {
		super(parent);
		if(impls == null || impls.length == 0) {
			throw new IllegalStateException("No HTR training jobs defined.");
		}
		docList = store.getDocList();
		colId = store.getCollId();	
		trainJobImpls = Arrays.asList(impls);
		tabList = new ArrayList<>(impls.length);
	}

	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(2, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(4, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Model Name:");
		modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label langLbl = new Label(paramCont, SWT.FLAT);
		langLbl.setText("Language:");
		langTxt = new Text(paramCont, SWT.BORDER);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		descTxt = new Text(paramCont, SWT.MULTI | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 3;
		// gd.horizontalSpan = 3;
		descTxt.setLayoutData(gd);

		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		int i = 0;
		CTabItem selection = null;
		if(trainJobImpls.contains(JobImpl.CITlabHtrTrainingJob)) {
			TrainMethodUITab tab = createCitlabTrainingTab(i++);
			selection = tab.getTabItem();
			tabList.add(tab);
		}

		if(trainJobImpls.contains(JobImpl.CITlabHtrPlusTrainingJob)) {
			TrainMethodUITab tab = createCitlabHtrPlusTrainingTab(i++);
			selection = tab.getTabItem();
			tabList.add(tab);
		}
		
		if(ENABLE_T2I && trainJobImpls.contains(JobImpl.CITlabSemiSupervisedHtrTrainingJob)) {
			TrainMethodUITab tab = createCitlabT2ITab(i++);
			if(selection == null) {
				//only select t2i if no other method is configured
				selection = tab.getTabItem();
			}
			tabList.add(tab);
		}
		
		paramTabFolder.setSelection(selection);		
		paramCont.pack();
		SWTUtil.onSelectionEvent(paramTabFolder, (e) -> { updateUI(); } );
		
		treeViewerSelector = new TreeViewerDataSetSelectionSashForm(sash, SWT.HORIZONTAL, colId, docList);
		
		sash.setWeights(new int[] { 45, 55 });
		
		updateUI();
		
		return cont;
	}

	private void updateUI() {
		boolean isT2I = isCitlabT2ISelected();
		descTxt.setEnabled(!isT2I);
		modelNameTxt.setEnabled(!isT2I);
		treeViewerSelector.getUseGtVersionChk().setEnabled(!isT2I);
		treeViewerSelector.getUseNewVersionChk().setEnabled(isT2I);
	}
	
	private TrainMethodUITab createCitlabT2ITab(final int tabIndex) {
		citlabT2ITabItem = new CTabItem(paramTabFolder, SWT.NONE);
		citlabT2ITabItem.setText("CITlab T2I");
		
		t2iConfComp = new Text2ImageConfComposite2(paramTabFolder, 0);
		citlabT2ITabItem.setControl(t2iConfComp);
		return new TrainMethodUITab(tabIndex, citlabT2ITabItem, t2iConfComp);
	}
	
	private TrainMethodUITab createCitlabTrainingTab(final int tabIndex) {
		citlabHtrTrainingTabItem = new CTabItem(paramTabFolder, SWT.NONE);

		citlabHtrParamCont = new CITlabHtrTrainingConfComposite(paramTabFolder, SWT.NONE);
		final String label = HtrTableLabelProvider.getLabelForHtrProvider(citlabHtrParamCont.getProvider());
		citlabHtrTrainingTabItem.setText(label);
		
		citlabHtrTrainingTabItem.setControl(citlabHtrParamCont);
		return new TrainMethodUITab(tabIndex, citlabHtrTrainingTabItem, citlabHtrParamCont);
	}
	
	private TrainMethodUITab createCitlabHtrPlusTrainingTab(final int tabIndex) {
		citlabHtrPlusTrainingTabItem = new CTabItem(paramTabFolder, SWT.NONE);

		citlabHtrPlusParamCont = new CITlabHtrPlusTrainingConfComposite(paramTabFolder, false, SWT.NONE);
		final String label = HtrTableLabelProvider.getLabelForHtrProvider(citlabHtrPlusParamCont.getProvider());
		citlabHtrPlusTrainingTabItem.setText(label);
		
		citlabHtrPlusTrainingTabItem.setControl(citlabHtrPlusParamCont);
		return new TrainMethodUITab(tabIndex, citlabHtrPlusTrainingTabItem, citlabHtrPlusParamCont);
	}
	
	private void setTrainAndTestDocsInHtrConfig(HtrTrainConfig config, EditStatus status) throws IOException {
		config.setColId(colId);
		//FIXME activate this method once the training job can handle such descriptors
//			config.setTrain(DescriptorUtils.buildSelectionDescriptorList(trainDocMap, status));
//			config.setTest(DescriptorUtils.buildSelectionDescriptorList(testDocMap, status));
		
		config.setTrain(DescriptorUtils.buildCompleteSelectionDescriptorList(treeViewerSelector.getTrainDocMap(), status));
		config.setTest(DescriptorUtils.buildCompleteSelectionDescriptorList(treeViewerSelector.getTestDocMap(), status));
	
		if (config.getTrain().isEmpty()) {
			throw new IOException("Train set must not be empty!");
		}
		
		if(config.getTest().isEmpty() && !isCitlabT2ISelected()){
			throw new IOException("Test set must not be empty! \nAt least one page must be selected to get meaningful error curve."
					+ " Please increase choice of text pages with increasing training pages.");
		}

		if (config.isTestAndTrainOverlapping()) {
			throw new IOException("Train and Test sets must not overlap!");
		}
	}
	
	private <T extends HtrTrainConfig> T createBaseConfig(T configObject) throws IOException {
		checkBasicConfig();
		configObject.setDescription(descTxt.getText());
		configObject.setModelName(modelNameTxt.getText());
		configObject.setLanguage(langTxt.getText());
		final boolean useGt = treeViewerSelector.getUseGtVersionChk().isEnabled() && treeViewerSelector.getUseGtVersionChk().getSelection();
		EditStatus status = useGt ? EditStatus.GT : null;
		setTrainAndTestDocsInHtrConfig(configObject, status);
		return configObject;
	}
	
	private CitLabHtrTrainConfig createCitlabTrainConfig() throws IOException {
		checkCitlabTrainingConfig();
		
		CitLabHtrTrainConfig citlabTrainConf = new CitLabHtrTrainConfig();
		citlabTrainConf = createBaseConfig(citlabTrainConf);		
		citlabTrainConf = citlabHtrParamCont.addParameters(citlabTrainConf);		

		return citlabTrainConf;
	}
	
	private CitLabHtrTrainConfig createCitlabHtrPlusTrainConfig() throws IOException {
		checkCitlabPlusTrainingConfig();
		
		CitLabHtrTrainConfig citlabTrainConf = new CitLabHtrTrainConfig();
		citlabTrainConf = createBaseConfig(citlabTrainConf);		
		citlabTrainConf = citlabHtrPlusParamCont.addParameters(citlabTrainConf);		

		return citlabTrainConf;
	}
	
	private CitLabSemiSupervisedHtrTrainConfig createCitlabT2IConfig() throws IOException {
		CitLabSemiSupervisedHtrTrainConfig config = t2iConfComp.getConfig();
		final boolean useInitial = treeViewerSelector.getUseNewVersionChk().isEnabled() 
				&& treeViewerSelector.getUseNewVersionChk().getSelection();
		EditStatus status = useInitial ? EditStatus.NEW : null;
		setTrainAndTestDocsInHtrConfig(config, status);
		
		return config;
	}
	
	boolean isCitlabTrainingSelected() {
		return paramTabFolder.getSelection().equals(citlabHtrTrainingTabItem);
	}
	
	boolean isCitlabHtrPlusTrainingSelected() {
		return paramTabFolder.getSelection().equals(citlabHtrPlusTrainingTabItem);
	}
	
	boolean isCitlabT2ISelected() {
		return paramTabFolder.getSelection().equals(citlabT2ITabItem);
	}

	@Override
	protected void okPressed() {
		citlabTrainConfig = citlabT2IConf = null;
		String msg = "";
		try {
			if (isCitlabTrainingSelected()) {
				msg = "You are about to start an HTR Training using CITlab HTR\n\n";
				citlabTrainConfig = createCitlabTrainConfig();
			} else if (isCitlabT2ISelected()) {
				logger.debug("creating citlab t2i config!");
				msg = "You are about to start a Text2Image alignment using CITlab HTR\n\n";
				citlabT2IConf = createCitlabT2IConfig();
			} else if (isCitlabHtrPlusTrainingSelected()){
				msg = "You are about to start an HTR Training using CITlab HTR+\n\n";
				citlabTrainConfig = createCitlabHtrPlusTrainConfig();
			} else {
				throw new IOException("Invalid method selected - should not happen anyway...");
			}
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Bad configuration", e.getMessage());
			return;
		}
		catch (Exception e) {
			TrpMainWidget.getInstance().onError("Unexcpected error", e.getMessage(), e);
			return;
		}

		DataSetMetadata trainSetMd = treeViewerSelector.getTrainSetMetadata();
		DataSetMetadata testSetMd = treeViewerSelector.getTestSetMetadata();
		msg += "Training set size:\t" + trainSetMd.getPages() + " pages\n";
		msg += "\t\t\t\t" + trainSetMd.getLines() + " lines\n";
		msg += "\t\t\t\t" + trainSetMd.getWords() + " words\n";
		msg += "Test set size:\t\t" + testSetMd.getPages() + " pages\n";
		msg += "\t\t\t\t" + +testSetMd.getLines() + " lines\n";
		msg += "\t\t\t\t" + testSetMd.getWords() + " words\n";

		msg += "\nStart?";

		int result = DialogUtil.showYesNoDialog(this.getShell(), "Start?", msg);
		
		if (result == SWT.YES) {
			super.okPressed();
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("HTR Training");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}

	private void checkCitlabTrainingConfig() throws IOException {
		List<String> errorList = citlabHtrParamCont.validateParameters(new ArrayList<>());
		if (!errorList.isEmpty()) {
			throw new IOException(errorList.stream()
					.collect(Collectors.joining("\n")));
		}
	}
	
	private void checkCitlabPlusTrainingConfig() throws IOException {
		List<String> errorList = citlabHtrPlusParamCont.validateParameters(new ArrayList<>());
		if (!errorList.isEmpty()) {
			throw new IOException(errorList.stream()
					.collect(Collectors.joining("\n")));
		}
	}

	private void checkBasicConfig() throws IOException {
		List<String> errorList = new ArrayList<>();
		if (StringUtils.isEmpty(modelNameTxt.getText())) {
			errorList.add("Model Name must not be empty!");
		}
		if (StringUtils.isEmpty(descTxt.getText())) {
			errorList.add("Description must not be empty!");
		}
		if (StringUtils.isEmpty(langTxt.getText())) {
			errorList.add("Language must not be empty!");
		}
		if (!errorList.isEmpty()) {
			throw new IOException(errorList.stream()
					.collect(Collectors.joining("\n")));
		}
	}

	public CitLabHtrTrainConfig getCitlabTrainConfig() {
		return citlabTrainConfig;
	}
	
	public CitLabSemiSupervisedHtrTrainConfig getCitlabT2IConfig() {
		return citlabT2IConf;
	}
	
	private class TrainMethodUITab {
		final int tabIndex;
		final CTabItem tabItem;
		final Composite configComposite;
		private TrainMethodUITab(int tabIndex, CTabItem tabItem, Composite configComposite) {
			this.tabIndex = tabIndex;
			this.tabItem = tabItem;
			this.configComposite = configComposite;
		}
		public int getTabIndex() {
			return tabIndex;
		}
		public CTabItem getTabItem() {
			return tabItem;
		}
		public Composite getConfigComposite() {
			return configComposite;
		}
	}
}
