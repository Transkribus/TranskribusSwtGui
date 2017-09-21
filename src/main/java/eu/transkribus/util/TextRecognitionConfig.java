package eu.transkribus.util;

import eu.transkribus.swt_gui.htr.HtrDictionaryComposite;
import eu.transkribus.swt_gui.htr.HtrTextRecognitionConfigDialog_Old;

public class TextRecognitionConfig {
	
	private final Mode mode;
	//common
	private String language;
	//CITLab and UPVLC, TODO replace with LangResource-ID
	private String dictionary;
	//CITlab and UPVLC
	private int htrId;
	private String htrName;
	
	public TextRecognitionConfig(Mode mode) {
		this.mode = mode;
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
