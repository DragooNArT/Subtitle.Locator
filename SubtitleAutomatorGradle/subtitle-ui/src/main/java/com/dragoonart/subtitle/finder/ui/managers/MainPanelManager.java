package com.dragoonart.subtitle.finder.ui.managers;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.dragoonart.subtitle.finder.SubtitleFileScanner;
import com.dragoonart.subtitle.finder.SubtitleFileUtils;
import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.StartUI;
import com.dragoonart.subtitle.finder.ui.controllers.MainPanelController;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;
import com.dragoonart.subtitle.finder.web.SubtitleFinder;
import com.dragoonart.subtitle.finder.web.SubtitleFinderAllocator;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;
import stormpot.PoolException;
import stormpot.Timeout;

public class MainPanelManager extends BaseManager {
	private MainPanelController panelCtrl;

	SubtitleFinderAllocator allocator = new SubtitleFinderAllocator();
	Config<SubtitleFinder> config = new Config<SubtitleFinder>().setAllocator(allocator).setSize(10);
	Pool<SubtitleFinder> pool = new BlazePool<SubtitleFinder>(config);
	Timeout timeout = new Timeout(60, TimeUnit.SECONDS);

	SubtitleFileScanner subFscanner;

	public MainPanelManager(MainPanelController mainPanelController) {
		this.panelCtrl = mainPanelController;
	}

	public MainPanelController getController() {
		return panelCtrl;
	}

	public void loadSubtitles(Set<SubtitleArchiveEntry> subtitles) {
		List<Path> result = new ArrayList<>();
		for (SubtitleArchiveEntry archE : subtitles) {
			result.addAll(archE.getSubtitleEntries().values());
		}
		panelCtrl.getSubtitlesList().setItems(FXCollections.observableArrayList(result));
	}

	public Path getSelectedVideo() {
		return panelCtrl.getVideosList().getSelectedItem().getPathToFile();
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

	private ScheduledThreadPoolExecutor stpex = new ScheduledThreadPoolExecutor(1);
	private Set<VideoEntry> veSet = new HashSet<VideoEntry>();

	public void loadFolderVideos(Path toFolder) {
		// reset videos
		veSet.clear();
		setRootFolder(toFolder);
		stpex.scheduleAtFixedRate(() -> {
			try {
				scanFolderForVideos();
			} catch (Exception e) {
				System.out.println("Main scanning exec failed!!!: ");
				e.printStackTrace();
			}
		}, 0, 60, TimeUnit.SECONDS);
	}

	private void scanFolderForVideos() {
		synchronized (veSet) {
			Set<VideoEntry> subtitlessVideos = subFscanner.getFolderVideos();
			Set<VideoEntry> subtitledVideos = subFscanner.getFolderSubtitledVideos();
			// add only the new entries
			Set<VideoEntry> tempAll = new HashSet<>();

			tempAll.addAll(subtitlessVideos.stream().filter(e -> !veSet.contains(e)).collect(Collectors.toSet()));
			tempAll.addAll(subtitledVideos.stream().filter(e -> !veSet.contains(e)).collect(Collectors.toSet()));
			veSet.addAll(tempAll);
			// leave only the entries which really have existing videos
			veSet = veSet.stream().filter(e -> Files.exists(e.getPathToFile())).collect(Collectors.toSet());
			ObservableList<VideoEntry> list = panelCtrl.getVideosList().itemsProperty().get();
			
//			list.addAll(tempAll);
			updateVideosList(tempAll, list);
			for (VideoEntry entry : veSet) {
				new Thread(() -> {
					// look for subs if neccessary
					if (!entry.hasSubtitles() && !entry.isSubtitlesProcessed()) {
						SubtitleFinder object = null;
						try {
							object = pool.claim(timeout);
							if (object != null) {
								Set<SubtitleArchiveEntry> saE = object.lookupEverywhere(entry);
								if(!saE.isEmpty()) {
									Platform.runLater(() -> getController().getVideosList().refresh());
								}
							} else {
								System.out.println("Unable to look for: " + entry.toString());
							}
							// Do stuff with 'object'.
							// Note: 'claim' returns 'null' if it times out.
						} catch (PoolException | InterruptedException e1) {
							e1.printStackTrace();
						} finally {
							if (object != null) {
								object.release();
							}
						}
					}
					// apply subs if neccessary
					if (entry.hasSubtitles() && !SubtitleFileUtils.hasSubs(entry.getPathToFile())) {
						if(subFscanner.autoApplySubtitles(entry)) {
							Platform.runLater(() -> getController().getVideosList().refresh());
							addNotificationForVideo(entry);
						}

					}
					// add entry to list if new
					if (!list.contains(entry)) {
						updateVideosList(entry, list);
					}

				}).start();
			}
		}
	}

	private void loadZipArchives(Set<VideoEntry> tempAll) {
		for (VideoEntry entry : tempAll) {
			Set<SubtitleArchiveEntry> result = new HashSet<>();

			try {
				Files.list(SubtitleFileUtils.getArchivesDir(entry.getParsedFilename())).forEach(e -> {
					if (isArchive(e)) {
						SubtitleArchiveEntry archEntry = new SubtitleArchiveEntry(e);
						result.add(archEntry);
					}

				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean isArchive(Path e) {
		return e.toString().endsWith(".7z") || e.toString().endsWith(".rar") || e.toString().endsWith(".zip");
	}

	private void updateVideosList(VideoEntry entry, ObservableList<VideoEntry> list) {
		Platform.runLater(() -> {
			list.add(entry);
			list.sort((VideoEntry p1, VideoEntry p2) -> p1.compareTo(p2));
			panelCtrl.getVideosList().setItems(list);
		});
	}

	private void updateVideosList(Set<VideoEntry> entries, ObservableList<VideoEntry> list) {
		Platform.runLater(() -> {
			list.addAll(entries);
			list.sort((VideoEntry p1, VideoEntry p2) -> p1.compareTo(p2));
			panelCtrl.getVideosList().setItems(list);
		});
	}

	private void addNotificationForVideo(VideoEntry entry) {
		if (!StartUI.isUiVisible() && entry.hasSubtitles()) {
			Platform.runLater(() -> StartUI.trayIcon.displayMessage(entry.getFileName(),
					"Found " + entry.getSubtitles().size() + " subtitles", MessageType.INFO));
		}

	}

	public void observeFolderVideos(File folder) {
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
				this.setOnMouseClicked(panelCtrl.getVideoSelListener());
			}

			private void loadVideoTIle(VideoEntry ve) {
				ListTile tile = new ListTile();
				if (!ve.hasSubtitles()) {
					tile.setStyle("-fx-background-color: #f9f7f7;");
				} else if (ve.hasSubtitles()) {
					tile.setStyle(SubtitleFileUtils.hasSubs(ve.getPathToFile()) ? "-fx-background-color: #eff9ef;"
							: "-fx-background-color: #ffff00;");
				}
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

				String dateAdded = sdf.format(ve.getPathToFile().toFile().lastModified());
				if (ve.getFileName().equals(ve.getAcceptableFileName())) {
					tile.textProperty().add(ve.getFileName() + " " + dateAdded);
				} else {
					tile.textProperty().add(ve.getAcceptableFileName() + "/" + ve.getFileName() + " " + dateAdded);
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

		if (pfn.isEpisodic()) {
			panelCtrl.getMovieNameField()
					.setText("Show: " + pfn.getShowName() + " S" + pfn.getSeason() + "E" + pfn.getEpisode());
		} else {
			panelCtrl.getMovieNameField().setText("Show: " + pfn.getShowName());
		}
		if (pfn.hasResolution()) {
			panelCtrl.getResolutionField().setText("Resolution: " + pfn.getResolution());
		}
		if (pfn.hasYear()) {
			panelCtrl.getYearField().setText("Year: " + pfn.getYear());
		}
		if (pfn.hasRelease()) {
			panelCtrl.getReleaseField().setText("Release: " + pfn.getRelease());
		}
	}
}
