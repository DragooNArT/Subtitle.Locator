package com.dragoonart.subtitle.finder.ui.controllers;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.listeners.SubtitleSelectedListener;
import com.dragoonart.subtitle.finder.ui.listeners.VideoSelectedListener;
import com.dragoonart.subtitle.finder.ui.managers.MainPanelManager;
import com.jfoenix.controls.JFXButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
	private CheckBox showAllbtn;

	public CheckBox getShowAllbtn() {
		return showAllbtn;
	}

	@FXML
	private ListView<VideoEntry> videosList;

	@FXML
	private Text movieName;

	@FXML
	private Text Year;

	@FXML
	private Text Resolution;

	@FXML
	private Text Release;

	@FXML
	private Button searchBtn;

	@FXML
	private ListView<Path> subtitlesList;

	public VideoSelectedListener getVideoSelListener() {
		return videoSelListener;
	}

	public MainPanelManager getManager() {
		return manager;
	}

	public ListView<VideoEntry> getVideosList() {
		return videosList;
	}

	public ListView<Path> getSubtitlesList() {
		return subtitlesList;
	}

	public ChoiceBox<Path> getFoldersList() {
		return foldersList;
	}

	public Text getMovieNameField() {
		return movieName;
	}

	@FXML
	private ImageView showImage;

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
			manager.observeFolderVideos(manager.getDirChooser().showDialog(addFolder.getScene().getWindow()));
		}
	}

	private void addVideosFolderListener() {
		foldersList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Path>() {
			@Override
			public void changed(ObservableValue<? extends Path> observable, Path oldValue, Path newValue) {
				manager.observeFolderVideos(newValue.toFile());
			}

		});
	}

	@FXML
	void filterVideos(ActionEvent event) {
		if (event.getSource() == showAllbtn) {
			manager.filterVideos(showAllbtn.isSelected());
		}

	}

	public ImageView getShowImage() {
		return showImage;
	}

	@FXML
	void searchForSubs(ActionEvent event) {
		if (event.getSource() == searchBtn) {
			new Thread(() -> manager.loadSubsForVideo(videosList.getSelectionModel().getSelectedItem()),
					"Search for '" + videosList.getSelectionModel().getSelectedItem().getAcceptableFileName() + "'").start();
		}
	}

	@FXML
	void initialize() {

		assert foldersList != null : "fx:id=\"foldersList\" was not injected: check your FXML file 'MainPanel.fxml'.";
		manager = new MainPanelManager(this);
		manager.loadLocations();
		videoSelListener = new VideoSelectedListener(manager);
		// select first entry
		foldersList.getSelectionModel().select(0);
		// load videos for that entry
		manager.loadFolderVideos(foldersList.getSelectionModel().getSelectedItem());

		manager.initVideosListCell();
		manager.initSubtitlesListCell();

		subListener = new SubtitleSelectedListener(manager);
		videosList.getSelectionModel().selectedItemProperty().addListener(videoSelListener);
		addVideosFolderListener();
	}

	public Button getSearchButton() {
		return searchBtn;
	}

	public EventHandler<? super MouseEvent> getSubSelectedListener() {
		return subListener;
	}
}
