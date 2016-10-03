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

import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

/**
 * Test with proxy.uibk.ac.at:3128
 * @author philip
 *
 */
public class ProxySettingsDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	
	TrpSettings trpSets;
	
	private Text proxyHostTxt;
	private Text proxyPortTxt;
	private Text proxyUserTxt;
	private Text proxyPasswordTxt;
	private Button isProxyEnabledBtn;
	Button saveButton;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ProxySettingsDialog(Shell parent, int style, TrpSettings trpSets) {
		super(parent, style);
		setText("Proxy Settings");
		
		this.trpSets = trpSets;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		addBindings();
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
	
	private void addBindings() {
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.PROXY_ENABLED, trpSets, isProxyEnabledBtn);
		
		DataBinder.get().bindBeanToWidgetText(TrpSettings.PROXY_HOST, trpSets, this.proxyHostTxt);
		DataBinder.get().bindBeanToWidgetText(TrpSettings.PROXY_PORT, trpSets, this.proxyPortTxt);
		DataBinder.get().bindBeanToWidgetText(TrpSettings.PROXY_USER, trpSets, this.proxyUserTxt);
		DataBinder.get().bindBeanToWidgetText(TrpSettings.PROXY_PW, trpSets, this.proxyPasswordTxt);
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
		
		proxyPasswordTxt = new Text(shell, SWT.BORDER);
//		proxyPasswordTxt.setText(trpSets.getProxyPassword());
		proxyPasswordTxt.setLayoutData(gd);

		isProxyEnabledBtn = new Button(shell, SWT.CHECK);
		isProxyEnabledBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		isProxyEnabledBtn.setText("Enable proxy server");
//		isProxyEnabledBtn.setSelection(trpSets.isProxyEnabled());
//		isProxyEnabledBtn.setToolTipText("");
				
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		buttonComposite.setLayout(new FillLayout());
		
		saveButton = new Button(buttonComposite, SWT.NONE);
		saveButton.setText("OK");
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
//				TrpConfig.save();
				shell.close();
			}
		});
		saveButton.setToolTipText("Stores the configuration in the configuration file and closes the dialog");
		
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
