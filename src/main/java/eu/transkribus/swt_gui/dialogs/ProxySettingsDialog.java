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

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.settings.ProxySettingsWidget;

/**
 * Test with proxy.uibk.ac.at:3128
 * @author philip
 *
 */
public class ProxySettingsDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	
	ProxySettingsWidget widget;
	
	private Button saveButton;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ProxySettingsDialog(Shell parent, int style) {
		super(parent, style);
		setText("Proxy Settings");
	}
	
	public Shell getShell() {
		return shell;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		
		SWTUtil.centerShell(shell);
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
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
//		shell.setSize(673, 420);
		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));
		
		shell.setLocation(getParent().getSize().x/2, getParent().getSize().y/3);
		
		widget = new ProxySettingsWidget(shell, SWT.NONE);
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		
		saveButton = new Button(buttonComposite, SWT.NONE);
		saveButton.setText("OK");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				try {
					widget.applyToSettings();
				} catch (Exception ex) {
					//widget will show exception in message label. do not close shell
					return;
				}
				shell.close();
			}
		});
		saveButton.setToolTipText("Stores the configuration in the registry and closes the dialog");
		
		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				shell.close();
			}	
		});
		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
	}
}
