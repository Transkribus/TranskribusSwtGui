package eu.transkribus.swt_gui.dialogs;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.ProgramInfo;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;

public class ChangeLogDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(ChangeLogDialog.class);

	protected Object result;
	protected Shell shell;
	protected String changelog;

	public ChangeLogDialog(Shell parent, int style) {
		super(parent, style|= (SWT.DIALOG_TRIM | SWT.RESIZE) );


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
	    
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.VERTICAL;
		shell.setLayout(fillLayout);
		
		StyledText text = new StyledText(shell, SWT.BORDER | SWT.VERTICAL | SWT.MULTI | /*SWT.WRAP |*/ SWT.V_SCROLL | SWT.H_SCROLL);
		text.setText(changelog);
		text.setSize(500, 250);

	    shell.setMinimumSize(text.getSize());
	    shell.setSize(new Point(text.getSize().x + 50, text.getSize().y+100));
	    
	    text.setTopIndex(text.getLineCount() - 1);
	}

	/**
	 * Open the dialog.
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

		if(shell.isDisposed()){
			open();
		}else{
			shell.setActive();	
			shell.setFocus();
		}
	}	
}
