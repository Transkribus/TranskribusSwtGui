package eu.transkribus.swt_gui.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.TableViewerUtils;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class TagListWidget extends Composite {
	
	TableViewer tableViewer;
	Map<CustomTag, ControlEditor> delSelectedEditors = new HashMap<>();
	Button clearTagsBtn;
	
	List<CustomTag> tagList = new ArrayList<>();

	public TagListWidget(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());
		
		Composite container = new Composite(this, 0);
		container.setLayout(new GridLayout(1, false));
//		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label headerLbl = new Label(container, 0);
		headerLbl.setText("Tags in selected region");
		Fonts.setBoldFont(headerLbl);
		headerLbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		tableViewer = new TableViewer(container);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setHeaderVisible(false);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		TableViewerColumn tagCol = TableViewerUtils.createTableViewerColumn(tableViewer, SWT.LEFT, "Tags in region", 300);
		TableViewerColumn deleteTagCol = TableViewerUtils.createTableViewerColumn(tableViewer, SWT.LEFT, "", 50);
		
		tagCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				final CustomTag tag = (CustomTag) cell.getElement();
				cell.setText(tag.getCssStr());
			}
		});
		
		class DeleteTagDefSelectionListener extends SelectionAdapter {
			CustomTag tag;
			
			public DeleteTagDefSelectionListener(CustomTag tag) {
				this.tag = tag;
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tag != null) {
				 	if (TrpMainWidget.getInstance() != null) {
				 		TrpMainWidget.getInstance().deleteTags(tag);
				 		
				 		
				 		
				 	}
				}
			}
		};
		        
		deleteTagCol.setLabelProvider(new CellLabelProvider() {
			@Override public void update(ViewerCell cell) {
				final CustomTag tag = (CustomTag) cell.getElement();
//				String tagName = tag.getTagName();
				
				final TableItem item = (TableItem) cell.getItem();
				TableEditor editor = new TableEditor(item.getParent());
				Button removeButton = new Button((Composite) cell.getViewerRow().getControl(), SWT.PUSH);
		        removeButton.setImage(Images.getOrLoad("/icons/delete_12.png"));
		        removeButton.setToolTipText("Delete this tag");
		        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
		        removeButton.addSelectionListener(new DeleteTagDefSelectionListener(tag));
		        Control c = removeButton;
				
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
		
//		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//			@Override public void selectionChanged(SelectionChangedEvent event) {
//				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
//				String selectedTagName = sel.isEmpty() ? null : ((CustomTag) sel.getFirstElement()).getTagName();
//				selectTagname(selectedTagName);				
//			}
//		});
		
		Composite btnsContainer = new Composite(container, 0);
		btnsContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnsContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		Button reloadBtn = new Button(btnsContainer, SWT.PUSH);
		reloadBtn.setImage(Images.REFRESH);
		reloadBtn.setToolTipText("Reload tags for this region");
		SWTUtil.onSelectionEvent(reloadBtn, (e) -> {
			// TODO
			refreshTable();
		});
		
		Button clearTagsBtn = new Button(btnsContainer, SWT.PUSH);
//		clearTagsBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		clearTagsBtn.setImage(Images.DELETE);
		clearTagsBtn.setText("Delete tags for selection");
		clearTagsBtn.setToolTipText("Clears all tags from the current selection in the transcription widget");
//		clearTagsBtn.addSelectionListener(new TagActionSelectionListener(this, listener, TaggingActionType.CLEAR_TAGS));
//		clearTagsBtn.addSelectionListener(new ClearTagsSelectionListener(listener));
		// TODO: add clear tags listener
		
		
	}
	
	public void refreshTable() {
		// TODO: reload tags from currenlty loaded tags
		
		tableViewer.setInput(tagList);
		TaggingWidgetUtils.updateEditors(delSelectedEditors, tagList);
	}
	
	public TableViewer getTableViewer() {
		return tableViewer;
	}
	
	public CustomTag getSelected() {
		return (CustomTag) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
	}

}
