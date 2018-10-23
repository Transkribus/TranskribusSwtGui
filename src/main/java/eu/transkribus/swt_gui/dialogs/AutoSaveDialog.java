package eu.transkribus.swt_gui.dialogs;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;

public class AutoSaveDialog extends Dialog{
	
	protected Object result;
	protected Shell shell;
	private TrpSettings trpSets;
	
	public AutoSaveDialog(Shell parent, TrpSettings trpSets) {
		super(parent, SWT.DIALOG_TRIM | SWT.RESIZE);
		setText("Autosave Settings");
		this.trpSets = trpSets;
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public Object open() {
		createContents();
		shell.setSize(600, shell.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
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
	
	private void createContents() {
		shell = new Shell(getParent(), getStyle());		
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		GridData gd  = new GridData(GridData.FILL_HORIZONTAL);
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
	    Button enableAutoSaveButton = new Button(shell, SWT.CHECK);
	    enableAutoSaveButton.setText("Enable Autosave");
	    enableAutoSaveButton.setSelection(trpSets.getAutoSaveEnabled());
		enableAutoSaveButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
	    Button enableCheckForNewerAutoSave = new Button(shell, SWT.CHECK);
	    enableCheckForNewerAutoSave.setText("Check for newer autosaved version on page load");
	    enableCheckForNewerAutoSave.setSelection(trpSets.isCheckForNewerAutosaveFile());
	    enableCheckForNewerAutoSave.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	    
		Label autoSaveIntervalLabel = new Label(shell, SWT.NONE);
		autoSaveIntervalLabel.setText("Save interval in seconds: ");
		autoSaveIntervalLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));		
		
		Text autoSaveIntervalText = new Text(shell, 0);
		autoSaveIntervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));	
		autoSaveIntervalText.setText(""+trpSets.getAutoSaveInterval());
	    
		Label autoSaveFolderLabel = new Label(shell, SWT.NONE);
		autoSaveFolderLabel.setText("AutoSave Folder:");
		autoSaveFolderLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		
	    Composite folderComp = new Composite(shell, SWT.NONE);
	    folderComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));	
	    folderComp.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
		Text autoSaveFolderTxt = new Text(folderComp, 0);
		autoSaveFolderTxt.setText(trpSets.getAutoSaveFolder());
		autoSaveFolderTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Button browseButton = new Button(folderComp, SWT.PUSH);
	    browseButton.setText("Browse...");
	    browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	    
	    browseButton.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        DirectoryDialog dlg = new DirectoryDialog(shell);
	        dlg.setFilterPath(autoSaveFolderTxt.getText());
	        dlg.setText("Select a directory");
//	        dlg.setMessage("Select a directory");
	        String dir = dlg.open();
	        if (dir != null) {
	        	autoSaveFolderTxt.setText(dir);
	        }
	      }
	    });		    
	    
	    Composite buttons = new Composite(shell, SWT.NONE);
	    buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true, 2, 1));	
	    buttons.setLayout(new GridLayout(2, false));
	    
		Button applyButton = new Button(buttons, SWT.PUSH);
		applyButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				trpSets.setAutoSaveFolder(autoSaveFolderTxt.getText());
				trpSets.setAutoSaveEnabled(enableAutoSaveButton.getSelection());
				trpSets.setCheckForNewerAutosaveFile(enableCheckForNewerAutoSave.getSelection());
				try {
					int newInterval = Integer.parseInt(autoSaveIntervalText.getText());
					trpSets.setAutoSaveInterval(newInterval);
				} catch (Exception ex) {
					DialogUtil.showErrorMessageBox(getShell(), "Error",
							"Error setting new time interval: " + ex.getMessage());
				}
				autoSaveIntervalText.setText(""+trpSets.getAutoSaveInterval());
			}
		});
	    applyButton.setText("Apply");
	    applyButton.setLayoutData(gd);
	    
	    Button resetButton = new Button(buttons, SWT.PUSH);
	    resetButton.addSelectionListener(new SelectionAdapter(){
	    	public void widgetSelected(SelectionEvent e){
	    		String defaultDir = TrpSettings.getDefaultAutoSaveFolder();
		    	trpSets.setAutoSaveFolder(defaultDir);
		    	trpSets.setAutoSaveInterval(TrpSettings.DEFAULT_AUTOSAVE_INTERVAL);
		    	trpSets.setAutoSaveEnabled(true);
		    	autoSaveFolderTxt.setText(defaultDir);
		    	autoSaveIntervalText.setText(""+TrpSettings.DEFAULT_AUTOSAVE_INTERVAL);
		    	enableAutoSaveButton.setSelection(true);
	    		
	    	}
	    });
	    resetButton.setText("Reset");
	    resetButton.setLayoutData(gd);	
	    	    
		shell.pack();
	}

}
