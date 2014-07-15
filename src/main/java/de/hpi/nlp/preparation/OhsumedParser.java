package de.hpi.nlp.preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.tartarus.martin.Stemmer;

import de.hpi.nlp.model.OhsumedArticle;
import de.hpi.nlp.model.OhsumedCorpus;
import de.hpi.nlp.model.Sentence;
import de.hpi.nlp.model.Token;

public class OhsumedParser {
	public static OhsumedCorpus parse(File uri) throws IOException, URISyntaxException {
		OhsumedCorpus corpus = new OhsumedCorpus();
		
		if (uri.isDirectory()) {
			// several subdirectories are expected, thus multiple classes/categories 
			File[] subfolders = uri.listFiles();
			
			// then each subfolder forms a separate class
			for(File classFolder : subfolders) {
				String className = classFolder.getName(); // e.g. C01, C02, ...
				
				for (File file : classFolder.listFiles()) {
					if (file.isFile()) {
						InputStream in = new FileInputStream(file);
						BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
						corpus.addArticle(parseArticle(reader, className));
					}
				}
			}
		} else {
			// the training files lie in this directory, there is only one class
		}
		
		return corpus;
	}

	private static OhsumedArticle parseArticle(BufferedReader reader, String className) throws IOException, URISyntaxException {
		String line = null;
		boolean firstLine = true;
		OhsumedArticle article = new OhsumedArticle(className);
		
		while ((line = reader.readLine()) != null) {
			if (firstLine) {
				article.setTitle(line);
				firstLine = false;
			}
			
			Sentence sentence = new Sentence();
			
			// tokenize and remove words with length < 2 and stop words
			ArrayList<String> parsedTokens = preProcess(line);
			
		
			for(String token : parsedTokens) {
				token = stemToken(token);
				Token tok = new Token(token);
				sentence.addToken(tok);
			}
			//System.out.println("line: " + line);
			//System.out.println("sentence: " + sentence);
			article.addSentence(sentence);
		}
		return article;
	}
	
	private static String stemToken(String token) {
		Stemmer stemmer = new Stemmer();
		stemmer.add(token.toCharArray(), token.length());
		stemmer.stem();
		return stemmer.toString();
	}
	
	/**
	 * Do some text processing to remove stop words and stem words for instance.
	 */
	private static ArrayList<String> preProcess(String line) throws IOException, URISyntaxException {
		StopWordParser stopParser = new StopWordParser();
		return stopParser.parseLine(line);
	}
}
