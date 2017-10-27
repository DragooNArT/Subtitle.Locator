package com.dragoonart.subtitle.finder;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.cache.CacheManager;

public class SubtitleFileScanner extends SimpleFileVisitor<Path> {

	private Path rootFolder;

	private static final String[] MOVIE_EXT = new String[] { "avi", "mpeg", "mkv", "mp4", "mpg", ".ts" };
	private Set<VideoEntry> subtitlessVideos = new HashSet<>();
	private Set<VideoEntry> subtitledVideos = new HashSet<>();

	public SubtitleFileScanner(String path) {
		rootFolder = Paths.get(path);
		CacheManager.getInsance();
	}

	public SubtitleFileScanner(Path path) {
		rootFolder = path;
		CacheManager.getInsance();
	}

	public Set<VideoEntry> getFolderVideos() {
		loadFolderVideos();
		return subtitlessVideos;
	}

	public Set<VideoEntry> getFolderSubtitledVideos() {
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

	private void logResults() {
		int success = 0;
		int fail = 0;
		for (VideoEntry ve : subtitlessVideos) {
			if (ve.getParsedFilename().hasShowName()) {
				System.out.println(ve.toString());
				success++;
			} else {
				fail++;
				System.out.println("FAIL: " + ve.getPathToFile().getFileName().toString());

			}
		}
		System.out.println("--- Parsing LOG ---");
		System.out.println("Total Videos Found : " + subtitlessVideos.size());
		System.out.println("Successfully Parssed : " + success);
		System.out.println("Fail Parssing : " + fail);
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
			if (StringUtils.startsWithIgnoreCase(release, subRelease)) {
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
				copySubtitleToMovie(newFilePath, entry.getValue());
				return true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}

	public void autoApplySubtitles(VideoEntry ve) {
		// TODO implement auto application in any case there's valid subs
		// boolean foundSuitable = false;
		ve.getSubtitles().stream().forEach(e -> {

			for (Entry<String, Path> entry : e.getSubtitleEntries().entrySet()) {
				System.out.println(
						"Name: " + entry.getKey() + "\nLocation: " + entry.getValue().toAbsolutePath().toString());
				if (areSuitableSubtitles(ve, entry)) {
					// foundSuitable = true;
					break;
				}
			}

		});
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

	public static boolean hasSubs(Path file) {
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
			VideoEntry vfb = CacheManager.getInsance().getCachedEntry(file);

			if (vfb == null) {
				vfb = new VideoEntry(file, rootFolder);
				CacheManager.getInsance().addCacheEntry(vfb);
			}
			if (hasSubs(file)) {
				subtitledVideos.add(vfb);
			} else {
				subtitlessVideos.add(vfb);
			}
		}
		return super.visitFile(file, attrs);
	}

	private boolean acceptSubtitle(String file) {
		return SubtitleFileUtils.isSubtitleEntry(file);
	}

}
