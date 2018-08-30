import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import eu.transkribus.core.util.SebisStopWatch;
import eu.transkribus.swt.util.ImgLoader;

public class TestImgExifOrientationFix {
	
	public static void main(String[] args) throws Exception {
		SebisStopWatch sw = new SebisStopWatch();
		
		URL imgUrl = null;
		if (false) { // local file
			String imgPath = "./src/test/resources/orientation_test_img.jpg";
			imgUrl = new URL("file:"+imgPath);
		}
		else { // url from filestore
			imgUrl = new URL("https://dbis-thure.uibk.ac.at/f/Get?id=PTNFHVBAYLRLUSCRJUMKETQE&fileType=orig");	
		}
		
		sw.start();
//		File imgFile = new File(imgPath);
		Metadata metadata = ImageMetadataReader.readMetadata(imgUrl.openStream());
		ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		int orientation = 1;
        try {
            orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("orientation: "+orientation);
        sw.stop();
		
        
        if (true) {
		sw.start();
		Image img = ImgLoader.load(imgUrl);
		sw.stop();
		
		System.out.println("w x h = "+img.getImageData().width+" x "+img.getImageData().height);
	        if (true) {
	        	sw.start();
	        	img = ImgLoader.fixOrientation(img, imgUrl);
	        	sw.stop();
	        	
	        	Shell shell = new Shell (Display.getDefault());
	        	shell.setLayout(new FillLayout());
	        	shell.setSize(1200, 1200);
	        	Label label = new Label (shell, SWT.BORDER);
	        	label.setImage(img);
	        	shell.open();
	        	while (!shell.isDisposed ()) {
	                if (!Display.getDefault().readAndDispatch ())
	                	Display.getDefault().sleep();
	            }
	        	
	        	System.out.println("w x h = "+img.getImageData().width+" x "+img.getImageData().height);
	        }
        }
        
		
	}
}
