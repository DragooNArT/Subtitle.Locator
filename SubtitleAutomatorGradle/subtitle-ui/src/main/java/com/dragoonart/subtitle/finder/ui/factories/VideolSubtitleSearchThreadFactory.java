package com.dragoonart.subtitle.finder.ui.factories;

import java.util.concurrent.ThreadFactory;

public class VideolSubtitleSearchThreadFactory implements ThreadFactory {
	private String folder;

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, "Scheduled subs lookup threadFactory for '" + folder + "'");
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

}
