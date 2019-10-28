package eu.transkribus.swt.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DropDownToolItem extends Widget {
	private final static Logger logger = LoggerFactory.getLogger(DropDownToolItem.class);

	public ToolItem ti;
	DropdownSelectionListener listener;
	MenuItem selected = null;
	int itemStyle=SWT.NONE;
	boolean renderTextInMainItem=false;
	boolean renderImageInMainItem=true;
	
	boolean showMenuOnItemClick=false;
	boolean showDropDownArrow=true;
	boolean isDropDown=true;
	
	boolean keepMenuOpenOnClick=false;

	public static final int IS_DROP_DOWN_ITEM_DETAIL = 1;
	
	List<SelectionListener> selListener=new ArrayList<>();

	public static final String ALT_TXT_KEY="altTxt";

//	boolean highlightSelected=false;
	
//	public static int getMenuHeight(Menu parent) {
//		try {
//			Method m = Menu.class.getDeclaredMethod("getBounds", null);
//			m.setAccessible(true);
//			Rectangle r = (Rectangle) m.invoke(parent, null);
//			return r.height;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return 0;
//		}
//	}

	class DropdownSelectionListener extends SelectionAdapter {
		ToolItem dropdown;
		Menu menu;

		public DropdownSelectionListener(ToolItem dropdown) {
			this.dropdown = dropdown;
			menu = new Menu(dropdown.getParent().getShell());
			
			menu.addMenuListener(new MenuListener() {
				@Override
				public void menuHidden(MenuEvent event) {
					try {
						Field field = Menu.class.getDeclaredField("hasLocation");
						field.setAccessible(true);
						field.set(menu, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void menuShown(MenuEvent event) {
				}
			});
			
//			menu.addListener(SWT.Hide, new Listener() {
//				@Override
//				public void handleEvent(Event event) {
//					logger.debug("hiding menu!");
//					event.doit = false;
//				}
//			});
			
//			menu.addListener(SWT.Hide, listener);
			
//			menu.

			menu.addMenuListener(new MenuListener() {
				@Override
				public void menuShown(MenuEvent e) {
					ti.setSelection(true);
				}
				
				@Override
				public void menuHidden(MenuEvent e) {
					ti.setSelection(false);
					
					if (isKeepMenuOpenOnClick()) {
						try {
							Field field = Menu.class.getDeclaredField("hasLocation");
							field.setAccessible(true);							
							field.set(menu, false);
						} catch (Exception ex) {
							logger.error(ex.getMessage(), ex);
						}
					}
				}
			});

		}

		public MenuItem add(String text, Image image, String toolTipText, int itemStyle, boolean isSelected, Object data) {
			MenuItem menuItem = new MenuItem(menu, itemStyle);
						
//			MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
			menuItem.setData("tooltip", toolTipText);
			menuItem.setData("text", text);
			menuItem.setText(text);
			if (image != null){
				menuItem.setImage(image);
			}
//			menuItem.setSelection(false);
			menuItem.setSelection(isSelected);
			menuItem.setData("data", data);
			
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (isKeepMenuOpenOnClick()) {
						Display.getDefault().asyncExec(() -> { showMenu(); });
					}
					
					MenuItem selected = (MenuItem) event.widget;
					logger.debug("menuitem selected: "+selected.getData()+" source = "+event.getSource()+ " isselected: "+selected.getSelection());
					
					if ( ( (selected.getStyle() & SWT.RADIO) != SWT.RADIO) || selected.getSelection() ) { // only one selection event for radio menuitem's!!
						selectItem(selected, true);
					}
				}
			});
			
			

			if (selected == null) {
				selectItem(menuItem, false);
			}
			
			return menuItem;
		}
		
		void showMenu() {
			Rectangle rect = ti.getBounds();
			Point pt = ti.getParent().toDisplay(new Point(rect.x, rect.y));
			
			menu.setLocation(pt.x + rect.width, pt.y + rect.height);
			menu.setVisible(true);
		}
		
		void hideMenu() {
			menu.setVisible(false);
		}
		
		private int findItemIndex(MenuItem toFind) {
			int i = 0;
			for (MenuItem mi : menu.getItems()) {
				if (mi == toFind)
					return i;
				++i;
			}
			return -1;
		}

		public void removeAll() {
			for (MenuItem mi : menu.getItems()) {
				mi.dispose();
			}
		}
		
		public void clearSelection() {
			selected = null;
			
				for (int i=0; i<menu.getItemCount(); ++i) {
					menu.getItem(i).setSelection(false);
				}
						
		}
		
		private Listener detachMenuItemSelectionListener(MenuItem mi) {
			Listener sl = mi.getListeners(SWT.Selection)[0];
			mi.removeListener(SWT.Selection, sl);
			return sl;
		}
		
//		private SelectionListener attachMenuItemSelectionListener(MenuItem mi) {
//			SelectionListener sl = (SelectionListener) mi.getListeners(SWT.Selection)[0];
//			mi.removeSelectionListener(sl);
//			return sl;
//		}		

		public void selectItem(MenuItem item, boolean fireSelectionEvent) {
			Assert.assertNotNull(item);

			selected = item;
			if (renderTextInMainItem) {
				Object altText = item.getData(ALT_TXT_KEY);
				if (altText != null) {
					dropdown.setText((String) altText);	
				} else {
					dropdown.setText(item.getText());	
				}
			}
			
			if ( (itemStyle & SWT.RADIO) == SWT.RADIO) {
				for (MenuItem mi : menu.getItems()) {
					mi.setSelection(mi==selected);
				}
			}

//			selected.setSelection(true);
			if (renderImageInMainItem)
				dropdown.setImage(item.getImage());
			dropdown.getParent().pack();
			
			
			if (item.getData("tooltip") != null && item.getData("tooltip") instanceof String) {
				dropdown.setToolTipText((String)item.getData("tooltip"));
			}

			// fire selection event on dropdown:
			if (fireSelectionEvent) {
				Event e = new Event();
				e.index = findItemIndex(item);
				logger.debug("firing selection event, data = "+item.getData());
				e.data = item.getData();
				e.detail = IS_DROP_DOWN_ITEM_DETAIL;
				
//				dropdown.notifyListeners(SWT.Selection, new Event());
				dropdown.notifyListeners(SWT.Selection, e);
			}
		}

		public void selectItem(int index, boolean fireSelectionEvent) {
			if (index >= 0 && index < menu.getItemCount()) {
				selectItem(menu.getItem(index), fireSelectionEvent);
			}
		}
		
		@Override
		public void widgetSelected(SelectionEvent event) {
			logger.debug("dropdown toolitem selected: "+event);
			
			if (isDropDown && event.detail == SWT.ARROW || (isShowMenuOnItemClick() && event.detail!=IS_DROP_DOWN_ITEM_DETAIL)) {
				showMenu();
			} else {
				
				//ti.setSelection(true);

				// logger.debug("setting selection event...");
				// MenuItem si = getSelectedItem();
				// event.item = si;
				// event.text = dropdown.getText();
				// event.detail = lastSelected;
				//
				//
				//
				// logger.debug("event here: "+event);
				//
				// event.doit = false;

				// System.out.println(dropdown.getText() + " Pressed");
			}
		}
	}
	
//	public DropDownToolItem(ToolBar parent) {
//		init(parent, false, false);
//	}
	public DropDownToolItem(ToolBar parent, boolean renderTextInMainItem, boolean renderImageInMainItem, boolean showMenuOnItemClick, int itemStyle) {
		this(parent, renderTextInMainItem, renderImageInMainItem, showMenuOnItemClick, itemStyle, -1);
	}
	
	public DropDownToolItem(ToolBar parent, boolean renderTextInMainItem, boolean renderImageInMainItem, boolean showMenuOnItemClick, int itemStyle, int index) {
		this(parent, true, renderTextInMainItem, renderImageInMainItem, showMenuOnItemClick, itemStyle, index);
	}

	/**
	 * Item style: SWT.NONE, SWT.RADIO, SWT.CHECK
	 */
	public DropDownToolItem(ToolBar parent, boolean showDropDownArrow, boolean renderTextInMainItem, boolean renderImageInMainItem, boolean showMenuOnItemClick, int itemStyle, int index) {
		super(parent, itemStyle);
		
		if ( false && (itemStyle & SWT.CHECK) == SWT.CHECK ) {
			setKeepMenuOpenOnClick(true);
		}
		
//		int tiItemStyle = showDropDownArrow ? SWT.DROP_DOWN : SWT.PUSH;
		isDropDown = showDropDownArrow && !showMenuOnItemClick;
		int tiItemStyle = isDropDown ? SWT.DROP_DOWN : SWT.CHECK;
		
		if (index >= 0)
			ti = new ToolItem(parent, tiItemStyle, index);
		else
			ti = new ToolItem(parent, tiItemStyle);
		
		// TEST - set style of toolitem to check and drop_down - did not work...
//		try {
//			logger.debug("style before: "+ti.getStyle());
//			Field f = FieldUtils.getField(ti.getClass(), "style", true);
//			f.set(ti, SWT.DROP_DOWN | SWT.RADIO);
//			logger.debug("style after: "+ti.getStyle());
//			
////			ti.setSelection(true);
//			
////			FieldUtils.writeDeclaredField(ti, "style", SWT.DROP_DOWN | SWT.RADIO, true);
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
		// END TEST
		
		this.renderTextInMainItem = renderTextInMainItem;
		this.renderImageInMainItem = renderImageInMainItem;
		this.showMenuOnItemClick = showMenuOnItemClick;
		this.showDropDownArrow = showDropDownArrow;
		
		this.itemStyle = itemStyle;
//		this.highlightSelected = highlightSelected;
//		if (highlightSelected)
//			itemStyle |= SWT.RADIO;
		
		listener = new DropdownSelectionListener(ti);
		ti.addSelectionListener(listener);
		
	}

//	@Override
	@Override
	protected void checkSubclass() {
	}
	
	public MenuItem addSeparator() {
		return addItem("", null, "", SWT.SEPARATOR, false, null);
	}
	
	public MenuItem addItem(String text, Image image, String toolTipText) {
		return addItem(text, image, toolTipText, false, null);
//		return listener.add(text, image, toolTipText, itemStyle, false);
	}
	
	public MenuItem addItem(String text, Image image, String toolTipText, int itemStyle) {
		return addItem(text, image, toolTipText, itemStyle, false, null);
//		return listener.add(text, image, toolTipText, itemStyle, false);
	}

	public MenuItem addItem(String text, Image image, String toolTipText, boolean isSelected) {
		return addItem(text, image, toolTipText, isSelected, null);
//		return listener.add(text, image, toolTipText, itemStyle, isSelected, null);
	}
	
	public MenuItem addItem(String text, Image image, String toolTipText, boolean isSelected, Object data) {
//		return listener.add(text, image, toolTipText, itemStyle, isSelected, data);
		return addItem(text, image, toolTipText, itemStyle, isSelected, data);
	}
	
	public MenuItem addItem(String text, Image image, String toolTipText, int itemStyle, boolean isSelected, Object data) {
		return listener.add(text, image, toolTipText, itemStyle, isSelected, data);
	}

	public void removeAll() {
		listener.removeAll();
	}

	public MenuItem getSelected() {
		return selected;
	}
	
	public int getItemCount() {
		return listener.menu.getItemCount();
	}
	
	public MenuItem getItemWithData(Object data) {
		for (MenuItem mi : listener.menu.getItems()) {
			if (mi.getData().equals(data))
				return mi;
		}
		return null;
	}

	/** The index of the item that was last selected. */
	public int getLastSelectedIndex() {
		return listener.findItemIndex(selected);
	}

	/** The indices of all the items that are selected. */
	public List<Integer> getSelectedIndices() {
		List<Integer> selected = new ArrayList<>();
		int i=0;
		for (MenuItem mi : listener.menu.getItems()) {
			if (mi.getSelection()) {
				selected.add(i);
			}
			++i;
		}
		return selected;
	}
	
	public void clearSelections() {
		listener.clearSelection();
	}

	public void selectItem(int index, boolean fireSelectionEvent) {
		listener.selectItem(index, fireSelectionEvent);
	}

	public void selectItem(MenuItem item, boolean fireSelectionEvent) {
		if (item==null)
			return;
		
		listener.selectItem(item, fireSelectionEvent);
	}
	
	public boolean isMenuVisible() {
		return listener.menu.isVisible();
	}

//	public void exchangeToolItemImage(Image newImage) {
//		if (ti.getImage() != null){
//			ti.getImage().dispose();
//		}
//		ti.setImage(newImage);
//	}

	public Menu getMenu() {
		return listener.menu;
	}
	
	public boolean isShowMenuOnItemClick() {
		return showMenuOnItemClick;
	}

	public void setShowMenuOnItemClick(boolean showMenuOnItemClick) {
		this.showMenuOnItemClick = showMenuOnItemClick;
	}
	
	public boolean isKeepMenuOpenOnClick() {
		return keepMenuOpenOnClick;
	}

	public void setKeepMenuOpenOnClick(boolean keepMenuOpenOnClick) {
		this.keepMenuOpenOnClick = keepMenuOpenOnClick;
	}
	
//	public void addSelectionListener(SelectionListener l) {
//		selListener.add(l);
//	}
//	
//	public void removeSelectionListener(SelectionListener l) {
//		selListener.remove(l);
//	}
	


}
