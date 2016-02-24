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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.builder.ExportUtils;
import eu.transkribus.core.model.builder.tei.TeiExportMode;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.SWTUtil;
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
	Button blackeningBtn;
	Button exportTagsBtn;
	Button createTitlePageBtn;
	boolean wordBased=false;
	boolean tagExport=false;
	boolean doBlackening = false;
	boolean createTitlePage = false;
	
	Button noZonesRadio;
	Button zonePerParRadio;
	Button zonePerLineRadio;
	Button zonePerWordRadio;
	
	TeiExportMode teiExportMode;
	boolean docxExport, pdfExport, teiExport, altoExport, metsExport, pageExport, xlsxExport;
	String fileNamePattern = "${filename}";
	Button addExtraTextPagesBtn;
	boolean addExtraTextPages2PDF;
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
	    group1.setText("Choose export formats?");
	    group1.setLayout(new GridLayout(1, false));
	    Button b0 = new Button(group1, SWT.CHECK);
	    b0.setText("IMAGE, PAGE, METS");
	    Button b1 = new Button(group1, SWT.CHECK); 
	    b1.setText("PDF");
	    Button b2 = new Button(group1, SWT.CHECK);
	    b2.setText("TEI");
	    Button b3 = new Button(group1, SWT.CHECK);
	    b3.setText("DOCX");
	    Button b4 = new Button(group1, SWT.CHECK);
	    b4.setText("Tag Export (Excel)");
	    
	    
	    Composite optionsComposite = new Composite(choiceComposite, SWT.NONE);
	    optionsComposite.setLayout(new GridLayout(1, false));
	    GridData gridData = new GridData();
	    //gridData.heightHint = 0;
	    gridData.horizontalAlignment = SWT.RIGHT;
	    gridData.verticalAlignment = SWT.FILL;
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
	    
		tagsSelector = new TagsSelector(optionsComposite, SWT.NONE, getSelectedTagsList());
		tagsSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tagsSelector.setVisible(false);
	
	    
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
	    
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		buttonComposite.setLayout(new FillLayout());
		
		Button exportButton = new Button(buttonComposite, SWT.NONE);
		exportButton.setText("OK");
		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateTeiExportMode();
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
			
			e1.setSelection(true);
			setPageExport(true);
			
			e1.setText("Export Page");
			e2.setText("Export ALTO");
			e3.setText("Standardized Filenames");
			
			e1.addSelectionListener(new SelectionAdapter() {
			
			    @Override
			    public void widgetSelected(SelectionEvent event) {
			        Button btn = (Button) event.getSource();
			        if (btn.getSelection()){
			        	setPageExport(true);
			        }
			        else{
			        	//one export should always be selected - so if no alto export the page export cannot be deselected
			        	if (!e2.getSelection()){
			        		e1.setSelection(true);
			        	}
			        	else{
			        		setPageExport(false);
			        	}
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
			        	e1.setSelection(true);
			        }
			    }
			});
			
			e3.addSelectionListener(new SelectionAdapter() {
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
		
	    addExtraTextPagesBtn = new Button(pdfComposite, SWT.CHECK);
	    addExtraTextPagesBtn.setText("PDF: extra text pages");
	    addExtraTextPagesBtn.setToolTipText("The transcribed text will be added to the PDF as extra page after each image");
	    addExtraTextPagesBtn.setSelection(true);
	    setAddExtraTextPages2PDF(true);
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
		
		noZonesRadio = new Button(teiComposite, SWT.RADIO);
		noZonesRadio.setText("No zones");
		noZonesRadio.setToolTipText("Create no zones, just paragraphs");
		
		zonePerParRadio = new Button(teiComposite, SWT.RADIO);
		zonePerParRadio.setText("Zone per region");
		zonePerParRadio.setToolTipText("Create a zone element for each region");
//		zonePerParRadio.setSelection(true);
		
		zonePerLineRadio = new Button(teiComposite, SWT.RADIO);
		zonePerLineRadio.setToolTipText("Create a zone element for each region and line");
		zonePerLineRadio.setText("Zone per line");
		zonePerLineRadio.setSelection(true);
		
		zonePerWordRadio = new Button(teiComposite, SWT.RADIO);
		zonePerWordRadio.setToolTipText("Create a zone element for each region, line and word");
		zonePerWordRadio.setText("Zone per word");

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
		exportTagsBtn.setText("Export Tags");
		exportTagsBtn.setToolTipText("If checked, all tags will be listed at the end of the export doc");
		exportTagsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));

		exportTagsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				setTagExport(exportTagsBtn.getSelection());
			}
		});
		
	    return docxComposite;
	}
	  
	
	public Set<Integer> getSelectedPages() {
		return selectedPages;
	}
	
	private void showPageChoice() {
		docPagesSelector.setVisible(isPageableExport());
	}
	
	private void showTagChoice() {
		tagsSelector.setVisible(isTagableExport());
	}
	
	public boolean isPageableExport() {
		return isMetsExport() || isPdfExport() || isDocxExport() || isXlsxExport() || isTeiExport();
	}
	
	public boolean isTagableExport() {
		return isPdfExport() || isDocxExport() || isXlsxExport() || isTeiExport();
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
	
	private void updatePages() {
//		startPage = startSpinner.getSelection();
//		endPage = endSpinner.getSelection();
		//logger.debug("pages " + startPage + "-" + endPage);
	}
	
	public TeiExportMode getTeiExportMode(){
		return teiExportMode;
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
