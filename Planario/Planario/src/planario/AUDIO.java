package planario;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public enum AUDIO {
	BGM("res/garden.wav"), EAT_1("res/se1.wav"), EAT_2("res/se2.wav"), END("res/se3.wav"), PON("res/pon.wav");

	private Clip clip;
	private static boolean activeSE = true;
	private static boolean activeBGM = true;

	private AUDIO(String path) {
		// Clip準備
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(LoadManager.getUrl(path))) {	// ファイル読み込み
			AudioFormat af = ais.getFormat();
			DataLine.Info dataLine = new DataLine.Info(Clip.class, af);
			clip = (Clip) AudioSystem.getLine(dataLine);

			clip.open(ais);	// Clip準備完了
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
			e.printStackTrace();
		}
	}

	// SEのミュート切り替え
	public static boolean toggleSE() {
		activeSE = (activeSE) ? false : true;
		return activeSE;
	}

	// BGMのミュート切り替え
	public static boolean toggleBGM() {
		activeBGM = (activeBGM) ? false : true;

		if (activeBGM) {
			BGM.loop();
		} else {
			BGM.clip.stop();
		}

		return activeBGM;
	}

	public static boolean getActiveSE() {
		return activeSE;
	}

	public static boolean getActiveBGM() {
		return activeBGM;
	}

	// 1回再生
	public void play() {
		if (!activeSE) {
			return;
		}

		if (clip.isRunning()) {
			clip.stop();
		}
		clip.setFramePosition(0);
		clip.start();
	}

	// ループ再生
	public void loop() {
		if (!activeBGM) {
			return;
		}

		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stop() {
		clip.stop();
	}

	public void restart() {
		clip.flush();
		clip.setFramePosition(0);
		loop();
	}

	// 初期化
	public static void init() {
		values();
	}
}
