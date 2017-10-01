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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

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
			int success = 0;
			int fail = 0;
			for(VideoEntry ve : acceptedFiles) {
				if(ve.getParsedFilename().getParsedAttributes().containsKey(IFileNameParser.SHOW_NAME)) {
					System.out.println(ve.toString());
				success++;
				} else {
					fail ++;
					
				}
			}
			System.out.println("Total  : "+acceptedFiles.size());
			System.out.println("Success: "+success);
			System.out.println("Fail: "+fail);
			List<SubtitleArchiveEntry> sre =sl.getSubtitleZips(acceptedFiles.get(15));
			for(SubtitleArchiveEntry e : sre) {
				for(Entry<String, Path> entry : e.getSubtitleEntries().entrySet()) {
					System.out.println("Name: "+entry.getKey()+" Location: "+entry.getValue().toAbsolutePath().toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private boolean acceptFile(String fileName) {
		for (String ext : movieExtensions) {
			if (fileName.endsWith("." + ext)) {
				for (String entry : fileName.split("\\.")) {
					if (StringUtils.containsIgnoreCase(entry, "sample")) {
						return false;
					}
				}
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
