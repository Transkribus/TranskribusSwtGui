package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.FontData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.transkribus.swt.util.Colors;
import eu.transkribus.swt.util.Fonts;

/** @deprecated This class causes problems and looks ugly - use {@link org.dea.swt.util.DropDownToolItem} instead! */
public class ComboToolItem extends ACustomToolItem {
	private final static Logger logger = LoggerFactory.getLogger(ComboToolItem.class);
	
//	Combo combo;
	CCombo combo;
	boolean isReadOnly = false;
	
	public ComboToolItem(ToolBar parent, int style) {
		super(parent, style);
	}

	public ComboToolItem(ToolBar parent, int style, int index) {
		super(parent, style, index);
	}
	
	@Override
	protected void initControl() {
		isReadOnly = (controlStyle & SWT.READ_ONLY) != 0;
		// BUG in SWT: clear READ_ONLY bit since this does block the ProgressMonitorDialog
		combo = new CCombo(parent, controlStyle | SWT.FLAT | SWT.SINGLE | SWT.BORDER) {
			@Override
			protected void checkSubclass() {
			}
//			
//			public void setSelection(Point selection) {
//				deselectAll();
//			}
			
		};
//		combo = new CCombo(parent, controlStyle & ~SWT.READ_ONLY);

		FontData[] fD = combo.getFont().getFontData();
		fD[0].setHeight(DEFAULT_FONT_SIZE);
//		combo.setFont(new Font(display,fD[0]));
		combo.setFont(Fonts.createFont(fD[0]));
		
		this.setControl(combo);
		
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setWidth(combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
			}
		});
		
		combo.setEditable(false);
		combo.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		combo.setListVisible(false);
			
//		if (isReadOnly) {
//			logger.debug("combo is read only!");
//			combo.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//			
//			
//			
//			combo.addKeyListener(new KeyListener() {
//	
//				@Override
//				public void keyPressed(KeyEvent e) {
//					
//					e.doit=false;
//				}
//	
//				@Override
//				public void keyReleased(KeyEvent e) {
//					
//					e.doit=false;
//				}
//			});
//		}
	}

//	@Override
//	protected void initControl2() {
//		isReadOnly = (controlStyle & SWT.READ_ONLY) != 0;
//		// BUG in SWT: clear READ_ONLY bit since this does block the ProgressMonitorDialog
//		combo = new Combo(parent, controlStyle & ~SWT.READ_ONLY);
//
//		FontData[] fD = combo.getFont().getFontData();
//		fD[0].setHeight(DEFAULT_FONT_SIZE);
//		combo.setFont(new Font(display,fD[0]));
//		
//		this.setControl(combo);
//		
//		combo.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				setWidth(combo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
//			}
//		});
//			
//		if (isReadOnly) {
//			logger.debug("combo is read only!");
//			combo.setBackground(Colors.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//			
//			
//			
//			combo.addKeyListener(new KeyListener() {
//	
//				@Override
//				public void keyPressed(KeyEvent e) {
//					
//					e.doit=false;
//				}
//	
//				@Override
//				public void keyReleased(KeyEvent e) {
//					
//					e.doit=false;
//				}
//			});
//		}
//	}
	
	public CCombo getCombo() { return combo; }

}
