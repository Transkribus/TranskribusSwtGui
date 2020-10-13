package eu.transkribus.util;

import java.util.List;

import eu.transkribus.core.model.beans.enums.CreditSelectionStrategy;
import eu.transkribus.core.rest.JobConst;
import eu.transkribus.swt_gui.htr.HtrDictionaryComposite;

public class TextRecognitionConfig {
	
	private final Mode mode;
	//common
	private String language;
	//CITLab and UPVLC, TODO replace with LangResource-ID
	private String dictionary;
	private String languageModel;
	//CITlab and UPVLC
	private int htrId;
	private String htrName;
	private List<String> structures;

	private boolean useExistingLinePolygons = false;
	private boolean doLinePolygonSimplification = true;
	private boolean keepOriginalLinePolygons = false;
	private boolean doStoreConfMats = true;
	private boolean clearLines = true;
	private boolean doWordSeg = true;
	private int batchSize = 10;
	private CreditSelectionStrategy creditSelectionStrategy;
	
	public TextRecognitionConfig(Mode mode) {
		this.mode = mode;
	}
	
	public boolean isDoLinePolygonSimplification() {
		return doLinePolygonSimplification;
	}

	public void setDoLinePolygonSimplification(boolean doLinePolygonSimplification) {
		this.doLinePolygonSimplification = doLinePolygonSimplification;
	}

	public boolean isUseExistingLinePolygons() {
		return useExistingLinePolygons;
	}

	public void setUseExistingLinePolygons(boolean useExistingLinePolygons) {
		this.useExistingLinePolygons = useExistingLinePolygons;
	}

	public boolean isKeepOriginalLinePolygons() {
		return keepOriginalLinePolygons;
	}



	public void setKeepOriginalLinePolygons(boolean keepOriginalLinePolygons) {
		this.keepOriginalLinePolygons = keepOriginalLinePolygons;
	}

	public boolean isDoStoreConfMats() {
		return doStoreConfMats;
	}
	
	public void setDoStoreConfMats(boolean doStoreConfMats) {
		this.doStoreConfMats = doStoreConfMats;
	}
	
	public boolean isClearLines() {
		return clearLines;
	}

	public void setClearLines(boolean clearLines) {
		this.clearLines = clearLines;
	}
	
	public boolean isDoWordSeg() {
		return doWordSeg;
	}

	public void setDoWordSeg(boolean doWordSeg) {
		this.doWordSeg = doWordSeg;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	public int getBatchSize() {
		return batchSize;
	}

	public Mode getMode() {
		return mode;
	}


	public String getLanguage() {
		return language;
	}



	public void setLanguage(String language) {
		this.language = language;
	}


	public String getDictionary() {
		return dictionary;
	}



	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}



	public int getHtrId() {
		return htrId;
	}



	public void setHtrId(int htrId) {
		this.htrId = htrId;
	}



	public String getHtrName() {
		return htrName;
	}



	public void setHtrName(String htrName) {
		this.htrName = htrName;
	}
	
	public List<String> getStructures() {
		return structures;
	}

	public void setStructures(List<String> structures) {
		this.structures = structures;
	}
	

	public String getLanguageModel() {
		return languageModel;
	}

	public void setLanguageModel(String languageModel) {
		this.languageModel = languageModel;
	}

	@Override
	public String toString() {
		return getProviderString() + " HTR "+htrId+"\n"
				+ "\t- Net Name: " + htrName + "\n"
				+ "\t- Language: " + language + "\n"
				+ "\t- " + getDictOrLMLabel();
	}
	
	private String getProviderString() {
		return mode == Mode.CITlab ? "CITlab" : "PyLaia";
	}
	
	private String getDictOrLMLabel() {
		return languageModel != null ? getLMLabel() : getDictLabel(); 
	}
	
	private String getLMLabel() {
		if(languageModel == null) {
			return "Language Model: "+HtrDictionaryComposite.NO_DICTIONARY;
		}
		else if (JobConst.PROP_TRAIN_DATA_LM_VALUE.equals(languageModel)) {
			return HtrDictionaryComposite.INTEGRATED_LM;
		}
		else {
			return "Language Model: "+languageModel;
		}
	}	
	
	private String getDictLabel() {
		String dictLabel;
		if(dictionary == null) {
			dictLabel = "Language Model: "+HtrDictionaryComposite.NO_DICTIONARY;
		} else if (JobConst.PROP_TRAIN_DATA_DICT_VALUE.equals(dictionary)) {
			dictLabel = HtrDictionaryComposite.INTEGRATED_DICTIONARY;
		} else if (JobConst.PROP_TRAIN_DATA_LM_VALUE.equals(dictionary)) {
			dictLabel = HtrDictionaryComposite.INTEGRATED_LM;
		} else {
			dictLabel = "Dictionary: " + dictionary;
		}
		return dictLabel;
	}

	public CreditSelectionStrategy getCreditSelectionStrategy() {
		return creditSelectionStrategy;
	}

	public void setCreditSelectionStrategy(CreditSelectionStrategy creditSelectionStrategy) {
		this.creditSelectionStrategy = creditSelectionStrategy;
	}

	public enum Mode {
		CITlab, UPVLC;
	}


}
