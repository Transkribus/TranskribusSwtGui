package eu.transkribus.util;

import java.util.List;

import eu.transkribus.core.rest.JobConst;
import eu.transkribus.swt_gui.htr.HtrDictionaryComposite;

public class TextRecognitionConfig {
	
	private final Mode mode;
	//common
	private String language;
	//CITLab and UPVLC, TODO replace with LangResource-ID
	private String dictionary;
	//CITlab and UPVLC
	private int htrId;
	private String htrName;
	private List<String> structures;

	private boolean doLinePolygonSimplification = true;
	private boolean keepOriginalLinePolygons = false;
	private boolean doStoreConfMats = true;
	private boolean clearLines = true;
	private int batchSize = 10;
	
	public TextRecognitionConfig(Mode mode) {
		this.mode = mode;
	}
	
	public boolean isDoLinePolygonSimplification() {
		return doLinePolygonSimplification;
	}

	public void setDoLinePolygonSimplification(boolean doLinePolygonSimplification) {
		this.doLinePolygonSimplification = doLinePolygonSimplification;
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

	@Override
	public String toString() {
		String s = "";
		
		switch(mode) {
		case CITlab:			
			s = "CITlab HTR "+htrId+"\n"
					+ "\t- Net Name: " + htrName + "\n"
					+ "\t- Language: " + language + "\n"
					+ "\t- " + getDictLabel(dictionary);
			break;
		case UPVLC:
			s = "PyLaia HTR "+htrId+"\n"
					+ "\t- Name: " + htrName + "\n"
					+ "\t- Language: " + language+ "\n" 
					+ "\t- " + getDictLabel(dictionary);
//			s = "PyLaia HTR\nNet Name: " + htrName + "\nLanguage: " + language;			
			break;
		default:
			s = "Could not load configuration!";
			break;
		}
		
		return s;
	}
	
	private String getDictLabel(String dictionary) {
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

	public enum Mode {
		CITlab, UPVLC;
	}
}
