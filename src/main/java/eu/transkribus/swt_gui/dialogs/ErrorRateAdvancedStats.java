package eu.transkribus.swt_gui.dialogs;
import java.math.BigDecimal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import eu.transkribus.core.model.beans.TrpErrorRate;

public class ErrorRateAdvancedStats extends Dialog{

	private TrpErrorRate result;
	private Composite composite;

	
	public ErrorRateAdvancedStats(Shell shell, TrpErrorRate e) {
		super(shell);
		this.result = result;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Advanced Statistics");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		
		this.composite = (Composite) super.createDialogArea(parent);
		
		barChart();
		
		return composite;
	}
	
	public void barChart() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		JFreeChart barChart = new JFreeChart(null);

		BigDecimal wer = new BigDecimal(result.getWer().trim().replace("%", "")).divide(BigDecimal.valueOf(100));;
		BigDecimal cer = new BigDecimal(result.getWer().trim().replace("%", "")).divide(BigDecimal.valueOf(100));;
		dataset.addValue(wer, "Word Error Rate ", "");
		dataset.addValue(cer, "Character Error Rate", "");
		
		barChart = ChartFactory.createBarChart("Word and Character Error Rate", "", "Percentage", dataset);
		
		
		
	}

}
