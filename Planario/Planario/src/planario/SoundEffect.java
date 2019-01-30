package planario;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundEffect {

	Clip[] clip = new Clip[4];

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
				clip.stop(); // Stop the player if it is still runnings
			}
			clip.setFramePosition(0); // rewind to the beginning
			clip.start();
		}

		public void loop() {
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		}

	}

	public SoundEffect() {

		try {
			try (AudioInputStream ais = AudioSystem.getAudioInputStream(LoadManager.getUrl("res/se1.wav"))) {
				AudioFormat af = ais.getFormat();
				DataLine.Info dataLine = new DataLine.Info(Clip.class, af);
				clip[0] = (Clip) AudioSystem.getLine(dataLine);

				clip[0].open(ais);
			}

//			clip[0] = AudioSystem.getClip();
//			clip[0].open(AudioSystem.getAudioInputStream(LoadManager.getUrl("res/se1.wav")));

			clip[1] = AudioSystem.getClip();
			clip[1].open(AudioSystem.getAudioInputStream(new File("res/se2.wav")));

			clip[2] = AudioSystem.getClip();
			clip[2].open(AudioSystem.getAudioInputStream(new File("res/se3.wav")));

			clip[3] = AudioSystem.getClip();
			clip[3].open(AudioSystem.getAudioInputStream(new File("res/garden.wav")));
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

		clip[3].loop(Clip.LOOP_CONTINUOUSLY);
	}
}