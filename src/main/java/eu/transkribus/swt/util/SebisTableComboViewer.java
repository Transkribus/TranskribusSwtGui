package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.TrpCollection;

public class SebisTableComboViewer extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(SebisTableComboViewer.class);

//	Composite container;
	
//	Combo combo;
//	Button combo;
//	Button combo;
	Button selected;
	Button arrow;
	
	TableViewer tv;
	Shell popup;
	
	IContentProvider contentProvider;
	TableComboViewerLabelProvider labelProvider;
	
	int selectedLabelColumn = -1;
	
//	ToolBar toolbar;
	
	public static abstract class TableComboViewerLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {}

	public SebisTableComboViewer(Composite parent, int style, ArrayContentProvider contentProvider, TableComboViewerLabelProvider labelProvider) {
		super(parent, style);
		this.setLayout(new FillLayout());
		this.setLayout(SWTUtil.createGridLayout(2, false, 0, 0));
		
//		container = new Composite(this, 0);
		
		
//		toolbar = new ToolBar( this, SWT.NONE );
//		combo = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		
//		selected = new Button(this, SWT.TOGGLE);
		selected = new Button(this, SWT.PUSH);
		selected.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		arrow = new Button(this, SWT.ARROW | SWT.DOWN);
		arrow.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, true, 1, 1));
		
//		combo = new ToolItem( toolBar, SWT.DROP_DOWN );
		
		this.contentProvider = contentProvider == null ? ArrayContentProvider.getInstance() : contentProvider;
		this.labelProvider = labelProvider;
		
		initPopup();
		
//		combo.addListener(SWT.Selection, new Listener() {
//			
//			@Override
//			public void handleEvent(Event event) {
//				event.doit = false;
//				
//			}
//		});
		
		SelectionListener selLis = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				e.doit = false;
				
				initPopup();
				
				System.out.println("setting popup visible: "+popup);
				
				showPopup(!popup.getVisible());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
				
		selected.addSelectionListener(selLis);
		arrow.addSelectionListener(selLis);
		
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				setSelectedText();
				showPopup(!popup.getVisible());
			}
		});
		
		this.getShell().addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				showPopup(popup.isVisible());
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				showPopup(popup.isVisible());				
			}
		});
		
//		popup.addFocusListener(new FocusListener() {
//			
//			@Override
//			public void focusLost(FocusEvent e) {
//				showPopup(false);				
//			}
//			
//			@Override
//			public void focusGained(FocusEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
	}
	
	public void setSelectedLabelColumn(int selectedLabelColumn) {
		this.selectedLabelColumn = selectedLabelColumn;
	}
	
	private void setSelectedText() {
		IStructuredSelection sel = (IStructuredSelection) tv.getSelection();
		
		if (sel == null || sel.isEmpty()) {
			selected.setText("");
//			combo.setItems(new String[]{""});
			return;
		}
			
		String text = "";	
		if (selectedLabelColumn < 0 || selectedLabelColumn >= tv.getTable().getColumnCount()) {
			for (int i=0; i<tv.getTable().getColumnCount(); ++i) {
				text += labelProvider.getColumnText(sel.getFirstElement(), i)+" ";
			}
			text = text.trim();
		} else {
			Font f = labelProvider.getFont(sel.getFirstElement(), selectedLabelColumn);
			selected.setFont(f);
			text = labelProvider.getColumnText(sel.getFirstElement(), selectedLabelColumn);
		}
		
		selected.setText(text);
//		combo.setItems(new String[]{text});
	}
	
	private void showPopup(boolean show) {
		if (!show) {
			popup.setVisible(false);
			return;
		}
		
		Rectangle r = this.getBounds();
//		System.out.println("r = "+r);
		Point pt = this.toDisplay(r.x, r.y);
		popup.setBounds(pt.x, pt.y+r.height, r.width, 300);
		
		popup.setVisible(true);
	}
	
	private void initPopup() {
		if (popup != null && !popup.isDisposed())
			return;
		
		popup = new Shell(getShell(), SWT.NO_TRIM);
		popup.setLayout(new FillLayout());
		tv = new TableViewer(popup, 0);
		tv.setContentProvider(contentProvider);
		tv.setLabelProvider(labelProvider);
		
		setSelectedText();
	}
	
	
	
	public TableColumn addColumn(String text, int width) {
		TableColumn column = new TableColumn(tv.getTable(), SWT.NONE);
        column.setText(text);
        column.setWidth(width);
        
        return column;
    }
	
	public void setContentProvider(IContentProvider provider) {
		tv.setContentProvider(provider);
	}
	
	public void setLabelProvider(ILabelProvider provider) {
		tv.setLabelProvider(provider);
	}
	
	public void setInput(Object input) {
		tv.setInput(input);
	}
	
	static List<TrpCollection> createTestCollections() {
		
		List<TrpCollection> colls = new ArrayList<>();
		for (int i=0; i<1000; ++i) {
			TrpCollection c = new TrpCollection(i, "coll-"+i, "i am coll "+i);
			colls.add(c);
		}
		
		return colls;
	}
	
	public static void main(String[] args) {
		ApplicationWindow aw = new ApplicationWindow(null) {
			@Override
			protected Control createContents(Composite parent) {
				// getShell().setLayout(new FillLayout());
//				getShell().setSize(600, 600);
				
				final SebisTableComboViewer c = new SebisTableComboViewer(parent, 0, ArrayContentProvider.getInstance(), new TableComboViewerLabelProvider() {

					/**
					 * We return null, because we don't support images yet.
					 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
					 */
					public Image getColumnImage (Object element, int columnIndex) {
						return null;
					}

					/**
					 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
					 */
					public String getColumnText (Object element, int columnIndex) {
						System.out.println("HI! "+element+" columIndex = "+columnIndex);
						
						TrpCollection c = (TrpCollection) element;
						
						switch (columnIndex) {
						case 0:
							return c.getColId() + "";
						case 1:
							return c.getColName();
						case 2:
							return (c.getRole() == null ? "Admin" : c.getRole().toString());
						}
						return "";
					}
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
					 */
					public Color getBackground(Object element, int columnIndex) {
						return null;
					}

					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
					 */
					public Color getForeground(Object element, int columnIndex) {
						return null;
					}
					
					/* (non-Javadoc)
					 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
					 */
					public Font getFont(Object element, int index) {
						TrpCollection item = (TrpCollection) element;
						
						if (index == 1) { // highlight collection name bold
							return Fonts.createBoldFont(SWTUtil.dummyShell.getFont());
						}
						
						return null;
					}		

				});
				c.addColumn("ID", 100);
				c.addColumn("Name", 200);
				c.addColumn("Description", 200);
				c.setSelectedLabelColumn(1);
				
//				c.setLabelProvider(new LabelProvider() {
//					@Override public String getText(Object element) {
//						if (element instanceof TrpCollection) {
//							return ((TrpCollection) element).getSummary();
//						}
//						else return "i am error";
//					}
//				});
//				c.setContentProvider(ArrayContentProvider.getInstance());
				c.setInput(createTestCollections());
				
//				InstallSpecificVersionDialog d = new InstallSpecificVersionDialog(getShell(), 0);
//				d.open();
				
				getShell().setSize(500, 200);

				SWTUtil.centerShell(getShell());

				return parent;
			}
		};
		aw.setBlockOnOpen(true);
		aw.open();

		Display.getCurrent().dispose();
		
		
	}

}
