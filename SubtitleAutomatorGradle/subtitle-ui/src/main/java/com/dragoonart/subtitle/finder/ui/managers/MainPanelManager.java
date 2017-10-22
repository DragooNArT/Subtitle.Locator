package com.dragoonart.subtitle.finder.ui.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.dragoonart.subtitle.finder.SubtitleFileScanner;
import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.controllers.MainPanelController;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;
import com.dragoonart.subtitle.finder.web.SubtitleFinder;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainPanelManager extends BaseManager {
	private MainPanelController panelCtrl;

	private SubtitleFinder subFinder = new SubtitleFinder();

	public MainPanelManager(MainPanelController mainPanelController) {
		this.panelCtrl = mainPanelController;
	}

	private SubtitleFileScanner subFscanner;

	public MainPanelController getController() {
		return panelCtrl;
	}

	public void loadSubtitles(Set<SubtitleArchiveEntry> subtitles) {
		List<Path> result = new ArrayList<>();
		for(SubtitleArchiveEntry archE : subtitles) {
			result.addAll(archE.getSubtitleEntries().values());
		}
		panelCtrl.getSubtitlesList().setItems(FXCollections.observableArrayList(result));
	}

	public void setNoSubtitles(VideoEntry value) {
		// TODO Auto-generated method stub

	}

	private void setRootFolder(Path toFolder) {
		if (subFscanner == null) {
			subFscanner = new SubtitleFileScanner(toFolder);
		} else {
			subFscanner.setScanFolder(toFolder);
		}
	}

	public void loadLocations() {
		for (Path locPath : PreferencesManager.INSTANCE.getLocationPaths()) {
			if (Files.exists(locPath)) {
				panelCtrl.getFoldersList().getItems().add(locPath);
			} else {
				try {
					PreferencesManager.INSTANCE.removeLocationPath(locPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void loadFolderVideos(Path toFolder) {

		setRootFolder(toFolder);
		Set<VideoEntry> veSet = subFscanner.getFolderVideos();
		ObservableList<VideoEntry> list = FXCollections.observableArrayList();
		list.sort( (VideoEntry p1, VideoEntry p2) -> p1.compareTo(p2));
		for (VideoEntry entry : veSet) {
			new Thread(() -> {
				subFinder.lookupEverywhere(entry);
				Platform.runLater(() -> { list.add(entry); list.sort( (VideoEntry p1, VideoEntry p2) -> p1.compareTo(p2));});
				Platform.runLater(() -> panelCtrl.getVideosList().setItems(list));
			}).start();
		}
	}

	public void loadFolderVideos(File folder) {
		if (folder == null) {
			return;
		}
		Path toFolder = Paths.get(folder.toURI());
		if (Files.exists(toFolder)) {
			PreferencesManager.INSTANCE.addLocationPath(toFolder);
			panelCtrl.getFoldersList().getItems().add(0, toFolder);
			// select first entry
			panelCtrl.getFoldersList().getSelectionModel().select(0);
			loadFolderVideos(toFolder);

		}
	}

	public void initVideosListCell() {
		panelCtrl.getVideosList().setCellFactory(p -> new CharmListCell<VideoEntry>() {
			@Override
			public void updateItem(VideoEntry ve, boolean empty) {
				super.updateItem(ve, empty);
				loadVideoTIle(ve);
			}

			private void loadVideoTIle(VideoEntry ve) {
				ListTile tile = new ListTile();
				if (ve.getSubtitles().isEmpty()) {
					tile.setStyle("-fx-background-color: #f9f7f7;");
				} else {
					tile.setStyle("-fx-background-color: #eff9ef;");
				}
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String dateAdded = sdf.format(ve.getPathToFile().toFile().lastModified());
				if (ve.getFileName().equals(ve.getAcceptableFileName())) {
					tile.textProperty().add(ve.getFileName() + " "  + dateAdded);
				} else {
					tile.textProperty().add(ve.getAcceptableFileName() + "/" + ve.getFileName()  + " "  + dateAdded);
				}
				
				// final Image image = USStates.getImage(item.getFlag());
				// if(image!=null){
				// tile.setPrimaryGraphic(new ImageView(image));
				// }
				setText(null);
				setGraphic(tile);

			}
		});
	}
	
	public void initSubtitlesListCell() {
		panelCtrl.getSubtitlesList().setCellFactory(p -> new CharmListCell<Path>() {
			@Override
			public void updateItem(Path ve, boolean empty) {
				super.updateItem(ve, empty);
				loadVideoTIle(ve);
			}

			private void loadVideoTIle(Path ve) {
				ListTile tile = new ListTile();
			
				tile.textProperty().add(ve.getFileName().toString());
		

				setText(null);
				setGraphic(tile);

			}
		});
	}
	
	private void cleanVideoMeta() {
		panelCtrl.getMovieNameField().setText("Show: ");
		panelCtrl.getResolutionField().setText("Resolution: ");
		panelCtrl.getYearField().setText("");
		panelCtrl.getReleaseField().setText("");
	}

	public void loadVideoMeta(VideoEntry value) {
		cleanVideoMeta();
		ParsedFileName pfn = value.getParsedFilename();
		
		if (pfn.hasShowName()) {
			panelCtrl.getMovieNameField().setText("Show: " + pfn.getShowName() + " S" + pfn.getSeason() + "E" + pfn.getEpisode());
		} else {
			panelCtrl.getMovieNameField().setText("Show: " + pfn.getShowName());
		}
		if(pfn.hasResolution()) {
			panelCtrl.getResolutionField().setText("Resolution: " + pfn.getResolution());
		}
		if(pfn.hasRelease()) {
			panelCtrl.getReleaseField().setText("Release: " + pfn.getRelease());
		}
	}
}
