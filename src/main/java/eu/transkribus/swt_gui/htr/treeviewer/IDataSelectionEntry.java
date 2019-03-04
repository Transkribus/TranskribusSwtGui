package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

/**
 * DataSelectionEntry build from pages of type {@link P} that are accumulated in document-like objects of type {@link D}.
 * An instance of this type represents a user's selection of pages from one {@link D} and is displayed as one row in a {@link DataSetTableWidget}.
 *
 * @param <D>
 * @param <P>
 */
public interface IDataSelectionEntry<D extends Comparable<D>, P> extends Comparable<IDataSelectionEntry<?, ?>> {
	public int getId();
	public String getTitle();
	public String getPageString();
	public void setPageString(String pageString);
	public D getDoc();
	public void setDoc(D doc);
	public List<P> getPages();
	public void setPages(List<P> pages);
	default int compareTo(IDataSelectionEntry<?, ?> o) {
		if(o == null) {
			return 1;
		}
		return this.getClass().getSimpleName()
			.compareTo(o.getClass().getSimpleName());
	}
}
