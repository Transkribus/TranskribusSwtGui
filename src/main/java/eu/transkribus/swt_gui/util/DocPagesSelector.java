package eu.transkribus.swt_gui.util;

import java.io.IOException;
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
import org.eclipse.swt.widgets.Text;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class DocPagesSelector extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(DocPagesSelector.class);
	
	List<TrpPage> pages;
	Text pagesText;
	Button selectPagesBtn;
	Button currentPageBtn, allPagesBtn;
	
	Button testBtn;
	Label label;
	
	static final boolean TEST=false;
		
//	public DocPagesSelector(Composite parent, int style) {
//		this(parent, style, true, new ArrayList<>());
//	}

	public DocPagesSelector(Composite parent, int style, final List<TrpPage> pages) {
		this(parent, style, true, pages);
	}
	
	public DocPagesSelector(Composite parent, int style, boolean showLabel, final List<TrpPage> pages) {
		this(parent, style, showLabel, false, false, pages);
	}
		
	public DocPagesSelector(Composite parent, int style, boolean showLabel, boolean showCurrentPageBtn, boolean showAllPagesBtn, final List<TrpPage> pages) {
		super(parent, style);
		
		int nColumns = 2;
		if (showLabel)
			nColumns++;
		if (showCurrentPageBtn)
			nColumns++;
		if (showAllPagesBtn)
			nColumns++;
		if (TEST)
			nColumns++;
		
		this.setLayout(new GridLayout(nColumns, false));
		
		Assert.assertNotNull("pages null!", pages);
		
		this.pages = pages;
		
		if(showLabel) {
			label = new Label(this, 0);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		}
		
		pagesText = new Text(this, SWT.SINGLE | SWT.BORDER);
		pagesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		pagesText.setToolTipText("Type page ranges and/or single page numbers seperated by commas, e.g. '1-10, 13, 14' or '1, 3-4, 6-8, 10'");
		
		resetLabelAndPagesStr();
		
		selectPagesBtn = new Button(this, SWT.PUSH);
		selectPagesBtn.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		selectPagesBtn.setText("...");
		selectPagesBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				openDocPageViewer();
			}
		});
		
		if (showCurrentPageBtn) {
			currentPageBtn = new Button(this, SWT.PUSH);
		    currentPageBtn.setText("Current");
		    currentPageBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		    currentPageBtn.setToolTipText("Set pages to currently loaded page");
		    currentPageBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					String currentPageNr = Integer.toString(Storage.getInstance().getPageIndex()+1);
					getPagesText().setText(currentPageNr);				
				}
			});
		}
		
		if (showAllPagesBtn) {
			allPagesBtn = new Button(this, SWT.PUSH);
			allPagesBtn.setText("All");
			allPagesBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			allPagesBtn.setToolTipText("Set pages to all pages");
			allPagesBtn.addSelectionListener(new SelectionAdapter() {
				@Override public void widgetSelected(SelectionEvent e) {
					resetLabelAndPagesStr();		
				}
			});
		}

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
	
	protected void openDocPageViewer() {
		logger.debug("opening DocPageViewer, pages = "+DocPagesSelector.this.pages);
		
		if (DocPagesSelector.this.pages.isEmpty())
			return;
		
		final DocPageViewer dpv = new DocPageViewer(SWTUtil.dummyShell, 0, false, true, false);
		dpv.setDataList(DocPagesSelector.this.pages);
//		Shell s = DialogUtil.openShellWithComposite(null, dpv, 400, 400, "Select pages");
		final MessageDialog d = DialogUtil.createCustomMessageDialog(getShell(), "Select pages", "", null, 0, new String[]{"OK",  "Cancel"}, 0, dpv);
		// gets called when dialog is closed:
		dpv.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				logger.trace("return code: "+d.getReturnCode());
				logger.trace("checked list: "+dpv.getCheckedList());
				if (d.getReturnCode() == 0) {
					String rs = CoreUtils.getRangeListStr(dpv.getCheckedList());
					logger.trace("rs = "+rs);
					pagesText.setText(rs);
				}
			}
		});
		d.open();
	}
	
	public void setPages(List<TrpPage> pages) {
		this.pages = pages;
		
		resetLabelAndPagesStr();
	}
	
	private void resetLabelAndPagesStr() {
		if (label != null) {
			label.setText("Pages ("+this.pages.size()+"): ");
		}
		
		if (!pages.isEmpty())
			pagesText.setText("1-"+this.pages.size());
		else
			pagesText.setText("");
	}
	
	public Set<Integer> getSelectedPageIndices() throws IOException {
		return CoreUtils.parseRangeListStr(pagesText.getText(), pages.size());
	}
	
	public Text getPagesText(){
		return pagesText;
	}
	
	public String getPagesStr() {
		return pagesText.getText();
	}

	public List<TrpPage> getPages() {
		return pages;
	}

	public Button getSelectPagesBtn() {
		return selectPagesBtn;
	}

	public Button getCurrentPageBtn() {
		return currentPageBtn;
	}

	public Button getAllPagesBtn() {
		return allPagesBtn;
	}

	public Button getTestBtn() {
		return testBtn;
	}

	public Label getLabel() {
		return label;
	}

	public void setEnabled(boolean enabled) {
		SWTUtil.setEnabled(pagesText, enabled);
		SWTUtil.setEnabled(selectPagesBtn, enabled);
		SWTUtil.setEnabled(allPagesBtn, enabled);
		SWTUtil.setEnabled(currentPageBtn, enabled);
	}
}
