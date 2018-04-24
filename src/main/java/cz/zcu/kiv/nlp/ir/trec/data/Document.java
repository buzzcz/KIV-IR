package cz.zcu.kiv.nlp.ir.trec.data;

import java.util.Date;

/**
 * Created by Tigi on 8.1.2015.
 */
public interface Document {

	String getText();

	String getId();

	String getTitle();

	Date getDate();

}
