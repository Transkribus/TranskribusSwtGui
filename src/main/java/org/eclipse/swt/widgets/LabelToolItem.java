package org.eclipse.swt.widgets;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

import eu.transkribus.swt_canvas.util.Fonts;

public class LabelToolItem extends ACustomToolItem {
	
	public final static int DEFAULT_FONT_SIZE = 10;

	CLabel label;
//	Label label;
	
	/**
	 * @wbp.parser.constructor
	 */
	public LabelToolItem(ToolBar parent, int style) {
		super(parent, style);
	}

	public LabelToolItem(ToolBar parent, int style, int index) {
		super(parent, style, index);
	}

	@Override
	protected void initControl() {
		label = new CLabel(parent, controlStyle);
//		label = new Label(parent, controlStyle);

		FontData[] fD = label.getFont().getFontData();
		fD[0].setHeight(DEFAULT_FONT_SIZE);
//		label.setFont(new Font(display,fD[0]));
		label.setFont(Fonts.createFont(fD[0]));
		
		this.setControl(label);
	}

	@Override
	public void setText(String string) {
		label.setText(string);
		updateSize();
	}
	
	@Override
	public void setImage(Image image) {
		label.setImage(image);
		updateSize();
	}

	@Override
	public String getText() {
		return label.getText();
	}
	
	public CLabel getLabel() { return label; }
//	public Label getLabel() { return label; }

}
