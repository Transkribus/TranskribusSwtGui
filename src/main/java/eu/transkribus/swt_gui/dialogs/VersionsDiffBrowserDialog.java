package eu.transkribus.swt_gui.dialogs;

import javax.xml.bind.JAXBException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.exceptions.NullValueException;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class VersionsDiffBrowserDialog extends Dialog {

	Browser browser = null;
    private String browserString;
    
    Button withLineNrs;
    boolean showLineNrs = false;

    public boolean isShowLineNrs() {
		return showLineNrs;
	}

	public void setShowLineNrs(boolean showLineNrs) {
		this.showLineNrs = showLineNrs;
	}

	public VersionsDiffBrowserDialog(Shell parentShell, String browserString) {
        super(parentShell);
        this.browserString = browserString;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);
               
        withLineNrs = new Button(composite, SWT.CHECK);
        withLineNrs.setText("Show line numbers");
        withLineNrs.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Button btn = (Button) event.getSource();
                System.out.println(btn.getSelection());
                setShowLineNrs(btn.getSelection());

            	try {
					refreshText(TrpMainWidget.getInstance().getTextDifferenceOfVersions(btn.getSelection()));
				} catch (NullValueException | JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
            }
        });

        browser = new Browser(composite, SWT.NONE);
        browser.setText(browserString);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        /*
         * in Mac no text is shown - is this the reason?? 
         * And why is this here??
         */
//        if (Util.isMac())
//            browser.refresh();
        
        composite.pack();

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
    }
    
	 // overriding this methods allows you to set the
	 // title of the custom dialog
	 @Override
	 protected void configureShell(Shell newShell) {
	   super.configureShell(newShell);
	   newShell.setText("Version Comparator");
	   SWTUtil.centerShell(newShell);
	 }
	  
	@Override protected boolean isResizable() {
	    return true;
	}

	@Override
	protected Point getInitialSize() {
	  return new Point(1100, 800);
	}
	  
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	    setBlockOnOpen(false);
	}	


    @Override
    public void okPressed() {
        close();
    }

	public void refreshText(String textDifferenceOfVersions) {
		if (browser != null && !browser.isDisposed()){
			browser.setText(textDifferenceOfVersions);
		}
		
	}

}
