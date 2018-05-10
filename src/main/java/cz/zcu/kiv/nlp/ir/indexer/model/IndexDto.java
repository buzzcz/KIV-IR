package cz.zcu.kiv.nlp.ir.indexer.model;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Jaroslav Klaus
 */
@Data
public class IndexDto {

	String term;

	Set<Document> docs;

	Integer allDocsCount;

	Map<String, Double> tfs;

	public IndexDto(String term, Document document, String[] tokens, Integer allDocsCount) {
		this.term = term;
		this.allDocsCount = allDocsCount;
		docs = new TreeSet<>(Comparator.comparing(Document::getId));
		docs.add(document);
		tfs = new HashMap<>();
		tfs.put(document.getId(), calculateTf(term, tokens));
	}

	private static double calculateTf(String term, String[] tokens) {
		double tf = 0;
		for (String s : tokens) {
			if (s.equals(term)) {
				tf++;
			}
		}
		if (tf > 0) {
			tf = 1 + Math.log10(tf);
		}
		return tf;
	}

	public double getIdf() {
		return Math.log10(allDocsCount / (double) docs.size());
	}

	public void addDoc(Document document, String[] tokens) {
		docs.add(document);
		tfs.put(document.getId(), calculateTf(term, tokens));
	}

}
