package eu.transkribus.swt_gui.outdated;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent.RegionType;
import eu.transkribus.core.model.beans.pagecontent.TextLineType;
import eu.transkribus.core.model.beans.pagecontent.WordType;

import org.dea.swt.canvas.shapes.CanvasPolygon;


public class RegionTypeComparator implements Comparator<Object> {
	private final static Logger logger = LoggerFactory.getLogger(RegionTypeComparator.class);
	
	@Override
	public int compare(Object o1, Object o2) {
		try {
			CanvasPolygon p1 = null;
			CanvasPolygon p2 = null;			
			if (o1 instanceof RegionType && o2 instanceof RegionType) {
				p1 = new CanvasPolygon(((RegionType)o1).getCoords().getPoints());
				p2 = new CanvasPolygon(((RegionType)o2).getCoords().getPoints());
			}
			else if (o1 instanceof TextLineType && o2 instanceof TextLineType) {
				p1 = new CanvasPolygon(((TextLineType)o1).getCoords().getPoints());
				p2 = new CanvasPolygon(((TextLineType)o2).getCoords().getPoints());
			}
			else if (o1 instanceof WordType && o2 instanceof WordType) {
				p1 = new CanvasPolygon(((WordType)o1).getCoords().getPoints());
				p2 = new CanvasPolygon(((WordType)o2).getCoords().getPoints());
			}
			else
				throw new AssertionError("RegionTypeComparator compare elements of class "+o1.getClass());
			
			return p1.compareTo(p2);
		} catch (Exception e) {
			logger.error("Error comparing region "+o1+" with "+o2+", message: "+e.getMessage(), e);
			throw new AssertionError(e);
		}
	}
}
