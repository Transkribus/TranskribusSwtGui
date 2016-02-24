package eu.transkribus.swt_canvas.util;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

public class Images {
	public static final Image LOCK = getOrLoad("/icons/lock.png");
	public static final Image LOCK_OPEN = getOrLoad("/icons/lock_open.png");
	public static final Image DELETE = getOrLoad("/icons/delete.png");
	public static final Image CROSS = getOrLoad("/icons/cross.png");
	public static final Image ADD = Images.getOrLoad("/icons/add.png");
	public static final Image APPLICATION_DOUBLE = Images.getOrLoad("/icons/application_double.png");
	public static final Image LOADING_IMG = Images.getOrLoad("/icons/loading.gif");
	public static final Image ERROR_IMG = Images.getOrLoad("/icons/broken_image.png");
	public static final Image COMMENT = Images.getOrLoad("/icons/comment.png");
	
	public static final Image IMAGE_EDIT = Images.getOrLoad("/icons/image_edit.png");
	public static final Image IMAGE_DELETE = Images.getOrLoad("/icons/image_delete.png");
	
	public static final Image APPLICATION_SIDE_CONTRACT = getOrLoad("/icons/application_side_contract.png");
	public static final Image APPLICATION_SIDE_EXPAND = getOrLoad("/icons/application_side_expand.png");
	public static final Image APPLICATION_SIDE_PUT = getOrLoad("/icons/application_put.png");
	public static final Image REFRESH = getOrLoad("/icons/refresh.png");
	
	public static final Image ARROW_UP = getOrLoad("/icons/arrow_up.png");
	public static final Image ARROW_DOWN = getOrLoad("/icons/arrow_down.png");
	public static final Image ARROW_LEFT = getOrLoad("/icons/arrow_left.png");
	public static final Image TICK = getOrLoad("/icons/tick.png");
	public static final Image FIND = getOrLoad("/icons/find.png");
	
	public static final Image DISK = getOrLoad("/icons/disk.png");
	public static final Image PAGE_NEXT = getOrLoad("/icons/page-next.gif");
	public static final Image PAGE_PREV = getOrLoad("/icons/page-prev.gif");
	
	public static final Image PENCIL = getOrLoad("/icons/pencil.png");
	
	public static final Image GROUP = getOrLoad("/icons/group.png");

	static HashMap<String, Image> imageMap;
	
	public static Image getSystemImage(int swtSysImg) {
		return Display.getDefault().getSystemImage(swtSysImg);
	}
	
	public static Image getOrLoad(String path) {
		if (imageMap==null)
			imageMap = new HashMap<String, Image>();
		
		Image img = imageMap.get(path);
		if (img == null) {
			img = SWTResourceManager.getImage(Images.class, path);
			imageMap.put(path, img);
		}
		return img;
	}
}
