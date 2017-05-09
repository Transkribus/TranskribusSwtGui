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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.ProxyPrefs;

/**
 * Test with proxy.uibk.ac.at:3128
 * @author philip
 *
 */
public class ProxySettingsDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	
	ProxyPrefs prefs;
	
	private Text proxyHostTxt;
	private Text proxyPortTxt;
	private Text proxyUserTxt;
	private Text proxyPasswordTxt;
	private Button isProxyEnabledBtn;
	private Label msgLbl;
	private Button saveButton;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ProxySettingsDialog(Shell parent, int style, ProxyPrefs prefs) {
		super(parent, style);
		setText("Proxy Settings");
		
		this.prefs = prefs;
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
		
		GridData gd  = new GridData(300, 20);
		
		Label lblHostLabel = new Label(shell, SWT.NONE);
		lblHostLabel.setText("Proxy Host:");
		
		proxyHostTxt = new Text(shell, SWT.BORDER);
//		proxyHostTxt.setText(trpSets.getProxyHost());
		proxyHostTxt.setLayoutData(gd);

		Label lblPortLabel = new Label(shell, SWT.NONE);
		lblPortLabel.setText("Proxy Port:");
		
		proxyPortTxt = new Text(shell, SWT.BORDER);
//		proxyPortTxt.setText(trpSets.getProxyPort());
		proxyPortTxt.setLayoutData(gd);

		Label lblUserLabel = new Label(shell, SWT.NONE);
		lblUserLabel.setText("Proxy User:");
		
		proxyUserTxt = new Text(shell, SWT.BORDER);
//		proxyUserTxt.setText(trpSets.getProxyUser());
		proxyUserTxt.setLayoutData(gd);

		Label lblPwLabel = new Label(shell, SWT.NONE);
		lblPwLabel.setText("Proxy Password:");
		
		proxyPasswordTxt = new Text(shell, SWT.BORDER | SWT.PASSWORD);
//		proxyPasswordTxt.setText(trpSets.getProxyPassword());
		proxyPasswordTxt.setLayoutData(gd);

		isProxyEnabledBtn = new Button(shell, SWT.CHECK);
		isProxyEnabledBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		isProxyEnabledBtn.setText("Enable proxy server");
//		isProxyEnabledBtn.setSelection(trpSets.isProxyEnabled());
//		isProxyEnabledBtn.setToolTipText("");
				
		msgLbl = new Label(shell, SWT.NONE);
		msgLbl.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
		msgLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		
		saveButton = new Button(buttonComposite, SWT.NONE);
		saveButton.setText("OK");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				prefs.setEnabled(isProxyEnabledBtn.getSelection());
				prefs.setHost(proxyHostTxt.getText());
				prefs.setUser(proxyUserTxt.getText());
				prefs.setPassword(proxyPasswordTxt.getText());
				
				final String portStr = proxyPortTxt.getText();
				if(!portStr.isEmpty()) {
					final int port;
					try {
						port = Integer.parseInt(portStr);
						if(port < 1) {
							throw new IllegalArgumentException("Port has to be a positive number!");
						}
						prefs.setPort(port);
					} catch (NumberFormatException nfe) {
						msgLbl.setText("Port has to be a number!");
						return;
					} catch (IllegalArgumentException iae) {
						msgLbl.setText(iae.getMessage());
						return;
					}
				}
				
				TrpGuiPrefs.setProxyPrefs(prefs);
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
		
		updateFields();
		
		shell.pack();
	}

	private void updateFields() {
		isProxyEnabledBtn.setSelection(prefs.isEnabled());
		proxyHostTxt.setText(prefs.getHost());
		proxyPortTxt.setText(prefs.getPort() > 0 ? ""+prefs.getPort() : "");
		proxyUserTxt.setText(prefs.getUser());
		proxyPasswordTxt.setText(prefs.getPassword());
	}
}
