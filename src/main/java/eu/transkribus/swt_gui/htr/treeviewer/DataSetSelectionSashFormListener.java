package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;

public class DataSetSelectionSashFormListener {
	private final DataSetSelectionHandler handler;
	private DataSetSelectionSashForm view;
	private final IDoubleClickListener treeViewerDoubleClickListener;
	private final ISelectionChangedListener treeViewerSelectionChangedListener;
	
	DataSetSelectionSashFormListener(DataSetSelectionSashForm view, DataSetSelectionHandler handler) {
		this.view = view;
		this.handler = handler;
		treeViewerDoubleClickListener = new TreeViewerDoubleClickListener();
		treeViewerSelectionChangedListener = new TreeViewerSelectionChangedListener();
		addListeners(view);
	}
	
	private void addListeners(DataSetSelectionSashForm view) {
		view.docTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
		view.docTv.addDoubleClickListener(treeViewerDoubleClickListener);
		
		view.groundTruthTv.addSelectionChangedListener(treeViewerSelectionChangedListener);
		view.groundTruthTv.addDoubleClickListener(treeViewerDoubleClickListener);
		
		view.addToTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(view.documentsTabItem.equals(view.dataTabFolder.getSelection())) {
					handler.addDocumentSelectionToTrainSet();
				} else if (view.gtTabItem.equals(view.dataTabFolder.getSelection())) {
					handler.addGtSelectionToTrainSet();
				}
			}
		});

		view.addToTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(view.documentsTabItem.equals(view.dataTabFolder.getSelection())) {
					handler.addDocumentSelectionToValidationSet();
				} else if (view.gtTabItem.equals(view.dataTabFolder.getSelection())) {
					handler.addGtSelectionToValidationSet();
				}
			}
		});

		view.removeFromTrainSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<IDataSelectionEntry<?, ?>> entries = view.trainSetOverviewTable.getSelectedDataSets();
				handler.removeSelectionFromTrainSet(entries);
			}
		});

		view.removeFromTestSetBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				List<IDataSelectionEntry<?, ?>> entries = view.testSetOverviewTable.getSelectedDataSets();
				handler.removeSelectionFromTestSet(entries);
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
			handler.updateThumbnail(selection);
		}
	};
	
	/**
	 * Expands items that have children on double click. Leaf elements are displayed.
	 *
	 */
	public class TreeViewerDoubleClickListener implements IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			Object o = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (o instanceof TrpDocMetadata) {
				expandTreeItem(o, view.docTv);
			} else if (o instanceof TrpPage) {
				handler.loadPageInMainWidget((TrpPage)o);
			} else if (o instanceof TrpHtr || o instanceof HtrGtDataSet) {
				expandTreeItem(o, view.groundTruthTv);
			}
		}
		private void expandTreeItem(Object o, TreeViewer tv) {
			final ITreeContentProvider provider = (ITreeContentProvider) tv.getContentProvider();
			if(!provider.hasChildren(o)) {
				return;
			}
			if (tv.getExpandedState(o)) {
				tv.collapseToLevel(o, AbstractTreeViewer.ALL_LEVELS);
			} else {
				tv.expandToLevel(o, 1);
			}
		}
	}
}
