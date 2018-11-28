package eu.transkribus.swt_gui.tools;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.SWTEventListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.ChooseTranscriptDialog;
import eu.transkribus.swt_gui.htr.TextRecognitionComposite;
import eu.transkribus.swt_gui.la.LayoutAnalysisComposite;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class ToolsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(ToolsWidget.class);
	
	Composite mdGroup;
	SWTEventListener listener=null;
	
	LayoutAnalysisComposite laComp;
	Button startLaBtn;
	
	TextRecognitionComposite trComp;
	
	Button polygon2baselinesBtn, baseline2PolygonBtn;
	CurrentTranscriptOrCurrentDocPagesSelector otherToolsPagesSelector;
		
	Image ncsrIcon = Images.getOrLoad("/NCSR_icon.png");
	Label ncsrIconLbl;

	TranscriptVersionChooser refVersionChooser, hypVersionChooser;
	
	Button computeWerBtn,computeAdvancedBtn,compareSamplesBtn;
	Button compareVersionsBtn;
	Composite werGroup;
	ExpandableComposite werExp;
	
	/*
	 * This can be safely removed when Error Rate tool integration is done.
	 */
	public final static boolean IS_LEGACY_WER_GROUP = false;
	
	public static class TranscriptVersionChooser extends Composite {
		public Button useCurrentBtn;
		public Button chooseVersionBtn;
		
		public TrpTranscriptMetadata selectedMd;

		public TranscriptVersionChooser(String label, Composite parent, int style) {
			super(parent, style);
			
			this.setLayout(new GridLayout(4, false));

			Label l = new Label(this, 0);
			l.setText(label);
			
			chooseVersionBtn = new Button(this, SWT.PUSH);
			chooseVersionBtn.setText("Choose...");
			chooseVersionBtn.setToolTipText("Click to choose another transcript version...");
			
			useCurrentBtn = new Button(this, SWT.PUSH);
			useCurrentBtn.setText("Use current");
			useCurrentBtn.setToolTipText("Click to use the currently opened transcript version");
			
			useCurrentBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					setToCurrent();
				}
			});
			
			chooseVersionBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					chooseTranscriptVersion();
				}
			});
			
			updateSelectedVersion();
		}
		
		public void updateSelectedVersion() {
			String l = selectedMd == null ? "Choose..." : getTranscriptLabel(selectedMd);
			
			if(!SWTUtil.isDisposed(chooseVersionBtn)){
				chooseVersionBtn.setText(l);
				chooseVersionBtn.pack();
				layout();
			}

		}
		
		public void chooseTranscriptVersion() {
			ChooseTranscriptDialog d = new ChooseTranscriptDialog(getShell());
			if (d.open() != Dialog.OK)
				return;
			
			TrpTranscriptMetadata md = d.getTranscript();
			if (md == null) {
				logger.debug("selected version was null...");
				return;
			}
			
			selectedMd = md;
			updateSelectedVersion();
		}

		public void setToCurrent() {
			if (Storage.getInstance().hasTranscript()) {
				selectedMd = Storage.getInstance().getTranscriptMetadata();
				updateSelectedVersion();
			}			
		}
		public void setToGT() {
			if (Storage.getInstance().hasTranscript()) {	
				for (TrpTranscriptMetadata version : Storage.getInstance().getTranscriptsSortedByDate(true, -1)) {
					if (version.getStatus() == EditStatus.GT) {
						selectedMd = version;
						updateSelectedVersion();
						return;
					}
				}
				selectedMd = Storage.getInstance().getTranscriptMetadata();
				updateSelectedVersion();				
			}		
		}
	}	
	
	public ToolsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
				
		Label ncsrIconL = new Label(SWTUtil.dummyShell, 0);
		ncsrIconL.setImage(ncsrIcon);
		
		initLayoutAnalysisTools();
		initRecogTools();
		
		if(IS_LEGACY_WER_GROUP) {
			initLegacyWerGroup();
		} else {
			initWerGroup();
		}
		
		initOtherTools();

	}
	
	public String getSelectedLaMethod() {
		return laComp.getSelectedMethod();
	}
	
	private void initLayoutAnalysisTools() {
		ExpandableComposite laToolsExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		laToolsExp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite laToolsGroup = new Composite(laToolsExp, SWT.SHADOW_ETCHED_IN);
		laToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		metadatagroup.setText("Document metadata");
		laToolsGroup.setLayout(new GridLayout(2, false));
		
		laComp = new LayoutAnalysisComposite(laToolsGroup, 0);
		laComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		startLaBtn = new Button(laToolsGroup, 0);
		startLaBtn.setText("Run");
		startLaBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		startLaBtn.setImage(Images.ARROW_RIGHT);
		
		if (false) {
//		laMethodCombo = new LabeledCombo(laToolsGroup, "Method: ");
//		laMethodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		laMethodCombo.combo.setItems(LayoutAnalysisComposite.getMethods(false).toArray(new String[0]));
//		laMethodCombo.combo.select(0);
//		Storage.getInstance().addListener(new IStorageListener() {
//			public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
//				if (arg.login) {
//					laMethodCombo.combo.setItems(LayoutAnalysisComposite.getMethods(false).toArray(new String[0]));
//					laMethodCombo.combo.select(0);
//				}
//			}
//		});
//		
//		laMethodCombo.combo.addModifyListener(new ModifyListener() {
//			@Override public void modifyText(ModifyEvent e) {
//				updateLaGui();
//			}
//		});
//		
//		regAndLineSegBtn = new Button(laToolsGroup, SWT.PUSH);
//		regAndLineSegBtn.setText("Detect regions, lines and baselines");
//		regAndLineSegBtn.setToolTipText("Detects regions, lines and baselines in this page - warning: current regions, lines and baselines will be lost!");
//		regAndLineSegBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		
////		Button aboutRegAndLineSegBtn = new Button(laToolsGroup, SWT.PUSH);
////		aboutRegAndLineSegBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		
//		lineSegBtn = new Button(laToolsGroup, SWT.PUSH);
//		lineSegBtn.setText("Detect lines and baselines");
//		lineSegBtn.setToolTipText("Detects lines and baselines in all selected regions (or in all regions if no region is selected) - warning: current lines and baselines in selected regions will be lost!");
//		lineSegBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
//		
//		batchLaBtn = new Button(laToolsGroup, SWT.PUSH);
//		batchLaBtn.setText("Batch job...");
//		batchLaBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		batchLaBtn.setToolTipText("Configure and start a batch job for layout analysis");
//		
//		Button aboutLaBtn = new Button(laToolsGroup, SWT.PUSH);
//		aboutLaBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutLaBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: Analyze Layout";
//				String msg = "Status\n"
//						+ "\t-Experimental\n"
//						+ "\t-Needs enhancement\n"
//						+ "Behaviour\n"
//						+ "\t-Text regions and lines are detected\n"
//						+ "\t-Already available text regions and/or lines are deleted\n"
//						+ "Background\n"
//						+ "\t-HTR processing needs correctly detected text regions and baselines\n"
//						+ "\t-In the future it is planned to have integrated solutions available where\n"
//						+ "\t text regions and baselines are detected in one process"
//						+ "Provider\n"
//						+ "\t-National Centre for Scientific Research (NCSR) – Demokritos in\n"
//						+ "\t Greece/Athens.\n"
//						+ "Contact\n"
//						+ "\t https://www.iit.demokritos.gr/cil";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, ncsrIcon, new String[] { "Close" }, 0);
//			}
//		});
		
		}
		
//		Button aboutLineSegBtn = new Button(laToolsGroup, SWT.PUSH);
//		aboutLineSegBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutLineSegBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: Detect Lines and Baselines";
//				String msg = "Status\n"
//						+ "\t-Beta version\n"
//						+ "\t-Can be used for productive work\n"
//						+ "Behaviour\n"
//						+ "\t-Detects lines and baselines in text regions.\n"
//						+ "\t-Note: For HTR purposes only baselines are necessary, therefore no need\n"
//						+ "\t to correct lines.\n"
//						+ "Background\n"
//						+ "\t-The PAGE format which is used internally in TRANSKRIBUS requires that\n"
//						+ "\t each baseline is part of a line region. Therefore the tool needs to produce\n"
//						+ "\t line regions although the line regions are not used for further processing\n"
//						+ "\t (and can therefore be ignored in the correction process).\n"
//						+ "Provider\n"
//						+ "\t-National Centre for Scientific Research (NCSR) – Demokritos in\n\tGreece/Athens.\n"
//						+ "Contact\n"
//						+ "\t https://www.iit.demokritos.gr/cil/";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, ncsrIcon, new String[] { "Close" }, 0);
//			}
//		});		
				
//		baselineBtn = new Button(laToolsGroup, SWT.PUSH);
//		baselineBtn.setText("Detect baselines");
//		baselineBtn.setToolTipText("Detects baselines in all lines of the selected regions (or of all regions if no region is selected) - warning: current baselines of affected lines will be lost!");
//		baselineBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
//		
//		Button aboutBaselineBtn = new Button(laToolsGroup, SWT.PUSH);
//		aboutBaselineBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutBaselineBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: Detect baselines";
//				String msg = "Status\n"
//						+ "\t-Beta version\n"
//						+ "\t-Can be used for productive work\n"
//						+ "Behaviour\n"
//						+ "\t-Note: This is a tool with a very special purpose: If line regions are\n"
//						+ "\t already available the tool will detect corresponding baselines\n"
//						+ "\t-Needs correct line regions as input\n"
//						+ "\t-Detects baselines within line regions\n"
//						+ "Background\n"
//						+ "\t-In some rare cases researchers may have correct line regions\n"
//						+ "\t available, these line regions can be enriched with baselines.\n"
//						+ "Provider\n"
//						+ "\t-National Centre for Scientific Research (NCSR) – Demokritos in\n"
//						+ "\t Greece/Athens.\n"
//						+ "Contact\n"
//						+ "\t https://www.iit.demokritos.gr/cil/";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, ncsrIcon, new String[] { "Close" }, 0);				
//			}
//		});		
//		
//		wordSegBtn = new Button(laToolsGroup, SWT.PUSH);
//		wordSegBtn.setText("Detect words");
//		wordSegBtn.setToolTipText("Detects words in all lines of the selected regions (or of all regions if no region is selected) - warning: current baselines of affected lines will be lost!");
//		wordSegBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
//		
//		Button aboutWordBtn = new Button(laToolsGroup, SWT.PUSH);
//		aboutWordBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutWordBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: Detect baselines";
//				String msg = "Status\n"
//						+ "\t-Beta version\n"
//						+ "\t-Can be used for productive work\n"
//						+ "Behaviour\n"
//						+ "\t-Note: This is a tool with a very special purpose: If line regions are\n"
//						+ "\t already available the tool will detect corresponding baselines\n"
//						+ "\t-Needs correct line regions as input\n"
//						+ "\t-Detects baselines within line regions\n"
//						+ "Background\n"
//						+ "\t-In some rare cases researchers may have correct line regions\n"
//						+ "\t available, these line regions can be enriched with baselines.\n"
//						+ "Provider\n"
//						+ "\t-National Centre for Scientific Research (NCSR) – Demokritos in\n"
//						+ "\t Greece/Athens.\n"
//						+ "Contact\n"
//						+ "\t https://www.iit.demokritos.gr/cil/";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, ncsrIcon, new String[] { "Close" }, 0);				
//			}
//		});		
		
//		Button aboutBtn = new Button(laToolsGroup, SWT.PUSH);
//		aboutBtn.setText("About NCSR...");
//		aboutBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				Shell s = new Shell();
//				s.setLayout(new RowLayout());
//				Label iconL = new Label(s, 0);
//				iconL.setImage(ncsrIcon);
//				
//				Label aboutL = new Label(s, 0);
//				aboutL.setText("Computational Intelligence Laboratory, Institute of Informatics and Telecommunications\nNational Center for Scientific Research “Demokritos”, GR-153 10 Agia Paraskevi, Athens, Greece");
//				s.pack();
//				s.setText("About: Layout analysis tools");
//				SWTUtil.centerShell(s);
//				s.open();
//			}
//		});
		
		laToolsExp.setClient(laToolsGroup);
		laToolsExp.setText("Layout Analysis");
		laToolsExp.setExpanded(true);
		Fonts.setBoldFont(laToolsExp);
		laToolsExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
		
//		updateLaGui();
	}
	
//	private void updateLaGui() {
//		if (regAndLineSegBtn == null || lineSegBtn == null)
//			return;
//		
//		String method = getSelectedLaMethod();
//		regAndLineSegBtn.setEnabled(true);
//		lineSegBtn.setEnabled(true);
//		
//		if (method.equals(LayoutAnalysisComposite.METHOD_NCSR)) {
//			regAndLineSegBtn.setEnabled(false);
//		}
//		else if (method.equals(LayoutAnalysisComposite.METHOD_CVL)) {
//			lineSegBtn.setEnabled(false);
//		}
//	}
	
	private void initRecogTools() {
		ExpandableComposite exp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		exp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		trComp = new TextRecognitionComposite(exp, 0);
		trComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		exp.setClient(trComp);
		exp.setText("Text Recognition");
		Fonts.setBoldFont(exp);
		exp.setExpanded(true);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}

	
	private void initOtherTools() {
		ExpandableComposite exp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		exp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite c = new Composite(exp, SWT.SHADOW_ETCHED_IN);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		c.setLayout(new GridLayout(1, true));
		
		otherToolsPagesSelector = new CurrentTranscriptOrCurrentDocPagesSelector(c, SWT.NONE, true);
		otherToolsPagesSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		polygon2baselinesBtn = new Button(c, SWT.PUSH);
		polygon2baselinesBtn.setText("Add Baselines to Polygons");
		polygon2baselinesBtn.setToolTipText("Creates baselines for all surrounding polygons - warning: existing baselines will be lost (text is retained however!)");
		polygon2baselinesBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		baseline2PolygonBtn = new Button(c, SWT.PUSH);
		baseline2PolygonBtn.setText("Add Polygons to Baselines");
		baseline2PolygonBtn.setToolTipText("Creates polygons for all baselines - warning: existing polygons will be lost (text is retained however!)");
		baseline2PolygonBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				
		exp.setClient(c);
		new Label(c, SWT.NONE);
		exp.setText("Other Tools");
		Fonts.setBoldFont(exp);
		exp.setExpanded(true);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}

	private void initLegacyWerGroup() {
		werExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		werExp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		werGroup = new Composite(werExp, SWT.SHADOW_ETCHED_IN);
		werGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		werGroup.setLayout(new GridLayout(2, false));
		
		refVersionChooser = new TranscriptVersionChooser("Reference:\n(Correct Text) ", werGroup, 0);
		refVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		hypVersionChooser = new TranscriptVersionChooser("Hypothesis:\n(HTR Text) ", werGroup, 0);
		hypVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));		
				
		computeWerBtn = new Button(werGroup, SWT.PUSH);
		computeWerBtn.setText("Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 0, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
		
		compareVersionsBtn = new Button(werGroup, SWT.PUSH);
		compareVersionsBtn.setText("Compare Versions in Textfile");
		compareVersionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		compareVersionsBtn.setToolTipText("Shows the difference of the two selected versions");
			
		werExp.setClient(werGroup);
		werExp.setText("Compute Accuracy");
		Fonts.setBoldFont(werExp);
		werExp.setExpanded(true);
		werExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}
	
	private void initWerGroup() {
		werExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
		werExp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		werGroup = new Composite(werExp, SWT.SHADOW_ETCHED_IN);
		werGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		metadatagroup.setText("Document metadata");
		werGroup.setLayout(new GridLayout(2, false));
		
		refVersionChooser = new TranscriptVersionChooser("Reference:\n(Correct Text) ", werGroup, 0);
		refVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		hypVersionChooser = new TranscriptVersionChooser("Hypothesis:\n(HTR Text) ", werGroup, 0);
		hypVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));		
		
//		computeWerBtn.pack();
		
		compareVersionsBtn = new Button(werGroup, SWT.PUSH);
		compareVersionsBtn.setText("Compare Versions in Textfile");
		compareVersionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		compareVersionsBtn.setToolTipText("Shows the difference of the two selected versions");
		
		computeWerBtn = new Button(werGroup, SWT.PUSH);
		computeWerBtn.setText("Quick Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 0, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
		
		computeAdvancedBtn = new Button(werGroup,SWT.PUSH);
		computeAdvancedBtn.setText("Advanced...");
		
		compareSamplesBtn = new Button(werGroup, SWT.PUSH);
		compareSamplesBtn.setText("Compare Samples");
		compareSamplesBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		compareSamplesBtn.setToolTipText("Shows the difference of the two selected versions");
		
		werExp.setClient(werGroup);
		werExp.setText("Compute Accuracy");
		Fonts.setBoldFont(werExp);
		werExp.setExpanded(true);
		werExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				layout();
			}
		});
	}

	public Button getCompareVersionsBtn() {
		return compareVersionsBtn;
	}

	public static String getTranscriptLabel(TrpTranscriptMetadata t) {
		final String labelStr = CoreUtils.newDateFormatUserFriendly().format(t.getTime()) 
				+ " - " + t.getUserName() 
				+ " - " + t.getStatus().getStr()
				+ (t.getToolName() != null ? " - " + t.getToolName() : "");
		
		return labelStr;
	}
}
