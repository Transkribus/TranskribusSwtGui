package eu.transkribus.swt_gui.search.text_and_tags;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.core.model.beans.customtags.CssSyntaxTag;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
//import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.CustomTagPropertyTable;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

class TagNormalizationWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TagNormalizationWidget.class);
	
	CustomTagPropertyTable propertyTable;
	Button normalizeBtn;
	Label label;
	
//	List<CustomTag> selectedTags;
	List<TrpDbTag> selectedTags;

	public TagNormalizationWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		label = new Label(this, 0);
		label.setText("Properties for selected tags: ");
		
		propertyTable = new CustomTagPropertyTable(this, 0);
		propertyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		normalizeBtn = new Button(this, 0);
		normalizeBtn.setText("Update!");
		normalizeBtn.setToolTipText("Updates the tag property values to the values above for all selected tags on the left!");
		
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
		
		final Storage s = Storage.getInstance();
		
		try {
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
//					monitor.setTaskName("Saving");
					logger.info("normalizing tags: "+selectedTags.size());
					
					monitor.beginTask("Updating tag values", selectedTags.size());
					int c=0;
					Map<TrpTranscriptMetadata, TrpPageType> affectedPages = new HashMap<>();
					for (TrpDbTag t : selectedTags) {
						if (monitor.isCanceled())
							return;
						
						// TODO: open pages from t.tsid and 
//						
//						t.setAttributes(propertyTable.getSelectedTag(), false, true);
//						affectedPages.put(t.getCustomTagList().getShape().getPage().getMd(), t.getCustomTagList().getShape().getPage());
						
//						t.getCustomTagList().getShape().getPage();
						monitor.worked(c++);
					}
					
					logger.debug("nr of affected transcripts: "+affectedPages.size());
					
					try {
						TagSearchComposite.saveAffectedPages(monitor, s.getCurrentDocumentCollectionId(), affectedPages.values());
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			}, "Normalizing tag values", true);
		} catch (Throwable e) {
			TrpMainWidget.getInstance().onError("Error normalizing tag values", e.getMessage(), e, true, false);
		}
	}
	
	void setInput(List<TrpDbTag> selectedTags) {
		if (selectedTags != null && !selectedTags.isEmpty() && isSameTags(selectedTags)) {
			this.selectedTags = selectedTags;
			
			try {			
	//			CustomTag protoTag = selectedTags.get(0).copy();
				CssSyntaxTag cssTag = CssSyntaxTag.parseSingleCssTag(selectedTags.get(0).getCustomTagCss());
				
				CustomTagFactory.create(cssTag.getTagName(), cssTag.getAttributes());
				
				CustomTag protoTag = CustomTagFactory.getTagObjectFromRegistry(cssTag.getTagName());
				// TODO clear attributes!?
	
				propertyTable.setInput(protoTag, protoTag);
			} catch (Exception e) {
				logger.error("Error setting normalization tag: "+e.getMessage(), e);
				this.selectedTags = null;
				propertyTable.setInput(null, null);
			}
		} else {
			this.selectedTags = null;
			propertyTable.setInput(null, null);
		}
	}

}
