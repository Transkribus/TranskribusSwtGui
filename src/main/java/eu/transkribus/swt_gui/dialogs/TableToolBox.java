package eu.transkribus.swt_gui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.table_editor.BorderFlags;

public class TableToolBox extends ToolBox {

	Button markupNone, markupAll, markupClosed, markupLeft, markupRight, markupTop, markupBottom;
	Button markupLeftRight, markupBottomTop;
	Button markupHorizontalClosed, markupVerticalClosed, markupHorizontalOpen, markupVerticalOpen;
	
	BorderFlags bf;
	
	public TableToolBox(Shell parent, boolean vertical, String title) {
		super(parent, vertical, title);
		
		markupNone = addButton("None", Images.BORDER_NONE, SWT.CHECK);
		markupAll  = addButton("All", Images.BORDER_ALL, SWT.CHECK);
		markupClosed = addButton("Closed", Images.BORDER_CLOSED, SWT.CHECK);
		
		markupLeft = addButton("Left", Images.BORDER_LEFT, SWT.CHECK);
		markupRight = addButton("Right", Images.BORDER_RIGHT, SWT.CHECK);
		markupLeftRight = addButton("Left / Right", Images.BORDER_LEFT_RIGHT, SWT.CHECK);
		
		markupBottom = addButton("Bottom", Images.BORDER_BOTTOM, SWT.CHECK);
		markupTop = addButton("Top", Images.BORDER_TOP, SWT.CHECK);
		markupBottomTop = addButton("Bottom / Top", Images.BORDER_BOTTOM_TOP, SWT.CHECK);
		
		markupHorizontalClosed = addButton("Horizontally closed", Images.BORDER_HORIZONTAL_CLOSED, SWT.CHECK);
		markupHorizontalOpen = addButton("Horizontally open", Images.BORDER_HORIZONTAL_OPEN, SWT.CHECK);
		markupVerticalClosed = addButton("Vertically closed", Images.BORDER_VERTICAL_CLOSED, SWT.CHECK);
		markupVerticalOpen = addButton("Vertically open", Images.BORDER_VERTICAL_OPEN, SWT.CHECK);
		
		bf = new BorderFlags();
	}

	public void show() {
		this.showAt(0, 0);
	}
	
	public void set(BorderFlags flags) {
		markupNone.setEnabled(flags.is_none());
		markupAll.setEnabled(flags.is_all());
		markupClosed.setEnabled(flags.is_closed());
		
		markupLeft.setEnabled(flags.is_left());
		markupRight.setEnabled(flags.is_right());
		markupLeftRight.setEnabled(flags.is_left_right());
		
		markupTop.setEnabled(flags.is_top());
		markupBottom.setEnabled(flags.is_bottom());
		markupBottomTop.setEnabled(flags.is_bottom_top());
		
		markupHorizontalClosed.setEnabled(flags.is_horizontal_closed());
		markupHorizontalOpen.setEnabled(flags.is_horizontal_open());
		
		markupVerticalClosed.setEnabled(flags.is_vertical_closed());
		markupVerticalOpen.setEnabled(flags.is_vertical_open());
	}
	
}
