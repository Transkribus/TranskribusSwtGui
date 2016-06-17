package eu.transkribus.swt_gui.structure_tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.structure_tree.StructureTreeWidget.ColConfig;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

public class StructureTreeLabelProvider extends CellLabelProvider implements ITableLabelProvider {
	private final static Logger logger = LoggerFactory.getLogger(StructureTreeLabelProvider.class);

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
	
	public static String getTextForElement(Object element, int columnIndex) {
		String name="", id="", coords="", text="", regionType="", readingOrder="";
		
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
			if (element instanceof TrpTextLineType || element instanceof TrpWordType || element instanceof TrpTextRegionType) {
				readingOrder = s.getReadingOrder()!=null ? ""+(s.getReadingOrder()+1) : "";
			}
			
			if (element instanceof TrpTableCellType) {
				text = ((TrpTableCellType) element).getCoords().getCornerPts();
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

		if (columnIndex < 0 || columnIndex >= StructureTreeWidget.COLUMNS.length)
			return "wrong col index";
		
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.TYPE_COL)
			return name;
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.ID_COL)
			return id;
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.TEXT_COL)
			return text;
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.COORDS_COL)
			return coords;
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.STRUCTURE_TYPE)
			return regionType;
		else if (StructureTreeWidget.COLUMNS[columnIndex] == StructureTreeWidget.READING_ORDER_TYPE)
			return readingOrder;
		
		return "i am error!";
		
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		return getTextForElement(element, columnIndex);
	}
	
	// CellLabelProvider:

	@Override
	public void update(ViewerCell cell) {
		ColConfig cf = StructureTreeWidget.COLUMNS[cell.getColumnIndex()];
		Object element = cell.getViewerRow().getElement();
//		logger.trace("column = "+cf.name);
		 
		String text = StructureTreeLabelProvider.getTextForElement(element, cell.getColumnIndex());
		cell.setText(text);
	}



}
