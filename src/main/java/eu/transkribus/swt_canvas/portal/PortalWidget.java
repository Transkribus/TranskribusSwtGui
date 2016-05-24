package eu.transkribus.swt_canvas.portal;

import java.util.HashMap;

import org.apache.batik.dom.GenericEntityReference;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt_canvas.util.databinding.DataBinder;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidget;
import eu.transkribus.swt_gui.mainwidget.TrpSettings;
import junit.framework.Assert;

public class PortalWidget extends Composite {
	public static enum Position {		
		TOP(true), BOTTOM(false), LEFT(true), RIGHT(false), CENTER(false);
		
		public final boolean isLeftSideOfSashForm;
		private Position(boolean isLeftSideOfSashForm) {
			this.isLeftSideOfSashForm = isLeftSideOfSashForm;
		}
	};
	
	public static enum Docking { 
		DOCKED(0), UNDOCKED(1), INVISIBLE(2);
		
		int value=0;
		private Docking(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return this.value;
		}
		
		public static Docking fromValue(int value) {
			switch (value) {
			case 0:
				return DOCKED;
			case 1:
				return UNDOCKED;
			case 2:
				return INVISIBLE;
			default:
				return DOCKED;
			}
		}
	};
	
	private final static Logger logger = LoggerFactory.getLogger(PortalWidget.class);
	
	public static final int DEFAULT_SASH_WIDTH = 5;
	
	private static int[] DEFAULT_WEIGHTS_HORIZONTAL_TOP_LEVEL = new int[] { 800, 220};
	private static int[] DEFAULT_WEIGHTS_HORIZONTAL = new int[] { 350, 1000};
	private static int[] DEFAULT_WEIGHTS_VERTICAL_TOP_LEVEL = new int[] {100, 30};
	private static int[] DEFAULT_WEIGHTS_VERTICAL = new int[] {10, 70};
	
	private SashForm sashFormHorizontalTopLevel;
	private SashForm sashFormHorizontal;
	private SashForm sashFormVerticalTopLevel;
	private SashForm sashFormVertical;
	
	private int sashWidth = DEFAULT_SASH_WIDTH;
	
	HashMap<SashForm, int []> weightsBackup=new HashMap<>();
	
	Composite parent;
	
//	ScrolledComposite topContainerWidget;
//	ScrolledComposite leftContainerWidget;
//	ScrolledComposite bottomContainerWidget;
//	ScrolledComposite rightContainerWidget;
//	ScrolledComposite centerContainerWidget;
	
	Shell topShell;
	Shell centerShell;
	Shell leftShell;
	Shell bottomShell;
	Shell rightShell;
	
//	HashMap<Position, Shell> shells = new HashMap<>();
	HashMap<Position, ScrolledComposite> widgets = new HashMap<>();
//	HashMap<Position, Docking> dockingMap = new HashMap<>();
	HashMap<Position, SashForm> positionSashFormMap = new HashMap<>();
	HashMap<Position, Shell> shells = new HashMap<>();
	HashMap<Position, Point> sizes = new HashMap<>();
	
	IObservableMap dockingMap = new WritableMap();
	
	/**
	 * Simple portal widget with a top, left, right, center and bottom widget and sashes to resize the areas.
	 * @param style For SWT.VERTICAL the top, bottom and center widgets fill the full horizontal span otherwise the left and right widgets fill the full vertical span 
	 */
	public PortalWidget(Composite parent, int style, 
			Composite topWidget, Composite centerWidget, 
			Composite leftWidget, Composite bottomWidget, Composite rightWidget) {
		super(parent, style);
		this.parent = parent;
		
		if ((style & SWT.VERTICAL) == SWT.VERTICAL)
			initVerticalWidgetsGrabExcess();
		else
			initHorizontalWidgetsGrabExcess();
						
		if (centerWidget != null) {
			fillScrolledComposite(widgets.get(Position.CENTER), centerWidget);
			setWidgetDockingType(Position.CENTER, Docking.DOCKED);
		}
		
		if (leftWidget != null) {
			fillScrolledComposite(widgets.get(Position.LEFT), leftWidget);
			setWidgetDockingType(Position.LEFT, Docking.DOCKED);
		}
		else
			setWidgetDockingType(Position.LEFT, Docking.INVISIBLE);
		
		if (bottomWidget != null) {
			fillScrolledComposite(widgets.get(Position.BOTTOM), bottomWidget);
			setWidgetDockingType(Position.BOTTOM, Docking.DOCKED);
		}
		else 
			setWidgetDockingType(Position.BOTTOM, Docking.INVISIBLE);
		
		if (topWidget != null) {
			fillScrolledComposite(widgets.get(Position.TOP), topWidget);
			setWidgetDockingType(Position.TOP, Docking.DOCKED);
		}
		else
			setWidgetDockingType(Position.TOP, Docking.INVISIBLE);
		
		if (rightWidget != null) {
			fillScrolledComposite(widgets.get(Position.RIGHT), rightWidget);
			setWidgetDockingType(Position.RIGHT, Docking.DOCKED);
		}
		else
			setWidgetDockingType(Position.RIGHT, Docking.INVISIBLE);
		
	}
	
	private void fillScrolledComposite(ScrolledComposite sc, Composite child) {
		child.setParent(sc);
//		child.pack();
		sc.setContent(child);
//		logger.debug("min size: "+child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		// FIXME: results in over-use of scrollbars...
//		sc.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		sc.pack();
//		sc.setAlwaysShowScrollBars(true);
	}
	
	private ScrolledComposite createScrolledComposite(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, /*SWT.FILL |*/ SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		return sc;
	}
	
//	private void createRightWidget(Composite parent) {
//		rightContainerWidget = new ScrolledComposite(parent, /*SWT.FILL |*/ SWT.V_SCROLL | SWT.H_SCROLL);
//		rightContainerWidget.setExpandHorizontal(true);
//		rightContainerWidget.setExpandVertical(true);
//	}
//	
//	private void createLeftWidget(Composite parent) {
//		leftContainerWidget = new ScrolledComposite(parent, SWT.FILL | SWT.V_SCROLL | SWT.H_SCROLL);
//		leftContainerWidget.setExpandHorizontal(true);
//		leftContainerWidget.setExpandVertical(true);
//	}
////	
//	private void createTopWidget(Composite parent) {
//		topContainerWidget = new ScrolledComposite(parent, SWT.FILL | SWT.V_SCROLL | SWT.H_SCROLL);
//		topContainerWidget.setExpandHorizontal(true);
//		topContainerWidget.setExpandVertical(true);
//	}
////	
//	private void createBottomWidget(Composite parent) {
//		bottomContainerWidget = new ScrolledComposite(parent, SWT.FILL | SWT.V_SCROLL | SWT.H_SCROLL);
//		bottomContainerWidget.setExpandHorizontal(true);
//		bottomContainerWidget.setExpandVertical(true);
//	}
//	
//	private void createCenterWidget(Composite parent) {
//		centerContainerWidget = new ScrolledComposite(parent, SWT.FILL | SWT.V_SCROLL | SWT.H_SCROLL);
//		centerContainerWidget.setExpandHorizontal(true);
//		centerContainerWidget.setExpandVertical(true);
//	}
	
	private void initHorizontalWidgetsGrabExcess() {
//		setLayout(new GridLayout(2, true));
		setLayout(new GridLayout());
		
		sashFormHorizontalTopLevel = new SashForm(parent, SWT.HORIZONTAL);
		sashFormHorizontalTopLevel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormHorizontalTopLevel.setSashWidth(5);	
		
		sashFormHorizontal = new SashForm(sashFormHorizontalTopLevel, SWT.HORIZONTAL);
		sashFormHorizontal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormHorizontal.setSashWidth(5);
				
		ScrolledComposite leftContainerWidget = createScrolledComposite(sashFormHorizontal);
		widgets.put(Position.LEFT, leftContainerWidget);
		positionSashFormMap.put(Position.LEFT, sashFormHorizontal);		
		
//		leftContainerWidget = new Composite(sashFormHorizontal, SWT.FILL);
//		leftContainerWidget.setLayout(new FillLayout());
		
		sashFormVerticalTopLevel = new SashForm(sashFormHorizontal, SWT.VERTICAL);
//		sashFormVerticalTopLevel = new SashForm(sashFormHorizontal, SWT.HORIZONTAL); // TEST
		
		sashFormVerticalTopLevel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormVerticalTopLevel.setSashWidth(5);
		
		sashFormVertical = new SashForm(sashFormVerticalTopLevel, SWT.VERTICAL);
		sashFormVerticalTopLevel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sashFormVertical.setSashWidth(5);

		ScrolledComposite topContainerWidget = createScrolledComposite(sashFormVertical);
		widgets.put(Position.TOP, topContainerWidget);
		positionSashFormMap.put(Position.TOP, sashFormVertical);
		
//		topContainerWidget = new Composite(sashFormVertical, SWT.FILL);
//		topContainerWidget.setLayout(new FillLayout());
		
		ScrolledComposite centerContainerWidget = createScrolledComposite(sashFormVertical);
		widgets.put(Position.CENTER, centerContainerWidget);
		positionSashFormMap.put(Position.CENTER, sashFormVertical);
				
//		centerContainerWidget = new Composite(sashFormVertical, SWT.FILL);
//		centerContainerWidget.setLayout(new FillLayout());
		
		ScrolledComposite bottomContainerWidget = createScrolledComposite(sashFormVerticalTopLevel);
		widgets.put(Position.BOTTOM, bottomContainerWidget);
		positionSashFormMap.put(Position.BOTTOM, sashFormVerticalTopLevel);
		
//		bottomContainerWidget = new Composite(sashFormVerticalTopLevel, SWT.FILL);
//		bottomContainerWidget.setLayout(new FillLayout());
		
		ScrolledComposite rightContainerWidget = createScrolledComposite(sashFormHorizontalTopLevel);
		widgets.put(Position.RIGHT, rightContainerWidget);
		positionSashFormMap.put(Position.RIGHT, sashFormHorizontalTopLevel);
//		rightWidget = new ScrolledComposite(sashFormHorizontalTopLevel, SWT.FILL | SWT.V_SCROLL | SWT.H_SCROLL);
//		rightWidget.setExpandHorizontal(true);
//		rightWidget.setExpandVertical(true);
//		rightWidget.setLayout(new FillLayout());

		setDefaultWeights();
	}
	
	private void initVerticalWidgetsGrabExcess() {
		setLayout(new GridLayout(2, false));
		
		sashFormVerticalTopLevel = new SashForm(parent, SWT.VERTICAL);
		sashFormVerticalTopLevel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormVerticalTopLevel.setSashWidth(5);
		
		sashFormVertical = new SashForm(sashFormVerticalTopLevel, SWT.VERTICAL);
		sashFormVertical.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormVertical.setSashWidth(5);
		
		ScrolledComposite topContainerWidget = createScrolledComposite(sashFormVertical);
		widgets.put(Position.TOP, topContainerWidget);
		positionSashFormMap.put(Position.TOP, sashFormVertical);
		
//		topContainerWidget = new Composite(sashFormVertical, SWT.FILL);
//		topContainerWidget.setLayout(new FillLayout());		
				
		sashFormHorizontalTopLevel = new SashForm(sashFormVertical, SWT.HORIZONTAL);
		sashFormHorizontalTopLevel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		sashFormHorizontalTopLevel.setSashWidth(5);
		
		sashFormHorizontal = new SashForm(sashFormHorizontalTopLevel, SWT.HORIZONTAL);
		sashFormHorizontal.setSashWidth(5);	
		
		ScrolledComposite leftContainerWidget = createScrolledComposite(sashFormHorizontal);
		widgets.put(Position.LEFT, leftContainerWidget);
		positionSashFormMap.put(Position.LEFT, sashFormHorizontal);
				
//		leftContainerWidget = new Composite(sashFormHorizontal, SWT.FILL);
//		leftContainerWidget.setLayout(new FillLayout());	
		
		ScrolledComposite centerContainerWidget = createScrolledComposite(sashFormHorizontal);
		widgets.put(Position.CENTER, centerContainerWidget);
		positionSashFormMap.put(Position.CENTER, sashFormHorizontal);
		
//		centerContainerWidget = new Composite(sashFormHorizontal, SWT.FILL);
//		centerContainerWidget.setLayout(new FillLayout());
		
		ScrolledComposite rightContainerWidget = createScrolledComposite(sashFormHorizontalTopLevel);
		widgets.put(Position.RIGHT, rightContainerWidget);
		positionSashFormMap.put(Position.RIGHT, sashFormHorizontalTopLevel);
//		rightContainerWidget = new Composite(sashFormHorizontalTopLevel, SWT.FILL);
//		righContainertWidget.setLayout(new FillLayout());
		
		ScrolledComposite bottomContainerWidget = createScrolledComposite(sashFormVerticalTopLevel);
		widgets.put(Position.BOTTOM, bottomContainerWidget);
		positionSashFormMap.put(Position.BOTTOM, sashFormVerticalTopLevel);
		
//		bottomContainerWidget = new Composite(sashFormVerticalTopLevel, SWT.FILL);
//		bottomContainerWidget.setLayout(new FillLayout());
				
		setDefaultWeights();
	}
	
	private void setDefaultWeights() {
		
		sizes.put(Position.LEFT, widgets.get(Position.LEFT).getSize());
		sizes.put(Position.CENTER, widgets.get(Position.CENTER).getSize());
		sizes.put(Position.RIGHT, widgets.get(Position.RIGHT).getSize());
		sizes.put(Position.BOTTOM, widgets.get(Position.BOTTOM).getSize());
		sizes.put(Position.TOP, widgets.get(Position.TOP).getSize());
		
		sashFormHorizontalTopLevel.setWeights(DEFAULT_WEIGHTS_HORIZONTAL_TOP_LEVEL);
		sashFormHorizontal.setWeights(DEFAULT_WEIGHTS_HORIZONTAL);
		
		sashFormVerticalTopLevel.setWeights(DEFAULT_WEIGHTS_VERTICAL_TOP_LEVEL);
		sashFormVertical.setWeights(DEFAULT_WEIGHTS_VERTICAL);
		
		weightsBackup.put(sashFormHorizontalTopLevel, 
				new int[] {DEFAULT_WEIGHTS_HORIZONTAL_TOP_LEVEL[0], DEFAULT_WEIGHTS_HORIZONTAL_TOP_LEVEL[1]});
		weightsBackup.put(sashFormHorizontal, 
				new int[] {DEFAULT_WEIGHTS_HORIZONTAL[0], DEFAULT_WEIGHTS_HORIZONTAL[1]});
		weightsBackup.put(sashFormVerticalTopLevel, 
				new int[] {DEFAULT_WEIGHTS_VERTICAL_TOP_LEVEL[0], DEFAULT_WEIGHTS_VERTICAL_TOP_LEVEL[1]});
		weightsBackup.put(sashFormVertical, 
				new int[] {DEFAULT_WEIGHTS_VERTICAL[0], DEFAULT_WEIGHTS_VERTICAL[1]});		
		
			
//		backupWeights();
	}
	
	private void backupWeightsAndSize(Position pos) {
		SashForm sf = positionSashFormMap.get(pos);
		if (sf != null) {
			weightsBackup.get(sf)[0] = sf.getWeights()[0];
			weightsBackup.get(sf)[1] = sf.getWeights()[1];
		}
		
	}
	
	private void closeShell(final Position pos) {
		Shell s = shells.get(pos);
		if (s != null) {
			for (Listener l : s.getListeners(SWT.Close)) 
				s.removeListener(SWT.Close, l); // remove close event listener to prevent inifinte loop			
			s.close();
			shells.remove(pos);
		}
	}
	
	// TEST
	private Shell createUndockedShell(final Position pos) {
		Shell existingShell = shells.get(pos);
		if (existingShell!=null)
			return existingShell;
		
		Shell shell = new Shell(parent.getDisplay(), SWT.SHELL_TRIM);
		shell.setText("");
		shell.setLayout(new FillLayout());
		
		SashForm sf = positionSashFormMap.get(pos);
		ScrolledComposite containerWidget = widgets.get(pos);
//		Point s = sizes.get(pos) == null ? containerWidget.getSize() : sizes.get(pos);
		if (containerWidget.getChildren().length>0) {
//			Point s = containerWidget.getChildren()[0].computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point s = containerWidget.getChildren()[0].getSize();
			shell.setSize(s.x, s.y);
			// FIXME:
			Point l = containerWidget.toDisplay(containerWidget.getChildren()[0].getLocation());
			logger.debug("shell location: "+l);
			shell.setLocation(l.x, l.y);
		}
	
		containerWidget.setParent(shell);
		sf.setWeights(new int[]{100});
		shell.layout(true);
		
		// react on closing this shell -> dock this widget again!
		shell.addListener(SWT.Close, new Listener() {
			Position position = pos;	
			@Override public void handleEvent(Event event) {
				// set dock type to DOCKED:
				setWidgetDockingType(position, Docking.DOCKED);
			}
		});
		shells.put(pos, shell);
		
		return shell;
	}
	// TEST
	public void setWidgetDockingType(Position pos, Docking docking) {
		Assert.assertNotNull(pos);
		Assert.assertNotNull(docking);

		logger.debug("setting "+ pos + " view docking type: "+docking);

//		if (getDocking(pos) == docking) {
//			logger.debug("already in this docking state");
//			return;
//		}
		
		SashForm sf = positionSashFormMap.get(pos);
		
//		int nChildren = sf.getChildren().length;
//		logger.debug("nChildren = "+nChildren);

		switch (docking) {
		case DOCKED:
			reattachWidget(pos);

//			if (nChildren > 2) {
				sf.setWeights(weightsBackup.get(sf));
				sf.setSashWidth(sashWidth);
//			}
			closeShell(pos);
			break;
		case UNDOCKED:
			Shell shell = createUndockedShell(pos);
			shell.open();
			break;
		case INVISIBLE:
			reattachWidget(pos);
//			backupWeightsAndSize(pos);
			int[] invisibileWeights = pos.isLeftSideOfSashForm ? new int[] {0, 100} : new int[]{100, 0}; 
			sf.setWeights(invisibileWeights);
			sf.setSashWidth(0);
			closeShell(pos);
			break;
		}
		
		dockingMap.put(pos, docking);
		
		Event e = new Event();
		e.data = pos;
		this.notifyListeners(SWT.Selection, e);
	}
	
	public Docking getDocking(Position pos) {
		return (Docking) dockingMap.get(pos);
	}
	
	private void reattachWidget(Position pos) {
		SashForm sf = positionSashFormMap.get(pos);
		ScrolledComposite w = widgets.get(pos);
		if (w.getParent() == sf) // already attached to SashForm -> do nothing!
			return;
		
		w.setParent(sf); // set parent to SashForm again
		
		// move to left or right side of SashForm depending on flag in position:
		if (pos.isLeftSideOfSashForm)
			w.moveAbove(null);
		else
			w.moveBelow(null);
		
		sf.layout(true);
	}
	
//	public void resizeWidget(Position pos) {
//		SashForm sf = positionSashFormMap.get(pos);
//		
//		Rectangle previous = sf.getBounds();
//		
//		if (pos.equals(Position.LEFT)){
//			
//			sf.setBounds(previous.x, previous.y, previous.width/2, previous.height);
//		}
//		
//		sf.layout(true);
//	}
	
//	private void makeWidgetInvisibleInSashForm(Position pos) {
//		SashForm sf = positionSashFormMap.get(pos);
//		int[] invisibileWeights = pos.isLeftSideOfSashForm ? new int[] {0, 100} : new int[]{100, 0}; 
//		sf.setWeights(invisibileWeights);
//		sf.setSashWidth(0);
//	}
	
//	private void setLeftViewVisible(boolean val) {
//		// TEST:
//		if (true) {
//			if (!val) {
//				Shell shell = createUndockedShell(Position.LEFT);
//				shell.open();
//			} else {
//				int nChildren = sashFormHorizontal.getChildren().length;
//				if (nChildren == 2) {
//					sashFormHorizontal.setWeights(weightsBackup.get(sashFormHorizontal));
//					sashFormHorizontal.setSashWidth(5);
//				}
//			}
//		} else {
//		// DEFAULT:
//		
//		int nChildren = sashFormHorizontal.getChildren().length;
//		if (nChildren == 2) {
//		
//		if (!val) {
//			backupWeights(sashFormHorizontal);
//			sashFormHorizontal.setWeights(new int[] {0, 100});
//			sashFormHorizontal.setSashWidth(0);
//		}
//		else {
//			sashFormHorizontal.setWeights(weightsBackup.get(sashFormHorizontal));
//			sashFormHorizontal.setSashWidth(5);
//		}
//		}
//		}
//	}
//	
//	private void setBottomViewVisible(boolean val) {
//		if (!val) {
//			backupWeights(sashFormVerticalTopLevel);			
//			sashFormVerticalTopLevel.setWeights(new int[] {100, 0});
//			sashFormVerticalTopLevel.setSashWidth(0);
//		}
//		else {
//			sashFormVerticalTopLevel.setWeights(weightsBackup.get(sashFormVerticalTopLevel));
//			sashFormVerticalTopLevel.setSashWidth(5);
//		}
//	}
//	
//	private void setRightViewVisible(boolean val) {
//		if (!val) {
//			backupWeights(sashFormHorizontalTopLevel);
//			sashFormHorizontalTopLevel.setWeights(new int[] {100, 0});
//			sashFormHorizontalTopLevel.setSashWidth(0);
//		}
//		else {
//			sashFormHorizontalTopLevel.setWeights(weightsBackup.get(sashFormHorizontalTopLevel));
//			sashFormHorizontalTopLevel.setSashWidth(5);
//		}
//	}
//	
//	private void setTopViewVisible(boolean val) {
//		if (!val) {
//			backupWeights(sashFormVertical);
//			sashFormVertical.setWeights(new int[] {0, 100});
//			sashFormVertical.setSashWidth(0);
//		}
//		else {
//			sashFormVertical.setWeights(weightsBackup.get(sashFormVertical));
//			sashFormVertical.setSashWidth(5);
//		}
//	}
	
	public void setMinHeight(Position pos, int height) {
		ScrolledComposite sc = widgets.get(pos);
		if (sc!=null) {
			sc.setMinHeight(height);
		}
	}
	
	public void setMinWidth(Position pos, int width) {
		ScrolledComposite sc = widgets.get(pos);
		
		if (sc!=null) {
			sc.setMinWidth(width);
		}
	}
	
	public ScrolledComposite getContainerWidget(Position pos) {
		return widgets.get(pos);
	}
	
	public void setSashWidth(int sashWidth) { this.sashWidth = sashWidth; }
	
	public IObservableMap getDockingMap() {
		return dockingMap;
	}

	public void setNewSashFormVerticalTopLevelWeights(int [] weights) {
		if (weights != null){
			sashFormVerticalTopLevel.setWeights(weights);
		}
		else{
			sashFormHorizontalTopLevel.setWeights(DEFAULT_WEIGHTS_VERTICAL_TOP_LEVEL);
		}
		
	}
	
//	public Shell getShell(Position pos) {
//		return shells.get(pos);
//	}

//	public ScrolledComposite getTopContainerWidget() {
//		return topContainerWidget;
//	}
//
//	public ScrolledComposite getCenterContainerWidget() {
//		return centerContainerWidget;
//	}
//
//	public ScrolledComposite getLeftContainerWidget() {
//		return leftContainerWidget;
//	}
//
//	public ScrolledComposite getBottomContainerWidget() {
//		return bottomContainerWidget;
//	}
//
//	public ScrolledComposite getRightContainerWidget() {
//		return rightContainerWidget;
//	}	
	

}
