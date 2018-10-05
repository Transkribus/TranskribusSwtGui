package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.util.EnumUtils;
import eu.transkribus.swt.portal.PortalWidget.Docking;
import eu.transkribus.swt.portal.PortalWidget.PortalWidgetListener;
import eu.transkribus.swt.portal.PortalWidget.Position;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt.util.DropDownToolItem;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt.util.databinding.DataBinder;
import eu.transkribus.swt_gui.TrpConfig;
import eu.transkribus.swt_gui.canvas.CanvasToolBarNew;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.mainwidget.TrpTabWidget.TrpTabItemSelectionListener;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.IStorageListener;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;
import eu.transkribus.swt_gui.vkeyboards.ITrpVirtualKeyboardsTabWidgetListener;
import eu.transkribus.swt_gui.vkeyboards.TrpVirtualKeyboardsTabWidget;

public class TrpMainWidgetViewListener extends SelectionAdapter implements ITrpVirtualKeyboardsTabWidgetListener, IStorageListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpMainWidgetViewListener.class);
	
	TrpMainWidget mw;
	TrpMainWidgetView ui;
	SWTCanvas canvas;
	
	public TrpMainWidgetViewListener(TrpMainWidget mainWidget) {
		this.mw = mainWidget;
		ui = mainWidget.getUi();
		canvas = ui.getCanvas();
		
		addListener();
	}

	private void addListener() {
		DataBinder db = DataBinder.get();
		Storage storage = Storage.getInstance();
				
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getUpdateIDsItem(), (e) -> { mw.updateIDs(); } );
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getClearPageItem(), (e) -> { 			
			if (DialogUtil.showYesNoDialog(ui.getShell(), "Really?", "Do you really want to clear the whole page content?")==SWT.YES) {
				ui.getCanvas().getShapeEditor().removeAll();
			} 
		});
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getDeleteSelectedBtn(), e -> {
			mw.getCanvas().getShapeEditor().removeSelected();
		});
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getSetReadingOrderRegions(), (e) -> { 
			mw.updateReadingOrderAccordingToCoordinates(false, false);
			canvas.redraw();
		} );
		
		SWTUtil.onSelectionEvent(ui.getStructureTreeWidget().getAssignGeometrically(), (e) -> { 
			mw.updateParentRelationshipAccordingToGeometricOverlap();
			canvas.redraw();
		} );
		
//		db.runOnSelection(ui.getReloadDocumentButton(), (e) -> { mw.reloadCurrentDocument(); } );
		
		SWTUtil.onSelectionEvent(ui.exportDocumentButton, (e) -> { mw.unifiedExport(); } );
		
		SWTUtil.onSelectionEvent(ui.reloadDocumentButton, (e) -> { mw.reloadCurrentDocument(); } );
						
		SWTUtil.onSelectionEvent(ui.versionsButton, (e) -> { mw.openVersionsDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.jobsButton, (e) -> { mw.openJobsDialog(); } );
				
		SWTUtil.onSelectionEvent(ui.saveDrowDown, (e) -> {
			if (e.detail != SWT.ARROW) {
				boolean withMessage = ui.saveDrowDown.getSelected()==ui.saveTranscriptWithMessageMenuItem;
				mw.saveTranscription(withMessage);
			}
		});
		
		SWTUtil.onSelectionEvent(ui.saveTranscriptToolItem, (e) -> { mw.saveTranscription(false); } );
		SWTUtil.onSelectionEvent(ui.saveTranscriptMenuItem, (e) -> { mw.saveTranscription(false); });
		
		SWTUtil.onSelectionEvent(ui.saveTranscriptWithMessageToolItem, (e) -> { mw.saveTranscription(true); } );
		SWTUtil.onSelectionEvent(ui.saveTranscriptWithMessageMenuItem, (e) -> { mw.saveTranscription(true); } );
		
		SWTUtil.onSelectionEvent(ui.saveOptionsToolItem.getToolItem(), (e) -> {
			if (e.detail != SWT.ARROW) {
				mw.saveTranscription(false);
			}
		});
		
		SWTUtil.onSelectionEvent(ui.statusCombo, (e) -> { 
			mw.changeVersionStatus(ui.statusCombo.getText(), mw.getStorage().getPage());
		});
		
		SWTUtil.onSelectionEvent(ui.autoSaveSettingsMenuItem, (e) -> { mw.openAutoSaveSetsDialog(); });

//		SWTUtil.addSelectionListener(ui.getSaveTranscriptButton(), this);
//		SWTUtil.addSelectionListener(ui.getSaveTranscriptWithMessageButton(), this);
		
		SWTUtil.onSelectionEvent(ui.getCloseDocBtn(), (e) -> { mw.closeCurrentDocument(false); } );
		
		SWTUtil.onSelectionEvent(ui.getOpenLocalFolderButton(), (e) -> { mw.loadLocalFolder(); } );
				
		SWTUtil.onSelectionEvent(ui.getLoadTranscriptInTextEditor(), (e) -> { mw.openPAGEXmlViewer(); } );
		
		SWTUtil.onSelectionEvent(ui.getServerWidget().getLoginBtn(), (e) -> {
			if (!mw.getStorage().isLoggedIn())
				mw.loginDialog("Enter your email and password");
			else
				mw.logout(false, true);
		});
		
		SWTUtil.onSelectionEvent(ui.getUploadDocsItem(), (e) -> { mw.uploadDocuments(); } );
		
		SWTUtil.onSelectionEvent(ui.getSearchBtn(), (e) -> { mw.openSearchDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.getQuickSearchButton(), (e) -> {
			mw.searchCurrentDoc();
		});
		
		SWTUtil.onEvent(ui.getQuickSearchText().getTextControl(),  SWT.Traverse,  (e) -> {
			if (e.detail == SWT.TRAVERSE_RETURN) {					
				mw.searchCurrentDoc();		
			}
		});
								
		SWTUtil.addSelectionListener(ui.getProfilesToolItem().ti, this);
		
		SWTUtil.onSelectionEvent(ui.getProfilesToolItem().ti, (e) -> {
			if (e.detail != SWT.ARROW && e.detail == DropDownToolItem.IS_DROP_DOWN_ITEM_DETAIL) {
				mw.changeProfileFromUi();
			}
		});
				
		SWTUtil.onSelectionEvent(ui.getThumbnailWidget().getCreateThumbs(), (e) -> { mw.createThumbs(storage.getDoc()); } );
						
		//db.bindBeanToWidgetSelection(TrpSettings.LOAD_THUMBS_PROPERTY, mw.getTrpSets(), ui.getThumbnailWidget().getLoadThumbs());
			
		SWTUtil.onSelectionEvent(ui.getServerWidget().getShowJobsBtn(), (e) -> { mw.openJobsDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.getServerWidget().getShowVersionsBtn(), (e) -> { mw.openVersionsDialog(); } );
				
//		SWTUtil.onSelectionEvent(ui.helpItem, (e) -> { mw.openCanvasHelpDialog(); } );
		
		SWTUtil.onSelectionEvent(ui.bugReportItem, (e) -> { mw.sendBugReport(); } );
		
		CanvasToolBarNew tb = ui.getCanvasWidget().getToolbar();
		
		SWTUtil.onSelectionEvent(tb.rotateLeftBtn, (e) -> { canvas.rotateLeft(); });
		SWTUtil.onSelectionEvent(tb.rotateRightBtn, (e) -> { canvas.rotateRight(); });
		SWTUtil.onSelectionEvent(tb.rotateLeft90Btn, (e) -> { canvas.rotate90Left(); });
		SWTUtil.onSelectionEvent(tb.rotateRight90Btn, (e) -> { canvas.rotate90Right(); });
		
		SWTUtil.onSelectionEvent(tb.translateLeftBtn, (e) -> { canvas.translateLeft(); });
		SWTUtil.onSelectionEvent(tb.translateRightBtn, (e) -> { canvas.translateRight(); });
		SWTUtil.onSelectionEvent(tb.translateUpBtn, (e) -> { canvas.translateUp(); });
		SWTUtil.onSelectionEvent(tb.translateDownBtn, (e) -> { canvas.translateDown(); });
		
		SWTUtil.onSelectionEvent(tb.fitPageItem, (e) -> { canvas.fitToPage(); });
		SWTUtil.onSelectionEvent(tb.fitWidthItem, (e) -> { canvas.fitWidth();; });
		SWTUtil.onSelectionEvent(tb.origSizeItem, (e) -> { canvas.resetTransformation(); });
		
		ui.autoSaveListMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuShown(MenuEvent e) {
				logger.debug("building autosave menu...");
				List<File> files = mw.getAutoSaveController().getAutoSavesFiles(Storage.getInstance().getPage());
				logger.debug("n-files: "+files.size());
				for (MenuItem item : ui.autoSaveListMenu.getItems()) {
				    item.dispose();
				}
				
				for (int i=0; i<files.size() && i<10; ++i) {
					File f = files.get(i);
					
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//				    System.out.println("Modified Date :- " + sdf.format(f.lastModified()));
					
					MenuItem autoSaveFileItem = new MenuItem(ui.autoSaveListMenu, SWT.PUSH);
					autoSaveFileItem.setText(sdf.format(f.lastModified()));
					autoSaveFileItem.setData(f);
					SWTUtil.onSelectionEvent(autoSaveFileItem, (evt) -> {
						MenuItem mi = (MenuItem) evt.getSource();
						logger.debug("selected autosave item for file: "+mi.getData());
						mw.getAutoSaveController().loadAutoSaveTranscriptFileIntoView((File) mi.getData());
					});
				}		
			}
			
			@Override
			public void menuHidden(MenuEvent e) {
			}
		});
		
		// tab item listener:
		ui.getTabWidget().addTabItemSelectionListener(new TrpTabItemSelectionListener() {
			@Override
			public void onTabItemSelected(CTabItem tabItem) {
				logger.debug("tab item selected: "+tabItem);
				
				ui.getCanvas().redraw();
				
				if (ui.getTabWidget().isTextTaggingItemSeleced()) {
					logger.debug("selected text tagging item!");
					ATranscriptionWidget transcriptionWidget = ui.getSelectedTranscriptionWidget();
					if (transcriptionWidget != null) {
						ui.getTaggingWidget().updateSelectedTag(transcriptionWidget.getCustomTagsForCurrentOffset());	
					}
				}
			}
		});
		
		ui.getPortalWidget().addPortalWidgetListener(new PortalWidgetListener() {
			@Override
			public void onDockingChanged(String widgetType, Composite widget, Position pos, Docking docking) {
				logger.debug("onDockingChanged: widgetType = "+widgetType+" pos = "+pos+" docking = "+docking);
				
				if (StringUtils.equals(widgetType, TrpMainWidgetView.MENU_WIDGET_TYPE)) {
					TrpConfig.getTrpSettings().setMenuViewDockingState(docking);
				}
				else if (StringUtils.equals(widgetType, TrpMainWidgetView.TRANSCRIPTION_WIDGET_TYPE)) {
					logger.debug("setting tr view docking state!");
					TrpConfig.getTrpSettings().setTranscriptionViewDockingState(docking);
				}
				
				canvas.fitWidth();
			}
			
			@Override
			public void onPositionChanged(String widgetType, Composite widget, Position pos, Docking docking) {
				logger.debug("onPositionChanged: widgetType = "+widgetType+" pos = "+pos+" docking = "+docking);
				
				if (StringUtils.equals(widgetType, TrpMainWidgetView.TRANSCRIPTION_WIDGET_TYPE)) {
					TrpConfig.getTrpSettings().setTranscriptionViewPosition(pos);
				}
				
				canvas.fitWidth();
			}
		});
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override public void onVirtualKeyPressed(TrpVirtualKeyboardsTabWidget w, char c, String description) {
		mw.insertTextOnSelectedTranscriptionWidget(c);
	}
	
}
