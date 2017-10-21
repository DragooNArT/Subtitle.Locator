package com.dragoonart.subtitle.finder.cache;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.dragoonart.subtitle.finder.beans.VideoEntry;

public class CacheManager {

	private List<LocationCache> locCache = new ArrayList<LocationCache>();
	private static CacheManager instance;

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

	}

	protected boolean hasCacheEntry(Path location) {
		return locCache.stream().filter(e -> e.getLocation().equals(location.toAbsolutePath().toString())).findFirst()
				.isPresent();
	}

	protected LocationCache getCacheEntry(String location) {
		return locCache.stream().filter(e -> e.getLocation().equals(location)).findFirst().orElse(null);
	}

	public LocationCache getCacheEntry(Path location) {
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
				return true;
			} else {
				createAndStoreNewEntry(entry);
				return true;
			}
		}
		return false;
	}

	private void writeToStorage() {

	}

}
