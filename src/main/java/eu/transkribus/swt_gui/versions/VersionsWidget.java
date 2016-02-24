package eu.transkribus.swt_gui.versions;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpDocMetadata;
import eu.transkribus.core.model.beans.TrpTranscriptMetadata;
import eu.transkribus.swt_canvas.mytableviewer.ColumnConfig;
import eu.transkribus.swt_canvas.mytableviewer.MyTableViewer;
import eu.transkribus.swt_canvas.util.DefaultTableColumnViewerSorter;
import eu.transkribus.swt_canvas.util.Images;
import eu.transkribus.swt_canvas.util.TableViewerSorter;
import eu.transkribus.swt_gui.pagination_tables.TranscriptsTableWidgetListener;

public class VersionsWidget extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(VersionsWidget.class);

//	PageableTable versionsTable;
//	TableViewer tableViewer;
	MyTableViewer tableViewer;
//	PageResultLoaderList<TrpTranscriptMetadata> versionsPageLoader;
	TrpTranscriptMetadata selected;
	
	Button reloadBtn, deleteBtn;
	
	public static final String DATE_COL = "Date";
	public static final String STATUS_COL = "Status";
	public static final String USER_ID_COL = "User-ID";
	public static final String TOOLNAME_COL = "Toolname";
	
	public class DateViewerSorter extends TableViewerSorter {
		public DateViewerSorter() {
			super(tableViewer);
		}

		@Override protected int doCompare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof TrpTranscriptMetadata && e2 instanceof TrpTranscriptMetadata) {
				return ((TrpTranscriptMetadata)e1).getTime().compareTo(((TrpTranscriptMetadata)e2).getTime());
			}
			
			return 0;
		}
	}
	
	public final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(USER_ID_COL, 100, false, TableViewerSorter.ASC),
		new ColumnConfig(STATUS_COL, 100, false, TableViewerSorter.ASC),
		new ColumnConfig(DATE_COL, 225, true, TableViewerSorter.DESC, SWT.LEFT, new DateViewerSorter()),
		new ColumnConfig(TOOLNAME_COL, 100, false, TableViewerSorter.ASC),
	};

	public VersionsWidget(Composite parent, int style) {
		super(parent, style);
		// setLayout(new FillLayout());

		GridLayout layout = new GridLayout(3, false);
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
		setLayout(layout);
		
//		reloadBtn = new Button(this, SWT.NONE);
//		reloadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
//		reloadBtn.setToolTipText("Reload versions list");
//		reloadBtn.setImage(Images.getOrLoad("/icons/refresh.gif"));

		deleteBtn = new Button(this, SWT.NONE);
		deleteBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1));
		deleteBtn.setToolTipText("Delete version");
		deleteBtn.setImage(Images.getOrLoad("/icons/delete.png"));
		
		initVersionTable();
	}

	private void initVersionTable() {

		// final List items = createList();

//		int pageSize = 20;
//		versionsTable = new PageableTable(this, SWT.BORDER, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, pageSize,
//				PageResultContentProvider.getInstance(), null, ResultAndNavigationPageLinksRendererFactory.getFactory());
//
//		versionsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
//		versionsPageLoader = new PageResultLoaderList<>(Storage.getInstance().getTranscriptsSortedByDate(false, 0));
//		versionsTable.setPageLoader(versionsPageLoader);
//		versionsTable.getController().addPageChangedListener(new IPageChangedListener() {
//			
//			@Override public void totalElementsChanged(long arg0, long arg1, PageableController arg2) {
//			}
//			
//			@Override public void sortChanged(String arg0, String arg1, int arg2, int arg3, PageableController arg4) {
//			}
//			
//			@Override public void pageSizeChanged(int arg0, int arg1, PageableController arg2) {
//			}
//			
//			@Override public void pageIndexChanged(int arg0, int arg1, PageableController arg2) {
//				tableViewer.refresh();
//			}
//			
//			@Override public void localeChanged(Locale arg0, Locale arg1, PageableController arg2) {
//			}
//		});

		// 2) Initialize the table viewer + SWT Table
//		tableViewer = versionsTable.getViewer();
		
		tableViewer = new MyTableViewer(this, SWT.SINGLE | SWT.FULL_SELECTION);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
//		viewer.setLabelProvider(new LabelProvider());
		tableViewer.setLabelProvider(new VersionsWidgetLabelProvider(this));
		
		tableViewer.addColumns(COLS);

		Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		// table.setSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, 300);
		// table.setItemCount(10);

//		createColumn(tableViewer, USER_ID_COL, 100);
//		createColumn(tableViewer, STATUS_COL, 100);
//		createColumn(tableViewer, DATE_COL, 225);

//		PageableController controller = versionsTable.getController();
		// 3) Set current page to 0 to refresh the table
//		controller.setCurrentPage(0);
	}

	public void setVersions(List<TrpTranscriptMetadata> list) {
		if (tableViewer == null)
			return;
		
		tableViewer.setInput(list);

//		if (list==null)
//			versionsPageLoader.setItems(new ArrayList<TrpTranscriptMetadata>());
//		else {
//			logger.debug("setting transcripts: " + list.size());
//			versionsPageLoader.setItems(new ArrayList<TrpTranscriptMetadata>(list));
//		}
					
//		versionsTable.getController().setCurrentPage(0);
//		tableViewer.refresh();
//		versionsTable.getViewer().getTable().update();

		// versionsTable.redraw();
		// versionsTable.getViewer().refresh();

	}

	private static TableColumn createColumn(TableViewer viewer, String title, int bound) {
//		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
//		final TableColumn column = viewerColumn.getColumn();
		
		TableColumn column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return column;
	}

//	private int indexOf(TrpTranscriptMetadata transcript) {
//		if (transcript==null)
//			return -1;
//		
//		int i = 0;
//		List<TrpTranscriptMetadata> newList = new ArrayList<>((List<TrpTranscriptMetadata>) versionsTable.getViewer().getInput());
//		
//		for (TrpTranscriptMetadata md : newList) {
//			if (md.getTimestamp() == transcript.getTimestamp()) {
//				return i;
//			}
//			++i;
//		}
//		return -1;
//	}
	
	public void updateSelectedVersion(TrpTranscriptMetadata selected) {
		this.selected = selected;
		tableViewer.refresh();
	}

	// TODO
//	public void updateSelectedVersion() {
//		return;
//		int index = indexOf(selected);
//
//		int i = 0;
//		for (TableItem ti : versionsTable.getViewer().getTable().getItems()) {
//			FontData fd = ti.getFont().getFontData()[0];
//
//			if (i == index) {
//				fd.setStyle(SWT.BOLD);
//			} else {
//				fd.setStyle(SWT.NORMAL);
//			}
//
//			ti.setFont(Fonts.createFont(fd));
//			++i;
//		}
//	}

	public void addListener(TranscriptsTableWidgetListener listener) {
		tableViewer.getTable().addSelectionListener(listener);
		tableViewer.addDoubleClickListener(listener);
	}
	
	public void removeListener(TranscriptsTableWidgetListener listener) {
		tableViewer.getTable().removeSelectionListener(listener);
		tableViewer.removeDoubleClickListener(listener);
	}

}
