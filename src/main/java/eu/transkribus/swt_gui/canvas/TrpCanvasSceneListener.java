package eu.transkribus.swt_gui.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpElementCoordinatesComparator;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.*;
import eu.transkribus.swt_canvas.canvas.CanvasMode;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_canvas.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_canvas.canvas.listener.CanvasSceneListener;
import eu.transkribus.swt_canvas.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_canvas.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.exceptions.NoParentLineException;
import eu.transkribus.swt_gui.exceptions.NoParentRegionException;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import eu.transkribus.swt_gui.util.GuiUtil;

public class TrpCanvasSceneListener extends CanvasSceneListener {
	private final static Logger logger = LoggerFactory.getLogger(TrpCanvasSceneListener.class);
	
	TrpMainWidget mainWidget;
	TrpSWTCanvas canvas;

	public TrpCanvasSceneListener(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.canvas = mainWidget.getCanvas();
		
		mainWidget.getScene().addCanvasSceneListener(this);
	}

	@Override
	public void onBeforeShapeAdded(SceneEvent e) {
		logger.debug("before shape added called!");
		ICanvasShape shape = e.getFirstShape();
		try {
			if (shape==null)
				throw new Exception("No shape to add - should not happen...");
							
			// add element to JAXB:
			CanvasMode mode = mainWidget.getCanvas().getMode();
			try {
				ITrpShapeType el = mainWidget.getShapeFactory().createJAXBElementFromShape(shape, mainWidget.getCanvas().getMode(), mainWidget.getCanvas().getFirstSelected());
			} catch (NoParentRegionException | NoParentLineException ex) {
				boolean noRegion = ex instanceof NoParentRegionException;
				String parentType = noRegion ? "region" : "line";
				
				// create parent shape if none existed!
				final boolean CREATE_PARENT_SHAPE_IF_NONE_EXISTS = true;
				if (CREATE_PARENT_SHAPE_IF_NONE_EXISTS) {
				boolean doCreateParentRegion = TrpMainWidget.getTrpSettings().isAutoCreateParent();
				if (!doCreateParentRegion) {
//					doCreateParentRegion =
//							DialogUtil.showYesNoDialog(canvas.getShell(), "Create parent?", "No parent "+parentType+" found - do you want to create it?")==SWT.YES;
					
					MessageDialogWithToggle d = MessageDialogWithToggle.openYesNoQuestion(canvas.getShell(), "Create parent?", "No parent "+parentType+" found - do you want to create it?", 
							"Create always", TrpMainWidget.getTrpSettings().isAutoCreateParent(), null, null);
					int rc = d.getReturnCode();
					logger.debug("answer = "+rc);
					
					doCreateParentRegion = rc==IDialogConstants.YES_ID;
					if (doCreateParentRegion)
						TrpMainWidget.getTrpSettings().setAutoCreateParent(d.getToggleState());
				}
				if (doCreateParentRegion) {
					// first add missing parent region:
					logger.debug("Adding parent "+parentType);
					// determine parent shape:
					ICanvasShape shapeOfParent = null;
					if (mode == TrpCanvasAddMode.ADD_BASELINE) { // for a baseline add a parent line that is extended beyond the bounding box
						CanvasPolyline baselineShape = (CanvasPolyline) shape;
						shapeOfParent = baselineShape.getDefaultPolyRectangle();
					} else  { // for a word or line add parent shape that is almost the same as the shape to add
						shapeOfParent = shape.getBoundsPolygon();
					}
					// backup and set correct mode:
					CanvasMode modeBackup = canvas.getMode();
					canvas.setMode(noRegion ? TrpCanvasAddMode.ADD_TEXTREGION : TrpCanvasAddMode.ADD_LINE);
					// try to add parent region:
					boolean success = canvas.getShapeEditor().addShapeToCanvas(shapeOfParent);
					canvas.setMode(modeBackup);
					if (!success) { // if not successfully added parent shape, abort this operation!
						e.stop = true;
					} else { // if successfully added parent shape, once again try to add original shape
						ITrpShapeType el = mainWidget.getShapeFactory().createJAXBElementFromShape(shape, mainWidget.getCanvas().getMode(), shapeOfParent);
					}
				} else {
					e.stop = true;
				}
				}
				else
					throw ex;
			}
		}
		catch (Throwable ex) {
			mainWidget.onError("Error adding element", "Error adding element", ex);
			e.stop = true;
		}
	}

	@Override
	public void onShapeAdded(SceneEvent e) {
		mainWidget.getScene().updateAllShapesParentInfo();
		mainWidget.getCanvasShapeObserver().addShapeToObserve(e.getFirstShape());
		mainWidget.refreshStructureView();
		if (TrpMainWidget.getTrpSettings().isSelectNewlyCreatedShape())
			mainWidget.getScene().selectObject(e.getFirstShape(), true, false);
		mainWidget.updateTranscriptionWidgetsData();
		mainWidget.getScene().updateSegmentationViewSettings();
	}
	
	@Override
	public void onBeforeShapeRemoved(SceneEvent e) {
//		logger.debug("before shape removed called: "+e.shape);
		
		TrpSettings set = mainWidget.getTrpSettings();
		
		for (ICanvasShape s : e.shapes) {
			ITrpShapeType st = GuiUtil.getTrpShape(s);
			if (st instanceof TrpBaselineType) {
				boolean removeParentLine = set.isDeleteLineIfBaselineDeleted();
				if (!removeParentLine) {
				
					MessageDialogWithToggle d = MessageDialogWithToggle.openYesNoQuestion(canvas.getShell(), "Delete line also?", "Do you also want to delete the parent line of this baseline?", 
							"Delete always", TrpMainWidget.getTrpSettings().isDeleteLineIfBaselineDeleted(), null, null);
					int rc = d.getReturnCode();
					logger.debug("answer = "+rc);
					
					removeParentLine = rc==IDialogConstants.YES_ID;
					if (removeParentLine)
						TrpMainWidget.getTrpSettings().setDeleteLineIfBaselineDeleted(d.getToggleState());
				}

				if (removeParentLine) { // remove parent line!
					logger.debug("TODO: remove parent line!");
					canvas.getShapeEditor().removeShapeFromCanvas(s.getParent());
					e.stop = true;
				}
			}
			
		}
		
	}

	@Override
	public void onShapeRemoved(SceneEvent e) {	
		logger.debug("on shape removed called: "+e.getFirstShape());
		
		try {
			if (e.getFirstShape()==null)
				throw new Exception("No element to remove!");
									
			// a shape was removed - remove it from JAXB also:
			if (e.getFirstShape().getData()!=null) {
				ITrpShapeType s = (ITrpShapeType) e.getFirstShape().getData();
				s.removeFromParent();
				
				// FIXME: what about the links on undo??
				// remove all links related to this shape:
				s.getPage().removeLinks(s);
				
				((ITrpShapeType) e.getFirstShape().getData()).getPage().removeDeadLinks();
				
				//sort children: means create new reading order without the deleted shape
				s.getParentShape().sortChildren(true);
			}
			logger.debug("removed "+e.getFirstShape().getData() +" from JAXB");
		
			mainWidget.updatePageRelatedMetadata();
			mainWidget.getScene().updateAllShapesParentInfo();
			mainWidget.refreshStructureView();
			mainWidget.getScene().selectObject(null, true, false);
			mainWidget.updateTranscriptionWidgetsData();

		}
		catch (Throwable ex) {
			mainWidget.onError("Error removing shape", "Could not remove element from JAXB", ex);
			e.stop = true;
		}		
	}

	@Override
	public void onSelectionChanged(final SceneEvent e) {
		try {
//			if (true) return;
			
			final boolean newFirstSelected = e.getFirstShape() == canvas.getFirstSelected();
			int nSelected = canvas.getNSelected();
			logger.debug("selected data size = "+nSelected+ " new first selected = "+newFirstSelected);
			
//			ITrpShapeType st = TrpUtil.getTrpShape(e.getFirstShape());
//			if (st!=null) {
//				logger.debug("last selected data = "+st+" text = "+st.getUnicodeText());
//			}
			
			logger.debug("main shell enabled: "+mainWidget.getShell().isEnabled());
			
//			if (true) return;
//			Display.getDefault().asyncExec(new Runnable() {
//				@Override public void run() {
//					mainWidget.updatePageRelatedMetadata();
//					mainWidget.updateTreeSelectionFromCanvas();
//					if (newFirstSelected) {
//						mainWidget.selectTranscriptionWidgetOnSelectedShape(e.getFirstShape());
//						mainWidget.updateTranscriptionWidgetsData();
//						mainWidget.getCanvas().updateEditors();
//					}
//				}
//			});
			
			mainWidget.updatePageRelatedMetadata();
			mainWidget.updateTreeSelectionFromCanvas();
			if (newFirstSelected) {
//				mainWidget.selectTranscriptionWidgetOnSelectedShape(e.getFirstShape());
				mainWidget.updateTranscriptionWidgetsData();
				mainWidget.getCanvas().updateEditors();
			}
			/*
			 * to see the width * height information of the selected shape in the page info
			 */
			mainWidget.updatePageInfo();
			
		} catch (Throwable th) {		
			mainWidget.onError("Error updating selection", "Could not update selection from canvas", th);
		}
	}

	@Override
	public void onBeforeShapeMoved(SceneEvent e) {
		
		
		
	}

	@Override
	public void onShapeMoved(SceneEvent e) {
	}

	@Override
	public void onBeforeUndo(SceneEvent e) {
		logger.debug("onBeforeUndo called, type: "+e.op.getType());
	}
	
	@Override
	public void onUndo(SceneEvent e) {
		logger.debug("onUndo called, type: "+e.op.getType());
		if (e.op.getType() == ShapeEditType.DELETE) {
			processUndoDelete(e.op);
		}
		else if (e.op.getType() == ShapeEditType.SPLIT) {
			processUndoSplit(e.op);
		}
		else if (e.op.getType() == ShapeEditType.MERGE) {
			processUndoMerge(e.op);
		}		
	}
	
	private void processUndoMerge(ShapeEditOperation op) {
		logger.debug("processUndoMerge");
		try {
			// reinsert data objects of all formerly removed shapes into the jaxb again: 
			for (ICanvasShape s : op.getShapes()) {
				ITrpShapeType st = GuiUtil.getTrpShape(s);
				
				Integer ro = st.getReadingOrder();
				st.reInsertIntoParent(ro==null ? -1 : ro-1);
				for (ICanvasShape c : s.getChildren(false)) {
					ITrpShapeType cSt = GuiUtil.getTrpShape(c);
					cSt.setParent(st);
					
					ro = st.getReadingOrder();
					cSt.reInsertIntoParent(ro==null ? -1 : ro-1);
				}
			}
			// note: the jaxb element from the merged shape has already been removed by the remove method in canvasscene

			mainWidget.getScene().updateAllShapesParentInfo();
			mainWidget.getScene().updateSegmentationViewSettings();
			mainWidget.refreshStructureView();
			mainWidget.getScene().selectObject(null, true, false);
			mainWidget.updateTranscriptionWidgetsData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			mainWidget.onError("Error undoing", "Could not undo merge operation "+op.getDescription(), e);
		}
	}
	
	private void processUndoSplit(ShapeEditOperation op) {
		logger.debug("processUndoSplit");

		try {
			mainWidget.getTranscriptObserver().setActive(false);
			
			// reset parent shape and all that shit:
			ICanvasShape origShape = op.getFirstShape();
			ITrpShapeType origSt = GuiUtil.getTrpShape(origShape);
			ITrpShapeType s1St = GuiUtil.getTrpShape(op.getNewShapes().get(0));
			ITrpShapeType s2St = GuiUtil.getTrpShape(op.getNewShapes().get(1));
			
			logger.debug("undo split: "+origSt+", parent = "+s1St.getParent()+" op = "+op);
			s1St.removeFromParent();
			s2St.removeFromParent();
			origSt.setParent(s1St.getParent());
			
			Integer ro = origSt.getReadingOrder();
			
			logger.debug("original ro: " + ro);
			
			origSt.reInsertIntoParent(ro==null ? -1 : ro);
						
			for (ICanvasShape s : origShape.getChildren(false)) {
				ITrpShapeType childSt = GuiUtil.getTrpShape(s);
				childSt.removeFromParent();
				childSt.setParent(origSt);
				mainWidget.getCanvasShapeObserver().addShapeToObserve(s);
				childSt.reInsertIntoParent();
				mainWidget.getScene().updateParentInfo(s, false);	
			}
			
//			mainWidget.getScene().updateAllShapesParentInfo();
//			mainWidget.getScene().updateSegmentationViewSettings();
//			mainWidget.refreshStructureView();
//			mainWidget.updateTranscriptionWidgetsData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			mainWidget.onError("Error undoing", "Could not undo split operation "+op.getDescription(), e);
		} finally {
			mainWidget.getTranscriptObserver().setActive(true);
			
			if (!op.isFollowUp()) {
				logger.debug("updating gui after last operation on undo split!");
				mainWidget.getCanvasShapeObserver().updateObserverForAllShapes();
				mainWidget.getScene().updateAllShapesParentInfo();
				mainWidget.getScene().updateSegmentationViewSettings();
				mainWidget.refreshStructureView();
				mainWidget.updateTranscriptionWidgetsData();
				mainWidget.redrawCanvas();
			}
			
		}
	}
	
	private void processUndoDelete(ShapeEditOperation op) {
		logger.debug("processUndoDelete");

		try {
			for (ICanvasShape s : op.getShapes()) {
				mainWidget.getCanvasShapeObserver().addShapeToObserve(s);
				ITrpShapeType st = (ITrpShapeType) s.getData();
				//preserve the old reading order
				Integer ro = st.getReadingOrder();
				st.reInsertIntoParent(ro==null ? -1 : ro);
				mainWidget.getScene().updateParentInfo(s, false);	
			}
			
			mainWidget.getScene().updateAllShapesParentInfo();
			mainWidget.getScene().updateSegmentationViewSettings();
			mainWidget.refreshStructureView();
			mainWidget.updateTranscriptionWidgetsData();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			mainWidget.onError("Error undoing", "Could not undo delete operation "+op.getDescription(), e);
		}
	}

	@Override
	public void onBeforeSplit(SceneEvent e) {
		try {
			logger.debug("before splitting, isFollowUp: "+e.op.isFollowUp()+" shape = "+e.op.getFirstShape());
			
			if (!e.op.isFollowUp() && e.op.getFirstShape().getData() instanceof TrpBaselineType) {
				throw new Exception("Cannot directly split a baseline!");
			}
			
			if (e.op.getFirstShape().getData() instanceof TrpPrintSpaceType) {
				throw new Exception("Cannot split a printspace!");
			}
		} catch (Exception ex) {
			e.stop = true;
			mainWidget.onError("Error during operation", "Could not split elements", ex);
		}
	}

	@Override
	public void onSplit(SceneEvent e) {
		try {
			mainWidget.getTranscriptObserver().setActive(false);
			
			logger.debug("on split, op = "+e.op);
			ICanvasShape origShape = e.op.getFirstShape();
			ITrpShapeType origShapeData = (ITrpShapeType) origShape.getData();
			
			int indexOfOrigShape = -1;
			if (origShapeData.getParentShape() != null)
				indexOfOrigShape = origShapeData.getParentShape().getChildren(false).indexOf(origShapeData);
			
			ICanvasShape s1 = e.op.getNewShapes().get(0);
			ICanvasShape s2 = e.op.getNewShapes().get(1);
			
			// remove orig shape from jaxb:
			if (origShape.getData()!=null) {
				origShapeData.removeFromParent();
				// remove all links related to this shape: FIXME: undo!!
				origShapeData.getPage().removeLinks(origShapeData);
			}
			
			logger.debug("splitting - parents: "+s1.getParent()+"/"+s2.getParent());
			
			// add new elements to JAXB (first second, then first to ensure wright order):
			ITrpShapeType el2 = mainWidget.getShapeFactory().copyJAXBElementFromShapeAndData(s2, indexOfOrigShape);
			s2.setData(el2);
			
			ITrpShapeType el1 = mainWidget.getShapeFactory().copyJAXBElementFromShapeAndData(s1, indexOfOrigShape);
			s1.setData(el1);


			// split text between elements
			final boolean SPLIT_TEXT_ON_AREA_PERCENTAGE = false;
			if (SPLIT_TEXT_ON_AREA_PERCENTAGE) {
				double a1 = s1.area(); double a2 = s2.area();
				double aSum = a1 + a2;
				if (aSum > 0) {
					a1 /= aSum;
					a2 /= aSum;
				}
				String origTxt = origShapeData.getUnicodeText();
				int splitIndex = (int) (origTxt.length() * a1);
				logger.debug("a1 = "+a1+" a2 = "+a2+", text size: "+origTxt.length()+", text split index: "+splitIndex);
				el2.setUnicodeText(origTxt.substring(0, splitIndex), this);
				el1.setUnicodeText(origTxt.substring(Math.min(splitIndex+1, origTxt.length())), this);
			} else { // just assign one (or both) element all the text
//				el1.setUnicodeText("", this);
			}
			
			logger.debug("el1, el2 children = "+el1.getChildren(false).size()+" / "+el2.getChildren(false).size());
			logger.debug("onSplit, s1 n-children = "+s1.getChildren(false).size() +" s2 n-children = "+s2.getChildren(false).size());

			// readjust parents of child shapes:
			mainWidget.getShapeFactory().readjustChildrenForShape(s1, el1);
			mainWidget.getShapeFactory().readjustChildrenForShape(s2, el2);
						
			// Remove deadlinks from page, i.e. links that point to non-existing shapes (not undoable!)
			el1.getPage().removeDeadLinks();
			
			logger.debug("el1, el2 children (2) = "+el1.getChildren(false).size()+" / "+el2.getChildren(false).size());

			if (!e.op.isFollowUp())
				mainWidget.getScene().selectObject(s1, true, false);

		}
		catch (Throwable ex) {
			mainWidget.onError("Error during operation", "Error splitting element", ex);
			e.stop = true;
		} finally {
			mainWidget.getTranscriptObserver().setActive(true);
			
//			if (e.op.isLast()) {
//				logger.debug("updating gui after last operation on split!");
//				mainWidget.getCanvasShapeObserver().updateObserverForAllShapes();
//				mainWidget.getScene().updateAllShapesParentInfo();
//				mainWidget.refreshStructureView();
//				mainWidget.updateTranscriptionWidgetsData();
//				mainWidget.redrawCanvas();
//			}
		}
	}
	
	@Override
	public void onAfterSplit(SceneEvent e) {
		logger.debug("updating gui after split is done!");
		mainWidget.getCanvasShapeObserver().updateObserverForAllShapes();
		mainWidget.getScene().updateAllShapesParentInfo();
		mainWidget.refreshStructureView();
		mainWidget.updateTranscriptionWidgetsData();
		mainWidget.redrawCanvas();
	}

	@Override
	public void onBeforeMerge(SceneEvent e) {
		try {
			logger.debug("before merging");
			
			ITrpShapeType st = GuiUtil.getTrpShape(e.getFirstShape());
			
			for (ICanvasShape s : e.shapes) {
				ITrpShapeType stC = GuiUtil.getTrpShape(s);
				if (st.getClass() != stC.getClass()) {
					throw new Exception("Cannot merge elements of different type!");
				}
				if (st.getParent() != stC.getParent()) {
					throw new Exception("Cannot merge elements with different parent shape!");
				}				
			}
		} catch (Throwable th) {
			e.stop = true;
			mainWidget.onError("Error during operation", "Could not merge elements", th);
		}		
	}

	@Override
	public void onMerge(SceneEvent e) {
		try {
			ICanvasShape newShape = e.op.getNewShapes().get(0);
			logger.debug("merged shape: "+newShape);
						
			String text = "";
			
			// put all ITrpShapeType objects of merged shapes into list and sort it according to their coordinates:
			List<ITrpShapeType> trpMergedShapes = new ArrayList<>();
			
			int minIndex= 10000000;
			for (ICanvasShape s : e.op.getShapes()) {
				ITrpShapeType st = GuiUtil.getTrpShape(s);
				if (st==null)
					throw new Exception("Could not extract the data from a merged shape - should not happen!");
				
				int index = -1;
				if (st.getParentShape() != null)
					index = st.getParentShape().getChildren(false).indexOf(st);
				
				if (index < minIndex)
					minIndex = index;
				
				trpMergedShapes.add(st);
			}
			Collections.sort(trpMergedShapes, new TrpElementCoordinatesComparator<ITrpShapeType>());
			
			ITrpShapeType mergedSt = mainWidget.getShapeFactory().copyJAXBElementFromShapeAndData(newShape, minIndex);
			logger.debug("newshape data: "+((ITrpShapeType)newShape.getData()).print());			

			for (ITrpShapeType st : trpMergedShapes) {				
				text += st.getUnicodeText();
				st.removeFromParent();
				// remove all links related to this shape: FIXME: undo!!
				st.getPage().removeLinks(st);				
			}
			text = StringUtils.removeEnd(text, " ");
			mergedSt.setUnicodeText(text, this);
			logger.debug("newshape data2: "+((ITrpShapeType)newShape.getData()).print());
			
//			mergedSt.reInsertIntoParent();
//			mergedSt.setData(newShape);
			
			boolean baselineSet=false;
			ICanvasShape baselineToRemove = null;
			for (ICanvasShape childShape : newShape.getChildren(false)) {
				ITrpShapeType st = (ITrpShapeType) childShape.getData();
				
				st.removeFromParent();
				
				if ((st instanceof TrpBaselineType)) {
					if (!baselineSet) {
						st.setParent(mergedSt);
						st.reInsertIntoParent();
						baselineSet = true;
					} else {
						baselineToRemove = childShape;
					}
				} else {
					st.setParent(mergedSt);
					st.reInsertIntoParent();					
				}
			}
			if (baselineToRemove!=null)
				mainWidget.getScene().removeShape(baselineToRemove, false, false);
			
			mainWidget.getScene().updateAllShapesParentInfo();
			mainWidget.refreshStructureView();
			mainWidget.getScene().selectObject(newShape, true, false);
			mainWidget.updateTranscriptionWidgetsData();
		} catch (Throwable th) {
			e.stop = true;
			mainWidget.onError("Error merging elements", "Could not merge elements", th);
		}
	}	
	
	@Override
	public void onReadingOrderChanged(SceneEvent e) {
		try {
			//logger.debug("on reading order changed");
			ITrpShapeType st = GuiUtil.getTrpShape(e.getFirstShape());
			String newRo = (String) e.data;
			logger.debug("on reading order changed " + newRo);
			st.removeFromParent();
			//decrease reading order with one to get proper index
			int ro2Idx = Integer.valueOf(newRo)-1;
			st.reInsertIntoParent(ro2Idx);
			//logger.debug("after reinsert " + newRo);
			
			//to store the reading order durable
			st.getObservable().setChangedAndNotifyObservers(new TrpReadingOrderChangedEvent(this));

			
		} catch (Throwable th) {
			e.stop = true;
			mainWidget.onError("Error during operation", "Could not set new reading order", th);
		}		
	}

}
