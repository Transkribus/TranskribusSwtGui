package eu.transkribus.swt_gui.htr;

import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.swt.ChartComposite;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.StrUtil;

public class HtrDetailsWidget extends SashForm {
	private static final Logger logger = LoggerFactory.getLogger(HtrDetailsWidget.class);

	private static final String NOT_AVAILABLE = "N/A";

	private static final String[] CITLAB_TRAIN_PARAMS = { CitLabHtrTrainConfig.NUM_EPOCHS_KEY,
			CitLabHtrTrainConfig.LEARNING_RATE_KEY, CitLabHtrTrainConfig.NOISE_KEY, CitLabHtrTrainConfig.TRAIN_SIZE_KEY,
			CitLabHtrTrainConfig.BASE_MODEL_ID_KEY, CitLabHtrTrainConfig.BASE_MODEL_NAME_KEY };

	private static final String CER_TRAIN_KEY = "CER Train";
	private static final String CER_TEST_KEY = "CER Test";
	
	Text nameTxt, langTxt, descTxt, nrOfLinesTxt, nrOfWordsTxt, finalTrainCerTxt, finalTestCerTxt;
	Table paramTable;
	Button showTrainSetBtn, showTestSetBtn, showCharSetBtn;
	ChartComposite jFreeChartComp;
	JFreeChart chart = null;
	
	public HtrDetailsWidget(Composite parent, int style) {
		super(parent, style);
		
		// a composite for the HTR metadata
		Composite mdComp = new Composite(this, SWT.BORDER);
		mdComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mdComp.setLayout(new GridLayout(2, true));
		
		Label nameLbl = new Label(mdComp, SWT.NONE);
		nameLbl.setText("Name:");
		Label langLbl = new Label(mdComp, SWT.NONE);
		langLbl.setText("Language:");

		nameTxt = new Text(mdComp, SWT.BORDER | SWT.READ_ONLY);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		langTxt = new Text(mdComp, SWT.BORDER | SWT.READ_ONLY);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label descLbl = new Label(mdComp, SWT.NONE);
		descLbl.setText("Description:");
		Label paramLbl = new Label(mdComp, SWT.NONE);
		paramLbl.setText("Parameters:");

		// TODO possibly descTxt and paramTxt should have x/y scroll
		// functionality?
		descTxt = new Text(mdComp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		descTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		paramTable = new Table(mdComp, SWT.BORDER);
		paramTable.setHeaderVisible(false);
		TableColumn paramCol = new TableColumn(paramTable, SWT.NONE);
		paramCol.setText("Parameter");
		TableColumn valueCol = new TableColumn(paramTable, SWT.NONE);
		valueCol.setText("Value");
		paramTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		Label nrOfWordsLbl = new Label(mdComp, SWT.NONE);
		nrOfWordsLbl.setText("Nr. of Words:");
		Label nrOfLinesLbl = new Label(mdComp, SWT.NONE);
		nrOfLinesLbl.setText("Nr. of Lines:");

		nrOfWordsTxt = new Text(mdComp, SWT.BORDER | SWT.READ_ONLY);
		nrOfWordsTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		nrOfLinesTxt = new Text(mdComp, SWT.BORDER | SWT.READ_ONLY);
		nrOfLinesTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Composite btnComp = new Composite(mdComp, SWT.NONE);
		btnComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnComp.setLayout(new GridLayout(3, true));

		showTrainSetBtn = new Button(btnComp, SWT.PUSH);
		showTrainSetBtn.setText("Show Train Set");
		showTrainSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showTestSetBtn = new Button(btnComp, SWT.PUSH);
		showTestSetBtn.setText("Show Test Set");
		showTestSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showCharSetBtn = new Button(btnComp, SWT.PUSH);
		showCharSetBtn.setText("Show Character Set");
		showCharSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// a composite for the CER stuff
		Composite cerComp = new Composite(this, SWT.BORDER);
		cerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cerComp.setLayout(new GridLayout(4, false));

		// Label cerLbl = new Label(cerComp, SWT.NONE);
		// cerLbl.setText("Train Curve:");

		jFreeChartComp = new ChartComposite(cerComp, SWT.BORDER);
		jFreeChartComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		Label finalTrainCerLbl = new Label(cerComp, SWT.NONE);
		finalTrainCerLbl.setText("CER on Train Set:");
		finalTrainCerTxt = new Text(cerComp, SWT.BORDER | SWT.READ_ONLY);
		finalTrainCerTxt.setLayoutData(gd);

		Label finalTestCerLbl = new Label(cerComp, SWT.NONE);
		finalTestCerLbl.setText("CER on Test Set:");
		finalTestCerTxt = new Text(cerComp, SWT.BORDER | SWT.READ_ONLY);
		finalTestCerTxt.setLayoutData(gd);
	}
	
	void updateDetails(TrpHtr htr) {
		if (htr == null) {
			return;
		}
		logger.debug("HTR = " + (htr==null ? "null" : htr.toShortString()));
		
		nameTxt.setText(StrUtil.get(htr.getName()));
		langTxt.setText(StrUtil.get(htr.getLanguage()));
		descTxt.setText(StrUtil.get(htr.getDescription()));
		nrOfWordsTxt.setText(htr.getNrOfWords() > 0 ? "" + htr.getNrOfWords() : NOT_AVAILABLE);
		nrOfLinesTxt.setText(htr.getNrOfLines() > 0 ? "" + htr.getNrOfLines() : NOT_AVAILABLE);

		updateParamTable(htr.getParamsProps());

		showCharSetBtn.setEnabled(htr.getCharSetList() != null && !htr.getCharSetList().isEmpty());

		showTestSetBtn.setEnabled(htr.getTestGtDocId() != null && htr.getTestGtDocId() > 0);
		showTrainSetBtn.setEnabled(htr.getGtDocId() != null);

		updateChart(htr);
	}

	private void updateParamTable(Properties paramsProps) {
		paramTable.removeAll();
		if (paramsProps.isEmpty()) {
			TableItem item = new TableItem(paramTable, SWT.NONE);
			item.setText(0, NOT_AVAILABLE);
			item.setText(1, NOT_AVAILABLE);
		} else {
			for (String s : CITLAB_TRAIN_PARAMS) {
				if (paramsProps.containsKey(s)) {
					TableItem item = new TableItem(paramTable, SWT.NONE);
					item.setText(0, s + " ");
					item.setText(1, paramsProps.getProperty(s));
				}
			}
		}
		paramTable.getColumn(0).pack();
		paramTable.getColumn(1).pack();
	}

	private void updateChart(final TrpHtr htr) {
		XYSeriesCollection dataset = new XYSeriesCollection();
		String storedHtrTrainCerStr = NOT_AVAILABLE;
		String storedHtrTestCerStr = NOT_AVAILABLE;

		double[] referenceSeries = null;
		if (htr.hasCerLog()) {
			XYSeries series = buildXYSeries(CER_TRAIN_KEY, htr.getCerLog());
			dataset.addSeries(series);
			referenceSeries = htr.getCerLog();
		}

		if (htr.hasCerTestLog()) {
			XYSeries testSeries = buildXYSeries(CER_TEST_KEY, htr.getCerTestLog());
			dataset.addSeries(testSeries);
			//if available then test CER is reference for stored net
			referenceSeries = htr.getCerTestLog();
		}
		
		chart = ChartFactory.createXYLineChart("Learning Curve", "Epochs", "Accuracy in CER", dataset,
				PlotOrientation.VERTICAL, true, true, false);
		XYPlot plot = (XYPlot) chart.getPlot();
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		DecimalFormat pctFormat = new DecimalFormat("#%");
		rangeAxis.setNumberFormatOverride(pctFormat);
		rangeAxis.setRange(0.0, 1.0);
		
		int storedNetEpoch = -1;
		XYLineAnnotation lineAnnot = null;
		if(referenceSeries != null && referenceSeries.length > 0) {
			//determine location of best net annotation line and final CER values to show in text fields
			double min = Double.MAX_VALUE;
			if(htr.isBestNetStored()) {
				//if best net is stored then seach reference CER series for the minimum value
				for (int i = 0; i < referenceSeries.length; i++) {
					final double val = referenceSeries[i];
					//HTR+ always stores best net. If validation CER does not change, the first net with this CER is kept
					if (val < min) {
						min = val;
						storedNetEpoch = i + 1;
					}
				}
			} else {
				//set last epoch as minimum
				storedNetEpoch = referenceSeries.length;
			}
			logger.debug("best net stored after epoch {}", storedNetEpoch);
			int seriesIndex = 0;
			if(htr.hasCerLog()) {
				double storedHtrTrainCer = htr.getCerLog()[storedNetEpoch - 1];
				storedHtrTrainCerStr = HtrCITlabUtils.formatCerVal(storedHtrTrainCer);
				plot.getRenderer().setSeriesPaint(seriesIndex++, Color.BLUE);
			}
			
			if (htr.hasCerTestLog()) {
				double storedHtrTestCer = htr.getCerTestLog()[storedNetEpoch - 1];
				storedHtrTestCerStr = HtrCITlabUtils.formatCerVal(storedHtrTestCer);
				plot.getRenderer().setSeriesPaint(seriesIndex++, Color.RED);
			}
			
			//annotate storedNetEpoch in the chart
			lineAnnot = new XYLineAnnotation(storedNetEpoch, 0.0, storedNetEpoch, 100.0,
					new BasicStroke(), Color.GREEN);
			lineAnnot.setToolTipText("Stored HTR");
			plot.addAnnotation(lineAnnot);
		} else {
			plot.setNoDataMessage("No data available");
		}
		
		jFreeChartComp.setChart(chart);
		chart.fireChartChanged();

		finalTrainCerTxt.setText(storedHtrTrainCerStr);
		finalTestCerTxt.setText(storedHtrTestCerStr);
	}

	private XYSeries buildXYSeries(String name, double[] cerLog) {
		XYSeries series = new XYSeries(name);
		series.setDescription(name);
		// build XYSeries
		for (int i = 0; i < cerLog.length; i++) {
			double val = cerLog[i];
			series.add(i + 1, val);
		}
		return series;
	}
	
	Button getShowTrainSetBtn() {
		return showTrainSetBtn;
	}
	
	Button getShowTestSetBtn() {
		return showTestSetBtn;
	}
	
	Button getShowCharSetBtn() {
		return showCharSetBtn;
	}
	
	void triggerChartUpdate() {
		if(chart != null) {
			chart.fireChartChanged();
		}
	}
}
