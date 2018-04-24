package cz.zcu.kiv.nlp.ir.utils;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Topic;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tigi
 */
public class SerializedDataHelper {

	public static final java.text.DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH_mm_SS");

	static Logger log = Logger.getLogger(SerializedDataHelper.class);

	private SerializedDataHelper() {
	}

	public static List<Document> loadDocument(File serializedFile) {
		final Object object;
		try (final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedFile))) {
			object = objectInputStream.readObject();
			List map = (List) object;
			if (!map.isEmpty() && map.get(0) instanceof Document) {
				return (List<Document>) object;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return new LinkedList<>();
	}


	public static void saveDocument(File outputFile, List<Document> data) {
		saveData(outputFile, data);
	}

	public static List<Topic> loadTopic(File serializedFile) {
		final Object object;
		try (final ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(serializedFile))) {
			object = objectInputStream.readObject();
			List map = (List) object;
			if (!map.isEmpty() && map.get(0) instanceof Topic) {
				return (List<Topic>) object;
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		return new LinkedList<>();
	}

	public static void saveTopic(File outputFile, List<Topic> data) {
		saveData(outputFile, data);
	}

	private static void saveData(File outputFile, List<?> data) {
		try (final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(outputFile))) {
			objectOutputStream.writeObject(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("Data saved to " + outputFile.getPath());
	}
}
