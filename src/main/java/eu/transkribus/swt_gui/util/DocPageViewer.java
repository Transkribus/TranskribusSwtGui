package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.util.APreviewListViewer;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.ImgLoader;

public class DocPageViewer extends APreviewListViewer<TrpPage> {
	private final static Logger logger = LoggerFactory.getLogger(DocPageViewer.class);
	
	public static final String PAGE_NR_COL = "Page";
	public static final String ID_COL = "ID";
	public static final String FN_COL = "Filename";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(PAGE_NR_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(FN_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};

	public DocPageViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages) {
		super(parent, style, COLS, new ITableLabelProvider() {
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
				String cn = COLS[columnIndex].name;
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
					return p.getImgFileName();
				}
				
				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}, showUpDownBtns, withCheckboxes, renderOriginalImages);
	}
	
	@Override protected Image loadImageForData(TrpPage data) throws IOException {
		URL url = renderOriginalImages ? data.getUrl() : data.getThumbUrl();
		Image img = ImgLoader.load(url);
		return img;
	}
}
