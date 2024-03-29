package cz.zcu.kiv.nlp.ir.indexer;

import cz.zcu.kiv.nlp.ir.indexer.model.IndexDto;
import cz.zcu.kiv.nlp.ir.preprocessing.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.preprocessing.CzechStemmerLight;
import cz.zcu.kiv.nlp.ir.preprocessing.Stemmer;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author tigi
 */
public class Index implements Indexer, Searcher {

	private static Logger log = Logger.getLogger(Index.class);

	/*private Analyzer analyzer;

	private Directory indexDir;*/

	private Map<String, IndexDto> index;

	private final String INDEX_NAME = "index.json";

	private Stemmer stemmer;

	private Analyzer queryAnalyzer = new StandardAnalyzer();

	public void index(List<Document> documents) {
		try {
			/*analyzer = new CzechAnalyzer(new CharArraySet(Files.readAllLines(new File("stopwords.txt").toPath()),
					true));
			indexDir = FSDirectory.open(new File("index").toPath());

			if (!DirectoryReader.indexExists(indexDir)) {
				IndexWriterConfig config = new IndexWriterConfig(analyzer);
				IndexWriter w = new IndexWriter(indexDir, config);
				addDocs(w, documents);

				w.close();
			}*/

			if (stemmer == null) {
				stemmer = new CzechStemmerLight();
			}

//			File f = new File(INDEX_NAME);
//			if (f.exists() && index == null) {
//				log.info("Loading index from file.");
//				index = new Gson().fromJson(new FileReader(f), Map.class);
//			} else if (index == null) {
			index = new HashMap<>();

			int i = 0;
			int size = documents.size();
			for (Document d : documents) {
				if (i % 1000 == 0) {
					log.info("Indexing document " + (++i) + " of " + size + ".");
				} else {
					i++;
				}

				String text = d.getDate().toString() + ' ' + d.getTitle() + ' ' + d.getText();
				String[] tokens = tokenize(text);


				for (String token : tokens) {
					if (index.containsKey(token)) {
						index.get(token).addDoc(d, tokens);
					} else {
						IndexDto indexDto = new IndexDto(token, d, tokens, size);
						index.put(token, indexDto);
					}
				}
			}

			// TODO: This is not working, too big file. Try to split it into docs and dictionary with only ids in
			// list.
//				try (Writer w = new FileWriter(f)) {
//					log.info("Writing index to file.");
//					new Gson().toJson(index, w);
//				}
//			}
		} catch (/*IO*/Exception e) {
			log.error(e);
		}
	}

	private String[] tokenize(String text) {
		List<String> retVal = new LinkedList<>();
		String[] tokens = AdvancedTokenizer.tokenize(text, AdvancedTokenizer.DEFAULT_REGEX);
		for (String token : tokens) {
			String t = stemmer.stem(token);
			retVal.add(AdvancedTokenizer.removeAccents(t));
		}

		return retVal.toArray(new String[0]);
	}

	/*private void addDocs(IndexWriter w, List<Document> documents) throws IOException {
		List<org.apache.lucene.document.Document> docs = new LinkedList<>();
		for (Document d : documents) {
			org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
			luceneDocument.add(new StringField("id", d.getId(), Field.Store.YES));
			luceneDocument.add(new StringField("date", d.getDate().toString(), Field.Store.YES));
			luceneDocument.add(new TextField("title", d.getTitle(), Field.Store.YES));
			luceneDocument.add(new TextField("text", d.getText(), Field.Store.YES));

			docs.add(luceneDocument);
		}

		w.addDocuments(docs);
	}*/

	public List<Result> search(String query) {
		List<Result> results = new LinkedList<>();

		/*try (IndexReader reader = DirectoryReader.open(indexDir)) {
			int hitsPerPage = 10;
			Query q = new QueryParser("text", analyzer).parse(query);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;

			for (ScoreDoc hit : hits) {
				ResultImpl result = new ResultImpl();
				int docId = hit.doc;
				org.apache.lucene.document.Document d = searcher.doc(docId);
				result.setDocumentID(d.get("id"));
				result.setScore(hit.score);
//				result.setRank(); FIXME: Klaus - Set value although it seems it's not needed.

				results.add(result);
			}*/
		try {
			Query q = new QueryParser("", queryAnalyzer).parse(query);
			List<IndexDto> required = new LinkedList<>();
			List<IndexDto> nonrequired = new LinkedList<>();
			List<IndexDto> prohibited = new LinkedList<>();
			extractQuery(q, required, nonrequired, prohibited);

			Set<Document> docs = conjunctRequired(required);

			removeProhibited(prohibited, docs);

			addNonrequired(nonrequired, docs);

			results = createResults(docs, q);
		} catch (Exception e) {
			log.error(e);
		}

		return results;
	}

	private List<Result> createResults(Set<Document> docs, Query q) {
		List<Result> results = new LinkedList<>();
		for (Document hit : docs) {
			ResultImpl result = new ResultImpl();
			result.setDocumentID(hit.getId());

			float score = 0;
			for (BooleanClause clause : ((BooleanQuery) q).clauses()) {
				if (!clause.isProhibited()) {
					String token = tokenize(clause.toString())[0];
					IndexDto indexDto = index.get(token);
					if (indexDto != null) {
						Double tf = indexDto.getTfs().get(hit.getId());
						if (tf != null) {
							score += tf * indexDto.getIdf();
						}
					}
				}
			}

			result.setScore(score);

			results.add(result);
		}

		results.sort(Comparator.comparing(Result::getScore));
		int i = 1;
		for (Result r : results) {
			((ResultImpl) r).setRank(i++);
		}

		return results;
	}

	private static void removeProhibited(List<IndexDto> prohibited, Set<Document> docs) {
		for (IndexDto indexDto : prohibited) {
			if (docs.isEmpty()) {
				break;
			}

			docs.removeAll(indexDto.getDocs());
		}
	}

	private static void addNonrequired(List<IndexDto> nonrequired, Set<Document> docs) {
		for (IndexDto indexDto : nonrequired) {
			docs.addAll(indexDto.getDocs());
		}
	}

	private static Set<Document> conjunctRequired(List<IndexDto> required) {
		required.sort(Comparator.comparingInt(o -> o.getDocs().size()));
		Set<Document> docs = new TreeSet<>(Comparator.comparing(Document::getId));
		if (required.size() > 1) {
			IndexDto first = required.get(0);
			IndexDto second = required.get(1);

			Set<Document> firstSet = new TreeSet<>(Comparator.comparing(Document::getId));
			firstSet.addAll(first.getDocs());
			firstSet.retainAll(second.getDocs());
			docs.addAll(firstSet);

			for (int i = 2; i < required.size(); i++) {
				if (docs.isEmpty()) {
					break;
				}

				docs.retainAll(required.get(i).getDocs());
			}
		} else if (required.size() == 1) {
			docs.addAll(required.get(0).getDocs());
		}

		return docs;
	}

	private void extractQuery(Query q, List<IndexDto> required, List<IndexDto> nonrequired, List<IndexDto>
			prohibited) {
		for (BooleanClause clause : ((BooleanQuery) q).clauses()) {
			String token = tokenize(clause.toString())[0];
			IndexDto indexDto = index.get(token);
			if (indexDto != null) {
				if (clause.isProhibited()) {
					prohibited.add(indexDto);
				} else if (clause.isRequired()) {
					required.add(indexDto);
				} else {
					nonrequired.add(indexDto);
				}
			}
		}
	}
}
