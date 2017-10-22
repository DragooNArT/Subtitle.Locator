package com.dragoonart.subtitle.finder.ui.controllers;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.listeners.VideoSelectedListener;
import com.gluonhq.charm.glisten.control.CharmListView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.DirectoryChooser;

public class MainPanelController {

	private DirectoryChooser dirChooser = new DirectoryChooser();

	private VideoSelectedListener videoSelListener;

	private MainPanelManager manager;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ChoiceBox<Path> foldersList;

	@FXML
	private Button addFolder;

	@FXML
	private CharmListView<VideoEntry, ?> videosList;

	public CharmListView<VideoEntry, ?> getVideosList() {
		return videosList;
	}

	public ChoiceBox<Path> getFoldersList() {
		return foldersList;
	}

	@FXML
	void addFolderLocation(ActionEvent event) {
		if (event.getSource() == addFolder) {
			manager.loadFolderVideos(dirChooser.showDialog(addFolder.getScene().getWindow()));
		}
	}

	@FXML
	void initialize() {
		assert foldersList != null : "fx:id=\"foldersList\" was not injected: check your FXML file 'MainPanel.fxml'.";
		manager = new MainPanelManager(this);
		dirChooser.setTitle("Choose Videos Directory");
		manager.loadLocations();

		// select first entry
		foldersList.getSelectionModel().select(0);
		// load videos for that entry
		manager.loadFolderVideos(foldersList.getSelectionModel().getSelectedItem());
		manager.initVideosListCell();

		videoSelListener = new VideoSelectedListener(manager);
		videosList.selectedItemProperty().addListener(videoSelListener);

	}

	

}
