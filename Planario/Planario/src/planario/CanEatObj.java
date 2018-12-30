package planario;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public abstract class CanEatObj extends JLabel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public int size;
	public int localId;
	public int posX;
	public int posY;

	public CanEatObj(ImageIcon icon) {
		super(icon);
	}

	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x - (width / 8), y - (height / 8), width, height);
	}

}
