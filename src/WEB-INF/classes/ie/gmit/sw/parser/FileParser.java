package ie.gmit.sw.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileParser implements Parseable {

	private List<String> ignoreWords;
	private File file;

	public FileParser(File f) {

		this.file = f;
	}

	@Override
	public void parse() {

		ignoreWords = new ArrayList<>();

		try {

			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;

			while ((line = br.readLine()) != null) {
				String[] w = line.split(" ");

				for (String s : w) {

					ignoreWords.add(s.trim().toLowerCase().replaceAll("[^a-z]", ""));
				}

			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public List<String> getIgnoreWords() {

		return ignoreWords;
	}
}
