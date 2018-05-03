package eu.transkribus.swt_gui.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.kws.KwsResultTableWidget;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class ErrorRateAdvancedDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedDialog.class);
	
	
	
	Storage store;
	private Composite composite;
	private KwsResultTableWidget resultTable;
	private Group resultGroup;
	private CurrentTranscriptOrCurrentDocPagesSelector dps;
	private Combo options;
	private Button compare;
	
//	final int docId = store.getDocId();
//	final String pageStr = dps.getPagesStr();
//	final ParameterMap params = null;
	

	public ErrorRateAdvancedDialog(Shell parentShell) {
		
		super(parentShell);
		store = Storage.getInstance();
	}
	
	public void createConfig() {
		
		Composite config = new Composite(composite,SWT.NONE);
		
		config.setLayout(new GridLayout(3,false));
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(config, SWT.NONE, true);		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		options = new Combo(config,SWT.READ_ONLY);
		String items[] = {" ","normcompatibility","normcanonic","non-case-sensitive"};
		options.setItems(items);
		
		options.addSelectionListener(new SelectionAdapter() {
		      public void widgetDefaultSelected(SelectionEvent e) {
//		        params.addParameter("option", options.getText());
		      }
		    });
		
		compare = new Button(config,SWT.PUSH);
		compare.setText("Compare");
		compare.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
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

//		try {
//			store.getConnection().computeErrorRateWithJob(docId, pageStr, params);
//			
//		} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException e) {
//			logger.error(e.getMessage(), e);
//			DialogUtil.showErrorMessageBox(getShell(), "Something went wrong.", e.getMessageToUser());
//			return;
//		} 
//		
	}


}
