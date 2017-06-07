package eu.transkribus.swt_gui.dialogs;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.CitLabHtrTrainConfig;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.StrUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.htr.HtrTableWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.util.TextRecognitionConfig;
import eu.transkribus.util.TextRecognitionConfig.Mode;

public class TextRecognitionConfigDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionConfigDialog.class);

	private static final String NOT_AVAILABLE = "N/A";
	
	private Storage store = Storage.getInstance();
	
	private CTabFolder folder;
	private CTabItem citLabTabItem;
	
	private Group dictGrp;
	
	private HtrTableWidget htw;
	private Text nameTxt, langTxt, descTxt, paramTxt, nrOfLinesTxt, nrOfWordsTxt, 
		finalTrainCerTxt, finalTestCerTxt;
	private Button showTrainSetBtn, showTestSetBtn, showCharSetBtn;
	private org.jfree.experimental.chart.swt.ChartComposite jFreeChartComp;
	
	private String charSetTitle, charSet;
//	private Integer trainSetId, testSetId;
	
	private DocImgViewerDialog trainDocViewer, testDocViewer = null;
	private CharSetViewerDialog charSetViewer = null;
	
	private Combo htrDictCombo, ocrLangCombo, typeFaceCombo;
	
	private TrpHtr htr;
	
	private List<String> htrDicts;
	
	private TextRecognitionConfig config;
	
	public final static String NO_DICTIONARY = "No dictionary";
	
	public TextRecognitionConfigDialog(Shell parent, TextRecognitionConfig config) {
		super(parent);
		this.config = config;
	}
    
	public void setVisible() {
		if(super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);
		
		
		
		folder = new CTabFolder(cont, SWT.BORDER | SWT.FLAT);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		citLabTabItem = new CTabItem(folder, SWT.NONE);
		citLabTabItem.setText("CITlab RNN HTR");
		
		SashForm uroSash = new SashForm(folder, SWT.HORIZONTAL);
		uroSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		uroSash.setLayout(new GridLayout(3, false));
//		sash.setWeights(new int[] {40, 60});
		
		htw = new HtrTableWidget(uroSash, SWT.BORDER);
		htw.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) htw.getTableViewer().getSelection();
				TrpHtr htr = (TrpHtr) sel.getFirstElement();		
				updateDetails(htr);
			}
		});
		
		final Table t = htw.getTableViewer().getTable();
		
		Menu menu = new Menu(t);
		t.setMenu(menu);
		
		MenuItem shareItem = new MenuItem(menu, SWT.NONE);
		shareItem.setText("Share model...");
		shareItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ChooseCollectionDialog ccd = new ChooseCollectionDialog(getParentShell());
				int ret = ccd.open();
				TrpCollection col = ccd.getSelectedCollection();
				TrpHtr htr = htw.getSelectedHtr();
				
				if(store.getCollId() == col.getColId()) {
					DialogUtil.showInfoMessageBox(getParentShell(), "Info", 
							"The selected HTR is already included in this collection.");
					return;
				}
				try {
					store.addHtrToCollection(htr, col);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not add HTR to collection!", e1);
					DialogUtil.showErrorMessageBox(getParentShell(), "Error sharing HTR", 
							"The selected HTR could not be added to this collection.");
				}
				super.widgetSelected(e);
			}
		});
		
		MenuItem delItem = new MenuItem(menu, SWT.NONE);
		delItem.setText("Remove model from collection");
		delItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrpHtr htr = htw.getSelectedHtr();
				try {
					store.removeHtrFromCollection(htr);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not remove HTR from collection!", e1);
					DialogUtil.showErrorMessageBox(getParentShell(), "Error removing HTR", 
							"The selected HTR could not be removed from this collection.");
				}
				super.widgetSelected(e);
			}
		});
		
		t.addListener(SWT.MenuDetect, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (t.getSelectionCount() <= 0) {
				      event.doit = false;
				} 
			}
			
		});
		
		Group detailGrp = new Group(uroSash, SWT.BORDER);
		detailGrp.setText("Details");
		detailGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailGrp.setLayout(new GridLayout(1, false));
		
		SashForm detailSash = new SashForm(detailGrp, SWT.VERTICAL);
		detailSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailSash.setLayout(new GridLayout(2, false));
		
		//a composite for the HTR metadata
		Composite mdComp = new Composite(detailSash, SWT.BORDER);
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
		
		// TODO possibly descTxt and paramTxt should have x/y scroll functionality?
		descTxt = new Text(mdComp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		descTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		paramTxt = new Text(mdComp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		paramTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
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
		showTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(trainDocViewer != null) {
					trainDocViewer.setVisible();
				} else {
					try {
						trainDocViewer = new DocImgViewerDialog(getParentShell(), "Train Set", store.getTrainSet(htr));
						trainDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					trainDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});
		
		showTestSetBtn = new Button(btnComp, SWT.PUSH);
		showTestSetBtn.setText("Show Test Set");
		showTestSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		showTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(testDocViewer != null) {
					testDocViewer.setVisible();
				} else {
					try {
						testDocViewer = new DocImgViewerDialog(getParentShell(), "Test Set", store.getTestSet(htr));
						testDocViewer.open();
					} catch (SessionExpiredException | ClientErrorException | IllegalArgumentException
							| NoConnectionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					testDocViewer = null;
				}
				super.widgetSelected(e);
			}
		});
		
		showCharSetBtn = new Button(btnComp, SWT.PUSH);
		showCharSetBtn.setText("Show Character Set");
		showCharSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showCharSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				List<String> charList = HtrCITlabUtils.parseCitLabCharSet(charSet);
				if(charSetViewer != null) {
					charSetViewer.setVisible();
				} else {
					try {
						charSetViewer = new CharSetViewerDialog(getParentShell(), "Character Set", charList);
						charSetViewer.open();
					} catch (ClientErrorException | IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					charSetViewer = null;
				}				
			}
		});	
		
		//a composite for the CER stuff
		Composite cerComp = new Composite(detailSash, SWT.BORDER);
		cerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		cerComp.setLayout(new GridLayout(4, false));
		
		Label cerLbl = new Label(cerComp, SWT.NONE);
		cerLbl.setText("Train Curve:");
		
//		cerChart = new Chart(detailGrp, SWT.BORDER);
//		cerChart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
//		cerChart.getTitle().setVisible(false);
//		cerChart.getAxisSet().getXAxis(0).getTitle().setText("Epochs");
//		cerChart.getAxisSet().getYAxis(0).getTitle().setText("CER");
		
		jFreeChartComp = new ChartComposite(cerComp, SWT.BORDER);
		jFreeChartComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		Label finalTrainCerLbl = new Label(cerComp, SWT.NONE);
		finalTrainCerLbl.setText("Final CER on Train Set:");
		finalTrainCerTxt = new Text(cerComp, SWT.BORDER | SWT.READ_ONLY);
		finalTrainCerTxt.setLayoutData(gd);
		
		Label finalTestCerLbl = new Label(cerComp, SWT.NONE);
		finalTestCerLbl.setText("Final CER on Test Set:");
		finalTestCerTxt = new Text(cerComp, SWT.BORDER | SWT.READ_ONLY);
		finalTestCerTxt.setLayoutData(gd);
		
		dictGrp = new Group(uroSash, SWT.NONE);
		dictGrp.setLayout(new GridLayout(1, false));
		dictGrp.setText("Dictionary");
		htrDictCombo = new Combo(dictGrp, SWT.READ_ONLY);
		htrDictCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		citLabTabItem.setControl(uroSash);

		folder.setSelection(citLabTabItem);
		
		updateHtrs();
		
		loadHtrDicts();
		htrDictCombo.setItems(this.htrDicts.toArray(new String[this.htrDicts.size()]));
		htrDictCombo.select(0);
				
		
//		updateDictGroup();
		applyConfig();
		
		uroSash.setWeights(new int[]{20, 60, 20});
		
		return cont;
	}
	
	private void applyConfig() {
		if(config == null) {
			return;
		}
		Mode mode = config.getMode();
		switch(mode) {
		case CITlab:
			htw.setSelection(config.getHtrId());
			
			if(config.getDictionary() == null) {
				//if this is null, no dictionary will be used
				// first entry in dictCombo is always "No dictionary"
				htrDictCombo.select(0);
			} else {
				//set the dictionary according to config
				for(int i = 0; i < htrDicts.size(); i++) {
					if(config.getDictionary().equals(htrDicts.get(i))) {
						htrDictCombo.select(i);
						break;
					}
				}
			}
			break;
		case UPVLC:
			break;
		default:
			break;
		
		}
	}

	private void updateDetails(TrpHtr htr) {
		nameTxt.setText(StrUtil.get(htr.getName()));
		langTxt.setText(StrUtil.get(htr.getLanguage()));
		descTxt.setText(StrUtil.get(htr.getDescription()));
		nrOfWordsTxt.setText(htr.getNrOfWords() > 0 ? ""+htr.getNrOfWords() : NOT_AVAILABLE);
		nrOfLinesTxt.setText(htr.getNrOfLines() > 0 ? ""+htr.getNrOfLines() : NOT_AVAILABLE);
		
		final String params = layoutParams(htr.getParamsProps());
		paramTxt.setText(params.isEmpty() ? NOT_AVAILABLE : params);
		
		charSetTitle = "Character Set of Model: " + htr.getName();
		charSet = htr.getCharList() == null || htr.getCharList().isEmpty() ? NOT_AVAILABLE : htr.getCharList();
		
		showCharSetBtn.setEnabled(htr.getCharList() != null && !htr.getCharList().isEmpty());
		
		this.htr = htr;
		
		showTestSetBtn.setEnabled(htr.getTestGtDocId() != null && htr.getTestGtDocId() > 0);
		showTrainSetBtn.setEnabled(htr.getGtDocId() != null);
		
		final double[] cerTrainSetVals = HtrCITlabUtils.parseCitlabCerString(htr.getCerString());
		final double[] cerTestSetVals = HtrCITlabUtils.parseCitlabCerString(htr.getCerTestString());
		updateChart(cerTrainSetVals, cerTestSetVals);
		finalTrainCerTxt.setText(HtrCITlabUtils.printLastCerPercentage(cerTrainSetVals));
		finalTestCerTxt.setText(HtrCITlabUtils.printLastCerPercentage(cerTestSetVals));
	}

	private String layoutParams(Properties p) {
		if(p.isEmpty()) {
			return "";
		}
		String res = "";
		res += CitLabHtrTrainConfig.NUM_EPOCHS_KEY + ":\t\t\t" 
				+ p.getProperty(CitLabHtrTrainConfig.NUM_EPOCHS_KEY) + "\n";
		res += CitLabHtrTrainConfig.LEARNING_RATE_KEY + ":\t\t\t" 
				+ p.getProperty(CitLabHtrTrainConfig.LEARNING_RATE_KEY) + "\n";
		res += CitLabHtrTrainConfig.NOISE_KEY + ":\t\t\t\t"
				+ p.getProperty(CitLabHtrTrainConfig.NOISE_KEY) + "\n";
		res += CitLabHtrTrainConfig.TRAIN_SIZE_KEY + ":\t"
				+ p.getProperty(CitLabHtrTrainConfig.TRAIN_SIZE_KEY) + "\n";
		if(p.containsKey(CitLabHtrTrainConfig.BASE_MODEL_ID_KEY)) {
			res += CitLabHtrTrainConfig.BASE_MODEL_ID_KEY + ":\t"
					+ p.getProperty(CitLabHtrTrainConfig.BASE_MODEL_ID_KEY) + "\n";
			res += CitLabHtrTrainConfig.BASE_MODEL_NAME_KEY + ":\t"
					+ p.getProperty(CitLabHtrTrainConfig.BASE_MODEL_NAME_KEY);
		}
		return res;
	}

	private void updateChart(double[] cerTrainSetVals, double[] cerTestSetVals) {
		
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		final String cerTrainKey = "CER Train";
		XYSeries series = new XYSeries(cerTrainKey);
		series.setDescription(cerTrainKey);
	    for(int i = 0; i < cerTrainSetVals.length; i++) {
	    	double val = cerTrainSetVals[i];
	    	series.add(i+1, val);
	    }
		dataset.addSeries(series);	
		
		if(cerTestSetVals.length > 0) {
			final String cerTestKey = "CER Test";
			XYSeries testSeries = new XYSeries(cerTestKey);
			testSeries.setDescription(cerTestKey);
		    for(int i = 0; i < cerTestSetVals.length; i++) {
		    	double val = cerTestSetVals[i];
		    	testSeries.add(i+1, val);
		    }
			dataset.addSeries(testSeries);	
		}

		JFreeChart chart = ChartFactory.createXYLineChart(
				"CER Series", "Epochs", "CER", dataset, PlotOrientation.VERTICAL, true, true, false);

		XYPlot plot = (XYPlot)chart.getPlot();
		LogAxis logAxis = new LogAxis("CER");
		TickUnits tickUnits = new TickUnits();
		tickUnits.add(new NumberTickUnit(0.1, NumberFormat.getPercentInstance()));
		logAxis.setStandardTickUnits(tickUnits);//NumberAxis.createStandardTickUnits());
		plot.setRangeAxis(logAxis);
		plot.getRenderer().setSeriesPaint(0, Color.BLUE);
		plot.getRenderer().setSeriesPaint(1, Color.RED);
		jFreeChartComp.setChart(chart);
		chart.fireChartChanged();
	}

	private void updateHtrs() {
		List<TrpHtr> uroHtrs = new ArrayList<>(0);
		try {
			uroHtrs = store.listHtrs("CITlab");
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e1) {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Could not load HTR model list!");
			return;
		}
		
		htw.refreshList(uroHtrs);
	}
	
	private void loadHtrDicts(){
		try {
			this.htrDicts = store.getHtrDicts();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			TrpMainWidget.getInstance().onError("Error", "Could not load HTR model list!", e);
			htrDicts = new ArrayList<>(0);
		}
		htrDicts.add(0, NO_DICTIONARY);
	}
	
	public TextRecognitionConfig getConfig() {
		return config;
	}
	
	@Override
	protected void okPressed() {
		
		if(folder.getSelection().equals(citLabTabItem)) {
			config = new TextRecognitionConfig(Mode.CITlab);
			
			final String dictName = htrDicts.get(htrDictCombo.getSelectionIndex());
			config.setDictionary(dictName == NO_DICTIONARY ? null : dictName);
			TrpHtr htr = htw.getSelectedHtr();
			if(htr == null) {
				DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Please select a HTR.");
				return;
			}
			config.setHtrId(htr.getHtrId());
			config.setHtrName(htr.getName());
			config.setLanguage(htr.getLanguage());
		} else {
			DialogUtil.showErrorMessageBox(this.getParentShell(), "Error", "Bad configuration!");
		}
		
		
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Text Recognition Configuration");
		newShell.setMinimumSize(800, 600);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1024, 768);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
		// setBlockOnOpen(false);
	}
}
