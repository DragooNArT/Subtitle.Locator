package com.dragoonart.subtitle.finder.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.ui.ResourceManager;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ChooseLocationController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private Pane choosePane;

	private DirectoryChooser dirChooser = new DirectoryChooser();

	@FXML
	void initialize() {
		assert choosePane != null : "fx:id=\"choosePane\" was not injected: check your FXML file 'ChooseLocation.fxml'.";
		dirChooser.setTitle("Choose Videos Directory");
	}

	private void processFolder(File folder) {
		if (folder == null) {
			return;
		}
		Path toFolder = Paths.get(folder.toURI());
		if (Files.exists(toFolder)) {
			PreferencesManager.INSTANCE.addLocationPath(toFolder);
			toMainPanel();
		}
	}

	private void toMainPanel() {
		try {
			Scene scene = new Scene(ResourceManager.getScene("MainPanel.fxml"),1280,800);
			scene.getStylesheets().add(ResourceManager.getResource("application.css").toExternalForm());
			((Stage)choosePane.getScene().getWindow()).setScene(scene);
		} catch (IOException e) {
			System.out.println("unable to go to main panel");
			e.printStackTrace();
		}
	}

	@FXML
	void browseDirs(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
			processFolder(dirChooser.showDialog(choosePane.getScene().getWindow()));
		}
	}
}
