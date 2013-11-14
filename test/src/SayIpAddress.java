import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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

public class SayIpAddress {

	public static void main(String[] args) throws Exception {
		String ip = getIp();
		for (char c : ip.toCharArray()) {
			String s = String.valueOf(c);
			if (c == '.')
				s = "plus";
			play("audio/" + s + ".wav");
		}
	}

	private static String getIp() throws SocketException {
		Enumeration<NetworkInterface> enumerations = NetworkInterface
				.getNetworkInterfaces();
		List<String> result = new ArrayList<String>();
		while (enumerations.hasMoreElements()) {
			NetworkInterface nic = enumerations.nextElement();
			Enumeration<InetAddress> ipadrs = nic.getInetAddresses();
			while (ipadrs.hasMoreElements()) {
				InetAddress ipadr = ipadrs.nextElement();
				if (ipadr instanceof Inet4Address) {
					if (!ipadr.isLoopbackAddress())
						result.add(ipadr.getHostAddress());
				}
			}
		}
		Collections.sort(result);
		return result.get(0);
	}

	private static void play(String sound)
			throws UnsupportedAudioFileException, IOException,
			LineUnavailableException, InterruptedException {
		File file = new File(sound);
		if (!file.canRead())
			throw new IOException("Can not read " + file);
		AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
		Line.Info linfo = new Line.Info(Clip.class);
		Line line = AudioSystem.getLine(linfo);
		Clip clip = (Clip) line;

		clip.open(audioIn);
		final AtomicBoolean done = new AtomicBoolean(false);
		clip.addLineListener(new LineListener() {

			@Override
			public void update(LineEvent event) {
				Type e = event.getType();
				if (e == Type.STOP) {
					synchronized (done) {
						done.set(true);
						done.notifyAll();
					}
				}
			}
		});
		clip.start();
		synchronized (done) {
			while (!done.get())
				done.wait();
		}
		clip.close();
		audioIn.close();
	}
}
