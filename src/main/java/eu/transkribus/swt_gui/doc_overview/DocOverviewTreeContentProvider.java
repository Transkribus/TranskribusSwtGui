package eu.transkribus.swt_gui.doc_overview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;

public class DocOverviewTreeContentProvider implements ITreeContentProvider {
	TrpPageType page;

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof ArrayList<?>) {
//			this.page = (TrpPageType) newInput;
		}
		
	}

	@Override
	public Object[] getElements(Object inputElement) {
//		if (inputElement instanceof TrpPageType) {
//			TrpPageType page = (TrpPageType) inputElement;
//			Object[] elements = new Object[page.getPrintSpace()==null ? page.getRegions().size() : page.getRegions().size()+1];
//			int i=0;
//			if (page.getPrintSpace()!=null)
//				elements[i++] = page.getPrintSpace();
//			for (TrpTextRegionType r : page.getRegions()) {
//				elements[i++] = r;
//			}
//			
//			return elements;
//		}
		return null;
	}
	
	public List<Object> getSubElements(Object parent) {
//		List<Object> subElements = new ArrayList<Object>();
//		if (hasChildren(parent)) {
//			for (Object c : getChildren(parent)) {
//				for (Object c1 : getSubElements(c)) {
//					subElements.add(c1);
//				}
//				subElements.add(c);
//			}
//		}
//		return subElements
		return null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
//		if (parentElement instanceof TrpPageType) {
//			return getElements(parentElement);
//		}
//		else if (parentElement instanceof TrpTextRegionType) {
//			return ((TrpTextRegionType)parentElement).getTextLine().toArray();
//		}
//		else if (parentElement instanceof TrpTextLineType) {
//			TrpTextLineType line = (TrpTextLineType) parentElement;
//			Object[] elements = new Object[line.getBaseline()==null ? line.getWord().size() : line.getWord().size()+1];
//			int i=0;
//			if (line.getBaseline()!=null)
//				elements[i++] = line.getBaseline();
//			for (WordType w : line.getWord()) {
//				elements[i++] = w;
//			}
//			
//			return elements;
//		}
//		else if (parentElement instanceof TrpBaselineType) {
//			return new Object[0];
//		}		
//		else if (parentElement instanceof TrpWordType) {
//			return new Object[0];
//		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
//		if (element instanceof TrpPageType) {
//			return null;
//		}
//		else if (element instanceof TrpTextRegionType) {
//			return ((TrpTextRegionType)element).getPage();
//		}
//		else if (element instanceof TrpTextLineType) {
//			return ((TrpTextLineType) element).getRegion();
//		}
//		else if (element instanceof TrpBaselineType) {
//			return ((TrpBaselineType)element).getLine();
//		}		
//		else if (element instanceof TrpWordType) {
//			return ((TrpWordType)element).getLine();
//		}
//
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
//		if (element instanceof TrpPageType) {
//			return !((TrpPageType)element).getRegions().isEmpty();
//		}
//		else if (element instanceof TrpTextRegionType) {
//			return !((TrpTextRegionType)element).getTextLine().isEmpty();
//		}
//		else if (element instanceof TrpTextLineType) {
//			return !((TrpTextLineType) element).getWord().isEmpty();
//		}
//		else if (element instanceof TrpBaselineType) {
//			return false;
//		}		
//		else if (element instanceof TrpWordType) {
//			return false;
//		}
//
		return false;
	}
	
	

}
