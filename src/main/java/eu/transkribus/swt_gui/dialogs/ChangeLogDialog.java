package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.ProgramInfo;

public class ChangeLogDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ChangeLogDialog.class);

	protected Object result;
	protected Shell shell;
	protected String changelog="";
	protected StyledText text;

	protected boolean hide=true;
	
	public ChangeLogDialog(Shell parent, int style) {
		super(parent, style |= (SWT.DIALOG_TRIM | SWT.RESIZE));

		ProgramInfo info = new ProgramInfo();

		setText("What's new in Transkribus - (active: " + info.getName() + info.getVersion() + ")");

		try {
			changelog = CoreUtils.readStringFromTxtFile("CHANGES.txt");
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.debug("Could not read from file CHANGES.txt");
		}
		createContents();
	}

	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImage(Images.getOrLoad("/icons/new.png"));
		shell.setLayout(SWTUtil.createGridLayout(1, true, 5, 5));
		
		text = new StyledText(shell, SWT.BORDER | SWT.VERTICAL | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.WRAP);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		text.setSize(500, 250);
		text.setText(changelog==null ? "" : changelog);
		text.setTopIndex(text.getLineCount() - 1);

		final boolean SHOW_HIDE_CHECKBOX = false;
		if (SHOW_HIDE_CHECKBOX) {
			Button hideOnStartup = new Button(shell, SWT.CHECK);
			hideOnStartup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			hideOnStartup.setText("Hide changes at next program start");
			hideOnStartup.setToolTipText("Check the box if you want to hide the changes on startup");
			hideOnStartup.setSelection(hide);
			
			hideOnStartup.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent e)
			    {
			    	hide = hideOnStartup.getSelection();
			    }
			});
		}
		
		
		shell.pack();
		
		SWTUtil.centerShell(shell);
	}

	public Object open() {
		createContents();
		Display display = getParent().getDisplay();
		shell.setSize(800, 500);
		SWTUtil.centerShell(shell);
		text.setTopIndex(text.getLineCount() - 1);
		shell.open();
		shell.layout();
		

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	public void setActive() {

		if (shell.isDisposed()) {
			open();
		} else {
			shell.setActive();
		}
		shell.setFocus();
	}

	public boolean isShowOnStartup() {
		return !hide;
	}
	
	public void setShowOnStartup(boolean show) {
		hide = !show;
	}
	
	public static void showChangeLogDialog() {
		
	}
}
