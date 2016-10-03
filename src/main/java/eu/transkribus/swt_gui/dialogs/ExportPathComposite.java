package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;

public class ExportPathComposite extends Composite {
	
	Text baseFolderText, fileOrFolderText;
	String lastExportFolder;
	String extension = null;
	
	Label pathLabel;

	public ExportPathComposite(Composite parent, final String lastExportFolder, String fileOrFolderLabel, String extension, String docName) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(3, false));
		this.extension = extension;
		
		Label labelBaseFolder = new Label(this, SWT.NONE);
		labelBaseFolder.setText("Base folder: ");
		labelBaseFolder.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		
		baseFolderText = new Text(this, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd.widthHint = 300;
		baseFolderText.setLayoutData(gd);
		
		File lastExportDir = null;
		if (lastExportFolder != null)
			lastExportDir = new File(lastExportFolder);
		
		if (lastExportDir!=null && lastExportDir.isDirectory()) {
			baseFolderText.setText(lastExportDir.getAbsolutePath());
		}
		
//		if (lastExportFolder != null && !lastExportFolder.startsWith("\\") /*&& !lastExportFolder.startsWith("/")*/){
//			baseFolderText.setText(lastExportFolder);
//		}
		
		baseFolderText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePathLabel();
			}
		});
		

//		baseFolderText.setSize(500, 200);

		Button baseFolderButton = new Button(this, SWT.NONE);
		baseFolderButton.setImage(Images.getOrLoad("/icons/folder.png"));
		baseFolderButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		baseFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				String baseFolder = 
						DialogUtil.showOpenFolderDialog(getShell(), "Select base folder", lastExportFolder);
				if (baseFolder != null) {
					baseFolderText.setText(baseFolder);
					updatePathLabel();
				}
			}
		});
		
		Label labelFileOrFolder = new Label(this, SWT.NONE);
		labelFileOrFolder.setText(fileOrFolderLabel != null ? fileOrFolderLabel : "File/Folder name: ");
		
		fileOrFolderText = new Text(this, SWT.BORDER);
		fileOrFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if (docName != "")
			fileOrFolderText.setText(docName);
		fileOrFolderText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePathLabel();
			}
		});
		
		
		pathLabel = new Label(this, SWT.NONE);
		pathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		updatePathLabel();
		
		parent.redraw();
	}
	
	public void updatePathLabel() {
		pathLabel.setText(getExportFile().getAbsolutePath());
	}
	
	public File getExportFile() {
		String fTxt = fileOrFolderText.getText();
		if (extension != null && !fTxt.toLowerCase().endsWith(extension))
			fTxt = fTxt + extension;
		
		return new File(baseFolderText.getText()+"/"+fTxt);
	}
	
	public boolean checkExportFile(File file, String extension, Shell shell) {

		String fTxt = file.getAbsolutePath();
		if (extension != null && !fTxt.toLowerCase().endsWith(extension)) {
			fTxt = fTxt + extension;
			file = new File(fTxt);
		}

		if (!file.getParentFile().exists()) {
			DialogUtil.showErrorMessageBox(shell, "Error trying to export",
					"The export destination folder does not exist - select an existing base folder!");
			return false;
		}

		if (file.exists() && extension != null) {
			int a = DialogUtil.showYesNoDialog(shell, "File exists", "The specified file " + file.getAbsolutePath() + " exists - overwrite?");
			if (a == SWT.YES)
				return true;
			else
				return false;
		} else if (file.exists()) {
			int a = DialogUtil.showYesNoDialog(shell, "Folder exists", "The specified document folder " + file.getAbsolutePath() + " exists - overwrite?");
			if (a == SWT.YES){
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			else{
				return false;
			}
		}

		return true;

	}
	
	
	/*
	 * was used for the single export dialogs -> for each differnt export one dialog
	 * now we use a common export dialog with above checkExport
	 */
	public boolean checkExportFile() {
		File file = getExportFile();
		if (!file.getParentFile().exists()) {
			DialogUtil.showErrorMessageBox(getShell(), "Error trying to export", 
					"The export destination folder does not exist - select an existing base folder!");
			return false;
		}
		if (file.exists() && extension != null) {
			int a = DialogUtil.showYesNoDialog(getShell(), "File exists", "The specified file exists - overwrite?");
			if (a == SWT.YES)
				return true;
			else
				return false;
		}
		else if (file.exists()){
			int a = DialogUtil.showYesNoDialog(getShell(), "Folder exists", "The specified document folder exists - overwrite?");
			
			System.out.println("yes no dialog: " + a);

			if (a == SWT.YES){
				System.out.println("yes no dialog: SWT.YES " + SWT.YES);
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
			else{
				System.out.println("yes no dialog: SWT.YES " + SWT.YES);
				return false;
			}
		}
		return true;
	}
	
	

}
