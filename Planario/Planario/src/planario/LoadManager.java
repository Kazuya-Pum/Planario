package planario;

import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class LoadManager {

	private static LoadManager lm = new LoadManager();

	private LoadManager() {
	}

	public static URL getUrl(String path) {
		return lm.getClass().getClassLoader().getResource(path);
	}

	public static BufferedImage getBuffImg(String path) {
		try {
			return ImageIO.read(getUrl(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage getBuffImg(String path, MediaTracker tracker) {
		try {
			BufferedImage buffimg = ImageIO.read(getUrl(path));
			tracker.addImage(buffimg, 100);
			tracker.waitForID(100);
			return buffimg;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ImageIcon getIcon(String path) {
		return new ImageIcon(getUrl(path));
	}
}
