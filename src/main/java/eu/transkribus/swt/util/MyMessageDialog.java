/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package eu.transkribus.swt.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class MyMessageDialog extends MessageDialog {
	
	Image customImage;

	/**
	 * A message dialog that can have a custom image
	 * @param image The custom image that is displayed left beside the dialogMessage
	 */
    public MyMessageDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, Image image,
            String[] dialogButtonLabels, int defaultIndex) {
    	super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, -1, dialogButtonLabels, defaultIndex);
    	this.customImage = image;
    }
    
    /**
	 * This is the default MesageDialog constructor from the base class
	 */
    public MyMessageDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, int dialogImageType,
            String[] dialogButtonLabels, int defaultIndex) {
    	super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
    }
    
    @Override public Image getImage() { return customImage; }
    
    

}

