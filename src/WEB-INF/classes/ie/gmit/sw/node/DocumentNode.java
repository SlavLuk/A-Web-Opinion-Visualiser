package ie.gmit.sw.node;

import org.jsoup.nodes.Document;

public class DocumentNode {
	private Document d;
	private int score;

	public DocumentNode(Document d, int score) {
		this.d = d;
		this.score = score;
	}

	public Document getDocument() {
		return d;
	}

	public int getScore() {
		return score;
	}

}
