package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.CustomTagList;
import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class CustomTagPropertyTableNew extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(CustomTagPropertyTableNew.class);
	
	TableViewer tv;
	Table table;
	
	TableViewerColumn nameCol;
	TableViewerColumn valueCol;

	private CustomTag selectedTag;
	boolean showNonEditableProperties=false;
	
	public interface ICustomTagPropertyTableNewListener {
		void onPropertyChanged(CustomTag tag, String name, Object value); 
	}
	
	List<ICustomTagPropertyTableNewListener> listener = new ArrayList<>();
	
	public CustomTagPropertyTableNew(Composite parent, int style, boolean showNonEditableProperties) {
		super(parent, style);
		this.setLayout(new FillLayout());
		
		this.showNonEditableProperties = showNonEditableProperties;
		
		tv = new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		nameCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "Property", 100);
		valueCol = TableViewerUtils.createTableViewerColumn(tv, SWT.LEFT, "Value", 400);
		
		// LABEL PROVIDERS:
		nameCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				String name = "";
				if (selectedTag != null) {
					name = (String) cell.getElement();
					if (selectedTag.isPredefinedAttribute(name)) {
						Font f = Fonts.createItalicFont(cell.getFont());
						cell.setFont(f);						
					}
				}
				cell.setText(name);
			}
		});
		
		valueCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				if (selectedTag==null) {
					return;
				}
				
				if (!canEdit(cell.getElement())) { // for editable values, the editor with it's content will always be visible!
					String attName = getAttributeName(cell.getElement());
					Object value = getAttributeValue(cell.getElement());
					
					cell.setText(value==null ? "" : String.valueOf(value));
					if (CustomTag.isOffsetOrLengthOrContinuedProperty(attName)) {
						cell.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
					}
					else {
						cell.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
					}
				}
				else {
					cell.setBackground(Colors.getSystemColor(SWT.COLOR_WHITE));
					cell.setText("");
				}
			}
		});
		tv.setContentProvider(ArrayContentProvider.getInstance());
		
		initTraverseStuff();
	}
	
	private void setValue(Object element, Object value) {
		if (selectedTag == null) {
			return;
		}
		String attName = getAttributeName(element);
		logger.debug("setting attribute value, att = "+attName+" vaue = "+value);
		try {
			if (!StringUtils.isEmpty(attName)) {
				// OLD VERSION: just apply for tag in this line:
//				selectedTag.setAttribute(attName, value, true);
//				tv.update(element, null);
//				listener.stream().forEach(l -> { 
//					l.onPropertyChanged(selectedTag, attName, value);
//				});						

				// NEW VERSION: apply for tag and continuations
				CustomTagUtil.applyForAllTagsAndContinuations(selectedTag, tag-> {
					try {
						tag.setAttribute(attName, value, true);
						tv.update(element, null);
						
						listener.stream().forEach(l -> { 
							l.onPropertyChanged(tag, attName, value);
						});
					}
					catch (Exception e) {
						logger.error("Error applying attribute value from editor: "+e.getMessage(), e);
					}
					logger.debug("tag after attribute set: "+selectedTag);
				});
				Storage.getInstance().setCurrentTranscriptEdited(true);
			}
		} catch (Exception e) {
			logger.error("Error applying attribute value from editor: "+e.getMessage(), e);
		}
		
//		tv.refresh(true);
//		tv.update(element, null);
	}
	
	private boolean canEdit(Object element) {
		if (selectedTag == null) {
			return false;
		}
		
		String attName = getAttributeName(element);
		return !CustomTag.isOffsetOrLengthOrContinuedProperty(attName);
	}
	
	public String getAttributeName(Object element) {
		return element==null || selectedTag==null ? "" : (String) element;
	}
	
	public Object getAttributeValue(Object element) {
		String attName = getAttributeName(element);
		if (!StringUtils.isEmpty(attName) && selectedTag!=null) {
			return selectedTag.getAttributeValue(attName);
		}
		else {
			return null;
		}
	}
	
	public void addListener(ICustomTagPropertyTableNewListener l) {
		listener.add(l);
	}
	
	public boolean removeListener(ICustomTagPropertyTableNewListener l) {
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
		if (selectedTag == null) {
			return;
		}

		int i = tv.getTable().getSelectionIndex();
		
		logger.debug("index = "+i);
		int nextIndex = (i+1) % tv.getTable().getItemCount();
		selectAttribute(nextIndex);
	}
	
	public void selectAttribute(int index) {
		int N = tv.getTable().getItemCount();
		if (selectedTag!=null && index>=0 && index<N) {
			tv.editElement(tv.getElementAt(index), 1);
		}
	}
	
	public void selectFirstAttribute() {
		selectAttribute(0);
	}
	 
	public TableViewer getTableViewer() {
		return tv;
	}
	
//	public CustomTag getPrototypeTag() { return prototypeTag; }
	public CustomTag getSelectedTag() { return selectedTag; }
	
//	private Entry<CustomTagAttribute, Object> getSelectedEntry() {
//		return getEntry(((IStructuredSelection) tv.getSelection()).getFirstElement());
//	}
	
//	private static Entry<CustomTagAttribute, Object> getEntry(Object element) {
//		return (Entry<CustomTagAttribute, Object>) element;
//	}
//		
//	private static Object getEntryValue(Object element) {
//		return getEntry(element).getValue();
//	}
//	
//	private static CustomTagAttribute getEntryAttribute(Object element) {
//		return getEntry(element).getKey();
//	}	
	
	public void setInput(CustomTag selectedTag) {
		try {
			logger.debug("setting input of property table to: selected: "+selectedTag);
			if (selectedTag == null) {
				this.selectedTag = null;
				tv.setInput(null);
				clearEditors();
				return;
			}
			
			this.selectedTag = selectedTag;
			CustomTagList ctl = selectedTag.getCustomTagList();
			ITrpShapeType shape = ctl==null ? null : ctl.getShape();
			
	//		if (ctl==null) {
	//			DialogUtil.showErrorMessageBox(getShell(), "Error setting tag", "This tag has no customtaglist!");
	//			return;
	//		}
	//		ITrpShapeType shape = ctl.getShape();
	//		if (shape==null) {
	//			DialogUtil.showErrorMessageBox(getShell(), "Error setting tag", "This customtaglist has no shape!");
	//			return;
	//		}		
			
			logger.debug("selectedTag: "+this.selectedTag+", shape: "+shape);
			CustomTag prototypeTag = CustomTagFactory.getTagObjectFromRegistry(selectedTag.getTagName());
			if (prototypeTag != null) {
				prototypeTag = prototypeTag.copy();
			}
			else {
				prototypeTag = selectedTag.copy();
			}
			logger.debug("prototypeTag: "+prototypeTag);
			
			boolean caseSensitiveOrder = false;
			List<String> attNames = prototypeTag.getAttributeNamesSortedByName(caseSensitiveOrder);
			// 1st: remove all non-editable properties
			attNames.remove(CustomTag.OFFSET_PROPERTY_NAME);
			attNames.remove(CustomTag.LENGTH_PROPERTY_NAME);
			attNames.remove(CustomTag.CONTINUED_PROPERTY_NAME);
			
			// add attributes that are unique to selectedTag
			for (String an : selectedTag.getAttributeNamesSortedByName(caseSensitiveOrder)) {
				if (!attNames.contains(an) && !CustomTag.isOffsetOrLengthOrContinuedProperty(an)) {
					attNames.add(an);
				}
			}
			Collections.sort(attNames, String.CASE_INSENSITIVE_ORDER);
			
			// if non-editable properties shall be shown, add them to the top of the list
			if (showNonEditableProperties) {
				int i=0;
				attNames.add(i++, CustomTag.OFFSET_PROPERTY_NAME);
				attNames.add(i++, CustomTag.LENGTH_PROPERTY_NAME);
				attNames.add(i++, CustomTag.CONTINUED_PROPERTY_NAME);
			}
			
			tv.setInput(attNames);
			createEditors();
			tv.refresh(); // needed?
		} catch (Exception e) {
			TrpMainWidget.getInstance().onError("Unable to set input for text tag property editor!", e.getMessage(), e);
		}
	}
	
	List<TableEditor> editors = new ArrayList<>();
//	DelayedTask setValueDelayedTask = new DelayedTask(task, isGuiTask);
	
	private void clearEditors() {
		for (TableEditor e : editors) {
			TaggingWidgetUtils.deleteEditor(e);
		}
		editors.clear();
	}
	
	private void createEditors() {
		clearEditors();
		
		for (TableItem item : tv.getTable().getItems()) {
			Object element = item.getData();

			if (!canEdit(element)) {
				continue;
			}
			
			TableEditor editor = new TableEditor(tv.getTable());
			Control ctrl = createEditor(element);
		    editor.grabHorizontal = true;
//		    editor.minimumWidth = 100;
		    editor.grabVertical = true;
		    editor.setEditor(ctrl, item, 1);
			
		    editors.add(editor);
		}
		
	}
	
	private Control createEditor(Object element) {
		Control ctrl;
		
		String attName = getAttributeName(element);
		Object value = getAttributeValue(element);
		Class<?> t = selectedTag.getAttributeType(attName);
		
		logger.debug("cell editor, att = "+attName+" type = "+t);
		if (t != null && (t.equals(Boolean.class) || t.equals(boolean.class))) { // e.g. for boolean values in TextStyleTypeTag
			logger.trace("creating a checkbox!");
			Button checkBox = new Button(table, SWT.CHECK);
			checkBox.setSelection(value==null ? false : (boolean) value);
			SWTUtil.onSelectionEvent(checkBox, evt -> {
				setValue(element, checkBox.getSelection());
			});
			ctrl = checkBox;
		}
		else if (t != null && t.isEnum()) { // e.g. for colors in TextStyleTypeTag
			logger.trace("creating a comboviewer!");
			ComboViewer combo = new ComboViewer(table, SWT.READ_ONLY);
			combo.setContentProvider(ArrayContentProvider.getInstance());
			combo.setInput(t.getEnumConstants());
			combo.setLabelProvider(new LabelProvider());
			Fonts.setFontHeight(combo.getControl(), table.getFont().getFontData()[0].getHeight()-1); // to fit it into the table cell height...
			if (value!=null) {
				combo.setSelection(new StructuredSelection(value));
			}
			combo.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent arg0) {
					StructuredSelection sel = (StructuredSelection) combo.getSelection();
					setValue(element, sel.getFirstElement());
				}
			});
			ctrl = combo.getControl();				
		}
		else {
			logger.trace("creating a textfield!");
			Text text = new Text(table, SWT.NONE);
			text.setText(value==null ? "" : String.valueOf(value));
			text.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setValue(element, text.getText());
				}
			});
			ctrl = text;
		}
		
		// TODO: add validators depending on type! (int, float ...)
		
		return ctrl;
	}
	
	public void setShowNonEditableProperties(boolean showNonEditableProperties) {
		this.showNonEditableProperties = showNonEditableProperties;
		reload();
	}
	
	/**
	 * @deprecated may cause attribute leak...
	 */
	private void reload() {
		this.setInput(selectedTag);
	}

	public CustomTagAttribute getSelectedProperty() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (!sel.isEmpty()) {
			return selectedTag.getAttribute(getAttributeName(sel.getFirstElement()));
		} else
			return null;
	}
	
}
