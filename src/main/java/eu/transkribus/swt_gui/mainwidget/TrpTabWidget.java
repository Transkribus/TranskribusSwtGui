package eu.transkribus.swt_gui.mainwidget;

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
import eu.transkribus.swt_gui.Msgs;

public class TrpTabWidget extends Composite {
	
	CTabFolder tf;
//	CTabFolder serverTf;
	CTabFolder documentTf;
	CTabFolder metadataTf;
	CTabFolder toolsTf;
	
	CTabItem serverItem, documentItem, metadataItem, toolsItem;
		
	// items for document tf:
	CTabItem docoverviewItem, structureItem, versionsItem, thumbnailItem;
	
	// items for metadata tf:
	CTabItem structuralMdItem, textTaggingItem, commentsItem;
	
	CTabItem remoteToolsItem, jobsItem, vkItem;
	
	public TrpTabWidget(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(1, false));
		
		tf = createTabFolder(this);

		tf.setBorderVisible(true);
		
		tf.setSelectionBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		initMainTfs();
	}
	
	void initMainTfs() {
//		serverTf = createTabFolder(tf);
//		serverTf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
//		serverTf.setLayout(new FillLayout());
		serverItem = createCTabItem(tf, new Composite(tf, 0), "Server");
		initServerTf();
				
		documentTf = createTabFolder(tf);//		documentTf.setLayout(new FillLayout());
		documentItem = createCTabItem(tf, documentTf, "Document");
		initDocumentTf();
		
		metadataTf = createTabFolder(tf);
		metadataItem = createCTabItem(tf, metadataTf, "Metadata");
		initMetadataTf();
		
		toolsTf = createTabFolder(tf);
		toolsItem = createCTabItem(tf, toolsTf, "Tools");
		initToolsTf();
		
		// set default selection:
		tf.setSelection(serverItem);
		documentTf.setSelection(structureItem);
		metadataTf.setSelection(structuralMdItem);
		toolsTf.setSelection(remoteToolsItem);
		
		addSelectionFontBold();
		
		updateAllSelectedTabs();
	}
	
	void updateAllSelectedTabs() {
		updateSelectedOnTabFolder(tf);
		updateSelectedOnTabFolder(documentTf);
		updateSelectedOnTabFolder(metadataTf);
		updateSelectedOnTabFolder(toolsTf);
	}
	
	void updateSelectedOnTabFolder(CTabFolder tf) {
		for (CTabItem i : tf.getItems()) {
			if (i == tf.getSelection()) {
				i.setFont(Fonts.createBoldFont(i.getFont()));
			} else {
				i.setFont(Fonts.createNormalFont(i.getFont()));
			}
		}
	}
	
	void addSelectionFontBold() {
		SelectionListener sl = new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!(e.item instanceof CTabItem))
					return;
				
				CTabItem item = (CTabItem) e.item;

				updateSelectedOnTabFolder(item.getParent());
			}
		};
		
		tf.addSelectionListener(sl);
		
		documentTf.addSelectionListener(sl);
		metadataTf.addSelectionListener(sl);
		toolsTf.addSelectionListener(sl);
	}
	
	void initServerTf() {
	}
	
	void initDocumentTf() {
		// TODO: create widgets
		Composite c = new Composite(documentTf, 0);
		
//		docoverviewItem = createCTabItem(documentTf, c, Msgs.get2("documents")); // TODO
		
		structureItem = createCTabItem(documentTf, c, Msgs.get2("layout_tab_title"));
//		jobOverviewItem = createCTabItem(leftTabFolder, jobOverviewWidget, Msgs.get2("jobs"));
		versionsItem = createCTabItem(documentTf, c, Msgs.get2("versions"));
		thumbnailItem = createCTabItem(documentTf, c, Msgs.get2("pages"));
	}
	
	void initMetadataTf() {
		Composite c = new Composite(metadataTf, 0);
		
		structuralMdItem = createCTabItem(metadataTf, c, "Structural");
		textTaggingItem = createCTabItem(metadataTf, c, "Tagging");
		commentsItem = createCTabItem(metadataTf, c, "Comments");
		
	}
	
	void initToolsTf() {
		Composite c = new Composite(toolsTf, 0);
		
		remoteToolsItem = createCTabItem(toolsTf, c, "Server Tools");
		jobsItem = createCTabItem(toolsTf, c, "Jobs");
		vkItem = createCTabItem(toolsTf, c, "Virtual Keyboards");
	}
	
	private CTabFolder createTabFolder(Composite parent) {
		CTabFolder tf = new CTabFolder(parent, SWT.BORDER | SWT.FLAT);
		tf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tf.setBorderVisible(true);
		tf.setSelectionBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		return tf;
	}
	
	private CTabItem createCTabItem(CTabFolder tabFolder, Control control, String Text) {
		CTabItem ti = new CTabItem(tabFolder, SWT.NONE);
		ti.setText(Text);
		ti.setControl(control);
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

}
