package eu.transkribus.swt.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorChooseButton extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(ColorChooseButton.class);
	
	public static RGB DEFAULT_COLOR = new RGB(0, 0, 255);
	
	Button colorBtn;
	
	boolean editorEnabled=true;
	
	public ColorChooseButton(Composite parent) { 
		this(parent, DEFAULT_COLOR);
	}
			
	public ColorChooseButton(Composite parent, RGB defaultColor) {
		super(parent, 0);
		
		this.setLayout(new FillLayout());
		
		colorBtn = new Button(this, SWT.PUSH);
        colorBtn.setData(defaultColor);
        
        colorBtn.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				Color c = Colors.createColor((RGB) colorBtn.getData());
				e.gc.setForeground(c);
				e.gc.setBackground(c);
				int o = 0;
				e.gc.fillRectangle(e.x+o, e.y+o, e.width-2*o, e.height-2*o);
			}
		});
        
        colorBtn.addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(SelectionEvent e) {
				if (!editorEnabled) {
					return;
				}
				
				ColorDialog dlg = new ColorDialog(getShell());
				
				dlg.setRGB((RGB) colorBtn.getData());
				dlg.setText("Choose new color!");
				RGB rgb = dlg.open();
				if (rgb != null) {
					String colorStr = Colors.toHex(rgb.red, rgb.green, rgb.blue);
					logger.debug("setting new color: "+colorStr);
					colorBtn.setData(dlg.getRGB());
					colorBtn.redraw();
					
					onColorChanged(dlg.getRGB());
			   }
			}
		});
	}
	
	protected void onColorChanged(RGB rgb) {
		
	}
	
	public RGB getRGB() {
		return (RGB) colorBtn.getData();
	}
	
	public Button getColorBtn() {
		return colorBtn;
	}

	public boolean isEditorEnabled() {
		return editorEnabled;
	}

	public void setEditorEnabled(boolean editorEnabled) {
		this.editorEnabled = editorEnabled;
	}
	
	
	

}
