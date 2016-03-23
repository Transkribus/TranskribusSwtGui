package eu.transkribus.swt_gui.search.kws;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.KwsDocHit;
import eu.transkribus.core.model.beans.KwsPageHit;

public class KwsTreeContentProvider implements ITreeContentProvider {
	private static final Logger logger = LoggerFactory.getLogger(KwsTreeContentProvider.class);

	List<KwsDocHit> hits;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {		
		if (newInput instanceof List<?>) {
			this.hits = (List<KwsDocHit>) newInput;
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof List<?>) {
			return ((List<KwsDocHit>) inputElement).toArray();			
		} 
		else if (inputElement instanceof KwsDocHit) {
			return new Object[] { ((KwsDocHit) inputElement).getHitList().toArray() };
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof KwsDocHit) {
			logger.debug("getting children elements for: "+parentElement);
//			return getElements(parentElement);
			return ((KwsDocHit) parentElement).getHitList().toArray();
		}
		else if (parentElement instanceof KwsPageHit) {
			return ((KwsPageHit)parentElement).getHitList().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
//		if (element instanceof List<?>) {
//			return null;
//		}
//		else if (element instanceof KwsDocHit) {
//			return ((KwsDocHit)element).getParent();
//		}

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof KwsDocHit) {
			return !((KwsDocHit)element).getHitList().isEmpty();
		}
		else if (element instanceof KwsPageHit) {
			return !((KwsPageHit)element).getHitList().isEmpty();
		}

		return false;
	}
	
	

}
