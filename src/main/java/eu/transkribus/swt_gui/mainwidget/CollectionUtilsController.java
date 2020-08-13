package eu.transkribus.swt_gui.mainwidget;

import java.io.FileWriter;
import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.util.CSVUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.pagination_tables.RecycleBinDialog;
import static eu.transkribus.core.util.CoreUtils.*;

/**
 * All stuff in regard with a loaded collection currently in TrpMainWidget should go in here in the future...
 */
public class CollectionUtilsController extends AMainWidgetController {
	private static final Logger logger = LoggerFactory.getLogger(CollectionUtilsController.class);
	
	RecycleBinDialog recycleBinDiag;

	public CollectionUtilsController(TrpMainWidget mw) {
		super(mw);
		
	}

	public void exportCurrentCollectionStats() {
		if (CollectionUtils.isEmpty(storage.getDocList())) {
			return;
		}
		
		String fn = DialogUtil.showSaveDialog(getShell(), "Specify the name of the file", null, new String[] { "*.csv" });
		if (StringUtils.isEmpty(fn)) {
			return;
		}
		
		logger.debug("writing doclist csv to "+fn);
		
		try (FileWriter writer = new FileWriter(fn)) {
			CSVUtils.writeLineWoEncoding(writer, "sep="+CSVUtils.DEFAULT_SEPARATOR);
			CSVUtils.writeLine(writer, "ID", "Title", "Pages", "Uploader", "Uploaded", "Collections");
			for (TrpDocMetadata md : storage.getDocList()) {
				CSVUtils.writeLine(writer,  ""+md.getDocId(),
											strOrEmpty(md.getTitle()),
											strOrEmpty(md.getNrOfPages()),
											strOrEmpty(md.getUploader()), 
											strOrEmpty(md.getUploadTime()),
											md.getColString());
			}
			DialogUtil.showInfoMessageBox(getShell(), "Success", "Successfully exported doclist to: "+fn);
		} catch (Exception e) {
			mw.onError("Error saving CSV", e.getMessage(), e);
		}
		
	}
	
	

}
