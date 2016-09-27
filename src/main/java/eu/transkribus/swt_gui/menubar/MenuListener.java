package eu.transkribus.swt_gui.menubar;

import java.util.Locale;

import org.docx4j.model.fields.merge.MailMergerWithNext;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;

import eu.transkribus.swt_gui.canvas.CanvasSettings;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;

public class MenuListener implements SelectionListener {
	TrpMainWidgetView view;
	TrpMenuBar menuBar;
	TrpSettings viewSets;
	CanvasSettings canvasSets;
	
	public MenuListener(TrpMainWidgetView view) {
		this.view = view;
		menuBar = view.getTrpMenuBar();
		viewSets = view.getTrpSets();
		canvasSets = view.getCanvas().getSettings();
		
		menuBar.showAllMenuItem.addSelectionListener(this);
		menuBar.hideAllMenuItem.addSelectionListener(this);
		menuBar.viewSettingsMenuItem.addSelectionListener(this);
		menuBar.proxySettingsMenuItem.addSelectionListener(this);
		
		if (menuBar.languageMenu!=null)
		for (MenuItem mi : menuBar.languageMenu.getItems()) {
			mi.addSelectionListener(this);
		}
	}

	@Override public void widgetSelected(SelectionEvent e) {
		Object s = e.getSource();
		if (s == menuBar.showAllMenuItem) {
			viewSets.setShowAll(true);
		}
		else if (s == menuBar.hideAllMenuItem) {
			viewSets.setShowAll(false);
		}
		else if (s == menuBar.viewSettingsMenuItem) {
			view.openViewSetsDialog();
		}
		else if (s == menuBar.proxySettingsMenuItem) {
			view.openProxySetsDialog();
		}
		else if (s instanceof MenuItem && ((MenuItem)s).getData() instanceof Locale) {
			Locale l = (Locale) ((MenuItem)s).getData();
			TrpMainWidget.getInstance().setLocale(l);
		}
	}

	@Override public void widgetDefaultSelected(SelectionEvent e) {
	}

}
