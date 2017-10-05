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

	public String getShowName() {
		return getParsedAttributes().get(IFileNameParser.SHOW_NAME);
	}

	public boolean hasShowName() {
		return getShowName() != null;
	}

	public String getSeason() {
		return getParsedAttributes().get(IFileNameParser.SHOW_SEASON);
	}

	public boolean isEpisodic() {
		return getSeason() != null;
	}

	public String getEpisode() {
		return getParsedAttributes().get(IFileNameParser.SHOW_EPISODE);
	}

	public String getRelease() {
		return getParsedAttributes().get(IFileNameParser.SHOW_RELEASE);
	}

	public boolean hasRelease() {
		return getRelease() != null;
	}

	public String getYear() {
		return getParsedAttributes().get(IFileNameParser.SHOW_YEAR);
	}

	public boolean hasYear() {
		return getYear() != null;
	}

	public String getResolution() {
		return getParsedAttributes().get(IFileNameParser.SHOW_RESOLUTION);
	}

	public boolean hasResolution() {
		return getResolution() != null;
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
		sb.append("Name: " + getShowName()).append("\n");
		sb.append("Year: " + getYear()).append("\n");
		sb.append("Season: " + getSeason()).append("\n");
		sb.append("Episode: " + getEpisode()).append("\n");
		sb.append("Resolution: " + getResolution()).append("\n");
		sb.append("Release: " + getRelease()).append("\n");
		return sb.toString();
	}
}
