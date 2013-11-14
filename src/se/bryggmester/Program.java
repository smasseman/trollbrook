package se.bryggmester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import se.bryggmester.instruction.Instruction;

/**
 * @author jorgen.smas@entercash.com
 */
public class Program {

	private static class KeyAndValue {

		String key;
		String value;

		private KeyAndValue(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

	}

	public static final Comparator<? super Program> NAME_COMPARATOR = new Comparator<Program>() {

		@Override
		public int compare(Program o1, Program o2) {
			return o1.name.compareTo(o2.name);
		}
	};

	private List<Instruction> instructions = new ArrayList<>();
	private String name;
	private Long id;

	public List<Instruction> getInstructions() {
		return instructions;
	}

	public void setInstructions(List<Instruction> instructions) {
		this.instructions = instructions;
	}

	public static Program parse(File f) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		Program p = new Program();
		parseProperties(p, reader);
		parseInstructions(p, reader);
		reader.close();
		return p;
	}

	private static void parseProperties(Program p, BufferedReader reader)
			throws IOException {
		String line;
		String name = null;
		Long id = null;
		while ((line = reader.readLine()) != null) {
			line = trim(line);
			if (line == null) {
				// Ignore
			} else if ("-".equals(line)) {
				if (name == null)
					throw new IllegalArgumentException(
							"Missing name in program properties.");
				p.setName(name);
				if (id == null)
					throw new IllegalArgumentException(
							"Missing id in program properties.");
				p.setId(id);
				return;
			} else {
				KeyAndValue kv = createKeyAndValue(line);
				if (kv.key.equals("name")) {
					name = kv.value;
				} else if (kv.key.equals("id")) {
					try {
						id = new Long(kv.value);
					} catch (Exception e) {
						throw new IllegalArgumentException("Invalid id ("
								+ kv.value + ") in program properties. "
								+ e.getMessage());
					}
				}
			}
		}
	}

	private static KeyAndValue createKeyAndValue(String line) {
		int index = line.indexOf('=');
		if (index < 0)
			throw new IllegalArgumentException("Line " + line
					+ " does not contain a = char.");
		return new KeyAndValue(line.substring(0, index),
				line.substring(index + 1));
	}

	private static void parseInstructions(Program p, BufferedReader reader)
			throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			line = trim(line);
			if (line != null) {
				p.getInstructions().add(Instruction.parse(line));
			}
		}
	}

	private static String trim(String line) {
		line = line.trim();
		if (line.length() == 0) {
			// Ignore empy lines.
			return null;
		} else if (line.startsWith("#")) {
			// Ignore comments
			return null;
		} else {
			return line;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Program other = (Program) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[id=" + id + ", name=" + name + "]";
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String export() {
		StringBuilder s = new StringBuilder();
		writeKeyAndValue(s, "id", id.toString());
		writeKeyAndValue(s, "name", name);
		s.append("-\n");
		for (Instruction i : instructions) {
			s.append(i.getType().name() + " " + i.getType().export(i)).append(
					"\n");
		}
		return s.toString();
	}

	private void writeKeyAndValue(StringBuilder s, String name, String value) {
		s.append(name).append("=").append(value).append("\n");
	}

}
