package com.dragoonart.subtitle.finder.ui.listeners;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.dragoonart.subtitle.finder.beans.VideoEntry;
import com.dragoonart.subtitle.finder.ui.managers.MainPanelManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class VideoSelectedListener implements ChangeListener<VideoEntry>, EventHandler<MouseEvent> {
	MainPanelManager panelManager;

	public VideoSelectedListener(MainPanelManager panelManager) {
		this.panelManager = panelManager;
	}

	@Override
	public void changed(ObservableValue<? extends VideoEntry> observable, VideoEntry oldValue, VideoEntry newValue) {
		panelManager.loadVideoMeta(observable.getValue());
		if (observable.getValue().hasSubtitles()) {
			panelManager.loadSubtitles(observable.getValue().getSubtitles());
		} else {
			panelManager.getController().getSubtitlesList().setItems(FXCollections.emptyObservableList());
		}

	}

	@Override
	public void handle(MouseEvent event) {
		if (event.getClickCount() > 1) {
			try {
				Process process = new ProcessBuilder(panelManager.getSelectedVideo().toAbsolutePath().toString())
						.start();
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;

				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
