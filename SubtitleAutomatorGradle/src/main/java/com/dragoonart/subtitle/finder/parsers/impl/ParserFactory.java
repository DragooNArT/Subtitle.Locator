package com.dragoonart.subtitle.finder.parsers.impl;

import com.dragoonart.subtitle.finder.parsers.IFileNameParser;

public class ParserFactory {
	
	private static IFileNameParser parser;
	
	public static IFileNameParser getFileNameParser() {
		if(parser == null) {
			parser = new FileNameParser();
		}
		return parser;
	}

}
