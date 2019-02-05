package planario;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JLayeredPane;

public class Resizable extends JLayeredPane {
	private static final long serialVersionUID = 1L;
	private BufferedImage buffimg;

	public Resizable(BufferedImage buffimg) {
		this.buffimg = buffimg;
	}

	// パスで指定
	public Resizable(String path) {
		this.buffimg = LoadManager.getBuffImg(path);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);	// コンポーネントの大きさに合わせてbuffimgが描画される
	}
}
