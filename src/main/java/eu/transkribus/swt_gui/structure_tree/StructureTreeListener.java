package eu.transkribus.swt_gui.structure_tree;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.util.SysUtils;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;

public class StructureTreeListener implements ISelectionChangedListener, IDoubleClickListener, KeyListener {
	private final static Logger logger = LoggerFactory.getLogger(StructureTreeListener.class);
	
//	TrpMainWidget mainWidget;
	TreeViewer treeViewer;
	Tree tree;
	
	public boolean isInsideTreeSelectionEvent=false;
			
	public StructureTreeListener(TreeViewer treeViewer, boolean autoAttachAll) {
//		this.mainWidget = TrpMainWidget.getInstance();
		this.treeViewer = treeViewer;
		this.tree = treeViewer.getTree();
		
		if (autoAttachAll) {
			attach();
		}
	}
	
	public void detach() {
		treeViewer.removeSelectionChangedListener(this);
		treeViewer.removeDoubleClickListener(this);
		treeViewer.getTree().removeKeyListener(this);
	}
	
	public void attach() {
		treeViewer.addSelectionChangedListener(this);
		treeViewer.addDoubleClickListener(this);
		treeViewer.getTree().addKeyListener(this);
	}


	@Override
	public void doubleClick(DoubleClickEvent event) {
		TrpMainWidget mainWidget = TrpMainWidget.getInstance();
//		logger.debug("source = "+e.getSource());
		
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object el = selection.getFirstElement();
		
		logger.debug("double click on element: "+el);
		if (el instanceof ITrpShapeType) {
	//		ICanvasShape shape = mainWidget.getScene().findShapeWithData(el);
			ICanvasShape shape = GuiUtil.getCanvasShape((ITrpShapeType)el);
			mainWidget.getCanvas().focusShape(shape, true);
		}
		else if (el instanceof TrpPageType) {
			mainWidget.getCanvas().fitToPage();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		TrpMainWidget mainWidget = TrpMainWidget.getInstance();
		isInsideTreeSelectionEvent = true;
		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				
		int nSelected = selection.size();
		logger.debug("selected size on tree: "+nSelected);

		mainWidget.getCanvas().getScene().selectObject(null, false, false);		
		for (Object el : selection.toArray()) {
			if (el instanceof ITrpShapeType) {
				
				// select baseline if line selected but they are not visible
				if (el instanceof TrpTextLineType && !mainWidget.getTrpSets().isShowLines()) {
					TrpTextLineType line = (TrpTextLineType) el;
					if (line.getBaseline()!=null)
						el = line.getBaseline();
				}
				
				ITrpShapeType s = (ITrpShapeType) el;
				logger.debug("tree selected data:  "+s+" id : "+s.getId());	
				mainWidget.selectObjectWithData(s, true, true);
			}
		}
		mainWidget.getCanvas().redraw();
		isInsideTreeSelectionEvent=false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		TrpMainWidget mainWidget = TrpMainWidget.getInstance();
//		if (true)
//			return;
		int nSelected = tree.getSelection().length;
		logger.trace("key pressed in TreeListener = "+e.keyCode+" nselected = "+nSelected);
				
		if (e.keyCode == SWT.DEL && nSelected > 0 && !Storage.getInstance().isPageLocked()) {
			logger.debug("deleting selected elements from canvas...");
			mainWidget.getCanvas().getShapeEditor().removeSelected();
		}
		else if (e.keyCode == SWT.ARROW_LEFT && nSelected > 0) {
			if (SysUtils.isLinux()) {
				Object o = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
				treeViewer.collapseToLevel(o, 1);
			}
		}
		else if (e.keyCode == SWT.ARROW_RIGHT && nSelected > 0) {
			if (SysUtils.isLinux()) {
				Object o = ((IStructuredSelection)treeViewer.getSelection()).getFirstElement();
				treeViewer.expandToLevel(o, 1);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {		
	}




}
