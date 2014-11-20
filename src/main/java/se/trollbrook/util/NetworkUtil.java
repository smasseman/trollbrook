package se.trollbrook.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author jorgen.smas@entercash.com
 */
public class NetworkUtil {

	public static String getIp() throws SocketException {
		Enumeration<NetworkInterface> enumerations = NetworkInterface.getNetworkInterfaces();
		List<String> result = new ArrayList<String>();
		while (enumerations.hasMoreElements()) {
			NetworkInterface nic = enumerations.nextElement();
			Enumeration<InetAddress> ipadrs = nic.getInetAddresses();
			while (ipadrs.hasMoreElements()) {
				InetAddress ipadr = ipadrs.nextElement();
				if (ipadr instanceof Inet4Address) {
					if (!ipadr.isLoopbackAddress() && !ipadr.getHostAddress().startsWith("169.254.")) {
						result.add(ipadr.getHostAddress());
					}
				}
			}
		}
		if (result.isEmpty())
			return null;
		Collections.sort(result);
		return result.get(0);

	}
}
