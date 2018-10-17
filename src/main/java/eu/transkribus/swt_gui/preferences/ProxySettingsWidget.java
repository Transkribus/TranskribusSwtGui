package eu.transkribus.swt_gui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.TrpGuiPrefs.ProxyPrefs;

public class ProxySettingsWidget extends Composite {
	private ProxyPrefs prefs;
	
	private Text proxyHostTxt;
	private Text proxyPortTxt;
	private Text proxyUserTxt;
	private Text proxyPasswordTxt;
	private Button isProxyEnabledBtn;
	private Label msgLbl;
	
	public ProxySettingsWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, false));
		prefs = TrpGuiPrefs.getProxyPrefs();
		createContent(this);
	}
	
	/**
	 * Reads text fields and applies values to preferences. Will throw exception upon illegal values.
	 */
	public void applyToSettings() throws IllegalArgumentException {
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
					msgLbl.setText("Port has to be a positive number!");
					throw new IllegalArgumentException("Port has to be a positive number!");
				}
				prefs.setPort(port);
			} catch (NumberFormatException nfe) {
				msgLbl.setText("Port has to be a number!");
				throw new IllegalArgumentException("Port has to be a number!");
			} catch (IllegalArgumentException iae) {
				msgLbl.setText("iae.getMessage()");
				throw new IllegalArgumentException(iae.getMessage());
			}
		}
		
		TrpGuiPrefs.setProxyPrefs(prefs);
	}
	
	protected void createContent(Composite content) {
		GridData gd  = new GridData(300, 20);
		
		Label lblHostLabel = new Label(content, SWT.NONE);
		lblHostLabel.setText("Proxy Host:");
		
		proxyHostTxt = new Text(content, SWT.BORDER);
//		proxyHostTxt.setText(trpSets.getProxyHost());
		proxyHostTxt.setLayoutData(gd);

		Label lblPortLabel = new Label(content, SWT.NONE);
		lblPortLabel.setText("Proxy Port:");
		
		proxyPortTxt = new Text(content, SWT.BORDER);
//		proxyPortTxt.setText(trpSets.getProxyPort());
		proxyPortTxt.setLayoutData(gd);

		Label lblUserLabel = new Label(content, SWT.NONE);
		lblUserLabel.setText("Proxy User:");
		
		proxyUserTxt = new Text(content, SWT.BORDER);
//		proxyUserTxt.setText(trpSets.getProxyUser());
		proxyUserTxt.setLayoutData(gd);

		Label lblPwLabel = new Label(content, SWT.NONE);
		lblPwLabel.setText("Proxy Password:");
		
		proxyPasswordTxt = new Text(content, SWT.BORDER | SWT.PASSWORD);
//		proxyPasswordTxt.setText(trpSets.getProxyPassword());
		proxyPasswordTxt.setLayoutData(gd);

		isProxyEnabledBtn = new Button(content, SWT.CHECK);
		isProxyEnabledBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		isProxyEnabledBtn.setText("Enable proxy server");
//		isProxyEnabledBtn.setSelection(trpSets.isProxyEnabled());
//		isProxyEnabledBtn.setToolTipText("");
				
		msgLbl = new Label(content, SWT.NONE);
		msgLbl.setForeground(Colors.getSystemColor(SWT.COLOR_RED));
		msgLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Composite buttonComposite = new Composite(content, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		
		updateFields();
	}
	
	public void updateFields() {
		isProxyEnabledBtn.setSelection(prefs.isEnabled());
		proxyHostTxt.setText(prefs.getHost());
		proxyPortTxt.setText(prefs.getPort() > 0 ? ""+prefs.getPort() : "");
		proxyUserTxt.setText(prefs.getUser());
		proxyPasswordTxt.setText(prefs.getPassword());
	}
	

}
