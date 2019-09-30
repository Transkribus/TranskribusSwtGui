package eu.transkribus.swt_gui.htr;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.enums.EditStatus;
import eu.transkribus.swt_gui.htr.treeviewer.DataSetSelectionSashForm;
import eu.transkribus.swt_gui.htr.treeviewer.DocumentDataSelectionEntry;
import eu.transkribus.swt_gui.htr.treeviewer.GroundTruthDataSelectionEntry;
import eu.transkribus.swt_gui.htr.treeviewer.IDataSelectionEntry;

public class DataSetTableLabelProvider implements ITableLabelProvider, ITableFontProvider, IColorProvider {
	private static final Logger logger = LoggerFactory.getLogger(DataSetTableLabelProvider.class);
	
	Table table;
	TableViewer tableViewer;
	
	private EditStatus transcriptStatusFilter = null;

	public DataSetTableLabelProvider(TableViewer tableViewer) {
		this.tableViewer = tableViewer;
		this.table = tableViewer.getTable();
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		//logger.trace("get column text: "+element+" id: "+columnIndex);
		
		if (element instanceof IDataSelectionEntry) {
			IDataSelectionEntry<?, ?> d = (IDataSelectionEntry<?, ?>) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(DataSetTableWidget.ID_COL)) {
				if(element instanceof GroundTruthDataSelectionEntry) {
					return "HTR " + d.getId();
				}
				return ""+d.getId();
			} else if (ct.equals(DataSetTableWidget.TITLE_COL)) {
				return d.getTitle();
			} else if (ct.equals(DataSetTableWidget.PAGES_COL)) {
				return getPageString(d);
			}
		}
		return "i am error";
	}
	
	private String getPageString(IDataSelectionEntry<?, ?> d) {
		if(d instanceof DocumentDataSelectionEntry && transcriptStatusFilter != null) {
			logger.debug("Producing pagestring on demand as filter is set.");
			return ((DocumentDataSelectionEntry)d).getPageString(transcriptStatusFilter);
		}
		return d.getPageString();
	}

	@Override
	public Color getForeground(Object element) {
		if(element instanceof DocumentDataSelectionEntry 
				&& transcriptStatusFilter != null
				//determine if the set version filter would remove all pages from the set
				&& StringUtils.isEmpty(getPageString((IDataSelectionEntry<?, ?>) element))) {
			return DataSetSelectionSashForm.GRAY;
		}
		return null;
	}
	
	@Override
	public Color getBackground(Object arg0) {
		return null;
	}

	@Override
	public Font getFont(Object element, int columnIndex) {
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
	public void setTranscriptStatusFilter(EditStatus status) {
		this.transcriptStatusFilter = status;
	}
}
