package de.hpi.nlp.model;

import java.util.ArrayList;
import java.util.Iterator;

public class OhsumedArticle implements Iterable<Sentence> {
	private String title;
	private String category; // a.k.a. class
	private final ArrayList<Sentence> sentences;
	
	public OhsumedArticle() {
		this.sentences = new ArrayList<Sentence>();
	}
	
	public OhsumedArticle(String category) {
		this.category = category;
		this.sentences = new ArrayList<Sentence>();
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	public Iterator<Sentence> iterator() {
		return sentences.iterator();
	}
	
	public void addSentence(Sentence s) {
		sentences.add(s);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
}
