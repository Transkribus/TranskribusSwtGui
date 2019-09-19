package eu.transkribus.util;

import java.util.List;

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
	private List<String> regions;

	private boolean doLinePolygonSimplification = true;
	private boolean keepOriginalLinePolygons = false;
	private boolean doStoreConfMats = true;
	
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
	
	public List<String> getRegions() {
		return regions;
	}

	public void setRegions(List<String> regions) {
		this.regions = regions;
	}

	@Override
	public String toString() {
		String s = "";
		
		switch(mode) {
		case CITlab:
			s = "CITlab RNN HTR\nNet Name: " + htrName + "\nLanguage: " + language + "\nDictionary: " 
					+ (dictionary == null ? HtrDictionaryComposite.NO_DICTIONARY : dictionary);
			break;
		case UPVLC:
			s = "This mode is not implemented.";
			break;
		default:
			s = "Could not load configuration!";
			break;
		}
		
		return s;
	}

	public enum Mode {
		CITlab, UPVLC;
	}
}
