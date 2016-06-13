package eu.transkribus.swt_gui.upload;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.FtpConsts;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocDir;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.mytableviewer.MyTableViewer;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.Storage.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.listener.AStorageObserver;

public class UploadDialogUltimate extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(UploadDialogUltimate.class);
	
//	private final static String ENC_USERNAME = Storage.getInstance().getUser().getUserName().replace("@", "%40");
	
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
	
	Text folderText, pdfFolderText;
	Text titleText, urlText;
	Text fileText;
	Combo uploadTypeCombo;
	
//	String dirName;
	String file, folder, title, url;
	
	boolean singleUploadViaFtp=false, isSingleDocUpload=true, isMetsUrlUpload=false, isPdfUpload=false;
	
//	TrpDoc doc;
	
	Link link;
	private Table docDirTable;
	private MyTableViewer docDirTv;
	List<TrpDocDir> docDirs = new ArrayList<>(0);
	
	TrpCollection selColl;
	private List<TrpDocDir> selDocDirs;
	Text newCollText;
	Button addCollBtn;
	Button reloadBtn;
	Button helpBtn;
	Combo collCombo;
	Storage store = Storage.getInstance();
	
	Composite container;
	
	
	TrpMainWidget mw = TrpMainWidget.getInstance();
	Label spacerLabel;

	public static final String DIRECTORY_COL = "Directory";
	public static final String TITLE_COL = "Title";
	public static final String NR_OF_IMGS_COL = "Nr. of Images";
	public static final String SIZE_COL = "Size";
	public static final String CREATE_DATE_COL = "Last modified";
	
	public static final ColumnConfig[] DOC_DIR_COLS = new ColumnConfig[] {
		new ColumnConfig(DIRECTORY_COL, 180, true, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(TITLE_COL, 110, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(NR_OF_IMGS_COL, 110, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(SIZE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(CREATE_DATE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
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
		singleDocButton.setText("Upload single document (only small docs)");
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
		lblCollections.setText("Collection:");
		
		collCombo = new Combo(container, SWT.READ_ONLY);
		collCombo.setToolTipText("This is the collection the document will be added to - you can only upload to collections with Owner / Editor rights");
		collCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		Label lblCreateCollection = new Label(container, SWT.NONE);
		lblCreateCollection.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCreateCollection.setText("Create collection:");
		
		newCollText = new Text(container, SWT.BORDER);
		newCollText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newCollText.setToolTipText("The title of the new collection");
		
		addCollBtn = new Button(container, SWT.NONE);
		addCollBtn.setImage(Images.getOrLoad("/icons/add.png"));
		addCollBtn.setToolTipText("Creates a new collection with the name on the left - you will be the owner of the collection");
		
		updateDocDirs();
		updateCollections();
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
		
		if (store.isLocalDoc())
			folderText.setText(store.getDoc().getMd().getLocalFolder().getAbsolutePath());

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

		titleText = new Text(container, SWT.BORDER);
		titleText.setToolTipText("The title of the uploaded document - leave blank to generate a default title");
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(container, SWT.NONE);
		
		Label l = new Label(container, 0);
		l.setText("Upload via: ");
		
		uploadTypeCombo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
		uploadTypeCombo.add("HTTP (default)");
		uploadTypeCombo.add("FTP");
		uploadTypeCombo.select(0);
		uploadTypeCombo.setToolTipText("The type of upload - usually HTTP should be fine but you can try FTP also if it does not work");
	}

	private void createPdfGroup(Composite container) {
		Label lblFileLabel = new Label(container, SWT.NONE);
		lblFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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

		Label lblExtractFolder = new Label(container, SWT.NONE);
		lblExtractFolder.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblExtractFolder.setText("Local folder for extracted images:");

		pdfFolderText = new Text(container, SWT.BORDER);
		pdfFolderText.setToolTipText("Name of directory to which images in pdf are extracted. "
				+ "Must have writing permits on chosen folder.");
		pdfFolderText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button setFolderBtn = new Button(container, SWT.NONE);
		setFolderBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				folder = DialogUtil.showOpenFolderDialog(getShell(), "Specify a folder to which the images are extracted", folder);
				if (folder != null) {
					pdfFolderText.setText(folder);
				}
			}
		});
		setFolderBtn.setImage(Images.getOrLoad("/icons/folder.png"));
		
		Label l = new Label(container, 0);
		l.setText("Upload via: ");
		
		uploadTypeCombo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
		uploadTypeCombo.add("HTTP (default)");
		uploadTypeCombo.add("FTP");
		uploadTypeCombo.select(0);
		uploadTypeCombo.setToolTipText("The type of upload - usually HTTP should be fine but you can try FTP also if it does not work");
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

		titleText = new Text(container, SWT.BORDER);
		titleText.setToolTipText("The title of the uploaded document - leave blank to generate a default title");
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(container, SWT.NONE);
		
	}
	
	private String getUsernameEncoded() {
		if (Storage.getInstance().isLoggedIn())
			return Storage.getInstance().getUser().getUserName().replace("@", "%40");
		else
			return "Not-logged-in";
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
		logger.info("is disposed: "+add.isDisposed());
		
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
		
		store.addObserver(new AStorageObserver() {
			@Override protected void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
				if (getShell() != null && !getShell().isDisposed())
					updateCollections();
			}
		});
		
		link.addSelectionListener(new SelectionAdapter(){
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	        	Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	                try {
	                    desktop.browse(new URI(e.text));
	                } catch (Exception ex) {
	                	//UnsupportedOperationException - if the current platform does not support the Desktop.Action.BROWSE action
	                	//IOException - if the user default browser is not found, or it fails to be launched, or the default handler application failed to be launched
	                	//SecurityException - if a security manager exists and it denies the AWTPermission("showWindowWithoutWarningBanner") permission, or the calling thread is not allowed to create a subprocess; and not invoked from within an applet or Java Web Started application
	                	//IllegalArgumentException - if the necessary permissions are not available and the URI can not be converted to a URL
	                	logger.error("Could not open ftp client!");
	                	
	                	DialogUtil.showMessageBox(getShell(), "Could not find FTP client", INFO_MSG, SWT.NONE);
	                }
	            }
	        	
//	        	try {
//	        		//  Open default external browser 
//	        		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(e.text));
//	        	} catch (PartInitException ex) {
//	        		// TODO Auto-generated catch block
//	        		ex.printStackTrace();
//	            } catch (MalformedURLException ex) {
//	            	// TODO Auto-generated catch block
//	            	ex.printStackTrace();
//	            }
	        }
	    });
		
		collCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
//				logger.debug("selected!");
				List<TrpCollection> ccm = store.getCollectionsCanManage();
				int i = collCombo.getSelectionIndex();
				if (i >= 0 && i < ccm.size()) {
					selColl = ccm.get(i);
				}
				updateBtnVisibility();
			}
		});
		
		addCollBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (store.isLoggedIn() && !newCollText.getText().isEmpty()) {
					try {
						store.getConnection().createCollection(newCollText.getText());
						store.reloadCollections();
					} catch (Exception e1) {
						mw.onError("Could not create new collection", e1.getMessage(), e1);
					}
				}
				
				List<TrpCollection> ccm = store.getCollectionsCanManage();
				int i = collCombo.getSelectionIndex();
				if (i >= 0 && i < ccm.size()) {
					selColl = ccm.get(i);
				}
				updateBtnVisibility();
			}
		});
		
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
			getButton(IDialogConstants.OK_ID).setEnabled(collCombo.getSelectionIndex() != -1);
	}
	
	private TrpCollection getSelectedCollection() {
		List<TrpCollection> ccm = store.getCollectionsCanManage();
		int i = collCombo.getSelectionIndex();
		if (i < 0 || i >= ccm.size())
			return null;
		else
			return ccm.get(collCombo.getSelectionIndex());
	}
	
	private void updateDocDirs() {
		try {
			docDirs = store.listDocDirsOnFtp();
		} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException
				| NoConnectionException e) {
			mw.onError("Error", "Could not load directory list!", e);
		}
		docDirTv.setInput(docDirs);
	}
	
	private void updateCollections() {
		collCombo.removeAll();
		List<TrpCollection> ccm = store.getCollectionsCanManage();
		
		List<String> elements = new ArrayList<>();
		
		int selCollId = selColl == null ? 0 : selColl.getColId();
		int selItemInd = 0;
		for(int i = 0; i < ccm.size(); i++){
			final TrpCollection c = ccm.get(i);
			logger.trace("collection name: "+c.getColName()+ " i = "+i);
			elements.add(c.getColName());
//			collCombo.add(c.getColName());
			if (c.getColId() == selCollId)
				selItemInd = i;
		}
		
		collCombo.setItems(elements.toArray(new String[0]));
		collCombo.select(selItemInd);
		
		
		if (collCombo.getItemCount() > 0 && collCombo.getSelectionIndex() == -1)
			collCombo.select(0);
		
		updateBtnVisibility();
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

	// override method to use "Login" as label for the OK button
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
		else if((!isSingleDocUpload && !isMetsUrlUpload && !isPdfUpload) && (selDocDirs == null || selDocDirs.isEmpty())) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "You have to select directories for ingesting.");
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
		} else if (isPdfUpload && StringUtils.isEmpty(folder)) {
			DialogUtil.showErrorMessageBox(getParentShell(), "Info", "Please specify a folder to "
					+ "which you want to extract the images in your pdf");
		}
		else {
			super.okPressed();
		}
	}

	private void saveInput() {
		this.isSingleDocUpload = singleDocButton.getSelection();
		this.isMetsUrlUpload = metsUrlButton.getSelection();
		this.isPdfUpload = pdfButton.getSelection();
		
		this.selDocDirs = getSelectedDocDirs();
		this.selColl =  getSelectedCollection();
		
		this.folder = folderText.getText();
		this.url = urlText.getText();
		this.file = fileText.getText();
		
		this.title = titleText.getText();
		this.singleUploadViaFtp = uploadTypeCombo.getSelectionIndex()==1;
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
	
	public String getFile() {
		return file;
	}
	
	public boolean isSingleUploadViaFtp() {
		return singleUploadViaFtp;
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
	
	public static void main(String[] args) throws Exception {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(300, 200);
				Button btn = new Button(parent, SWT.PUSH);
				btn.setText("Open upload dialog");
				btn.addSelectionListener(new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent e) {
						(new UploadDialogUltimate(getShell(), null)).open();
					}
				});

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
	}

	public boolean isMetsUrlUpload() {
		return isMetsUrlUpload;
	}

	public String getMetsUrl() {
		return url;
	}
}
