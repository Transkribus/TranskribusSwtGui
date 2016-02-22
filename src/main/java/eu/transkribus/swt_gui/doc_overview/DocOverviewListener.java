package eu.transkribus.swt_gui.doc_overview;

import org.dea.swt.util.SWTUtil;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocOverviewListener extends SelectionAdapter implements ISelectionChangedListener, IDoubleClickListener, KeyListener  {
	private final static Logger logger = LoggerFactory.getLogger(DocOverviewListener.class);
	
	static TrpMainWidget mainWidget;
	DocOverviewWidget dow;
	TableViewer docTableViewer;
	
	Storage storage = Storage.getInstance();
	
	public DocOverviewListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.dow = mainWidget.getUi().getDocOverviewWidget();
		this.docTableViewer = dow.getTableViewer();

		attach();
	}
	
	public void detach() {
		docTableViewer.removeSelectionChangedListener(this);
		docTableViewer.removeDoubleClickListener(this);
		docTableViewer.getTable().removeKeyListener(this);
//		docOverviewWidget.getDocMetadataEditor().getApplyBtn().removeSelectionListener(this);
		dow.getOpenMetadataEditorBtn().removeSelectionListener(this);
		dow.getOpenEditDeclManagerBtn().removeSelectionListener(this);
//		dow.uploadSingleDocItem.removeSelectionListener(this);
		dow.uploadDocsItem.removeSelectionListener(this);
		dow.findDocumentsBtn.removeSelectionListener(this);
//		docOverviewWidget.getDeleteItem().removeSelectionListener(this);
		dow.collectionComboViewerWidget.collectionCombo.removeSelectionListener(this);
//		dow.collectionComboViewerWidget.reloadCollectionsBtn.removeSelectionListener(this);
		dow.manageCollectionsBtn.removeSelectionListener(this);
		dow.syncWithLocalDocBtn.removeSelectionListener(this);
		dow.showActivityWidgetBtn.removeSelectionListener(this);
		dow.applyAffineTransformBtn.removeSelectionListener(this);
		dow.batchReplaceImgsBtn.removeSelectionListener(this);
		
//		dow.collectionsTable.getTableViewer().removeDoubleClickListener(this);
	}
	
	public void attach() {
		docTableViewer.addSelectionChangedListener(this);
		docTableViewer.addDoubleClickListener(this);
		docTableViewer.getTable().addKeyListener(this);
//		docOverviewWidget.getDocMetadataEditor().getApplyBtn().addSelectionListener(this);
		dow.getOpenMetadataEditorBtn().addSelectionListener(this);
		dow.getOpenEditDeclManagerBtn().addSelectionListener(this);
//		dow.uploadSingleDocItem.addSelectionListener(this);
		dow.uploadDocsItem.addSelectionListener(this);
		dow.findDocumentsBtn.addSelectionListener(this);
//		docOverviewWidget.getDeleteItem().addSelectionListener(this);
		dow.collectionComboViewerWidget.collectionCombo.addSelectionListener(this);
//		dow.collectionComboViewerWidget.reloadCollectionsBtn.addSelectionListener(this);
		dow.manageCollectionsBtn.addSelectionListener(this);
		dow.showActivityWidgetBtn.addSelectionListener(this);
		
		// admin area:
		dow.syncWithLocalDocBtn.addSelectionListener(this);
		dow.applyAffineTransformBtn.addSelectionListener(this);
		dow.batchReplaceImgsBtn.addSelectionListener(this);
		
//		dow.collectionsTable.getTableViewer().addDoubleClickListener(this);
	}


	@Override
	public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			Object el = selection.getFirstElement();
			if (el == null)
				return;
			
			if (el instanceof TrpDocMetadata) {
				int docId = ((TrpDocMetadata)el).getDocId();
				logger.debug("Loading doc with id: "+docId);
												
				mainWidget.loadRemoteDoc(docId, dow.getSelectedCollectionId());
			}
//			else if (el instanceof TrpCollection) {
//				logger.debug("double clicked element: "+(TrpCollection) el);
////				dow.selectedCollection = (TrpCollection) el;
//				dow.setSelectedCollection((TrpCollection) el);
//				mainWidget.reloadDocList((TrpCollection) el);
//			}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();

//		if (s == dow.uploadSingleDocItem) {
//			mainWidget.uploadSingleDocument();
//		} 
		if (s == dow.uploadDocsItem) {
			mainWidget.uploadDocuments();
		} else if (s == dow.findDocumentsBtn) {
			mainWidget.findDocuments();
		}
//		else if (s == docOverviewWidget.getDocMetadataEditor().getApplyBtn()) {
//			mainWidget.applyMetadata();
//		} 
		else if (s == dow.getOpenMetadataEditorBtn()) {
			createMetadataEditor();
		}
		else if (s == dow.getOpenEditDeclManagerBtn()) {
			dow.openEditDeclManagerWidget();
		}
//		else if (s == docOverviewWidget.getDeleteItem()) {
//			mainWidget.deleteSelectedDocument();
//		}
		else if (s == dow.collectionComboViewerWidget.collectionCombo) {
			mainWidget.reloadDocList(dow.getSelectedCollection());
		}
//		else if (s == dow.collectionComboViewerWidget.reloadCollectionsBtn) {
//			mainWidget.reloadCollections();
//		}
		else if (s == dow.manageCollectionsBtn) {
			dow.openCollectionsManagerWidget();
		}
		else if (s == dow.showActivityWidgetBtn) {
			dow.openActivityDialog();
		}
		else if (s == dow.syncWithLocalDocBtn) {
			mainWidget.syncWithLocalDoc();
		}
		else if (s == dow.applyAffineTransformBtn) {
			mainWidget.applyAffineTransformToDoc();
		}
		else if (s == dow.batchReplaceImgsBtn) {
			mainWidget.batchReplaceImagesForDoc();
		}		
	}
		
	public static void createMetadataEditor() {
		createMetadataEditor(null);
	}
	
	public static void createMetadataEditor(final String message) {
		final Storage store = Storage.getInstance();
		if (!store.isDocLoaded())
			return;
		
		if(TrpMainWidget.docMetadataEditor == null || TrpMainWidget.docMetadataEditor.isDisposed()){
			final Shell s = new Shell();
			s.setLayout(new FillLayout());
			TrpMainWidget.docMetadataEditor = new DocMetadataEditor(s, SWT.NONE, message);
			
			TrpMainWidget.docMetadataEditor.setMetadata(store.getDoc().getMd());
			TrpMainWidget.docMetadataEditor.getApplyBtn().addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					TrpMainWidget.docMetadataEditor.applyMetadataFromGui(store.getDoc().getMd());
					mainWidget.saveDocMetadata();
					s.close();
				}
			});
			s.setSize(500, 800);
			s.setText("Document metadata");
			SWTUtil.centerShell(s);
			s.open();
		} else {
			TrpMainWidget.docMetadataEditor.setVisible(true);
			TrpMainWidget.docMetadataEditor.forceFocus();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {		
	}




}
