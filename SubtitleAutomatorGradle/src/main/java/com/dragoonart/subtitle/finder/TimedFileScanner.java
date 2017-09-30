package com.dragoonart.subtitle.finder;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.dragoonart.subtitle.finder.beans.VideoEntry;

public class TimedFileScanner extends SimpleFileVisitor<Path> implements Runnable {

	private Path rootFolder;
	private String[] movieExtensions = new String[] { "avi", "mpeg", "mkv", "mp4", "mpg", "" };

	private List<VideoEntry> acceptedFiles = new ArrayList<>();

	public TimedFileScanner(String path) {
		rootFolder = Paths.get(path);
	}

	@Override
	public void run() {
		try {
			Files.walkFileTree(rootFolder, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(VideoEntry ve : acceptedFiles) {
			System.out.println(ve.getPathToFile().toAbsolutePath().toString());
		}
		System.out.println(acceptedFiles.size());
	}

	private boolean acceptFile(String fileName) {
		for (String ext : movieExtensions) {
			if (fileName.endsWith("." + ext)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (acceptFile(file.getFileName().toString())) {
			VideoEntry vfb = new VideoEntry(file);
			if (!acceptedFiles.contains(vfb)) {
				acceptedFiles.add(vfb);
			}
		}
		return super.visitFile(file, attrs);
	}

}
