package com.dragoonart.subtitle.finder.ui.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.dragoonart.subtitle.finder.ui.ResourceManager;
import com.dragoonart.subtitle.finder.ui.controllers.ChooseLocationController;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChooseLocationManager extends BaseManager {

	private ChooseLocationController locController;
	
	public ChooseLocationManager(ChooseLocationController chooseLocController) {
		this.locController = chooseLocController;
	}
	
	public void processFolder(File folder) {
		if (folder == null) {
			return;
		}
		
		Path toFolder = Paths.get(folder.toURI());
		if (Files.exists(toFolder)) {
			PreferencesManager.INSTANCE.addLocationPath(toFolder);
			displayMainPanel();
		}
	}

	public void displayMainPanel() {
		try {
			Scene scene = new Scene(ResourceManager.getScene("MainPanel.fxml"),1280,800);
			scene.getStylesheets().add(ResourceManager.getResource("application.css").toExternalForm());
			((Stage)locController.getChoosePane().getScene().getWindow()).setScene(scene);
		} catch (IOException e) {
			System.out.println("unable to go to main panel");
			e.printStackTrace();
		}
	}
}
