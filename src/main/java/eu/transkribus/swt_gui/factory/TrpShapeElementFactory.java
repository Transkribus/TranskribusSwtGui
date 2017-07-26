package eu.transkribus.swt_gui.factory;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.BaselineType;
import eu.transkribus.core.model.beans.pagecontent.CoordsType;
import eu.transkribus.core.model.beans.pagecontent.TextEquivType;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.RegionTypeUtil;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpBaselineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPageType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpPrintSpaceType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableCellType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTableRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextRegionType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.core.model.beans.pagecontent_trp.observable.TrpObserveEvent.TrpConstructedWithParentEvent;
import eu.transkribus.core.util.PointStrUtils;
import eu.transkribus.swt_gui.canvas.CanvasException;
import eu.transkribus.swt_gui.canvas.CanvasMode;
import eu.transkribus.swt_gui.canvas.SWTCanvas;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolygon;
import eu.transkribus.swt_gui.canvas.shapes.CanvasPolyline;
import eu.transkribus.swt_gui.canvas.shapes.CanvasQuadPolygon;
import eu.transkribus.swt_gui.canvas.shapes.ICanvasShape;
import eu.transkribus.swt_gui.exceptions.BaselineExistsException;
import eu.transkribus.swt_gui.exceptions.NoParentLineException;
import eu.transkribus.swt_gui.exceptions.NoParentRegionException;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.settings.TrpSettings;
import eu.transkribus.swt_gui.mainwidget.storage.Storage;
import eu.transkribus.swt_gui.util.GuiUtil;

/**
 * Collection of static methods to create a TreeItem and / or ICanvasShape objects from a given PAGE data object or vice versa
 */
public class TrpShapeElementFactory {
	private final static Logger logger = LoggerFactory.getLogger(TrpShapeElementFactory.class);
	
	TrpMainWidget mainWidget;
	SWTCanvas canvas;

	public TrpShapeElementFactory(TrpMainWidget mainWidget) {
		this.mainWidget = mainWidget;
		this.canvas = mainWidget.getCanvas();
	}
	
	public void readjustChildrenForShape(ICanvasShape shape, ITrpShapeType parentTrpShape) throws Exception {
		parentTrpShape.removeChildren();
		for (ICanvasShape childShape : shape.getChildren(false)) {
			ITrpShapeType st = GuiUtil.getTrpShape(childShape);
			
			logger.trace("shape type: "+st+" parent shape: "+parentTrpShape);
			
			if (st!=null) {
				st.removeFromParent();
				st.setParent(parentTrpShape);
				st.reInsertIntoParent();
			} else {
				throw new Exception("Fatal error: could not find the data object for the child shape: "+childShape);
			}
			
		}
		logger.trace("n-childs after child readjust: "+parentTrpShape.getChildren(false).size());
	}
	
	/** Synchronizes parent/child and data info between a CanvasShape and a ITrpShapeType. Also sets the color of the shape
	 * according to the ITrpShapeType. Returns the parent shape of the given ICanvasShape */
	public static ICanvasShape syncCanvasShapeAndTrpShape(ICanvasShape shape, ITrpShapeType trpShape) {
		TrpSettings sets = TrpMainWidget.getTrpSettings();
		TrpMainWidget mainWidget = TrpMainWidget.getInstance();
		SWTCanvas canvas = mainWidget.getCanvas();
		
		// update info in shape:
		shape.setColor(TrpSettings.determineColor(sets, trpShape));
		shape.setLevel(trpShape.getLevel());
		
		boolean hasBaseline = false;
		double readingOrderSize = 0;
				
		double baselineY = -1;
		double baselineX = -1;
		if (trpShape.getChildren(false).size() > 0 && trpShape.getChildren(false).get(0) instanceof TrpBaselineType) {
			hasBaseline = true;
			TrpBaselineType baseline = (TrpBaselineType) trpShape.getChildren(false).get(0);
			if (baseline != null){
				String coords1 = baseline.getCoordinates();
				
				List<java.awt.Point> pts1 = PointStrUtils.parsePoints(coords1);
				
				if (pts1.size() > 0){
					baselineX = (int) pts1.get(0).getX();
					baselineY = (int) pts1.get(0).getY();
				}
			}
			//logger.debug("baselineY" + baselineY);
		}
		
//		if (trpShape instanceof RegionType) {
//			for (ITrpShapeType currShape : trpShape.getChildren(false)){
//				if (currShape instanceof TrpTextLineType){
//					TrpTextLineType tl = (TrpTextLineType) currShape;
//					Rectangle tmp = (Rectangle) PageXmlUtils.buildPolygon(tl.getCoords().getPoints()).getBounds();
//					if (readingOrderSize == 0){
//						readingOrderSize = tmp.getHeight()/2;
//					}
//					else{
//						readingOrderSize = (readingOrderSize + tmp.getHeight()/2)/2;
//					}
//					
//				}
//			}
//		}
//		else if (trpShape instanceof TrpTextLineType || trpShape instanceof TrpWordType){
//			TrpTextLineType tl = null;
//			if (trpShape instanceof TrpTextLineType)
//				tl = (TrpTextLineType) trpShape;
//			else
//				tl = (TrpTextLineType) trpShape.getParentShape();
//			
//			Rectangle tmp = (Rectangle) PageXmlUtils.buildPolygon(tl.getCoords().getPoints()).getBounds();
//			readingOrderSize = tmp.getHeight()/2;
//		}
		
		//create reading Order shape for canvas shape
		shape.createReadingOrderShape((SWTCanvas) canvas, trpShape instanceof TrpTextRegionType, trpShape instanceof TrpTextLineType, trpShape instanceof TrpWordType, hasBaseline, baselineX, baselineY);
		
		// needed? also done in onBeforeDrawScene
//		if (trpShape instanceof TrpTextRegionType && !(trpShape instanceof TrpTableCellType)){
//			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderRegions());
//		}
//		else if (trpShape instanceof TrpTextLineType){
//			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderLines());
//		}
//		else if (trpShape instanceof TrpWordType){
//			shape.showReadingOrder(mainWidget.getTrpSets().isShowReadingOrderWords());
//		}

		
		// update parent info for shape:
		ICanvasShape pShape = null;
		if (trpShape.getParentShape() != null) {
			pShape = canvas.getScene().findShapeWithData(trpShape.getParentShape());
			shape.setParentAndAddAsChild(pShape);
		} else {
			shape.setParent(null);
		}
			
		// set new data to this shape:
		shape.setData(trpShape);
		
		// set shape as data of new ITrpShapeType object
		trpShape.setData(shape);		
		// add observer:
		trpShape.addObserver(mainWidget.getTranscriptObserver());
		
		return pShape;
	}
	
	/** Creates a <emph>new</emph> ITrpShapeType element from the given canvas shape where an existing ITrpShape element is
	 * already emebedded. This is needed for the split and merge operations! */
	public ITrpShapeType copyJAXBElementFromShapeAndData(ICanvasShape shape, int index) throws Exception {
		if (Storage.getInstance().getTranscript()==null)
			throw new Exception("No transcript loaded - should not happen!");
		
		ITrpShapeType trpShape = GuiUtil.getTrpShape(shape);
		
		ITrpShapeType copyTrpShape=trpShape.copy();
		
		// update parent info for trpShape:
		logger.debug("setting new parent shape: "+GuiUtil.getTrpShape(shape.getParent())+ " shape: "+shape);
		ITrpShapeType parentTrpShape = GuiUtil.getTrpShape(shape.getParent());
		if (parentTrpShape!=null){
			logger.debug("pareent ungleich null " ); 
			copyTrpShape.setParent(parentTrpShape);
		}
		//in this case parent is the page
		else if (trpShape instanceof TrpRegionType){
			logger.debug("else if : instanceof TrpRegionType"  ); 
			copyTrpShape.setParent(trpShape.getPage());
		}
		else{
			logger.debug("else should not happen" ); 
		}
		
		// set coordinates:
		copyTrpShape.setCoordinates(PointStrUtils.pointsToString(shape.getPoints()), this);
		
		// set corner pts if this is a TrpTableCellType and shape is a CanvasQuadPolygon:
		if (copyTrpShape instanceof TrpTableCellType && shape instanceof CanvasQuadPolygon) {
			String cornerPts = PointStrUtils.cornerPtsToString( ((CanvasQuadPolygon) shape).getCorners());
			((TrpTableCellType) copyTrpShape).setCornerPts(cornerPts, this);
		}
		
		copyTrpShape.reInsertIntoParent(index);				
			
		// sync canvas shape and trp shape info:
		syncCanvasShapeAndTrpShape(shape, copyTrpShape);
		
		return copyTrpShape;		
	}
		
	/** Creates a new ITrpShapeType element from the given shape that was created in the canvas. The CanvasMode m determines
	 * the type of shape that shall be created. 
	 */
	public ITrpShapeType createJAXBElementFromShape(ICanvasShape shape, CanvasMode m, ICanvasShape selectedParentShape) throws NoParentRegionException, NoParentLineException, BaselineExistsException, CanvasException {
		if (Storage.getInstance().getTranscript()==null)
			throw new CanvasException("No transcript loaded - should not happen!");
		
		ITrpShapeType trpShape=null;
		ICanvasShape parentShape = null;
		
		TrpSettings setts = TrpMainWidget.getTrpSettings();
				
		logger.debug("adding - data = "+m.data);
		
		String specialRegionType = (m.data != null && m.data instanceof String) ? (String) m.data : "";
//		String specialRegionType = mainWidget.getCanvasWidget().getToolBar().getSelectedSpecialRegionType();

		if (m.equals(CanvasMode.ADD_PRINTSPACE) || specialRegionType.equals(RegionTypeUtil.PRINTSPACE)) {
			TrpPageType parent = Storage.getInstance().getTranscript().getPage();
			if (parent.getPrintSpace()!=null)
				throw new CanvasException("Printspace already exists!");
			TrpPrintSpaceType ps = createPAGEPrintSpace(shape, parent);
			trpShape = ps;
		}
		else if (m.equals(CanvasMode.ADD_TEXTREGION)) {
			// create text region and add it to the shape:
			TrpPageType parent = Storage.getInstance().getTranscript().getPage();
			TrpTextRegionType tr = createPAGETextRegion(shape, parent);
			trpShape = tr;
		}
		else if (m.equals(CanvasMode.ADD_TABLEREGION)) {
			logger.debug("creating table region...");
			TrpPageType parent = Storage.getInstance().getTranscript().getPage();
			logger.debug("parent = "+parent);
			TrpTableRegionType tr = createPAGETableRegion(shape, parent);
			trpShape = tr;
		}
		else if (m.equals(CanvasMode.ADD_OTHERREGION)) {
			logger.debug("adding special region, type  = "+specialRegionType);
			if (!specialRegionType.isEmpty()) {
				TrpPageType parent = Storage.getInstance().getTranscript().getPage();
				TrpRegionType rt = createRegionType(shape, parent, specialRegionType);
				trpShape = rt;
			} else
				throw new CanvasException("Invalid special region type: "+specialRegionType+" - should not happen!");			
		}
		else if (m.equals(CanvasMode.ADD_LINE)) {
			String errorMsg = "";
			if (setts.isAddLinesToOverlappingRegions()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextRegionType.class);
				errorMsg = "Could not find an overlapping parent text region!";
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextRegionType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent region selected!";
			}
			
			if (parentShape == null)
				throw new NoParentRegionException(errorMsg);
			
			TrpTextRegionType parent = (TrpTextRegionType) parentShape.getData();
			TrpTextLineType tl = createPAGETextLine(shape, parent);
			trpShape = tl;
		}
		else if (m.equals(CanvasMode.ADD_BASELINE)) {
			String errorMsg = "";
			if (setts.isAddBaselinesToOverlappingLines()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextLineType.class);
				errorMsg = "Could not find an overlapping parent line!";	
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextLineType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent line selected!";
			}
			
			if (parentShape == null)
				throw new NoParentLineException(errorMsg);			
			
			TrpTextLineType parent = (TrpTextLineType) parentShape.getData();
			if (parent.getBaseline()!=null)
				throw new BaselineExistsException("Baseline already exists in parent line with id = "+parent.getId()+"\nRemove or edit existing baseline!");
			
			TrpBaselineType bl = createPAGEBaseline(shape, parent);
			trpShape = bl;
		}
		else if (m.equals(CanvasMode.ADD_WORD)) {
			String errorMsg = "";
			if (setts.isAddWordsToOverlappingLines()) {
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTextLineType.class);
				errorMsg = "Could not find an overlapping parent line!";
			}
			else if (selectedParentShape != null && selectedParentShape.getData() instanceof TrpTextLineType) {
				parentShape = selectedParentShape;
				errorMsg = "No parent line selected!";
			}
			
			if (parentShape == null)
				throw new NoParentLineException(errorMsg);
			
			TrpTextLineType parent = (TrpTextLineType) parentShape.getData();
			TrpWordType word = createPAGEWord(shape, parent);
			trpShape = word;
		}
		else if (m.equals(CanvasMode.ADD_TABLECELL)) {
			logger.debug("1 creating tablecell");
			String errorMsg="";
						
			// if selected parent shape is a table, set is a as parent shape
			if (selectedParentShape!=null && selectedParentShape.getData() instanceof TrpTableRegionType)
				parentShape = selectedParentShape;
			else // else: find overlapping parent table
				parentShape = canvas.getScene().findOverlappingShapeWithDataType(shape, TrpTableRegionType.class);
			
			logger.debug("parentShape = "+parentShape);
			if (parentShape == null)
				throw new NoParentRegionException(errorMsg);
			
			TrpTableRegionType parent = (TrpTableRegionType) parentShape.getData();
			logger.debug("parent = "+parent);
			
			TrpTableCellType tc = createPAGETableCell(shape, parent);
			trpShape = tc;
		}
		else {
			throw new CanvasException("No add valid operation specified (should not happen...)");
		}
		
		// sync canvas shape and trp shape info:
		syncCanvasShapeAndTrpShape(shape, trpShape);
		
		return trpShape;		
	}
	
	/**
	 * Creates a canvas shape for the given jaxb shape element
	 */
	public ICanvasShape addCanvasShape(ITrpShapeType trpShape) throws Exception {
		String points = trpShape.getCoordinates();
				
		// create polygon and set wrapped data and color:
		ICanvasShape shape = null;
		if (trpShape instanceof BaselineType) {
			shape = new CanvasPolyline(points);
		}
		else if (trpShape instanceof TrpTableCellType) {
			logger.trace("adding table cell as CanvasQuadPolygon shape");
			shape = new CanvasQuadPolygon(points);
			TrpTableCellType tc = (TrpTableCellType) trpShape;
			
//			int[] corners = PointStrUtils.parseCornerPts(tc.getCoords().getCornerPts());
			int[] corners = PointStrUtils.parseCornerPts(tc.getCornerPts());
			((CanvasQuadPolygon) shape).setCornerPts(corners);
		}
		else {
			shape = new CanvasPolygon(points);
		}
		
		ICanvasShape pShape = syncCanvasShapeAndTrpShape(shape, trpShape);
		
		// add it to the canvas and adjust some stuff:
		canvas.getScene().addShape(shape, pShape, false); // add shape without sending a signal
		mainWidget.getCanvasShapeObserver().addShapeToObserve(shape);
		shape.setEditable(canvas.getSettings().isEditingEnabled());
		
		return shape;
	}
	
	/**
	 * Creates and adds canvas shapes for the given ITrpShapeType object and all its children elements
	 */
	public List<ICanvasShape> addAllCanvasShapes(ITrpShapeType trpShape) throws Exception {
		// add shape for given shape:
		List<ICanvasShape> shapes = new ArrayList<ICanvasShape>();
		shapes.add(addCanvasShape(trpShape));

		// add shape for all subelemetns:
		for (ITrpShapeType t : trpShape.getChildren(true)) {
			shapes.add(addCanvasShape(t));
		}
		
		return shapes;
	}
		
	// Methods to create PAGE elements from a shape:
	private static TrpPrintSpaceType createPAGEPrintSpace(ICanvasShape shape, TrpPageType parent) {
		TrpPrintSpaceType ps = new TrpPrintSpaceType(parent);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		ps.setCoords(coords);
		
		parent.setPrintSpace(ps);
		
		return ps;
	}
	
	private static void assertNotNull(Object o, String objDesc) throws CanvasException {
		if (o == null) {
			throw new CanvasException(objDesc+" cannot be null!");
		}
	}
	
	private static TrpTextRegionType createPAGETextRegion(ICanvasShape shape, TrpPageType parent) {
		assertNotNull(shape, "Shape");
		assertNotNull(parent, "Parent page");
		
		TrpTextRegionType tr = new TrpTextRegionType(parent);
		
		tr.setId(TrpPageType.getUniqueId("region"));
//		tr.setId("region_"+System.currentTimeMillis());
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		tr.setCoords(coords);	
				
		//TODO: add index according to coordinates		int idxOfNewLine = parent.getIndexAccordingToCoordinates(tl);
		int idxOfNewTextRegion = parent.getIndexAccordingToCoordinates(tr);
		logger.debug("idxOfNewTextRegion " + idxOfNewTextRegion);
		
		if (parent.getTextRegionOrImageRegionOrLineDrawingRegion().size() > idxOfNewTextRegion){
			//inserts at specific pos
			tr.setReadingOrder(idxOfNewTextRegion,  TrpShapeElementFactory.class);
			parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(idxOfNewTextRegion, tr);
		}
		else{
			//append list
			tr.setReadingOrder(parent.getTextRegionOrImageRegionOrLineDrawingRegion().size(),  TrpShapeElementFactory.class);
			parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(tr);
		}
				
		//parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(tr);
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		parent.sortRegions();
		
		return tr;
	}
	
	private static TrpTableRegionType createPAGETableRegion(ICanvasShape shape, TrpPageType parent) {		
		TrpTableRegionType tr = new TrpTableRegionType(parent);
		
		tr.setId(TrpPageType.getUniqueId(tr.getName()));
		tr.getObservable().setChangedAndNotifyObservers(new TrpConstructedWithParentEvent(tr));
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		tr.setReadingOrder(-1,  TrpShapeElementFactory.class);		
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		tr.setCoords(coords);	
	
		parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(tr);
		parent.sortRegions();
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		
		return tr;
	}
	
	private static TrpTextLineType createPAGETextLine(ICanvasShape shape, TrpTextRegionType parent) {
		TrpTextLineType tl = new TrpTextLineType(parent);
		
		tl.setId(TrpPageType.getUniqueId("line"));
//		tl.setId("line_"+System.currentTimeMillis());
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		//tl.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		tl.setCoords(coords);			
		
		tl.setTextEquiv(new TextEquivType());
		tl.getTextEquiv().setUnicode("");
		
		int idxOfNewLine = parent.getIndexAccordingToCoordinates(tl);
		logger.debug("idxOfNewLine " + idxOfNewLine);
		
		if (parent.getTextLine().size() > idxOfNewLine){
			//inserts at specific pos
			tl.setReadingOrder(idxOfNewLine,  TrpShapeElementFactory.class);
			parent.getTextLine().add(idxOfNewLine, tl);
		}
		else{
			//append list
			tl.setReadingOrder(parent.getTextLine().size(),  TrpShapeElementFactory.class);
			parent.getTextLine().add(tl);
		}

		if (false)
		for (int i = 0; i<parent.getTextLine().size(); i++) {
			logger.debug(i + "-th line in text " + parent.getTextLine().get(i).getId());	
		}
		
		parent.applyTextFromLines();
		
		TrpMainWidget.getInstance().getScene().updateAllShapesParentInfo();
		parent.sortLines();
//		parent.getPage().sortContent();
		
//		parent.getTextLine().add(tl);
		//parent.sortLines();
		
			
		return tl;
	}
	
	public static TrpBaselineType createPAGEBaseline(ICanvasShape shape, TrpTextLineType parent) {
		TrpBaselineType bl = new TrpBaselineType(parent);
		bl.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		bl.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		parent.setBaseline(bl);
		return bl;
	}
	
	private static TrpWordType createPAGEWord(ICanvasShape shape, TrpTextLineType parent) {
		TrpWordType word = new TrpWordType(parent);
		
		word.setId(TrpPageType.getUniqueId("word"));
//		word.setId("word_"+System.currentTimeMillis());
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		word.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		word.setCoords(coords);	
		
		word.setTextEquiv(new TextEquivType());
		word.getTextEquiv().setUnicode("");
		
		parent.getWord().add(word);
		parent.sortWords();
			
		return word;
	}
	
	private static TrpTableCellType createPAGETableCell(ICanvasShape shape, TrpTableRegionType parent) {
		if (!(shape instanceof CanvasQuadPolygon))
			throw new RuntimeException("table cell shape is not a quad polygon: "+shape);
		
		CanvasQuadPolygon qp = ((CanvasQuadPolygon) shape);
		logger.debug("corners: "+qp.getCorners());
		
		
		TrpTableCellType tc = new TrpTableCellType(parent);
		
		tc.setId(TrpPageType.getUniqueId(tc.getName()));
		
		// TODO: set reading order ???? -> maybe r.o. for table cells is rowwise from left to right...
		
		tc.setCoordinates(PointStrUtils.pointsToString(shape.getPoints()), TrpShapeElementFactory.class);
		tc.setCornerPts(PointStrUtils.cornerPtsToString(qp.getCorners()));
		
		parent.getTableCell().add(tc);
		// TODO: sort table cells??? (most probably not...)
		
		return tc;
	}
	
	/**
	 * Creates a generic region of the given type. Valid types are contained in the field {@link #REGIONS}.<br>
	 * NOTE: currently under testing
	 * @param shape The shape where the points are extracted from
	 * @param parent The parent {@link TrpPageType} object where the created region is added to
	 * @param type The type of region. Valid types are contained in the field {@link #REGIONS}.
	 * @return The created region
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static TrpRegionType createRegionType(ICanvasShape shape, TrpPageType parent, String type) throws CanvasException {
		if (!RegionTypeUtil.isSpecialRegion(type)) {
			throw new CanvasException("This is not a special region type: "+type);
		}
		
		Class<? extends ITrpShapeType> clazz = RegionTypeUtil.getRegionClass(type);
		if (clazz == null) {
			throw new CanvasException("Could not create region of type: "+type);
		}
		
		TrpRegionType rt;
		try {
			rt = (TrpRegionType) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new CanvasException("Could not instantiate region type: "+type, e);
		}
		
		rt.setParent(parent);
		rt.getObservable().setChangedAndNotifyObservers(new TrpConstructedWithParentEvent(rt));
		
		//during creation set the ReadingOrder on the first position - sorting should than merge the shape according to the coordinates
		rt.setReadingOrder(-1,  TrpShapeElementFactory.class);
		
		if (type.equals(RegionTypeUtil.BLACKENING_REGION)) {
			RegionTypeUtil.setRegionTypeTag(rt, RegionTypeUtil.BLACKENING_REGION, null);
		}
		
		rt.setId(TrpPageType.getUniqueId(type));
//		word.setId("word_"+System.currentTimeMillis());
		
		CoordsType coords = new CoordsType();
		coords.setPoints(PointStrUtils.pointsToString(shape.getPoints()));
		rt.setCoords(coords);	
				
		parent.getTextRegionOrImageRegionOrLineDrawingRegion().add(rt);
		parent.sortRegions();
			
		return rt;
	}
	

	

}
