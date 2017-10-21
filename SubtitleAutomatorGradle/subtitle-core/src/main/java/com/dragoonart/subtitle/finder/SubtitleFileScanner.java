package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.cache.CacheManager;
import com.dragoonart.subtitle.finder.cache.LocationCache;
import com.dragoonart.subtitle.finder.web.SubtitleFinder;

public class SubtitleFileScanner extends SimpleFileVisitor<Path> implements Runnable {

	private Path rootFolder;

	private String[] movieExtensions = new String[] { "avi", "mpeg", "mkv", "mp4", "mpg", "" };
	private SubtitleFinder subFinder = new SubtitleFinder();
	private Set<VideoEntry> acceptedFiles = new HashSet<>();
	private LocationCache locCache;

	public SubtitleFileScanner(String path) {
		rootFolder = Paths.get(path);
		locCache = CacheManager.getInsance().getCacheEntry(rootFolder);
	}

	public SubtitleFileScanner(Path path) {
		rootFolder = path;
		locCache = CacheManager.getInsance().getCacheEntry(rootFolder);
	}

	public Set<VideoEntry> getFolderVideos() {
		loadFolderVideos();
		return acceptedFiles;
	}

	private void loadFolderVideos() {
		try {
			Files.walkFileTree(rootFolder, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			// Initialize ( Find all files + create file parsers for them )
			Files.walkFileTree(rootFolder, this);
			// Log info about accepted files
			logResults();
			// Start searching and downloading subs for accepted files
			insertExactSubMatches();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void insertExactSubMatches() {
		List<VideoEntry> forRemoval = new ArrayList<VideoEntry>();
		acceptedFiles.parallelStream().forEach(ve -> {
			Set<SubtitleArchiveEntry> sre = subFinder.lookupEverywhere(ve);
			sre.stream().forEach(e -> {
				for (Entry<String, Path> entry : e.getSubtitleEntries().entrySet()) {
					System.out.println(
							"Name: " + entry.getKey() + "\nLocation: " + entry.getValue().toAbsolutePath().toString());
					if (areSuitableSubtitles(ve, entry)) {
						forRemoval.add(ve);
						break;
					}
				}
			});
		});
		// remove files for which an exact match has been found
		acceptedFiles.removeAll(forRemoval);
	}

	private void logResults() {
		int success = 0;
		int fail = 0;
		for (VideoEntry ve : acceptedFiles) {
			if (ve.getParsedFilename().hasShowName()) {
				System.out.println(ve.toString());
				success++;
			} else {
				fail++;
				System.out.println("FAIL: " + ve.getPathToFile().getFileName().toString());

			}
		}
		System.out.println("Total  : " + acceptedFiles.size());
		System.out.println("Success: " + success);
		System.out.println("Fail: " + fail);
	}

	private boolean areSuitableSubtitles(VideoEntry ve, Entry<String, Path> entry) {
		String release = ve.getParsedFilename().getRelease();
		if (release != null) {
			ParsedFileName pfn = null;
			try {
				pfn = new ParsedFileName(
						entry.getKey().indexOf(".") > -1 ? entry.getKey().substring(0, entry.getKey().lastIndexOf("."))
								: entry.getKey());
			} catch (Throwable t) {
				System.out.println("Bad entry: " + entry.getKey());
				throw t;
			}
			String subRelease = pfn.getRelease();
			if (release.equalsIgnoreCase(subRelease)) {
				try {
					Path newFilePath = ve.getPathToFile().getParent()
							.resolve(ve.getFileName() + entry.getKey().substring(entry.getKey().lastIndexOf(".")));
					if (!Files.exists(newFilePath)) {
						Files.copy(entry.getValue(), newFilePath);
						return true;
					}
				} catch (IOException e1) {
					System.out.println("Bad Value:" + entry.getValue());
					e1.printStackTrace();
				}
			}
		}
		if (ve.getAcceptableFileName().equals(entry.getKey())) {
			try {
				Path newFilePath = ve.getPathToFile().getParent().resolve(entry.getValue().getFileName());
				copySubtitleToMovie(newFilePath, entry);
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}

	private void copySubtitleToMovie(Path newFilePath, Entry<String, Path> entry) throws IOException {
		// remove the old subtitle file
		if (Files.exists(newFilePath)) {
			Files.delete(newFilePath);
		}
		// place the new subtitle file
		Files.copy(entry.getValue(), newFilePath);
	}

	private boolean acceptFile(Path file) {
		for (String ext : movieExtensions) {
			String fileName = file.getFileName().toString();
			if (fileName.endsWith("." + ext)) {
				if(hasSubs(file) || isSample(file)) {
					return false;
				}
				
				// finally add it
				return true;
			}
		}
		// doesn't match the extension list
		return false;
	}

	private boolean hasSubs(Path file) {
		// calculate if it already has subtitles
		String fileName = file.getFileName().toString();
		String srt = fileName.substring(0, fileName.lastIndexOf(".")) + ".srt";
		String sub = fileName.substring(0, fileName.lastIndexOf(".")) + ".sub";
		if (Files.exists(file.getParent().resolve(srt)) || Files.exists(file.getParent().resolve(sub))) {
			return true;
		}
		return false;
	}

	private boolean isSample(Path file) {
		// assert it's not a sample file
		for (String entry : file.getFileName().toString().split("\\.")) {
			if (StringUtils.containsIgnoreCase(entry, "sample")) {
				return true;
			}
		}
		if (file.getParent() != rootFolder) {
			for (String entry : file.getParent().getFileName().toString().split("\\.")) {
				if (StringUtils.containsIgnoreCase(entry, "sample")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

		if (acceptFile(file)) {
			VideoEntry vfb;
			if (locCache == null || (vfb = locCache.getCacheEntry(file)) == null) {
				vfb = new VideoEntry(file, rootFolder);
				if (CacheManager.getInsance().addCacheEntry(vfb) && locCache == null) {
					locCache = CacheManager.getInsance().getCacheEntry(rootFolder);
				}
			}
			acceptedFiles.add(vfb);
		}
		return super.visitFile(file, attrs);
	}

	private boolean acceptSubtitle(String file) {
		return file.endsWith(".srt") || file.endsWith(".sub");
	}

}
