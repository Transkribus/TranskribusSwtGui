package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.mainwidget.ProgramInfo;

public class ChangeLogDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ChangeLogDialog.class);

	protected Object result;
	protected Shell shell;
	protected String changelog;

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

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImage(Images.getOrLoad("/icons/new.png"));

		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		rowLayout.fill = true;
		shell.setLayout(rowLayout);

		StyledText text = new StyledText(shell, SWT.BORDER | SWT.VERTICAL | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setText(changelog);
		text.setSize(500, 250);

		RowData rowData = new RowData(500, 250);
		text.setLayoutData(rowData);

		// shell.setMinimumSize(text.getSize());
		// shell.setSize(new Point(text.getSize().x + 50, text.getSize().y+100));

		text.setTopIndex(text.getLineCount() - 1);

		
		Button hideOnStartup = new Button(shell, SWT.CHECK);
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
		
		rowData = new RowData(500, SWT.DEFAULT);
		hideOnStartup.setLayoutData(rowData);
		
		
		shell.pack();
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		Display display = getParent().getDisplay();
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
}
