package eu.transkribus.swt_gui.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.apache.fontbox.afm.Composite;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.io.GetBufferedRandomAccessSource;

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

	Button markupNone, markupAll, markupOuter;
	Button markupLeft, markupRight, markupTop, markupBottom;
	Button markupInner, markupInnerHorizonal, markupInnerVertical;
	
	Button markupLeftRight, markupBottomTop;
	Button markupHorizontalClosed, markupVerticalClosed, markupHorizontalOpen, markupVerticalOpen;


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

		shell.setLayout(new GridLayout(1, true));		
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
		
		markupInner = addButton("Inner", Images.BORDER_INNER, SWT.CHECK, BorderFlags.inner());
		
		markupInnerHorizonal = addButton("Inner horizontal", Images.BORDER_INNER_HORIZONTAL, SWT.CHECK, BorderFlags.horizontal_inner());
		markupInnerVertical = addButton("Inner vertical", Images.BORDER_INNER_VERTICAL, SWT.CHECK, BorderFlags.vertical_inner());

		Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		markupOuter = addButton("Closed", Images.BORDER_CLOSED, SWT.CHECK, BorderFlags.closed());
		
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
		posX = shell.getLocation().x;
		posY = shell.getLocation().y;
		shell.setLocation(posX, posY);
		shell.setVisible(false);
	}
	
	public Shell getShell() {
		return shell;
	}
	
	public void set(BorderFlags flags) {
		set(flags, true);
	}
	
	public void set(BorderFlags f, boolean keep) {
		BorderFlags flags = new BorderFlags();
		flags = checkLogic(f, keep);
		
		markupNone.setSelection((flags.is_none()));
		markupAll.setSelection((flags.is_all()));
		markupOuter.setSelection((flags.is_closed()));
		
		markupInner.setSelection((flags.is_inner()));
		markupInnerHorizonal.setSelection((flags.is_horizontal_inner()));
		markupInnerVertical.setSelection((flags.is_vertical_inner()));

		markupLeft.setSelection((flags.is_left()));
		markupRight.setSelection((flags.is_right()));
		markupLeftRight.setSelection((flags.is_left_right()));

		markupTop.setSelection((flags.is_top()));
		markupBottom.setSelection((flags.is_bottom()));
		markupBottomTop.setSelection((flags.is_bottom_top()));

		markupHorizontalClosed.setSelection((flags.is_horizontal_closed()));
		markupHorizontalOpen.setSelection((flags.is_horizontal_open()));

		markupVerticalClosed.setSelection((flags.is_vertical_closed()));
		markupVerticalOpen.setSelection((flags.is_vertical_open()));

		bf.set(flags, false);
		
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

			boolean keep = b.getSelection();

			BorderFlags setFlags = new BorderFlags();
			
			// (re)set flag pattern of sending button
			if ((Button) e.getSource() == markupAll) {
				setFlags = BorderFlags.all();
			} else if ((Button) e.getSource() == markupNone) {
				setFlags = BorderFlags.none();
			} else if ((Button) e.getSource() == markupOuter) {
				setFlags = BorderFlags.closed();
			} else if ((Button) e.getSource() == markupLeft) {
				setFlags = BorderFlags.left();
			} else if ((Button) e.getSource() == markupRight) {
				setFlags = BorderFlags.right();
			} else if ((Button) e.getSource() == markupBottom) {
				setFlags = BorderFlags.bottom();
			} else if ((Button) e.getSource() == markupTop) {
				setFlags = BorderFlags.top();
			} else if ((Button) e.getSource() == markupInner) {
				setFlags = BorderFlags.inner();
			} else if ((Button) e.getSource() == markupInnerHorizonal) {
				setFlags = BorderFlags.horizontal_inner();
			} else if ((Button) e.getSource() == markupInnerVertical) {
				setFlags = BorderFlags.vertical_inner();
			} else if ((Button) e.getSource() == markupLeftRight) {
				setFlags = BorderFlags.left_right(); 
			} else if ((Button) e.getSource() == markupBottomTop) {
				setFlags = BorderFlags.bottom_top();
			} else if ((Button) e.getSource() == markupHorizontalClosed) {
				setFlags = BorderFlags.horizontal_closed();
			} else if ((Button) e.getSource() == markupHorizontalOpen) {
				setFlags = BorderFlags.horizontal_open();
			} else if ((Button) e.getSource() == markupVerticalClosed) {
				setFlags = BorderFlags.vertical_closed(); 
			} else if ((Button) e.getSource() == markupVerticalOpen) {
				setFlags = BorderFlags.vertical_open();
			}
			
			set(setFlags, keep);
						
//			if (flags.is_all() || flags.is_none())
//				keep=false;
			
			Event event = new TableBorderEditEvent(b, bf, false);
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
	
	private BorderFlags checkLogic(BorderFlags newSelection, boolean enable) {
		// check whether combination flags are set

		BorderFlags flags = new BorderFlags();
		flags.set(bf, true);
		
		// case 1: none selected || all deselected --> disable everything
		if ((newSelection.is_all() && !enable) || (newSelection.is_none() && enable)) {
			flags.setAll(false);
		} 
		
		// case 2: set all flags
		else if ((newSelection.is_all() && enable)) {
			flags.setAll(true);
		}
		// case 3: (combination) flags set, new selection deactivates some parts
		else if ((flags.is_all() || flags.is_closed() 
				|| flags.is_vertical_closed() || flags.is_vertical_open()
				|| flags.is_horizontal_closed() || flags.is_horizontal_open()) 
				&& !newSelection.equals(flags) && !enable) {
			flags.subtract(newSelection);
		}
		// case 4: set pre-selection
		else if (enable && 
				(newSelection.is_closed() || newSelection.is_left_right() || newSelection.is_bottom_top() 
				|| newSelection.is_horizontal_closed() || newSelection.is_horizontal_open() 
				|| newSelection.is_vertical_closed() || newSelection.is_vertical_open())) {
			flags.set(newSelection, false);
		}
		// case 5: add additional flags
		else if (enable) {
			flags.set(newSelection, true);
		} 
		// case 6: subtract if not enabled
		else if (!enable) {
			flags.subtract(newSelection);
		}

		return flags;
	}

}
