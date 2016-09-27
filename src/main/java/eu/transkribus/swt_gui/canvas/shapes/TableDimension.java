package eu.transkribus.swt_gui.canvas.shapes;

public enum TableDimension {
	ROW(0), COLUMN(1);
	
	public final int val;
	
	private TableDimension(int val) {
		this.val = val;
	}
}