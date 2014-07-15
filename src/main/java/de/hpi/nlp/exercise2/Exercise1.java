package de.hpi.nlp.exercise2;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.hpi.nlp.model.NaiveBayesClassifier;
import de.hpi.nlp.model.NaiveBayesModel;
import de.hpi.nlp.model.OhsumedCorpus;
import de.hpi.nlp.preparation.OhsumedParser;

public class Exercise1 {
	public static void main(String[] args) throws URISyntaxException, IOException {
		String trainingDir = "/OhsumedCorpus/training/";
		String testDir = "/OhsumedCorpus/test/";
		OhsumedCorpus training = OhsumedParser.parse(new File(Exercise1.class.getResource(trainingDir).toURI()));
		OhsumedCorpus test = OhsumedParser.parse(new File(Exercise1.class.getResource(testDir).toURI()));
		
		NaiveBayesModel model = new NaiveBayesModel(training);
		double precision = NaiveBayesClassifier.determine(model, test);
		System.out.println("Overall Classification Precision: " + precision);
	}
}
