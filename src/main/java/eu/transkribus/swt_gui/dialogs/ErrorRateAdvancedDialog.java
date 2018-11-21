package eu.transkribus.swt_gui.dialogs;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.kws.KwsResultTableWidget;
import eu.transkribus.swt_gui.tool.error.TrpErrorResultTableEntry;
import eu.transkribus.swt_gui.tools.ToolsWidget.TranscriptVersionChooser;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class ErrorRateAdvancedDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedDialog.class);
	
	Storage store;
	private Composite composite;
	private SashForm sashFormOverall,sashFormAdvance;
	private CTabFolder tabFolder;
	private CTabItem advanceCompare;
	private CTabItem quickCompare;
	private KwsResultTableWidget resultTable;
	private Group resultGroup;
	private CurrentTranscriptOrCurrentDocPagesSelector dps;
	private LabeledCombo options;
	private Button compare, wikiOptions;
	private ParameterMap params = new ParameterMap();
	ResultLoader rl;
	
	TranscriptVersionChooser refVersionChooser, hypVersionChooser;
	
	Button computeWerBtn;
	Button computeAdvancedBtn;
	Button compareVersionsBtn;
	Composite werGroup;
	ExpandableComposite werExp;
	
	private IStorageListener storageListener;

	protected static final String HELP_WIKI_OPTION = "https://en.wikipedia.org/wiki/Unicode_equivalence";
	
	public final static boolean IS_LEGACY_TAB = true;

	public ErrorRateAdvancedDialog(Shell parentShell) {
		super(parentShell);
		store = Storage.getInstance();
		rl = new ResultLoader();
		setShellStyle(getShellStyle() | SWT.RESIZE);

	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Compare");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		sashFormOverall = new SashForm(composite,SWT.VERTICAL);
		
		tabFolder = new CTabFolder(sashFormOverall,SWT.NONE);
		
		sashFormAdvance = new SashForm(tabFolder,SWT.VERTICAL);
		
		quickCompare = new CTabItem(tabFolder,SWT.NONE);
		quickCompare.setText("Quick Compare");
		
		advanceCompare = new CTabItem(tabFolder,SWT.NONE);
		advanceCompare.setText("Advanced Compare");
		
		createConfig();
		
		createExplainText();
		
		createJobTable();
		
		createQuickTab();
		
		rl.start();
		this.composite.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				logger.debug("Disposing ErrorRateAdvancedDialog composite.");
				rl.setStopped();
				store.removeListener(storageListener);
			}
		});
		
		advanceCompare.setControl(sashFormAdvance);
		addListener();
		return composite;
	}
	
	public void createConfig() {
		
		Composite config = new Composite(sashFormAdvance,SWT.NONE);
		
		config.setLayout(new GridLayout(3,false));
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(config, SWT.NONE, true);
		dps.getCurrentTranscriptButton().setText("Current page");
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		options = new LabeledCombo(config, "Options");
		options.combo.setItems("default (case sensitive) ","case insensitive");
		options.combo.select(0);

		compare = new Button(config,SWT.PUSH);
		compare.setText("Compare");
	
	}
	
	public void createExplainText() {
		
		Composite textComp = new Composite(sashFormAdvance,SWT.NONE);
		textComp.setLayout(new GridLayout(3,false));
		Text text = new Text(textComp, SWT.FILL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		text.setText("Compares the latest GT with latest version available (if no GT given compares the two latest versions)");
	}
	
	private void addListener() {
		
		options.combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				logger.debug("Selected Combo "+options.combo.getSelectionIndex());			
			}
		});
		
		compare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				params.addParameter("option", options.combo.getSelectionIndex());
				if(dps.isCurrentTranscript()) {
					startError(store.getDocId(),""+store.getPage().getPageNr());
				}else {
					startError(store.getDocId(), dps.getPagesStr());
				}
				
			}
			
		});
		
		storageListener = new IStorageListener() {
			public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
				if (SWTUtil.isDisposed(refVersionChooser) || SWTUtil.isDisposed(hypVersionChooser)) {
					return;
				}
				
				refVersionChooser.setToGT();
				hypVersionChooser.setToCurrent();
			}
		};
		store.addListener(storageListener);
		
		computeWerBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);

				TrpTranscriptMetadata ref = (TrpTranscriptMetadata) refVersionChooser.selectedMd;
				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata) hypVersionChooser.selectedMd;

				if (ref != null && hyp != null) {
					params.addIntParam("option", -1);
						try {
							store.computeErrorRate(ref.getDocId(), ""+ref.getPageNr(), params);
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e1) {
							e1.printStackTrace();
						}
				}
			}
		});
		
	}

	public void createJobTable() {
		
		Composite jobs = new Composite(sashFormOverall,SWT.NONE);
		
		jobs.setLayout(new GridLayout(1,false));
		jobs.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
		
		GridLayout groupLayout = new GridLayout(1, true);
		GridData groupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		
		resultGroup = new Group(jobs, SWT.FILL);
		resultGroup.setText("Previous Compare Results");
		resultGroup.setLayout(groupLayout);
		resultGroup.setLayoutData(groupGridData);
		
		resultTable = new KwsResultTableWidget(resultGroup,0);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		resultTable.getTableViewer().addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TrpErrorResultTableEntry entry = (TrpErrorResultTableEntry) resultTable.getSelectedEntry();
				if(entry != null && entry.getStatus().equals("Completed") ) {
					Integer docId = store.getDocId();
					ErrorRateAdvancedStats stats = new ErrorRateAdvancedStats(getShell(), entry.getResult(),docId);
					stats.open();
					}
				}
			});
	}
	
	public TrpCollection getCurrentCollection() {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		return mw.getUi().getServerWidget().getSelectedCollection();
	}

	private void createQuickTab() {

		werGroup = new Composite(tabFolder, SWT.SHADOW_ETCHED_IN);
		werGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		werGroup.setLayout(new GridLayout(2, false));
		
		refVersionChooser = new TranscriptVersionChooser("Reference:\n(Correct Text) ", werGroup, 0);
		refVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		refVersionChooser.setToGT();
		
		hypVersionChooser = new TranscriptVersionChooser("Hypothesis:\n(HTR Text) ", werGroup, 0);
		hypVersionChooser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		hypVersionChooser.setToCurrent();
				
		computeWerBtn = new Button(werGroup, SWT.PUSH);
		computeWerBtn.setText("Quick Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 0, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
		
		quickCompare.setControl(werGroup);
	}

	protected void startError(int docID, String pageString) {

		try {
			store.getConnection().computeErrorRateWithJob(docID, pageString, params);
		} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException e) {
			logger.error(e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Something went wrong.", e.getMessageToUser());
			return;
		} 
		
	}
	
	private void updateResultTable(List<TrpJobStatus> jobs) {
		List<TrpErrorResultTableEntry> errorList = new LinkedList<>();

		for(TrpJobStatus j : jobs) {
			errorList.add(new TrpErrorResultTableEntry(j));
		}
		
		Display.getDefault().asyncExec(() -> {	
			if(resultTable != null && !resultTable.isDisposed()) {
				logger.debug("Updating Error result table");
				resultTable.getTableViewer().setInput(errorList);
			}
		});
	}
	
	
	private class ResultLoader extends Thread {
		private final static int SLEEP = 3000;
		private boolean stopped = false;
		
		@Override
		public void run() {
			logger.debug("Starting result polling.");
			while(!stopped) {
				List<TrpJobStatus> jobs;
				try {
					jobs = this.getErrorJobs();
					updateResultTable(jobs);
				} catch (ServerErrorException | ClientErrorException
						| IllegalArgumentException e) {
					logger.error("Could not update ResultTable!", e);
				}
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
					logger.error("Sleep interrupted.", e);
				}
			}
		}
		private List<TrpJobStatus> getErrorJobs()  {
			Integer docId = store.getDocId();
			List<TrpJobStatus> jobs = new ArrayList<>();
			if (store != null && store.isLoggedIn()) {
				try {
					jobs = store.getConnection().getJobs(true, null, JobImpl.ErrorRateJob.getLabel(), docId, 0, 0, null, null);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| IllegalArgumentException e) {	
					logger.error("Could not load Jobs!");
				}
			}
			return jobs;
		}
		public void setStopped() {
			logger.debug("Stopping result polling.");
			stopped = true;
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		wikiOptions = createButton(parent, IDialogConstants.HELP_ID, "Options", false);
		wikiOptions.setImage(Images.HELP);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();	
		
		wikiOptions.setLayoutData(buttonLd);
		wikiOptions.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_OPTION, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});


	}

}
