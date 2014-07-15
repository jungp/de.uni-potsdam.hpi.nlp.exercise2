package de.hpi.nlp.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NaiveBayesModel {
	// each category (or class) is a unigram language model
	private final Map<String, Map<String, Integer>> categoryUnigrams;
	public Map<String, Map<String, Integer>> getCategoryUnigrams() {
		return categoryUnigrams;
	}
	private final Map<String, Integer> tokensPerCategory;
	private final Map<String, Set<String>> overallTermOccurrences; // which term appears in which documents
	public Map<String, Set<String>> getOverallTermOccurrences() {
		return overallTermOccurrences;
	}
	private final Map<String, Long> numArticlesPerCategory;
	private final long numArticles;
	
	public NaiveBayesModel(OhsumedCorpus corpus) {
		Set<String> categories = corpus.getCategories();
		this.categoryUnigrams = new HashMap<>();
		this.numArticles = corpus.getNumArticles();
		this.numArticlesPerCategory = new HashMap<>();
		this.overallTermOccurrences = new HashMap<>();
		this.tokensPerCategory = corpus.getTokensPerCategory();
		
		Iterator<String> it = categories.iterator();
		while (it.hasNext()) {
			String categoryName = it.next();
			numArticlesPerCategory.put(categoryName, corpus.getNumArticlesPerCategory(categoryName)); 
		}
		
		// build bayes model for each category/class there is, so that it can later be used
		// do determine how much a new unseen article matches the classes (and which one matches best)
		Set<String> categoryNames = corpus.getCategories();
		it = categoryNames.iterator();
		while (it.hasNext()) {
			String categoryName = it.next();
			
			// every class has its own unigrams
			if (!categoryUnigrams.containsKey(categoryName)) {
				categoryUnigrams.put(categoryName, new HashMap<String, Integer>());
			}
			Map<String, Integer> categoryLocalUnigrams = categoryUnigrams.get(categoryName);
			
			// iterate over all articles of one category/class
			Iterator<OhsumedArticle> iter = corpus.getArticlesOfCategory(categoryName);
			while (iter.hasNext()) {
				OhsumedArticle article = iter.next();
				for(Sentence sentence : article) {
					for(Token token : sentence) {	
						// feature selection: use only a subset of words (features)
						
						String word = token.getText();
						
						// remember in which document (title as id) a term occurred
						// will later be used for calculation of inverse document frequency
						if (!overallTermOccurrences.containsKey(word)) {
							overallTermOccurrences.put(word, new HashSet<String>());
						}
						Set<String> documentsContainingWord = overallTermOccurrences.get(word);
						documentsContainingWord.add(article.getTitle());
						
						// increase occurrence count of the token
						if (!categoryLocalUnigrams.containsKey(word)) {
							categoryLocalUnigrams.put(word, 1);
						} else {
							categoryLocalUnigrams.put(word, categoryLocalUnigrams.get(word) + 1);
						}	
					}
				}
			}
		}

	}

	public Map<String, Integer> getTokensPerCategory() {
		return tokensPerCategory;
	}

	public long getNumArticles() {
		return numArticles;
	}

	public Long getNumArticlesPerCategory(String categoryName) {
		return numArticlesPerCategory.get(categoryName);
	}
	
}
