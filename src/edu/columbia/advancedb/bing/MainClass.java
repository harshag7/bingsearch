package edu.columbia.advancedb.bing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import edu.columbia.advancedb.bing.vo.AppDocument;

/*
 * Main Entry class for the program
 * Accepts arguments in below order
 * 1 - Bing Account Key
 * 2 - Desired Precision
 * 3 - Key words separated by space
 */
public class MainClass {
	
	//public static String ACCOUNT_KEY = "zwdDE6X7sdfQJFaccidYZ9xFhXssOH+buQuMn2Owv9g";
	
	// Will be instantiated upon execution start. Use stopWords.isStopWord(str) to check if it is stop word.
	public static StopWords stopWords;

	public static void main(String[] args) throws IOException {
		if(args != null && args.length >= 3) {
			String accountKey = args[0];
			String desiredPrecision = args[1];
			String keyWords = args[2];
			List<String> currentQuery = keyWordsToList(keyWords);
			
			// Instantiate StopWords
			stopWords = new StopWords();
			
			// Start Bing Search
			BingSearch search = new BingSearch();
			List<AppDocument> docs = search.getResults(accountKey, currentQuery);
			
			// TO-DO
			// Output result to User and populate relevance in AppDocument
			
			
			// TO-DO
			// Check if desired precision is reached else call QueryStats to get new query list
			// while precision not reached call
			QueryStats stats = new QueryStats();
			List<String> query = stats.getStatistics(docs, currentQuery);
			
			
		} else {
			System.out.println("Invalid number of arguments");
			System.exit(1);
		}
	}
	
	private static List<String> keyWordsToList(String keyWords) {
		String[] keys = keyWords.split("");
		List<String> wordList = Arrays.asList(keys);  
		return wordList;
	}

}