package eu.transkribus.swt_gui.collection_treeviewer;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.exceptions.NoConnectionException;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.util.ACollectionBoundStructuredContentProvider;

public class CollectionContentProvider extends ACollectionBoundStructuredContentProvider implements ITreeContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(CollectionContentProvider.class);
	List<TrpDocMetadata> docs;
	
	public CollectionContentProvider() {
		super(null);
	}
	
	public CollectionContentProvider(int colId) {
		super(colId);
	}
	
	@Override
	public void dispose() {}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
		if (newInput instanceof List<?>) {
			this.docs = (List<TrpDocMetadata>) newInput;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<TrpDocMetadata>) inputElement).toArray();			
		} 
		else if (inputElement instanceof TrpDocMetadata) {
			TrpDoc doc;
			try {
				doc = store.getRemoteDoc(super.getCollId(), ((TrpDocMetadata) inputElement).getDocId(), -1);
				return doc.getPages().toArray();
			} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
				logger.error("No Connection!");
				return new Object[] {};
			}
			
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TrpDocMetadata) {
			TrpDoc doc;
			try {
				doc = store.getRemoteDoc(super.getCollId(), ((TrpDocMetadata) parentElement).getDocId(), -1);
				return doc.getPages().toArray();
			} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
				logger.error("No Connection!");
				return new Object[] {};
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof List<?>) {
			return null;
		}
		else if (element instanceof TrpPage) {
			final int docId = ((TrpPage)element).getDocId();
			for(TrpDocMetadata d : docs) {
				if(d.getDocId() == docId) {
					return d;
				}
			}
		}

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof TrpDocMetadata) {
			return true;
		}

		return false;
	}
}
