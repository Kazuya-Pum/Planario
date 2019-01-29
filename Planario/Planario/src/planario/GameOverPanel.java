package planario;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class GameOverPanel extends JLayeredPane implements KeyListener, MouseListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	BufferedImage buffimg;
	Graphics2D bfg;
	JLabel scoreText;
	Drow drow;

	public GameOverPanel(int width, int height, Drow drow) {
		this.drow = drow;

		setPreferredSize(new Dimension(width, height));
		buffimg = LoadManager.getBuffImg("res/GameOver.png");
		bfg = buffimg.createGraphics();

		setOpaque(false);
		addKeyListener(this);
		addMouseListener(this);

		scoreText = new JLabel();
		scoreText.setBounds(10, 10, 300, 30);
		scoreText.setOpaque(false);
		scoreText.setFont(new Font("Agency FB", Font.BOLD, 30));
		add(scoreText);
	}

	public void setScoreText(int score) {
		scoreText.setText("Score : " + score);
	}

	@Override
	public void paintComponent(Graphics myg) {
		myg.drawImage(buffimg, 0, 0, getSize().width, getSize().height, this);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		drow.hideGameOver();
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		drow.hideGameOver();
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

}
