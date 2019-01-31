package planario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLayeredPane;

public class FieldPane extends JLayeredPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	BufferedImage buffimg;

	public FieldPane(int size) {
		buffimg = LoadManager.getBuffImg("res/mizuumi.png");
		setBounds(0, 0, size, size);
		setOpaque(false);
		setLayout(null);
	}

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}
}
