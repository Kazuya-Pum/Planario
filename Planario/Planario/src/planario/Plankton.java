package planario;

import javax.swing.ImageIcon;

public class Plankton extends CanEatObj {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static int id;

	public Plankton(ImageIcon icon, int posX, int posY, int size, int id) {
		super(icon, posX, posY, size);

		if (id == -1) {
			this.localId = ++Plankton.id;
		} else {
			this.localId = id;
			Plankton.id = id;
		}
	}
}
