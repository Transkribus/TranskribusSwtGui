package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocMetadataWidget extends Composite {
	
	Label loadedDocLabel;
	Text loadedDocText, currentCollectionText, loadedPageText, loadedImageUrl, loadedTranscriptUrl;
	Button openMetadataEditorBtn, openEditDeclManagerBtn;	
	
	EditDeclManagerDialog edm;
	DocMetadataEditor docMetadataEditor;

	public DocMetadataWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
//		docMdExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
//		Fonts.setBoldFont(docMdExp);
		
		Composite c1 = new Composite(this, SWT.NONE);
		c1.setLayout(new GridLayout(2, false));
		c1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
				
		loadedDocLabel = new Label(c1, SWT.NONE);
		loadedDocLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedDocLabel.setText("Loaded doc: ");
		
		loadedDocText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedDocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l0 = new Label(c1, 0);
		l0.setText("Current collection: ");
		
		currentCollectionText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		currentCollectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedPageLabel = new Label(c1, SWT.NONE);
		loadedPageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedPageLabel.setText("Current filename: ");
		
		loadedPageText = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedPageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		Label loadedPageKeyLabel = new Label(c1, SWT.NONE);
//		loadedPageKeyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
//		loadedPageKeyLabel.setText("Key: ");
//		
//		loadedPageKey = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
//		loadedPageKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedImageUrlLabel = new Label(c1, SWT.NONE);
		loadedImageUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedImageUrlLabel.setText("Current image URL: ");
		
		loadedImageUrl = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedImageUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedTranscriptUrlLabel = new Label(c1, SWT.NONE);
		loadedTranscriptUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedTranscriptUrlLabel.setText("Current transcript URL: ");
		
		loadedTranscriptUrl = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
		loadedTranscriptUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		ExpandBar container = new ExpandBar (this, SWT.V_SCROLL);
		
		openMetadataEditorBtn = new Button(c1, SWT.PUSH);
		openMetadataEditorBtn.setText("Document metadata...");
		openMetadataEditorBtn.setToolTipText("Edit document metadata");
		openMetadataEditorBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		openEditDeclManagerBtn = new Button(c1, SWT.PUSH);
		openEditDeclManagerBtn.setText("Editorial Declaration...");
		openEditDeclManagerBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		//TODO activate this
//		openEditDeclManagerBtn.setVisible(false);		
		
	}
	
	public void openEditDeclManagerWidget() {
		Storage store = Storage.getInstance();
		
		if(!store.isDocLoaded()) {
			return;
		}
		if (!isEditDeclManagerOpen()) {
			if(store.getRoleOfUserInCurrentCollection().getValue() < TrpRole.Editor.getValue()){
				edm = new EditDeclViewerDialog(getShell(), SWT.NONE);
			} else {
				edm = new EditDeclManagerDialog(getShell(), SWT.NONE);
			}
			edm.open();
		} else {
			edm.getShell().setVisible(true);
		}
	}
	
	public boolean isEditDeclManagerOpen() {
		return edm != null && edm.getShell() != null && !edm.getShell().isDisposed();
	}
	
	public void openMetadataEditor(final String message) {
		final Storage store = Storage.getInstance();
		if (!store.isDocLoaded())
			return;
		
		if(docMetadataEditor == null || docMetadataEditor.isDisposed()){
			final Shell s = new Shell(SWT.SHELL_TRIM | SWT.RESIZE);
			s.setLayout(new FillLayout());
			docMetadataEditor = new DocMetadataEditor(s, SWT.NONE, message);
			
			docMetadataEditor.setMetadata(store.getDoc().getMd());
			docMetadataEditor.getApplyBtn().addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					docMetadataEditor.applyMetadataFromGui(store.getDoc().getMd());
					TrpMainWidget.getInstance().saveDocMetadata();
					s.close();
				}
			});
			s.setSize(700, 600);
			s.setText("Document metadata");
			SWTUtil.centerShell(s);
			s.open();
		} else {
			docMetadataEditor.setVisible(true);
			docMetadataEditor.forceFocus();
		}
	}
	
	public Text getLoadedDocText() { return loadedDocText; }
	public Text getCurrentCollectionText() { return currentCollectionText; }	
	public Text getLoadedImageUrl() { return loadedImageUrl; }
	public Text getLoadedTranscriptUrl() { return loadedTranscriptUrl; }
	public Text getLoadedPageText() { return loadedPageText; }
	public Button getOpenMetadataEditorBtn() { return openMetadataEditorBtn; }
	public Button getOpenEditDeclManagerBtn() { return openEditDeclManagerBtn; }

}
