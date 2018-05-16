package eu.transkribus.swt_gui.dialogs;


import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledComboWithButton;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.kws.KwsResultTableWidget;
import eu.transkribus.swt_gui.tool.error.TrpErrorResultTableEntry;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class ErrorRateAdvancedDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedDialog.class);
	
	
	
	Storage store;
	private Composite composite;
	private KwsResultTableWidget resultTable;
	private Group resultGroup;
	private CurrentTranscriptOrCurrentDocPagesSelector dps;
	private LabeledCombo options;
	final ParameterMap params = new ParameterMap();


	public ErrorRateAdvancedDialog(Shell parentShell) {
		
		super(parentShell);
		store = Storage.getInstance();
	}
	
	public void createConfig() {
		
		Composite config = new Composite(composite,SWT.NONE);
		
		config.setLayout(new GridLayout(3,false));
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(config, SWT.NONE, true);		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		
		options = new LabeledComboWithButton(config,"Options","Compare");
		options.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false,1,1));
		options.combo.setItems(" ","normcompatibility","normcanonic","non-case-sensitive");
		
		addListener();
		
	
	}
	private void addListener() {
		
		options.combo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				logger.debug("Selected Combo "+options.combo.getSelectionIndex());			
			}
		});
		
		((LabeledComboWithButton)options).getButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				params.addIntParam("option", options.combo.getSelectionIndex());
				startError();
			}
			
		});
		
	}

	public void createJobTable() {
		
		Composite jobs = new Composite(composite,SWT.NONE);
		
		jobs.setLayout(new GridLayout(1,false));
		jobs.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		
		GridLayout groupLayout = new GridLayout(1, false);
		GridData groupGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		
		resultGroup = new Group(jobs, SWT.NONE);
		resultGroup.setText("Previous Compare Results");
		resultGroup.setLayout(groupLayout);
		resultGroup.setLayoutData(groupGridData);
		
		resultTable = new KwsResultTableWidget(resultGroup, SWT.BORDER);
		resultTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		resultTable.getTableViewer().addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TrpErrorResultTableEntry entry = (TrpErrorResultTableEntry) resultTable.getSelectedEntry();
				if(entry.getResult() != null) {
					try {
						logger.debug(JaxbUtils.marshalToString(entry.getResult(), true, TrpErrorRate.class));
					} catch (JAXBException e) {
						logger.error("Could not read result.", e);
					}
					ErrorRateAdvancedStats stats = new ErrorRateAdvancedStats(getShell(), entry.getResult());
					stats.open();
				}
			}
		});
	}
	
	public TrpCollection getCurrentCollection() {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		return mw.getUi().getServerWidget().getSelectedCollection();
	}
	
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Advanced Compare");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		createConfig();
		
		createJobTable();
		
		return composite;
	}
	
	protected void startError() {

		try {
			logger.debug("Store ID :" + store.getDocId());
			logger.debug("Page String : "+ dps.getPagesStr());
			logger.debug("Parameter Query " + params.getIntParam("option"));
			store.getConnection().computeErrorRateWithJob(store.getDocId(), dps.getPagesStr(), params);
			
		} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException e) {
			logger.error(e.getMessage(), e);
			DialogUtil.showErrorMessageBox(getShell(), "Something went wrong.", e.getMessageToUser());
			return;
		} 
		
	}


}
