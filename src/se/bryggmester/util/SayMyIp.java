package se.bryggmester.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jorgen.smas@entercash.com
 */
public class SayMyIp {

	@Autowired
	private ServletContext ctx;
	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void sayMyIp() throws UnsupportedAudioFileException, IOException,
			LineUnavailableException, InterruptedException {
		String ip = NetworkUtil.getIp();
		logger.debug("My IP is " + ip);
		List<File> files = new ArrayList<>(ip.length());
		for (char c : ip.toCharArray()) {
			files.add(getAudioFile(c));
		}
		new AudioPlayer().play(files);
		logger.debug("Done playing ip.");
	}

	private File getAudioFile(char c) {
		String name = String.valueOf(c);
		if (c == '.')
			name = "plus";
		return new File(ctx.getRealPath("/audio") + "/" + name + ".wav");
	}

}
