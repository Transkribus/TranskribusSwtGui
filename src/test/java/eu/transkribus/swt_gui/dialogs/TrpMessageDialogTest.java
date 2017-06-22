package eu.transkribus.swt_gui.dialogs;

import javax.security.auth.login.LoginException;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class TrpMessageDialogTest {

	public static void main(String[] args) throws LoginException {		
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				getShell().setSize(600, 600);
				
				Exception e = new Exception("asdadf asdfasdf asdfasdfa sdfasdfa sdfaksdjfkl ajskldfjklaj dklfjalksjdfkljas kljlaksjdflkjaskl jfklajskldfjkl ajakljsfdlkjaklsdjfkl ajsdkljf klajsdklfj kljasldkfjla jdklfjaklsdj kljasdklfj klasdjfklj aklsdjfkl ajsdklfj kladjsfklajdklfj aklsjdfkl ajklsdfjkl ajsdklfj klsfdaj", 
						new Exception("ddd", new Exception(new Exception(new Exception(new Exception(new Exception(new Exception(new Exception())))))))); 
				
				String msg = "I am Error! adfasdf asdfasdf asdfasd asdf asdfa sdfasdfasdf asdfasdfa asdf asdfa sfasdfa sfd sa fdd asdf asdfas dddd dd asdf asdf asdf asdf asfdasdf asdf asfd asd fasdfasdf as fd\n mutliple lines long!\nasdf\nasdfasdfasd\nasdfasdf";
				
//				TrpMessageDialog.showErrorDialog(getShell(), "Error in...", msg, null, null);
				TrpMessageDialog.showErrorDialog(getShell(), "Error in...", msg, "a detailed error message!", e);
				
//				TrpErrorDialog d = new TrpErrorDialog(getShell(), "Error in...", "I am Error!", "detailed error message!", new Exception("asdadf", new Exception("ddd", new Exception())));
//				TrpMessageDialog d = new TrpMessageDialog(getShell(), "Error in...", "I am Error!", null, new Exception("asdadf", new Exception("ddd", new Exception())));
//				d.setSwtIcon(SWT.ICON_INFORMATION);
//				TrpErrorDialog d = new TrpErrorDialog(getShell(), "Error in...", "I am Error!", "detailed error message!", null);
//				d.open();
				
//				s = new Status(IStatus.ERROR, "ID0", 0, "", null);
//				ExceptionDetailsErrorDialog.openError(getShell(), "asdf", "massage", new Status(IStatus.ERROR, "ID0", 0, "", null));

//				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

}
