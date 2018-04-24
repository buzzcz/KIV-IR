package cz.zcu.kiv.nlp.ir.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tigi
 */
public class IOUtils {

	private static final String UTF_8 = "UTF-8";

	private static Logger log = Logger.getLogger(IOUtils.class);

	private IOUtils() {

	}

	/**
	 * Read lines from the stream; lines are trimmed and empty lines are ignored.
	 *
	 * @param inputStream stream
	 * @return list of lines
	 */
	public static List<String> readLines(InputStream inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Cannot locate stream");
		}
		try {
			List<String> result = new ArrayList<>();

			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
			String line;

			while ((line = br.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					result.add(line.trim());
				}
			}

			inputStream.close();

			return result;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Read lines from the stream; lines are trimmed and empty lines are ignored.
	 *
	 * @param inputStream stream
	 * @return text
	 */
	public static String readFile(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		if (inputStream == null) {
			throw new IllegalArgumentException("Cannot locate stream");
		}
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			inputStream.close();

			return sb.toString().trim();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Saves lines from the list into given file; each entry is saved as a new line.
	 *
	 * @param file file to save
	 * @param list lines of text to save
	 */
	public static void saveFile(File file, Collection<String> list) {
		try (final PrintStream printStream = new PrintStream(new FileOutputStream(file), true, UTF_8)) {
			for (String text : list) {
				printStream.println(text);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * Saves lines from the list into given file; each entry is saved as a new line.
	 *
	 * @param file file to save
	 * @param text text to save
	 */
	public static void saveFile(File file, String text) {
		List<String> texts = new LinkedList<>();
		texts.add(text);
		saveFile(file, texts);
	}
}
