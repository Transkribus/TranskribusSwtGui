package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import eu.transkribus.swt.util.MultiCheckSelectionCombo;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.ToolBox;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;

public class StructureTagComposite extends Composite {
	
	private Storage store = Storage.getInstance();
	private MultiCheckSelectionCombo multiCombo;
	

	public StructureTagComposite(Composite parent) {
		
		super(parent, 0);
		this.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
		
		List<StructCustomTagSpec> tags = new ArrayList<>();
		tags = store.getStructCustomTagSpecs();
		
		setMultiCombo(new MultiCheckSelectionCombo(this, SWT.FILL,"Restrict on structure tags", 1, 200, 300 ));
		getMultiCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		for(StructCustomTagSpec tag : tags) {
			int itemCount = getMultiCombo().getItemCount();
			List<String> items = new ArrayList<>();
			for(int i = 0; i < itemCount; i++) {
				items.add(getMultiCombo().getItem(i));
			}	
			if(!items.contains(tag.getCustomTag().getType())) {
				getMultiCombo().add(tag.getCustomTag().getType());
			}	
		}
		
	}


	public MultiCheckSelectionCombo getMultiCombo() {
		return multiCombo;
	}


	public void setMultiCombo(MultiCheckSelectionCombo multiCombo) {
		this.multiCombo = multiCombo;
	}

}