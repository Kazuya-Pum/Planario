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
	BGM("res/garden.wav"), EAT_1("res/se1.wav"), EAT_2("res/se2.wav"), END("res/se3.wav");

	private Clip clip;

	private AUDIO(String path) {
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(LoadManager.getUrl(path))) {
			AudioFormat af = ais.getFormat();
			DataLine.Info dataLine = new DataLine.Info(Clip.class, af);
			clip = (Clip) AudioSystem.getLine(dataLine);

			clip.open(ais);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (clip.isRunning()) {
			clip.stop();
		}
		clip.setFramePosition(0);
		clip.start();
	}

	public void loop() {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public void stop() {
		clip.stop();
	}

	public void restart() {
		clip.flush();
		clip.setFramePosition(0);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	static void init() {
		values();
	}
}
