package com.dragoonart.subtitle.finder.ui.managers;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.SubtitleFileScanner;
import com.dragoonart.subtitle.finder.SubtitleFileUtils;
import com.dragoonart.subtitle.finder.VideoState;
import com.dragoonart.subtitle.finder.beans.ParsedFileName;
import com.dragoonart.subtitle.finder.beans.SubtitleArchiveEntry;
import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.beans.videometa.VideoMetaBean;
import com.dragoonart.subtitle.finder.cache.VideoMetaCachedProvider;
import com.dragoonart.subtitle.finder.cache.VideoPosterCachedProvider;
import com.dragoonart.subtitle.finder.cache.VideoEntryCache;
import com.dragoonart.subtitle.finder.ui.StartUI;
import com.dragoonart.subtitle.finder.ui.controllers.MainPanelController;
import com.dragoonart.subtitle.finder.ui.factories.FolderSearchThreadFactory;
import com.dragoonart.subtitle.finder.ui.factories.VideolSubtitleSearchThreadFactory;
import com.dragoonart.subtitle.finder.ui.usersettings.PreferencesManager;
import com.dragoonart.subtitle.finder.web.SubtitleFinder;
import com.dragoonart.subtitle.finder.web.SubtitleFinderAllocator;
import com.gluonhq.charm.glisten.control.CharmListCell;
import com.gluonhq.charm.glisten.control.ListTile;
import com.gluonhq.charm.glisten.control.ProgressIndicator;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BackgroundFill;
import javafx.util.Callback;
import stormpot.BlazePool;
import stormpot.Config;
import stormpot.Pool;
import stormpot.PoolException;
import stormpot.Timeout;

public class MainPanelManager extends BaseManager {
	private static final Logger logger = LoggerFactory.getLogger(MainPanelManager.class);
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
		panelCtrl.getSubtitlesList().refresh();
		// initSubtitlesListCell();
	}

	public Path getSelectedVideo() {
		return panelCtrl.getVideosList().getSelectionModel().getSelectedItem().getPathToFile();
	}

	private void setRootFolder(Path toFolder) {
		searchThreadFactory.setFolder(toFolder.toString());
		subSearchThreadFactory.setFolder(toFolder.toString());
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

	private FolderSearchThreadFactory searchThreadFactory = new FolderSearchThreadFactory();
	private VideolSubtitleSearchThreadFactory subSearchThreadFactory = new VideolSubtitleSearchThreadFactory();
	private ScheduledThreadPoolExecutor searchFilesPool = new ScheduledThreadPoolExecutor(1, searchThreadFactory);
	private ScheduledThreadPoolExecutor searchSubsForFilesPool = new ScheduledThreadPoolExecutor(1,
			subSearchThreadFactory);
	private Set<VideoEntry> veSet = new LinkedHashSet<VideoEntry>();

	public void loadFolderVideos(Path toFolder) {
		// reset videos
		veSet.clear();
		setRootFolder(toFolder);
		searchFilesPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("scanning folder: " + toFolder);
					scanFolderForVideos();
				} catch (Exception e) {
					logger.error("Main scanning exec failed!!!: ", e);
				}

			}

			@Override
			public String toString() {
				return toFolder.toString();
			}
		}, 0, 60, TimeUnit.SECONDS);

		searchSubsForFilesPool.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("scanning videos for subs from folder: " + toFolder);
					synchronized (veSet) {
						scanVideosWithoutSubs(veSet);
					}
				} catch (Exception e) {
					logger.error("Main scanning exec failed!!!: ", e);
				}

			}

			@Override
			public String toString() {
				return toFolder.toString();
			}
		}, 0, 60, TimeUnit.MINUTES);
	}

	private SortedSet<VideoEntry> subtitlessVideos;
	private SortedSet<VideoEntry> subtitledVideos;

	private void scanFolderForVideos() {
		synchronized (veSet) {
			boolean lookupAllVideosFirst = veSet.isEmpty();
			subtitlessVideos = subFscanner.getFolderVideos();
			subtitledVideos = subFscanner.getFolderSubtitledVideos();
			// add only the new entries
			LinkedHashSet<VideoEntry> newSubtitless = subtitlessVideos.stream().filter(e -> !veSet.contains(e))
					.collect(Collectors.toCollection(LinkedHashSet::new));
			veSet.addAll(newSubtitless);
			LinkedHashSet<VideoEntry> newSubtitled = subtitledVideos.stream().filter(e -> !veSet.contains(e))
					.collect(Collectors.toCollection(LinkedHashSet::new));
			veSet.addAll(newSubtitled);

			// leave only the entries which really have existing videos
			veSet = veSet.stream().filter(e -> Files.exists(e.getPathToFile()))
					.collect(Collectors.toCollection(LinkedHashSet::new));
			ObservableList<VideoEntry> list = panelCtrl.getVideosList().itemsProperty().get();

			updateVideosList(veSet, list);

			// lookup for all videos only once
			if (lookupAllVideosFirst) {
				scanVideosWithoutSubs(veSet);
			} else {
				scanVideosWithoutSubs(newSubtitless);
				scanVideosWithoutSubs(newSubtitled);
			}
		}
	}

	private void scanVideosWithoutSubs(Set<VideoEntry> videoEntries) {
		for (VideoEntry entry : videoEntries) {
			if (!SubtitleFileUtils.hasSubs(entry.getPathToFile()) || !entry.hasSubtitles()) {
				new Thread(() -> {
					if (entry.getState() != VideoState.FINISHED) {
						// look for subs if neccessary
						if (entry.getState() == VideoState.PENDING) {
							loadSubsForVideo(entry);
						}
					}
					if (subFscanner.autoApplySubtitles(entry)) {
						addNotificationForVideo(entry);
						Platform.runLater(() -> {
							getController().getVideosList().getSelectionModel().select(entry);
							getController().getVideosList().refresh();
						});
					}
				}, "Search for '" + entry.getAcceptableFileName() + "'").start();

			}
		}
	}

//	private void loadZipArchives(Set<VideoEntry> tempAll) {
//		for (VideoEntry entry : tempAll) {
//			Set<SubtitleArchiveEntry> result = new HashSet<>();
//
//			try {
//				Files.list(SubtitleFileUtils.getArchivesDir(entry.getParsedFileName())).forEach(e -> {
//					if (isArchive(e)) {
//						SubtitleArchiveEntry archEntry = new SubtitleArchiveEntry(e);
//						result.add(archEntry);
//					}
//
//				});
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//	}

	private boolean isArchive(Path e) {
		return e.toString().endsWith(".7z") || e.toString().endsWith(".rar") || e.toString().endsWith(".zip");
	}

//	private void updateVideosList(VideoEntry entry, ObservableList<VideoEntry> list) {
//		Platform.runLater(() -> {
//			list.add(entry);
//			// list.sort((VideoEntry p1, VideoEntry p2) -> p1.compareTo(p2));
//			// panelCtrl.getVideosList().setItems(list);
//		});
//	}

	private void updateVideosList(Set<VideoEntry> entries, ObservableList<VideoEntry> list) {
		Platform.runLater(() -> {
			VideoEntry selectedItem = panelCtrl.getVideosList().getSelectionModel().getSelectedItem();
			list.clear();
			list.addAll(panelCtrl.getShowAllbtn().isSelected() ? entries
					: entries.stream().filter(e -> !SubtitleFileUtils.hasSubs(e.getPathToFile()))
							.collect(Collectors.toCollection(LinkedHashSet::new)));
			panelCtrl.getVideosList().setItems(list);
			if (selectedItem != null) {
				Optional<VideoEntry> toSelect = list.stream()
						.filter(ve -> selectedItem.getFileName().equals(ve.getFileName())).findFirst();
				toSelect.ifPresent(e -> panelCtrl.getVideosList().getSelectionModel().select(e));

			}
		});
	}

	public void filterVideos(boolean showAll) {

		Platform.runLater(() -> {
			ObservableList<VideoEntry> list = panelCtrl.getVideosList().itemsProperty().get();
			list.clear();
			list.addAll(showAll ? veSet
					: veSet.stream().filter(e -> subtitlessVideos.contains(e))
							.collect(Collectors.toCollection(LinkedHashSet::new)));
			panelCtrl.getVideosList().setItems(list);
			// initVideosListCell();
			panelCtrl.getVideosList().scrollTo(showAll ? veSet.size() - (subtitledVideos.size() + 1) : 0);
		});
	}

	private void addNotificationForVideo(VideoEntry entry) {
		if (!StartUI.isUiVisible() && entry.hasSubtitles()) {
			Platform.runLater(() -> StartUI.trayIcon.displayMessage(entry.getFileName(),
					"Found " + entry.getSubtitleArchives().size() + " subtitles", MessageType.INFO));
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

		panelCtrl.getVideosList().setCellFactory(new Callback<ListView<VideoEntry>, ListCell<VideoEntry>>() {

			@Override
			public ListCell<VideoEntry> call(ListView<VideoEntry> param) {
				ListCell<VideoEntry> listCell = new ListCell<VideoEntry>() {
					@Override
					protected void updateItem(VideoEntry item, boolean empty) {
						if (!empty) {
							loadVideoTIle(item);
						}
						super.updateItem(item, empty);
					}

					private void loadVideoTIle(VideoEntry ve) {
						ListTile tile = new ListTile();
						tile.setOnMouseClicked(panelCtrl.getVideoSelListener());
						tile.setStyle(SubtitleFileUtils.hasSubs(ve.getPathToFile()) ? "-fx-background-color: #eff9ef;"
								: ve.hasSubtitles() ? "-fx-background-color: #ffff00;"
										: "-fx-background-color: #f9f7f7;");
						tile.textProperty().add(ve.getAcceptableFileName());
						if (ve.getState() == VideoState.LOADING) {
							tile.textProperty().add("Fetching..");
						}
						// setText(null);

						setGraphic(tile);

					}
				};

				return listCell;
			}
		});
	}

	public void initSubtitlesListCell() {
		panelCtrl.getSubtitlesList().setCellFactory(p -> new CharmListCell<Path>() {
			@Override
			public void updateItem(Path ve, boolean empty) {
				super.updateItem(ve, empty);
				if (!empty) {
					loadVideoTIle(ve);
				}
			}

			private void loadVideoTIle(Path ve) {
				ListTile tile = new ListTile();
				tile.setOnMouseClicked(panelCtrl.getSubSelectedListener());
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
		ParsedFileName pfn = value.getParsedFileName();
		panelCtrl.getSearchButton().setVisible(true);
		panelCtrl.getMovieNameField().setVisible(true);
		if (pfn.isEpisodic()) {
			panelCtrl.getMovieNameField().setText(pfn.getShowName() + " S" + pfn.getSeason() + "E" + pfn.getEpisode());
		} else {
			panelCtrl.getMovieNameField().setText(pfn.getShowName());
		}
		if (pfn.hasResolution()) {
			panelCtrl.getResolutionField().setVisible(true);
			panelCtrl.getResolutionField().setText("Resolution: " + pfn.getResolution());
		}
		if (pfn.hasYear()) {
			panelCtrl.getYearField().setVisible(true);
			panelCtrl.getYearField().setText("Year: " + pfn.getYear());
		}
		if (pfn.hasRelease()) {
			panelCtrl.getReleaseField().setVisible(true);
			panelCtrl.getReleaseField().setText("Release: " + pfn.getRelease());
		}
		VideoMetaBean vmb = VideoMetaCachedProvider.INSTANCE.getMovieData(value);
		loadMovieImage(vmb);
	}

	private void loadMovieImage(VideoMetaBean vmb) {
		if (vmb != null && vmb != VideoMetaBean.NOT_FOUND && vmb.getPoster() != null) {
			new Thread(() -> {
				try {
					final Image img = new Image(VideoPosterCachedProvider.INSTANCE.getLocalImagePath(vmb.getPoster()),
							600, 882, false, true);
					Platform.runLater(() -> panelCtrl.getShowImage().setImage(img));
				} catch (Exception e) {
					final Image img = new Image(vmb.getPoster(), 600, 882, false, true);
					Platform.runLater(() -> panelCtrl.getShowImage().setImage(img));
				}
			}).start();
		} else {
			final Image img = new Image(getClass().getResourceAsStream("../popcornImage.jpg"), 600, 882, false, true);
			Platform.runLater(() -> panelCtrl.getShowImage().setImage(img));
		}
	}

	public void loadSubsForVideo(VideoEntry entry) {
		if (entry == null) {
			return;
		}
		SubtitleFinder finder = null;
		try {
			finder = pool.claim(timeout);
			if (finder != null) {
				entry.setState(VideoState.LOADING);
				Platform.runLater(() -> getController().getVideosList().refresh());
				int subsNow = entry.getSubtitleArchives() != null ? entry.getSubtitleArchives().size() : 0;

				finder.lookupEverywhere(entry);
				int subsAfter = entry.getSubtitleArchives() != null ? entry.getSubtitleArchives().size() : 0;

				if (subsAfter > 0 && subsAfter >= subsNow) {
					entry.setState(VideoState.FINISHED);

				} else {
					entry.setState(VideoState.PENDING);
				}

				VideoEntryCache.getInsance().addCacheEntry(entry);
				Platform.runLater(() -> {
					getController().getVideosList().refresh();
					// initVideosListCell();
				});
			} else {
				logger.warn("Timed out waiting for SubtitleFinder object " + entry.toString());
			}
			// Do stuff with 'object'.
			// Note: 'claim' returns 'null' if it times out.
		} catch (PoolException | InterruptedException e1) {
			e1.printStackTrace();
		} finally {
			if (finder != null) {
				finder.release();
			}
		}
	}
}
