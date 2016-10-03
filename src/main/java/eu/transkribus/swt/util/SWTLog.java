/*******************************************************************************
 * Copyright (c) 2013 DEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     DEA - initial API and implementation
 ******************************************************************************/
package eu.transkribus.swt.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.widgets.Shell;

public class SWTLog {
	public static int showError(Logger logger, Shell shell, String title, String message, Throwable th) {
		return DialogUtil.showDetailedErrorMessageBox(shell, title, message, th);
		
//		if (logger.getEffectiveLevel().toInt() <= Priority.DEBUG_INT) { // show throwable message in dialog when in debug mode!
//			return DialogUtil.showErrorMessageBox(shell, title, message+"\n\nError Message:\n"+th.getMessage());
//		}
//		else {
//			return DialogUtil.showErrorMessageBox(shell, title, message+"\n\nError Message:\n"+th.getMessage());
//		}
	}
	
	public static void logError(Logger logger, Shell shell, String title, String message, Throwable th, boolean printStackTrace) {
		if (th != null && printStackTrace)
			logger.error(title+" - "+message+" - "+th.getMessage(), th);
		else if (th != null)
			logger.error(title+" - "+message+" - "+th.getMessage());
		else
			logger.error(title+" - "+message);
	}
	
//	public static void showAndLogError(Logger logger, Shell shell, String title, String message, Throwable th) {
//		logError(logger, shell, title, message, th, true);
//		showError(logger, shell, title, message, th);
//	}
}
