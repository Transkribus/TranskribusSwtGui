package eu.transkribus.swt_gui.canvas;

public class CanvasMode {
//	public static enum SplitMode { LINE, HORIZONTAL, VERTICAL };
	
	public final static CanvasMode UNKNOWN = new CanvasMode(false, false, false, false, "UNKNOWN");
	public final static CanvasMode SELECTION = new CanvasMode(false, true, false, false, "SELECTION");
	public final static CanvasMode ZOOM = new CanvasMode(false, false, false, false, "ZOOM");
	public final static CanvasMode LOUPE = new CanvasMode(false, false, false, false, "LOUPE");
	public final static CanvasMode MOVE = new CanvasMode(false, false, true, false, "MOVE");
	// EDIT OPERATIONS:
	public final static CanvasMode RESIZE_BOUNDING_BOX = new CanvasMode(true, false, true, false, "RESIZE_BOUNDING_BOX");
	public final static CanvasMode MOVE_POINT = new CanvasMode(true, true, true, false, "MOVE_POINT");
	
	public final static CanvasMode MOVE_SHAPE = new CanvasMode(true, false, true, false, "MOVE_SHAPE");
	public final static CanvasMode ADD_POINT = new CanvasMode(true, false, false, false, "ADD_POINT");
	public final static CanvasMode REMOVE_POINT = new CanvasMode(true, true, false, false, "REMOVE_POINT");
	public final static CanvasMode REMOVE_SHAPE = new CanvasMode(true, false, false, false, "REMOVE_SHAPE");
	
//	public final static CanvasMode SPLIT_SHAPE = new CanvasMode(true, false, false, false, "SPLIT_SHAPE");
	
	public final static CanvasMode SPLIT_SHAPE_LINE = new CanvasMode(true, false, false, false, "SPLIT_SHAPE_LINE");
	public final static CanvasMode SPLIT_SHAPE_BY_VERTICAL_LINE = new CanvasMode(true, false, false, false, "SPLIT_SHAPE_BY_VERTICAL_LINE");
	public final static CanvasMode SPLIT_SHAPE_BY_HORIZONTAL_LINE = new CanvasMode(true, false, false, false, "SPLIT_SHAPE_BY_HORIZONTAL_LINE");
		
//	public final static CanvasMode ADD_SHAPE = new CanvasMode(true, false, false, true, "ADD_SHAPE");
	
	public static final CanvasMode ADD_PRINTSPACE = new CanvasMode(true, false, false, true, "Printspace");
	public static final CanvasMode ADD_TEXTREGION = new CanvasMode(true, false, false, true, "TextRegion");
	public static final CanvasMode ADD_LINE = new CanvasMode(true, false, false, true, "Line");
	public static final CanvasMode ADD_BASELINE = new CanvasMode(true, false, false, true, "Baseline");
	public static final CanvasMode ADD_WORD = new CanvasMode(true, false, false, true, "Word");
	public static final CanvasMode ADD_TABLEREGION = new CanvasMode(true, false, false, true, "TableRegion");
	public static final CanvasMode ADD_OTHERREGION = new CanvasMode(true, false, false, true, "Region");
	public static final CanvasMode ADD_TABLECELL = new CanvasMode(true, false, false, true, "TableCell");
	
	public static final CanvasMode ADD_ARTICLE = new CanvasMode(true, false, false, true, "Article");
	
	boolean isAddOperation;
	boolean isEditOperation;
	boolean highlightPointsRequired;
	boolean endsWithMouseUp;
	String description="";
	public Object data;
		
	protected CanvasMode(boolean isEditOperation, boolean highlightPointsRequired, boolean endsWithMouseUp, boolean isAddOperation, String description) {
		this.isEditOperation = isEditOperation;
		this.highlightPointsRequired = highlightPointsRequired;
		this.endsWithMouseUp = endsWithMouseUp;
		this.isAddOperation = isAddOperation;
		this.description = description;
	}
	
	public boolean isSplitOperation() {
		return this.equals(SPLIT_SHAPE_LINE) || this.equals(SPLIT_SHAPE_BY_VERTICAL_LINE) || this.equals(SPLIT_SHAPE_BY_HORIZONTAL_LINE);
	}
	
	public boolean isEditOperation() {
		return isEditOperation;
	}
	
	public boolean isHighlightPointsRequired() {
		return highlightPointsRequired;
	}
	
	public boolean isEndsWithMouseUp() {
		return endsWithMouseUp;
	}
	
	public boolean isAddOperation() {
		return isAddOperation;
	}
	
	public String getDescription() { return description; }
	public void setDescription(String description) {
		this.description = description != null ? description : "";
	}
	
	@Override public String toString() {
		return "CanvasMode: "+description;
	}
}
