package de.hpi.nlp.model;

import java.util.Map;

public class BayesClassifier {
	
	/**
	 * Will do text classification for a given test corpus. The test corpus is required to be 
	 * correctly annotated with the right classes/categories per article.
	 * Returns the overall classification precision.
	 */
	public static double determine(BayesModel model, OhsumedCorpus corpus) {
		Map<String, Map<String, Map<String, Integer>>> categoryBigrams = model.getCategoryBigrams();
		
		int correct = 0;
		int incorrect = 0;
		
		for(OhsumedArticle article : corpus) {
			String correctCategory = article.getCategory();
			
			if (!categoryBigrams.containsKey(article.getCategory())) {
				System.err.println("Category did not exist in training data! Cannot proceed.");
				System.exit(-1);
			}

			String determinedCategory = determineBestCategory(article, model);
			
			if (correctCategory.equals(determinedCategory)) {
				correct++;
			} else {
				incorrect++;
			}
		}
		
		return (double) correct / (correct + incorrect);
	}
	
	// TODO: precision, recall, f-measure per class
	
	private static String determineBestCategory(OhsumedArticle article, BayesModel model) {
		Map<String, Map<String, Map<String, Integer>>> categoryBigrams = model.getCategoryBigrams();
		
		String bestCategory = null;
		double maxProbability = Double.NEGATIVE_INFINITY;
		
		for(String categoryName : categoryBigrams.keySet()) {
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
	private static double determinePriorClassProbability(BayesModel model, String categoryName) {
		//System.out.println("catName: " + categoryName);
		long numAllClassInstances = model.getNumArticles();
		long numInstancesInClass = model.getNumArticlesPerCategory(categoryName);
		
		return (double) numInstancesInClass / numAllClassInstances;
	}
	
	/**
	 * Return probability that a given article belongs to a given class.
	 */
	private static double determineArticleInClassLogProbability(BayesModel model, OhsumedArticle article, String categoryName) {
		Map<String, Map<String, Map<String, Integer>>> categoryBigrams = model.getCategoryBigrams();
		Map<String, Map<String, Integer>> bigrams = categoryBigrams.get(categoryName);
		Map<String, Map<String, Integer>> categoryTermFrequency = model.getCategoryTermFrequency();
		Map<String, Integer> termFrequency = categoryTermFrequency.get(categoryName);
		
		double articleProbability = 0.0;
		
		for(Sentence s : article) {
			String prevToken = "";
			String currToken = "";
			double sentenceProbability = 0.0;
			
			for(Token tok : s) {
				currToken = tok.getText();
				
				// double wordProbability = (double) (occurrenceCount + 1.0) / (numInstancesInClass + vocabSize);
				if (prevToken.equals("")) {
					prevToken = BayesModel.BEGINNING_OF_SENTENCE;
					sentenceProbability += Math.log(tokenProbabilityWithoutPreviousToken(currToken, bigrams, termFrequency));
				} else {
					sentenceProbability += Math.log(tokenProbabilityWithPreviousToken(prevToken, currToken, bigrams, termFrequency));
				}
				prevToken = currToken;
				
			}
			//System.out.println(sentenceProbability);
			articleProbability += sentenceProbability;
		}
		return articleProbability;
	}
	
	public static double tokenProbabilityWithoutPreviousToken(String currToken, Map<String, Map<String, Integer>> bigrams, Map<String, Integer> termFrequency) {
		int prevTokenOccurrences = termFrequency.get(BayesModel.BEGINNING_OF_SENTENCE);
		int currTokenOccurrences = 0;
		int vocabSize = termFrequency.size();
		
		if (termFrequency.containsKey(currToken)) {
			// if contained get count, if not leave it 0
			currTokenOccurrences = termFrequency.get(currToken);
		}
		
		return (currTokenOccurrences + 1.0) / (prevTokenOccurrences + vocabSize);
	}
	
	public static double tokenProbabilityWithPreviousToken(String prevToken, String currToken, Map<String, Map<String, Integer>> bigrams, Map<String, Integer> termFrequency) {
		int prevTokenOccurrences = 0;
		int bigramOccurrences = 0;
		int vocabSize = termFrequency.size();
		
		if (!bigrams.containsKey(prevToken)) {
			// probability = (0.0 + 1.0) / (0.0 + vocabSize);
			bigramOccurrences = 0;
			prevTokenOccurrences = 0;
		} else {
			if (!bigrams.get(prevToken).containsKey(currToken)) {
				// probability = (0.0 + 1.0) / (prevTokenOccs + vocabSize);
				bigramOccurrences = 0;
				prevTokenOccurrences = termFrequency.get(prevToken);
			}
			else {
				// probability = (bigramOccs + 1.0) / (prevTokenOccs + vocabSize);
				bigramOccurrences = bigrams.get(prevToken).get(currToken);
				prevTokenOccurrences = termFrequency.get(prevToken);
			}
		}
		return (bigramOccurrences + 1.0) / (prevTokenOccurrences + vocabSize);
	}
	
}
