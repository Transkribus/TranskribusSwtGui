package eu.transkribus.swt_gui.metadata;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.MyCheckboxEditor;
import eu.transkribus.swt.util.MyTextCellEditor;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;

public class CustomTagPropertyTable extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(CustomTagPropertyTable.class);
	
	TableViewer tv;
	Table table;
	
	TableViewerColumn nameCol;
	TableViewerColumn valueCol;
	EditingSupport valueEditingSupport;

	private CustomTag prototypeTag;

	private CustomTag selectedTag;
	boolean withOffsetLengthContinuedProperties;
	
	public interface ICustomTagPropertyTableListener {
		void onPropertyChanged(CustomTagAttribute property); 
	}
	
	List<ICustomTagPropertyTableListener> listener = new ArrayList<>();
	
	public CustomTagPropertyTable(Composite parent, int style) {
		this(parent, style, true);
	}

	public CustomTagPropertyTable(Composite parent, int style, boolean withOffsetLengthContinuedProperties) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		this.withOffsetLengthContinuedProperties = withOffsetLengthContinuedProperties;
		
		tv = new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		nameCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "Property", 100);
		valueCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "Value", 100);
		
		// LABEL PROVIDERS:
		nameCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				// change font to italic if this is a predefined attribute:
				if (prototypeTag != null) {
					CustomTagAttribute a = getEntryAttribute(cell.getElement());
					boolean isPredefined = prototypeTag.isPredefinedAttribute(a.getName());
					if (isPredefined) {
						Font f = Fonts.createItalicFont(cell.getFont());
						cell.setFont(f);
					}
				}
				
				
//				if (!a.isEditable()) {
//					cell.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//				} else
//					cell.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
				
				cell.setText(getEntryAttribute(cell.getElement()).getName());
			}
		});
		
		valueCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				CustomTagAttribute a = getEntryAttribute(cell.getElement());
				if (!a.isEditable()) {
					cell.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				} else
					cell.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));				
				
				Object v = getEntryValue(cell.getElement());
				if (v == null)
					cell.setText("");
				else
					cell.setText(String.valueOf(v));
				
//				cell.setText(String.valueOf(getEntryValue(cell.getElement())));
			}
		});
		
		
		
		// CONTENT PROVIDER:
		tv.setContentProvider(new IStructuredContentProvider() {
			@Override public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override public void dispose() {
			}
			
			@Override public Object[] getElements(Object inputElement) {
				if (prototypeTag != null) {
					Map<CustomTagAttribute, Object> m = prototypeTag.getAttributesValuesMap();
					logger.debug("getElements, nr of props = "+m.size()+" nr of atts = "+prototypeTag.getAttributes().size()+" class ="+prototypeTag.getClass().getSimpleName());
					logger.debug("att names: "+prototypeTag.getAttributeNames());
					
					// create list of entries with offset/length/continued properties first:
					List<Entry<CustomTagAttribute, Object>> entries = new ArrayList<>();
					
					if (withOffsetLengthContinuedProperties) {
						entries.add(new SimpleEntry(CustomTag.OFFSET_PROPERTY,  m.get(CustomTag.OFFSET_PROPERTY)));
						entries.add(new SimpleEntry(CustomTag.LENGTH_PROPETY,  m.get(CustomTag.LENGTH_PROPETY)));
						entries.add(new SimpleEntry(CustomTag.CONTINUED_PROPERTY,  m.get(CustomTag.CONTINUED_PROPERTY)));
					}
					
					for (CustomTagAttribute a : m.keySet()) {
						if (CustomTag.isOffsetOrLengthOrContinuedProperty(a.getName()))
							continue;
						
						entries.add(new SimpleEntry(a, m.get(a)));
					}					
					
					return entries.toArray();
//					return prototypeTag.getAttributesValuesMap().entrySet().toArray();
				}
				
				return null;
			}
		});
		
		// EDITING SUPPORT:
		valueEditingSupport = new EditingSupport(tv) {
			@Override protected void setValue(Object element, Object value) {
				CustomTagAttribute a = getEntryAttribute(element);
				logger.debug("setting attribute value, att = "+a+" vaue = "+value);
				try {
					if (selectedTag != null) {
						selectedTag.setAttribute(a.getName(), value, true);
					}
				} catch (IOException e) {
					logger.error("Error applying attribute value from editor: "+e.getMessage(), e);
				}
				
				tv.refresh(true);
			}
			
			@Override protected Object getValue(Object element) {
				return getEntryValue(element);
			}
			
			@Override protected CellEditor getCellEditor(Object element) {
				if (prototypeTag == null)
					return null;
				
				CustomTagAttribute a = getEntryAttribute(element);
				CellEditor e = null;
				
				Class<?> t = prototypeTag.getAttributeType(a.getName());
				
				logger.debug("cell editor, att = "+a.getName()+" type = "+t);
				if (t.equals(Boolean.class) || t.equals(boolean.class)) {
					e = new MyCheckboxEditor(table);
				}
				// FIXME: true for every class???
				else if (Enum.class.getClass().isAssignableFrom(t)) {
					ComboBoxViewerCellEditor cbe = new ComboBoxViewerCellEditor(table);
					cbe.setContentProvider(new ArrayContentProvider());
					cbe.setInput(t.getEnumConstants());
					cbe.setLabelProvider(new LabelProvider());
					e = cbe;					
				}
				else {
					e = new MyTextCellEditor(table);
				}
				ICellEditorValidator v = SWTUtil.createNumberCellValidator(t);
				if (v != null)
					e.setValidator(v);
				
				e.addListener(new ICellEditorListener() {
					
					@Override
					public void editorValueChanged(boolean arg0, boolean arg1) {
						logger.trace("VALUE OF EDITOR CHANGED!!");						
					}
					
					@Override
					public void cancelEditor() {
						logger.trace("EDITOR CANCELED!!");
					}
					
					@Override
					public void applyEditorValue() {
						listener.stream().forEach(l -> { l.onPropertyChanged(a); });
					}
				});
												
				return e;
			}
			
			@Override protected boolean canEdit(Object element) {
				return getEntryAttribute(element).isEditable();
			}
		};
		valueCol.setEditingSupport(valueEditingSupport);
		
//		tv.getTable().addTraverseListener(new TraverseListener() {
//			@Override
//			public void keyTraversed(TraverseEvent e) {
//				e.doit = false;
//				
//				System.out.println("traversed!");
//				if (e.detail == SWT.TRAVERSE_RETURN) {
//					e.doit = false;
//					
//					System.out.println("return!");
//					
//					
//					
//				}
//			}
//		});
		
//		tv.getTable().addKeyListener(new KeyListener() {
//			@Override
//			public void keyReleased(KeyEvent e) {
//				System.out.println("key released: "+e);
//				if (e.keyCode == 0x1000050) {
//					System.out.println("enter released!");
//				}
//			}
//			
//			@Override
//			public void keyPressed(KeyEvent e) {
//				System.out.println("key pressed!");
//				
//			}
//		});
		
		initTraverseStuff();
	}
	
	public void addListener(ICustomTagPropertyTableListener l) {
		listener.add(l);
	}
	
	public boolean removeListener(ICustomTagPropertyTableListener l) {
		return listener.remove(l);
	}
	
	private void initTraverseStuff() {
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(tv, new FocusCellOwnerDrawHighlighter(tv));

		ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(tv) {
		    protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
		        if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) {
		            EventObject source = event.sourceEvent;
		            if (source instanceof MouseEvent && ((MouseEvent)source).button == 3)
		                return false;
		        }
		        return super.isEditorActivationEvent(event) || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR);
		    }
		};

		TableViewerEditor.create(tv, focusCellManager, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL | 
		    ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | 
		    ColumnViewerEditor.TABBING_VERTICAL |
		    ColumnViewerEditor.KEYBOARD_ACTIVATION
		    
			);		
	}

	public void selectNextAttribute() {
		if (prototypeTag == null) {
			return;
		}
		
		Entry<CustomTagAttribute, Object> entry = getSelectedEntry();
		
		int index=-1;
		int N = tv.getTable().getItemCount();
		for (int i=0; i<N; ++i) {
			if (tv.getElementAt(i) == entry) {
				index=i;
				break;
			}
		}
		
		logger.debug("index = "+index);
		
		int nextIndex = (index+1) % N;
		selectAttribute(nextIndex);
	}
	
	public void selectAttribute(int index) {
		int N = tv.getTable().getItemCount();
		if (prototypeTag!=null && index>=0 && index<N) {
			Entry<CustomTagAttribute, Object> entry = (Entry<CustomTagAttribute, Object>) tv.getElementAt(index);
			tv.editElement(entry, 1);
		}
	}
	
	public void selectFirstAttribute() {
		selectAttribute(0);
	}
	 
	public TableViewer getTableViewer() {
		return tv;
	}
	
	public CustomTag getPrototypeTag() { return prototypeTag; }
	public CustomTag getSelectedTag() { return selectedTag; }
	
	private Entry<CustomTagAttribute, Object> getSelectedEntry() {
		return getEntry(((IStructuredSelection) tv.getSelection()).getFirstElement());
	}
	
	private static Entry<CustomTagAttribute, Object> getEntry(Object element) {
		return (Entry<CustomTagAttribute, Object>) element;
	}
		
	private static Object getEntryValue(Object element) {
		return getEntry(element).getValue();
	}
	
	private static CustomTagAttribute getEntryAttribute(Object element) {
		return getEntry(element).getKey();
	}	
	
	public void setInput(CustomTag prototypeTag, CustomTag selectedTag) {
		logger.debug("setting input of property table to: "+prototypeTag+" selected: "+selectedTag);
		this.prototypeTag = prototypeTag;
		this.selectedTag = selectedTag;
		tv.setInput(prototypeTag);		
		tv.refresh();
	}
	
	public CustomTagAttribute getSelectedProperty() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (!sel.isEmpty()) {
			return getEntryAttribute(sel.getFirstElement());
		} else
			return null;
	}
	
}
