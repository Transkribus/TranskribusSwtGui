package eu.transkribus.swt_gui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.Event;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.canvas.ICanvasContextMenuListener.TableBorderEditEvent;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.table_editor.BorderFlags;
import eu.transkribus.swt_gui.table_editor.TableUtils;

public class TableMarkupBox { // extends ToolBox {
	private final static Logger logger = LoggerFactory.getLogger(TableMarkupBox.class);
	Shell shell;
	int posX, posY;

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

	public TableMarkupBox(Shell parent, String title) {
		shell = new Shell(parent, SWT.RESIZE | SWT.CLOSE | SWT.MODELESS);
		shell.setText(title);

		shell.setLayout(new RowLayout(SWT.VERTICAL));		
		shell.addShellListener(new ShellListener() {
			
			@Override
			public void shellIconified(ShellEvent e) {}
			
			@Override
			public void shellDeiconified(ShellEvent e) {}
			
			@Override
			public void shellDeactivated(ShellEvent e) {}
			
			@Override
			public void shellClosed(ShellEvent e) {
				e.doit = false;
				hide();
			}
			
			@Override
			public void shellActivated(ShellEvent e) {}
		});
		
		shell.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					hide();
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});

		addButtons();
		
		bf = new BorderFlags();
		
		posX = parent.getLocation().x;
		posY = parent.getLocation().y;
	}

	private void addButtons() {
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
	}
	
	
	public void showAt(int x, int y) {
		posX=x;
		posY=y;
		
		shell.pack();
		shell.setVisible(true);
		shell.setLocation(x, y);
		shell.setActive();
	}
	
	public void show() {
		showAt(posX, posY);
	}
	
	public void hide() {
		shell.setLocation(posX, posY);
		shell.setVisible(false);
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public void set(BorderFlags flags, boolean enable) {
		markupNone.setSelection((bf.is_none() || flags.is_none()) && enable);
		markupAll.setSelection((bf.is_all() || flags.is_all()) && enable);
		markupClosed.setSelection((bf.is_closed() || flags.is_closed()) && enable);

		markupLeft.setSelection((bf.is_left() || flags.is_left()) && enable);
		markupRight.setSelection((bf.is_right() || flags.is_right()) && enable);
		markupLeftRight.setSelection((bf.is_left_right() || flags.is_left_right()) && enable);

		markupTop.setSelection((bf.is_top() || flags.is_top()) && enable);
		markupBottom.setSelection((bf.is_bottom() || flags.is_bottom()) && enable);
		markupBottomTop.setSelection((bf.is_bottom_top() || flags.is_bottom_top()) && enable);

		markupHorizontalClosed.setSelection((bf.is_horizontal_closed() || flags.is_horizontal_closed()) && enable);
		markupHorizontalOpen.setSelection((bf.is_horizontal_open() || flags.is_horizontal_open()) && enable);

		markupVerticalClosed.setSelection((bf.is_vertical_closed() || flags.is_vertical_closed()) && enable);
		markupVerticalOpen.setSelection((bf.is_vertical_open() || flags.is_vertical_open()) && enable);

		bf = flags;
		
		shell.pack();
	}

	
	protected Button addButton(String txt, Image img, int style, BorderFlags flags) {
		Button b = new Button(shell, style);
		if (!StringUtils.isEmpty(txt))
			b.setText(txt);
		if (img != null)
			b.setImage(img);
		shell.pack();

		// add selection listener
		SWTUtil.onSelectionEvent(b, (e) -> {

			// todo: add logic to disable some parts, i.e. if all is selected, check, whether none was selected before and deactivate accordingly
			boolean keep = b.getSelection();

			set(flags, keep);
			
			if (flags.is_all() || flags.is_none())
				keep=false;
			
			Event event = new TableBorderEditEvent(b, flags, keep);
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
