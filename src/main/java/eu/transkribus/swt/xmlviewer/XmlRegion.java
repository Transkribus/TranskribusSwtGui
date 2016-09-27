package eu.transkribus.swt.xmlviewer;

/**
 * A XML region, with a type, a start position (included) and an end position
 * (excluded).
 * <p>
 * A XML region is limited in the range [start, end[
 * </p>
 * 
 * @author Vincent Zurczak
 * @version 1.0 (tag version)
 */
public class XmlRegion {
	public enum XmlRegionType {
		INSTRUCTION, COMMENT, CDATA, MARKUP, ATTRIBUTE, MARKUP_VALUE, ATTRIBUTE_VALUE, WHITESPACE, UNEXPECTED;
	}

	private final XmlRegionType xmlRegionType;
	private final int start;
	private int end;

	/**
	 * Constructor.
	 * 
	 * @param xmlRegionType
	 * @param start
	 */
	public XmlRegion(XmlRegionType xmlRegionType, int start) {
		this.xmlRegionType = xmlRegionType;
		this.start = start;
	}

	/**
	 * Constructor.
	 * 
	 * @param xmlRegionType
	 * @param start
	 * @param end
	 */
	public XmlRegion(XmlRegionType xmlRegionType, int start, int end) {
		this(xmlRegionType, start);
		this.end = end;
	}

	/**
	 * @return the end
	 */
	public int getEnd() {
		return this.end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(int end) {
		this.end = end;
	}

	/**
	 * @return the xmlRegionType
	 */
	public XmlRegionType getXmlRegionType() {
		return this.xmlRegionType;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		return this.start;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object #toString()
	 */
	@Override
	public String toString() {
		return this.xmlRegionType + " [" + this.start + ", " + this.end + "[";
	}
}