package cz.zcu.kiv.nlp.ir.indexer;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tigi
 */

public class Index implements Indexer, Searcher {

	private static Logger log = Logger.getLogger(Index.class);

	private Analyzer analyzer;

	private Directory indexDir;

	public void index(List<Document> documents) {
		try {
			analyzer = new CzechAnalyzer(new CharArraySet(Files.readAllLines(new File("stopwords.txt").toPath()),
					true));
			indexDir = FSDirectory.open(new File("index").toPath());

			if (!DirectoryReader.indexExists(indexDir)) {
				IndexWriterConfig config = new IndexWriterConfig(analyzer);
				IndexWriter w = new IndexWriter(indexDir, config);
				addDocs(w, documents);

				w.close();
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void addDocs(IndexWriter w, List<Document> documents) throws IOException {
		List<org.apache.lucene.document.Document> docs = new LinkedList<>();
		for (Document d : documents) {
			org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
			luceneDocument.add(new StringField("id", d.getId(), Field.Store.YES));
			luceneDocument.add(new TextField("date", d.getDate().toString(), Field.Store.YES));
			luceneDocument.add(new TextField("title", d.getTitle(), Field.Store.YES));
			luceneDocument.add(new TextField("text", d.getText(), Field.Store.YES));

			docs.add(luceneDocument);
		}

		w.addDocuments(docs);
	}

	public List<Result> search(String query) {
		List<Result> results = new LinkedList<>();

		try (IndexReader reader = DirectoryReader.open(indexDir)) {
			Query q = new QueryParser("text", analyzer).parse(query);
			int hitsPerPage = 10;
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;

			for (ScoreDoc hit : hits) {
				ResultImpl result = new ResultImpl();
				int docId = hit.doc;
				org.apache.lucene.document.Document d = searcher.doc(docId);
				result.setDocumentID(d.get("id"));
				result.setScore(hit.score);
//				result.setRank(); FIXME

				results.add(result);
			}
		} catch (Exception e) {
			log.error(e);
		}

		return results;
	}
}
