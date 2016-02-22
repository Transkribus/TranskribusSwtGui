package examples;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
public class GetAllFonts {
  public static void main(String[] a) {
    GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Font[] fonts = e.getAllFonts(); // Get the fonts
    
//    for (FontData fd : Display.getDefault().getFontList(null, true)) {
//    	System.out.println(fd.getStyle());
//    }
    
    
//    for (Font f : fonts) {
//      System.out.println(f.getFontName());
//    }
//    
//    System.out.println("font families:");
//    for (String f : e.getAvailableFontFamilyNames()) {
//        System.out.println(f);
//    }
    
//    FontDialog fd = new FontDialog();
  }
}