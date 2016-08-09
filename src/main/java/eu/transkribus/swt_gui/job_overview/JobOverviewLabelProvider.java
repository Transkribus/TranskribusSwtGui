package eu.transkribus.swt_gui.job_overview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt_canvas.util.Colors;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class JobOverviewLabelProvider implements ITableLabelProvider, ITableColorProvider {
	private final static Logger logger = LoggerFactory.getLogger(JobOverviewLabelProvider.class);
	
//	DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
	DateFormat timeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	JobOverviewWidget jobOverviewWidget;
	Table table;

	private TableViewer tableViewer;

	public JobOverviewLabelProvider(JobOverviewWidget jobOverviewWidget) {
		this.jobOverviewWidget = jobOverviewWidget;
		table = jobOverviewWidget.jobTableViewer.getTable();
		tableViewer = jobOverviewWidget.jobTableViewer;
	}
	
	@Override public void addListener(ILabelProviderListener listener) {
	}

	@Override public void dispose() {
	}

	@Override public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override public void removeListener(ILabelProviderListener listener) {
	}

	@Override public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override public String getColumnText(Object element, int columnIndex) {
		if (element instanceof TrpJobStatus) {
			TrpJobStatus job = (TrpJobStatus) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
//			List<TrpJobStatus> data = jobOverviewWidget.getJobs();
//			int ri = data.indexOf(element);
//			logger.debug(ri+" element = "+element+" nrofdata = "+data.size()+" items = "+table.getItemCount());
//			TableItem ti = table.getItem(ri);						
			if (ct.equals(JobOverviewWidget.ID_COL)) {
				return job.getJobId();
			} else if (ct.equals(JobOverviewWidget.TYPE_COL)) {
				return job.getType();
			} else if (ct.equals(JobOverviewWidget.STATE_COL)) {				
				return job.getState();
			} else if (ct.equals(JobOverviewWidget.CREATION_COL)) {
				return timeFormatter.format(new Date(job.getCreateTime()));
			} else if (ct.equals(JobOverviewWidget.DOC_ID_COL)) {
				return ""+job.getDocId();
			} else if (ct.equals(JobOverviewWidget.PAGE_COL)) {
				return job.getPages();
			} else if (ct.equals(JobOverviewWidget.DESCRIPTION_COL)) {
				return job.getDescription();
			} else if (ct.equals(JobOverviewWidget.USER_NAME_COL)) {
				return job.getUserName();
			}
		}

		return "i am error";
	}

	@Override public Color getForeground(Object element, int columnIndex) {
		if (element instanceof TrpJobStatus) {
			TrpJobStatus job = (TrpJobStatus) element;
			
			TableColumn column = table.getColumn(columnIndex);
			String ct = column.getText();
			
			if (ct.equals(JobOverviewWidget.STATE_COL)) {
				if (job.getState().equals(TrpJobStatus.FINISHED)) {
					return Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
				}
				else if (job.getState().equals(TrpJobStatus.FAILED)) {
					return Colors.getSystemColor(SWT.COLOR_RED);
				}
				else {
					return Colors.getSystemColor(SWT.COLOR_DARK_YELLOW);
				}
			}
		}		
		
		return null;
	}

	@Override public Color getBackground(Object element, int columnIndex) {
		return null;
	}


}
