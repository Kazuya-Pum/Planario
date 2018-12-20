package planario;

import javax.swing.ImageIcon;

public class Plankton extends CanEatObj {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static int id;

	public Plankton(ImageIcon icon, int posX, int posY, int size, int id) {
		super(icon);
		this.size = size;

		if (id != -1) {
			this.localId = id;
		} else {
			this.localId = Planaria.id++;
		}
	}

}