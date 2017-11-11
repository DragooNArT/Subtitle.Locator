package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.cache.CacheManager;

public class SubtitleFileScanner extends SimpleFileVisitor<Path> {

	private Path rootFolder;
	private static final Logger logger = LoggerFactory.getLogger(SubtitleFileScanner.class);
	private static final String[] MOVIE_EXT = new String[] { "avi", "mpeg", "mkv", "mp4", "mpg", ".ts" };
	private SortedSet<VideoEntry> subtitlessVideos = new TreeSet<>();
	private SortedSet<VideoEntry> subtitledVideos = new TreeSet<>();

	public SubtitleFileScanner(String path) {
		rootFolder = Paths.get(path);
		CacheManager.getInsance();
	}

	public SubtitleFileScanner(Path path) {
		rootFolder = path;
		CacheManager.getInsance();
	}

	public SortedSet<VideoEntry> getFolderVideos() {
		loadFolderVideos();
		return subtitlessVideos;
	}

	public SortedSet<VideoEntry> getFolderSubtitledVideos() {
		loadFolderVideos();
		return subtitledVideos;
	}

	public void setScanFolder(Path rootFolder) {
		this.rootFolder = rootFolder;
		subtitlessVideos.clear();
		subtitledVideos.clear();
	}

	private void loadFolderVideos() {
		try {
			Files.walkFileTree(rootFolder, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				logger.error("Can't parse subtitle name " + entry.getKey(), t);
				return false;
			}
			String subRelease = pfn.getRelease();
			//check if releases match
			if (StringUtils.startsWithIgnoreCase(release, subRelease)) {
				try {
					Path newFilePath = ve.getPathToFile().getParent()
							.resolve(ve.getFileName() + entry.getKey().substring(entry.getKey().lastIndexOf(".")));
					copySubtitleToMovie(newFilePath, entry.getValue());
					return true;
				} catch (Exception e1) {
					logger.error("Failed to copy subtitle:" + entry.getValue(), e1);
					return false;
				}
			}
		}
		if (ve.getAcceptableFileName().equals(entry.getKey())) {
			try {
				Path newFilePath = ve.getPathToFile().getParent().resolve(entry.getValue().getFileName());
				copySubtitleToMovie(newFilePath, entry.getValue());
				return true;
			} catch (Exception e1) {
				logger.error("Failed to copy subtitle:" + entry.getValue(), e1);
				return false;
			}
		}
		return false;
	}

	public boolean autoApplySubtitles(VideoEntry ve) {
		
		// TODO implement auto application in any case there's valid subs
		// boolean foundSuitable = false;
		for(SubtitleArchiveEntry e : ve.getSubtitles()) {

			for (Entry<String, Path> entry : e.getSubtitleEntries().entrySet()) {
				
				if (areSuitableSubtitles(ve, entry)) {
					// foundSuitable = true;
					logger.info("Found and applied subs to: "+ ve.getAcceptableFileName());
					return true;
				}
			}

		};
		return false;
		// //if can't approximate subtitles, apply the first entry
		// if(!foundSuitable && !ve.getSubtitles().isEmpty()) {
		// applyFirstFound(ve, ve.getSubtitles().iterator().next());
		// }
	}

	private void applyFirstFound(VideoEntry ve, SubtitleArchiveEntry e) {
		Path entry = e.getSubtitleEntries().get(0);
		Path newFilePath = ve.getPathToFile().getParent().resolve(ve.getFileName().concat(".srt"));
		try {
			copySubtitleToMovie(newFilePath, entry);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private void copySubtitleToMovie(Path newFilePath, Path entry) throws IOException {
		// remove the old subtitle file
		if (Files.exists(newFilePath)) {
			Files.delete(newFilePath);
		}
		// place the new subtitle file
		Files.copy(entry, newFilePath);
	}

	private boolean acceptFile(Path file) {
		for (String ext : MOVIE_EXT) {
			String fileName = file.getFileName().toString();
			if (fileName.endsWith("." + ext)) {
				if (isSample(file)) {
					return false;
				}

				// finally add it
				return true;
			}
		}
		// doesn't match the extension list
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
			VideoEntry vfb = CacheManager.getInsance().getCachedEntry(file);

			if (SubtitleFileUtils.hasSubs(file)) {
				vfb = new VideoEntry(file, rootFolder, VideoState.FINISHED);
				subtitledVideos.add(vfb);
			} else {
				vfb = new VideoEntry(file, rootFolder, VideoState.PENDING);
				subtitlessVideos.add(vfb);
			}
			CacheManager.getInsance().addCacheEntry(vfb);
		}
		return super.visitFile(file, attrs);
	}

//	private boolean acceptSubtitle(String file) {
//		return SubtitleFileUtils.isSubtitleEntry(file);
//	}

}
