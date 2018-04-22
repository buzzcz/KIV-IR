package cz.zcu.kiv.nlp.ir.trec;

import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;

import java.util.LinkedList;
import java.util.List;

/**
 * @author tigi
 */

public class Index implements Indexer, Searcher {

    public void index(List<Document> documents) {
        //  todo implement
    }

    public List<Result> search(String query) {
        //  todo implement
        return new LinkedList<>();
    }
}
