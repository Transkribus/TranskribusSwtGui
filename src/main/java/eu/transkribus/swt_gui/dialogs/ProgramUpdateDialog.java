package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;

public class ProgramUpdateDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(InstallSpecificVersionDialog.class);
	
	protected int result;
	protected Shell shell;
	
	Label msgLabel;
	Button update, replace, cancel, downloadAll;

	ProgramPackageFile selected=null;
	boolean isDownloadAll=false;
	boolean isReplaceConfigFiles=false;
	
	String msg;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ProgramUpdateDialog(Shell parent, int style, String msg) {
		super(parent, style|= (SWT.DIALOG_TRIM | SWT.RESIZE) );
		setText("New version found");
		
		this.msg = msg;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public int open() {
		createContents();
		if (shell.isDisposed())
			return result;
		
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
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(547, 333);
		shell.setText(getText());
		int nCols = 3;
		shell.setLayout(new GridLayout(nCols, false));
		
		msgLabel = new Label(shell, 0);
		msgLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		msgLabel.setText(msg);		
		
		update = new Button(shell, SWT.PUSH);
		update.setText("Update");
		update.setToolTipText("Updates the application");
		update.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				saveFileAndClose(1);
			}
		});
		
		replace = new Button(shell, SWT.CHECK);
		replace.setText("Replace config files");
		replace.setToolTipText("Check this to overwrite locally modified configuation files during the update");
		
		downloadAll = new Button(shell, SWT.CHECK);
		downloadAll.setText("Download complete package");
		downloadAll.setToolTipText("By default only the updated libs are downloaded to decrease the size - check this to download the complete package!");
		downloadAll.setSelection(false);
		
		cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = 0;
				shell.close();
			}
		});
		
		try {
//			initData();
			
//			SelectionListener combosListener = new SelectionAdapter() {
//				@Override public void widgetSelected(SelectionEvent e) {
//					updateTimestamps();
//				}
//			};
			
			shell.pack();
		} catch (Exception e1) {
			logger.debug(e1.getMessage(), e1);
			DialogUtil.showErrorMessageBox(shell, "Could not retrieve versions", "The list of available versions could not be retrieved - check your internet connection!");
			result = -1;
			shell.close();
		}
	}
	
	private void saveFileAndClose(int result) {
		this.result = result;
		isDownloadAll = downloadAll.getSelection();
		isReplaceConfigFiles = replace.getSelection();
		
		shell.close();
	}
	
//	private void fillCombo(List<ProgramPackageFile> files, Combo combo) {
//		List<String> versions = new ArrayList<>();
//		for (ProgramPackageFile f : files) {
//			logger.debug("name = "+f.getName()+" f = "+f.toString());
//			versions.add(ProgramPackageFile.stripVersion(f.getName()));
//		}
//		combo.setItems(versions.toArray(new String[0]));
//		combo.select(versions.size()-1);
//	}
//	
//	private void initData() throws Exception {
//		releases = ProgramUpdaterDialog.PROGRAM_UPDATER.getAllReleases();
//		fillCombo(releases, releasesCombo);
//		snapshots = ProgramUpdaterDialog.PROGRAM_UPDATER.getAllSnapshots();
//		fillCombo(snapshots, snapshotsCombo);
//		updateTimestamps();
//	}
	
//	private void updateTimestamps() {
//		int ri = releasesCombo.getSelectionIndex();
//		int si = snapshotsCombo.getSelectionIndex();
//		
//		timestampRelease.setText("Built: "+((ProgramPackageFile) (releases.get(ri))).getTimestamp());
//		timestampSnapshots.setText("Built: "+((ProgramPackageFile) (snapshots.get(si))).getTimestamp());
//		shell.pack();
//	}
	
	public ProgramPackageFile getSelectedFile() { 
		return selected;
	}
	
	public boolean isDownloadAll() {
		return isDownloadAll;
	}
	
	public boolean isReplaceConfigFiles() {
		return isReplaceConfigFiles;
	}

}

