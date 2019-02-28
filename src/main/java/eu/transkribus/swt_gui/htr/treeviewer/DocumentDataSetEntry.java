package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.util.CoreUtils;

public class DocumentDataSetEntry implements IDataSetEntry<TrpDocMetadata, TrpPage> {
	private String pageString;
	private TrpDocMetadata doc;
	private List<TrpPage> pages;

	public DocumentDataSetEntry(TrpDocMetadata doc, List<TrpPage> pages) {
		Collections.sort(pages);
		final int nrOfPages = doc.getNrOfPages();
		List<Boolean> boolList = new ArrayList<>(nrOfPages);
		for (int i = 0; i < nrOfPages; i++) {
			boolList.add(i, Boolean.FALSE);
		}

		for (TrpPage p : pages) {
			boolList.set(p.getPageNr() - 1, Boolean.TRUE);
		}
		this.pageString = CoreUtils.getRangeListStr(boolList);
		this.pages = pages;
		this.doc = doc;
	}

	public int getId() {
		return doc.getDocId();
	}

	public String getTitle() {
		return doc.getTitle();
	}

	public String getPageString() {
		return pageString;
	}

	public void setPageString(String pageString) {
		this.pageString = pageString;
	}

	public TrpDocMetadata getDoc() {
		return doc;
	}

	public void setDoc(TrpDocMetadata doc) {
		this.doc = doc;
	}

	public List<TrpPage> getPages() {
		return pages;
	}

	public void setPages(List<TrpPage> pages) {
		this.pages = pages;
	}

	@Override
	public int compareTo(IDataSetEntry<?, ?> o) {
		if(o instanceof DocumentDataSetEntry && this instanceof DocumentDataSetEntry) {
			if (this.doc.getDocId() > o.getId()) {
				return 1;
			}
			if (this.doc.getDocId() < o.getId()) {
				return -1;
			}
			return 0;
		} 
		return this.compareTo(o);
	}
}
