package planario;

import java.awt.image.BufferedImage;

public class Plankton extends CanEatObj {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static int id;

	public Plankton(BufferedImage buffimg, int posX, int posY, int size, int id) {
		super(buffimg, posX, posY, size);

		if (id == -1) {
			this.localId = ++Plankton.id;
		} else {
			this.localId = id;
			Plankton.id = id;
		}
	}
}
