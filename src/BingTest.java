import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

//Download and add this library to the build path.
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class BingTest {

	public static void main(String[] args) throws IOException {
		String bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%27gates%27&$top=10&$format=JSON";
		//Provide your account key here. 
		String accountKey = "zwdDE6X7sdfQJFaccidYZ9xFhXssOH+buQuMn2Owv9g";
		
		byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(bingUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
				
	/*	InputStream inputStream = (InputStream) urlConnection.getContent();		
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);
		String content = new String(contentRaw);*/

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
                System.out.println("Result " + (i+1));
                System.out.println("[");
                System.out.println("\tURL: " + aResult.get("Url"));
                System.out.println("\tTitle: " + aResult.get("Title"));
                System.out.println("\tSummary: " + aResult.get("Description"));
                System.out.println("]");
            }
        }
	}

}