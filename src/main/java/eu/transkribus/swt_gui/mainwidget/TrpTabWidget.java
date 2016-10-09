package eu.transkribus.swt_gui.mainwidget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;
import eu.transkribus.swt.util.SWTUtil;
import eu.transkribus.swt_gui.Msgs;

public class TrpTabWidget extends Composite {

	static class Item {
		public Item() {
		};

		public Item(CTabItem parent, CTabItem... children) {
			this.parent = parent;
			for (CTabItem c : children) {
				this.children.add(c);
			}
		}

		public CTabItem parent;
		public List<CTabItem> children;
	}

	CTabFolder mainTf;

	CTabFolder serverTf;
	CTabFolder documentTf;
	CTabFolder structureTf;
	CTabFolder metadataTf;
	CTabFolder toolsTf;

	CTabItem serverItem;
	CTabItem documentItem;
	CTabItem metadataItem;
	CTabItem toolsItem;

	// server items:
	CTabItem docListItem;

	// items for document tf:
//	CTabItem docoverviewItem;
	CTabItem structureItem;
	CTabItem versionsItem;
//	CTabItem thumbnailItem;

	// items for metadata tf:
	CTabItem docMdItem;
	CTabItem structuralMdItem;
	CTabItem textTaggingItem;
	CTabItem commentsItem;

	CTabItem remoteToolsItem;
	CTabItem jobsItem;
	CTabItem vkItem;

	List<CTabItem> firstRowItems = new ArrayList<>();
	List<CTabItem> secondRowItems = new ArrayList<>();
	List<CTabItem> allItems = new ArrayList<>();

	List<CTabFolder> tabfolder = new ArrayList<>();

	public TrpTabWidget(Composite parent, int style) {
		super(parent, style);

		this.setLayout(new GridLayout(1, false));

		init();
	}

	void init() {
		mainTf = createTabFolder(this);

		serverTf = createTabFolder(mainTf);
		serverItem = createCTabItem(mainTf, serverTf, "Server", firstRowItems);
//		initServerTf();

		documentTf = createTabFolder(mainTf);//		documentTf.setLayout(new FillLayout());
		documentItem = createCTabItem(mainTf, documentTf, "Overview", firstRowItems);
//		initDocumentTf();

		structureTf = createTabFolder(mainTf);
		structureItem = createCTabItem(mainTf, structureTf, Msgs.get2("layout_tab_title"), firstRowItems);

		metadataTf = createTabFolder(mainTf);
		metadataItem = createCTabItem(mainTf, metadataTf, "Metadata", firstRowItems);
		initMetadataTf();

		Composite c = new Composite(mainTf, 0);
		toolsItem = createCTabItem(mainTf, c, "Tools", firstRowItems);
//		vkItem = createCTabItem(mainTf, c, "Virtual Keyboards", firstRowItems);

		allItems.addAll(firstRowItems);
		allItems.addAll(secondRowItems);

		setDefaultSelection();

		updateFirstRowColors();
		addTabFolderSelectionListener();
	}

	void setDefaultSelection() {
		SWTUtil.setSelection(mainTf, serverItem);
		SWTUtil.setSelection(serverTf, docListItem);
		SWTUtil.setSelection(documentTf, structureItem);
		SWTUtil.setSelection(metadataTf, structuralMdItem);
		SWTUtil.setSelection(toolsTf, remoteToolsItem);
	}

	void updateFirstRowColors() {
		for (CTabItem i : firstRowItems) {
			i.setFont(Fonts.addStyleBit(i.getFont(), SWT.ITALIC));
		}
	}

	void updateTabItemStyles() {
		for (CTabFolder tf : tabfolder) {
			if (tf != null)
				updateSelectedOnTabFolder(tf);
		}
	}

	void updateSelectedOnTabFolder(CTabFolder tf) {
		if (tf == null)
			return;

		for (CTabItem i : tf.getItems()) {
			if (i == tf.getSelection()) {
//				Font f = Fonts.createFont(i.getFont().getFo, height, style);
				i.setFont(Fonts.addStyleBit(i.getFont(), SWT.BOLD));
				i.setFont(Fonts.removeStyleBit(i.getFont(), SWT.NORMAL));
			} else {
				i.setFont(Fonts.removeStyleBit(i.getFont(), SWT.BOLD));
				i.setFont(Fonts.addStyleBit(i.getFont(), SWT.NORMAL));
			}

		}
	}

	void addTabFolderSelectionListener() {
		SelectionListener sl = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(e.item instanceof CTabItem))
					return;

				CTabItem item = (CTabItem) e.item;

				updateSelectedOnTabFolder(item.getParent());
			}
		};

		for (CTabFolder tf : tabfolder) {
			tf.addSelectionListener(sl);
		}

		updateTabItemStyles();
	}

	void initServerTf() {
		Composite c = new Composite(serverTf, 0);

		docListItem = createCTabItem(serverTf, c, "Documents", secondRowItems);
		remoteToolsItem = createCTabItem(serverTf, c, "Tools", secondRowItems);
		jobsItem = createCTabItem(serverTf, c, "Jobs", secondRowItems);
	}

	void initDocumentTf() {
		// TODO: create widgets
		Composite c = new Composite(documentTf, 0);

//		docoverviewItem = createCTabItem(documentTf, c, "Overview", secondRowItems); // TODO
//		structureItem = createCTabItem(documentTf, c, Msgs.get2("layout_tab_title"), secondRowItems);
//		jobOverviewItem = createCTabItem(leftTabFolder, jobOverviewWidget, Msgs.get2("jobs"));
//		versionsItem = createCTabItem(documentTf, c, Msgs.get2("versions"), secondRowItems);
//		thumbnailItem = createCTabItem(documentTf, c, Msgs.get2("pages"), secondRowItems);
	}

	void initMetadataTf() {
		Composite c = new Composite(metadataTf, 0);

		docMdItem = createCTabItem(metadataTf, c, "Document", secondRowItems);
		structuralMdItem = createCTabItem(metadataTf, c, "Structural", secondRowItems);
		textTaggingItem = createCTabItem(metadataTf, c, "Tagging", secondRowItems);
		commentsItem = createCTabItem(metadataTf, c, "Comments", secondRowItems);

	}

	void initToolsTf() {
		Composite c = new Composite(toolsTf, 0);

		vkItem = createCTabItem(toolsTf, c, "Virtual Keyboards", secondRowItems);
	}

	private CTabFolder createTabFolder(Composite parent) {
		CTabFolder tf = new CTabFolder(parent, SWT.BORDER | SWT.FLAT);
		tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tf.setBorderVisible(true);
		tf.setSelectionBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		tabfolder.add(tf);

		return tf;
	}

	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		return createCTabItem(tabFolder, control, Text, null);
	}

	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text, List<CTabItem> list) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);

		if (list != null)
			list.add(ti);

		return ti;
	}

	public void selectServerTab() {
		mainTf.setSelection(serverItem);
		updateTabItemStyles();
	}

	public static void run() {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Show CTabFolder");
		shell.setLayout(new FillLayout());
		new TrpTabWidget(shell, 0);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public static void main(String[] args) {
		run();
	}

}
