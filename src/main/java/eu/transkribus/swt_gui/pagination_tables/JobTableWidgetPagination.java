package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.pagination_table.ATableWidgetPagination;
import org.dea.swt.pagination_table.IPageLoadMethods;
import org.dea.swt.pagination_table.RemotePageLoader;
import org.dea.swt.pagination_table.TableColumnBeanLabelProvider;
import org.dea.swt.util.Colors;
import org.dea.swt.util.DefaultTableColumnViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.swt_gui.mainwidget.Storage;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class JobTableWidgetPagination extends ATableWidgetPagination<TrpJobStatus> {
	private final static Logger logger = LoggerFactory.getLogger(JobTableWidgetPagination.class);
	
	public static final String ID_COL = "ID";
	public static final String TYPE_COL = "Type";
	public static final String STATE_COL = "State";
	public static final String CREATION_COL = "Created";
	public static final String STARTED_COL = "Started";
	public static final String FINISHED_COL = "Finished";
	public static final String DOC_ID_COL = "Doc-Id";
	public static final String PAGE_COL = "Page";
	public static final String DESCRIPTION_COL = "Description";
	public static final String USER_NAME_COL = "Username";
	
	public final ColumnConfig[] COLS = new ColumnConfig[] {
			new ColumnConfig(TYPE_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(STATE_COL, 75, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(CREATION_COL, 120, true, DefaultTableColumnViewerSorter.DESC),
			new ColumnConfig(STARTED_COL, 120, true, DefaultTableColumnViewerSorter.DESC),
			new ColumnConfig(FINISHED_COL, 120, true, DefaultTableColumnViewerSorter.DESC),
			new ColumnConfig(DOC_ID_COL, 80, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(USER_NAME_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(PAGE_COL, 50, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(DESCRIPTION_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
			new ColumnConfig(ID_COL, 100, false, DefaultTableColumnViewerSorter.ASC),
		};	

	Button showAllJobsBtn, cancelBtn;
	Combo stateCombo;
	Text docIdText;
	
	public JobTableWidgetPagination(Composite parent, int style, int initialPageSize) {
		super(parent, style, initialPageSize);
		
		pageableTable.getController().setSort("createTime", SWT.UP);
		
		Composite btns = new Composite(this, 0);
		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.fill = true;
//		btns.setLayout(rl);
		btns.setLayout(new GridLayout(2, false));
		
		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btns.moveAbove(pageableTable);
		
		showAllJobsBtn = new Button(btns, SWT.CHECK);
//		showAllJobsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		showAllJobsBtn.setText("Show all jobs");
		
		cancelBtn = new Button(btns, SWT.NONE);
//		cancelBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		cancelBtn.setText("Cancel job");
		
		Label l = new Label(btns, 0);
		l.setText("State: ");
		stateCombo = new Combo(btns, SWT.READ_ONLY | SWT.DROP_DOWN);
		stateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		stateCombo.add("ALL");
		stateCombo.add(TrpJobStatus.CANCELED);
		stateCombo.add(TrpJobStatus.CREATED);
		stateCombo.add(TrpJobStatus.FAILED);
		stateCombo.add(TrpJobStatus.FINISHED);
		stateCombo.add(TrpJobStatus.RUNNING);
		stateCombo.add(TrpJobStatus.UNFINISHED);
		stateCombo.add(TrpJobStatus.WAITING);
		stateCombo.select(0);
		
		stateCombo.addModifyListener(new ModifyListener() {
			@Override public void modifyText(ModifyEvent e) {
				logger.debug("state changed: "+stateCombo.getText());
				
				refreshPage(true);
			}
		});
		
		Label l1 = new Label(btns, 0);
		l1.setText("Doc-Id: ");
		
		docIdText = new Text(btns, SWT.BORDER);
		docIdText.addVerifyListener(new VerifyListener() {
			@Override public void verifyText(VerifyEvent e) {
	            // get old text and create new text by using the VerifyEvent.text
	            final String oldS = docIdText.getText();
	            String newS = oldS.substring(0, e.start) + e.text + oldS.substring(e.end);
	            if (newS.isEmpty())
	            	return;

				try {
					Integer.parseInt(newS);
				} catch (NumberFormatException ex) {
					e.doit = false;
				}
			}
		});
		docIdText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		docIdText.addTraverseListener(new TraverseListener() {
			@Override public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					logger.debug("docId = "+docIdText.getText());
					refreshPage(true);
				}
			}
		});
		
		
//		pageableTable.sortChanged("",  "createTimeFormatted", 0, SWT.UP, pageableTable.getController());
//		pageableTable.refreshPage();
	}
	
	private String getState() {
		return stateCombo.getText().equals("ALL") ? null : stateCombo.getText();
	}
	
	private Integer getDocId() {	
		try {
			return Integer.parseInt(docIdText.getText());
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
		
	}
	
//	public Button getReloadBtn() { return reloadBtn; }
	public Button getShowAllJobsBtn() { return showAllJobsBtn; }
	public Button getCancelBtn() { return cancelBtn; }
	
	@Override public void setEnabled(boolean enabled) {
//		reloadBtn.setEnabled(enabled);
		showAllJobsBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);
		pageableTable.setEnabled(enabled);
	}

	@Override protected void setPageLoader() {
		if (methods == null) {
			methods = new IPageLoadMethods<TrpJobStatus>() {
				Storage store = Storage.getInstance();
				
				@Override public int loadTotalSize() {					
					int N = 0;
					if (store.isLoggedIn()) {
						try {
							N = store.getConnection().countJobs(!showAllJobsBtn.getSelection(), getState(), getDocId());
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
						}
					}
					return N;
				}
	
				@Override public List<TrpJobStatus> loadPage(int fromIndex, int toIndex, String sortPropertyName, String sortDirection) {			
					List<TrpJobStatus> jobs = new ArrayList<>();
					if (store.isLoggedIn()) {
						try {
							jobs = store.getConnection().getJobs(!showAllJobsBtn.getSelection(), getState(), getDocId(), fromIndex, toIndex-fromIndex, sortPropertyName, sortDirection);
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
						}
					}
					return jobs;
				}
			};
		}
			
		RemotePageLoader<TrpJobStatus> pl = new RemotePageLoader<>(pageableTable.getController(), methods);
		pageableTable.setPageLoader(pl);
	}

	@Override protected void createColumns() {
		class CollectionsTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			public CollectionsTableColumnLabelProvider(String colName) {
				super(colName);
			}
           
        	@Override public Color getForeground(Object element) {
        		if (element instanceof TrpJobStatus) {
        		    TrpJobStatus job = (TrpJobStatus) element;
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
        		
        		return null;
        	}
		}		
		
		createDefaultColumn(TYPE_COL, 100, "type", true);
		createColumn(STATE_COL, 75, "state", new CollectionsTableColumnLabelProvider("state"));
		
		createDefaultColumn(DOC_ID_COL, 50, "docId", true);
		createDefaultColumn(PAGE_COL, 50, "pageNr", true);
		createDefaultColumn(USER_NAME_COL, 100, "userName", true);
		createDefaultColumn(DESCRIPTION_COL, 100, "description", true);
		
		createDefaultColumn(CREATION_COL, 120, "createTimeFormatted", "createTime");
		createDefaultColumn(STARTED_COL, 120, "startTimeFormatted", "startTime");
		createDefaultColumn(FINISHED_COL, 120, "endTimeFormatted", "endTime");

		createDefaultColumn(ID_COL, 100, "jobId", true);
		
		// sort by creation date down:
//		pageableTable.getViewer().getTable().setSortColumn(creationCol.getColumn());
//		pageableTable.getViewer().getTable().setSortDirection(SWT.UP);
	}

//	public void refreshList() {
//		refreshPage(true);
//	}
	
}
