package edu.columbia.advancedb.bing;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import edu.columbia.advancedb.bing.vo.AppDocument;

/*
 * Main Entry class for the program
 * Accepts arguments in below order
 * 1 - Bing Account Key
 * 2 - Desired Precision
 * 3 - Key words enclosed in quotes, separated by space
 */
public class MainClass {
	
	// Will be instantiated upon execution start. Use stopWords.isStopWord(str) to check if it is stop word.
	public static StopWords stopWords;
	private static Scanner scan;

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
			
			//Initial output
			System.out.println("Parameters:");
			System.out.println("Client key - " + accountKey);
			System.out.println("Query - " + listToKeyWords(currentQuery));
			System.out.println("Desired Precision - " + desiredPrecision);
			System.out.println("Total no of results - " + docs.size());
			
			// Check if result is less than 10 and exit, if so
			checkResultLessThan10(docs);
			
			System.out.println("Bing Search Results:");
			System.out.println("======================");
			
			// Output result to User and populate relevance in AppDocument
			getUserRelevance(docs);
			
			// Check if desired precision is reached else call QueryStats to get new query list
			float currPrecision = getPrecision(docs);
			float desiredPreInDouble = Float.parseFloat(desiredPrecision);
			
			// Check if current precision is 0, exit if so
			checkCurrentPrecision(currPrecision);
			
			// Check if desired precision is reached. If not, then augment query.
			while(desiredPreInDouble > currPrecision) {
				
				System.out.println("FEEDBACK SUMMARY:");
				System.out.println("Precision - " + currPrecision);
				System.out.println("Still below the desired precision of  - " + desiredPrecision);
				System.out.println("Issuing new query");
				
				QueryStats stats = new QueryStats();
				currentQuery = stats.getStatistics(docs, currentQuery);
				
				System.out.println("Client key - " + accountKey);
				System.out.println("Query - " + listToKeyWords(currentQuery));
				
				// Start Bing Search
				search = new BingSearch();
				docs = search.getResults(accountKey, currentQuery);
				
				System.out.println("Total no of results - " + docs.size());
				
				// Check if result is less than 10 and exit, if so
				checkResultLessThan10(docs);
				
				System.out.println("Bing Search Results:");
				System.out.println("======================");
				
				// Output result to User and populate relevance in AppDocument
				getUserRelevance(docs);
				
				// Update the precision
				currPrecision = getPrecision(docs);
				
				// Check if current precision is 0 and exit, if so
				checkCurrentPrecision(currPrecision);
			}
			
			System.out.println("======================");
			System.out.println("FEEDBACK SUMMARY");
			System.out.println("Query - " + listToKeyWords(currentQuery));
			System.out.println("Precision - " + currPrecision);
			System.out.println("Desired precision reached, done");
			
			
		} else {
			System.out.println("Invalid number of arguments");
			System.exit(1);
		}
	}
	
	/*
	 * Utility method to convert Key Words String to Array List in order.
	 */
	private static List<String> keyWordsToList(String keyWords) {
		String[] keys = keyWords.split(" ");
		List<String> wordList = Arrays.asList(keys);  
		return wordList;
	}
	
	/*
	 * Utility method to convert List of key words back to String.
	 */
	public static String listToKeyWords(List<String> keyWords) {
		String keys = "";
		for(String key: keyWords) {
			keys = keys + key.toLowerCase() + " ";
		}
		return keys.trim();
	}
	
	/*
	 * Method to show Bing result to User and scan relevance from User
	 */
	private static void getUserRelevance(List<AppDocument> docs) {
		for(int i=0;i< docs.size(); i++) {
			System.out.println("Result " + (i+1));
            System.out.println("[");
            System.out.println("\tURL: " + docs.get(i).getUrl());
            System.out.println("\tTitle: " + docs.get(i).getTitle());
            System.out.println("\tSummary: " + docs.get(i).getDescription());
            System.out.println("]\n");
            System.out.print("Relevant (Y/N)?");
            
            scan = new Scanner(System.in);
            String s = scan.next();
            if(s.equalsIgnoreCase("y")) {
            	docs.get(i).setRelevant(true);
            } else {
            	docs.get(i).setRelevant(false);
            }
		}
	}
	
	/*
	 * Calculate precision of docs
	 */
	private static float getPrecision(List<AppDocument> docs) {
		int numDocs = 0;
		for (AppDocument doc: docs) {
			if(doc.isRelevant()) {
				numDocs++;
			}
		}
		
		return ((numDocs * 1.0f)/docs.size());
	}
	
	/*
	 * Safety check to see if bing returned less than 10 result and exit, if so
	 */
	private static void checkResultLessThan10(List<AppDocument> docs) {
		if(docs.size() < 10) {
			System.out.println("Returned less than 10 results");
			System.out.println("Exiting");
			System.exit(0);
		}
	}
	
	/*
	 * Safety check to see if no relevant document is present and exit, if so, as query can't be augmented
	 */
	private static void checkCurrentPrecision(float currPrecision) {
		if(currPrecision == 0f) {
			System.out.println("Precision 0.0");
			System.out.println("Cannot Augment query. Exiting");
			System.exit(0);
		}
	}

}