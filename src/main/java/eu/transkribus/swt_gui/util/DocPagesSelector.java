package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.SWTUtil;
import org.dea.util.Utils;
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
import eu.transkribus.core.util.CoreUtils;

public class DocPagesSelector extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocPagesSelector.class);
	
	List<TrpPage> pages;
	Text pagesText;
	Button selectPagesBtn;
	Button testBtn;
	
	static final boolean TEST=false;

	public DocPagesSelector(Composite parent, int style, final List<TrpPage> pages) {
		super(parent, style);
		this.setLayout(new GridLayout(3, false));
		
		Assert.assertNotNull("pages null!", pages);
		
		this.pages = pages;
		
		Label l = new Label(this, 0);
		l.setText("Pages ("+pages.size()+"): ");
		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		pagesText = new Text(this, SWT.SINGLE | SWT.BORDER);
		pagesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pagesText.setToolTipText("Type page ranges and/or single page numbers seperated by commas, e.g. '1-10, 13, 14' or '1, 3-4, 6-8, 10'");
		pagesText.setText("1-"+pages.size());
		
		selectPagesBtn = new Button(this, SWT.PUSH);
		selectPagesBtn.setText("...");
		selectPagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				final DocPageViewer dpv = new DocPageViewer(SWTUtil.dummyShell, 0, false, true, false);
				dpv.setDataList(pages);
//				Shell s = DialogUtil.openShellWithComposite(null, dpv, 400, 400, "Select pages");
				final MessageDialog d = DialogUtil.createCustomMessageDialog(getShell(), "Select pages", "", null, 0, new String[]{"OK",  "Cancel"}, 0, dpv);
				// gets called when dialog is closed:
				dpv.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						logger.info("return code: "+d.getReturnCode());
						logger.info("checked list: "+dpv.getCheckedList());
						if (d.getReturnCode() == 0) {
							String rs = CoreUtils.getRangeListStr(dpv.getCheckedList());
							logger.info("rs = "+rs);
							pagesText.setText(rs);
						}
					}
				});
				d.open();
			}
		});
		selectPagesBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		
		if (TEST) {
			testBtn = new Button(this, SWT.PUSH);
			testBtn.setText("test");
			testBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					Set<Integer> pi;
					try {
						pi = getSelectedPageIndices();
						logger.info("pi = "+pi);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			testBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		}
	}
	
	public Set<Integer> getSelectedPageIndices() throws IOException {
		return CoreUtils.parseRangeListStr(pagesText.getText(), pages.size());
	}
	
	public Text getPagesText(){
		return pagesText;
	}
}
