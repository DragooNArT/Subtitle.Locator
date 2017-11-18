package com.dragoonart.subtitle.finder.beans;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.parsers.IFileNameParser;
import com.dragoonart.subtitle.finder.parsers.impl.ParserFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ParsedFileName {
	
	private static final Logger logger = LoggerFactory.getLogger(ParsedFileName.class);
	private int version = -1;
	private String origName;
	private Map<String, String> parsedAttributes = new HashMap<String, String>();
	private IFileNameParser nameParser;
	//constructor for JSON
	public ParsedFileName() {
		nameParser = ParserFactory.getFileNameParser();
	}
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
	@JsonIgnore
	public String getShowName() {
		return getParsedAttributes().get(IFileNameParser.SHOW_NAME);
	}

	public boolean hasShowName() {
		return getShowName() != null;
	}
	@JsonIgnore
	public String getSeason() {
		return getParsedAttributes().get(IFileNameParser.SHOW_SEASON);
	}
	@JsonIgnore
	public boolean isEpisodic() {
		return getSeason() != null;
	}
	@JsonIgnore
	public String getEpisode() {
		return getParsedAttributes().get(IFileNameParser.SHOW_EPISODE);
	}
	@JsonIgnore
	public String getRelease() {
		return getParsedAttributes().get(IFileNameParser.SHOW_RELEASE);
	}

	public boolean hasRelease() {
		return getRelease() != null;
	}
	@JsonIgnore
	public String getYear() {
		return getParsedAttributes().get(IFileNameParser.SHOW_YEAR);
	}

	public boolean hasYear() {
		return getYear() != null;
	}
	@JsonIgnore
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
	
	public void setParsedAttributes(Map<String, String> parsedAttrs) {
		parsedAttributes = parsedAttrs;
	}
	
	private void load() {
		if (nameParser.getVersion() > version) {
			parsedAttributes.clear();
			try {
			parsedAttributes = nameParser.getParsedName(getOrigName());
			version = nameParser.getVersion();
			} catch(Exception e) {
				logger.error("Unable to parse file with name: "+getOrigName(), e);
			}
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
