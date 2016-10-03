package eu.transkribus.swt_gui.upload;

import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.io.TrpDocUploadZipHttp;
import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.Storage.CollectionsLoadEvent;
import eu.transkribus.swt_gui.mainwidget.listener.AStorageObserver;

public class UploadDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(UploadDialog.class);
	
	Text folderText;
	Text titleText;
	String folder;
	String title;
//	TrpDoc doc;
	TrpCollection selColl;
	Text newCollText;
	Button addCollBtn;
	Combo collCombo;
	Combo uploadTypeCombo;
	Storage store = Storage.getInstance();
	TrpMainWidget mw = TrpMainWidget.getInstance();
	
	boolean viaFtp=false;
	
	public UploadDialog(Shell parentShell, TrpCollection selColl) {
		super(parentShell);
//		this.doc = trpDoc;
		this.selColl = selColl;
		
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
	}
	
	@Override protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 3;

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
		
		Label l = new Label(container, 0);
		l.setText("Upload via: ");
		
		uploadTypeCombo = new Combo(container, SWT.READ_ONLY | SWT.DROP_DOWN);
		uploadTypeCombo.add("HTTP (default)");
		uploadTypeCombo.add("FTP");
		uploadTypeCombo.select(0);
		uploadTypeCombo.setToolTipText("The type of upload - usually HTTP should be fine but you can try FTP also if it does not work");
		
		new Label(container, 0); // spacer
		
		Label maxSizeInfoLabel = new Label(container, 0);
		maxSizeInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		maxSizeInfoLabel.setText("Note: for the HTTP upload the zip file can have at most "+TrpDocUploadZipHttp.MAX_UPLOAD_SIZE_MB+" MB.\nIf your document is larger use document ingest via your private FTP!" );
		
		updateCollections();
		addListener();
		
		return container;
	}
		
	private void addListener() {
		store.addObserver(new AStorageObserver() {
			@Override protected void handleCollectionsLoadEvent(CollectionsLoadEvent cle) {
				if (getShell() != null && !getShell().isDisposed())
					updateCollections();
			}
		});
		
		collCombo.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
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
				
				
//				List<TrpCollection> ccm = store.getCollectionsCanManage();
//				int i = collCombo.getSelectionIndex();
//				if (i >= 0 && i < ccm.size()) {
//					selColl = ccm.get(i);
//				}
//				updateBtnVisibility();
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
	
	private void updateCollections() {
		collCombo.removeAll();
		List<TrpCollection> ccm = store.getCollectionsCanManage();
		
		int i=0;
		for (TrpCollection c : ccm) {
			logger.debug("collection name: "+c.getColName()+ " i = "+i);
			collCombo.add(c.getColName());
			if (selColl!=null && c.getColId() == selColl.getColId())
				collCombo.select(i);
			++i;
		}
		
		if (collCombo.getItemCount() > 0 && collCombo.getSelectionIndex() == -1)
			collCombo.select(0);
		
		updateBtnVisibility();
	}

	// overriding this methods allows you to set the
	// title of the custom dialog
	@Override protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Upload dialog");
		SWTUtil.centerShell(newShell);
//		newShell.pack();
	}

	@Override protected Point getInitialSize() {
		return new Point(500, 400);
	}

	// override method to use "Login" as label for the OK button
	@Override protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Upload", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override protected void okPressed() {
		saveInput();
		super.okPressed();
	}

	private void saveInput() {
		this.folder = folderText.getText();
		this.title = titleText.getText();
		this.selColl =  getSelectedCollection();
		this.viaFtp = uploadTypeCombo.getSelectionIndex()==1;
	}
	
	public TrpCollection getCollection() {
		return selColl;
	}
	
	public String getFolder() {
		return folder;
	}

	public String getTitle() {
		return title;
	}
	
	public boolean isUploadViaFtp() {
		return viaFtp;
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
				getShell().setSize(300, 200);

				Button btn = new Button(parent, SWT.PUSH);
				btn.setText("Open upload dialog");
				btn.addSelectionListener(new SelectionListener() {

					@Override public void widgetSelected(SelectionEvent e) {
						(new UploadDialog(getShell(), null)).open();
					}

					@Override public void widgetDefaultSelected(SelectionEvent e) {
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
}
