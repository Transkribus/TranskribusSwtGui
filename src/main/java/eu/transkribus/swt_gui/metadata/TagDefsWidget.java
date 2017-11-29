package eu.transkribus.swt_gui.metadata;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.swt.util.ColorChooseButton;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TagDefsWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TagDefsWidget.class);
	
	TableViewer tableViewer;
	
	Map<CustomTagDef, ControlEditor> insertTagEditors = new HashMap<>();
	Map<CustomTagDef, ControlEditor> removeTagDefEditors = new HashMap<>();
	Map<CustomTagDef, ControlEditor> colorEditors = new HashMap<>();
	
	boolean isEditable=true;
	
	public TagDefsWidget(Composite parent, int style, boolean isEditable) {
		super(parent, style);
		setLayout(new FillLayout());
		
		this.isEditable = isEditable;
		int nCols = isEditable ? 1 : 2;

		Composite container = new Composite(this, SWT.NONE);
//		container.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		container.setLayout(new GridLayout(nCols, false));
		
		Label headerLbl = new Label(container, 0);
		headerLbl.setText("Tag defintions for current collection");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (!isEditable) {
			Button editBtn = new Button(container, 0);
			editBtn.setText("Edit..");
			editBtn.setImage(Images.PENCIL);
			SWTUtil.onSelectionEvent(editBtn, e -> {
				TagConfDialog diag = new TagConfDialog(getShell());
				diag.open();
			});
		}
		
		if (isEditable) {
			Button enforceEqualColorsForEqualTagNamesBtn = new Button(container, SWT.CHECK);
			enforceEqualColorsForEqualTagNamesBtn.setText("Enforce equal colors for equal tag names");
			enforceEqualColorsForEqualTagNamesBtn.setToolTipText("Enforces equal colors for tags with the same name but different attributes");
			enforceEqualColorsForEqualTagNamesBtn.setSelection(true);
			DataBinder.get().bindBeanToWidgetSelection(TrpSettings.ENFORCE_EQUAL_COLORS_FOR_EQUAL_TAG_NAMES_PROPERTY, TrpConfig.getTrpSettings(), enforceEqualColorsForEqualTagNamesBtn);
		}
		
		int tableViewerStyle = SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		tableViewer = new TableViewer(container, tableViewerStyle);
		tableViewer.getTable().setToolTipText("List of tag definitions that are available in the user interface");
		
//		tagsTableViewer = new TableViewer(taggingGroup, SWT.FULL_SELECTION|SWT.HIDE_SELECTION|SWT.NO_FOCUS | SWT.H_SCROLL
//		        | SWT.V_SCROLL | SWT.FULL_SELECTION /*| SWT.BORDER*/);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, nCols, 1);
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd.heightHint = 150;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		
		TableViewerColumn tagDefCol = new TableViewerColumn(tableViewer, SWT.NONE);
		tagDefCol.getColumn().setText("Tag definition");
		tagDefCol.getColumn().setResizable(true);
		tagDefCol.getColumn().setWidth(300);
		ColumnLabelProvider nameColLP = new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				if (!(element instanceof CustomTagDef)) {
					return "i am error";
				}
				
				CustomTagDef tagDef = (CustomTagDef) element;
				return tagDef.getCustomTag().getCssStr();
			}
		};
		tagDefCol.setLabelProvider(nameColLP);
		
		if (true) {
			TableViewerColumn colorCol = new TableViewerColumn(tableViewer, SWT.NONE);
			colorCol.getColumn().setText("Color");
			colorCol.getColumn().setResizable(true);
			colorCol.getColumn().setWidth(50);
			colorCol.setLabelProvider(new CellLabelProvider() {
				@Override public void update(ViewerCell cell) {
					if (!(cell.getElement() instanceof CustomTagDef)) {
						return;
					}
					
					TableItem item = (TableItem) cell.getItem();
					CustomTagDef tagDef = (CustomTagDef) cell.getElement();
					
					TableEditor editor = new TableEditor(item.getParent());				
	                editor.grabHorizontal  = true;
	                editor.grabVertical = true;
	                editor.horizontalAlignment = SWT.LEFT;
	                editor.verticalAlignment = SWT.TOP;
	                
	                ColorChooseButton colorCtrl = new ColorChooseButton((Composite) cell.getViewerRow().getControl(), tagDef.getRGB()) {
	                	@Override protected void onColorChanged(RGB rgb) {
	                		tagDef.setRGB(rgb);
	                		logger.info("color of tag def changed, tagDef: "+tagDef);
	                		Storage.getInstance().signalCustomTagDefsChanged();
	                	}
	                };
	                colorCtrl.setEditorEnabled(isEditable);

	                editor.setEditor(colorCtrl , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(colorEditors, tagDef, editor);
					
				}
			});
			
		}

		if (this.isEditable) { // remove btn for table rows
			TableViewerColumn removeBtnCol = new TableViewerColumn(tableViewer, SWT.NONE);
			removeBtnCol.getColumn().setText("");
			removeBtnCol.getColumn().setResizable(false);
			removeBtnCol.getColumn().setWidth(100);
			
			class RemoveTagDefSelectionListener extends SelectionAdapter {
				CustomTagDef tagDef;
				
				public RemoveTagDefSelectionListener(CustomTagDef tagDef) {
					this.tagDef = tagDef;
				}
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Storage.getInstance().removeCustomTag(tagDef);
	 				tableViewer.refresh();
				}
			};
			
			CellLabelProvider removeButtonColLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
								
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button removeButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
	//		        addButton.setImage(Images.ADD);
			        removeButton.setImage(Images.getOrLoad("/icons/delete_12.png"));
			        removeButton.setToolTipText("Remove tag definition");
			        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
			        
			        CustomTagDef tagDef = (CustomTagDef) cell.getElement();	
			        removeButton.addSelectionListener(new RemoveTagDefSelectionListener(tagDef));
			        Control c = removeButton;
			        
	                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					editor.minimumWidth = size.x;
					editor.horizontalAlignment = SWT.LEFT;
	                editor.setEditor(c , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(removeTagDefEditors, tagDef, editor);
				}
			};
			removeBtnCol.setLabelProvider(removeButtonColLabelProvider);
		}
		
		if (!this.isEditable) { // add an "add tag button" to add the tag to the current position in the transcription widget 
			TableViewerColumn addButtonCol = new TableViewerColumn(tableViewer, SWT.NONE);
			addButtonCol.getColumn().setText("");
			addButtonCol.getColumn().setResizable(false);
			addButtonCol.getColumn().setWidth(100);
			
			CellLabelProvider addButtonColLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
					CustomTagDef tagDef = (CustomTagDef) cell.getElement();
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button addBtn = new Button((Composite) cell.getViewerRow().getControl(), 0);
					addBtn.setImage(Images.ADD_12);
					addBtn.setToolTipText("Insert this tag at the selected position in the transcription");
					SWTUtil.onSelectionEvent(addBtn, e -> {
//						CustomTagDef selTagDef = getSelected();
						if (TrpMainWidget.getInstance() != null && tagDef != null && tagDef.getCustomTag()!=null) {
							CustomTag ct = tagDef.getCustomTag();
							TrpMainWidget.getInstance().addTagForSelection(ct.getTagName(), ct.getAttributeNamesValuesMap(), null);
						}
					});
					                
	                Point size = addBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	                
					editor.minimumWidth = size.x;
					editor.horizontalAlignment = SWT.LEFT;
					
	                editor.setEditor(addBtn , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(insertTagEditors, tagDef, editor);
				}
			};
			addButtonCol.setLabelProvider(addButtonColLabelProvider);
		} // end add button column
		
		tableViewer.refresh(true);
		tableViewer.getTable().pack();

		container.layout(true);
		
		
		updateTagDefsFromStorage();

		Storage.getInstance().addListener(new IStorageListener() {
			public void handlTagDefsChangedEvent(TagDefsChangedEvent e) {
				updateTagDefsFromStorage();
			}
		});
	}
	
	private void updateTagDefsFromStorage() {
		logger.info("updating tag defs from storage: "+Storage.getInstance().getCustomTagDefs());
		Display.getDefault().syncExec(() -> {
			if (SWTUtil.isDisposed(tableViewer.getTable()) || SWTUtil.isDisposed(this)) {
				return;
			}
			
			tableViewer.setInput(Storage.getInstance().getCustomTagDefs());
			tableViewer.refresh();
			
			TaggingWidgetUtils.updateEditors(colorEditors, Storage.getInstance().getCustomTagDefs());
			TaggingWidgetUtils.updateEditors(removeTagDefEditors, Storage.getInstance().getCustomTagDefs());
			TaggingWidgetUtils.updateEditors(insertTagEditors, Storage.getInstance().getCustomTagDefs());
		});
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public CustomTagDef getSelected() {
		return (CustomTagDef) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
	}

}
