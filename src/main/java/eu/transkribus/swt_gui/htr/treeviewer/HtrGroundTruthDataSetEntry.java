package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.List;

import eu.transkribus.core.exceptions.NotImplementedException;
import eu.transkribus.core.model.beans.TrpGroundTruthPage;
import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt_gui.htr.treeviewer.HtrGroundTruthContentProvider.GtSetType;

public class HtrGroundTruthDataSetEntry implements IDataSetEntry<TrpHtr, TrpGroundTruthPage> {
	private String pageString;
	private TrpHtr htr;
	private GtSetType gtSetType;
	private List<TrpGroundTruthPage> pages;
	
	public HtrGroundTruthDataSetEntry(TrpHtr htr, final GtSetType setType, List<TrpGroundTruthPage> pages) {
		if(pages == null || pages.size() < 1) {
			throw new IllegalArgumentException("pages argument is null or empty");
		}
		if(setType == null) {
			throw new IllegalArgumentException("GtSetType argument is null");
		}
		if(htr == null) {
			throw new IllegalArgumentException("htr argument is null");
		}
		final int nrOfPages;
		switch(setType) {
		case TRAIN:
			nrOfPages = htr.getNrOfTrainGtPages();
			break;
		case VALIDATION:
			nrOfPages = htr.getNrOfValidationGtPages();
			break;
		default:
			throw new NotImplementedException("Unknown GtSetType argument.");
		}
		List<Boolean> boolList = new ArrayList<>(nrOfPages);
		for (int i = 0; i < nrOfPages; i++) {
			boolList.add(i, Boolean.FALSE);
		}

		for (TrpGroundTruthPage p : pages) {
			boolList.set(p.getPageNr() - 1, Boolean.TRUE);
		}
		this.pageString = CoreUtils.getRangeListStr(boolList);
		this.htr = htr;
		this.gtSetType = setType;
		this.pages = pages;
	}
	
	public int getId() {
		return htr.getHtrId();
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
		return "HTR '" + htr.getName() + "'" + setName;
	}

	public String getPageString() {
		return pageString;
	}

	public void setPageString(String pageString) {
		this.pageString = pageString;
	}

	public TrpHtr getDoc() {
		return htr;
	}

	public void setDoc(TrpHtr htr) {
		this.htr = htr;
	}

	public List<TrpGroundTruthPage> getPages() {
		return pages;
	}

	public void setPages(List<TrpGroundTruthPage> pages) {
		this.pages = pages;
	}

	public GtSetType getGtSetType() {
		return gtSetType;
	}
	
	public void setGtSetType(GtSetType gtSetType) {
		this.gtSetType = gtSetType;
	}
	
	@Override
	public int compareTo(IDataSetEntry<?, ?> o) {
		if(o instanceof HtrGroundTruthDataSetEntry && this instanceof HtrGroundTruthDataSetEntry) {
			if (this.getId() > o.getId()) {
				return 1;
			}
			if (this.getId() < o.getId()) {
				return -1;
			}
			return 0;
		} else 
		return IDataSetEntry.super.compareTo(o);
	}
}
