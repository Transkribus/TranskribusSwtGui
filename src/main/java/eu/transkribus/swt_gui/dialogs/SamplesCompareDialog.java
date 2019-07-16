package eu.transkribus.swt_gui.dialogs;


import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.NullArgumentException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.swt.ChartComposite;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.io.util.TrpProperties;
import eu.transkribus.core.model.beans.TrpComputeSample;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpErrorRate;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpLocation;
import eu.transkribus.core.model.beans.rest.ParameterMap;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.JaxbUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt_gui.collection_treeviewer.CollectionContentProvider;
import eu.transkribus.swt_gui.collection_treeviewer.SampleLabelProvider;
import eu.transkribus.swt_gui.htr.DataSetMetadata;
import eu.transkribus.swt_gui.htr.DocumentDataSetTableWidget;
import eu.transkribus.swt_gui.htr.treeviewer.DocumentDataSelectionEntry;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.kws.KwsResultTableWidget;
import eu.transkribus.swt_gui.tool.error.TrpSampleResultTableEntry;


public class SamplesCompareDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(SamplesCompareDialog.class);
	
	private final int colId;

	private CTabFolder paramTabFolder;
	private CTabItem samplesTabItem, computeSampleTabItem;
	
	LabeledText nrOfLinesTxt;
	
	private Text modelNameTxt, descTxt;

	private Storage store = Storage.getInstance();

	private List<TrpDocMetadata> docList;
	private List<TrpDocMetadata> sampleDocList;
	
	private static final RGB BLUE_RGB = new RGB(0, 0, 140);
	private static final RGB LIGHT_BLUE_RGB = new RGB(0, 140, 255);
	private static final RGB CYAN_RGB = new RGB(85, 240, 240);
	
	private static final Color BLUE = Colors.createColor(BLUE_RGB);
	private static final Color LIGHT_BLUE = Colors.createColor(LIGHT_BLUE_RGB);
	private static final Color CYAN = Colors.createColor(CYAN_RGB);
	private static final Color WHITE = Colors.getSystemColor(SWT.COLOR_WHITE);
	private static final Color BLACK = Colors.getSystemColor(SWT.COLOR_BLACK);
	
	private TreeViewer tv, tvCompute;
	private CollectionContentProvider contentProv, contentProvComp;
	private SampleLabelProvider labelProv;
	private Composite buttonComp,buttonComputeComp, jobsComp, samplesConfComposite ;
	private KwsResultTableWidget resultTable;
	private ChartComposite jFreeChartComp;
	private Button addToSampleSetBtn, removeFromSampleSetBtn,createSampleButton, computeSampleBtn;
	private ParameterMap params = new ParameterMap();
	DecimalFormat df;
	Combo comboRef,comboHyp;
	Label labelRef,labelHyp, chartText, cerText;
	TrpDocMetadata docMd;
	JFreeChart chart;
	private DocumentDataSetTableWidget sampleSetOverviewTable;
	private Map<TrpDocMetadata, List<TrpPage>> sampleDocMap;
	
	ResultLoader rl;
	

	public SamplesCompareDialog(Shell parentShell) {
		super(parentShell);
		docList = store.getDocList();
		sampleDocList = new ArrayList<>();
		colId = store.getCollId();
		docMd = new TrpDocMetadata();
		df = new DecimalFormat("#0.000");
		rl = new ResultLoader();
		
	}
	
	public void setVisible() {
		if (super.getShell() != null && !super.getShell().isDisposed()) {
			super.getShell().setVisible(true);
		}
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Compare Samples");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 900);
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.CLOSE | SWT.MAX | SWT.RESIZE | SWT.TITLE);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite cont = (Composite) super.createDialogArea(parent);

		SashForm sash = new SashForm(cont, SWT.VERTICAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sash.setLayout(new GridLayout(1, false));

		Composite paramCont = new Composite(sash, SWT.BORDER);
		paramCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		paramCont.setLayout(new GridLayout(1, false));

		Label modelNameLbl = new Label(paramCont, SWT.FLAT);
		modelNameLbl.setText("Sample Title:");
		modelNameTxt = new Text(paramCont, SWT.BORDER);
		modelNameTxt.setText("Sample_ "+store.getDoc().getMd().getTitle());
		modelNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label descLbl = new Label(paramCont, SWT.FLAT);
		descLbl.setText("Description:");
		descTxt = new Text(paramCont, SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = 20;
		descTxt.setLayoutData(gd);
		
		nrOfLinesTxt = new LabeledText(paramCont, "Nr. of lines", true);
		nrOfLinesTxt.setText("100");
		nrOfLinesTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true , false ));

		paramTabFolder = new CTabFolder(paramCont, SWT.BORDER | SWT.FLAT);
		paramTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		SamplesMethodUITab tab = createSampelsComputeTab();
		CTabItem selection = tab.getTabItem();
		
		paramTabFolder.setSelection(selection);		
		paramCont.pack();
		
		
		createSampleDocTab(samplesConfComposite, SWT.HORIZONTAL);
		
		cont.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				logger.debug("Disposing SamplesCompareDialog composite.");
				rl.setStopped();
			}
		});
		
		return cont;
	}
	
	private void createSampleDocTab(Composite parent, int style) {
		
		SashForm sampleTreeViewer = new SashForm(parent,style);
		sampleDocMap = new TreeMap<>();
		sampleTreeViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sampleTreeViewer.setLayout(new GridLayout(1, false));
		
		Group treeViewerCont = new Group(sampleTreeViewer, SWT.NONE);
		treeViewerCont.setText("Collection");
		treeViewerCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewerCont.setLayout(new GridLayout(1, false));
		
		
		tv = new TreeViewer(treeViewerCont, SWT.BORDER | SWT.MULTI);
		contentProv = new CollectionContentProvider();
		labelProv = new SampleLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tv.setInput(this.docList);
		
		buttonComp = new Composite(sampleTreeViewer, SWT.NONE);
		buttonComp.setLayout(new GridLayout(1, true));
		
		addToSampleSetBtn = new Button(buttonComp, SWT.PUSH);
		addToSampleSetBtn.setImage(Images.ADD);
		addToSampleSetBtn.setText("Add to Sample Set");
		addToSampleSetBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		Group trainOverviewCont = new Group(sampleTreeViewer, SWT.NONE);
		trainOverviewCont.setText("Overview");
		trainOverviewCont.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		trainOverviewCont.setLayout(new GridLayout(1, false));
		
		GridData tableGd = new GridData(SWT.FILL, SWT.FILL, true, true);
		GridLayout tableGl = new GridLayout(1, true);
		
		Group sampleSetGrp = new Group(trainOverviewCont, SWT.NONE);
		sampleSetGrp.setText("Documents added to Sample Set");
		sampleSetGrp.setLayoutData(tableGd);
		sampleSetGrp.setLayout(tableGl);

		sampleSetOverviewTable = new DocumentDataSetTableWidget(sampleSetGrp, SWT.BORDER);
		sampleSetOverviewTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridData buttonGd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		removeFromSampleSetBtn = new Button(sampleSetGrp, SWT.PUSH);
		removeFromSampleSetBtn.setLayoutData(buttonGd);
		removeFromSampleSetBtn.setImage(Images.CROSS);
		removeFromSampleSetBtn.setText("Remove selected entries from train set");
		
		createSampleButton = new Button(sampleSetGrp, SWT.PUSH);
		createSampleButton.setLayoutData(buttonGd);
		createSampleButton.setImage(Images.DISK);
		createSampleButton.setText("Create Sample");
		
		sampleTreeViewer.setWeights(new int[] {40,20,40});
		addListeners();
		
	}

	private SamplesMethodUITab createSampelsComputeTab() {
		samplesTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		samplesTabItem.setText("Documents");
		
		samplesConfComposite = new Composite(paramTabFolder,0);
		samplesConfComposite.setLayout(new GridLayout(1,false));
		
		samplesTabItem.setControl(samplesConfComposite);
		
		computeSampleTabItem = new CTabItem(paramTabFolder, SWT.NONE);
		computeSampleTabItem.setText("Samples");
		
		SashForm samplesComputesash = new SashForm(paramTabFolder, SWT.HORIZONTAL);
		samplesComputesash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		samplesComputesash.setLayout(new GridLayout(3, false));
		
		tvCompute = new TreeViewer(samplesComputesash, SWT.BORDER | SWT.MULTI);
		contentProvComp = new CollectionContentProvider();
		labelProv = new SampleLabelProvider();
		tvCompute.setContentProvider(contentProvComp);
		tvCompute.setLabelProvider(labelProv);
		tvCompute.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
//		Only display sample documents in tree viewer
		for(TrpDocMetadata document : docList) {
			if(document.isSampleDoc()) {
				sampleDocList.add(document);
			}
		}
		
		tvCompute.setInput(this.sampleDocList);
		
		buttonComputeComp = new Composite(samplesComputesash, SWT.NONE);
		buttonComputeComp.setLayout(new GridLayout(1, true));
		
		labelRef = new Label(buttonComputeComp,SWT.NONE );
		labelRef.setText("Reference : ");
		labelRef.setVisible(false);
		comboRef = new Combo(buttonComputeComp, SWT.DROP_DOWN);
		comboRef.setItems(new String[] {"GT"});
		comboRef.select(0);
		params.addParameter("ref", comboRef.getItem(comboRef.getSelectionIndex()));
		comboRef.setEnabled(false);
		comboRef.setVisible(false);
		labelHyp = new Label(buttonComputeComp,SWT.NONE );
		labelHyp.setText("Select hypothese by toolname : ");
		labelHyp.setVisible(false);
		comboHyp = new Combo(buttonComputeComp, SWT.DROP_DOWN);
		final GridData gd_combo = new GridData(SWT.FILL,SWT.FILL, true, false);
		gd_combo.widthHint = 250;
		comboHyp.setLayoutData(gd_combo);
		comboHyp.setVisible(false);
		
		computeSampleBtn = new Button(buttonComputeComp, SWT.PUSH);
		computeSampleBtn.setImage(Images.ARROW_RIGHT);
		computeSampleBtn.setText("Compute");
		computeSampleBtn.setEnabled(false);
		computeSampleBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		chartText = new Label(buttonComputeComp, SWT.WRAP | SWT.LEFT);
		chartText.setText("Upper bound : \n Lower bound : \n Mean : \n \nWith the probability of 95% the CER for the entire document will be in the interval [.. | .. ] with the mean : .. \n \nBy taking 4 times the number of lines the interval size can be cut in half");
		chartText.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1));
		chartText.setVisible(false);
		cerText = new Label(buttonComputeComp, SWT.WRAP | SWT.LEFT);
		cerText.setText("The CER for the sample pages is [ . . . . %]  ");
		cerText.setLayoutData(new GridData(SWT.HORIZONTAL, SWT.TOP, true, true, 1, 1));
		cerText.setVisible(false);
		
//		Date date = new Date();
//		BoxAndWhiskerXYDataset dataset = createDataset(0,0,0,date);
//		chart = createChart(dataset);
//		jFreeChartComp = new ChartComposite(buttonComputeComp, SWT.FILL);
//		jFreeChartComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		jFreeChartComp.setChart(chart);
		
		jobsComp = new Composite(samplesComputesash,SWT.NONE); 
		jobsComp.setLayout(new GridLayout(1,true));
		
		resultTable = new KwsResultTableWidget(jobsComp, 0);
		resultTable.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
	
		computeSampleTabItem.setControl(samplesComputesash);
		
		rl.start();

		return new SamplesMethodUITab(0, samplesTabItem, samplesConfComposite);
	}
	
	private JFreeChart createChart(BoxAndWhiskerXYDataset dataset) {
		JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
				"Confidence Interval for CER", "Sample", "CER",  dataset, true);
		return chart;

	}
	
	private BoxAndWhiskerXYDataset createDataset(double meanDoub, double minDoub, double maxDoub, Date date) {

		DefaultBoxAndWhiskerXYDataset dataset = new DefaultBoxAndWhiskerXYDataset("Upper and lower bound for CER");
		
		Number mean = meanDoub*100;
		Number median = 0;
		Number q1 = 0;
		Number q3 = 0;
		Number minRegularValue = minDoub*100;
		Number maxRegularValue = maxDoub*100;
		Number minOutlier = 0;
		Number maxOutlier = 0;
		List outliers = null;
		
		BoxAndWhiskerItem item = new BoxAndWhiskerItem(mean, median, q1, q3, minRegularValue, maxRegularValue, minOutlier, maxOutlier, outliers);
		dataset.add(date,item);
		  
		return dataset;

	}	
	
	private void addListeners() {
		
		
		comboHyp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				params.addParameter("hyp", comboHyp.getItem(comboHyp.getSelectionIndex()));
			}
		});
		
		comboRef.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				params.addParameter("ref", comboRef.getItem(comboRef.getSelectionIndex()));
			}
		});
		
		resultTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener(){
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				TrpSampleResultTableEntry entry = (TrpSampleResultTableEntry) resultTable.getSelectedEntry();
	
				if(entry != null && entry.getStatus().equals("Completed") ) {
					try {
						logger.debug(entry.getQuery());
						setCERTextJob(entry.getJob(), entry.getQuery());
						drawChartJob(entry.getJob());
					} catch (ServerErrorException | ClientErrorException
							| IllegalArgumentException e1) {
						e1.printStackTrace();
					} catch (SessionExpiredException e) {
						e.printStackTrace();
					}
					}
				}
			});
		
		computeSampleBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int docId = docMd.getDocId();
				params.addParameter("computeSample", "computeSample");
				params.addIntParam("option", 0);
				String newPageString = null;
				boolean hasGT = false;
				rl.resumePolling();
				if(params.getParameterValue("hyp") != null) {
					try {
						// create new pagestring, take only pages with chosen toolname
						TrpDoc doc = store.getConnection().getTrpDoc(store.getCollId(), store.getDocId(), 10);
						Set<Integer> pageIndices = CoreUtils.parseRangeListStr("1-"+docMd.getNrOfPages(), store.getDoc().getNPages());
						Set<Integer> newPageIndices = new HashSet<Integer>();
						List<TrpTranscriptMetadata> transcripts = new ArrayList<TrpTranscriptMetadata>();
						for (Integer pageIndex : pageIndices) {
							logger.debug("pageIndex : "+pageIndex);
							transcripts = doc.getPages().get(pageIndex).getTranscripts();
							// check if all pages contain GT version
							TrpTranscriptMetadata transGT = doc.getPages().get(pageIndex).getTranscriptWithStatusOrNull(EditStatus.GT);
							if(transGT == null) {
								throw new NullArgumentException("page "+ (pageIndex+1));
							}
							for(TrpTranscriptMetadata transcript : transcripts){
								if(transcript.getToolName() != null) {
									if(transcript.getToolName().equals(comboHyp.getItem(comboHyp.getSelectionIndex()))) {
										newPageIndices.add(pageIndex);
									}
								}
							}	
						}
						newPageString = CoreUtils.getRangeListStrFromSet(newPageIndices);
						String msg = "";
						msg += "Compute confidence interval for page(s) : 1-" + docMd.getNrOfPages() + "\n";
						msg += "Ref: " +params.getParameterValue("ref")+"\n";
						msg += "Hyp: " +params.getParameterValue("hyp");
						if (params.getParameterValue("ref") == null || params.getParameterValue("hyp") == null) {
							DialogUtil.showErrorMessageBox(getShell(), "Hyp or Ref missing", "Please choose a reference and hypothesis!");
						}else {
							int result = DialogUtil.showYesNoDialog(getShell(), "Start?", msg);
							if (result == SWT.YES) {
								try {
									 TrpJobStatus status =  store.computeSampleRate(docId,params);
									 TrpJobStatus statusCER = store.getConnection().computeErrorRateWithJob(docId, "1-"+docMd.getNrOfPages(), params);
									
									if(status != null &&  status.isFinished()) {			
										drawChartFromJobs();
									}
									if(statusCER != null && statusCER.isFinished()) {
										setCERinText();
									}

									DialogUtil.showInfoMessageBox(getShell(), "Compute Interval Job started", "Started compute interval job with id = "+status.getJobId());
									
									
								} catch (TrpServerErrorException | TrpClientErrorException | SessionExpiredException e1) {
									e1.printStackTrace();
								}
							}
						}	
					} catch (IOException | SessionExpiredException | ServerErrorException | ClientErrorException e1) {
						e1.printStackTrace();
					} catch (NullArgumentException e2) {
						DialogUtil.showErrorMessageBox(getShell(), "Missing GT", "GT for " +e2.getLocalizedMessage());
					}
				}
				
			}
		});

		tvCompute.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				computeSampleBtn.setEnabled(true);
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object o = selection.getFirstElement();
				rl.resumePolling();
				if (o instanceof TrpDocMetadata) {
					docMd = (TrpDocMetadata) o;
					labelRef.setVisible(true);
					comboRef.select(0);
					comboRef.setVisible(true);
					labelHyp.setVisible(true);
					comboHyp.setVisible(true);
					comboHyp.deselectAll();
					for(int i = 0; i < comboHyp.getItemCount();i++) {
						comboHyp.remove(i);
					}
					try {
						Object[] pageObjArr = contentProvComp.getChildren(docMd);
						for (Object obj : pageObjArr) {
							TrpPage page = (TrpPage) obj;
							List<TrpTranscriptMetadata> transcripts = page.getTranscripts();
							for(TrpTranscriptMetadata transcript : transcripts){
								if(transcript.getToolName() != null) {
									String[] items = comboHyp.getItems();
									if(!Arrays.stream(items).anyMatch(transcript.getToolName()::equals)) {
										comboHyp.add(transcript.getToolName());
										comboHyp.redraw();
									}
								}
								
							}
							
						}
						drawChartFromJobs();
						setCERinText();
						
					} catch (ServerErrorException | IllegalArgumentException | ClientErrorException | SessionExpiredException e) {
						e.printStackTrace();
					}
					
				}

			}
		});

		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (o instanceof TrpDocMetadata) {
					for (TreeItem i : tv.getTree().getItems()) {
						if (i.getData().equals(o)) {
							tv.setExpandedState(o, !i.getExpanded());
							break;
						}
					}
					updateColors();
				} else if (o instanceof TrpPage) {
					TrpPage p = (TrpPage)o;
					TrpLocation loc = new TrpLocation();
					loc.collId = colId;
					loc.docId = p.getDocId();
					loc.pageNr = p.getPageNr();
					TrpMainWidget.getInstance().showLocation(loc);
				}
			}
		});

		tv.getTree().addListener(SWT.Expand, new Listener() {
			public void handleEvent(Event e) {
				updateColors();
			}
		});

		addToSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
				Iterator<?> it = sel.iterator();
				while (it.hasNext()) {
					Object o = it.next();
					if (o instanceof TrpDocMetadata) {
						TrpDocMetadata docMd = (TrpDocMetadata) o;
						modelNameTxt.setText("Sample_"+docMd.getTitle());
						Object[] pageObjArr = contentProv.getChildren(docMd);
						List<TrpPage> pageList = new LinkedList<>();
						for (Object page : pageObjArr) {
							pageList.add((TrpPage) page);
						}

						sampleDocMap.put(docMd, pageList);

					} else if (o instanceof TrpPage) {
						TrpPage p = (TrpPage) o;
						TrpDocMetadata parent = (TrpDocMetadata) contentProv.getParent(p);
						modelNameTxt.setText("Sample_"+parent.getTitle());
						if (sampleDocMap.containsKey(parent) && !sampleDocMap.get(parent).contains(p)) {
							sampleDocMap.get(parent).add(p);
						} else if (!sampleDocMap.containsKey(parent)) {
							List<TrpPage> pageList = new LinkedList<>();
							pageList.add(p);
							sampleDocMap.put(parent, pageList);
						}

					}
				}
				updateTable(sampleSetOverviewTable, sampleDocMap);
				updateColors();
			}
		});

		removeFromSampleSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<DocumentDataSelectionEntry> entries = sampleSetOverviewTable.getSelectedDataSets();
				if (!entries.isEmpty()) {
					for (DocumentDataSelectionEntry entry : entries) {
						sampleDocMap.remove(entry.getDoc());
					}
					updateTable(sampleSetOverviewTable, sampleDocMap);
					updateColors();
				}
			}
		});
		
		createSampleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String msg = "";
				DataSetMetadata sampleSetMd = getSampleSetMetadata();
				msg += "Sample set size:\n \t\t\t\t" + sampleSetMd.getPages() + " pages\n";
				msg += "\t\t\t\t" + sampleSetMd.getLines() + " lines\n";
				msg += "\t\t\t\t" + sampleSetMd.getWords() + " words\n";
				msg += "Samples Options:\n ";
				msg += "\t\t\t\t" + nrOfLinesTxt.getText()  + " lines\n";
				
				if(sampleSetMd.getLines() < Integer.parseInt(nrOfLinesTxt.getText())) {
					DialogUtil.showErrorMessageBox(getShell(), "Error number of lines", "Choose at most "+sampleSetMd.getLines()+" lines for your sample");
				}else if (modelNameTxt.getText().equals("")) {
					DialogUtil.showErrorMessageBox(getShell(), "Error title of sample", "Please choose a title for the sample");
				}else {
					
					int result = DialogUtil.showYesNoDialog(getShell(), "Start?", msg);
					
					if (result == SWT.YES) {	
						try {
							
							store.createSample(sampleDocMap, Integer.parseInt(nrOfLinesTxt.getText()), modelNameTxt.getText(), descTxt.getText());
							DialogUtil.showInfoMessageBox(getShell(), "Sample Job started", "Started sample job ");
							

						} catch (SessionExpiredException | ServerErrorException | ClientErrorException
								| IllegalArgumentException ex) {
							ex.printStackTrace();
						}
						
					}
				}
			}
		});
	}
	
	private void setCERTextJob(TrpJobStatus job, String query) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException  {
		Integer docId = docMd.getDocId();
		List<TrpJobStatus> jobs = new ArrayList<>();
		jobs = store.getConnection().getJobs(true, null, JobImpl.ErrorRateJob.getLabel(), docId, 0, 0, "jobId", "asc");
		for(TrpJobStatus jobLoop : jobs) {
			if(jobLoop.isFinished()) {
					TrpProperties props = jobLoop.getJobDataProps();
					final String xmlStr = props.getString(JobConst.PROP_RESULT);
					TrpErrorRate res = new TrpErrorRate ();
					ParameterMap paramsErr = res.getParams();
				if(paramsErr.getParameterValue("hyp") != null && query.contains(paramsErr.getParameterValue("hyp"))) {
					if(xmlStr != null) {
						try {
							res = JaxbUtils.unmarshal(xmlStr, TrpErrorRate.class);
							cerText.setText("The CER for the sample pages is "+res.getCer());
							cerText.setVisible(true);
						} catch (JAXBException e) {
							logger.error("Could not unmarshal error cer result from job!");
						}
					}
				}
			}
		}
//		if(job.isFinished()) {
//			TrpProperties props = job.getJobDataProps();
//			final String xmlStr = props.getString(JobConst.PROP_RESULT);
//			TrpErrorRate res = new TrpErrorRate ();
//			logger.debug("Set CER by table entry "+job.getCreateTime());
//			if(xmlStr != null) {
//				try {
//					res = JaxbUtils.unmarshal(xmlStr, TrpErrorRate.class);
//					cerText.setText("The CER for the sample pages is "+res.getCer());
//					cerText.setVisible(true);
//				} catch (JAXBException e) {
//					logger.error("Could not unmarshal error cer result from job!");
//				}
//			}
//		}
	}
	
	private void drawChartJob(TrpJobStatus job) {
		if(job.isFinished()) {
			TrpProperties props = job.getJobDataProps();
			final String xmlStr = props.getString(JobConst.PROP_RESULT);
			TrpComputeSample res = new TrpComputeSample();
			logger.debug("Drawing chart by table entry "+job.getCreateTime());
			if(xmlStr != null) {
				try {
					res = JaxbUtils.unmarshal(xmlStr, TrpComputeSample.class);
					BoxAndWhiskerXYDataset dataset = createDataset(res.getMean(),res.getMinProp(),res.getMaxProp(),job.getCreated());
//					chart = createChart(dataset);
//					jFreeChartComp.setChart(chart);
//					chart.fireChartChanged();
					chartText.setText("Upper bound : "+df.format(res.getMaxProp()*100)  +"% \nLower bound : "+df.format(res.getMinProp()*100) +"% \nMean : "+df.format(res.getMean()*100) +"% \n\nWith the probability of 95% the CER for the entire document will be in the interval ["+df.format(res.getMinProp()*100)  +"%  "+df.format(res.getMaxProp()*100) +"%] with the mean : "+df.format(res.getMean()*100) +"% \n \nBy taking 4 times the number of lines the interval size can be cut in half.");
					chartText.setVisible(true);
					chartText.redraw();
				} catch (JAXBException e) {
					logger.error("Could not unmarshal interval result from job!");
				}
			}
		}
	}
	
	
	private void setCERinText() throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException {
		Integer docId = docMd.getDocId();
		List<TrpJobStatus> jobs = new ArrayList<>();
		jobs = store.getConnection().getJobs(true, null, JobImpl.ErrorRateJob.getLabel(), docId, 0, 0, "jobId", "asc");
		if(jobs == null || jobs.isEmpty()) {
			cerText.setText("The CER for the sample pages is [ . . . . %]  ");
		}else{
			if(jobs.get(0).isFinished()) {
				rl.pause();
			}
			for(TrpJobStatus job : jobs) {
				if(job.isFinished()) {
					TrpProperties props = job.getJobDataProps();
					final String xmlStr = props.getString(JobConst.PROP_RESULT);
					TrpErrorRate res = new TrpErrorRate ();
					if(xmlStr != null) {
						try {
							res = JaxbUtils.unmarshal(xmlStr, TrpErrorRate.class);
							cerText.setText("The CER for the sample pages is "+res.getCer());
							cerText.setVisible(true);
						} catch (JAXBException e) {
							logger.error("Could not unmarshal error cer result from job!");
						}
					}
				}
				
			}
		}
		
	}
	
	private void drawChartFromJobs() throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException{
		
		Integer docId = docMd.getDocId();
		List<TrpJobStatus> jobs = new ArrayList<>();
		jobs = store.getConnection().getJobs(true, null, JobImpl.ComputeSampleJob.getLabel(), docId, 0, 0, "jobId", "asc");
		if(jobs == null || jobs.isEmpty()) {
			chartText.setText("Upper bound : \n Lower bound : \n Mean : \n\nWith the probability of 95% the CER for the entire document will be in the interval [.. | .. ] with the mean : .. \n \nBy taking 4 times the number of lines the interval size can be cut in half");
//			Date date = new Date();
//			BoxAndWhiskerXYDataset dataset = createDataset(0,0,0,date);
//			chart = createChart(dataset);
//			jFreeChartComp.setChart(chart);
//			chart.fireChartChanged();
		}else {
			for(TrpJobStatus job : jobs) {
				if(job.isFinished()) {
					TrpProperties props = job.getJobDataProps();
					final String xmlStr = props.getString(JobConst.PROP_RESULT);
					TrpComputeSample res = new TrpComputeSample();
					if(xmlStr != null) {
						try {
							res = JaxbUtils.unmarshal(xmlStr, TrpComputeSample.class);
//							BoxAndWhiskerXYDataset dataset = createDataset(res.getMean(),res.getMinProp(),res.getMaxProp(),job.getCreated());
//							chart = createChart(dataset);
//							jFreeChartComp.setChart(chart);
//							chart.fireChartChanged();
							chartText.setText("Upper bound : "+df.format(res.getMaxProp()*100)  +"% \nLower bound : "+df.format(res.getMinProp()*100) +"% \nMean : "+df.format(res.getMean()*100) +"% \n\nWith the probability of 95% the CER for the entire document will be in the interval ["+df.format(res.getMinProp()*100)  +"%  "+df.format(res.getMaxProp()*100) +"%] with the mean : "+df.format(res.getMean()*100) +"% \n \nBy taking 4 times the number of lines the interval size can be cut in half.");
							chartText.setVisible(true);
							chartText.redraw();
						} catch (JAXBException e) {
							logger.error("Could not unmarshal interval result from job!");
						}

					}
				}
			}
		}
		
		
	}
	
	private void updateColors() {
		List<TrpPage> trainPages;
		for (TreeItem i : tv.getTree().getItems()) {
			TrpDocMetadata doc = (TrpDocMetadata) i.getData();

			// default color set
			Color fgColor = BLACK;
			Color bgColor = WHITE;

			if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				bgColor = CYAN;
			} else if (sampleDocMap.containsKey(doc)) {
				fgColor = WHITE;
				if (doc.getNrOfPages() == sampleDocMap.get(doc).size()) {
					bgColor = BLUE;
				} else {
					bgColor = LIGHT_BLUE;
				}
			} 
			i.setBackground(bgColor);
			i.setForeground(fgColor);

			trainPages = sampleDocMap.containsKey(doc) ? sampleDocMap.get(doc) : new ArrayList<>(0);

			for (TreeItem child : i.getItems()) {
				TrpPage page = (TrpPage) child.getData();
				if (trainPages.contains(page)) {
					child.setBackground(BLUE);
					child.setForeground(WHITE);
				}else {
					child.setBackground(WHITE);
					child.setForeground(BLACK);
				}
			}
		}
	}
	
	private void updateTable(DocumentDataSetTableWidget t, Map<TrpDocMetadata, List<TrpPage>> map) {
		List<DocumentDataSelectionEntry> list = new ArrayList<>(map.entrySet().size());
		for (Entry<TrpDocMetadata, List<TrpPage>> entry : map.entrySet()) {
			TrpDocMetadata doc = entry.getKey();

			List<TrpPage> pageList = entry.getValue();

			list.add(new DocumentDataSelectionEntry(doc, pageList));
		}
		Collections.sort(list);
		t.setInput(list);
	}
	
	public DataSetMetadata getSampleSetMetadata() {
		return computeDataSetSize(getTrainDocMap());
	}
	
	public Map<TrpDocMetadata, List<TrpPage>> getTrainDocMap() {
		return sampleDocMap;
	}
	
	private DataSetMetadata computeDataSetSize(Map<TrpDocMetadata, List<TrpPage>> map) {
		int pages = 0;
		int lines = 0;
		int words = 0;
		for (Entry<TrpDocMetadata, List<TrpPage>> e : map.entrySet()) {
			for (TrpPage p : e.getValue()) {
				TrpTranscriptMetadata tmd = p.getCurrentTranscript();
					for (TrpTranscriptMetadata t : p.getTranscripts()) {
							tmd = t;
							break;
					}
					pages++;
					lines += tmd.getNrOfLines();
					words += tmd.getNrOfWordsInLines();
				}
				
			
		}
		return new DataSetMetadata(pages, lines, words);
	}
	
	private void updateResultTable(List<TrpJobStatus> jobs) {
		List<TrpSampleResultTableEntry> errorList = new LinkedList<>();

		for(TrpJobStatus j : jobs) {
			errorList.add(new TrpSampleResultTableEntry(j));
		}
		
		Display.getDefault().asyncExec(() -> {	
			if(resultTable != null && !resultTable.isDisposed()) {
				logger.debug("Updating Error result table");
				resultTable.getTableViewer().setInput(errorList);
			}
		});
	}
	
	private class ResultLoader extends Thread{
		private final static int SLEEP = 2000;
		private boolean stopped = false;
		private final AtomicBoolean pauseFlag = new AtomicBoolean(false);
		@Override
		public void run() {
			while(!stopped) {
				while (!Thread.currentThread().isInterrupted()) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							List<TrpJobStatus> jobs;
							try {
								jobs = getSampleComputeJobs();
								updateResultTable(jobs);
							} catch (ServerErrorException | ClientErrorException
									| IllegalArgumentException e) {
								e.printStackTrace();
							}
						}
						
					});
					
					 if (pauseFlag.get()) {
					       synchronized (pauseFlag) {   	  
					          while (pauseFlag.get()) {
					             try {	 
					                pauseFlag.wait();
					             } catch (InterruptedException e) {
					                Thread.currentThread().interrupt();
					                return;
					             }
					          }
					       }
					    }
				}
			}
		}
		
		public void pause() {
			   pauseFlag.set(true);
		}
		
		public void resumePolling() {
			   pauseFlag.set(false);
			   synchronized (pauseFlag) {
			       pauseFlag.notify();
			   }
		}
		
		private List<TrpJobStatus> getSampleComputeJobs(){
			Integer docId = docMd.getDocId();
			List<TrpJobStatus> jobs = new ArrayList<>();
			try {
				jobs = store.getConnection().getJobs(true, null, JobImpl.ComputeSampleJob.getLabel(), docId, 0, 0, null, null);
			} catch (SessionExpiredException | ServerErrorException | ClientErrorException
					| IllegalArgumentException e) {
				e.printStackTrace();
			}
			return jobs;
		}
		public void setStopped() {
			logger.debug("Stopping result polling.");
			stopped = true;
		}
		
	}
	
	private class SamplesMethodUITab {
		final int tabIndex;
		final CTabItem tabItem;
		final Composite configComposite;
		private SamplesMethodUITab(int tabIndex, CTabItem tabItem, Composite configComposite) {
			this.tabIndex = tabIndex;
			this.tabItem = tabItem;
			this.configComposite = configComposite;
		}
		
		public CTabItem getTabItem() {
			return tabItem;
		}
		
	}

}
