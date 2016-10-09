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
				db.runOnSelection(mi, () -> {
					Locale l = (Locale) mi.getData();
					TrpMainWidget.getInstance().setLocale(l); 
				});
			}
		}
		
		db.runOnSelection(mb.showAllMenuItem, () -> { trpSets.setShowAll(true); });
		
		db.runOnSelection(mb.hideAllMenuItem, () -> { trpSets.setShowAll(false); });
		
		db.runOnSelection(mb.viewSettingsMenuItem, () -> { mw.openViewSetsDialog(); });
		
		db.runOnSelection(mb.proxySettingsMenuItem, () -> { mw.openProxySetsDialog(); });
			
		db.runOnSelection(mb.openLocalDocItem, () -> { mw.loadLocalFolder(); });
		
		db.runOnSelection(mb.openLocalPageFileItem, () -> { mw.loadLocalPageXmlFile(); });
		
		db.runOnSelection(mb.deletePageMenuItem, () -> { mw.deletePage(); });
		
		db.runOnSelection(mb.manageCollectionsItem, () -> { mw.getUi().getServerWidget().openCollectionsManagerWidget(); });
		
		db.runOnSelection(mb.userActivityItem, () -> { mw.getUi().getServerWidget().openActivityDialog(); });
		
		db.runOnSelection(mb.uploadItem, () -> { mw.uploadDocuments(); });
		
		db.runOnSelection(mb.exportItem, () -> { mw.unifiedExport(); });
		
		db.runOnSelection(mb.syncWordsWithLinesMenuItem, () -> { mw.syncTextOfDocFromWordsToLinesAndRegions(); });
		
		db.runOnSelection(mb.saveTranscriptionToNewFileMenuItem, () -> { mw.saveTranscriptionToNewFile(); });
		
		db.runOnSelection(mb.saveTranscriptionMenuItem, () -> { mw.saveTranscription(false); });
		
		db.runOnSelection(mb.updateMenuItem, () -> { mw.checkForUpdates(); });
		
		db.runOnSelection(mb.installMenuItem, () -> { mw.installSpecificVersion(); });
		
		db.runOnSelection(mb.tipsOfTheDayMenuItem, () -> { mw.showTipsOfTheDay(); });
		
		db.runOnSelection(mb.aboutMenuIItem, () -> { mw.showAboutDialog(); });
		
		db.runOnSelection(mb.replaceImageItem, () -> { mw.replacePageImg(); });
		
		db.runOnSelection(mb.bugReportItem, () -> { mw.sendBugReport(); });
		
		db.runOnSelection(mb.exitItem, () -> { mw.getShell().close(); });	
	}

	@Override public void widgetSelected(SelectionEvent e) {
//		Object s = e.getSource();

	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

}
