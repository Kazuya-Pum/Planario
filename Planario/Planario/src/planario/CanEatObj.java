package planario;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public abstract class CanEatObj extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public int size;
	public int localId;
	public int posX;// TODO posX, posYは自身のplanariaでのみ必要
	public int posY;
	public Point current = new Point();

	BufferedImage buffimg;
	Graphics2D bfg;

	public CanEatObj(BufferedImage buffimg, int x, int y, int size) {
		this.buffimg = buffimg;
		setBounds(x, y, size, size);
		setOpaque(false);

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

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}

}
