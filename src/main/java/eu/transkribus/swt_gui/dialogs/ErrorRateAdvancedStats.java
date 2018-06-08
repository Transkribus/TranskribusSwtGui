package eu.transkribus.swt_gui.dialogs;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.swt.SWT;
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

import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.tool.error.ErrorTableLabelProvider;
import eu.transkribus.swt_gui.tool.error.ErrorTableViewer;

public class ErrorRateAdvancedStats extends Dialog{
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedStats.class);

	private TrpErrorRate resultErr;
	private Composite composite;
	
	private ErrorTableViewer overall;
	ErrorTableViewer page;

	private Button wikiErrButton, wikiFmeaButton, downloadXLS;
	ErrorTableLabelProvider labelProvider;
	Menu contextMenu;

	protected static final String HELP_WIKI_ERR = "https://en.wikipedia.org/wiki/Word_error_rate";
	protected static final String HELP_WIKI_FMEA = "https://en.wikipedia.org/wiki/F1_score";
	

	public ErrorRateAdvancedStats(Shell shell, TrpErrorRate resultErr) {
		super(shell);
		this.resultErr = resultErr;
		
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
		item.setText(new String[] { "Overall", resultErr.getWer(), resultErr.getCer(),resultErr.getwAcc(),resultErr.getcAcc(),resultErr.getBagTokensPrec(),resultErr.getBagTokensRec(),resultErr.getBagTokensF()});
		
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

		Table table = page.getTable();
		table.setHeaderVisible(true);
		
		page.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		page.setInput(this.resultErr.getList() == null ? new ArrayList<>() : this.resultErr.getList());
		
	
	}
	

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		wikiErrButton = createButton(parent, IDialogConstants.HELP_ID, "Error Rate", false);
		wikiErrButton.setImage(Images.HELP);

		wikiFmeaButton = createButton(parent, IDialogConstants.HELP_ID, "F-Measure", false);
		wikiFmeaButton.setImage(Images.HELP);
		
		downloadXLS = createButton(parent,0, "Download XLS", false);

		createButton(parent, IDialogConstants.OK_ID, "Ok", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
		GridData buttonLd = (GridData) getButton(IDialogConstants.CANCEL_ID).getLayoutData();
		
		downloadXLS = createButton(parent,0, "Download XLS", false);
		downloadXLS.setLayoutData(buttonLd);
		downloadXLS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				saveExcelData();
			}

		});
		
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
	private void saveExcelData() {
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Error Measures");
		
		Map<String, Object[]> excelData = new HashMap<String, Object[]>();
		
		
		
	}

}


