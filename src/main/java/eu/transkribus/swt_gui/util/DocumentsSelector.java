package eu.transkribus.swt_gui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.DocumentSelectionDescriptor;
import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.util.APreviewListViewer;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;

public class DocumentsSelector extends APreviewListViewer<TrpDocMetadata> {
	private final static Logger logger = LoggerFactory.getLogger(DocPageViewer.class);
	
	public static final String ID_COL = "ID";
	public static final String TITLE_COL = "Title";
	public static final String N_PAGES_COL = "N-Pages";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(ID_COL, 65, true, DefaultTableColumnViewerSorter.DESC),
		new ColumnConfig(TITLE_COL, 150, false, DefaultTableColumnViewerSorter.ASC),
		new ColumnConfig(N_PAGES_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
	};

	public DocumentsSelector(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes) {
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
				if (!(element instanceof TrpDocMetadata)) {
					return "i am error";
				}
				
				String cn = COLS[columnIndex].name;
				TrpDocMetadata d = (TrpDocMetadata) element;
				
				if (cn.equals(ID_COL)) {
					return ""+d.getDocId();
				}
				else if (cn.equals(TITLE_COL)) {
					return d.getTitle();
				}
				else if (cn.equals(N_PAGES_COL)) {
					return ""+d.getNrOfPages();
				}
				
				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		}, showUpDownBtns, withCheckboxes, false);
	}
	
	public List<TrpDocMetadata> getCheckedDocuments() {
		return getCheckedDataList();
	}
	
	public List<DocumentSelectionDescriptor> getCheckedDocumentDescriptors() {
		List<DocumentSelectionDescriptor> dsds = new ArrayList<>();
		for (TrpDocMetadata d : getCheckedDocuments()) {
			dsds.add(new DocumentSelectionDescriptor(d.getDocId()));
		}
		return dsds;
	}

	@Override
	protected Control createPreviewArea(Composite previewContainer) {
		return null;
	}

	@Override
	protected void reloadPreviewForSelection() {
	}
}