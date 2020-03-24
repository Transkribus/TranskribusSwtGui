package eu.transkribus.swt_gui.htr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import eu.transkribus.core.model.beans.customtags.GapTag;
import eu.transkribus.core.model.beans.customtags.UnclearTag;

/**
 * Quick'n'dirty tag selection composite. Contains check boxes for a limited number of tags (gap and unclear right now).
 * Replace internal logic once there is a nicer solution for selecting tags (including user-defined ones?).
 * 
 * @see {@link StructureTagComposite} which requires the same refactoring.
 */
public class HtrTagsToIgnoreChooserComposite extends Composite {
	
	private Button gapTagBtn, unclearTagBtn;
	
	public HtrTagsToIgnoreChooserComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout omitLinesCompLayout = new GridLayout(2, false);
		//remove the margin. otherwise the buttons might have ugly x-offsets
		omitLinesCompLayout.marginHeight = omitLinesCompLayout.marginWidth = 0;
		this.setLayout(omitLinesCompLayout);
		
		gapTagBtn = new Button(this, SWT.CHECK);
		gapTagBtn.setText(GapTag.TAG_NAME);
		gapTagBtn.setLayoutData(new GridData(GridData.BEGINNING));
		unclearTagBtn = new Button(this, SWT.CHECK);
		unclearTagBtn.setText(UnclearTag.TAG_NAME);
		unclearTagBtn.setLayoutData(new GridData(GridData.BEGINNING));
	}
	
	/**
	 * @return a list with names of the selected tags, as defined in the tag 
	 * class' TAG_NAME constant (e.g. {@link GapTag#TAG_NAME})
	 */
	public List<String> getSelectedTags() {
		List<String> selectedTags = new ArrayList<>();
		if(gapTagBtn.getSelection()) {
			selectedTags.add(GapTag.TAG_NAME);
		}
		if(unclearTagBtn.getSelection()) {
			selectedTags.add(UnclearTag.TAG_NAME);
		}
		return selectedTags;
	}

	public void clearSelection() {
		unclearTagBtn.setSelection(false);
		gapTagBtn.setSelection(false);
	}
}
