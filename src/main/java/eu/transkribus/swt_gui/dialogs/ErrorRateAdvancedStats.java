package eu.transkribus.swt_gui.dialogs;



import java.awt.Color;
import java.awt.Paint;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.swt.ChartComposite;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpErrorRateListEntry;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt_gui.TrpGuiPrefs;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.tool.error.ErrorTableLabelProvider;
import eu.transkribus.swt_gui.tool.error.ErrorTableViewer;

public class ErrorRateAdvancedStats extends Dialog{
	private final static Logger logger = LoggerFactory.getLogger(ErrorRateAdvancedStats.class);

	private TrpErrorRate resultErr;
	private Composite composite;
	Storage store;
	Shell shell;
	
	ChartComposite jFreeChartComp;
	JFreeChart chart = null;
	
	ErrorTableViewer overall;
	ErrorTableViewer page;
	
	CTabFolder exportTypeTabFolder;
	CTabItem clientExportItem;
	CTabItem serverExportItem;

	private Button wikiErrButton, wikiFmeaButton, downloadXLS;
	ErrorTableLabelProvider labelProvider;
	Menu contextMenu;

	String lastExportFolder;
	String lastExportFolderTmp;
	String docName;
	ExportPathComposite exportPathComp;
	File result=null;
	SashForm sf;


	protected static final String HELP_WIKI_ERR = "https://en.wikipedia.org/wiki/Word_error_rate";
	protected static final String HELP_WIKI_FMEA = "https://en.wikipedia.org/wiki/F1_score";
	

	public ErrorRateAdvancedStats(Shell shell, TrpErrorRate resultErr, Integer docId) {
		super(shell);
		this.shell = shell;
		this.resultErr = resultErr;
		this.lastExportFolder = "";
		this.docName = "DocId_"+docId;
		setShellStyle(getShellStyle() | SWT.RESIZE);

		
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Advanced Statistics");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		chartComposite();

		errOverallTable();
		
		errPageTable();
		
		downloadXls();
		
		return composite;
	}
	
	private void chartComposite() {
		
		jFreeChartComp = new ChartComposite(composite, SWT.FILL);
		jFreeChartComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 15, 30));
		updateChartOverall();
	
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
		overall.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		overall.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateChartOverall();
			}
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

		page.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		page.setInput(this.resultErr.getList() == null ? new ArrayList<>() : this.resultErr.getList());
			
		page.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
			
				TableItem[] selection = page.getTable().getSelection();
				updateChart(selection);
			}
		});

	}
	
	private void updateChartOverall() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		List<TrpErrorRateListEntry> list = resultErr.getList();
		
		for(TrpErrorRateListEntry temp : list) {
			dataset.addValue(temp.getWerDouble(), "WER", "Page "+temp.getPageNumber());
			dataset.addValue(temp.getCerDouble(), "CER", "Page "+temp.getPageNumber());
//			dataset.addValue(temp.getwAccDouble(), "Word Accuracy", "Page "+temp.getPageNumber());
//			dataset.addValue(temp.getcAccDouble(), "Char Accuracy", "Page "+temp.getPageNumber());
//			dataset.addValue(temp.getBagTokensPrecDouble(), "Bag Tokens Precision", "Page "+temp.getPageNumber());
//			dataset.addValue(temp.getBagTokensRecDouble(), "Bag Tokens Recall", "Page "+temp.getPageNumber());
//			dataset.addValue(temp.getBagTokensFDouble(), "Bag Tokens F-Measure", "Page "+temp.getPageNumber());
		}
		
		chart = ChartFactory.createBarChart("Error Rate Chart", "Category", "Value", dataset,PlotOrientation.VERTICAL,true,true,false);
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		plot.setBackgroundPaint(new Color(255,255,255));
		plot.setRangeGridlinePaint(Color.black);
		plot.setOutlineVisible(false);
		renderer.setBarPainter(new StandardBarPainter());
		
		Paint[] colors = {
		                  new Color(0, 172, 178),      // blue
		                  new Color(239, 70, 55),      // red
		                  new Color(85, 177, 69),      // green
		                  new Color(255, 255, 51),     //yellow
		                  new Color(128, 128, 128),   //grey
		                  new Color(255, 128, 0),  	  //orange
		                  new Color(178,102,255)
		};
		
		for(int i=0; i < 7; i++) {
			renderer.setSeriesPaint(i, colors[i % colors.length]);
		}
		
		jFreeChartComp.setChart(chart);
		chart.fireChartChanged();
		
	}
	
	protected void updateChart(TableItem[] selection) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		TrpErrorRateListEntry page = (TrpErrorRateListEntry) selection[0].getData();
		
		dataset.addValue(page.getWerDouble(), "WER", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getWerDouble(), "WER", "Overall");
		dataset.addValue(page.getCerDouble(), "CER", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getCerDouble(), "CER", "Overall");
		dataset.addValue(page.getwAccDouble(), "Word Accuracy", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getwAccDouble(), "Word Accuracy", "Overall");
		dataset.addValue(page.getcAccDouble(), "Char Accuracy", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getcAccDouble(), "Char Accuracy", "Overall");
		dataset.addValue(page.getBagTokensPrecDouble(), "Bag Tokens Precision", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getBagTokensPrecDouble(), "Bag Tokens Precision", "Overall");
		dataset.addValue(page.getBagTokensRecDouble(), "Bag Tokens Recall", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getBagTokensRecDouble(), "Bag Tokens Recall", "Overall");
		dataset.addValue(page.getBagTokensFDouble(), "Bag Tokens F-Measure", "Page "+page.getPageNumber());
		dataset.addValue(resultErr.getBagTokensFDouble(), "Bag Tokens F-Measure", "Overall");
		
		chart = ChartFactory.createBarChart("Error Rate Chart", "Category", "Value", dataset,PlotOrientation.VERTICAL,true,true,false);
		CategoryPlot plot = chart.getCategoryPlot();
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		plot.setBackgroundPaint(new Color(255,255,255));
		plot.setRangeGridlinePaint(Color.black);
		plot.setOutlineVisible(false);
		
		renderer.setBarPainter(new StandardBarPainter());
		
		Paint[] colors = {
                new Color(0, 172, 178),      // blue
                new Color(239, 70, 55),      // red
                new Color(85, 177, 69),      // green
                new Color(255, 255, 51),     //yellow
                new Color(128, 128, 128),   //grey
                new Color(255, 128, 0),  	  //orange
                new Color(178,102,255)
		};

		for(int i=0; i < 7; i++) {
			renderer.setSeriesPaint(i, colors[i % colors.length]);
		}
		
		jFreeChartComp.setChart(chart);
		chart.fireChartChanged();
		
	}

	public void downloadXls() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(1,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
		
		try {
			lastExportFolderTmp = TrpGuiPrefs.getLastExportFolder();
		} catch (Exception e) {
			logger.error("Could not load last export folder");
		}
		if (lastExportFolderTmp != null && !lastExportFolderTmp.equals("")) {
			lastExportFolder = lastExportFolderTmp;
		}
	    
		exportPathComp = new ExportPathComposite(body, lastExportFolder, "File/Folder name: ", ".xls", docName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		downloadXLS = new Button(body,SWT.PUSH);
		downloadXLS.setText("Download XLS");
		downloadXLS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
					result = exportPathComp.getExportFile();
					logger.debug("Export path "+exportPathComp.getBaseFolderText());
					TrpGuiPrefs.storeLastExportFolder(exportPathComp.getBaseFolderText());
					createWorkBook(result.getAbsolutePath(), resultErr);
					MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
					dialog.setText("XLS created");
					dialog.setMessage("The Worksheet has been created and saved in : "+result.getPath());
					dialog.open();
				
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
		GridData buttonLd = (GridData) getButton(IDialogConstants.OK_ID).getLayoutData();	
		
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
		List<TrpErrorRateListEntry> list = resultErr.getList();
		
		excelData.put(Integer.toString(1),new Object[] {
				"Pages",
				"Word Error Rate",
				"Char Error Rate",
				"Word Accuracy",
				"Char Accuracy",
				"Bag Tokens Precision",
				"Bag Tokens Recall",
				"Bag Tokens F-Measure"
				});
		
		excelData.put(Integer.toString(rowCount),new Object[] {
				"Overall",
				resultErr.getWerDouble(),
				resultErr.getCerDouble(),
				resultErr.getwAccDouble(),
				resultErr.getcAccDouble(),
				resultErr.getBagTokensPrecDouble(),
				resultErr.getBagTokensRecDouble(),
				resultErr.getBagTokensFDouble()
				});
		
		if(resultErr.getList() != null) {
			for (TrpErrorRateListEntry page : list) {
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




