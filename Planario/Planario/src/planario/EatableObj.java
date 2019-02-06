package planario;

import java.awt.Point;
import java.awt.image.BufferedImage;

public abstract class EatableObj extends Resizable {
	private static final long serialVersionUID = 1L;

	public int size;
	protected int localId;
	public Point current = new Point();

	public EatableObj(BufferedImage buffimg, int x, int y, int size) {
		super(buffimg);

		setBounds(x, y, size);
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

	public void setBounds(int x, int y, int size) {
		super.setBounds(x - (size / 2), y - (size / 2), size, size);
	}

	public int getID() {
		return this.localId;
	}
}
