package eu.transkribus.swt_gui.dialogs;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;

public class TrpMessageDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TrpMessageDialog.class);
	
	public static final int DEFAULT_ICON = SWT.ICON_ERROR;
	public static final int MAX_WIDTH = 800;

	String title;
	String message;
	String detailedMessage;
	Throwable exception;
	
	int swtIcon = DEFAULT_ICON;
	Label iconLabel;
	StyledText messageText;
	StyledText exceptionText;
	
	boolean hasDetails = false;
	
	ExpandableComposite ec;

	public TrpMessageDialog(Shell parentShell, String title, String message, String detailedMessage, Throwable exception) {
		super(parentShell);
		
		this.title = title;
		this.message = message;
		this.detailedMessage = detailedMessage;
		this.exception = exception;
	}
	
	public static int showErrorDialog(Shell parentShell, String title, String message, String detailedMessage, Throwable exception) {
		TrpMessageDialog d = new TrpMessageDialog(parentShell, title, message, detailedMessage, exception);
		return d.open();
	}
	
	public static int showInfoDialog(Shell parentShell, String title, String message, String detailedMessage, Throwable exception) {
		TrpMessageDialog d = new TrpMessageDialog(parentShell, title, message, detailedMessage, exception);
		d.setSwtIcon(SWT.ICON_INFORMATION);
		return d.open();
	}	
	
	public void setSwtIcon(int swtIcon) {
		this.swtIcon = swtIcon;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(StringUtils.isEmpty(title) ? "" : title);
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(MAX_WIDTH, getMinHeight());
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		b.setFocus();
		b = createButton(parent, IDialogConstants.HELP_ID, "Bug...", false);
		b.setImage(Images.BUG);
		b.setToolTipText("Send a bug report");
		
		// close dialog and return HELP_ID when user pushes bug report button:
		getButton(IDialogConstants.HELP_ID).addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setReturnCode(IDialogConstants.HELP_ID);
				close();
			}
		});
		
		updateSize();
	}
	
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	    setBlockOnOpen(false);
	}	
	
	private int getMinHeight() {
//		System.out.println(messageText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		
		return messageText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y + (hasDetails ? 150 : 100);
	}
	
	private int getMaxHeight() {
		return getMinHeight() + 500;
	}
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		iconLabel = new Label(container, 0);
		iconLabel.setImage(Images.getSystemImage(swtIcon));
		
		messageText = new StyledText(container, SWT.FLAT | SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.VERTICAL);
		messageText.setBackground(container.getBackground());
		messageText.setText(message);
		messageText.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		hasDetails = !StringUtils.isEmpty(detailedMessage) || exception != null;
		if (hasDetails) {
			ec = new ExpandableComposite(container, ExpandableComposite.COMPACT);
			ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			ec.setLayout(new GridLayout());
						
			exceptionText = new StyledText(ec, SWT.BORDER | SWT.MULTI | SWT.VERTICAL | SWT.WRAP);
			
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 800;
			exceptionText.setLayoutData(gd);
			
			String msg = "";
			int i=0;
			if (!StringUtils.isEmpty(detailedMessage)) {
				msg = detailedMessage+"\n\n";
				i = msg.length();
			}
			
			
			String exceptionStr = exception==null ? "" : ExceptionUtil.printStackTrace(exception);
			
			String exceptionHeader = "Exception:\n";
			if (!StringUtils.isEmpty(exceptionStr)) {
				msg += exceptionHeader;
				msg += exceptionStr;
			}
			
			exceptionText.setText(msg);
			
			if (!StringUtils.isEmpty(exceptionStr)) {
				TextStyle ts = new TextStyle(Fonts.createBoldFont(exceptionText.getFont()), exceptionText.getForeground(), exceptionText.getBackground());
				StyleRange sr = new StyleRange(ts);
				sr.start = i;
				sr.length = exceptionHeader.length();
				
				exceptionText.setStyleRange(sr);
			}
			
			
			ec.setClient(exceptionText);
			ec.setText("Details");
			Fonts.setBoldFont(ec);
			ec.setExpanded(false);
			ec.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					updateSize();
				}
			});
			
//			getShell().pack();
		}
		
//		getShell().setSize(getShell().computeSize(SWT.DEFAULT, getMinHeight()));
		
//		parent.pack();
//		getShell().setSize(getShell().computeSize(SWT.DEFAULT, getMinHeight()));
		
		SWTUtil.centerShell(getShell());
//		updateSize();

		return container;
	}
	
	@Override protected void initializeBounds() {
		SWTUtil.centerShell(getShell());
	}
	
	private void updateSize() {
		Point size;
		if (ec != null && ec.isExpanded() == true) {
			size = getShell().computeSize(SWT.DEFAULT, getMaxHeight());
		} else {
			size = getShell().computeSize(SWT.DEFAULT, getMinHeight());
		}
		
		size.x = Math.min(MAX_WIDTH, size.x);
		
		getShell().setSize(size);
		getShell().layout(true);
	}
	
	

}

