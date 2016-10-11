package eu.transkribus.swt_gui.mainwidget;

import java.util.Locale;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.swt.util.databinding.DataBinder;
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
				db.runOnSelection(mi, (e) -> {
					Locale l = (Locale) mi.getData();
					TrpMainWidget.getInstance().setLocale(l); 
				});
			}
		}
		
		db.runOnSelection(mb.showAllMenuItem, (e) -> { trpSets.setShowAll(true); });
		
		db.runOnSelection(mb.hideAllMenuItem, (e) -> { trpSets.setShowAll(false); });
		
		db.runOnSelection(mb.viewSettingsMenuItem, (e) -> { mw.openViewSetsDialog(); });
		
		db.runOnSelection(mb.proxySettingsMenuItem, (e) -> { mw.openProxySetsDialog(); });
			
		db.runOnSelection(mb.openLocalDocItem, (e) -> { mw.loadLocalFolder(); });
		
		db.runOnSelection(mb.openLocalPageFileItem, (e) -> { mw.loadLocalPageXmlFile(); });
		
		db.runOnSelection(mb.deletePageMenuItem, (e) -> { mw.deletePage(); });
		
		db.runOnSelection(mb.manageCollectionsItem, (e) -> { mw.getUi().getServerWidget().openCollectionsManagerWidget(); });
		
		db.runOnSelection(mb.userActivityItem, (e) -> { mw.getUi().getServerWidget().openActivityDialog(); });
		
		db.runOnSelection(mb.uploadItem, (e) -> { mw.uploadDocuments(); });
		
		db.runOnSelection(mb.exportItem, (e) -> { mw.unifiedExport(); });
		
		db.runOnSelection(mb.syncWordsWithLinesMenuItem, (e) -> { mw.syncTextOfDocFromWordsToLinesAndRegions(); });
		
		db.runOnSelection(mb.saveTranscriptionToNewFileMenuItem, (e) -> { mw.saveTranscriptionToNewFile(); });
		
		db.runOnSelection(mb.saveTranscriptionMenuItem, (e) -> { mw.saveTranscription(false); });
		
		db.runOnSelection(mb.updateMenuItem, (e) -> { mw.checkForUpdates(); });
		
		db.runOnSelection(mb.installMenuItem, (e) -> { mw.installSpecificVersion(); });
		
		db.runOnSelection(mb.tipsOfTheDayMenuItem, (e) -> { mw.showTipsOfTheDay(); });
		
		db.runOnSelection(mb.aboutMenuIItem, (e) -> { mw.showAboutDialog(); });
		
		db.runOnSelection(mb.replaceImageItem, (e) -> { mw.replacePageImg(); });
		
		db.runOnSelection(mb.bugReportItem, (e) -> { mw.sendBugReport(); });
		
		db.runOnSelection(mb.exitItem, (e) -> { mw.getShell().close(); });	
	}

	@Override public void widgetSelected(SelectionEvent e) {
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

}
