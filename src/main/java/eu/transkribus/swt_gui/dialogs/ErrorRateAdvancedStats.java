package eu.transkribus.swt_gui.dialogs;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jfree.data.category.DefaultCategoryDataset;

import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;

public class ErrorRateAdvancedStats extends Dialog{

	private TrpErrorRate resultErr;
	private Composite composite;
	
	public static final String ERR_EMPTY_COL = " ";
	public static final String ERR_WORD_COL = "Error Rate";
	public static final String ERR_CHAR_COL = "Accuracy";
	public static final String BAG_PREC_COL = "Precision";
	public static final String BAG_REC_COL = "Recall";
	public static final String BAG_FMEA_COL = "F-Measure";

	MyTableViewer viewer;
	
	public final ColumnConfig[] ERR_COLS = new ColumnConfig[] { new ColumnConfig(ERR_EMPTY_COL, 200),
			new ColumnConfig(ERR_WORD_COL, 100), new ColumnConfig(ERR_CHAR_COL, 100),
			new ColumnConfig(BAG_PREC_COL, 100), new ColumnConfig(BAG_REC_COL, 100),
			new ColumnConfig(BAG_FMEA_COL, 100), };
	
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
		
		errTable();
		
//		barChart();
		
		return composite;
	}
	
	public void errTable() {
		
		Composite body = new Composite(composite,SWT.NONE);
		
		body.setLayout(new GridLayout(1,false));
		body.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,false));
	
		final MyTableViewer viewer = new MyTableViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTable().setLinesVisible(true);

		viewer.addColumns(ERR_COLS);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);

		TableItem itemWord = new TableItem(table, SWT.NONE);
		itemWord.setText(new String[] { "Word", resultErr.getWer(), resultErr.getwAcc(), "", "", "" });

		TableItem itemChar = new TableItem(table, SWT.NONE);
		itemChar.setText(new String[] { "Character", resultErr.getCer(), resultErr.getcAcc(), "", "", "" });

		TableItem itemBag = new TableItem(table, SWT.NONE);
		itemBag.setText(new String[] { "Bag of Tokens", "", "", resultErr.getBagTokensPrec(),
				resultErr.getBagTokensRec(), resultErr.getBagTokensF() });
	}
	
	public void barChart() {
		
		Composite chart = new Composite(composite,SWT.NONE);
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chart.setLayout(new GridLayout(4, false));
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

//		BigDecimal wer = new BigDecimal(result.getWer().trim().replace("%", "")).divide(BigDecimal.valueOf(100));
//		BigDecimal cer = new BigDecimal(result.getWer().trim().replace("%", "")).divide(BigDecimal.valueOf(100));
//		dataset.addValue(wer, "Word Error Rate ", "");
//		dataset.addValue(cer, "Character Error Rate", "");
//		
//		JFreeChart barChart = ChartFactory.createBarChart("Word and Character Error Rate", "", "Percentage", dataset,PlotOrientation.HORIZONTAL, true, true, true);
//	
		
		
		
	}

}
