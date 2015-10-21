package main;

import cc.mallet.fst.CRF;
import cc.mallet.fst.CRFOptimizableByLabelLikelihood;
import cc.mallet.fst.CRFTrainerByValueGradients;
import cc.mallet.fst.CRFWriter;
import cc.mallet.fst.MultiSegmentationEvaluator;
import cc.mallet.fst.TransducerEvaluator;
import cc.mallet.fst.TransducerTrainer;
import cc.mallet.optimize.Optimizable;
import cc.mallet.types.InstanceList;

public class Trainner {
	public void run (InstanceList trainingData, InstanceList testingData) {
		// setup:
		//    CRF (model) and the state machine
		//    CRFOptimizableBy* objects (terms in the objective function)
		//    CRF trainer
		//    evaluator and writer

		// model
		CRF crf = new CRF(trainingData.getDataAlphabet(),
				trainingData.getTargetAlphabet());
		// construct the finite state machine
		crf.addFullyConnectedStatesForLabels();
		// initialize model's weights
		crf.setWeightsDimensionAsIn(trainingData, false);
		
		//  CRFOptimizableBy* objects (terms in the objective function)
		// objective 1: label likelihood objective
		CRFOptimizableByLabelLikelihood optLabel =
				new CRFOptimizableByLabelLikelihood(crf, trainingData);

		// CRF trainer
		Optimizable.ByGradientValue[] opts =
				new Optimizable.ByGradientValue[]{optLabel};
		// by default, use L-BFGS as the optimizer
		CRFTrainerByValueGradients crfTrainer =
				new CRFTrainerByValueGradients(crf, opts);

		// *Note*: labels can also be obtained from the target alphabet
		String[] labels = new String[]{ "N", "M", "CH", "V", "Np", "Cc", "R",
										"A", "C", "P", "E", "Ny", "Nu", "X",
										"L", "Nc", "Nb", "T", "I", "Xy", "Z",
										"Vb", "B", "Ni", "Y", "Vy", "Ab" };
		TransducerEvaluator evaluator = new MultiSegmentationEvaluator(
				new InstanceList[]{trainingData, testingData},
				new String[]{"train", "test"}, labels, labels) {
			@Override
			public boolean precondition(TransducerTrainer tt) {
				// evaluate model every 5 training iterations
				return tt.getIteration() % 5 == 0;
			}
		};
		crfTrainer.addEvaluator(evaluator);

		CRFWriter crfWriter = new CRFWriter("postag.model") {
			@Override
			public boolean precondition(TransducerTrainer tt) {
				// save the trained model after training finishes
				return tt.getIteration() % Integer.MAX_VALUE == 0;
			}
		};
		crfTrainer.addEvaluator(crfWriter);

		// all setup done, train until convergence
		crfTrainer.setMaxResets(0);
		crfTrainer.train(trainingData, Integer.MAX_VALUE);
		// evaluate
		evaluator.evaluate(crfTrainer);

		// save the trained model (if CRFWriter is not used)
		// FileOutputStream fos = new FileOutputStream("ner_crf.model");
		// ObjectOutputStream oos = new ObjectOutputStream(fos);
		// oos.writeObject(crf);
	}
}
