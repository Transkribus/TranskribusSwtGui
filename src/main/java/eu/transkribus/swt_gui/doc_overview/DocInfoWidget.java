package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import eu.transkribus.swt.util.ThumbnailWidgetVirtual;
import eu.transkribus.swt.util.ThumbnailManagerVirtual;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocInfoWidget extends Composite {
	
	Text loadedDocText, currentCollectionText, loadedPageText, loadedImageUrl, loadedTranscriptUrl;
	ThumbnailWidgetVirtual thumbnailWidget;
	
//	Button openEditDeclManagerBtn;
	
//	DocMetadataEditor docMetadataEditor;
//	EditDeclManagerDialog edm;

	public DocInfoWidget(Composite parent, int style, TrpMainWidget mw) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		docMdExp = new ExpandableComposite(this, ExpandableComposite.COMPACT);
//		Fonts.setBoldFont(docMdExp);
				
		Label loadedDocLabel = new Label(this, SWT.NONE);
		loadedDocLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		loadedDocLabel.setText("Loaded doc: ");
		
		loadedDocText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedDocText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label collLabel = new Label(this, 0);
		collLabel.setText("Current collection: ");
		
		currentCollectionText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		currentCollectionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label fnLabel = new Label(this, SWT.NONE);
		fnLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		fnLabel.setText("Current filename: ");
		
		loadedPageText = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedPageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		Label loadedPageKeyLabel = new Label(c1, SWT.NONE);
//		loadedPageKeyLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
//		loadedPageKeyLabel.setText("Key: ");
//		
//		loadedPageKey = new Text(c1, SWT.BORDER | SWT.READ_ONLY);
//		loadedPageKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label imgUrlLabel = new Label(this, SWT.NONE);
		imgUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		imgUrlLabel.setText("Current image URL: ");
		
		loadedImageUrl = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedImageUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label transcriptUrlLabel = new Label(this, SWT.NONE);
		transcriptUrlLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		transcriptUrlLabel.setText("Current transcript URL: ");
		
		loadedTranscriptUrl = new Text(this, SWT.BORDER | SWT.READ_ONLY);
		loadedTranscriptUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
//		ExpandBar container = new ExpandBar (this, SWT.V_SCROLL);
		
//		Group docMdGroup = new Group(this, 0);
//		docMdGroup.setText("Document metadata");
//		docMdGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//		docMdGroup.setLayout(new FillLayout());
		
		thumbnailWidget = new ThumbnailWidgetVirtual(this, SWT.NONE, mw);
		thumbnailWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
//		docMetadataEditor = new DocMetadataEditor(docMdGroup, 0);
		
		//TODO activate this
//		openEditDeclManagerBtn.setVisible(false);		
	}

	public Text getLoadedDocText() { return loadedDocText; }
	public Text getCurrentCollectionText() { return currentCollectionText; }	
	public Text getLoadedImageUrl() { return loadedImageUrl; }
	public Text getLoadedTranscriptUrl() { return loadedTranscriptUrl; }
	public Text getLoadedPageText() { return loadedPageText; }
	
	public ThumbnailWidgetVirtual getThumbnailWidget() { return thumbnailWidget; }

	public ThumbnailManagerVirtual getThumbnailManager() {
		return thumbnailWidget.getThumbnailManager();
	}
}
