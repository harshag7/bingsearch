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

public class BingSearch {

	public static String BING_URL = "https://api.datamarket.azure.com/Bing/Search/Web";
	public static String EXTRA_PARAMS = "$top=10&$format=JSON";
	
	public List<AppDocument> getResults(String accountKey, List<String> queries) throws IOException {
		
		String searchText = getQueryString(queries);
        searchText = searchText.replaceAll(" ", "%20");
		
		List<AppDocument> docs = new ArrayList<>();
		String query=BING_URL + "?%27" + searchText + "%27&" + EXTRA_PARAMS;
		
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(query);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
				
		try (final BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
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
                
           /*     System.out.println("Result " + (i+1));
                System.out.println("[");
                System.out.println("\tURL: " + aResult.get("Url"));
                System.out.println("\tTitle: " + aResult.get("Title"));
                System.out.println("\tSummary: " + aResult.get("Description"));
                System.out.println("]");*/
                docs.add(doc);
            }
        }
		
		
		return docs;
	}
	
	private String getQueryString(List<String> queries) {
		String searchText = "";
		for(String query : queries) {
			searchText = query + " ";
		}
		searchText = searchText.trim();
		return searchText;
	}
	
}
