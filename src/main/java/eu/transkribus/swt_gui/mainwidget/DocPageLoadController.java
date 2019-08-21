package eu.transkribus.swt_gui.mainwidget;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.AsyncExecutor.AsyncCallback;
import eu.transkribus.swt_gui.canvas.CanvasAutoZoomMode;
import eu.transkribus.swt_gui.mainwidget.storage.PageLoadResult;

public class DocPageLoadController extends AMainWidgetController {
	private static final Logger logger = LoggerFactory.getLogger(DocPageLoadController.class);

	public DocPageLoadController(TrpMainWidget mw) {
		super(mw);
	}

	public Future<PageLoadResult> reloadCurrentPageAsync(boolean force, boolean reloadTranscript, CanvasAutoZoomMode zoomMode,
			Runnable onSuccess, Runnable onError) {
		if (!force && !mw.saveTranscriptDialogOrAutosave()) {
			return null;
		}

		try {
			logger.info("loading page: " + storage.getPage());
			mw.clearCurrentPage();

			final int colId = storage.getCurrentDocumentCollectionId();
			final String fileType = mw.getSelectedImageFileType();
			logger.debug("selected img filetype = " + fileType);

			final Runnable uiUpdates = () -> {
				logger.debug("finallyUpdateUiRunnable");
				mw.updateVersionStatus();
				mw.updatePageLock();
				mw.ui.getCanvasWidget().updateUiStuff();
				mw.updateSegmentationEditStatus();
				getCanvas().updateEditors();
				mw.updatePageRelatedMetadata();
				mw.updateToolBars();
				mw.updatePageInfo();
			};

			AsyncCallback<PageLoadResult> callback = new AsyncCallback<PageLoadResult>() {
				@Override
				public void onSuccess(PageLoadResult result) {
					logger.debug("onSuccess: " + result);

					if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
						mw.getScene().setMainImage(storage.getCurrentImage());
					}

					mw.getScene().setCanvasAutoZoomMode(zoomMode);

					logger.debug("before reloading transcript, reloadTranscript = " + reloadTranscript
							+ ", nTranscripts = " + storage.getNTranscripts());
					if (reloadTranscript && storage.getNTranscripts() > 0) {
						logger.debug("reloading transcript!");
						storage.setLatestTranscriptAsCurrent();
						reloadCurrentTranscriptAsync(false, true, 
								() -> {
									CoreUtils.run(uiUpdates);
									CoreUtils.run(onSuccess);
								}, 
								() -> {
									CoreUtils.run(uiUpdates);
									CoreUtils.run(onError);
								});
//							mw.updateVersionStatus();
					}
					else {
						CoreUtils.run(uiUpdates);
						CoreUtils.run(onSuccess);
					}
				}

				@Override
				public void onError(Throwable error) {
					String msg = "Could not load page " + (storage.getPageIndex() + 1);
					mw.onError("Error loading page", msg, error);

					CoreUtils.run(uiUpdates);
					CoreUtils.run(onError);
				}
			};

			return storage.reloadCurrentPageAsync(colId, fileType, callback);
		} catch (Throwable th) {
			String msg = "Could not load page " + (storage.getPageIndex() + 1);
			mw.onError("Error loading page", msg, th);

			return null;
		}
	}

	/**
	 * Reload the current page that is set in {@link #storage}.
	 * 
	 * @param force            Forces a reload of the page without asking to save
	 *                         changes
	 * @param reloadTranscript Also reload the current transcript?
	 * @param zoomMode         Specifies the zoom mode this page should be set to,
	 *                         if null the current transformation is kept.
	 * @return True if page was reloaded, false otherwise
	 */
	public boolean reloadCurrentPage(boolean force, boolean reloadTranscript, CanvasAutoZoomMode zoomMode, Runnable onSuccess, Runnable onError) {
		if (!force && !mw.saveTranscriptDialogOrAutosave()) {
			return false;
		}

		try {
			logger.info("loading page: " + storage.getPage());
			mw.clearCurrentPage();

			final int colId = storage.getCurrentDocumentCollectionId();
			final String fileType = mw.getSelectedImageFileType();
			logger.debug("selected img filetype = " + fileType);

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						logger.debug("Runnable reloads page with index = " + (storage.getPageIndex() + 1));
						monitor.beginTask("Loading page " + (storage.getPageIndex() + 1), IProgressMonitor.UNKNOWN);
						storage.reloadCurrentPage(colId, fileType);
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			}, "Loading page", false);

			if (storage.isPageLoaded() && storage.getCurrentImage() != null) {
				mw.getScene().setMainImage(storage.getCurrentImage());
			}

			mw.getScene().setCanvasAutoZoomMode(zoomMode);

			if (reloadTranscript && storage.getNTranscripts() > 0) {
				storage.setLatestTranscriptAsCurrent();
				reloadCurrentTranscript(false, true, onSuccess, onError);
				mw.updateVersionStatus();
			}
			else {
				CoreUtils.run(onSuccess);
			}

			return true;
		} catch (Throwable th) {
			String msg = "Could not load page " + (storage.getPageIndex() + 1);
			mw.onError("Error loading page", msg, th);
			CoreUtils.run(onError);

			return false;
		} finally {
			mw.updatePageLock();
			mw.ui.getCanvasWidget().updateUiStuff();
			mw.updateSegmentationEditStatus();
			getCanvas().updateEditors();
			mw.updatePageRelatedMetadata();
			mw.updateToolBars();
			mw.updatePageInfo();
		}
	}

	public Future<TrpPageType> reloadCurrentTranscriptAsync(boolean tryLocalReload, boolean force, final Runnable onSuccess,
			final Runnable onError) {
		if (!force && !mw.saveTranscriptDialogOrAutosave()) {
			return null;
		}

		// LOAD STRUCT ELEMENTS FROM TRANSCRIPTS
		try {
			// save transcript if edited:
			// clearTranscriptFromView();
			logger.info(
					"loading transcript: " + storage.getTranscript().getMd() + " tryLocalReload: " + tryLocalReload);
			canvas.getScene().selectObject(null, true, false); // to prevent freeze of progress dialog

			AsyncCallback<TrpPageType> callback = new AsyncCallback<TrpPageType>() {
				@Override
				public void onSuccess(TrpPageType result) {
					logger.debug("onSuccess: " + result);
					logger.debug("CHANGED: " + storage.getTranscript().getPage().isEdited());
					mw.loadJAXBTranscriptIntoView(storage.getTranscript());

//						ui.taggingWidget.updateAvailableTags();
					mw.updateSelectedTranscriptionWidgetData();
					canvas.getScene().updateSegmentationViewSettings();
					logger.debug("loaded transcript - edited = " + storage.isTranscriptEdited());

					CoreUtils.run(onSuccess);
				}

				@Override
				public void onError(Throwable error) {
					String msg = "Could not load transcript for page " + (storage.getPageIndex() + 1);
					mw.onError("Error loading transcript", msg, error);
					mw.clearTranscriptFromView();

					CoreUtils.run(onError);
				}
			};

			if (!tryLocalReload || !storage.hasTranscript()) {
				logger.debug("reloading transcript async from storage!");
				return storage.reloadTranscriptAsync(callback);
			} else {
				callback.onSuccess(storage.getTranscript().getPage());
				return null;
			}
		} catch (Throwable th) {
			String msg = "Could not load transcript for page " + (storage.getPageIndex() + 1);
			mw.onError("Error loading transcript", msg, th);
			mw.clearTranscriptFromView();
			return null;
		}
	}

	public boolean reloadCurrentTranscript(boolean tryLocalReload, boolean force, Runnable onSuccess, Runnable onError) {
		if (!force && !mw.saveTranscriptDialogOrAutosave()) {
			return false;
		}

		// LOAD STRUCT ELEMENTS FROM TRANSCRIPTS
		try {
			// save transcript if edited:
			// clearTranscriptFromView();
			logger.info(
					"loading transcript: " + storage.getTranscript().getMd() + " tryLocalReload: " + tryLocalReload);
			canvas.getScene().selectObject(null, true, false); // security
																// measure due
																// to mysterious
																// bug leading
																// to freeze of
																// progress
																// dialog
			if (!tryLocalReload || !storage.hasTranscript()) {
				ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("Loading transcription", IProgressMonitor.UNKNOWN);
							storage.reloadTranscript();
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				}, "Loading transcription", false);
				// storage.reloadTranscript();
				// add observers for transcript:
				// addTranscriptObserver();
			}
			// logger.debug("CHANGED: "+storage.getTranscript().getPage().isEdited());
			mw.loadJAXBTranscriptIntoView(storage.getTranscript());

//			ui.taggingWidget.updateAvailableTags();
			mw.updateSelectedTranscriptionWidgetData();
			canvas.getScene().updateSegmentationViewSettings();

			logger.debug("loaded transcript - edited = " + storage.isTranscriptEdited());

			CoreUtils.run(onSuccess);
			return true;
		} catch (Throwable th) {
			String msg = "Could not load transcript for page " + (storage.getPageIndex() + 1);
			mw.onError("Error loading transcript", msg, th);
			mw.clearTranscriptFromView();

			CoreUtils.run(onError);
			return false;
		}
	}

}
