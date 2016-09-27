package eu.transkribus.swt_gui.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.tei.TeiExportPars.TeiExportMode;
import eu.transkribus.core.model.builder.tei.TeiExportPars.TeiLinebreakMode;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.util.DocPagesSelector;
import eu.transkribus.swt_gui.util.TagsSelector;

public class CommonExportDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(CommonExportDialog.class);
	
	Shell shell;
	ExportPathComposite exportPathComp;

	String lastExportFolder;
	String docName;
	File result=null;
	Button wordBasedBtn;
	Button exportTagsBtn;
	Button blackeningBtn;
	Button createTitlePageBtn;
	boolean wordBased=false;
	boolean tagExport=false;
	boolean doBlackening = false;
	boolean createTitlePage = false;
	
	Button preserveLinebreaksBtn;
	boolean preserveLinebreaks = false;
	Button markUnclearWordsBtn;
	boolean markUnclearWords = false;
	Button keepAbbrevBtn;
	boolean keepAbbreviations = false;
	Button expandAbbrevBtn;
	boolean expandAbbreviations = false;
	Button substituteAbbrevBtn;
	boolean substituteAbbreviations = false;
	
	Button noZonesRadio;
	Button zonePerParRadio;
	Button zonePerLineRadio;
	Button zonePerWordRadio;
	
	Button lineTagsRadio, lineBreaksRadio;
	
	TeiExportMode teiExportMode;
	TeiLinebreakMode teiLinebreakMode;
	
	boolean docxExport, pdfExport, teiExport, altoExport, imgExport, metsExport, pageExport, xlsxExport, zipExport;
	String fileNamePattern = "${filename}";
	Button addExtraTextPagesBtn;
	boolean addExtraTextPages2PDF;
	Button imagesOnlyBtn;
	boolean exportImagesOnly;
	Button imagesPlusTextBtn;
	boolean exportImagesPlusText;
	Button highlightTagsBtn;
	boolean highlightTags;
	CTabFolder tabFolder;
	CTabItem tabItemMets;
	CTabItem tabItemPDF;
	CTabItem tabItemTEI;
	CTabItem tabItemDOCX;
	
	Composite metsComposite;
	Composite pdfComposite;
	Composite teiComposite;
	Composite docxComposite;
	
	List<TrpPage> pages;
	Set<String> selectedTagsList;
	DocPagesSelector docPagesSelector;
	
	TagsSelector tagsSelector;
	
	Set<Integer> selectedPages = new HashSet<Integer>(); 
	
	Button currentPageBtn;

	String versionStatus;
	
	public CommonExportDialog(Shell parent, int style, String lastExportFolder, String docName, List<TrpPage> pages) {
		super(parent, style |= SWT.DIALOG_TRIM);
		this.lastExportFolder = lastExportFolder;
		this.docName = docName;
		this.pages = pages;
		Set<String> regTagNames = CustomTagFactory.getRegisteredTagNames();
		Set<String> usedTagNames = ExportUtils.getOnlyWantedTagnames(regTagNames);
		setSelectedTagsList(usedTagNames);
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle() | SWT.RESIZE);
//		shell.setSize(673, 420);
		shell.setSize(300, 300);
		shell.setText("Export document");
		shell.setLayout(new GridLayout(1, false));
		
		exportPathComp = new ExportPathComposite(shell, lastExportFolder, "File/Folder name: ", null, docName);
		exportPathComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		Composite choiceComposite = new Composite(shell, SWT.NONE);
		choiceComposite.setLayout(new GridLayout(2, false));
		choiceComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
		
	    // Create the first Group
	    Group group1 = new Group(choiceComposite, SWT.SHADOW_IN);
	    group1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
	    group1.setText("Choose export formats");
	    group1.setLayout(new GridLayout(1, false));
	    
	    final Button b0 = new Button(group1, SWT.CHECK);
	    b0.setText("TRP Document");
	    final Button b1 = new Button(group1, SWT.CHECK); 
	    b1.setText("PDF");
	    final Button b2 = new Button(group1, SWT.CHECK);
	    b2.setText("TEI");
	    final Button b3 = new Button(group1, SWT.CHECK);
	    b3.setText("DOCX");
	    final Button b4 = new Button(group1, SWT.CHECK);
	    b4.setText("Tag Export (Excel)");
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
	    
	    Composite optionsComposite = new Composite(choiceComposite, SWT.NONE);
	    optionsComposite.setLayout(new GridLayout(1, false));
	    GridData gridData = new GridData();
	    //gridData.heightHint = 0;
	    gridData.horizontalAlignment = SWT.RIGHT;
	    gridData.verticalAlignment = SWT.FILL;
	    gridData.verticalSpan = 2;
	    optionsComposite.setLayoutData(gridData);
	    
	    Label label = new Label(optionsComposite, SWT.NONE);
	    label.setText("Export options:");
	    
	    createTabFolders(optionsComposite);
	    
		wordBasedBtn = new Button(optionsComposite, SWT.CHECK);
		wordBasedBtn.setText("Word based");
		wordBasedBtn.setToolTipText("If checked, text from word based segmentation will be exported");
		wordBasedBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		wordBasedBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				wordBased = wordBasedBtn.getSelection();
			}
		});
		
		blackeningBtn = new Button(optionsComposite, SWT.CHECK);
		blackeningBtn.setText("Do blackening");
		blackeningBtn.setToolTipText("If checked, the blacked tagged words get considered");
		blackeningBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		blackeningBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				doBlackening = blackeningBtn.getSelection();
			}
		});
		
		createTitlePageBtn = new Button(optionsComposite, SWT.CHECK);
		createTitlePageBtn.setText("Create Title Page");
		createTitlePageBtn.setToolTipText("Title page contains author, title and editorial declaration");
		createTitlePageBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		createTitlePageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				createTitlePage = createTitlePageBtn.getSelection();
			}
		});
	    
	    docPagesSelector = new DocPagesSelector(optionsComposite, SWT.NONE, pages);
	    docPagesSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    docPagesSelector.setVisible(false);
	    
	    currentPageBtn = new Button(optionsComposite, SWT.PUSH);
	    currentPageBtn.setText("Export Current Page");
	    currentPageBtn.setToolTipText("Press this button if you want to export the current page only");
	    currentPageBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				String currentPageNr = Integer.toString(Storage.getInstance().getPageIndex()+1);
				docPagesSelector.getPagesText().setText(currentPageNr);				
			}
		});
	    
		tagsSelector = new TagsSelector(optionsComposite, SWT.NONE, getSelectedTagsList());
		tagsSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tagsSelector.setVisible(false);
		
	    // Create the first Group
	    Group group2 = new Group(choiceComposite, SWT.SHADOW_IN);
	    group2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
	    group2.setText("Choose version status");
	    group2.setLayout(new GridLayout(1, false));
	    
	    final Combo statusCombo = new Combo(group2, SWT.DROP_DOWN | SWT.READ_ONLY);

	    int size = EnumUtils.stringsArray(EditStatus.class).length + 2;
	    
	    String items[] = new String[size];
	    
	    items[0] = "Latest version";
	    setVersionStatus(items[0]);
	    items[1] = "Loaded version (for current page)";
	    int a = 2;

	    for (String s : EnumUtils.stringsArray(EditStatus.class)){
	    	items[a++] = s;
	    	//logger.debug("editStatus " + s);
	    }
	    
		statusCombo.setItems(items);
		statusCombo.select(0);
		
		statusCombo.addSelectionListener(new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent event) {
	        	setVersionStatus(statusCombo.getText());
	        }
		});

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
	    

		
	    b2.addSelectionListener(new SelectionAdapter() {

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
	    
	    b4.addSelectionListener(new SelectionAdapter() {

	        @Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
            	setXlsxExport(btn.getSelection());
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
	            b2.setSelection(btn.getSelection());
	            b2.notifyListeners(SWT.Selection, new Event());
	            b3.setSelection(btn.getSelection());
	            b3.notifyListeners(SWT.Selection, new Event());
	            b4.setSelection(btn.getSelection());
	            b4.notifyListeners(SWT.Selection, new Event());

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
	    
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		buttonComposite.setLayout(new FillLayout());
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateTeiExportMode();
				updateLineBreakMode();
				
				if (!isMetsExport() && !isPdfExport() && !isDocxExport() && !isTeiExport() && !isAltoExport() && !isXlsxExport()){
					DialogUtil.showInfoMessageBox(shell, "Missing export format", "Please choose an export format to continue");
				}
				else {
					if(isTagableExport()){
						setSelectedTagsList(tagsSelector.getCheckedTagnames());
						setTagExport(isTagExport());
					}
					//if (exportPathComp.checkExportFile()) {
						result = exportPathComp.getExportFile();
						
						boolean canWrite = result!=null && result.getParentFile()!=null && result.getParentFile().canWrite(); 
						if (!canWrite) {
							DialogUtil.showErrorMessageBox(shell, "Cannot write to folder", "Cannot write into the specified folder - do you have write access?\n\n"+result.getAbsolutePath());		
						} else						
							shell.close();
					//}
				}
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
		
		shell.pack();
		shell.layout();
		
		// save values when shell is disposed:
		shell.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				try {
					selectedPages = docPagesSelector.getSelectedPageIndices();
					
				} catch (IOException e1) {
					selectedPages = null;
				}
				logger.debug("selectedPages: "+selectedPages);
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
			final Button e3 = new Button(metsComposite, SWT.CHECK);
			final Button e4 = new Button(metsComposite, SWT.CHECK);
			
			e1.setSelection(true);
			e3.setSelection(true);
			setImgExport(true);
			setPageExport(true);
			
			e1.setText("Export Page");
			e2.setText("Export ALTO");
			e3.setText("Export Image");
			e4.setText("Standardized Filenames");
			
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
			        	setAltoExport(true);
			        }
			        else{
			        	setAltoExport(false);
			        	//e1.setSelection(true);
			        }
			    }
			});
			
			e3.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setImgExport(true);
			        }
			        else{
			        	setImgExport(false);
			        }
			    }
			});
			
			e4.addSelectionListener(new SelectionAdapter() {
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setFileNamePattern("${docId}_${pageNr}_${pageId}");
			        }
			        else{
			        	setFileNamePattern("${filename}");
			        }
			    }
			});
			
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
	    highlightTagsBtn.setToolTipText("The tags will be underlined with colors in the export PDF");
	    highlightTagsBtn.setSelection(false);
	    setHighlightTags(false);
	    highlightTagsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setHighlightTags(highlightTagsBtn.getSelection());
			}
		});

	    return pdfComposite;
	}
	  
	private void setHighlightTags(boolean b) {
		highlightTags = b;
		
	}

	private Control getTabThreeControl(CTabFolder tabFolder) {
		  
	  	teiComposite = new Composite(tabFolder, SWT.NONE);
	  	
		teiComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		teiComposite.setLayout(new GridLayout(1, true));
//		Label modeLabel = new Label(modeComposite, SWT.NONE);
//		modeLabel.setText("TEI Mode: ");
		
		Group zonesGroup = new Group(teiComposite, 0);
		zonesGroup.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		zonesGroup.setLayout(new GridLayout(1, true));
		zonesGroup.setText("Zones");
		
		noZonesRadio = new Button(zonesGroup, SWT.RADIO);
		noZonesRadio.setText("No zones");
		noZonesRadio.setToolTipText("Create no zones, just paragraphs");
		
		zonePerParRadio = new Button(zonesGroup, SWT.RADIO);
		zonePerParRadio.setText("Zone per region");
		zonePerParRadio.setToolTipText("Create a zone element for each region");
//		zonePerParRadio.setSelection(true);
		
		zonePerLineRadio = new Button(zonesGroup, SWT.RADIO);
		zonePerLineRadio.setToolTipText("Create a zone element for each region and line");
		zonePerLineRadio.setText("Zone per line");
		zonePerLineRadio.setSelection(true);
		
		zonePerWordRadio = new Button(zonesGroup, SWT.RADIO);
		zonePerWordRadio.setToolTipText("Create a zone element for each region, line and word");
		zonePerWordRadio.setText("Zone per word");
		
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
				setTagExport(exportTagsBtn.getSelection());
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
		
		
		
	    return docxComposite;
	}
	  
	


	public Set<Integer> getSelectedPages() {
		return selectedPages;
	}
	
	private void showPageChoice() {
		docPagesSelector.setVisible(isPageableExport());
		currentPageBtn.setVisible(isPageableExport());
	}
	
	private void showTagChoice() {
		tagsSelector.setVisible(isTagableExport());
	}
	
	public boolean isPageableExport() {
		return isMetsExport() || isPdfExport() || isDocxExport() || isXlsxExport() || isTeiExport();
	}
		
	public boolean isTagableExport(){
		return (isPdfExport() || isDocxExport() || isXlsxExport() || isTeiExport());
	}
	
	public boolean isTagableExportChosen(){
		return (isPdfExport() || isDocxExport() || isXlsxExport() || isTeiExport()) && (isHighlightTags() || isTagExport() || isXlsxExport());
	}
	
	private void updateTeiExportMode() {
		teiExportMode = TeiExportMode.ZONE_PER_PAR;
		
		if (noZonesRadio.getSelection()) {
			teiExportMode = TeiExportMode.SIMPLE;
		} else if (zonePerParRadio.getSelection()) {
			teiExportMode = TeiExportMode.ZONE_PER_PAR;
		} else if (zonePerLineRadio.getSelection()) {
			teiExportMode = TeiExportMode.ZONE_PER_LINE;
		} else if (zonePerWordRadio.getSelection()) {
			teiExportMode = TeiExportMode.ZONE_PER_WORD;
		} else {
			logger.error("No TEI export mode could be set - should never happen!");
		}
	}
	
	private void updateLineBreakMode() {
		teiLinebreakMode = TeiLinebreakMode.LINE_TAG;
		
		if (lineBreaksRadio.getSelection()) {
			teiLinebreakMode = TeiLinebreakMode.LINE_BREAKS;
		} else if (lineTagsRadio.getSelection()) {
			teiLinebreakMode = TeiLinebreakMode.LINE_TAG;
		} else {
			logger.error("No TEI linebreak mode could be set - should never happen!");
		}
		
	}
	
	private void updatePages() {
//		startPage = startSpinner.getSelection();
//		endPage = endSpinner.getSelection();
		//logger.debug("pages " + startPage + "-" + endPage);
	}
	
	public TeiExportMode getTeiExportMode(){
		return teiExportMode;
	}
	
	public TeiLinebreakMode getTeiLinebreakMode() {
		return teiLinebreakMode;
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

	public void setAltoExport(boolean altoExport) {
		this.altoExport = altoExport;
	}
	
	public void setFileNamePattern(final String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
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

	public boolean isTagExport() {
		return tagExport;
	}

	public void setTagExport(boolean tagExport) {
		this.tagExport = tagExport;
	}
	
	public boolean isPreserveLinebreaks() {
		// TODO Auto-generated method stub
		return preserveLinebreaks;
	}
	
	protected void setPreserveLineBreaks(boolean preserveBreaks) {
		this.preserveLinebreaks = preserveBreaks;
		
	}
	
	public boolean isMarkUnclearWords() {
		return markUnclearWords;
	}

	public void setMarkUnclearWords(boolean markUnclear) {
		this.markUnclearWords = markUnclear;
	}
	
	public boolean isExpandAbbrevs() {
		return expandAbbreviations;
	}

	public void setExpandAbbrevs(boolean expandAbbrevs) {
		this.expandAbbreviations = expandAbbrevs;
	}

	public boolean isXlsxExport() {
		return xlsxExport;
	}

	public void setXlsxExport(boolean xlsxExport) {
		this.xlsxExport = xlsxExport;
	}

	public void setAddExtraTextPages2PDF(boolean addExtraTextPages2PDF) {
		this.addExtraTextPages2PDF = addExtraTextPages2PDF;
	}

	public boolean isHighlightTags() {
		// TODO Auto-generated method stub
		return highlightTags;
	}

	public boolean isCreateTitlePage() {
		return createTitlePage;
	}

	public ExportPathComposite getExportPathComp() {
		return exportPathComp;
	}

	public String getFileNamePattern() {
		return fileNamePattern;
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


	
//	  /**
//	   * Gets the control for tab one
//	   * 
//	   * @param tabFolder2 the parent tab folder
//	   * @return Control
//	   */
//	  private Control getTabOneControlTest(CTabFolder tabFolder2) {
//		  
//		    //final Composite checkExport = new Composite(tabFolder2, SWT.NONE);
//		    Table checkExport = new Table(tabFolder, SWT.CHECK | SWT.BORDER);
//		    //checkExport.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
//		    //checkExport.setLayout(new FillLayout());
//		    final TableItem ti1 = new TableItem(checkExport, SWT.CHECK);
//		    final TableItem ti2 = new TableItem(checkExport, SWT.CHECK);
//		    
//		    final Button e1 = new Button(checkExport, SWT.CHECK);
//		    final Button e2 = new Button(checkExport, SWT.CHECK);
//		    
//		    e1.setSelection(true);
//		    setPageExport(true);
//		    
//		    ti1.setText("Export Page");
//		    ti2.setText("Export ALTO");
//		    
//		   ti1.setGrayed(true);
//		    
//		    TableEditor editor=new TableEditor(checkExport);
//		    editor.setEditor(e1, ti1, 0);
//		    editor.setEditor(e2, ti2, 1);
//		    
//		    e1.addSelectionListener(new SelectionAdapter() {
//
//		        @Override
//		        public void widgetSelected(SelectionEvent event) {
//		            Button btn = (Button) event.getSource();
//		            if (btn.getSelection()){
//		            	setPageExport(true);
//		            }
//		            else{
//		            	//one export should always be selected - so if no alto export the page export cannot be deselected
//		            	if (!e2.getSelection()){
//		            		e1.setSelection(true);
//		            	}
//		            	else{
//		            		setPageExport(false);
//		            	}
//		            }	
//		        }
//		    });
//		    
//		    e2.addSelectionListener(new SelectionAdapter() {
//		        @Override
//		        public void widgetSelected(SelectionEvent event) {
//		            Button btn = (Button) event.getSource();
//		            if (btn.getSelection()){
//		            	setAltoExport(true);
//		            }
//		            else{
//		            	setAltoExport(false);
//		            	e1.setSelection(true);
//		            }
//		        }
//		    });
//		    
//		    return checkExport;
//	  }
	
}
