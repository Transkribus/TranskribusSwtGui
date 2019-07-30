package eu.transkribus.swt_gui.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.util.NaturalOrderFileComparator;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.APreviewListViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class FileListViewer extends APreviewListViewer<File> {
	private static final Logger logger = LoggerFactory.getLogger(FileListViewer.class);
	
	public static final String PAGE_NR_COL = "Page";
//	public static final String ID_COL = "ID";
	public static final String FN_COL = "Filename";
	
	protected Text previewText;
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(PAGE_NR_COL, 65, false, DefaultTableColumnViewerSorter.ASC),
//			new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(FN_COL, 300, false, DefaultTableColumnViewerSorter.ASC),
		};
	
	public class FileListViewerTableProvider implements ITableLabelProvider {
		@Override
		public void addListener(ILabelProviderListener arg0) {
		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		@Override
		public void removeListener(ILabelProviderListener arg0) {
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			String cn = COLS[columnIndex].name;
			
			File f = (File) element;
			if (cn.equals(PAGE_NR_COL)) {
				return ""+(indexOf(f)+1);
			}
			else if (cn.equals(FN_COL)) {
				return f.getName();
//				return withCheckboxes? FilenameUtils.getBaseName(p.getImgFileName()) : p.getImgFileName() ;
			}
			
			return null;
		}
	}
	
	public FileListViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean enablePreview) {
		super(parent, style, COLS, null, showUpDownBtns, withCheckboxes, enablePreview);
		setLabelProvider(new FileListViewerTableProvider());
		setComparator(new NaturalOrderFileComparator());
	}
	

	@Override
	protected Control createPreviewArea(Composite previewContainer) {
		previewText = new Text(previewContainer, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		previewText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));	
		
		return previewText;
	}

	@Override
	protected void reloadPreviewForSelection() {
		if (previewContainer == null || previewText == null) {
			return;
		}
		
		if ( !showPreview || !showPreviewBtn.getSelection() ) {
			previewText.setText("");
		}
		
		String txt = "";
		File f = getFirstSelected();
		if (f != null && f.exists()) {
			try {
				txt = FileUtils.readFileToString(f);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				txt = "Error reading file";
			}
		}
		previewText.setText(txt);
	}
}