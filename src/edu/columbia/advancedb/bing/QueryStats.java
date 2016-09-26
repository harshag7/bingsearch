package edu.columbia.advancedb.bing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import edu.columbia.advancedb.bing.vo.AppDocument;
import edu.columbia.advancedb.bing.vo.AppTuple;

public class QueryStats {

	private HashMap<String, Integer> relevantDocFreqMap = new HashMap<>();
	private HashMap<String, Integer> nonRelevantDocFreqMap = new HashMap<>();
	private HashMap<String, Float> relevantTermFreqMap = new HashMap<>();
	private HashMap<String, Float> nonRelevantTermFreqMap = new HashMap<>();
	private final Float TITLE_WEIGHT = 1.0f;
	private final Float DESCRIPTION_WEIGHT = 1.0f;
	private final String TOKENIZE_PATTERN = " ";
	
	public List<String> getStatistics(List<AppDocument> docs, List<String> currentQuery) {
		
		// Build the relevant and non-relevant map
		relevantTermFreqMap.clear();
		nonRelevantTermFreqMap.clear();
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
		
		for(int i=0; i<docs.size();i++) {
			List<AppTuple<String, Float>> tokenList = tokenizeDocument(docs.get(i));
			float docTFVal = 0.0f;
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
		
		// We have the term frequencies now
		// Calculate tf*idf value for each relevant term
		// subtracting their non-relevant tf * idf value
		Iterator<Entry<String, Float>> relevantTermIterator = relevantTermFreqMap.entrySet().iterator();
		while(relevantTermIterator.hasNext()) {
			Entry<String,Float> relevantEntry = relevantTermIterator.next();
			Float tempVal = relevantEntry.getValue();
			double relevantVal = tempVal * Math.log10(docs.size()/relevantDocFreqMap.get(relevantEntry.getKey()));
			if(nonRelevantDocFreqMap.containsKey(relevantEntry.getKey())) {
				// This term is also present in the non relevant documents
				// Subtract the tf * idf value
				relevantVal = relevantVal - (nonRelevantTermFreqMap.get(relevantEntry.getKey()) *
								Math.log10(docs.size()/nonRelevantDocFreqMap.get(relevantEntry.getKey())));
			}
						
			// Check if this term is more than the maximum
			if(relevantVal > firstNewStringVal.getItem2()) {
				if(!currentQuery.contains(relevantEntry.getKey())) {
					// Replace the secondNewStringVal with firstNewStringVal
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
				if(relevantVal > secondNewStringVal.getItem2()) {
					// Greater . Replace if this entry has not been considered
					// by the user
					if(!currentQuery.contains(relevantEntry.getKey())) {
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
			}
		}
		
		// Add the firstNewVal and the secondNewVal to the List
		// Possible that only one relevant term has been added
		// or no relevant term has been added
		if(firstNewStringVal.getItem1() != null) {
			queryList.add(firstNewStringVal);
		}
		
		if(secondNewStringVal.getItem1() != null) {
			queryList.add(secondNewStringVal);
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
		
		// Create new Query list and return
		List<String> newQueryList = new ArrayList<String>();
		for(AppTuple<String, Double> tuple : queryList) {
			newQueryList.add(tuple.getItem1());
		}
		return newQueryList;
	}
	
	private List<AppTuple<String, Float>> tokenizeDocument(AppDocument document) {
		List<AppTuple<String, Float>> result = new ArrayList<>();
		
		// Tokenize the title. Multiple occurrences are considered as single occurrence
		HashSet<String> occurrenceSet = new HashSet<String>();
		String[] titleArray = document.getTitle().split(TOKENIZE_PATTERN);
		for(int i=0; i<titleArray.length; i++) {
			if(!occurrenceSet.contains(titleArray[i].toLowerCase())) {
				occurrenceSet.add(titleArray[i].toLowerCase());
			}
		}
		Iterator<String> titleIterator = occurrenceSet.iterator();
		while(titleIterator.hasNext()) {
			AppTuple<String, Float> tokenObj = new AppTuple<String, Float>(titleIterator.next(), TITLE_WEIGHT);
			result.add(tokenObj);
			tokenObj = null;
		}
		
		// Tokenize the description. Multiple occurrences are considered as single occurrence
		occurrenceSet.clear();
		String[] descArray = document.getDescription().split(TOKENIZE_PATTERN);
		for(int i=0; i<descArray.length; i++) {
			if(!occurrenceSet.contains(descArray[i].toLowerCase())) {
				occurrenceSet.add(descArray[i].toLowerCase());
			}
		}
		Iterator<String> descIterator = occurrenceSet.iterator();
		while(descIterator.hasNext()) {
			AppTuple<String, Float> tokenObj = new AppTuple<String, Float>(descIterator.next(), DESCRIPTION_WEIGHT);
			result.add(tokenObj);
			tokenObj = null;
		}
		
		return result;
	}
}
