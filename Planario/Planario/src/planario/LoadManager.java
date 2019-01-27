package planario;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.ImageProducer;
import java.net.URL;

public class LoadManager extends Component {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static LoadManager lm = new LoadManager();

	public static Image loadImage(String path) {
		URL url = lm.getClass().getClassLoader().getResource(path);
		try {
			return lm.createImage((ImageProducer) url.getContent());
		} catch (Exception ex) {
			System.out.println("Resource Error!");
			return null;
		}
	}
}
