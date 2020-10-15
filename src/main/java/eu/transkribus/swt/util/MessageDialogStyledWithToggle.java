package eu.transkribus.swt.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of JFace's MessageDialogWithToggle that displays the message within a StyledText widget.<b>
 * URLs within the message are automatically marked up and will be opened in the system browser on click.
 */
public class MessageDialogStyledWithToggle extends MessageDialogWithToggle {
	private static final Logger logger = LoggerFactory.getLogger(MessageDialogStyledWithToggle.class);
	
	private static String URL_PATTERN = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	public MessageDialogStyledWithToggle(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType,
			LinkedHashMap<String, Integer> buttonLabelToIdMap, int defaultIndex, String toggleMessage,
			boolean toggleState) {
		super(parentShell, dialogTitle, image, message, dialogImageType, buttonLabelToIdMap, defaultIndex, toggleMessage,
				toggleState);
	}
	
	public MessageDialogStyledWithToggle(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex, toggleMessage,
				toggleState);
	}

	@Override
	protected Control createMessageArea(Composite composite) {
		// default code from super class
		// create composite
		// create image
		Image image = getImage();
		if (image != null) {
			imageLabel = new Label(composite, SWT.NULL);
			image.setBackground(imageLabel.getBackground());
			imageLabel.setImage(image);
			GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.BEGINNING)
					.applyTo(imageLabel);
		}
		// create message: custom code
		// For now use StyledText and make only links clickable.
		if (message != null) {			
			StyledText area = new StyledText(composite, SWT.READ_ONLY | SWT.BORDER | super.getMessageLabelStyle());
			area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			final int margin = 5;
			area.setMargins(margin, margin, margin, margin);
			area.setEditable(false);
			area.setText(super.message);
			area.setStyleRanges(createStyleRanges(super.message));
			area.addListener(SWT.MouseDown, event -> {
				logger.debug("Event: {}", event);
		        int offset = area.getOffsetAtPoint(new Point(event.x, event.y));
		        if (offset == -1) {
		        	logger.debug("Event target offset out of bounds.");
		        	return;
		        }
		        StyleRange target;
	            try {
	                target = area.getStyleRangeAtOffset(offset);
	            } catch (IllegalArgumentException e) {
	                logger.debug("No link found at mouse event target.");
	                return;
	            }
	            if (target != null && target.underline && target.underlineStyle == SWT.UNDERLINE_LINK) {
	            	String url = (String) target.data;
	                logger.debug("Link clicked: {}", url);
	                DesktopUtil.browse(url, null, this.getParentShell());
	            }
			});
			GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false)
				.hint(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), SWT.DEFAULT)
				.applyTo(area);
		}
		return composite;
	}

	private StyleRange[] createStyleRanges(String string) {
		if(StringUtils.isEmpty(string)) {
			return new StyleRange[] {};
		}
		Pattern p = Pattern.compile(URL_PATTERN);
		Matcher m = p.matcher(string);
		List<StyleRange> l = new ArrayList<>();
		while(m.find()) {
			logger.debug("Found link in message: {}", m.group());
			StyleRange styleRange = new StyleRange(m.start(), m.end() - m.start(), Colors.getSystemColor(SWT.COLOR_DARK_BLUE), null);
			styleRange.underline = true;
			styleRange.underlineStyle = SWT.UNDERLINE_LINK;
			styleRange.data = m.group();
			l.add(styleRange);
		}
		return l.toArray(new StyleRange[l.size()]);
	}
}
