package eu.transkribus.swt.util;
import java.util.Stack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the Undo-Redo functionality (working Ctrl+Z and Ctrl+Y) to an instance
 * of {@link StyledText}.
 * 
 * @author Petr Bodnar
 * @see {@linkplain http
 *      ://www.java2s.com/Code/Java/SWT-JFace-Eclipse/SWTUndoRedo.htm} -
 *      inspiration for this code, though not really functioning - it mainly
 *      shows which listeners to use...
 * @see {@linkplain http
 *      ://stackoverflow.com/questions/7179464/swt-how-to-recreate
 *      -a-default-context-menu-for-text-fields} -
 *      "SWT's StyledText doesn't support Undo-Redo out-of-the-box"
 */
public class UndoRedoImpl implements KeyListener, ExtendedModifyListener {
	private static final Logger logger = LoggerFactory.getLogger(UndoRedoImpl.class);

	public static final boolean USE_TIME_DELTA = true;
	public static int DELTA = 500;
	boolean enabled=true;

    /**
     * Encapsulation of the Undo and Redo stack(s).
     */
    private static class UndoRedoStack<T> {

        private Stack<T> undo;
        private Stack<T> redo;

        public UndoRedoStack() {
            undo = new Stack<T>();
            redo = new Stack<T>();
        }

        public void pushUndo(T delta) {
            undo.add(delta);
        }

        public void pushRedo(T delta) {
            redo.add(delta);
        }

        public T popUndo() {
            T res = undo.pop();
            return res;
        }

        public T popRedo() {
            T res = redo.pop();
            return res;
        }

        public void clearRedo() {
            redo.clear();
        }
        
        public void clearUndo() {
            undo.clear();
        }
        
        public void clear() {
        	clearUndo();
        	clearRedo();
        }

        public boolean hasUndo() {
            return !undo.isEmpty();
        }

        public boolean hasRedo() {
            return !redo.isEmpty();
        }

    }

    private StyledText editor;

    private UndoRedoStack<ExtendedModifyEvent> stack;

    private boolean isUndo;

    private boolean isRedo;

    /**
     * Creates a new instance of this class. Automatically starts listening to
     * corresponding key and modify events coming from the given
     * <var>editor</var>.
     * 
     * @param editor
     *            the text field to which the Undo-Redo functionality should be
     *            added
     */
    public UndoRedoImpl(StyledText editor) {
        editor.addExtendedModifyListener(this);
        editor.addKeyListener(this);

        this.editor = editor;
        stack = new UndoRedoStack<ExtendedModifyEvent>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
     * KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
    	if (!enabled) return;
    	
        // Listen to CTRL+Z for Undo, to CTRL+Y or CTRL+SHIFT+Z for Redo
        boolean isCtrl = (e.stateMask & SWT.CTRL) > 0;
        boolean isAlt = (e.stateMask & SWT.ALT) > 0;
        if (isCtrl && !isAlt) {
            boolean isShift = (e.stateMask & SWT.SHIFT) > 0;
            if (!isShift && e.keyCode == 'z') {
                undo();
            } else if (!isShift && e.keyCode == 'y' || isShift
                    && e.keyCode == 'z') {
                redo();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
     * .KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
    	if (!enabled) return;
    	
        // ignore
    }

    /**
     * Creates a corresponding Undo or Redo step from the given event and pushes
     * it to the stack. The Redo stack is, logically, emptied if the event comes
     * from a normal user action.
     * 
     * @param event
     * @see org.eclipse.swt.custom.ExtendedModifyListener#modifyText(org.eclipse.
     *      swt.custom.ExtendedModifyEvent)
     */
    public void modifyText(ExtendedModifyEvent event) {
    	if (!enabled) return;
    	
    	long time = 0xFFFFFFFFL & event.time;
    	
    	logger.debug("modified: "+event+", l="+event.length+" rt="+event.replacedText+" time= "+time);
        if (isUndo) {
            stack.pushRedo(event);
        } else { // is Redo or a normal user action
            stack.pushUndo(event);
            if (!isRedo) {
                stack.clearRedo();
                // TODO Switch to treat consecutive characters as one event?
            }
        }
    }

    /**
     * Performs the Undo action. A new corresponding Redo step is automatically
     * pushed to the stack.
     */
    public void undo() {
    	if (USE_TIME_DELTA) {
    	long timeBefore = -1;
    	while (stack.hasUndo()) {
    		ExtendedModifyEvent e = stack.popUndo();
            if (timeBefore >= 0) {
            	long diff = timeBefore - (0xFFFFFFFFL & e.time);
            	if (diff > DELTA) {
            		stack.pushUndo(e);
            		break;
            	}
            }
    		
            isUndo = true;
            revertEvent(e);
            isUndo = false;
            timeBefore = 0xFFFFFFFFL & e.time;
    	}
    	} else {
        if (stack.hasUndo()) {
            isUndo = true;
            revertEvent(stack.popUndo());
            isUndo = false;
        }
    	}
    }

    /**
     * Performs the Redo action. A new corresponding Undo step is automatically
     * pushed to the stack.
     */
    public void redo() {
    	if (USE_TIME_DELTA) {
        	long timeBefore = -1;
        	while (stack.hasRedo()) {
        		ExtendedModifyEvent e = stack.popRedo();
                if (timeBefore >= 0) {
                	long diff = timeBefore - (0xFFFFFFFFL & e.time);
                	if (diff > DELTA) {
                		stack.pushRedo(e);
                		break;
                	}
                }
        		
                isRedo = true;
                revertEvent(e);
                isRedo = false;
                timeBefore = 0xFFFFFFFFL & e.time;
        	}
    	} else {
            if (stack.hasRedo()) {
                isRedo = true;
                revertEvent(stack.popRedo());
                isRedo = false;
            }	
    	}
    	

    }
    
    public void clear() {
    	stack.clear();
    }

    /**
     * Reverts the given modify event, in the way as the Eclipse text editor
     * does it.
     * 
     * @param event
     */
    private void revertEvent(ExtendedModifyEvent event) {
        editor.replaceTextRange(event.start, event.length, event.replacedText);
        // (causes the modifyText() listener method to be called)

        editor.setSelectionRange(event.start, event.replacedText.length());
    }

	public void setEnabled(boolean value) {
		this.enabled = value;		
	}
	
	

}
