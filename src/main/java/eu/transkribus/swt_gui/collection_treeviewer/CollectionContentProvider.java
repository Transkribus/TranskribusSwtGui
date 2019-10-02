package eu.transkribus.swt_gui.collection_treeviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	Map<TrpDocMetadata, List<TrpPage>> docMap;
	
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
			this.docMap = new HashMap<>();
			for(TrpDocMetadata d : (List<TrpDocMetadata>) newInput) {
				this.docMap.put(d, new ArrayList<>(0));
			}
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List<?>) {
			return ((List<TrpDocMetadata>) inputElement).toArray();			
		} 
		else if (inputElement instanceof TrpDocMetadata) {
			return getPagesOfDoc((TrpDocMetadata) inputElement).toArray();		
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof TrpDocMetadata) {
			return getChildren((TrpDocMetadata)parentElement);
		}
		return null;
	}
	
	public TrpPage[] getChildren(TrpDocMetadata docMd) {
		List<TrpPage> pageList = getPagesOfDoc(docMd);
		return pageList.toArray(new TrpPage[pageList.size()]);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof List<?>) {
			return null;
		}
		else if (element instanceof TrpPage) {
			final int docId = ((TrpPage)element).getDocId();
			for(TrpDocMetadata d : docMap.keySet()) {
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
	
	protected List<TrpPage> getPagesOfDoc(TrpDocMetadata docMd) {
		if(!docMap.get(docMd).isEmpty()) {
			logger.debug("Returning cached pages for docId = {}", docMd.getDocId());
			return docMap.get(docMd);
		}
		List<TrpPage> pages;
		try {
			logger.debug("Fetching pages for docId = {}", docMd.getDocId());
			TrpDoc doc = store.getRemoteDoc(super.getCollId(), docMd.getDocId(), -1);
			pages = doc.getPages();
			docMap.put(docMd, pages);
		} catch (SessionExpiredException | IllegalArgumentException | NoConnectionException e) {
			logger.error("No Connection!");
			pages = new ArrayList<>(0);
		}
		return pages;
	}
}
