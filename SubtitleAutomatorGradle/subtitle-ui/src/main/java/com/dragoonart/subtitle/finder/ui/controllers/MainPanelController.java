package com.dragoonart.subtitle.finder.ui.controllers;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.listeners.SubtitleSelectedListener;
import com.dragoonart.subtitle.finder.ui.listeners.VideoSelectedListener;
import com.dragoonart.subtitle.finder.ui.managers.MainPanelManager;
import com.gluonhq.charm.glisten.control.CharmListView;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Text;

public class MainPanelController {

	private VideoSelectedListener videoSelListener;
	private SubtitleSelectedListener subListener;
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

	@FXML
	private Text movieName;

	@FXML
	private Text Year;

	@FXML
	private Text Resolution;

	@FXML
	private Text Release;

	@FXML
	private CharmListView<Path, ?> subtitlesList;

	public CharmListView<VideoEntry, ?> getVideosList() {
		return videosList;
	}

	public CharmListView<Path, ?> getSubtitlesList() {
		return subtitlesList;
	}

	public ChoiceBox<Path> getFoldersList() {
		return foldersList;
	}

	public Text getMovieNameField() {
		return movieName;
	}

	public Text getYearField() {
		return Year;
	}

	public Text getResolutionField() {
		return Resolution;
	}

	public Text getReleaseField() {
		return Release;
	}

	@FXML
	void addFolderLocation(ActionEvent event) {
		if (event.getSource() == addFolder) {
			manager.loadFolderVideos(manager.getDirChooser().showDialog(addFolder.getScene().getWindow()));
		}
	}

	@FXML
	void initialize() {
		assert foldersList != null : "fx:id=\"foldersList\" was not injected: check your FXML file 'MainPanel.fxml'.";
		manager = new MainPanelManager(this);
		manager.loadLocations();

		// select first entry
		foldersList.getSelectionModel().select(0);
		// load videos for that entry
		manager.loadFolderVideos(foldersList.getSelectionModel().getSelectedItem());
		manager.initVideosListCell();
		manager.initSubtitlesListCell();
		videoSelListener = new VideoSelectedListener(manager);
		subListener = new SubtitleSelectedListener(manager);
		subtitlesList.selectedItemProperty().addListener(subListener);
		videosList.selectedItemProperty().addListener(videoSelListener);

	}

}
