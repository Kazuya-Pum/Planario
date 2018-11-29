package planario;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Drow extends JFrame implements MouseListener, MouseMotionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel panel;
	private JPanel BackGound;
	public java.util.List<Planaria> planariaList = new ArrayList<Planaria>();
	MediaTracker tracker;
	public ImageIcon[] skins = new ImageIcon[2];
	public int Vector2[] = new int[2];

	public int fps = 20;

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
			while (true) {
				try {
					tracker.waitForAll();

					for (int i = planariaList.size() - 1; i >= 0; i--) {
						Planaria p = planariaList.get(i);
						p.setData(p.posX + Vector2[0] * p.speed, p.posY + Vector2[1] * p.speed, -1);
						Update(p);
					}

					Dimension dr = contentPane.getSize();
					Point centerPoint = planariaList.get(0).getLocation();
					centerPoint.x -= dr.width / 2;
					centerPoint.y -= dr.height / 2;
					centerPoint.x *= -1;
					centerPoint.y *= -1;
					panel.setLocation(centerPoint);
					BackGound.setLocation(centerPoint);

					repaint();
					sleep(fps);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
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
		skins[0] = new ImageIcon("mizuumi.png");

		panel = new JPanel();
		panel.setBounds(0, 0, 2000, 2000);
		panel.setOpaque(false);
		contentPane.add(panel);
		panel.setLayout(null);

		skins[1] = new ImageIcon("planaria.png");
		Create(1, 800, 500, 100);

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

	private Planaria Create(int skin, int x, int y, int size) {
		Planaria planaria = new Planaria(ResizeIcon(skin, size), skin, x, y, size);
		planaria.setBounds(x, y, size, size);

		planariaList.add(planaria);
		panel.add(planaria);

		return planaria;
	}

	private void normalize(int[] vector) {
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
		// TODO contentPaneの取得をrunに統一する
		Dimension dr = contentPane.getSize();
		Vector2[0] = e.getPoint().x - dr.width / 2;
		Vector2[1] = e.getPoint().y - dr.height / 2;

		normalize(Vector2);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		Planaria[] tmp = new Planaria[planariaList.size()];
		planariaList.toArray(tmp);

		for (Planaria planaria : tmp) {
			Planaria child = Create(planaria.skin, planaria.posX, planaria.posY, planaria.size / 2);
			child.setData(planaria.posX + Vector2[0] * 100, planaria.posY + Vector2[1] * 100, -1);
			planaria.setData(-1, -1, planaria.size / 2);
		}
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
}