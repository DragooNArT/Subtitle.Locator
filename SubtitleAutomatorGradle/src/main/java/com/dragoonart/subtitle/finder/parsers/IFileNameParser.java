package com.dragoonart.subtitle.finder.parsers;

import java.util.Map;

public interface IFileNameParser {
		
	public static final String SHOW_NAME = "NAME";
	public static final String SHOW_YEAR = "YEAR";
	public static final String SHOW_SEASON = "SEASON";
	public static final String SHOW_EPISODE = "EPISODE";
	public static final String SHOW_RESOLUTION = "RESOLUTION";
	public static final String SHOW_RELEASE = "RELEASE";
		
	
	public int getVersion();
	
	public Map<String, String> getParsedName(String origName);
	
}
