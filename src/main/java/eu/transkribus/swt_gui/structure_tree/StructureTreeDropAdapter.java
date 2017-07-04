package eu.transkribus.swt_gui.structure_tree;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.WordType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;

public class StructureTreeDropAdapter extends ViewerDropAdapter {
	private static final Logger logger = LoggerFactory.getLogger(StructureTreeDropAdapter.class);
	
	public static final boolean ALLOW_DROPPING_ON_DIFFERENT_PARENT_SHAPE = true;
	
	TreeViewer treeViewer;

	public StructureTreeDropAdapter(TreeViewer treeViewer) {
		super(treeViewer);
		this.treeViewer = treeViewer;
	}
	
	private void insertElement(IStructuredSelection selected, ITrpShapeType targetSt, List<?> targetList, boolean insertBefore) {
		Iterator<ITrpShapeType> selectedIt = selected.iterator();
		
		int targetIdx = 0;
		int oldIdx = 0;
				
		while (selectedIt.hasNext()) {
			ITrpShapeType st = selectedIt.next();
			
			oldIdx = targetList.indexOf(st);
						
			//drag form list
			st.removeFromParent();

			//get target idx only after removing drag shape because index changes if dragged before the target
			targetIdx = targetList.indexOf(targetSt);
			boolean isLastElementOrNotFound = targetIdx<0 || targetIdx >= targetList.size()-1;
			
			st.setParent(targetSt.getParent());
						
			if (targetIdx == -1 && oldIdx != -1) {
				st.reInsertIntoParent(oldIdx);
			}
			else {
				if (insertBefore) { // inserting before targetSt
					st.reInsertIntoParent(targetIdx);
				}
				else if (!isLastElementOrNotFound) { // inserting after targetSt
					++targetIdx;
					st.reInsertIntoParent(targetIdx);
				}
				else{ // inserting after targetSt but targetSt is last element
					st.reInsertIntoParent();
				}
			}
			
			logger.debug("ro after reinsert: " + st.getReadingOrder());
		}
		
		logger.debug("Dropped "+(insertBefore ? "before" : "after")+" the target " + targetSt.getId() + " with index: " + targetIdx);
	}

	@Override
	public boolean performDrop(Object arg0) {
		// Note: data is null here since the Transfer type is LocalSelectionTransfer
		logger.debug("performorming drop!");
		
	    // perform dropping of data:
		IStructuredSelection sel = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
		
		ITrpShapeType targetSt = (ITrpShapeType) getCurrentTarget();
		if (targetSt == null)
			return false;
		
		List<TrpRegionType> regions = null;
		List<TextLineType> lines = null;
		List<WordType> words = null;
		
		List<?> targetList = null;
		
		ITrpShapeType parentShape = targetSt.getParentShape();
		TrpTextRegionType parentRegion = null;
		TrpTextLineType parentLine = null;

		if (targetSt instanceof TrpTextLineType){
			//parent is a region
			lines = ((TrpTextRegionType) parentShape).getTextLine();
			targetList = lines;
		}
		else if(targetSt instanceof TrpWordType){
			//parentLine is a line;
			words = ((TrpTextLineType) parentShape).getWord();
			targetList = words;
		}
		else if(targetSt instanceof TrpRegionType){
			//get all regions
			regions = ((TrpRegionType) targetSt).getPage().getTextRegionOrImageRegionOrLineDrawingRegion();
			targetList = regions;
		}
		
		if (targetList == null)
			return false;
		
//		if (targetSt.getReadingOrder() != null) {
//			targetRo = targetSt.getReadingOrder();
//		}
		
		int location = this.determineLocation(getCurrentEvent());					
//		int newRo = 0;

		Iterator<ITrpShapeType> selectedIt = sel.iterator();
		
		switch (location) {
			case LOCATION_BEFORE:
				insertElement(sel, targetSt, targetList, true);
				break;
			case LOCATION_AFTER:
				insertElement(sel, targetSt, targetList, false);
				break;
			case LOCATION_ON:
				// parent switching - remove from current parent and insert into targetSt
				while (selectedIt.hasNext()) {
					ITrpShapeType st = selectedIt.next();
					st.removeFromParent();
					st.setParent(targetSt);
					st.reInsertIntoParent();
				}
				
				logger.debug("Dropped on the target " + targetSt.getId());
				break;
			case LOCATION_NONE:
				logger.debug("Dropped into nothing");
				break;
			default:
				break;
		}
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		targetSt.getPage().sortContent();
		
		TrpMainWidget.getInstance().getCanvas().redraw();
		TrpMainWidget.getInstance().getCanvas().update();
		
		treeViewer.refresh();
		return true;
	}
	
	private static boolean hasSameParent(ITrpShapeType st, String parentId) {
		ITrpShapeType parent = TrpShapeTypeUtils.getParentShape(st);
		if (parent != null) {
			return StringUtils.equals(parentId, parent.getId());
		}
		
		return false;
	}
	
	private static boolean canInsert(IStructuredSelection sel, ITrpShapeType targetSt) {
		Iterator<?> it = sel.iterator();
		
		boolean isRegion = targetSt instanceof TrpTextRegionType;
		boolean isLine = targetSt instanceof TrpTextLineType;
		boolean isWord = targetSt instanceof TrpWordType;
		
		String targetParentId = "parentID";
		ITrpShapeType targetStParent = TrpShapeTypeUtils.getParentShape(targetSt);
		if (targetStParent != null) {
			targetParentId = targetStParent.getId();
		}	
		
		while (it.hasNext()) {
			ITrpShapeType st = (ITrpShapeType) it.next();
			
			if (!ALLOW_DROPPING_ON_DIFFERENT_PARENT_SHAPE && !isRegion && !hasSameParent(st, targetParentId)) {
				return false;
			}
							
			if (isRegion && !(st instanceof TrpTextRegionType))
				return false;
			// target is line but selected not a line -> do not allow drop			
			else if (isLine && !(st instanceof TrpTextLineType))
				return false;
			
			else if (isWord && !(st instanceof TrpWordType))
				return false;
		}
		
		return true;
	}

	@Override public boolean validateDrop(Object target, int operation, TransferData transferType) {
		int location = this.determineLocation(getCurrentEvent());
		
		if (!(target instanceof ITrpShapeType) || target instanceof TrpBaselineType)
			return false;
		
		ITrpShapeType targetSt = (ITrpShapeType) target;
		
		boolean isRegion = targetSt instanceof TrpTextRegionType;
		boolean isLine = targetSt instanceof TrpTextLineType;
		boolean isWord = targetSt instanceof TrpWordType;
		
		if (!isRegion && !isLine && !isWord)
			return false;
		
		if (location == LOCATION_ON) {
			if (ALLOW_DROPPING_ON_DIFFERENT_PARENT_SHAPE) {
				if (!isRegion && !isLine)
					return false;
				
				// now check if all dragged elements are 'addable' to the target:
				IStructuredSelection sel = (IStructuredSelection) treeViewer.getSelection();
				Iterator<?> it = sel.iterator();
				while (it.hasNext()) {
					ITrpShapeType st = (ITrpShapeType) it.next();
					// target is region but selected not a line -> do not allow drop
					if (isRegion && !(st instanceof TrpTextLineType))
						return false;
					// target is line but selected not a word -> do not allow drop			
					else if (isLine && !(st instanceof TrpWordType))
						return false;
				}
			} else { // no parent switching
				return false;
			}
		}
		else if (location == LOCATION_BEFORE) {
			return canInsert((IStructuredSelection) treeViewer.getSelection(), targetSt);
		}
		else if (location == LOCATION_AFTER) {
			return canInsert((IStructuredSelection) treeViewer.getSelection(), targetSt);
		}

		return true;
	}

}
