package main;

import java.util.ArrayList;
import java.util.List;

public class GenTrainingData {
	private static final String TRAIN_FILE = "data/train_0.txt";
	private static final String TEST_FILE = "data/test_0.txt";
	
	public static void main(String[] args) {
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
	
		System.out.println("Done.");
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
		
		cpsList.add(sequence.get(pos).getText());
		
		return cpsList;
	}
}
