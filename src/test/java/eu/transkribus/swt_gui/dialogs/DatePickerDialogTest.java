/**
 * 
 */
package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.widgets.Shell;

/**
 * @author lange
 *
 */
public class DatePickerDialogTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DatePickerDialog aw = new DatePickerDialog(new Shell());
		aw.open();
			
		System.out.println(aw.getDate());
	}

}
