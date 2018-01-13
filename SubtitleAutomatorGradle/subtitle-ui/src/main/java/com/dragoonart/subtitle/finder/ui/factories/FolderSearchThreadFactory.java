package com.dragoonart.subtitle.finder.ui.factories;

import java.util.concurrent.ThreadFactory;

public class FolderSearchThreadFactory implements ThreadFactory {
	private String folder;
	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, "Scanner for '"+folder+"'");
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}
	
}
