package eu.transkribus.swt_gui.canvas.shapes;

import java.awt.Point;

public class ShapePoint {
	java.awt.Point p;
	int index;
	
	public ShapePoint(Point p, int index) {
		this.p = p;
		this.index = index;
	}
	
	public Point getP() { return p; }
	public int getIndex() { return index; }
}