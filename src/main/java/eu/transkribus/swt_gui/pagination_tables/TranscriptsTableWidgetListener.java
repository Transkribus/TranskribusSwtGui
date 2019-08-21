package eu.transkribus.swt_gui.pagination_tables;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

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
			int nrOfVersions2Delete = selectedVersions.size();
			if (nrOfVersions2Delete == 0 || DialogUtil.showYesNoDialog(tw.getShell(), "Delete Version(s)", "Do you really want to delete " + nrOfVersions2Delete + " selected versions ")!=SWT.YES) {
				return;
			}
			
			int itemCount = (int) tw.getPageableTable().getController().getTotalElements();
			try {		
				if(itemCount == 1){
					throw new Exception("Cannot delete all transcripts of a page!");
				}
				
				final TrpTranscriptMetadata currentTranscript = Storage.getInstance().getTranscriptMetadata();
			
				ProgressBarDialog.open(tw.getShell(), new IRunnableWithProgress() {
					@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

							monitor.beginTask("Delete version(s) ", nrOfVersions2Delete);
							int c=0;
							
							for (TrpTranscriptMetadata md : selectedVersions){
								if (monitor.isCanceled()){
									monitor.done();
									return;
								}

								if (md!=null) {
									try {
										logger.info("delete transcript: " + md.getKey());
										Storage.getInstance().deleteTranscript(md);
									} catch (SessionExpiredException | ServerErrorException
											| IllegalArgumentException | NoConnectionException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
									// reload page if current transcript was deleted:
									if (currentTranscript!=null && currentTranscript.equals(md)) {
										//TrpMainWidget.getInstance().reloadCurrentPage(false);
										Storage.getInstance().setLatestTranscriptAsCurrent();
									}
									
								}
								
								monitor.subTask("Version(s) deleted " + ++c + "/" + nrOfVersions2Delete );
								monitor.worked(c);
							}
							monitor.done();
					}
				}, "Delete transcript version(s)", true);
			} catch (Throwable ex) {
				TrpMainWidget.getInstance().onError("Error deleting versions", ex.getMessage(), ex, true, false);
//				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
//				messageBox.setMessage("Could not delete transcript: " + ex.getMessage());
//				messageBox.setText("Error");
//				messageBox.open();
			}finally{
				try {
					TrpMainWidget.getInstance().reloadCurrentPage(true, null, null);
					//Storage.getInstance().reloadDocWithAllTranscripts();
					tw.redraw();
				} catch (ClientErrorException | IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

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
			TrpMainWidget.getInstance().updateVersionStatus();
		}		
	}

	@Override
	public void mouseDown(MouseEvent e) {
		
        TableItem[] selection = tv.getTable().getSelection();
        
        boolean isLatestTranscriptAndLoaded = false;
        if(tw.getFirstSelected() != null) {
        	//current transcript means 'latest'
        	isLatestTranscriptAndLoaded = (tw.getFirstSelected().getTsId() == Storage.getInstance().getPage().getCurrentTranscript().getTsId()) && (tw.getFirstSelected().getTsId() == Storage.getInstance().getTranscriptMetadata().getTsId());
        }
        /*
         * only the newest transcript can be set to a new status with right click (only  for this transcipt the menu gets visible)
         */
        if(selection.length==1 && isLatestTranscriptAndLoaded && (e.button == 3)){
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
