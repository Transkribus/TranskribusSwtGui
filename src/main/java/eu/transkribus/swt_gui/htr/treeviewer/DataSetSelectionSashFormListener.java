package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.enums.DataSetType;
import eu.transkribus.swt_gui.htr.treeviewer.DataSetSelectionSashForm.VersionComboStatus;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;

public class DataSetSelectionSashFormListener {
	private static final Logger logger = LoggerFactory.getLogger(DataSetSelectionSashFormListener.class);

	private final DataSetSelectionController controller;
	private DataSetSelectionSashForm view;
	private final IDoubleClickListener treeViewerDoubleClickListener;
	private final ISelectionChangedListener treeViewerSelectionChangedListener;

	private DragSourceListener docTreeDragSourceListener, gtTreeDragSourceListener;
	private DropTargetListener trainSetDropTargetListener, valSetDropTargetListener;

	DataSetSelectionSashFormListener(DataSetSelectionSashForm view, DataSetSelectionController controller) {
		this.view = view;
		this.controller = controller;
		treeViewerDoubleClickListener = new TreeViewerDoubleClickListener();
		treeViewerSelectionChangedListener = new TreeViewerSelectionChangedListener();
		addListeners(view);
		addDndSupport(view);
	}

	private void addDndSupport(DataSetSelectionSashForm view) {
		trainSetDropTargetListener = new DataSetSelectionDropAdapter(view.trainSetOverviewTable.getTableViewer(),
				DataSetType.TRAIN);
		valSetDropTargetListener = new DataSetSelectionDropAdapter(view.valSetOverviewTable.getTableViewer(),
				DataSetType.VALIDATION);
		docTreeDragSourceListener = new DataTreeDragSourceListener(view.docTv);
		gtTreeDragSourceListener = new DataTreeDragSourceListener(view.groundTruthTv);

		final int operations = DND.DROP_MOVE;// | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] transferTypes = new Transfer[] { LocalSelectionTransfer.getTransfer() };

		view.docTv.addDragSupport(operations, transferTypes, docTreeDragSourceListener);
		view.groundTruthTv.addDragSupport(operations, transferTypes, gtTreeDragSourceListener);

		view.trainSetOverviewTable.getTableViewer().addDropSupport(operations, transferTypes,
				trainSetDropTargetListener);
		view.valSetOverviewTable.getTableViewer().addDropSupport(operations, transferTypes, valSetDropTargetListener);
	}

	private void addListeners(DataSetSelectionSashForm view) {
		view.docTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
		view.docTv.addDoubleClickListener(treeViewerDoubleClickListener);

		view.groundTruthTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
		view.groundTruthTv.addDoubleClickListener(treeViewerDoubleClickListener);

		view.addToTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						if (view.isDocumentsTabActive()) {
							controller.addDocumentSelectionToTrainSet();
						} else if (view.isGtTabActive()) {
							controller.addGtSelectionToTrainSet();
						}
					}
				};
				BusyIndicator.showWhile(view.getDisplay(), r);
			}
		});

		view.addToValSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						if (view.isDocumentsTabActive()) {
							controller.addDocumentSelectionToValidationSet();
						} else if (view.isGtTabActive()) {
							controller.addGtSelectionToValidationSet();
						}
					}
				};
				BusyIndicator.showWhile(view.getDisplay(), r);
			}
		});

		view.removeFromTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						List<IDataSelectionEntry<?, ?>> entries = view.trainSetOverviewTable.getSelectedDataSets();
						controller.removeSelectionFromTrainSet(entries);
					}
				};
				BusyIndicator.showWhile(view.getDisplay(), r);
			}
		});

		view.removeFromValSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						List<IDataSelectionEntry<?, ?>> entries = view.valSetOverviewTable.getSelectedDataSets();
						controller.removeSelectionFromValSet(entries);
					}
				};
				BusyIndicator.showWhile(view.getDisplay(), r);
			}
		});
		
		view.versionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						VersionComboStatus status = view.getVersionComboStatus();
						logger.debug("Selection of versionCombo changed: {}", status);
						controller.setTranscriptVersionToUse(status);
					}
				};
				BusyIndicator.showWhile(view.getDisplay(), r);
			}
		});
	}

	/**
	 * Updates thumbnail image on selection change
	 *
	 */
	private class TreeViewerSelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			controller.updateThumbnail(selection);
		}
	};

	/**
	 * Expands items that have children on double click. Leaf elements are
	 * displayed.
	 *
	 */
	public class TreeViewerDoubleClickListener implements IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (o instanceof TrpDocMetadata) {
						expandTreeItem(o, view.docTv);
					} else if (o instanceof TrpPage) {
						controller.loadPageInMainWidget((TrpPage) o);
					} else if (o instanceof TrpHtr || o instanceof HtrGtDataSet) {
						expandTreeItem(o, view.groundTruthTv);
					}
				}
			};
			BusyIndicator.showWhile(view.getDisplay(), r);
		}

		private void expandTreeItem(Object o, TreeViewer tv) {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					final ITreeContentProvider provider = (ITreeContentProvider) tv.getContentProvider();
					if (!provider.hasChildren(o)) {
						return;
					}
					if (tv.getExpandedState(o)) {
						tv.collapseToLevel(o, AbstractTreeViewer.ALL_LEVELS);
					} else {
						tv.expandToLevel(o, 1);
					}
				}
			};
			BusyIndicator.showWhile(view.getDisplay(), r);
		}
	}

	class DataTreeDragSourceListener implements DragSourceListener {

		private final TreeViewer treeViewer;

		public DataTreeDragSourceListener(TreeViewer dataTreeViewer) {
			this.treeViewer = dataTreeViewer;
		}

		@Override
		public void dragStart(DragSourceEvent event) {
			logger.debug("DragStart: " + event);
			// the controller will retrieve the selection in the end.
			LocalSelectionTransfer.getTransfer().setSelection(treeViewer.getSelection());
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			logger.debug("DragSetData: " + event);
			event.data = (IStructuredSelection) treeViewer.getSelection();
		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			logger.debug("DragFinished: " + event);
		}
	}

	class DataSetSelectionDropAdapter implements DropTargetListener {
		private DataSetType setType;

		protected DataSetSelectionDropAdapter(Viewer viewer, DataSetType setType) {
			this.setType = setType;
		}

		@Override
		public void drop(DropTargetEvent event) {
			logger.debug("Dropping on " + setType + ": " + event);
			Runnable r = new Runnable() {
				@Override
				public void run() {
					if (view.isDocumentsTabActive()) {
						addDocumentSelectionToSet();
					} else if (view.isGtTabActive()) {
						addGtSelectionToSet();
					}
				}
			};
			BusyIndicator.showWhile(view.getDisplay(), r);
		}

		private void addDocumentSelectionToSet() {
			switch (setType) {
			case TRAIN:
				controller.addDocumentSelectionToTrainSet();
				break;
			case VALIDATION:
				controller.addDocumentSelectionToValidationSet();
				break;
			default:
				break;
			}
		}

		private void addGtSelectionToSet() {
			switch (setType) {
			case TRAIN:
				controller.addGtSelectionToTrainSet();
				break;
			case VALIDATION:
				controller.addGtSelectionToValidationSet();
				break;
			default:
				break;
			}
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
	}
}
