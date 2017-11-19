package com.dragoonart.subtitle.finder.beans;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.dragoonart.subtitle.finder.VideoState;
import com.dragoonart.subtitle.finder.beans.videometa.VideoMetaBean;
import com.dragoonart.subtitle.finder.parsers.impl.FileNameParser;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class VideoEntry implements Comparable<VideoEntry> {

	private Path pathToFile;

	private Set<SubtitleArchiveEntry> subtitleArchives;

	private String acceptableFileName = null;

	private ParsedFileName parsedFileName;
	
	private VideoState state;
	
	//constructor for JSON
	public VideoEntry() {
	}
	public VideoEntry(Path pathToFile, VideoState initialState) {
		this.pathToFile = pathToFile;
		this.state = initialState;
	}
	@JsonIgnore
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

	public ParsedFileName getParsedFileName() {
		if(parsedFileName == null) {
			parsedFileName = new ParsedFileName(getAcceptableFileName());
		}
		return parsedFileName;
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
		return subtitleArchives;
	}

	public void setSubtitleArchives(Set<SubtitleArchiveEntry> subtitles) {
		this.subtitleArchives = subtitles;
	}

	public void addSubtitles(Set<SubtitleArchiveEntry> subtitles) {
		if (this.subtitleArchives == null) {
			this.subtitleArchives = new HashSet<SubtitleArchiveEntry>();
		}
		for(SubtitleArchiveEntry sE : subtitles) {
			if(this.subtitleArchives.contains(sE)) {
				this.subtitleArchives.remove(sE);
				
			} 
			this.subtitleArchives.add(sE);
		}
	}

	public boolean hasSubtitles() {
		return subtitleArchives != null && !subtitleArchives.isEmpty();
	}

	@JsonIgnore
	public String getFileName() {
		return pathToFile.getFileName().toString().substring(0, pathToFile.getFileName().toString().lastIndexOf("."));
	}

	@Override
	public String toString() {
		return new StringBuilder().append("Original file name: ").append(getFileName()).append("\n")
				.append(getParsedFileName().toString()).append("\n").append(new Date(pathToFile.toFile().lastModified()).toString()).toString();
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

	public void setPathToFile(Path location) {
		this.pathToFile = location;
		acceptableFileName = null;
	}
}
