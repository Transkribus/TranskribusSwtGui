package eu.transkribus.swt_gui.dialogs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swtchart.Chart;
import org.swtchart.IAxisSet;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.internal.series.LineSeries;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.htr.HtrTableWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.util.TextRecognitionConfig;
import eu.transkribus.util.TextRecognitionConfig.Mode;

public class TextRecognitionConfigDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(TextRecognitionConfigDialog.class);

	private Storage store = Storage.getInstance();
	
	private CTabFolder folder;
	private CTabItem citLabTabItem;
	
	private Group dictGrp;
	
	private HtrTableWidget htw;
	private Text nameTxt, langTxt, descTxt;
	private Button showTrainSetBtn, showTestSetBtn, showCharSetBtn;
	private Chart cerChart;
	
	private String charSetTitle, charSet;
//	private Integer trainSetId, testSetId;
	
	private DocImgViewerDialog trainDocViewer, testDocViewer = null;
	private CharSetViewerDialog charSetViewer = null;
	
	Combo htrDictCombo, ocrLangCombo, typeFaceCombo;
	
	private TrpHtr htr;
	
	List<String> htrDicts;
	
	private TextRecognitionConfig config;
	
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
		detailGrp.setLayout(new GridLayout(4, false));
		
		Label nameLbl = new Label(detailGrp, SWT.NONE);
		nameLbl.setText("Name:");
		nameTxt = new Text(detailGrp, SWT.BORDER | SWT.READ_ONLY);
		nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label langLbl = new Label(detailGrp, SWT.NONE);
		langLbl.setText("Language:");
		langTxt = new Text(detailGrp, SWT.BORDER | SWT.READ_ONLY);
		langTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label descLbl = new Label(detailGrp, SWT.NONE);
		descLbl.setText("Description:");
		descTxt = new Text(detailGrp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
		descTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		new Label(detailGrp, SWT.NONE);
		showTrainSetBtn = new Button(detailGrp, SWT.PUSH);
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
		
		showTestSetBtn = new Button(detailGrp, SWT.PUSH);
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
		
		showCharSetBtn = new Button(detailGrp, SWT.PUSH);
		showCharSetBtn.setText("Show Character Set");
		showCharSetBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		showCharSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				List<String> charList = parseCitLabCharSet(charSet);
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
			
			/** TODO put this into some HtrUtils class as static method
			 * @param charSet
			 * @return
			 */
			private List<String> parseCitLabCharSet(String charSet) {
				Pattern p = Pattern.compile("(.)=[0-9]+");
				Matcher m = p.matcher(charSet);
				List<String> result = new LinkedList<>();
				while(m.find()) {
					result.add(m.group(1));
				}
				return result;
			}
		});	
		
		Label cerLbl = new Label(detailGrp, SWT.NONE);
		cerLbl.setText("Train Curve:");
		
		cerChart = new Chart(detailGrp, SWT.BORDER);
		cerChart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		cerChart.getTitle().setVisible(false);
		cerChart.getAxisSet().getXAxis(0).getTitle().setText("Epochs");
		cerChart.getAxisSet().getYAxis(0).getTitle().setText("CER");
		
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
			for(int i = 0; i < htrDicts.size(); i++) {
				if(config.getDictionary().equals(htrDicts.get(i))) {
					htrDictCombo.select(i);
					break;
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
		nameTxt.setText(htr.getName());
		langTxt.setText(htr.getLanguage());
		descTxt.setText(htr.getDescription());
		
		charSetTitle = "Character Set of Model: " + htr.getName();
		charSet = htr.getCharList() == null || htr.getCharList().isEmpty() ? "N/A" : htr.getCharList();
		
		showCharSetBtn.setEnabled(htr.getCharList() != null && !htr.getCharList().isEmpty());
		
		this.htr = htr;
		
		showTestSetBtn.setEnabled(htr.getTestGtDocId() != 0);
		showTrainSetBtn.setEnabled(htr.getGtDocId() != 0);
		
		updateChart(htr.getCerString());
	}

	private void updateChart(String cerString) {
		String[] cerStrs = cerString.split(" ");
		double[] cerVals = new double[cerStrs.length];
		for(int i = 0; i < cerStrs.length; i++) {
			try {
				cerVals[i] = Double.parseDouble(cerStrs[i].replace(',', '.'));
			} catch(NumberFormatException e) {
				logger.error("Could not parse CER String: " + cerStrs[i]);
			}
		}
		
		ISeriesSet seriesSet = cerChart.getSeriesSet();
		ISeries series = seriesSet.createSeries(SeriesType.LINE, "CER series");
		series.setVisibleInLegend(false);
		((LineSeries)series).setAntialias(SWT.ON);
		series.setYSeries(cerVals);
		
		IAxisSet axisSet = cerChart.getAxisSet();
		axisSet.getXAxis(0).getTitle().setText("Epochs");
		axisSet.getYAxis(0).getTitle().setText("CER");
		axisSet.adjustRange();
		
		cerChart.redraw();
		cerChart.update();
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
	}
	
	public TextRecognitionConfig getConfig() {
		return config;
	}
	
	@Override
	protected void okPressed() {
		
		if(folder.getSelection().equals(citLabTabItem)) {
			config = new TextRecognitionConfig(Mode.CITlab);
			config.setDictionary(htrDicts.get(htrDictCombo.getSelectionIndex()));
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
