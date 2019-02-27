package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.List;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;

public abstract class DataSetEntry implements Comparable<DataSetEntry> {
	
	public abstract int getId();

	public abstract String getTitle();

	public abstract String getPageString();

	public abstract void setPageString(String pageString);

	public abstract TrpDocMetadata getDoc();

	public abstract void setDoc(TrpDocMetadata doc);

	public abstract List<TrpPage> getPages();

	public abstract void setPages(List<TrpPage> pages);

	@Override
	public int compareTo(DataSetEntry o) {
		return 0;

//		if(o instanceof DataSetEntry && this instanceof DataSetEntry) {
//			if (this.doc.getDocId() > o.getId()) {
//				return 1;
//			}
//			if (this.doc.getDocId() < o.getId()) {
//				return -1;
//			}
//		}
//		return 0;
	}
}
