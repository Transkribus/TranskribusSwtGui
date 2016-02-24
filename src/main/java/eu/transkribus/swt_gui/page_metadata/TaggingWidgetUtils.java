package eu.transkribus.swt_gui.page_metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.dea.swt.util.Colors;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.customtags.CustomTag;
import eu.transkribus.core.model.beans.customtags.CustomTagFactory;
import eu.transkribus.core.model.beans.pagecontent_trp.ITrpShapeType;
import eu.transkribus.core.util.IntRange;
import eu.transkribus.swt_gui.mainwidget.TrpMainWidgetView;
import eu.transkribus.swt_gui.transcription.ATranscriptionWidget;

public class TaggingWidgetUtils {
	private final static Logger logger = LoggerFactory.getLogger(TaggingWidgetUtils.class);
	
	public static boolean APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION=false;
	
	/** an array of different colors for tags: */
	public final static String[] INDEX_COLORS = new String[] {
        "#000000", "#1CE6FF", "#FF34FF", "#008941", "#006FA6", "#A30059", "#FFFF00", "#00846F",
        "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF", "#997D87",
        "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
        "#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100",
        "#DDEFFF", "#000035", "#7B4F4B", "#A1C299", "#300018", "#0AA6D8", "#013349", "#FF4A46",
        "#372101", "#FFB500", "#C2FFED", "#A079BF", "#CC0744", "#C0B9B2", "#C2FF99", "#001E09",
        "#00489C", "#6F0062", "#0CBD66", "#EEC3FF", "#456D75", "#B77B68", "#7A87A1", "#788D66",
        "#885578", "#FAD09F", "#FF8A9A", "#D157A0", "#BEC459", "#456648", "#0086ED", "#886F4C",

        "#34362D", "#B4A8BD", "#00A6AA", "#452C2C", "#636375", "#A3C8C9", "#FF913F", "#938A81",
        "#575329", "#00FECF", "#B05B6F", "#8CD0FF", "#3B9700", "#04F757", "#C8A1A1", "#1E6E00",
        "#7900D7", "#A77500", "#6367A9", "#A05837", "#6B002C", "#772600", "#D790FF", "#9B9700",
        "#549E79", "#FFF69F", "#201625", "#72418F", "#BC23FF", "#99ADC0", "#3A2465", "#922329",
        "#5B4534", "#FDE8DC", "#404E55", "#0089A3", "#CB7E98", "#A4E804", "#324E72", "#6A3A4C",
        "#83AB58", "#001C1E", "#D1F7CE", "#004B28", "#C8D0F6", "#A3A489", "#806C66", "#222800",
        "#BF5650", "#E83000", "#66796D", "#DA007C", "#FF1A59", "#8ADBB4", "#1E0200", "#5B4E51",
        "#C895C5", "#320033", "#FF6832", "#66E1D3", "#CFCDAC", "#D0AC94", "#7ED379", "#012C58"
	};
		
	public static Color getColorForIndex(int ri) {
		String colorCode = TaggingWidgetUtils.INDEX_COLORS[ri%(TaggingWidgetUtils.INDEX_COLORS.length-1)+1];
		Color c = Colors.decode(colorCode);
		return c;
	}
	
	public static IntRange getTagRange(TrpMainWidgetView ui, int nRanges, Pair<ITrpShapeType, IntRange> r) {
		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
		if (aw==null) {
			logger.debug("no transcription widget selected - returning null range!");
			return null;
		}
		boolean isLineEditor = aw.getType() == ATranscriptionWidget.Type.LINE_BASED;
		boolean isSingleSelection = nRanges==1 && r.getRight().length==0;
			
		// create range:
		if ( (isSingleSelection && APPLY_TAG_TO_WHOLE_LINE_IF_SINGLE_SELECTION) || !isLineEditor) {
			return new IntRange(0, r.getLeft().getUnicodeText().length());
		} 
		else if (r.getRight().length>=0) {
			return r.getRight();
		}
		
		return null;
	}
	
	public static List<Pair<ITrpShapeType, CustomTag>> constructTagsFromSelectionInTranscriptionWidget(TrpMainWidgetView ui, String tagName, Map<String, Object> attributes) {
		List<Pair<ITrpShapeType, CustomTag>> tags4Shapes = new ArrayList<>();
		ATranscriptionWidget aw = ui.getSelectedTranscriptionWidget();
		if (aw==null) {
			logger.debug("no transcription widget selected - doin nothing!");
			return tags4Shapes;
		}
		List<Pair<ITrpShapeType, IntRange>> selRanges = aw.getSelectedShapesAndRanges();
		logger.debug("selRanges: "+selRanges.size());
		
		for (Pair<ITrpShapeType, IntRange> r : selRanges) {
//			if (settings.isEnableIndexedStyles() && isLineEditor && !recursive) {
				CustomTag t = null;	
				IntRange tagRange = getTagRange(ui, selRanges.size(), r);
				logger.debug("range is: "+tagRange);
				if (tagRange != null) {
					try {
						t = CustomTagFactory.create(tagName, tagRange.offset, tagRange.length, attributes);
						logger.debug("created tag: "+t);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
				tags4Shapes.add(Pair.of(r.getLeft(), t));
		}
		return tags4Shapes;
		
	}
	
	public static <T> void replaceEditor(Map<T, ControlEditor> editors, T key, ControlEditor editor) {
		if (editors.containsKey(key)) {
			ControlEditor t = editors.get(key);
			deleteEditor(t);
		}
		editors.put(key, editor);
	}
	
	public static void deleteEditor(ControlEditor editor) {
		if (editor.getEditor()!=null)
			editor.getEditor().dispose();
		editor.dispose();
	}
		
	public static <T> void updateEditors(Map<T, ControlEditor> editors, Collection<String> tagNames) {
		Set<T> keys = new HashSet<>(editors.keySet());
	
		for (T tn : keys) {
			if (!tagNames.contains(tn)) {
				deleteEditor(editors.get(tn));
				editors.remove(tn);
			}
		}
	}	

}
