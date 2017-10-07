package com.dragoonart.subtitle.finder.ui.controllers;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;

public class MainPanelController {

	private DirectoryChooser dirChooser = new DirectoryChooser();

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ChoiceBox<Path> foldersList;

	@FXML
	private Button addFolder;

	private void processFolder(File folder) {
		if (folder == null) {
			return;
		}
		Path toFolder = Paths.get(folder.toURI());
		if (Files.exists(toFolder)) {
			PreferencesManager.INSTANCE.addLocationPath(toFolder);
			foldersList.getItems().add(0,toFolder);
			//select first entry
			foldersList.getSelectionModel().select(0);
		}
	}

	@FXML
	void addFolderLocation(ActionEvent event) {
		if (event.getSource() == addFolder) {
			processFolder(dirChooser.showDialog(addFolder.getScene().getWindow()));
		}
	}

	@FXML
	void initialize() {
		assert foldersList != null : "fx:id=\"foldersList\" was not injected: check your FXML file 'MainPanel.fxml'.";
		dirChooser.setTitle("Choose Videos Directory");
		for (Path locPath : PreferencesManager.INSTANCE.getLocationPaths()) {
			foldersList.getItems().add(locPath);
		}
		//select first entry
		foldersList.getSelectionModel().select(0);
	}

}
