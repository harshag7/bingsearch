package edu.columbia.advancedb.bing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopWords {
	
	List<String> words = new ArrayList<>();
	
	public StopWords() {
		// Read file and store words in a list
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader("/Harsha/workspace/Java_J2EE/advanceddb1/src/stop.txt"));

			while ((sCurrentLine = br.readLine()) != null) {
				words.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public boolean isStopWord(String word) {
		return words.contains(word);
	}
}
