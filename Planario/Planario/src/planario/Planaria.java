package planario;

import java.awt.image.BufferedImage;

public class Planaria extends EatableObj {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static int id;
	int skin;
	private double speed;

	public int nextX;
	public int nextY;

	public Planaria(BufferedImage buffimg, int skin, int x, int y, int size, int id) {
		super(buffimg, x, y, size);
		this.skin = skin;

		setData(x, y, size);

		if (id != -1) {
			this.localId = id;
		} else {
			this.localId = Planaria.id++;
		}
	}

	public void setData(int x, int y, int size) {

		if (x >= 0) {
			nextX = x;
		}
		if (y >= 0) {
			nextY = y;
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
		double tmp = 10 - (0.01 * size);
		return (tmp < 1) ? 1 : tmp;
	}

	public double getSpeed() {
		return this.speed;
	}
}
