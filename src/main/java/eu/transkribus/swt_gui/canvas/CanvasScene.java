package eu.transkribus.swt_gui.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTagUtil;
import eu.transkribus.core.model.beans.customtags.StructureTag;
import eu.transkribus.core.model.beans.pagecontent.TextStyleType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpShapeTypeUtils;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpReadingOrderChangedEvent;
import eu.transkribus.core.util.CoreUtils;
import eu.transkribus.core.util.TextStyleTypeUtils;
import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.DialogUtil;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation;
import eu.transkribus.swt_gui.canvas.editing.ShapeEditOperation.ShapeEditType;
import eu.transkribus.swt_gui.canvas.listener.ICanvasSceneListener;
import eu.transkribus.swt_gui.canvas.listener.ICanvasSceneListener.SceneEvent;
import eu.transkribus.swt_gui.canvas.listener.ICanvasSceneListener.SceneEventType;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeReadingOrderComparator;
import eu.transkribus.swt_gui.canvas.shapes.CanvasShapeUtil;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.dialogs.ChangeReadingOrderDialog;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.metadata.StructCustomTagSpec;
import eu.transkribus.swt_gui.table_editor.BorderFlags;
import eu.transkribus.swt_gui.table_editor.TableShapeEditOperation;
import eu.transkribus.swt_gui.table_editor.TableUtils;
import eu.transkribus.swt_gui.util.GuiUtil;

/**
 * The scene contains all objects to be drawn, i.e. the main image, sub images
 * and a set of shapes stored in a TreeSet s.t. they are ordered according to
 * their position. <br>
 * Note that images are not intended to be actually stored in the scene - it
 * just stores a reference to them. You have to create and dispose them by
 * yourself!
 * 
 * <p><b>Important note from sebic:</b> all the shape editing stuff is done 
 * in three different classes: CanvasShapeEditor, a controller where every edit operation should start,
 * CanvasScene, where the editing of CanvasShape's is done and CanvasSceneListener, where all ITrpShapeType's related stuff is implemented.
 * This is due to historic reasons, when I wanted to implement a clean separation between the canvas shapes and the PAGE related stuff.
 * This separation has been abandoned however and now the code is scattered across those classes. Sorry for the confusion :-)</p>  
 */
public class CanvasScene {
	private final static Logger logger = LoggerFactory.getLogger(CanvasScene.class);
	
	public static void updateParentShape(ITrpShapeType st) {
			ICanvasShape shape = GuiUtil.getCanvasShape(st);
			if (shape == null)
				return;
					
			if (st.getParentShape() != null) {
				ICanvasShape pShape = (ICanvasShape) st.getParentShape().getData();
	//			logger.debug("parent shape: "+pShape);
				shape.setParentAndAddAsChild(pShape);
			} else {
				shape.setParent(null);
			}
		}

	public static void updateParentInfo(ICanvasShape shape, boolean recursive) {
		ITrpShapeType st = GuiUtil.getTrpShape(shape);
		if (st == null)
			return;
		
		updateParentShape(st);
		if (recursive) {
			for (ITrpShapeType childSt : st.getChildren(recursive)) {
				updateParentShape(childSt);
			}
		}
	}

	protected SWTCanvas canvas;
	protected CanvasImage mainImage = null;
	protected int mainImageWidth = 0;
	protected int mainImageHeight = 0;

	protected HashSet<Image> subimages = new HashSet<Image>();

//	protected TreeSet<ICanvasShape> shapes = new TreeSet<ICanvasShape>();
	protected List<ICanvasShape> shapes = new ArrayList<ICanvasShape>();

	protected List<ICanvasShape> selected = new ArrayList<ICanvasShape>();
	
	boolean allRO = false;
	boolean regionsRO = false;
	boolean linesRO = false;
	boolean wordsRO = false;
	
	boolean transcriptionMode = false;
	
	protected List<ICanvasSceneListener> sceneListener = new ArrayList<>();
	
	public CanvasScene(SWTCanvas canvas) {
		this.canvas = canvas;
	}

	// public void loadMainImage(String urlStr) throws Exception {
	// try {
	// loadMainImage(new URL(urlStr));
	// } catch (MalformedURLException e) {
	// loadMainImage(new URL("file://"+urlStr));
	// }
	// }

	// public void loadMainImage(URL url) throws Exception {
	// CanvasImage im = imCache.getOrPut(url);
	//
	// setMainImage(im);
	// }

	/** Sets the reference to the main image to null */
	public void clearMainImage() {
		setMainImage(null);
	}

	/** Sets the reference to the main image */
	public void setMainImage(CanvasImage img) {
		mainImage = img;
		if (mainImage != null) {
			logger.debug("nr of pixels in loaded image = " + mainImage.nPixels);
		}
//		canvas.fitWidth();
	}
	
	public void setCanvasAutoZoomMode(CanvasAutoZoomMode zoomMode) {
		canvas.setCanvasAutoZoomMode(zoomMode);
	}

	public CanvasImage getMainImage() {
		return mainImage;
	}

	public boolean hasDataToPaint() {
		return mainImage != null;
	}

	public void paint(GC gc) {
		// Draw main image:
		if (mainImage != null && !mainImage.isDisposed()) {
			long t = System.currentTimeMillis();
			mainImage.paint(gc, canvas);
			logger.trace("t-painting-image = "+(System.currentTimeMillis()-t));
		}
		else {
			logger.debug("mainImage: " + mainImage + ", isDisposed: " + mainImage.isDisposed());
		}

		// Draw sub images:
		for (Image image : subimages) {
			gc.drawImage(image, 0, 0);
		}

		gc.setTransform(canvas.getPersistentTransform());
		// Draw shapes:
		// first draw non-selected shapes:
		for (ICanvasShape s : shapes) {
			
			//during transcription only the selected baseline is drawn, lines and regions are always drawn if visible
			if (isTranscriptionMode()) {
				ITrpShapeType trpShape = (ITrpShapeType) s.getData();
				if (!(trpShape instanceof TrpRegionType) && !(trpShape instanceof TrpTextLineType))
					continue;
			}
			
			if (s.isVisible() && !selected.contains(s)){
				s.draw(canvas, gc);
			}
		}
		
		// then draw selected shape over them (to make borders visible for
		// overlapping regions!):
		for (ICanvasShape s : selected) {
			if (s.isVisible())
				s.draw(canvas, gc);
		}

//		for (ICanvasShape s : readingOrderShapes) {
//			//if (s.isVisible())
//				s.draw(canvas, gc);
//		}
		
		
		// TEST - draw border types of table cells
		for (ICanvasShape s : shapes) {
			TrpTableCellType c = TableUtils.getTableCell(s);
			if (c != null) {
				CanvasQuadPolygon qp = (CanvasQuadPolygon) s;
				CanvasSettings sets = canvas.getSettings();
				
				for (int i=0; i<4; ++i) {
					List<java.awt.Point> pts = qp.getPointsOfSegment(i, true);
					int[] ptArr = CoreUtils.getPointArray(pts);

					if (i == 0 && c.isLeftBorderVisible() || i == 1 && c.isBottomBorderVisible() || i == 2 && c.isRightBorderVisible()
							|| i == 3 && c.isTopBorderVisible()) {
						gc.setAlpha(sets.getForegroundAlpha());
						if (s.isSelected()) { // if selected:
							gc.setLineWidth(sets.getSelectedLineWidth() + 4); // set selected line with
							gc.setBackground(s.getColor()); // set background color
						} else {
							gc.setLineWidth(sets.getDrawLineWidth() + 4);
							gc.setBackground(s.getColor());
						}
						gc.setForeground(s.getColor());
						
						// TEST
						gc.setBackground(Colors.getSystemColor(SWT.COLOR_BLACK));
						gc.setForeground(Colors.getSystemColor(SWT.COLOR_BLACK));
						
						gc.setLineStyle(canvas.getSettings().getLineStyle());
	
						gc.setAlpha(sets.getForegroundAlpha());
						gc.drawPolyline(ptArr);
	
					}
				}
			}
		}
		// END OF TEST
		
	}
	

	/**
	 * Returns the bounds of the <em>original</em> image, i.e. not the bounds of
	 * the probably scaled one (which would be getMainImage().img.getBounds())
	 */
	public Rectangle getBounds() {
		if (mainImage != null && !mainImage.isDisposed())
			return mainImage.getBounds();
		else
			return new Rectangle(0, 0, 0, 0);
	}

	public Point getCenter() {
		return new Point(getBounds().width / 2, getBounds().height / 2);
	}

	// public TreeSet<ICanvasShape> getShapes() {
	// return shapes;
	// }
	public List<ICanvasShape> getShapes() {
		return shapes;
	}
	
//	public List<ICanvasShape> getShapesInReversedOrder() {
//		Collections.reverse(shapes);
//		List<ICanvasShape> reversedShapes = shapes;
//		sortShapes();
//		return reversedShapes;
//	}

	public ICanvasShape[] getShapesArray() {
		return (ICanvasShape[]) shapes.toArray();
	}
	
	public void sortShapes() {
		if (true)
			return;
		
		Collections.sort(shapes);
//		logger.debug("sorted shapes: ");
//		for (ICanvasShape s : shapes) {
//			logger.debug("s = "+s);
//			
//		}
	}
	
	/**
	 * Merges the selected shapes. 
	 * @param replaceBaselinesWithLines If true, baselines are replaced by lines and thus merging baselines and lines is treated equally.
	 * @param sendSignal send signals to CanvasSceneListener or not.
	 */
	public ShapeEditOperation mergeSelected(boolean sendSignal, boolean replaceBaselinesWithLines) {
		List<ICanvasShape> selectedShapes = getSelectedAsNewArray();
		
		if (selectedShapes.size() < 2)
			return null;
		
		logger.debug("merging "+selectedShapes.size()+" shapes");

		// replace baseline shapes by lines -> this leads to the effect that baselines and lines are handled equivalent during a merge 
		if (replaceBaselinesWithLines) {
			for (int i=0; i<selectedShapes.size(); ++i) {
				ICanvasShape s = selectedShapes.get(i);
				if (GuiUtil.getTrpShape(s) instanceof TrpBaselineType) {
					selectedShapes.set(i, s.getParent());
				}
			}
		}
		
		// sort shapes if possible
//		CanvasShapeUtil.sortCanvasShapesByReadingOrder(selectedShapes);
		//CanvasShapeUtil.sortCanvasShapesByXY(selectedShapes);
		//this will sort the shapes with YX - like it is the standard now
		//TODO: sort dependent on the situation - merge one upon the other or side by side (if this makes any sense because would you merge two columns into one??)
		CanvasShapeUtil.sortCanvasShapesByCoordinates(selectedShapes, true);
		for (ICanvasShape s : selectedShapes) {
			logger.debug(CanvasShapeUtil.getTrpShapeType(s).getId());
		}
		
		if (sendSignal) {
			if (notifyOnBeforeShapesMerged(selectedShapes)) { // calls the method CanvasSceneListener::onBeforeMerge which checks if all shapes are of same type etc.
				return null;
			}
		}
			
		clearSelected();
		ICanvasShape merged = selectedShapes.get(0).copy();
		for (int i=1; i<selectedShapes.size(); ++i) {
			merged = merged.merge(selectedShapes.get(i));
			if (merged == null)
				return null;
												
			for (ICanvasShape child : selectedShapes.get(i).getChildren(false)) {
				ITrpShapeType st = GuiUtil.getTrpShape(child);
				if (st instanceof TrpBaselineType) { // skip baselines as they get merged explicitly in CanvasSceneListener::onMerge!
					continue;
				}
				
				child.setParentAndAddAsChild(merged);
			}
		}
//		if (isTextLine && newBaseline != null) {
//			logger.debug("newBaseline = "+newBaseline+" pts = "+newBaseline.getPoints());
//			newBaseline.setParentAndAddAsChild(merged);
//		}
		
		//remove all selected shapes
		for (int i=0; i<selectedShapes.size(); ++i){
			removeShape(selectedShapes.get(i), false, false);
		}
				
		/*
		 * resort the points of the merged shape
		 * for some reason the shape points start not at top left but at the lowest level of the shape?? - don't know why the hell!!
		 * must be the result of the splitByPolyline - intersection function (GPCJ library) 
		 * Hence we get the index of the top left point and resort the list
		 * 
		 */
		if (true) {
		List<java.awt.Point> pts1 = merged.getPoints();
		List<java.awt.Point> sortedPts1 = new ArrayList<java.awt.Point>();
		

		int tl = getIndexOfLeftTopPoint(pts1);	
		if (tl != -1){
			sortedPts1.addAll(pts1.subList(tl, pts1.size()));
			sortedPts1.addAll(pts1.subList(0, tl));	
			merged.setPoints(sortedPts1);
		}
		}
				
		//add the merged shape
		ShapeEditOperation opa = addShape(merged, null, false);
		if (opa == null) {
			addShape(selectedShapes.get(0), null, false);
			logger.warn("unable to add merged shape: "+merged);
			return null;
		}
		
		logger.debug(selectedShapes.size()+" shapes merged");
		ShapeEditOperation op = 
				new ShapeEditOperation(ShapeEditType.MERGE, selectedShapes.size()+" shapes merged", selectedShapes);
		op.addNewShape(merged);
		
		if (sendSignal) {
			notifyOnShapesMerged(op); // actual merging of TrpShapeType's is done here, i.e. in the method CanvasSceneListener::onMerge
		}
		
		canvas.redraw();
				
		return op;
		
	}
	
	/**
	 * Splits the given shape by the line running through [x1,y1], [x1, y2] 
	 * @param shape The shape to split
	 * @param sendSignal True if event signal shall be sent
	 * @param p1 The parent shape for the first (left) split; if null, the parent shape of the splitted shape is used
	 * @param p2 The parent shape for the second (right) split; if null, the parent shape of the splitted shape is used
	 * @param isFollowUp Indicates that this is a follow-up split, i.e. a split occuring from splitting a parent shape!
	 * @return A ShapeEditOperation object that contains information on the performed split or null if there was some error
	 */
	public ShapeEditOperation splitShape(ICanvasShape shape, CanvasPolyline pl, boolean sendSignal, ICanvasShape p1, ICanvasShape p2, boolean isFollowUp) {
		if (shape == null)
			return null;
		
		logger.debug("splitting shape "+shape);
		//logger.debug("shape points "+shape.getPoints());

		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.SPLIT, "Shape splitted", shape);
		op.setFollowUp(isFollowUp);
		
		if (sendSignal) {
			if (notifyOnBeforeShapeSplitted(op))
				return null;
		}
		
		Pair<ICanvasShape, ICanvasShape> splits = shape.splitByPolyline(pl);
		logger.debug("splits "+splits);
		if (splits == null)
			return null;
		
		ICanvasShape s1 = splits.getLeft();
		ICanvasShape s2 = splits.getRight();
		
//		logger.debug("shape 1: " + s1.getPoints());
//		logger.debug("shape 2: " + s2.getPoints());
		
		//try to resort the points of the shape:
		List<java.awt.Point> pts1 = s1.getPoints();
		List<java.awt.Point> sortedPts1 = new ArrayList<java.awt.Point>();
		
		List<java.awt.Point> pts2 = s2.getPoints();
		List<java.awt.Point> sortedPts2 = new ArrayList<java.awt.Point>();
		
		/*
		 * 
		 * resort the points
		 * for some reason the shape points start not at top left but at the lowest level of the shape?? - don't know why the hell!!
		 * must be the result of the splitByPolyline - intersection function (GPCJ library) 
		 * Hence we get the index of the top left point and resort the list
		 * But only for text line types - for baselines this is not valid
		 * 
		 */
		
		if (!(shape instanceof TrpBaselineType)){
			int tl = getIndexOfLeftTopPoint(pts1);	
			if (tl != -1){
				sortedPts1.addAll(pts1.subList(tl, pts1.size()));
				sortedPts1.addAll(pts1.subList(0, tl));	
				s1.setPoints(sortedPts1);
			}
			
			int t2 = getIndexOfLeftTopPoint(pts2);	
			if(t2 != -1){
				sortedPts2.addAll(pts2.subList(t2, pts2.size()));
				sortedPts2.addAll(pts2.subList(0, t2));	
				s2.setPoints(sortedPts2);
			}
		}

//		logger.debug("shape 1 sorted: " + s1.getPoints());
//		logger.debug("shape 2 sorted: " + s2.getPoints());

		// remove old shape from parent and set parent for new shapes; also, remove children from new shapes:
		shape.removeFromParent();
		
		if (p1 == null)
			p1 = shape.getParent();
		if (p2 == null)
			p2 = shape.getParent();
		
		// determine correct association of s1-p1 and s2-p2 according to overlap		
		if (p1!=null && p2!=null && s1!=null && s2!=null) {
			if (s1.intersectionArea(p1) < s1.intersectionArea(p2)) {
				logger.debug("switching split <-> parent association!");
				ICanvasShape tmp = p1;
				p1 = p2;
				p2 = tmp;
			}
		}
		
		s1.setParentAndAddAsChild(p1);
		s2.setParentAndAddAsChild(p2);			
		
		s1.removeChildren();
		s2.removeChildren();	
		
		// split up children between the two split shapes s1, s2	
		for (ICanvasShape child : shape.getChildren(false)) {
			// partition children upon splits by 1st area of intersection and 2nd distance to the center of the split shapes
			double a1 = child.intersectionArea(s1);
			double a2 = child.intersectionArea(s2);
			if (a1 < a2) {
				child.setParentAndAddAsChild(s2);
			} else if (a1 > a2) {
				child.setParentAndAddAsChild(s1);
			} else {				
				double d1 = child.distanceToCenter(s1);
				double d2 = child.distanceToCenter(s2);
				if (d1 < d2) {
					child.setParentAndAddAsChild(s1);
				} else {
					child.setParentAndAddAsChild(s2);
				}
			}
		}
						
		op.addNewShape(s1);
		op.addNewShape(s2);
				
		// add the new shapes to the canvas:
		addShape(s1, p1, false);
		addShape(s2, p2, false);
		// remove the old shape from the canvas:
		removeShape(shape, false, false);
		
		if (sendSignal) {
			notifyOnShapeSplitted(op);
		}
		
		return op;
	}
	

	private int getIndexOfLeftTopPoint(List<java.awt.Point> pts) {

		double minDist = Integer.MAX_VALUE;
		int i = 0;
		int idx = -1;
		for (java.awt.Point p : pts){
			//the point top left is closest point to 0,0 and can be found with sqrt
			if (Math.sqrt(p.x*p.x + p.y*p.y) < minDist){
				minDist = Math.sqrt(p.x*p.x + p.y*p.y);
				idx=i;
			}
			i++;
		}
		logger.debug("index " + idx);		
		return idx;
	}

	public ShapeEditOperation addShape(ICanvasShape newShape, ICanvasShape parentShape, boolean sendSignal) {
		if (sendSignal) {
			if (notifyOnBeforeShapeAdded(newShape))
				return null;
		}
		
		if (!shapes.contains(newShape)) { // dont add the same shape twice
			shapes.add(newShape);
			newShape.setParentAndAddAsChild(parentShape);
			
			sortShapes();
			
			if (sendSignal) {
				notifyOnShapeAdded(newShape);
			}

			return new ShapeEditOperation(ShapeEditType.ADD, "Shape added", newShape);
		} else {
			logger.warn("Could not add shape: " + newShape);
			return null;			
		}
	}

	public boolean removeShape(ICanvasShape shape, boolean removeChildren, boolean sendSignal) {
		if (shape==null)
			return false;
		
		if (sendSignal) {
			if (notifyOnBeforeShapeRemoved(shape))
				return false;
		}
		
		boolean wasRemoved = shapes.remove(shape);
//		logger.debug("after removed, contains = "+shapes.contains(shape));
		if (wasRemoved) {
			shape.removeFromParent();
			selected.remove(shape);
			shape.setSelected(false);
			
			if (removeChildren) {
				List<ICanvasShape> children = shape.getChildren(true);
	//			logger.debug("removing, nr of children: "+children.size());
				for (ICanvasShape c : children) {
					shapes.remove(c);
					selected.remove(c);
					c.setSelected(false);
				}
			}
		} else {
			logger.warn("Could not remove shape: " + shape
					+ " (maybe its parent was removed at the same time and thus this element was removed automatically before?)");
		}
		sortShapes();
		
		if (wasRemoved && sendSignal)
			notifyOnShapeRemoved(shape);
		return wasRemoved;
	}

	public boolean moveShape(ICanvasShape shape, int tx, int ty, boolean sendSignal) {
		if (sendSignal) {
			if (notifyOnBeforeShapeMoved(shape, tx, ty))
				return false;
		}

		shape.translate(tx, ty);

		if (sendSignal)
			notifyOnShapeMoved(shape, tx, ty);

		return true;
	}

	public boolean hasShape(ICanvasShape shape) {
		return shape!=null && shapes.contains(shape);
	}
	
	public void clear() {
		clearShapes();
		clearMainImage();
	}
	
	public void clearShapes() {
		selectObject(null, true, false);
		shapes.clear();
		clearSelected();
		
		canvas.getUndoStack().clear();
	}

	public int nShapes() {
		return shapes.size();
	}

	public boolean addSubImage(Image image) {
		return subimages.add(image);
	}

	public boolean removeSubImage(Image image) {
		return subimages.remove(image);
	}

	public boolean hasSubImage(Image image) {
		return subimages.contains(image);
	}

	public void removeAllImages() {
		subimages.clear();
		clearMainImage();
	}
	
	public void clearSelected() {
		for (ICanvasShape sel : shapes) {
			sel.setSelected(false);
		}		
		for (ICanvasShape sel : selected) {
			sel.setSelected(false);
		}
		selected.clear();
	}	
	
//	public void clearReadingOrder() {
//		for (ICanvasShape sel : shapes) {
//			if (sel.isShowReadingOrder()){
//				sel.setShowReadingOrder(!sel.isShowReadingOrder());
//			}
//		}
//	}

//	private void deselectAll() {
//		clearSelected();
//	}

	/**
	 * Iterates through all shapes and returns the list of selected objects
	 */
	public List<ICanvasShape> getSelectedAsNewArray() {
		return new ArrayList<ICanvasShape>(selected);
	}
	
	public List<ICanvasShape> getSelected() {
		return selected;
	}
	
	public List<Object> getSelectedData() {
		List<Object> sd = new ArrayList<Object>();
		for (ICanvasShape s : selected) {
			sd.add(s.getData());
		}
		return sd;
	}
	
	public List<ITrpShapeType> getSelectedTrpShapeTypes() {
		return getSelectedData(ITrpShapeType.class);
	}
	
	public <T> List<T> getSelectedData(Class<T> clazz) {
		List<T> sd = new ArrayList<>();
		for (Object o : getSelectedData()) {
			if (clazz.isAssignableFrom(o.getClass()))
//			if (o.getClass().equals(clazz))
				sd.add((T) o);
		}
		return sd;
	}
	
	public <T> List<ICanvasShape> getSelectedShapesWithData(Class<T> clazz) {
		List<ICanvasShape> sd = new ArrayList<>();
		for (ICanvasShape s : getSelectedAsNewArray()) {
			if (s.getData()!=null && clazz.isAssignableFrom(s.getData().getClass()))
				sd.add(s);
		}
		return sd;
	}
	
	

	/**
	 * Iterates through all shapes and returns the list of selected objects
	 * sorted by time of selection
	 */
	// public List<ICanvasShape> getSelectedSortedByTime() {
	// List<ICanvasShape> sel = getSelected();
	// Collections.sort(sel, new Comparator<ICanvasShape>() {
	// @Override
	// public int compare(ICanvasShape o1, ICanvasShape o2) {
	// return new Long(o1.getSelectedTime()).compareTo(new
	// Long(o2.getSelectedTime()));
	// }
	// });
	// return sel;
	// }

	/** Clears selection out of all shapes except the first selected item */
	public void clearMultiSelection() {
		if (selected.isEmpty())
			return;
		
		ICanvasShape firstSel = selected.get(0);
		clearSelected();
		selected.add(firstSel);
	}

	/**
	 * Returns the edit focused element, i.e. the first or last selected element
	 * when multiple shapes are selected. Whether the first or last element is returned
	 * depends on the focusFirstSelected property in the CanvasSettings
	 */
	public ICanvasShape getEditFocused() {
		return (canvas.getSettings().isFocusFirstSelected()) ? getFirstSelected() : getLastSelected();
	}

	public boolean isEditFocused(ICanvasShape shape) {
		if (shape == null)
			return false;

		return getEditFocused() == shape;
	}

	/**
	 * Returns the shape that was selected first when using multiselect
	 */
	public ICanvasShape getFirstSelected() {
		if (selected.isEmpty())
			return null;
		else
			return selected.get(0);

		// OLD VERSION:
		// return first in list of selected objects or return null if list is
		// empty:
		// List<ICanvasShape> sel = getSelectedSortedByTime();
		// if (sel.size()>0) {
		// return sel.get(0);
		// }
		// return null;
	}

	/**
	 * Returns the shape that was selected last when using multiselect
	 */
	public ICanvasShape getLastSelected() {
		if (selected.isEmpty())
			return null;
		else
			return selected.get(selected.size() - 1);

		// OLD VERSION:
		// return first in list of selected objects or return null if list is
		// empty:
		// List<ICanvasShape> sel = getSelectedSortedByTime();
		// if (sel.size()>0) {
		// return sel.get(sel.size()-1);
		// }
		// return null;
	}

	/**
	 * Returns the shape containing the specified date or null if not found
	 */
	public ICanvasShape findShapeWithData(Object data) {
		for (ICanvasShape shape : getShapes()) {
			if (shape.getData() == data){
				logger.trace("shape found " + shape);
				return shape;
			}
		}
		return null;
	}

	/**
	 * Finds the shape that overlaps with the given shape and has the specified
	 * dataType.<br>
	 * Returns null if no such shape is found. <br>
	 * If two or more overlapping shapes are found the one whose
	 * <em>bounding box</em> overlaps most is returned.
	 */
	public <T> ICanvasShape findOverlappingShapeWithDataType(ICanvasShape shape, Class<T> dataType) {
		ICanvasShape maxShape = null;
		double maxArea = Double.MIN_VALUE;

		for (ICanvasShape s : getShapes()) {
			// logger.debug("dataType: "+s.getData().getClass()+", assform: "+dataType.isAssignableFrom(s.getData().getClass()));
			if (s.getData() == null || !(dataType.isAssignableFrom(s.getData().getClass())))
				continue;

			double area = s.intersectionArea(shape);
			if (area > 0 && area > maxArea) {
				maxArea = area;
				maxShape = s;
			}
		}
		return maxShape;
	}
	
	/**
	 * Finds all lines that are intersected or contained inside a bounding box of a drawn polyline
	 * dataType.<br>
	 * Returns null if no such lines are found. <br>
	 * @throws IOException 
	 */
	public List<ICanvasShape> findArticleLines(ICanvasShape shape) throws IOException {
		if (!shape.getType().equals("POLYLINE")){
			logger.error("Shape must be a polyline for selecting articles!");
			return null;
		}
		
		CanvasPolyline polyline = (CanvasPolyline) shape;
		java.awt.Point startPoint = (polyline.getNPoints() > 0 ? polyline.getPoint(0) : new java.awt.Point(0,0)); 
		java.awt.Point endPoint = startPoint;
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
		
		int nrOfPoints = polyline.getNPoints();
		logger.debug("number of points " + nrOfPoints);
		
		for (int i = 1; i<nrOfPoints; i++){
			java.awt.Point currPoint = polyline.getPoint(i);
			
//			logger.debug("i " + i);
//			logger.debug("currPoint.y " + currPoint.y);
//			logger.debug("endPoint.y " + endPoint.y);
			
			int endPointY = endPoint.y;
			
			if (currPoint.y > endPointY){
				//logger.debug("set new endPoint ");
				endPoint = currPoint;
			}
			
			if (currPoint.y <= endPointY|| i == nrOfPoints-1){
				logger.debug("create new rect for finding article lines: rectY: " + startPoint.y + ", rectX: " + startPoint.x + ", rectHeight " + (endPoint.y-startPoint.y) + ", rectWidth " + (endPoint.x-startPoint.x));
				rectangles.add(new Rectangle(startPoint.x, startPoint.y, endPoint.x-startPoint.x, endPoint.y-startPoint.y));
				startPoint = currPoint;
				endPoint = currPoint;
			}
		}
		
		logger.debug("find all article lines in number of total shapes " + this.getShapes().size());
		List<ICanvasShape> allLines = new ArrayList<ICanvasShape>();
		int newArticleNr = 1;
		
		//first get next article number
		for (ICanvasShape currentShape : this.getShapes()){
			if (currentShape.getData() instanceof TrpTextLineType){
				TrpTextLineType tl = (TrpTextLineType) currentShape.getData(); 

				StructureTag structTag = CustomTagUtil.getStructureTag(tl);
				if (structTag != null && structTag.getType().equals("article")){
					String id = (String) structTag.getAttributeValue("id");
										
					//logger.debug("id of article: " + id);
					String articleNr = ( (id != null && id.length() > 0) ? id.substring(1) : "0");
					//logger.debug("articleNr: " + articleNr);
					int nextArtNr = Integer.valueOf(articleNr)+1;
					if (nextArtNr > newArticleNr){
						newArticleNr = nextArtNr++;
					}
				}
			}
		}
		
		logger.debug("current article ID: " + newArticleNr);
					
		boolean isFirstLine = true;
		boolean regionChanged = false;
		
		TrpTextRegionType parentRegion = null;
		TrpTextRegionType previousRegion = null;
		
		int readingOrder = 0;
		for (ICanvasShape currShape : this.getShapes()){
			
			if (currShape.getData() instanceof TrpTextLineType){
									
				//logger.debug(k++ + " th line found.");
				for (Rectangle currRect : rectangles){
//					logger.debug("curr Rect " + currRect.y + " currRect x " + currRect.x + " height " + currRect.height + " width " + currRect.width);
//					logger.debug("curr currShape " + currShape.getY());
					if (currRect.intersects(currShape.getX(), currShape.getY(), currShape.getBounds().width, currShape.getBounds().height)){
						allLines.add(currShape);
						logger.debug("drawn article rectangle intersects this line: " + ((TrpTextLineType) currShape.getData()).getId()); 
						
						TrpTextLineType lineType = (TrpTextLineType) currShape.getData();
						parentRegion = (TrpTextRegionType) lineType.getParentShape();
						
						//get reading order of first region in this article
						if (isFirstLine){
							isFirstLine = false;
							readingOrder = parentRegion.getReadingOrder();
							logger.debug("first region ro: " + readingOrder); 
						}
						
						if (parentRegion != previousRegion){
							if (previousRegion != null){
								readingOrder = readingOrder+1;
							}
							if (previousRegion != null && parentRegion.getReadingOrder() != (readingOrder)){
								logger.debug("reinsert: " + readingOrder); 
								
								setNewReadingOrder(parentRegion, readingOrder);
							}
							previousRegion = parentRegion;
						}
						
						ITrpShapeType st = GuiUtil.getTrpShape(currShape);
						//logger.debug("updating struct type for " + currShape +" type = article, TrpShapeType = "+st);
						
						//CustomTagFactory.getAttribute(tagName, attributeName);
						Storage.getDefaultStructCustomTagSpecs();
						
						if (st != null) {
							StructureTag at = new StructureTag("article");
							
							at.setAttribute("id", "a"+newArticleNr, true);
							st.getCustomTagList().addOrMergeTag(at, null);
							
							String structType = "article_a"+newArticleNr;

							StructCustomTagSpec spec = Storage.getInstance().getStructCustomTagSpec(structType);
							
							if (spec == null) { // tag not found --> create new one and add it to the list with a new color!
								StructureTag newStructTag = new StructureTag(structType);
								newStructTag.setAttribute("id", "a"+newArticleNr, true);
								spec = new StructCustomTagSpec(newStructTag, Storage.getInstance().getNewStructCustomTagColor());
								logger.debug("add undefined article: "+spec);	
								Storage.getInstance().addStructCustomTagSpec(spec);
							}
						}	
					}
				}
			}
		}
		canvas.redraw();
		
		return null;
	}
	
	public void setNewReadingOrder(TrpTextRegionType st, int newRo){
		st.removeFromParent();
		//decrease reading order with one to get proper index
		int ro2Idx = Integer.valueOf(newRo);
		st.setReadingOrder(Integer.valueOf(ro2Idx), CanvasScene.class);
		st.reInsertIntoParent(ro2Idx);
		//logger.debug("after reinsert " + newRo);
		
		//to store the reading order durable
		st.getObservable().setChangedAndNotifyObservers(new TrpReadingOrderChangedEvent(this));
	}

	public ICanvasShape selectObjectWithData(Object data, boolean sendSignal, boolean multiselect) {
		//data is TrpTextlineType
		ICanvasShape shape = findShapeWithData(data);
		if (shape != null) {
			logger.debug("shape is not null");
			selectObject(shape, sendSignal, multiselect);
		}
		return shape;
	}

	public void makeShapeVisible(ICanvasShape shape) {
		if (shape != null && !isShapeVisible(shape)) {
			canvas.focusShape(shape);
		}
		canvas.redraw();
	}

	public boolean isShapeVisible(ICanvasShape shape) {
		if (shape == null) return false;
		
		java.awt.Rectangle r = canvas.getPersistentTransform().transform(shape.getBounds());

		return (canvas.getClientArea().contains(r.x, r.y) && canvas.getClientArea().contains(r.x + r.width, r.y)
				&& canvas.getClientArea().contains(r.x + r.width, r.y + r.height) && canvas.getClientArea().contains(r.x, r.y + r.height));
	}
	
	/**
	 * Selects the given shape if it is contained in this scene
	 * 
	 * @param sendSignal
	 *            Determines a selection event is fired
	 */
	public void selectObject(ICanvasShape shape, boolean sendSignal, boolean multiselect) {
		if (!multiselect) {
			clearSelected();
		}
		
		logger.debug("selecting, sendSignal: "+sendSignal+", multiselect: " + multiselect);
		if (hasShape(shape)) {
			logger.trace("isSelected: "+shape.isSelected()+ " for "+shape.getTrpShapeType().toString());
			shape.setSelected(!shape.isSelected());
			if (shape.isSelected()){
				logger.trace("shape of type " + shape.getType() + " is selected");
				selected.add(shape);
			}
			else{
				logger.trace("removing shape from selected");
				selected.remove(shape);
			}
		}

		if (sendSignal)
			notifyOnSelectionChanged(shape);
	}

	/**
	 * Selects the given shape if it is contained in this scene
	 * 
	 * @param sendSignal
	 *            Determines a selection event is fired
	 */
//	public void selectObject(ICanvasShape shape, boolean sendSignal, boolean multiselect) {
//		if (!multiselect) {
//			clearSelected();
//			clearReadingOrder();
//			readingOrderShapes.clear();
//		}
//		
//		if( selected.size() == 0){
//			//delete the reading order shapes for text regions when no shape was selected
//			clearReadingOrder();
//			readingOrderShapes.clear();
//		}
////		if (shape == null && !multiselect) {
////			clearSelected();
////		} 
//		if (hasShape(shape)) {
//			logger.debug("selecting, sendSignal: "+sendSignal+", multiselect: " + multiselect+", isSelected: "+shape.isSelected());
//			shape.setSelected(!shape.isSelected());
//			if (shape.isSelected()){
//				selected.add(shape);
//				
//				//add reading order shapes (for all child shapes of the selected shape) to the canvas
//				ITrpShapeType trpShape = (ITrpShapeType) shape.getData();
//
//				for (int i = 0; i < trpShape.getChildren(false).size(); i++){
//					ITrpShapeType currChild = trpShape.getChildren(false).get(i);
//					ICanvasShape cs = (ICanvasShape)currChild.getData();
//					cs.setShowReadingOrder(true);
////					java.awt.Rectangle boundingRect = cs.getBounds();
////					CanvasRect cr = new CanvasRect(boundingRect.x-30, boundingRect.y, 20, 20);
////					if (!readingOrderShapes.contains(cr)){
////						readingOrderShapes.add(cr);
////					}
//				}
//
//			}
//			else{
//				
//				selected.remove(shape);
//
//				//remove all shape childs of the deselected shapes
//				List<ICanvasShape> children = shape.getChildren(true);
//				for (ICanvasShape c : children) {
//					c.setShowReadingOrder(false);
//				}
//			}
//		}
//
//		if (sendSignal)
//			notifyOnSelectionChanged(shape);
//	}
		
	public List<ICanvasShape> selectObjects(Rectangle rect, boolean sendSignal, boolean multiselect) {
		return selectObjects(rect.x, rect.y, rect.width, rect.height, sendSignal, multiselect);
	}
	
	public List<ICanvasShape> selectObjects(int x, int y, int w, int h, boolean sendSignal, boolean multiselect) {
		if (!multiselect) 
			clearSelected();
		
		List<ICanvasShape> selected = new ArrayList<ICanvasShape>();
//		boolean first=true;
		for (ICanvasShape s : shapes) {
			if (!s.isVisible() || !s.isSelectable())
				continue;			
			
			boolean intersects = s.intersects(new java.awt.Rectangle(x, y, w, h));
			
			if (intersects) {
				selected.add(s);
				
				selectObject(s, sendSignal, true);
//				first = false;
			}
		}
		return selected;
	}

	/**
	 * Selects an object by coordinates <em>without any transformation</em> (has
	 * to inverted before!)
	 * 
	 * @param sendSignal Determines if a selection event is fired
	 */
	public ICanvasShape selectObject(int x, int y, boolean sendSignal, boolean multiselect) {
		ICanvasShape minShape = null;
		double minDist = Double.MAX_VALUE;	
		boolean found = false;
		
		//go from last shape backwards to select always the highest reading order circle (is drawn over the lower ones) during changing reading order
		List<ICanvasShape> tmpShapes = new ArrayList<ICanvasShape>();
		tmpShapes.addAll(shapes);
		Collections.reverse(tmpShapes);
				
		for (ICanvasShape s : tmpShapes) {
			if (s.isReadingOrderVisible() && s.getReadingOrderCircle().contains(x, y) && !found){
				logger.trace("reading order selected is true for mouse point " + x + " , " + y );
				//Display display = canvas.getDisplay();
				ITrpShapeType trpShape = (ITrpShapeType) s.getData();
				trpShape.getReadingOrderAsInt();
				Shell shell = canvas.getShell();
				ChangeReadingOrderDialog diag = new ChangeReadingOrderDialog(shell, trpShape.getReadingOrderAsInt());
				String changedRo = diag.open(x, y);
				if (changedRo != null && !changedRo.equals("")){
					logger.trace(" new reading order is " + changedRo);
					
					notifyOnReadingOrderChanged(s, changedRo, diag.isDoItForAll());
				}
				found = true;
			}
			
			if (!s.isVisible() || !s.isSelectable())
				continue;

			double dist = s.distance(x, y, true);

			if (dist < 0) { // dist < 0 --> inside!
			 logger.trace("s.data = "+s.getData().toString()+" dist = "+dist+" level = "+s.getLevel());

				dist *= -1;
				// prioritize shapes with a larger level:
				dist += (100 - s.getLevel()) * 1e5;
				logger.trace("dist after prior: "+dist);

				if (dist < minDist) {
					minDist = dist;
					minShape = s;
				}
			}
		}

		selectObject(minShape, sendSignal, multiselect);
		return minShape;
	}
	
	public boolean isTranscriptionMode() {
		return transcriptionMode;
	}

	public void setTranscriptionMode(boolean transcriptionMode) {
		this.transcriptionMode = transcriptionMode;
	}

	public void setAllEditable(boolean val) {
		for (ICanvasShape s : getShapes()) {
			s.setEditable(val);
		}
		if (!canvas.isDisposed()){
			canvas.redraw();
		}
	}
	
	public ShapeEditOperation simplifyShapeByPercentageOfLength(ICanvasShape shape, double perc) {
		if (!hasShape(shape)) {
			return null;
		}
		logger.debug("simplifyShapeByPercentageOfLength, perc = "+perc);
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Polygon simplification", shape);
		shape.simplifyByPercentageOfLength(perc);
		
		return op;
	}
	
	public ShapeEditOperation simplifyShape(ICanvasShape shape, double perc, Integer length) {
		if (!hasShape(shape)) {
			return null;
		}
		
		ShapeEditOperation op = new ShapeEditOperation(ShapeEditType.EDIT, "Polygon simplification", shape);
		if (length == null) {
			length = shape.getPolygonLength();
		}
		double eps = (length * perc)/100.0d;
		logger.debug("simplifying shape with perc: "+perc+" length = "+length+ " eps = "+eps);
		shape.simplify(eps);
		
		return op;
	}

	// event stuff:
	public void addCanvasSceneListener(ICanvasSceneListener listener) {
		sceneListener.add(listener);
	}

	public void removeCanvasSceneListener(ICanvasSceneListener listener) {
		sceneListener.remove(listener);
	}

	public boolean notifyOnSelectionChanged(ICanvasShape shape) {
		SceneEvent e = new SceneEvent(SceneEventType.SELECTION_CHANGED, this, shape);
		canvas.onSelectionChanged(shape);
		return notifyAllListener(e);
	}
	
	public boolean notifyOnBeforeUndo(ShapeEditOperation op) {
		return notifyAllListener(new SceneEvent(SceneEventType.BEFORE_UNDO, this, op));
	}

	public boolean notifyOnUndo(ShapeEditOperation op) {
		return notifyAllListener(new SceneEvent(SceneEventType.UNDO, this, op));
	}	

	public boolean notifyOnBeforeShapeAdded(ICanvasShape shape) {
		return notifyAllListener(new SceneEvent(SceneEventType.BEFORE_ADD, this, shape));
	}

	public boolean notifyOnShapeAdded(ICanvasShape shape) {
		return notifyAllListener(new SceneEvent(SceneEventType.ADD, this, shape));
	}

	public boolean notifyOnBeforeShapeRemoved(ICanvasShape shape) {
		SceneEvent e = new SceneEvent(SceneEventType.BEFORE_REMOVE, this, shape);
		return notifyAllListener(e);
	}

	public boolean notifyOnShapeRemoved(ICanvasShape shape) {
		SceneEvent e = new SceneEvent(SceneEventType.REMOVE, this, shape);
		return notifyAllListener(e);
	}

	public boolean notifyOnBeforeShapeMoved(ICanvasShape shape, int tx, int ty) {
		SceneEvent e = new SceneEvent(SceneEventType.BEFORE_MOVE, this, shape);
		e.data = new Point(tx, ty);
		return notifyAllListener(e);
	}

	public boolean notifyOnShapeMoved(ICanvasShape shape, int tx, int ty) {
		SceneEvent e = new SceneEvent(SceneEventType.MOVE, this, shape);
		e.data = new Point(tx, ty);
		return notifyAllListener(e);
	}
	
	public boolean notifyOnBeforeShapeSplitted(ShapeEditOperation op) {
		SceneEvent e = new SceneEvent(SceneEventType.BEFORE_SPLIT, this, op);
		return notifyAllListener(e);
	}

	public boolean notifyOnShapeSplitted(ShapeEditOperation op) {
		SceneEvent e = new SceneEvent(SceneEventType.SPLIT, this, op);
		return notifyAllListener(e);
	}
	
	public boolean notifyOnAfterShapeSplitted(ShapeEditOperation op) {
		SceneEvent e = new SceneEvent(SceneEventType.AFTER_SPLIT, this, op);
		return notifyAllListener(e);
	}
	
	public boolean notifyOnBeforeShapesMerged(List<ICanvasShape> merged) {
		SceneEvent e = new SceneEvent(SceneEventType.BEFORE_MERGE, this, merged);
		return notifyAllListener(e);
	}
	
	public boolean notifyOnReadingOrderChanged(ICanvasShape changed, String newRo, boolean doItForAllFollowing) {
		SceneEvent e = new SceneEvent(SceneEventType.READING_ORDER_CHANGED, this, changed);
		e.data = new Object[] {newRo, doItForAllFollowing};
		return notifyAllListener(e);
	}

	public boolean notifyOnShapesMerged(ShapeEditOperation op) {
		SceneEvent e = new SceneEvent(SceneEventType.MERGE, this, op);
		return notifyAllListener(e);
	}

	public boolean notifyOnShapeBorderEdited(TableShapeEditOperation op) {
		SceneEvent e = new SceneEvent(SceneEventType.BORDER_CHANGED, this, op);
		logger.debug("notifying on shape border edited");
		return notifyAllListener(e);
	}
	
	public boolean notifyOnShapeBorderRetrieval(List<ICanvasShape> cells, BorderFlags bf) {
		SceneEvent e = new SceneEvent(SceneEventType.BORDER_FLAGS_CALLED, this, cells);
		e.data = bf;
		logger.debug("notifying on shape border retrieval");
		return notifyAllListener(e);
	}
	
	public void updateSegmentationViewSettings() {
		TrpSettings sets = TrpMainWidget.getInstance().getTrpSets();
		logger.trace("trpsets: " + sets.toString());
		
		final boolean SHOW_PS_ON_SHOW_REGIONS = true;
	
		for (ICanvasShape s : getShapes()) {
			if (s.hasDataType(TrpPrintSpaceType.class)) {
				if (SHOW_PS_ON_SHOW_REGIONS) {
					s.setVisible(sets.isShowTextRegions());
				} else {
					s.setVisible(sets.isShowPrintSpace());
				}
				
//				s.setVisible(sets.isShowPrintSpace());				
			}
			if (s.hasDataType(TrpTextRegionType.class)) {
				s.setVisible(sets.isShowTextRegions());
			}
			if (s.hasDataType(TrpTextLineType.class)) {
				s.setVisible(sets.isShowLines());
			}
			if (s.hasDataType(TrpBaselineType.class)) {
				s.setVisible(sets.isShowBaselines());
				s.setBaselineVisibiliy(!sets.isShowOnlySelectedBaseline());
			}
			if (s.hasDataType(TrpWordType.class)) {
				s.setVisible(sets.isShowWords());
			}
		}
		
		canvas.redraw();
	}

	public ICanvasShape selectObjectWithId(String id, boolean sendSignal, boolean multiselect) {
		for (ICanvasShape s : getShapes()) {
			if (s.getData() instanceof ITrpShapeType) {
				ITrpShapeType st = (ITrpShapeType) s.getData();
				if (st.getId().equals(id)) {
					selectObject(s, sendSignal, multiselect);
					return s;
				}
			}
		}
		return null;
	}

	public TextStyleType getCommonTextStyleOfSelected() {
		TextStyleType textStyle = null;
		for (int i=0; i<selected.size(); ++i) {
			ITrpShapeType st = GuiUtil.getTrpShape(selected.get(i));
			if (st == null)
				continue;
			
			if (textStyle == null) {
				textStyle = st.getTextStyle();
			}
			else {
				textStyle = TextStyleTypeUtils.mergeEqualTextStyleTypeFields(textStyle, st.getTextStyle());
			}
		}
		
		return textStyle;
	}
	
	/** Returns true is stopping is requested. **/
	public boolean notifyAllListener(SceneEvent e) {
		boolean stop = false;
		
		for (ICanvasSceneListener sl : sceneListener) {
			if (sl.triggerEventMethod(sl, e)) {
				stop = true;
			}
		}
		return stop;
	}

	public int getNSelected() {
		return selected.size();
	}

	public void updateAllShapesParentInfo() {
		Storage store = Storage.getInstance();
		
		if (!store.hasTranscript() || store.getTranscript().getPage()==null)
			return;
		
		for (ICanvasShape s : shapes) {
			s.setParent(null);
			s.removeChildren();
		}
		
		for (ITrpShapeType st : store.getTranscript().getPage().getAllShapes(true)) {
			updateParentShape(st);
		}
	}

	public List<TrpTableCellType> getSelectedTableCells() {
		return getSelectedData(TrpTableCellType.class);
	}

	public List<ICanvasShape> getSelectedTableCellShapes() {
		return getSelectedShapesWithData(TrpTableCellType.class);
	}

}
