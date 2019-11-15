package eu.transkribus.swt_gui.htr;

import java.awt.BasicStroke;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.ws.rs.ClientErrorException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.enums.DataSetType;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.StrUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.MetadataTextFieldValidator;
import eu.transkribus.swt_gui.dialogs.CharSetViewerDialog;
import eu.transkribus.swt_gui.dialogs.DocImgViewerDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrDetailsWidget extends SashForm {
	private static final Logger logger = LoggerFactory.getLogger(HtrDetailsWidget.class);
	
	private static final String NOT_AVAILABLE = "N/A";

	private static final String[] CITLAB_TRAIN_PARAMS = { CitLabHtrTrainConfig.NUM_EPOCHS_KEY, 
			CitLabHtrTrainConfig.LEARNING_RATE_KEY, CitLabHtrTrainConfig.NOISE_KEY, CitLabHtrTrainConfig.TRAIN_SIZE_KEY,
			CitLabHtrTrainConfig.BASE_MODEL_ID_KEY, CitLabHtrTrainConfig.BASE_MODEL_NAME_KEY, CitLabHtrTrainConfig.EARLY_STOPPING_KEY };

	private static final String CER_TRAIN_KEY = "CER Train";
	private static final String CER_VAL_KEY = "CER Validation";
	
	Text nameTxt, langTxt, descTxt, nrOfLinesTxt, nrOfWordsTxt, finalTrainCerTxt, finalValCerTxt;
	Table paramTable;
	Button updateMetadataBtn, showTrainSetBtn, showValSetBtn, showCharSetBtn;
	ChartComposite jFreeChartComp;
	JFreeChart chart = null;
	DocImgViewerDialog trainDocViewer, valDocViewer = null;
	CharSetViewerDialog charSetViewer = null;
	
	private final Storage store;
	private TrpHtr htr;
	private final MetadataTextFieldValidator<TrpHtr> validator;
	
	public HtrDetailsWidget(Composite parent, int style) {
		super(parent, style);
		validator = new MetadataTextFieldValidator<>();
		
		// a composite for the HTR metadata
		Composite mdComp = new Composite(this, SWT.BORDER);
		mdComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mdComp.setLayout(new GridLayout(2, true));
		
		Label nameLbl = new Label(mdComp, SWT.NONE);
		nameLbl.setText("Name:");
		Label langLbl = new Label(mdComp, SWT.NONE);
		langLbl.setText("Language:");

		nameTxt = new Text(mdComp, SWT.BORDER);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		validator.attach("Name", nameTxt, 1, 100, h -> h.getName());
		
		langTxt = new Text(mdComp, SWT.BORDER);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		validator.attach("Language", langTxt, 1, 100, h -> h.getLanguage());

		Label descLbl = new Label(mdComp, SWT.NONE);
		descLbl.setText("Description:");
		Label paramLbl = new Label(mdComp, SWT.NONE);
		paramLbl.setText("Parameters:");

		descTxt = new Text(mdComp, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		descTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		validator.attach("Description", descTxt, 1, 2048, h -> h.getDescription());
		
		paramTable = new Table(mdComp, SWT.BORDER | SWT.V_SCROLL);
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
		
		//save, trainSet, valSet, charSet buttons
		final int numButtons = 4;
		btnComp.setLayout(new GridLayout(numButtons, true));

		updateMetadataBtn = new Button(btnComp, SWT.PUSH);
		updateMetadataBtn.setText("Save");
		updateMetadataBtn.setImage(Images.DISK);
		updateMetadataBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showTrainSetBtn = new Button(btnComp, SWT.PUSH);
		showTrainSetBtn.setText("Show Train Set");
		showTrainSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showValSetBtn = new Button(btnComp, SWT.PUSH);
		showValSetBtn.setText("Show Validation Set");
		showValSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showCharSetBtn = new Button(btnComp, SWT.PUSH);
		showCharSetBtn.setText("Show Characters");
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

		Label finalValCerLbl = new Label(cerComp, SWT.NONE);
		finalValCerLbl.setText("CER on Validation Set:");
		finalValCerTxt = new Text(cerComp, SWT.BORDER | SWT.READ_ONLY);
		finalValCerTxt.setLayoutData(gd);
		
		this.htr = null;
		store = Storage.getInstance();
		
		//init with no HTR selected, i.e. disable controls
		updateDetails(null);
		
		addListeners();
	}

	void updateDetails(TrpHtr htr) {		
		this.htr = htr;
		validator.setOriginalObject(htr);
		
		nameTxt.setEnabled(htr != null);
		descTxt.setEnabled(htr != null);
		langTxt.setEnabled(htr != null);
		jFreeChartComp.setEnabled(htr != null);
		
		logger.debug("HTR = " + (htr==null ? "null" : htr.toShortString()));
		
		if (htr == null) {
			//clear text fields and disable buttons
			nameTxt.setText("");
			descTxt.setText("");
			langTxt.setText("");
			finalTrainCerTxt.setText("");
			finalValCerTxt.setText("");
			paramTable.clearAll();
			nrOfLinesTxt.setText("");
			nrOfWordsTxt.setText("");
			showCharSetBtn.setEnabled(false);
			showValSetBtn.setEnabled(false);
			showTrainSetBtn.setEnabled(false);
			updateParamTable(null);
			updateChart(null);
			return;
		}
		
		nameTxt.setText(StrUtil.get(htr.getName()));
		langTxt.setText(StrUtil.get(htr.getLanguage()));
		descTxt.setText(StrUtil.get(htr.getDescription()));
		nrOfWordsTxt.setText(htr.getNrOfWords() > 0 ? "" + htr.getNrOfWords() : NOT_AVAILABLE);
		nrOfLinesTxt.setText(htr.getNrOfLines() > 0 ? "" + htr.getNrOfLines() : NOT_AVAILABLE);

		updateParamTable(htr.getParamsProps());

		showCharSetBtn.setEnabled(htr.getCharSetList() != null && !htr.getCharSetList().isEmpty());

		showTrainSetBtn.setEnabled(htr.hasTrainGt());
		showValSetBtn.setEnabled(htr.hasValidationGt());
		
		updateChart(htr);
		
		enableMetadataEditing(store.isAdminLoggedIn() || store.getUserId() == htr.getUserId());
	}
	
	private void enableMetadataEditing(boolean enabled) {
		updateMetadataBtn.setEnabled(enabled);
		Text[] mdTextFields = {
				nameTxt,
				descTxt,
				langTxt
			};
		for(Text t : mdTextFields) {
			t.setEditable(enabled);
		}
	}

	private void updateParamTable(Properties paramsProps) {
		paramTable.removeAll();
		if (paramsProps == null || paramsProps.isEmpty()) {
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
		String storedHtrValCerStr = NOT_AVAILABLE;

		double[] referenceSeries = null;
		if (htr != null && htr.hasCerLog()) {
			XYSeries series = buildXYSeries(CER_TRAIN_KEY, htr.getCerLog());
			dataset.addSeries(series);
			referenceSeries = htr.getCerLog();
		}

		if (htr != null && htr.hasCerTestLog()) {
			XYSeries valSeries = buildXYSeries(CER_VAL_KEY, htr.getCerTestLog());
			dataset.addSeries(valSeries);
			//if available then validation CER is reference for stored net
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
				plot.getRenderer().setSeriesPaint(seriesIndex++, java.awt.Color.BLUE);
			}
			
			if (htr.hasCerTestLog()) {
				double storedHtrValCer = htr.getCerTestLog()[storedNetEpoch - 1];
				storedHtrValCerStr = HtrCITlabUtils.formatCerVal(storedHtrValCer);
				plot.getRenderer().setSeriesPaint(seriesIndex++, java.awt.Color.RED);
			}
			
			//annotate storedNetEpoch in the chart
			lineAnnot = new XYLineAnnotation(storedNetEpoch, 0.0, storedNetEpoch, 100.0,
					new BasicStroke(), java.awt.Color.GREEN);
			lineAnnot.setToolTipText("Stored HTR");
			plot.addAnnotation(lineAnnot);
		} else {
			plot.setNoDataMessage("No data available");
		}
		
		jFreeChartComp.setChart(chart);
		triggerChartUpdate();

		finalTrainCerTxt.setText(storedHtrTrainCerStr);
		finalValCerTxt.setText(storedHtrValCerStr);
	}
	
	void triggerChartUpdate() {
		if(chart != null) {
			chart.fireChartChanged();
		}
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
	
	
	private void addListeners() {
		this.showTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(htr == null) {
					return;
				}
				if (trainDocViewer != null) {
					trainDocViewer.setVisible();
				} else {
					try {
						TrpDoc doc = store.getHtrDataSetAsDoc(store.getCollId(), htr, DataSetType.TRAIN);
						trainDocViewer = new DocImgViewerDialog(getShell(), "Train Set", doc);
						trainDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						logger.error(e1.getMessage(), e);
					}

					trainDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});
		
		this.showValSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(htr == null) {
					return;
				}
				if (valDocViewer != null) {
					valDocViewer.setVisible();
				} else {
					try {
						TrpDoc doc = store.getHtrDataSetAsDoc(store.getCollId(), htr, DataSetType.VALIDATION);
						valDocViewer = new DocImgViewerDialog(getShell(), "Validation Set", doc);
						valDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						logger.error(e1.getMessage(), e);
					}

					valDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});

		this.showCharSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(htr == null) {
					return;
				}
				if (charSetViewer != null) {
					charSetViewer.setVisible();
					charSetViewer.update(htr);
				} else {
					charSetViewer = new CharSetViewerDialog(getShell(), htr);
					charSetViewer.open();
					charSetViewer = null;
				}
			}
		});
		
		this.updateMetadataBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!validator.hasInputChanged()) {
					logger.debug("No changes. Ignoring {}", e);
					return;
				}
				updateMetadata();
			}
		});
	}
	
	private void updateMetadata() {
		if(!validator.isInputValid()) {
			final String msg = validator.getValidationErrorMessages().stream().collect(Collectors.joining("\n"));
			DialogUtil.showBalloonToolTip(updateMetadataBtn, SWT.ICON_WARNING, "Invalid input", msg);
			return;
		}
		TrpHtr htrToStore = new TrpHtr(HtrDetailsWidget.this.htr);
		htrToStore.setHtrId(htr.getHtrId());
		htrToStore.setName(nameTxt.getText());
		htrToStore.setDescription(descTxt.getText());
		htrToStore.setLanguage(langTxt.getText());
		try {
			store.updateHtrMetadata(htrToStore);
			//reset the text fields to new values
			updateDetails(htrToStore);
			DialogUtil.showBalloonToolTip(updateMetadataBtn, SWT.ICON_INFORMATION, "", "Changes saved.");
		} catch(Exception ex) {
			DialogUtil.showDetailedErrorMessageBox(getShell(), "Error while saving metadata", "HTR metadata could not be updated.", ex);
		}
	}
	
	/**
	 * Checks all editable text fields for unsaved changes and bothers the user with a yes/no-dialog that allows to save or discard changes.
	 */
	public void checkForUnsavedChanges() {
		if(!validator.hasInputChanged()) {
			//no changes. Go on
			return;
		}
		int answer = DialogUtil.showYesNoDialog(getShell(), "Unsaved Changes", 
				"You have edited the metadata of this model. Do you want to save the changes?", SWT.ICON_WARNING);
		if(answer == SWT.YES) {
			updateMetadata();
		}
	}
}
