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

import eu.transkribus.swt_canvas.util.Colors;
import eu.transkribus.swt_canvas.util.Fonts;
import eu.transkribus.swt_canvas.util.SWTUtil;
import eu.transkribus.swt_gui.Msgs;

public class TrpTabWidget extends Composite {
	
	static class Item {
		public Item() {};
		
		public Item(CTabItem parent, CTabItem... children) {
			this.parent = parent;
			for (CTabItem c : children) {
				this.children.add(c);
			}
		}
		
		public CTabItem parent;
		public List<CTabItem> children; 
	}
	
	CTabFolder tf;
	CTabFolder serverTf;
	CTabFolder documentTf;
	CTabFolder metadataTf;
	CTabFolder toolsTf;
	
	CTabItem serverItem, documentItem, metadataItem, toolsItem;
	
	// server items:
	CTabItem docListItem;
		
	// items for document tf:
	CTabItem docoverviewItem, structureItem, versionsItem, thumbnailItem;
	
	// items for metadata tf:
	CTabItem structuralMdItem, textTaggingItem, commentsItem;
	
	CTabItem remoteToolsItem, jobsItem, vkItem;
	
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
		tf = createTabFolder(this);		
		
		serverTf = createTabFolder(tf);
//		serverTf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		serverTf.setLayout(new FillLayout());
		serverItem = createCTabItem(tf, serverTf, "Server", firstRowItems);
		initServerTf();
				
		documentTf = createTabFolder(tf);//		documentTf.setLayout(new FillLayout());
		documentItem = createCTabItem(tf, documentTf, "Document", firstRowItems);
		initDocumentTf();
		
		metadataTf = createTabFolder(tf);
		metadataItem = createCTabItem(tf, metadataTf, "Metadata", firstRowItems);
		initMetadataTf();
		
		if (false) {
		toolsTf = createTabFolder(tf);
		toolsItem = createCTabItem(tf, toolsTf, "Tools", firstRowItems);
		initToolsTf();
		}
		
		Composite c = new Composite(tf, 0);
		vkItem = createCTabItem(tf, c, "Virtual Keyboards", firstRowItems);
		
		allItems.addAll(firstRowItems);
		allItems.addAll(secondRowItems);
		
		setDefaultSelection();
		
		updateFirstRowColors();
		addTabFolderSelectionListener();
	}
	
	void setDefaultSelection() {
		SWTUtil.setSelection(tf, serverItem);
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
		
		docListItem = createCTabItem(serverTf, c, "Doclist", secondRowItems);
		remoteToolsItem = createCTabItem(serverTf, c, "Tools", secondRowItems);
		jobsItem = createCTabItem(serverTf, c, "Jobs", secondRowItems);
		
		
	}
	
	void initDocumentTf() {
		// TODO: create widgets
		Composite c = new Composite(documentTf, 0);
		
//		docoverviewItem = createCTabItem(documentTf, c, Msgs.get2("documents")); // TODO
		
		structureItem = createCTabItem(documentTf, c, Msgs.get2("layout_tab_title"), secondRowItems);
//		jobOverviewItem = createCTabItem(leftTabFolder, jobOverviewWidget, Msgs.get2("jobs"));
		versionsItem = createCTabItem(documentTf, c, Msgs.get2("versions"), secondRowItems);
		thumbnailItem = createCTabItem(documentTf, c, Msgs.get2("pages"), secondRowItems);
	}
	
	void initMetadataTf() {
		Composite c = new Composite(metadataTf, 0);
		
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
		tf.setSelection(serverItem);
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

	public void selectDocListTab() {
		tf.setSelection(serverItem);
		serverTf.setSelection(docListItem);
		
		updateTabItemStyles();
	}

	public void selectJobListTab() {
		tf.setSelection(serverItem);
		serverTf.setSelection(jobsItem);
		updateTabItemStyles();
	}
	
	

}
