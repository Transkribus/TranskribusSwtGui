package eu.transkribus.swt_gui.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.AImagePreviewListViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.ImgLoader;
import eu.transkribus.swt.util.SWTUtil;

public class DocPageViewer extends AImagePreviewListViewer<TrpPage> {
	private final static Logger logger = LoggerFactory.getLogger(DocPageViewer.class);
	
	public static final String PAGE_NR_COL = "Page";
	public static final String ID_COL = "ID";
	public static final String FN_COL = "Filename";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(PAGE_NR_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FN_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
	};
	
	public static final ColumnConfig[] COLS_2 = new ColumnConfig[] {
			new ColumnConfig(PAGE_NR_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
//			new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(FN_COL, 200, false, DefaultTableColumnViewerSorter.ASC),
	};	

	public DocPageViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages) {
		this(parent, style, showUpDownBtns, withCheckboxes, renderOriginalImages, true, true);
	}
	
	public DocPageViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages, 
			boolean showPreview, boolean useIdColumn) {
		super(parent, style, useIdColumn ? COLS : COLS_2, null, showUpDownBtns, withCheckboxes, showPreview);
		this.renderOriginalImages = renderOriginalImages;
		setLabelProvider(new ITableLabelProvider() {
			@Override public void removeListener(ILabelProviderListener listener) {
			}
			
			@Override public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			
			@Override public void dispose() {
			}
			
			@Override public void addListener(ILabelProviderListener listener) {
			}
			
			@Override public String getColumnText(Object element, int columnIndex) {
				String cn = tv.getColumn(columnIndex).getText();
//				String cn = COLS[columnIndex].name;
				TrpPage p = (TrpPage) element;
				if (cn.equals(PAGE_NR_COL)) {
					return ""+p.getPageNr();
				}
				else if (cn.equals(ID_COL)) {
//					if (doc.isLocalDoc())
//						return p.getImgFileName();
					return p.getKey();
				}
				else if (cn.equals(FN_COL)) {
					// display basename of image only if selection is allowed 
					return withCheckboxes? FilenameUtils.getBaseName(p.getImgFileName()) : p.getImgFileName() ;
				}
				
				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		});
		
		this.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(DisposeEvent e) {
				SWTUtil.dispose(selectedImage);
			}
		});		
	}
	
	@Override protected Image loadImageForData(TrpPage data) throws IOException {
		URL url = renderOriginalImages ? data.getUrl() : data.getThumbUrl();
		
		if (!renderOriginalImages) { // if local file and thumb does not exist => use original image!
			File file = FileUtils.toFile(url);
			if (file!=null && !file.exists()) {
				logger.debug("using original image url for preview: "+data.getUrl());
				url = data.getUrl();
			}
		}
		
		Image img = ImgLoader.load(url);
		return img;
	}	
}
