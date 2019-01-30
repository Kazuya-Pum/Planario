package planario;

import java.awt.image.BufferedImage;


public class Planaria extends CanEatObj {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static int id;
	int skin;
	double speed;

	public Planaria(BufferedImage buffimg, int skin, int posX, int posY, int size, int id) {
		super(buffimg, posX, posY, size);
		this.skin = skin;

		speed = chSpeed(this.size);

		if (id != -1) {
			this.localId = id;
		} else {
			this.localId = Planaria.id++;
		}
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
			speed = chSpeed(this.size);
		}
	}

	public void setCurrent(int x, int y, int size) {
		if (x >= 0) {
			current.x = x;
		}
		if (y >= 0) {
			current.y = y;
		}
		if (size > 0) {
			this.size = size;
			speed = chSpeed(this.size);
		}
	}

	private double chSpeed(int size) {
		double tmp = 10 - (0.005 * size);
		return (tmp < 1) ? 1 : tmp;
	}
}
