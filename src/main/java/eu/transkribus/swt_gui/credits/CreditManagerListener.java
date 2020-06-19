package eu.transkribus.swt_gui.credits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpCreditPackage;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CreditManagerListener implements IStorageListener {
	private static final Logger logger = LoggerFactory.getLogger(CreditManagerListener.class);
	
	Storage store;
	private final CreditManagerDialog view;
	
	CreditManagerListener(CreditManagerDialog view) {
		this.view = view;
		this.store = Storage.getInstance();
		
		addListeners(view);
//		addDndSupport(view);
	}
	
	private void addListeners(CreditManagerDialog view) {
		//enable this to trigger a refresh on tab switch
//		SWTUtil.onSelectionEvent(view.tabFolder, (e) -> { 
//			view.updateUI(false);
//		});
		SWTUtil.setTabFolderBoldOnItemSelection(view.tabFolder);
		SWTUtil.onSelectionEvent(view.addToCollectionBtn, (e) -> {
			List<TrpCreditPackage> packageList = view.userCreditsTable.getSelected();
			assignPackagesToCollection(store.getCollId(), packageList);
		});
		
		SWTUtil.onSelectionEvent(view.removeFromCollectionBtn, (e) -> {
			List<TrpCreditPackage> packageList = view.collectionCreditsTable.getSelected();
			removePackagesFromCollection(store.getCollId(), packageList);
		});
		
		//register as storage listener for handling collection changes (DocListLoadEvent). Deregister on dialog close.
		store.addListener(this);
		view.dialogArea.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent arg0) {
				Storage.getInstance().removeListener(CreditManagerListener.this);
			}
		});
		
		view.jobsTable.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				BusyIndicator.showWhile(view.dialogArea.getDisplay(), new Runnable() {
					@Override
					public void run() {
						List<TrpJobStatus> jobs = view.jobsTable.getSelected();
						if(CollectionUtils.isEmpty(jobs)) {
							logger.debug("No job selected");
							return;
						}
						view.transactionsTable.setJobId(jobs.get(0).getJobIdAsInt());
					}
				});
			}
		});
	}

	private void assignPackagesToCollection(int collId, List<TrpCreditPackage> packageList) {
		if(CollectionUtils.isEmpty(packageList)) {
			return;
		}
		int addCount = 0;
		int notModifiedCount = 0;
		final List<Exception> fails = new ArrayList<>(0);
		List<String> nonShareableErrorMsgList = new ArrayList<>(0);
		for(TrpCreditPackage p : packageList) {
			if(!p.getProduct().getShareable()) {
				final String msg = "'" + p.getProduct().getLabel() + "' packages are not shareable.";
				if(!nonShareableErrorMsgList.contains(msg)) {
					nonShareableErrorMsgList.add(msg);
				}
				//do not attempt to add. the server would respond with status 400 on this.
				continue;
			}
			try {
				store.getConnection().getCreditCalls().addCreditPackageToCollection(store.getCollId(), p.getPackageId());
				addCount++;
			} catch (IllegalStateException ise) {
				//Client currently maps "304 - Not modified" to an IllegalStateException. The package was already assigned to this collection.
				logger.debug("Package is already assigned to this collection.");
				notModifiedCount++;
			} catch (SessionExpiredException e1) {
				//TODO abort and show login dialog
			} catch (TrpServerErrorException e2) {
				logger.error(e2.getMessageToUser());
				fails.add(e2);
			}
		}
		final String balloonMsgTitle = addCount + "/" + packageList.size() + " packages assigned";
		String msg = "";
		if(notModifiedCount > 0) {
			msg += notModifiedCount + " already assigned\n";
		}
		if(nonShareableErrorMsgList.size() > 0) {
			msg += nonShareableErrorMsgList.stream().collect(Collectors.joining("\n")) + "\n";
		}
		if(fails.size() > 0) {
			msg += fails.size() + " errors occurred";
		}
		final String balloonMsg = msg;
		
		view.dialogArea.getDisplay().asyncExec(new Runnable() {
			public void run() {
				view.collectionCreditsTable.refreshPage(false);
				DialogUtil.showBalloonToolTip(view.collectionCreditsTable, null, balloonMsgTitle, balloonMsg.trim());
			}
		});
	}

	private void removePackagesFromCollection(int collId, List<TrpCreditPackage> packageList) {
		if(CollectionUtils.isEmpty(packageList)) {
			return;
		}
		int addCount = 0;
		final List<Exception> fails = new ArrayList<>(0);
		for(TrpCreditPackage p : packageList) {
			try {
				store.getConnection().getCreditCalls().removeCreditPackageFromCollection(store.getCollId(), p.getPackageId());
				addCount++;
			} catch (SessionExpiredException e1) {
				//TODO abort and show login dialog
			} catch (TrpServerErrorException | TrpClientErrorException e2) {
				fails.add(e2);
			}
		}
		final int successCount = addCount;
		view.dialogArea.getDisplay().asyncExec(new Runnable() {
			public void run() {
				view.collectionCreditsTable.refreshPage(false);
				DialogUtil.showBalloonToolTip(view.collectionCreditsTable, null, successCount + " packages removed", "");
			}
		});
	}
	
	/**
	 * Does not work as required.<br>
	 * Drag and drop should only work from one table to another, not within the same table.
	 * <br><br>
	 * I wanted to use DND.DROP_LINK and DND.DROP_MOVE to link the respective dragSourceListener and dropTargetAdapter
	 * but only with the DND.DROP_MOVE operation the event will trigger the dragAccepted method of the listener and execute the drop.
	 * The reason may be found in the source code of jface structured viewer or SWT but I can't find it for the specific version used...
	 * <br><br>
	 * Using classic buttons instead for now.
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private void addDndSupport(CreditManagerDialog view) {
		CreditPackageDragSourceListener userDragSourceListener, collectionDragSourceListener;
		CreditPackageDropTargetListener collectionDropTargetListener, userDropTargetListener;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };
		
		//listeners for adding credits to a collection
		final int addOps = DND.DROP_LINK;
		userDragSourceListener = new CreditPackageDragSourceListener(view.userCreditsTable.getTableViewer());
		collectionDropTargetListener = new CreditPackageDropTargetListener(view.collectionCreditsTable.getTableViewer()) {
			@Override
			protected void performDropAction(DropTargetEvent event) {
				// add credit package to collection
				super.performDropAction(event);
			}
		};

		view.userCreditsTable.getTableViewer().addDragSupport(addOps, transferTypes, userDragSourceListener);
		view.collectionCreditsTable.getTableViewer().addDropSupport(addOps, transferTypes, collectionDropTargetListener);
		
		//listeners for removing credits from a collection
		final int removeOps = DND.DROP_MOVE;
		collectionDragSourceListener = new CreditPackageDragSourceListener(view.collectionCreditsTable.getTableViewer());
		userDropTargetListener = new CreditPackageDropTargetListener(view.userCreditsTable.getTableViewer()) {
			@Override
			protected void performDropAction(DropTargetEvent event) {
				// remove credit package from collection
				super.performDropAction(event);
			}
		};

		view.collectionCreditsTable.getTableViewer().addDragSupport(removeOps, transferTypes, collectionDragSourceListener);
		view.userCreditsTable.getTableViewer().addDropSupport(removeOps, transferTypes, userDropTargetListener);
	}
	
	@Override
	public void handleDocListLoadEvent(DocListLoadEvent e) {
		if(e.isCollectionChange) {
			view.updateUI(true);
		}
	}
	
	class CreditPackageDragSourceListener implements DragSourceListener {

		private final Viewer viewer;

		public CreditPackageDragSourceListener(Viewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			logger.debug("DragStart: " + event);
			
			ISelection selection = viewer.getSelection();
			logger.debug("Setting selection = {}", selection);
			// the controller can retrieve the selection in the end.
			LocalSelectionTransfer.getTransfer().setSelection(selection);
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			logger.debug("DragSetData: " + event);
			event.data = (IStructuredSelection) viewer.getSelection();
		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			logger.debug("DragFinished: " + event);
		}
	}

	class CreditPackageDropTargetListener implements DropTargetListener { //extends ViewerDropAdapter {

		Viewer viewer;
		
		protected CreditPackageDropTargetListener(Viewer viewer) {
			this.viewer = viewer;
		}
		
//		protected CreditPackageDropTargetListener(Viewer viewer) {
//			super(viewer);
//		}

		@Override
		public void drop(DropTargetEvent event) {
			logger.debug("Drop event: " + event);
			Runnable r = new Runnable() {
				@Override
				public void run() {
					performDropAction(event);
				}
			};
			BusyIndicator.showWhile(view.dialogArea.getDisplay(), r);
		}
		
		protected void performDropAction(DropTargetEvent event) {
			logger.debug("Perform drop action here.");
			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
			logger.debug("Transfer selection = {}", selection);
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
			logger.debug("DropEnter: " + event);
		}

		@Override
		public void dragLeave(DropTargetEvent event) {
			logger.debug("DropLeave: " + event);
		}

		@Override
		public void dragOperationChanged(DropTargetEvent event) {
			logger.debug("DropOperationChanged: " + event);
		}

		@Override
		public void dragOver(DropTargetEvent event) {
			logger.trace("DragOver: " + event);
		}

		@Override
		public void dropAccept(DropTargetEvent event) {
			logger.debug("DropAccept: " + event);
			// in some cases we want to block the drop action. set event.detail = DND.DROP_NONE to do this
//			event.detail = DND.DROP_NONE;
		}

//		@Override
//		public boolean performDrop(Object data) {
//			logger.debug("Performing drop. Data = {}", data);
//			return false;
//		}
//
//		@Override
//		public boolean validateDrop(Object target, int op, TransferData type) {
//			logger.debug("Validating drop:");
//			logger.debug("Target = {}", target);
//			logger.debug("op = {}", op);
//			logger.debug("type = {}", type);
//			logger.debug("Current target = {}", this.getCurrentTarget());
//			logger.debug("Selected object = {}", this.getSelectedObject());
//			return false;
//		}
	}
}
