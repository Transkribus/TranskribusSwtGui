package eu.transkribus.swt_gui.htr;

import java.text.DateFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import eu.transkribus.core.model.beans.TrpHtr;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.HtrCITlabUtils;
import eu.transkribus.core.util.HtrPyLaiaUtils;
import eu.transkribus.swt.util.Images;

public class HtrTreeLabelProvider implements ITreePathLabelProvider{

	private final static String NOT_AVAILABLE_LABEL = "N/A";

	Tree tree;
	TreeViewer treeViewer;
	DateFormat createDateFormat;

	public HtrTreeLabelProvider(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
		this.tree = treeViewer.getTree();
		this.createDateFormat = CoreUtils.newDateFormatddMMYY();
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

	public Image getColumnImage(Object element, int columnIndex) {
		TreeColumn column = tree.getColumn(columnIndex);
		String ct = column.getText();
		if (element instanceof TrpHtr) {
			switch (ct) {
			case HtrTableWidget.HTR_NAME_COL:
				if(((TrpHtr)element).getReleaseLevelValue() > 0) {
					return Images.MODEL_SHARED_ICON;
				}
				return Images.MODEL_ICON;
			default:
				return null;
			}
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TrpHtr) {
			TrpHtr htr = (TrpHtr) element;

			TreeColumn column = tree.getColumn(columnIndex);
			String ct = column.getText();
			
			return getColumnText(htr, ct);
		} else {
			return NOT_AVAILABLE_LABEL;
		}
	}
	
	public String getColumnText(TrpHtr htr, String columnName) {
		switch (columnName) {
		case HtrTableWidget.HTR_NAME_COL:
			return htr.getName();
		case HtrTableWidget.HTR_LANG_COL:
			return htr.getLanguage();
		case HtrTableWidget.HTR_ID_COL:
			return "" + htr.getHtrId();
		case HtrTableWidget.HTR_CREATOR_COL:
			return htr.getUserName() == null ? "Unknown" : htr.getUserName();
		case HtrTableWidget.HTR_TECH_COL:
			return getLabelForHtrProvider(htr.getProvider());
		case HtrTableWidget.HTR_DATE_COL:
			return createDateFormat.format(htr.getCreated());
		default:
			return NOT_AVAILABLE_LABEL;
		}
	}

	public static String getLabelForHtrProvider(String provider) {
		if (StringUtils.isEmpty(provider)) {
			return NOT_AVAILABLE_LABEL;
		}
		switch (provider) {
		case HtrCITlabUtils.PROVIDER_CITLAB:
			return "CITlab HTR";
		case HtrCITlabUtils.PROVIDER_CITLAB_PLUS:
			return "CITlab HTR+";
		case HtrPyLaiaUtils.PROVIDER_PYLAIA:
			return "PyLaia";
		default:
			return NOT_AVAILABLE_LABEL;
		}
	}


	@Override
	public void updateLabel(ViewerLabel arg0, TreePath arg1) {
		// TODO Auto-generated method stub
		
	}
}
