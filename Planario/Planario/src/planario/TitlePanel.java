package planario;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class TitlePanel extends JLayeredPane implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public IpText ipStr;
	private MyClient mc;
	private JLabel menu;
	private JLabel errorText;

	public TitlePanel(MyClient mc, int width, int height) {
		this.mc = mc;

		setPreferredSize(new Dimension(width, height));
		setOpaque(false);

		int anqX = 250;
		int anqY = 50;

		menu = new JLabel(LoadManager.getIcon("res/TitleMenu.png"));
		menu.setOpaque(false);
		menu.setBounds(anqX, anqY, 500, 500);
		add(menu);

		JButton playButton = new JButton(LoadManager.getIcon("res/play.png"));
		playButton.addActionListener(this);
		playButton.setContentAreaFilled(false);
		playButton.setBorderPainted(false);
		playButton.setOpaque(false);
		playButton.setPressedIcon(LoadManager.getIcon("res/pressPlay.png"));
		playButton.setBounds(100, 300, 300, 140);
		menu.add(playButton);
		setLayer(playButton, JLayeredPane.PALETTE_LAYER);

		ipStr = new IpText();
		ipStr.setBounds(100, 250, 300, 50);
		menu.add(ipStr);
		ipStr.setOpaque(false);
		setLayer(ipStr, JLayeredPane.PALETTE_LAYER);
		ipStr.setPlaceholder("localhost");
		ipStr.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 30));
		ipStr.addActionListener(this);

		errorText = new JLabel("エラーメッセージ");
		errorText.setBounds(100, 200, 300, 50);
		errorText.setOpaque(false);
		errorText.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 20));
		errorText.setForeground(new Color(255, 0, 0));
		errorText.setHorizontalAlignment(JLabel.CENTER);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String ip = ipStr.getText().trim();
		ipStr.setText(ip);
		mc.Access(ip);
	}

	public void setErrorMsg(String msg) {
		errorText.setText(msg);
		menu.add(errorText);
		setLayer(errorText, JLayeredPane.PALETTE_LAYER);
	}

	public void hideErrorMsg() {
		try {
			remove(errorText);
		} catch (Exception e) {

		}
	}

	public void setNewSize(Dimension dr) {
		setSize(dr);
		menu.setLocation(dr.width / 2 - menu.getSize().width / 2, dr.height / 2 - menu.getSize().height / 2);
	}

	public class IpText extends JTextField {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private String placeholder;

		public IpText() {
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
				return;
			}

			final Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getDisabledTextColor());
			g2.drawString(placeholder, getInsets().left, g2.getFontMetrics().getMaxAscent() + getInsets().top);
		}

		public void setPlaceholder(String s) {
			placeholder = s;
		}

	}

}
