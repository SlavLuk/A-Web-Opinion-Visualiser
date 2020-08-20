package ie.gmit.sw.parser;

import java.io.File;
import java.util.concurrent.Callable;

import ie.gmit.sw.ai.cloud.WordFrequency;

public class NodeWorker extends NodeParser implements Callable<WordFrequency[]>{

	public NodeWorker(String term, Parseable ignore, int numOfWords, File fname) throws Exception {
		super(term,ignore,numOfWords,fname);
		
		
	}
	
	@Override
	public WordFrequency[] call() throws Exception {

		parse();

		return getWordFrequencyKeyValue();
	}

}
