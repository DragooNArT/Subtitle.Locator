package com.dragoonart.subtitle.finder.ui.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javax.swing.event.DocumentEvent.EventType;

import com.dragoonart.subtitle.finder.SubtitleFileScanner;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.listeners.VideoSelectedListener;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.CharmListView;
import com.gluonhq.charm.glisten.control.ListTile;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;

public class MainPanelController {

	private DirectoryChooser dirChooser = new DirectoryChooser();
	private VideoSelectedListener videoSelListener;
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
	
	private SubtitleFileScanner subFscanner;

	private void processFolder(File folder) {
		if (folder == null) {
			return;
		}
		Path toFolder = Paths.get(folder.toURI());
		if (Files.exists(toFolder)) {
			PreferencesManager.INSTANCE.addLocationPath(toFolder);
			foldersList.getItems().add(0, toFolder);
			// select first entry
			foldersList.getSelectionModel().select(0);
			loadFolderVideos(toFolder);

		}
	}

	private void loadFolderVideos(Path toFolder) {
		subFscanner = new SubtitleFileScanner(toFolder);
		ObservableList<VideoEntry> list = FXCollections.observableArrayList(subFscanner.getFolderVideos());
		videosList.setItems(list);
		videoSelListener = new VideoSelectedListener(this);
		videosList.selectedItemProperty().addListener(videoSelListener);
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
		loadLocations();
		
		// select first entry
		foldersList.getSelectionModel().select(0);
		// load videos for that entry
		loadFolderVideos(foldersList.getSelectionModel().getSelectedItem());
		listCell();

	}

	private void listCell() {
		videosList.setCellFactory(p -> new CharmListCell<VideoEntry>() {
			@Override
			public void updateItem(VideoEntry item, boolean empty) {
				super.updateItem(item, empty);

				if (item != null && !empty) {
					ListTile tile = new ListTile();
					if (item.getFileName().equals(item.getAcceptableFileName())) {
						tile.textProperty().addAll("Name: " + item.getFileName());
					} else {
						tile.textProperty().addAll("Name: " + item.getAcceptableFileName() + "/" + item.getFileName());
					}
					// final Image image = USStates.getImage(item.getFlag());
					// if(image!=null){
					// tile.setPrimaryGraphic(new ImageView(image));
					// }
					setText(null);
					setGraphic(tile);
				} else {
					setText(null);
					setGraphic(null);
				}
			}
		});
	}

	private void loadLocations() {
		for (Path locPath : PreferencesManager.INSTANCE.getLocationPaths()) {
			if (Files.exists(locPath)) {
				foldersList.getItems().add(locPath);
			} else {
				try {
					PreferencesManager.INSTANCE.removeLocationPath(locPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
