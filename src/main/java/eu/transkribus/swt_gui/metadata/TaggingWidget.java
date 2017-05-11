package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagAttribute;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.customtags.TextStyleTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory.TagRegistryChangeEvent;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.CustomTagPropertyTable;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasKeys;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TaggingWidget extends Composite implements Observer {
	private final static Logger logger = LoggerFactory.getLogger(TaggingWidget.class);
		
	public static final boolean USE_SIMPLE_ATTRIBUTES = true;
	
	private Set<String> availableTagNames = new TreeSet<>();
	
	CheckboxTableViewer tagsTableViewer;
	Table tagsTable;
	
	TableViewer selectedTagsTableViewer;
	
	Composite tagsTableContainer, btnsContainer;
	ExpandableComposite tagsTableExp;
	
	Label tagsInSelectionLabel;
	
	Button addTagBtn, deleteTagBtn, addSelectedTagBtn, removeSelectedTagBtn;
	Button clearTagsBtn;
	
	Composite propertyTableContainer;
	ExpandableComposite propsExp, tagExp;
	Button addAtrributeBtn;
	
//	Button searchTagsBtn;
	
	List<CustomTag> selectedTags = new ArrayList<>();
		
	Map<String, ControlEditor> addDelEditors = new HashMap<>();
	Map<String, ControlEditor> colorEditors = new HashMap<>();
	
//	Map<String, ControlEditor> delSelectedEditors = new HashMap<>();
	Map<CustomTag, ControlEditor> delSelectedEditors = new HashMap<>();
	
	ISelectionChangedListener tagsTableSelectionListener;
	
	List<ITaggingWidgetListener> listener = new ArrayList<>();
	ExpansionAdapter expansionListener;
	
	CustomTagPropertyTable ctPropTable;
	
	boolean withProperties;
	
	SashForm sf;
	
	static int colorIndex = 1; 
	public static final Map<String, Color> TAG_COLOR_REGISTRY = new HashMap<>();
	static {
		TAG_COLOR_REGISTRY.put(TextStyleTag.TAG_NAME, Colors.getSystemColor(SWT.COLOR_BLACK));
	}
	
	/**
	 * 
	 * @param parent
	 * @param style
	 * @param type 0 = default composite, 1 = group, 2 = expandable
	 * @param withProperties
	 */
	public TaggingWidget(Composite parent, int style, int type, boolean withProperties) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		Assert.assertTrue("type of widget container must be either 0, 1 or 2!", type>=0 && type<3);
		this.withProperties = withProperties;
		
		sf = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH );
//		sf.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		sf.setBackground(sf.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		
		initTagsTable(type);
				
		if (withProperties)
			initPropertyTableExpandable();
		
		tagsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updatePropertiesForSelectedTag();
			}
		});
		
		updatePropertiesForSelectedTag();
		
		if (sf instanceof SashForm && withProperties) {
//			logger.debug("l = "+sf.getWeights().length);
//			sf.setWeights(new int[] {65, 35});
			sf.setWeights(new int[] {50, 15, 35});
		}
		
		CustomTagFactory.registryObserver.addObserver(this);
	}
	
	private void initTagsTable(int type) {
		tagsTableContainer = null;
		if (type == 0) {
			tagsTableContainer = new Composite(sf, SWT.NONE);
		}
		else if (type == 2) {
			tagsTableExp = new ExpandableComposite(sf, ExpandableComposite.COMPACT);
			tagsTableExp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			tagsTableExp.setExpanded(true);
			tagsTableExp.setText("Tags");
			tagsTableExp.setToolTipText("The list of available tags - predefined tags are displayed in italic font");
			
			tagsTableContainer = new Composite(tagsTableExp, 0);
			tagsTableContainer.setLayout(new GridLayout(1, false));
		} 
		else if (type == 1) {
			Group grp = new Group(sf, SWT.NONE);
			grp.setText("Tags");
//			final int nCols = 1;
//			GridLayout gl = new GridLayout(nCols, false);
//			gl.marginHeight = 10;
//			gl.marginWidth = 5;
//			grp.setLayout(gl);
			
			tagsTableContainer = grp;
		}
		
		tagsTableContainer.setLayout(new GridLayout(1, false));
		tagsTableContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		getDisplay().addFilter(SWT.KeyDown, new Listener() {
			@Override public void handleEvent(Event event) {
				if (event.type == SWT.KeyDown) {
					if (CanvasKeys.isKeyDown(event.stateMask, SWT.ALT) && event.keyCode == 'c') {
						for (ITaggingWidgetListener l : listener) {
							l.addTagsForSelection(getCheckedTags());
						}
					}
				}
			}
		});
		
		btnsContainer = new Composite(tagsTableContainer, 0);
		btnsContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		btnsContainer.setLayout(new GridLayout(4, false));

		addTagBtn = new Button(btnsContainer, SWT.PUSH);
		addTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		addTagBtn.setText("Add tag...");
		addTagBtn.setImage(Images.ADD);
		addTagBtn.setToolTipText("Defines a new tag");
		
		addTagBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				CreateTagNameDialog d = new CreateTagNameDialog(getShell(), "Specify new tag name");				
				if (d.open() == Window.OK) {
					String name = d.getName();
					for (ITaggingWidgetListener l : listener) {
						l.createNewTag(name);
					}
				}
			}
		});
		
		deleteTagBtn = new Button(btnsContainer, SWT.PUSH);
		deleteTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		deleteTagBtn.setText("Delete tag");
		deleteTagBtn.setImage(Images.DELETE);
		deleteTagBtn.setToolTipText("Deletes the selected tag from the list of available tags");
		deleteTagBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				deleteSelectedTagFromList();
			}
		});
		
//		searchTagsBtn = new Button(btnsContainer, SWT.PUSH);
//		searchTagsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		searchTagsBtn.setText("Find tags...");
//		searchTagsBtn.setImage(Images.FIND);
//		searchTagsBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				TagSearchDialog d = new TagSearchDialog(getShell());
//				int rc = d.open();
//				logger.debug("rc = "+rc);
//			}
//		});
		
//		searchTextBtn = new Button(btnsContainer, SWT.PUSH);
//		searchTextBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		searchTextBtn.setText("Find text...");
//		searchTextBtn.setImage(Images.FIND);
//		searchTextBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				TagSearchDialog d = new TagSearchDialog(getShell());
//				int rc = d.open();
//				logger.debug("rc = "+rc);
//			}
//		});
		
		Button saveBtn = new Button(btnsContainer, SWT.PUSH);
		saveBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		saveBtn.setImage(Images.DISK);
		saveBtn.setToolTipText("Save the tag definitions to the local config.properties file s.t. they are recovered next time around\n(Note: predefined tags are only stored, if their attributes were extended!)");
		saveBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				String tagNamesProp = CustomTagFactory.constructTagDefPropertyForConfigFile();
				logger.debug("storing tagNamesProp: "+tagNamesProp);
				
				TrpConfig.getTrpSettings().setTagNames(tagNamesProp);
//				TrpConfig.save();
			}
		});
				
//		if (false) {
//		addSelectedTagBtn = new Button(btnsContainer, SWT.PUSH);
//		addSelectedTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		addSelectedTagBtn.setImage(Images.ADD);
//		addSelectedTagBtn.addSelectionListener(new AddTagSelectionListener(listener, this, tagName));
//		addSelectedTagBtn.addSelectionListener(new TagActionSelectionListener(this, listener, TaggingActionType.ADD_TAG));
//		addSelectedTagBtn.setToolTipText("Adds the selected tag to the selection in the transcription widget");
//		
//		removeSelectedTagBtn = new Button(tagsTableContainer, SWT.PUSH);
//		removeSelectedTagBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		removeSelectedTagBtn.setImage(Images.DELETE);
//		removeSelectedTagBtn.addSelectionListener(new TagActionSelectionListener(this, listener, TaggingActionType.DELETE_TAG));
//		removeSelectedTagBtn.setToolTipText("Removes the selected tag from the current selection in the transcription widget");
//		}
		
		if (false) {
		tagsInSelectionLabel = new Label(btnsContainer, SWT.NONE);
		tagsInSelectionLabel.setText("Tags in selection: ");
		tagsInSelectionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1));
		}
		
		int tableViewerStyle = SWT.NO_FOCUS | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		tagsTableViewer = CheckboxTableViewer.newCheckList(tagsTableContainer, tableViewerStyle);
		tagsTableViewer.getTable().setToolTipText("List of tags - predefined tags are italic, checked tags are added via the key combination ALT+C in the transcription editor");
		
//		tagsTableViewer = new TableViewer(taggingGroup, SWT.FULL_SELECTION|SWT.HIDE_SELECTION|SWT.NO_FOCUS | SWT.H_SCROLL
//		        | SWT.V_SCROLL | SWT.FULL_SELECTION /*| SWT.BORDER*/);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gd.heightHint = 150;
//		tagsTableViewer.getControl().setLayoutData(gd);
		tagsTable = tagsTableViewer.getTable();
		tagsTable.setLayoutData(gd);
		
		tagsTableViewer.setContentProvider(new ArrayContentProvider());
		tagsTable.setHeaderVisible(false);
		tagsTable.setLinesVisible(true);
		tagsTableSelectionListener = new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				updateButtonVisibility();
			}
		};
//		tagsTableViewer.addSelectionChangedListener(tagsTableSelectionListener);
		
		TableViewerColumn nameCol = new TableViewerColumn(tagsTableViewer, SWT.NONE);
		nameCol.getColumn().setText("Name");
		nameCol.getColumn().setResizable(true);
		nameCol.getColumn().setWidth(150);
		ColumnLabelProvider nameColLP = new ColumnLabelProvider() {
			@Override public String getText(Object element) {
				return (String) element;
			}
			@Override public Font getFont(Object element) {
				CustomTag t = CustomTagFactory.getTagObjectFromRegistry((String)element);
				if (t != null && !t.isDeleteable()) {
					return Fonts.createItalicFont(tagsTable.getFont());
				}
				
				return null;
			}
		};
		nameCol.setLabelProvider(nameColLP);

		TableViewerColumn colorCol = new TableViewerColumn(tagsTableViewer, SWT.NONE);
		colorCol.getColumn().setText("Color");
		colorCol.getColumn().setResizable(true);
		colorCol.getColumn().setWidth(50);
		colorCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {				
				final TableItem item = (TableItem) cell.getItem();
				final String tagName = (String) cell.getElement();
				
				TableEditor editor = new TableEditor(item.getParent());				
                editor.grabHorizontal  = true;
                editor.grabVertical = true;
                editor.horizontalAlignment = SWT.LEFT;
                editor.verticalAlignment = SWT.TOP;
                
                Button colorCtrl = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
                colorCtrl.addPaintListener(new PaintListener() {
        			@Override
        			public void paintControl(PaintEvent e) {
        				e.gc.setForeground(TaggingWidget.getTagColor(tagName));
        				e.gc.setBackground(TaggingWidget.getTagColor(tagName));
        				int o = 0;
        				e.gc.fillRectangle(e.x+o, e.y+o, e.width-2*o, e.height-2*o);
        			}
        		});
                
                colorCtrl.addSelectionListener(new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent e) {
						ColorDialog dlg = new ColorDialog(getShell());
						Color c = TaggingWidget.getTagColor(tagName);
						dlg.setRGB(c.getRGB());
						dlg.setText("Choose new tag color!");
						
						RGB rgb = dlg.open();
						if (rgb != null) {
							String colorStr = Colors.toHex(rgb.red, rgb.green, rgb.blue);
							logger.debug("setting new color: "+colorStr+" for tag: "+tagName);
							CustomTagFactory.setTagColor(tagName, colorStr);
							tagsTableViewer.refresh();
							if (TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget()!=null) {
								TrpMainWidget.getInstance().getUi().getSelectedTranscriptionWidget().redrawText(true);
							}
					   }
					}
				});
                
//                Label colorCtrl = new Label((Composite) cell.getViewerRow().getControl(), SWT.NONE);
////                int ri = getRowIndex(cell);
//                colorCtrl.setBackground(TaggingWidget.getTagColor(tagName));
//                colorLabel.setBackground(TaggingWidgetUtils.getColorForIndex(ri));
                
                editor.setEditor(colorCtrl , item, cell.getColumnIndex());
                editor.layout();
                
                TaggingWidgetUtils.replaceEditor(colorEditors, tagName, editor);
			}
		});
		
		if (true) {
		TableViewerColumn addButtonCol = new TableViewerColumn(tagsTableViewer, SWT.NONE);
		addButtonCol.getColumn().setText("");
		addButtonCol.getColumn().setResizable(false);
		addButtonCol.getColumn().setWidth(100);
		
		CellLabelProvider addButtonColLabelProvider = new CellLabelProvider() {
			@Override public void update(final ViewerCell cell) {
				String tagName = (String) cell.getElement();
				boolean isSelected = isTagSelected(tagName);
				logger.trace("tagName: "+tagName+" isSelected: "+isSelected);
				
				final TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				
				boolean createDelBtn = false;
				TagAddRemoveComposite c = new TagAddRemoveComposite((Composite) cell.getViewerRow().getControl(), SWT.NONE, true, createDelBtn);
				
				if (c.getAddButton() != null)
					c.getAddButton().addSelectionListener(new AddTagSelectionListener(listener, TaggingWidget.this, tagName));
//				if (c.getRemoveButton() != null)
//					c.getRemoveButton().addSelectionListener(new TagActionSelectionListener(TaggingWidget.this, listener, TaggingActionType.DELETE_TAG, tagName));
				c.pack();
				                
                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                
				editor.minimumWidth = size.x;
				editor.horizontalAlignment = SWT.LEFT;
                
//                editor.grabHorizontal  = true;
//                editor.grabVertical = true;
//                editor.horizontalAlignment = SWT.LEFT;
//                editor.verticalAlignment = SWT.TOP;
//                editor.minimumWidth = size.x;
//                editor.minimumHeight = size.y;
                                
                editor.setEditor(c , item, cell.getColumnIndex());
                editor.layout();
                
                TaggingWidgetUtils.replaceEditor(addDelEditors, tagName, editor);
			}
		};
		addButtonCol.setLabelProvider(addButtonColLabelProvider);
		}
		
		tagsTableViewer.refresh(true);
		tagsTableViewer.getTable().pack();
		
		updateButtonVisibility();
		
		initSelectedTagsTable();
		
		if (type == 2) {
			tagsTableExp.setClient(tagsTableContainer);
			tagsTableExp.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					tagsTableContainer.layout(true);
					sf.layout(true);
					ctPropTable.layout(true);
//					sf.setWeights(new int[] {tagsTableContainer.getSize().y, propertyTableContainer.getSize().y});
					layout(true);
					
				}
			});
			tagsTableExp.setExpanded(true);
		}
		
		tagsTableContainer.layout(true);
		
//		for (Object p : tagsTableViewer.getColumnProperties()) {
//			logger.debug("property = "+p);
//		}
		
		updateAvailableTags();
	}
	
	private void initSelectedTagsTable() {
		Composite container = new Composite(sf, 0);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		selectedTagsTableViewer = new TableViewer(tagsTableContainer);
		selectedTagsTableViewer = new TableViewer(container);
		selectedTagsTableViewer.setContentProvider(new ArrayContentProvider());
		final Table t = selectedTagsTableViewer.getTable();
		t.setHeaderVisible(true);
		
//		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
//		gd.heightHint = 75;
//		t.setLayoutData(gd);
		t.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tagCol = TableViewerUtils.createTableViewerColumn(selectedTagsTableViewer, SWT.LEFT, "Tags under cursor", 200);
		TableViewerColumn deleteTagCol = TableViewerUtils.createTableViewerColumn(selectedTagsTableViewer, SWT.LEFT, "", 50);
		
		tagCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				final CustomTag tag = (CustomTag) cell.getElement();
				cell.setText(tag.getCssStr());
			}
		});
		        
		deleteTagCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				final CustomTag tag = (CustomTag) cell.getElement();
//				String tagName = tag.getTagName();
				
				final TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				TagAddRemoveComposite c = new TagAddRemoveComposite((Composite) cell.getViewerRow().getControl(), SWT.NONE, false, true);
				c.getRemoveButton().setToolTipText("Remove this tag");
//				c.getRemoveButton().addSelectionListener(new TagActionSelectionListener(TaggingWidget.this, listener, TaggingActionType.DELETE_TAG, tagName));
				c.getRemoveButton().addSelectionListener(new DeleteTagSelectionListener(listener, tag));
				c.pack();
				   
                Point size = c.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                
				editor.minimumWidth = size.x;
				editor.horizontalAlignment = SWT.LEFT;
                editor.setEditor(c , item, cell.getColumnIndex());
                editor.layout();
                
//                TaggingWidgetUtils.replaceEditor(delSelectedEditors, tagName, editor);
                TaggingWidgetUtils.replaceEditor(delSelectedEditors, tag, editor);
			}
		});
		
		selectedTagsTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				String selectedTagName = sel.isEmpty() ? null : ((CustomTag) sel.getFirstElement()).getTagName();
				selectTagname(selectedTagName);				
			}
		});
		
		clearTagsBtn = new Button(container, SWT.PUSH);
		clearTagsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		clearTagsBtn.setImage(Images.DELETE);
		clearTagsBtn.setText("Clear tags for selection");
		clearTagsBtn.setToolTipText("Clears all tags from the current selection in the transcription widget");
//		clearTagsBtn.addSelectionListener(new TagActionSelectionListener(this, listener, TaggingActionType.CLEAR_TAGS));
		clearTagsBtn.addSelectionListener(new ClearTagsSelectionListener(listener));
	}
	
	boolean isTagSelected(String tagName) {
		return getSelectedTag(tagName) != null;
	}
	
	CustomTag getSelectedTag(String tagName) {
		for (CustomTag t : selectedTags) {
			if (t.getTagName().equals(tagName))
				return t;
		}
		return null;
	}
	
	public void addListener(ITaggingWidgetListener listener) {
		if (!this.listener.contains(listener))
			this.listener.add(listener);
	}
	
	public void removeListener(ITaggingWidgetListener listener) {
		this.listener.remove(listener);
	}
		
	private void updateEditors() {
		TaggingWidgetUtils.updateEditors(colorEditors, availableTagNames);
		TaggingWidgetUtils.updateEditors(addDelEditors, availableTagNames);
//		TaggingWidgetUtils.updateEditors(delSelectedEditors, getSelectedTagNames());
		TaggingWidgetUtils.updateEditors(delSelectedEditors, selectedTags);
	}
		
	public void updateAvailableTags() {
		logger.debug("UPDATING TAGS, THREAD = "+Thread.currentThread().getName());
		
		availableTagNames.clear();
		for (CustomTag t : CustomTagFactory.getRegisteredTagObjects()) {
			logger.trace("update of av. tags, tn = "+t.getTagName()+" showInTagWidget: "+t.showInTagWidget());
			if (t.showInTagWidget()) {
				availableTagNames.add(t.getTagName());
			}
		}
		
		Display.getDefault().asyncExec(new Runnable() {
		@Override public void run() {
//			updateAvailableTags();
			tagsTableViewer.setInput(availableTagNames);
			updateTable();
			}
		});
		
//		// init predifined tag-names:
//		String tagNamesProp = TrpConfig.getTrpSettings().getTagNames();
//		logger.debug("tagNames = "+tagNamesProp);
//
//		Matcher wordMatcher = Pattern.compile("(\\w|_|-)+").matcher(tagNamesProp);
//		while (wordMatcher.find()) {
//			String tn = tagNamesProp.substring(wordMatcher.start(), wordMatcher.end());
//			logger.debug("adding tag: '"+tn+"'");
//			try {
//				addAvailableTag(tn, true);
//			} catch (IOException e1) {
//				logger.warn(e1.getMessage());
//			}
//		}
	}
	
	private void deleteSelectedTagFromList() {
		// FIXME:
		if (false) return;
				
		if (tagsTable.getSelectionIndex()!=-1) {
			String tn = (String) tagsTable.getSelection()[0].getData();
			logger.debug("selected tag = "+tn);
			
//			logger.info("selection index = "+tagsTable.getSelectionIndex());
//			String tn = (String) availableTagNames.toArray()[tagsTable.getSelectionIndex()];
		
			for (ITaggingWidgetListener l : listener) {
				l.removeTagDefinition(tn);
			}
		}
	}
	
	public void updateButtonVisibility() {
		if (removeSelectedTagBtn!=null)
			removeSelectedTagBtn.setEnabled(isTagSelected(getTagNameSelectedInTable()));
		
		logger.trace("updating buttons: "+addDelEditors.size());
		for (String tagName : addDelEditors.keySet()) {
			ControlEditor e = addDelEditors.get(tagName);
			if (e.getEditor() == null || e.getEditor().isDisposed())
				continue;
			
			TagAddRemoveComposite c = (TagAddRemoveComposite) e.getEditor();
			SWTUtil.setEnabled(c.getRemoveButton(), isTagSelected(tagName));
		}
		
//		for (String k : removeBtns.keySet()) {
//			SWTUtil.setEnabled(removeBtns.get(k), selectedTags.contains(k));
////			removeBtns.get(k).setEnabled(selectedTags.contains(k));
//		}
	}
	
//	public void updateTagsUnderCaretOffset() {
//		// FIXME: GTK open/close bug................................... only KDE???
//		
//		for (MenuItem mi : tagsUnderCursorMenu.getItems()) {
//			mi.dispose();
//		}
//		logger.debug("nr of items in menu = "+tagsUnderCursorMenu.getItemCount());
//		
//		TrpMainWidget mw = TrpMainWidget.getInstance();
//		ATranscriptionWidget aw = mw.getUi().getSelectedTranscriptionWidget();
//		if (aw == null)
//			return;
//		
//		int offset = aw.getText().getCaretOffset();
//				
//		final Pair<ITrpShapeType, Integer> shapeAndRelativePositionAtOffset = aw.getTranscriptionUnitAndRelativePositionFromOffset(offset);
//		if (shapeAndRelativePositionAtOffset==null)
//			return;
//		
//		final ITrpShapeType shape = shapeAndRelativePositionAtOffset.getLeft();
//		final CustomTagList ctl = shape.getCustomTagList();
//
//		List<CustomTag> tags = aw.getCustomTagsForOffset(offset);
//		logger.debug("nr. of custom tags = " + tags.size());
//		
//		int i=0;
//		for (CustomTag t : tags) {
//			if (t.getTagName().equals(TextStyleTag.TAG_NAME))
//				continue;
//			
//			MenuItem ti = new MenuItem(tagsUnderCursorMenu, SWT.PUSH);
//			
////			MenuItem ti;
////			if (i >= tagsUnderCursorMenu.getItemCount())
////				ti = new MenuItem(tagsUnderCursorMenu, SWT.PUSH);
////			else
////				ti = tagsUnderCursorMenu.getItems()[i];
//			
//			ti.setText(t.getTagName());
//			ti.setImage(Images.DELETE);
//			
////			MenuItem ti = tagsToolItem.addItem(t.getTagName(), Images.DELETE, "");
//			ti.setData(t);
//			ti.addSelectionListener(new TagRemoveSelectionListener(aw, ctl, t));
//			
//			++i;
//		}
//		
//		tagsUnderCursorButton.pack();
//		
//		
////		for (int j=i; j<tagsUnderCursorMenu.getItemCount(); ++j) {
////			tagsUnderCursorMenu.getItems()[j].setMenu(SWTUtil.dummyMenu);
////		}
//		
//		
//	}
	
	public static Color getTagColor(String tagName) {
		String tagColorStr = CustomTagFactory.getTagColor(tagName);
		Color c = Colors.decode2(tagColorStr);

		if (c == null) {
			c = Colors.getSystemColor(SWT.COLOR_GRAY); // default tag color
		}
		
		return c;
		
		// OLD:
//		CustomTag t = CustomTagFactory.getTagObjectFromRegistry(tagName);
//		if (t== null) { // should not happen...
//			
//		}
//		
//		
//		Color c = TAG_COLOR_REGISTRY.get(tagName);
//		if (c == null) {
//			c = TaggingWidgetUtils.getColorForIndex(colorIndex++);
//			logger.debug("created color for tag, colorIndex = "+colorIndex+", color = "+c);
//			TAG_COLOR_REGISTRY.put(tagName, c);
//		}
//		return c;
	}
	
	public String getTagNameSelectedInTable() {
		if (tagsTableViewer.getSelection().isEmpty())
			return null;
		else
			return (String) ((IStructuredSelection) tagsTableViewer.getSelection()).getFirstElement();		
	}
	
	public Map<String, Object> getCurrentAttributes() {
		Map<String, Object> props = new HashMap<>();
		CustomTag pt = ctPropTable.getPrototypeTag();
		if (pt != null) {
			return pt.getAttributeNamesValuesMap();
		}
	
		return props;
	}
	
	public boolean hasTag(String tagName) {
		Collection<String> tags = (Collection<String>) tagsTableViewer.getInput();
		for (String t : tags) {
			if (t.equals(tagName))
				return true;
		}
		return false;
	}
	
//	public void addAvailableTags(Collection<String> tagNames) {
	
//	public void addAvailableTag(String tagName, boolean checkIfRegistered) throws IOException {
//		if (checkIfRegistered && CustomTagFactory.getRegisteredTagNames().contains(tagName))
//			throw new IOException("'"+tagName+"' cannot be added as a custom tag because it is a predefined tag!");
//		
//		availableTagNames.add(tagName);
//		tagsTableViewer.setInput(availableTagNames);
//		
//		updateTable();
//	}
	
//	public void removeAvailableTag(String tag) {
//		availableTagNames.remove(tag);
//		tagsTableViewer.setInput(availableTagNames);
//		
//		updateTable();
//	}
	
	private void updateTable() {
		updateEditors();
		tagsTableViewer.refresh(true);
//		tagsTableViewer.getTable().pack();
		
		tagsTableContainer.layout(true);
//		taggingGroup.layout(true);
	}
	
	public void setSelectedTags(List<CustomTag> selectedTags2) {
//		if (true) return; // TEST
		
		selectedTags.clear();
		
		if (selectedTags2 != null) {
			for (CustomTag t : selectedTags2) {
				if (t.showInTagWidget())
					selectedTags.add(t);
			}
		}		
		logger.debug("n-selected tags: "+selectedTags.size());

		selectedTagsTableViewer.setInput(selectedTags);
//		TaggingWidgetUtils.updateEditors(delSelectedEditors, getSelectedTagNames());
		TaggingWidgetUtils.updateEditors(delSelectedEditors, selectedTags);
				
		selectFirstSelectedTag();
		updateButtonVisibility();
	}
	
	private List<String> getSelectedTagNames() {
		List<String> tn = new ArrayList<>();
		for (CustomTag t : selectedTags) {
			tn.add(t.getTagName());
		}
		return tn;
	}
	
	private void selectTagname(String name) {
		tagsTableViewer.setSelection(name == null ? null : new StructuredSelection(name), true);
		updatePropertiesForSelectedTag();
	}
	
	private void selectFirstSelectedTag() {
		if (!selectedTags.isEmpty()) {
			logger.trace("selecting tag name: "+selectedTags.get(0));
			selectTagname(selectedTags.get(0).getTagName());
		} else
			selectTagname(null);
	}
	
//	private void selectTagWithName(String tagName) {
//		int i=0;
//		for (String t : availableTagNames) {
//			if (tagName.equals(t)) {
//				tagsTable.select(i);
//				return;
//			}
//			++i;
//		}
//	}
	
	public int getRowIndex(ViewerCell cell) {
		if (cell==null)
			return -1;
		
	    TableItem ti = (TableItem) cell.getItem();
	    for (int i = 0; i < tagsTable.getItemCount(); i++) {
	    	if (tagsTable.getItem(i).equals(ti))
	            return i;
	    }
	    return -1;
	}

	public List<String> getCheckedTags() {
		List<String> checkedTags = new ArrayList<>();
		for (Object o : tagsTableViewer.getCheckedElements()) {
			checkedTags.add((String) o);
		}
		return checkedTags;
	}
	
	private void updatePropertiesBtns() {
		if (!withProperties)
			return;
		
		logger.debug("tags table selection empty: "+tagsTableViewer.getSelection().isEmpty()+" addAtrributeBtn: "+addAtrributeBtn);
		
		addAtrributeBtn.setEnabled(!tagsTableViewer.getSelection().isEmpty());
	}
	
	public void updatePropertiesForSelectedTag() {
		if (!withProperties)
			return;
		
//		if (false) return;
		
		updatePropertiesBtns();
		
		IStructuredSelection sel = (IStructuredSelection) tagsTableViewer.getSelection();
		String tn = (String) sel.getFirstElement();
		logger.debug("selected tag name: "+tn);
		if (tn == null) {
			propsExp.setText("Properties");
			ctPropTable.setInput(null, null);
			ctPropTable.update();
			return;
		}
		
		propsExp.setText("Properties of '"+tn+"' tag");
		
		// THE NEW SHIT:
		try {
			CustomTag tag = CustomTagFactory.getTagObjectFromRegistry(tn);
			if (tag == null)
				throw new Exception("could not retrieve tag from registry: "+tn+" - should not happen here!");
			
			logger.debug("tag from object registry: "+tag);
			logger.debug("tag atts: "+tag.getAttributeNames());
			
			CustomTag protoTag = tag.copy();
			logger.debug("protoTag copy: "+protoTag);
			logger.debug("protoTag atts: "+protoTag.getAttributeNames());
			
			CustomTag selTag = getSelectedTag(tn);
			logger.debug("selTag: "+selTag);

			if (selTag != null) {
				protoTag.setAttributes(selTag, true);
			}
			
			ctPropTable.setInput(protoTag, selTag);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}
	
	private void initPropertyTableExpandable() {
//		Sash sash = new Sash(sf, SWT.VERTICAL);
		
		propsExp = new ExpandableComposite(sf, ExpandableComposite.COMPACT);
		propsExp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		propsExp.setExpanded(true);
		propsExp.setText("Properties");
		propsExp.setToolTipText("The list of properties for the selected tag - predefined properties are italic");
		
		propertyTableContainer = new Composite(propsExp, 0);
		propertyTableContainer.setLayout(new GridLayout(2, false));
		
		addAtrributeBtn = new Button(propertyTableContainer, SWT.PUSH);
		addAtrributeBtn.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		addAtrributeBtn.setText("Add attribute...");
		addAtrributeBtn.setImage(Images.ADD);
		addAtrributeBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final String tn = getTagNameSelectedInTable();
				if (tn == null)
					return;
				
				CreateTagNameDialog d = new CreateTagNameDialog(getShell(), "Specify attribute for new tag: "+tn+"");				
				if (d.open() == Window.OK) {
					String name = d.getName();
					CustomTagAttribute att = new CustomTagAttribute(name);
					for (ITaggingWidgetListener l : listener) {
						l.addAttributeOnCustomTag(tn, att);
					}
				}
			}
			
		});
		
		Button deleteAttributeButton = new Button(propertyTableContainer, SWT.PUSH);
		deleteAttributeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		deleteAttributeButton.setText("Delete selected attribute");
		deleteAttributeButton.setImage(Images.DELETE);
		deleteAttributeButton.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (tagsTableViewer.getSelection().isEmpty())
					return;
				
				String tn = getTagNameSelectedInTable();
				CustomTagAttribute selectedProperty = getSelectedProperty();
				logger.debug("selected property: "+selectedProperty);

				if (tn != null && selectedProperty != null) {
					for (ITaggingWidgetListener l : listener) {
						l.deleteAttributeOnCustomTag(tn, selectedProperty.getName());
					}
				}
			}
			
		});
		
		initCustomPropertyTable();
		
//		initPropertyTable();
		
		propsExp.setClient(propertyTableContainer);
		propsExp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				propertyTableContainer.layout();
				sf.layout();
			}
		});
		propsExp.setExpanded(propsExp.isExpanded());
		
		layout();
	}
	
	private void initCustomPropertyTable() {
		ctPropTable = new CustomTagPropertyTable(propertyTableContainer, 0);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
//		gd.heightHint = 200;
		ctPropTable.setLayoutData(gd);
	}
	
	private CustomTagAttribute getSelectedProperty() {
		return ctPropTable.getSelectedProperty();
	}

	@Override public void update(Observable o, Object arg) {
		
		
		if (arg instanceof TagRegistryChangeEvent) {
			logger.debug("updated tag registry "+arg);

			TagRegistryChangeEvent e = (TagRegistryChangeEvent) arg;
			logger.debug("updating available tags, e = "+arg);
			
			updateAvailableTags();
		}
	}

//	private void initPropertyTable() {
//		logger.debug("initing property table!");
//		if (propertyTable != null && !propertyTable.isDisposed()) {	
//			logger.debug("disposing old property table...");
//			propertyTable.dispose();
//			propertyTable = null;
//		}
//		
//		propertyTable = new MyPropertyTable(propertyTableContainer, SWT.NONE);
//		propertyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//		propertyTable.hideButtons();
//		
////		propertyTable.addChangeListener(new PTPropertyChangeListener() {
////			@Override public void propertyHasChanged(PTProperty property) {
////				logger.debug("property changed: "+property.getName()+" value: "+property.getValue());
////			}
////		});
//				
////		propertyTable.viewAsFlatList();
//				
//		propsExp.layout();
//		this.layout();
//	}
	
//	private Table getPropertyTableWidget()  {
//		try {
//			Field f = propertyTable.getClass().getDeclaredField("widget"); //NoSuchFieldException
//			f.setAccessible(true);
//			PTWidget w = (PTWidget) f.get(propertyTable);
//			logger.info("ptwidget: "+w);
//			
//			return (Table) w.getWidget();
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			return null;
//		}
//	}
	
//	private void updatePropertyTableProperties(CustomTag tag, boolean setValues) {
//		if (!withProperties)
//			return;
//		
//		if (true)
//			return;
//		
//		initPropertyTable();
//				
////		propertyTable.getPropertiesListPointer().clear();
//		
//		for (CustomTagAttribute an : tag.getAttributes()) {
//			logger.debug("adding attribute: "+an);
//			if (false && !an.isEditable()) {
//				logger.debug("attribute "+an.getName()+" is not editable - skipping!");
//				continue;
//			}
//			
//			Class<?> type = tag.getAttributeType(an.getName());
//			if (type == null) {
//				logger.warn("could not determine type for attribute: "+an+" - skipping!");
//				continue;
//			}
//			logger.debug("type = "+type);
//						
//			String displayName = an.getName(); // default: use tag name as display name!
//			if (USE_SIMPLE_ATTRIBUTES)
//				displayName = StringUtils.isEmpty(an.getDisplayName()) ? an.getName() : an.getDisplayName();
//				
//			PTProperty pt = new PTProperty(an.getName(), displayName, an.getDescription(), null);
//			if (type.equals(Boolean.class))
//				pt.setEditor(new PTCheckboxEditor());
//			else if (type.equals(Float.class))
//				pt.setEditor(new MyPTFloatEditor());
//			else if (type.equals(Integer.class))
//				pt.setEditor(new MyPTIntegerEditor());
//			
//			pt.setEnabled(an.isEditable());
//			
//			try {
//				propertyTable.addProperty(pt);
//				if (setValues)
//					pt.setValue(tag.getAttributeValue(an.getName()));
//				else
//					pt.setValue(null);				
//			} catch (Exception e) {
//				logger.warn(e.getMessage(), e);
//			}
//			
//
//		}
////		this.layout();
////		propertyTable.layout();
//		
////		propertyTable.viewAsFlatList();
//		
//		logger.debug("widget: "+propertyTable.getWidget());
//		propertyTable.getWidget().refillData();
//		propertyTable.layout();
//		
//		propsExp.layout();
//		layout();
//		redraw();
//	}
	
	

}
