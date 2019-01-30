package planario;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class LoadManager extends Component {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static LoadManager lm = new LoadManager();

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

	public static ImageIcon getIcon(String path) {
		return new ImageIcon(getUrl(path));
	}

	public static Image loadImage(String path) {
		URL url = getUrl(path);
		try {
			return lm.createImage((ImageProducer) url.getContent());
		} catch (Exception ex) {
			System.out.println("Resource Error!");
			return null;
		}
	}
}
