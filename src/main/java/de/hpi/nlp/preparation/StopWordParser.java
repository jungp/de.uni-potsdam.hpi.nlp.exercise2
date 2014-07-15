package de.hpi.nlp.preparation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StopWordParser {
	private final Set<String> stopWords;
	
	public StopWordParser() throws IOException, URISyntaxException {
		this.stopWords = new HashSet<String>();
		
		File file = new File(StopWordParser.class.getResource("/common-english-words-with-contractions.txt").toURI());
		InputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] stop = line.split(",");
			for(String word : stop) {
				stopWords.add(word);
			}
		}	
		reader.close();
	}
	
	public ArrayList<String> parseLine(String line) {
		ArrayList<String> parsedTokens = new ArrayList<>();
		String[] tokens = line.split("\\s");
		for(String token : tokens) {
			if (token.length() > 1) { // remove tokens like "." or any other nonsense
				token = token.toLowerCase();
				if (!stopWords.contains(token)) {
					parsedTokens.add(token); // only tokens that are not on the stop word list pass
				}
			}
		}
		return parsedTokens;
	}
}
