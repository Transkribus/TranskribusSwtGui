package eu.transkribus.swt_canvas.canvas.shapes;


public enum CanvasShapeType {
	POLYGON("Polygon"),
	POLYLINE("Polyline"),
	RECTANGLE("Rectangle");
	
	String desc;
	
	private CanvasShapeType(String desc) {
		this.desc = desc;
	}
	
	@Override
	public String toString() { return desc; }
	
	public static CanvasShapeType fromString(String str) {
		for (CanvasShapeType t : CanvasShapeType.values()) {
			if (t.toString().equals(str)) {
				return t;
			}
		}
		return null;	
	}
}
