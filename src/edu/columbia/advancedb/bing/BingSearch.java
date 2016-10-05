package edu.columbia.advancedb.bing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.columbia.advancedb.bing.vo.AppDocument;

/*
 * Utility class to query Bing API, store results in AppDocument object
 */
public class BingSearch {

	public static String BING_URL = "https://api.datamarket.azure.com/Bing/Search/Web";
	public static String EXTRA_PARAMS = "$top=10&$format=JSON";
	
	public List<AppDocument> getResults(String accountKey, List<String> queries) throws IOException {
		
		String searchText = MainClass.listToKeyWords(queries);
        searchText = searchText.replaceAll(" ", "%20");
		
		List<AppDocument> docs = new ArrayList<AppDocument>();
		String query=BING_URL + "?Query=%27" + searchText + "%27&" + EXTRA_PARAMS;
		
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		System.out.println("Bing web query: " + query);
		URL url = new URL(query);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
		final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));		
		try 
		{
            String inputLine;
            final StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            final JSONObject json = new JSONObject(response.toString());
            final JSONObject d = json.getJSONObject("d");
            final JSONArray results = d.getJSONArray("results");
            final int resultsLength = results.length();
            for (int i = 0; i < resultsLength; i++) {
                final JSONObject aResult = results.getJSONObject(i);
                
                AppDocument doc = new AppDocument();
                doc.setUrl((String) aResult.get("Url"));
                doc.setTitle((String) aResult.get("Title"));
                doc.setDescription((String) aResult.get("Description"));
                
                docs.add(doc);
            }
        }
		catch(Exception ex) {
			// Catch exception
			System.out.println("Sorry. An unexpected error occurred while sending request to Bing");
		}
		
		return docs;
	}
	
}
