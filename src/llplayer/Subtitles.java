package llplayer;

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Subtitles {
	String filePath;
	ArrayList<Script> scriptList;
	int curIndex = 0;
	
	class Script{
		long sync_start;
		String script;
		public Script(long start, String text) {
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
	
	public Subtitles(String path) throws IOException {
        filePath = path;
        scriptList = new ArrayList<Script>();
		parse();
	}
	
	public String getScript(long time) {
		if (scriptList.get(curIndex).sync_start <= time && time < scriptList.get(curIndex + 1).sync_start) {
			return scriptList.get(curIndex).script;
		}
		else if(scriptList.get(curIndex + 1).sync_start <= time && time < scriptList.get(curIndex + 2).sync_start) {
			curIndex++;
			return scriptList.get(curIndex + 1).script;
		}
		
		int lo = 1, hi = scriptList.size() - 2;
		int mid = 0;
		
		while(lo <= hi) {
			mid = (lo + hi)/2;
			if(scriptList.get(mid).sync_start <= time) {
				if(time < scriptList.get(mid + 1).sync_start) break;
				lo = mid;
			}
			else {
				if(time >= scriptList.get(mid - 1).sync_start) {
					mid--;
					break;
				}
				hi = mid;
			}
		}
		
		curIndex = mid;
		
		return scriptList.get(mid).script;
	}
	
	public long getPrevTime() {
		if(curIndex < 2) return scriptList.get(curIndex).sync_start;
		else {
			while(curIndex > 0) {
				curIndex--;
				if(!scriptList.get(curIndex).script.isEmpty()) break;
			}
			return scriptList.get(curIndex).sync_start;
		}
	}
	
	public long getNextTime() {
		if(curIndex >= scriptList.size() - 2)return scriptList.get(curIndex).sync_start;
		else {
			while(curIndex < scriptList.size() - 2) {
				curIndex++;
				if(!scriptList.get(curIndex).script.isEmpty()) break;
			}
			return scriptList.get(curIndex).sync_start;
		}
	}
	
	public void parse() throws IOException {
		if(filePath.endsWith("smi")) parseSMI();
		else if(filePath.endsWith("srt")) parseSRT();
	}
	
	public void parseSMI() throws IOException {
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
            		text = "";
            		scriptList.add(new Script(start, text));
            		text = "";
            	}
        	}
        	else text = text.concat(line);
        }
        scriptList.add(new Script(Long.MAX_VALUE, ""));
        br.close();
        
        /* print all scripts for test
        for(int i = 0; i < scriptList.size(); i++) {
        	System.out.println(scriptList.get(i).sync_start);
        	System.out.println(scriptList.get(i).script);
        }
        */
	}
	
	public void parseSRT() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
	    String line;
	    String[] time;
	    long[] start = new long[2];
	    String script;
	    scriptList.add(new Script(0, ""));
	    while((line = br.readLine()) != null) {
	    	if(line.isEmpty() || line.isBlank()) continue;
	    	else {
	    		line = br.readLine();
	    		time = line.split("-->");
	    		for(int i = 0; i < 2; i++) {
	    			start[i] = 0;
	    			time[i] = time[i].trim();
	    			long hour = (time[i].charAt(0) - '0') * 10 + (time[i].charAt(1) - '0');
	    			long min = (time[i].charAt(3) - '0') * 10 + (time[i].charAt(4) - '0');
	    			long second = (time[i].charAt(6) - '0') * 10 + (time[i].charAt(7) - '0');
	    			time[i] = time[i].substring(9);
	    			start[i] += Long.valueOf(time[i]);
	    			min += hour * 60;
	    			second += min * 60;
	    			start[i] += second * 1000;
	    		}
	    		script = "";
	    		
	    		while(true) {
		    		line = br.readLine();
		    		if(!line.isEmpty() && !line.isBlank())
		    			script += line;
		    		else break;
	    		}
	    		
	    		scriptList.add(new Script(start[0], script));
	    		scriptList.add(new Script(start[1], ""));
	    	}
	    }
	    scriptList.add(new Script(Long.MAX_VALUE, ""));
	    br.close();
	}
}