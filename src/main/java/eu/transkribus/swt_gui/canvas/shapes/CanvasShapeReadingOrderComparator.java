package eu.transkribus.swt_gui.canvas.shapes;

import java.util.Comparator;

import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpElementReadingOrderComparator;
import eu.transkribus.swt_gui.util.GuiUtil;

public class CanvasShapeReadingOrderComparator implements Comparator<ICanvasShape> {

	TrpElementReadingOrderComparator<ITrpShapeType> stComparator = new TrpElementReadingOrderComparator<>(true);
	
	public CanvasShapeReadingOrderComparator() {
	}

	@Override
	public int compare(ICanvasShape o1, ICanvasShape o2) {
		
		ITrpShapeType st1 = GuiUtil.getTrpShape(o1);
		ITrpShapeType st2 = GuiUtil.getTrpShape(o2);
		
		return stComparator.compare(st1, st2);
	}

}
