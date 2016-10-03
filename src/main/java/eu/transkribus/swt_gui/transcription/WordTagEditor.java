package eu.transkribus.swt_gui.transcription;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.core.model.beans.pagecontent_trp.TaggedWord;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpTextLineType;
import eu.transkribus.core.model.beans.pagecontent_trp.TrpWordType;
import eu.transkribus.swt_gui.canvas.SWTCanvas;

@Deprecated
public class WordTagEditor extends CanvasShapeAttachWidget<TrpWordType> {
	private final static Logger logger = LoggerFactory.getLogger(WordTagEditor.class);
	
	Combo tagCombo;
	ComboViewer comboViewer;	

	public WordTagEditor(final SWTCanvas canvas, int style) {
		super(canvas, style, TrpWordType.class, 300, 35);
		
		comboViewer = new ComboViewer(this, SWT.READ_ONLY | SWT.DROP_DOWN);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
	        @Override
	        public String getText(Object element) {
	            if (element instanceof TaggedWord) {
	            	TaggedWord tw = (TaggedWord) element;
	            	return tw.getWordItself();
	            }
	            return "ERROR";
	        }
	    });
				
		tagCombo = comboViewer.getCombo();
		tagCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tagCombo.moveBelow(label);
		
		label.setText("Tagged Word: ");
		
//		addWidget(tagCombo);
		
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
	        @Override
	        public void selectionChanged(SelectionChangedEvent event) {
	            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	            logger.debug("selection = "+selection);
	            if (shape == null || selection == null)
	            	return;
	            
	            TaggedWord tw = (TaggedWord) selection.getFirstElement();
	            tw.assignRegionToWord(shape);
	        }
	    });
	}
	
	@Override protected boolean isDisabled() { return true; }
	
	@Override
	public boolean showThisEditor() {
		return false;
	}
	
	public Combo getTagCombo() { return tagCombo; }
	
	@Override
	public void updateData() {
		if (isDisabled())
			return;		
		
//		Listener[] listeners = detachListener(SWT.Selection);
		
		if (shape==null)
			return;
		
		TrpTextLineType line = shape.getLine();
		comboViewer.setInput(line.getTaggedWords());
			
		int i = 0;
		for (TaggedWord w : line.getTaggedWords()) {			
			if (w.getWordRegion() == shape) { // the word region assigned to the TaggedWord corresponds with this word!
				tagCombo.select(i);
			}
			++i;
		}
				
		layout(true);
		
//		attachListener(listeners, SWT.Selection);
		
		tagCombo.redraw();
		logger.debug("end of update");
	}
		
//	private Listener[] detachListener(int eventType) {
//		Listener[] removing = tagCombo.getListeners(eventType);
//		for (Listener l : removing) {
//			tagCombo.removeListener(eventType, l);
//		}
//		return removing;
//	}
//	
//	private void attachListener(Listener[] listener, int eventType) {
//		for (Listener l : listener) {
//			tagCombo.addListener(eventType, l);
//		}
//	}
	
//	public StyledText getTextField() { return textField; }
		
	@Override
	protected void onUpdatePosition() {
		changeComboSize();
	}
	
	public void changeComboSize() {
//		GC gc = new GC(tagCombo);
//		int te = gc.textExtent(tagCombo.getText()).x + 20;
//		
//		gc.dispose();
//		if (textField.getSize().x < te) {
//			int diff = te - textField.getSize().x;
//			textField.setSize(te+10, textField.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//			Rectangle r = getBounds();
//			r.width += diff;
//			setBounds(r);
//		}
	}

}
