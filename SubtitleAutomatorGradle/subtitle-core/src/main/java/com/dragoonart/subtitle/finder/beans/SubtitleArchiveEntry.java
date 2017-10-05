package com.dragoonart.subtitle.finder.beans;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.dragoonart.subtitle.finder.SubtitleFileUtils;

public class SubtitleArchiveEntry {

	private Path pathToSubtitle;

	private String link;

	private Map<String, Path> subtitleEntries = new HashMap<String, Path>();

	public SubtitleArchiveEntry(String link, Path pathToArchive) {
		this.pathToSubtitle = pathToArchive;
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public Map<String, Path> getSubtitleEntries() {
		if (subtitleEntries.isEmpty() && Files.exists(pathToSubtitle)) {
			try {
				subtitleEntries.putAll(SubtitleFileUtils.unpackSubs(pathToSubtitle,pathToSubtitle.getParent().getParent()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return subtitleEntries;
	}

	public String getSubtitleName() {
		return pathToSubtitle.getFileName().toString();
	}
}
