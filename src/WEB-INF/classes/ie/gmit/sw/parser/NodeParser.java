package ie.gmit.sw.parser;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ie.gmit.sw.ai.cloud.WordFrequency;
import ie.gmit.sw.fuzzy.FuzzyLogic;
import ie.gmit.sw.node.DocumentNode;
import ie.gmit.sw.utils.StringUtils;

public class NodeParser implements Parseable {

	private static final int MAX = 100;
	private static final int TITLE_WEIGHT = 70;
	private static final int HEADING_WEIGHT = 20;
	private static final int PARAGRAPH_WEIGHT = 10;
	private static final int FREQUENCY = 1;
	private int numOfWords;
	private List<String> ignoreWords;
	private String url;
	private FuzzyLogic fuzzy;
	private String term;
	private int score;
	private Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();	
	private Set<String> closed = new ConcurrentSkipListSet<>();
	private Queue<DocumentNode> queue = new PriorityQueue<>(Comparator.comparing(DocumentNode::getScore));

	public NodeParser(String term, Parseable ignore, int numOfWords, File fname) throws Exception {
		this.term = term;
		this.numOfWords = numOfWords;
		fuzzy = new FuzzyLogic(fname);
		ignoreWords = ((FileParser)ignore).getIgnoreWords();
		this.url = String.format("https://www.bing.com/search?q=%s",
				URLEncoder.encode(term, StandardCharsets.UTF_8.toString()));

	}

	@Override
	public void parse() throws Exception {

		System.out.println("Processing request....");

		Document doc = Jsoup.connect(url).get();
		closed.add(url);
		score = getHeuristicScore(doc);
		queue.offer(new DocumentNode(doc, score));

		while (!queue.isEmpty() && closed.size() <= MAX) {

			DocumentNode node = queue.poll();
			doc = node.getDocument();

			Elements edges = doc.select("a[href]");

			for (Element e : edges) {
				String link = e.absUrl("href");

				if (link != null && closed.size() <= MAX && !closed.contains(link)) {
					try {

						closed.add(link);
						Document childNode = Jsoup.connect(link).get();
						score = getHeuristicScore(childNode);
						queue.offer(new DocumentNode(childNode, score));

					} catch (Exception ex) {

						System.err.println(ex.getMessage());
					}

				}
			}
		}

	}

	private int getHeuristicScore(Document doc) throws Exception {

		int score = 0;
		int titleScore = 0;
		int headingsScore = 0;
		int bodyScore = 0;
		StringBuilder st = new StringBuilder();

		String title = doc.title();
		st.append(title);
		st.append(" ");

		titleScore += getFrequence(title, term) * TITLE_WEIGHT;

		Elements bodies = doc.select("body");

		for (Element b : bodies) {
			if (!b.hasText()) {
				continue;
			}
			String body = b.text();

			st.append(body);
			st.append(" ");
			bodyScore += getFrequence(body, term) * PARAGRAPH_WEIGHT;

		}

		Elements headings = doc.select("h1");

		for (Element heading : headings) {
			if (!heading.hasText()) {
				continue;
			}
			String h1 = heading.text();
			st.append(h1);
			st.append(" ");

			headingsScore += getFrequence(h1, term) * HEADING_WEIGHT;

		}

		score = fuzzy.getFuzzyHerustic(titleScore, headingsScore, bodyScore);

		if (score >= 240) {

			index(st.toString());
		}

		return score;
	}



	private int getFrequence(String text, String term) {

		String[] terms = term.toLowerCase().trim().split(" ");

		int frequency = 0;

		for (String t : terms) {

			frequency += StringUtils.countMatches(text.toLowerCase(), t);

		}

		return frequency;
	}

	// Extract each word from the string and add to the map after filtering with
	// ignore words.
	private void index(String text) {

		String[] words = text.split(" ");
		String[] terms = term.toLowerCase().trim().split(" ");
		List<String> term = Arrays.asList(terms);

		for (String s : words) {

			String w = s.toLowerCase().trim().replaceAll("[^a-z]", "");

			if (w.isEmpty() || w.length() <= 3) {
				continue;
			}

			if (ignoreWords.contains(w)) {
				continue;
			}

			if (term.contains(w)) {
				continue;
			}

			if (map.get(w) instanceof Integer) {

				int f = map.get(w);

				map.put(w, ++f);

			} else {

				map.put(w, FREQUENCY);

			}

		}
	}

	public WordFrequency[] getWordFrequencyKeyValue() {

		WordFrequency[] wordFrequencies = new WordFrequency[numOfWords];

		Stream<Map.Entry<String, Integer>> sorted = map.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

		Stream<Entry<String, Integer>> keySet = sorted.limit(numOfWords);

		Map<String, Integer> words = new LinkedHashMap<>();

		keySet.forEach(entry -> {

			words.put(entry.getKey(), entry.getValue());

		});

		Iterator<String> itr = words.keySet().iterator();

		int i = 0;

		while (itr.hasNext()) {

			String key = itr.next();

			wordFrequencies[i] = new WordFrequency(key, words.get(key));

			i++;
		}

		return wordFrequencies;
	}



}
