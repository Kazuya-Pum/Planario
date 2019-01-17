package planario;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class GameOverPanel extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage buffimg;
	Graphics bfg;

	public GameOverPanel(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		try {
			buffimg = ImageIO.read(new File("GameOver.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		bfg = buffimg.createGraphics();
		setOpaque(false);
	}

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}
}
