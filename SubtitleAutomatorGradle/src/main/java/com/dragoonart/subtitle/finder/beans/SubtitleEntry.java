package com.dragoonart.subtitle.finder.beans;
import java.nio.file.Path;

public class SubtitleEntry {

	private Path pathToSubtitle;
	
	
	public SubtitleEntry(Path pathToSubtitle) {
		this.pathToSubtitle = pathToSubtitle;
	}
	
	
	public Path getSubtitlePath() {
		return pathToSubtitle;
	}
	
	public String getSubtitleName() {
		return pathToSubtitle.getFileName().toString();
	}
}
