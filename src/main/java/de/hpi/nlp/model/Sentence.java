package de.hpi.nlp.model;

import java.util.ArrayList;
import java.util.Iterator;

public class Sentence implements Iterable<Token>{
	private final ArrayList<Token> tokens;
	private int numTokens = 0;
	
	public Sentence() {
		tokens = new ArrayList<Token>();
	}
	
	public void addToken(Token token) {
		tokens.add(token);
		numTokens++;
	}

	public Iterator<Token> iterator() {
		return tokens.iterator();
	}
	
	public int getNumTokens() {
		return numTokens;
	}
	
	// mainly for test purposes
	public String toString() {
		String string = "";
		for(Token t : tokens) {
			string += t.getText() + " ";
		}
		return string;
	}
}
