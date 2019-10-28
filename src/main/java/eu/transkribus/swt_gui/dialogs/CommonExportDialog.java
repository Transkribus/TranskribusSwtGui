package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang.StringUtils;
import org.dea.fimgstoreclient.beans.ImgType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.io.ExportFilePatternUtils;
import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.builder.CommonExportPars;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.alto.AltoExportPars;
import eu.transkribus.core.model.builder.docx.DocxExportPars;
import eu.transkribus.core.model.builder.pdf.PdfExportPars;
import eu.transkribus.core.model.builder.tei.TeiExportPars;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.CurrentDocPagesSelector;
import eu.transkribus.swt_gui.util.TagsSelector;

public class CommonExportDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(CommonExportDialog.class);
	
	Shell shell;
	ExportPathComposite exportPathComp;

	String lastExportFolder;
	String docName;
	List<TrpPage> pages = null;
	File result=null;
	Button wordBasedBtn;
	Button exportTagsBtn;
	Button blackeningBtn;
	Button createTitlePageBtn;
	boolean wordBased=false;
	boolean docxTagExport=false;
	boolean doBlackening = false;
	boolean createTitlePage = false;
	
	Button preserveLinebreaksBtn;
	boolean preserveLinebreaks = false;
	Button forcePagebreaksBtn;
	boolean forcePagebreaks = false;
	Button markUnclearWordsBtn;
	boolean markUnclearWords = false;
	Button keepAbbrevBtn;
	boolean keepAbbreviations = false;
	Button expandAbbrevBtn;
	boolean expandAbbreviations = false;
	Button substituteAbbrevBtn;
	boolean substituteAbbreviations = false;
	
	Button showSuppliedWithBracketsBtn;
	boolean showSuppliedWithBrackets = false;
	Button ignoreSuppliedBtn;
	boolean ignoreSupplied = false;
	
//	Button serverExportBtn;
	boolean doServerExport = false;
	boolean exportCurrentDocOnServer = true;
	
	Button noZonesRadio;
//	CheckBoxGroup zonesGroup;
	Button zonePerParRadio;
	Button zonePerLineRadio;
	Button zonePerWordRadio;
	Button zonesCoordsAsBoundingBoxChck;
	Button pbImageNameXmlIdChck;
	
	Button btnTei;
	
	Button lineTagsRadio, lineBreaksRadio;
	
	CommonExportPars commonPars;
	TeiExportPars teiPars;
	AltoExportPars altoPars;
	PdfExportPars pdfPars;
	DocxExportPars docxPars;

	boolean docxExport, pdfExport, teiExport, altoExport, splitUpWords, imgExport, metsExport, 
	pageExport, tagXlsxExport, tagIOBExport, tableXlsxExport, zipExport, txtExport;

//	String fileNamePattern = ExportFilePatternUtils.FILENAME_PATTERN;
	
	Button addExtraTextPagesBtn;
	boolean addExtraTextPages2PDF;
	Button imagesOnlyBtn;
	boolean exportImagesOnly;
	Button imagesPlusTextBtn;
	boolean exportImagesPlusText;
	Button highlightTagsBtn;
	boolean highlightTags;
	Button highlightArticlesBtn;
	boolean highlightArticles;
	CTabFolder tabFolder;
	CTabItem tabItemMets;
	CTabItem tabItemPDF;
	CTabItem tabItemTEI;
	CTabItem tabItemDOCX;
	
	CTabFolder exportTypeTabFolder;
	CTabItem clientExportItem;
	CTabItem serverExportItem;
	
	Button docsSelectorBtn, currentDocRadio, multipleDocsRadio;
	Label serverExportLabel;
	StyledText exportHistoryText;
	Button reloadExportHistoryBtn;
	
	Composite metsComposite;
	Composite pdfComposite;
	Composite teiComposite;
	Composite docxComposite;
	
	FilenamePatternComposite filenamePatternComp;
	
	Set<String> selectedTagsList;
//	DocPagesSelector docPagesSelector;
	CurrentDocPagesSelector docPagesSelector;
	
	TagsSelector tagsSelector;
	
	Set<Integer> pageIndices = null; 
	String pagesStr = null;
	
	Combo imgQualityCmb = null;
	Combo fontDropDown = null;
	String font = "";
	/*
	 * TODO add image quality choice to PDF export too
	 * Combo pdfImgQualityCmb = null;
	 */
	Combo pdfImgQualityCmb = null;
	
	//only add values here that can be resolved to ImgTypeLabels enum!
	final String[] imgQualityChoices = { 
			ImgTypeLabels.Original.toString(),
			ImgTypeLabels.JPEG.toString()
	};
	
	List<DocumentSelectionDescriptor> documentsToExportOnServer = null;
	
//	Button currentPageBtn;

	String versionStatus;
	
	public CommonExportDialog(Shell parent, int style, String lastExportFolder, String docName, List<TrpPage> pages) {
		super(parent, style |= SWT.DIALOG_TRIM | SWT.RESIZE);
		this.lastExportFolder = lastExportFolder;
		this.docName = docName;
		this.pages = pages;
		Set<String> regTagNames = CustomTagFactory.getRegisteredTagNames();
		Set<String> usedTagNames = ExportUtils.getOnlyWantedTagnames(regTagNames);
		setSelectedTagsList(usedTagNames);
	}
	
	private void createServerExportContent() {
	    Composite serverExportComposite = new Composite(exportTypeTabFolder, 0);
	    serverExportComposite.setLayout(new GridLayout(1, false));
	    
	    serverExportLabel = new Label(serverExportComposite, 0);
	    serverExportLabel.setFont(Fonts.createBoldFont(serverExportLabel.getFont()));
	    serverExportLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    	    
	    Composite docsToExportComp = new Composite(serverExportComposite, 0);
	    docsToExportComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    docsToExportComp.setLayout(new FillLayout());
	    
	    SelectionAdapter serverExportSelListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportCurrentDocOnServer = currentDocRadio.getSelection();
				docsSelectorBtn.setEnabled(!exportCurrentDocOnServer);
				updateServerExportLabel();
				
				docPagesSelector.setEnabled(!isDoServerExport() || exportCurrentDocOnServer);
			}
		};
	    
	    currentDocRadio = new Button(docsToExportComp, SWT.RADIO);
	    currentDocRadio.setText("Current document");
	    currentDocRadio.setSelection(true);
	    currentDocRadio.addSelectionListener(serverExportSelListener);
	    
	    multipleDocsRadio = new Button(docsToExportComp, SWT.RADIO);
	    multipleDocsRadio.setText("Current collection");
	    multipleDocsRadio.addSelectionListener(serverExportSelListener);
	    
	    docsSelectorBtn = new Button(docsToExportComp, SWT.PUSH);
	    docsSelectorBtn.setText("Choose documents to export...");
	    docsSelectorBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DocumentsSelectorDialog dsd = new DocumentsSelectorDialog(shell, "Select documents to export", Storage.getInstance().getDocList());
				if (dsd.open() == IDialogConstants.OK_ID) {
					documentsToExportOnServer = dsd.getCheckedDocumentDescriptors();
					System.out.println("n selected documents: "+dsd.getCheckedDocs().size());
				}
				
				updateServerExportLabel();
			}
		});
	    docsSelectorBtn.setEnabled(false);
	    
	    Composite exportHistoryHeader = new Composite(serverExportComposite, 0);
	    exportHistoryHeader.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    exportHistoryHeader.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
	    
	    Label exportHistoryLabel = new Label(exportHistoryHeader, 0);
	    exportHistoryLabel.setText("Finished server exports (not older than 2 weeks)");
	    exportHistoryLabel.setFont(Fonts.createBoldFont(exportHistoryLabel.getFont()));
//	    exportHistoryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    
	    reloadExportHistoryBtn = new Button(exportHistoryHeader, SWT.PUSH);
	    reloadExportHistoryBtn.setImage(Images.REFRESH);
	    reloadExportHistoryBtn.setToolTipText("Refresh export history");
	    SWTUtil.onSelectionEvent(reloadExportHistoryBtn, (e) -> { updateExportHistory(); });
	    	    
	    exportHistoryText = new StyledText(serverExportComposite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
	    exportHistoryText.setLayoutData(new GridData(GridData.FILL_BOTH));
	    updateExportHistory();
	    
	    serverExportItem.setControl(serverExportComposite);
	    updateServerExportLabel();
	}
	
	private void onDocLoad() {
		doServerExport = !Storage.getInstance().isLocalDoc();
	    exportTypeTabFolder.setSelection(doServerExport ? serverExportItem : clientExportItem);
	    setDoServerExport(doServerExport);
	    updateServerExportLabel();
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
		
		shell.setSize(1000, 800);
		shell.setText("Export document");
//		shell.setLayout(new GridLayout(1, false));
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		
		SashForm sf = new SashForm(shell, SWT.VERTICAL);
		sf.setLayout(new GridLayout(1, false));		
		
//	    ScrolledComposite sc = new ScrolledComposite(sf, SWT.H_SCROLL
//		        | SWT.V_SCROLL);
	    
		SashForm sf1 = new SashForm(sf, SWT.VERTICAL);
	    sf1.setLayout(new GridLayout(1, false));
	    
//	    Composite mainComp = new Composite(sc, SWT.NONE);
//	    mainComp.setLayout(new GridLayout(1,false));
	    	    
	    exportTypeTabFolder = new CTabFolder(sf1, SWT.NONE);
	    exportTypeTabFolder.setLayout(new GridLayout(1,false));
	    exportTypeTabFolder.setSelectionBackground(new Color[]{shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT), shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND)}, new int[]{100}, true);
	    exportTypeTabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (Storage.getInstance().isLocalDoc()) { // prevent switching to server-type export for local docs
					exportTypeTabFolder.setSelection(clientExportItem);
					e.doit = false;
					return;
				}
				
				boolean isServerTabSelected = exportTypeTabFolder.getSelection() == serverExportItem;
				//logger.debug("setting server type export: "+isServerTabSelected);
				setDoServerExport(isServerTabSelected);

//				if (!isDoServerExport() && btnTei.getSelection()){
//					btnTei.setSelection(false); 
//				}
				//btnTei.setEnabled(isDoServerExport());
				tabItemTEI.setControl(getTabThreeControl(tabFolder));
				recursiveSetEnabled(teiComposite, isTeiExport());
			}
		});
	    
	    IStorageListener storageListener = new IStorageListener() {
			public void handleDocLoadEvent(DocLoadEvent dle) {
				onDocLoad();
			}
		};
		Storage.getInstance().addListener(storageListener);
		shell.addDisposeListener(e -> {
			if (Storage.getInstance()!=null) {
				Storage.getInstance().removeListener(storageListener);	
			}
		});
	    
	    serverExportItem = new CTabItem(exportTypeTabFolder, SWT.FILL);
	    serverExportItem.setText("Server export");
	    
	    clientExportItem = new CTabItem(exportTypeTabFolder, SWT.FILL);
	    clientExportItem.setText("Client export");
		exportPathComp = new ExportPathComposite(exportTypeTabFolder, lastExportFolder, "File/Folder name: ", null, docName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		clientExportItem.setControl(exportPathComp);
	    
	    createServerExportContent();
	    onDocLoad();
	    
//	    CTabFolder mainComp = new CTabFolder(sc, SWT.NONE);
//	    mainComp.setLayout(new GridLayout(1,false));

		SashForm choiceComposite = new SashForm(sf1, SWT.HORIZONTAL);
//		Composite choiceComposite = new Composite(mainComp, SWT.NONE);
		choiceComposite.setLayout(new GridLayout(2, false));
		choiceComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		
	    // Create the first Group
	    Group group1 = new Group(choiceComposite, SWT.SHADOW_IN);
	    group1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
	    group1.setText("Choose export formats");
	    group1.setLayout(new GridLayout(1, false));
	    group1.setFont(Fonts.createBoldFont(group1.getFont()));
	    
	    final Button b0 = new Button(group1, SWT.CHECK);
	    b0.setText("Transkribus Document");
	    final Button b1 = new Button(group1, SWT.CHECK); 
	    b1.setText("PDF");
	    btnTei = new Button(group1, SWT.CHECK);
	    btnTei.setText("TEI");
	    //btnTei.setText("TEI (restricted to server export)");
	    //btnTei.setEnabled(isDoServerExport());
	    final Button b3 = new Button(group1, SWT.CHECK);
	    b3.setText("DOCX");
	    final Button b30 = new Button(group1, SWT.CHECK);
	    b30.setText("Simple TXT");
	    final Button b4 = new Button(group1, SWT.CHECK);
	    b4.setText("Tag Export (Excel)");
	    final Button b40 = new Button(group1, SWT.CHECK);
	    b40.setText("Tag Export (IOB)");
	    final Button b41 = new Button(group1, SWT.CHECK);
	    b41.setText("Table Export into Excel");
	    // Create a horizontal separator
	    Label separator = new Label(group1, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    //----------
	    Button b5 = new Button(group1, SWT.CHECK);
	    b5.setText("Export ALL formats");
	    // Create a horizontal separator
	    Label separator2 = new Label(group1, SWT.HORIZONTAL | SWT.SEPARATOR);
	    separator2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    //----------
	    Button b6 = new Button(group1, SWT.CHECK);
	    b6.setText("Export Selected as ZIP");  
	    
	    Group optionsGroup = new Group(choiceComposite, SWT.NONE);
	    optionsGroup.setText("Export options:");
	    optionsGroup.setLayout(new GridLayout(2, false));
	    optionsGroup.setFont(Fonts.createBoldFont(group1.getFont()));
	    GridData gridData = new GridData();
	    //gridData.heightHint = 0;
	    gridData.horizontalAlignment = SWT.RIGHT;
	    gridData.verticalAlignment = SWT.FILL;
	    gridData.verticalSpan = 2;
	    optionsGroup.setLayoutData(gridData);
	    
	    createTabFolders(optionsGroup);
	    
	    Composite otherOptionsComp = new Composite(optionsGroup, SWT.NONE);
	    otherOptionsComp.setLayout(new GridLayout(1, false));
	    otherOptionsComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    createChooseVersionGroup(otherOptionsComp);	    
	    
		wordBasedBtn = new Button(otherOptionsComp, SWT.CHECK);
		wordBasedBtn.setText("Word based");
		wordBasedBtn.setToolTipText("If checked, text from word based segmentation will be exported");
		wordBasedBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		wordBasedBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				wordBased = wordBasedBtn.getSelection();
			}
		});
		
		blackeningBtn = new Button(otherOptionsComp, SWT.CHECK);
		blackeningBtn.setText("Do blackening");
		blackeningBtn.setToolTipText("If checked, the blacked tagged words get considered");
		blackeningBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		blackeningBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				doBlackening = blackeningBtn.getSelection();
			}
		});
		
		createTitlePageBtn = new Button(otherOptionsComp, SWT.CHECK);
		createTitlePageBtn.setText("Create Title Page");
		createTitlePageBtn.setToolTipText("Title page contains document metadata like author, title,... and editorial declaration");
		createTitlePageBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		createTitlePageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				createTitlePage = createTitlePageBtn.getSelection();
			}
		});
	    
	    docPagesSelector = new CurrentDocPagesSelector(otherOptionsComp, SWT.NONE, true, true, true);
	    docPagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
	    docPagesSelector.setVisible(false);
	    
//	    currentPageBtn = new Button(otherOptionsComp, SWT.PUSH);
//	    currentPageBtn.setText("Export Current Page");
//	    currentPageBtn.setToolTipText("Press this button if you want to export the current page only");
//	    currentPageBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				String currentPageNr = Integer.toString(Storage.getInstance().getPageIndex()+1);
//				docPagesSelector.getPagesText().setText(currentPageNr);				
//			}
//		});
	    
		tagsSelector = new TagsSelector(otherOptionsComp, SWT.FILL, getSelectedTagsList());
		tagsSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tagsSelector.setVisible(false);
		
//		serverExportBtn = new Button(otherOptionsComp, SWT.CHECK);
//		serverExportBtn.setText("Do server export");
//		serverExportBtn.setToolTipText("If checked, the export is started on the server and can be downloaded after export is finished");
//		serverExportBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
//		
//		serverExportBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//	            Button btn = (Button) e.getSource();
//            	setDoServerExport(btn.getSelection());
//			}
//		});
		
		choiceComposite.setWeights(new int[]{25, 75});

	    b0.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	//checkExport.setVisible(btn.getSelection());
	            metsComposite.setEnabled(btn.getSelection());
            	setMetsExport(btn.getSelection());
            	showPageChoice();
            	if (btn.getSelection()){
            		recursiveSetEnabled(metsComposite, true);
            		showTab(0);
            	}
	            else{
	            	recursiveSetEnabled(metsComposite, false);
	            	//metsComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
	            }
	        }
	    });
	    
	    b1.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            setPdfExport(btn.getSelection());
	            //addExtraTextPagesBtn.setVisible(btn.getSelection());
	            pdfComposite.setEnabled(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	            if (btn.getSelection()){
	            	recursiveSetEnabled(pdfComposite, true);
	            	showTab(1);
	            }
	            else{
	            	recursiveSetEnabled(pdfComposite, false);
	            	//pdfComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
	            }

	        }
	    });
	    
	    btnTei.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setTeiExport(btn.getSelection());
            	//modeComposite.setVisible(btn.getSelection());
            	//teiComposite.setVisible(btn.getSelection());
            	teiComposite.setEnabled(btn.getSelection());
            	showPageChoice();
            	showTagChoice();
	            shell.layout();
	            if (btn.getSelection()){
	            	recursiveSetEnabled(teiComposite, true);
	            	showTab(2);
	            }
	            else{
	            	recursiveSetEnabled(teiComposite, false);
	            	//teiComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
	            }

	        }
	    });
	
	    b3.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	//wordBasedBtn.setVisible(btn.getSelection());
	            docxComposite.setEnabled(btn.getSelection());
            	setDocxExport(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	            if (btn.getSelection()){
	            	recursiveSetEnabled(docxComposite, true);
	            	//rtfComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
	            	showTab(3);
	            }
	            else{
	            	recursiveSetEnabled(docxComposite, false);
	            	//rtfComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
	            }

	        }
	    });
	    
	    b30.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setTxtExport(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	        }
	    });
	    
	    b4.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setTagXlsxExport(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	            
	        }
	    });
	    
	    b40.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            setTagIOBExport(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	            
	        }
	    });
	    
	    b41.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setTableXlsxExport(btn.getSelection());
	            showPageChoice();
	            showTagChoice();
	            shell.layout();
	            
	        }
	    });

	    b5.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            b0.setSelection(btn.getSelection());
	            b0.notifyListeners(SWT.Selection, new Event());
	            b1.setSelection(btn.getSelection());
	            b1.notifyListeners(SWT.Selection, new Event());
	            btnTei.setSelection(btn.getSelection());
	            btnTei.notifyListeners(SWT.Selection, new Event());
	            b3.setSelection(btn.getSelection());
	            b3.notifyListeners(SWT.Selection, new Event());
	            b30.setSelection(btn.getSelection());
	            b30.notifyListeners(SWT.Selection, new Event());
	            b4.setSelection(btn.getSelection());
	            b4.notifyListeners(SWT.Selection, new Event());
	            b40.setSelection(btn.getSelection());
	            b40.notifyListeners(SWT.Selection, new Event());
	            b41.setSelection(btn.getSelection());
	            b41.notifyListeners(SWT.Selection, new Event());

	        }
	    });
	    
	    b6.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setZipExport(btn.getSelection());
	            shell.layout();
	            
	        }
	    });
	    
//	    sc.setContent(mainComp);
//	    sc.setMinSize(600, 200);
	    
//	    sc.setExpandHorizontal(true);
//	    sc.setExpandVertical(true);
	    
	    sf1.setWeights(new int[] {30, 70});
	    
//	    Composite fixedButtons = new Composite(shell,SWT.NONE);
//	    fixedButtons.setLayout(new GridLayout(1,false));
//	    fixedButtons.setSize(300,200);
	    
		Composite buttonComposite = new Composite(sf, SWT.NONE);
		buttonComposite.setLayout(new FillLayout());
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		GridDataFactory.fillDefaults().grab(true, false).span(SWT.FILL, SWT.DEFAULT).hint(SWT.DEFAULT, 200).applyTo(buttonComposite);
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				
				updateParameters();
				
				if (!isMetsExport() && !isPdfExport() && !isDocxExport() && !isTxtExport() && !isTeiExport() && !isAltoExport() && !isTagXlsxExport()&& !isTableXlsxExport() && !isTagIOBExport()){
					DialogUtil.showErrorMessageBox(shell, "Missing export format", "Please choose an export format to continue");
					return;
				}

				result = exportPathComp.getExportFile();
				
				boolean canWrite = result!=null && result.getParentFile()!=null && result.getParentFile().canWrite();
				
				if (!canWrite) {
					DialogUtil.showErrorMessageBox(shell, "Cannot write to folder", "Cannot write into the specified folder - do you have write access?\n\n"+result.getAbsolutePath());
					return;
				}		
				
				shell.close();
			}
		});
//		saveButton.setToolTipText("Stores the configuration in the configuration file and closes the dialog");
//		
		Button closeButton = new Button(buttonComposite, SWT.PUSH);
		closeButton.setText("Cancel");
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				result = null;
				shell.close();
			}
		});
//		closeButton.setToolTipText("Closes this dialog without saving");
		
	    b0.setSelection(true);
	    b0.notifyListeners(SWT.Selection, new Event());
		
	    sf.setWeights(new int[] { 90, 10 });

//		shell.pack();
		shell.layout();
		
		// save values when shell is disposed:
//		shell.addDisposeListener(new DisposeListener() {
//			@Override public void widgetDisposed(DisposeEvent e) {
//				updateSelectedPages();
//			}
//		});
	}
	
	private void updateExportHistory() {
		new Thread() {
			public void run() {
				logger.debug("refreshing export history...");
				Storage store = Storage.getInstance();
				
				final String txt;
				final List<StyleRange> styleRanges = new ArrayList<>();
				
				if (store != null && store.isLoggedIn()) {
					String txtTmp = "";
					try {
						List<TrpJobStatus> jobs = store.getConnection().getJobs(false, TrpJobStatus.FINISHED, "Export Document", null, 0, 0, null, null);
						logger.debug("got finished export jobs: "+jobs.size());
						
//						int i=0;
						for (TrpJobStatus job : jobs) {
							if (job.getEnded() == null)
								continue;
							
							long tdiff = System.currentTimeMillis() - job.getEnded().getTime();
							if (tdiff > 1000*60*60*24*14) { // only list jobs finished in the last two weeks (else: download link expired!)
								continue;
							}
							txtTmp += "id: "+job.getJobId()+", finished: "+job.getEndTimeFormatted()+", link: ";
							
							// add link
							int start = txtTmp.length();
							txtTmp += job.getResult()+"\n";
							StyleRange sr = new StyleRange();
							sr.underlineStyle = SWT.UNDERLINE_LINK; // FIXME: does not work...
							sr.data = job.getResult();
							sr.start = start;
							sr.length = StringUtils.length(job.getResult());
							sr.fontStyle = SWT.BOLD;
							styleRanges.add(sr);
						}
					} catch (SessionExpiredException | ServerErrorException | ClientErrorException
							| IllegalArgumentException e) {
						logger.error(e.getMessage(), e);
						txtTmp = "Error retrieving jobs: "+e.getMessage();
					} finally {
						txt = txtTmp;
					}
				} else {
					txt = "";
				}
				
				Display.getDefault().asyncExec(() -> {
					//check if the dialog has been closed before accessing exportHistoryText
					if(exportHistoryText != null && !exportHistoryText.isDisposed()) {
						exportHistoryText.setText(txt);
						exportHistoryText.setStyleRanges(styleRanges.toArray(new StyleRange[0]));
					}
				});
			}
		}.start();
	}
	
	private void updateServerExportLabel() {
		String txt;
		if (currentDocRadio.getSelection()) {
			TrpDoc d = Storage.getInstance().getDoc();
			if (d == null) {
				txt = "No document found!";
			} else {
				txt = d.getMd().getTitle()+" ("+d.getId()+")";
			}
		} else {
			TrpCollection c = Storage.getInstance().getCurrentDocumentCollection();
			if (c == null) {
				txt = "No collection found!";
			} else {
				int total = Storage.getInstance().getDocList().size();
				int nToExport = documentsToExportOnServer == null ? total : documentsToExportOnServer.size();
				txt = "Exporting "+nToExport+"/"+total+" documents from collection "+c.getColName()+" ("+c.getColId()+")";
			}
		}
		
		serverExportLabel.setText(txt);
	}
	
	private void createChooseVersionGroup(Composite parent) {
	    // Create the first Group
	    Group group2 = new Group(parent, SWT.SHADOW_IN);
	    group2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
	    group2.setText("Version status");
	    group2.setLayout(new GridLayout(1, false));
	    
	    final Combo statusCombo = new Combo(group2, SWT.DROP_DOWN | SWT.READ_ONLY);

	    int size = EnumUtils.stringsArray(EditStatus.class).length + 2;
	    
	    //String items[] = new String[size];
	    //String[] items = statusCombo.getItems();
	    
	    statusCombo.add("Latest version",0);
	    setVersionStatus(statusCombo.getItem(0));
	    statusCombo.add("Loaded version (for current page)",1);
	    int a = 2;

	    for (String s : EnumUtils.stringsArray(EditStatus.class)){
	    	statusCombo.add(s,a++);
	    	//logger.debug("editStatus " + s);
	    }
	    
	    //also select versions by toolname
		//List<TrpPage> pages = Storage.getInstance().getDoc().getPages();
		for(TrpPage page : pages) {
			//logger.debug("get transcript of page " + page.getPageId());
			List<TrpTranscriptMetadata> transcripts = page.getTranscripts();
			//logger.debug("nr of transcripts "+ transcripts.size());
			for(TrpTranscriptMetadata transcript : transcripts){
				//logger.debug("get transcript  " + transcript.getToolName());
				if(transcript.getToolName() != null) {
					String[] items = statusCombo.getItems();
					if(!Arrays.stream(items).anyMatch(transcript.getToolName()::equals)) {
						statusCombo.add(transcript.getToolName(),a++);
					}
				}
			}
		}
	    
		//statusCombo.setItems(items);
		statusCombo.select(0);
		
		statusCombo.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	        	setVersionStatus(statusCombo.getText());
	        }
		});
	}
	
	public void recursiveSetEnabled(Control ctrl, boolean enabled) {
		   if (ctrl instanceof Composite) {
		      Composite comp = (Composite) ctrl;
		      for (Control c : comp.getChildren())
		         recursiveSetEnabled(c, enabled);
		   } else {
		      ctrl.setEnabled(enabled);
		   }
	}
	
	  /**
	   * Creates the contents
	   * 
	   * @param shell the parent shell
	   */
	  private void createTabFolders(Composite tabComposite) {
	    // Create the containing tab folder
		tabFolder = new CTabFolder(tabComposite, SWT.NONE);
		tabFolder.setLayout(new FillLayout());
		//tabFolder.setSimple(false);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

	    // Create each tab and set its text, tool tip text,
	    // image, and control
	    tabItemMets = new CTabItem(tabFolder, SWT.FILL);
	    tabItemMets.setText("Mets");
	    tabItemMets.setToolTipText("Set export options for METS export");
	    tabItemMets.setControl(getTabOneControl(tabFolder));

	    tabItemPDF = new CTabItem(tabFolder, SWT.FILL);
	    tabItemPDF.setText("PDF");
	    tabItemPDF.setToolTipText("Set export options for PDF export");
	    tabItemPDF.setControl(getTabTwoControl(tabFolder));
	    
	    tabItemTEI = new CTabItem(tabFolder, SWT.FILL);
	    tabItemTEI.setText("TEI");
	    tabItemTEI.setToolTipText("Set export options for TEI export");
	    tabItemTEI.setControl(getTabThreeControl(tabFolder));

	    tabItemDOCX = new CTabItem(tabFolder, SWT.FILL);
	    tabItemDOCX.setText("DOCX");
	    tabItemDOCX.setToolTipText("Set export options for DOCX export");
	    tabItemDOCX.setControl(getTabFourControl(tabFolder));
	    
	    tabFolder.setSelectionBackground(new Color[]{shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT), shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND)}, new int[]{100}, true);
	    tabFolder.pack();
	    
	    if (!isMetsExport()){
	    	recursiveSetEnabled(tabItemMets.getControl(), false);
	    }
	    if (!isPdfExport())
	    	recursiveSetEnabled(tabItemPDF.getControl(), false);
	    if(!isTeiExport())
	    	recursiveSetEnabled(tabItemTEI.getControl(), false);
	    if(!isDocxExport())
	    	recursiveSetEnabled(tabItemDOCX.getControl(), false);
	    
	    showTab(0);

	  }
	  
	  private void showTab(int i){
		  tabFolder.setSelection(i);
	  }
	  
//	  private void refreshTabs(int i){
//		  if (!isMetsExport()){
//			  metsComposite.setEnabled(false);
//		  }
//		  if (!isPdfExport()){
//			  pdfComposite.setEnabled(false);
//		  }
//		  if(!isTeiExport()){
//			  teiComposite.setEnabled(false);
//		  }
//		  if(!isRtfExport()){
//			  rtfComposite.setEnabled(false);
//		  }
//		  if (isMetsExport()){
//			  metsComposite.setEnabled(true);
//		  }
//		  if (isPdfExport()){
//			  pdfComposite.setEnabled(true);
//		  }
//		  if(isTeiExport()){
//			  teiComposite.setEnabled(true);
//		  }
//		  if(isRtfExport()){
//			  rtfComposite.setEnabled(true);
//		  }
////		  if (isMetsExport()){
////			  tabItemMets = new CTabItem(tabFolder, SWT.NONE, i);
////			  tabItemMets.setText("Mets");
////			  tabItemMets.setToolTipText("Set export options for METS export");
////			  tabItemMets.setControl(metsComposite);
////		  }
//	  }
	  

	  
	  /**
	   * Gets the control for tab one
	   * 
	   * @param tabFolder2 the parent tab folder
	   * @return Control
	   */
	  private Control getTabOneControl(CTabFolder tabFolder2) {
		  	
			metsComposite = new Composite(tabFolder2, SWT.NONE);
			metsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
			metsComposite.setLayout(new GridLayout(1, true));
			
			final Button e1 = new Button(metsComposite, SWT.CHECK);
			final Button e2 = new Button(metsComposite, SWT.CHECK);
			final Button e21 = new Button(metsComposite, SWT.CHECK);
			final Button e3 = new Button(metsComposite, SWT.CHECK);
//			final Button e4 = new Button(metsComposite, SWT.CHECK);
			final Composite imgComp = new Composite(metsComposite, SWT.NONE);
			imgComp.setLayout(new GridLayout(2, false));
			Label imgQualLbl = new Label(imgComp, SWT.NONE);
			imgQualLbl.setText("Image type:");
			imgQualityCmb = new Combo(imgComp, SWT.READ_ONLY);
			
			filenamePatternComp = new FilenamePatternComposite(metsComposite, 0);
			
			e1.setSelection(true);
			e3.setSelection(true);
			setImgExport(true);
			setPageExport(true);
			
			e1.setText("Export Page");
			e2.setText("Export ALTO (Line Level)");
			e21.setText("Export ALTO (Word Level)");
			e21.setToolTipText("Words get determined from the lines with some degree of fuzziness");
			e3.setText("Export Image");
			
			imgQualityCmb.setItems(imgQualityChoices);
			imgQualityCmb.select(0);
			imgQualityCmb.pack();
			
//			e4.setText("Standardized Filenames");
			
			e1.addSelectionListener(new SelectionAdapter() {
			
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setPageExport(true);
			        }
			        else{
			        	setPageExport(false);
			        	
			        	/*
			        	 * one export should always be selected - so if no alto export the page export cannot be deselected
			        	 * not at the moment
			        	 */
//			        	if (!e2.getSelection()){
//			        		e1.setSelection(true);
//			        	}
//			        	else{
//			        		setPageExport(false);
//			        	}
			        }	
			    }
			});
			
			e2.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setAltoExport(true, false);
			        	e21.setSelection(false);
			        }
			        else{
			        	setAltoExport(false, false);
			        	//e1.setSelection(true);
			        }
			    }
			});
			
			e21.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setAltoExport(true, true);
			        	e2.setSelection(false);
			        }
			        else{
			        	setAltoExport(false, false);
			        	//e1.setSelection(true);
			        }
			    }
			});
			
			e3.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        boolean checked = btn.getSelection();
			        setImgExport(checked);
			        imgQualityCmb.setEnabled(checked);
			    }
			});
			
//			e4.addSelectionListener(new SelectionAdapter() {
//			    @Override
//			    public void widgetSelected(SelectionEvent event) {
//			        Button btn = (Button) event.getSource();
//			        if (btn.getSelection()){
//			        	setFileNamePattern(ExportFilenameUtils.STANDARDIZED_PATTERN);
//			        }
//			        else{
//			        	setFileNamePattern(ExportFilenameUtils.FILENAME_PATTERN);
//			        }
//			    }
//			});
			
			return metsComposite;
	}
	  
	private Control getTabTwoControl(CTabFolder tabFolder) {
		  
		pdfComposite = new Composite(tabFolder, SWT.NONE);
		pdfComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		pdfComposite.setLayout(new GridLayout(1, true));
		
	    imagesPlusTextBtn = new Button(pdfComposite, SWT.CHECK);
	    imagesPlusTextBtn.setText("Images plus text layer");
	    imagesPlusTextBtn.setToolTipText("The transcribed text will be added to the PDF under each image");
	    imagesPlusTextBtn.setSelection(true);
	    setExportImagesPlusText(true);
	    imagesPlusTextBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setExportImagesPlusText(imagesPlusTextBtn.getSelection());
				if (imagesPlusTextBtn.getSelection()){
					setExportImagesOnly(false);
					imagesOnlyBtn.setSelection(false);
				}
				else {
					setExportImagesPlusText(true);
					imagesPlusTextBtn.setSelection(true);
				}
			}
			
		});
	    
	    imagesOnlyBtn = new Button(pdfComposite, SWT.CHECK);
	    imagesOnlyBtn.setText("Images only");
	    imagesOnlyBtn.setToolTipText("Only the images get exported to PDF");
	    //imagesOnlyBtn.setSelection(true);
	    imagesOnlyBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setExportImagesOnly(imagesOnlyBtn.getSelection());
				if (imagesOnlyBtn.getSelection()){
					setExportImagesPlusText(false);
					imagesPlusTextBtn.setSelection(false);
				}
				else{
					if (!isExportImagesPlusText()){
						setExportImagesPlusText(true);
						imagesPlusTextBtn.setSelection(true);
					}
				}
			}
		});
	    
	    addExtraTextPagesBtn = new Button(pdfComposite, SWT.CHECK);
	    addExtraTextPagesBtn.setText("Extra text pages");
	    addExtraTextPagesBtn.setToolTipText("The transcribed text will be added to the PDF as extra page after each image");
	    //addExtraTextPagesBtn.setSelection(true);
	    addExtraTextPagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setAddExtraTextPages2PDF(addExtraTextPagesBtn.getSelection());
			}
		});
	    
	    highlightTagsBtn = new Button(pdfComposite, SWT.CHECK);
	    highlightTagsBtn.setText("Highlight tags");
	    highlightTagsBtn.setToolTipText("The tags will be underlined with colors in the exported PDF");
	    highlightTagsBtn.setSelection(false);
	    setHighlightTags(false);
	    highlightTagsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setHighlightTags(highlightTagsBtn.getSelection());
			}
		});
	    
	    highlightArticlesBtn = new Button(pdfComposite, SWT.CHECK);
	    highlightArticlesBtn.setText("Highlight articles");
	    highlightArticlesBtn.setToolTipText("The articles will be underlined with different colors in the exported PDF");
	    highlightArticlesBtn.setSelection(false);
	    setHighlightArticles(false);
	    highlightArticlesBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setHighlightArticles(highlightArticlesBtn.getSelection());
			}
		});
	    
		final Composite imgComp = new Composite(pdfComposite, SWT.NONE);
		imgComp.setLayout(new GridLayout(2, false));
	    
	    /*
	     * dropdown with fonts for export
	     */
	    Label fontLabel = new Label(imgComp, SWT.NONE);
	    fontLabel.setText("Select Font");
	    fontDropDown = new Combo(imgComp, SWT.READ_ONLY);
	    fontDropDown.add("Arial");
	    fontDropDown.add("Arialunicodems");
	    /*
	     * next font covers a lot but is not for free use
	     */
	    //fontDropDown.add("Code2000");
	    fontDropDown.add("FreeSerif");
	    fontDropDown.add("Junicode");
	    fontDropDown.add("NotoSans-Regular");
	    //fontDropDown.add("DejaVuSansMono");
	    fontDropDown.select(2);
	    setFont(fontDropDown.getText());
	    fontDropDown.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e) {
	        	setFont(fontDropDown.getText());
	        }
	    });
	    

		Label imgQualLbl = new Label(imgComp, SWT.NONE);
		imgQualLbl.setText("Image type:");
		pdfImgQualityCmb = new Combo(imgComp, SWT.READ_ONLY);
		
		pdfImgQualityCmb.setItems(imgQualityChoices);
		pdfImgQualityCmb.select(1);
		pdfImgQualityCmb.pack();

	    return pdfComposite;
	}
	  
	private void setHighlightTags(boolean b) {
		highlightTags = b;
		
	}
	
	private void setHighlightArticles(boolean b) {
		highlightArticles = b;
		
	}

	private Control getTabThreeControl(CTabFolder tabFolder) {
		 
		if (teiComposite != null && !teiComposite.isDisposed()){
			teiComposite.dispose();
		}
	  	teiComposite = new Composite(tabFolder, SWT.NONE);
	  	
		teiComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		teiComposite.setLayout(new GridLayout(1, true));
			
//		Text infoText = new Text(teiComposite, SWT.MULTI);
//		infoText.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
//		infoText.setText("The xslt for the 'TEI base export' can be found here:");
		
		if (isDoServerExport()){
		
			Link help = new Link(teiComposite, 0);
			String t2iParsLink="https://github.com/dariok/page2tei";
			help.setText("\nThe xslt for the 'TEI base export' can be found here:\n\nView it on Github: <a href=\""+t2iParsLink+"\">"+t2iParsLink+"</a>\n");
			help.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event e) {
					try {
						org.eclipse.swt.program.Program.launch(e.text);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
				}
			});
			
			Text infoText2 = new Text(teiComposite, SWT.MULTI);
			infoText2.setEditable(false);
			infoText2.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			infoText2.setText("If you want to use your own transformation we/you can adapt this template.\n"
					+ "Please send us a feature request or email (email@transkribus.eu)!\n"
					+ "Afterwards we will check and integrate your xslt and put it here for selection.\n\n"
					+ "Please be aware that the TEI files from the server are different to the TEI files\n"
					+ "exported via the client!");
		}
		else{
		
	//		zonesGroup = new CheckBoxGroup(teiComposite, 0);
			Group zonesGroup = new Group(teiComposite, SWT.CHECK);
			zonesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			zonesGroup.setLayout(new GridLayout(1, true));
			zonesGroup.setText("Zones");
	//		zonesGroup.activate();
			
	//		zonesGroup.addSelectionListener(new SelectionAdapter() {
	//			@Override public void widgetSelected(SelectionEvent e) {
	//				System.out.println("selected!!");
	//				zonePerParRadio.setEnabled(zonesGroup.isActivated());
	//				zonePerLineRadio.setEnabled(zonesGroup.isActivated());
	//				zonePerWordRadio.setEnabled(zonesGroup.isActivated());
	//				zonesCoordsAsBoundingBoxChck.setEnabled(zonesGroup.isActivated());
	//			}
	//		});
	
			noZonesRadio = new Button(zonesGroup, SWT.CHECK);
			noZonesRadio.setText("No zones");
			noZonesRadio.setToolTipText("Create no zones, just paragraphs");
			noZonesRadio.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					zonePerParRadio.setEnabled(!noZonesRadio.getSelection());
					zonePerLineRadio.setEnabled(!noZonesRadio.getSelection());
					zonePerWordRadio.setEnabled(!noZonesRadio.getSelection());
					zonesCoordsAsBoundingBoxChck.setEnabled(!noZonesRadio.getSelection());
				}
			});
			
			zonePerParRadio = new Button(zonesGroup, SWT.CHECK);
			zonePerParRadio.setText("Zone per region");
			zonePerParRadio.setToolTipText("Create a zone element for each region");
			zonePerParRadio.setSelection(true);
			
			zonePerLineRadio = new Button(zonesGroup, SWT.CHECK);
			zonePerLineRadio.setToolTipText("Create a zone element for each region and line");
			zonePerLineRadio.setText("Zone per line");
			zonePerLineRadio.setSelection(true);
			
			zonePerWordRadio = new Button(zonesGroup, SWT.CHECK);
			zonePerWordRadio.setToolTipText("Create a zone element for each region, line and word");
			zonePerWordRadio.setText("Zone per word");
			
			zonesCoordsAsBoundingBoxChck = new Button(zonesGroup, SWT.CHECK);
			zonesCoordsAsBoundingBoxChck.setToolTipText("By default all polygon coordinates are exported as 'points' attribute in the zone tag.\nWhen checked, coordinates are reduced to bounding boxes using 'ulx, uly, lrx, lry' attributes");
			zonesCoordsAsBoundingBoxChck.setText("Use bounding box coordinates");
			
			pbImageNameXmlIdChck = new Button(zonesGroup, SWT.CHECK);
			pbImageNameXmlIdChck.setToolTipText("Use the image name as xml:id attribute for page break (pb) elements\nWarning: xml:id's starting with a number are not valid!");
			pbImageNameXmlIdChck.setText("Image name as <pb> xml:id"); 
			
			Group linebreakTypeGroup = new Group(teiComposite, 0);
			linebreakTypeGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
			linebreakTypeGroup.setLayout(new GridLayout(1, true));
			linebreakTypeGroup.setText("Line breaks");
			
			lineTagsRadio = new Button(linebreakTypeGroup, SWT.RADIO);
			lineTagsRadio.setToolTipText("Create a line tag (<l>...</l>) to tag a line");
			lineTagsRadio.setText("Line tags (<l>...</l>)");
			lineTagsRadio.setSelection(true);
			
			lineBreaksRadio = new Button(linebreakTypeGroup, SWT.RADIO);
			lineBreaksRadio.setToolTipText("Create a line break (<lb/>) to tag a line");
			lineBreaksRadio.setText("Line breaks (<lb/>");
		}

	    return teiComposite;
	}
	
	private Control getTabFourControl(CTabFolder tabFolder) {
		  
	  	docxComposite = new Composite(tabFolder, SWT.NONE);
	  	docxComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
	  	docxComposite.setLayout(new GridLayout(1, true));

//		wordBasedBtn = new Button(rtfComposite, SWT.CHECK);
//		wordBasedBtn.setText("Word based");
//		wordBasedBtn.setToolTipText("If checked, text from word based segmentation will be exported");
//		wordBasedBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		
//		wordBasedBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				wordBased = wordBasedBtn.getSelection();
//			}
//		});
		
		exportTagsBtn = new Button(docxComposite, SWT.CHECK);
		exportTagsBtn.setText("Export selected Tags");
		exportTagsBtn.setToolTipText("If checked, all tags will be listed at the end of the export doc");
		exportTagsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		exportTagsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setDocxTagExport(exportTagsBtn.getSelection());
			}
		});
		
		preserveLinebreaksBtn = new Button(docxComposite, SWT.CHECK);
		preserveLinebreaksBtn.setText("Preserve line breaks");
		preserveLinebreaksBtn.setToolTipText("If checked, all line breaks are adopted to the exported text.");
		preserveLinebreaksBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		preserveLinebreaksBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setPreserveLineBreaks(preserveLinebreaksBtn.getSelection());
			}
		});
		
		forcePagebreaksBtn = new Button(docxComposite, SWT.CHECK);
		forcePagebreaksBtn.setText("Force page breaks");
		forcePagebreaksBtn.setToolTipText("If checked, after each document page a page break is made.");
		forcePagebreaksBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		forcePagebreaksBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setForcePagebreaks(forcePagebreaksBtn.getSelection());
			}
		});
		
		markUnclearWordsBtn = new Button(docxComposite, SWT.CHECK);
		markUnclearWordsBtn.setText("Mark unclear words");
		markUnclearWordsBtn.setToolTipText("If checked, all unclear tags get printed inside two square brackets -> [unclear]. Tag export must be choosen too!");
		markUnclearWordsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		markUnclearWordsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setMarkUnclearWords(markUnclearWordsBtn.getSelection());
			}
		});
		
		Group abbrevGroup = new Group(docxComposite, 0);
		abbrevGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		abbrevGroup.setLayout(new GridLayout(1, true));
		abbrevGroup.setText("Abbreviation Settings");
		
		keepAbbrevBtn = new Button(abbrevGroup, SWT.CHECK);
		keepAbbrevBtn.setText("Keep abbreviations");
		keepAbbrevBtn.setToolTipText("If checked, all abbreviations are shown as they are. Tag export must be choosen too!");
		keepAbbrevBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));
		keepAbbrevBtn.setSelection(true);

		keepAbbrevBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setKeepAbbreviations(keepAbbrevBtn.getSelection());
				//only one of the abbreviation possibilities can be chosen
				if (keepAbbrevBtn.getSelection()){
					setSubstituteAbbreviations(false);
					substituteAbbrevBtn.setSelection(false);
					setExpandAbbrevs(false);
					expandAbbrevBtn.setSelection(false);
				}
				//keep abbrevs as they are as default if no other option is selected
				else if (!substituteAbbrevBtn.getSelection() && !expandAbbrevBtn.getSelection()){
					setKeepAbbreviations(true);
					keepAbbrevBtn.setSelection(true);
				}
			}
		});
		
		expandAbbrevBtn = new Button(abbrevGroup, SWT.CHECK);
		expandAbbrevBtn.setText("Expand abbreviations");
		expandAbbrevBtn.setToolTipText("If checked, all abbreviations are followed by their expansion. Tag export must be choosen too!");
		expandAbbrevBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		expandAbbrevBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setExpandAbbrevs(expandAbbrevBtn.getSelection());
				//only one of the abbreviation possibilities can be chosen
				if (expandAbbrevBtn.getSelection()){
					setSubstituteAbbreviations(false);
					substituteAbbrevBtn.setSelection(false);
					setKeepAbbreviations(false);
					keepAbbrevBtn.setSelection(false);
				}
				else{
					//at this point no abbrevBtn is selected -> keep abbrevs as they are as default
					if (!substituteAbbrevBtn.getSelection()){
						setKeepAbbreviations(true);
						keepAbbrevBtn.setSelection(true);
					}
				}
			}
		});
		
		substituteAbbrevBtn = new Button(abbrevGroup, SWT.CHECK);
		substituteAbbrevBtn.setText("Substitute abbreviations");
		substituteAbbrevBtn.setToolTipText("If checked, all abbreviations get replaced by their expansion. Tag export must be choosen too!");
		substituteAbbrevBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		substituteAbbrevBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setSubstituteAbbreviations(substituteAbbrevBtn.getSelection());
				//only one of the abbreviation possibilities can be chosen
				if (substituteAbbrevBtn.getSelection()){
					setExpandAbbrevs(false);
					expandAbbrevBtn.setSelection(false);
					setKeepAbbreviations(false);
					keepAbbrevBtn.setSelection(false);
				}
				else{
					//at this point no abbrevBtn is selected -> keep abbrevs as they are as default
					if (!expandAbbrevBtn.getSelection()){
						setKeepAbbreviations(true);
						keepAbbrevBtn.setSelection(true);
					}
				}
			}
		});
		
		Group suppliedGroup = new Group(docxComposite, 0);
		suppliedGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		suppliedGroup.setLayout(new GridLayout(1, true));
		suppliedGroup.setText("Supplied Tags Settings");
				
		ignoreSuppliedBtn = new Button(suppliedGroup, SWT.CHECK);
		ignoreSuppliedBtn.setText("Ignore supplied tags");
		ignoreSuppliedBtn.setToolTipText("If checked, all supplied tags gets ignored. Tag export must be choosen too!");
		ignoreSuppliedBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		ignoreSuppliedBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setIgnoreSupplied(ignoreSuppliedBtn.getSelection());
				//only one of the supplied possibilities can be chosen
				if (ignoreSuppliedBtn.getSelection()){
					setShowSuppliedWithBrackets(false);
					showSuppliedWithBracketsBtn.setSelection(false);
				}
			}
		});
		
		showSuppliedWithBracketsBtn = new Button(suppliedGroup, SWT.CHECK);
		showSuppliedWithBracketsBtn.setText("[Show supplied tags inside brackets]");
		showSuppliedWithBracketsBtn.setToolTipText("If checked, all supplied tags are shown inside brackets. Tag export must be choosen too!");
		showSuppliedWithBracketsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		showSuppliedWithBracketsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setShowSuppliedWithBrackets(showSuppliedWithBracketsBtn.getSelection());
				//only one of the abbreviation possibilities can be chosen
				if (showSuppliedWithBracketsBtn.getSelection()){
					setIgnoreSupplied(false);
					ignoreSuppliedBtn.setSelection(false);
				}
			}
		});
		
		
		
	    return docxComposite;
	}
	
	private void showPageChoice() {
		docPagesSelector.setVisible(isPageableExport());
//		docPagesSelector.getCurrentPageBtn().setVisible(isPageableExport());
	}
	
	private void showTagChoice() {
		tagsSelector.setVisible(isTagableExport());
	}
	
	public boolean isPageableExport() {
		return isMetsExport() || isPdfExport() || isDocxExport() || isTxtExport() || isTagXlsxExport() || isTableXlsxExport() || isTeiExport();
	}
		
	public boolean isTagableExport(){
		return (isPdfExport() || isDocxExport() || isTagXlsxExport() || isTeiExport());
	}
	
	public boolean isTagableExportChosen(){
		return (isPdfExport() || isDocxExport() || isTagXlsxExport() || isTeiExport()) && (isHighlightTags() || isDocxTagExport() || isTagXlsxExport());
	}
		
	private void updateSelectedPages() {
		try {
			pagesStr = docPagesSelector.getPagesText().getText();
			if (isDoServerExport() && !exportCurrentDocOnServer) { // multiple doc-export
				pageIndices = CoreUtils.parseRangeListStr(pagesStr, Integer.MAX_VALUE);
			}
			else {
				pageIndices = docPagesSelector.getSelectedPageIndices();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			pageIndices = null;
			pagesStr = null;
		}
		
		logger.debug("pagesStr: "+pagesStr);	
	}
	
	private void updateParameters() {
		updateSelectedPages();
		
		if (StringUtils.isEmpty(pagesStr)) {
			DialogUtil.showErrorMessageBox(shell, "Invalid pages", "Invalid pages specified: "+pagesStr);
			return;
		}				
		
		if(isTagableExport()) { // needed here?
			setSelectedTagsList(tagsSelector.getCheckedTagnames());
			setDocxTagExport(isDocxTagExport());
		}
		
		if (!ExportFilePatternUtils.isFileNamePatternValid(filenamePatternComp.pattern.getText())) {
			DialogUtil.showErrorMessageBox(shell, "Invalid filename pattern", "Invalid filename pattern specified: "+filenamePatternComp.pattern.getText());
			return;
		}

		updateCommonPars();
		updateAltoPars();
		if (!isDoServerExport()){
			updateTeiPars();
		}
		updatePdfPars();
		updateDocxPars();
	}
		
	private void updateCommonPars() {
		commonPars = new CommonExportPars(getPagesStr(), metsExport, imgExport, pageExport, altoExport, 
				pdfExport, teiExport, docxExport, txtExport, tagXlsxExport, tagIOBExport, tableXlsxExport, createTitlePage, versionStatus, wordBased, doBlackening, getSelectedTagsList(), font);
		commonPars.setFileNamePattern(filenamePatternComp.pattern.text.getText());
		
		if(isImgExport() && imgQualityCmb != null) {
			ImgType type = getSelectedImgType(imgQualityCmb);
			logger.debug("Setting img quality for export: " + type.toString());
			commonPars.setRemoteImgQuality(type);
		}
		
		commonPars.setSplitIntoWordsInAltoXml(isSplitUpWords());
	}
	
	private ImgType getSelectedImgType(Combo cmb) {
		final int i = cmb.getSelectionIndex();
		ImgTypeLabels type = ImgTypeLabels.valueOf(imgQualityChoices[i]);
		if(type == null) {
			logger.error("Could not determine selected image type: " + imgQualityChoices[i]);
			type = ImgTypeLabels.Original;
		}
		return type.getImgType();
	}

	public CommonExportPars getCommonExportPars() {
		return commonPars;
	}
	
	private void updateTeiPars() {
		boolean noZones = noZonesRadio.getSelection();
		boolean regions = noZones ? false : zonePerParRadio.getSelection();
		boolean lines = noZones ? false : zonePerLineRadio.getSelection();
		boolean words = noZones ? false : zonePerWordRadio.getSelection();
		boolean boundingBoxCoords = zonesCoordsAsBoundingBoxChck.getSelection();
		String linebreakType = TeiExportPars.LINE_BREAK_TYPE_LINE_TAG;
		if (lineBreaksRadio.getSelection()) {
			linebreakType = TeiExportPars.LINE_BREAK_TYPE_LINE_BREAKS;
		}
		if (lineTagsRadio.getSelection()) {
			linebreakType = TeiExportPars.LINE_BREAK_TYPE_LINE_TAG;
		}
		
		teiPars = new TeiExportPars(regions, lines, words, boundingBoxCoords, linebreakType);
		teiPars.setPbImageNameAsXmlId(pbImageNameXmlIdChck.getSelection());
	}
	
	public TeiExportPars getTeiExportPars() {
		return teiPars;
	}
	
	private void updateAltoPars() {
		altoPars = new AltoExportPars(splitUpWords);
	}
	
	public AltoExportPars getAltoPars() {
		return altoPars;
	}
	
	private void updatePdfPars() {		
		pdfPars = new PdfExportPars(exportImagesOnly, exportImagesPlusText, addExtraTextPages2PDF, highlightTags, highlightArticles);
		if(pdfImgQualityCmb != null) {
			ImgType type = getSelectedImgType(pdfImgQualityCmb);
			logger.debug("Setting img quality for export: " + type.toString());
			pdfPars.setPdfImgQuality(type);
		}
	}
	
	public PdfExportPars getPdfPars() {
		return pdfPars;
	}
	
	private void updateDocxPars() {
		docxPars = new DocxExportPars(docxTagExport, preserveLinebreaks, forcePagebreaks, markUnclearWords, 
				keepAbbreviations, expandAbbreviations, substituteAbbreviations);
	}
	
	public DocxExportPars getDocxPars() {
		return docxPars;
	}
	
	public boolean isWordBased() {
		return wordBased;
	}
	
	public boolean isDoBlackening() {
		return doBlackening;
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public File open() {
		result = null;
		createContents();
		SWTUtil.centerShell(shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
//	public Integer getStartPage(){
//		return startPage;
//	}
//	public Integer getEndPage(){
//		return endPage;
//	}

	public boolean isDocxExport() {
		return docxExport;
	}

	public void setDocxExport(boolean docxExport) {
		this.docxExport = docxExport;
	}
	
	public boolean isTxtExport() {
		return txtExport;
	}

	public void setTxtExport(boolean txtExport) {
		this.txtExport = txtExport;
	}

	public boolean isPdfExport() {
		return pdfExport;
	}

	public void setPdfExport(boolean pdfExport) {
		this.pdfExport = pdfExport;
	}

	public boolean isTeiExport() {
		return teiExport;
	}

	public void setTeiExport(boolean teiExport) {
		this.teiExport = teiExport;
	}

	public boolean isAltoExport() {
		return altoExport;
	}
	
	public boolean isSplitUpWords() {
		return splitUpWords;
	}

	public void setAltoExport(boolean altoExport, boolean wordLevel) {
		this.altoExport = altoExport;
		this.splitUpWords = wordLevel;
	}

	public boolean isMetsExport() {
		return metsExport;
	}

	public void setMetsExport(boolean metsPageExport) {
		this.metsExport = metsPageExport;
	}

	public boolean isPageExport() {
		return pageExport;
	}

	public void setPageExport(boolean pageExport) {
		this.pageExport = pageExport;
	}

	public boolean isAddExtraTextPages2PDF() {
		return addExtraTextPages2PDF;
	}

	public Set<String> getSelectedTagsList() {
			return selectedTagsList;
	}

	public void setSelectedTagsList(Set<String> set) {
		logger.debug("set tag list : " + set);
		this.selectedTagsList = set;
	}

	public boolean isDocxTagExport() {
		return docxTagExport;
	}

	public void setDocxTagExport(boolean docxTagExport) {
		this.docxTagExport = docxTagExport;
	}
	
	public boolean isPreserveLinebreaks() {
		return preserveLinebreaks;
	}
	
	protected void setPreserveLineBreaks(boolean preserveBreaks) {
		this.preserveLinebreaks = preserveBreaks;
		
	}
	
	public boolean isForcePagebreaks() {
		return forcePagebreaks;
	}

	public void setForcePagebreaks(boolean forcePagebreaks) {
		this.forcePagebreaks = forcePagebreaks;
	}

	public boolean isMarkUnclearWords() {
		return markUnclearWords;
	}

	public void setMarkUnclearWords(boolean markUnclear) {
		this.markUnclearWords = markUnclear;
	}
	
	public boolean isTagXlsxExport() {
		return tagXlsxExport;
	}

	public void setTagXlsxExport(boolean tagXlsxExport) {
		this.tagXlsxExport = tagXlsxExport;
	}
	
	public boolean isTagIOBExport() {
		return tagIOBExport;
	}
	
	public void setTagIOBExport(boolean tagIOBExport) {
		this.tagIOBExport = tagIOBExport;
	}
	
	public boolean isTableXlsxExport() {
		return tableXlsxExport;
	}

	public void setTableXlsxExport(boolean tableExport) {
		this.tableXlsxExport = tableExport;
	}

	public void setAddExtraTextPages2PDF(boolean addExtraTextPages2PDF) {
		this.addExtraTextPages2PDF = addExtraTextPages2PDF;
	}

	public boolean isHighlightTags() {
		return highlightTags;
	}
	
	public boolean isHighlightArticles() {
		return highlightArticles;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}

	public boolean isCreateTitlePage() {
		return createTitlePage;
	}

	public ExportPathComposite getExportPathComp() {
		return exportPathComp;
	}

	public boolean isImgExport() {
		return imgExport;
	}

	public void setImgExport(boolean imgExport) {
		this.imgExport = imgExport;
	}

	public boolean isZipExport() {
		return zipExport;
	}

	public void setZipExport(boolean zipExport) {
		this.zipExport = zipExport;
	}
	
	public boolean isExpandAbbrevs() {
		return expandAbbreviations;
	}

	public void setExpandAbbrevs(boolean expandAbbrevs) {
		this.expandAbbreviations = expandAbbrevs;
	}

	public boolean isSubstituteAbbreviations() {
		return substituteAbbreviations;
	}

	public void setSubstituteAbbreviations(boolean substituteAbbreviations) {
		this.substituteAbbreviations = substituteAbbreviations;
	}

	public boolean isKeepAbbreviations() {
		return keepAbbreviations;
	}

	public void setKeepAbbreviations(boolean keepAbbreviations) {
		this.keepAbbreviations = keepAbbreviations;
	}
	
	public boolean isIgnoreSupplied() {
		return ignoreSupplied;
	}

	public void setIgnoreSupplied(boolean ignore) {
		this.ignoreSupplied = ignore;
	}
	
	public boolean isShowSuppliedWithBrackets() {
		return showSuppliedWithBrackets;
	}

	public void setShowSuppliedWithBrackets(boolean showWithBrackets) {
		this.showSuppliedWithBrackets = showWithBrackets;
	}

	public boolean isExportImagesOnly() {
		return exportImagesOnly;
	}

	public void setExportImagesOnly(boolean exportImagesOnly) {
		this.exportImagesOnly = exportImagesOnly;
	}

	public boolean isExportImagesPlusText() {
		return exportImagesPlusText;
	}

	public void setExportImagesPlusText(boolean exportImagesPlusText) {
		this.exportImagesPlusText = exportImagesPlusText;
	}

	public String getVersionStatus() {
		return versionStatus;
	}

	public void setVersionStatus(String versionStatus) {
		this.versionStatus = versionStatus;
	}
	
	public boolean isDoServerExport() {
		return doServerExport;
	}

	public void setDoServerExport(boolean doServerExport) {
		this.doServerExport = doServerExport;
	}
	
	public boolean isExportCurrentDocOnServer() {
		return exportCurrentDocOnServer;
	}
	
	public List<DocumentSelectionDescriptor> getDocumentsToExportOnServer() {
		return documentsToExportOnServer;
	}

	public String getPagesStr() {
		return pagesStr;
	}
	
	public Set<Integer> getPageIndices() {
		return pageIndices;
	}
	
	public static class FilenamePatternComposite extends Composite {
		Group group;
		Button pageNr_FilenamePattern, fileNamePattern, docId_PageNr_PageIdPattern;
		LabeledText pattern;
		Label patternDescription;
		
		public FilenamePatternComposite(Composite parent, int style) {
			super(parent, style);
			this.setLayout(new FillLayout());
			
			group = new Group(this, 0);
			group.setText("Filename pattern");
			group.setLayout(new GridLayout(1, false));
			
			pageNr_FilenamePattern = new Button(group, SWT.RADIO);
			pageNr_FilenamePattern.setText("pageNr + filename");
			//pageNr_FilenamePattern.setSelection(true);
			
			fileNamePattern = new Button(group, SWT.RADIO);
			fileNamePattern.setText("filename (warning: filenames must be unique for document)");
			fileNamePattern.setSelection(true);
			
			docId_PageNr_PageIdPattern = new Button(group, SWT.RADIO);
			docId_PageNr_PageIdPattern.setText("docId + pageNr + pageId");
			
//			customPattern = new Button(group, SWT.RADIO);
//			customPattern.setText("Custom pattern");
			
			pattern = new LabeledText(group, "Pattern: ");
			pattern.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			pattern.text.setToolTipText("The filename pattern is a combination of regular characters and placeholders for document-id etc. (see below)");
//			pattern.text.addKeyListener(new KeyAdapter() {		
//				@Override
//				public void keyPressed(KeyEvent e) {
//					// prevent editing 
//					if (!customPattern.getSelection())
//						e.doit = false;
//				}
//			});
			
			patternDescription = new Label(group, 0);
			patternDescription.setText("Placeholder: "+StringUtils.join(ExportFilePatternUtils.ALL_PATTERNS, ", "));
			
			addListener();
			updatePattern();
		}
		
		void addListener() {
			SelectionListener listener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updatePattern();
				}
			};
			
			pageNr_FilenamePattern.addSelectionListener(listener);
			fileNamePattern.addSelectionListener(listener);
			docId_PageNr_PageIdPattern.addSelectionListener(listener);
//			customPattern.addSelectionListener(listener);
		}
		
		void updatePattern() {
			if (pageNr_FilenamePattern.getSelection()) {
				pattern.text.setText(ExportFilePatternUtils.PAGENR_PATTERN+"_"+ExportFilePatternUtils.FILENAME_PATTERN);
			}
			else if (fileNamePattern.getSelection()) {
				pattern.text.setText(ExportFilePatternUtils.FILENAME_PATTERN);
			}
			else if (docId_PageNr_PageIdPattern.getSelection()) {
				pattern.text.setText(ExportFilePatternUtils.STANDARDIZED_PATTERN);
			}
//			pattern.text.setEnabled(customPattern.getSelection());
		}
	}
	
	private enum ImgTypeLabels {
		Original(ImgType.orig),
		JPEG(ImgType.view);
		private ImgType imgType;
		private ImgTypeLabels(ImgType type) {
			this.imgType = type;		}
		public ImgType getImgType() {
			return this.imgType;
		}
	}
}
