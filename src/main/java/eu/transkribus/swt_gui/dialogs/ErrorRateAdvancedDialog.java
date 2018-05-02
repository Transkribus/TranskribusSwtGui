package eu.transkribus.swt_gui.dialogs;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.search.kws.KwsResultTableWidget;
import eu.transkribus.swt_gui.util.CurrentTranscriptOrCurrentDocPagesSelector;

public class ErrorRateAdvancedDialog extends Dialog {
	

	private TrpTranscriptMetadata ref;
	private TrpTranscriptMetadata hyp;
	private Composite composite;
	private KwsResultTableWidget resultTable;
	private Group resultGroup;
	private CurrentTranscriptOrCurrentDocPagesSelector dps;
	private Combo options;
	

	public ErrorRateAdvancedDialog(Shell parentShell, TrpTranscriptMetadata ref, TrpTranscriptMetadata hyp) {
		
		super(parentShell);
		this.ref = ref;
		this.hyp = hyp;

	}
	
	public void createConfig() {
		
		Composite config = new Composite(composite,SWT.NONE);
		
		config.setLayout(new GridLayout(2,false));
		
		dps = new CurrentTranscriptOrCurrentDocPagesSelector(config, SWT.NONE, true);		
		dps.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		
		options = new Combo(config,SWT.READ_ONLY);
		String items[] = {"normcompatibility","normcanonic","case sensitive"};
		options.setItems(items);
		
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

}
