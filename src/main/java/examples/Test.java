package examples;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Test {
	// file:///home/sebastianc/Downloads/LeedsUni10-12-13.ttf
		
	public static void main(String[] args) throws Exception {
		// This font is < 35Kb.
		URL fontUrl = new URL("http://www.webpagepublicity.com/" + "free-fonts/a/Airacobra%20Condensed.ttf");
		
		InputStream is = null;
		
//		is = Test.class.getResourceAsStream("/LeedsUni10-12-13.ttf");
		is = Test.class.getResourceAsStream("/Andron Scriptor Web.ttf");
		
		System.out.println("is = "+is);
		
		
		Font font = Font.createFont(Font.TRUETYPE_FONT, is);
		
		
		List<Character> charList = new ArrayList<>();
//		for (char c = 0x0000; c <= 0xFFFF; c++) {
		for (int c = 0; c < 65536; c++) {
			System.out.println(c+" / "+c+": ");
			System.out.println(0xFFFF);
//		  if (font.canDisplay(c)) {
//			  System.out.println("can display");
//			  charList.add(c);
//		  }
//		  else
//			  System.out.println("cannot display");
		}
		System.out.println("can display list: "+charList);
		
		
		System.out.println("font = "+font);
		System.out.println(font.getNumGlyphs());
		
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(font);
		
		
		font = font.deriveFont(20.0f);
		
//		font.
//		
//		font.createGlyphVector(frc, new char[]);
//		
		
//		String sentence = "The quick brown fox jumps over the lazy dog. 0123456789";		
//		JLabel l = new JLabel(sentence);
//	        l.setFont(font);
//	        
//	    JOptionPane.showMessageDialog(null, l);		
		
//		JList fonts = new JList(ge.getAvailableFontFamilyNames());
//		JOptionPane.showMessageDialog(null, new JScrollPane(fonts));
	}

}
