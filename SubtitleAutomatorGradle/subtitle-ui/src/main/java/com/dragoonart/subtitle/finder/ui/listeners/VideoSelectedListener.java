package com.dragoonart.subtitle.finder.ui.listeners;

import java.awt.Desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.managers.MainPanelManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;

public class VideoSelectedListener implements ChangeListener<VideoEntry>, EventHandler<MouseEvent> {
	private static final Logger logger = LoggerFactory.getLogger(VideoSelectedListener.class);
	MainPanelManager panelManager;

	public VideoSelectedListener(MainPanelManager panelManager) {
		this.panelManager = panelManager;
	}

	@Override
	public void changed(ObservableValue<? extends VideoEntry> observable, VideoEntry oldValue, VideoEntry newValue) {
		panelManager.loadVideoMeta(observable.getValue());
		if (observable.getValue().hasSubtitles()) {
			panelManager.loadSubtitles(observable.getValue().getSubtitleArchives());
		} else {
			panelManager.getController().getSubtitlesList().setItems(FXCollections.emptyObservableList());
		}

	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getClickCount() > 1) {
			try {
				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					desktop.open(panelManager.getSelectedVideo().toAbsolutePath().toFile());
				} else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Warning");
					alert.setHeaderText("Video starting not supported");
					alert.setContentText("Opening videos is not supported on this Environment");
					alert.showAndWait();
				}
			} catch (Exception e) {
				logger.warn("Failed while opening: " + panelManager.getSelectedVideo().toAbsolutePath().toFile(), e);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Warning");
				alert.setHeaderText("Video starting failed");
				alert.setContentText("Opening this video failed, check logs.");
				alert.showAndWait();
			}
		}
	}

}
