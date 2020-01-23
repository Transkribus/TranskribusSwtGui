package eu.transkribus.swt_gui.htr;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpCollection;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.rest.TrpHtrList;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.dialogs.ChooseCollectionDialog;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class HtrModelsComposite extends Composite implements IStorageListener {
	private static final Logger logger = LoggerFactory.getLogger(HtrModelsComposite.class);

	Storage store = Storage.getInstance();

	HtrTableWidget htw;
	
	HtrDetailsWidget hdw;

	MenuItem shareToCollectionItem, removeFromCollectionItem, deleteItem;
	
	TrpHtr selectedHtr;

	public HtrModelsComposite(Composite parent, final String providerFilter, int flags) {
		super(parent, flags);
		this.setLayout(new GridLayout(1, false));
		
		SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashForm.setLayout(new GridLayout(2, false));

		htw = new HtrTableWidget(sashForm, SWT.BORDER, providerFilter);

		Menu menu = new Menu(htw.getTableViewer().getTable());
		htw.getTableViewer().getTable().setMenu(menu);

		shareToCollectionItem = new MenuItem(menu, SWT.NONE);
		shareToCollectionItem.setText("Share model...");

		//use ShareHtrDialog
//		removeFromCollectionItem = new MenuItem(menu, SWT.NONE);
//		removeFromCollectionItem.setText("Remove model from collection");
		
		deleteItem = new MenuItem(menu, SWT.NONE);
		deleteItem.setText("Delete model...");

		Group detailGrp = new Group(sashForm, SWT.BORDER);
		detailGrp.setText("Details");
		detailGrp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		detailGrp.setLayout(new GridLayout(1, false));

		hdw = new HtrDetailsWidget(detailGrp, SWT.VERTICAL);
		hdw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		hdw.setLayout(new GridLayout(2, false));

		sashForm.setWeights(new int[] { 60, 40 });
		
		updateHtrs(htw.getProviderComboValue());
		
		addListeners();
	}
	
	private void addListeners() {
		
		// fix for missing tooltip in chart after resize. Still does not work always...
		this.getShell().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				logger.trace("Resizing...");
				if(getShell().getMaximized()) {
					logger.trace("To MAX!");
				}
				
				hdw.triggerChartUpdate();
			}
		});
		
		htw.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				BusyIndicator.showWhile(getParent().getDisplay(), new Runnable() {
					@Override
					public void run() {
						updateDetails(getSelectedHtr());
					}
				});
			}
		});
		
		Listener filterModifyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateHtrs(htw.getProviderComboValue());
				updateDetails(getSelectedHtr());
			}
		};
		htw.addListener(SWT.Modify, filterModifyListener);
		
		htw.getReloadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				store.reloadHtrs();
			}
		});
		
		shareToCollectionItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//openCollectionChooserDialog(e);
				ShareHtrDialog diag = new ShareHtrDialog(getShell(), htw.getSelectedHtr());
				int ret = diag.open();
				logger.debug("ShareHtrDialog closed with return code: {}", ret);
			}
			
			
			/**
			 * @deprecated use ShareHtrDialog instead
			 */
			public void openCollectionChooserDialog(SelectionEvent e) {
				ChooseCollectionDialog ccd = new ChooseCollectionDialog(getShell());
				
				@SuppressWarnings("unused")
				int ret = ccd.open();
				TrpCollection col = ccd.getSelectedCollection();
				TrpHtr htr = htw.getSelectedHtr();

				//because admin can see all models and if he then wants to share it to the collection he is actually in it is forbidden
				if (store.getCollId() == col.getColId() && !store.getUser().isAdmin()) {
					DialogUtil.showInfoMessageBox(getShell(), "Info",
							"The selected HTR is already included in this collection.");
					return;
				}
				try {
					store.addHtrToCollection(htr, col);
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not add HTR to collection!", e1);
					String errorMsg = "The selected HTR could not be added to this collection.";
					if(!StringUtils.isEmpty(e1.getMessage())) {
						errorMsg += "\n" + e1.getMessage();
					}
					DialogUtil.showErrorMessageBox(getShell(), "Error sharing HTR",
							errorMsg);
				}
				DialogUtil.showInfoMessageBox(getShell(), "Success", "The HTR was added to the selected collection.");
				super.widgetSelected(e);
			}
		});

		SelectionAdapter removeItemListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrpHtr htr = htw.getSelectedHtr();
				try {
					store.removeHtrFromCollection(htr);
					reloadHtrsFromServer();
					clearTableSelection();
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.debug("Could not remove HTR from collection!", e1);
					DialogUtil.showErrorMessageBox(getShell(), "Error removing HTR",
							"The selected HTR could not be removed from this collection.");
				}
				super.widgetSelected(e);
			}
		};
		if(removeFromCollectionItem != null) {
			removeFromCollectionItem.addSelectionListener(removeItemListener);
		}
		
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//menu item is only enabled if delete action is allowed. See menu detect listener
				TrpHtr htr = getSelectedHtr();
				final String msg = "You are about to delete the HTR model '" + htr.getName() 
					+ "'.\nDo you really want to do this?";
				final int response = DialogUtil.showYesNoDialog(getShell(), "Delete HTR model?", msg);
				
				if(response != SWT.YES) {
					logger.debug("User canceled deletion of HTR with ID = {}", htr.getHtrId());
					return;
				}
				
				try {
					store.deleteHtr(htr);
					//if that worked update the list in Storage
					reloadHtrsFromServer();
					clearTableSelection();
				} catch (SessionExpiredException | ServerErrorException | ClientErrorException
						| NoConnectionException e1) {
					logger.error("Could not delete HTR!", e1);
					DialogUtil.showErrorMessageBox(getShell(), "Error removing HTR",
							"The selected HTR could not be removed from this collection.");
				}
				super.widgetSelected(e);
			}
		});

		htw.getTableViewer().getTable().addListener(SWT.MenuDetect, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (htw.getTableViewer().getTable().getSelectionCount() <= 0) {
					event.doit = false;
					return;
				} 
				deleteItem.setEnabled(isOwnerOfSelectedHtr());
			}
		});
		
		//listen to HtrListLoadEvents
		store.addListener(this);
		
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				logger.debug("Detaching HtrModelsComposite IStorageListener from Storage");
				store.removeListener(HtrModelsComposite.this);
			}
		});
	}
	
	public HtrModelsComposite(Composite parent, int flags) {
		this(parent, null, flags);
	}
	
	public void setSelection(int htrId) {
		logger.trace("Setting selection to htrId = {}", htrId);
		htw.setSelection(htrId);
	}
	
	public TrpHtr getSelectedHtr() {
		return htw.getSelectedHtr();
	}
	
	public boolean isOwnerOfSelectedHtr() {
		if(getSelectedHtr() == null) {
			return false;
		}
		if(!store.isLoggedIn()) {
			return false;
		}
		if(store.isAdminLoggedIn()) {
			return true;
		}
		return getSelectedHtr().getUserId() == store.getUserId();
	}
	
	void updateDetails(TrpHtr selectedHtr) {
		this.selectedHtr = selectedHtr;
		hdw.checkForUnsavedChanges();
		hdw.updateDetails(selectedHtr);
	}

	private void updateHtrs(final String providerFilter) {
		List<TrpHtr> uroHtrs = store.getHtrs(providerFilter);
		htw.refreshList(uroHtrs);
	}
	
	@Override
	public void handleHtrListLoadEvent(HtrListLoadEvent e) {
		htw.resetProviderFilter();
		htw.refreshList(e.htrs.getList());
	}
	
	private void reloadHtrsFromServer() {
		//reload HTRs and show busy indicator in the meantime.
		ReloadHtrListRunnable reloadRunnable = new ReloadHtrListRunnable();
		BusyIndicator.showWhile(getDisplay(), reloadRunnable);
		
		if(reloadRunnable.hasError()) {
			logger.error("Reload of HTR models failed!", reloadRunnable.getError());
			DialogUtil.showDetailedErrorMessageBox(getShell(), "Error loading HTR models",
					"Could not reload the HTR model list from the server.", reloadRunnable.getError());
		}
	}
	
	private void clearTableSelection() {
		//remove any selection in table
		setSelection(-1);
		//clear any data from HtrDetailsWidget
		updateDetails(getSelectedHtr());
	}
	
	/**
	 * Helper class for reloading the HTR list as task with a BusyIndicator.
	 * Any error can be retrieved from it and handled after BusyIndicator.showWhile() completes.
	 * Opening dialogs within the Runnable would block the BusyIndicator from completing.
	 */
	private class ReloadHtrListRunnable implements Runnable {
		private Throwable error;
		
		ReloadHtrListRunnable() {
			error = null;
		}
		
		public void run() {
			//update of the view is done in the handleHtrListLoadEvent method
			Future<TrpHtrList> future = store.reloadHtrs();
			try {
				//after 60 seconds we can assume that something is wrong
				future.get(60, TimeUnit.SECONDS);
			} catch(ExecutionException e) {
				//extract the exception thrown within the future's task
				error = e.getCause();
			} catch(Exception e) {
				error = e;
			}
		}
		
		boolean hasError() {
			return error != null;
		}
		
		Throwable getError() {
			return error;
		}
	}
}
