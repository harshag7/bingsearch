package edu.columbia.advancedb.bing;

import java.util.ArrayList;
import java.util.List;

public class StopWords {
	
	List<String> words = new ArrayList<>();
	
	public StopWords() {
		// TO - DO
		// Read file and store words in a list
	}
	
	public boolean isStopWord(String word) {
		return words.contains(word);
	}
}
