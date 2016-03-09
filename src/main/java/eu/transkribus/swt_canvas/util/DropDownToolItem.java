package eu.transkribus.swt_canvas.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
	
	List<SelectionListener> selListener=new ArrayList<>();

//	boolean highlightSelected=false;

	class DropdownSelectionListener extends SelectionAdapter {
		ToolItem dropdown;
		Menu menu;

		public DropdownSelectionListener(ToolItem dropdown) {
			this.dropdown = dropdown;
			menu = new Menu(dropdown.getParent().getShell());
			
			menu.addListener(SWT.Hide, new Listener() {
				@Override
				public void handleEvent(Event event) {
				}
			});
		}

		public MenuItem add(String item, Image image, String toolTipText, int itemStyle, boolean isSelected, Object data) {
			MenuItem menuItem = new MenuItem(menu, itemStyle);
//			MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
			menuItem.setData("tooltip", toolTipText);
			menuItem.setText(item);
			if (image != null){
				menuItem.setImage(image);
			}
//			menuItem.setSelection(false);
			menuItem.setSelection(isSelected);
			menuItem.setData(data);
			
			menuItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					MenuItem selected = (MenuItem) event.widget;
					logger.debug("menuitem selected: "+selected.getData()+" source = "+event.getSource()+ " isselected: "+selected.getSelection());
					
					if ( ( (selected.getStyle() & SWT.RADIO) != SWT.RADIO) || selected.getSelection() ) // only one selection event for radio menuitem's!!
						selectItem(selected, true);
				}
			});

			if (selected == null) {
				selectItem(menuItem, false);
			}
			
			return menuItem;
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
			if (renderTextInMainItem)
				dropdown.setText(item.getText());
			
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
			ToolItem item = (ToolItem) event.widget;
			if (event.detail == SWT.ARROW) {
				Rectangle rect = item.getBounds();
				Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
				menu.setLocation(pt.x, pt.y + rect.height);
				menu.setVisible(true);
				
//				for (MenuItem m : menu.getItems()) {
//					String text = m.getText();
//					if (highlightSelected && m == getSelected() && text!=null && !text.isEmpty()) {
//						m.
//					}
//				}
			} else {				
				ti.setSelection(true);

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
	public DropDownToolItem(ToolBar parent, boolean renderTextInMainItem, boolean renderImageInMainItem, int itemStyle) {
		this(parent, renderTextInMainItem, renderImageInMainItem, itemStyle, -1);
	}

	/**
	 * Item style: SWT.NONE, SWT.RADIO, SWT.CHECK
	 */
	public DropDownToolItem(ToolBar parent, boolean renderTextInMainItem, boolean renderImageInMainItem, int itemStyle, int index) {
		super(parent, itemStyle);
		
		if (index >= 0)
			ti = new ToolItem(parent, SWT.DROP_DOWN, index);
		else
			ti = new ToolItem(parent, SWT.DROP_DOWN);
		
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
		return listener.add(text, image, toolTipText, itemStyle, isSelected, data);
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

	public void exchangeToolItemImage(Image newImage) {
		if (ti.getImage() != null){
			ti.getImage().dispose();
		}
		ti.setImage(newImage);
		
	}

	
//	public void addSelectionListener(SelectionListener l) {
//		selListener.add(l);
//	}
//	
//	public void removeSelectionListener(SelectionListener l) {
//		selListener.remove(l);
//	}
	


}
