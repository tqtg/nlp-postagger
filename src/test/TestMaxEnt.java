package test;

import java.util.List;

import jmdn.base.util.filesystem.FileLoader;
import main.POSTagClassifier;

public class TestMaxEnt {
	private static POSTagClassifier cl = new POSTagClassifier("data");
	public static int nTokens = 0;
	
	public static void main(String[] args) {
		List<String> dataLines = FileLoader.readFile("data/train_0.txt", "UTF8");
		
		long beginTime = System.currentTimeMillis();
		
		for (String line : dataLines) {
			cl.doPOSTagClassification(line);
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Running time: " + String.valueOf(endTime - beginTime) + "ms");
		System.out.println("Total tokens: " + nTokens);
		double speed = nTokens / (double)(endTime - beginTime);
		System.out.println("Speed: " + Math.floor(speed * 1000) + " token/s");
	}
}
