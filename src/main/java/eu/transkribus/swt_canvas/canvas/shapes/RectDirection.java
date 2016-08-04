package eu.transkribus.swt_canvas.canvas.shapes;

import java.awt.Point;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;

public enum RectDirection {
	NONE(SWT.CURSOR_ARROW, false),
	
	NW(SWT.CURSOR_SIZENW, true),
	N(SWT.CURSOR_SIZEN, false),
	NE(SWT.CURSOR_SIZENE, true),
	E(SWT.CURSOR_SIZEE, false),
	SE(SWT.CURSOR_SIZESE, true),
	S(SWT.CURSOR_SIZES, false),
	SW(SWT.CURSOR_SIZESW, true),
	W(SWT.CURSOR_SIZEW, false);
	
	int cursorType;
	boolean corner=false;
	final static Logger logger = LoggerFactory.getLogger(RectDirection.class);
	
	RectDirection(int cursorType, boolean corner) {		
		this.corner = corner;
		this.cursorType = cursorType;
	}
	public int getCursorType() {
		return cursorType;
	}
	public boolean isCorner() { return corner; }
	
	public boolean isPointAffected(Rectangle bounds, int x, int y) {
		switch (this) {
		case NW:
			return bounds.x == x || bounds.y == y;
		case N:
			return bounds.y == y;
		case NE:
			return bounds.x+bounds.width == x || bounds.y == y;
		case E:
			return bounds.x+bounds.width == x;
		case SE:
			return bounds.x+bounds.width == x || bounds.y+bounds.height == y;
		case S:
			return bounds.y+bounds.height == y;
		case SW:
			return bounds.x == x || bounds.y+bounds.height == y;
		case W:
			return bounds.x == x;
		default:
			return false;
		}
	}
	
	/** Moves a point by [tx, ty] if affected by this direction, depending ont the given center coordinates [cx, cy] */
	public void movePointIfAffected(/*Rectangle bounds, */Point p, int tx, int ty, int cx, int cy) {
//		logger.debug("move pt if affected p = "+p+" tx = "+tx+" ty = "+ty+" center: "+cx+" x "+cy);
		
//		int tol = 5;
		switch (this) {
		case NW:
			if (p.x < cx)
				p.x += tx;
			if (p.y < cy)
				p.y += ty;
			
//			if (MathUtil.eqBounds(bounds.x, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
//			if (MathUtil.eqBounds(bounds.y, p.y, tol))
//				p.setLocation(p.x, p.y+ty);
			return;
		case N:
			if (p.y < cy)
				p.y += ty;
			
//			if (MathUtil.eqBounds(bounds.y, p.y, tol)) {
//				p.setLocation(p.x, p.y+ty);
//			}
			return;
		case NE:
			if (p.x > cx)
				p.x += tx;
			if (p.y < cy)
				p.y += ty;
			
//			if (MathUtil.eqBounds(bounds.x+bounds.width, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
//			if (MathUtil.eqBounds(bounds.y, p.y, tol))
//				p.setLocation(p.x, p.y+ty);
			return;
		case E:
			if (p.x > cx)
				p.x += tx;
			
//			if (MathUtil.eqBounds(bounds.x+bounds.width, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
			return;
		case SE:
			if (p.x > cx)
				p.x += tx;
			if (p.y > cy)
				p.y += ty;
			
//			if (MathUtil.eqBounds(bounds.x+bounds.width, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
//			if (MathUtil.eqBounds(bounds.y+bounds.height, p.y, tol))
//				p.setLocation(p.x, p.y+ty);
			return;	
		case S:
			if (p.y > cy)
				p.y += ty;			
			
//			if (MathUtil.eqBounds(bounds.y+bounds.height, p.y, tol))
//				p.setLocation(p.x, p.y+ty);
			return;
		case SW:	
			if (p.x < cx)
				p.x += tx;
			if (p.y > cy)
				p.y += ty;
			
//			if (MathUtil.eqBounds(bounds.x, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
//			if (MathUtil.eqBounds(bounds.y+bounds.height, p.y, tol))
//				p.setLocation(p.x, p.y+ty);
			return;	
		case W:
			if (p.x < cx)
				p.x += tx;
			
//			if (MathUtil.eqBounds(bounds.x, p.x, tol))
//				p.setLocation(p.x+tx, p.y);
			return;
		default:
			return;
		}
		
		
	}
}
