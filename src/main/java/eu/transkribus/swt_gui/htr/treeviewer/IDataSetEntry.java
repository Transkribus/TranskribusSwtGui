package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

/**
 * DataSetEntry build from pages of type P that are accumulated in document-like objects of type D
 *
 * @param <D>
 * @param <P>
 */
public interface IDataSetEntry<D, P> extends Comparable<IDataSetEntry<?, ?>> {
	public int getId();
	public String getTitle();
	public String getPageString();
	public void setPageString(String pageString);
	public D getDoc();
	public void setDoc(D doc);
	public List<P> getPages();
	public void setPages(List<P> pages);
	default int compareTo(IDataSetEntry<?, ?> o) {
		if(o == null) {
			return 1;
		}
		return this.getClass().getSimpleName()
			.compareTo(o.getClass().getSimpleName());
	}
}
