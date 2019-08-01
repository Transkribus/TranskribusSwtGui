package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDoc;

public class DocSyncWithTxtFilesDialog extends DocSyncWithFilesDialog {
	private static final Logger logger = LoggerFactory.getLogger(DocSyncWithTxtFilesDialog.class);

	protected Button useExistingLayoutBtn;
	
	public static final String TYPE_OF_FILES = "text files";
	
	public DocSyncWithTxtFilesDialog(Shell parentShell) {
		super(parentShell, TYPE_OF_FILES);
	}
	
	public DocSyncWithTxtFilesDialog(Shell parentShell, TrpDoc target, List<File> sourceFiles) {
		super(parentShell, TYPE_OF_FILES, target, sourceFiles);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		useExistingLayoutBtn = new Button(container, SWT.CHECK);
		useExistingLayoutBtn.setText("Use existing layout");
		useExistingLayoutBtn.setToolTipText("If checked, text is assigned to the lines of the existing layout rather than creating new (dummy) lines");
		
		return container;
	}
	
	@Override protected void setData() {
		this.data = (Boolean) useExistingLayoutBtn.getSelection();
	}

}
