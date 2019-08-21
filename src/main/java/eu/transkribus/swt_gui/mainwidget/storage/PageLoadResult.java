package eu.transkribus.swt_gui.mainwidget.storage;

import java.util.List;

import org.dea.fimagestore.core.beans.ImageMetadata;

import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_gui.canvas.CanvasImage;

public class PageLoadResult {
	public TrpDoc doc;
	public TrpPage page;
	public CanvasImage image;
	public ImageMetadata imgMd;
	public List<TrpTranscriptMetadata> metadataList;
}