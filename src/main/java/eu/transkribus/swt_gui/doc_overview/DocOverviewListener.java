package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class DocOverviewListener extends SelectionAdapter implements ISelectionChangedListener, IDoubleClickListener, KeyListener, MouseTrackListener {
	private final static Logger logger = LoggerFactory.getLogger(DocOverviewListener.class);
	
	TrpMainWidget mainWidget;
	ServerWidget dow;
	TableViewer docTableViewer;
	
	Storage storage = Storage.getInstance();
	
	public DocOverviewListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.dow = mainWidget.getUi().getServerWidget();
		this.docTableViewer = dow.getTableViewer();
		
		attach();
	}
	
	public void detach() {
		docTableViewer.removeSelectionChangedListener(this);
		docTableViewer.removeDoubleClickListener(this);
		docTableViewer.getTable().removeMouseTrackListener(this);
		docTableViewer.getTable().removeKeyListener(this);
		dow.uploadDocsItem.removeSelectionListener(this);
//		dow.searchBtn.removeSelectionListener(this);
		dow.collectionComboViewerWidget.collectionCombo.removeSelectionListener(this);
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
		docTableViewer.getTable().addMouseTrackListener(this);
		docTableViewer.getTable().addKeyListener(this);
//		docOverviewWidget.getDocMetadataEditor().getApplyBtn().addSelectionListener(this);
//		dow.uploadSingleDocItem.addSelectionListener(this);
//		dow.uploadDocsItem.addSelectionListener(this);
//		dow.searchBtn.addSelectionListener(this);
//		docOverviewWidget.getDeleteItem().addSelectionListener(this);
		dow.collectionComboViewerWidget.collectionCombo.addSelectionListener(this);
		dow.recentDocsComboViewerWidget.lastDocsCombo.addSelectionListener(this);
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

		if (s == dow.collectionComboViewerWidget.collectionCombo) {
			mainWidget.reloadDocList(dow.getSelectedCollectionId());
		}
		else if (s == dow.recentDocsComboViewerWidget.lastDocsCombo){
			String docToLoad = dow.getSelectedRecentDoc();
			mainWidget.loadRecentDoc(docToLoad);

		}
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

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {		
	}

	@Override
	public void mouseEnter(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExit(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseHover(MouseEvent e) {
////		System.out.println("mouse event hover on source : " + e.getSource()	);
////		docTableViewer.getTable().getItem(new Point (e.x, e.y));
//		
//		TableViewer tv = dow.getDocTableWidget().getPageableTable().getViewer();
////		ColumnViewerToolTipSupport.enableFor(tv);
//		
//		//tv.getLabelProvider();
//		
//		class MyDocTableColumnLabelProvider extends ColumnLabelProvider {
//			
//			@Override
//			public String getToolTipText(Object element) {
//				
//				TrpDocMetadata docMd = (TrpDocMetadata) element;
//				return "ID=" + docMd.getDocId() + " / Title=" + docMd.getTitle() + " / N-Pages=" + docMd.getNrOfPages() + " / Uploader=" + docMd.getUploader() + " / Uploaded=" + docMd.getUploadTime().toString() + " / Collections=" + docMd.getColString();
//			}
//
//		}
//		
//
//		MyDocTableColumnLabelProvider labelProvider = new MyDocTableColumnLabelProvider();
//
//	    tv.setLabelProvider(labelProvider);
////		
////		ViewerCell vc = tv.getCell(new Point(e.x, e.y));
////		
////		tv.getTable().getToolTipText();
//		
//		
//		
//		
	}




}
