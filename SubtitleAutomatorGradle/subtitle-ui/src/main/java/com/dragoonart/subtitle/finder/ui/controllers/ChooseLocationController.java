package com.dragoonart.subtitle.finder.ui.controllers;

import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.ui.managers.ChooseLocationManager;

import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;

public class ChooseLocationController {

	@FXML
	private ResourceBundle resources;

	@FXML
	private Pane choosePane;
	
	private ChooseLocationManager locManager;

	@FXML
	void initialize() {
		assert choosePane != null : "fx:id=\"choosePane\" was not injected: check your FXML file 'ChooseLocation.fxml'.";
		locManager = new ChooseLocationManager(this);
	}

	@FXML
	void browseDirs(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
			locManager.processFolder(locManager.getDirChooser().showDialog(choosePane.getScene().getWindow()));
		}
	}
	
	public Pane getChoosePane() {
		return choosePane;
	}
}
