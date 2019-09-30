package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.List;

import eu.transkribus.core.model.beans.enums.DataSetType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.DescriptorUtils.AGtDataSet;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.AGtDataSetElement;

public class GroundTruthDataSelectionEntry implements IDataSelectionEntry<AGtDataSet<?>, AGtDataSetElement<?>> {
	private String pageString;
	private AGtDataSet<?> gtDataSet;
	private List<AGtDataSetElement<?>> pages;
	
	public GroundTruthDataSelectionEntry(AGtDataSet<?> gtDataSet, List<AGtDataSetElement<?>> pages) {
		if(pages == null || pages.size() < 1) {
			throw new IllegalArgumentException("pages argument is null or empty");
		}
		if(gtDataSet == null) {
			throw new IllegalArgumentException("htrGtDataSet argument is null");
		}
		this.gtDataSet = gtDataSet;
		this.pages = new ArrayList<>(pages);
		
		final int nrOfPages = gtDataSet.getSize();
		List<Boolean> boolList = new ArrayList<>(nrOfPages);
		for (int i = 0; i < nrOfPages; i++) {
			boolList.add(i, Boolean.FALSE);
		}

		for (AGtDataSetElement<?> p : pages) {
			boolList.set(p.getGroundTruthPage().getPageNr() - 1, Boolean.TRUE);
		}
		this.pageString = CoreUtils.getRangeListStr(boolList);
	}
	
	public int getId() {
		return gtDataSet.getId();
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
		return gtDataSet.getTypeLabel() + " '" + gtDataSet.getName() + "'" + setName;
	}

	public String getPageString() {
		return pageString;
	}

	public AGtDataSet<?> getDoc() {
		return gtDataSet;
	}

	public void setDoc(AGtDataSet<?> htrGtDataSet) {
		this.gtDataSet = htrGtDataSet;
	}

	public List<AGtDataSetElement<?>> getPages() {
		return pages;
	}

	public void setPages(List<AGtDataSetElement<?>> pages) {
		this.pages = pages;
	}

	public DataSetType getGtSetType() {
		return gtDataSet.getDataSetType();
	}
	
	@Override
	public int compareTo(IDataSelectionEntry<?, ?> o) {
		if(o instanceof GroundTruthDataSelectionEntry && this instanceof GroundTruthDataSelectionEntry) {
			return this.getDoc().compareTo((AGtDataSet<?>) o.getDoc());
		}
		return IDataSelectionEntry.super.compareTo(o);
	}
}
