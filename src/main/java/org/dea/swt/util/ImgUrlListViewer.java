package org.dea.swt.util;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.mytableviewer.MyTableViewer;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImgUrlListViewer extends APreviewListViewer<URL> {
	private final static Logger logger = LoggerFactory.getLogger(ImgUrlListViewer.class);
	
	MyTableViewer tv;
	Label imgLabel;
	Label titleLabel;
	
	List<URL> imgUrls;
	Button upBtn, downBtn;
	Composite tableContainer;
	Button showPreviewBtn;
	SashForm sf;
	
	Image selectedImage = null;
	
	boolean showUpDownBtns;
	boolean withCheckboxes;
	boolean renderOriginalImages;
	
	public static final String URL_COL = "URL";

	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(URL_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
	};

	public ImgUrlListViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages) {
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
				URL p = (URL) element;
				if (cn.equals(URL_COL)) {
					return ""+p;
				}

				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}, showUpDownBtns, withCheckboxes, renderOriginalImages);
	}

	@Override protected Image loadImageForData(URL data) throws IOException {
		return ImgLoader.load(data);
	}

	
}
