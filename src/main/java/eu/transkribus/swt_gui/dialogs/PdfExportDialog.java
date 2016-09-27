package eu.transkribus.swt_gui.dialogs;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class PdfExportDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(PdfExportDialog.class);
	private static final String PDF_EXT = ".pdf";
	File result=null;
	Shell shell;
//	Text baseFolderText, fileText;
//	Label pathLabel;
	String lastExportFolder;
	String adjustedDocName;
	int start, end;
	final int nrOfPages;
	Spinner startSpinner, endSpinner;
	Integer startPage, endPage;
	
	ExportPathComposite exportPathComp;

	public PdfExportDialog(Shell parent, int style, String lastExportFolder, int nrOfPages, String adjustedDocName) {
		super(parent, style |= SWT.DIALOG_TRIM);
		setText("Export as PDF");
		this.lastExportFolder = lastExportFolder;
		this.nrOfPages = nrOfPages;
		this.startPage = 1;
		this.endPage = nrOfPages;
		this.adjustedDocName = adjustedDocName;
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
//		shell.setSize(673, 420);
		shell.setSize(300, 300);
		shell.setText("Export PDF");
		shell.setLayout(new GridLayout(3, false));
		
		exportPathComp = new ExportPathComposite(shell, lastExportFolder, "File name: ", PDF_EXT, adjustedDocName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite pagesComposite = new Composite(shell, SWT.NONE);
		pagesComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1));
		pagesComposite.setLayout(new FillLayout());
		Label pagesLabel = new Label(pagesComposite, SWT.NONE);
		pagesLabel.setText("Pages: ");
		
		final ModifyListener pagesListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updatePages();
			}
		};
		
		startSpinner = new Spinner(pagesComposite, SWT.NONE);
		startSpinner.setMaximum(nrOfPages);
		startSpinner.setMinimum(1);
		startSpinner.addModifyListener(pagesListener);
		
		Label toLabel = new Label(pagesComposite, SWT.CENTER);
		toLabel.setText(" - ");
		endSpinner = new Spinner(pagesComposite, SWT.NONE);
		endSpinner.setMaximum(nrOfPages);
		endSpinner.setMinimum(1);
		endSpinner.setSelection(nrOfPages);
		endSpinner.addModifyListener(pagesListener);
		
		
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
		buttonComposite.setLayout(new FillLayout());
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
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

	private void updatePages() {
		startPage = startSpinner.getSelection();
		endPage = endSpinner.getSelection();
		logger.debug("pages " + startPage + "-" + endPage);
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
		
	public Integer getStartPage(){
		return startPage;
	}
	public Integer getEndPage(){
		return endPage;
	}

}
