package edu.columbia.advancedb.bing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import edu.columbia.advancedb.bing.vo.AppDocument;
import edu.columbia.advancedb.bing.vo.AppTuple;

public class QueryStats {

	private HashMap<String, Integer> relevantDocFreqMap = new HashMap<>();
	private HashMap<String, Integer> nonRelevantDocFreqMap = new HashMap<>();
	private HashMap<String, Float> relevantTermFreqMap = new HashMap<>();
	private HashMap<String, Float> nonRelevantTermFreqMap = new HashMap<>();
	private final Float TITLE_WEIGHT = 0.3f;
	private final Float DESCRIPTION_WEIGHT = 0.7f;
	// Tokenize on any character that is not a hyphen,apostrophe and not a word character(a-zA-Z_0-9)
	private final String TOKENIZE_PATTERN = "[^'\\w]";
	
	public List<String> getStatistics(List<AppDocument> docs, List<String> currentQuery) {
		
		// Build the relevant and non-relevant map
		relevantTermFreqMap.clear();
		nonRelevantTermFreqMap.clear();
		StopWords stopWords = new StopWords();
		String nearestWord = getNearestCommonWord(docs, currentQuery);
		Double nearestWordWeight = Double.NEGATIVE_INFINITY;
		AppTuple<String, Double> firstNewStringVal = new AppTuple<String, Double>(null, Double.NEGATIVE_INFINITY);
		AppTuple<String, Double> secondNewStringVal = new AppTuple<String, Double>(null, Double.NEGATIVE_INFINITY);
		
		// Create the current Query List
		// The current Query List would contain the (tf*idf) product
		// of the search strings
		// These (tf*idf) product would be used to determine the ordering of
		// the search words
		List<AppTuple<String, Double>> queryList = new ArrayList<AppTuple<String, Double>>();
		for(String s: currentQuery) {
			AppTuple<String, Double> queryObj = new AppTuple<String, Double>(s, Double.NEGATIVE_INFINITY);
			queryList.add(queryObj);
			queryObj = null;
		}
		
		HashSet<String> docsTermProcessSet = new HashSet<>();
		for(int i=0; i<docs.size();i++) {
			List<AppTuple<String, Float>> tokenList = tokenizeDocument(docs.get(i));
			float docTFVal = 0.0f;
			docsTermProcessSet.clear();
			for(int j=0; j<tokenList.size(); j++) {
				// Create the document frequency map
				if(!docsTermProcessSet.contains(tokenList.get(j).getItem1())) {
					// This term has not been processed yet
					docsTermProcessSet.add(tokenList.get(j).getItem1());
					if(docs.get(i).isRelevant()) {
						// Add to relevant map
						// Create the document frequency
						if(!relevantDocFreqMap.containsKey(tokenList.get(j).getItem1())) {
							relevantDocFreqMap.put(tokenList.get(j).getItem1(), 1);
						}
						else {
							// Term already exists. Add to the term
							int oldValue = relevantDocFreqMap.get(tokenList.get(j).getItem1());
							relevantDocFreqMap.put(tokenList.get(j).getItem1(), oldValue + 1);
						}
					}
					else {
						// Add to non-relevant map
						// Create the non-relevant document frequency
						if(!nonRelevantDocFreqMap.containsKey(tokenList.get(j).getItem1())) {
							nonRelevantDocFreqMap.put(tokenList.get(j).getItem1(), 1);
						}
						else {
							int oldValue = nonRelevantDocFreqMap.get(tokenList.get(j).getItem1());
							nonRelevantDocFreqMap.put(tokenList.get(j).getItem1(), oldValue + 1);
						}
					}
				}
			}
			for(int j=0; j<tokenList.size();j++) {
				// Iterate through the tokens
				if(docs.get(i).isRelevant()) {
					if(!relevantTermFreqMap.containsKey(tokenList.get(j).getItem1())) {
						relevantTermFreqMap.put(tokenList.get(j).getItem1(), 
							tokenList.get(j).getItem2());
					}
					else {
						// Term already exists. Add to the term
						Float oldValue = relevantTermFreqMap.get(tokenList.get(j).getItem1());
						relevantTermFreqMap.put(tokenList.get(j).getItem1(),
							oldValue + tokenList.get(j).getItem2());
					}
				}
				else {
					// Repeat the same with non-relevant map
					if(!nonRelevantTermFreqMap.containsKey(tokenList.get(j).getItem1())) {
						nonRelevantTermFreqMap.put(tokenList.get(j).getItem1(), 
								tokenList.get(j).getItem2());
					}
					else {
						// Term already exists. Add to the term
						Float oldValue = nonRelevantTermFreqMap.get(tokenList.get(j).getItem1());
						nonRelevantTermFreqMap.put(tokenList.get(j).getItem1(), 
								oldValue + tokenList.get(j).getItem2());
					}
				}
			}
		}
		
		// We have the term frequencies now
		// Calculate tf*idf value for each relevant term
		// subtracting their non-relevant tf * idf value
		Iterator<Entry<String, Float>> relevantTermIterator = relevantTermFreqMap.entrySet().iterator();
		while(relevantTermIterator.hasNext()) {
			Entry<String,Float> relevantEntry = relevantTermIterator.next();
			Float tempVal = relevantEntry.getValue();
			double relevantVal = tempVal * Math.log10((docs.size() * 1.0)/relevantDocFreqMap.get(relevantEntry.getKey()));
			if(!currentQuery.contains(relevantEntry.getKey())) {
				// Calculate negative values only for those keys
				// which do not appear in the current query
				if(nonRelevantDocFreqMap.containsKey(relevantEntry.getKey())) {
					// This term is also present in the non relevant documents
					// Subtract the tf * idf value
					relevantVal = relevantVal - (nonRelevantTermFreqMap.get(relevantEntry.getKey()) *
								Math.log10((docs.size() * 1.0)/nonRelevantDocFreqMap.get(relevantEntry.getKey())));
				}
			}
						
			if(relevantEntry.getKey().equalsIgnoreCase(nearestWord)) {
				nearestWordWeight = relevantVal;
			}
			// Check if this term is more than the maximum
			if(relevantVal >= firstNewStringVal.getItem2()) {
				if(!stopWords.isStopWord(relevantEntry.getKey()) && 
						!currentQuery.contains(relevantEntry.getKey())) {
					// Replace the secondNewStringVal with firstNewStringVal
					// Consider this word only if it is not a part of the current query
					// and is also not a stop word
					// If both values are equal, then replace the values
					// only if the length of the string replacing the
					// first string is more
					if(firstNewStringVal.getItem1() != null) {
						if(firstNewStringVal.getItem2() == relevantVal) {
							if(relevantEntry.getKey().length() <= firstNewStringVal.getItem1().length()) {
								// Both strings are of same length
								// Solve the tiebreaker by ignoring the smaller string
								continue;
							}
						}
					}
					// Overwrite
					secondNewStringVal.setItem1(firstNewStringVal.getItem1());
						secondNewStringVal.setItem2(firstNewStringVal.getItem2());
						
						// Overwrite the firstNewStringVal with relevantVal
						firstNewStringVal.setItem1(relevantEntry.getKey());
						firstNewStringVal.setItem2(relevantVal);
				}
				else {
					// This entry has already been considered by the user
					// Update the tf*idf value for the term
					for(AppTuple<String, Double> termEntry: queryList) {
						if(termEntry.getItem1().equalsIgnoreCase(relevantEntry.getKey())) {
							termEntry.setItem2(relevantVal);
							break;
						}
					}
				}
			}
			else {
				// not greater than the first value
				// Check if greater than the second value
				if(relevantVal >= secondNewStringVal.getItem2()) {
					// Greater . Replace if this entry has not been considered
					// by the user
					if(!stopWords.isStopWord(relevantEntry.getKey()) && 
							!currentQuery.contains(relevantEntry.getKey())) {
						// Consider this token only if it is not a stop word
						// and is also not present in the current query
						
						if(secondNewStringVal.getItem1() != null) {
							if(secondNewStringVal.getItem2() == relevantVal) {
								if(relevantEntry.getKey().length() <= secondNewStringVal.getItem1().length()) {
									// Both strings are of same length
									// Solve the tiebreaker by ignoring the smaller string
									continue;
								}
							}
						}
						// Overwrite
						secondNewStringVal.setItem1(relevantEntry.getKey());
						secondNewStringVal.setItem2(relevantVal);
					}
					else {
						// This entry has already been considered by the user
						// Update the tf * idf value for the term
						for(AppTuple<String, Double> termEntry: queryList) {
							if(termEntry.getItem1().equalsIgnoreCase(relevantEntry.getKey())) {
								termEntry.setItem2(relevantVal);
								break;
							}
						}
					}
				}
				else {
					// It might be possible that the relevantVal
					// for the currentQuery is less than firstNewStringVal
					// and secondNewStringVal. We still need to update
					// the values for the query items. These values will be used
					// for reordering the query
					for(AppTuple<String, Double> termEntry: queryList) {
						if(termEntry.getItem1().equalsIgnoreCase(relevantEntry.getKey())) {
							termEntry.setItem2(relevantVal);
							break;
						}
					}
				}
			}
		}
		
		// Add the firstNewVal and the secondNewVal to the List
		// Possible that only one relevant term has been added
		// or no relevant term has been added
		if(firstNewStringVal.getItem1() != null) {
			queryList.add(firstNewStringVal);
		}
		
		if((!isStringNullOrEmpty(nearestWord)) &&
				(!nearestWord.equalsIgnoreCase(firstNewStringVal.getItem1())) &&
				(!nearestWord.equalsIgnoreCase(secondNewStringVal.getItem1()))) {
			queryList.add(new AppTuple<String, Double>(nearestWord, nearestWordWeight));
		}
		else {
			// Add the second term only if the
			// nearest word does not exist
			// or the nearestWord is not equal to the first new string and second new string
			if(secondNewStringVal.getItem1() != null) {
				queryList.add(secondNewStringVal);
			}
		}
		
		// Sort the list
		Collections.sort(queryList, new Comparator<AppTuple<String, Double>>() {

			@Override
			public int compare(AppTuple<String, Double> arg0, AppTuple<String, Double> arg1) {
				if(arg0.getItem2() > arg1.getItem2()) {
					return 1;
				}
				else if(arg0.getItem2() == arg1.getItem2()) {
					return 0;
				}
				else {
					return -1;
				}
			}
		});
		
		Collections.reverse(queryList);
		// Create new Query list and return
		List<String> newQueryList = new ArrayList<String>();
		for(AppTuple<String, Double> tuple : queryList) {
			newQueryList.add(tuple.getItem1());
			System.out.println(tuple.getItem1() + ": " + tuple.getItem2());
		}
		return newQueryList;
	}
	
	// This method would fetch the word that appears just before the
	// currentQuery in all the documents
	// NULL if no such word exists
	// Similar to proximity searching
	private String getNearestCommonWord(List<AppDocument> documentList,
			List<String> currentQuery) {
		String queryPhrase = MainClass.listToKeyWords(currentQuery);
		HashMap<String, Integer> freqMap = new HashMap<String, Integer>();
		AppTuple<String, Integer> candidateNearestString = new AppTuple<String, Integer>(null, 0);
		int numRelevantDocs = 0;
		for(AppDocument document: documentList) {
			if(document.isRelevant()) {
				// Consider only relevant documents
				// where the phrase appears as it is
				// in the title or description
				if((document.getTitle().toLowerCase().trim().indexOf(queryPhrase) > -1) ||
						(document.getDescription().toLowerCase().trim().indexOf(queryPhrase) > -1)) {
					numRelevantDocs++;
				}
				else {
					// There is a document which has been marked as relevant
					// and the phrase does not appear as it is in that document
					// As a result, that document will not be counted in this method
					// resulting in skewed values. Therefore, return null
					return null;
				}
				
				String candidateDescription = null;
				int idx = document.getDescription().toLowerCase().trim().indexOf(queryPhrase);
				if(idx >= 0) {
					// Found the phrase in description
					int lastSpace = idx - 1;
					String prevWord = findPreviousWord(document.getDescription().trim(), queryPhrase, lastSpace);
					if(!isStringNullOrEmpty(prevWord)) {
						// Valid word
						// Add to dictionary
						candidateDescription = prevWord;
						if(!freqMap.containsKey(prevWord)) {
							freqMap.put(prevWord, 1);
							if(1 > candidateNearestString.getItem2()) {
								candidateNearestString.setItem1(prevWord);
								candidateNearestString.setItem2(1);
							}
						}
						else {
							int oldValue = freqMap.get(prevWord);
							freqMap.put(prevWord, oldValue + 1);
							if((oldValue + 1) > candidateNearestString.getItem2()) {
								candidateNearestString.setItem1(prevWord);
								candidateNearestString.setItem2(oldValue + 1);
							}
						}
					}
				}
			}
		}
		return candidateNearestString.getItem1();
	}
	
	// Phrase is the string in which the searching needs to be done
	// lastPosition is the position of the character just before the current query
	private String findPreviousWord(String phrase, String searchQuery, int lastSpacePosition) {
		final int positionLimit = 5;
		int counter = 0;
		
		String candidateWord = null;
		StopWords stopWords = new StopWords();
		String subPhrase = null;
		String[] splitSubPhraseArray = null;
		
		if(lastSpacePosition > -1) {
			if(phrase.charAt(lastSpacePosition) != ' ') {
				// The character at lastPosition is not a space
				// CurrentQuery is present as a part of a word. Not a stand-alone word
				// Not a valid scenario
				return null;
			}
			subPhrase = phrase.substring(0, lastSpacePosition);
			splitSubPhraseArray = subPhrase.split(" ");
			
			// Find the first capitalized word from reverse
			// Read the array in reverse and find the first capitalized word
			// The first capitalized word is a proper noun and usually signifies a name
			for(int i = splitSubPhraseArray.length - 1; i>=0; i--) {
				counter++;
				if(splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim().length() > 0) {
					if(Character.isUpperCase(splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim().charAt(0))) {
						candidateWord = splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim();
						break;
					}
				}
				if(counter == positionLimit) {
					// No candidate word found in the position limits
					break;
				}
			}
			
			if(candidateWord != null) {
				// There is such a capitalized word. Check if it is a stop word
				if(!stopWords.isStopWord(candidateWord)) {
					// this is a valid word. Return this word
					return candidateWord.toLowerCase();
				}
			}
		}
		
		// Repeat the same for words that appear after the phrase
		counter = 0;
		if((lastSpacePosition + searchQuery.length()) < (phrase.length() - 2)) {
			// There can be a capital word that exists after the searchQuery
			subPhrase = phrase.substring(lastSpacePosition + searchQuery.length());
			splitSubPhraseArray = subPhrase.split(" ");
			// Repeat the same process as we did above
			// However, increment this time
			for(int i = 0; i < splitSubPhraseArray.length; i++) {
				counter++;
				if(splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim().length() > 0) {
					if(Character.isUpperCase(splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim().charAt(0))) {
						candidateWord = splitSubPhraseArray[i].replaceAll(TOKENIZE_PATTERN, "").trim();
						break;
					}
				}
				if(counter == positionLimit) {
					// No candidate word found in the position limits
					break;
				}
			}
			
			if(candidateWord != null) {
				// There is such a capitalized word. Check if it is a stop word
				if(!stopWords.isStopWord(candidateWord)) {
					// this is a valid word. Return this word
					return candidateWord.toLowerCase();
				}
			}
		}
		
		// There is no such candidate word or it was a stop word
		// Find the nearest previous word
		if(lastSpacePosition <= 1) {
			// No previous word possible
			return null;
		}
		int spaceIdxBeforeEligibleWord = phrase.lastIndexOf(' ', lastSpacePosition - 1);
		candidateWord = phrase.substring(spaceIdxBeforeEligibleWord + 1, lastSpacePosition).trim();
		
		if(stopWords.isStopWord(candidateWord.toLowerCase())) {
			// If the stop word is the previous word, return null
			return null;
		}
		return candidateWord.toLowerCase();
	}
	
	private boolean isStringNullOrEmpty(String input) {
		return ((input == null) || (input.trim().length() == 0));
	}
	
	private List<AppTuple<String, Float>> tokenizeDocument(AppDocument document) {
		List<AppTuple<String, Float>> result = new ArrayList<>();
		
		// Tokenize the title. Multiple occurrences
		// are considered as multiple occurrences and therefore,
		// carry more preference
		HashMap<String, Integer> occurrenceSet = new HashMap<String, Integer>();
		String[] titleArray = document.getTitle().split(TOKENIZE_PATTERN);
		for(int i=0; i<titleArray.length; i++) {
			if(titleArray[i].length() > 0) {
				// Consider only those strings which are not empty
				if(!occurrenceSet.containsKey(titleArray[i].toLowerCase())) {
					occurrenceSet.put(titleArray[i].toLowerCase(), 1);
				}
				else {
					// Value exist. Add to the existing count
					int oldValue = occurrenceSet.get(titleArray[i].toLowerCase());
					occurrenceSet.put(titleArray[i].toLowerCase(), oldValue + 1);
				}
			}
		}
		Set<Entry<String,Integer>> titleEntrySet = occurrenceSet.entrySet();
		Iterator<Entry<String,Integer>> titleIterator = titleEntrySet.iterator();
		while(titleIterator.hasNext()) {
			Entry<String,Integer> currentEntry = titleIterator.next();
			AppTuple<String, Float> tokenObj = new AppTuple<String, Float>(currentEntry.getKey(), TITLE_WEIGHT * currentEntry.getValue());
			result.add(tokenObj);
			tokenObj = null;
		}
		
		// Tokenize the description. Multiple occurrences are considered
		// as multiple occurrences and therefore, carry more preference
		occurrenceSet.clear();
		String[] descArray = document.getDescription().split(TOKENIZE_PATTERN);
		for(int i=0; i<descArray.length; i++) {
			if(descArray[i].length() > 0) {
				// Consider only those strings which are not empty
				if(!occurrenceSet.containsKey(descArray[i].toLowerCase())) {
					occurrenceSet.put(descArray[i].toLowerCase(), 1);
				}
				else {
					// Value exist. Add to the existing count
					int oldValue = occurrenceSet.get(descArray[i].toLowerCase());
					occurrenceSet.put(descArray[i].toLowerCase(), oldValue + 1);
				}
			}
		}
		// Parse the description set
		Set<Entry<String,Integer>> descEntrySet = occurrenceSet.entrySet();
		Iterator<Entry<String,Integer>> descIterator = descEntrySet.iterator();
		while(descIterator.hasNext()) {
			Entry<String,Integer> currentEntry = descIterator.next();
			AppTuple<String, Float> tokenObj = new AppTuple<String, Float>(currentEntry.getKey(), DESCRIPTION_WEIGHT * currentEntry.getValue());
			result.add(tokenObj);
			tokenObj = null;
		}
		
		return result;
	}
}
