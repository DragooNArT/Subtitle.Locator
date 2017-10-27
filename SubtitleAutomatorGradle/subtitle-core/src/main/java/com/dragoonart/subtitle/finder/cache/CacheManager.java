package com.dragoonart.subtitle.finder.cache;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import com.dragoonart.subtitle.finder.beans.VideoEntry;

public class CacheManager {

	private class LocationCache {

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
			// TODO
		}

		@Override
		public int hashCode() {
			return location.hashCode();
		}
	}

	public static final String GLOBAL_CACHE_DIR = "";

	private Set<LocationCache> locCache = new HashSet<LocationCache>();
	private static CacheManager instance;

	static {
		// Initialise
		instance.getInsance();
	}

	public CacheManager() {
		loadCache();
	}

	public static synchronized CacheManager getInsance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}

	private void loadCache() {
		// TODO
	}

	public boolean hasCacheEntry(Path location) {
		return locCache.stream().filter(e -> e.getLocation().equals(location.toAbsolutePath().toString())).findFirst()
				.isPresent();
	}

	public VideoEntry getCachedEntry(Path location) {
		for (LocationCache lc : locCache) {
			VideoEntry ve = lc.getCacheEntry(location);
			if (ve != null)
				return ve;
		}
		return null;
	}

	private LocationCache getCacheEntry(String location) {
		return locCache.stream().filter(e -> e.getLocation().equals(location)).findFirst().orElse(null);
	}

	private LocationCache getCacheEntry(Path location) {
		return getCacheEntry(location.toAbsolutePath().toString());
	}

	private void createAndStoreNewEntry(VideoEntry entry) {
		LocationCache locEntry = new LocationCache(entry.getRootDir().toAbsolutePath().toString());
		locEntry.addCacheEntry(entry);
		locCache.add(locEntry);
	}

	public boolean addCacheEntry(VideoEntry entry) {
		if (entry != null && entry.getRootDir() != null) {
			if (hasCacheEntry(entry.getRootDir())) {
				getCacheEntry(entry.getRootDir()).addCacheEntry(entry);
			} else {
				createAndStoreNewEntry(entry);
			}

			// TODO Maybe tell the Location to store now. This can be a trigger for
			// re-assigning
			return true;
		}
		return false;
	}
}
