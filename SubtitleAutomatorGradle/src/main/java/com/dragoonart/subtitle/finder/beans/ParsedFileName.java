package com.dragoonart.subtitle.finder.beans;

import java.util.HashMap;
import java.util.Map;

import com.dragoonart.subtitle.finder.parsers.IFileNameParser;
import com.dragoonart.subtitle.finder.parsers.impl.ParserFactory;

public class ParsedFileName {

	private int version = -1;
	private String origName;
	private Map<String, String> parsedAttributes = new HashMap<String, String>();
	private IFileNameParser nameParser;

	public ParsedFileName(String origName) {
		this.origName = origName;
		nameParser = ParserFactory.getFileNameParser();
	}

	public String getOrigName() {
		return origName;
	}

	public int getVersion() {
		return version;
	}

	public Map<String, String> getParsedAttributes() {
		load();
		return parsedAttributes;
	}

	private void load() {
		if (nameParser.getVersion() > version) {
			parsedAttributes.clear();
			parsedAttributes = nameParser.getParsedName(getOrigName());
			version = nameParser.getVersion();
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Best file name: " + origName).append("\n");
		
		sb.append("Name: " + getParsedAttributes().get(IFileNameParser.SHOW_NAME)).append("\n");
		
		if (getParsedAttributes().containsKey(IFileNameParser.SHOW_YEAR)) {
			sb.append("Year: " + getParsedAttributes().get(IFileNameParser.SHOW_YEAR)).append("\n");
		}
		if (getParsedAttributes().containsKey(IFileNameParser.SHOW_SEASON)) {
			sb.append("Season: " + getParsedAttributes().get(IFileNameParser.SHOW_SEASON)).append("\n");
		}
		if (getParsedAttributes().containsKey(IFileNameParser.SHOW_EPISODE)) {
			sb.append("Episode: " + getParsedAttributes().get(IFileNameParser.SHOW_EPISODE)).append("\n");
		}
		if (getParsedAttributes().containsKey(IFileNameParser.SHOW_RESOLUTION)) {
			sb.append("Resolution: " + getParsedAttributes().get(IFileNameParser.SHOW_RESOLUTION)).append("\n");
		}
		if (getParsedAttributes().containsKey(IFileNameParser.SHOW_RELEASE)) {
			sb.append("Release: " + getParsedAttributes().get(IFileNameParser.SHOW_RELEASE)).append("\n");
		}
		return sb.toString();
	}
}
