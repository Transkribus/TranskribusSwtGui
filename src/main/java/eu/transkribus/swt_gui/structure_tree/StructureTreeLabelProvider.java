package eu.transkribus.swt_gui.structure_tree;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class StructureTreeLabelProvider extends CellLabelProvider implements ITableLabelProvider {
	private final static Logger logger = LoggerFactory.getLogger(StructureTreeLabelProvider.class);
	TreeViewer treeViewer;
	Storage store = Storage.getInstance();
	boolean useStructColors=false;
	
	public StructureTreeLabelProvider(TreeViewer treeViewer, boolean useStructColors) {
		this.treeViewer = treeViewer;
		this.useStructColors = useStructColors;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {		
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
	public static String getTextForElement(Object element, int columnIndex, String columnName) {
		String name="", id="", coords="", text="", regionType="", readingOrder="", other="";
		
		if (element instanceof TrpPageType) {
			name = "Page";
		}
		else if (element instanceof ITrpShapeType) {
			try {
//			logger.debug("getting label for "+element);
			ITrpShapeType s = (ITrpShapeType) element;
			
			name = RegionTypeUtil.getRegionType(s);
//			name = s.getName();
//			if (RegionTypeUtil.isBlackening(s))
//				name = RegionTypeUtil.getRegionType(s);
			
			coords = s.getCoordinates();
			
			id = s.getId();
			regionType = s.getStructure()!=null ? s.getStructure() : "";
			
			// show text only for lines and words, otherwise the tree elements get too big:
			if (element instanceof TrpTextLineType || element instanceof TrpWordType)
				text = s.getUnicodeText();
			//show reading order for lines, words as well as for all regions
			if (element instanceof TrpTextLineType || element instanceof TrpWordType || element instanceof TrpRegionType) {
				readingOrder = s.getReadingOrder()!=null ? ""+(s.getReadingOrder()+1) : "";
			}
			
			if (element instanceof TrpTableCellType) {
				TrpTableCellType tc = (TrpTableCellType) element;
				
				other = tc.toString();
				
				text = tc.toString();
				
//				other = tc.getCornerPts();
//				other += " ("+tc.getRow()+","+tc.getCol()+")"+" ("+tc.getRowSpan()+","+tc.getColSpan()+")";
			}
			
//			if (element instanceof TrpTextRegionType) {
//				TrpTextRegionType tr = (TrpTextRegionType) element;
//				TextTypeSimpleType st = tr.getType();
//				if (st != null)
//					regionType = st.value();
//				else
//					regionType = "NA";
//			}
			} catch (Throwable th) {
				logger.error(th.getMessage(), th);
			}
		}
		
//		ColConfig col = StructureTreeWidget.COLUMNS[columnIndex];
//		if (columnIndex < 0 || columnIndex >= StructureTreeWidget.COLUMNS.length)
//			return "wrong col index";
		
		if (columnName.equals(StructureTreeWidget.TYPE_COL.name))
			return name;
		else if (columnName.equals(StructureTreeWidget.ID_COL.name))
			return id;
		else if (columnName.equals(StructureTreeWidget.TEXT_COL.name))
			return text;
		else if (columnName.equals(StructureTreeWidget.COORDS_COL.name))
			return coords;
		else if (columnName.equals(StructureTreeWidget.STRUCTURE_TYPE_COL.name))
			return regionType;
		else if (columnName.equals(StructureTreeWidget.READING_ORDER_COL.name))
			return readingOrder;
		else if (columnName.equals(StructureTreeWidget.READING_ORDER_COL.name))
			return readingOrder;
		else if (columnName.equals(StructureTreeWidget.OTHER_COL.name))
			return other;
		
		return "i am error!";
		
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		String columnName = treeViewer.getTree().getColumn(columnIndex).getText();
		return getTextForElement(element, columnIndex, columnName);
	}
	
	// CellLabelProvider:

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getViewerRow().getElement();
		String columnName = treeViewer.getTree().getColumn(cell.getColumnIndex()).getText();
		String text = StructureTreeLabelProvider.getTextForElement(element, cell.getColumnIndex(), columnName);
		
		
		cell.setText(text);
		
		if (useStructColors && element instanceof ITrpShapeType && columnName.equals(StructureTreeWidget.STRUCTURE_TYPE_COL.name)) {
			ITrpShapeType st = (ITrpShapeType) element;
			cell.setForeground(store.getStructureTypeColor(st.getStructure()));
		}
	}



}
