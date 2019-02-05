package planario;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.*;

public class Drow extends JFrame implements MouseMotionListener, ComponentListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private JLayeredPane contentPane;
	private TitlePanel title;
	private GameOverPanel gameOver;
	private FieldPane field;

	public static final int FPS = 30;
	private static final int VIRUS = 90; // ウイルスのサイズ
	private static final int SHRINK = 2000 / FPS; // 縮小スピード
	private static final int SHRINK_SIZE = 100; // 縮小が始まるサイズ
	private int shrinkCount = 0;

	private MediaTracker tracker;
	private BufferedImage planktonSkin;
	private BufferedImage virusSkin;

	private Point mouse = new Point();
	private double Vector2[] = new double[2]; // 正規化したカーソル位置

	private MyClient mc;

	private Dimension dr; // 画面サイズ

	private boolean init = false;

	private Random random = new Random();

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

					myUpdate();
					otherUpdate();

					repaint();
					sleep(FPS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Point centerPoint = new Point();
	Point prevCenter = new Point();

	private void myUpdate() {
		centerPoint.x = 0;
		centerPoint.y = 0;

		int count = 0;
		shrinkCount++;

		PlayerData player = mc.getPlayer(mc.getMyID());

		if (player == null) {
			return;
		}

		for (EatableObj c : player.planariaData.values()) {

			Planaria p = (Planaria) c;
			normalize(mouse.x - prevCenter.x - p.current.x, mouse.y - prevCenter.y - p.current.y);

			p.setNext(p.getNext().x + (int) (Vector2[0] * p.getSpeed()),
					p.getNext().y + (int) (Vector2[1] * p.getSpeed()));
			posUpdate(p, 0.25f);

			centerPoint.x += p.current.x;
			centerPoint.y += p.current.y;

			mc.search(p);
			count++;

			int shrinkRate = p.size / SHRINK_SIZE;
			if (shrinkCount >= SHRINK && shrinkRate > 0) {
				// 経過時間とサイズが一定以上の時縮小
				p.setEatSize(p.size - shrinkRate);
			}
		}

		if (shrinkCount >= SHRINK) {
			shrinkCount = 0;
		}

		if (count > 0) {
			centerPoint.x /= count;
			centerPoint.y /= count;

			centerPoint.x -= dr.width / 2;
			centerPoint.y -= dr.height / 2;
			centerPoint.x *= -1;
			centerPoint.y *= -1;
			field.setLocation(centerPoint);

			prevCenter.x = centerPoint.x;
			prevCenter.y = centerPoint.y;
		}
	}

	private void otherUpdate() {

		for (PlayerData player : mc.playerData.values()) {
			if (player.getID() == 0) {
				continue;
			}

			for (EatableObj p : player.planariaData.values()) {
				if (player.getID() != mc.getMyID()) {
					posUpdate((Planaria) p, 0.8f);
				}
				update((Planaria) p);
			}
		}
	}

	public Drow() {
		this(new MyClient());
	}

	public void Login() {
		Point spawnPoint = mc.searchSpawnPoint();

		create(mc.getPlayer(mc.getMyID()).getSkin(), spawnPoint.x, spawnPoint.y, mc.defualtSize);
		DrowThread dt = new DrowThread();
		dt.start();
	}

	/**
	 * Create the frame.
	 */
	public Drow(MyClient mc) {
		this.mc = mc;

		tracker = new MediaTracker(this);
		importSkins();

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
		setTitlePane();

		field = new FieldPane(MyClient.fieldSize);
		contentPane.add(field);
		contentPane.setLayer(field, JLayeredPane.DEFAULT_LAYER);
		repaint();
		init = true;
	}

	public void changeFieldSize(int fieldSize) {
		field.setSize(fieldSize, fieldSize);
		repaint();
	}

	private void importSkins() {
		planktonSkin = LoadManager.getBuffImg("res/plankton.png");
		tracker.addImage(planktonSkin, 0);
		virusSkin = LoadManager.getBuffImg("res/virus.png");
		tracker.addImage(virusSkin, 1);
		SKINS.init();

		try {
			tracker.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private int currentSize;
	private int size;

	private void update(Planaria planaria) {
		currentSize = planaria.getSize().width;
		size = planaria.size;
		if (currentSize != size) {
			size = lerp(currentSize, size, 0.6f);
		}

		planaria.setBounds(planaria.current.x, planaria.current.y, size);
	}

	private void posUpdate(Planaria planaria, float t) {
		planaria.getNext().x = (planaria.getNext().x > MyClient.fieldSize) ? MyClient.fieldSize : planaria.getNext().x;
		planaria.getNext().y = (planaria.getNext().y > MyClient.fieldSize) ? MyClient.fieldSize : planaria.getNext().y;

		planaria.current.x = lerp(planaria.current.x, planaria.getNext().x, t);
		planaria.current.y = lerp(planaria.current.y, planaria.getNext().y, t);
	}

	public Planaria create(int skin, int x, int y, int size) {
		return create(skin, x, y, size, mc.getMyID(), -1);
	}

	public Planaria create(int skin, int x, int y, int size, int playerID, int planariaID) {
		Planaria planaria = new Planaria(SKINS.getSkin(skin).getBuffimg(), skin, x, y, size, planariaID);

		mc.getPlayer(playerID).planariaData.put(planaria.getID(), planaria);
		field.add(planaria);
		field.setLayer(planaria, JLayeredPane.PALETTE_LAYER);

		return planaria;
	}

	public Plankton popPlankton(int x, int y, int id) {
		Plankton plankton = new Plankton(planktonSkin, x, y, mc.planktonSize, id);

		field.add(plankton);

		return plankton;
	}

	public Plankton popVirus(int x, int y, int id) {
		Plankton virus = new Plankton(virusSkin, x, y, VIRUS, id, true);

		field.add(virus);
		field.setLayer(virus, JLayeredPane.MODAL_LAYER);

		return virus;
	}

	public void delete(EatableObj p) {
		if (p == null) {
			return;
		}
		field.remove(p);
	}

	public void fieldReset() {
		field.removeAll();
		repaint();
	}

	private void spilit() {
		boolean se = false;
		int count = mc.getPlayer(mc.getMyID()).getSize();
		for (EatableObj c : mc.getPlayer(mc.getMyID()).planariaData.values()) {

			Planaria planaria = (Planaria) c;
			if (planaria.size < mc.defualtSize * 2) {
				continue;
			}

			se = true;

			planaria.setEatSize(planaria.size / 2);
			Planaria child = create(planaria.getSkin(), planaria.current.x, planaria.current.y, planaria.size);
			child.setNext(planaria.current.x + (int) (Vector2[0] * 300), planaria.current.y + (int) (Vector2[1] * 300));

			if (--count <= 0) {
				break;
			}
		}

		if (se) {
			AUDIO.PON.play();
		}
	}

	public void virusSpilit(Planaria planaria) {
		AUDIO.PON.play();

		int count = planaria.size / mc.defualtSize;
		count = (count > 5) ? 5 : count;

		int size = planaria.size / count;

		planaria.setEatSize(size);
		for (int i = 1; i < count; i++) {
			Planaria child = create(planaria.getSkin(), planaria.current.x, planaria.current.y, size);
			child.setNext(planaria.current.x + (random.nextInt(11) - 6) * 60,
					planaria.current.y + (random.nextInt(11) - 6) * 60);
		}
	}

	// 正規化
	private void normalize(int x, int y) {
		double mag = Math.hypot(x, y);

		double dist = (mag > 100) ? 1 : mag / 100;

		Vector2[0] = x * dist / mag;
		Vector2[1] = y * dist / mag;
	}

	// tでfromとtoの間を補間
	private int lerp(int from, int to, float t) {
		boolean positive = from < to;

		int value = (int) Math.ceil(Math.abs(to - from) * t);

		value *= (positive) ? 1 : -1;

		return from + value;
	}

	public void setTitleError(String err) {
		title.setErrorMsg(err);
	}

	public void setTitlePane() {
		if (title == null) {
			title = new TitlePanel(mc, dr.width, dr.height);
		}

		title.setNewSize(dr);
		contentPane.add(title);
		contentPane.setLayer(title, JLayeredPane.POPUP_LAYER);
		title.focusIpText();
	}

	public void hideTilePane() {
		try {
			requestFocus();
			contentPane.remove(title);
			title = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void setGameOver() {
		if (gameOver == null) {
			gameOver = new GameOverPanel(dr.width, dr.height, this);
		}

		gameOver.setScoreText(mc.score);
		gameOver.setSize(dr);
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
			gameOver = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
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

	// 画面サイズ変更イベント
	@Override
	public void componentResized(ComponentEvent e) {
		dr = contentPane.getSize();

		if (!init) {
			// contentPaneがレンダリングされ、サイズが取得できたタイミングで初期化
			initialize();
		} else {
			if (gameOver != null) {
				gameOver.setSize(dr);
			}
			if (title != null) {
				title.setNewSize(dr);
			}
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			spilit();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
