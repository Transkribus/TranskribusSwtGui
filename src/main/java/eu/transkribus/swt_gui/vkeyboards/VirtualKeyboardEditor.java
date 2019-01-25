package eu.transkribus.swt_gui.vkeyboards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.UnicodeList;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.dialogs.MultilineInputDialog;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.CustomTagSpec;
import eu.transkribus.swt_gui.metadata.TaggingWidgetUtils;

public class VirtualKeyboardEditor extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(VirtualKeyboardEditor.class);
	
	TableViewer tv;
	List<Pair<Integer, String>> unicodes = new ArrayList<>();
	
	Map<Pair<Integer, String>, ControlEditor> addEditors = new HashMap<>();
	
	Button addBtn, deleteBtn;
	
	public VirtualKeyboardEditor(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(2, false));
		
		addBtn = new Button(this, 0);
		addBtn.setText("Add...");
		addBtn.setImage(Images.ADD);
		addBtn.setToolTipText("Adds new unicode characters to the list");
		SWTUtil.onSelectionEvent(addBtn, e -> {
			InputDialog dlg = new MultilineInputDialog(getShell(), "Add unicode characters", 
					"Enter unicode character string, i.e. either:"
					+ "\n\t- Provide a unicode code directly (U+XXXX)"
					+ "\n\t- Provide a valid unicode code range (U+XXXX-U+YYYY)"
					+ "\n\t- Copy and paste some characters into the text field."
					+ "\nNote: Unicode codes and ranges have to be separated by whitespaces or new lines"
					+ "\n(unicode tables: https://unicode-table.com)", "", null);
			
			if (dlg.open()==Window.OK) {
				String value = dlg.getValue();
				logger.debug("value = "+value);
				
				List<Pair<Integer,String>> unicodes = UnicodeList.parseUnicodeChars(value);
				
				List<Pair<Integer,String>> addedUnicodes = CoreUtils.addNewElements(this.unicodes, unicodes);
				refreshTable();
				
				if (!addedUnicodes.isEmpty()) { // select and show new elements
					IStructuredSelection sel = new StructuredSelection(addedUnicodes);
					tv.setSelection(sel);
					tv.getTable().showSelection();
					tv.reveal(addedUnicodes.get(addedUnicodes.size()-1));
				}
				else {
					DialogUtil.showErrorMessageBox(getShell(), "", "No new unicode values entered!");
				}
			}
		});
		
		deleteBtn = new Button(this, 0);
		deleteBtn.setText("Remove selected");
		deleteBtn.setToolTipText("Removes the selected unicode characters from the list");
		deleteBtn.setImage(Images.DELETE);
		SWTUtil.onSelectionEvent(deleteBtn, e -> {
			Iterator it = tv.getStructuredSelection().iterator();
			while (it.hasNext()) {
				Pair<Integer, String> el = (Pair<Integer, String>) it.next();
				unicodes.remove(el);
			}
			
			refreshTable();
		});		
		
		tv = new TableViewer(this, SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 150;
		tv.getTable().setLayoutData(gd);
		
		tv.setContentProvider(new ArrayContentProvider());
		tv.getTable().setHeaderVisible(true);
		tv.getTable().setLinesVisible(true);
		
		TableViewerColumn glyphCol = createColumn(tv, "Glyph", 100, true);
		ColumnLabelProvider glyphColLP = new ColumnLabelProvider() {
			Font boldFont = Fonts.createBoldFont(tv.getTable().getFont());
			
			@Override public String getText(Object element) {
				try {
					return getElementValue(element);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
			@Override public Font getFont(Object element) {
				return boldFont;
			}
		};
		glyphCol.setLabelProvider(glyphColLP);
		
		TableViewerColumn unicodeCol = createColumn(tv, "Unicode", 100, true);
		ColumnLabelProvider unicodeColLP = new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				try {
					Integer code = getElementKey(element);
					return String.format("U+%04X", code);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		};
		unicodeCol.setLabelProvider(unicodeColLP);
		
		TableViewerColumn descCol = createColumn(tv, "Description", 300, true);
		ColumnLabelProvider descColLp = new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				try {
					Integer code = getElementKey(element);	
					return Character.getName(code);
				} catch (Exception e) {
					return e.getMessage();
				}
			}
		};
		descCol.setLabelProvider(descColLp);
		
		if (false) {
		TableViewerColumn addCol = createColumn(tv, "", 100, false);
		CellLabelProvider addButtonColLabelProvider = new CellLabelProvider() {
			@Override public void update(final ViewerCell cell) {
				Pair<Integer, String> element = getElement(cell.getElement());

				final TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				
				Button addButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
		        addButton.setImage(Images.getOrLoad("/icons/add_12.png"));
				
		        SWTUtil.onSelectionEvent(addButton, e -> {
		        	// TODO: react on add btn pressed -> insert unicode 
		        });
				          
                Point size = addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				editor.minimumWidth = size.x;
				editor.horizontalAlignment = SWT.LEFT;                                
                editor.setEditor(addButton , item, cell.getColumnIndex());
                editor.layout();
                
                TaggingWidgetUtils.replaceEditor(addEditors, element, editor);
			}
		};
		addCol.setLabelProvider(addButtonColLabelProvider);
		}
		
		createShortCutColumn();
	}
	
	private void createShortCutColumn() {
		TableViewerColumn shortcutCol = createColumn(tv, "Shortcut", 100, false);		
		shortcutCol.setLabelProvider(new CellLabelProvider() {
			Storage storage = Storage.getInstance();
			
			@Override
			public void update(ViewerCell cell) {
				try {
					Pair<Integer, String> vk = getElement(cell.getElement());
					String key = storage.getVirtualKeyShortCutKey(vk);
					if (key != null) {
						cell.setText("Ctrl+"+key);
					}
					else {
						cell.setText("");
					}
				} catch (Exception e) {
					cell.setText(e.getMessage());
				}
			}
			
			@Override
	        public String getToolTipText(Object element) {
	           return "Ctrl + a number between 0 and 9";
	        }
		});

		if (true) { // is editable
		shortcutCol.setEditingSupport(new EditingSupport(tv) {
			Storage storage = Storage.getInstance();
			
			@Override
			protected void setValue(Object element, Object value) {
				Pair<Integer, String> vk = getElement(element);
				String oldShortcut = storage.getVirtualKeyShortCutKey(vk);
				String newShortcut = ""+value;
				logger.debug("setting shortcut for value: "+element+" to: "+newShortcut+" old shorcut: "+oldShortcut);
				if (StringUtils.isEmpty(newShortcut)) { // delete shortcut if empty input
					Pair<Integer, String> removed = storage.removeVirtualKeyShortCut(oldShortcut);
					logger.debug("removed: "+removed);
					tv.refresh(true);
					return;
				}
				if (!storage.isValidVirtualKeyShortCutKey(newShortcut)) {
					return;
				}
				
				try {
					storage.setVirtualKeyShortCut(newShortcut, vk);
					logger.debug("new key: "+storage.getVirtualKeyShortCutKey(vk)+" new value: "+storage.getVirtualKeyShortCutValue(storage.getVirtualKeyShortCutKey(vk)));
					tv.refresh(true);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			@Override
			protected Object getValue(Object element) {
				try {
					Pair<Integer, String> vk = getElement(element);
					String key = storage.getVirtualKeyShortCutKey(vk);
					return key==null ? "" : key;
//					storage.setVirtualKeyShortCut(""+value, vk);
//					tv.refresh(true);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return "";
				}
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor ce = new TextCellEditor(tv.getTable());
				
				// add a "default" description text when no shortcut is set
				ce.getControl().addFocusListener(new FocusAdapter() {				
					@Override
					public void focusGained(FocusEvent e) {
						Pair<Integer, String> vk = getElement(element);
						String key = storage.getVirtualKeyShortCutKey(vk);
						
						if (StringUtils.isEmpty(key)) {
							ce.setValue("Enter a number between 0 and 9");
							ce.performSelectAll();		
						}
					}
				});
				
				ce.setValidator(new ICellEditorValidator() {
					
					@Override
					public String isValid(Object value) {
						String str = (String) value;
						int len = StringUtils.length(str);
						logger.debug("sc = "+str+" len = "+len);
						if (len <= 0) { // empty string are allowed for deleting shortcut
							return null;
						}
						if (len>=2) {
							return "Not a string of size 1!";
						}
						if (!storage.isValidVirtualKeyShortCutKey(""+value)) {
							return "Not a valid shortcut character (0-9)!";
						}
						
						return null;
					}
				});
				
				return ce;
			}
			
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		}
		
	}
		
	private static TableViewerColumn createColumn(TableViewer tv, String text, int width, boolean isResizable) {
		TableViewerColumn col = new TableViewerColumn(tv, SWT.NONE);
		col.getColumn().setText(text);
		col.getColumn().setResizable(isResizable);
		col.getColumn().setWidth(width);
		
		return col;
	}
	
	@SuppressWarnings("unchecked")
	private static Pair<Integer, String> getElement(Object element) {
		if (element instanceof Pair<?, ?>) {
			return (Pair<Integer, String>) element;
		}
		else {
			return null;
		}
	}
	
	private static Integer getElementKey(Object element) {
		Pair<Integer, String> el = getElement(element);
		if (el != null) {
			return el.getKey();
		}
		else {
			return null;
		}
	}
	
	private static String getElementValue(Object element) {
		Pair<Integer, String> el = getElement(element);
		if (el != null) {
			return el.getValue();
		}
		else {
			return null;
		}
	}
	
	public void setUnicodeList(List<Pair<Integer, String>> unicodes) {
		this.unicodes.clear();
		if (unicodes != null) {
			this.unicodes.addAll(unicodes);
		}
		
		refreshTable();
	}
	
	public List<Pair<Integer, String>> getUnicodes() {
		return this.unicodes;
	}
	
	public void refreshTable() {
		tv.setInput(this.unicodes);
		updateEditors();
	}

	private void updateEditors() {
		TaggingWidgetUtils.updateEditors(addEditors, this.unicodes);
	}
	

}
