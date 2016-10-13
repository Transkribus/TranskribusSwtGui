package eu.transkribus.swt_gui.pagination_tables;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.listener.IStorageListener;

public class TranscriptsTableWidgetListener implements SelectionListener, IDoubleClickListener, MouseListener, IStorageListener {
	private final static Logger logger = LoggerFactory.getLogger(TranscriptsTableWidgetListener.class);
	
	TranscriptsTableWidgetPagination tw;
	TableViewer tv;
	
	public TranscriptsTableWidgetListener(TranscriptsTableWidgetPagination tw) {
		this.tw = tw;
		this.tv = tw.getPageableTable().getViewer();
		
		tw.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				detach();
			}
		});
		
		attach();
	}
	
	public void attach() {
		tv.getTable().addMouseListener(this);
		tv.addDoubleClickListener(this);
		tv.getTable().addSelectionListener(this);
		Storage.getInstance().addListener(this);
		
		if (tw.deleteBtn != null)
			tw.deleteBtn.addSelectionListener(this);		
	}
	
	public void detach() {
		tv.getTable().removeMouseListener(this);
		tv.removeDoubleClickListener(this);
		tv.getTable().removeSelectionListener(this);
		Storage.getInstance().removeListener(this);
		
		if (tw.deleteBtn != null)
			tw.deleteBtn.removeSelectionListener(this);
		
	}
	
	@Override public void handleLoginOrLogout(LoginOrLogoutEvent arg) {
		if (SWTUtil.isDisposed(tw))
			return;
			
		tw.refreshPage(true);
	}
	
	@Override public void handleTranscriptListLoadEvent(TranscriptListLoadEvent arg) {
		if (SWTUtil.isDisposed(tw))
			return;		
		
		tw.refreshPage(true);
	}

	@Override public void handleTranscriptLoadEvent(TranscriptLoadEvent arg) {
		if (SWTUtil.isDisposed(tw))
			return;		
		
		tv.refresh();
	}	

	@Override public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		if(s == tw.deleteBtn){
			List<TrpTranscriptMetadata> selectedVersions = tw.getSelected();
			if (DialogUtil.showYesNoDialog(tw.getShell(), "Delete Version(s)", "Do you really want to delete " + selectedVersions.size() + " selected versions ")!=SWT.YES) {
				return;
			}
			for (TrpTranscriptMetadata md : selectedVersions){
				if (md!=null) {
					deleteTranscript(md);
				}
			}
//			TrpTranscriptMetadata md = tw.getFirstSelected();
//			if (md!=null) {
//				deleteTranscript(md);
//			}
		}
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override public void doubleClick(DoubleClickEvent event) {
		TrpTranscriptMetadata md = tw.getFirstSelected();
		logger.debug("double click on transcript: "+md);
		
		if (md!=null) {
			logger.debug("Loading transcript: "+md);
			TrpMainWidget.getInstance().jumpToTranscript(md, true);
		}		
	}
	
	private void deleteTranscript(TrpTranscriptMetadata tMd) {
		logger.info("delete transcript: " + tMd.getKey());
		
		int itemCount = (int) tw.getPageableTable().getController().getTotalElements();
		
		if(itemCount == 1 || tMd.getKey() == null){
			MessageBox messageBox = new MessageBox(tw.getShell(), SWT.ICON_INFORMATION
		            | SWT.OK);
	        messageBox.setMessage("Can not delete this version.");
	        messageBox.setText("Unauthorized");
	        messageBox.open();
		} else {
			try {
				Storage store = Storage.getInstance();
				
				TrpTranscriptMetadata currentTranscript = store.getTranscriptMetadata();
				logger.debug("deleting transcript");
				store.deleteTranscript(tMd);
				
				// reload page if current transcript was deleted:
				if (currentTranscript!=null && currentTranscript.equals(tMd)) {
					TrpMainWidget.getInstance().reloadCurrentPage(false);
				} else {
					store.reloadTranscriptsList(store.getCurrentDocumentCollectionId());
				}
			} catch (Exception e1) {
				MessageBox messageBox = new MessageBox(tw.getShell(), SWT.ICON_ERROR
			            | SWT.OK);
		        messageBox.setMessage("Could not delete transcript: " + e1.getMessage());
		        messageBox.setText("Error");
		        messageBox.open();
			}
		}
	}


	@Override
	public void mouseDown(MouseEvent e) {
		
        TableItem[] selection = tv.getTable().getSelection();
        
        boolean isCurrentTranscript = false;
        if(tw.getFirstSelected() != null) {
        	isCurrentTranscript = (tw.getFirstSelected().getTime().getTime() == Storage.getInstance().getTranscriptMetadata().getTime().getTime());
        }
        /*
         * only the current loaded transcript can be set to a new status with right click (only  for this transcipt the menu gets visible)
         */
        if(selection.length==1 && isCurrentTranscript && (e.button == 3)){
        	logger.debug("show content menu");
        	//vw.setContextMenuVisible(true);
        	tw.enableContextMenu();
        }
        else{
        	tw.disableContextMenu();
        	//vw.setContextMenuVisible(false);
        }

		
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseUp(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
