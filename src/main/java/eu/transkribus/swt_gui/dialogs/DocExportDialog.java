package eu.transkribus.swt_gui.dialogs;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.SWTUtil;

/**
 * @deprecated not used anymore
 *
 */
public class DocExportDialog extends Dialog {

	Shell shell;
	ExportPathComposite exportPathComp;

	String lastExportFolder;
	File result=null;
	String docName;

	public DocExportDialog(Shell parent, int style, String lastExportFolder, String docName) {
		super(parent, style |= SWT.DIALOG_TRIM);
		this.lastExportFolder = lastExportFolder;
		this.docName = docName;
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
//		shell.setSize(673, 420);
		shell.setSize(300, 300);
		shell.setText("Export document");
		shell.setLayout(new GridLayout(1, false));
		
		exportPathComp = new ExportPathComposite(shell, lastExportFolder, "Folder name: ", null, docName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		buttonComposite.setLayout(new FillLayout());
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (exportPathComp.checkExportFile()) {
					result = exportPathComp.getExportFile();
					shell.close();
				}
			}
		});
//		saveButton.setToolTipText("Stores the configuration in the configuration file and closes the dialog");
//		
		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
//		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public File open() {
		result = null;
		createContents();
		SWTUtil.centerShell(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
}
