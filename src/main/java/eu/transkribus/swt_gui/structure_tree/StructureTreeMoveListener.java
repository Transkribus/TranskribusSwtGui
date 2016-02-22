package eu.transkribus.swt_gui.structure_tree;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class StructureTreeMoveListener implements Listener {

	private final static Logger logger = LoggerFactory.getLogger(StructureTreeListener.class);
	
	StructureTreeWidget treeWidget;
	TreeViewer treeViewer;
	Tree tree;
	private int direction;	
	
	public StructureTreeMoveListener(int direction, StructureTreeWidget treeWidget) {
		this.direction = direction;
		this.treeWidget = treeWidget;
	}

	public void handleEvent(Event event) {
		
		if (direction==SWT.UP){
			treeWidget.moveUpItem();
		}
		else{
			treeWidget.moveDownItem();
		}
		

	}
}
