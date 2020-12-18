/*
 * Dictionary.java
 * 사전 기능을 구현한 클래스
 * 작성자 김범철
 * 
 * 메소드 목록
 * setKey(String id, String key): API id와 key값을 받아서 멤버변수로 저장.
 * search(String word): api url을 사용하여 검색 후, JSON파일을 파싱하여 결과값을 반환.
 * buildURL(final  String word): api에 요청하기 위한 url을 반환
 * getRequest(String link): api key와 url을 합쳐서 최종적으로 request할 url을 반환.
 */
package llplayer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import  javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import  java.io.InputStreamReader;
import  java.net.URL;

public class Dictionary{
    private static String app_id ;
    private static String app_key;
    
	protected Dictionary(){
	}
	
	public void setKey(String id, String key) {
		app_id = id;
		app_key = key;
	}
	
	public static String search(String word){
		JSONParser parser = new JSONParser();
		String lexCat = new String();
		String def = new String();
	    try{
	        final String url = getRequest(buildURL(word));
	        final JSONObject jsonObj = (JSONObject)parser.parse(url);
	        
	        JSONArray results = (JSONArray) jsonObj.get("results");
	        for(int i = 0; i < results.size(); i++) {
		        JSONArray lexicalEntries = (JSONArray) ((JSONObject)results.get(i)).get("lexicalEntries");
		        if(lexicalEntries == null) continue;
	        	for(int j = 0; j < lexicalEntries.size(); j++) {
	        		//lexcialCategory를 출력
	        		JSONObject lexicalCategory = (JSONObject)((JSONObject)lexicalEntries.get(j)).get("lexicalCategory");
			        JSONArray entries = (JSONArray) ((JSONObject)lexicalEntries.get(j)).get("entries");
			        if(entries == null) continue;
	        		if(lexicalCategory != null)
	        			lexCat = lexicalCategory.get("text").toString();
	        		for(int k = 0; k < entries.size(); k++) {
				        JSONArray senses = (JSONArray) ((JSONObject)entries.get(k)).get("senses");
				        if(senses == null) continue;
				        for(int l = 0; l < senses.size(); l++) {
					        JSONArray definitions = (JSONArray) ((JSONObject)senses.get(l)).get("definitions");
					        if(definitions == null) continue;
					        
					        //한 개만 출력
		        			def = definitions.get(0).toString();
					        i = results.size() + 1;
					        j = lexicalEntries.size() + 1;
					        k = entries.size() + 1;
					        l = senses.size() + 1;
					        
					        /*전부 출력
					        for(int m = 0; m < definitions.size(); m++) {
				                System.out.println(definitions.get(m).toString());
					        }
					        */
				        }
	        		}
	        	}
	        }
	        
	    }
	    catch (Exception e){
	        return "no result (or check your API ID & Key";
	    }
	    return lexCat + '\n' + def;
	}
	
	private static String buildURL(final  String word){
		final String language="en-gb";
	    final String word_id=word.toLowerCase();
	    return "https://od-api.oxforddictionaries.com:443/api/v2/entries/" + language + "/" + word_id;
	}
	
	private static String getRequest(String link){
	    try{
	    	URL url = new URL(link);
		    HttpsURLConnection urlConnection=(HttpsURLConnection) url.openConnection();
		    urlConnection.setRequestProperty("Accept", "application/json");
		    urlConnection.setRequestProperty("app_id", app_id);
		    urlConnection.setRequestProperty("app_key", app_key);
		    // read the output from the server
		    BufferedReader reader=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		    StringBuilder stringBuilder=new StringBuilder();
		    String line =null;
		    while ((line=reader.readLine()) !=null){
		    	stringBuilder.append(line + "\n");
		    }
	    	return stringBuilder.toString();
	    }
	    catch (Exception e){
	    	//e.printStackTrace();
	    	return e.toString();
	    }
	}
}