package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

public class ExclusiveButtonSelectionGroup extends SelectionAdapter {
	List<Button> btns = new ArrayList<Button>();
	
	public void addButton(Button btn) { 
		btns.add(btn);
		btn.addSelectionListener(this);
	}
	
	@Override public void widgetSelected(SelectionEvent e) {
		for (Button b : btns) {
			if (e.getSource() != b)
				b.setSelection(false);
		}
	}
}
