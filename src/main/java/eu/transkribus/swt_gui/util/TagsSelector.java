package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.swt_canvas.util.DialogUtil;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.util.Utils;

public class TagsSelector extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TagsSelector.class);
	
	Button exportTagsBtn;
	Button selectTagsBtn;
	Set<String> tagnames;
	Set<String> checkedTags;
	boolean tagExport=false;

	public TagsSelector(Composite parent, int style, final Set<String> tagnames) {

		super(parent, style);
		this.tagnames = tagnames;
		this.setLayout(new GridLayout(2, false));
		
//		exportTagsBtn = new Button(this, SWT.CHECK);
//		exportTagsBtn.setText("Export Tags");
//		exportTagsBtn.setToolTipText("If checked, all tags will be listed at the end of the export doc");
//		exportTagsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));
//
//		exportTagsBtn.addSelectionListener(new SelectionAdapter() {
//			@Override public void widgetSelected(SelectionEvent e) {
//				setTagExport(exportTagsBtn.getSelection());
//			}
//		});
		
		selectTagsBtn = new Button(this, SWT.PUSH);
		selectTagsBtn.setText("select Tags");
		selectTagsBtn.setToolTipText("Select tags you wish to export");
		selectTagsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false));
		selectTagsBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final TagsViewer dpv = new TagsViewer(SWTUtil.dummyShell, 0, false, true, false);
				dpv.setTags(tagnames, getCheckedTagnames());
//				Shell s = DialogUtil.openShellWithComposite(null, dpv, 400, 400, "Select pages");
				final MessageDialog d = DialogUtil.createCustomMessageDialog(getShell(), "Select tags", "", null, 0, new String[]{"OK",  "Cancel"}, 0, dpv);
				// gets called when dialog is closed:
				dpv.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
//						logger.info("return code: "+d.getReturnCode());
//						logger.info("checked list: "+dpv.getCheckedList());
						if (d.getReturnCode() == 0) {
							//logger.info("rs = "+ dpv.getCheckedList());
							setCheckedTagnames(dpv.getCheckedList());
							
						}
					}
				});
				d.open();
			}
		});

		
	}


	public Set<String> getCheckedTagnames() {
		return (checkedTags==null? tagnames : checkedTags);
	}

	public void setCheckedTagnames(Set<String> list) {
		this.checkedTags = list;
	}


}
