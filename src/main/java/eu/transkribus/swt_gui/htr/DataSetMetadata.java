package eu.transkribus.swt_gui.htr;

import org.apache.commons.lang.StringUtils;

public class DataSetMetadata {
	private String label;
	private final int pages;
	private final int lines;
	private final int words;

	/**
	 * @deprecated use constructor that sets a label
	 */
	public DataSetMetadata(int pages, int lines, int words) {
		this("N/A", pages, lines, words);
	}
	
	public DataSetMetadata(String label, int pages, int lines, int words) {
		this.label = label;
		this.pages = pages;
		this.lines = lines;
		this.words = words;
	}
	
	public String getLabel() {
		return label;
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
	 * @deprecated formatted String display breaks depending on OS and default font! Data is displayed in a {@link DataSetMetadataTableWidget} now.
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