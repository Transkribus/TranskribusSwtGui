package eu.transkribus.swt_gui.dialogs;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;

public class AutoSaveDialog extends Dialog{

	protected Object result;
	protected Shell shell;
	private TrpSettings trpSets;
	
	public AutoSaveDialog(Shell parent, int style, TrpSettings trpSets) {
		super(parent, style);
		setText("Autosave Settings");
		this.trpSets = trpSets;
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public Object open() {
		createContents();
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
	    
		Label autoSaveIntervalLabel = new Label(shell, SWT.NONE);
		autoSaveIntervalLabel.setText("Time Interval:");
		autoSaveIntervalLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));		
		
	    Composite timeComp = new Composite(shell, SWT.NONE);
	    timeComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));	
	    timeComp.setLayout(new GridLayout(2, true));	  
		
	    Combo timeMinCombo = new Combo(timeComp, SWT.DROP_DOWN | SWT.BORDER);
	    timeMinCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
	    
	    Combo timeSecCombo = new Combo(timeComp, SWT.DROP_DOWN | SWT.BORDER);
	    timeSecCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
	    
	    for(int i=0; i<=60; i++) {
	      timeMinCombo.add(i+"m");
	      if(i<60)
	      timeSecCombo.add(i+"s");
	    }
	    
	    int secIndex = trpSets.getAutoSaveInterval()%60;
	    int minIndex = (int)Math.floor((float)trpSets.getAutoSaveInterval()/60.0f);
	    timeSecCombo.select(secIndex);
	    timeMinCombo.select(minIndex);
	    
		Label autoSaveFolderLabel = new Label(shell, SWT.NONE);
		autoSaveFolderLabel.setText("AutoSave Folder:");
		autoSaveFolderLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
		
	    Composite folderComp = new Composite(shell, SWT.NONE);
	    folderComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 2));	
	    folderComp.setLayout(new GridLayout(2, false));
		
		Text autoSaveFolderTxt = new Text(folderComp, SWT.BORDER);
		autoSaveFolderTxt.setText(trpSets.getAutoSaveFolder());
		autoSaveFolderTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		Button browseButton = new Button(folderComp, SWT.PUSH);
	    browseButton.setText("Browse...");
	    browseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
	    
	    browseButton.addSelectionListener(new SelectionAdapter() {
	      public void widgetSelected(SelectionEvent event) {
	        DirectoryDialog dlg = new DirectoryDialog(shell);
	        dlg.setFilterPath(autoSaveFolderTxt.getText());
	        dlg.setText("SWT's DirectoryDialog");
	        dlg.setMessage("Select a directory");
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
		applyButton.addSelectionListener(new SelectionAdapter(){
		      public void widgetSelected(SelectionEvent e) {
		    	  trpSets.setAutoSaveFolder(autoSaveFolderTxt.getText());
		    	  trpSets.setAutoSaveEnabled(enableAutoSaveButton.getSelection());
		    	  if(timeMinCombo.getSelectionIndex() == 0 && timeSecCombo.getSelectionIndex() == 0){
		    		  DialogUtil.showErrorMessageBox(getShell(), "Error", "Please select valid time interval");
		    	  }else{
		    		  trpSets.setAutoSaveInterval(timeMinCombo.getSelectionIndex()*60 + timeSecCombo.getSelectionIndex());
		    		  TrpMainWidget.getInstance().beginAutoSaveThread();
		    	  }
		        }
		});
	    applyButton.setText("Apply");
	    applyButton.setLayoutData(gd);
	    
	    Button resetButton = new Button(buttons, SWT.PUSH);
	    resetButton.addSelectionListener(new SelectionAdapter(){
	    	public void widgetSelected(SelectionEvent e){
	    		String defaultDir = TrpSettings.getDefaultAutoSaveFolder();
	    		int defaultInterval = 60;
		    	trpSets.setAutoSaveFolder(defaultDir);
		    	trpSets.setAutoSaveInterval(defaultInterval);
		    	trpSets.setAutoSaveEnabled(true);
		    	autoSaveFolderTxt.setText(defaultDir);
		    	timeSecCombo.select(0);
		    	timeMinCombo.select(1);
		    	enableAutoSaveButton.setSelection(true);
	    		
	    	}
	    });
	    resetButton.setText("Reset");
	    resetButton.setLayoutData(gd);	
	    	    
		shell.pack();
	}

}
