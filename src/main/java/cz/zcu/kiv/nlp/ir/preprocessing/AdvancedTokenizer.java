/**
 * Copyright (c) 2014, Michal Konkol
 * All rights reserved.
 */
package cz.zcu.kiv.nlp.ir.preprocessing;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Konkol
 */
public class AdvancedTokenizer implements Tokenizer {

	private static final String HTTP_REGEX = "(https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b" +
			"([-a-zA-Z0-9@:%_\\+.~#?&\\/=]*))";

	private static final String DATE_REGEX = "(\\d{1,2}\\.\\d{1,2}\\.(\\d{2,4})?)";

	private static final String DECIMAL_REGEX = "(\\d+[.,](\\d+)?)";

	private static final String TEXT_AND_NUMBER_REGEX = "([\\p{L}*\\d]+)";

	private static final String HTML_REGEX = "(<.*?>)";

	private static final String PUNCTIATION_REGEX = "([\\p{Punct}])";

	public static final String DEFAULT_REGEX = HTTP_REGEX + "|" + DATE_REGEX + "|" + DECIMAL_REGEX + "|" +
			TEXT_AND_NUMBER_REGEX + "|" + HTML_REGEX + "|" + PUNCTIATION_REGEX;

	public static String[] tokenize(String text, String regex) {
		Pattern pattern = Pattern.compile(regex);

		ArrayList<String> words = new ArrayList<>();

		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();

			words.add(text.substring(start, end));
		}

		String[] ws = new String[words.size()];
		ws = words.toArray(ws);

		return ws;
	}

	public static String removeAccents(String text) {
		return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll
				("\\p{InCombiningDiacriticalMarks}+", "");
	}

	@Override
	public String[] tokenize(String text) {
		return tokenize(text, DEFAULT_REGEX);
	}
}
