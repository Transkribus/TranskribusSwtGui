package eu.transkribus.swt_gui.upload;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.FtpConsts;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.core.util.AuthUtils;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.mytableviewer.MyTableViewer;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DateTableColumnViewerSorter;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.DesktopUtil;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.collection_comboviewer.CollectionSelectorWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class UploadDialogUltimate extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(UploadDialogUltimate.class);
	
	private final static String INFO_MSG = 
			"You can upload folders containing image files to:\n\n"
			+ FtpConsts.FTP_PROT + FtpConsts.FTP_URL + "\n\n"
			+ "by using your favorite FTP client.\n"
			+ "For accessing the FTP server please use your\n"
			+ "Transkribus credentials.\n"
			+ "After the upload is done, you can ingest the\n"
			+ "documents into the platform by selecting the\n"
			+ "respective folders and the collection, to which\n"
			+ "the documents should be linked, within this Dialog.";
	
	
	Button singleDocButton, ftpButton, metsUrlButton, pdfButton;
	Group ftpGroup, singleGroup, metsUrlGroup, pdfGroup;
	
	Text folderText; //, pdfFolderText;
	Text titleText, urlText;
	Text fileText;
	
//	String dirName;
	String file, folder, /*pdffolder, */ title, url;
	
	boolean isSingleDocUpload=true, isMetsUrlUpload=false, isPdfUpload=false;
	
//	TrpDoc doc;
	
	Link link;
	private Table docDirTable;
	private static MyTableViewer docDirTv;
	List<TrpDocDir> docDirs = new ArrayList<>(0);
	
	private List<TrpDocDir> selDocDirs;
	Button reloadBtn;
	Button helpBtn;
	
	Storage store = Storage.getInstance();
	
	TrpCollection selColl;
	
//	Text newCollText;
//	Button addCollBtn;
//	Combo collCombo;
	
	CollectionSelectorWidget collSelector;
	
	Composite container;
	
	
	TrpMainWidget mw = TrpMainWidget.getInstance();
	Label spacerLabel;
	
	boolean isLoadingFtpDocDirs = false;

	public static final String DIRECTORY_COL = "Directory";
	public static final String TITLE_COL = "Title";
	public static final String NR_OF_FILES_COL = "Nr. of Files";
	public static final String CREATE_DATE_COL = "Last modified";
	public static final File USER_TMP_DIR = new File(System.getProperty("java.io.tmpdir"), "TrpPDFimgs");
	
	public static final ColumnConfig[] DOC_DIR_COLS = new ColumnConfig[] {
		new ColumnConfig(DIRECTORY_COL, 180, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TITLE_COL, 110, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(NR_OF_FILES_COL, 110, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CREATE_DATE_COL, 150, false, DateTableColumnViewerSorter.ASC, new DateTableColumnViewerSorter(docDirTv,null)),
	};
	
	public UploadDialogUltimate(Shell parentShell, TrpCollection selColl) {
		super(parentShell);
		this.selColl = selColl;
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout)container.getLayout();
		gridLayout.numColumns = 3;
//		GridData gridData = (GridData)container.getLayoutData();
//		gridData.widthHint = 600;
//		gridData.heightHint = 500;
//		gridData.minimumWidth = 600;
//		gridData.minimumHeight = 500;
//		container.setSize(700, 600);
		
		ftpButton = new Button(container, SWT.RADIO);
		ftpButton.setText("Upload via private FTP");
		//ftpButton.setSelection(true);
		ftpButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		singleDocButton = new Button(container, SWT.RADIO);
		singleDocButton.setText("Upload single document");
		singleDocButton.setSelection(true);
		singleDocButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		metsUrlButton = new Button(container, SWT.RADIO);
		metsUrlButton.setText("Upload via URL of DFG Viewer METS");
		metsUrlButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		
		pdfButton = new Button(container, SWT.RADIO);
		pdfButton.setText("Extract and upload images from pdf");
		pdfButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		spacerLabel = new Label(container, 0); // spacer
		
		ftpGroup = new Group(container, 0);
		ftpGroup.setText("Documents on private FTP");
		ftpGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		ftpGroup.setLayout(new GridLayout(3, false));
		createFtpGroup(ftpGroup);
		
		singleGroup = new Group(SWTUtil.dummyShell, 0);
		singleGroup.setText("Single document upload");
		singleGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		singleGroup.setLayout(new GridLayout(3, false));
		createSingleGroup(singleGroup);
		
		metsUrlGroup = new Group(SWTUtil.dummyShell, 0);
		metsUrlGroup.setText("Document upload via METS");
		metsUrlGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		metsUrlGroup.setLayout(new GridLayout(3, false));
		createMetsUrlGroup(metsUrlGroup);
		
		pdfGroup = new Group(SWTUtil.dummyShell, 0);
		pdfGroup.setText("Extract images from pdf (locally) and upload");
		pdfGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		pdfGroup.setLayout(new GridLayout(3, false));
		createPdfGroup(pdfGroup);
		
		Label lblCollections = new Label(container, SWT.NONE);
		lblCollections.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCollections.setText("Add to collection:");
		Fonts.setBoldFont(lblCollections);
		
		Predicate<TrpCollection> collSelectorPredicate = (c) -> { return c!=null && AuthUtils.canManage(c.getRole()); }; // show only collections where user can upload to!
		collSelector = new CollectionSelectorWidget(container, 0, false, collSelectorPredicate );
		collSelector.setToolTipText("This is the collection the document will be added to - you can only upload to collections where you are at least editor");
		collSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		// set to current collection if possible:
		mw = TrpMainWidget.getInstance();
		if (mw != null) {
			TrpCollection c = mw.getSelectedCollection();
			if (collSelectorPredicate.test(c)) {
				collSelector.setSelectedCollection(c);
			}
		} 
//		collCombo = new Combo(container, SWT.READ_ONLY);
//		collCombo.setToolTipText("This is the collection the document will be added to - you can only upload to collections where you are at least editor");
//		collCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		new Label(container, SWT.NONE);
		
//		Label lblCreateCollection = new Label(container, SWT.NONE);
//		lblCreateCollection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblCreateCollection.setText("Create collection:");
//		
//		newCollText = new Text(container, SWT.BORDER);
//		newCollText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		newCollText.setToolTipText("The title of the new collection");
//		
//		addCollBtn = new Button(container, SWT.NONE);
//		addCollBtn.setImage(Images.getOrLoad("/icons/add.png"));
//		addCollBtn.setToolTipText("Creates a new collection with the name on the left - you will be the owner of the collection");
				
		updateDocDirs();
//		updateCollections();
		addListener();
		
		this.container = container;
		updateGroupVisibility();
		
		return container;
	}
	
	
	
	private void createSingleGroup(Composite container) {
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Local folder:");

		folderText = new Text(container, SWT.BORDER);
		folderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if (store.isLocalDoc()) {
			folderText.setText(store.getDoc().getMd().getLocalFolder().getAbsolutePath());
		}
		Button setFolderBtn = new Button(container, SWT.NONE);
		setFolderBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				folder = DialogUtil.showOpenFolderDialog(getShell(), "Specify a folder to upload", folder);
				if (folder != null) {
					folderText.setText(folder);
				}
			}
		});
		setFolderBtn.setImage(Images.getOrLoad("/icons/folder.png"));

		Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title on server:");

		if(titleText == null)
			titleText = new Text(container, SWT.BORDER);
		titleText.setToolTipText("The title of the uploaded document - leave blank to generate a default title");
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
	}

	private void createPdfGroup(Composite container) {
		
		Label lblFileLabel = new Label(container, SWT.NONE);
		lblFileLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblFileLabel.setText("Local pdf file:");

		fileText = new Text(container, SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if (store.isLocalDoc())
			fileText.setText(store.getDoc().getMd().getLocalFolder().getAbsolutePath());

		Button setFileBtn = new Button(container, SWT.NONE);
		setFileBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				file = DialogUtil.showOpenFileDialog(getShell(), 
						"Select pdf file containing images for upload", file, new String[]{"*.pdf", "*.PDF", "*.*"});
				if (file != null) {
					fileText.setText(file);
				}
			}
		});
		setFileBtn.setImage(Images.getOrLoad("/icons/folder_explore.png"));
		
		Text lblInfo = new Text(container, SWT.NONE);
		
		lblInfo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		//lblExtractFolder.text.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblInfo.setText("Extracted images can be found at "+ USER_TMP_DIR.getAbsolutePath());
		lblInfo.setEnabled(false);
	}
	
	private void createMetsUrlGroup(Composite container) {
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("METS URL:");

		urlText = new Text(container, SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lblTitle = new Label(container, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title on server:");

		if(titleText == null)
			titleText = new Text(container, SWT.BORDER);
		titleText.setToolTipText("The title of the uploaded document - leave blank to generate a default title");
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(container, SWT.NONE);
		
	}
	
	private String getUsernameEncoded() {
		if (Storage.getInstance().isLoggedIn()) {
			String un = Storage.getInstance().getUser().getUserName();
			try {
				un = URLEncoder.encode(un, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Could not UTF-8 encode username for URL", e);
				//in most cases only "@" has to be encoded
				un = un.replace("@", "%40");
			}
			return un;
		} else {
			return "Not-logged-in";
		}
	}
	
	private void createFtpGroup(Composite container) {
		
		Label lblDir = new Label(container, SWT.NONE);
		lblDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDir.setText("Location:");
		link = new Link(container, SWT.NONE);
	    final String linkText = "<a href=\"" + FtpConsts.FTP_PROT + getUsernameEncoded() + "@" + FtpConsts.FTP_URL + "\">"+ FtpConsts.FTP_PROT + FtpConsts.FTP_URL + "</a>";
	    link.setText(linkText);
	    helpBtn = new Button(container, SWT.NONE);
		helpBtn.setImage(Images.getOrLoad("/icons/help.png"));
		helpBtn.setToolTipText("What's that?");
		
//		Label lblDir = new Label(container, SWT.NONE);
//		lblDir.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
//		lblDir.setText("Directory:");
		docDirTv = new MyTableViewer(container, SWT.MULTI | SWT.FULL_SELECTION);
		docDirTv.setContentProvider(new ArrayContentProvider());
		docDirTv.setLabelProvider(new DocDirTableLabelProvider(docDirTv));
		
		docDirTable = docDirTv.getTable();
		docDirTable.setHeaderVisible(true);
		docDirTable.setLinesVisible(true);
		docDirTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		docDirTv.addColumns(DOC_DIR_COLS);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		reloadBtn = new Button(container, SWT.NONE);
		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));
		reloadBtn.setToolTipText("Reload the directories from the FTP server");
		
	}
	
	private void updateGroupVisibility() {
		
		Composite remove = null;
		Composite add = null;
		
		if (singleDocButton.getSelection()) {
			add = singleGroup;
			ftpGroup.setParent(SWTUtil.dummyShell);
			metsUrlGroup.setParent(SWTUtil.dummyShell);
			pdfGroup.setParent(SWTUtil.dummyShell);
		} else if (ftpButton.getSelection()){
			add = ftpGroup;
			singleGroup.setParent(SWTUtil.dummyShell);
			metsUrlGroup.setParent(SWTUtil.dummyShell);
			pdfGroup.setParent(SWTUtil.dummyShell);
			//remove = singleGroup;
		} else if (pdfButton.getSelection()) {
			// new case: pdf upload
			add = pdfGroup;
			ftpGroup.setParent(SWTUtil.dummyShell);
			singleGroup.setParent(SWTUtil.dummyShell);
			metsUrlGroup.setParent(SWTUtil.dummyShell);
		}
		//new case: upload via url 
		else{
			add = metsUrlGroup;
			singleGroup.setParent(SWTUtil.dummyShell);
			ftpGroup.setParent(SWTUtil.dummyShell);
			pdfGroup.setParent(SWTUtil.dummyShell);
		}
		logger.trace("is disposed: "+add.isDisposed());
		
		//remove.setParent(SWTUtil.dummyShell);
		add.setParent(container);
		add.moveBelow(spacerLabel);
	
		add.layout();
		container.layout();
		getShell().layout();
//		getShell().redraw();
	}
		
	private void addListener() {
		
		singleDocButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateGroupVisibility();
			}
		});
		
		pdfButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateGroupVisibility();
			}
		});
		
		ftpButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateGroupVisibility();
			}
		});	
		
		metsUrlButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateGroupVisibility();
			}
		});
		
//		store.addListener(new IStorageListener() {
//			@Override public void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
//				if (getShell() != null && !getShell().isDisposed())
//					updateCollections();
//			}
//		});
		
		link.addSelectionListener(new SelectionAdapter(){
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	        	final String uriStr = e.text;
	            DesktopUtil.browse(uriStr, INFO_MSG, getShell());
	        }
	    });
		
		collSelector.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(Event event) {
				selColl = collSelector.getSelectedCollection();
			}
		});
		
//		collCombo.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
////				logger.debug("selected!");
//				List<TrpCollection> ccm = store.getCollectionsCanManage();
//				int i = collCombo.getSelectionIndex();
//				if (i >= 0 && i < ccm.size()) {
//					selColl = ccm.get(i);
//				}
//				updateBtnVisibility();
//			}
//		});
		
//		addCollBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				if (store.isLoggedIn() && !newCollText.getText().isEmpty()) {
//					try {
//						store.getConnection().createCollection(newCollText.getText());
//						store.reloadCollections();
//					} catch (Exception e1) {
//						mw.onError("Could not create new collection", e1.getMessage(), e1);
//					}
//				}
//				
//				List<TrpCollection> ccm = store.getCollectionsCanManage();
//				int i = collCombo.getSelectionIndex();
//				if (i >= 0 && i < ccm.size()) {
//					selColl = ccm.get(i);
//				}
//				updateBtnVisibility();
//			}
//		});
		
		reloadBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				updateDocDirs();
			}
		});
		
		helpBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				DialogUtil.showInfoMessageBox(getParentShell(), "Information", INFO_MSG);
			}
		});
	}
	
	@Override protected Control createButtonBar(Composite parent) {
		Control ctrl = super.createButtonBar(parent);
		
		updateBtnVisibility();
		return ctrl;
	}
	
	private void updateBtnVisibility() {
		if (getButton(IDialogConstants.OK_ID) != null)
			getButton(IDialogConstants.OK_ID).setEnabled(collSelector.getSelectedCollection()!=null);
	}
	
//	private TrpCollection getSelectedCollection() {
//		List<TrpCollection> ccm = store.getCollectionsCanManage();
//		int i = collCombo.getSelectionIndex();
//		if (i < 0 || i >= ccm.size())
//			return null;
//		else
//			return ccm.get(collCombo.getSelectionIndex());
//	}
	
	private void updateDocDirs() {
		// loading doc dirs async:
		if (!isLoadingFtpDocDirs) {
			isLoadingFtpDocDirs = true;
			docDirTv.getTable().setEnabled(false);
			new Thread() {
				@Override public void run() {
					try {
						docDirs = store.listDocDirsOnFtp();
						logger.debug("loaded ftp doc dir: "+docDirs.size());
					} catch (Exception e) {
						docDirs = new ArrayList<>();
						Display.getDefault().asyncExec(() -> {
							if (mw != null)
								mw.onError("Error", "Could not load directory list!", e);
							else 
								logger.error("Could not load directory list " + e.getMessage());
						});
					} finally {
						Display.getDefault().asyncExec(() -> {
							docDirTv.getTable().setEnabled(true);
							docDirTv.setInput(docDirs);
						});
						isLoadingFtpDocDirs = false;
					}
				}
			}.start();
		}
		
		// OLD CODE: SYNC LOADING -> opening dialog takes very long for many FTP dirs
//		try {
//			docDirs = store.listDocDirsOnFtp();
//		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
//				| NoConnectionException e) {
//			mw.onError("Error", "Could not load directory list!", e);
//		}
//		docDirTv.setInput(docDirs);
	}
	
	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Document ingest / upload");
	}

	@Override protected Point getInitialSize() {
		return new Point(700, 500);
	}

	// override method to use "Upload" as label for the OK button
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Upload", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		saveInput();
		
		if (isSingleDocUpload && StringUtils.isEmpty(folder) ) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "Please specify a valid input folder!");
		}
		else if(isMetsUrlUpload && StringUtils.isEmpty(url)){
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "Please copy a valid url into the text field!");
		}
		else if(!isSingleDocUpload && !isMetsUrlUpload && !isPdfUpload) {
			// this is Upload from private FTP
			if(selDocDirs == null || selDocDirs.isEmpty()) {
				DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select directories for ingesting.");
			} else {	
				ProgressBarDialog pbd = new ProgressBarDialog(getParentShell());
				
				IRunnableWithProgress r = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						monitor.beginTask("Checking remote files...", selDocDirs.size());
						//check selected Dirs on server
						int i = 0;
						for(TrpDocDir d : selDocDirs) {
							monitor.setTaskName("Checking dir: " + d.getName());
							try {
								if(!store.checkDocumentOnPrivateFtp(d.getName())) {
									Runnable r = new Runnable() {
										@Override
										public void run() {
											DialogUtil.showErrorMessageBox(mw.getShell(), "Error", 
													"The selected directory \"" + d.getName() + "\" is not a valid Transkribus document!");
										}
									};
									Display.getDefault().syncExec(r);
									monitor.done();
									return;
								}
								monitor.worked(++i);
							} catch (Exception e) {
								logger.error("Error in Runnable", e);
								Runnable r = new Runnable() {
									@Override
									public void run() {
										DialogUtil.showErrorMessageBox(new Shell(Display.getDefault()), "Error", e.getMessage());
									}
								};
								Display.getDefault().syncExec(r);
							}
						}
						logger.debug("Finished checking directories on server.");
					}
				};
				
				try {
					pbd.open(r, "Checking remote directories...", true);
					logger.debug("OK pressed.");
					super.okPressed();
				} catch (Throwable e) {
					DialogUtil.showErrorMessageBox(getParentShell(), "Error", e.getMessage());
					logger.error("Error in ProgressMonitorDialog", e);
				}
			}
		} 
//		else if (!isSingleDocUpload) {
//			// check if name already exists!
//			for (TrpDocDir d : selDocDirs) { // check if path already exists
//				String title = d.getMetadata()==null ? d.getName() : d.getMetadata().getTitle();
//
//			}
////			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select directories for ingesting.");
//		}
		else if (isPdfUpload && StringUtils.isEmpty(file)) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You need to select a pdf first");
		
		} else {
			super.okPressed();
		}
	}

	private void saveInput() {
		this.isSingleDocUpload = singleDocButton.getSelection();
		this.isMetsUrlUpload = metsUrlButton.getSelection();
		this.isPdfUpload = pdfButton.getSelection();
		
		this.selDocDirs = getSelectedDocDirs();
		this.selColl = collSelector.getSelectedCollection();
		
		this.folder = folderText.getText();
//		this.pdffolder = pdfFolderText.getText();
		this.url = urlText.getText();
		this.file = fileText.getText();
		
		this.title = titleText.getText();
	}
	
	public TrpCollection getCollection() {
		return selColl;
	}
	
	public List<TrpDocDir> getDocDirs(){
		return selDocDirs;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getFolder() {
		return folder;
	}
	
	public String getPdfFolder() {
		return USER_TMP_DIR.getAbsolutePath();
	}
	
	public String getFile() {
		return file;
	}
	
	public boolean isSingleDocUpload() {
		return isSingleDocUpload;
	}
	
	public boolean isUploadFromPdf() {
		return isPdfUpload;
	}
		
	public List<TrpDocDir> getSelectedDocDirs(){
		IStructuredSelection sel = (IStructuredSelection) docDirTv.getSelection();
		List<TrpDocDir> list = new LinkedList<>();
		Iterator<Object> it = sel.iterator();
		while(it.hasNext()){
			final TrpDocDir docDir = (TrpDocDir)it.next();
			logger.debug("Selected dir: " + docDir.getName());
			list.add(docDir);
		}
		return list;	
	}
	
	public boolean isMetsUrlUpload() {
		return isMetsUrlUpload;
	}

	public String getMetsUrl() {
		return url;
	}
	
//	public static void main(String[] args) throws Exception {
//		ApplicationWindow aw = new ApplicationWindow(null) {
//			@Override
//			protected Control createContents(Composite parent) {
//				// getShell().setLayout(new FillLayout());
//				getShell().setSize(300, 200);
//				Button btn = new Button(parent, SWT.PUSH);
//				btn.setText("Open upload dialog");
//				btn.addSelectionListener(new SelectionAdapter() {
//					@Override public void widgetSelected(SelectionEvent e) {
//						(new UploadDialogUltimate(getShell(), null)).open();
//					}
//				});
//
//				SWTUtil.centerShell(getShell());
//
//				return parent;
//			}
//		};
//		aw.setBlockOnOpen(true);
//		aw.open();
//
//		Display.getCurrent().dispose();
//	}
}
