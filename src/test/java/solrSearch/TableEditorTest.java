package solrSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TableEditorTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);

        shell.setLayout(new FillLayout());


        TableViewer viewer = new TableViewer(shell);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        viewer.setContentProvider(new ArrayContentProvider());

        TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("First Name");
        column.setWidth(100);
        TableViewerColumn firstNameCol = new TableViewerColumn(viewer, column);
        firstNameCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Person p = (Person)element;

                return p.getFirstName();
            }

        });

        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Last Name");
        column.setWidth(100);
        TableViewerColumn lastNameCol = new TableViewerColumn(viewer, column);
        lastNameCol.setLabelProvider(new ColumnLabelProvider(){

            @Override
            public String getText(Object element) {
                Person p = (Person)element;

                return "text";
            }

        });




        column = new TableColumn(viewer.getTable(), SWT.NONE);
        column.setText("Actions");
        column.setWidth(100);
        TableViewerColumn actionsNameCol = new TableViewerColumn(viewer, column);
        actionsNameCol.setLabelProvider(new ColumnLabelProvider(){
            //make sure you dispose these buttons when viewer input changes
            Map<Object, Button> buttons = new HashMap<Object, Button>();


            @Override
            public void update(ViewerCell cell) {

                TableItem item = (TableItem) cell.getItem();
                Button button;
                if(buttons.containsKey(cell.getElement()))
                {
                    button = buttons.get(cell.getElement());
                }
                else
                {
                     button = new Button((Composite) cell.getViewerRow().getControl(),SWT.NONE);
                    button.setText("Remove");
                    buttons.put(cell.getElement(), button);
                }
                TableEditor editor = new TableEditor(item.getParent());
                editor.grabHorizontal  = true;
                editor.grabVertical = true;
                editor.setEditor(button , item, cell.getColumnIndex());
                editor.layout();
            }

        });



        Person p1 = new Person();
        p1.setFirstName("George");
        p1.setLastName("Burne");

        Person p2 = new Person();
        p2.setFirstName("Adam");
        p2.setLastName("Silva");

        Person p3 = new Person();
        p3.setFirstName("Nathan");
        p3.setLastName("Cowl");

        List<Person> persons = new ArrayList<Person>();
        persons.add(p1);
        persons.add(p2);
        persons.add(p3);

        viewer.setInput(persons);

        shell.open();
        while(!shell.isDisposed())
        {

            if(!display.readAndDispatch())
            {
                display.sleep();
            }
        }

        display.dispose();

    }


    private static class Person
    {

        String firstName;
        String lastName;

        Person()
        {

        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

    }


}
