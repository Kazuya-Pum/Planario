package planario;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class BackGroundPane extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	BufferedImage buffimg;
	Graphics2D bfg;

	public BackGroundPane(int size) {
		buffimg = LoadManager.getBuffImg("res/mizuumi.png");
		setBounds(0, 0, size, size);
	}

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}
}
