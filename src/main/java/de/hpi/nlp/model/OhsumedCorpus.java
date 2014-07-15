package de.hpi.nlp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;

public class OhsumedCorpus implements Iterable<OhsumedArticle> {
	private final Map<String, ArrayList<OhsumedArticle>> articlesPerCategory;
	private final Map<String, Integer> tokensPerCategory;
	public Map<String, Integer> getTokensPerCategory() {
		return tokensPerCategory;
	}

	private final Set<String> vocabulary;
	private long numArticles = 0;
	private long numSentences = 0;
	private long numTokens = 0;
	
	public OhsumedCorpus() {
		articlesPerCategory = new HashMap<>();
		tokensPerCategory = new HashMap<>();
		vocabulary =  new HashSet<String>();
	}
	
	public void addArticle(OhsumedArticle a) {
		String categoryName = a.getCategory();
		
		// count number of tokens appearing in this article
		if (!tokensPerCategory.containsKey(categoryName)) {
			tokensPerCategory.put(categoryName, 0);
		}
		Integer numTokens = tokensPerCategory.get(categoryName);
		
		// get respective class/category to add articles to
		if (!articlesPerCategory.containsKey(categoryName)) {
			articlesPerCategory.put(categoryName, new ArrayList<OhsumedArticle>());
		}
		ArrayList<OhsumedArticle> articles = articlesPerCategory.get(categoryName);
		
		// add articles to the selected class
		articles.add(a);
		for(Sentence s : a) {
			numSentences++;
			for(Token token : s) {
				vocabulary.add(token.getText());
				numTokens++;
			}
		}
		numArticles++;
	}
	
	public long getNumTokens() {
		return numTokens;
	}
	
	public long getNumSentences() {
		return numSentences;
	}
	
	public long getNumArticles() {
		return numArticles;
	}
	
	public long getNumArticlesPerCategory(String categoryName) {
		return articlesPerCategory.get(categoryName).size();
	}
	
	public int getVocabularySize() {
		return vocabulary.size();
	}
	
	public int getNumCategories() {
		return articlesPerCategory.keySet().size();
	}
	
	public Set<String> getCategories() {
		return articlesPerCategory.keySet();
	}
	
	public Iterator<OhsumedArticle> getArticlesOfCategory(String categoryName) {
		return articlesPerCategory.get(categoryName).iterator();
	}

	@Override
	public Iterator<OhsumedArticle> iterator() {
		return Iterables.concat(articlesPerCategory.values()).iterator();
	}
	
}
