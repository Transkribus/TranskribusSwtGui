package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.SWTUtil;

public class VersionsDiffBrowserDialog extends Dialog {

    private String browserString;

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

        Browser browser = new Browser(composite, SWT.NONE);
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


    @Override
    public void okPressed() {
        close();
    }

}
