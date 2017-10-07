package com.dragoonart.subtitle.finder.ui;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ResourceManager {

	
	
	
	public static Parent getScene(String sceneName) throws IOException {
		return FXMLLoader.load(ResourceManager.class.getResource(sceneName));
	}
	public static URL getResource(String resName) throws IOException {
		return ResourceManager.class.getResource(resName);
	}
}
