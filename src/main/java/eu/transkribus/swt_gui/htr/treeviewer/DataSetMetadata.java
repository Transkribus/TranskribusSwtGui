package eu.transkribus.swt_gui.htr.treeviewer;

import org.apache.commons.lang.StringUtils;

public class DataSetMetadata {
	private final int pages;
	private final int lines;
	private final int words;

	public DataSetMetadata(int pages, int lines, int words) {
		this.pages = pages;
		this.lines = lines;
		this.words = words;
	}

	public int getPages() {
		return pages;
	}

	public int getLines() {
		return lines;
	}

	public int getWords() {
		return words;
	}
	
	/**
	 * FIXME this String needs still be rendered in a monospaced font...
	 * 
	 * @param title
	 * @param indentSize
	 * @return
	 */
	public String toFormattedString(String title, final int indentSize) {
		if(title == null) {
			title = "";
		}
		return StringUtils.rightPad(title, indentSize) + getPages() + " pages\n"
			+ StringUtils.rightPad("", indentSize) + getLines() + " lines\n"
			+ StringUtils.rightPad("", indentSize) + getWords() + " words\n";
	}
}