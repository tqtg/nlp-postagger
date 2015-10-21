package main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GenTrainingData {
	private static final String TRAIN_FILE = "data/train_0.txt";
	private static final String TEST_FILE = "data/test_0.txt";
	private static final String DEFAULT_E_DICT = "data/ComputerDict.txt";
	
	private static Map word2dictags = new HashMap<String, List>();
	
	static {
		readDict();
	}
	
	public static boolean readDict(){
		try {
			List<String> lines = Utils.readFile(DEFAULT_E_DICT);
			
			word2dictags.clear();
						
			String temp = null;
			for (String line : lines ){
				String [] tokens = line.split("\t");
		
				String word, tag;
				if (tokens == null)
					continue;
				
				if (tokens.length != 2){
					continue;					
				}
				else if (tokens.length == 2){
					if (tokens[0].equals("")){
						if (temp == null)
							continue;
						else {
							//System.out.println(temp);
							word = temp;
							tag = tokens[1];
						}
					}
					else{ 
						word = tokens[0].trim().toLowerCase();
						tag = tokens[1].trim();
						temp = word;
					}
				}
				else continue;
				
				word = word.replace(" ","_");
				List dictags = (List) word2dictags.get(word);
				if (dictags == null){
					dictags = new ArrayList<String>();
				}
				dictags.add(tag);
				word2dictags.put(word, dictags);
			}
			
			System.out.println("Dictionary reading done!");
			System.out.println("Dictionary size: " + word2dictags.size());
			return true;
		}
		
		catch (Exception e){
			return false;
		}
	}
	
	public static void genTrainTest() {
		List<String> trainFile = Utils.readFile(TRAIN_FILE);
		List<String> testFile = Utils.readFile(TEST_FILE);
		
		List<Sequence> trainSequences = new ArrayList<>();
		List<Sequence> testSequences = new ArrayList<>();
		
		for (String line : trainFile) {
			trainSequences.add(text2Sequence(line));
		}
		
		for (String line : testFile) {
			testSequences.add(text2Sequence(line));
		}
		
		trainFile.clear();
		testFile.clear();
		
		for (Sequence sequence : trainSequences) {
			trainFile.addAll(genSequenceFeatures(sequence));
			trainFile.add("");
		}
		
		for (Sequence sequence : testSequences) {
			testFile.addAll(genSequenceFeatures(sequence));
			testFile.add("");
		}
		
		Utils.writeFile(trainFile, "data/train.txt");
		Utils.writeFile(testFile, "data/test.txt");
	
		System.out.println("Generate training and test file completely!");
	}
	
	public static Sequence text2Sequence(String line) {
		Sequence sequence = new Sequence();
		
		String[] tokens = line.split(" ");
		for (String token : tokens) {
			int index = token.lastIndexOf("/");
			if (index < 0) {
				continue;
			} else {
				Observation o = new Observation();
				o.setText(token.substring(0, index));
				o.setTag(token.substring(index + 1));
				sequence.add(o);
			}
		}
		
		return sequence;
	}
	
	public static List<String> genSequenceFeatures(Sequence sequence) {
		List<String> cpsListOfSequence = new ArrayList<>();
		
		for (int i = 0; i < sequence.size(); i++) {
			List<String> cpsList = new ArrayList<>();
			Observation o = sequence.get(i);
			cpsList.addAll(genFeatures(sequence, i));			
			cpsList.add(o.getTag());
			cpsListOfSequence.add(Utils.join(cpsList));
		}
		
		return cpsListOfSequence;
	}
	
	public static List<String> genFeatures(Sequence sequence, int pos) {
		List<String> cpsList = new ArrayList<>();
		String curWord = sequence.get(pos).getText();
		String prevWord = null; 
		String nextWord = null;
		if (pos >= 1) prevWord = sequence.get(pos - 1).getText();
		if (pos < sequence.size() - 1) nextWord = sequence.get(pos + 1).getText();
		
		// 1-gram, window size = 5
		for (int i = -2; i <= 2; i++) {
			String cp = "w:";
			
			if (pos + i == sequence.size()) {
				cp += i + ":" + "ES";
			} else if (pos + i == -1) {
				cp += i + ":" + "BS";
			} else if (0 <= pos + i && pos + i < sequence.size()) {
				cp += i + ":" + sequence.get(pos + i).getText();
			} else {
				cp = "";
			}
			
			if (!cp.equals("")) cpsList.add(cp);
		}
		
		// 2-gram, window size = 3
		if (pos - 2 >= 0){
			String cp = "ww:-2-1:" + sequence.get(pos - 2).getText() + ":" + prevWord;
			cpsList.add(cp);
		}
		
		if (pos - 1 >= 0){
			String cp = "ww:-10:" + prevWord + ":" + curWord;
			cpsList.add(cp);
		}
		
		if (pos + 1 < sequence.size()){
			String cp = "ww:01:" + curWord + ":" + nextWord;
			cpsList.add(cp);
		}
		
		if (pos + 2 < sequence.size()){
			String cp = "ww:12:" + nextWord + ":" + sequence.get(pos + 2).getText();
			cpsList.add(cp);
		}
		
		// pos tag dict
		if (word2dictags.containsKey(prevWord)) {
			List tags = (List) word2dictags.get(prevWord);
			
			for (int i = 0; i < tags.size(); ++i){
				cpsList.add("dict:-1:" + tags.get(i));
			}
		}
		
		if (word2dictags.containsKey(curWord)) {
			List tags = (List) word2dictags.get(curWord);
			
			for (int i = 0; i < tags.size(); ++i){
				cpsList.add("dict:0:" + tags.get(i));
			}
		}
		
		if (word2dictags.containsKey(nextWord)) {
			List tags = (List) word2dictags.get(nextWord);
			
			for (int i = 0; i < tags.size(); ++i){
				cpsList.add("dict:1:" + tags.get(i));
			}
		}
		
		return cpsList;
	}
}
