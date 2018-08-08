package eu.transkribus.swt.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesktopUtil {
	private static final Logger logger = LoggerFactory.getLogger(DesktopUtil.class);
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static void browse(final String uriStr, final String infoOnError, Shell shell) {
		if(uriStr == null || shell == null) {
			throw new IllegalArgumentException("Arguments may not be null!");
		}
		if(isUnix()) {
			try {
				Runtime.getRuntime().exec("xdg-open "+uriStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			try {
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE ) && System.getProperty("os.name").toLowerCase().contains("win")) {
				desktop.browse(new URI(uriStr));
	        }
			}catch (Exception ex) {
		        	//UnsupportedOperationException - if the current platform does not support the Desktop.Action.BROWSE action
		        	//IOException - if the user default browser is not found, or it fails to be launched, or the default handler application failed to be launched
		        	//SecurityException - if a security manager exists and it denies the AWTPermission("showWindowWithoutWarningBanner") permission, or the calling thread is not allowed to create a subprocess; and not invoked from within an applet or Java Web Started application
		        	//IllegalArgumentException - if the necessary permissions are not available and the URI can not be converted to a URL
		        	logger.error("Could not open client for URI: " + uriStr, ex);
		        	DialogUtil.showMessageBox(shell, "Could not find a client to handle URI: " + uriStr, infoOnError != null ? infoOnError : "", SWT.NONE);
		        	}
			}
	    } 
    	
//    	try {
//    		//  Open default external browser 
//    		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
//    	} catch (PartInitException ex) {
//    		// TODO Auto-generated catch block
//    		ex.printStackTrace();
//        } catch (MalformedURLException ex) {
//        	// TODO Auto-generated catch block
//        	ex.printStackTrace();
//        }
	
	public static boolean isUnix() {

		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
		
	}
		
	}


