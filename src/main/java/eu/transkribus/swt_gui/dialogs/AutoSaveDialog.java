package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.qos.logback.classic.Logger;
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
		shell.setSize(700, 420);
		shell.setText(getText());
		shell.setLayout(new GridLayout(3, true));
		GridData gd  = new GridData(GridData.FILL_HORIZONTAL);
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
		Label autoSaveFolderLabel = new Label(shell, SWT.NONE);
		autoSaveFolderLabel.setText("AutoSave Folder");
		
		Text autoSaveFolderTxt = new Text(shell, SWT.BORDER);
		autoSaveFolderTxt.setText(trpSets.getAutoSaveFolder());
;
		autoSaveFolderTxt.setLayoutData(gd);

		Button browseButton = new Button(shell, SWT.PUSH);
	    browseButton.setText("Browse...");
	    
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
	    
		Label autoSaveIntervalLabel = new Label(shell, SWT.NONE);
		autoSaveIntervalLabel.setText("Time Interval:");
		
	    Combo timeMinCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);
	    timeMinCombo.setLayoutData(gd);
	    
	    Combo timeSecCombo = new Combo(shell, SWT.DROP_DOWN | SWT.BORDER);
	    timeSecCombo.setLayoutData(gd);
	    
	    for(int i=0; i<=60; i++) {
	      timeMinCombo.add(i+"m");
	      if(i<60)
	      timeSecCombo.add(i+"s");
	    }
	    
	    int secIndex = trpSets.getAutoSaveInterval()%60;
	    int minIndex = (int)Math.floor((float)trpSets.getAutoSaveInterval()/60.0f);
	    timeSecCombo.select(secIndex);
	    timeMinCombo.select(minIndex);
		
	    Button enableAutoSaveButton = new Button(shell, SWT.CHECK);
	    enableAutoSaveButton.setText("Enable Autosave");
	    enableAutoSaveButton.setSelection(trpSets.getAutoSaveEnabled());
	    
	    
		Button applyButton = new Button(shell, SWT.PUSH);
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
		
		shell.pack();
	}

}
