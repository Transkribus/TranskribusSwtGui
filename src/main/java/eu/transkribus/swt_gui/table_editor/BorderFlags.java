package eu.transkribus.swt_gui.table_editor;

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
	
	public BorderFlags(boolean val) {
		super();
		this.vertLeft = val;
		this.vertRight = val;
		this.vertInner = val;
		this.horBottom = val;
		this.horTop = val;
		this.horInner = val;
	}
	
	public void setAll(boolean val) {
		this.vertLeft = val;
		this.vertRight = this.vertInner = this.horBottom = this.horTop = this.horInner = val;
	}
	
	public void set(BorderFlags bf, boolean keepExisting) {
		if (keepExisting) {
			// only set updated flags
			vertLeft = bf.vertLeft ? bf.vertLeft : vertLeft;
			vertRight = bf.vertRight ? bf.vertRight : vertRight;
			vertInner = bf.vertInner ? bf.vertInner : vertInner;
			horBottom = bf.horBottom ? bf.horBottom : horBottom;
			horTop = bf.horTop ? bf.horTop : horTop;
			horInner = bf.horInner ? bf.horInner : horInner;
		} else {
			vertLeft = bf.vertLeft;
			vertRight = bf.vertRight;
			vertInner = bf.vertInner;
			horBottom = bf.horBottom;
			horTop = bf.horTop;
			horInner = bf.horInner;
		}
		
	}
	
	public boolean vertLeft=false;
	public boolean vertRight=false;
	public boolean vertInner=false;
	
	public boolean horBottom=false;
	public boolean horTop=false;
	public boolean horInner=false;
	
	public BorderFlags invert() {
		vertLeft = !vertLeft;
		vertRight = !vertRight;
		vertInner = !vertInner;
		horBottom = !horBottom;
		horTop = !horTop;
		horInner = !horInner;
		
		return this;
	}
	
	/**
	 * Disables any existing entry according to set entries in incoming BorderFlags,
	 * e.g. all flags are set, bf only has left border set --> all minus left border will be set
	 * @param bf
	 * @return true if changes occured
	 */
	public boolean subtract(BorderFlags bf) {
		boolean changed = false;
		if (bf.horBottom) horBottom = false;
		if (bf.horInner) horInner = false;
		if (bf.horTop) horTop = false;
		if (bf.vertInner) vertInner = false;
		if (bf.vertLeft) vertLeft = false;
		if (bf.vertRight) vertRight = false;
		return changed;
	}
	
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
		return (vertLeft && vertRight && !vertInner && !horBottom && !horTop && !horInner);
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
		return (!vertLeft && !vertRight && !vertInner && horBottom && horTop && !horInner);
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
		return (horBottom && horTop && horInner && vertLeft && vertRight && !vertInner);
	}
	
	public static BorderFlags horizontal_open() {
		BorderFlags bf = new BorderFlags();
		bf.horBottom = true;
		bf.horTop = true;
		bf.horInner = true;
		return bf;
	}
	
	public boolean is_horizontal_open() {
		return (!vertLeft && !vertRight && !vertInner && horBottom && horTop && horInner);	
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
		return (vertLeft && vertRight && vertInner && horBottom && horTop && !horInner);
	}
	
	public static BorderFlags vertical_open() {
		BorderFlags bf = new BorderFlags();
		bf.vertLeft = true;
		bf.vertRight = true;
		bf.vertInner = true;
		return bf;
	}
	
	public boolean is_vertical_open() {
		return (vertLeft && vertRight && vertInner && !horBottom && !horTop && !horInner);
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
	
	public static BorderFlags inner() {
		BorderFlags bf = new BorderFlags();
		bf.vertInner = true;
		bf.horInner = true;
		return bf;
	}
	
	public boolean is_inner() {
		return (vertInner && horInner);
	}
	
	public static BorderFlags vertical_inner() {
		BorderFlags bf = new BorderFlags();
		bf.vertInner = true;
		return bf;
	}
	
	public boolean is_vertical_inner() {
		return (vertInner);
	}
	
	public static BorderFlags horizontal_inner() {
		BorderFlags bf = new BorderFlags();
		bf.horInner = true;
		return bf;
	}
	
	public boolean is_horizontal_inner() {
		return (horInner);
	}
	
	@Override 
	public String toString() {
		return "BorderFlags [vertLeft=" + vertLeft + ", vertRight=" + vertRight + ", vertInner=" + vertInner + ", horBottom=" + horBottom + ", horTop="
				+ horTop + ", horInner=" + horInner + "]";
	}
	
	@Override 
	public boolean equals(Object o){
		if (this == o) return true;
		if (!(o instanceof BorderFlags)) return false;
		
		BorderFlags bf = (BorderFlags) o;
		return (bf.horBottom == horBottom && bf.horInner == horInner && bf.horTop == horTop
				&& bf.vertInner == vertInner && bf.vertLeft == vertLeft && bf.vertRight == vertRight);
	}

}