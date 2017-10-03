package com.dragoonart.subtitle.finder.parsers.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.dragoonart.subtitle.finder.parsers.IFileNameParser;

public class FileNameParser implements IFileNameParser {
	
	Pattern sXXeXX_pattern = Pattern.compile("[Ss]\\d\\d[Ee]\\d\\d");
	Pattern matchYear = Pattern.compile("\\d\\d\\d\\d");
	Pattern dotSepPattern = Pattern.compile("\\d{1,}[.]\\d{1,}");
	
	private static final String[] allForRemoval = new String[] { "HDTV", "HEVC", "UNRATED", "BluRay", "x265", "DTS-HD",
			"X264", "WEB-DL", "H264" ,"DDC5", "AAC5" , "DTS" , "HDRip" , "DD5" , "BRRip" };

	
	FileNameParser() {
		//package private
	}
	@Override
	public int getVersion() {
		return 1;
	}

	public static boolean canSplit(String origName) {
		if (StringUtils.countMatches(origName, ".") > 0 || StringUtils.countMatches(origName, "-") > 1 || StringUtils.countMatches(origName, " ") > 2) {
			return true;
		}
		return false;
	}

	private int seasonEpisodeLookup(List<String> parts, Map<String, String> result) {
		int patternIndex = -1;
		for (int i = 0; i < parts.size(); i++) {
			String entry = parts.get(i);
			Matcher matcher = sXXeXX_pattern.matcher(entry);
			if (matcher.matches()) {
				result.put(SHOW_SEASON, entry.substring(1, 3));
				result.put(SHOW_EPISODE, entry.substring(4, 6));
				patternIndex = i;
				break;
			} else if (dotSepPattern.matcher(entry).matches()) {
				result.put(SHOW_SEASON, entry.substring(0, entry.indexOf('.')));
				result.put(SHOW_EPISODE, entry.substring(entry.indexOf('.') + 1, entry.length()));
				patternIndex = i;
			}

		}
		return patternIndex;
	}

	private boolean isResolution(String part) {
		return "480p".equals(part) || "720p".equals(part) || "1080p".equals(part) || "576p".equals(part);
	}

	private boolean isRelease(String part) {
		return part != null && !part.isEmpty() && StringUtils.countMatches(part, "-") > 0;

	}

	private List<String> splitAndCleanup(String origName, Map<String, String> result) {
		List<String> split = null;
		int dotMatch = StringUtils.countMatches(origName, ".");
		int hashMatch = StringUtils.countMatches(origName, "-");
		int spaceMatch = StringUtils.countMatches(origName, " ");
		
		if (dotMatch > hashMatch && dotMatch > spaceMatch) {
			split = new LinkedList<String>(Arrays.asList(origName.split("\\.")));
			resolveRelease(split, result);
		} else if(hashMatch > dotMatch && hashMatch > spaceMatch){
			split = new LinkedList<String>(Arrays.asList(origName.split("-")));
		} else if(spaceMatch > dotMatch && spaceMatch > hashMatch){
			split = new LinkedList<String>(Arrays.asList(origName.split(" ")));
		} else {
			split = new LinkedList<String>();
			split.add(origName);
			return split;
		}
		
		split = split.stream().map(e -> e.trim()).collect(Collectors.toList());
		List<String> forRemoval = new ArrayList<>();
		resolveResolution(split, forRemoval, result);
		resolveForRemoval(split, forRemoval, result);
		resolveYearForRemoval(split,forRemoval,result);
		for (String forRem : forRemoval) {
			split.remove(forRem);
		}
		return split;

	}

	private void resolveYearForRemoval(List<String> split, List<String> forRemoval, Map<String, String> result) {
		for(String entry : split) {
			Matcher matcher = matchYear.matcher(entry);
			if(matcher.matches()) {
				result.put(SHOW_YEAR, entry);
				forRemoval.add(entry);
				break;
			}
		}
		
	}

	private void resolveForRemoval(List<String> dotSplit, List<String> forRemoval, Map<String, String> result) {
		for (String entry : dotSplit) {
			if (isForRemoval(entry)) {
				forRemoval.add(entry);
			}
		}
	}

	private void resolveRelease(List<String> dotSplit, Map<String, String> result) {
		String lastEntry = dotSplit.get(dotSplit.size() - 1);
		if (isRelease(lastEntry)) {
			result.put(SHOW_RELEASE, lastEntry);
			dotSplit.remove(lastEntry);
		}
	}

	private void resolveResolution(List<String> dotSplit, List<String> forRemoval, Map<String, String> result) {
		for (String entry : dotSplit) {
			if (isResolution(entry)) {
				result.put(SHOW_RESOLUTION, entry);
				forRemoval.add(entry);
				break;
			}
		}

	}

	private boolean isForRemoval(String entry) {
		if (entry.length() <= 2) {
			return true;
		}
		for (String remEntry : allForRemoval) {
			if (remEntry.equalsIgnoreCase(entry)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Map<String, String> getParsedName(String origName) {
		Map<String, String> result = new HashMap<String, String>();
		if (!canSplit(origName)) {
			result.put(IFileNameParser.SHOW_NAME, origName);
			return result;
		}
	
		List<String> dotSplit = splitAndCleanup(origName, result);

		int SEindex = seasonEpisodeLookup(dotSplit, result);
		showNameLookup(dotSplit, SEindex, result);
		return result;
	}

	private void showNameLookup(List<String> dotSplit, int sEindex, Map<String, String> result) {
		StringBuilder sBuilder = new StringBuilder();
		int start = sEindex < 0 || sEindex > 0 ? 0 : 1;
		int endIndex = sEindex == 0 || sEindex < 0 ? dotSplit.size() : sEindex;

		for (int i = start; i < endIndex; i++) {
			sBuilder.append(dotSplit.get(i)).append(" ");
		}
		result.put(SHOW_NAME, sBuilder.toString().trim());
	}
}
