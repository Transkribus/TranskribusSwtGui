package eu.transkribus.swt_gui.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.MessageDialogStyledWithToggle;

public class EventDialogTest {
	public static void main(String[] args) {
		Shell shell = new Shell();
		String msg = "Dear User,\n"
				+ "due to maintenance work the Transkribus server and our website will be unavailable on\n"
				+ "\n"
				+ "Friday, June 7, in the time between 11:00 - 11:30 CEST.\n"
				+ "\n"
				+ "Running jobs will not be affected by this.\n"
				+ "Please plan your work accordingly. We apologize for any inconvenience.\n"
				+ "Find more information at https://transkribus.eu\n"
				+ "\n"
				+ "Best regards,\n"
				+ "the Transkribus team";
		
		MessageDialogStyledWithToggle d = new MessageDialogStyledWithToggle(shell, "Test", null, msg, MessageDialog.INFORMATION, new String[] { "Geh Weida" }, 
				0, "Schleich Di", false);
		d.open();
		
		DialogUtil.showMessageDialogWithToggle(shell, "Notification", msg, "Do not show this message again", false,
				SWT.NONE, "OK");
		
	}
}
