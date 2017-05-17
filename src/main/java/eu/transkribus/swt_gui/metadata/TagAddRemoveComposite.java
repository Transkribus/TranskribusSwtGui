package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.swt.util.Images;

public class TagAddRemoveComposite extends Composite {
	
//	public static class AddOrRemoveTagListener extends SelectionAdapter {
//		
//		TaggingActionType type;
//		
//		int rowIndex=-1;
//		
//	 	public AddOrRemoveTagListener(TaggingActionType type) {
//	 		this.type = type;
//    	}
//	 	    	
//		public AddOrRemoveTagListener(TaggingActionType type, int rowIndex) {
//			this(type);
//			this.rowIndex = rowIndex;
//		}
//
//		@Override public void widgetSelected(SelectionEvent e) {
//			Event se = new Event();
//		
//			
//			if (type == TaggingActionType.ADD_TAG || type == TaggingActionType.DELETE_TAG) {
//				String selectedTag = null;
//				if (rowIndex != -1) {
//					selectedTag = (String) availableTagNames.toArray()[rowIndex];
////					selectedTag = availableTagNames.toArray(new String[])[]
//				} else {
//					selectedTag = getSelectedTagName();
//				}
//				se.text = selectedTag;
////				se.detail = type;
//			}
//			se.data = type;
//		
//			notifyListeners(SWT.Selection, se);
//		}
//	};
	
	Button addButton, removeButton;

	public TagAddRemoveComposite(Composite parent, int style, boolean addBtn, boolean removeBtn) {
		super(parent, style);
		
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.marginBottom = 0;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginLeft = 0;
        
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);
//      setLayout(new FillLayout());
        
        if (addBtn) {
        addButton = new Button(this, SWT.PUSH);
//        addButton.setImage(Images.ADD);
        addButton.setImage(Images.getOrLoad("/icons/add_12.png"));
//        addButton.setText("+");
//        addButton.addSelectionListener(new AddOrRemoveTagListener(TaggingActionType.ADD_TAG, rowIndex));
        addButton.setToolTipText("Add this tag to the current selection in the transciption widget");
        addButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
//        addToMap(addBtns, tagName, addButton);
        }
        
        if (removeBtn) {
//        logger.debug("adding remove button for tag = "+tagName);
        removeButton = new Button(this, SWT.PUSH);
        removeButton.setImage(Images.getOrLoad("/icons/delete_12.png"));
//        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        addToMap(removeBtns, tagName, removeButton);
        
//        Button removeButton = removeBtns.get(tagName);
//        logger.debug("removeButton = "+removeButton);
//        if (removeButton == null) {
//        	removeButton = new Button(c, SWT.PUSH);
//        	removeBtns.put(tagName, removeButton);
//        }
        
//        removeButton.setImage(Images.DELETE);
//        removeButton.setText("-");
//        removeButton.addSelectionListener(new AddOrRemoveTagListener(TaggingActionType.DELETE_TAG, ri));
        removeButton.setToolTipText("Remove this tag from the current selection in the transcription widget if there");
//        removeButton.setEnabled(isSelected);
        removeButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        }
	}
	
	public Button getAddButton() { return addButton; }
	public Button getRemoveButton() { return removeButton; }
	
	

}
