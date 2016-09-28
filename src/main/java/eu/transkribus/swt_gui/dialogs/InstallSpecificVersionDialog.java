package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.program_updater.FTPProgramPackageFile;
import eu.transkribus.core.program_updater.ProgramPackageFile;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.util.FTPProgramUpdater;
import eu.transkribus.swt_gui.util.ProgramUpdater;

public class InstallSpecificVersionDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(InstallSpecificVersionDialog.class);
	
	protected int result;
	protected Shell shell;
	
	Combo releasesCombo, snapshotsCombo;
	Button releasesRadio, snapshotsRadio;
	Button update, replace, cancel, downloadAll;
	
	List<ProgramPackageFile> releases, snapshots;
//	List releases, snapshots;
	
	ProgramPackageFile selected=null;
	boolean isDownloadAll=false;
	
	Label timestampRelease, timestampSnapshots;
	
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public InstallSpecificVersionDialog(Shell parent, int style) {
		super(parent, style|= (SWT.DIALOG_TRIM | SWT.RESIZE) );
		setText("Install a specific version of the tool");
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
		
		releasesRadio = new Button(shell, SWT.RADIO);
		releasesRadio.setText("Releases: ");
//		releasesRadio.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				releasesCombo.setEnabled(releasesRadio.getSelection());
//			}
//		});
		releasesRadio.setSelection(true);		
		releasesCombo = new Combo(shell, SWT.READ_ONLY);
		releasesCombo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		timestampRelease = new Label(shell, SWT.NONE);
		timestampRelease.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		snapshotsRadio = new Button(shell, SWT.RADIO);
		snapshotsRadio.setText("Snapshots: ");
//		snapshotsRadio.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				releasesCombo.setEnabled(releasesRadio.getSelection());
//			}
//		});
		snapshotsCombo = new Combo(shell, SWT.READ_ONLY);
		snapshotsCombo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		
		timestampSnapshots = new Label(shell, SWT.NONE);
		timestampSnapshots.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));		
		
		Label label = new Label(shell, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, nCols, 1));
		
//		 updates the application with the new version keeping your customized configuration files";
//			msg += "\n Replace: updates the application and overwrites all configuration files";
//			msg += "\n Cancel: cancel the operation
		
		update = new Button(shell, SWT.PUSH);
		update.setText("Update");
		update.setToolTipText("Updates the application with the selected version above");
		update.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateAndClose(replace.getSelection() ? 1 : 0);
			}
		});
		
		replace = new Button(shell, SWT.CHECK);
		replace.setText("Replace config files");
		replace.setToolTipText("Check this to overwrite locally modified configuation files during the update");
//		replace.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				saveFileAndClose(1);
//			}
//		});
		
		downloadAll = new Button(shell, SWT.CHECK);
		downloadAll.setText("Download complete package");
		downloadAll.setToolTipText("By default only the updated libs are downloaded to decrease the size - check this to download the complete package!");
		downloadAll.setSelection(false);
		
		
		cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = 2;
				shell.close();
			}
		});
		
		try {
			initData();
			
			SelectionListener combosListener = new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					updateTimestamps();
				}
			};
			
			releasesCombo.addSelectionListener(combosListener);
			snapshotsCombo.addSelectionListener(combosListener);
			
			shell.pack();
		} catch (Exception e1) {
			logger.debug(e1.getMessage(), e1);
			DialogUtil.showErrorMessageBox(shell, "Could not retrieve versions", "The list of available versions could not be retrieved - check your internet connection!");
			result = -1;
			shell.close();
		}
	}
	
	private void updateAndClose(int result) {
		this.result = result;
		
		isDownloadAll = downloadAll.getSelection();
		if (snapshotsRadio.getSelection()) {
			selected = (ProgramPackageFile) snapshots.get(snapshotsCombo.getSelectionIndex());
		} else {
			selected = (ProgramPackageFile) releases.get(releasesCombo.getSelectionIndex());
		}
		logger.debug("Selected file: "+selected.getName()+" version: "+selected.getName());
		
		shell.close();
	}
	
	private void fillCombo(List<ProgramPackageFile> files, Combo combo) {
		List<String> versions = new ArrayList<>();
		for (ProgramPackageFile f : files) {
			logger.debug("name = "+f.getName()+" f = "+f.toString());
			versions.add(ProgramPackageFile.stripVersion(f.getName()));
		}
		combo.setItems(versions.toArray(new String[0]));
		combo.select(versions.size()-1);
	}
	
	private void initData() throws Exception {
		releases = ProgramUpdaterDialog.PROGRAM_UPDATER.getAllReleases();
		fillCombo(releases, releasesCombo);
		snapshots = ProgramUpdaterDialog.PROGRAM_UPDATER.getAllSnapshots();
		fillCombo(snapshots, snapshotsCombo);
		updateTimestamps();
	}
	
	private void updateTimestamps() {
		int ri = releasesCombo.getSelectionIndex();
		int si = snapshotsCombo.getSelectionIndex();
		
		timestampRelease.setText("Built: "+((ProgramPackageFile) (releases.get(ri))).getTimestamp());
		timestampSnapshots.setText("Built: "+((ProgramPackageFile) (snapshots.get(si))).getTimestamp());
		shell.pack();
	}
	
	public ProgramPackageFile getSelectedFile() { 
		return selected;
	}
	
	public boolean isDownloadAll() {
		return isDownloadAll;
	}

}
