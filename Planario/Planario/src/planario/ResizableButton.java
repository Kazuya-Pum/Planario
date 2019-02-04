package planario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JButton;

public class ResizableButton extends JButton {
	private static final long serialVersionUID = 1L;

	private BufferedImage buffimg;

	public ResizableButton(String path) {
		buffimg = LoadManager.getBuffImg(path);
	}

	public ResizableButton(BufferedImage buffimg) {
		setImage(buffimg);
	}

	public void setImage(BufferedImage buffimg) {
		this.buffimg = buffimg;
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}
}
