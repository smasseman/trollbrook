package se.bryggmester;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * @author jorgen.smas@entercash.com
 */
@Repository
public class Database {

	private Logger logger = LoggerFactory.getLogger(getClass());
	@Resource(name = "programdirectory")
	private File directory;
	private FileFilter PROGRAM_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(".brw");
		}
	};

	public List<Program> listPrograms() throws IOException {
		File[] files = directory.listFiles(PROGRAM_FILTER);
		if (files == null)
			throw new RuntimeException("Can not list files in " + directory);

		List<Program> result = new ArrayList<>(files.length);
		for (File f : files) {
			addProgram(result, f);
		}
		Collections.sort(result, Program.NAME_COMPARATOR);
		return result;
	}

	private void addProgram(List<Program> result, File f) throws IOException {
		try {
			Program p = Program.parse(f);
			result.add(p);
			logger.debug("Program " + p + " was found in " + f);
		} catch (RuntimeException | IOException t) {
			logger.error("Failed to parse " + f.getAbsolutePath(), t);
			throw t;
		}
	}

	public Program getProgramById(Long programId) throws IOException {
		List<Program> list = listPrograms();
		for (Program p : list) {
			if (p.getId().equals(programId))
				return p;
		}
		return null;
	}

	public void add(Program p) throws IOException {
		Long id = System.currentTimeMillis();
		p.setId(id);
		update(p);
	}

	public void update(Program p) throws IOException {
		String programExport = p.export();
		File file = getFilename(p);
		FileWriter writer = new FileWriter(file);
		writer.write(programExport);
		writer.close();
		logger.debug("Program " + p.getId() + " is saved in " + file);
	}

	public void delete(Program p) {
		File file = getFilename(p);
		file.delete();
	}

	private File getFilename(Program p) {
		return new File(directory, p.getId() + ".brw");
	}
}
