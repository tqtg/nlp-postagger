package main;

import java.util.ArrayList;
import java.util.List;

import jmdn.base.util.string.StrUtil;
import jmdn.method.classification.maxent.Classification;
import test.TestMaxEnt;

public class POSTagClassifier {
	private Classification classifier = null;
	
	public POSTagClassifier(String modelDirectory) {
		this.classifier = new Classification(modelDirectory);
		classifier.init();
	}
	
	public void init() {
		if (!this.classifier.isInitialized()) {
			this.classifier.init();
		}
	}

	public String doPOSTagClassification(String sentence) {
		List<String> taggedSentence = new ArrayList<>();
		
		main.Sequence sequence = GenTrainingData.text2Sequence(sentence);
		List<String> cpsList = GenTrainingData.genSequenceFeatures(sequence);
		TestMaxEnt.nTokens += cpsList.size();
		for (int i = 0; i < cpsList.size(); i++) {
			List<String> cps = StrUtil.tokenizeString(cpsList.get(i));
			cps.remove(cps.size() - 1);
			String tag = classifier.classify(StrUtil.join(cps));
			taggedSentence.add(sequence.get(i).getText() + "/" + tag);
		}
		
		return StrUtil.join(taggedSentence);
	}
	public static void main(String[] args){
		POSTagClassifier cl = new POSTagClassifier("data");
		cl.init();
		System.out.println(cl.doPOSTagClassification("Cuộc_đời/N dưới/E vành/N mũ/N thám_tử/N ./CH"));
	}
	
}
