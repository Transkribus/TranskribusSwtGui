package eu.transkribus.util;

import java.util.LinkedList;
import java.util.List;

import eu.transkribus.core.model.beans.enums.ScriptType;
import eu.transkribus.core.util.FinereaderUtils;

public class OcrConfig {
	
	private List<String> languages = new LinkedList<>();
	private ScriptType typeFace;
	
	public OcrConfig() {
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	
	public String getLanguageString() {
		String langStr = "";
		boolean isFirst = true;
		for(String l : languages) {
			if(isFirst) {
				langStr += l;
				isFirst = false;
			} else {
				langStr += "," + l;
			}
		}
		return langStr;
	}
	
	public void setLanguages(String langStr) {
		if(langStr.contains(",")) {
			String[] langs = langStr.split(",");
			for(String l : langs) {
				if(FinereaderUtils.isFinreaderLanguage(l)) {
					continue;
				}
				languages.add(l);
			}
		} else {
			languages.add(langStr);
		}
	}

	public ScriptType getTypeFace() {
		return typeFace;
	}

	public void setTypeFace(ScriptType typeFace) {
		this.typeFace = typeFace;
	}
	
}
