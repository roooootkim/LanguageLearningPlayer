package llplayer;

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Sami {
	String filePath;
	ArrayList<Script> scriptList;
	
	class Script{
		int sync_start;
		String script;
		public Script(int start, String text) {
			sync_start = start;
			script = convertToScript(text);
		}
		
		private String convertToScript(String text) {
			text = text.replaceAll("<br>", "\n");
			int i, j;
			while((i = text.indexOf('<')) != -1) {
				j = text.indexOf('>');
				if(j != text.length() - 1)
					text = text.substring(0, i) + text.substring(j + 1);
				else text = text.substring(0, i);
			}
			return text;
		}
	}
	
	public Sami(String path) throws IOException {
        filePath = path;
        scriptList = new ArrayList<Script>();
		parse();
	}
	
	public void parse() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        
        while((line = br.readLine()) != null) {
        	if(line.equals("<BODY>")) break;
        }
        
        int index, start = 0;
        String text ="";
        while((line = br.readLine()) != null) {
        	if(line.equals("</BODY>")) {
        		if(!text.isEmpty()) 
            		scriptList.add(new Script(start, text));
        		break;
        	}
        	
        	if(line.indexOf("<SYNC") == 0) {
            	scriptList.add(new Script(start, text));
            	text = "";
        		
            	index = line.indexOf('>');
            	start = Integer.parseInt(line.substring(12, index));
            	if(line.indexOf("&nbsp") != -1) {
            		text = " ";
            		scriptList.add(new Script(start, text));
            		text = "";
            	}
        	}
        	else text = text.concat(line);
        }
        br.close();
        
        /* print all scripts for test
        for(int i = 0; i < scriptList.size(); i++) {
        	System.out.println(scriptList.get(i).sync_start);
        	System.out.println(scriptList.get(i).script);
        }
        */
	}
}