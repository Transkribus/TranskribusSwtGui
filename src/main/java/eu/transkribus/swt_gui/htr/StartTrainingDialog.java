package eu.transkribus.swt_gui.htr;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class StartTrainingDialog extends Dialog {

	List<DataSetMetadata> trainSetMd;
	List<DataSetMetadata> validationSetMd;
	
	public StartTrainingDialog(Shell parentShell, List<DataSetMetadata> trainSetMd, List<DataSetMetadata> validationSetMd) {
		super(parentShell);
		
		this.trainSetMd = trainSetMd;
		this.validationSetMd = validationSetMd;
	}
	
	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Dataset Overview");
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		final GridLayout gl = new GridLayout(1, true);
		final GridData gd = new GridData(GridData.FILL);
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(gl);
		
		Group trainGrp = new Group(container, SWT.NONE);
		trainGrp.setText("Train Set:");
		trainGrp.setLayoutData(gd);
		trainGrp.setLayout(gl);
		
		DataSetMetadataTableWidget trainMdTable = new DataSetMetadataTableWidget(trainGrp, SWT.NONE);
		trainMdTable.setLayoutData(gd);
		trainMdTable.setInput(trainSetMd);
		
		Group valGrp = new Group(container, SWT.NONE);
		valGrp.setText("Validation Set:");
		valGrp.setLayoutData(gd);
		valGrp.setLayout(gl);
		
		DataSetMetadataTableWidget valMdTable = new DataSetMetadataTableWidget(valGrp, SWT.NONE);
		valMdTable.setLayoutData(gd);
		valMdTable.setInput(validationSetMd);
		
		return container;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setText("Start Training"); 
	}
}
