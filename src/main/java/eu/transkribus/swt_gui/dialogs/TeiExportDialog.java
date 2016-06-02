package eu.transkribus.swt_gui.dialogs;

import java.io.File;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.builder.tei.TeiExportPars.TeiExportMode;
import eu.transkribus.swt_canvas.util.SWTUtil;

public class TeiExportDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TeiExportDialog.class);
	private static final String TEI_EXT = ".xml";
	File result=null;
	Shell shell;

	String lastExportFolder;
	String adjustedDocName;
//	int start, end;
//	final int nrOfPages;
	Button zonePerParRadio;
	Button zonePerLineRadio;
//	Spinner startSpinner, endSpinner;
//	Integer startPage, endPage;
	TeiExportMode mode;
	
	ExportPathComposite exportPathComp;

	public TeiExportDialog(Shell parent, int style, String lastExportFolder, int nrOfPages, String docName) {
		super(parent, style |= SWT.DIALOG_TRIM);
		this.lastExportFolder = lastExportFolder;
//		this.nrOfPages = nrOfPages;
		this.mode = TeiExportMode.ZONE_PER_PAR;
		this.adjustedDocName = docName;
//		this.startPage = 1;
//		this.endPage = nrOfPages;
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
//		shell.setSize(673, 420);
		shell.setSize(300, 300);
		shell.setText("Export as TEI XML");
		shell.setLayout(new GridLayout(3, false));
		
		exportPathComp = new ExportPathComposite(shell, lastExportFolder, "File name: ", TEI_EXT, adjustedDocName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite modeComposite = new Composite(shell, SWT.NONE);
		modeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1));
		modeComposite.setLayout(new FillLayout());
		Label modeLabel = new Label(modeComposite, SWT.NONE);
		modeLabel.setText("Mode: ");
		
		zonePerParRadio = new Button(modeComposite, SWT.RADIO);
		zonePerParRadio.setText("Zone per paragraph");
		zonePerParRadio.setSelection(true);
		
		zonePerLineRadio = new Button(modeComposite, SWT.RADIO);
		zonePerLineRadio.setText("Zone per line");
		
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		buttonComposite.setLayout(new FillLayout());
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateMode();
				if (exportPathComp.checkExportFile()) {
					result = exportPathComp.getExportFile();
					shell.close();
				}
			}
		});
//		saveButton.setToolTipText("Stores the configuration in the configuration file and closes the dialog");
//		
		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
//		closeButton.setToolTipText("Closes this dialog without saving");
		
		shell.pack();
	}
	
	private void updateMode() {
		if (zonePerLineRadio.getSelection()) {
			mode = TeiExportMode.ZONE_PER_LINE;
		} else {
			mode = TeiExportMode.ZONE_PER_PAR;
		}
		logger.debug("Mode: " + mode);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public File open() {
		result = null;
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
	
	public TeiExportMode getMode(){
		return mode;
	}
//	public Integer getEndPage(){
//		return endPage;
//	}

}
