package eu.transkribus.swt_gui.doc_overview;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class ServerWidgetListener extends SelectionAdapter implements Listener, ISelectionChangedListener, IDoubleClickListener, KeyListener, MouseTrackListener, IStorageListener {
	private final static Logger logger = LoggerFactory.getLogger(ServerWidgetListener.class);
	
	ServerWidget sw;
	TableViewer dtv;
	
	Storage storage = Storage.getInstance();
	
	public ServerWidgetListener(ServerWidget sw) {
		this.sw = sw;
		this.dtv = sw.getTableViewer();
		
		sw.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
		
		attach();
	}
		
	public void attach() {
		dtv.addSelectionChangedListener(this);
		dtv.addDoubleClickListener(this);
		dtv.getTable().addMouseTrackListener(this);
		dtv.getTable().addKeyListener(this);

//		sw.collectionComboViewerWidget.collectionCombo.addSelectionListener(this);
		sw.collectionSelectorWidget.addListener(SWT.Selection, this);
		
		sw.recentDocsComboViewerWidget.lastDocsCombo.addSelectionListener(this);
		SWTUtil.addSelectionListener(sw.manageCollectionsBtn, this);
		sw.showActivityWidgetBtn.addSelectionListener(this);
				
		SWTUtil.addSelectionListener(sw.duplicateDocMenuItem, this);
		SWTUtil.addSelectionListener(sw.deleteDocMenuItem, this);
		SWTUtil.addSelectionListener(sw.addToCollectionMenuItem, this);
		SWTUtil.addSelectionListener(sw.removeFromCollectionMenuItem, this);
		
		SWTUtil.addSelectionListener(sw.collectionUsersBtn, this);
		SWTUtil.addSelectionListener(sw.createCollectionBtn, this);
		SWTUtil.addSelectionListener(sw.deleteCollectionBtn, this);
		SWTUtil.addSelectionListener(sw.modifyCollectionBtn, this);
		
		SWTUtil.addSelectionListener(sw.openLocalDocBtn, this);
		SWTUtil.addSelectionListener(sw.importBtn, this);
		SWTUtil.addSelectionListener(sw.exportBtn, this);
		SWTUtil.addSelectionListener(sw.findBtn, this);
		
		Storage.getInstance().addListener(this);
	}
	
	public void detach() {
		dtv.removeSelectionChangedListener(this);
		dtv.removeDoubleClickListener(this);
		dtv.getTable().removeMouseTrackListener(this);
		dtv.getTable().removeKeyListener(this);
		
//		sw.collectionComboViewerWidget.collectionCombo.removeSelectionListener(this);
		sw.collectionSelectorWidget.removeListener(SWT.Selection, this);
		sw.recentDocsComboViewerWidget.lastDocsCombo.removeSelectionListener(this);
		SWTUtil.removeSelectionListener(sw.manageCollectionsBtn, this);
		sw.showActivityWidgetBtn.removeSelectionListener(this);
		
		SWTUtil.removeSelectionListener(sw.duplicateDocMenuItem, this);
		SWTUtil.removeSelectionListener(sw.deleteDocMenuItem, this);
		SWTUtil.removeSelectionListener(sw.addToCollectionMenuItem, this);
		SWTUtil.removeSelectionListener(sw.removeFromCollectionMenuItem, this);
		
		SWTUtil.removeSelectionListener(sw.collectionUsersBtn, this);
		SWTUtil.removeSelectionListener(sw.createCollectionBtn, this);
		SWTUtil.removeSelectionListener(sw.deleteCollectionBtn, this);
		SWTUtil.removeSelectionListener(sw.modifyCollectionBtn, this);
		
		SWTUtil.removeSelectionListener(sw.openLocalDocBtn, this);
		SWTUtil.removeSelectionListener(sw.importBtn, this);
		SWTUtil.removeSelectionListener(sw.exportBtn, this);
		SWTUtil.removeSelectionListener(sw.findBtn, this);
		
		Storage.getInstance().removeListener(this);
	}
	
	@Override public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
		sw.updateLoggedIn();
	}

	@Override public void doubleClick(DoubleClickEvent event) {
		TrpMainWidget mw = TrpMainWidget.getInstance();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object el = selection.getFirstElement();
		if (el == null)
			return;

		if (el instanceof TrpDocMetadata) {
			int docId = ((TrpDocMetadata) el).getDocId();
			logger.debug("Loading doc with id: " + docId);

			mw.loadRemoteDoc(docId, sw.getSelectedCollectionId());
		}
	}


	@Override
	public void selectionChanged(SelectionChangedEvent event) {
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		TrpMainWidget mw = TrpMainWidget.getInstance();

		if (s == sw.recentDocsComboViewerWidget.lastDocsCombo){
			String docToLoad = sw.getSelectedRecentDoc();
			if (docToLoad != null) {
				mw.loadRecentDoc(docToLoad);
			}
		}
		else if (s == sw.manageCollectionsBtn) {
			mw.openCollectionManagerDialog();
		}
		else if (s == sw.showActivityWidgetBtn) {
			mw.openActivityDialog();
		}
		else if (s == sw.duplicateDocMenuItem) {
			mw.duplicateDocument(mw.getSelectedCollectionId(), sw.getSelectedDocument());
		}
		else if (s == sw.deleteDocMenuItem) {
			mw.deleteDocuments(sw.getSelectedDocuments());
		}
		else if (s == sw.addToCollectionMenuItem) {
			mw.addDocumentsToCollection(mw.getSelectedCollectionId(), sw.getSelectedDocuments());
		}
		else if (s == sw.removeFromCollectionMenuItem) {
			mw.removeDocumentsFromCollection(mw.getSelectedCollectionId(), sw.getSelectedDocuments());
		}
		else if (s == sw.collectionUsersBtn) {
			mw.openCollectionUsersDialog(mw.getUi().getServerWidget().getSelectedCollection());
		}
		else if (s == sw.createCollectionBtn) {
			mw.createCollection();
		}
		else if (s == sw.deleteCollectionBtn) {
			mw.deleteCollection(mw.getSelectedCollection());
		}
		else if (s == sw.modifyCollectionBtn) {
			mw.modifyCollection(mw.getSelectedCollection());
		}
		else if (s == sw.openLocalDocBtn) {
			mw.loadLocalFolder();
		}
		else if (s == sw.importBtn) {
			mw.uploadDocuments();
		}
		else if (s == sw.exportBtn) {
			mw.unifiedExport();
		}
		else if (s == sw.findBtn) {
			mw.openSearchDialog();
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

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection && event.widget == sw.collectionSelectorWidget) {
			logger.debug("selected a collection, id: "+sw.getSelectedCollectionId()+" coll: "+sw.getSelectedCollection());
			TrpMainWidget.getInstance().reloadDocList(sw.getSelectedCollectionId());
		}
	}




}
