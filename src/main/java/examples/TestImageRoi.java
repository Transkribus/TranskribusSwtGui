package examples;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;

import eu.transkribus.core.util.ImgUtils;


public class TestImageRoi {
	
	public static int getRGBA(int a, int r, int g, int b) {
		Assert.assertTrue(a <= 255 && a >= 0);
		Assert.assertTrue(r <= 255 && r >= 0);
		Assert.assertTrue(g <= 255 && g >= 0);
		Assert.assertTrue(b <= 255 && b >= 0);

		int rgba = 0;
		rgba |= a << 24;
		rgba |= r << 16;
		rgba |= g << 8;
		rgba |= b;
		
//		System.out.println(Integer.toHexString(rgba));		
		return rgba;
	}
	
	public static void main(String [] args) throws FileNotFoundException, IOException {
//		ImagePlus image = new ImagePlus("file:///home/sebastianc/Bilder/heidi1.gif");
//		image.getBufferedImage().setRGB(1, 0, 3);
		
		
		
		Polygon p = new Polygon();
		p.addPoint(100, 200);
		p.addPoint(200, 400);
//		p.addPoint(100, 600);
		p.addPoint(150, 700);
		p.addPoint(120, 500);
		
		
		System.out.println("poly: "+p+"bounds of poly = "+p.getBounds());
		
		BufferedImage imgOrig = ImgUtils.readImage(new File("file:///home/sebastianc/Bilder/heidi1.gif"));
		System.out.println("im size: "+imgOrig.getWidth()+"x"+imgOrig.getHeight());
		Rectangle bounds = p.getBounds();
		
//		BufferedImage cropped =  new BufferedImage(bounds.width, bounds.height, imgOrig.getType());
		System.out.println("type of image: "+imgOrig.getType());
		
		BufferedImage cropped =  new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
		
		for (int x=0; x<cropped.getWidth(); ++x) {
			for (int y=0; y<cropped.getHeight(); ++y) {
				if (p.contains(x+bounds.x, y+bounds.y)) {
					cropped.setRGB(x, y, imgOrig.getRGB(x+bounds.x, y+bounds.y));
				} else {
					int backColor = getRGBA(0, 0, 0, 0);
					cropped.setRGB(x, y, backColor);
				}
			}
		}
		
		try {
			ImageIO.write(cropped, "PNG", new File("poly_out2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	
//		PolygonRoi proi = new PolygonRoi(p, Roi.POLYGON);
//		image.setRoi(proi);
//		
//		
//		
//		ImageProcessor cr = image.getProcessor().crop();
//		
//		
//		ImagePlus roiIm = new ImagePlus("Polygon", cr);
//		
//		
//		FileSaver fs = new FileSaver(roiIm);
//		
//		fs.saveAsPng("poly_out.png");
		
		System.out.println("DONE!");
	}

}
