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
	
//	Group mdGroup;
	Composite mdGroup;
	SWTEventListener listener=null;
	
	LayoutAnalysisComposite laComp;
	Button startLaBtn;
	
	TextRecognitionComposite trComp;
	
//	LabeledCombo laMethodCombo;
//	Button /*blockSegBtn,*/ regAndLineSegBtn, lineSegBtn, wordSegBtn;
//	Button batchLaBtn;

	Button polygon2baselinesBtn;
	CurrentTranscriptOrCurrentDocPagesSelector otherToolsPagesSelector;
		
//	Button ocrBtn, htrTrainBtn, recogBtn;
	
	Image ncsrIcon = Images.getOrLoad("/NCSR_icon.png");
	Label ncsrIconLbl;

	TranscriptVersionChooser refVersionChooser, hypVersionChooser;
	
	Button computeWerBtn;
	Button compareVersionsBtn;
	Composite werGroup;
	ExpandableComposite werExp;
	
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
			chooseVersionBtn.setToolTipText("Click to use the currently opened transcript version");
			
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
			
			chooseVersionBtn.setText(l);
			chooseVersionBtn.pack();
			layout();
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
	}	
	
	public ToolsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
				
		Label ncsrIconL = new Label(SWTUtil.dummyShell, 0);
		ncsrIconL.setImage(ncsrIcon);
		
		initLayoutAnalysisTools();
		initRecogTools();
		initWerGroup();
		initOtherTools();
//		initOCRTools();
//		initHTRTools();
		
//		Button btn = createButton(segModeGroup, SWT.RADIO, "Block segmentation", 1, false);
//		segModeRadios.add(btn);
//		
//		Button btn2 = createButton(segModeGroup, SWT.RADIO, "Block/line segmentation", 1, false);
//		segModeRadios.add(btn2);
//		
//		Button btn3 = createButton(segModeGroup, SWT.RADIO, "Lines on regions:", 1, false);
//		segModeRadios.add(btn3);
		

		
//		applyBtn.setToolTipText("Applies the metadata to all selected elements (note: if multiple elements are selected, the metadata is not applied automatically but with this button)");
	}
	
	public String getSelectedLaMethod() {
		return laComp.getSelectedMethod();
		
//		if (laMethodCombo == null) 
//			return "";
//		
//		if (laMethodCombo.combo.getSelectionIndex()>=0 && laMethodCombo.combo.getSelectionIndex()<laMethodCombo.combo.getItemCount()) {
//			return laMethodCombo.combo.getItems()[laMethodCombo.combo.getSelectionIndex()];	
//		} else {
//			return "";
//		}
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
		Composite c = new Composite(exp, SWT.SHADOW_ETCHED_IN);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		c.setLayout(new GridLayout(3, false));
		
		trComp = new TextRecognitionComposite(c, 0);
		trComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
//		if (false) {
//		ocrBtn = new Button(c, SWT.PUSH);
//		ocrBtn.setText("Run OCR...");
//		ocrBtn.setImage(Images.getOrLoad("/icons/ocr_16.png"));
//		ocrBtn.setToolTipText("Starts the recognition process for the current book");
//		ocrBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		
//		
//		Button aboutOcrBtn = new Button(c, SWT.PUSH);
//		aboutOcrBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutOcrBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: OCR";
//				String msg = "Status\n"
//						+ "\t-Productive\n"
//						+ "Behaviour\n"
//						+ "\t-All pages/images of the document are processed with\n"
//						+ "\tABBYY FineReader 11 SDK";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, null, new String[] { "Close" }, 0);				
//			}
//		});		
//		
//		htrTrainBtn = new Button(c, SWT.PUSH);
//		htrTrainBtn.setText("Train Text Recognition...");
//		htrTrainBtn.setImage(Images.getOrLoad("/icons/muscle_16.png"));
//		htrTrainBtn.setToolTipText("EXPERIMENTAL");
//		htrTrainBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		
//		Button aboutHtrBtn = new Button(c, SWT.PUSH);
//		aboutHtrBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutHtrBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: Text Recognition";
//				String msg = "Status\n"
//						+ "\t-Productive\n"
//						+ "Behaviour\n"
//						+ "\t-All pages/images of the document are processed with\n"
//						+ "\tHTR Technology\n"
//						+ "Provider\n"
//						+ "\t-University of Rostock, Institute of Mathematics, CITlab for HTR";
//						
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, null, new String[] { "Close" }, 0);				
//			}
//		});
//		
//		recogBtn = new Button(c, SWT.PUSH);
//		recogBtn.setText("Run Text Recognition...");
//		recogBtn.setToolTipText("EXPERIMENTAL");
//		recogBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
//		recogBtn.setImage(Images.getOrLoad("/icons/htr_16.png"));
//		
//		}
		
		exp.setClient(c);
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
		
//		Label refLabel = new Label(werGroup, 0);
//		refLabel.setText("Reference:");
//		refVersionCombo = new Combo(werGroup, SWT.READ_ONLY);
//		
//		Label hypLabel = new Label(werGroup, 0);
//		hypLabel.setText("Hypothesis:");
//		hypVersionCombo = new Combo(werGroup, SWT.READ_ONLY);
		
		Label emptyLabel = new Label(werGroup,0);
		computeWerBtn = new Button(werGroup, SWT.PUSH);
		computeWerBtn.setText("Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
//		computeWerBtn.pack();
		
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

//	public Button getBlocksBtn() { return blockSegBtn; }
//	public Button getBlocksInPsBtn() { return blockSegWPsBtn; }
//	public Button getLinesBtn() { return lineSegBtn; }
//	public Button getWordsBtn() { return wordSegBtn; }
//	public Button getBaselineBtn() { return baselineBtn; }
//	public Button getLaBtn() { return batchLaBtn; }

	
	public Button getCompareVersionsBtn() {
		return compareVersionsBtn;
	}

	public static String getTranscriptLabel(TrpTranscriptMetadata t) {
		final String labelStr = CoreUtils.DATE_FORMAT_USER_FRIENDLY.format(t.getTime()) 
				+ " - " + t.getUserName() 
				+ " - " + t.getStatus().getStr()
				+ (t.getToolName() != null ? " - " + t.getToolName() : "");
		
		return labelStr;
	}

//	public void updateVersions(List<TrpTranscriptMetadata> transcripts) {
//		refVersionCombo.removeAll();
//		hypVersionCombo.removeAll();
//		for(TrpTranscriptMetadata t : transcripts){
//			final String labelStr = getTranscriptLabel(t);
//			refVersionCombo.add(labelStr);
//			refVersionCombo.setData(labelStr, t);
//			hypVersionCombo.add(labelStr);
//			hypVersionCombo.setData(labelStr, t);
//		}
//		refVersionCombo.pack();
//		hypVersionCombo.pack();
//		werExp.layout();
//		//pack the whole tools widget or the expandable won't resize
//		this.pack();
//	}
	
// OLD OCR and HTR settings ======
	
//	private void initOCRTools() {
//		ExpandableComposite exp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
//		exp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		Composite c = new Composite(exp, SWT.SHADOW_ETCHED_IN);
//		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		c.setLayout(new GridLayout(2, false));
//		
//		Composite c2 = new Composite(c, SWT.SHADOW_ETCHED_IN);
//		c2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
//		c2.setLayout(new GridLayout(3, false));
//		
//		startOcrBtn = new Button(c2, SWT.PUSH);
//		startOcrBtn.setText("Start OCR for document");
//		startOcrBtn.setToolTipText("Starts the OCR process for the current book");
//		startOcrBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		startOcrPageBtn = new Button(c2, SWT.PUSH);
//		startOcrPageBtn.setText("Start OCR for page");
//		startOcrPageBtn.setToolTipText("Starts the OCR process for the current page");
//		startOcrPageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		Button aboutOcrBtn = new Button(c2, SWT.PUSH);
//		aboutOcrBtn.setImage(Images.getOrLoad("/icons/information.png"));
//		aboutOcrBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String title = "About: OCR";
//				String msg = "Status\n"
//						+ "\t-Productive\n"
//						+ "Behaviour\n"
//						+ "\t-All pages/images of the document are processed with\n"
//						+ "\tABBYY FineReader 11 SDK\n"
//						+ "\t-Select one or more languages\n"
//						+ "\t-Select “combined” if Gothic text and Roman Typeface\n"
//						+ "\t are used within one document\n"
//						+ "\t-Select “OldGerman”, “OldEnglish”, etc. to activate the\n"
//						+ "\t recognition of the long “s” in Roman Type Face books\n"
//						+ "\t-The document is processed from scratch, manually segmented\n"
//						+ "\t text blocks are not taken into account.\n"
//						+ "Background\n"
//						+ "\t-ABBYY FineReader is one of the leading OCR engines worldwide.\n"
//						+ "\t-We have implemented only a very small set of the features provided\n"
//						+ "\t by the ABBYY SDK.\n"
//						+ "\t-UIBK runs a powerful ABBYY FineReader SDK Cluster and is able to\n"
//						+ "\t process large mounts of documents.\n"
//						+ "Provider\n"
//						+ "\t-ABBYY FineReader\n"
//						+ "\t-University Innsbruck, Digitisation and Digital Preservation group\n"
//						+ "Credits\n"
//						+ "\t-ABBYY FineReader for 15 years of cooperation\n"
//						+ "\t-This implementation is based on the infrastructure set up during\n"
//						+ "\t the the Europeana Newspaper Project (2013-2015)\n"
//						+ "\t http://www.europeana-newspapers.eu/";
//				
//				DialogUtil.showMessageDialog(getShell(), title, msg, null, null, new String[] { "Close" }, 0);				
//			}
//		});		
//		
//		Label l0 = new Label(c, 0);
//		l0.setText("Script type: ");		
//		scriptTypeCombo = new Combo(c, SWT.DROP_DOWN | SWT.READ_ONLY);
////		TrpDocMetadata md = Storage.getInstance().getDoc().getMd();
//		scriptTypeCombo.setItems(EnumUtils.stringsArray(ScriptType.class));
//		scriptTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//
//		Label l1 = new Label(c, 0);
//		l1.setText("Language: ");
////		languageCombo = new Combo(c, SWT.DROP_DOWN);
////		languageCombo.setItems(FinereaderUtils.FINEREADER_LANGUAGES);
//		languagesTable = new LanguageSelectionTable(c, 0);
//		languagesTable.setAvailableLanguages(FinereaderUtils.FINEREADER_LANGUAGES);
//		languagesTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		
//		postInitExpandable(exp, c, "OCR");
//	}
//	public Button getRunHtrOnPageBtn() { return runHtrOnPageBtn; }
//	public Combo getHtrModelsCombo() {return htrModelsCombo; }
//	public Combo getScriptTypeCombo() { return scriptTypeCombo; }

//	public Combo getLanguageCombo() { return languageCombo; }

//	public void clearHtrModelList() {
//		htrModelsCombo.setItems(new String[]{""});
//		htrModelsCombo.setEnabled(false);
//	}
//
//	public void setHtrModelList(String[] htrModels) {
//		htrModelsCombo.setEnabled(true);
//		htrModelsCombo.setItems(htrModels);
//		htrModelsCombo.select(0);
//	}

//	public void updateParameter(ScriptType st, String languages) {
//		SWTUtil.select(scriptTypeCombo, EnumUtils.indexOf(st));
//		languagesTable.setSelectedLanguages(languages);		
//	}
//	private void initHTRTools() {
//	ExpandableComposite htrToolsExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
//	htrToolsExp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//	Composite htrToolsComp = new Composite(htrToolsExp, SWT.SHADOW_ETCHED_IN);
//	htrToolsComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
////	metadatagroup.setText("Document metadata");
//	htrToolsComp.setLayout(new GridLayout(2, false));
//	
//	htrModelsCombo = new Combo(htrToolsComp,  SWT.DROP_DOWN | SWT.READ_ONLY);
//	htrModelsCombo.setEnabled(false);
//	htrModelsCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//	htrModelsCombo.setItems(new String[]{""});
//	
//	runHtrOnPageBtn = new Button(htrToolsComp, SWT.PUSH);
//	runHtrOnPageBtn.setText("Start HTR for page ");
////	runHtrOnPageBtn.setEnabled(false);
//	runHtrOnPageBtn.setToolTipText("Runs handwritten text recognition on current page");
//	runHtrOnPageBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//	
//	Button aboutBtn = new Button(htrToolsComp, SWT.PUSH);
//	aboutBtn.setImage(Images.getOrLoad("/icons/information.png"));
////	aboutBtn.setText("About...");
//	aboutBtn.addSelectionListener(new SelectionAdapter() {
//		@Override public void widgetSelected(SelectionEvent e) {
//			String title = "About: HTR Processing";
//			String msg = "Status\n"
//					+ "\t-Experimental\n"
//					+ "\t-Do not use for production\n"
//					+ "Behaviour\n"
//					+ "\t-Trained HTR models can be selected and applied to one page\n"
//					+ "\t-Note: HTR is a sophisticated system where character sets and\n"
//					+ "\t language models need to play together.\n"
//					+ "\t-Words which are not in the lexicon will not be recognized.\n"
//					+ "\t-Characters (e.g. special characters) which were not seen by\n"
//					+ "\t the HTR engine during the training process, can also not be recognized\n"
//					+ "Available HTR models\n"
//					+ "\t-Reichsgericht\n"
//					+ "\t\t-Trained on German Kurrent from the early 20th century.\n"
//					+ "\t\t Three writers.\n"
//					+ "\t\t-Only a very limited vocabulary based on juridical texts was used for\n"
//					+ "\t\t training.\n"
//					+ "\t-Bozen and Zwettl\n"
//					+ "\t\t-Trained on about 200 pages of German Kurrent texts from the\n"
//					+ "\t\t 17th century. Several writers.\n"
//					+ "\t\t-No lexicon currently available in the background, therefore\n"
//					+ "\t\t limited applicability\n"
//					+ "Background\n"
//					+ "\t-This is one of the very first implementations for processing handwritten\n"
//					+ "\t historical texts out-of-the-box.\n"
//					+ "Provider\n"
//					+ "\t-Technical University Valencia, Pattern Recognition and Human\n"
//					+ "\t Language Technology\n"
//					+ "\t-Instituut voor Nederlandse Lexicologie (INL)\n"
//					+ "Contact\n"
//					+ "\t https://www.prhlt.upv.es/\n"
//					+ "\t http://www.inl.nl/";
//			
//			DialogUtil.showMessageDialog(getShell(), title, msg, null, null, new String[] { "Close" }, 0);
//		}
//	});
//	
//	htrToolsExp.setClient(htrToolsComp);
//	htrToolsExp.setText("HTR");
//	htrToolsExp.setExpanded(false);
//	htrToolsExp.addExpansionListener(new ExpansionAdapter() {
//		public void expansionStateChanged(ExpansionEvent e) {
//			layout();
//		}
//	});
//	
//}
//private void postInitExpandable(ExpandableComposite exp, Composite c, String title) {
//exp.setClient(c);
//exp.setText("OCR (TYPEWRITTEN documents ONLY!!)");
//exp.setExpanded(false);
//exp.addExpansionListener(new ExpansionAdapter() {
//	public void expansionStateChanged(ExpansionEvent e) {
//		layout();
//	}
//});
//}	
}
