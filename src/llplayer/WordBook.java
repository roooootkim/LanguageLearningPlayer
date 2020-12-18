/*
 * WordBook.java
 * 단어장의 기능을 구현한 Class
 * 
 * 작성자 오기탁
 * WordBook(): 생성자를 통해서 WordBook.txt 백업파일을 불러온다.
 * save(): 단어장리스트를 WordBook.txt파일에 백업.
 * 
 * 작성자 김영현
 * wordList: 단어 목록들을 저장할 리스트 변수
 * input(String word): 입력 단어를 읽어온 후,
 * 단어장에 이미 존재하는 단어라면 삭제, 아니라면 단어장에 새로 추가.
 * ArrayList<String> getList(): 단어 목록을 저장한 리스트의 참초값을 반환.
 */
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
