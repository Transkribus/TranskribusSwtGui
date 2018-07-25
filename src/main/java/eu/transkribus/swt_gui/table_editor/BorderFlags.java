package eu.transkribus.swt_gui.table_editor;

import net.sf.saxon.functions.AccessorFn.HoursFromDateTime;

public class BorderFlags {
	public BorderFlags() {}
	public BorderFlags(boolean vertLeft, boolean vertRight, boolean vertInner, boolean horBottom, boolean horTop, boolean horInner) {
		super();
		this.vertLeft = vertLeft;
		this.vertRight = vertRight;
		this.vertInner = vertInner;
		this.horBottom = horBottom;
		this.horTop = horTop;
		this.horInner = horInner;
	}
	
	public void setAll(boolean val) {
		this.vertLeft = this.vertRight = this.vertInner = this.horBottom = this.horTop = this.horInner = val;
	}
	
	public boolean vertLeft=false;
	public boolean vertRight=false;
	public boolean vertInner=false;
	
	public boolean horBottom=false;
	public boolean horTop=false;
	public boolean horInner=false;
	
	public static BorderFlags none() {
		return new BorderFlags();
	}
	
	public boolean is_none() {
		return (!vertLeft && !vertRight && !vertInner && !horBottom && !horTop && !horInner);
	}
	
	public static BorderFlags all() {
		BorderFlags bf = new BorderFlags();
		bf.setAll(true);
		return bf;
	}
	
	public boolean is_all() {
		return (vertLeft && vertRight && vertInner && horBottom && horTop && horInner);
	}
	
	public static BorderFlags left() {
		BorderFlags bf = new BorderFlags();
		bf.vertLeft = true;
		return bf;
	}
	
	public boolean is_left() {
		return vertLeft;
	}
	
	public static BorderFlags right() {
		BorderFlags bf = new BorderFlags();
		bf.vertRight = true;
		return bf;
	}
	
	public boolean is_right() {
		return vertRight;
	}
	
	public static BorderFlags left_right() {
		BorderFlags bf = new BorderFlags();
		bf.vertLeft = true;
		bf.vertRight = true;
		return bf;
	}
	
	public boolean is_left_right() {
		return (vertLeft && vertRight);
	}
	
	public static BorderFlags bottom() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		return bf;
	}
	
	public boolean is_bottom() {
		return horBottom;
	}
	
	public static BorderFlags top() {
		BorderFlags bf = new BorderFlags();
		bf.horTop = true;
		return bf;
	}
	
	public boolean is_top() {
		return horTop;
	}
	
	public static BorderFlags bottom_top() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		bf.horTop = true;
		return bf;
	}
	
	public boolean is_bottom_top() {
		return (horBottom && horTop);
	}
	
	public static BorderFlags horizontal_closed() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		bf.horTop = true;
		bf.horInner = true;
		bf.vertLeft = true;
		bf.vertRight = true;
		return bf;
	}
	
	public boolean is_horizontal_closed() {
		return (horBottom && horTop && horInner && vertLeft && vertRight);
	}
	
	public static BorderFlags horizontal_open() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		bf.horTop = true;
		bf.horInner = true;
		return bf;
	}
	
	public boolean is_horizontal_open() {
		return (horBottom && horTop && horInner);
	}
	
	public static BorderFlags vertical_closed() {
		BorderFlags bf = new BorderFlags();
		bf.vertLeft = true;
		bf.vertRight = true;
		bf.vertInner = true;
		
		bf.horBottom = true;
		bf.horTop = true;
		return bf;
	}
	
	public boolean is_vertical_closed() {
		return (vertLeft && vertRight && vertInner && horBottom && horTop);
	}
	
	public static BorderFlags vertical_open() {
		BorderFlags bf = new BorderFlags();
		bf.vertLeft = true;
		bf.vertRight = true;
		bf.vertInner = true;
		return bf;
	}
	
	public boolean is_vertical_open() {
		return (vertLeft && vertRight && vertInner);
	}
	
	public static BorderFlags closed() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		bf.horTop = true;
		bf.vertLeft = true;
		bf.vertRight = true;
		return bf;
	}
	
	public boolean is_closed() {
		return (horBottom && horTop && vertLeft && vertRight && !horInner && !vertInner);
	}
	
	@Override public String toString() {
		return "BorderFlags [vertLeft=" + vertLeft + ", vertRight=" + vertRight + ", vertInner=" + vertInner + ", horBottom=" + horBottom + ", horTop="
				+ horTop + ", horInner=" + horInner + "]";
	}

}