package eu.transkribus.swt.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.ListItemRenderer;
import org.eclipse.nebula.widgets.gallery.MyDefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.NoGroupRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.exceptions.NullValueException;
import eu.transkribus.core.io.UnsupportedFormatException;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.TextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.ThumbnailManager.EditStatusMenuItemListener;
import eu.transkribus.swt.util.ThumbnailWidget.ThmbImg;
import eu.transkribus.swt.util.ThumbnailWidget.ThmbImgLoadThread;
import eu.transkribus.swt_gui.la.LayoutAnalysisDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import jersey.repackaged.com.google.common.collect.Lists;

public class ThumbnailManagerVirtual extends Dialog{
	protected final static Logger logger = LoggerFactory.getLogger(ThumbnailManagerVirtual.class);
	
	static final int TEXT_TO_THUMB_OFFSET = 5;
	
	public static final int THUMB_WIDTH = 80;
	public static final int THUMB_HEIGHT = 120;
	
	protected List<ThmbImg> thumbs = new ArrayList<>();
	
	protected ThmbImgLoadThread loadThread;
	
//	TableViewer tv;
	protected Method setItemHeightMethod;
	//protected Gallery gallery;
	
	protected Composite groupComposite;
	protected Composite labelComposite;
	
	Combo labelCombo, statusCombo;

	Composite editCombos;
	
	protected Label statisticLabel;
	protected Label pageNrLabel;
	protected Label totalTranscriptsLabel;
	protected Label totalWordTranscriptsLabel;
	
	protected GalleryItem group;
	
	protected Button reload, showOrigFn, createThumbs, startLA;
	//protected ToolItem reload, showOrigFn, createThumbs, showPageManager;
//	protected TextToolItem infoTi;

	protected List<URL> urls;
	protected List<String> names=null;
	
	protected List<TrpTranscriptMetadata> transcripts;
	
	protected List<Integer> nrTranscribedLines;
	
//	protected NoGroupRenderer groupRenderer;
	protected AbstractGridGroupRenderer groupRenderer;
	
	ThumbnailWidgetVirtualMinimal tw;
	
	static int thread_counter=0;
	
	static final Color lightGreen = new Color(Display.getCurrent(), 200, 255, 200);
	static final Color lightYellow = new Color(Display.getCurrent(), 255, 255, 200);
	static final Color lightRed = new Color(Display.getCurrent(), 252, 204, 188);
	
	private int maxWidth = 0;
	
	static GalleryItem[] draggedItem;
	static int[] originalItemIndex;
	
	private static final boolean DISABLE_TRANSCRIBED_LINES=false;
	
	Shell shell;
	public Shell getShell() {
		return shell;
	}

	TrpDocMetadata docMd;
	TrpMainWidget mw;
	
	Menu contextMenu;
		
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {

		shell.setMinimumSize(800, 800);
		shell.setSize(1100, 800);
		SWTUtil.centerShell(shell);
				
		shell.open();
		shell.layout();
		
		//take the thumbs from the widget to show in the manager
		//thumbsWidget.loadThumbsIntoManager();
		addStatisticalNumbers();
		
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return null;
	}
	
	public ThumbnailManagerVirtual(Composite parent, int style, TrpMainWidget mw) {
		super(parent.getShell(), style |= (SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MAX));
		
		this.mw = mw;
		
		if (Storage.getInstance().getDoc() == null){
			return;
		}

		if (Storage.getInstance().getDoc() != null){
			docMd = Storage.getInstance().getDoc().getMd();
		}
		
		shell = new Shell((Shell)parent, style);
		shell.setText("Document Manager");
	
		FillLayout l = new FillLayout();
		l.marginHeight = 5;
		l.marginWidth = 5;
		shell.setLayout(l);
		
		Composite container = new Composite(shell, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);
		
		groupComposite = new Composite(container, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.makeColumnsEqualWidth = true;
		groupComposite.setLayout(gl);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		groupComposite.setLayoutData(gridData);
		
		labelComposite = new Composite(groupComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));
		labelComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		statisticLabel = new Label(labelComposite, SWT.TOP);
		statisticLabel.setText("Loaded Document is " + docMd.getTitle() + " with ID " + docMd.getDocId());
		statisticLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		

		
		editCombos = new Composite(groupComposite, SWT.NONE);
		editCombos.setLayout(new GridLayout(2, true));
		editCombos.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		statusCombo = initComboWithLabel(editCombos, "Edit status: ", SWT.DROP_DOWN | SWT.READ_ONLY);
		statusCombo.setItems(EnumUtils.stringsArray(EditStatus.class));
		statusCombo.setEnabled(false);
		statusCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				logger.debug(statusCombo.getText());
		        changeVersionStatus(statusCombo.getText());
	    		reload();
	    		
	    		tw.getGallery().redraw();
			}
		});
		
//		labelCombo = initComboWithLabel(editCombos, "Edit label: ", SWT.DROP_DOWN | SWT.READ_ONLY);
//		String[] tmps = {"Upcoming feature - cannot be set at at the moment", "GT", "eLearning"};
//		labelCombo.setItems(tmps);
//		labelCombo.setEnabled(false);
		
		Label la = new Label(editCombos, SWT.CENTER);
		la.setText("Layout Analysis");
		startLA = new Button(editCombos, SWT.PUSH);
		startLA.setText("Setting up");
		startLA.setEnabled(false);
		startLA.addListener(SWT.Selection, event-> {
			String pages = getPagesString();
            try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
		
		Button showFn = new Button(editCombos, SWT.CHECK);
		showFn.setText("Show filename in label");
		showFn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		showFn.setSelection(false);
		showFn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				tw.setShowFilenames(showFn.getSelection());
				tw.reload();
				//tw.setTHUMB_WIDTH(Math.max(tw.getMaxWidth(), shell.getSize().x/3));
			}
		});
		
		Composite btns = new Composite(container, 0);
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		reload = new Button(btns, SWT.PUSH);
		reload.setToolTipText("Reload thumbs");
		reload.setImage(Images.REFRESH);
		reload.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				logger.debug("reloading thumbwidget...");
				reload();
			}
		});
		
		tw = new ThumbnailWidgetVirtualMinimal(groupComposite, true, SWT.BORDER);
		tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		//tw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		//logger.debug("doc ID is " + Storage.getInstance().getDoc().getId());
		tw.setDoc(Storage.getInstance().getDoc(), false);
		
		tw.getGallery().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				List<TrpPage> selList = tw.getSelection();
				if(!selList.isEmpty()) {	
					Event e = new Event();
					logger.debug("group size : " + tw.getGroup().getItemCount());
					logger.debug("tw.getGallery().getSelection()[0] " + tw.getGallery().getSelection()[0]);
					//e.index = tw.getGroup().indexOf(tw.getGallery().getSelection()[0]);
					
					e.index = tw.getGallery().indexOf(tw.getGallery().getSelection()[0]);
					
					logger.debug("selection index " + e.index);
					
//					if (names != null && names.size()>e.index) {
//						infoTi.setText(names.get(e.index));
//					} else 
//						infoTi.setText(""+e.index);
//					infoTi.setWidth(100);
					btns.pack();
					
					getShell().notifyListeners(SWT.Selection, e);
					
					//notifyListeners(SWT.Selection, e);
					//loadImg(selList.get(0));
				}
			}
		});
		
		tw.getGallery().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	if (tw.getGallery().getSelectionCount()>=1){
            		//set to true if edits are allowed
            		logger.debug("At least one gallery item is selected");
            		enableEdits(true);
            	}
            	else{
            		enableEdits(false);
            	}            	
            }


		});
		
		tw.getGallery().addKeyListener(new KeyAdapter() {
		    @Override
		    public void keyPressed( KeyEvent e ) {
		 
		        if( e.keyCode == 'a'
		                && ( e.stateMask & SWT.MODIFIER_MASK ) == SWT.CTRL ) {
		        	logger.debug("select all");
		            tw.getGallery().selectAll();
		            enableEdits(true);
		            
		        }
		    }
		});
						
	    contextMenu = new Menu(tw.getGallery());
	    tw.getGallery().setMenu(contextMenu);
	    //at the moment not enabled because not the total functionality to edit status, label is available
	    contextMenu.setEnabled(false);
	    
	    addMenuItems(contextMenu, EnumUtils.stringsArray(EditStatus.class));
	    
	    addPageMoveMenuItems(contextMenu);    	    
	    
//	    contextMenu.addMenuListener(new MenuAdapter()
//	    {
//	        public void menuShown(MenuEvent e)
//	        {
//	            MenuItem[] items = contextMenu.getItems();
//	            for (int i = 0; i < items.length; i++)
//	            {
//	                items[i].dispose();
//	            }
//	            MenuItem newItem = new MenuItem(contextMenu, SWT.NONE);
//	            newItem.setText("Menu for " + gallery.getSelection()[0].getText());
//	        }
//	    });
		
		shell.pack();

	}
	

	private void addPageMoveMenuItems(Menu contextMenu) {
	    MenuItem movePage = new MenuItem(contextMenu, SWT.CASCADE);
	    movePage.setText("Move page to");
	    
	    Menu subMoveMenu = new Menu(contextMenu);
	    movePage.setMenu(subMoveMenu);
	    
	    MenuItem moveFront = new MenuItem(subMoveMenu, SWT.NONE);
	    moveFront.setText("Beginning");
	    
	    MenuItem moveBack = new MenuItem(subMoveMenu, SWT.NONE);
	    moveBack.setText("End");
	    
	    MenuItem moveSpecific = new MenuItem(subMoveMenu, SWT.NONE);
	    moveSpecific.setText("Select position");
	    
	    MenuItem deletePage = new MenuItem(contextMenu, SWT.NONE);
	    deletePage.setText("Delete page");
	    
	    MenuItem addPage = new MenuItem(contextMenu, SWT.NONE);
	    addPage.setText("Add new page(s)");
	    addPage.setToolTipText("For importing several pages press 'ctrl' or 'shift' button");
	    
	    addPage.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {
				//mw.addPage();
				mw.addSeveralPages2Doc();
				try {
					Storage.getInstance().reloadCurrentDocument(tw.getDoc().getCollection().getColId());
				} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException | IOException
						| NullValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tw.setDoc(Storage.getInstance().getDoc(), false);
				reload();
				mw.getUi().getThumbnailWidget().reload();		    		
				tw.getGallery().redraw();
			}
	    	
	    });
	    
	    deletePage.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {
				int response = DialogUtil.showYesNoCancelDialog(
										mw.getShell(),
										"Delete page from server",
										"Are you sure you want to delete the selected page(s)? \nThis action cannot be undone.");
				if(response == SWT.YES){
					tw.getSelection();
					List<TrpPage> selection = tw.getSelection();
					deletePages(selection);
					try {
						Storage.getInstance().reloadCurrentDocument(tw.getDoc().getCollection().getColId());
						tw.setDoc(Storage.getInstance().getDoc(), false);
						reload();
						mw.getUi().getThumbnailWidget().reload();
					} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException | IOException
							| NullValueException e) {
						e.printStackTrace();
					}
					
				}
				
			}

	    	
	    });
	    
	    
	    moveFront.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {							
				
				List<TrpPage> selection = tw.getSelection();	
				
				try {
					int i = 0;
//					for(TrpPage page : selection){
//						movePage(page.getPageNr()+i,1);
//						i++;
//					}
					movePages(selection,1);
					Storage.getInstance().reloadCurrentDocument(tw.getDoc().getCollection().getColId());
					tw.setDoc(Storage.getInstance().getDoc(), false);
					reload();
					mw.getUi().getThumbnailWidget().reload();		    		
					tw.getGallery().redraw();
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}	   	    	
	    });   
	    
	    moveBack.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {							
				
				List<TrpPage> selection = tw.getSelection();			
				int NPages = tw.getDoc().getNPages();
				int i = 0;
				try {					
//					for(TrpPage page : selection){
//						movePage(page.getPageNr()-i, NPages);
//						i++;						
//					}
					movePages(selection, NPages);
					Storage.getInstance().reloadCurrentDocument(tw.getDoc().getCollection().getColId());
					tw.setDoc(Storage.getInstance().getDoc(), false);
					reload();
					mw.getUi().getThumbnailWidget().reload();		    		
					tw.getGallery().redraw();
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}	   	    	
	    });
	    
	    moveSpecific.addListener(SWT.Selection, new Listener(){

			@Override
			public void handleEvent(Event event) {
				Shell shell = new Shell(Display.getCurrent());
				shell.setLayout(new GridLayout(2, false));
				Text inputText = new Text(shell, SWT.BORDER);
				inputText.setText("");
				Button apply = new Button(shell, SWT.PUSH);
				apply.setText("Apply");
				shell.setLocation(Display.getCurrent().getCursorLocation());
				
				shell.pack();
				shell.open();
				
				apply.addListener(SWT.Selection, new Listener(){

					@Override
					public void handleEvent(Event event) {
						if(inputText.getText().isEmpty()) return;
						
						int targetPage;
						targetPage = Integer.parseInt(inputText.getText());
						if(targetPage > tw.getDoc().getNPages() || targetPage < 1){
							DialogUtil.showErrorMessageBox(getShell(), "Error", "Invalid position");
						}
						try {
							movePages(tw.getSelection(), targetPage);
							Storage.getInstance().reloadCurrentDocument(tw.getDoc().getCollection().getColId());
							tw.setDoc(Storage.getInstance().getDoc(), false);
							reload();
							mw.getUi().getThumbnailWidget().reload();		    		
							tw.getGallery().redraw();
						} catch (SessionExpiredException | ServerErrorException | ClientErrorException
								| IllegalArgumentException | NoConnectionException | IOException
								| NullValueException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				});
				
			}
	    	
	    });
		
	}	
	
	private void movePage(int fromPageNr, int toPageNr) {
		try {
			Storage.getInstance().movePage(tw.getDoc().getCollection().getColId(), tw.getDoc().getId(), fromPageNr, toPageNr);
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e) {
			logger.error(e.toString());
		}

	}
	
	private void deletePage(TrpPage page){
		try {
			Storage.getInstance().deletePage(tw.getDoc().getCollection().getColId(), page.getDocId(), page.getPageNr());
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | NoConnectionException e) {
			logger.error(e.toString());
		}
	}
	
	private void deletePages(List<TrpPage> selection){
		Collections.sort(selection, new Comparator<TrpPage>() {
			@Override
			public int compare(TrpPage o1, TrpPage o2) {
				return o1.getPageNr()-o2.getPageNr();
			}			
			});
		
		Collections.reverse(selection);
		for(TrpPage page : selection){
			deletePage(page);
		}
		


		
		
	}
	
	private void movePages(List<TrpPage> selection, int toPageNr) 
			throws SessionExpiredException, ServerErrorException,
					ClientErrorException, NoConnectionException,
					IllegalArgumentException, UnsupportedFormatException,
					IOException, NullValueException	{			

		
		Collections.sort(selection, new Comparator<TrpPage>() {
			@Override
			public int compare(TrpPage o1, TrpPage o2) {
				return o1.getPageNr()-o2.getPageNr();
			}			
			});

		
		if(selection.get(0).getPageNr() > toPageNr){

			int j=0;
			for(int i = selection.size()-1; i>=0; i--){
				movePage(selection.get(i).getPageNr()+j,toPageNr);
				j++;
				
			}

		}else if(selection.get(0).getPageNr() < toPageNr){
			int i = 0;
			for(TrpPage page : selection){
				movePage(page.getPageNr()-i, toPageNr);
				i++;				
			}
		}		

	}

	private void changeVersionStatus(String text){
		Storage storage = Storage.getInstance();
		String pages = getPagesString();
		
		String[] pageList = pages.split(",");
		
		if (!pages.equals("") && pageList.length >= 1){
			
			for (String page : pageList){
				int pageNr = Integer.valueOf(page);
				int colId = storage.getCurrentDocumentCollectionId();
				int docId = storage.getDocId();
				int transcriptId = 0;
				if ( (pageNr-1) >= 0){
					transcriptId = storage.getDoc().getPages().get(pageNr-1).getCurrentTranscript().getTsId();
				}
				try {
					storage.getConnection().updatePageStatus(colId, docId, pageNr, transcriptId, EditStatus.fromString(text), "");
					storage.reloadCurrentDocument(colId);
					tw.setDoc(Storage.getInstance().getDoc(), false);
					enableEdits(false);
					//logger.debug("status is changed to : " + storage.getDoc().getPages().get(pageNr-1).getCurrentTranscript().getStatus());
						
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	private static Combo initComboWithLabel(Composite parent, String label, int comboStyle) {
		
		Label l = new Label(parent, SWT.LEFT);
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		l.setText(label);
		
		Combo combo = new Combo(parent, comboStyle);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
				
		return combo;
	}
	
	protected void setup_layout_recognition(String pages) throws SessionExpiredException, ServerErrorException, ClientErrorException, IllegalArgumentException, NoConnectionException {
		LayoutAnalysisDialog laD = new LayoutAnalysisDialog(shell);
		
		laD.create();
		//all selected pages are shown as default and are taken for segmentation
		laD.setPageSelectionToSelectedPages(pages);
		
		int ret = laD.open();

		if (ret == IDialogConstants.OK_ID) {			
			try {
				List<String> jobIds = Storage.getInstance().analyzeLayoutOnLatestTranscriptOfPages(laD.getPages(),
						laD.isDoBlockSeg(), laD.isDoLineSeg(), laD.isDoWordSeg(), false, false, laD.getJobImpl(), null);
				
				if (jobIds != null && mw != null) {
					logger.debug("started jobs: "+jobIds.size());
					String jobIdsStr = mw.registerJobsToUpdate(jobIds);				
					Storage.getInstance().sendJobListUpdateEvent();
					mw.updatePageLock();
					
					DialogUtil.showInfoMessageBox(getShell(), jobIds.size()+ " jobs started", jobIds.size()+ " jobs started\nIDs:\n "+jobIdsStr);
				}
			} catch (Exception e) {
				mw.onError("Error", e.getMessage(), e);
			}			
		}
	}

	private void addMenuItems(Menu contextMenu, String[] editStatusArray) {
		MenuItem tmp;
		
		Menu statusMenu = new Menu(contextMenu);
		MenuItem statusMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		statusMenuItem.setText("Edit Status");
		statusMenuItem.setMenu(statusMenu);
		//statusMenuItem.setEnabled(false);
		
		for (String editStatus : editStatusArray){
			tmp = new MenuItem(statusMenu, SWT.PUSH);
			tmp.setText(editStatus);
			tmp.addSelectionListener(new EditStatusMenuItemListener());
			//tmp.setEnabled(true);
		}
		
//		Menu labelMenu = new Menu(contextMenu);
//		MenuItem labelMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
//		labelMenuItem.setText("Edit Label");
//		labelMenuItem.setMenu(labelMenu);
		
		Menu layoutMenu = new Menu(contextMenu);
		MenuItem layoutMenuItem = new MenuItem(contextMenu, SWT.CASCADE);
		layoutMenuItem.setText("Layout Analysis");
		layoutMenuItem.setMenu(layoutMenu);

		
		//just dummy labels for testing
//		tmp = new MenuItem(labelMenu, SWT.None);
//		tmp.setText("Upcoming feature - cannot be set at at the moment");
//		tmp.addSelectionListener(new EditLabelMenuItemListener());
//		tmp.setEnabled(false);
//		
//		tmp = new MenuItem(labelMenu, SWT.None);
//		tmp.setText("GT");
//		tmp.addSelectionListener(new EditLabelMenuItemListener());
//		tmp.setEnabled(false);
//		
//		tmp = new MenuItem(labelMenu, SWT.None);
//		tmp.setText("eLearning");
//		tmp.addSelectionListener(new EditLabelMenuItemListener());
//		tmp.setEnabled(false);
		
		tmp = new MenuItem(layoutMenu, SWT.None);
		tmp.setText("Setting Up");
		//tmp.setEnabled(false);
		
		tmp.addListener(SWT.Selection, event-> {
			String pages = getPagesString();
            try {
				setup_layout_recognition(pages);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });

	}
	
	private String getPagesString() {
		String pages = "";
		if (tw.getGallery().getSelectionCount() > 0) {
			for(GalleryItem si : tw.getGallery().getSelection()){
				int selectedPageNr = tw.getGallery().indexOf(si) + 1;
				logger.debug("page " + selectedPageNr);
				String tmp = Integer.toString(selectedPageNr);
				pages += (pages.equals("")? tmp : ",".concat(tmp));
			}			
		}
		return pages;
	}
	
	/*
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 */
	class EditStatusMenuItemListener extends SelectionAdapter {	
		
	    public void widgetSelected(SelectionEvent event) {
	    	System.out.println("You selected " + ((MenuItem) event.widget).getText());
	    	//System.out.println("You selected cont.1 " + EnumUtils.fromString(EditStatus.class, ((MenuItem) event.widget).getText()));

    		String tmp = ((MenuItem) event.widget).getText();
    		changeVersionStatus(tmp);
    		
    		reload();
    		mw.getUi().getThumbnailWidget().reload();
    		
    		tw.getGallery().redraw();
    		tw.getGallery().deselectAll();	    	
	    }
	}
	
	/*
	 * right click listener for the transcript table
	 * for the latest transcript the new status can be set with the right click button and by choosing the new status
	 */
//	class EditLabelMenuItemListener extends SelectionAdapter {
//	    public void widgetSelected(SelectionEvent event) {
//	    	
//	    }
//	}
		
	
	public void setUrls(List<URL> urls, List<String> names) {
		this.urls = urls;
		this.names = names;
	}
	
	public void setTranscripts(List<TrpTranscriptMetadata> transcripts2) {
		this.transcripts = transcripts2;
	}
				
	public void reload() {
		tw.reload();
		
		//addStatisticalNumbers();
	}
	
	private void addStatisticalNumbers() {
		
		Storage storage = Storage.getInstance();
		TrpDoc doc = storage.getDoc();
		
		if ( doc != null){
			if(pageNrLabel != null && !pageNrLabel.isDisposed()){
				pageNrLabel.dispose();
			}
			if(totalTranscriptsLabel != null && !totalTranscriptsLabel.isDisposed()){
				totalTranscriptsLabel.dispose();
			}
			pageNrLabel = new Label(labelComposite, SWT.NONE);
			pageNrLabel.setText("Nr of pages: " + storage.getDoc().getNPages());
			
			int totalLinesTranscribed = 0;
			int totalWordsTranscribed = 0;
			
			for (int i = 0; i < doc.getTranscripts().size(); i++){
				TrpTranscriptMetadata tmd;
				tmd = doc.getTranscripts().get(i);
				
				totalLinesTranscribed += tmd.getNrOfTranscribedLines();
				totalWordsTranscribed += tmd.getNrOfWordsInLines();
			}

			
			totalTranscriptsLabel = new Label(labelComposite, SWT.None);
			totalTranscriptsLabel.setText("Nr. of lines trancribed: " + totalLinesTranscribed);
			
			totalWordTranscriptsLabel = new Label(labelComposite, SWT.None);
			totalWordTranscriptsLabel.setText("Nr. of words trancribed: " + totalWordsTranscribed);
	
			groupComposite.layout(true, true);
			
			//gallery.redraw();
		}
		
	}
	
	private void enableEdits(boolean enable) {
		statusCombo.setEnabled(enable);
		//labelCombo.setEnabled(enable);
		startLA.setEnabled(enable);
		contextMenu.setEnabled(enable);
		
	}
	
	public Button getCreateThumbs() {
		return createThumbs;
	}

	public void addListener(int selection, Listener listener) {
		logger.debug("add double click listener");
		shell.addListener(selection, listener);	
	}

	public ThumbnailWidgetVirtualMinimal getWidget() {
		return tw;
	}

}
