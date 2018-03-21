package eu.transkribus.swt_gui.search.text_and_tags;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDbTag;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CssSyntaxTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.CustomTagPropertyTable;

class TagNormalizationWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TagNormalizationWidget.class);
	
	CustomTagPropertyTable propertyTable;
	Button normalizeBtn;
	Label label;
	
//	List<CustomTag> selectedTags;
	List<TrpDbTag> selectedTags;
	
	boolean currentPageAffected = false;

	public TagNormalizationWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		label = new Label(this, 0);
		label.setText("Properties for selected tags: ");
		
		propertyTable = new CustomTagPropertyTable(this, 0, false);
		propertyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		normalizeBtn = new Button(this, 0);
		normalizeBtn.setText("Update!");
		normalizeBtn.setToolTipText("Updates the attributes for all selected tags with the values above and saves the affected pages.");
		
		normalizeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		normalizeBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				normalizeSelectedTags();
			}
		});
	}
	
	static boolean isSameTags(Collection<TrpDbTag> tags) {
		String tn = null;
		
		try {
			for (TrpDbTag t : tags) {
				CssSyntaxTag cssTag = CssSyntaxTag.parseSingleCssTag(t.getCustomTagCss());
				
				if (tn == null) {
					tn = cssTag.getTagName();
					continue;
				}
				else if (!tn.equals(cssTag.getTagName())) {
					return false;
				}
			}
			return true;
		} catch (ParseException e) {
			logger.error("Error parsing tag: "+e.getMessage(), e);
			return false;
			
		}
	}
	
	void normalizeSelectedTags() {
		if (selectedTags == null)
			return;
		
		
		try {
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
	//					monitor.setTaskName("Saving");
						logger.info("normalizing tags: "+selectedTags.size());
						
						monitor.beginTask("Updating tag values", selectedTags.size());
						int c=0;
						
						Storage s = Storage.getInstance();
						
						// a pages cache: key is pageid, value is a pair of collection-id and corresponding TrpPageType
						Map<Integer, Pair<Integer, TrpPageType>> affectedPages = new HashMap<>();
						for (TrpDbTag t : selectedTags) {
							if (monitor.isCanceled())
								return;
							
							// compile list of affected pages and tags to be changed
							logger.debug("Updating tag value for " + t.toString());
							
							// set attributes in tag result object
							t.setCustomTagCss(propertyTable.getSelectedTag().getCssStr(false));
							logger.trace("New value: " + selectedTags.get(c));
							
							// load page on which the tag is on (if not loaded yet)
							Pair<Integer, TrpPageType> ptPair = affectedPages.get(t.getPageid());
							if (ptPair == null) {
								TrpPage page = s.getConnection().getTrpDoc(t.getCollId(), t.getDocid(), 1).getPages().get(t.getPagenr()-1);
								TrpPageType pt = s.getOrBuildPage(page.getCurrentTranscript(), true);
								ptPair = Pair.of(t.getCollId(), pt);
								affectedPages.put(t.getPageid(), ptPair);
								if (s.getPage().getPageId() == page.getPageId()){
									currentPageAffected = true;
								}
							}
							
							// convert DbTag to CustomTag
							CssSyntaxTag cssTag =  CssSyntaxTag.parseSingleCssTag(t.getCustomTagCss());
							CustomTag ct = CustomTagFactory.create(cssTag.getTagName(), t.getOffset(), t.getLength(), cssTag.getAttributes());

							// retrieve parent line / shape
							TrpTextLineType lt = ptPair.getRight().getLineWithId(t.getRegionid());
							
							// add or merge tag on line
							lt.getCustomTagList().addOrMergeTag(ct, null, true);
							
							monitor.worked(c++);
						}
						
						logger.debug("nr of affected pages: "+affectedPages.size());
						s.saveTranscriptsMap(affectedPages, monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			}, "Normalizing tag attributes", true);
		} catch (Throwable e) {
			TrpMainWidget.getInstance().onError("Error normalizing tag attributes", e.getMessage(), e, true, false);
		}
		//if loaded pae (loaded in GUI) has normalized tags then the new transcript must be loaded
		if (currentPageAffected){
			TrpMainWidget.getInstance().reloadCurrentPage(true, true);
		}
	}
	
	void setInput(List<TrpDbTag> selectedTags) {
		if (selectedTags != null && !selectedTags.isEmpty() && isSameTags(selectedTags)) {
			this.selectedTags = selectedTags;
			
			try {
	//			CustomTag protoTag = selectedTags.get(0).copy();
				CssSyntaxTag cssTag = CssSyntaxTag.parseSingleCssTag(selectedTags.get(0).getCustomTagCss());
				CustomTag selectedCustomTag = CustomTagFactory.create(cssTag.getTagName(), cssTag.getAttributes());
				logger.debug("selectedCustomTag = "+selectedCustomTag);
				
//				CustomTag protoTag = CustomTagFactory.getTagObjectFromRegistry(cssTag.getTagName());
				// TODO clear attributes!?
	
				propertyTable.setInput(selectedCustomTag);
			} catch (Exception e) {
				logger.error("Error setting normalization tag: "+e.getMessage(), e);
				this.selectedTags = null;
				propertyTable.setInput(null);
			}
		} else {
			this.selectedTags = null;
			propertyTable.setInput(null);
		}
		propertyTable.redraw();
	}

}
