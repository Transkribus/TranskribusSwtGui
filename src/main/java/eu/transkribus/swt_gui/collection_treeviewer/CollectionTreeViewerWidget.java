package eu.transkribus.swt_gui.collection_treeviewer;

import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;

/**
 * Not used right now...
 * 
 * @author philip
 *
 */
@Deprecated
public class CollectionTreeViewerWidget {
	private static final Logger logger = LoggerFactory.getLogger(CollectionTreeViewerWidget.class);
	private TreeViewer tv;
	private CollectionContentProvider contentProv;
	private CollectionLabelProvider labelProv;
	
	public CollectionTreeViewerWidget(Composite parent, int style) {
		tv = new TreeViewer(parent, style);
		contentProv = new CollectionContentProvider();
		labelProv = new CollectionLabelProvider();
		tv.setContentProvider(contentProv);
		tv.setLabelProvider(labelProv);
		
		tv.addDoubleClickListener(new IDoubleClickListener(){
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object o = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if(o instanceof TrpDocMetadata) {
					for(TreeItem i : tv.getTree().getItems()) {
						if(i.getData().equals(o)) {
							tv.setExpandedState(o, !i.getExpanded());
							return;
						}
					}
				}
			}
		});
	}

	public void setLayoutData(Object layoutData) {
		tv.getTree().setLayoutData(layoutData);
	}
	
	public void setInput(List<TrpDocMetadata> docList) {
		tv.setInput(docList);
	}
}
