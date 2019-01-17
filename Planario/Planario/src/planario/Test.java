package planario;

import java.awt.EventQueue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;

public class Test extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JFrame frame;
	private JLayeredPane contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Test window = new Test();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Test() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		setBounds(100, 100, 760, 483);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JLayeredPane();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel panel = new GameOverPanel(760,483);
		contentPane.add(panel);
		contentPane.setLayer(panel, JLayeredPane.MODAL_LAYER);
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.NORTH);


		JLabel a = new JLabel();
	}

}
