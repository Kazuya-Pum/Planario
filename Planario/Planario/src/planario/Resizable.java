package planario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JLayeredPane;

public abstract class Resizable extends JLayeredPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage buffimg;

	public Resizable(BufferedImage buffimg) {
		this.buffimg = buffimg;
	}

	public Resizable(String path) {
		this.buffimg = LoadManager.getBuffImg(path);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}
}
