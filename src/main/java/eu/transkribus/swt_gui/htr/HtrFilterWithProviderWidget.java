package eu.transkribus.swt_gui.htr;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class HtrFilterWithProviderWidget extends HtrFilterWidget {
	protected Combo providerCombo;
	
	public HtrFilterWithProviderWidget(Composite parent, StructuredViewer viewer, final String htrProviderFilterValue, int style) {
		super(parent, viewer, style);
		
		Label providerLabel = new Label(this, SWT.NONE);
		providerLabel.setText("Technology:");
		providerLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		providerCombo = new Combo(this, SWT.READ_ONLY);
		
		if(htrProviderFilterValue == null) {
			addProviderFilter(providerCombo, "Show all", null);
			for (String p : HtrTableWidget.providerValues) {
				addProviderFilter(providerCombo, HtrTableLabelProvider.getLabelForHtrProvider(p), p);
			}
		} else {
			addProviderFilter(providerCombo, HtrTableLabelProvider.getLabelForHtrProvider(htrProviderFilterValue), htrProviderFilterValue);
			//lock the combo as no choice is allowed
			providerCombo.setEnabled(false);
		}
		
		//filtering by provider is done in Storage and that's why the listener is attached in the outer Composite
		providerCombo.select(0);
		providerCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));		
	}
	
	@Override
	public void addListener(int eventType, Listener listener) {
		super.addListener(eventType, listener);
		providerCombo.addListener(eventType, listener);
	}
	
	/**
	 * set two additional columns to hold the provider combo filter
	 */
	@Override
	protected Layout createLayout() {
		return new GridLayout(5, false);
	}
	
	private void addProviderFilter(Combo providerCombo, String label, String data) {
		providerCombo.add(label);
		providerCombo.setData(label, data);
	}
	
	public void resetProviderFilter() {
		if(providerCombo != null) {
			providerCombo.select(0);
		}
	}
	
	public Combo getProviderCombo() {
		return providerCombo;
	}
	
	public Text getFilterText() {
		return filterTxt;
	}
}
