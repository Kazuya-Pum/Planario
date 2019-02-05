package planario;

import java.awt.Point;
import java.awt.image.BufferedImage;

public class Planaria extends EatableObj {
	private static final long serialVersionUID = 1L;

	private static int id;
	private final int skin;
	private double speed;

	private Point next = new Point();

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
			next.x = x;
		}
		if (y >= 0) {
			next.y = y;
		}

		this.setEatSize(size);
	}

	public void setNext(int x, int y) {
		setData(x, y, -1);
	}

	public void setEatSize(int size) {
		if (size > 0) {
			super.size = size;
			speed = chSpeed(super.size);
		}
	}

	public Point getNext() {
		return this.next;
	}

	public void setCurrent(int x, int y, int size) {
		if (x >= 0) {
			current.x = x;
		}
		if (y >= 0) {
			current.y = y;
		}

		this.setEatSize(size);
	}

	private double chSpeed(int size) {
		double tmp = 10 - (0.01 * size);
		return (tmp < 1) ? 1 : tmp;
	}

	public double getSpeed() {
		return this.speed;
	}

	public int getSkin() {
		return this.skin;
	}
}
