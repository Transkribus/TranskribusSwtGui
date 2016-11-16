package eu.transkribus.swt_gui.dialogs;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor.PageDescriptor;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.UroHtrTrainConfig;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.ThumbnailWidgetVirtualMinimal;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrTrainingDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(HtrTrainingDialog.class);
	
	private CTabFolder paramTabFolder;
	private CTabItem uroTabItem;

	private Button addTrainDocBtn, addTestDocBtn;
	private CTabFolder docTabFolder, testDocTabFolder;
	
	private Button useTrainGtVersionChk, useTestGtVersionChk; 
	
	private List<ThumbnailWidgetVirtualMinimal> trainTwList, testTwList;
	
	private Text modelNameTxt, descTxt, langTxt, trainSizeTxt;
	private Combo baseModelCmb, noiseCmb;	
	
	private Text numEpochsTxt, learningRateTxt;
	
	private UroHtrTrainConfig conf;
	
	Storage store = Storage.getInstance();

	private final static String[] NOISE_OPTIONS = new String[] {"no", "preproc", "net", "both"};
	private final static int NOISE_DEFAULT_CHOICE = 3;
	
	private final static int NUM_EPOCHS_DEFAULT = 200;
	private final static String LEARNING_RATE_DEFAULT = "2e-3";
	private final static int TRAIN_SIZE_DEFAULT = 1000;
	
	public HtrTrainingDialog(Shell parent) {
		super(parent);
		trainTwList = new LinkedList<>();
		testTwList = new LinkedList<>();
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
//		gd.horizontalSpan = 3;
		descTxt.setLayoutData(gd);
		
		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		uroTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		uroTabItem.setText("CITlab RNN");

		Composite uroParamCont = new Composite(paramTabFolder, SWT.NONE);
		uroParamCont.setLayout(new GridLayout(4, false));

		paramTabFolder.setSelection(uroTabItem);

		Label numEpochsLbl = new Label(uroParamCont, SWT.NONE);
		numEpochsLbl.setText("Nr. of Epochs:");
		numEpochsTxt = new Text(uroParamCont, SWT.BORDER);
		numEpochsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label learningRateLbl = new Label(uroParamCont, SWT.NONE);
		learningRateLbl.setText("Learning Rate:");
		learningRateTxt = new Text(uroParamCont, SWT.BORDER);
		learningRateTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label noiseLbl = new Label(uroParamCont, SWT.NONE);
		noiseLbl.setText("Noise:");
		noiseCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		noiseCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		noiseCmb.setItems(NOISE_OPTIONS);
		
		Label trainSizeLbl = new Label(uroParamCont, SWT.NONE);
		trainSizeLbl.setText("Train Size per Epoch:");
		trainSizeTxt = new Text(uroParamCont, SWT.BORDER);
		trainSizeTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label baseModelLbl = new Label(uroParamCont, SWT.NONE);
		baseModelLbl.setText("Base Model:");
		baseModelCmb = new Combo(uroParamCont, SWT.READ_ONLY);
		baseModelCmb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		baseModelCmb.setItems(new String[] {"", "bla", "blubb"});
		
		setUroDefaults();
		
		Label emptyLbl = new Label(uroParamCont, SWT.NONE);
		Button resetUroDefaultsBtn = new Button(uroParamCont, SWT.PUSH);
		resetUroDefaultsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		resetUroDefaultsBtn.setText("Reset to defaults");
		resetUroDefaultsBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				setUroDefaults();
			}
		});
		
		uroTabItem.setControl(uroParamCont);

		paramCont.pack();

		// doc selection ===========================================================================================

		Composite docCont = new Composite(sash, SWT.BORDER);
		docCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docCont.setLayout(new GridLayout(1, false));

		SashForm docSash = new SashForm(docCont, SWT.HORIZONTAL);
		docSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		docSash.setLayout(new GridLayout(2, false));		
		
		
		Composite trainDocCont = new Composite(docSash, SWT.NONE);
		trainDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainDocCont.setLayout(new GridLayout(2, false));
		
		addTrainDocBtn = new Button(trainDocCont, SWT.PUSH);
		addTrainDocBtn.setText("Add Train Document");
		addTrainDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(docTabFolder, SWT.CLOSE);
				item.setText("Document " + docTabFolder.getItemCount());
				Composite docOverviewCont = createDocOverviewCont(trainTwList, 
						useTrainGtVersionChk.getSelection(), docTabFolder, store.getDoc());
				item.setControl(docOverviewCont); 
			}
		});
		
		useTrainGtVersionChk = new Button(trainDocCont, SWT.CHECK);
		useTrainGtVersionChk.setText("Use Groundtruth versions");
		useTrainGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(ThumbnailWidgetVirtualMinimal tw : trainTwList) {
					tw.setUseGtVersions(useTrainGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});

		docTabFolder = new CTabFolder(trainDocCont, SWT.BORDER | SWT.FLAT);
		docTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem item = new CTabItem(docTabFolder, SWT.NONE);
		item.setText("Document 1");
		
		Composite docOverviewCont = createDocOverviewCont(trainTwList, useTrainGtVersionChk.getSelection(), 
				docTabFolder, store.getDoc());
		item.setControl(docOverviewCont); 

		docTabFolder.setSelection(0);
		
		docTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				System.out.println("GONE");
			}
		});

		Composite testDocCont = new Composite(docSash, SWT.NONE);
		testDocCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		testDocCont.setLayout(new GridLayout(2, false));
		
		addTestDocBtn = new Button(testDocCont, SWT.PUSH);
		addTestDocBtn.setText("Add Test Document");
		addTestDocBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				CTabItem item = new CTabItem(testDocTabFolder, SWT.CLOSE);
				item.setText("Document " + testDocTabFolder.getItemCount());
				Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
						testDocTabFolder, store.getDoc());
				item.setControl(testDocOverviewCont); 
			}
		});
		
		useTestGtVersionChk = new Button(testDocCont, SWT.CHECK);
		useTestGtVersionChk.setText("Use Groundtruth versions");
		useTestGtVersionChk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for(ThumbnailWidgetVirtualMinimal tw : testTwList) {
					tw.setUseGtVersions(useTestGtVersionChk.getSelection());
				}
				super.widgetSelected(e);
			}
		});
		
		testDocTabFolder = new CTabFolder(testDocCont, SWT.BORDER | SWT.FLAT);
		testDocTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		CTabItem testItem = new CTabItem(testDocTabFolder, SWT.NONE);
		testItem.setText("Document 1");
		
		Composite testDocOverviewCont = createDocOverviewCont(testTwList, useTestGtVersionChk.getSelection(),
				testDocTabFolder, store.getDoc());
		testItem.setControl(testDocOverviewCont); 

		testDocTabFolder.setSelection(0);
		
		testDocTabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				System.out.println("GONE");
			}
		});
		
		docSash.setWeights(new int[] {50, 50});
		testDocCont.pack();
		trainDocCont.pack();
		
		docCont.pack();

		sash.setWeights(new int[] { 34, 66 });
		return cont;
	}

	private void setUroDefaults() {
		numEpochsTxt.setText("" + NUM_EPOCHS_DEFAULT);
		learningRateTxt.setText(LEARNING_RATE_DEFAULT);
		noiseCmb.select(NOISE_DEFAULT_CHOICE);
		trainSizeTxt.setText(""+ TRAIN_SIZE_DEFAULT);
		baseModelCmb.select(0);
	}

	private Composite createDocOverviewCont(List<ThumbnailWidgetVirtualMinimal> twList, boolean useGtVersions, CTabFolder parent, TrpDoc doc) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		c.setLayout(new GridLayout(1, false));
		
		Combo docCombo = new Combo(c, SWT.READ_ONLY);
		docCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		List<TrpDocMetadata> docList = store.getDocList();
		String[] items = new String[docList.size()];
		int selIndex = 0;
		for(int i = 0; i < docList.size(); i++) {
			TrpDocMetadata d = docList.get(i);
			items[i] = d.getDocId() + " - " + d.getTitle();
			if(doc != null && doc.getId() == d.getDocId()) {
				selIndex = i;
			}
		}
		docCombo.setItems(items);
		docCombo.select(selIndex);
		
		final ThumbnailWidgetVirtualMinimal tw = new ThumbnailWidgetVirtualMinimal(c, true, SWT.NONE);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		if(doc != null) {
			tw.setDoc(doc, useGtVersions);
		}
		twList.add(tw);
		
		docCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final int index = docCombo.getSelectionIndex();
				TrpDocMetadata d = store.getDocList().get(index);
				try {
					tw.setDoc(store.getRemoteDoc(store.getCollId(), d.getDocId(), -1), useGtVersions);
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		c.pack();
		return c;
	}
	
	@Override
	protected void okPressed() {
		if(!isConfigValid()) {
			return;
		}
		if(paramTabFolder.getSelection().equals(uroTabItem)) {
			conf = new UroHtrTrainConfig();
			conf.setDescription(descTxt.getText());
			conf.setModelName(modelNameTxt.getText());
			conf.setLanguage(langTxt.getText());
			
			conf.setNumEpochs(Integer.parseInt(numEpochsTxt.getText()));
			conf.setNoise(noiseCmb.getText());
			conf.setLearningRate(learningRateTxt.getText());
			conf.setTrainSizePerEpoch(Integer.parseInt(trainSizeTxt.getText()));
		} else {
			throw new IllegalArgumentException();
		}
		
		conf.setColId(store.getCollId());
		
		for(ThumbnailWidgetVirtualMinimal tw : trainTwList) {
			DocumentSelectionDescriptor dsd = tw.getSelection();
			
			if(dsd != null) {
				conf.getTrain().add(dsd);
			}
		}
		
		for(ThumbnailWidgetVirtualMinimal tw : testTwList) {
			DocumentSelectionDescriptor dsd = tw.getSelection();
			
			if(dsd != null) {
				conf.getTest().add(dsd);
			}
		}
		
		if(conf.getTrain().isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad configuration", 
					"Train set must not be empty!");
			return;
		}
		
		if(conf.isTestAndTrainOverlapping()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad configuration", 
					"Train and Test sets must not overlap!");
			return;
		}
		
		try {
			System.out.println(JaxbUtils.marshalToString(conf, DocumentSelectionDescriptor.class, PageDescriptor.class));
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.okPressed();
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

	private boolean isConfigValid() {
		String error = "";
		if(!isString(modelNameTxt)) {
			error += "Model Name must not be empty!\n";
		}
		if(!isString(descTxt)) {
			error += "Description must not be empty!\n";
		}
		if(!isString(langTxt)) {
			error += "Language must not be empty!\n";
		}
		if(paramTabFolder.getSelection().equals(uroTabItem)) {
			if(!isNumber(numEpochsTxt)) {
				error += "Number of Epochs must contain a number!\n";
			}
			if(!isString(learningRateTxt)) {
				error += "Learning rate must not be empty!\n";
			}
			if(!isNumber(trainSizeTxt)) {
				error += "Train size per epoch must contain a number!\n";
			}
		}
		if(!error.isEmpty()) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Bad Configuration", error);
		}
		return error.isEmpty();
	}
	
	private boolean isString(Text text) {
		return !text.getText().isEmpty();
	}
	
	private boolean isNumber(Text text) {
		if(text.getText().isEmpty()) {
			return false;
		}
		try {
			Integer.parseInt(text.getText());
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
}
