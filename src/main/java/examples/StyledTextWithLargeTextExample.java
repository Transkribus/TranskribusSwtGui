package examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class StyledTextWithLargeTextExample {

	private static final int LINES = 500000;
	private static final int LINES_PER_RANGE_CHANGE = 10;
	private static final int WORDS_PER_LINE = 10;
	private static final String WORD = "123456789 ";

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setBounds(10, 10, 300, 300);
		shell.setLayout(new FillLayout());
		final StyledText text = new StyledText(shell, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		Color red = new Color(display, 255, 0, 0);

		StringBuilder buffer = new StringBuilder(LINES * WORDS_PER_LINE * WORD.length() * 2);
		StyleRange[] ranges = new StyleRange[LINES / LINES_PER_RANGE_CHANGE * 2];
		for (int i = 0; i < LINES / LINES_PER_RANGE_CHANGE; i++) {
			int start = buffer.length();
			for (int k = 0; k < LINES_PER_RANGE_CHANGE - 1; k++) {
				buffer.append("Group " + i + " Line " + k + ": ");
				for (int j = 0; j < WORDS_PER_LINE; j++) {
					buffer.append(WORD);
				}
				buffer.append("\n");
			}
			int length = buffer.length() - start;
			ranges[i * 2] = new StyleRange(start, length, null, null);

			int startHighlight = buffer.length();
			buffer.append("Group " + i + " Highlight" + ": ");
			for (int j = 0; j < WORDS_PER_LINE; j++) {
				buffer.append(WORD);
			}
			buffer.append("\n");
			int lengthHighlight = buffer.length() - startHighlight;
			ranges[i * 2 + 1] = new StyleRange(startHighlight, lengthHighlight, red, null);
		}
		
		
		shell.open();
		text.setText(buffer.toString());
		// Uncomment to test with ranges, not sure it makes a big
		// difference  
		// text.setStyleRanges(ranges);
		System.out.println("Total buffer length: " + buffer.length());
		System.out.println("Total number of ranges: " + ranges.length);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
