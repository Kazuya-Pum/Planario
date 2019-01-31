package planario;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.*;

public class Drow extends JFrame implements MouseMotionListener, ComponentListener, KeyListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JLayeredPane contentPane;
	public TitlePanel title;
	private GameOverPanel gameOver;
	private FieldPane field;

	private static final int MAX_PLANKTON = 100;
//	private static final int VIRUS = 100;

	MediaTracker tracker;
	public BufferedImage[] skins = new BufferedImage[1];
	private BufferedImage planktonSkin;
	public double Vector2[] = new double[2];

	public int fps = 30;

	MyClient mc;

	Dimension dr;

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
				AUDIO.BGM.restart();
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

		int count = 0;

		for (EatableObj c : mc.GetPlayer(mc.myNumberInt).planariaData.values()) {

			Planaria p = (Planaria) c;
			normalize(mouse.x - prevCenter.x - p.current.x, mouse.y - prevCenter.y - p.current.y);

			p.setData(p.nextX + (int) (Vector2[0] * p.getSpeed()), p.nextY + (int) (Vector2[1] * p.getSpeed()), -1);
			posUpdate(p);
			centerPoint.x += p.current.x;
			centerPoint.y += p.current.y;

			mc.Search(p);
			count++;
		}

		if (count == 0) {
			return;
		}

		try {
			centerPoint.x /= count;
			centerPoint.y /= count;

			centerPoint.x -= dr.width / 2;
			centerPoint.y -= dr.height / 2;
			centerPoint.x *= -1;
			centerPoint.y *= -1;
			field.setLocation(centerPoint);

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

			for (EatableObj p : player.planariaData.values()) {
				Update((Planaria) p);
			}
		}
	}

	public Drow() {
		this(new MyClient());
	}

	public void Login() {
		Point spawnPoint = mc.searchSpawnPoint();

		Create(mc.GetPlayer(mc.myNumberInt).skin, spawnPoint.x, spawnPoint.y, mc.defualtSize);
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

		field = new FieldPane(mc.fieldSize);
		contentPane.add(field);
		contentPane.setLayer(field, JLayeredPane.DEFAULT_LAYER);

		gameOver = new GameOverPanel(dr.width, dr.height, this);

		repaint();

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
		planktonSkin = LoadManager.getBuffImg("res/plankton.png");
		tracker.addImage(planktonSkin, 0);
		skins[0] = LoadManager.getBuffImg("res/planaria.png");
		tracker.addImage(skins[0], 1);

		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int currentSize;
	private int size;

	private void Update(Planaria planaria) {
		currentSize = planaria.getSize().width;
		size = planaria.size;
		if (currentSize != size) {
			size = Lerp(currentSize, size, 0.6f);
		}

		planaria.setBounds(planaria.current.x, planaria.current.y, size, size);
	}

	private void posUpdate(Planaria planaria) {
		planaria.nextX = (planaria.nextX > mc.fieldSize) ? mc.fieldSize : planaria.nextX;
		planaria.nextY = (planaria.nextY > mc.fieldSize) ? mc.fieldSize : planaria.nextY;

		planaria.current.x = Lerp(planaria.current.x, planaria.nextX, 0.25f);
		planaria.current.y = Lerp(planaria.current.y, planaria.nextY, 0.25f);
	}

	public Planaria Create(int skin, int x, int y, int size) {
		return Create(skin, x, y, size, mc.myNumberInt, -1);
	}

	public Planaria Create(int skin, int x, int y, int size, int playerID, int planariaID) {
		Planaria planaria = new Planaria(skins[skin], skin, x, y, size, planariaID);

		mc.GetPlayer(playerID).planariaData.put(planaria.localId, planaria);
		field.add(planaria);
		field.setLayer(planaria, JLayeredPane.PALETTE_LAYER);

		return planaria;
	}

	public Plankton PopPlankton(int x, int y) {
		return PopPlankton(x, y, -1);
	}

	public Plankton PopPlankton(int x, int y, int id) {
		Plankton plankton = new Plankton(planktonSkin, x, y, mc.planktonSize, id);

		field.add(plankton);

		return plankton;
	}

	public void Delete(EatableObj p) {
		if (p == null) {
			return;
		}
		field.remove(p);
	}

	public void fieldReset() {
		field.removeAll();
		repaint();
	}

	private void Spilit() {

		int count = mc.GetPlayer(mc.myNumberInt).getSize();
		for (EatableObj c : mc.GetPlayer(mc.myNumberInt).planariaData.values()) {

			Planaria planaria = (Planaria) c;
			if (planaria.size < mc.defualtSize * 2) {
				continue;
			}

			planaria.setData(-1, -1, planaria.size / 2);
			Planaria child = Create(planaria.skin, planaria.current.x, planaria.current.y, planaria.size);
			child.setData(planaria.current.x + (int) (Vector2[0] * 300), planaria.current.y + (int) (Vector2[1] * 300),
					-1);

			if (--count <= 0) {
				break;
			}
		}
	}

	public void VirusSpilit(Planaria planaria) {
		int count = planaria.size / mc.defualtSize;

		count = (count > 5) ? 5 : count;

		int size = planaria.size / count;

		planaria.setData(-1, -1, size);

		for (int i = 1; i < count; i++) {

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
		contentPane.add(gameOver);
		contentPane.setLayer(gameOver, JLayeredPane.MODAL_LAYER);
		gameOver.requestFocus();

		repaint();
	}

	public void hideGameOver() {
		setTitlePane();
		AUDIO.BGM.restart();
		try {
			contentPane.remove(gameOver);
		} catch (Exception e) {

		}
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
			gameOver.setSize(dr.width, dr.height);
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
