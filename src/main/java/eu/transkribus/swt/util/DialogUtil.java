package eu.transkribus.swt.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialogUtil {
	private final static Logger logger = LoggerFactory.getLogger(DialogUtil.class);
	
	public static Shell openShellWithComposite(Shell parentShell, Composite composite, int width, int height, String title) {
		return openShellWithComposite(parentShell, composite, width, height, title, SWT.DIALOG_TRIM | SWT.RESIZE);
	}
	
	public static Shell openShellWithComposite(Shell parentShell, Class<? extends Composite> clazz, int width, int height, String title, int shellStyle) {
		Shell shell = new Shell(parentShell, shellStyle);
		shell.setSize(width, height);
		shell.setText(title);
		shell.setLayout(new FillLayout());
		
		try {
			Composite c = clazz.getConstructor(Composite.class, int.class).newInstance(parentShell, 0);
			
			shell.layout(true);
			
			SWTUtil.centerShell(shell);
			shell.open();
			
			return shell;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			logger.error(e.getMessage(), e);
			return shell;
		}

	}
	
	public static Shell openShellWithComposite(Shell parentShell, Composite composite, int width, int height, String title, int shellStyle) {
		Shell shell = new Shell(parentShell, shellStyle);
		shell.setSize(width, height);
		shell.setText(title);
		shell.setLayout(new FillLayout());
		
		composite.setParent(shell);
		
		shell.layout(true);
		
		SWTUtil.centerShell(shell);
		shell.open();
		
		return shell;
	}
	
	public static int showYesNoDialog(Shell shell, String title, String message, final int SWT_ICON) {
		MessageBox messageBox = new MessageBox(shell, SWT_ICON
	            | SWT.YES | SWT.NO);
	        messageBox.setMessage(message);
	        messageBox.setText(title);
	        
	        int response = messageBox.open();
	        return response;		
	}	
	
	public static int showYesNoDialog(Shell shell, String title, String message) {
		return showYesNoDialog(shell, title, message, SWT.ICON_QUESTION);		
	}	
	
	public static int showYesNoCancelDialog(Shell shell, String title, String message) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
	            | SWT.YES | SWT.NO | SWT.CANCEL);
	        messageBox.setMessage(message);
	        messageBox.setText(title);
	        int response = messageBox.open();
	        return response;		
	}
	
	public static int showErrorMessageBox(Shell shell, String title, String message) {
		return showMessageBox(shell, title, message, SWT.ERROR);
	}
	
	public static int showDetailedErrorMessageBox(Shell shell, String title, String message, Throwable th) {
//		Label l = new Label(SWTUtil.dummyShell, 0);
//		l.setImage(Images.getSystemImage(SWT.ICON_INFORMATION));
//				
//		return showCustomMessageDialog(shell, title, message, null, SWT.ERROR, new String[] {"OK",  "Send bug report"}, 0, l);
		
		Status s = null;
		if (th != null) {
			s = new Status(IStatus.ERROR, "ID0", 0, th.getMessage(), th);
		} else {
			s = new Status(IStatus.ERROR, "ID0", 0, "", null);
		}
			 
		return ExceptionDetailsErrorDialog.openError(shell, title, message, s);		
	}
	
	public static int showInfoMessageBox(Shell shell, String title, String message) {
		return showMessageBox(shell, title, message, SWT.ICON_INFORMATION);	
	}
	
	public static int showMessageDialog(Shell shell, String title, String message, 
			Image dialogTitleImage, int dialogImageType, String[] buttons, int defaultIndex) {
		MessageDialog dialog = new MessageDialog(shell, title, dialogTitleImage, message, dialogImageType, buttons, defaultIndex);
		
		return dialog.open();
	}
	
	public static int showMessageDialog(Shell shell, String title, String message, 
			Image dialogTitleImage, Image dialogImage, String[] buttons, int defaultIndex) {
		MyMessageDialog dialog = new MyMessageDialog(shell, title, dialogTitleImage, message, dialogImage, buttons, defaultIndex);
		
		return dialog.open();
	}
	
	public static int showCustomMessageDialog(Shell shell, String title, String message, 
			Image dialogTitleImage, int style, String[] buttons, int defaultIndex, final Control custom) {
		return createCustomMessageDialog(shell, title, message, dialogTitleImage, style, buttons, defaultIndex, custom).open();
	}
	
	public static MessageDialog createCustomMessageDialog(Shell shell, String title, String message, 
			Image dialogTitleImage, int style, String[] buttons, int defaultIndex, final Control custom) {
		MessageDialog dialog = new MessageDialog(shell, title, dialogTitleImage, message, style, buttons, defaultIndex) {
		    protected Control createCustomArea(Composite parent) {
		    	if (custom != null) {
		    		custom.setParent(parent);
		    		custom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		    	}
		    	
		        return custom;
		    }
		};
		
		return dialog;
	}
	
	public static Pair<Integer, Boolean> showMessageDialogWithToggle(Shell shell, String title, String message, 
			String toggleMessage, boolean toggleState, int style, String... buttons) {
		MessageDialogWithToggle dialog = new MessageDialogWithToggle(shell, title, null, message, style, buttons, 0, toggleMessage, toggleState);
		int answer = dialog.open();
		
		return Pair.of(answer, dialog.getToggleState());
	}	
	
	public static int showMessageBox(Shell shell, String title, String message, int style) {
		MessageBox mBox = new MessageBox(shell, style | SWT.RESIZE);

		mBox.setMessage(message==null ? "" : message);
		mBox.setText(title);
		
		return mBox.open();		
	}
	
	public static String showSaveDialog(Shell shell, String title, String filterPath, String[] exts) {
		FileDialog fd = new FileDialog(shell, SWT.SAVE);
		fd.setOverwrite(true); // prompt user if file exists!
		fd.setText(title);
		if (filterPath == null)
			filterPath = System.getProperty("user.dir");
		fd.setFilterPath(filterPath);
		if (exts == null)
			exts = new String[]{"*.*"};
		fd.setFilterExtensions(exts);
		String selected = fd.open();
		return selected;
	}
	
	public static String showOpenFileDialog(Shell shell, String title, String filterPath, String[] exts) {
		FileDialog fd = new FileDialog(shell, SWT.OPEN);
		fd.setOverwrite(true); // prompt user if file exists!
		fd.setText(title);
		if (filterPath == null)
			filterPath = System.getProperty("user.dir");
		fd.setFilterPath(filterPath);
		if (exts == null)
			exts = new String[]{"*.*"};
		fd.setFilterExtensions(exts);
		String selected = fd.open();
		return selected;
	}
	
	//for choosing several files at once
	public static ArrayList<String> showOpenFilesDialog(Shell shell, String title, String filterPath, String[] exts) {
		FileDialog fd = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
		fd.setOverwrite(true); // prompt user if file exists!
		fd.setText(title);
		if (filterPath == null)
			filterPath = System.getProperty("user.dir");
		fd.setFilterPath(filterPath);
		if (exts == null)
			exts = new String[]{"*.*"};
		fd.setFilterExtensions(exts);
		
		ArrayList<String> files = new ArrayList<String>();
	    if (fd.open() != null) {
	      String[] names = fd.getFileNames();
	      for (int i = 0, n = names.length; i < n; i++) {
	        StringBuffer buf = new StringBuffer(fd.getFilterPath());
	        if (buf.charAt(buf.length() - 1) != File.separatorChar)
	          buf.append(File.separatorChar);
	        buf.append(names[i]);
	        files.add(buf.toString());
	      }
	    }
	    System.out.println(files);

		return files;
	}
	
	public static String showOpenFolderDialog(Shell shell, String title, String filterPath) {
		DirectoryDialog fd = new DirectoryDialog(shell);
		fd.setFilterPath(filterPath);
		fd.setText(title);
		String selected = fd.open();
		return selected;
	}	
	
	public static ToolTip createBallonToolTip(Shell shell, int iconStyle, String message, String title, int posX, int posY) {
		final ToolTip tip = new ToolTip(shell, SWT.BALLOON | iconStyle);
		
		tip.setMessage(message);
		tip.setText(title);

		if (posX < 0 || posY < 0)
			tip.setLocation(shell.getLocation().x+shell.getSize().x, shell.getLocation().y+shell.getSize().y);
		else
			tip.setLocation(posX, posY);
		
		return tip;
	}
	
	public static ToolTip createAndShowBalloonToolTip(Shell shell, int iconStyle, String message, String title, int pos, boolean autohide) {
		int posX = shell.getLocation().x;
		int posY = shell.getLocation().y;
		if (pos == 1) {
			posX += shell.getSize().x;
		}
		else if (pos == 2) {
			posX += shell.getSize().x;
			posY += shell.getSize().y;
		}
		else {
			posY += shell.getSize().y;
		}
		
		
		ToolTip tt = createBallonToolTip(shell, iconStyle, message, title, posX, posY);
		tt.setAutoHide(autohide);
		tt.setVisible(true);
		
		return tt;
	}
	
	public static ToolTip createAndShowBalloonToolTip(Shell shell, int iconStyle, String message, String title, int posX, int posY, boolean autohide) {
		ToolTip tt = createBallonToolTip(shell, iconStyle, message, title, posX, posY);
		tt.setAutoHide(autohide);
		tt.setVisible(true);
		
		return tt;
	}
	
	public static void showBallonToolTip(Composite c, Integer iconType, String title, String message) {
		if (iconType == null)
			iconType = SWT.ICON_INFORMATION;

		Rectangle b = c.getBounds();
		Point l = c.getParent().toDisplay(c.getLocation());
		DialogUtil.createAndShowBalloonToolTip(c.getShell(), iconType, message, title, l.x+b.width, l.y+b.height, true);		
	}

}
