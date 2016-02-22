package eu.transkribus.swt_gui.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.dea.swt.mytableviewer.ColumnConfig;
import org.dea.swt.mytableviewer.MyTableViewer;
import org.dea.swt.util.DefaultTableColumnViewerSorter;
import org.dea.swt.util.DialogUtil;
import org.dea.swt.util.Images;
import org.dea.swt.util.ImgLoader;
import org.dea.swt.util.SWTUtil;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.client.connection.TrpServerConn;
import eu.transkribus.core.io.LocalDocReader;
import eu.transkribus.core.model.beans.TrpDoc;
import eu.transkribus.core.model.beans.TrpPage;
import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.swt_gui.mainwidget.Storage;

public class TagsViewer extends Composite {
	private final static Logger logger = LoggerFactory.getLogger(TagsViewer.class);
	
	MyTableViewer tv;
	Storage store = Storage.getInstance();
	Label titleLabel;
	
	ArrayList<String> tags;
	Button upBtn, downBtn;
	Composite tableContainer;
	SashForm sf;
	
	boolean showUpDownBtns;
	boolean withCheckboxes;
	
	public static final String Tag_Name = "Tagname";
	
	public static final ColumnConfig[] COLS = new ColumnConfig[] {
		new ColumnConfig(Tag_Name, 200, true)
	};

	public TagsViewer(Composite parent, int style, boolean showUpDownBtns, boolean withCheckboxes, boolean renderOriginalImages) {
		super(parent, style);
		this.setLayout(new GridLayout(1, false));
		
		this.showUpDownBtns = showUpDownBtns;
		this.withCheckboxes = withCheckboxes;
		
		boolean hasBtns = showUpDownBtns || withCheckboxes;
		
		int tableStyle = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		if (withCheckboxes)
			tableStyle |= SWT.CHECK;
		
		titleLabel = new Label(SWTUtil.dummyShell, 0);
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
//		sf = new SashForm(this, SWT.NONE);
//		sf.setLayout(new GridLayout(1, false));
//		sf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		tableContainer = new Composite(this, 0);
		tableContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableContainer.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		tableContainer.setLayout(new GridLayout(1, false));
				
		tv = new MyTableViewer(tableContainer, tableStyle);
		tv.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, hasBtns ? 1 : 2, 1));
		tv.getTable().setHeaderVisible(true);
		tv.setContentProvider(new ArrayContentProvider());
		tv.setLabelProvider(new ITableLabelProvider() {
			@Override public void removeListener(ILabelProviderListener listener) {
			}
			
			@Override public boolean isLabelProperty(Object element, String property) {
				return true;
			}
			
			@Override public void dispose() {
			}
			
			@Override public void addListener(ILabelProviderListener listener) {
			}
			
			@Override public String getColumnText(Object element, int columnIndex) {
				String cn = COLS[columnIndex].name;
				String p = (String) element;
				if (cn.equals(Tag_Name)) {
					return ""+p;
				}				
				return null;
			}
			
			@Override public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}
		});
		
		tv.addColumns(COLS);
		
		if (hasBtns) {
			Composite btns = new Composite(tableContainer, 0);
			btns.setLayout(new RowLayout(SWT.VERTICAL));
			btns.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, true, 1, 1));
			
			if (withCheckboxes) {
				final Button selectAllBtn = new Button(btns, SWT.PUSH);
				selectAllBtn.setImage(Images.TICK);
				selectAllBtn.setToolTipText("Select all");
				
				final Button deselectAllBtn = new Button(btns, SWT.PUSH);
				deselectAllBtn.setImage(Images.CROSS);
				deselectAllBtn.setToolTipText("Deselect all");
				
				SelectionAdapter selectAllLis = new SelectionAdapter() {
					@Override public void widgetSelected(SelectionEvent event) {
						selectAll(event.getSource() == selectAllBtn);
					}
				};
				
				selectAllBtn.addSelectionListener(selectAllLis);
				deselectAllBtn.addSelectionListener(selectAllLis);
			}
						
		}

		
	}
	
	CustomTag getSelectedTag() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		if (sel.isEmpty())
			return null;
		else
			return (CustomTag) sel.getFirstElement();
	}
	
	
	public void setTitle(String title) {
		if (StringUtils.isEmpty(title))
			titleLabel.setParent(SWTUtil.dummyShell);
		else {
			titleLabel.setText(title);
			titleLabel.setParent(this);
			titleLabel.moveAbove(sf);
		}
		layout();
	}
	
	public void setTags(Set<String> tagnames) {
		Assert.assertNotNull("CustomTags cannot be null!", tagnames);
		
		this.tags = new ArrayList<>(tagnames);		
		reloadTags(true);
	}
	
	public List<String> getCustomTags() {
		return tags;
	}
		
	void reloadTags(boolean initCheckState) {
		tv.setInput(tags);
		
		if (initCheckState)
			selectAll(true);

	}
	
	void selectAll(boolean checked) {
		for (TableItem ti : tv.getTable().getItems()) {
			ti.setChecked(checked);
		}
	}
	
	public Set<String> getCheckedList() {
		Set<String> checked = new HashSet<String>();
		for (TableItem ti : tv.getTable().getItems()) {
			if (ti.getChecked()){
				checked.add(ti.getText(0));
				//logger.debug("ti text " + ti.getText(0));
			}
		}
		
		return checked;
	}

	public void setTags(Set<String> tagnames, Set<String> checkedTagnames) {
		Assert.assertNotNull("CustomTags cannot be null!", tagnames);
			
		this.tags = new ArrayList<>(tagnames);	
		tv.setInput(tags);
		for (TableItem ti : tv.getTable().getItems()) {
			ti.setChecked(false);
			for (String s : checkedTagnames){
				if (ti.getText(0).equals(s)){
					ti.setChecked(true);
				}
			}
		}
		
		
	}
		
}
