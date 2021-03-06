package de.hterhors.semanticmr.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TFIDF {

	public static final String[] DOC1 = { "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in",
			"into", "a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "for", "if",
			"in", "into", "their", "then", "there", "these", "baum", "they", "this", "to", "was", "is", "it", "no",
			"not", "of", "baum", "they", "this", "to", "was", "is", "it", "no", "not", "of", "baum", "they", "this",
			"to", "was", "is", "it", "no", "not", "of", "on", "or", "such", "that", "the", "into", "a", "an", "and",
			"are", "as", "haus", "at", "be", "but", "by", "for", "if", "in", "into", "for", "if", "in", "into", "their",
			"then", "there", "these", "they", "this", "to", "was", "their", "then", "there", "these", "they", "this",
			"to", "was", "will", "with" };

	public static final String[] DOC2 = { "a", "an", "and", "are", "as", "at", "are", "as", "at", "be", "but", "by",
			"for", "if", "in", "into", "pflanze", "they", "this", "to", "was", "is", "it", "no", "not", "of", "on",
			"at", "be", "but", "by", "for", "if", "in", "into", "for", "if", "to", "was", "their", "then", "there",
			"these", "they", "this", "to", "was", "will", "with" };

	public static final String[] DOC3 = { "a", "an", "and", "are", "as", "at", "are", "as", "at", "be", "but", "by",
			"for", "if", "in", "into", "pflanze", "they", "this", "to", "was", "is", "it", "no", "not", "for", "if",
			"in", "into", "their", "then", "there", "these", "baum", "they", "this", "to", "was", "is", "it", "no",
			"not", "of", "on", "or", "such", "that", "the", "into", "a", "an", "and", "are", "as", "haus", "at", "be",
			"but", "by", "for", "if", "in", "into", "for", "if", "in", "into", "their", "then", "there", "these",
			"they", "this", "to", "was", "will", "with" };

	public static void main(String[] args) {
		System.out.println(getTF(Arrays.asList(DOC1), "baum"));
		System.out.println(getTF(Arrays.asList(DOC2), "baum"));
		System.out.println(getTF(Arrays.asList(DOC3), "baum"));

		System.out.println(getTFs(Arrays.asList(DOC3), true));

		final Map<String, List<String>> documents = new HashMap<String, List<String>>();

		documents.put("doc1", Arrays.asList(DOC1));
		documents.put("doc2", Arrays.asList(DOC2));
		documents.put("doc3", Arrays.asList(DOC3));

		System.out.println(getIDF(documents, "baum"));

		System.out.println(getTFIDF(documents, "doc1", "baum"));
		System.out.println(getTFIDF(documents, "doc2", "pflanze"));
		System.out.println(getTFIDF(documents, "doc3", "baum"));
	}

	public static final double getTF(final List<String> document, String term) {
		final AtomicInteger tf = new AtomicInteger(0);

		document.stream().forEach(word -> {
			tf.addAndGet(word.equals(term) ? 1 : 0);
		});

		return tf.doubleValue();
	}

	public static final Map<String, Double> getTFs(final List<String> document, boolean normalize) {
		Map<String, Double> tfs = new ConcurrentHashMap<String, Double>(document.size());

		document.stream().forEach(word -> {
			tfs.put(word, tfs.getOrDefault(word, 0d) + 1);
		});

		if (normalize) {
			AtomicInteger sum = new AtomicInteger(0);
			tfs.values().stream().forEach(d -> sum.addAndGet(d.intValue()));
			tfs.entrySet().stream().forEach(e -> e.setValue(e.getValue() / sum.get()));
		}
		return tfs;
	}

	public static double getIDF(final Map<String, List<String>> documents, String term) {
		final double N = documents.size();
		final AtomicInteger termCount = new AtomicInteger(0);

		documents.values().stream().forEach(word -> {
			termCount.addAndGet(word.contains(term) ? 1 : 0);
		});
		if (termCount.intValue() == 0) {
			return 0;
		}
		double idf = Math.log(N / termCount.doubleValue());
		return idf;
	}

	public static <A extends Collection<String>> Map<String, Double> getIDFs(final Map<String, A> documents) {

		final double N = documents.size();

		Set<String> dict = new HashSet<String>();
		Map<String, Double> termCounts = new HashMap<String, Double>(dict.size());
		Map<String, Double> idfs = new HashMap<String, Double>(dict.size());

		documents.entrySet().forEach(e -> e.getValue().stream().forEach(w -> {
			dict.add(w);
		}));

//		System.out.println("Built dictionary: " + dict.size());

//		System.out.println("Calculate document counts: ");

//		final AtomicInteger count = new AtomicInteger(0);

		dict.stream().forEach(word -> {
//			if (count.incrementAndGet() % 1000 == 0) {
//				System.out.println("count = " + count.get());
//			}
			documents.values().stream().forEach(document -> {

				termCounts.put(word, termCounts.getOrDefault(word, 0d) + (document.contains(word) ? 1d : 0d));
			});
		});

//		count.set(0);

//		System.out.println("Calculate inverse document frequency: ");
		termCounts.entrySet().forEach(termCount -> {

//				if (count.incrementAndGet() % 1000 == 0) {
//					System.out.println("count = " + count.get());
//				}
			idfs.put(termCount.getKey(),
					(termCount.getValue().intValue()) == 0 ? 0 : Math.log(N / termCount.getValue().doubleValue()));
		});
		return idfs;
	}

	public static final double getTFIDF(final Map<String, List<String>> documents, String documentName, String term) {
		double tfidf = getTF(documents.get(documentName), term) * getIDF(documents, term);
		return tfidf;
	}

	public static final Map<String, Map<String, Double>> getTFIDFs(final Map<String, List<String>> documents,
			boolean normalizedTFValues) {

		Map<String, Map<String, Double>> output = new HashMap();
		Map<String, Double> idfs = getIDFs(documents);

		for (Entry<String, List<String>> e : documents.entrySet()) {
			Map<String, Double> tfs = getTFs(e.getValue(), normalizedTFValues);

			Map<String, Double> v = new HashMap<>();
			output.put(e.getKey(), v);
			double max = 1;
			if (normalizedTFValues) {
				for (Entry<String, Double> string : tfs.entrySet()) {
					max = string.getValue() * Math.max(max, idfs.getOrDefault(string.getKey(), 0D));
				}
			}
			for (Entry<String, Double> string : tfs.entrySet()) {

				v.put(string.getKey(), string.getValue() * idfs.getOrDefault(string.getKey(), 0D) / max);
			}

		}

		return output;
	}

	public static final Map<String, Map<String, Double>> getIDFs(final Map<String, List<String>> documents,
			boolean localNormalizing) {

		Map<String, Map<String, Double>> output = new HashMap();
		Map<String, Double> idfs = getIDFs(documents);

		for (Entry<String, List<String>> e : documents.entrySet()) {
			Map<String, Double> tfs = getTFs(e.getValue(), localNormalizing);

			Map<String, Double> v = new HashMap<>();
			output.put(e.getKey(), v);
			double max = 1;
			if (localNormalizing) {
				for (Entry<String, Double> string : tfs.entrySet()) {
					max = Math.max(max, idfs.getOrDefault(string.getKey(), 0D));
				}
			}
			for (Entry<String, Double> string : tfs.entrySet()) {
				v.put(string.getKey(), idfs.getOrDefault(string.getKey(), 0D) / max);
			}

		}

		return output;
	}

}
