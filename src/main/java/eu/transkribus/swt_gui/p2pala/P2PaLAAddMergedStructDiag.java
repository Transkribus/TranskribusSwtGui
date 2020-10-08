package eu.transkribus.swt_gui.p2pala;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.LabeledCombo;

public class P2PaLAAddMergedStructDiag extends Dialog {
	
	String[] structures;
	LabeledCombo baseStructCombo, mergedStructCombo;
	
	String baseStruct, mergedStruct;

	public P2PaLAAddMergedStructDiag(Shell parentShell, String[] structures) {
		super(parentShell);
		this.structures = structures;
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(1, false));
		
		baseStructCombo = new LabeledCombo(container,  "Base structure: ");
		baseStructCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		baseStructCombo.setToolTipText("The structure that the merged structure is treated equals to");
		baseStructCombo.setItems(structures);
		
		mergedStructCombo = new LabeledCombo(container,  "Merged structure: ");
		mergedStructCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mergedStructCombo.setToolTipText("This is the structure that is treated equal as the base structure");
		mergedStructCombo.setItems(structures);
		
		Label l = new Label(container, 0);
		l.setText("(Merged structure is treated equal as base structure during training)");
		
		return container;
	}
	
	private void updateVals() {
		baseStruct = baseStructCombo.getCombo().getText();
		mergedStruct = mergedStructCombo.getCombo().getText();
	}
	
	private boolean isStructure(String s) {
		return Arrays.asList(structures).contains(s);
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected Point getInitialSize() {
		return new Point(450, 200);
	}
	
	@Override
	public void okPressed() {
		updateVals();
		
		if (StringUtils.isEmpty(baseStruct) || StringUtils.isEmpty(mergedStruct)) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid values", "Please specify base and merged structure type!");
			return;
		}
		
		if (!isStructure(baseStruct) || !isStructure(mergedStruct)) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid values", "No known structures types specified!");
			return;
		}		
		
		if (StringUtils.equals(baseStruct, mergedStruct)) {
			DialogUtil.showErrorMessageBox(getShell(), "Invalid values", "Merging equal structure types does not make any sense...");
			return;			
		}

		super.okPressed();
	}

	public String getBaseStruct() {
		return baseStruct;
	}

	public void setBaseStruct(String baseStruct) {
		this.baseStruct = baseStruct;
	}

	public String getMergedStruct() {
		return mergedStruct;
	}

	public void setMergedStruct(String mergedStruct) {
		this.mergedStruct = mergedStruct;
	}
	
	public String getConfig() {
		return this.baseStruct + ":" + this.mergedStruct;
	}

}
