package eu.transkribus.swt_gui.htr.treeviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.CoreUtils;

public class DocumentDataSelectionEntry implements IDataSelectionEntry<TrpDocMetadata, TrpPage> {
	private static final Logger logger = LoggerFactory.getLogger(DocumentDataSelectionEntry.class);
	
	private String pageString;
	private TrpDocMetadata doc;
	private List<TrpPage> pages;
	
	private TrainDataValidator validator;

	public DocumentDataSelectionEntry(TrpDocMetadata doc, List<TrpPage> pages, TrainDataValidator validator) {
		if(validator == null) {
			//use base impl
			this.validator = new TrainDataValidator();
		} else {
			this.validator = validator;
		}
		this.pages = new ArrayList<>(pages);
		this.doc = doc;
		Collections.sort(this.pages);
		pageString = computePageStr(null);
	}
	
	public DocumentDataSelectionEntry(TrpDocMetadata doc, List<TrpPage> pages) {
		this(doc, pages, null);
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
	
	/**
	 * This method allows the LabelProvider to request a pageString depending on a filter setting in the view
	 * 
	 * @param statusFilter
	 * @return
	 */
	public String getPageString(EditStatus statusFilter) {
		return computePageStr(statusFilter);
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
	
	private String computePageStr(EditStatus status) {
		final int nrOfPages = doc.getNrOfPages();
		List<Boolean> boolList = new ArrayList<>(nrOfPages);
		for (int i = 0; i < nrOfPages; i++) {
			boolList.add(i, Boolean.FALSE);
		}

		for (TrpPage p : pages) {
			logger.debug("Checking page nr. {} for status {}", p, status);
			if(validator.isQualifiedForTraining(p, status)) {
				boolList.set(p.getPageNr() - 1, Boolean.TRUE);
			}
		}
		return CoreUtils.getRangeListStr(boolList);
	}

	@Override
	public int compareTo(IDataSelectionEntry<?, ?> o) {
		if(o instanceof DocumentDataSelectionEntry && this instanceof DocumentDataSelectionEntry) {
			return this.getDoc().compareTo((TrpDocMetadata)o.getDoc());
		}
		return IDataSelectionEntry.super.compareTo(o);
	}
}
