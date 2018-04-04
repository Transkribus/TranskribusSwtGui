package eu.transkribus.swt_gui.metadata;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class StructTagConfWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(StructTagConfWidget.class);
	
	IStorageListener storageListener;
	Storage store = Storage.getInstance();
	
	StructTagSpecWidget specWidget;
//	Combo structTypesCombo;
	ComboViewer structTypesCombo;
	Button addStructBtn;
	Button restoreDefaultBtn;
	
	public StructTagConfWidget(Composite parent, int style) {
		super(parent, style);
		
//		this.setLayout(SWTUtil.createGridLayout(2, true, 0, 0));
		this.setLayout(new GridLayout(2, false));
		
		storageListener = new IStorageListener() {
			@Override public void handlStructTagSpecsChangedEvent(StructTagSpecsChangedEvent e) {
//				logger.debug("updating list!!");
//				updateList();
			}
		};
		store.addListener(storageListener);
		
		specWidget = new StructTagSpecWidget(this, 0, true);
		specWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		structTypesCombo = new ComboViewer(this, 0);
		structTypesCombo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		structTypesCombo.setContentProvider(ArrayContentProvider.getInstance());
		structTypesCombo.setLabelProvider(new LabelProvider() {
	        @Override
	        public String getText(Object element) {
	            if (element instanceof StructCustomTagSpec) {
	            	StructCustomTagSpec spec = (StructCustomTagSpec) element;
	            	return spec.getCustomTag().getType();
	            }
	            else {
	            	return "i am error";
	            }
	        }
	    });
		
		structTypesCombo.getCombo().addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if(e.detail == SWT.TRAVERSE_RETURN) {
					addStructType();
				}
			}
		});
		updateDefaultTypesList();
		
		addStructBtn = new Button(this, 0);
		addStructBtn.setImage(Images.ADD);
		addStructBtn.setToolTipText("Add new struct type from text in combo on the left");
		SWTUtil.onSelectionEvent(addStructBtn, e -> { addStructType(); });
		
		restoreDefaultBtn = new Button(this, 0);
		restoreDefaultBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		restoreDefaultBtn.setText("Restore default");
		restoreDefaultBtn.setImage(Images.REFRESH);
		restoreDefaultBtn.setToolTipText("Restores the default structures types and color");
		
		SWTUtil.onSelectionEvent(restoreDefaultBtn, e -> {
			store.restoreDefaultStructCustomTagSpecs();
		});
		
		this.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				store.removeListener(storageListener);
			}
		});
	}
	
	private void updateDefaultTypesList() {
//		structTypesCombo.setInput(store.getStructCustomTagSpecs());
		structTypesCombo.setInput(store.getDefaultStructCustomTagSpecs());
	}
	
	private void addStructType() {
		String newStructType = structTypesCombo.getCombo().getText();
		if (StringUtils.isEmpty(newStructType)) {
			DialogUtil.showErrorMessageBox(getShell(), "Error", "No struct type specified!");
			return;
		}
		
//		if (store.getStructCustomTagSpecs()
//				.stream().filter(t -> t.getCustomTag().getType().equalsIgnoreCase(newStructType)).findFirst().orElse(null) == null) {
//			DialogUtil.showErrorMessageBox(getShell(), "Error", "Struct type already specified!");
//			return;			
//		}
		
		StructCustomTagSpec t = new StructCustomTagSpec(new StructureTag(newStructType), store.getNewStructCustomTagColor());
		logger.debug("t = "+t);
		store.addStructCustomTagSpec(t);
	}
	
}
