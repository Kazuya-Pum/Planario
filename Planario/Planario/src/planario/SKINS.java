package planario;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

public enum SKINS {
	DEFAULT("res/planaria.png", 0), PURPLE("res/Pp.png", 1), RED("res/Pr.png", 2), YELLOW("res/Py.png", 3),
	BLACK("res/Pb.png", 4), HEAD3("res/P3.png", 5);

	private static JLayeredPane basePane = new Resizable("res/backMenu.png");
	private static int selected = 0;
	private int id;
	private BufferedImage buffimg;

	private static JLabel mark = (new JLabel() {
		private static final long serialVersionUID = 1L;
		private BufferedImage buffimg = LoadManager.getBuffImg("res/en.png");

		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
		}
	});

	private JButton btn;

	private SKINS(String path, int id) {

		buffimg = LoadManager.getBuffImg(path);
		this.id = id;

		btn = new ResizableButton(buffimg);

		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				selectSkin();
			}
		});
	}

	private int getID() {
		return id;
	}

	public BufferedImage getBuffimg() {
		return buffimg;
	}

	public static int getSelect() {
		return selected;
	}

	public void selectSkin() {
		selected = id;
		mark.setLocation(btn.getLocation());
	}

	public JButton getButton() {
		return btn;
	}

	public static SKINS getSkin(int id) {
		for (SKINS s : values()) {
			if (s.getID() == id) {
				return s;
			}
		}

		return null;
	}

	public static JLayeredPane getPane() {
		return basePane;
	}

	// 初期化
	public static void init() {
		int size = 100;

		mark.setOpaque(false);
		mark.setSize(size, size);

		basePane.setLayout(null);
		basePane.setOpaque(false);
		basePane.add(mark);
		basePane.setLayer(mark, JLayeredPane.DEFAULT_LAYER);

		values();

		int r = 4;
		int x = 0;
		int y = 0;
		double count = 0;
		for (SKINS s : values()) {
			basePane.add(s.btn, JLayeredPane.PALETTE_LAYER);
			s.btn.setBounds(50 + (size + 10) * x++, 50 + (size + 10) * y, size, size);
			s.btn.setOpaque(false);
			s.btn.setBorderPainted(false);

			count++;
			if (x >= r) {
				x = 0;
				y++;
			}
		}

		int width = 0;
		int height = 0;
		if (y > 0) {
			width = 100 + (size + 10) * r;
			height = 100 + (size + 10) * (int) Math.ceil(count / r);
		} else {
			width = 100 + (size + 10) * x;
			height = 100 + size;
		}

		basePane.setSize(width, height);

		getSkin(selected).selectSkin();
	}
}
