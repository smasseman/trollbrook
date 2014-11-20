package se.trollbrook.bryggmester.web;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * @author jorgen.smas@entercash.com
 */
public class JsonWriter {

	public static void writeJson(HttpServletResponse resp, String jsonString) throws IOException {
		byte[] data = jsonString.getBytes("utf-8");
		resp.setContentType("application/json");
		resp.setCharacterEncoding("utf-8");
		resp.setContentLength(data.length);
		ServletOutputStream out = resp.getOutputStream();
		out.write(data);
	}

}
