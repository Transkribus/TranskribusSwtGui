package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.transkribus.core.model.beans.auth.TrpRole;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclManagerDialog;
import eu.transkribus.swt_gui.edit_decl_manager.EditDeclViewerDialog;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocInfoWidget extends Composite {
	
	Label loadedDocLabel;
	Text loadedDocText, currentCollectionText, loadedPageText, loadedImageUrl, loadedTranscriptUrl;
	Button openEditDeclManagerBtn;	
	
	DocMetadataEditor docMetadataEditor;
	EditDeclManagerDialog edm;

	public DocInfoWidget(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		docMdExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
//		Fonts.setBoldFont(docMdExp);
				
		loadedDocLabel = new Label(this, SWT.NONE);
		loadedDocLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedDocLabel.setText("Loaded doc: ");
		
		loadedDocText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedDocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l0 = new Label(this, 0);
		l0.setText("Current collection: ");
		
		currentCollectionText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		currentCollectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedPageLabel = new Label(this, SWT.NONE);
		loadedPageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedPageLabel.setText("Current filename: ");
		
		loadedPageText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedPageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		Label loadedPageKeyLabel = new Label(c1, SWT.NONE);
//		loadedPageKeyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
//		loadedPageKeyLabel.setText("Key: ");
//		
//		loadedPageKey = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
//		loadedPageKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedImageUrlLabel = new Label(this, SWT.NONE);
		loadedImageUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedImageUrlLabel.setText("Current image URL: ");
		
		loadedImageUrl = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedImageUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label loadedTranscriptUrlLabel = new Label(this, SWT.NONE);
		loadedTranscriptUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedTranscriptUrlLabel.setText("Current transcript URL: ");
		
		loadedTranscriptUrl = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedTranscriptUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		ExpandBar container = new ExpandBar (this, SWT.V_SCROLL);

		openEditDeclManagerBtn = new Button(this, SWT.PUSH);
		openEditDeclManagerBtn.setText("Editorial Declaration...");
		openEditDeclManagerBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		
		Group docMdGroup = new Group(this, 0);
		docMdGroup.setText("Document metadata");
		docMdGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		docMdGroup.setLayout(new FillLayout());
		
		docMetadataEditor = new DocMetadataEditor(docMdGroup, 0);
		
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

	public Text getLoadedDocText() { return loadedDocText; }
	public Text getCurrentCollectionText() { return currentCollectionText; }	
	public Text getLoadedImageUrl() { return loadedImageUrl; }
	public Text getLoadedTranscriptUrl() { return loadedTranscriptUrl; }
	public Text getLoadedPageText() { return loadedPageText; }
	public Button getOpenEditDeclManagerBtn() { return openEditDeclManagerBtn; }

}
