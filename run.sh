#!/bin/bash

mode=$1
input=$2
output=$3

function print_help {
	echo "Usage:"
	echo "    train : train model with jmdn"
	echo "    gen input output : scan features for given file (train/test)"
}

function train {
	java -classpath lib/args4j-2.0.6.jar:lib/jmdn-base.jar:lib/jmdn-methods.jar jmdn.method.classification.maxent.Trainer -all -d data
}

function gen_training_data {
	rm $output
	java -classpath nlp-postagger.jar main.GenTrainingData ${input} ${output}
}

if [ "$#" -eq 0 ]; then
	print_help
elif [ "$mode" = "train" ]; then
	train
elif [ "$mode" = "gen" -a "$#" -eq 3 ]; then
	gen_training_data
else
	print_help
fi