package eu.transkribus.swt_gui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.core.util.Event;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.TableBorderEditEvent;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.BorderFlags;

public class TableToolBox extends ToolBox {
	Button markupNone, markupAll, markupClosed, markupLeft, markupRight, markupTop, markupBottom;
	Button markupLeftRight, markupBottomTop;
	Button markupHorizontalClosed, markupVerticalClosed, markupHorizontalOpen, markupVerticalOpen;
	Button applySelection;

	BorderFlags bf;

	class BorderSelectionChangedListener implements SelectionListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			// setup borderflags and send flag changed event

		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			// TODO Auto-generated method stub

		}

	}

	public TableToolBox(Shell parent, boolean vertical, String title) {
		super(parent, vertical, title);

		markupNone = addButton("None", Images.BORDER_NONE, SWT.CHECK, BorderFlags.none());
		markupAll = addButton("All", Images.BORDER_ALL, SWT.CHECK, BorderFlags.all());

		markupLeft = addButton("Left", Images.BORDER_LEFT, SWT.CHECK, BorderFlags.left());
		markupRight = addButton("Right", Images.BORDER_RIGHT, SWT.CHECK, BorderFlags.right());

		markupBottom = addButton("Bottom", Images.BORDER_BOTTOM, SWT.CHECK, BorderFlags.bottom());
		markupTop = addButton("Top", Images.BORDER_TOP, SWT.CHECK, BorderFlags.top());

		// todo add separator lines
		
		markupClosed = addButton("Closed", Images.BORDER_CLOSED, SWT.CHECK, BorderFlags.closed());

		markupLeftRight = addButton("Left / Right", Images.BORDER_LEFT_RIGHT, SWT.CHECK, BorderFlags.left_right());
		markupBottomTop = addButton("Bottom / Top", Images.BORDER_BOTTOM_TOP, SWT.CHECK, BorderFlags.bottom_top());
		
		markupHorizontalClosed = addButton("Horizontally closed", Images.BORDER_HORIZONTAL_CLOSED, SWT.CHECK, BorderFlags.horizontal_closed());
		markupHorizontalOpen = addButton("Horizontally open", Images.BORDER_HORIZONTAL_OPEN, SWT.CHECK, BorderFlags.horizontal_open());
		
		markupVerticalClosed = addButton("Vertically closed", Images.BORDER_VERTICAL_CLOSED, SWT.CHECK, BorderFlags.vertical_closed());
		markupVerticalOpen = addButton("Vertically open", Images.BORDER_VERTICAL_OPEN, SWT.CHECK, BorderFlags.vertical_open());

		


		bf = new BorderFlags();

	}

	
	public void set(BorderFlags flags) {
		markupNone.setSelection(flags.is_none());
		markupAll.setSelection(flags.is_all());
		markupClosed.setSelection(flags.is_closed());

		markupLeft.setSelection(flags.is_left());
		markupRight.setSelection(flags.is_right());
		markupLeftRight.setSelection(flags.is_left_right());

		markupTop.setSelection(flags.is_top());
		markupBottom.setSelection(flags.is_bottom());
		markupBottomTop.setSelection(flags.is_bottom_top());

		markupHorizontalClosed.setSelection(flags.is_horizontal_closed());
		markupHorizontalOpen.setSelection(flags.is_horizontal_open());

		markupVerticalClosed.setSelection(flags.is_vertical_closed());
		markupVerticalOpen.setSelection(flags.is_vertical_open());

		bf = flags;
	}

	protected Button addButton(String txt, Image img, int style, BorderFlags flags) {
		Button b = addButton(txt, img, style);

		// add selection listener
		SWTUtil.onSelectionEvent(b, (e) -> {

			// todo: add logic to disable some parts, i.e. if all is selected, check, whether none was selected before and deactivate accordingly
			
			Event event = new TableBorderEditEvent(b, flags, b.getSelection());
			TrpMainWidget.getInstance().getCanvas().getContextMenu().sendEvent(event);
		});

		return b;
	}

	public BorderFlags getSelection() {
		BorderFlags flags = new BorderFlags();

		// complete and easy cases: all or nothing
		flags.setAll(markupAll.getSelection());
		flags.setAll(!markupNone.getSelection());

		// adding on cases - if other buttons are enabled
		if (!markupAll.getSelection() || !markupNone.getSelection()) {

		}

		return bf;
	}

}
