package planario;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Drow extends JFrame implements MouseListener, MouseMotionListener, ComponentListener, KeyListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private JPanel backGound;
	MediaTracker tracker;
	public ImageIcon[] skins = new ImageIcon[3];
	public float Vector2[] = new float[2];

	public int fps = 30;

	MyClient mc;

	Dimension dr;

	boolean loginFlag = false;

	Random random = new Random();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Drow frame = new Drow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public class DrowThread extends Thread {
		public void run() {
			try {
				System.out.println("nowLoading");
				while (!loginFlag) {
					sleep(10);
				}
				System.out.println("ok");
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (true) {
				try {
					tracker.waitForAll();

					MyUpdate();
					OtherUpdate();

					if (random.nextInt(10) == 0) {
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

	private void MyUpdate() {
		Point centerPoint = new Point();

		int count = mc.GetPlayer(mc.myNumberInt).planariaData.size();
		Planaria[] tmp = new Planaria[count];
		mc.GetPlayer(mc.myNumberInt).planariaData.values().toArray(tmp);
		for (Planaria p : tmp) {
			p.setData(p.posX + (int) (Vector2[0] * p.speed), p.posY + (int) (Vector2[1] * p.speed), -1);
			Update(p);
			centerPoint.x += p.posX;
			centerPoint.y += p.posY;

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
		} catch (ArithmeticException e) {

		}
	}

	private void OtherUpdate() {
		PlayerData[] tmpPlayer = new PlayerData[mc.playerData.size()];
		mc.playerData.values().toArray(tmpPlayer);

		for (PlayerData player : tmpPlayer) {
			if (player.playerID == mc.myNumberInt || player.playerID == 0) {
				continue;
			}

			Planaria[] tmpPlanaria = new Planaria[player.planariaData.size()];
			player.planariaData.values().toArray(tmpPlanaria);
			for (Planaria p : tmpPlanaria) {
				Update(p);
			}
		}
	}

	public Drow() {
		this(new MyClient());
	}

	public void Login() {
		Create(1, 800, 500, 100);
		loginFlag = true;
	}

	/**
	 * Create the frame.
	 */
	public Drow(MyClient mc) {
		this.mc = mc;

		tracker = new MediaTracker(this);
		ImportSkins();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1024, 640);
		setTitle("Planar.io");

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.addMouseListener(this);
		contentPane.addMouseMotionListener(this);
		contentPane.addComponentListener(this);
		addKeyListener(this);
		dr = contentPane.getSize();

		panel = new JPanel();
		panel.setBounds(0, 0, mc.fieldSize, mc.fieldSize);
		panel.setOpaque(false);
		contentPane.add(panel);
		panel.setLayout(null);

		backGound = new JPanel();
		backGound.setBounds(0, 0, mc.fieldSize, mc.fieldSize);
		contentPane.add(backGound);

		JLabel backGoundLabel = new JLabel(ResizeIcon(0, mc.fieldSize));
		backGound.add(backGoundLabel);

		DrowThread dt = new DrowThread();
		dt.start();
	}

	private void ImportSkins() {
		skins[0] = new ImageIcon("mizuumi.png");
		skins[1] = new ImageIcon("planaria.png");
		skins[2] = new ImageIcon("plankton.png");
		skins[2] = ResizeIcon(2, 10);
	}

	Point current;
	Point next;
	int currentSize;

	private void Update(Planaria planaria) {
		current = planaria.getLocation();
		next = new Point();

		planaria.posX = (planaria.posX > mc.fieldSize) ? mc.fieldSize : planaria.posX;
		planaria.posY = (planaria.posY > mc.fieldSize) ? mc.fieldSize : planaria.posY;

		next.x = Lerp(current.x, planaria.posX, 0.25f);
		next.y = Lerp(current.y, planaria.posY, 0.25f);

		currentSize = planaria.getIcon().getIconWidth();
		int size = planaria.size / 3;
		if (currentSize != size) {
			planaria.setIcon(ResizeIcon(planaria.skin, Lerp(currentSize, size, 0.6f)));
		}

		planaria.setBounds(next.x, next.y, size, size);
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
		Planaria planaria = new Planaria(ResizeIcon(skin, size), skin, x, y, size, planariaID);
		planaria.setBounds(x, y, size, size);

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
		Planaria[] tmp = new Planaria[mc.GetPlayer(mc.myNumberInt).planariaData.size()];
		mc.GetPlayer(mc.myNumberInt).planariaData.values().toArray(tmp);

		for (Planaria planaria : tmp) {
			if (planaria.size / 3 < 30) {
				continue;
			}

			Planaria child = Create(planaria.skin, planaria.posX, planaria.posY, planaria.size / 2);
			child.setData(planaria.posX + (int) (Vector2[0] * 100), planaria.posY + (int) (Vector2[1] * 100), -1);
			planaria.setData(-1, -1, planaria.size / 2);
		}
	}

	private void normalize(float[] vector) {
		double mag = Math.hypot(vector[0], vector[1]);

		vector[0] *= 1 / mag;
		vector[1] *= 1 / mag;
	}

	private int Lerp(int from, int to, float t) {
		boolean positive = from < to;

		int value = (int) Math.ceil(Math.abs(to - from) * t);

		value *= (positive) ? 1 : -1;

		return from + value;
	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Vector2[0] = e.getPoint().x - dr.width / 2;
		Vector2[1] = e.getPoint().y - dr.height / 2;

		normalize(Vector2);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void componentResized(ComponentEvent e) {
		dr = contentPane.getSize();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			Spilit();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
