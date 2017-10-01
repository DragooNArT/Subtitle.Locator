package com.dragoonart.subtitle.finder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.parsers.IFileNameParser;

public class TimedFileScanner extends SimpleFileVisitor<Path> implements Runnable {

	private Path rootFolder;

	private SubtitleLocator sl = new SubtitleLocator();
	private String[] movieExtensions = new String[] { "avi", "mpeg", "mkv", "mp4", "mpg", "" };

	private List<VideoEntry> acceptedFiles = new ArrayList<>();

	public TimedFileScanner(String path) {
		rootFolder = Paths.get(path);
	}

	@Override
	public void run() {
		try {
			Files.walkFileTree(rootFolder, this);
			logResults();
			insertExactSubMatches();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void insertExactSubMatches() {
		List<VideoEntry> toRemove = new ArrayList<VideoEntry>();
		acceptedFiles.parallelStream().forEach(ve -> {
			List<SubtitleArchiveEntry> sre = sl.getSubtitleZips(ve);
			sre.parallelStream().forEach(e -> {
				for (Entry<String, Path> entry : e.getSubtitleEntries().entrySet()) {
					System.out.println(
							"Name: " + entry.getKey() + "\nLocation: " + entry.getValue().toAbsolutePath().toString());
					if (subtitleExactMatch(ve, entry)) {
						toRemove.add(ve);
						break;
					}
				}
			});
		});
		// remove files for which an exact match has been found
		acceptedFiles.removeAll(toRemove);
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

			}
		}
		System.out.println("Total  : " + acceptedFiles.size());
		System.out.println("Success: " + success);
		System.out.println("Fail: " + fail);
	}

	private boolean subtitleExactMatch(VideoEntry ve, Entry<String, Path> entry) {
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
				if (!Files.exists(newFilePath)) {
					Files.copy(entry.getValue(),
							ve.getPathToFile().getParent().resolve(entry.getValue().getFileName()));
					return true;
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return false;
	}

	private boolean acceptFile(Path file) {
		for (String ext : movieExtensions) {
			String fileName = file.getFileName().toString();
			if (fileName.endsWith("." + ext)) {
				// calculate if it already has subtitles
				String srt = fileName.substring(0, fileName.lastIndexOf(".")) + ".srt";
				String sub = fileName.substring(0, fileName.lastIndexOf(".")) + ".sub";
				if (Files.exists(file.getParent().resolve(srt)) || Files.exists(file.getParent().resolve(sub))) {
					return false;
				}
				// assert it's not a sample file
				for (String entry : fileName.split("\\.")) {
					if (StringUtils.containsIgnoreCase(entry, "sample")) {
						return false;
					}
				}
				// finally add it
				return true;
			}
		}
		// doesn't match the extension list
		return false;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

		if (acceptFile(file)) {
			VideoEntry vfb = new VideoEntry(file);
			if (!acceptedFiles.contains(vfb)) {
				acceptedFiles.add(vfb);
			}
		}
		return super.visitFile(file, attrs);
	}

	private boolean acceptSubtitle(String file) {
		return file.endsWith(".srt") || file.endsWith(".sub");
	}

}
