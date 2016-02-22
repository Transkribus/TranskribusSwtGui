package examples;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class VisualKeyboard extends Composite {
	
	Font font;
	private Table table;

	public VisualKeyboard(Composite parent, int style) throws Exception {
		super(parent, style);
		setLayout(new GridLayout());
				
		init();
	}
	
	private void init() throws Exception {
		InputStream is = null;
//		is = Test.class.getResourceAsStream("/LeedsUni10-12-13.ttf");
		is = Test.class.getResourceAsStream("/Andron Scriptor Web.ttf");
		
		System.out.println("is = "+is);
		
		font = Font.createFont(Font.TRUETYPE_FONT, is);
		font = font.deriveFont(20.0f);
		is.close();
	
		System.out.println("font = "+font);
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(font);
		////////////////////////////
		
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		
		TableViewer tableViewer = new TableViewer(this, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		data.heightHint = 200;
		table.setLayoutData(data);
		
		
		for (int i=0; i<16; i++) {
			TableColumn column = new TableColumn (table, SWT.NONE);
			column.setText (Integer.toHexString(i));
		}
		
		for (int i=0; i<100; ++i) {
			TableItem ti = new TableItem(table, SWT.PUSH);
			for (int j=0; j<16; ++j)
				ti.setText(j, ""+i);
			
			
		     TableEditor editor = new TableEditor(table);
		     editor.grabHorizontal  = true;
		     editor.grabVertical = true;
		     int colI = 0;
		     editor.setEditor(new Button(table, SWT.PUSH), ti, 0);
		     editor.layout();
		     
		     editor.getEditor();
			
		}
		for (int i=0; i<16; i++) {
			table.getColumn (i).pack ();
		}
		
//		tableViewer.setLabelProvider(new ColumnLabelProvider() {
//			  @Override
//			  public void update(ViewerCell cell) {
//			     TableItem item = (TableItem) cell.getItem();
//
//			     Composite buttonPane = new Composite(table, SWT.NONE);
//			     buttonPane.setLayout(new FillLayout());
//
//			     Button button = new Button(buttonPane,SWT.NONE);
//			     button.setText("Edit");
//
//			     button = new Button(buttonPane,SWT.NONE);
//			     button.setText("Remove");
//
//			     button = new Button(buttonPane,SWT.NONE);
//			     button.setText("Deactivate");
//
//			     TableEditor editor = new TableEditor(table);
//			     editor.grabHorizontal  = true;
//			     editor.grabVertical = true;
//			     int colI = 0;
//			     editor.setEditor(buttonPane, item, colI);
//			     editor.layout();
//			     }
//			  });		
	}
	
	public static void main(String [] args) throws Exception {
		Display display = new Display ();
		Shell shell = new Shell(display);
		shell.setLayout(new GridLayout());
		
		VisualKeyboard vs = new VisualKeyboard(shell, SWT.NONE);
		
		
		shell.pack ();
		shell.open ();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}

	

}
