package cz.zcu.kiv.nlp.ir.indexer.model;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import lombok.Data;

import java.util.Comparator;
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

	public IndexDto(String term, Document document, Integer allDocsCount) {
		this.term = term;
		this.allDocsCount = allDocsCount;
		docs = new TreeSet<>(Comparator.comparing(Document::getId));
		docs.add(document);
	}

	public double getIdf() {
		return Math.log(allDocsCount / (double) docs.size());
	}

	public void addDoc(Document document) {
		docs.add(document);
	}

}
