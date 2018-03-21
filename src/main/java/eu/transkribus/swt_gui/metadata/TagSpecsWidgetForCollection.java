package eu.transkribus.swt_gui.metadata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory.TagRegistryChangeEvent;
import eu.transkribus.swt.util.ColorChooseButton;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TagSpecsWidgetForCollection extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TagSpecsWidgetForCollection.class);
	
	TableViewer tableViewer;
	Label headerLbl;
	
	Map<CustomTagSpec, ControlEditor> insertTagEditors = new ConcurrentHashMap<>();
	Map<CustomTagSpec, ControlEditor> removeTagDefEditors = new ConcurrentHashMap<>();
	Map<CustomTagSpec, ControlEditor> colorEditors = new ConcurrentHashMap<>();
	
//	Map<CustomTagSpec, ControlEditor> moveUpEditors = new ConcurrentHashMap<>();
//	Map<CustomTagSpec, ControlEditor> moveDownEditors = new ConcurrentHashMap<>();
	
	
	public TagSpecsWidgetForCollection(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		Composite topContainer = new Composite(this, SWT.NONE);
		topContainer.setLayout(new GridLayout(1, false));
		
		headerLbl = new Label(topContainer, 0);
		headerLbl.setText("Tags for collection");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
						
		Composite tableContainer = new Composite(topContainer, SWT.NONE);
		tableContainer.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		int tableViewerStyle = SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		tableViewer = new TableViewer(tableContainer, tableViewerStyle);
		tableViewer.getTable().setToolTipText("List of tag specifications that are available in the web interface");
		
//		tagsTableViewer = new TableViewer(taggingGroup, SWT.FULL_SELECTION|SWT.HIDE_SELECTION|SWT.NO_FOCUS | SWT.H_SCROLL
//		        | SWT.V_SCROLL | SWT.FULL_SELECTION /*| SWT.BORDER*/);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd.heightHint = 150;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		
		TableViewerColumn tagDefCol = new TableViewerColumn(tableViewer, SWT.NONE);
		tagDefCol.getColumn().setText("Tag specification");
		tagDefCol.getColumn().setResizable(true);
		tagDefCol.getColumn().setWidth(300);
		ColumnLabelProvider nameColLP = new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				if (!(element instanceof CustomTagSpec)) {
					return "i am error";
				}
				
				CustomTagSpec tagDef = (CustomTagSpec) element;
				String tagDefStr = tagDef.getCustomTag().getCssStr();
				return tagDefStr.replaceAll("\\{\\}", "").trim(); // remove empty properties braces {} 
			}
			
//			@Override public Color getForeground(Object element) {
//				if (!(element instanceof CustomTagSpec)) {
//					return null;
//				}
//				CustomTagSpec tagDef = (CustomTagSpec) element;
//				
//				String tagColor = CustomTagFactory.getTagColor(tagDef.getCustomTag().getTagName());
//				return Colors.decode2(tagColor);
//			}
		};
		tagDefCol.setLabelProvider(nameColLP);
		
		if (true) {
			TableViewerColumn colorCol = new TableViewerColumn(tableViewer, SWT.NONE);
			colorCol.getColumn().setText("Color");
			colorCol.getColumn().setResizable(true);
			colorCol.getColumn().setWidth(50);
			colorCol.setLabelProvider(new CellLabelProvider() {
				@Override public void update(ViewerCell cell) {
					if (!(cell.getElement() instanceof CustomTagSpec)) {
						return;
					}
					
					TableItem item = (TableItem) cell.getItem();
					CustomTagSpec tagSpec = (CustomTagSpec) cell.getElement();
					
					TableEditor editor = new TableEditor(item.getParent());				
	                editor.grabHorizontal  = true;
	                editor.grabVertical = true;
	                editor.horizontalAlignment = SWT.LEFT;
	                editor.verticalAlignment = SWT.TOP;
	                
	                String tagColor = CustomTagFactory.getTagColor(tagSpec.getCustomTag().getTagName());
	                
	                logger.trace("tag color for tag: "+tagSpec.getCustomTag().getTagName()+" color: "+tagColor);
	                
	                if (tagColor == null) {
	                	tagColor = CustomTagFactory.getNewTagColor();
	                }
	                ColorChooseButton colorCtrl = new ColorChooseButton((Composite) cell.getViewerRow().getControl(), Colors.toRGB(tagColor)) {
	                	@Override protected void onColorChanged(RGB rgb) {
	                		CustomTagFactory.setTagColor(tagSpec.getCustomTag().getTagName(), Colors.toHex(rgb));
	                	}
	                };
	                colorCtrl.setEditorEnabled(false);

	                editor.setEditor(colorCtrl , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(colorEditors, tagSpec, editor);
				}
			});
		}
		
		TableViewerColumn shortcutCol = new TableViewerColumn(tableViewer, SWT.NONE);
		shortcutCol.getColumn().setText("Shortcut");
		shortcutCol.getColumn().setResizable(false);
		shortcutCol.getColumn().setWidth(100);
		
		shortcutCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				String text = "";
				logger.trace("element = "+element);
				if (!(element instanceof CustomTagSpec)) {
					cell.setText("i am error");
				}
				
				CustomTagSpec tagDef = (CustomTagSpec) element;
				if (tagDef.getShortCut()!=null) {
					text = "Alt+"+tagDef.getShortCut();
				}
				else {
					text = "";
				}
				
				cell.setText(text);
			}
			
			@Override
	        public String getToolTipText(Object element) {
	           return "Alt + a number between 0 and 9";
	        }
		});
						
		tableViewer.refresh(true);
		tableViewer.getTable().pack();
		
		if (true) {
			Composite btnsComp = new Composite(tableContainer, 0);
			btnsComp.setLayout(new RowLayout(SWT.VERTICAL));
			btnsComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
			
			Button removeBtn = new Button(btnsComp, 0);
			removeBtn.setImage(Images.DELETE);
			removeBtn.setToolTipText("Remove selected tag from list");
			SWTUtil.onSelectionEvent(removeBtn, e -> {
				removeSelected();
			});
			
//			Button moveUpBtn = new Button(btnsComp, 0);
//			moveUpBtn.setImage(Images.getOrLoad("/icons/arrow_up.png"));
//			moveUpBtn.setToolTipText("Move up selected");
//			SWTUtil.onSelectionEvent(moveUpBtn, e -> {
//				moveSelected(true);
//			});			
//			
//			Button moveDownBtn = new Button(btnsComp, 0);
//			moveDownBtn.setImage(Images.getOrLoad("/icons/arrow_down.png"));
//			moveDownBtn.setToolTipText("Move down selected");
//			SWTUtil.onSelectionEvent(moveDownBtn, e -> {
//				moveSelected(false);
//			});			
		}		

		topContainer.layout(true);
		
		//load tag definitions from DB
		Storage.getInstance().readCollectionTagSpecsFromDB();

		updateAvailableTagSpecs();

		Storage.getInstance().addListener(new IStorageListener() {
			public void handlTagDefsChangedEvent(TagDefsChangedEvent e) {
				updateAvailableTagSpecs();
			}
		});
		
		CustomTagFactory.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if (arg instanceof TagRegistryChangeEvent) {
					logger.debug("TagRegistryChangeEvent: "+arg);
					updateAvailableTagSpecs();
				}				
			}
		});
		
		TrpConfig.getTrpSettings().addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(TrpSettings.SHOW_ALL_TAGS_IN_TAG_EDITOR_PROPERTY)) {
					updateAvailableTagSpecs();
				}
			}
		});
	}
	
	private void removeSelected() {
		CustomTagSpec cDef = getSelected();
		if (cDef != null) {
			Storage.getInstance().removeCustomTagSpec(cDef, true);
			Storage.getInstance().updateCustomTagSpecsForCurrentCollectionInDB();
		}
	}
	
	private void synchronizeDB(){
		Storage.getInstance().updateCustomTagSpecsForCurrentCollectionInDB();
	}
	
	private void moveSelected(boolean moveUp) {
		CustomTagSpec tagDef = getSelected();
		if (tagDef==null) {
			return;
		}
		
		logger.debug("moving selected: "+tagDef);
		
		List<CustomTagSpec> cDefs = Storage.getInstance().getCustomTagSpecs();
		int i = cDefs.indexOf(tagDef);
		if (moveUp && i>=1) {
			if (cDefs.remove(tagDef)) {
				cDefs.add(i-1, tagDef);
				Storage.getInstance().signalCustomTagSpecsChanged();
			}
		}
		else if (!moveUp && i<cDefs.size()-1) {
			if (cDefs.remove(tagDef)) {
				cDefs.add(i+1, tagDef);
				Storage.getInstance().signalCustomTagSpecsChanged();
			}
		}
	}
	
	private void updateAvailableTagSpecs() {
		logger.info("updating available tag specs: "+Storage.getInstance().getCustomTagSpecsForCurrentCollection());
		Display.getDefault().asyncExec(() -> {
			if (SWTUtil.isDisposed(tableViewer.getTable()) || SWTUtil.isDisposed(this)) {
				return;
			}
			headerLbl.setText("Tags for collection");
			tableViewer.setInput(Storage.getInstance().getCustomTagSpecsForCurrentCollection());

			Collection<CustomTagSpec> tagSpecs = (Collection<CustomTagSpec>) tableViewer.getInput();
			TaggingWidgetUtils.updateEditors(colorEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(removeTagDefEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(insertTagEditors, tagSpecs);
//			TaggingWidgetUtils.updateEditors(moveUpEditors, tagSpecs);
//			TaggingWidgetUtils.updateEditors(moveDownEditors, tagSpecs);
			
			tableViewer.refresh(true);
		});
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public CustomTagSpec getSelected() {
		return (CustomTagSpec) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
	}

}
