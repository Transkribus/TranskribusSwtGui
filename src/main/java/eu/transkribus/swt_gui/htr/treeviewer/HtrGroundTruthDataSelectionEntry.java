package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.List;

import eu.transkribus.core.model.beans.enums.DataSetType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.HtrGtDataSetElement;

public class HtrGroundTruthDataSelectionEntry implements IDataSelectionEntry<HtrGtDataSet, HtrGtDataSetElement> {
	private String pageString;
	private HtrGtDataSet htrGtDataSet;
	private List<HtrGtDataSetElement> pages;
	
	public HtrGroundTruthDataSelectionEntry(HtrGtDataSet htrGtDataSet, List<HtrGtDataSetElement> pages) {
		if(pages == null || pages.size() < 1) {
			throw new IllegalArgumentException("pages argument is null or empty");
		}
		if(htrGtDataSet == null) {
			throw new IllegalArgumentException("htrGtDataSet argument is null");
		}
		this.htrGtDataSet = htrGtDataSet;
		this.pages = new ArrayList<>(pages);
		
		final int nrOfPages = htrGtDataSet.getSize();
		List<Boolean> boolList = new ArrayList<>(nrOfPages);
		for (int i = 0; i < nrOfPages; i++) {
			boolList.add(i, Boolean.FALSE);
		}

		for (HtrGtDataSetElement p : pages) {
			boolList.set(p.getGroundTruthPage().getPageNr() - 1, Boolean.TRUE);
		}
		this.pageString = CoreUtils.getRangeListStr(boolList);
	}
	
	public int getId() {
		return htrGtDataSet.getHtr().getHtrId();
	}

	public String getTitle() {
		String setName = "";
		switch(getGtSetType()) {
		case TRAIN:
			setName = " Train Set";
			break;
		case VALIDATION:
			setName = " Validation Set";
			break;
		}
		return "HTR '" + htrGtDataSet.getHtr().getName() + "'" + setName;
	}

	public String getPageString() {
		return pageString;
	}

	public HtrGtDataSet getDoc() {
		return htrGtDataSet;
	}

	public void setDoc(HtrGtDataSet htrGtDataSet) {
		this.htrGtDataSet = htrGtDataSet;
	}

	public List<HtrGtDataSetElement> getPages() {
		return pages;
	}

	public void setPages(List<HtrGtDataSetElement> pages) {
		this.pages = pages;
	}

	public DataSetType getGtSetType() {
		return htrGtDataSet.getDataSetType();
	}
	
	@Override
	public int compareTo(IDataSelectionEntry<?, ?> o) {
		if(o instanceof HtrGroundTruthDataSelectionEntry && this instanceof HtrGroundTruthDataSelectionEntry) {
			return this.getDoc().compareTo((HtrGtDataSet)o.getDoc());
		}
		return IDataSelectionEntry.super.compareTo(o);
	}
}
