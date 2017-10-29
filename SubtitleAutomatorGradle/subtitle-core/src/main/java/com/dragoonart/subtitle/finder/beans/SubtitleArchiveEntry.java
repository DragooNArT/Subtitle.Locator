package com.dragoonart.subtitle.finder.beans;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.dragoonart.subtitle.finder.SubtitleFileUtils;
import com.dragoonart.subtitle.finder.web.SubtitleProvider;

public class SubtitleArchiveEntry {

	private Path pathToSubtitle;

	private String link;

	private SubtitleProvider source;
	private Map<String, Path> subtitleEntries = new HashMap<String, Path>();

	public SubtitleArchiveEntry(SubtitleProvider source, String link, Path pathToArchive) {
		this.pathToSubtitle = pathToArchive;
		this.link = link;
		this.source = source;
	}

	public SubtitleArchiveEntry(Path pathToArchive) {
		this.pathToSubtitle = pathToArchive;
	}

	public String getLink() {
		return link;
	}

	public SubtitleProvider getProvider() {
		return source;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setSource(SubtitleProvider source) {
		this.source = source;
	}

	public Map<String, Path> getSubtitleEntries() {
		if (subtitleEntries.isEmpty() && Files.exists(pathToSubtitle)) {
			try {
				subtitleEntries
						.putAll(SubtitleFileUtils.unpackSubs(pathToSubtitle, pathToSubtitle.getParent().getParent()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return subtitleEntries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathToSubtitle == null) ? 0 : pathToSubtitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SubtitleArchiveEntry other = (SubtitleArchiveEntry) obj;
		if (pathToSubtitle == null) {
			if (other.pathToSubtitle != null)
				return false;
		} else if (!pathToSubtitle.equals(other.pathToSubtitle))
			return false;
		return true;
	}

	public String getSubtitleName() {
		return pathToSubtitle.getFileName().toString();
	}
}
