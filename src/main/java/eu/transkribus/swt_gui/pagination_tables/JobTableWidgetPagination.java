package eu.transkribus.swt_gui.pagination_tables;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ServerErrorException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.util.SessionExpiredException;
import eu.transkribus.client.util.TrpClientErrorException;
import eu.transkribus.client.util.TrpServerErrorException;
import eu.transkribus.core.model.beans.job.TrpJobStatus;
import eu.transkribus.core.model.beans.job.enums.JobImpl;
import eu.transkribus.core.model.beans.rest.JobErrorList;
import eu.transkribus.swt.mytableviewer.ColumnConfig;
import eu.transkribus.swt.pagination_table.ATableWidgetPaginationWithInfoBtn;
import eu.transkribus.swt.pagination_table.IPageLoadMethods;
import eu.transkribus.swt.pagination_table.RemotePageLoader;
import eu.transkribus.swt.pagination_table.TableColumnBeanLabelProvider;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt.util.Images;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;

public class JobTableWidgetPagination extends ATableWidgetPaginationWithInfoBtn<TrpJobStatus> {
	private final static Logger logger = LoggerFactory.getLogger(JobTableWidgetPagination.class);

	public static final String ID_COL = "ID";
	public static final String TYPE_COL = "Type";
	public static final String STATE_COL = "State";
	public static final String CREATION_COL = "Created";
	public static final String STARTED_COL = "Started";
	public static final String FINISHED_COL = "Finished";
	public static final String DOC_ID_COL = "Doc-Id";
	public static final String PAGE_COL = "Pages";
	public static final String DESCRIPTION_COL = "Description";
	public static final String NR_OF_ERRORS_COL = "Errors";
	public static final String USER_NAME_COL = "Username";
	public static final String RESULT_COL = "Download-Link";

	@Deprecated //never used!?
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
			new ColumnConfig(RESULT_COL, 100, false, DefaultTableColumnViewerSorter.ASC),};

	protected Button showAllJobsBtn, cancelBtn, allowUsersForJobBtn, undoJobBtn;
	protected Combo stateCombo;
	protected Text docIdText;
	protected Text typeFilterTxt;
	
	protected JobErrorDialog errorDiag = null;

	// DocJobUpdater docUpdater;
	static Storage store = Storage.getInstance();

	public JobTableWidgetPagination(Composite parent, int style, int initialPageSize) {
//		super(parent, style, initialPageSize, Images.getOrLoad("/icons/exclamation.png"), "Show job error details");
		super(parent, style, initialPageSize); //, Images.EXCLAMATION, "Show job error details");

		pageableTable.getController().setSort("createTime", SWT.UP);
		
		Composite topBtns = new Composite(this, 0);
		topBtns.setLayout(SWTUtil.createGridLayout(4, false, 0, 0));
		topBtns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		topBtns.moveAbove(pageableTable);
		
		cancelBtn = new Button(topBtns, SWT.NONE);
		cancelBtn.setText("Cancel job");		
		
		showAllJobsBtn = new Button(topBtns, SWT.CHECK);
		// showAllJobsBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true,
		// false, 1, 1));
		showAllJobsBtn.setText("Show all jobs");
		
		allowUsersForJobBtn = new Button(topBtns, 0);
		allowUsersForJobBtn.setText("Allow users for job...");
		
		undoJobBtn = new Button(topBtns, 0);
		undoJobBtn.setText("Undo job");
		undoJobBtn.setToolTipText("Undo this job on the loaded document. First load the document of the job to enable the button");
		undoJobBtn.setEnabled(false);
		
		boolean visible = Storage.getInstance().isLoggedIn() && Storage.getInstance().getUser().isAdmin();
		showAllJobsBtn.setVisible(visible);
		allowUsersForJobBtn.setVisible(visible);		

		Composite btns = new Composite(this, 0);
		btns.setLayout(new GridLayout(2, false));
		topBtns.setLayout(SWTUtil.createGridLayout(4, false, 0, 0));

		btns.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		btns.moveAbove(pageableTable);
		btns.moveBelow(topBtns);

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
			@Override
			public void modifyText(ModifyEvent e) {
				logger.debug("state changed: " + stateCombo.getText());

				refreshPage(true);
			}
		});

		TraverseListener refreshPageOnReturnListener = new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN) {
					logger.debug("docId = " + docIdText.getText()+", type = "+getTypeFilterText());
					refreshPage(true);
				}
			}
		};

		Label docIdLabel = new Label(btns, 0);
		docIdLabel.setText("Doc-Id: ");
		docIdText = new Text(btns, SWT.BORDER);
		docIdText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				// get old text and create new text by using the
				// VerifyEvent.text
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
		docIdText.addTraverseListener(refreshPageOnReturnListener);

		Label typeFilterLabel = new Label(btns, 0);
		typeFilterLabel.setText("Type filter: ");

		typeFilterTxt = new Text(btns, SWT.BORDER);
		typeFilterTxt.addTraverseListener(refreshPageOnReturnListener);
		typeFilterTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// pageableTable.sortChanged("", "createTimeFormatted", 0, SWT.UP,
		// pageableTable.getController());
		// pageableTable.refreshPage();

		// addDisposeListener(new DisposeListener() {
		// @Override public void widgetDisposed(DisposeEvent e) {
		// logger.debug("doc table widget disposed - stopping job update
		// thread!");
		// docUpdater.stopJobThread();
		// }
		// });

		// initDocUpdater();

		reloadJobList();
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
	
	private String getTypeFilterText() {
		return StringUtils.isEmpty(typeFilterTxt.getText()) ? null : typeFilterTxt.getText()+"%";
	}

	// public Button getReloadBtn() { return reloadBtn; }
	public Button getShowAllJobsBtn() {
		return showAllJobsBtn;
	}

	public Button getCancelBtn() {
		return cancelBtn;
	}
	
	public Button getAllowUsersForJobBtn() {
		return allowUsersForJobBtn;
	}

	public Button getUndoJobBtn() {
		return undoJobBtn;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// reloadBtn.setEnabled(enabled);
		showAllJobsBtn.setEnabled(enabled);
		cancelBtn.setEnabled(enabled);
		pageableTable.setEnabled(enabled);
	}

	@Override
	protected void setPageLoader() {
		if (methods == null) {
			methods = new IPageLoadMethods<TrpJobStatus>() {
				@Override
				public int loadTotalSize() {
					int N = 0;
					if (store == null || showAllJobsBtn == null)
						return 0;

					if (store.isLoggedIn()) {
						try {
							// sw.start();
							N = store.getConnection().countJobs(!showAllJobsBtn.getSelection(), getState(), getTypeFilterText(), getDocId());
							// sw.stop(true, "time for counting jobs: ",
							// logger);
						} catch (SessionExpiredException | ServerErrorException | IllegalArgumentException e) {
							TrpMainWidget.getInstance().onError("Error loading jobs", e.getMessage(), e);
						}
					}
					return N;
				}

				@Override
				public List<TrpJobStatus> loadPage(int fromIndex, int toIndex, String sortPropertyName,
						String sortDirection) {
					List<TrpJobStatus> jobs = new ArrayList<>();
					if (store == null || showAllJobsBtn == null)
						return jobs;

					if (store.isLoggedIn()) {
						try {
							// sw.start();
							logger.debug("loading jobs from server...");
							jobs = store.getConnection().getJobs(!showAllJobsBtn.getSelection(), getState(), getTypeFilterText(), getDocId(),
									fromIndex, toIndex - fromIndex, sortPropertyName, sortDirection);
							// sw.stop(true, "time for loading jobs: ", logger);
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

	@Override
	protected void createColumns() {
		class JobStateTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			public JobStateTableColumnLabelProvider(String colName) {
				super(colName);
			}
			
			@Override
			public String getText(Object element) {
				if(element instanceof TrpJobStatus 
						&& TrpJobStatus.CREATED.equals(((TrpJobStatus)element).getState())) {
					//show PENDING instead of CREATED to make clear that the job will be executed
					return "PENDING";
				}
				return super.getText(element);
			}

			@Override
			public Color getForeground(Object element) {
				if (element instanceof TrpJobStatus) {
					TrpJobStatus job = (TrpJobStatus) element;
					if (job.getState().equals(TrpJobStatus.FINISHED)) {
						return Colors.getSystemColor(SWT.COLOR_DARK_GREEN);
					} else if (job.getState().equals(TrpJobStatus.FAILED)) {
						return Colors.getSystemColor(SWT.COLOR_RED);
					} else {
						return Colors.getSystemColor(SWT.COLOR_DARK_YELLOW);
					}
				}

				return null;
			}
			
			@Override
			public Image getImage(Object element) {
				if (element instanceof TrpJobStatus) {
					TrpJobStatus job = (TrpJobStatus) element;
					if (job.getNrOfErrors() > 0) {
						return Images.ERROR;
					}
				}
				return null;
			}
		}
		class JobTypeTableColumnLabelProvider extends TableColumnBeanLabelProvider {
			private final static String HTR_MODEL_NAME_PATTERN_STR = ".*<modelName>(.*)</modelName>.*";
			private final Pattern htrModelNamePattern;
			public JobTypeTableColumnLabelProvider(String colName) {
				super(colName);
				htrModelNamePattern = Pattern.compile(HTR_MODEL_NAME_PATTERN_STR);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof TrpJobStatus) {
					final TrpJobStatus job = (TrpJobStatus)element;
					String type = job.getType();
					if (type.equals(JobImpl.CITlabHtrTrainingJob.getLabel()) && !StringUtils.isEmpty(job.getJobData())) {
						Matcher m = htrModelNamePattern.matcher(job.getJobData());
						if(m.find()) {
							type += ": " + m.group(1).trim();
						}
						
						// this is slower...
//						final String objectStr = (String) job.getJobDataProps().get(JobConst.PROP_CONFIG);
//						try {
//							CitLabHtrTrainConfig config = JaxbUtils.unmarshal(objectStr, CitLabHtrTrainConfig.class,
//									DocumentSelectionDescriptor.class, PageDescriptor.class);
//							type += ": " + config.getModelName();
//						} catch (JAXBException e) {
//							logger.error("Could not unmarshal config in jobData!");
//						}
					}
					return type;
				}
				return "i am error";
			}
		}

		createColumn(TYPE_COL, 100, "type", new JobTypeTableColumnLabelProvider("type"));
		createColumn(STATE_COL, 75, "state", new JobStateTableColumnLabelProvider("state"));

		createDefaultColumn(DOC_ID_COL, 50, "docId", true);
		createDefaultColumn(PAGE_COL, 50, "pages", true);
		createDefaultColumn(USER_NAME_COL, 100, "userName", true);
		createDefaultColumn(DESCRIPTION_COL, 100, "description", true);
		createDefaultColumn(NR_OF_ERRORS_COL, 50, "nrOfErrors", true);

		createDefaultColumn(CREATION_COL, 120, "createTimeFormatted", "createTime");
		createDefaultColumn(STARTED_COL, 120, "startTimeFormatted", "startTime");
		createDefaultColumn(FINISHED_COL, 120, "endTimeFormatted", "endTime");

		createDefaultColumn(ID_COL, 100, "jobId", true);
		TableViewerColumn resultColumn = createDefaultColumn(RESULT_COL, 300, "result", false);
		//Text text = new Text(resultColumn, SWT.NONE);
		resultColumn.getColumn().addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("widget selected");
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		resultColumn.setEditingSupport(new EditingSupport(resultColumn.getViewer()) {
			
			@Override
			protected void setValue(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected Object getValue(Object arg0) {
				// TODO Auto-generated method stub
				return resultColumn.getColumn().getText();
			}
			
			@Override
			protected CellEditor getCellEditor(Object arg0) {
				// TODO Auto-generated method stub
				return new TextCellEditor();
			}
			
			@Override
			protected boolean canEdit(Object arg0) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		

		// sort by creation date down:
		// pageableTable.getViewer().getTable().setSortColumn(creationCol.getColumn());
		// pageableTable.getViewer().getTable().setSortDirection(SWT.UP);
	}

	public void reloadJobList() {
		try {
			logger.debug("reloading job list!");
			refreshPage(true);
			// startOrResumeJobThread();
		} catch (Exception ex) {
			TrpMainWidget.getInstance().onError("Error", "Error during update of jobs", ex);
		}
	}

	public void updateJobInTable(TrpJobStatus job) {
		// FIXME: direct access to job table not "clean" here...
		List<TrpJobStatus> jobsInTable = (List<TrpJobStatus>) tv.getInput();
		if (jobsInTable == null) // should not happen!
			return;

		synchronized (jobsInTable) {
			for (TrpJobStatus j : jobsInTable) {
				if (j.getJobId().equals(job.getJobId())) {
					j.copy(job);
					tv.refresh(true);
					break;
				}
			}
		}
	}
	
	@Override
	protected void updateItemInfoBtn() {
		List<TrpJobStatus> selection = getSelected();
		int nrOfErrors = 0;
		if(selection.size() == 1) {
			TrpJobStatus job = selection.get(0);
			nrOfErrors = job.getNrOfErrors();
		} 
		itemInfoBtn.setEnabled(nrOfErrors > 0);
	}

	/**
	 * ask for errors on this job at server. TrpJobStatus now has field nrOfErrors though....
	 */
	@Deprecated
	protected void updateItemInfoBtnOld() {
		List<TrpJobStatus> selection = getSelected();
		if(selection.size() == 1) {
			TrpJobStatus job = selection.get(0);
			Display.getCurrent().asyncExec(new Runnable () {
				public void run() {
					JobErrorList errors = new JobErrorList(new ArrayList<>(0), 0, 0, -1, null, null);
					try {
						errors = store.getConnection().getJobErrors(job.getJobId(), 0, 1, null, null);
					} catch (SessionExpiredException | TrpServerErrorException | TrpClientErrorException e) {
						logger.error("Could not load job errors for job " + job.getJobId(), e);
					}
					itemInfoBtn.setEnabled(errors.getTotal() > 0);
				};
			});
		} else {
			//nothing selected...
			itemInfoBtn.setEnabled(false);
		}
	}
	
	@Override
	protected void onItemInfoBtnPressed(TrpJobStatus item) {
		logger.debug("opening job error dialog");
		if (SWTUtil.isOpen(errorDiag)) {
			errorDiag.setJobId(item.getJobIdAsInt());
			errorDiag.getShell().setVisible(true);
		} else {
			errorDiag = new JobErrorDialog(getShell(), item.getJobIdAsInt());
			errorDiag.open();
		}
	}

	public void setUndoVisible() {
		undoJobBtn.setEnabled(true);
	}

	public void setUndoInvisible() {
		undoJobBtn.setEnabled(false);		
	}
	
}
