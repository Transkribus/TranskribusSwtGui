package eu.transkribus.swt_gui.metadata;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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
import eu.transkribus.core.model.beans.customtags.CustomTagFactory.TagRegistryChangeEvent;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.swt.util.ColorChooseButton;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class TagSpecsWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TagSpecsWidget.class);
	
	TableViewer tableViewer;
	Button showAllTagsBtn, customizeBtn;
	Label headerLbl;
	
	Map<CustomTagSpec, ControlEditor> insertTagEditors = new ConcurrentHashMap<>();
	Map<CustomTagSpec, ControlEditor> removeTagDefEditors = new ConcurrentHashMap<>();
	Map<CustomTagSpec, ControlEditor> colorEditors = new ConcurrentHashMap<>();
	
	Map<CustomTagSpec, ControlEditor> moveUpEditors = new ConcurrentHashMap<>();
	Map<CustomTagSpec, ControlEditor> moveDownEditors = new ConcurrentHashMap<>();
	
	boolean isEditable=true;
	
	Composite header;
	
	public interface TagSpecsWidgetListener {
		public void addTagButtonClicked(CustomTagSpec tagSpec);
	}
	
	List<TagSpecsWidgetListener> listener = new ArrayList<>();
	
	public TagSpecsWidget(Composite parent, int style, boolean isEditable) {
		super(parent, style);
		setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		this.isEditable = isEditable;
		int nCols = isEditable ? 1 : 3;

		header = new Composite(this, SWT.NONE);
		header.setLayout(new GridLayout(nCols, false));
		header.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		headerLbl = new Label(header, 0);
		headerLbl.setText("Tag specifications");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		if (!isEditable) {
			showAllTagsBtn = new Button(header, SWT.CHECK | SWT.FLAT);
			showAllTagsBtn.setText("Show all");
			showAllTagsBtn.setToolTipText("Show all available tags");
			DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_ALL_TAGS_IN_TAG_EDITOR_PROPERTY, TrpConfig.getTrpSettings(), showAllTagsBtn);
			
			customizeBtn = new Button(header, 0);
			customizeBtn.setText("Customize...");
			customizeBtn.setImage(Images.PENCIL);
			SWTUtil.onSelectionEvent(customizeBtn, e -> {
				TagConfDialog diag = new TagConfDialog(getShell());
				diag.open();
			});
		}
				
		Composite tableContainer = new Composite(this, SWT.NONE);
		tableContainer.setLayout(SWTUtil.createGridLayout(isEditable ? 2 : 1, false, 0, 0));
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		int tableViewerStyle = SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		tableViewer = new TableViewer(tableContainer, tableViewerStyle);
		tableViewer.getTable().setToolTipText("List of tag specifications");
		
//		tagsTableViewer = new TableViewer(taggingGroup, SWT.FULL_SELECTION|SWT.HIDE_SELECTION|SWT.NO_FOCUS | SWT.H_SCROLL
//		        | SWT.V_SCROLL | SWT.FULL_SELECTION /*| SWT.BORDER*/);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, isEditable ? 1 : 2, 1);
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd.heightHint = 150;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new ArrayContentProvider());
		ColumnViewerToolTipSupport.enableFor(tableViewer);
//		tableViewer.getTable().addControlListener(new ControlAdapter() {
//	        public void controlResized(ControlEvent e) {
//	            SWTUtil.packAndFillLastColumn(tableViewer);
//	        }
//	    });
		
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
	                		Storage.getInstance().saveTagDefinitions(); // tag color is stored in tag definitions (*not* tag specifications!)
	                		tagSpec.setColor(Colors.toHex(rgb)); // store also to tagSpec for the DB
	                	}
	                };
	                colorCtrl.setEditorEnabled(isEditable);

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
					return;
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

		if (this.isEditable) {
			shortcutCol.setEditingSupport(new EditingSupport(tableViewer) {
				@Override
				protected void setValue(Object element, Object value) {
					logger.debug("setting value of: "+element+" to: "+value);
						CustomTagSpec cDef = (CustomTagSpec) element;
						if (cDef == null) {
							return;
						}
						
						logger.debug("setting value to: "+value);
						
						String shortCut = (String) value;
						if (shortCut==null || shortCut.isEmpty()) {
							cDef.setShortCut(null);
						} else {
							cDef.setShortCut(shortCut);
						}
						
						tableViewer.refresh(true);
						logger.debug("shorcut value changed - sending signal to storage!");
						Storage.getInstance().signalCustomTagSpecsChanged();
				}
				
				@Override
				protected Object getValue(Object element) {
					CustomTagSpec cDef = (CustomTagSpec) element;
					if (cDef == null) { // shouldn't happen I guess...
						return "";
					} else {
						return cDef.getShortCut()==null ? "" : cDef.getShortCut();
					}
				}
				
				@Override
				protected CellEditor getCellEditor(Object element) {
					TextCellEditor ce = new TextCellEditor(tableViewer.getTable());
					
					// add a "default" description text when no shortcut is set
					ce.getControl().addFocusListener(new FocusAdapter() {				
						@Override
						public void focusGained(FocusEvent e) {
							CustomTagSpec cDef = (CustomTagSpec) element;
							if (StringUtils.isEmpty(cDef.getShortCut())) {
								ce.setValue("0 - 9");
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
							if (!CustomTagSpec.isValidShortCut(str)) {
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

		if (false && this.isEditable) { // remove btn for table rows
			TableViewerColumn removeBtnCol = new TableViewerColumn(tableViewer, SWT.NONE);
			removeBtnCol.getColumn().setText("");
			removeBtnCol.getColumn().setResizable(false);
			removeBtnCol.getColumn().setWidth(50);
			
			class RemoveTagDefSelectionListener extends SelectionAdapter {
				CustomTagSpec tagDef;
				
				public RemoveTagDefSelectionListener(CustomTagSpec tagDef) {
					this.tagDef = tagDef;
				}
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					Storage.getInstance().removeCustomTagSpec(tagDef, false);
	 				tableViewer.refresh();
				}
			};
			
			CellLabelProvider removeButtonColLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
								
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button removeButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
			        removeButton.setImage(Images.getOrLoad("/icons/delete_12.png"));
			        removeButton.setToolTipText("Remove tag specification");
			        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
			        
			        CustomTagSpec tagDef = (CustomTagSpec) cell.getElement();	
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
		
		if (false && this.isEditable) { // move up/down btns
			class MoveTagDefUpDownSelectionListener extends SelectionAdapter {
				CustomTagSpec tagDef;
				boolean moveUp;
				
				public MoveTagDefUpDownSelectionListener(CustomTagSpec tagDef, boolean moveUp) {
					this.tagDef = tagDef;
					this.moveUp = moveUp;
				}
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					List<CustomTagSpec> cDefs = Storage.getInstance().getCustomTagSpecs();
					int i = cDefs.indexOf(tagDef);
					if (!moveUp && i>=1) {
						if (cDefs.remove(tagDef)) {
							cDefs.add(i-1, tagDef);
							Storage.getInstance().signalCustomTagSpecsChanged();
						}
					}
					else if (moveUp && i<cDefs.size()-1) {
						if (cDefs.remove(tagDef)) {
							cDefs.add(i+1, tagDef);
							Storage.getInstance().signalCustomTagSpecsChanged();
						}
					}
				}
			};	
			
			TableViewerColumn moveUpBtnCol = new TableViewerColumn(tableViewer, SWT.NONE);
			moveUpBtnCol.getColumn().setText("");
			moveUpBtnCol.getColumn().setResizable(false);
			moveUpBtnCol.getColumn().setWidth(50);
			
			CellLabelProvider moveUpBtnColLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
								
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button btn = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
			        btn.setImage(Images.getOrLoad("/icons/arrow_up.png"));
			        btn.setToolTipText("Move up");
			        btn.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
			        
			        CustomTagSpec tagDef = (CustomTagSpec) cell.getElement();	
			        btn.addSelectionListener(new MoveTagDefUpDownSelectionListener(tagDef, true));
			        Control c = btn;
			        
	                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					editor.minimumWidth = size.x;
					editor.horizontalAlignment = SWT.LEFT;
	                editor.setEditor(c , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(moveUpEditors, tagDef, editor);
				}
			};
			moveUpBtnCol.setLabelProvider(moveUpBtnColLabelProvider);

			TableViewerColumn moveDownBtnCol = new TableViewerColumn(tableViewer, SWT.NONE);
			moveDownBtnCol.getColumn().setText("");
			moveDownBtnCol.getColumn().setResizable(false);
			moveDownBtnCol.getColumn().setWidth(50);
						
			CellLabelProvider moveDownBtnLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
								
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button removeButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
			        removeButton.setImage(Images.getOrLoad("/icons/arrow_down.png"));
			        removeButton.setToolTipText("Move down");
			        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
			        
			        CustomTagSpec tagDef = (CustomTagSpec) cell.getElement();	
			        removeButton.addSelectionListener(new MoveTagDefUpDownSelectionListener(tagDef, false));
			        Control c = removeButton;
			        
	                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					editor.minimumWidth = size.x;
					editor.horizontalAlignment = SWT.LEFT;
	                editor.setEditor(c , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(moveDownEditors, tagDef, editor);
				}
			};
			moveDownBtnCol.setLabelProvider(moveDownBtnLabelProvider);
		}
		
		if (!this.isEditable) { // add an "add tag button" to add the tag to the current position in the transcription widget 
			TableViewerColumn addButtonCol = new TableViewerColumn(tableViewer, SWT.NONE);
			addButtonCol.getColumn().setText("");
			addButtonCol.getColumn().setResizable(false);
			addButtonCol.getColumn().setWidth(100);
			
			CellLabelProvider addButtonColLabelProvider = new CellLabelProvider() {
				@Override public void update(final ViewerCell cell) {
					CustomTagSpec tagSpec = (CustomTagSpec) cell.getElement();
					final TableItem item = (TableItem) cell.getItem();
					TableEditor editor = new TableEditor(item.getParent());
					
					Button addBtn = new Button((Composite) cell.getViewerRow().getControl(), 0);
					addBtn.setImage(Images.ADD_12);
					addBtn.setToolTipText("Insert this tag at the selected position in the transcription");
					SWTUtil.onSelectionEvent(addBtn, e -> {
						listener.stream().forEach(l -> {
							l.addTagButtonClicked(tagSpec);
						});
					});
					                
	                Point size = addBtn.computeSize(SWT.DEFAULT, SWT.DEFAULT);
	                
					editor.minimumWidth = size.x;
					editor.horizontalAlignment = SWT.LEFT;
					
	                editor.setEditor(addBtn , item, cell.getColumnIndex());
	                editor.layout();
	                
	                TaggingWidgetUtils.replaceEditor(insertTagEditors, tagSpec, editor);
				}
			};
			addButtonCol.setLabelProvider(addButtonColLabelProvider);
		} // end add button column
		
		tableViewer.refresh(true);
		tableViewer.getTable().pack();
		
		if (this.isEditable) {
			Composite btnsComp = new Composite(tableContainer, 0);
			btnsComp.setLayout(new RowLayout(SWT.VERTICAL));
			btnsComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
			
			Button removeBtn = new Button(btnsComp, 0);
			removeBtn.setImage(Images.DELETE);
			removeBtn.setToolTipText("Remove selected tag from list");
			SWTUtil.onSelectionEvent(removeBtn, e -> {
				removeSelected();
			});
			
			Button moveUpBtn = new Button(btnsComp, 0);
			moveUpBtn.setImage(Images.getOrLoad("/icons/arrow_up.png"));
			moveUpBtn.setToolTipText("Move up selected");
			SWTUtil.onSelectionEvent(moveUpBtn, e -> {
				moveSelected(true);
			});			
			
			Button moveDownBtn = new Button(btnsComp, 0);
			moveDownBtn.setImage(Images.getOrLoad("/icons/arrow_down.png"));
			moveDownBtn.setToolTipText("Move down selected");
			SWTUtil.onSelectionEvent(moveDownBtn, e -> {
				moveSelected(false);
			});			
		}		

		header.layout(true);
		
		updateAvailableTagSpecs();

		IStorageListener storageListener = new IStorageListener() {
			public void handlTagSpecsChangedEvent(TagSpecsChangedEvent e) {
				updateAvailableTagSpecs();
			}
		};
		Storage.getInstance().addListener(storageListener);
		
		Observer customTagFactoryObserver = new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				if (arg instanceof TagRegistryChangeEvent) {
					logger.debug("TagRegistryChangeEvent: "+arg);
					updateAvailableTagSpecs();
				}				
			}
		};
		CustomTagFactory.addObserver(customTagFactoryObserver);
		
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(TrpSettings.SHOW_ALL_TAGS_IN_TAG_EDITOR_PROPERTY)) {
					updateAvailableTagSpecs();
				}
			}
		};
		TrpConfig.getTrpSettings().addPropertyChangeListener(propertyChangeListener);
		
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				CustomTagFactory.deleteObserver(customTagFactoryObserver);
				TrpConfig.getTrpSettings().removePropertyChangeListener(propertyChangeListener);
				Storage.getInstance().removeListener(storageListener);
			}
		});
	}
	
	private void removeSelected() {
		CustomTagSpec cDef = getSelected();
		if (cDef != null) {
			Storage.getInstance().removeCustomTagSpec(cDef, false);
		}
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
		logger.info("updating available tag specs: "+Storage.getInstance().getCustomTagSpecs());
		Display.getDefault().asyncExec(() -> {
			if (SWTUtil.isDisposed(tableViewer.getTable()) || SWTUtil.isDisposed(this)) {
				return;
			}
			
			boolean showAllTags = TrpConfig.getTrpSettings().isShowAllTagsInTagEditor();
			if (!isEditable) {
				showAllTagsBtn.setSelection(showAllTags);	
			}
			
			if (isEditable || !showAllTags) {
				headerLbl.setText("Tag Specifications");
				tableViewer.setInput(Storage.getInstance().getCustomTagSpecs());
			}
			else {
				headerLbl.setText("All Tags");
				List<CustomTagSpec> allTagsSpecs = new ArrayList<>();
				for (CustomTag t : CustomTagFactory.getRegisteredTagObjectsSortedByName(true)) {
					if (!t.showInTagWidget() || t.getTagName().equals(TextStyleTag.TAG_NAME)) { // exclude TextStyle tag
						continue;
					}
					
					CustomTag tc = t.copy(); // to be sure not to overwrite existing objects
					// clear attributes from the list of all tags
//					tc.setOffset(-1);
//					tc.setLength(-1);
//					tc.setContinued(false);
//					for (String an : tc.getAttributeNames()) {
//						try {
//							tc.setAttribute(an, null, false);
//						} catch (IOException e) {
//							// silently ignore exceptions
//						}
//					}
					
					CustomTagSpec ts = new CustomTagSpec(tc);
					allTagsSpecs.add(ts);
				}
				tableViewer.setInput(allTagsSpecs);
			}
			
			Collection<CustomTagSpec> tagSpecs = (Collection<CustomTagSpec>) tableViewer.getInput();
			TaggingWidgetUtils.updateEditors(colorEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(removeTagDefEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(insertTagEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(moveUpEditors, tagSpecs);
			TaggingWidgetUtils.updateEditors(moveDownEditors, tagSpecs);
			
			tableViewer.refresh(true);
		});
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}

	public CustomTagSpec getSelected() {
		return (CustomTagSpec) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
	}
	
	public Composite getHeader() {
		return header;
	}
	
	public void addListener(TagSpecsWidgetListener l) {
		listener.add(l);
	}
	
	public boolean removeListener(TagSpecsWidgetListener l) {
		return listener.remove(l);
	}

}
