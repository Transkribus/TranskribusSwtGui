package eu.transkribus.swt_gui.dialogs;

import java.awt.Desktop;
import java.awt.Label;
import java.net.URI;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.client.util.FtpConsts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;

public class ShowServerExportLinkDialog extends Dialog {
	
	String downloadLink;
	public ShowServerExportLinkDialog(Shell parentShell, String linkText) {
		super(parentShell);
		downloadLink = linkText;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText("Show Download Link");
	      SWTUtil.centerShell(shell);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(350, 150);
	}
		
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.WRAP);
	}	
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
    
		Link link;
		link = new Link(container, SWT.WRAP);
	    link.setText("\nPress this link to get your server download " +
	               "<a href=\"" +
	               downloadLink + "\">Click me</a>");
    
		link.addSelectionListener(new SelectionAdapter(){
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	        	Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	                try {
	                    desktop.browse(new URI(e.text));
	                } catch (Exception ex) {
	                	//UnsupportedOperationException - if the current platform does not support the Desktop.Action.BROWSE action
	                	//IOException - if the user default browser is not found, or it fails to be launched, or the default handler application failed to be launched
	                	//SecurityException - if a security manager exists and it denies the AWTPermission("showWindowWithoutWarningBanner") permission, or the calling thread is not allowed to create a subprocess; and not invoked from within an applet or Java Web Started application
	                	//IllegalArgumentException - if the necessary permissions are not available and the URI can not be converted to a URL
//	                	//logger.error("Could not open ftp client!");
	                }
	            } else {
	            	org.eclipse.swt.program.Program.launch(e.text);
	            }
	        	
	//        	try {
	//        		//  Open default external browser 
	//        		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
	//        	} catch (PartInitException ex) {
	//        		// TODO Auto-generated catch block
	//        		ex.printStackTrace();
	//            } catch (MalformedURLException ex) {
	//            	// TODO Auto-generated catch block
	//            	ex.printStackTrace();
	//            }
	        }
	    });
    
		return container;
	}

}
