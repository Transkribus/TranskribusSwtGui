package eu.transkribus.swt_gui.dialogs;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorList;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.tool.error.ErrorTableLabelProvider;
import eu.transkribus.swt_gui.tool.error.ErrorTableViewer;

public class ErrorRateAdvancedStats extends Dialog{
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedStats.class);

	private TrpErrorRate resultErr;
	private Composite composite;
	Shell shell;
	
	ErrorTableViewer overall;
	ErrorTableViewer page;
	
	CTabFolder exportTypeTabFolder;
	CTabItem clientExportItem;
	CTabItem serverExportItem;

	private Button wikiErrButton, wikiFmeaButton, downloadXLS;
	ErrorTableLabelProvider labelProvider;
	Menu contextMenu;

	String lastExportFolder;
	String docName;
	ExportPathComposite exportPathComp;
	File result=null;
	SashForm sf;


	protected static final String HELP_WIKI_ERR = "https://en.wikipedia.org/wiki/Word_error_rate";
	protected static final String HELP_WIKI_FMEA = "https://en.wikipedia.org/wiki/F1_score";
	

	public ErrorRateAdvancedStats(Shell shell, TrpErrorRate resultErr) {
		super(shell);
		this.shell = shell;
		this.resultErr = resultErr;
		this.lastExportFolder = "";
		this.docName = "DocId_";
		
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Advanced Statistics");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);

		errOverallTable();
		
		errPageTable();
		
		downloadXls();
		
		return composite;
	}
	
	public void errOverallTable() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(1,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
	
		overall = new ErrorTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		overall.getTable().setLinesVisible(true);

		Table table = overall.getTable();
		table.setHeaderVisible(true);

		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(new String[] { "Overall", 
									resultErr.getWer(),
									resultErr.getCer(),
									resultErr.getwAcc(),
									resultErr.getcAcc(),
									resultErr.getBagTokensPrec(),
									resultErr.getBagTokensRec(),
									resultErr.getBagTokensF()
									});
		
	}
	
	public void errPageTable() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(1,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
	
		page = new ErrorTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		page.getTable().setLinesVisible(true);
		page.setContentProvider(new ArrayContentProvider());
		labelProvider = new ErrorTableLabelProvider(page);
		page.setLabelProvider(labelProvider);

		page.getTable().setHeaderVisible(true);
		
		page.getTable().getColumns();
	
		page.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		page.setInput(this.resultErr.getList() == null ? new ArrayList<>() : this.resultErr.getList());

	}
	
	public void downloadXls() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(2,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
	    
		exportPathComp = new ExportPathComposite(body, lastExportFolder, "File/Folder name: ", ".xls", docName+""+resultErr.getParams().getParameterValue("docId"));
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		downloadXLS = new Button(body,SWT.PUSH);
		downloadXLS.setText("Download XLS");
		downloadXLS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
					result = exportPathComp.getExportFile();
					logger.debug("Export path "+result.getAbsolutePath());
					createWorkBook(result.getAbsolutePath(), resultErr);
				
			}	
			
		});
		
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		wikiErrButton = createButton(parent, IDialogConstants.HELP_ID, "Error Rate", false);
		wikiErrButton.setImage(Images.HELP);

		wikiFmeaButton = createButton(parent, IDialogConstants.HELP_ID, "F-Measure", false);
		wikiFmeaButton.setImage(Images.HELP);
		
		createButton(parent, IDialogConstants.OK_ID, "Ok", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();	
		
		wikiErrButton.setLayoutData(buttonLd);
		wikiErrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_ERR, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});

		wikiFmeaButton.setLayoutData(buttonLd);
		wikiFmeaButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DesktopUtil.browse(HELP_WIKI_FMEA, "You can find the relevant information on the Wikipedia page.",
						getParentShell());
			}
		});
		

	}
	
	public void createWorkBook(String filePath , TrpErrorRate resultErr) {
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Error Measurements");
		Map<String, Object[]> excelData = new HashMap<String, Object[]>();
		int rowCount = 0;
		List<TrpErrorList> list = resultErr.getList();
		
		excelData.put(Integer.toString(rowCount++),new Object[] {
				"Pages",
				"Word Error Rate",
				"Char Error Rate",
				"Word Accuracy",
				"Char Accuracy",
				"Bag Tokens Precision",
				"Bag Tokens Recall",
				"Bag Tokens F-Measure"
				});
		
		excelData.put(Integer.toString(rowCount++),new Object[] {
				"Overall",
				resultErr.getWerDouble(),
				resultErr.getCerDouble(),
				resultErr.getwAccDouble(),
				resultErr.getcAccDouble(),
				resultErr.getBagTokensPrecDouble(),
				resultErr.getBagTokensRecDouble(),
				resultErr.getBagTokensFDouble()
				});
		
		for (TrpErrorList page : list) {
			if(rowCount < list.size()) {
				rowCount++;
			}
			excelData.put(Integer.toString(rowCount),new Object[] {
					"Page "+page.getPageNumber(),
					page.getWerDouble(),
					page.getCerDouble(),
					page.getwAccDouble(),
					page.getcAccDouble(),
					page.getBagTokensPrecDouble(),
					page.getBagTokensRecDouble(),
					page.getBagTokensFDouble()
					});
		}
		
		Set<String> keyset = excelData.keySet();
		int rownum = 0;
		for (String key : keyset) {
			Row row = sheet.createRow(rownum++);
			Object[] objArr = excelData.get(key);
			int cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				} else {
					cell.setCellValue((String) obj);
				}
			}
		}
		
		try {
			FileOutputStream file = new FileOutputStream(new File(filePath));
			workbook.write(file);
			file.close();
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	
	
}




