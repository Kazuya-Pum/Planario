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

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Drow extends JFrame implements MouseListener, MouseMotionListener, ComponentListener, KeyListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private JPanel BackGound;
	MediaTracker tracker;
	public ImageIcon[] skins = new ImageIcon[2];
	public float Vector2[] = new float[2];

	public int fps = 20;

	MyClient mc;

	Dimension dr;

	boolean loginFlag = false;

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

			mc.SendMyPlanariaData(p);
		}

		try {
			centerPoint.x /= count;
			centerPoint.y /= count;

			centerPoint.x -= dr.width / 2;
			centerPoint.y -= dr.height / 2;
			centerPoint.x *= -1;
			centerPoint.y *= -1;
			panel.setLocation(centerPoint);
			BackGound.setLocation(centerPoint);
		} catch (ArithmeticException e) {

		}
	}

	private void OtherUpdate() {
		PlayerData[] tmpPlayer = new PlayerData[mc.playerData.size()];
		mc.playerData.values().toArray(tmpPlayer);

		for (PlayerData player : tmpPlayer) {
			if (player.playerID == mc.myNumberInt) {
				continue;
			}

			Planaria[] tmpPlanaria = new Planaria[player.planariaData.size()];
			player.planariaData.values().toArray(tmpPlanaria);
			for (Planaria p : tmpPlanaria) {
				Update(p);
			}
		}
	}

	public Drow(MyClient mc) {
		this();
		this.mc = mc;
	}

	public void Login() {
		Create(1, 800, 500, 100);
		loginFlag = true;
	}

	/**
	 * Create the frame.
	 */
	public Drow() {
		tracker = new MediaTracker(this);

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
		skins[0] = new ImageIcon("mizuumi.png");

		panel = new JPanel();
		panel.setBounds(0, 0, 2000, 2000);
		panel.setOpaque(false);
		contentPane.add(panel);
		panel.setLayout(null);

		skins[1] = new ImageIcon("planaria.png");

		BackGound = new JPanel();
		BackGound.setBounds(0, 0, 2000, 2000);
		contentPane.add(BackGound);

		JLabel backGoundLabel = new JLabel(ResizeIcon(0, 2000));
		BackGound.add(backGoundLabel);

		DrowThread dt = new DrowThread();
		dt.start();
	}

	private void Update(Planaria planaria) {
		Point current = planaria.getLocation();
		Point next = new Point();

		next.x = (int) (current.x + (planaria.posX - current.x) * 0.25);
		next.y = (int) (current.y + (planaria.posY - current.y) * 0.25);

		if (planaria.getIcon().getIconWidth() != planaria.size) {
			planaria.setIcon(ResizeIcon(planaria.skin, planaria.size));
		}

		planaria.setBounds(next.x, next.y, planaria.size, planaria.size);
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

	public void Delete(Planaria p) {
		panel.remove(p);
		System.out.println("Delete : " + p.localId);
		p = null;
	}

	private void Spilit() {
		Planaria[] tmp = new Planaria[mc.GetPlayer(mc.myNumberInt).planariaData.size()];
		mc.GetPlayer(mc.myNumberInt).planariaData.values().toArray(tmp);

		for (Planaria planaria : tmp) {
			Planaria child = Create(planaria.skin, planaria.posX, planaria.posY, planaria.size / 2);
			child.setData(planaria.posX + (int) (Vector2[0] * 100), planaria.posY + (int) (Vector2[1] * 100), -1);
			planaria.setData(-1, -1, planaria.size / 2);
		}
	}

	private void normalize(float[] vector) {
		double mag = Math.hypot(vector[0], vector[1]);

		vector[0] *= 5 / mag;
		vector[1] *= 5 / mag;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ

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
