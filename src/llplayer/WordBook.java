package llplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class WordBook {
	private ArrayList<String> wordList = new ArrayList<String>();
	
	public WordBook() {
		File file = new File("WordBook.txt");
		if(!file.exists()) return;
		
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			
			while(true) {
				String line = br.readLine();
				if(line == null) break;
				wordList.add(line);
			}
			
			fis.close();
			isr.close();
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	void save() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File("WordBook.txt"));
			
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			
			for(String word : wordList) {
				bw.write(word);
				bw.newLine();
			}
			
			bw.close();
			osw.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void input(String word) {
		if(wordList.indexOf(word) == -1) wordList.add(word);
		else wordList.remove(word);
	}
	
	public ArrayList<String> getList(){
		return wordList;
	}
}
