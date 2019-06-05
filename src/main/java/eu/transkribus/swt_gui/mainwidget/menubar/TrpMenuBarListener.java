package eu.transkribus.swt_gui.mainwidget.menubar;

import java.util.Locale;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import junit.framework.Assert;

public class TrpMenuBarListener implements SelectionListener {
	TrpMainWidget mw;
	TrpMenuBar mb;
	
	public TrpMenuBarListener(TrpMainWidget mw) {
		Assert.assertNotNull("TrpMainWidget cannot be null!, mw");
		
		this.mw = mw;
		mb = mw.getUi().getTrpMenuBar();
					
		addMenuBindings();
	}
	
	private void addMenuBindings() {
		TrpSettings trpSets = mw.getTrpSets();
		DataBinder db = DataBinder.get();
		
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_PRINTSPACE_PROPERTY, trpSets, mb.showPrintspaceMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_TEXT_REGIONS_PROPERTY, trpSets, mb.showRegionsMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_LINES_PROPERTY, trpSets, mb.showLinesMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_BASELINES_PROPERTY, trpSets, mb.showBaselinesMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.SHOW_WORDS_PROPERTY, trpSets, mb.showWordsMenuItem);
		db.bindBeanToWidgetSelection(TrpSettings.CREATE_THUMBS_PROPERTY, trpSets, mb.createThumbsMenuItem);
		
		if (mb.languageMenu != null) {
			for (MenuItem mi : mb.languageMenu.m.getItems()) {
				SWTUtil.onSelectionEvent(mi, (e) -> {
					Locale l = (Locale) mi.getData();
					TrpMainWidget.getInstance().setLocale(l); 
				});
			}
		}
		
		SWTUtil.onSelectionEvent(mb.showAllMenuItem, (e) -> { trpSets.setShowAll(true); });
		
		SWTUtil.onSelectionEvent(mb.hideAllMenuItem, (e) -> { trpSets.setShowAll(false); });
		
		SWTUtil.onSelectionEvent(mb.viewSettingsMenuItem, (e) -> { mw.openViewSetsDialog(); });
		
		SWTUtil.onSelectionEvent(mb.proxySettingsMenuItem, (e) -> { mw.openProxySetsDialog(); });
		
		SWTUtil.onSelectionEvent(mb.autoSaveSettingsMenuItem, (e) -> { mw.openAutoSaveSetsDialog(); });
			
		SWTUtil.onSelectionEvent(mb.openLocalDocItem, (e) -> { mw.loadLocalFolder(); });
		
		SWTUtil.onSelectionEvent(mb.openLocalPageFileItem, (e) -> { mw.loadLocalPageXmlFile(); });
		
		SWTUtil.onSelectionEvent(mb.deletePageMenuItem, (e) -> { mw.deletePage(); });
		
		//new: add one or more pages
		SWTUtil.onSelectionEvent(mb.addPageMenuItem, (e) -> { mw.addSeveralPages2Doc(); });
		//SWTUtil.onSelectionEvent(mb.addPageMenuItem, (e) -> { mw.addPage(); });
		
		//SWTUtil.onSelectionEvent(mb.manageCollectionsItem, (e) -> { mw.openCollectionManagerDialog(); });
		
		SWTUtil.onSelectionEvent(mb.userActivityItem, (e) -> { mw.openActivityDialog(); });
		
		SWTUtil.onSelectionEvent(mb.uploadItem, (e) -> { mw.uploadDocuments(); });
		
		SWTUtil.onSelectionEvent(mb.exportItem, (e) -> { mw.unifiedExport(); });
		
		SWTUtil.onSelectionEvent(mb.syncXmlItem, (e) -> {mw.syncWithLocalDoc();} );
		
		SWTUtil.onSelectionEvent(mb.syncTextItem, (e) -> {mw.syncWithLocalDoc();} );

		SWTUtil.onSelectionEvent(mb.syncWordsWithLinesMenuItem, (e) -> { mw.syncTextOfDocFromWordsToLinesAndRegions(); });
		
		SWTUtil.onSelectionEvent(mb.saveTranscriptionToNewFileMenuItem, (e) -> { mw.saveTranscriptionToNewFile(); });
		
		SWTUtil.onSelectionEvent(mb.saveTranscriptionMenuItem, (e) -> { mw.saveTranscription(false); });
		
		SWTUtil.onSelectionEvent(mb.updateMenuItem, (e) -> { mw.checkForUpdates(); });
		
		SWTUtil.onSelectionEvent(mb.installMenuItem, (e) -> { mw.installSpecificVersion(); });
		
		SWTUtil.onSelectionEvent(mb.aboutMenuItem, (e) -> { mw.openAboutDialog(); });
		
		SWTUtil.onSelectionEvent(mb.helpMenuItem, (e) -> { mw.openHowToGuides(); });
		
		SWTUtil.onSelectionEvent(mb.changelogMenuItem,  (e) -> {mw.openChangeLogDialog(true); }); 
		
		SWTUtil.onSelectionEvent(mb.replaceImageItem, (e) -> { mw.replacePageImg(); });
		
		SWTUtil.onSelectionEvent(mb.bugReportItem, (e) -> { mw.sendBugReport(); });
		
		SWTUtil.onSelectionEvent(mb.exitItem, (e) -> { mw.getShell().close(); });	
		
		SWTUtil.onSelectionEvent(mb.movePagesByFilelistItem, (e) -> { mw.movePagesByFilelist(); });
	}

	@Override public void widgetSelected(SelectionEvent e) {
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

}
