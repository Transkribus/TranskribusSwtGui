package eu.transkribus.swt_canvas.canvas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.exec.OS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public final class CanvasKeys {
	private final static Logger logger = LoggerFactory.getLogger(CanvasKeys.class);
	
	private final static List<KeyAction> keyActions = new ArrayList<>();
	
	public static class KeyAction {
		int[] keys;
		boolean isEditOperation;
		String description;
		
		public KeyAction(String desc, int[] keys, boolean isEditOperation) {
			this.description = desc;
			this.keys = keys;
			this.isEditOperation = isEditOperation;
			
			keyActions.add(this);
		}
	}
	
	public final static KeyAction FIT_TO_PAGE = new KeyAction("Fit to page", new int[]{'f'}, false);
	public final static KeyAction FIT_TO_WIDTH = new KeyAction("Fit to width", new int[]{'w'}, false);
	public final static KeyAction FIT_TO_HEIGHT = new KeyAction("Fit to height", new int[]{'h'}, false);
//	public final static KeyAction ROTATE_RIGHT = new KeyAction("Rotate right", new int[]{'r'}, false);
//	public final static KeyAction ROTATE_LEFT = new KeyAction("Rotate left", new int[]{'l'}, false);
	public final static KeyAction ZOOM_IN = new KeyAction("Zoom in", new int[]{'i', '+'}, false);
	public final static KeyAction ZOOM_OUT = new KeyAction("Zoom out", new int[]{'o', '-'}, false);
	public final static KeyAction TRANSLATE_RIGHT = new KeyAction("Translate right", new int[]{SWT.ARROW_RIGHT}, false);
	public final static KeyAction TRANSLATE_LEFT = new KeyAction("Translate left", new int[]{SWT.ARROW_LEFT}, false);
	public final static KeyAction TRANSLATE_UP = new KeyAction("Translate up", new int[]{SWT.ARROW_UP}, false);
	public final static KeyAction TRANSLATE_DOWN = new KeyAction("Translate down", new int[]{SWT.ARROW_DOWN}, false);
	public final static KeyAction SET_SELECTION_MODE = new KeyAction("Set selection mode", new int[]{SWT.ESC}, false);
	
	// EDIT OPERATIONS
	public final static KeyAction FINISH_SHAPE = new KeyAction("Finish shape", new int[]{SWT.CR , SWT.KEYPAD_CR }, true);
	public final static KeyAction DELETE_SHAPE = new KeyAction("Delete shape", new int[]{SWT.DEL }, true);
	public final static KeyAction ADD_SHAPE = new KeyAction("Add shape", new int[]{ 'a' }, true);
	public final static KeyAction SPLIT_SHAPE = new KeyAction("Split shape", new int[]{ 's' }, true);
	public final static KeyAction UNDO = new KeyAction("Undo", new int[]{ 'z' }, true);
	
	public final static KeyAction MOVE_SCENE = new KeyAction("Move scene", new int[] { SWT.ALT }, true);
		
	public final static int RESIZE_BOUNDING_BOX_REQUIRED_KEY = SWT.SHIFT;
	public final static int MOVE_SUBSHAPES_REQUIRED_KEY = SWT.SHIFT;
	public final static int MULTISELECTION_REQUIRED_KEY = SWT.CTRL;
	public final static int MOVE_SCENE_REQUIRED_KEYS = SWT.ALT | SWT.CTRL;
	public final static int SELECTION_RECTANGLE_REQUIRED_KEYS = SWT.CTRL;
	
	/** Maps keybindings to a list of additional keycodes that are required to be pressed */
	static HashMap<int[], int[]> requiredButtons = new HashMap<int[], int[]>();
	
	static {
		requiredButtons.put(FIT_TO_PAGE.keys, new int[]{SWT.CTRL});
		requiredButtons.put(FIT_TO_WIDTH.keys, new int[]{SWT.CTRL});
		requiredButtons.put(FIT_TO_HEIGHT.keys, new int[]{SWT.CTRL});
//		requiredButtons.put(ROTATE_RIGHT, new int[]{SWT.CTRL});
//		requiredButtons.put(ROTATE_LEFT, new int[]{SWT.CTRL});
//		requiredButtons.put(ZOOM_IN.keys, new int[]{SWT.CTRL});
//		requiredButtons.put(ZOOM_OUT.keys, new int[]{SWT.CTRL});
		requiredButtons.put(TRANSLATE_RIGHT.keys, new int[]{SWT.CTRL});
		requiredButtons.put(TRANSLATE_LEFT.keys, new int[]{SWT.CTRL});
		requiredButtons.put(TRANSLATE_UP.keys, new int[]{SWT.CTRL});
		requiredButtons.put(TRANSLATE_DOWN.keys, new int[]{SWT.CTRL});
		requiredButtons.put(ADD_SHAPE.keys, new int[]{SWT.CTRL});
		requiredButtons.put(SPLIT_SHAPE.keys, new int[]{SWT.CTRL});
		requiredButtons.put(UNDO.keys, new int[]{SWT.CTRL});		
	}
	
	private static int[] findRequiredKeys(int keycode) {
		for (int[] keys : requiredButtons.keySet()) {
			for (int k : keys) {
				if (k==keycode)
					return requiredButtons.get(keys);
			}
		}
		return null;
	}
	
	public static boolean isEditOperation(KeyEvent e) {
		for (KeyAction ka : keyActions) {
			if (containsKey(ka.keys, e.keyCode)) {
				return ka.isEditOperation;
			}
		}
		return false;
	}
	
	public static boolean hasRequiredKeysDown(KeyEvent e) {
		int[] requiredKeys = findRequiredKeys(e.keyCode);
		if (requiredKeys==null) // key not found in mapping so assume no keys are required!
			return true;
		
		// generate mask of required keys:
		int reqKeysMask = 0;
		logger.trace("nr. of required keys: "+requiredKeys.length);
		for (Integer key : requiredKeys) {
			logger.trace("required key: "+key);
			reqKeysMask |= key;
		}
		
		logger.debug("stateMask: "+e.stateMask+" reqKeysMask: "+reqKeysMask);
		
		return (e.stateMask == reqKeysMask);
		
//		for (Integer key : requiredKeys) {
//			if (!isKeyDown(e, key))
//				return false;
//		}
//		logger.debug("Required key "+requiredKeys+" are all down!");
//		return true;
	}
	
	public static boolean containsKey(KeyAction ka, int key) {
		return containsKey(ka.keys, key);
	}
	
	public static boolean containsKey(int[] keys, int key) {
		for (int k : keys)
			if (k == key)
				return true;
		
		return false;
	}
	
	public static boolean isKeyDown(KeyEvent e, int key) {
		return isKeyDown(e.stateMask, key);
	}
	
	public static boolean isKeyDown(int mask, int key) {
		return (mask & key) == key;
	}
	
	public static boolean isCtrlKeyDown(KeyEvent e) {
		return isCtrlKeyDown(e.stateMask);
	}
	
	public static boolean isCtrlKeyDown(int mask) {
		return isKeyDown(mask, SWT.CTRL);
	}
		
	public static boolean isCommandKeyDown(KeyEvent e) {
		return isCommandKeyDown(e.stateMask);
	}
	
	public static boolean isCommandKeyDown(int mask) {
		return isKeyDown(mask, SWT.COMMAND);
	}
	
	public static boolean isCtrlOrCommandKeyDown(KeyEvent e) {
		return isCtrlOrCommandKeyDown(e.stateMask);
	}
	
	/**
	 * On MAC, returns true if command key is down, elsewise if ctrl key is down
	 */
	public static boolean isCtrlOrCommandKeyDown(int mask) {
		return OS.isFamilyMac() ? isCommandKeyDown(mask) : CanvasKeys.isCtrlKeyDown(mask);	
	}
	
	public static boolean isAltKeyDown(int mask) {
		return isKeyDown(mask, SWT.ALT);
	}
	
	public static boolean isAltKeyDown(KeyEvent e) {
		return isKeyDown(e, SWT.ALT);
	}
	
	public static boolean isShiftKeyDown(int mask) {
		return isKeyDown(mask, SWT.SHIFT);
	}
	
	public static boolean isShiftKeyDown(KeyEvent e) {
		return isKeyDown(e, SWT.SHIFT);
	}	
	

}
