package de.hpi.nlp.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NaiveBayesClassifier {
	
	/**
	 * Will do text classification for a given test corpus. The test corpus is required to be 
	 * correctly annotated with the right classes/categories per article.
	 * Returns the overall classification precision.
	 */
	public static double determine(NaiveBayesModel model, OhsumedCorpus corpus) {
		Map<String, Map<String, Integer>> categoryUnigrams = model.getCategoryUnigrams();
		int correct = 0;
		int incorrect = 0;
		
		// categoryName -> [ # correct, # incorrect]
		Map<String, Integer[]> measures = new HashMap<>();
		Map<String, Integer> falsePositivesPerClass = new HashMap<>();
		
		for(OhsumedArticle article : corpus) {
			String correctCategory = article.getCategory();

			if (!categoryUnigrams.containsKey(article.getCategory())) {
				System.err.println("Category did not exist in training data! Cannot proceed.");
				System.exit(-1);
			}

			String determinedCategory = determineBestCategory(article, model);
			
			if (!measures.containsKey(correctCategory)) {
				measures.put(correctCategory, new Integer[2]);
				// init with 0
				measures.get(correctCategory)[0] = 0;
				measures.get(correctCategory)[1] = 0;
			}
			Integer[] evalCount = measures.get(correctCategory);
			
			if (correctCategory.equals(determinedCategory)) {
				evalCount[0] += 1;
				correct++;
			} else {
				// remember the false positive (some article has been assigned the wrong category)
				if (!falsePositivesPerClass.containsKey(determinedCategory)) {
					falsePositivesPerClass.put(determinedCategory, 0);
				}
				falsePositivesPerClass.put(determinedCategory, falsePositivesPerClass.get(determinedCategory) + 1);
				evalCount[1] += 1;
				incorrect++;
			}
		}
		
		// evaluation per class
		List<String> nameList = new ArrayList<String>(measures.keySet());
		Collections.sort(nameList);
		for(String categoryName : nameList) {
			Integer[] evalCount = measures.get(categoryName);
			NumberFormat formatter = new DecimalFormat("#0.00");
			
			double precision = (double) evalCount[0] / (evalCount[0] + falsePositivesPerClass.get(categoryName)); // tp / tp + fp
			double recall = (double) evalCount[0] / (evalCount[0] + evalCount[1]); // tp / tp + fn
			double fMeasure = (2.0 * precision * recall) / (precision + recall);
			
			System.out.println("Class: " + categoryName 
					+ " | Precision: " + formatter.format(precision) 
					+ "; Recall: " + formatter.format(recall)
					+ "; F-Measure: " + formatter.format(fMeasure));
		}
		return (double) correct / (correct + incorrect);
	}
	
	private static String determineBestCategory(OhsumedArticle article, NaiveBayesModel model) {
		Map<String, Map<String, Integer>> categoryUnigrams = model.getCategoryUnigrams();
		String bestCategory = null;
		double maxProbability = Double.NEGATIVE_INFINITY;
		
		for(String categoryName : categoryUnigrams.keySet()) {
			double prior = determinePriorClassProbability(model, categoryName);
			double articleInClass = determineArticleInClassLogProbability(model, article, categoryName);
			
			double classProbability = Math.log(prior) + articleInClass;
			
			// argmax { log(prior) + log(wordInClass) }
			if (classProbability > maxProbability) {
				maxProbability = classProbability;
				bestCategory = categoryName;
			}
		}
		return bestCategory;
	}
	
	/**
	 * Return prior probability, which describes how likely
	 * it is for any document to be in this class.
	 */
	private static double determinePriorClassProbability(NaiveBayesModel model, String categoryName) {
		//System.out.println("catName: " + categoryName);
		long numAllClassInstances = model.getNumArticles();
		long numInstancesInClass = model.getNumArticlesPerCategory(categoryName);
		
		return (double) numInstancesInClass / numAllClassInstances;
	}
	
	/**
	 * Return probability that a given article belongs to a given class.
	 */
	private static double determineArticleInClassLogProbability(NaiveBayesModel model, OhsumedArticle article, String categoryName) {	
		Map<String, Map<String, Integer>> categoryUnigrams = model.getCategoryUnigrams();
		Map<String, Integer> unigrams = categoryUnigrams.get(categoryName);
		Integer numWordsInClass = model.getTokensPerCategory().get(categoryName);
		int vocabSize = unigrams.size();
		double articleProbability = 0.0;
		
		for(Sentence s : article) {
			for(Token tok : s) {
				
				String token = tok.getText();
				
				int occurrenceCount = 0;
				if (unigrams.containsKey(token)) {
					occurrenceCount = unigrams.get(token);
				} 
				
				double wordProbability = (double) (occurrenceCount + 1.0) / (numWordsInClass + vocabSize);
				articleProbability += Math.log(wordProbability);
			}
		}
		return articleProbability;
	}
	
	/**
	 * The bigger the better. The bigger the number, the more important (less frequent) the word is,
	 * considering the whole collection of documents.
	 */
	private static double getIDF(NaiveBayesModel model, String token) {
		Map<String, Set<String>> termToDocuments = model.getOverallTermOccurrences();
		long numDocuments = model.getNumArticles();
		int numDocumentsContainingToken = 0;
		
		if (termToDocuments.containsKey(token)) {
			numDocumentsContainingToken = termToDocuments.get(token).size();
		}
		
		return Math.log((double) numDocuments / numDocumentsContainingToken);
	}
}
