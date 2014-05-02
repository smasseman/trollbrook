package se.trollbrook.bryggmester;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletConfigAware;

import se.trollbrook.util.NetworkUtil;

/**
 * @author jorgen.smas@entercash.com
 */
@Service
public class RegisterIP implements ServletConfigAware {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private Thread thread;
	private String url = "http://trollbrook.se/brewerurl/save.php";

	@PostConstruct
	public void start() {
		logger.info("Start.");
		this.thread = new Thread() {

			@Override
			public void run() {
				try {
					while (!interrupted()) {
						String ip = getIP();
						registerIP(ip);
					}
				} catch (InterruptedException e) {
					logger.debug("Thread interrupted before register done.");
					return;
				} finally {
					logger.debug("Register thread is down.");
				}
			}

			private void registerIP(String ip) throws InterruptedException {
				while (true) {
					HttpURLConnection con = null;
					try {
						String url = getUrl(ip);
						String mac = getMac(ip);
						URL saveurl = new URL(RegisterIP.this.url + "?url=" + URLEncoder.encode(url, "utf-8") + "&mac="
								+ mac);
						con = (HttpURLConnection) saveurl.openConnection();
						con.setDoInput(true);
						con.setDoOutput(true);
						con.getOutputStream().close();

						String result = readString(con.getInputStream());
						con.getInputStream().close();
						logger.debug("Servern returned: \n" + result);
						logger.info("URL " + url + " is saved on remote server.");
						return;
					} catch (Exception e) {
						logger.debug("Failed to register ip: " + e);
					}
					Thread.sleep(10 * 1000);
				}
			}

			private String getUrl(String ip) {
				return "http://" + ip;
			}

			private String getMac(String ip) throws IOException {
				NetworkInterface network = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
				byte[] mac = network.getHardwareAddress();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
				}
				return sb.toString();
			}

			public String readString(InputStream inputStream) throws IOException {
				ByteArrayOutputStream into = new ByteArrayOutputStream();
				byte[] buf = new byte[4096];
				for (int n; 0 < (n = inputStream.read(buf));) {
					into.write(buf, 0, n);
				}
				into.close();
				return new String(into.toByteArray(), "UTF-8");
			}

			private String getIP() throws InterruptedException {
				while (true) {
					try {
						String ip = NetworkUtil.getIp();
						if (ip != null)
							return ip;
					} catch (SocketException e) {
					}
					Thread.sleep(5 * 1000);
				}
			}
		};
		this.thread.start();
	}

	@PreDestroy
	public void stop() {
		this.thread.interrupt();
	}

	@Override
	public void setServletConfig(ServletConfig conf) {
		ServletContext ctx = conf.getServletContext();
		logger.debug("Servet info: " + ctx.getServerInfo());
		logger.debug("context path=" + ctx.getContextPath());
	}
}
