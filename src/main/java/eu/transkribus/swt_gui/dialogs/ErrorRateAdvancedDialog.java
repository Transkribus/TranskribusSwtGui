package eu.transkribus.swt_gui.dialogs;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.TrpErrorRateResult;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.kws.AJobResultTableEntry;
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
	
	Combo comboRef;
	Combo comboHyp;
	Label labelRef;
	Label labelHyp;
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
		try {
			store.reloadDocWithAllTranscripts();
		} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Compare");
		shell.setMinimumSize(800, 600);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		sashFormOverall = new SashForm(composite,SWT.VERTICAL);
		sashFormOverall.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tabFolder = new CTabFolder(sashFormOverall,SWT.NONE);
		
		sashFormAdvance = new SashForm(tabFolder,SWT.VERTICAL);
		
		quickCompare = new CTabItem(tabFolder,SWT.NONE);
		quickCompare.setText("Compare");
		
		advanceCompare = new CTabItem(tabFolder,SWT.NONE);
		advanceCompare.setText("Advanced Compare");
		
		createConfig();
		
		refAndHypChooser();
		
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
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(config, SWT.NONE, true,false);
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		options = new LabeledCombo(config, "Options");
		options.combo.setItems("default (case sensitive) ","case insensitive");
		options.combo.select(0);

	
	}
	
	public void refAndHypChooser() {
		
		Composite comp = new Composite(sashFormAdvance,SWT.NONE);
		comp.setLayout(new GridLayout(4,false));
		
		labelRef = new Label(comp,SWT.NONE );
		labelRef.setText("Reference:");
		labelRef.setVisible(true);
		comboRef = new Combo(comp, SWT.DROP_DOWN);
		comboRef.setItems(new String[] {"GT"});
		comboRef.select(0);
		params.addParameter("ref", comboRef.getItem(comboRef.getSelectionIndex()));
		comboRef.setEnabled(false);
		comboRef.setVisible(true);
		labelHyp = new Label(comp,SWT.NONE );
		labelHyp.setText("Select hypothese by toolname:");
		labelHyp.setVisible(true);
		comboHyp = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboHyp.setVisible(true);
		try {
			List<TrpPage> pages = store.getDoc().getPages();
			for(TrpPage page : pages) {
				List<TrpTranscriptMetadata> transcripts = page.getTranscripts();
				for(TrpTranscriptMetadata transcript : transcripts){
					if(transcript.getToolName() != null) {
						String[] items = comboHyp.getItems();
						if(!Arrays.stream(items).anyMatch(transcript.getToolName()::equals)) {
							comboHyp.add(transcript.getToolName());
						}
					}
					
				}
				
			}
		} catch (ServerErrorException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		if(comboHyp.getItemCount() != 0) {
			comboHyp.select(0);
			params.addParameter("hyp", comboHyp.getItem(comboHyp.getSelectionIndex()));
		}
		compare = new Button(comp,SWT.PUSH);
		compare.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 4, 4));
		compare.setText("Compare");
		
	}
	
	private void addListener() {
		
		comboHyp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				params.addParameter("hyp", comboHyp.getItem(comboHyp.getSelectionIndex()));
			}
		});
		
		comboRef.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				params.addParameter("ref", comboRef.getItem(comboRef.getSelectionIndex()));
			}
		});
		
		compare.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				params.addParameter("option", options.combo.getSelectionIndex());
				logger.debug("Option chosen : "+options.combo.getSelectionIndex());
				String newPageString = null;
				String deleteGTPageString = null;
				String deleteHypPageString = null;
					try {
						// create new pagestring, take only pages with chosen toolname
						TrpDoc doc = store.getConnection().getTrpDoc(store.getCollId(), store.getDocId(), 10);
						Set<Integer> pageIndices = CoreUtils.parseRangeListStr(dps.getPagesStr(), store.getDoc().getNPages());
						Set<Integer> newPageIndices = new HashSet<Integer>();
						Set<Integer> delGTIndices = new HashSet<Integer>();
						Set<Integer> delHypIndices = new HashSet<Integer>();
						List<TrpTranscriptMetadata> transcripts = new ArrayList<TrpTranscriptMetadata>();
						for (Integer pageIndex : pageIndices) {
							logger.debug("pageIndex : "+pageIndex);
							transcripts = doc.getPages().get(pageIndex).getTranscripts();
							// check if all pages contain GT version
							TrpTranscriptMetadata transGT = doc.getPages().get(pageIndex).getTranscriptWithStatusOrNull(EditStatus.GT);
//							
							for(TrpTranscriptMetadata transcript : transcripts){
								logger.debug(""+comboHyp.getSelectionIndex());
								if(transGT != null && transcript.getToolName() != null) {
									if(comboHyp.getItem(comboHyp.getSelectionIndex()) != null &&  transcript.getToolName().equals(comboHyp.getItem(comboHyp.getSelectionIndex()))) {
										newPageIndices.add(pageIndex);
									}
								}
								if(transGT == null) {
									delGTIndices.add(pageIndex);
								}
							}
							if(!newPageIndices.contains(pageIndex) && !delGTIndices.contains(pageIndex)) {
								delHypIndices.add(pageIndex);
							}
						}
						newPageString = CoreUtils.getRangeListStrFromSet(newPageIndices);
						deleteGTPageString = CoreUtils.getRangeListStrFromSet(delGTIndices);
						deleteHypPageString = CoreUtils.getRangeListStrFromSet(delHypIndices);
						String msg = "";
						msg += "Compute error rate for page(s) :" + newPageString + "\n";
						msg += "Pages ignored for missing GT : " + deleteGTPageString + "\n";
						msg += "Pages ignored for missing Hyp : " + deleteHypPageString + "\n";
						msg += "Ref: " +params.getParameterValue("ref")+"\n";
						msg += "Hyp: " +params.getParameterValue("hyp");
						if(params.getParameterValue("ref") != null && params.getParameterValue("hyp") != null && !StringUtils.isEmpty(newPageString)) {
							rl.setStopped();
							int result = DialogUtil.showYesNoDialog(getShell(), "Start?", msg);
							if (result == SWT.YES) {
								startError(store.getDocId(), newPageString);
							}
						}
						else if (StringUtils.isEmpty(newPageString)) {
							DialogUtil.showErrorMessageBox(getShell(), "Error", "No pages chosen, reasons for skipping pages below: \nPages ignored for missing GT : " + deleteGTPageString +"\nPages ignored for missing Hyp : " + deleteHypPageString);
						}
						else {
							DialogUtil.showErrorMessageBox(getShell(), "Error", "The hypothesis and reference must be set for the computation");
						}
					} catch (IOException | SessionExpiredException | ServerErrorException | ClientErrorException e1) {
						e1.printStackTrace();
					} 
					
		
				
			}
			
		});
		
		computeWerBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);

				TrpTranscriptMetadata ref = (TrpTranscriptMetadata) refVersionChooser.selectedMd;
				TrpTranscriptMetadata hyp = (TrpTranscriptMetadata) hypVersionChooser.selectedMd;

				if (ref != null && hyp != null) {
					params.addIntParam("option", 0);
					params.addParameter("hyp", hyp.getToolName());
					params.addParameter("ref", ref.getToolName());
					rl.setStopped();
						try {
							startError(store.getDocId(),""+store.getPage().getPageNr());
						} catch (ServerErrorException | IllegalArgumentException e1) {
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
		resultGroup.setText("Previous Advanced Compare Results");
		resultGroup.setLayout(groupLayout);
		resultGroup.setLayoutData(groupGridData);
		
		resultTable = new KwsResultTableWidget(resultGroup,0);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		resultTable.getTableViewer().addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TrpErrorResultTableEntry entry = (TrpErrorResultTableEntry) resultTable.getSelectedEntry();
				if(entry != null && entry.getStatus().equals("Completed") ) {
					int docId = store.getDocId();
					String query = entry.getQuery();
					TrpErrorRate result = entry.getResult();
					ErrorRateAdvancedStats stats = new ErrorRateAdvancedStats(getShell(), result,docId,query);
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
		computeWerBtn.setText("Compare");
		computeWerBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 0, 1));
		computeWerBtn.setToolTipText("Compares the two selected transcripts and computes word error rate and character error rate.");
		
		quickCompare.setControl(werGroup);
	}

	protected void startError(int docID, String pageString) {

		try {
			store.getConnection().computeErrorRateWithJob(docID, pageString, params);
			rl = new ResultLoader();
			rl.start();
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
		
		if(jobs != null && jobs.size() != 0 && jobs.get(0).isFinished()) {
			rl.setStopped();
		}
		Display.getDefault().asyncExec(() -> { 
			AJobResultTableEntry<?> e = resultTable.getSelectedEntry();
			if(resultTable != null && !resultTable.isDisposed()) {
				resultTable.getTableViewer().setInput(errorList);
			}
			if(e != null) {
				TrpErrorResultTableEntry o = (TrpErrorResultTableEntry)e;
				int index = errorList.indexOf(o);
				resultTable.getTableViewer().getTable().select(index);
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
