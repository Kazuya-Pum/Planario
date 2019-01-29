package planario;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import javax.swing.*;

public class Drow extends JFrame implements MouseMotionListener, ComponentListener, KeyListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JLayeredPane contentPane;
	public JPanel panel;
	private JPanel backGound;
	private JLayeredPane borderPane;
	public TitlePanel title;
	private GameOverPanel gameOver;

	private static final int MAX_PLANKTON = 100;

	MediaTracker tracker;
	public ImageIcon[] skins = new ImageIcon[3];
	public double Vector2[] = new double[2];

	public int fps = 30;

	MyClient mc;

	Dimension dr;

	int sizeRate = 3;

	boolean init = false;

	Random random = new Random();

	private Point mouse = new Point();

	public class DrowThread extends Thread {
		public void run() {
			try {
				System.out.println("nowLoading");
				while (!init) {
					sleep(10);
				}
				System.out.println("ok");
			} catch (Exception e) {
				e.printStackTrace();
			}

			mc.loginFlag = true;

			mc.mst.start();

			while (mc.loginFlag) {
				try {
					tracker.waitForAll();

					MyUpdate();
					OtherUpdate();

					if (mc.planktons.getSize() <= MAX_PLANKTON && random.nextInt(10) == 0) {
						mc.Pop();
					}

					repaint();
					sleep(fps);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Point centerPoint = new Point();
	Point prevCenter = new Point();

	private void MyUpdate() {
		centerPoint.x = 0;
		centerPoint.y = 0;

		int count = mc.GetPlayer(mc.myNumberInt).getSize();

		if (count == 0) {
			return;
		}

		for (CanEatObj c : mc.GetPlayer(mc.myNumberInt).planariaData.values()) {

			Planaria p = (Planaria) c;
			normalize(mouse.x - prevCenter.x - p.current.x, mouse.y - prevCenter.y - p.current.y);

			p.setData(p.posX + (int) (Vector2[0] * p.speed), p.posY + (int) (Vector2[1] * p.speed), -1);
			posUpdate(p);
			centerPoint.x += p.current.x;
			centerPoint.y += p.current.y;

			mc.Search(p);
		}

		try {
			centerPoint.x /= count;
			centerPoint.y /= count;

			centerPoint.x -= dr.width / 2;
			centerPoint.y -= dr.height / 2;
			centerPoint.x *= -1;
			centerPoint.y *= -1;
			panel.setLocation(centerPoint);
			backGound.setLocation(centerPoint);

			prevCenter.x = centerPoint.x;
			prevCenter.y = centerPoint.y;

		} catch (ArithmeticException e) {

		}
	}

	private void OtherUpdate() {

		for (PlayerData player : mc.playerData.values()) {
			if (player.playerID == 0) {
				continue;
			}

			for (CanEatObj p : player.planariaData.values()) {
				Update((Planaria) p);
			}
		}
	}

	public Drow() {
		this(new MyClient());
	}

	public void Login() {
		Point spawnPoint = mc.searchSpawnPoint();

		Create(1, spawnPoint.x, spawnPoint.y, mc.defualtSize);
		DrowThread dt = new DrowThread();
		dt.start();
	}

	/**
	 * Create the frame.
	 */
	public Drow(MyClient mc) {
		this.mc = mc;

		tracker = new MediaTracker(this);
		ImportSkins();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1024, 640);
		setLocationRelativeTo(null);
		setTitle("Planar.io");

		contentPane = new JLayeredPane();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.addMouseMotionListener(this);
		contentPane.addComponentListener(this);
		addKeyListener(this);
	}

	private void initialize() {
		dr = contentPane.getSize();

		setTitlePane();

		borderPane = new JLayeredPane();
		borderPane.setLayout(new BorderLayout(0, 0));
		borderPane.setSize(dr.width, dr.height);
		contentPane.add(borderPane);
		contentPane.setLayer(borderPane, JLayeredPane.DEFAULT_LAYER);

		JLayeredPane field = new JLayeredPane();
		field.setLayout(null);
		borderPane.add(field);
		borderPane.setLayer(field, JLayeredPane.DEFAULT_LAYER);

		panel = new JPanel();
		panel.setBounds(0, 0, mc.fieldSize, mc.fieldSize);
		panel.setOpaque(false);
		field.add(panel);
		field.setLayer(panel, JLayeredPane.PALETTE_LAYER);
		panel.setLayout(null);

		backGound = new JPanel();
		backGound.setBounds(0, 0, mc.fieldSize, mc.fieldSize);
		field.add(backGound);
		field.setLayer(backGound, JLayeredPane.DEFAULT_LAYER);

		JLabel backGoundLabel = new JLabel(ResizeIcon(0, mc.fieldSize));
		backGound.add(backGoundLabel);

		init = true;
	}

	public void setTitlePane() {
		if (title == null) {
			title = new TitlePanel(mc, dr.width, dr.height);
		}

		title.setSize(dr.width, dr.height);
		contentPane.add(title);
		contentPane.setLayer(title, JLayeredPane.POPUP_LAYER);
		title.ipStr.requestFocus();
	}

	public void hideTilePane() {
		try {
			requestFocus();
			contentPane.remove(title);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private void ImportSkins() {
		skins[0] = LoadManager.getIcon("res/mizuumi.png");
		skins[1] = LoadManager.getIcon("res/planaria.png");
		skins[2] = LoadManager.getIcon("res/plankton.png");
		skins[2] = ResizeIcon(2, 10);
	}

	private int currentSize;
	private int size;
	private int iconSize;

	private void Update(Planaria planaria) {
		currentSize = planaria.getIcon().getIconWidth();
		size = planaria.size / sizeRate;
		iconSize = size;
		if (currentSize != size) {
			iconSize = Lerp(currentSize, size, 0.6f);
			planaria.setIcon(ResizeIcon(planaria.skin, iconSize));
		}

		planaria.setBounds(planaria.current.x, planaria.current.y, iconSize, iconSize);
	}

	private void posUpdate(Planaria planaria) {
		planaria.posX = (planaria.posX > mc.fieldSize) ? mc.fieldSize : planaria.posX;
		planaria.posY = (planaria.posY > mc.fieldSize) ? mc.fieldSize : planaria.posY;

		planaria.current.x = Lerp(planaria.current.x, planaria.posX, 0.25f);
		planaria.current.y = Lerp(planaria.current.y, planaria.posY, 0.25f);
	}

	private ImageIcon ResizeIcon(int icon, int size) {
		Image resizeImg = skins[icon].getImage().getScaledInstance(size, -1, Image.SCALE_SMOOTH);
		tracker.addImage(resizeImg, 1);
		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			System.out.println("error: ResizeIcon");
		}

		return new ImageIcon(resizeImg);
	}

	public Planaria Create(int skin, int x, int y, int size) {
		return Create(skin, x, y, size, mc.myNumberInt, -1);
	}

	public Planaria Create(int skin, int x, int y, int size, int playerID, int planariaID) {
		Planaria planaria = new Planaria(ResizeIcon(skin, size / sizeRate), skin, x, y, size, planariaID);
		planaria.setBounds(x, y, size / sizeRate, size / sizeRate);

		mc.GetPlayer(playerID).planariaData.put(planaria.localId, planaria);
		panel.add(planaria);

		return planaria;
	}

	public Plankton PopPlankton(int x, int y) {
		return PopPlankton(x, y, -1);
	}

	public Plankton PopPlankton(int x, int y, int id) {
		Plankton plankton = new Plankton(skins[2], x, y, mc.planktonSize, id);
		plankton.setBounds(x, y, mc.planktonSize, mc.planktonSize);

		panel.add(plankton);

		return plankton;
	}

	public void Delete(CanEatObj p) {
		if (p == null) {
			return;
		}
		panel.remove(p);
	}

	private void Spilit() {

		for (CanEatObj c : mc.GetPlayer(mc.myNumberInt).planariaData.values()) {

			Planaria planaria = (Planaria) c;
			if (planaria.size / 3 < 30) {
				continue;
			}

			planaria.setData(-1, -1, planaria.size / 2);
			Planaria child = Create(planaria.skin, planaria.current.x, planaria.current.y, planaria.size);
			child.setData(planaria.current.x + (int) (Vector2[0] * 300), planaria.current.y + (int) (Vector2[1] * 300),
					-1);
		}
	}

	private void normalize(int x, int y) {
		double mag = Math.hypot(x, y);

		double dist = (mag > 100) ? 1 : mag / 100;

		Vector2[0] = x * dist / mag;
		Vector2[1] = y * dist / mag;
	}

	private int Lerp(int from, int to, float t) {
		boolean positive = from < to;

		int value = (int) Math.ceil(Math.abs(to - from) * t);

		value *= (positive) ? 1 : -1;

		return from + value;
	}

	public void setGameOver() {
		if (gameOver == null) {
			gameOver = new GameOverPanel(dr.width, dr.height, this);
		}

		gameOver.setScoreText(mc.score);
		gameOver.setSize(dr.width, dr.height);
		borderPane.add(gameOver, BorderLayout.CENTER);
		borderPane.setLayer(gameOver, JLayeredPane.MODAL_LAYER);
		gameOver.requestFocus();

		repaint();
		System.out.println("GameOver");
	}

	public void hideGameOver() {
		setTitlePane();
		try {
			borderPane.remove(gameOver);
		} catch (Exception e) {

		}
	}

	public void reStart() {

	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouse = e.getPoint();
	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {

	}

	@Override
	public void componentResized(ComponentEvent e) {
		dr = contentPane.getSize();

		if (!init) {
			initialize();
		} else {
			borderPane.setSize(dr.width, dr.height);
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			Spilit();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
}
