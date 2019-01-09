package planario;

import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public abstract class CanEatObj extends JLabel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public int size;
	public int localId;
	public int posX;// TODO posX, posYは自身のplanariaでのみ必要
	public int posY;
	public Point current = new Point();

	public CanEatObj(ImageIcon icon, int x, int y, int size) {
		super(icon);

		if (x >= 0) {
			posX = x;
			current.x = x;
		} else {
			posX = 0;
			current.x = 0;
		}

		if (y >= 0) {
			posY = y;
			current.y = y;
		} else {
			posY = 0;
			current.y = 0;
		}

		this.size = (size > 0) ? size : 1;

	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x - (width / 2), y - (height / 2), width, height);
	}

	public int getVisualSize() {
		return size / 3;
	}

}
