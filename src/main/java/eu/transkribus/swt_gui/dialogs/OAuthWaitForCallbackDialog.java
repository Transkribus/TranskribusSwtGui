package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Test with proxy.uibk.ac.at:3128
 * @author philip
 *
 */
public class OAuthWaitForCallbackDialog extends Dialog {

	protected Object result;
	protected Shell shell;
		
	private int port;
	private String codePattern;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public OAuthWaitForCallbackDialog(Shell parent, int style, int port, String codePattern) {
		super(parent, style);
		setText("Connecting Account...");
		this.port = port;
		this.codePattern = codePattern;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		shell = new Shell(getParent(), getStyle());
//		shell.setSize(673, 420);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
		GridData gd  = new GridData(300, 20);
	
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		

		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				shell.close();
			}	
		});
		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
}
