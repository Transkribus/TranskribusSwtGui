package eu.transkribus.swt_gui.search;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ServerErrorException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.TrpTotalTranscriptStatistics;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.LabeledCombo;
import eu.transkribus.swt.util.LabeledText;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.search.documents.DocSearchComposite;
import eu.transkribus.swt_gui.search.fulltext.FullTextSearchComposite;
import eu.transkribus.swt_gui.search.kws.KeywordSpottingComposite;
import eu.transkribus.swt_gui.search.kws.OldKeywordSpottingComposite;
import eu.transkribus.swt_gui.search.kws_solr.KWSearchComposite;
import eu.transkribus.swt_gui.search.text_and_tags.TagSearchComposite;

public class SearchDialog extends Dialog {
	private final static Logger logger = LoggerFactory.getLogger(SearchDialog.class);
	
	DocSearchComposite docSearchComposite;
	OldKeywordSpottingComposite oldKwsComposite;
	FullTextSearchComposite fullTextSearchComposite;
	KeywordSpottingComposite kwsComposite;
	KWSearchComposite kwSolrSearchComposite;
	

	LabeledText kwsDocId;
	LabeledCombo kwsCollection;
	
	CTabFolder tabFolder;
	CTabItem docSearchTabItem, oldKwsTabItem, tagsItem, fullTextSearchItem, kwsTabItem, kwSolrSearchItem;
	
	
	public enum SearchType { 
		DOC, OLD_KWS, TAGS, FULLTEXT, KWS
	}


	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SearchDialog(Shell parentShell) {
		super(parentShell);
		
//		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setShellStyle(SWT.SHELL_TRIM | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
		setBlockOnOpen(false);
	}
	
	@Override protected boolean isResizable() {
		return true;
	}
	
	@Override protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Search for...");
		SWTUtil.centerShell(shell);
		shell.setMinimumSize(640, 480);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override protected Control createDialogArea(Composite parent) {
		Composite c = (Composite) super.createDialogArea(parent);
//		c.setLayout(new FillLayout());
		c.setLayout(new GridLayout());
		
		tabFolder = new CTabFolder(c, SWT.BORDER | SWT.FLAT);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
						
		docSearchComposite = new DocSearchComposite(tabFolder, 0);
		docSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		docSearchTabItem = createCTabItem(tabFolder, docSearchComposite, "Documents");
		
		fullTextSearchComposite = new FullTextSearchComposite(tabFolder, 0);
		fullTextSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fullTextSearchItem = createCTabItem(tabFolder, fullTextSearchComposite, "Fulltext (Solr)");
				
		TagSearchComposite tagSearchComp = new TagSearchComposite(tabFolder, 0);
		tagSearchComp.setLayoutData(new GridData(GridData.FILL_BOTH));
		tagsItem = createCTabItem(tabFolder, tagSearchComp, "Tags");

//		TextAndTagSearchComposite tsc = new TextAndTagSearchComposite(tabFolder, 0);
//		tsc.setLayoutData(new GridData(GridData.FILL_BOTH));
//		textAndTagsItem = createCTabItem(tabFolder, tsc, "Text / Tags");
		
		if (false) {
			oldKwsComposite = new OldKeywordSpottingComposite(tabFolder, 0);
			oldKwsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			oldKwsTabItem = createCTabItem(tabFolder, oldKwsComposite, "KWS (Demo)");
		}
		
		final boolean showKws = isUserAllowedForKws();
		if(showKws) {
			kwsComposite = new KeywordSpottingComposite(tabFolder, 0);
			kwsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			kwsTabItem = createCTabItem(tabFolder, kwsComposite, "KWS");
		}
		
		if(isUserAllowedForSolrKws()){
			kwSolrSearchComposite = new KWSearchComposite(tabFolder,0);
			kwSolrSearchComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
			kwSolrSearchItem = createCTabItem(tabFolder, kwSolrSearchComposite, "Keyword (Solr)");
		}		
		
		SWTUtil.onSelectionEvent(tabFolder, e -> { updateTabItemStyles(); });
		
		updateTabItemStyles();
		
		return c;
	}
	
	public void updateTabItemStyles() {
		SWTUtil.setBoldFontForSelectedCTabItem(tabFolder);
	}
	
	public boolean isUserAllowedForSolrKws(){
		
		boolean allowed = false;
		try {
			allowed = Storage.getInstance().getConnection().canManageCollection(1555);
		} catch (SessionExpiredException | ServerErrorException | ClientErrorException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		final String testServer = "https://transkribus.eu/TrpServerTesting";
		if(!Storage.getInstance().getCurrentServer().equals(testServer)){
			allowed = false;
		}
		
		return allowed;
	}
	
	public boolean isUserAllowedForKws() {
		boolean showKws = false;
		if(Storage.getInstance() != null && Storage.getInstance().isLoggedIn()) {
			try {
				showKws = Storage.getInstance()
						.getConnection()
						.isUserAllowedForJob(JobImpl.CITlabKwsJob.toString());
			} catch (Exception e) {
				logger.error("Could not determine permission for KWS on server!", e);
				showKws = false;
			}
		}
		return showKws;
	}
	
	public void selectTab(SearchType type) {
		if (type == SearchType.DOC) {
			tabFolder.setSelection(docSearchTabItem);
		}
		else if (type == SearchType.OLD_KWS) {
			tabFolder.setSelection(oldKwsTabItem);
		}
		else if (type == SearchType.TAGS) {
			tabFolder.setSelection(tagsItem);
		}
		else if (type == SearchType.FULLTEXT) {
			tabFolder.setSelection(fullTextSearchItem);
		}
		else if (type == SearchType.KWS) {
			tabFolder.setSelection(kwsTabItem);
		}
		
		updateTabItemStyles();
	}
	
	
	public CTabFolder getTabFolder(){
		return tabFolder;
	}
	
	public CTabItem getFulltextTabItem(){
		return fullTextSearchItem;
	}
	
	public FullTextSearchComposite getFulltextComposite(){
		return fullTextSearchComposite;
	}
	
	public CTabItem getTagsItem() {
		return tagsItem;
	}
	
	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override protected void createButtonsForButtonBar(Composite parent) {
//		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override protected Point getInitialSize() {
		return new Point(1000, 800);
	}
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
		return ti;
	}
	

}