package com.dragoonart.subtitle.finder.beans;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.dragoonart.subtitle.finder.VideoState;
import com.dragoonart.subtitle.finder.parsers.impl.FileNameParser;

public class VideoEntry implements Comparable<VideoEntry> {

	private final Path pathToFile;

	private final Path rootDir;

	private Set<SubtitleArchiveEntry> subtitles;

	private String acceptableFileName = null;

	private ParsedFileName pfn;
	
	private VideoState state;
	
	public VideoEntry(Path pathToFile, Path rootDir,VideoState initialState) {
		this.pathToFile = pathToFile;
		this.rootDir = rootDir;
		this.state = initialState;
		pfn = new ParsedFileName(getAcceptableFileName());
	}

	public String getAcceptableFileName() {
		resolveBestFileName();
		return acceptableFileName;
	}
	
	public VideoState getState() {
		return state;
	}
	
	public void setState(VideoState state) {
		this.state = state;
	}
	
	private void resolveBestFileName() {
		if (acceptableFileName == null) {
			if (FileNameParser.canSplit(getFileName())) {
				acceptableFileName = getFileName();
			} else if (FileNameParser.canSplit(pathToFile.getParent().getFileName().toString())) {
				acceptableFileName = pathToFile.getParent().getFileName().toString();
			} else {
				acceptableFileName = getFileName();
			}
		}
	}

	public Path getPathToFile() {
		return pathToFile;
	}

	public Path getRootDir() {
		return rootDir;
	}

	public ParsedFileName getParsedFilename() {
		return pfn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathToFile == null) ? 0 : pathToFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VideoEntry other = (VideoEntry) obj;
		if (pathToFile == null) {
			if (other.pathToFile != null)
				return false;
		} else if (!pathToFile.equals(other.pathToFile))
			return false;
		return true;
	}

	public Set<SubtitleArchiveEntry> getSubtitleArchives() {
		return subtitles;
	}

	public void setSubtitles(Set<SubtitleArchiveEntry> subtitles) {
		this.subtitles = subtitles;
	}

	public void addSubtitles(Set<SubtitleArchiveEntry> subtitles) {
		if (this.subtitles == null) {
			this.subtitles = new HashSet<SubtitleArchiveEntry>();
		}
		for(SubtitleArchiveEntry sE : subtitles) {
			if(this.subtitles.contains(sE)) {
				this.subtitles.remove(sE);
				
			} 
			this.subtitles.add(sE);
		}
	}

	public boolean hasSubtitles() {
		return subtitles != null && !subtitles.isEmpty();
	}


	public String getFileName() {
		return pathToFile.getFileName().toString().substring(0, pathToFile.getFileName().toString().lastIndexOf("."));
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Original file name: ").append(getFileName()).append("\n")
				.append(getParsedFilename().toString()).append("\n").append(new Date(pathToFile.toFile().lastModified()).toString()).toString();
	}
	
	@Override
	public int compareTo(VideoEntry p2) {
		if (pathToFile.toFile().lastModified() < p2.pathToFile.toFile().lastModified()) {
			return 1;
		} else if (pathToFile.toFile().lastModified() == p2.pathToFile.toFile().lastModified()) {
			return 0;
		}
		return -1;

	}
}
