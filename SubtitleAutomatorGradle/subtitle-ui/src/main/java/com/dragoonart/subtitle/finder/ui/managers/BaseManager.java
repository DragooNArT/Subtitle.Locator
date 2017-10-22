package com.dragoonart.subtitle.finder.ui.managers;

import javafx.stage.DirectoryChooser;

public abstract class BaseManager {

	private DirectoryChooser dirChooser;

	public BaseManager() {
		dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Choose Videos Directory");
	}

	public DirectoryChooser getDirChooser() {
		return dirChooser;
	}
}
