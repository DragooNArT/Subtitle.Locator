package com.dragoonart.subtitle.finder.cache;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.dragoonart.subtitle.finder.beans.VideoEntry;

public class LocationCache {

	private String location;

	private Set<VideoEntry> videoEntries;

	public LocationCache(String location) {
		this.location = location;
		videoEntries = new HashSet<VideoEntry>();
	}

	public String getLocation() {
		return location;
	}

	protected void addCacheEntry(VideoEntry entry) {
		videoEntries.add(entry);
	}

	public VideoEntry getCacheEntry(Path videoLoc) {
		return videoEntries.stream().filter(e -> e.getPathToFile().equals(videoLoc)).findFirst().orElse(null);
	}

	protected void removeCacheEntry() {

	}
}
