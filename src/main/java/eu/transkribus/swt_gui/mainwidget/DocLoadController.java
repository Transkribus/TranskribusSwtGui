package eu.transkribus.swt_gui.mainwidget;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.CanvasAutoZoomMode;
import eu.transkribus.util.RecentDocsPreferences;

/**
 * @deprecated Not finished and used yet
 * Shall be used in the same way as DocSyncController or AutoSaveController to reduce number of lines in TrpMainWidget.
 * All document loading related stuff should go in here.
 */
public class DocLoadController extends AMainWidgetController {
	private static final Logger logger = LoggerFactory.getLogger(DocLoadController.class);
	
	String lastLocalDocFolder = null;

	public DocLoadController(TrpMainWidget mw) {
		super(mw);
	}
	
	public boolean loadLocalDoc(String folder) {
		return loadLocalDoc(folder, 0);
	}

	public boolean loadLocalDoc(String folder, int pageIndex) {
		if (!mw.saveTranscriptDialogOrAutosave()) {
			return false;
		}

		try {
			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Loading local document from "+folder, IProgressMonitor.UNKNOWN);
					try {
						storage.loadLocalDoc(folder, monitor);
						logger.debug("loaded local doc "+folder);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
//					} finally {
//						monitor.done();
//					}
				}
			}, "Loading local document", false);

			final boolean DISABLE_THUMB_CREATION_ON_LOAD = true;
			if (!DISABLE_THUMB_CREATION_ON_LOAD && getTrpSets().isCreateThumbs()) {
				//CreateThumbsService.createThumbForDoc(storage.getDoc(), false, updateThumbsWidgetRunnable);
			}

			storage.setCurrentPage(pageIndex);
			mw.reloadCurrentPage(true, true, CanvasAutoZoomMode.FIT_WIDTH, () -> {
				getCanvas().fitWidth();
			}, null);
			
			//store the path for the local doc
			RecentDocsPreferences.push(folder);
			mw.ui.getServerWidget().updateRecentDocs();
			
			mw.clearThumbs();
//			getCanvas().fitWidth();
			return true;
		} catch (Throwable th) {
			mw.onError("Error loading local document", "Could not load document: " + th.getMessage(), th);
			return false;
		}
	}

	public void loadLocalFolder() {
		logger.debug("loading a local folder...");
		String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose a folder with images and (optional) PAGE XML files in a subfolder 'page'", lastLocalDocFolder);
		if (fn == null)
			return;

		lastLocalDocFolder = fn;
		loadLocalDoc(fn);
	}

}
