package eu.transkribus.swt_gui.canvas.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpElementReadingOrderComparator;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpReadingOrderChangedEvent;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpStructureChangedEvent;
import eu.transkribus.core.util.PageXmlUtils;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.CanvasException;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.CanvasScene;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.exceptions.NoParentLineException;
import eu.transkribus.swt_gui.exceptions.NoParentRegionException;
import eu.transkribus.swt_gui.factory.TrpShapeElementFactory;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.util.GuiUtil;

/**
 * <p><b>Important note from sebic:</b> all the shape editing stuff is done 
 * in three different classes: CanvasShapeEditor, a controller where every edit operation should start,
 * CanvasScene, where the editing of CanvasShape's is done and CanvasSceneListener, where all ITrpShapeType's related stuff is implemented.
 * This is due to historic reasons, when I wanted to implement a clean separation between the canvas shapes and the PAGE related stuff.
 * This separation has been abandoned however and now the code is scattered across those classes. Sorry for the confusion :-)</p> 
 */
public class CanvasSceneListener implements EventListener, ICanvasSceneListener {
	private final static Logger logger = LoggerFactory.getLogger(CanvasSceneListener.class);
	
//	public static enum SceneEventType {
//		BEFORE_UNDO, UNDO, BEFORE_ADD, ADD, BEFORE_REMOVE, REMOVE, BEFORE_MOVE, MOVE, SELECTION_CHANGED,
//		BEFORE_SPLIT, AFTER_SPLIT, SPLIT, BEFORE_MERGE, MERGE, READING_ORDER_CHANGED
//	}
//	
//	@SuppressWarnings({"serial"})
//	static public class SceneEvent extends EventObject {
//		public final SceneEventType type;
////		public ICanvasShape shape=null;
//		public List<ICanvasShape> shapes=new ArrayList<>();
//		public ShapeEditOperation op;
//		public Object data;
//		
//		public boolean stop=false;
//		
//		public SceneEvent(SceneEventType type, CanvasScene scene, ICanvasShape inputShape) {
//			super(scene);
//			this.type = type;
//			
//			this.shapes.add(inputShape);
//		}
//		
//		public SceneEvent(SceneEventType type, CanvasScene scene, Collection<ICanvasShape> inputShapes) {
//			super(scene);
//			this.type = type;
//			for (ICanvasShape s : inputShapes)
//				this.shapes.add(s);
//		}		
//		
//		public SceneEvent(SceneEventType type, CanvasScene scene, ShapeEditOperation op) {
//			super(scene);
//			this.type = type;
//			this.op = op;
//		}
//		
//		public ICanvasShape getFirstShape() { return shapes.isEmpty() ? null : shapes.get(0); }
//
//		@Override
//		public CanvasScene getSource() { return (CanvasScene) source; }
//	}

	protected TrpMainWidget mw;
	protected SWTCanvas canvas;
	protected Shell shell;

	public CanvasSceneListener(TrpMainWidget mw) {
		this.mw = mw;
		this.canvas = mw.getCanvas();
		this.shell = mw.getShell();
		
		mw.getScene().addCanvasSceneListener(this);
	}
		
	@Override
	public void onBeforeShapeAdded(SceneEvent e) {
			logger.debug("before shape added called!");
			ICanvasShape shape = e.getFirstShape();
			try {
				if (shape==null)
					throw new CanvasException("Shape is null in onBeforeShapeAdded method - should not happen...");
								
				// add element to JAXB:
				CanvasMode mode = mw.getCanvas().getMode();
				try {
					
					//for creating article GT
					if (mode.equals(CanvasMode.ADD_ARTICLE)){
						//call method to find all lines which are intersected by a polyline or are inside the bounding rectangle
						//PageXmlUtils.
						logger.debug("Add article to pageXML - TODO: add check if user really wants to add the drawn article");
						logger.debug("pointlist of article: " + shape.getPoints());
						mw.getScene().findArticleLines(shape);
						e.stop = true;
						return;
					}
					
					ITrpShapeType el = mw.getShapeFactory().createJAXBElementFromShape(shape, mw.getCanvas().getMode(), mw.getCanvas().getFirstSelected());
					logger.debug("created trp element: "+el);
					
					TrpTableRegionType table = TableUtils.getTable(shape, false);
					if (table != null) { // create table cell for table!
						logger.debug("creating table cell for table!");
						CanvasQuadPolygon qp = new CanvasQuadPolygon(shape.getBounds());
						qp.setEditable(true);
						
						ITrpShapeType cellEl = mw.getShapeFactory().createJAXBElementFromShape(qp, CanvasMode.ADD_TABLECELL, shape);
						ShapeEditOperation op = canvas.getScene().addShape(qp, shape, false);
						if (op == null) {
							e.stop = true;
						}
						logger.debug("created cell element: "+cellEl);
					}
				} catch (NoParentRegionException | NoParentLineException ex) {
					boolean noRegion = ex instanceof NoParentRegionException;
					String parentType = noRegion ? "region" : "line";
					
					// create parent shape if none existed!
					final boolean CREATE_PARENT_SHAPE_IF_NONE_EXISTS = true;
					boolean askForParentCreation = mode != CanvasMode.ADD_TABLECELL;
					
					if (CREATE_PARENT_SHAPE_IF_NONE_EXISTS && askForParentCreation) {
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
							if (mode == CanvasMode.ADD_BASELINE) { // for a baseline add a parent line that is extended beyond the bounding box
								CanvasPolyline baselineShape = (CanvasPolyline) shape;
								shapeOfParent = baselineShape.getDefaultPolyRectangle();
							} else  { // for a word or line add parent shape that is almost the same as the shape to add
								shapeOfParent = shape.getBoundsPolygon();
							}
							// backup and set correct mode:
							CanvasMode modeBackup = canvas.getMode();
							canvas.setMode(noRegion ? CanvasMode.ADD_TEXTREGION : CanvasMode.ADD_LINE);
							// try to add parent region:
							ShapeEditOperation op = canvas.getShapeEditor().addShapeToCanvas(shapeOfParent, true);
							canvas.setMode(modeBackup);
							if (op == null) { // if not successfully added parent shape, abort this operation!
								e.stop = true;
							} else { // if successfully added parent shape, once again try to add original shape
								ITrpShapeType el = mw.getShapeFactory().createJAXBElementFromShape(shape, mw.getCanvas().getMode(), shapeOfParent);
							}
						} else {
							e.stop = true;
						}
					}
					else {
						DialogUtil.showErrorMessageBox(mw.getShell(), "Error adding element", "No suitable parent region!");
						e.stop = true;
	//					throw ex;
					}
				}
			}
			catch (Throwable ex) {
				mw.onError("Error adding element", "Error adding element", ex);
				e.stop = true;
			}
			finally {
				mw.updateSelectedTranscriptionWidgetData();
				mw.refreshStructureView();
				mw.redrawCanvas();
			}
		}

	@Override
	public void onShapeAdded(SceneEvent e) {		
		mw.getScene().updateAllShapesParentInfo();
		mw.getCanvasShapeObserver().addShapeToObserve(e.getFirstShape());
		mw.refreshStructureView();
		if (TrpMainWidget.getTrpSettings().isSelectNewlyCreatedShape())
			mw.getScene().selectObject(e.getFirstShape(), true, false);
		mw.updateSelectedTranscriptionWidgetData();
		mw.getScene().updateSegmentationViewSettings();
	}

	@Override
	public void onBeforeShapeRemoved(SceneEvent e) {
	//		logger.debug("before shape removed called: "+e.shape);
			
			TrpSettings set = TrpMainWidget.getTrpSettings();
			
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
						logger.debug("removing parent line!");
						canvas.getShapeEditor().removeShapeFromCanvas(s.getParent(), true);
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
				TrpShapeTypeUtils.removeShape(s);
			}
			logger.debug("removed "+e.getFirstShape().getData() +" from JAXB");
		
			mw.updatePageRelatedMetadata();
			mw.getScene().updateAllShapesParentInfo();
			mw.refreshStructureView();
			mw.getScene().selectObject(null, true, false);
			mw.updateSelectedTranscriptionWidgetData();
	
		}
		catch (Throwable ex) {
			mw.onError("Error removing shape", "Could not remove element from JAXB", ex);
			e.stop = true;
		}		
	}

	@Override
	public void onSelectionChanged(final SceneEvent e) {
			try {			
	//			if (true) return;
				
				final boolean newFirstSelected = e.getFirstShape() == canvas.getFirstSelected();
				int nSelected = canvas.getNSelected();
				logger.debug("selected data size = "+nSelected+ " new first selected = "+newFirstSelected + " for shape " + e.shapes.toString());
				
				// TEST: do sth. is table cell is selected
				// update selection for table markup box if and only if selected shapes are TrpTableCellType
				if (e.getFirstShape() != null && e.getFirstShape().getData() instanceof TrpTableCellType)
					canvas.getTableMarkup().set(canvas.getShapeEditor().retrieveExistingBordersForTableCells(canvas.getScene().getSelectedTableCellShapes()));
				
				if (false) {
				if (e.getFirstShape() != null && e.getFirstShape().getData() instanceof TrpTableCellType) {
					TrpTableCellType tc = (TrpTableCellType) e.getFirstShape().getData();
					for (int i=0; i<4; ++i) {
						for (TrpTableCellType n : tc.getNeighborCells(i)) {
							logger.debug("i = "+i+" neighbor = "+n);	
						}
					}
				}
				}
				
	//			ITrpShapeType st = TrpUtil.getTrpShape(e.getFirstShape());
	//			if (st!=null) {
	//				logger.debug("last selected data = "+st+" text = "+st.getUnicodeText());
	//			}
				
				logger.debug("main shell enabled: "+mw.getShell().isEnabled());
				
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
				
				mw.updatePageRelatedMetadata();
				mw.updateTreeSelectionFromCanvas();
				if (newFirstSelected) {
	//				mainWidget.selectTranscriptionWidgetOnSelectedShape(e.getFirstShape());
					mw.updateSelectedTranscriptionWidgetData();
					mw.getCanvas().updateEditors();
				}
				/*
				 * to see the width * height information of the selected shape in the page info
				 */
				mw.updatePageInfo();
				
			} catch (Throwable th) {		
				mw.onError("Error updating selection", "Could not update selection from canvas", th);
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
			mw.getTranscriptObserver().setActive(false);
			
			ITrpShapeType mergedShape = GuiUtil.getTrpShape(op.getNewShapes().get(0));
				
			// reinsert data objects of all formerly removed shapes into the jaxb again: 
			// seems that it needs the backup shapes to get the former baselines
			for (ICanvasShape s : op.getBackupShapes()) {
						
				ITrpShapeType st = GuiUtil.getTrpShape(s);
				
				st.removeFromParent();
				st.setParent(mergedShape.getParent());
								
				Integer ro = st.getReadingOrder();
				st.reInsertIntoParent(ro==null ? -1 : ro-1);
				
				for (ICanvasShape c : s.getChildren(false)) {
					ITrpShapeType cSt = GuiUtil.getTrpShape(c);
					cSt.setParent(st);
					
					ro = st.getReadingOrder();
					cSt.reInsertIntoParent(ro==null ? -1 : ro-1);
				}
				
				mw.getShapeFactory().syncCanvasShapeAndTrpShape(s, st);
			}
			// note: the jaxb element from the merged shape has already been removed by the remove method in canvasscene
	
			mw.getScene().updateAllShapesParentInfo();
			mw.getCanvasShapeObserver().updateObserverForAllShapes();
			mw.getScene().updateSegmentationViewSettings();
			mw.refreshStructureView();
			mw.getScene().selectObject(null, true, false);
			mw.updateSelectedTranscriptionWidgetData();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			mw.onError("Error undoing", "Could not undo merge operation "+op.getDescription(), e);
		} finally {
			mw.getTranscriptObserver().setActive(true);
			mw.redrawCanvas();
		}

	}

	private void processUndoSplit(ShapeEditOperation op) {
			logger.debug("processUndoSplit");
	
			try {
				mw.getTranscriptObserver().setActive(false);
				
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
					mw.getCanvasShapeObserver().addShapeToObserve(s);
					childSt.reInsertIntoParent();
	//				mw.getScene().updateParentInfo(s, false);	
				}
				
	//			mainWidget.getScene().updateAllShapesParentInfo();
	//			mainWidget.getScene().updateSegmentationViewSettings();
	//			mainWidget.refreshStructureView();
	//			mainWidget.updateTranscriptionWidgetsData();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				mw.onError("Error undoing", "Could not undo split operation "+op.getDescription(), e);
			} finally {
				mw.getTranscriptObserver().setActive(true);
				
				mw.getScene().updateAllShapesParentInfo();
				mw.getCanvasShapeObserver().updateObserverForAllShapes();
				
				if (!op.isFollowUp()) {
					logger.debug("updating gui after last operation on undo split!");
					mw.getScene().updateSegmentationViewSettings();
					mw.refreshStructureView();
					mw.updateSelectedTranscriptionWidgetData();
					mw.redrawCanvas();
				}
				
			}
		}

	private void processUndoDelete(ShapeEditOperation op) {
			logger.debug("processUndoDelete");
	
			try {
				for (ICanvasShape s : op.getShapes()) {
					mw.getCanvasShapeObserver().addShapeToObserve(s);
					ITrpShapeType st = (ITrpShapeType) s.getData();
					//preserve the old reading order
					st.reInsertIntoParent(st.getReadingOrderAsInt());
	//				mw.getScene().updateParentInfo(s, false);	
				}
				
				mw.getScene().updateAllShapesParentInfo();
				mw.getScene().updateSegmentationViewSettings();
				mw.refreshStructureView();
				mw.updateSelectedTranscriptionWidgetData();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				mw.onError("Error undoing", "Could not undo delete operation "+op.getDescription(), e);
			}
		}

	@Override
	public void onBeforeSplit(SceneEvent e) {
		try {
			
			logger.debug("before splitting, isFollowUp: "+e.op.isFollowUp()+" shape = "+e.op.getFirstShape());
			
			// this case should never happen, since direct splitting of baselines gets prevented in TrpCanvasScene.splitShape function!
			if (!e.op.isFollowUp() && e.op.getFirstShape().getData() instanceof TrpBaselineType) {
				//bthrow new Exception("Cannot directly split a baseline!");
//				ICanvasShape shape = e.op.getFirstShape();
//				ICanvasShape parentShape = shape.getParent();
//				TrpTextLineType parent = (TrpTextLineType)parentShape.getData();
//				//First step: remove parent shape (=line)
//				canvas.getScene().removeShape(parentShape, false, true);
//				//Second step: add parent as a poly rectangle
//				CanvasMode rememberMode = mw.getCanvas().getMode();
//				mw.getCanvas().setMode(CanvasMode.ADD_LINE); 
//				CanvasPolyline baselineShape = (CanvasPolyline) shape;
//				ICanvasShape shapeOfParent = baselineShape.getDefaultPolyRectangle();
//				ShapeEditOperation op = canvas.getShapeEditor().addShapeToCanvas(shapeOfParent, true);
//				if (op != null){
//					logger.debug("!op not null!");
//					//mw.getShapeFactory().createJAXBElementFromShape(shape, mw.getCanvas().getMode(), shapeOfParent);
//					shape.setParentAndAddAsChild(shapeOfParent);
//					((TrpBaselineType)shape.getData()).reInsertIntoParent();
//				}
//				else{
//					logger.debug("!op is null!");
//				}
//
//				mw.getShapeFactory().syncCanvasShapeAndTrpShape(shapeOfParent, parent);
//				mw.getCanvas().setMode(rememberMode);
//				
////				e.op.getShapes().clear();
////				e.op.getShapes().add(shapeOfParent);
//				mw.getScene().selectObject(shapeOfParent, true, false);
//				shapeOfParent.setSelected(true);
				//After that the normal line split can be called
			}
			
			if (e.op.getFirstShape().getData() instanceof TrpPrintSpaceType) {
				throw new Exception("Cannot split a printspace!");
			}
		} catch (Exception ex) {
			e.stop = true;
			mw.onError("Error during operation", "Could not split elements", ex);
		}
	}

	@Override
	public void onSplit(SceneEvent e) {
			try {
				mw.getTranscriptObserver().setActive(false);
				
				logger.debug("on split, op = "+e.op);
				logger.debug("follow up, op = "+e.op.isFollowUp());
				ICanvasShape origShape = e.op.getFirstShape();
				ITrpShapeType origShapeData = (ITrpShapeType) origShape.getData();
				
				if(!e.op.isFollowUp() && origShapeData instanceof TrpBaselineType){
					//split the parent line and baseline even the line is not visible in the canvas
					//go on with parent line -> baseline is splittet as child later on
					logger.debug("SHOULD BE LINE");
//					origShape = origShape.getParent();
//					origShapeData = (ITrpShapeType) origShape.getData();
				}
				
	//			e.op.data
				
				Integer oldReadingOrder = -1;
				if (origShapeData != null){
					oldReadingOrder = origShapeData.getReadingOrder();
				}
				
				
				int indexOfOrigShape = -1;
				if (origShapeData.getParentShape() != null){
					//get only words of a line to determine its index because the contained baseline otherwise destroys the word index
					if (origShapeData.getParentShape() instanceof TrpTextLineType){
						indexOfOrigShape = ((TrpTextLineType) origShapeData.getParentShape()).getWord().indexOf(origShapeData);
					}
					else{
						indexOfOrigShape = origShapeData.getParentShape().getChildren(false).indexOf(origShapeData);
					}
					
				}				
				
				//for region types: otherwise the splitted elements got inserted at the end of the regions because they have not parent shape
				if (indexOfOrigShape == -1 && oldReadingOrder != null) {
					indexOfOrigShape = oldReadingOrder;
				}
				
				ICanvasShape s1 = e.op.getNewShapes().get(0);
				ICanvasShape s2 = e.op.getNewShapes().get(1);
				
				// remove orig shape from jaxb:
				if (origShape.getData()!=null) {
					origShapeData.removeFromParent();
					// remove all links related to this shape: FIXME: undo!!
					origShapeData.getPage().removeLinks(origShapeData);
				}
				
				logger.debug("splitting - parents: "+s1.getParent()+"/"+s2.getParent());
				
				logger.debug("INDEX OF ORIG SHAPE: "+ indexOfOrigShape);
				
				
				
				// add new elements to JAXB (first second, then first to ensure right order):
				ITrpShapeType el2 = mw.getShapeFactory().copyJAXBElementFromShapeAndData(s2, indexOfOrigShape);
				s2.setData(el2);
				
				ITrpShapeType el1 = mw.getShapeFactory().copyJAXBElementFromShapeAndData(s1, indexOfOrigShape);
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
				mw.getShapeFactory().readjustChildrenForShape(s1, el1);
				mw.getShapeFactory().readjustChildrenForShape(s2, el2);
							
				// Remove deadlinks from page, i.e. links that point to non-existing shapes (not undoable!)
				el1.getPage().removeDeadLinks();
				
				logger.debug("el1, el2 children (2) = "+el1.getChildren(false).size()+" / "+el2.getChildren(false).size());
	
				// select new shape: 
				if (!e.op.isFollowUp())
					mw.getScene().selectObject(s1, true, false);
	
			}
			catch (Throwable ex) {
				mw.onError("Error during operation", "Error splitting element", ex);
				e.stop = true;
			} finally {
				mw.getTranscriptObserver().setActive(true);
				
	//			mainWidget.getScene().updateAllShapesParentInfo();
	//			mainWidget.getCanvasShapeObserver().updateObserverForAllShapes();
				
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
		mw.getCanvasShapeObserver().updateObserverForAllShapes();
		mw.getScene().updateAllShapesParentInfo();
		mw.refreshStructureView();
		mw.updateSelectedTranscriptionWidgetData();
		mw.redrawCanvas();
	}

	@Override
	public void onBeforeMerge(SceneEvent e) {
		try {
			logger.debug("before merging");
			
			ITrpShapeType st = GuiUtil.getTrpShape(e.getFirstShape());
			
			for (ICanvasShape s : e.shapes) {
				ITrpShapeType stC = GuiUtil.getTrpShape(s);
				//&& stC instanceof TrpBaselineType
				if (st.getClass() != stC.getClass() ) {
//					throw new Exception("Cannot merge elements of different type!");
					e.stop = true;
					DialogUtil.showErrorMessageBox(shell, "Error during merge", "Cannot merge elements of different type!");
					return;
				}
				if (st.getParent() != stC.getParent()) {
					e.stop = true;
					DialogUtil.showErrorMessageBox(shell, "Error during merge", "Cannot merge elements with different parent shape!");
					return;
				}
//				if (GuiUtil.getCanvasShape(st)==null) {
//					e.stop = true;
//					DialogUtil.showDetailedErrorMessageBox(canvas.getShell(), "Error during merge", "Could not extract shape for element - should not happen - please report this bug!", null);
//					return;
//				}
			}
		} catch (Throwable th) {
			e.stop = true;
			mw.onError("Error during merge", "Could not merge elements: "+th.getMessage(), th);
		}		
	}

	@Override
	public void onMerge(SceneEvent e) {
			try {
				mw.getTranscriptObserver().setActive(false);
				
				ICanvasShape newShape = e.op.getNewShapes().get(0);
				logger.debug("merged shape: "+newShape);
							
				
				
				// put all ITrpShapeType objects of merged shapes into list and determine the minimal reading order:
				List<ITrpShapeType> trpMergedShapes = new ArrayList<>();
				int minIndex= Integer.MAX_VALUE;
				for (ICanvasShape s : e.op.getShapes()) {
					ITrpShapeType st = GuiUtil.getTrpShape(s);
					if (st==null)
						throw new Exception("Could not extract the data from a merged shape - should not happen!");
									
					Integer oldReadingOrder = st.getReadingOrder();
					int index = -1;
					if (st.getParentShape() != null)
						index = st.getParentShape().getChildren(false).indexOf(st);
					
					//parent shape for region is null -> hence take ro of the smallest shape
					if (index == -1 && oldReadingOrder != null){
						index = oldReadingOrder;
					}
					
					if (index < minIndex)
						minIndex = index;
					
					trpMergedShapes.add(st);
				}
				Collections.sort(trpMergedShapes, new TrpElementReadingOrderComparator<ITrpShapeType>(true));
				
				logger.debug("minIndex = "+minIndex);
				
				ITrpShapeType mergedSt = mw.getShapeFactory().copyJAXBElementFromShapeAndData(newShape, minIndex);
				//logger.debug("newshape data: "+((ITrpShapeType)newShape.getData()).print());
				logger.debug("new shape ro = "+mergedSt.getReadingOrderAsInt());
				
				// gather text of merged shapes and remove them from the PAGE:
				String text = "";
				for (ITrpShapeType st : trpMergedShapes) {
					String delimiter = StringUtils.isEmpty(text) ? "" : " ";
					if (!StringUtils.isEmpty(st.getUnicodeText())) {
						text += delimiter+st.getUnicodeText();
					}
					st.removeFromParent();
					// FIXME links are lost on undo!
					st.getPage().removeLinks(st);
				}
				//text = StringUtils.removeEnd(text, " ");
				
				logger.debug("mergedSt = "+mergedSt+" ro = "+mergedSt.getReadingOrderAsInt());
				mergedSt.reInsertIntoParent(mergedSt.getReadingOrderAsInt());
				mergedSt.setUnicodeText(text, this);
				//logger.debug("newshape data2: "+((ITrpShapeType)newShape.getData()).print());
				
	//			mergedSt.reInsertIntoParent();
	//			mergedSt.setData(newShape);
							
				// assign children				
				logger.trace("nr of children is: " + newShape.getChildren(false).size());
				for (ICanvasShape childShape : newShape.getChildren(false)) {
					ITrpShapeType st = (ITrpShapeType) childShape.getData();
					st.setParent(mergedSt);
					st.reInsertIntoParent();					
				}
				
				// if merged shapes were lines -> merge baselines also!
				if (mergedSt instanceof TrpTextLineType) {
					logger.debug("baseline merge - n-merged shapes: "+trpMergedShapes.size());
					TrpTextLineType mergedTl = (TrpTextLineType) mergedSt;
					
					ICanvasShape newBl = null;
					for (ITrpShapeType st : trpMergedShapes) {
						if (st != null && st instanceof TrpTextLineType) {
							TrpTextLineType tl = (TrpTextLineType) st;
							if (tl.getTrpBaseline() != null && tl.getTrpBaseline().getData() instanceof ICanvasShape) {
								ICanvasShape blShape = (ICanvasShape) tl.getTrpBaseline().getData();
								mw.getScene().removeShape(blShape, false, false);
								newBl = newBl==null ? blShape.copy() : newBl.merge(blShape);
							}
						}
					}
					if (newBl!=null) {
//							CanvasPolyline pl = new CanvasPolyline(blPts);
						newBl.setEditable(true);
						newBl.setParent(newShape);
						mw.getScene().addShape(newBl, newShape, false);
						
						TrpBaselineType bl = TrpShapeElementFactory.createPAGEBaseline(newBl, mergedTl);
						TrpShapeElementFactory.syncCanvasShapeAndTrpShape(newBl, bl);
					}
				}
	
				// update ui stuff
				mw.getScene().updateAllShapesParentInfo();
				if (mergedSt.getParent() instanceof TrpPageType){
					((TrpPageType) mergedSt.getParent()).sortRegions();
				}
				else {
					((ITrpShapeType) mergedSt.getParent()).sortChildren(true);
				}
					
				//((ITrpShapeType) mergedSt.getParent()).sortChildren(true);
				mw.refreshStructureView();
				mw.getScene().selectObject(newShape, true, false);
				mw.updateSelectedTranscriptionWidgetData();
				canvas.redraw();
			} catch (Throwable th) {
				e.stop = true;
				mw.onError("Error merging elements", "Could not merge elements", th);
			} finally {
				//!!otherwise the changes in the canvas will be drawn but not stored at the next save!!
				mw.getCanvasShapeObserver().updateObserverForAllShapes();
				mw.getTranscriptObserver().setActive(true);
				
			}
		}

	@Override
	public void onReadingOrderChanged(SceneEvent e) {
		try {
			//logger.debug("on reading order changed");
			ITrpShapeType st = GuiUtil.getTrpShape(e.getFirstShape());
			String newRo = (String) ((Object[]) e.data)[0];
			boolean doIt4All = (boolean) ((Object[]) e.data)[1];
//			logger.debug("on reading order changed " + newRo);
//			logger.debug("do this for all successors  " + doIt4All);
			
			int successor = 0;
			/*
			 * if this is set the reading order will be changed for all successors of the selected shape as well
			 * e.g. ro '35' changes to '3', afterwards '36' to '4', '37' -> '5', '38' -> '6', and so on till the end of the shape list.
			 */
			if (doIt4All){
//				logger.debug("this shape: " + st.getName());
//				logger.debug("parent shape: " + st.getParent());
				List<ITrpShapeType> allShapes = new ArrayList<ITrpShapeType>();
				if (st.getParent() instanceof TrpPageType){
					allShapes.addAll(((TrpPageType) st.getParent()).getTextRegions(false));
				}
				else{
					allShapes = st.getParentShape().getChildren(false);
				}
	
				for (int i = 0; i<allShapes.size(); i++){
					ITrpShapeType currShape = allShapes.get(i);
					if (allShapes.get(i).getReadingOrder()>=st.getReadingOrder()){
						currShape.removeFromParent();
						//decrease reading order with one to get proper index
						int ro2Idx = Integer.valueOf(newRo)-1+successor++;
						currShape.setReadingOrder(Integer.valueOf(ro2Idx), CanvasScene.class);
						currShape.reInsertIntoParent(ro2Idx);
						//logger.debug("after reinsert " + newRo);
						
						//to store the reading order durable
						currShape.getObservable().setChangedAndNotifyObservers(new TrpReadingOrderChangedEvent(this));
					}
					
				}

			}
			else{
				st.removeFromParent();
				//decrease reading order with one to get proper index
				int ro2Idx = Integer.valueOf(newRo)-1;
				st.setReadingOrder(Integer.valueOf(ro2Idx), CanvasScene.class);
				st.reInsertIntoParent(ro2Idx);
				//logger.debug("after reinsert " + newRo);
				
				//to store the reading order durable
				st.getObservable().setChangedAndNotifyObservers(new TrpReadingOrderChangedEvent(this));
			}
			
			mw.getScene().updateAllShapesParentInfo();
			mw.refreshStructureView();
			canvas.setFocus();
			canvas.redraw();
			canvas.update();

		} catch (Throwable th) {
			e.stop = true;
			mw.onError("Error during operation", "Could not set new reading order", th);
		}		
	}
	
	@Override
	public void onBorderChanged(SceneEvent e) {
		logger.debug("on border changed" + e.toString());
		
		ITrpShapeType st = e.getSource().getSelectedTrpShapeTypes().get(0);
		
		if (st != null)
			st.getObservable().setChangedAndNotifyObservers(new TrpStructureChangedEvent(this));
		
		mw.getCanvasShapeObserver().updateObserverForAllShapes();
		
		
	}

	@Override
	public void onBorderFlagsCalled(SceneEvent e) {
		logger.debug("on border flags called - open / refresh dialog" + e.toString());
		
//		BorderFlags bf = canvas.getShapeEditor().retrieveExistingBordersForTableCells(canvas.getScene().getSelectedTableCellShapes());
//		canvas.getTableMarkup().set((BorderFlags) e.data);
		canvas.getTableMarkup().show();
	}
	
}
