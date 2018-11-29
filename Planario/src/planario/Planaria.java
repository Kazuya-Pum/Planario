package planario;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class Planaria extends JLabel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	int posX, posY;
	int size = 99;
	static long id;
	long localId;
	int skin;
	int speed;

	public Planaria(ImageIcon icon, int skin, int posX, int posY, int size) {
		setIcon(icon);
		this.skin = skin;

		this.localId = id++;
		setData(posX, posY, size);
	}

	public void setData(int x, int y, int size) {

		if (x >= 0) {
			posX = x;
		}
		if (y >= 0) {
			posY = y;
		}
		if (size > 0) {
			this.size = size;
			speed = 100 / size;
		}
	}
}
