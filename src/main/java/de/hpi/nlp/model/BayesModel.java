package de.hpi.nlp.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BayesModel {
	public static final String BEGINNING_OF_SENTENCE = "<s>";
	// each category (or class) is a bigram language model
	private final Map<String, Map<String, Map<String, Integer>>> categoryBigrams;
	private final Map<String, Map<String, Integer>> categoryTermFrequency;
	private final Map<String, Long> numArticlesPerCategory;
	private final long numArticles;
	
	public BayesModel(OhsumedCorpus corpus) {
		Set<String> categories = corpus.getCategories();
		this.categoryBigrams = new HashMap<>();
		this.numArticles = corpus.getNumArticles();
		this.numArticlesPerCategory = new HashMap<>();
		this.categoryTermFrequency = new HashMap<>();
		
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
			
			// every class has its own bigrams and term frequencies
			if (!categoryBigrams.containsKey(categoryName)) {
				categoryBigrams.put(categoryName, new HashMap<String, Map<String, Integer>>());
			}
			Map<String, Map<String, Integer>> categoryLocalBigrams = categoryBigrams.get(categoryName);
			
			if (!categoryTermFrequency.containsKey(categoryName)) {
				categoryTermFrequency.put(categoryName, new HashMap<String, Integer>());
			}
			Map<String, Integer> categoryLocalTermFrequency = categoryTermFrequency.get(categoryName);
			
			// iterate over all articles of one category/class
			Iterator<OhsumedArticle> iter = corpus.getArticlesOfCategory(categoryName);
			while (iter.hasNext()) {
				OhsumedArticle article = iter.next();
				for(Sentence sentence : article) {
					// reset per sentence
					String prevToken = "";
					
					for(Token token : sentence) {	
						// feature selection: use only a subset of words (features)
						
						// increase occurrence count of the token
						String currToken = token.getText();

						if (prevToken.equals("")) {
							currToken = BEGINNING_OF_SENTENCE;
							
							if (categoryLocalBigrams.get(currToken) == null) {
								categoryLocalBigrams.put(currToken, new HashMap<String, Integer>());
							}
						} else {
							// anywhere in the sentence
								
							if (!categoryLocalBigrams.containsKey(currToken)) {
								categoryLocalBigrams.put(currToken, new HashMap<String, Integer>());
							}

							Map<String, Integer> tempMap = categoryLocalBigrams.get(prevToken);

							if (tempMap.containsKey(currToken)) {
								tempMap.put(currToken, tempMap.get(currToken) + 1);
							} else {
								tempMap.put(currToken, 1);
							}
							
						}
						
						prevToken = currToken;
						
						// save term frequency for later probability calculation
						if (categoryLocalTermFrequency.get(currToken) != null) {
							categoryLocalTermFrequency.put(currToken, categoryLocalTermFrequency.get(currToken) + 1);
						} else {
							categoryLocalTermFrequency.put(currToken, 1);
						}	
					}
				}
			}
		}

	}

	public Map<String, Map<String, Integer>> getCategoryTermFrequency() {
		return categoryTermFrequency;
	}

	public long getNumArticles() {
		return numArticles;
	}
	
	public Map<String, Map<String, Map<String, Integer>>> getCategoryBigrams() {
		return categoryBigrams;
	}
	
	public Long getNumArticlesPerCategory(String categoryName) {
		return numArticlesPerCategory.get(categoryName);
	}
	
}
