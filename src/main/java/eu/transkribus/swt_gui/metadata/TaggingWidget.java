package eu.transkribus.swt_gui.metadata;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;

public class TaggingWidget extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(TaggingWidget.class);
	
	SashForm verticalSf;
//	SashForm horizontalSf;
	
	TagSpecsWidget tagDefsWidget;
	CustomTagPropertyTable propsTable;
	
	TagListWidget tagListWidget; 
	TranscriptionTaggingWidget transcriptionTaggingWidget;
	Shell transcriptionTaggingWidgetShell;
	
	Button enableTagEditorBtn, searchTagsBtn;
	
	public TaggingWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		verticalSf = new SashForm(this, SWT.VERTICAL);
		verticalSf.setLayout(new GridLayout(1, false));
		verticalSf.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		Composite topWidget = new Composite(verticalSf, 0);
//		topWidget.setLayout(SWTUtil.createGridLayout(1, false, 0, 0));
//		topWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		Composite btnsTopContainer = new Composite(topWidget, 0);
//		btnsTopContainer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
////		btnsTopContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
//		btnsTopContainer.setLayout(new FillLayout());
//		
//		Button enableTagEditorBtn = new Button(btnsTopContainer, SWT.TOGGLE);
//		enableTagEditorBtn.setText("Show text based tag editor");
//		enableTagEditorBtn.setToolTipText("Enables / disables the text based tag editor in the transcription widget");
//		enableTagEditorBtn.setImage(Images.getOrLoad("/icons/tag_blue_edit.png"));
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_TAG_EDITOR_PROPERTY, TrpConfig.getTrpSettings(), enableTagEditorBtn);
		
//		if (false) { // already contained in TranscriptionTaggingWidget ...
//		Button editTagSpecsBtn = new Button(btnsTopContainer, 0);
//		editTagSpecsBtn.setImage(Images.PENCIL);
//		editTagSpecsBtn.setText("Edit tag specs...");
//		editTagSpecsBtn.setToolTipText("Opens the dialog for editing the list of tag specifications");
//		editTagSpecsBtn.setImage(Images.PENCIL);
//		SWTUtil.onSelectionEvent(editTagSpecsBtn, e -> {
//			TagConfDialog diag = new TagConfDialog(getShell());
//			diag.open();
//		});
//		}
		
		tagListWidget = new TagListWidget(verticalSf, 0);
		tagListWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		enableTagEditorBtn = new Button(tagListWidget.getBtnsContainer(), SWT.TOGGLE);
//		enableTagEditorBtn.setText("Show text based tag editor");
		enableTagEditorBtn.setToolTipText("Shows / hides the tagging editor");
		enableTagEditorBtn.setImage(Images.getOrLoad("/icons/tag_blue_edit.png"));
		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_TAG_EDITOR_PROPERTY, TrpConfig.getTrpSettings(), enableTagEditorBtn);
		
		searchTagsBtn = new Button(tagListWidget.getBtnsContainer(), SWT.PUSH);
		searchTagsBtn.setToolTipText("Search for tags...");
		searchTagsBtn.setImage(Images.FIND);
		SWTUtil.onSelectionEvent(searchTagsBtn, e -> {
			TrpMainWidget.getInstance().openSearchForTagsDialog();
		});
		
//		DataBinder.get().bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_TAG_EDITOR_PROPERTY, TrpConfig.getTrpSettings(), enableTagEditorBtn);		
		
		transcriptionTaggingWidget = new TranscriptionTaggingWidget(verticalSf, 0);
		transcriptionTaggingWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		transcriptionTaggingWidget.getTabFolder().addCTabFolder2Listener(new CTabFolder2Adapter() {
			@Override
			public void maximize(CTabFolderEvent event) {
				setTaggingEditorVisiblity(2);
			}
			
			@Override
			public void minimize(CTabFolderEvent event) {
				setTaggingEditorVisiblity(1);
			}
		});		

//		verticalSf = new SashForm(this, SWT.VERTICAL);
//		
//		initTagDefsWidget(verticalSf);
//		
//		horizontalSf = new SashForm(verticalSf, SWT.HORIZONTAL); 
//		
//		initTagListWidget(horizontalSf);
//		initPropsWidget(horizontalSf);
//		
		verticalSf.setWeights(new int[] { 77, 33 } );
		
//		horizontalSf.setWeights(new int[] { 50, 50 } );
		
		setTaggingEditorVisiblity(TrpConfig.getTrpSettings().isShowTextTagEditor());
	}
	
	public void setTaggingEditorVisiblity(boolean visible) {
		setTaggingEditorVisiblity(visible ? 1 : 0);
	}
	
	public void setTaggingEditorVisiblity(int visibility) {
		logger.debug("setTaggingEditorVisiblity: "+visibility);
		
		enableTagEditorBtn.setSelection(visibility > 0);
		
		if (visibility <= 1) {
			transcriptionTaggingWidget.setParent(verticalSf);
			transcriptionTaggingWidget.moveBelow(null);
			transcriptionTaggingWidget.getTabFolder().setMaximizeVisible(true);
			transcriptionTaggingWidget.getTabFolder().setMinimizeVisible(false);
			transcriptionTaggingWidget.pack();
			
			if (!SWTUtil.isDisposed(transcriptionTaggingWidgetShell)) {
				logger.trace("disposing shell!");
				transcriptionTaggingWidgetShell.dispose();
			}
			
			verticalSf.setWeights(new int[] { 77, 33 });
			if (true) // false -> show editor always
			if (visibility<=0) {
				verticalSf.setMaximizedControl(tagListWidget);
			} else {
				verticalSf.setMaximizedControl(null);
				if (transcriptionTaggingWidget.isTagPropertyEditorSelected()) {
					transcriptionTaggingWidget.getTagPropertyEditor().findAndSetNextTag();	
				}
			}
		} else {
			if (SWTUtil.isDisposed(transcriptionTaggingWidgetShell)) {
//				int shellStyle = SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE /*| SWT.MAX*/;
				int shellStyle = SWT.MODELESS | SWT.SHELL_TRIM;
				transcriptionTaggingWidgetShell = new Shell(getDisplay(), shellStyle);
				transcriptionTaggingWidgetShell.setText("Text based Tagging");
				transcriptionTaggingWidgetShell.setLayout(new FillLayout());
				
				// on closing this shell -> dock this widget again!
				transcriptionTaggingWidgetShell.addListener(SWT.Close, new Listener() {
					@Override public void handleEvent(Event event) {
						TrpConfig.getTrpSettings().setShowTextTagEditor(true);
					}
				});				
			
				Point l = this.toDisplay(this.getLocation());
				int height=600;
				transcriptionTaggingWidgetShell.setSize(400, height);
				transcriptionTaggingWidgetShell.setLocation(l.x, l.y+this.getSize().y-height);		
			}
			
			transcriptionTaggingWidgetShell.setVisible(true);
			transcriptionTaggingWidgetShell.setActive();

			transcriptionTaggingWidget.setParent(transcriptionTaggingWidgetShell);
			transcriptionTaggingWidget.getTabFolder().setMaximizeVisible(false);
			transcriptionTaggingWidget.getTabFolder().setMinimizeVisible(true);
			transcriptionTaggingWidget.pack();
			transcriptionTaggingWidget.layout();
			verticalSf.setWeights(new int[] { 100 });
			verticalSf.setMaximizedControl(tagListWidget);
			
			transcriptionTaggingWidgetShell.layout(true);
			transcriptionTaggingWidgetShell.open();
		}
	}
	
	public TagListWidget getTagListWidget() {
		return tagListWidget;
	}
	
	public TranscriptionTaggingWidget getTranscriptionTaggingWidget() {
		return transcriptionTaggingWidget;
	}
	


}
