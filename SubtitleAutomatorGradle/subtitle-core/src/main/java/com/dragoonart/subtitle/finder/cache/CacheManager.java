package com.dragoonart.subtitle.finder.cache;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.FileLocations;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CacheManager {
	private static final Path VIDEO_ENTRIES_DIR = FileLocations.SETTINGS_DIRECTORY.resolve("videoEntries");
	private ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);

	private Map<String, VideoEntry> locCache = new HashMap<String, VideoEntry>();
	private static CacheManager instance;

	private CacheManager() {
	}

	public static synchronized CacheManager getInsance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}

	private String getCacheDirFromBaseDir(Path baseDir) {
		String cacheDir = baseDir.toAbsolutePath().toString();
		cacheDir = cacheDir.replaceAll("/", "");
		cacheDir = cacheDir.replaceAll("\\\\", "");
		cacheDir = cacheDir.replaceAll("\\.", "");
		cacheDir = cacheDir.replaceAll(":", "");
		return cacheDir;
	}

	public VideoEntry getCacheEntry(Path location, Path baseDir) {
		if (!Files.exists(VIDEO_ENTRIES_DIR)) {
			try {
				Files.createDirectories(VIDEO_ENTRIES_DIR);
			} catch (IOException e) {
				logger.warn("unable to create dir '" + VIDEO_ENTRIES_DIR + "' for cache!", e);
			}
		} else if (Files.exists(VIDEO_ENTRIES_DIR.resolve(
				location.getFileName().toString().substring(0, location.getFileName().toString().lastIndexOf("."))))) {
			try {
				String fileName = location.getFileName().toString().substring(0,
						location.getFileName().toString().lastIndexOf("."));

				VideoEntry ve = locCache.containsKey(fileName) ? locCache.get(fileName)
						: mapper.readValue(VIDEO_ENTRIES_DIR.resolve(fileName).toFile(), VideoEntry.class);

				validateAndUpdateCacheEntry(fileName, ve, location);
				if (!locCache.containsKey(fileName)) {
					locCache.put(fileName, ve);
				}
				return ve;
			} catch (IOException e) {
				logger.warn("unable to load cache entry for '" + location + "' for cache!", e);
			}
		}
		return null;
	}

	private void validateAndUpdateCacheEntry(String fileName, VideoEntry entry, Path location) {
		Set<SubtitleArchiveEntry> verifiedEntries = entry.getSubtitleArchives() != null ? entry.getSubtitleArchives().stream()
				.filter(e -> Files.exists(e.getPathToSubtitle())).collect(Collectors.toSet()) : null;
		boolean shouldUpdate = false;
		if(entry.getSubtitleArchives() != null && verifiedEntries.size() != entry.getSubtitleArchives().size()) {
			entry.setSubtitleArchives(verifiedEntries);
			shouldUpdate = true;
		}
		//if needed update the entry in the file storage
		if(shouldUpdate) {
			addCacheEntry(entry);
			locCache.put(fileName, entry);
		}
		//set the current entry's path before returning it.
		if(!entry.getPathToFile().equals(location)) {
			entry.setPathToFile(location);
		}
	}

	public boolean addCacheEntry(VideoEntry entry) {
		if (entry != null) {
			try {
				mapper.writeValue(VIDEO_ENTRIES_DIR.resolve(entry.getFileName()).toFile(), entry);
				locCache.put(entry.getFileName(), entry);
				return true;
			} catch (Exception e) {
				logger.warn("unable to add '" + entry + "' to cache!", e);
			}
		}
		return false;
	}
}
