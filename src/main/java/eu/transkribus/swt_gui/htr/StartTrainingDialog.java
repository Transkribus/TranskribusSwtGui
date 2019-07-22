package eu.transkribus.swt_gui.htr;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.Colors;

public class StartTrainingDialog extends Dialog {

	List<DataSetMetadata> trainSetMd, validationSetMd;
	boolean insufficientTrainData = false, insufficientValidationData = false;
	
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
		trainGrp.setLayout(gl);
		trainGrp.setLayoutData(gd);
		
		DataSetMetadataTableWidget trainMdTable = new DataSetMetadataTableWidget(trainGrp, SWT.NONE);
		trainMdTable.setInput(trainSetMd);
		
		insufficientTrainData = trainMdTable.getTotalDataSetMetadata().isDataSetEmpty();
		if (insufficientTrainData) {
			createErrorLabel(container, "Insufficient transcribed pages in train set!"); 
		}
		
		Group valGrp = new Group(container, SWT.NONE);
		
		//Keep this named "test set" for now
		valGrp.setText("Test Set:");
//		valGrp.setText("Validation Set:");
		valGrp.setLayout(gl);
		valGrp.setLayoutData(gd);
		
		DataSetMetadataTableWidget valMdTable = new DataSetMetadataTableWidget(valGrp, SWT.NONE);
		valMdTable.setInput(validationSetMd);
		
		insufficientValidationData = valMdTable.getTotalDataSetMetadata().isDataSetEmpty();
		if (insufficientValidationData) {
			createErrorLabel(container, "Insufficient transcribed pages in test set!");
		}
		
		return container;
	}	
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		Button startBtn = getButton(IDialogConstants.OK_ID);
		startBtn.setText("Start Training");
		//disable start button if selected data is insufficient
		startBtn.setEnabled(!insufficientTrainData && !insufficientValidationData);
	}
	
	private Label createErrorLabel(Composite comp, final String errorMsg) {
		Label lbl = new Label(comp, SWT.WRAP);
		lbl.setText(errorMsg);
		lbl.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
		lbl.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, false));
		return lbl;
	}
}
