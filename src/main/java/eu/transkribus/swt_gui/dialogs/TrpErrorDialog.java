package eu.transkribus.swt_gui.dialogs;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
import eu.transkribus.swt_gui.pagination_tables.PageLockTablePagination;

public class TrpErrorDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TrpErrorDialog.class);
	
	Group pageLocksGroup;
	PageLockTablePagination pageLockTable;
	
	String title;
	String message;
	String detailedMessage;
	Throwable exception;
	
	Label iconLabel;
	Text messageText;
	StyledText exceptionText;

	public TrpErrorDialog(Shell parentShell, String title, String message, String detailedMessage, Throwable exception) {
		super(parentShell);
		
		this.title = title;
		this.message = message;
		this.detailedMessage = detailedMessage;
		this.exception = exception;
	}
	
	@Override protected void configureShell(Shell shell) {
	      super.configureShell(shell);
	      shell.setText(StringUtils.isEmpty(title) ? "" : title);
	      SWTUtil.centerShell(shell);
	      shell.pack();
	}
	
	@Override protected boolean isResizable() {
	    return true;
	}
	
	@Override protected Point getInitialSize() {
		return new Point(600, 200);
	}
	
	@Override protected void createButtonsForButtonBar(Composite parent) {
		Button b = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		b.setFocus();
//		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
		createButton(parent, IDialogConstants.CLIENT_ID, "Send bug report", false);
	}
	
	@Override protected void setShellStyle(int newShellStyle) {           
	    super.setShellStyle(SWT.CLOSE | SWT.MODELESS| SWT.BORDER | SWT.TITLE | SWT.RESIZE);
	    setBlockOnOpen(false);
	}	
	
	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		iconLabel = new Label(container, 0);
		iconLabel.setImage(Images.getSystemImage(SWT.ICON_ERROR));
		
		messageText = new Text(container, SWT.FLAT | SWT.READ_ONLY | SWT.SINGLE);
		messageText.setBackground(container.getBackground());
//		messageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		messageText.setText(message);
		messageText.clearSelection();
		
		if (!StringUtils.isEmpty(detailedMessage) || exception != null) {
			ExpandableComposite ec = new ExpandableComposite(container, ExpandableComposite.COMPACT);
			ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			ec.setLayout(new FillLayout());
			
			exceptionText = new StyledText(ec, SWT.BORDER | SWT.MULTI | SWT.VERTICAL | SWT.WRAP);
			
			String msg = "";
			int i=0;
			if (!StringUtils.isEmpty(detailedMessage)) {
				msg = detailedMessage+"\n\n";
				i = msg.length();
			}
			
			String exceptionStr = ExceptionUtil.printStackTrace(exception);
			
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
			
//			exceptionText.setLineSt
			
			ec.setClient(exceptionText);
			ec.setText("Details");
			Fonts.setBoldFont(ec);
			ec.setExpanded(false);
			ec.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					getShell().pack();
//					container.pack();
				}
			});
			
//			getShell().pack();
		}
		
		parent.pack();

		return container;
	}
	
	

}

