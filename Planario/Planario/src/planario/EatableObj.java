package planario;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public abstract class EatableObj extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public int size;
	public int localId;
	public Point current = new Point();

	BufferedImage buffimg;

	public EatableObj(BufferedImage buffimg, int x, int y, int size) {
		this.buffimg = buffimg;
		setBounds(x, y, size, size);
		setOpaque(false);

		if (x >= 0) {
			current.x = x;
		} else {
			current.x = 0;
		}

		if (y >= 0) {
			current.y = y;
		} else {
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

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}

}
