package com.dragoonart.subtitle.finder.beans;
import java.nio.file.Path;
import java.util.List;

public class VideoEntry {

	private Path pathToFile;

	private List<SubtitleEntry> subtitles;

	private boolean subtitlesFound = false;

	public VideoEntry(Path pathToFile) {
		this.pathToFile = pathToFile;
	}

	public boolean isSubtitlesFound() {
		return subtitlesFound;
	}

	public void setSubtitlesFound(boolean subtitlesFound) {
		this.subtitlesFound = subtitlesFound;
	}

	public Path getPathToFile() {
		return pathToFile;
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
	
	public List<SubtitleEntry> getSubtitles() {
		return subtitles;
	}

	public void setSubtitles(List<SubtitleEntry> subtitles) {
		this.subtitles = subtitles;
	}
	
	public boolean isProccessedForSubtitles() {
		return subtitles != null;
	}

	public String getFileName() {
		return pathToFile.getFileName().toString();
	}

	@Override
	public String toString() {
		return pathToFile.getFileName().toString() + " subtitlesFound: " + subtitlesFound;
	}
}
