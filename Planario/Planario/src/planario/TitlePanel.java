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
import javax.swing.text.Document;

public class TitlePanel extends JLayeredPane implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
//	private BufferedImage buffimg;
//	private Graphics bfg;

	public IpText ipStr;
	private MyClient mc;

	private JLabel errorText;

	public TitlePanel(MyClient mc, int width, int height) {
		this.mc = mc;

		setPreferredSize(new Dimension(width, height));
		setOpaque(false);

		int anqX = 250;
		int anqY = 50;

		JLabel menu = new JLabel(LoadManager.getIcon("res/TitleMenu.png"));
		menu.setOpaque(false);
		menu.setBounds(anqX, anqY, 500, 500);


		JButton playButton = new JButton(LoadManager.getIcon("res/play.png"));
		playButton.addActionListener(this);
		playButton.setContentAreaFilled(false);
		playButton.setBorderPainted(false);
		playButton.setOpaque(false);
		playButton.setPressedIcon(LoadManager.getIcon("res/pressPlay.png"));
		playButton.setBounds(anqX + 100, anqY + 300, 300, 140);
		add(playButton);
		setLayer(playButton, JLayeredPane.PALETTE_LAYER);

		ipStr = new IpText();
		ipStr.setBounds(anqX + 100, anqY + 250, 300, 50);
		add(ipStr);
		ipStr.setOpaque(false);
		setLayer(ipStr, JLayeredPane.PALETTE_LAYER);
		ipStr.setPlaceholder("localhost");
		ipStr.setFont(new Font("ＭＳ ゴシック", Font.PLAIN, 30));
		ipStr.addActionListener(this);

		errorText = new JLabel("エラーメッセージ");
		errorText.setBounds(anqX + 100, anqY + 200, 300, 50);
		errorText.setOpaque(false);
		errorText.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 20));
		errorText.setForeground(new Color(255, 0, 0));
		errorText.setHorizontalAlignment(JLabel.CENTER);

		add(menu);
//		try {
//			buffimg = ImageIO.read(new File("GameOver.png"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		bfg = buffimg.createGraphics();
	}

//	@Override
//	public void paintComponent(Graphics myg) {
//		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
//	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!MyClient.accessFlag) {
			mc.Access(ipStr.getText());
		}
	}

	public void setErrorMsg(String msg) {
		errorText.setText(msg);
		add(errorText);
		setLayer(errorText, JLayeredPane.PALETTE_LAYER);
	}

	public void hideErrorMsg() {
		try {
			remove(errorText);
		} catch (Exception e) {

		}
	}

	public class IpText extends JTextField {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private String placeholder;

		public IpText() {
		}

		public IpText(final Document pDoc, final String pText, final int pColumns) {
			super(pDoc, pText, pColumns);
		}

		public IpText(final int pColumns) {
			super(pColumns);
		}

		public IpText(final String pText) {
			super(pText);
		}

		public IpText(final String pText, final int pColumns) {
			super(pText, pColumns);
		}

		public String getPlaceholder() {
			return placeholder;
		}

		@Override
		protected void paintComponent(final Graphics pG) {
			super.paintComponent(pG);

			if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
				return;
			}

			final Graphics2D g = (Graphics2D) pG;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(getDisabledTextColor());
			g.drawString(placeholder, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
		}

		public void setPlaceholder(final String s) {
			placeholder = s;
		}

	}

}
