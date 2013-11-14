package se.bryggmester.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author jorgen.smas@entercash.com
 */
public class AudioPlayer {

	public void play(List<File> files) throws UnsupportedAudioFileException,
			IOException, LineUnavailableException, InterruptedException {
		for (File file : files) {
			AudioInputStream in = AudioSystem.getAudioInputStream(file);
			Line.Info linfo = new Line.Info(Clip.class);
			Line line = AudioSystem.getLine(linfo);
			Clip clip = (Clip) line;
			// Clip clip = AudioSystem.getClip();
			clip.open(in);
			clip.start();
			final AtomicBoolean done = new AtomicBoolean(false);
			clip.addLineListener(new LineListener() {

				@Override
				public void update(LineEvent event) {
					if (event.getType() == Type.STOP) {
						synchronized (done) {
							done.set(true);
							done.notify();
						}
					}
				}
			});
			synchronized (done) {
				while (!done.get())
					done.wait();
			}
			clip.stop();
		}
	}

	public static void main(String[] args) throws Exception {
		List<File> files = new ArrayList<>(args.length);
		for (String s : args) {
			files.add(new File(s));
		}
		new AudioPlayer().play(files);
	}
}
