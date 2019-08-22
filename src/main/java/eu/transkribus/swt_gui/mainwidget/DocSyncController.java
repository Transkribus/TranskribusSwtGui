package eu.transkribus.swt_gui.mainwidget;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.io.LocalDocReader.DocLoadConfig;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.core.model.beans.pagecontent.PcGtsType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.progress.ProgressBarDialog;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.dialogs.DocSyncDialog;
import eu.transkribus.swt_gui.dialogs.DocSyncWithFilesDialog;
import eu.transkribus.swt_gui.dialogs.DocSyncWithTxtFilesDialog;
import eu.transkribus.swt_gui.exceptions.CustomDataException;
import eu.transkribus.util.CheckedConsumer;

public class DocSyncController extends AMainWidgetController {
	private static final Logger logger = LoggerFactory.getLogger(DocSyncController.class);
	
	String lastTxtFileSyncFolder = null;
	String lastPageFileSyncFolder = null;
	
	public DocSyncController(TrpMainWidget mw) {
		super(mw);
	}
	
	/**
	 * Syncs local PAGE-XML files with the currently loaded (remote) document
	 */
	public void syncPAGEFilesWithLoadedDoc() {
		int collId = storage.getCurrentDocumentCollectionId();
		TrpServerConn conn = storage.getConnection();
		String typeOfFiles = "PAGE-XML";
		syncFilesWithLoadedDoc(new  DocSyncWithFilesDialog(getShell(), typeOfFiles), match -> {
			TrpPage p = match.getLeft();
			File f = match.getRight();

			if (!p.isLocalFile()) {
				PcGtsType pcGtsType = PageXmlUtils.unmarshal(f);
				String toolname = "Synced with PAGE-XML";
				String note = "TRP: Synced from local PAGE-XML file: '"+f.getName()+"'";
				conn.updateTranscript(collId, p.getDocId(), p.getPageNr(), EditStatus.IN_PROGRESS,
						pcGtsType, p.getPageId(), toolname, note);				
			}
			else {
				logger.error("Syncing PAGE-XML files with local docs does not make sense - you shouldn't get here anyway...");
			}
		}, new String[] {".xml"}, typeOfFiles, true);
		
	}
	
	/**
	 * Syncs local plaintext files with the currently loaded document
	 */
	public void syncTxtFilesWithLoadedDoc() {
		int collId = storage.getCurrentDocumentCollectionId();
		TrpServerConn conn = storage.getConnection();
		String typeOfFiles = "text";
		DocSyncWithTxtFilesDialog d = new  DocSyncWithTxtFilesDialog(getShell());
		syncFilesWithLoadedDoc(d, match -> {
			TrpPage p = match.getLeft();
			File f = match.getRight();
			
			boolean useExistingData = d.getData()==null ? false : (boolean) d.getData();
			logger.debug("useExistingData = "+useExistingData);

			String text = LocalDocReader.readTextFromFile(f);
			if (p.isLocalFile()) {
				PcGtsType pcGtsType = PageXmlUtils.unmarshal(p.getCurrentTranscript().getFile());
				PageXmlUtils.applyTextToLines((TrpPageType) pcGtsType.getPage(), text);
				PageXmlUtils.marshalToFile(pcGtsType, p.getCurrentTranscript().getFile());
			} else {
				String toolname = "Synced with TXT";
				String note = "Synced from local plaintext file: '"+f.getName()+"'";
				conn.assignPlainTextToPage(collId, p.getDocId(), p.getPageNr(), EditStatus.IN_PROGRESS, text,
						p.getPageId(), useExistingData, toolname, note);
			}
		}, new String[] {".txt"}, typeOfFiles, false);
	}
	
	private void syncFilesWithLoadedDoc(DocSyncWithFilesDialog d, CheckedConsumer<Pair<TrpPage, File>> c, String[] exts, String typeOfFiles, boolean requiresRemoteDoc) {
		try {
			logger.debug("syncing txt files with loaded doc!");
			
			if (!storage.isDocLoaded()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No document loaded!");
				return;
			}
			
			if (requiresRemoteDoc && !storage.isRemoteDoc()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No remote document loaded!");
				return;
			}

//			if (!storage.isLoggedIn() || !storage.isRemoteDoc()) {
//				DialogUtil.showErrorMessageBox(getShell(), "Error", "No remote document loaded!");
//				return;
//			}

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose the folder with the "+typeOfFiles+" files", lastTxtFileSyncFolder);
			if (fn == null) {
				return;
			}

			// store current location 
			lastTxtFileSyncFolder = fn;
			
			List<File> files = CoreUtils.listFilesReturnFiles(1, fn, exts, false);
			if (CoreUtils.isEmpty(files)) {
				DialogUtil.showErrorMessageBox(getShell(), "No files found", "No "+typeOfFiles+" files found in this folder!");
				return;
			}
			
//			File[] files = new File(fn).listFiles((dir,name) -> {
//				return exts==null ? true : Arrays.asList(exts).contains(FilenameUtils.getExtension(name).toLowerCase());
//			});

//			final DocSyncWithFilesDialog d = new DocSyncWithFilesDialog(getShell(), storage.getDoc(), files);
			d.setData(storage.getDoc(), files);
			if (d.open() != Dialog.OK) {
				return;
			}
			if (CoreUtils.isEmpty(d.getMatches())) {
				DialogUtil.showErrorMessageBox(getShell(), "No files to sync", "No "+typeOfFiles+" files to sync!");
				return;
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						List<Pair<TrpPage, File>> errors = storage.syncFilesWithDoc(d.getMatches(), c, typeOfFiles, monitor);
						if (errors != null && errors.size()>0) {
							String errorsStr = errors.stream().map(m -> "Page "+m.getLeft().getPageNr()+" <-- "+m.getRight().getName()).collect(Collectors.joining("\n"));
							throw new CustomDataException(errors.size()+" files could not be matched", errorsStr);
						}
					} 
					catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Syncing", true);

			mw.reloadCurrentDocument();
		} 
		catch (CustomDataException e) {
			DialogUtil.showErrorMessageBox(getShell(), "Errors occured", e.getMessage()+"\n"+(String) e.getData());	
		}
		catch (Throwable e) {
			mw.onError("Sync error", "Error during sync of remote document", e);
		}
	}	
	
	/**
	 * @deprecated use {@link #syncPAGEFilesWithLoadedDoc()}
	 */
	public void syncWithLocalDoc() {
		try {
			logger.debug("syncing with local doc!");

			if (!storage.isLoggedIn() || !storage.isRemoteDoc()) {
				DialogUtil.showErrorMessageBox(getShell(), "Error", "No remote document loaded!");
				return;
			}

			String fn = DialogUtil.showOpenFolderDialog(getShell(), "Choose the 'page' folder with the page XMLs", lastPageFileSyncFolder);
			if (fn == null)
				return;

			// store current location 
			lastPageFileSyncFolder = fn;
			
			// enable sync mode to allow for local docs without images
			DocLoadConfig config = new DocLoadConfig();
			config.setEnableSyncWithoutImages(true);
			config.setDimensionMapFromDoc(storage.getDoc());
			TrpDoc localDoc = LocalDocReader.load(fn, config);

			final DocSyncDialog d = new DocSyncDialog(getShell(), storage.getDoc(), localDoc);
			if (d.open() != Dialog.OK) {
				return;
			}

			ProgressBarDialog.open(getShell(), new IRunnableWithProgress() {
				@Override public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						storage.syncDocPages(d.getSourcePages(), d.getChecked(), monitor);
					} catch (Exception e) {
						throw new InvocationTargetException(e, e.getMessage());
					}
				}
			}, "Syncing", true);

			mw.reloadCurrentDocument();
		} catch (Throwable e) {
			mw.onError("Sync error", "Error during sync of remote document", e);
		}

	}
	
	public void movePagesByFilelist() {
		try {
			if (!storage.isRemoteDoc()) {
				DialogUtil.showErrorMessageBox(getShell(), "No remote doc loaded", "Please load a remote document first!");
				return;
			}
			
			String filename = DialogUtil.showOpenFileDialog(getShell(), "Select txt file with image filenames of document in desired order", null, new String[] {"*.txt"});
			logger.debug("filename = "+filename);
			if (filename != null) {
				storage.getConnection().moveImagesByNames(storage.getCollId(), storage.getDocId(), new File(filename));
				
				mw.reloadCurrentDocument();
			}
		}
		catch (Exception e) {
			mw.onError("Error moving pages by image file list", e.getMessage(), e, true, false);
		}
	}

}
