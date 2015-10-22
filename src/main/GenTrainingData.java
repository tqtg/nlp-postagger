package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author TuanTQ
 *
 *
 * @Featues
 * 1: 1-gram, window size = 5
 * 2: 2-gram, window size = 5
 * 3: pos tags dictionary, window size = 3
 * 4: current word is all number
 * 5: current word has number
 * 6: current word has hyphen '-'
 * 7: current word has slash '/'
 * 8: current word has colon ':'
 * 9: current word is initial capitalized
 * 10: current word is all capitalized
 * 11: current word is punctuation mark
 * 12: current word is full repetitive word
 * 13: current word is rhythm repetitive word
 * 14: prefix
 * 15: suffix
 * 16: all characters of current word is none lowercase
 * 17: current word is in middle of sentence and has uppercase character
 * 18: current word contains number and hyphen
 * 19: number of syllables of current word
 * 20: number of characters of current word
 * 21: all syllables of current word are capitalized
 * 22: current word contains only uppercase characters and number
 * 
 */


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
	
	public static void main(String[] args) {
		List<String> trainFile = Utils.readFile(args[0]);
		List<Sequence> trainSequences = new ArrayList<>();

		for (String line : trainFile) {
			trainSequences.add(text2Sequence(line));
		}

		trainFile.clear();

		for (Sequence sequence : trainSequences) {
			trainFile.addAll(genSequenceFeatures(sequence));
			trainFile.add("");
		}

		Utils.writeFile(trainFile, args[1]);

		System.out.println("Generate training file completely!");
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
			String cp = "1:w:";

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

		// 2-gram, window size = 5
		if (pos - 2 >= 0){
			String cp = "2:pp:" + sequence.get(pos - 2).getText() + ":" + prevWord;
			cpsList.add(cp);
		}

		if (pos - 1 >= 0){
			String cp = "2:pc:" + prevWord + ":" + curWord;
			cpsList.add(cp);
		}

		if (pos + 1 < sequence.size()){
			String cp = "2:cn:" + curWord + ":" + nextWord;
			cpsList.add(cp);
		}

		if (pos + 2 < sequence.size()){
			String cp = "2:nn:" + nextWord + ":" + sequence.get(pos + 2).getText();
			cpsList.add(cp);
		}

		// pos tag dict
//		if (word2dictags.containsKey(prevWord)) {
//			List tags = (List) word2dictags.get(prevWord);
//
//			for (int i = 0; i < tags.size(); ++i){
//				cpsList.add("3:dic:-1:" + tags.get(i));
//			}
//		}

		if (word2dictags.containsKey(curWord)) {
			List tags = (List) word2dictags.get(curWord);

			for (int i = 0; i < tags.size(); ++i){
				cpsList.add("3:dic:" + tags.get(i));
			}
		}

//		if (word2dictags.containsKey(nextWord)) {
//			List tags = (List) word2dictags.get(nextWord);
//
//			for (int i = 0; i < tags.size(); ++i){
//				cpsList.add("3:dic:1:" + tags.get(i));
//			}
//		}

		//has number, all number
		boolean isAllNumber = true, hasNumber = false;
		for (int i = 0; i < curWord.length(); ++i){
			char c = curWord.charAt(i);
			if (c == '_') continue;

			if (Character.isDigit(c)){
				if (!hasNumber) hasNumber = true;
			} else {
				if (isAllNumber) isAllNumber = false;
			}
		}
		
		if (isAllNumber)
			cpsList.add("4:an");
		
		if (!isAllNumber && hasNumber)
			cpsList.add("5:hn");

		//has hyphen
		if (curWord.contains("-"))
			cpsList.add("6:hy");

		
		//has slash
		if (curWord.contains("/"))
			cpsList.add("7:sl");
		
		//has comma
		if (curWord.contains(":"))
			cpsList.add("8:co");
		
		//all cap and initial cap
		boolean isAllCap = true, hasUpperCase = false;
		boolean noneLowercase = true;

		
		for (int i = 0 ; i < curWord.length(); ++i){
			if (curWord.charAt(i) == '_' || curWord.charAt(i) == '.') continue;
			
			if (!Character.isUpperCase(curWord.charAt(i))){
				isAllCap = false;
			} else {
				hasUpperCase = true;
			}
			
			if (Character.isLowerCase(curWord.charAt(i))) {
				noneLowercase = false;
			}
		}
		
		if (!isAllCap && Character.isUpperCase(curWord.charAt(0)))
			cpsList.add("9:ic");
		
		if (isAllCap)
			cpsList.add("10:ac");
		
		//is mark context
		String [] marks = {".", "...", "?", "!", "(", ")", ":", "-", "/", "\"", ","};
		boolean isMark = false;
		
		for (int i = 0; i < marks.length; ++i){
			if (marks[i].equalsIgnoreCase(curWord)){
				isMark = true;
				break;
			}
		}
		
		if (isMark)
			cpsList.add("11:mk");	
		
		// REPRETATIVE CONTEXT && PREFIX && SUFFIX
		String [] sylls = curWord.split("_");
		if (sylls.length == 2){ //consider 2-syllable words
			VnSyllParser parser1 = new VnSyllParser(sylls[0]);
			VnSyllParser parser2 = new VnSyllParser(sylls[1]);
			
			if (parser1.isValidVnSyllable() && parser2.isValidVnSyllable()){
				if (parser1.getNonToneSyll().equalsIgnoreCase(parser2.getNonToneSyll())){
					cpsList.add("12:fr");
				}
				else if (parser1.getRhyme().equalsIgnoreCase(parser2.getRhyme())){
					cpsList.add("13:rr");
				}
			}
		}
		
		boolean allSyllCap = false;
		if (sylls.length >= 2) {
			allSyllCap = true;
			
			cpsList.add("14:pf:" + sylls[0]);
			cpsList.add("15:sf:" + sylls[sylls.length - 1]);
			
			for (int i = 0; i < sylls.length; i++) {
				if (sylls[i].length() > 0 && !Character.isUpperCase(sylls[i].charAt(0))) {
					allSyllCap = false;
					break;
				}
			}
		}
		
		// none lowercase
		if (noneLowercase)
			cpsList.add("16:nl");
		
		// mid-sentence word
		if (0 < pos && pos < sequence.size() - 1 && hasUpperCase)
			cpsList.add("17:ms");
		
		// number and hyphen
		if (hasNumber && curWord.contains("-"))
			cpsList.add("18:nh");
		
		// number of syllables
		cpsList.add("19:ns:" + sylls.length);
		
		// number of characters
		cpsList.add("20:nc:" + curWord.length());
		
		if (allSyllCap)
			cpsList.add("21:asc");
		
		if (curWord.matches("[A-Z]+(_)?(\\d)*$"))
			cpsList.add("22:un");
			
		return cpsList;
	}
}
