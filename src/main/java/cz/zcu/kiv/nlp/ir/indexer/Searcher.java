package cz.zcu.kiv.nlp.ir.indexer;

import cz.zcu.kiv.nlp.ir.trec.data.Result;

import java.util.List;

/**
 * Created by Tigi on 6.1.2015.
 */
public interface Searcher {

	List<Result> search(String query);
}
